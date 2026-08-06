package me.xv.holymoderation.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import me.xv.holymoderation.core.BaseService;
import me.xv.holymoderation.core.ModBuild;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.util.NotificationType;
import org.jetbrains.annotations.NotNull;

public class NetService extends BaseService {
   private final String journalApiPath = "https://journal.holyworld.me/srv/api/v1/";
   private final Gson gson = new Gson();

   public Map<String, Object> getPlayerHistory() {
      if (!ModBuild.JOURNAL_API) {
         return Collections.emptyMap();
      }
      return this.parsePlayerList("me");
   }

   public Map<String, Object> getCheckoutSessions() {
      if (!ModBuild.JOURNAL_API) {
         return Collections.emptyMap();
      }
      return this.parsePlayerList("stats");
   }

   public boolean validateApiToken(String token) {
      if (!ModBuild.JOURNAL_API) {
         return false;
      }
      HttpsURLConnection connection = null;

      try {
         connection = this.openJournalConnection(this.journalApiPath + "me", "GET", null);
         if (connection == null) {
            return false;
         }

         connection.setRequestProperty("x-token", sanitizeApiToken(token));
         connection.setRequestProperty("Content-Type", "application/json");
         String body = this.readResponseBody(connection);
         return body != null && !body.isBlank();
      } catch (IOException ignored) {
         return false;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static String sanitizeApiToken(String token) {
      if (token == null) {
         return "";
      }

      token = token.trim();
      if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
         token = token.substring(1, token.length() - 1).trim();
      }

      token = token.replaceAll("\\s+", "");
      token = token.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");
      token = token.replaceAll("§[0-9a-zA-Z]", "");
      return token;
   }

   public void submitJournalEntry(String username, String reason, String mode, int anarchyNumber, boolean isPvpAnarchy) {
      if (!ModBuild.JOURNAL_API) {
         this.journalLog("start skipped: journal api disabled player=" + username + " reason=" + reason);
         return;
      }
      JsonObject body = new JsonObject();
      body.addProperty("username", username);
      body.addProperty("reason", reason);
      body.addProperty("mode", mode);
      body.addProperty("anarchyNumber", anarchyNumber);
      body.addProperty("isPvpAnarchy", isPvpAnarchy);
      this.journalLog("start request body=" + body);

      if (this.ensureSuccess()) {
         this.journalLog("start blocked: active checkout on server");
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.ERROR,
            "§c§lОшибка",
            "У вас уже есть активная проверка.",
            5.0F
         );
         return;
      }

      HttpsURLConnection connection = null;

      try {
         connection = this.openJournalConnection("https://journal.holyworld.me/srv/api/v1/checkout/start", "POST", null);
         if (connection == null) {
            throw new IOException("connection is null");
         }

         this.configureConnection(connection);
         this.journalLog("start token=" + this.describeToken());

         if (!this.validateResponse(connection, body)) {
            this.journalLog("start failed: request body not sent");
            return;
         }

         int responseCode = connection.getResponseCode();
         String responseBody = this.readRawResponseBody(connection);
         this.journalLog("start response code=" + responseCode + " body=" + responseBody);

         if (responseCode == 201) {
            ServiceRegistry.getNotificationService().showToast(
               NotificationType.SUCCESS,
               "§a§lУспех",
               "Вы успешно внесли проверку в журнал.",
               5.0F
            );
         } else {
            ServiceRegistry.getNotificationService().showToast(
               NotificationType.ERROR,
               "§c§lОшибка",
               "Ошибка при внесении проверки. Код: §c" + responseCode,
               5.0F
            );
         }
      } catch (Exception exception) {
         this.journalLog("start exception=" + exception);
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§4§lИсключение",
            "Исключение в NetService/startCheckout: §4" + exception,
            5.0F
         );
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public void queueJournalEntry(String result, String banReason, boolean destroyStash) {
      if (!ModBuild.JOURNAL_API) {
         this.journalLog("end skipped: journal api disabled result=" + result);
         return;
      }
      JsonObject body = new JsonObject();
      body.addProperty("result", result);
      body.addProperty("banReason", banReason);
      body.addProperty("destroyStash", destroyStash);
      this.journalLog("end request body=" + body);

      if (!this.ensureSuccess()) {
         this.journalLog("end blocked: no active checkout on server");
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.ERROR,
            "§c§lОшибка",
            "У вас нет активной проверки.",
            5.0F
         );
         return;
      }

      HttpsURLConnection connection = null;

      try {
         connection = this.openJournalConnection("https://journal.holyworld.me/srv/api/v1/checkout/end", "POST", null);
         if (connection == null) {
            throw new IOException("connection is null");
         }

         this.configureConnection(connection);
         this.journalLog("end token=" + this.describeToken());

         if (!this.validateResponse(connection, body)) {
            this.journalLog("end failed: request body not sent");
            return;
         }

         int responseCode = connection.getResponseCode();
         String responseBody = this.readRawResponseBody(connection);
         this.journalLog("end response code=" + responseCode + " body=" + responseBody);

         if (responseCode == 201) {
            ServiceRegistry.getNotificationService().showToast(
               NotificationType.SUCCESS,
               "§a§lУспех",
               "Вы успешно закончили проверку в журнале.",
               5.0F
            );
         } else {
            ServiceRegistry.getNotificationService().showToast(
               NotificationType.ERROR,
               "§c§lОшибка",
               "Ошибка при завершении проверки. Код: §c" + responseCode,
               5.0F
            );
         }
      } catch (Exception exception) {
         this.journalLog("end exception=" + exception);
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§4§lИсключение",
            "Исключение в NetService/endCheckout: §4" + exception,
            5.0F
         );
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   private boolean ensureSuccess() {
      HttpsURLConnection connection = null;

      try {
         connection = this.openJournalConnection("https://journal.holyworld.me/srv/api/v1/checkout/status", "GET", null);
         if (connection == null) {
            throw new IOException("connection is null");
         }

         this.configureConnection(connection);
         int responseCode = connection.getResponseCode();
         String responseBody = this.readRawResponseBody(connection);
         this.journalLog("status response code=" + responseCode + " body=" + responseBody);

         Map<String, Object> response = this.parsePlayersFromResponse(responseBody);
         Object status = response.get("status");
         boolean active = status instanceof Boolean && (Boolean)status;
         this.journalLog("status active=" + active);
         return active;
      } catch (Exception exception) {
         this.journalLog("status exception=" + exception);
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§4§lИсключение",
            "Исключение в NetService/hasActiveCheckout: §4" + exception,
            5.0F
         );
         return false;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   private Map<String, Object> parsePlayerList(String path) {
      HttpsURLConnection connection = null;

      try {
         connection = this.openJournalConnection(this.journalApiPath + path, "GET", null);
         if (connection == null) {
            throw new IOException("connection is null");
         }

         this.configureConnection(connection);
         String body = this.readResponseBody(connection);
         if (body == null || body.isBlank()) {
            return Collections.emptyMap();
         }
         return this.parsePlayersFromResponse(body);
      } catch (IOException exception) {
         this.showRequestError(path, exception);
         return Collections.emptyMap();
      } catch (Exception exception) {
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§4§lИсключение",
            "Исключение в NetService/executeGetRequest: §4" + exception,
            5.0F
         );
         return Collections.emptyMap();
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   private void showRequestError(String path, IOException exception) {
      String message = exception.getMessage() == null ? "" : exception.getMessage();
      if (message.contains("401")) {
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.ERROR,
            "§c§lОшибка",
            "Неверный API ключ журнала. Открой §bjournal.holyworld.me/api§f, скопируй §6API ключ§f через кнопку copy (не auth_token из cookies) и пропиши: §6/hm setapitoken <ключ>",
            15.0F
         );
         return;
      }

      ServiceRegistry.getNotificationService().showToast(
         NotificationType.EXCEPTION,
         "§4§lИсключение",
         "Исключение в NetService/getResponse (" + path + "): §4" + exception,
         5.0F
      );
   }

   private void configureConnection(@NotNull HttpsURLConnection connection) {
      String token = sanitizeApiToken(ServiceRegistry.getConfigManager().getState().getApiToken());
      connection.setRequestProperty("x-token", token);
      connection.setRequestProperty("Content-Type", "application/json");
   }

   private boolean validateResponse(@NotNull HttpsURLConnection connection, @NotNull JsonObject body) {
      try {
         connection.setDoOutput(true);

         try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(body.toString());
         }

         return true;
      } catch (Exception exception) {
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§4§lИсключение",
            "Исключение в NetService/writeJson: §4" + exception,
            5.0F
         );
         return false;
      }
   }

   public String readResponseBody(@NotNull HttpsURLConnection connection) throws IOException {
      int responseCode = connection.getResponseCode();
      String body = this.readRawResponseBody(connection);
      if (responseCode >= 400) {
         throw new IOException("Server returned HTTP response code: " + responseCode + " for URL: " + connection.getURL());
      }

      return body;
   }

   private String readRawResponseBody(@NotNull HttpsURLConnection connection) throws IOException {
      int responseCode = connection.getResponseCode();
      java.io.InputStream stream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
      if (stream == null) {
         return "";
      }

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
         StringBuilder response = new StringBuilder();
         String line;

         while ((line = reader.readLine()) != null) {
            response.append(line);
         }

         return response.toString();
      }
   }

   private void journalLog(String message) {
      ServiceRegistry.getDebugLogService().write("journal", message);
   }

   private String describeToken() {
      String token = sanitizeApiToken(ServiceRegistry.getConfigManager().getState().getApiToken());
      if (token.isEmpty()) {
         return "empty";
      }

      if (token.length() <= 8) {
         return "len=" + token.length();
      }

      return "len=" + token.length() + " prefix=" + token.substring(0, 4) + " suffix=" + token.substring(token.length() - 4);
   }

   private Map<String, Object> parsePlayersFromResponse(@NotNull String response) {
      try {
         Type type = new TypeToken<Map<String, Object>>() {}.getType();
         Map<String, Object> parsed = this.gson.fromJson(response, type);
         return parsed == null ? Collections.emptyMap() : parsed;
      } catch (Exception exception) {
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§4§lИсключение",
            "Исключение в NetService/parseJsonResponse: §4" + exception,
            5.0F
         );
         return Collections.emptyMap();
      }
   }

   public HttpsURLConnection openJournalConnection(String url, String method, String cookie) {
      try {
         HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
         connection.setRequestMethod(method);
         connection.setRequestProperty("User-Agent", "Mozilla/5.0");

         if (cookie != null) {
            connection.setRequestProperty("Cookie", cookie);
         }

         return connection;
      } catch (Exception exception) {
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§4§lИсключение",
            "Исключение в NetService/openHttpsConnection: §4" + exception,
            5.0F
         );
         return null;
      }
   }
}

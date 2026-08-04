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
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.util.NotificationType;
import org.jetbrains.annotations.NotNull;

public class NetService extends BaseService {
   private final String journalApiPath = "https://journal.holyworld.me/srv/api/v1/";
   private final Gson gson = new Gson();

   public Map<String, Object> getPlayerHistory() {
      return this.parsePlayerList("me");
   }

   public Map<String, Object> getCheckoutSessions() {
      return this.parsePlayerList("stats");
   }

   public void submitJournalEntry(String username, String reason, String mode, int anarchyNumber, boolean isPvpAnarchy) {
      if (this.ensureSuccess()) {
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

         JsonObject body = new JsonObject();
         body.addProperty("username", username);
         body.addProperty("reason", reason);
         body.addProperty("mode", mode);
         body.addProperty("anarchyNumber", anarchyNumber);
         body.addProperty("isPvpAnarchy", isPvpAnarchy);

         if (this.validateResponse(connection, body)) {
            if (connection.getResponseCode() == 201) {
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
                  "Ошибка при внесении проверки. Код: §c" + connection.getResponseCode(),
                  5.0F
               );
            }
         }
      } catch (Exception exception) {
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
      if (!this.ensureSuccess()) {
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

         JsonObject body = new JsonObject();
         body.addProperty("result", result);
         body.addProperty("banReason", banReason);
         body.addProperty("destroyStash", destroyStash);

         if (this.validateResponse(connection, body)) {
            if (connection.getResponseCode() == 201) {
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
                  "Ошибка при завершении проверки. Код: §c" + connection.getResponseCode(),
                  5.0F
               );
            }
         }
      } catch (Exception exception) {
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
         Map<String, Object> response = this.parsePlayersFromResponse(this.readResponseBody(connection));
         Object status = response.get("status");
         return status instanceof Boolean && (Boolean)status;
      } catch (Exception exception) {
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
            "Неверный API токен журнала. Скопируй токен из профиля на journal.holyworld.me и пропиши: §6/hm setapitoken <token>",
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
      String token = ServiceRegistry.getConfigManager().getState().getApiToken().trim();
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
      java.io.InputStream stream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
      if (stream == null) {
         throw new IOException("Server returned HTTP response code: " + responseCode + " for URL: " + connection.getURL());
      }

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
         StringBuilder response = new StringBuilder();
         String line;

         while ((line = reader.readLine()) != null) {
            response.append(line);
         }

         if (responseCode >= 400) {
            throw new IOException("Server returned HTTP response code: " + responseCode + " for URL: " + connection.getURL());
         }

         return response.toString();
      }
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

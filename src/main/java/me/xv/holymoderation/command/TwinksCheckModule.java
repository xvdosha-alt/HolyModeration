package me.xv.holymoderation.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.xv.holymoderation.config.PathsConfig;
import me.xv.holymoderation.event.ChatMessageEvent;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.util.NotificationType;
import org.apache.commons.io.FileUtils;

public class TwinksCheckModule extends BaseCommandHandler {
   private final Path outputDir = PathsConfig.tempDir();
   private final File tempFile = PathsConfig.tempFile().toFile();

   @Subscribe
   public void onTwinksCommand(CommandEvent event) {
      String command = event.getCommand();
      String[] parts = command.split(" ");
      if (!command.startsWith("hm") || parts.length < 2) {
         return;
      }

      if (!parts[1].equals("twinks")) {
         return;
      }

      if (this.serviceContext.getStateService().getInHub()) {
         this.showWarning("В хабе этого делать нельзя.");
         return;
      }

      File checkFile = PathsConfig.checkTwinksFile().toFile();
      if (!checkFile.exists()) {
         this.showError("Не найден файл 'checktwinks.txt' по пути '" + PathsConfig.root() + "'.");
         return;
      }

      this.serviceContext.getNotificationService().showToast(
         NotificationType.WARNING, "§6§lПредупреждение", "Проверка твинков началась.", 5.0F
      );

      try {
         try {
            if (this.tempFile.exists()) {
               Files.delete(this.tempFile.toPath());
            }
            if (!Files.exists(this.outputDir)) {
               Files.createDirectory(this.outputDir);
            }
            Files.createFile(this.tempFile.toPath());
         } catch (Exception e) {
            this.showException("TwinksCheckModule/onMessageSend", e);
         }

         try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(Files.newInputStream(checkFile.toPath()), StandardCharsets.UTF_16LE)
         )) {
            reader.mark(1);
            if (reader.read() != 65279) {
               reader.reset();
            }

            String[] players = reader.readLine().split(" ");
            this.serviceContext.getStateService().setCheckingTwinks(true);
            var executor = this.serviceContext.getSchedulerService().getExecutor();
            char[] invalidChars = this.serviceContext.getChatService().Chars;

            for (int i = 0; i < players.length; i++) {
               String player = players[i];
               boolean invalid = false;
               for (char c : invalidChars) {
                  if (player.contains(String.valueOf(c))) {
                     invalid = true;
                     break;
                  }
               }
               if (invalid) {
                  continue;
               }

               executor.schedule(() -> this.handleTwinkAlert(player), i, TimeUnit.SECONDS);
               if (i + 1 == players.length) {
                  executor.schedule(this::initTwinksWatcher, i + 1L, TimeUnit.SECONDS);
               }
            }
         }
      } catch (Exception e) {
         this.showException("TwinksCheckModule/onMessageSend", e);
      }
   }

   @Subscribe(priority = 98)
   public void onTwinksChat(ChatMessageEvent event) {
      String message = this.serviceContext.getChatService().stripFormatting(event.getMessage().getString());
      if (message == null) {
         return;
      }

      if (!this.serviceContext.getStateService().getCheckingTwinks()) {
         return;
      }

      if (!message.startsWith(" -- [")
         && !message.startsWith("Игрок")
         && !message.startsWith("по причина:")
         && !message.startsWith("История")
         && !message.startsWith("Окончание через")
         && !message.startsWith("Разбанен:")
         && !message.startsWith("Размьючен:")
         && !message.trim().isEmpty()) {
         return;
      }

      event.setCancelled(true);
      try {
         Files.writeString(
            this.tempFile.toPath(),
            System.lineSeparator() + message,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
         );
      } catch (Exception e) {
         this.showException("TwinksCheckModule/onMessageReceive", e);
      }
   }

   private void scanTwinksFolder() {
      try {
         List<String> lines = Files.readAllLines(this.tempFile.toPath());
         Files.delete(this.tempFile.toPath());

         List<String> blockLines = new ArrayList<>();
         String currentPlayer = "";
         for (String line : lines) {
            if (line.startsWith("%%%")) {
               if (!currentPlayer.isEmpty() && !blockLines.isEmpty()) {
                  this.writeTwinkReport(this.outputDir, currentPlayer, blockLines);
               }
               currentPlayer = line.substring(3, line.length() - 3);
               blockLines = new ArrayList<>();
            } else if (!currentPlayer.isEmpty()) {
               blockLines.add(line);
            }
         }

         if (!currentPlayer.isEmpty() && !blockLines.isEmpty()) {
            this.writeTwinkReport(this.outputDir, currentPlayer, blockLines);
         }

         StringBuilder results = new StringBuilder();
         try (var stream = Files.list(this.outputDir)) {
            stream.filter(TwinksCheckModule::isValidTwinkFile).forEach(path -> this.appendTwinkHeader(results, path));
         }

         Path resultsPath = PathsConfig.resultsFile();
         Files.writeString(resultsPath, results.toString());
         FileUtils.deleteDirectory(this.outputDir.toFile());
         this.serviceContext.getNotificationService().showToastWithAction(
            NotificationType.SUCCESS,
            "§a§lУспех",
            "Проверка твинков завершена, просмотрите результаты в " + resultsPath + ".",
            5.0F,
            "twinksDone.wav"
         );
      } catch (Exception e) {
         this.showException("TwinksCheckModule/parseHistory", e);
      }
   }

   private static String formatTwinkLine(String playerName, List<String> lines) {
      Pattern statusPattern = Pattern.compile("\\[(Активный|Истёкший)]");
      boolean banned = false;

      for (int i = 0; i < lines.size(); i++) {
         String line = lines.get(i);
         if (!line.contains("был забанен") || !line.contains(playerName)) {
            continue;
         }

         Matcher matcher = statusPattern.matcher(line);
         if (!matcher.find() && i + 1 < lines.size()) {
            matcher = statusPattern.matcher(lines.get(i + 1));
         }
         if (!matcher.find()) {
            continue;
         }

         if ("Активный".equals(matcher.group(1))) {
            banned = true;
            break;
         }
      }

      return banned ? " - забанен" : " - не забанен";
   }

   private static List<String> collectTwinkMatches(String playerName, List<String> lines) {
      List<String> matches = new ArrayList<>();
      Pattern pattern = Pattern.compile(
         "-- \\[(.*?) назад] --.*?Игрок " + Pattern.quote(playerName) + " был (\\S+).*?по причина:\\s*'(.*?)'",
         Pattern.DOTALL
      );

      for (int i = 0; i < lines.size(); i++) {
         String line = lines.get(i);
         if (!line.startsWith(" -- [")) {
            continue;
         }

         StringBuilder blockBuilder = new StringBuilder(line);
         while (i + 1 < lines.size()) {
            String nextLine = lines.get(i + 1);
            if (nextLine.startsWith(" -- [")
               || nextLine.startsWith("История")
               || nextLine.startsWith("Окончание")
               || nextLine.startsWith("Разбанен:")
               || nextLine.startsWith("Размьючен:")) {
               break;
            }
            i++;
            blockBuilder.append("\n").append(lines.get(i));
         }

         String block = blockBuilder.toString();
         Matcher matcher = pattern.matcher(block);
         if (!matcher.find()) {
            continue;
         }

         String timeAgo = matcher.group(1);
         String action = matcher.group(2);
         String reason = matcher.group(3);
         if (!action.matches("(забанен|кикнут)") || !isTwinkSuspect(timeAgo)) {
            continue;
         }

         String actionLabel = action.equals("забанен") ? "БАН" : "КИК";
         String moderator = normalizePlayerName(block);
         matches.add(String.format("%s (%s) by %s - %s назад", actionLabel, reason, moderator, timeAgo));
      }

      return matches;
   }

   private static String normalizePlayerName(String text) {
      String name = "Console";
      String[] parts = text.split("\n");
      if (parts.length <= 1) {
         return name;
      }

      String secondLine = parts[1];
      if (secondLine.contains("игроком ")) {
         name = secondLine.split("игроком ")[1].trim();
      } else if (secondLine.contains("модератором ")) {
         name = secondLine.split("модератором ")[1].trim();
      }
      return name;
   }

   private static boolean isTwinkSuspect(String text) {
      int days = 0;
      int hours = 0;
      int minutes = 0;
      String[] parts = text.split(" ");

      for (int i = 0; i < parts.length; i++) {
         if ("дн.".equals(parts[i]) && i > 0) {
            try {
               days = Integer.parseInt(parts[i - 1]);
            } catch (NumberFormatException ignored) {
               days = 0;
            }
         } else if ("ч.".equals(parts[i]) && i > 0) {
            try {
               hours = Integer.parseInt(parts[i - 1]);
            } catch (NumberFormatException ignored) {
               hours = 0;
            }
         } else if ("мин.".equals(parts[i]) && i > 0) {
            try {
               minutes = Integer.parseInt(parts[i - 1]);
            } catch (NumberFormatException ignored) {
               minutes = 0;
            }
         }
      }

      double totalDays = days + hours / 24.0 + minutes / 1440.0;
      return totalDays <= 30.0;
   }

   private void writeTwinkReport(Path outputDir, String playerName, List<String> lines) {
      try {
         boolean hasHistory = false;
         for (String line : lines) {
            if (line.startsWith("История " + playerName) || line.startsWith("История не найдена.")) {
               hasHistory = true;
               break;
            }
         }
         if (!hasHistory) {
            return;
         }

         Path reportPath = outputDir.resolve(playerName + ".txt");
         Files.write(reportPath, lines);
      } catch (Exception e) {
         this.showException("TwinksCheckModule/saveBlock", e);
      }
   }

   private void appendTwinkHeader(StringBuilder builder, Path filePath) {
      try {
         String playerName = filePath.getFileName().toString().replace(".txt", "");
         List<String> lines = Files.readAllLines(filePath);
         String status = formatTwinkLine(playerName, lines);

         if (lines.size() == 1 && lines.get(0).equals("История не найдена.")
            || lines.size() == 2
               && lines.get(0).startsWith("История ")
               && lines.get(1).isEmpty()) {
            builder.append(playerName)
               .append(status)
               .append(" {\n")
               .append("  История не найдена\n")
               .append("}\n\n");
            return;
         }

         List<String> matches = collectTwinkMatches(playerName, lines);
         builder.append(playerName).append(status).append(" {\n");
         if (matches.isEmpty()) {
            builder.append("  Нет наказаний за последние 30 дней\n");
         } else {
            matches.forEach(match -> appendTwinkEntry(builder, match));
         }
         builder.append("}\n\n");
      } catch (Exception e) {
         this.showException("TwinksCheckModule/parseHistory", e);
      }
   }

   private static void appendTwinkEntry(StringBuilder builder, String entry) {
      builder.append("  ").append(entry).append("\n");
   }

   private static boolean isValidTwinkFile(Path path) {
      return path.toString().endsWith(".txt");
   }

   private void initTwinksWatcher() {
      this.serviceContext.getStateService().setCheckingTwinks(false);
      this.scanTwinksFolder();
   }

   private void handleTwinkAlert(String player) {
      try {
         String prefix = this.tempFile.length() > 0L ? System.lineSeparator() : "";
         Files.writeString(
            this.tempFile.toPath(),
            prefix + "%%%" + player + "%%%",
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
         );
         this.serviceContext.getChatService().sendChatOrCommand("/history " + player + " 100");
      } catch (Exception e) {
         this.showException("TwinksCheckModule/onMessageSend", e);
      }
   }

   private void showWarning(String message) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.WARNING, "§6§lПредупреждение", message, 5.0F
      );
   }

   private void showError(String message) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.ERROR, "§c§lОшибка", message, 5.0F
      );
   }

   private void showException(String context, Exception e) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.EXCEPTION, "§4§lИсключение", "Исключение в " + context + ": §4" + e, 5.0F
      );
   }

   static {
   }
}

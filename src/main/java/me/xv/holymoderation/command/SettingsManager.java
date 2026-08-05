package me.xv.holymoderation.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import me.xv.holymoderation.config.ModState;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.service.ChatService;
import me.xv.holymoderation.util.NotificationType;
import net.minecraft.network.chat.Component;

public class SettingsManager extends BaseCommandHandler {
   public static final Map<Integer, String> RANKS;
   private final String[] settingsCommands = new String[]{
      "autoban",
      "autocopy",
      "autodupeip",
      "autofly",
      "autogm3",
      "autogod",
      "autoha",
      "autotp",
      "autovanish",
      "copy",
      "me",
      "setcopy",
      "setmarker",
      "setspydelay",
      "setsoundsvolume",
      "sounds",
      "stats",
      "textadd",
      "textedit",
      "textremove",
      "textsclear",
      "textslist"
   };
   private final String[] settingsWithoutArguments = new String[]{
      "autoban",
      "autocopy",
      "autodupeip",
      "autofly",
      "autogm3",
      "autogod",
      "autoha",
      "autotp",
      "autovanish",
      "copy",
      "me",
      "sounds",
      "stats",
      "textsclear",
      "textslist"
   };
   private final String[] settingsWithOneArgument = new String[]{"setcopy", "setmarker", "setspydelay", "setsoundsvolume", "textadd", "textremove"};
   private final String[] settingsWithTwoArguments = new String[]{"textedit"};

   @Subscribe
   public void onSettingsCommand(CommandEvent event) {
      String command = event.getCommand();
      String[] parts = command.split(" ");
      if (!command.startsWith("hm") || parts.length < 2) {
         return;
      }

      String subcommand = parts[1];
      ChatService chat = this.serviceContext.getChatService();
      if (!chat.matchesCommand(this.settingsCommands, subcommand)) {
         return;
      }

      List<String> textsList = this.serviceContext.getConfigManager().getState().getTextsList();
      if (chat.matchesCommand(this.settingsWithoutArguments, subcommand)) {
         this.handleSettingsWithoutArguments(subcommand, textsList);
      }
      if (chat.matchesCommand(this.settingsWithOneArgument, subcommand)) {
         this.handleSettingsWithOneArgument(command, subcommand, textsList);
      }
      if (chat.matchesCommand(this.settingsWithTwoArguments, subcommand)) {
         this.handleSettingsWithTwoArguments(command, textsList);
      }

      this.serviceContext.getConfigManager().save(this.serviceContext.getConfigManager().getState());
   }

   private void handleSettingsWithoutArguments(String subcommand, List<String> textsList) {
      ModState state = this.serviceContext.getConfigManager().getState();
      switch (subcommand) {
         case "textslist":
            if (textsList.isEmpty()) {
               this.showError("У вас нет настроенных текстов.");
            }
            StringBuilder listBuilder = new StringBuilder("");
            for (int i = 0; i < textsList.size(); i++) {
               listBuilder.append("§b§l").append(i + 1).append("§f").append(". ").append(textsList.get(i));
               if (i < textsList.size() - 1) {
                  listBuilder.append("\n");
               }
            }
            this.serviceContext.getNotificationService().showToast(
               NotificationType.SUCCESS, "§a§lСписок ваших текстов", listBuilder.toString(), 5.0F
            );
            break;
         case "textsclear":
            textsList.clear();
            this.showSuccess("Вы успешно очистили все тексты.");
            break;
         case "autodupeip":
            state.setDupeIpEnabled(!state.getDupeIpEnabled());
            this.showSuccess("Автоматический /dupeip " + (state.getDupeIpEnabled() ? "включён" : "выключен") + ".");
            break;
         case "autocopy":
            state.setAutoAnyDeskEnabled(!state.getAutoAnyDeskEnabled());
            this.showSuccess("Автоматическое копирование айди AnyDesk " + (state.getAutoAnyDeskEnabled() ? "включено" : "выключено") + ".");
            break;
         case "autotp":
            state.setAutoTpEnabled(!state.getAutoTpEnabled());
            this.showSuccess("Атоматический телепорт на /warp logo " + (state.getAutoTpEnabled() ? "включён" : "выключен") + ".");
            break;
         case "autoban":
            state.setAutoBanEnabled(!state.getAutoBanEnabled());
            this.showSuccess("Автоманический бан игрока при ливе с проверки " + (state.getAutoBanEnabled() ? "включён" : "выключен") + ".");
            break;
         case "autovanish":
            state.setAutoVanishEnabled(!state.getAutoVanishEnabled());
            this.showSuccess("Автоматический ваниш " + (state.getAutoVanishEnabled() ? "включён" : "выключен") + ".");
            break;
         case "autofly":
            state.setAutoFlyEnabled(!state.getAutoFlyEnabled());
            this.showSuccess("Автоматический флай " + (state.getAutoFlyEnabled() ? "включён" : "выключен") + ".");
            break;
         case "autogm3":
            state.setAutoGm3Enabled(!state.getAutoGm3Enabled());
            this.showSuccess("Автоматический гм3 " + (state.getAutoGm3Enabled() ? "включён" : "выключен") + ".");
            break;
         case "autoha":
            state.setAutoHacAlertsEnabled(!state.getAutoHacAlertsEnabled());
            this.showSuccess("Автоматический hac alerts " + (state.getAutoHacAlertsEnabled() ? "включён" : "выключен") + ".");
            break;
         case "autogod":
            state.setAutoGodEnabled(!state.getAutoGodEnabled());
            this.showSuccess("Автоматический god " + (state.getAutoGodEnabled() ? "включён" : "выключен") + ".");
            break;
         case "me":
            CompletableFuture.runAsync(this::dispatchSettings);
            break;
         case "stats":
            this.showModeratorStats();
            break;
         case "copy":
            state.setCopyButtonEnabled(!state.getCopyButtonEnabled());
            this.showSuccess("Кнопка копирования " + (state.getCopyButtonEnabled() ? "включена" : "выключена") + ".");
            break;
         case "sounds":
            state.setSoundsEnabled(!state.getSoundsEnabled());
            this.showSuccess("Звуки мода " + (state.getSoundsEnabled() ? "включены" : "выключены") + ".");
            break;
         default:
            break;
      }
   }

   private void handleSettingsWithOneArgument(String command, String subcommand, List<String> textsList) {
      String[] parts = command.split(" ", 3);
      ModState state = this.serviceContext.getConfigManager().getState();
      ChatService chat = this.serviceContext.getChatService();

      switch (subcommand) {
         case "textadd":
            if (parts.length == 2) {
               this.showError("Вы не указали текст.");
               return;
            }
            textsList.add(parts[2].replace("&", "§"));
            this.showSuccess("Вы добавили новый текст.");
            break;
         case "textremove":
            if (textsList.isEmpty()) {
               this.showError("У вас нет настроенных текстов.");
               return;
            }
            if (parts.length == 2) {
               this.showError("Вы не указали номер текста.");
               return;
            }
            if (!chat.isHmCommand(parts[2])) {
               this.showError("Некорректный номер текста.");
               return;
            }
            int removeIndex = Integer.parseInt(parts[2]) - 1;
            if (removeIndex < 0 || removeIndex >= textsList.size()) {
               this.showError("Элемента с таким номером в списке ваших текстов не существует.");
               return;
            }
            textsList.remove(removeIndex);
            this.showSuccess("Вы удалили текст номер " + parts[2] + "§b§l.");
            break;
         case "setcopy":
            if (parts.length == 2) {
               state.setCopyButtonText("§f§l[§a§lcopy§f§l]");
               this.showSuccess("Текст кнопки был сброшен.");
            } else {
               state.setCopyButtonText(parts[2].replace("&", "§"));
               this.showSuccess("Вы установили новый текст кнопки копирования.");
            }
            break;
         case "setmarker":
            if (parts.length == 2) {
               state.setPlayerMarker("§d§l[CHECK]");
               this.showSuccess("Текст метки был сброшен.");
            } else {
               state.setPlayerMarker(parts[2].replace("&", "§"));
               this.showSuccess("Вы установили новый текст маркера.");
            }
            break;
         case "setspydelay":
            if (parts.length == 2) {
               this.showError("Вы не указали число.");
               return;
            }
            if (!chat.isHmCommand(parts[2])) {
               this.showError("Некорректное число.");
               return;
            }
            state.setSpyDelay(Integer.parseInt(parts[2]));
            this.showSuccess("Вы установили новую задержку в spy: " + state.getSpyDelay() + ".");
            break;
         case "setsoundsvolume":
            if (parts.length == 2) {
               this.showError("Вы не указали число.");
               return;
            }
            if (!chat.isHmCommand(parts[2])) {
               this.showError("Некорректное число.");
               return;
            }
            state.setSoundsVolume(Integer.parseInt(parts[2]));
            this.showSuccess("Вы установили новую громкость звуков: " + state.getSoundsVolume() + ".");
            break;
         default:
            break;
      }
   }

   private void handleSettingsWithTwoArguments(String command, List<String> textsList) {
      String[] parts = command.split(" ", 4);
      ChatService chat = this.serviceContext.getChatService();

      if (textsList.isEmpty()) {
         this.showError("У вас нет настроенных текстов.");
         return;
      }
      if (parts.length == 2) {
         this.showError("Вы не указали номер текста и новый текст.");
         return;
      }
      if (!chat.isHmCommand(parts[2])) {
         this.showError("Некорректный номер текста.");
         return;
      }
      if (parts.length == 3) {
         this.showError("Вы не указали новый текст.");
         return;
      }

      int editIndex = Integer.parseInt(parts[2]) - 1;
      if (editIndex < 0 || editIndex >= textsList.size()) {
         this.showError("Элемента с таким номером в списке ваших текстов не существует.");
         return;
      }

      String newText = parts[3].replace("&", "§");
      if (newText.contains("%%")) {
         this.showError("Текст не должен содержать '%%'.");
         return;
      }

      textsList.set(editIndex, newText);
      this.showSuccess("Вы изменили текст номер " + (editIndex + 1) + ".");
   }

   private void dispatchSettings() {
      try {
         Map<?, ?> profile = this.serviceContext.getStateService().getJournalProfile();
         String nickname = profile.get("nickname").toString();
         int rank = (int)Double.parseDouble(profile.get("rank").toString());
         String rankName = RANKS.get(rank);
         String fullname = profile.get("fullname").toString();
         long idVk = (long)Double.parseDouble(profile.get("idVk").toString());
         int neponyatki = (int)Double.parseDouble(profile.get("neponyatki").toString());
         int reprimands = (int)Double.parseDouble(profile.get("reprimands").toString());
         int warns = (int)Double.parseDouble(profile.get("warns").toString());
         String anarchyMode = String.valueOf(profile.get("anarchyMode"));
         String message = "§fВаш никнейм: §b§l" + nickname
            + "\n§fВаша должность: " + rankName
            + "\n§fВаш вк: §b§l" + fullname + " (§fvk.com/id" + idVk + "§b§l)"
            + "\n§fВаш баланс: §a§l" + neponyatki
            + "\n§fКоличество выговоров: §c§l" + reprimands
            + "\n§fКоличество предупреждений: §6§l" + warns
            + "\n§fРежим: §e§l" + anarchyMode;
         this.serviceContext.getNotificationService().showToast(
            NotificationType.SUCCESS, "§a§lИНФОРМАЦИЯ О МОДЕРАТОРЕ", message, 10.0F
         );
      } catch (Exception exception) {
         this.serviceContext.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§3§lИсключение",
            "Исключение в SettingsManager/onMessageSend: §4" + exception,
            5.0F
         );
      }
   }

   private void showModeratorStats() {
      try {
         Map<?, ?> stats = this.serviceContext.getStateService().getJournalStats();
         String message = "";

         Map<?, ?> revisesAll = (Map<?, ?>)stats.get("revisesAll");
         Map<?, ?> revisesMonth = (Map<?, ?>)stats.get("revisesMonth");
         Map<?, ?> revisesWeek = (Map<?, ?>)stats.get("revisesWeek");
         Map<?, ?> revisesToday = (Map<?, ?>)stats.get("revisesToday");
         if (revisesAll != null && revisesMonth != null && revisesWeek != null && revisesToday != null) {
            message = "§d§lСТАТИСТИКА ПРОВЕРОК\n§fПроверок за всё время: §b§l"
               + this.parseStat(revisesAll, "total") + " (лайт: " + this.parseStat(revisesAll, "lite")
               + ", лайт 1.20: " + this.parseStat(revisesAll, "lite120") + ", классик: " + this.parseStat(revisesAll, "classic")
               + ", прайм: " + this.parseStat(revisesAll, "prime") + ")\n"
               + "§fПроверок за последний месяц: §b§l" + this.parseStat(revisesMonth, "total")
               + " (лайт: " + this.parseStat(revisesMonth, "lite") + ", лайт 1.20: " + this.parseStat(revisesMonth, "lite120")
               + ", классик: " + this.parseStat(revisesMonth, "classic") + ", прайм: " + this.parseStat(revisesMonth, "prime") + ")\n"
               + "§fПроверок за последнюю неделю: §b§l" + this.parseStat(revisesWeek, "total")
               + " (лайт: " + this.parseStat(revisesWeek, "lite") + ", лайт 1.20: " + this.parseStat(revisesWeek, "lite120")
               + ", классик: " + this.parseStat(revisesWeek, "classic") + ", прайм: " + this.parseStat(revisesWeek, "prime") + ")\n"
               + "§fПроверок за сегодня: §b§l" + this.parseStat(revisesToday, "total")
               + " (лайт: " + this.parseStat(revisesToday, "lite") + ", лайт 1.20: " + this.parseStat(revisesToday, "lite120")
               + ", классик: " + this.parseStat(revisesToday, "classic") + ", прайм: " + this.parseStat(revisesToday, "prime") + ")\n";
         }

         message = message
            + "§d§lСТАТИСТИКА МУТОВ И ГАРАНТОВ\n§fМутов за всё время: §b§l" + this.parseStat(stats, "mutesAll")
            + "\n§fМутов за последний месяц: §b§l" + this.parseStat(stats, "mutesMonth")
            + "\n§fМутов за сегодня: §b§l" + this.parseStat(stats, "mutesToday")
            + "\n§fГарантов за всё время: §b§l" + this.parseStat(stats, "gaurantsAll")
            + "\n§fГарантов за последний месяц: §b§l" + this.parseStat(stats, "gaurantsMonth")
            + "\n§fГарантов за сегодня: §b§l" + this.parseStat(stats, "gaurantsToday");

         this.serviceContext.getNotificationService().showToast(
            NotificationType.SUCCESS, "§a§lСТАТИСТИКА МОДЕРАТОРА", message, 10.0F
         );
      } catch (Exception exception) {
         this.serviceContext.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§3§lИсключение",
            "Исключение в SettingsManager/onMessageSend: §4" + exception,
            5.0F
         );
      }
   }

   private int parseStat(Map<?, ?> map, String key) {
      Object value = map.get(key);
      if (value == null) {
         return 0;
      }
      return (int)Double.parseDouble(value.toString());
   }

   private void showSuccess(String message) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.SUCCESS, "§a§lУспех", message, 5.0F
      );
      this.serviceContext.getChatService().sendMessage(Component.literal("§a" + message));
   }

   private void showError(String message) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.ERROR, "§c§lОшибка", message, 5.0F
      );
      this.serviceContext.getChatService().sendMessage(Component.literal("§c" + message));
   }

   static {
      RANKS = new HashMap<Integer, String>() {
         {
            this.put(1, "§b§lСтажёр");
            this.put(2, "§e§lМл. Сотрудник");
            this.put(3, "§6§lСотрудник");
            this.put(4, "§6§lСотрудник+");
            this.put(5, "§6§lВед. Сотрудник");
            this.put(6, "§f§lСпектатор");
            this.put(7, "§c§lСт. Сотрудник");
            this.put(8, "§c§lАдмин");
            this.put(9, "§c§lКуратор");
         }
      };
   }
}

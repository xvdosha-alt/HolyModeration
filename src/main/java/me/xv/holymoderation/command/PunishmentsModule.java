package me.xv.holymoderation.command;

import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.service.ChatService;
import me.xv.holymoderation.util.NotificationType;

public class PunishmentsModule extends BaseCommandHandler {
   private final String[] punishmentsCommands = new String[]{
      "/mute", "/muteip", "/tempmute", "/tempmuteip", "/ban", "/banip", "/tempban", "/warn"
   };
   private final String[] tempPunishments = new String[]{"/tempmute", "/tempmuteip", "/tempban"};
   private final String[] infinityPunishments = new String[]{"/mute", "/muteip", "/ban", "/banip"};
   private final String[] banCommands = new String[]{"/ban", "/banip", "/tempban"};
   private final String[] muteCommands = new String[]{"/mute", "/muteip", "/tempmute", "/tempmuteip"};
   private final String[] vkCommands = new String[]{"/mute", "/muteip", "/ban", "/banip", "/tempban"};
   private boolean nicknameHasChar = false;
   private boolean strangePunishmentConfirm = false;
   private boolean strangeFrzPunishmentConfirm = false;
   private String strangeMessage = "";
   private String strangeFrzMessage = "";

   @Subscribe
   public void onPunishCommand(CommandEvent event) {
      String command = event.getCommand();
      String[] parts = command.split(" ", 3);
      String subCommand = "/" + parts[0];
      ChatService chat = this.serviceContext.getChatService();

      if (!command.equals(this.strangeMessage)) {
         this.strangePunishmentConfirm = false;
      }
      if (!command.equals(this.strangeFrzMessage)) {
         this.strangeFrzPunishmentConfirm = false;
      }

      if (!chat.matchesCommand(this.punishmentsCommands, subCommand)) {
         return;
      }

      event.setCancelled(true);
      if (parts.length <= 1) {
         return;
      }

      String player = parts[1];
      if (!this.validatePlayer(player, command, chat)) {
         return;
      }

      if (!this.confirmFreezePunishment(player, command)) {
         return;
      }

      if (chat.matchesCommand(this.tempPunishments, subCommand)) {
         this.handleTempPunishment(command, subCommand, chat);
      } else if (chat.matchesCommand(this.infinityPunishments, subCommand) || subCommand.equals("/warn")) {
         this.handleInfinityPunishment(command, subCommand, chat);
      }

      this.finishFreezePunishmentCleanup(subCommand, chat);
   }

   private boolean validatePlayer(String player, String command, ChatService chat) {
      this.nicknameHasChar = false;
      for (char c : chat.Chars) {
         if (player.contains(String.valueOf(c))) {
            this.nicknameHasChar = true;
            break;
         }
      }

      if (this.nicknameHasChar) {
         this.showError("Некорректный никнейм.");
         return false;
      }

      char lastChar = player.charAt(player.length() - 1);
      if (!String.valueOf(lastChar).matches("(?i)[smhd]")
         && chat.isHmCommand(player.substring(0, player.length() - 1))) {
         if (!this.strangePunishmentConfirm) {
            this.strangeMessage = command;
            this.strangePunishmentConfirm = true;
            this.showWarning(
               "§fВы §c§lУВЕРЕНЫ§f, что хотите выдать наказание игроку §a§l" + player
                  + "§f? Если вы §c§lУВЕРЕНЫ§f, то введите команду ещё раз."
            );
            return false;
         }
         this.strangePunishmentConfirm = false;
         this.strangeMessage = "";
      }

      return true;
   }

   private boolean confirmFreezePunishment(String player, String command) {
      if (!player.equals(this.serviceContext.getStateService().getPlayer())) {
         return true;
      }

      if (!this.strangeFrzPunishmentConfirm) {
         this.strangeFrzMessage = command;
         this.strangeFrzPunishmentConfirm = true;
         this.showWarning(
            "§fВы §c§lУВЕРЕНЫ§f, что хотите выдать наказание игроку, который у вас на проверке? Если вы §c§lУВЕРЕНЫ§f, то введите команду ещё раз."
         );
         return false;
      }

      this.strangeFrzMessage = "";
      return true;
   }

   private void handleTempPunishment(String command, String subCommand, ChatService chat) {
      String[] parts = command.split(" ", 4);
      if (parts.length == 1) {
         this.showError("Вы не указали ник игрока, время и причину.");
         return;
      }
      if (parts.length == 2) {
         this.showError("Вы не указали время и причину.");
         return;
      }
      if (parts.length == 3) {
         this.showError("Вы не указали причину.");
         return;
      }

      String player = parts[1];
      String duration = parts[2];
      String reason = parts[3];
      var punishments = this.serviceContext.getPunishmentsService();
      if (chat.matchesCommand(this.muteCommands, subCommand)) {
         if (!punishments.shouldExecutePunishment(subCommand, player, duration, reason, false, this.serviceContext)) {
            return;
         }
      }
      if (chat.matchesCommand(this.banCommands, subCommand)) {
         if (!punishments.shouldExecutePunishment(subCommand, player, duration, reason, true, this.serviceContext)) {
            return;
         }
      }
   }

   private void handleInfinityPunishment(String command, String subCommand, ChatService chat) {
      String[] parts = command.split(" ", 3);
      if (parts.length == 1) {
         this.showError("Вы не указали ник игрока и причину.");
         return;
      }
      if (parts.length == 2) {
         this.showError("Вы не указали причину.");
         return;
      }

      this.serviceContext.getPunishmentsService().executePunishment(
         subCommand,
         parts[1],
         parts[2],
         chat.matchesCommand(this.vkCommands, subCommand),
         this.serviceContext
      );
   }

   private void finishFreezePunishmentCleanup(String subCommand, ChatService chat) {
      if ((chat.matchesCommand(this.banCommands, subCommand) || subCommand.equals("/warn"))
         && this.strangeFrzPunishmentConfirm
         && this.strangeFrzMessage.isEmpty()) {
         this.strangeFrzPunishmentConfirm = false;
         this.serviceContext.getCheckoutsService().init(this.serviceContext);
      }

      if (chat.matchesCommand(this.muteCommands, subCommand)
         && this.strangeFrzPunishmentConfirm
         && this.strangeFrzMessage.isEmpty()) {
         this.strangeFrzPunishmentConfirm = false;
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

   static {
   }
}

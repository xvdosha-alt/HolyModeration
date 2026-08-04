package me.xv.holymoderation.command;

import me.xv.holymoderation.event.ChatMessageEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.util.NotificationType;

public class ReportModule extends BaseCommandHandler {
   private boolean messageIsReportInfo = false;

   @Subscribe(priority = 97)
   public void onReportChatMessage(ChatMessageEvent event) {
      String message = this.serviceContext.getChatService().stripFormatting(event.getMessage().getString());
      if (message == null) {
         return;
      }

      if (message.startsWith("▍ Заявитель:")) {
         this.messageIsReportInfo = true;
      }

      if (message.contains("Подозреваемый:")) {
         String suspect = message.split(": ", 2)[1].split(" ")[0];
         this.serviceContext.getChatService().copyToClipboard(suspect);
         this.serviceContext.getNotificationService().showToast(
            NotificationType.SUCCESS, "§a§lУспех", "Ник игрока из репорта скопирован: " + suspect, 5.0F
         );
      }

      if (this.messageIsReportInfo) {
         event.setCancelled(true);
      }

      if (message.startsWith("▶ [ПКМ]") || message.startsWith("◤          Подано")) {
         this.messageIsReportInfo = false;
      }
   }

   static {
   }
}

package me.xv.holymoderation.command;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.ServerConnectEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.util.NotificationType;

public class UpdaterModule extends BaseCommandHandler {
   private boolean tokenWarningShown = false;

   @Subscribe
   public void onUpdateServerConnect(ServerConnectEvent event) {
      if (!event.isSwitch()) {
         this.syncJournal();
      }
   }

   @Subscribe
   public void onUpdateCommand(CommandEvent event) {
      String command = event.getCommand();
      String[] parts = command.split(" ");
      if (!command.startsWith("hm") || parts.length < 2) {
         return;
      }

      if (parts[1].equals("net")) {
         this.syncJournal();
      }
   }

   private void syncJournal() {
      CompletableFuture.runAsync(this::performJournalSync);
   }

   private void performJournalSync() {
      if (this.serviceContext.getConfigManager().getState().getApiToken().isEmpty()) {
         if (!this.tokenWarningShown) {
            this.tokenWarningShown = true;
            this.serviceContext.getNotificationService().showToast(
               NotificationType.ERROR,
               "§c§lОшибка",
               "У вас не установлен API токен из журнала. Установите его: §6/hm setapitoken §a<token>§f",
               15.0F
            );
         }
         this.serviceContext.getStateService().setBlocked(true);
         return;
      }

      this.tokenWarningShown = false;

      Map<String, Object> profile = this.serviceContext.getNetService().getPlayerHistory();
      if (profile.isEmpty() || profile.get("rank") == null || profile.get("idVk") == null) {
         this.serviceContext.getStateService().setBlocked(true);
         return;
      }

      var state = this.serviceContext.getStateService();
      state.setJournalProfile(profile);
      state.setJournalStats(this.serviceContext.getNetService().getCheckoutSessions());
      state.setRank((int)Double.parseDouble(profile.get("rank").toString()));
      state.setVkUrl("vk.com/id" + (long)Double.parseDouble(profile.get("idVk").toString()));
      this.serviceContext.getNotificationService().showToast(
         NotificationType.SUCCESS, "§a§lУспех", "Синхронизация завершена!", 5.0F
      );
   }
}

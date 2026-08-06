package me.xv.holymoderation.command;

import me.xv.holymoderation.config.ModState;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.ServerConnectEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.util.NotificationType;

public class UpdaterModule extends BaseCommandHandler {
   @Subscribe
   public void onUpdateServerConnect(ServerConnectEvent event) {
      if (!event.isSwitch()) {
         this.syncLocalState();
      }
   }

   @Subscribe
   public void onUpdateCommand(CommandEvent event) {
      String command = event.getCommand();
      String[] parts = command.split(" ");
      if (!command.startsWith("hm") || parts.length < 2) {
         return;
      }

      if (parts[1].equals("net") || parts[1].equals("update")) {
         this.syncLocalState(true);
      }
   }

   private void syncLocalState() {
      this.syncLocalState(false);
   }

   private void syncLocalState(boolean notify) {
      ModState modState = this.serviceContext.getConfigManager().getState();
      var state = this.serviceContext.getStateService();
      if (!modState.getVkUrl().isEmpty()) {
         state.setVkUrl(modState.getVkUrl());
      }
      state.setBlocked(false);

      if (notify) {
         String vk = state.getVkUrl();
         if (vk.isEmpty()) {
            this.serviceContext.getNotificationService().showToast(
               NotificationType.WARNING,
               "§6§lПредупреждение",
               "VK не задан. Укажи: §6/hm setvk vk.com/id<номер>",
               8.0F
            );
            return;
         }

         this.serviceContext.getNotificationService().showToast(
            NotificationType.SUCCESS,
            "§a§lУспех",
            "VK загружен: " + vk,
            5.0F
         );
      }
   }
}

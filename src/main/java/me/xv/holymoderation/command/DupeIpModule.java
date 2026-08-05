package me.xv.holymoderation.command;

import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.ChatMessageEvent;
import me.xv.holymoderation.event.ClientTickEvent;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.Subscribe;

public class DupeIpModule extends BaseCommandHandler {
   @Subscribe(priority = 50)
   public void onCommand(CommandEvent event) {
      if (ServiceRegistry.getDupeIpScannerService().isInternalSend()) {
         return;
      }

      String command = event.getCommand();
      if (!command.startsWith("dupeip ")) {
         return;
      }

      String[] parts = command.split(" ");
      if (parts.length >= 2) {
         ServiceRegistry.getDupeIpScannerService().startScan(parts[1]);
      }
   }

   @Subscribe(priority = 98)
   public void onChatMessage(ChatMessageEvent event) {
      ServiceRegistry.getDupeIpScannerService().handleMessage(event.getMessage());
   }

   @Subscribe
   public void onClientTick(ClientTickEvent event) {
      ServiceRegistry.getDupeIpScannerService().tick();
   }
}

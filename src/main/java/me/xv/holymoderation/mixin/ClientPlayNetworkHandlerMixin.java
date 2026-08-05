package me.xv.holymoderation.mixin;

import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.ChatSendEvent;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.ServerConnectEvent;
import me.xv.holymoderation.service.StateService;
import me.xv.holymoderation.service.TabLocationService;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class, priority = 500)
public class ClientPlayNetworkHandlerMixin {
   @Inject(method = "handleLogin", at = @At("TAIL"))
   private void onGameJoin(CallbackInfo ci) {
      LocalPlayer player = ServiceRegistry.getMinecraftService().getPlayer();
      if (player != null) {
         ServerData serverInfo = player.connection.getServerData();
         if (serverInfo != null) {
            ServiceRegistry.getEventBus().post(new ServerConnectEvent(serverInfo, ServiceRegistry.getStateService().getConnected()));
         }
      }
   }

   @Inject(method = "handleTabListCustomisation", at = @At("TAIL"))
   private void onTabListCustomisation(ClientboundTabListPacket packet, CallbackInfo ci) {
      TabLocationService.updateModerLocation();
   }

   @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
   private void onSendChatMessage(String text, CallbackInfo ci) {
      ChatSendEvent event = new ChatSendEvent(text);
      ServiceRegistry.getEventBus().post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
   private void onSendCommand(String text, CallbackInfo ci) {
      this.interceptModCommand(text, ci);
   }

   @Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true)
   private void onSendUnattendedCommand(String text, Screen screen, CallbackInfo ci) {
      this.interceptModCommand(text, ci);
   }

   private void interceptModCommand(String text, CallbackInfo ci) {
      if (ci.isCancelled()) {
         return;
      }

      String command = normalizeCommand(text);
      if (isModCommand(command)) {
         ServiceRegistry.getEventBus().post(new CommandEvent(command));
         ci.cancel();
         return;
      }

      syncTrackedCommandState(command);
   }

   private static void syncTrackedCommandState(String command) {
      StateService state = ServiceRegistry.getStateService();
      String[] parts = command.split(" ");
      if (parts.length == 0) {
         return;
      }

      switch (parts[0]) {
         case "v":
            if (parts.length == 1) {
               state.setVanishEnabled(!state.getVanishEnabled());
            } else if (parts[1].equals("enable")) {
               state.setVanishEnabled(true);
            } else if (parts[1].equals("disable")) {
               state.setVanishEnabled(false);
            }
            break;
         case "gm":
         case "gamemode":
            if (parts.length > 1) {
               String mode = parts[1];
               if (mode.equals("3") || mode.equals("spectator")) {
                  state.setGm3Enabled(true);
               } else if (mode.equals("0") || mode.equals("1") || mode.equals("2")
                  || mode.equals("survival") || mode.equals("creative") || mode.equals("adventure")) {
                  state.setGm3Enabled(false);
               }
            }
            break;
         case "fly":
            if (parts.length == 1) {
               state.setFlyEnabled(!state.getFlyEnabled());
            } else if (parts.length > 1 && parts[1].equals("enable")) {
               state.setFlyEnabled(true);
            } else if (parts.length > 1 && parts[1].equals("disable")) {
               state.setFlyEnabled(false);
            }
            break;
         case "god":
            if (parts.length == 1) {
               state.setGodEnabled(!state.getGodEnabled());
            } else if (parts.length > 1 && parts[1].equals("enable")) {
               state.setGodEnabled(true);
            } else if (parts.length > 1 && parts[1].equals("disable")) {
               state.setGodEnabled(false);
            }
            break;
         default:
            break;
      }
   }

   private static String normalizeCommand(String text) {
      if (text.startsWith("/")) {
         return text.substring(1);
      }
      return text;
   }

   private static boolean isModCommand(String command) {
      return command.equals("hm")
         || command.startsWith("hm ")
         || command.equals("frz")
         || command.startsWith("frz ")
         || command.equals("freezing")
         || command.startsWith("freezing ");
   }
}

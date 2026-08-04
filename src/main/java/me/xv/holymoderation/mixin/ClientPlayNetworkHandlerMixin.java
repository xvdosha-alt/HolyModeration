package me.xv.holymoderation.mixin;

import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.ChatSendEvent;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.ServerConnectEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ServerData;
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
      if (!isModCommand(command)) {
         return;
      }

      ServiceRegistry.getEventBus().post(new CommandEvent(command));
      ci.cancel();
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

package me.xv.holymoderation.mixin;

import com.mojang.authlib.GameProfile;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.ChatMessageEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class MessageHandlerMixin {
   @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
   private void onSystemMessage(Component message, boolean overlay, CallbackInfo ci) {
      this.dispatch(message, overlay, ci);
   }

   @Inject(method = "handleDisguisedChatMessage", at = @At("HEAD"), cancellable = true)
   private void onDisguisedMessage(Component message, ChatType.Bound bound, CallbackInfo ci) {
      this.dispatch(message, false, ci);
   }

   @Inject(method = "handlePlayerChatMessage", at = @At("HEAD"), cancellable = true)
   private void onPlayerMessage(PlayerChatMessage message, GameProfile profile, ChatType.Bound bound, CallbackInfo ci) {
      Minecraft client = ServiceRegistry.getMinecraftService().getClient();
      PlayerChatMessage filtered = client.options.onlyShowSecureChat().get()
         ? message.removeUnsignedContent()
         : message;
      Component content = bound.decorate(filtered.decoratedContent());
      this.dispatch(content, false, ci, profile.name());
   }

   private void dispatch(Component message, boolean overlay, CallbackInfo ci) {
      this.dispatch(message, overlay, ci, "");
   }

   private void dispatch(Component message, boolean overlay, CallbackInfo ci, String senderName) {
      ChatMessageEvent event = new ChatMessageEvent(message);
      event.setSenderName(senderName);
      ServiceRegistry.getEventBus().post(event);
      ci.cancel();
      if (event.isCancelled()) {
         return;
      }

      Minecraft client = ServiceRegistry.getMinecraftService().getClient();
      if (overlay) {
         client.gui.setOverlayMessage(event.getMessage(), false);
      } else {
         client.gui.getChat().addMessage(event.getMessage());
      }
   }
}

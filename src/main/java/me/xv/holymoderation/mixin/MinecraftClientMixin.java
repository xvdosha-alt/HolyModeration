package me.xv.holymoderation.mixin;

import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
   @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("TAIL"))
   private void onDisconnect(CallbackInfo ci) {
      ServiceRegistry.getEventBus().post(new ClientTickEvent());
   }
}

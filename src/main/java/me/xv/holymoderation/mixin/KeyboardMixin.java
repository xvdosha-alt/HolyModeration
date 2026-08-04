package me.xv.holymoderation.mixin;

import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.KeyPressEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
   @Inject(method = "keyPress", at = @At("TAIL"))
   private void onKey(long window, int action, KeyEvent event, CallbackInfo ci) {
      ServiceRegistry.getEventBus().post(new KeyPressEvent(window, event.key(), event.scancode(), action, event.modifiers()));
   }
}

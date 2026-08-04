package me.xv.holymoderation.mixin;

import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.RenderHudEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {
   @Inject(method = "render", at = @At("TAIL"))
   public void onRenderHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
      ServiceRegistry.getEventBus().post(new RenderHudEvent(context, tickCounter.getGameTimeDeltaPartialTick(false)));
   }
}

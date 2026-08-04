package me.xv.holymoderation.event;

import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;

public class RenderHudEvent extends BaseEvent {
   private final GuiGraphics drawContext;
   private final float tickDelta;

   public RenderHudEvent(GuiGraphics drawContext, float tickDelta) {
      this.drawContext = drawContext;
      this.tickDelta = tickDelta;
   }

   @Generated
   public GuiGraphics getGuiGraphics() {
      return this.drawContext;
   }

   @Generated
   public float getTickDelta() {
      return this.tickDelta;
   }
}

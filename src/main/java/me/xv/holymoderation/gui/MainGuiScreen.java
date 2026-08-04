package me.xv.holymoderation.gui;

import java.awt.Color;
import me.xv.holymoderation.core.ServiceContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class MainGuiScreen extends AnimatedScreen {
   private final Color outlineColor = Color.WHITE;

   public MainGuiScreen(ServiceContext context) {
      super(Component.literal("HolyModeration Main Gui Screen"), context);
      this.tabs.put("General", new GeneralGuiTab(this, context));
   }

   @Override
   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      float baseWidth = context.guiWidth() / 2.2F;
      float baseHeight = context.guiHeight() / 1.8F;
      this.width = baseWidth * this.getOpenProgress();
      this.height = baseHeight * this.getOpenProgress();
      float centerX = context.guiWidth() / 2.0F;
      float centerY = context.guiHeight() / 2.0F;
      this.x = centerX - this.width / 2.0F;
      this.y = centerY - this.height / 2.0F;
      float outlineScale = Math.min(this.width, this.height) / 100.0F;
      this.serviceContext.getRender2DService().drawSoftRoundedRectOutline(
         context,
         this.x,
         this.y,
         Math.max(1.0F, this.width),
         Math.max(1.0F, this.height),
         10.0F,
         new Color(11007),
         this.outlineColor,
         outlineScale,
         3.0F
      );
      super.render(context, mouseX, mouseY, delta);
   }
}

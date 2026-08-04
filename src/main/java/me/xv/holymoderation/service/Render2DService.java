package me.xv.holymoderation.service;

import java.awt.Color;
import me.xv.holymoderation.core.BaseService;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class Render2DService extends BaseService {
   public void drawSoftRoundedRectOutline(
      GuiGraphics graphics,
      float x,
      float y,
      float width,
      float height,
      float radius,
      Color background,
      Color outline,
      float outlineWidth,
      float blurWidth
   ) {
      drawPanel(graphics, x - blurWidth, y - blurWidth, width + blurWidth * 2.0F, height + blurWidth * 2.0F, background, outline, outlineWidth);
   }

   public void drawRoundedRectOutline(
      GuiGraphics graphics,
      float x,
      float y,
      float width,
      float height,
      float radius,
      Color background,
      Color outline,
      float outlineWidth
   ) {
      drawPanel(graphics, x, y, width, height, background, outline, outlineWidth);
   }

   public void fillGradientRect(GuiGraphics graphics, float centerX, float centerY, float radius, Color outlineColor, float outlineWidth) {
      int left = (int)(centerX - radius - outlineWidth);
      int top = (int)(centerY - radius - outlineWidth);
      int size = (int)((radius + outlineWidth) * 2.0F);
      graphics.fill(left, top, left + size, top + size, toArgb(outlineColor));
      graphics.fill(left + 2, top + 2, left + size - 2, top + size - 2, 0xFFFFFFFF);
   }

   public void drawText(Font font, String text, float x, float y, int color, boolean shadow, GuiGraphics graphics) {
      graphics.drawString(font, text, (int)x, (int)y, color, shadow);
   }

   private void drawPanel(GuiGraphics graphics, float x, float y, float width, float height, Color background, Color outline, float outlineWidth) {
      int left = Math.max(0, (int)x);
      int top = Math.max(0, (int)y);
      int right = Math.min(graphics.guiWidth(), (int)(x + Math.max(1.0F, width)));
      int bottom = Math.min(graphics.guiHeight(), (int)(y + Math.max(1.0F, height)));
      if (right <= left || bottom <= top) {
         return;
      }
      int bg = toArgb(background);
      int border = toArgb(outline);
      int borderSize = Math.max(1, (int)outlineWidth);
      graphics.fill(left, top, right, bottom, bg);
      graphics.fill(left, top, right, top + borderSize, border);
      graphics.fill(left, bottom - borderSize, right, bottom, border);
      graphics.fill(left, top, left + borderSize, bottom, border);
      graphics.fill(right - borderSize, top, right, bottom, border);
   }

   private static int toArgb(Color color) {
      return (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
   }
}

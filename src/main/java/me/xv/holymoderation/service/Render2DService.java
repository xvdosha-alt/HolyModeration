package me.xv.holymoderation.service;

import java.awt.Color;
import me.xv.holymoderation.core.BaseService;
import me.xv.holymoderation.gui.HudPanelStyle;
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
      drawModernPanel(graphics, Math.round(x - blurWidth), Math.round(y - blurWidth), Math.round(width + blurWidth * 2.0F), Math.round(height + blurWidth * 2.0F), Math.round(radius), new HudPanelStyle(background, outline, outline, new Color(255, 255, 255, 30), 0, 0xFFFFFFFF, 0xFFB0B0B0));
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
      drawModernPanel(graphics, Math.round(x), Math.round(y), Math.round(width), Math.round(height), Math.round(radius), new HudPanelStyle(background, outline, outline, new Color(255, 255, 255, 20), 0, 0xFFFFFFFF, 0xFFB0B0B0));
   }

   public void drawModernPanel(GuiGraphics graphics, int x, int y, int width, int height, int radius, HudPanelStyle style) {
      if (width <= 0 || height <= 0) {
         return;
      }

      radius = Math.min(radius, Math.min(width, height) / 2);
      int glow = withAlpha(style.outline(), 70);
      fillRoundedRect(graphics, x - 1, y - 1, width + 2, height + 2, radius + 1, glow);
      fillRoundedRect(graphics, x, y, width, height, radius, style.background().getRGB());
      fillRoundedRect(graphics, x + 2, y + 2, width - 4, Math.min(10, height - 4), Math.max(0, radius - 1), style.accentSoft().getRGB());
      graphics.fill(x + 3, y + 3, x + 5, y + height - 3, style.accent().getRGB());
      graphics.fillGradient(x + radius, y + 1, x + width - radius, y + 2, withAlpha(Color.WHITE, 35), withAlpha(Color.WHITE, 0));
      drawRoundedOutline(graphics, x, y, width, height, radius, style.outline().getRGB());
   }

   public void drawToastPanel(GuiGraphics graphics, int x, int y, int width, int height, Color background, Color accent, Color outline) {
      if (width <= 0 || height <= 0) {
         return;
      }

      int radius = 6;
      fillRoundedRect(graphics, x, y, width, height, radius, background.getRGB());
      graphics.fill(x + 3, y + 4, x + 5, y + height - 4, accent.getRGB());
      graphics.fillGradient(x + radius, y + 1, x + width - radius, y + 2, withAlpha(Color.WHITE, 28), withAlpha(Color.WHITE, 0));
      drawRoundedOutline(graphics, x, y, width, height, radius, withAlpha(outline, 180));
   }

   public void fillRoundedRect(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
      if (width <= 0 || height <= 0) {
         return;
      }

      radius = Math.min(radius, Math.min(width, height) / 2);
      graphics.fill(x + radius, y, x + width - radius, y + height, color);
      graphics.fill(x, y + radius, x + width, y + height - radius, color);

      for (int row = 0; row < radius; row++) {
         int inset = cornerInset(radius, row);
         graphics.fill(x + inset, y + row, x + radius, y + row + 1, color);
         graphics.fill(x + width - radius, y + row, x + width - inset, y + row + 1, color);
         graphics.fill(x + inset, y + height - row - 1, x + radius, y + height - row, color);
         graphics.fill(x + width - radius, y + height - row - 1, x + width - inset, y + height - row, color);
      }
   }

   public void fillGradientRect(GuiGraphics graphics, float centerX, float centerY, float radius, Color outlineColor, float outlineWidth) {
      int left = (int)(centerX - radius - outlineWidth);
      int top = (int)(centerY - radius - outlineWidth);
      int size = (int)((radius + outlineWidth) * 2.0F);
      graphics.fill(left, top, left + size, top + size, outlineColor.getRGB());
      graphics.fill(left + 2, top + 2, left + size - 2, top + size - 2, 0xFFFFFFFF);
   }

   public void drawText(Font font, String text, float x, float y, int color, boolean shadow, GuiGraphics graphics) {
      graphics.drawString(font, text, (int)x, (int)y, color, shadow);
   }

   private void drawRoundedOutline(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
      graphics.fill(x + radius, y, x + width - radius, y + 1, color);
      graphics.fill(x + radius, y + height - 1, x + width - radius, y + height, color);
      graphics.fill(x, y + radius, x + 1, y + height - radius, color);
      graphics.fill(x + width - 1, y + radius, x + width, y + height - radius, color);
   }

   private static int cornerInset(int radius, int row) {
      double dy = radius - row - 0.5D;
      double dx = Math.sqrt(Math.max(0.0D, radius * radius - dy * dy));
      return (int)Math.round(radius - dx);
   }

   private static int withAlpha(Color color, int alpha) {
      return (Math.min(255, alpha) << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
   }
}

package me.xv.holymoderation.gui;

import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.service.Render2DService;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class HudPanelRenderer {
   private static final float PADDING_X = 12.0F;
   private static final float PADDING_Y = 9.0F;
   private static final float BADGE_HEIGHT = 11.0F;
   private static final float LINE_GAP = 4.0F;
   private static final int CORNER_RADIUS = 6;

   private HudPanelRenderer() {
   }

   public record Content(String badge, String primary, String secondary) {
   }

   public static float measureWidth(Font font, Content content) {
      float badgeWidth = font.width(content.badge()) + 14.0F;
      float primaryWidth = font.width(stripLegacy(content.primary())) + PADDING_X * 2.0F + 8.0F;
      float width = Math.max(badgeWidth, primaryWidth);
      if (content.secondary() != null && !content.secondary().isEmpty()) {
         width = Math.max(width, font.width(stripLegacy(content.secondary())) + PADDING_X * 2.0F + 8.0F);
      }
      return width;
   }

   public static float measureHeight(Font font, Content content) {
      boolean hasSecondary = content.secondary() != null && !content.secondary().isEmpty();
      float lines = hasSecondary ? 2.0F : 1.0F;
      return PADDING_Y + BADGE_HEIGHT + 6.0F + lines * font.lineHeight + (hasSecondary ? LINE_GAP : 0.0F) + PADDING_Y;
   }

   public static void drawCentered(
      Render2DService render,
      GuiGraphics graphics,
      Font font,
      float centerX,
      float y,
      float width,
      float height,
      HudPanelStyle style,
      Content content
   ) {
      draw(render, graphics, font, centerX - width / 2.0F, y, width, height, style, content);
   }

   public static void draw(
      Render2DService render,
      GuiGraphics graphics,
      Font font,
      float x,
      float y,
      float width,
      float height,
      HudPanelStyle style,
      Content content
   ) {
      int left = Math.round(x);
      int top = Math.round(y);
      int panelWidth = Math.max(1, Math.round(width));
      int panelHeight = Math.max(1, Math.round(height));

      render.drawModernPanel(graphics, left, top, panelWidth, panelHeight, CORNER_RADIUS, style);

      float innerX = left + PADDING_X;
      float badgeWidth = font.width(content.badge()) + 10.0F;
      render.fillRoundedRect(graphics, Math.round(innerX), Math.round(top + PADDING_Y), Math.round(badgeWidth), Math.round(BADGE_HEIGHT), 3, style.accent().getRGB());
      graphics.drawString(font, content.badge(), Math.round(innerX + 5.0F), Math.round(top + PADDING_Y + 1.0F), style.badgeText(), false);

      float textY = top + PADDING_Y + BADGE_HEIGHT + 6.0F;
      graphics.drawString(font, ServiceRegistry.getChatService().legacyComponent(content.primary()), Math.round(innerX), Math.round(textY), style.primaryText(), true);
      if (content.secondary() != null && !content.secondary().isEmpty()) {
         graphics.drawString(font, ServiceRegistry.getChatService().legacyComponent(content.secondary()), Math.round(innerX), Math.round(textY + font.lineHeight + LINE_GAP), style.secondaryText(), true);
      }
   }

   private static String stripLegacy(String text) {
      return text == null ? "" : text.replaceAll("§[0-9a-zA-Z]", "");
   }
}

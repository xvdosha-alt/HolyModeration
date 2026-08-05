package me.xv.holymoderation.gui;

import java.util.EnumMap;
import java.util.Map;
import me.xv.holymoderation.config.ModState;
import me.xv.holymoderation.core.ServiceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

public final class HudPanelLayout {
   private static final float SPY_DEFAULT_TOP = 28.0F;
   private static final float CHECKOUT_BOTTOM_OFFSET = 92.0F;
   private static final HudPanelType[] HIT_TEST_ORDER = new HudPanelType[]{HudPanelType.SPY, HudPanelType.CHECKOUT};

   private static final Map<HudPanelType, Bounds> lastBounds = new EnumMap<>(HudPanelType.class);
   private static HudPanelType dragging;
   private static double dragOffsetX;
   private static double dragOffsetY;
   private static float dragCenterX;
   private static float dragTopY;

   private HudPanelLayout() {
   }

   public record Bounds(float centerX, float topY, float width, float height) {
      public float left() {
         return this.centerX - this.width / 2.0F;
      }

      public float right() {
         return this.centerX + this.width / 2.0F;
      }

      public float bottom() {
         return this.topY + this.height;
      }

      public boolean contains(double mouseX, double mouseY) {
         return mouseX >= this.left()
            && mouseX <= this.right()
            && mouseY >= this.topY
            && mouseY <= this.bottom();
      }
   }

   public static Bounds resolve(HudPanelType type, float screenWidth, float screenHeight, float width, float height) {
      ModState state = ServiceRegistry.getConfigManager().getState();
      float centerX;
      float topY;

      if (dragging == type) {
         centerX = dragCenterX;
         topY = dragTopY;
      } else {
         switch (type) {
            case SPY -> {
               centerX = state.getSpyHudCenterX() != null ? state.getSpyHudCenterX() : screenWidth / 2.0F;
               topY = state.getSpyHudTopY() != null ? state.getSpyHudTopY() : SPY_DEFAULT_TOP;
            }
            case CHECKOUT -> {
               centerX = state.getCheckoutHudCenterX() != null ? state.getCheckoutHudCenterX() : screenWidth / 2.0F;
               topY = state.getCheckoutHudTopY() != null
                  ? state.getCheckoutHudTopY()
                  : screenHeight - height - CHECKOUT_BOTTOM_OFFSET;
            }
            default -> throw new IllegalArgumentException("Unknown panel: " + type);
         }
      }

      Bounds bounds = clamp(new Bounds(centerX, topY, width, height), screenWidth, screenHeight);
      lastBounds.put(type, bounds);
      return bounds;
   }

   public static boolean isChatOpen() {
      return Minecraft.getInstance().screen instanceof ChatScreen;
   }

   public static boolean isDragging() {
      return dragging != null;
   }

   public static boolean isDragging(HudPanelType type) {
      return dragging == type;
   }

   public static boolean isHighlighted(HudPanelType type) {
      if (!isChatOpen()) {
         return false;
      }

      if (dragging == type) {
         return true;
      }

      Bounds bounds = lastBounds.get(type);
      if (bounds == null) {
         return false;
      }

      Minecraft client = Minecraft.getInstance();
      double mouseX = client.mouseHandler.getScaledXPos(client.getWindow());
      double mouseY = client.mouseHandler.getScaledYPos(client.getWindow());
      return bounds.contains(mouseX, mouseY);
   }

   public static boolean onMouseClick(double mouseX, double mouseY, int button) {
      if (!isChatOpen() || button != 0) {
         return false;
      }

      for (HudPanelType type : HIT_TEST_ORDER) {
         Bounds bounds = lastBounds.get(type);
         if (bounds != null && bounds.contains(mouseX, mouseY)) {
            dragging = type;
            dragOffsetX = mouseX - bounds.centerX();
            dragOffsetY = mouseY - bounds.topY();
            dragCenterX = bounds.centerX();
            dragTopY = bounds.topY();
            return true;
         }
      }

      return false;
   }

   public static void onMouseDrag(double mouseX, double mouseY) {
      if (dragging == null) {
         return;
      }

      Bounds bounds = lastBounds.get(dragging);
      if (bounds == null) {
         return;
      }

      Minecraft client = Minecraft.getInstance();
      float screenWidth = client.getWindow().getGuiScaledWidth();
      float screenHeight = client.getWindow().getGuiScaledHeight();
      Bounds dragged = clamp(
         new Bounds(
            (float)(mouseX - dragOffsetX),
            (float)(mouseY - dragOffsetY),
            bounds.width(),
            bounds.height()
         ),
         screenWidth,
         screenHeight
      );
      dragCenterX = dragged.centerX();
      dragTopY = dragged.topY();
      lastBounds.put(dragging, dragged);
   }

   public static void onMouseRelease() {
      if (dragging == null) {
         return;
      }

      ModState state = ServiceRegistry.getConfigManager().getState();
      switch (dragging) {
         case SPY -> {
            state.setSpyHudCenterX(dragCenterX);
            state.setSpyHudTopY(dragTopY);
         }
         case CHECKOUT -> {
            state.setCheckoutHudCenterX(dragCenterX);
            state.setCheckoutHudTopY(dragTopY);
         }
      }

      ServiceRegistry.getConfigManager().save(state);
      dragging = null;
   }

   private static Bounds clamp(Bounds bounds, float screenWidth, float screenHeight) {
      float halfWidth = bounds.width() / 2.0F;
      float centerX = Math.max(halfWidth, Math.min(bounds.centerX(), screenWidth - halfWidth));
      float topY = Math.max(0.0F, Math.min(bounds.topY(), screenHeight - bounds.height()));
      return new Bounds(centerX, topY, bounds.width(), bounds.height());
   }
}

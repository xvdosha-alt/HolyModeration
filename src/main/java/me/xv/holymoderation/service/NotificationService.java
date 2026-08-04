package me.xv.holymoderation.service;

import java.util.ArrayList;
import java.util.List;
import me.xv.holymoderation.core.BaseService;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.util.NotificationType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

public class NotificationService extends BaseService {
   private static final int TEXT_COLOR = 0xFFFFFFFF;
   private final List<ActiveNotification> notificationPool = new ArrayList<>();
   private long lastNano = System.nanoTime();

   public void showToast(NotificationType type, String title, String text, float liveTime) {
      this.notificationPool.add(new ActiveNotification(type, title, text, liveTime));
      ServiceRegistry.getSoundService().playSound(type.getSoundName());
   }

   public void showToastWithAction(NotificationType type, String title, String text, float liveTime, String soundName) {
      this.notificationPool.add(new ActiveNotification(type, title, text, liveTime));
      if (!soundName.isEmpty()) {
         ServiceRegistry.getSoundService().playSound(soundName);
      }
   }

   public void clearToasts() {
      this.notificationPool.clear();
   }

   public void renderToasts(GuiGraphics graphics) {
      float screenWidth = graphics.guiWidth();
      float screenHeight = graphics.guiHeight();
      if (screenWidth < 64.0F || screenHeight < 64.0F) {
         return;
      }

      Render2DService render2DService = ServiceRegistry.getRender2DService();
      Font font = ServiceRegistry.getMinecraftService().getClient().font;
      ChatService chatService = ServiceRegistry.getChatService();
      long now = System.nanoTime();
      float deltaTime = (now - this.lastNano) / 1.0E9F;
      this.lastNano = now;

      float margin = 8.0F;
      float spacing = 10.0F;
      float maxWrapWidth = Math.max(120.0F, screenWidth / 6.0F);
      float cornerRadius = 6.0F;
      float blurWidth = 6.0F;
      float outlineWidth = 1.5F;
      float padding = 8.0F;
      int wrapWidth = Math.max(1, (int)(maxWrapWidth - padding * 2.0F));

      for (ActiveNotification notification : this.notificationPool) {
         notification.titleLines.clear();
         for (String line : notification.title.split("\n")) {
            notification.titleLines.addAll(font.split(chatService.legacyComponent(line), wrapWidth));
         }

         notification.bodyLines.clear();
         for (String line : notification.text.split("\n")) {
            notification.bodyLines.addAll(font.split(chatService.legacyComponent(line), wrapWidth));
         }

         notification.width = maxWrapWidth;
         notification.height = padding
            + notification.titleLines.size() * 9.0F
            + 4.0F
            + notification.bodyLines.size() * 9.0F
            + padding;
      }

      float currentY = screenHeight - margin;
      for (int i = this.notificationPool.size() - 1; i >= 0; i--) {
         ActiveNotification notification = this.notificationPool.get(i);
         notification.targetY = currentY - notification.height;
         currentY -= notification.height + spacing;
         notification.targetX = screenWidth - notification.width - margin;
         if (notification.state == NotificationAnchor.HIDING) {
            notification.targetX = screenWidth + notification.width + 40.0F;
         }
      }

      for (ActiveNotification notification : this.notificationPool) {
         if (notification.state == NotificationAnchor.SPAWNING && !notification.initialized) {
            notification.x = notification.targetX + notification.width + 20.0F;
            notification.y = screenHeight + notification.height + 20.0F;
            notification.initialized = true;
         }
      }

      for (ActiveNotification notification : this.notificationPool) {
         notification.elapsed += deltaTime;
         if (notification.state == NotificationAnchor.IDLE && notification.elapsed >= notification.liveTime) {
            notification.state = NotificationAnchor.HIDING;
         }

         float ySpeed = notification.state == NotificationAnchor.SPAWNING ? 14.0F : 8.0F;
         notification.y += (notification.targetY - notification.y) * Math.min(1.0F, ySpeed * deltaTime);

         float xSpeed = notification.state == NotificationAnchor.HIDING ? 12.0F : 10.0F;
         notification.x += (notification.targetX - notification.x) * Math.min(1.0F, xSpeed * deltaTime);

         if (notification.state == NotificationAnchor.SPAWNING && Math.abs(notification.y - notification.targetY) < 0.5F) {
            notification.state = NotificationAnchor.IDLE;
         }

         if (notification.state == NotificationAnchor.HIDING && notification.x > screenWidth + notification.width * 0.5F) {
            notification.remove = true;
         }
      }

      this.notificationPool.removeIf(NotificationService::shouldRemove);

      for (ActiveNotification notification : this.notificationPool) {
         render2DService.drawSoftRoundedRectOutline(
            graphics,
            notification.x,
            notification.y,
            notification.width,
            notification.height,
            cornerRadius,
            notification.type.getBackgroundColor(),
            notification.type.getOutlineColor(),
            outlineWidth,
            blurWidth
         );

         float textX = notification.x + padding;
         float textY = notification.y + padding;
         for (FormattedCharSequence line : notification.titleLines) {
            graphics.drawString(font, line, (int)textX, (int)textY, TEXT_COLOR, true);
            textY += font.lineHeight;
         }

         textY += 4.0F;
         for (FormattedCharSequence line : notification.bodyLines) {
            graphics.drawString(font, line, (int)textX, (int)textY, TEXT_COLOR, true);
            textY += font.lineHeight;
         }
      }
   }

   private static boolean shouldRemove(ActiveNotification notification) {
      return notification.remove;
   }

   private static class ActiveNotification {
      NotificationType type;
      String title;
      String text;
      float liveTime;
      float elapsed;
      float x;
      float y;
      float targetX;
      float targetY;
      float width;
      float height;
      boolean remove;
      boolean initialized;
      NotificationAnchor state = NotificationAnchor.SPAWNING;
      List<FormattedCharSequence> titleLines = new ArrayList<>();
      List<FormattedCharSequence> bodyLines = new ArrayList<>();

      ActiveNotification(NotificationType type, String title, String text, float liveTime) {
         this.type = type;
         this.title = title;
         this.text = text;
         this.liveTime = liveTime;
         this.x = -5000.0F;
         this.y = -5000.0F;
      }
   }

   private enum NotificationAnchor {
      SPAWNING,
      IDLE,
      HIDING
   }
}

package me.xv.holymoderation.gui;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import me.xv.holymoderation.core.ServiceContext;
import net.minecraft.network.chat.Component;

public class AnimatedScreen extends BaseAnimatedScreen {
   private final float speed = 0.05F;
   private float progress = 0.0F;
   private boolean opening = true;
   private float animValue = 0.0F;
   private ScheduledFuture<?> task;

   protected AnimatedScreen(Component title, ServiceContext context) {
      super(title, context);
   }

   @Override
   protected void init() {
      this.progress = 0.0F;
      this.animValue = 0.0F;
      this.opening = true;
      this.initAnimation();
   }

   private void initAnimation() {
      this.updateAnimation();
      this.task = this.serviceContext.getSchedulerService().getExecutor().scheduleAtFixedRate(this::drawTabs, 0L, 16L, TimeUnit.MILLISECONDS);
   }

   private void updateAnimation() {
      if (this.task != null && !this.task.isCancelled()) {
         this.task.cancel(false);
      }
   }

   private void closeAnimation() {
      this.updateAnimation();
      this.serviceContext.getMinecraftService().getClient().execute(this::drawBackground);
   }

   public void closeScreen() {
      this.opening = false;
   }

   @Override
   public void onClose() {
      this.opening = false;
      this.closeAnimation();
   }

   @Override
   public void resize(int width, int height) {
      this.updateAnimation();
      super.resize(width, height);
   }

   @Generated
   public float getOpenProgress() {
      return this.animValue;
   }

   private void drawBackground() {
      super.onClose();
   }

   private void drawTabs() {
      if (this.opening) {
         this.progress += 0.05F;
         if (this.progress > 1.0F) {
            this.progress = 1.0F;
         }
      } else {
         this.progress -= 0.05F;
         if (this.progress < 0.0F) {
            this.progress = 0.0F;
            this.closeAnimation();
         }
      }

      float eased;
      float maxScale = 1.25F;
      if (this.progress < 0.5F) {
         eased = this.progress / 0.5F * maxScale;
      } else {
         eased = maxScale - (this.progress - 0.5F) / 0.5F * (maxScale - 1.0F);
      }
      this.animValue = eased;
   }
}

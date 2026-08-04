package me.xv.holymoderation.gui;

import java.awt.Color;
import lombok.Generated;
import me.xv.holymoderation.core.ServiceContext;
import net.minecraft.client.gui.GuiGraphics;

public class ColorPickerWidget extends GuiWidget {
   private float centerX;
   private float centerY;
   private float radius;
   private Color outlineColor;
   private float outlineWidth;
   private Color selectedColor = Color.WHITE;

   public ColorPickerWidget(GuiTab parent, ServiceContext context) {
      super(parent, context);
   }

   public void renderColorPicker(GuiGraphics graphics, float centerX, float centerY, float radius, Color outlineColor, float outlineWidth) {
      this.centerX = centerX;
      this.centerY = centerY;
      this.radius = radius;
      this.outlineColor = outlineColor;
      this.outlineWidth = outlineWidth;
      this.serviceContext.getRender2DService().fillGradientRect(graphics, centerX, centerY, radius, outlineColor, outlineWidth);
   }

   public boolean isHovered(double mouseX, double mouseY) {
      float dx = (float)(mouseX - this.centerX);
      float dy = (float)(mouseY - this.centerY);
      float distance = (float)Math.sqrt(dx * dx + dy * dy);
      return distance <= this.radius + this.outlineWidth;
   }

   public void onClick(double mouseX, double mouseY) {
      float dx = (float)(mouseX - this.centerX);
      float dy = (float)(mouseY - this.centerY);
      float distance = (float)Math.sqrt(dx * dx + dy * dy);
      if (distance > this.radius) {
         return;
      }
      float angle = (float)Math.atan2(dy, dx);
      float hue = (angle + (float)Math.PI) / ((float)Math.PI * 2.0F);
      float saturation = Math.min(distance / this.radius, 1.0F);
      this.selectedColor = Color.getHSBColor(hue, saturation, 1.0F);
   }

   @Generated
   public float getHue() {
      return this.centerX;
   }

   @Generated
   public float getSaturation() {
      return this.centerY;
   }

   @Generated
   public float getBrightness() {
      return this.radius;
   }

   @Generated
   public Color getSelectedColor() {
      return this.outlineColor;
   }

   @Generated
   public float getAlpha() {
      return this.outlineWidth;
   }

   @Generated
   public Color getPreviewColor() {
      return this.selectedColor;
   }

   @Generated
   public void setHue(float hue) {
      this.centerX = hue;
   }

   @Generated
   public void setSaturation(float saturation) {
      this.centerY = saturation;
   }

   @Generated
   public void setBrightness(float brightness) {
      this.radius = brightness;
   }

   @Generated
   public void setSelectedColor(Color selectedColor) {
      this.outlineColor = selectedColor;
   }

   @Generated
   public void setAlpha(float alpha) {
      this.outlineWidth = alpha;
   }

   @Generated
   public void setPreviewColor(Color previewColor) {
      this.selectedColor = previewColor;
   }
}

package me.xv.holymoderation.gui;

import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.CommandEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class DupeIpBanConfirmScreen extends Screen {
   private final String command;
   private int boxX;
   private int boxY;
   private final int boxWidth = 260;
   private final int boxHeight = 80;
   private boolean isDragging = false;
   private int dragOffsetX = 0;
   private int dragOffsetY = 0;
   private Button confirmBtn;
   private Button cancelBtn;

   public DupeIpBanConfirmScreen(String command) {
      super(Component.literal("Confirm Ban"));
      this.command = command.startsWith("/") ? command : "/" + command;
   }

   @Override
   protected void init() {
      var state = ServiceRegistry.getConfigManager().getState();
      if (state.getDupeIpBanWindowX() != -1 && state.getDupeIpBanWindowY() != -1) {
         this.boxX = state.getDupeIpBanWindowX();
         this.boxY = state.getDupeIpBanWindowY();
      } else {
         this.boxX = this.width - this.boxWidth - 10;
         this.boxY = this.height - this.boxHeight - 10;
      }

      this.boxX = Math.max(0, Math.min(this.width - this.boxWidth, this.boxX));
      this.boxY = Math.max(0, Math.min(this.height - this.boxHeight, this.boxY));

      this.confirmBtn = Button.builder(
         Component.literal("Забанить"),
         button -> {
            String cmd = this.command.startsWith("/") ? this.command.substring(1) : this.command;
            ServiceRegistry.getEventBus().post(new CommandEvent(cmd));
            ServiceRegistry.getEventBus().post(new CommandEvent(cmd));
            this.onClose();
         }
      ).bounds(this.boxX + 15, this.boxY + 55, 110, 20).build();

      this.cancelBtn = Button.builder(
         Component.literal("Отмена"),
         button -> this.onClose()
      ).bounds(this.boxX + 135, this.boxY + 55, 110, 20).build();

      this.addRenderableWidget(this.confirmBtn);
      this.addRenderableWidget(this.cancelBtn);
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      graphics.fill(this.boxX, this.boxY, this.boxX + this.boxWidth, this.boxY + this.boxHeight, 0xE0000000);
      graphics.fill(this.boxX, this.boxY, this.boxX + this.boxWidth, this.boxY + 1, 0xFF555555);
      graphics.fill(this.boxX, this.boxY + this.boxHeight - 1, this.boxX + this.boxWidth, this.boxY + this.boxHeight, 0xFF555555);
      graphics.fill(this.boxX, this.boxY, this.boxX + 1, this.boxY + this.boxHeight, 0xFF555555);
      graphics.fill(this.boxX + this.boxWidth - 1, this.boxY, this.boxX + this.boxWidth, this.boxY + this.boxHeight, 0xFF555555);
      graphics.fill(this.boxX + 1, this.boxY + 1, this.boxX + this.boxWidth - 1, this.boxY + 14, 0xFFFF5555);
      graphics.drawCenteredString(this.font, "Подтвердить бан", this.boxX + this.boxWidth / 2, this.boxY + 4, 0xFFFFFFFF);
      graphics.drawString(this.font, "Команда:", this.boxX + 15, this.boxY + 22, 0xFFAAAAAA, false);

      String displayCmd = this.command;
      if (this.font.width(this.command) > this.boxWidth - 30) {
         while (this.font.width(displayCmd + "...") > this.boxWidth - 30 && !displayCmd.isEmpty()) {
            displayCmd = displayCmd.substring(0, displayCmd.length() - 1);
         }
         displayCmd += "...";
      }
      graphics.drawString(this.font, displayCmd, this.boxX + 15, this.boxY + 34, 0xFFFFAA00, false);
      super.render(graphics, mouseX, mouseY, partialTick);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      if (ServiceRegistry.getConfigManager().getState().getDupeIpDraggableBanWindow()) {
         double mouseX = event.x();
         double mouseY = event.y();
         if (mouseX >= this.boxX && mouseX <= this.boxX + this.boxWidth && mouseY >= this.boxY && mouseY <= this.boxY + 14) {
            this.isDragging = true;
            this.dragOffsetX = (int)mouseX - this.boxX;
            this.dragOffsetY = (int)mouseY - this.boxY;
            return true;
         }
      }
      return super.mouseClicked(event, doubled);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double offsetX, double offsetY) {
      if (this.isDragging) {
         double mouseX = event.x();
         double mouseY = event.y();
         this.boxX = (int)mouseX - this.dragOffsetX;
         this.boxY = (int)mouseY - this.dragOffsetY;
         this.boxX = Math.max(0, Math.min(this.width - this.boxWidth, this.boxX));
         this.boxY = Math.max(0, Math.min(this.height - this.boxHeight, this.boxY));
         this.confirmBtn.setX(this.boxX + 15);
         this.confirmBtn.setY(this.boxY + 55);
         this.cancelBtn.setX(this.boxX + 135);
         this.cancelBtn.setY(this.boxY + 55);
         return true;
      }
      return super.mouseDragged(event, offsetX, offsetY);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.isDragging) {
         var state = ServiceRegistry.getConfigManager().getState();
         state.setDupeIpBanWindowX(this.boxX);
         state.setDupeIpBanWindowY(this.boxY);
         ServiceRegistry.getConfigManager().save(state);
      }
      this.isDragging = false;
      return super.mouseReleased(event);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}

package me.xv.holymoderation.command;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import me.xv.holymoderation.event.ChatMessageEvent;
import me.xv.holymoderation.event.ClientTickEvent;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.RenderHudEvent;
import me.xv.holymoderation.event.ServerConnectEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.service.Render2DService;
import me.xv.holymoderation.service.StateService;
import me.xv.holymoderation.util.NotificationType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;

public class SpyModule extends BaseCommandHandler {
   private boolean enabled = false;
   private boolean checkingSpy = false;
   private boolean processingPlaytimeInfo = false;
   private boolean shouldUpdate = false;
   private boolean instantUpdate = false;
   private String lastKnownLocation = "";
   private float anim = 0.0F;
   private float animTarget = 0.0F;
   private float currentWidth = 1.0F;
   private float currentHeight = 1.0F;
   private String display0 = "";
   private String display1 = "";
   private boolean clearDisplayWhenHidden = false;

   @Subscribe
   public void onSpyCommand(CommandEvent event) {
      String command = event.getCommand();
      String[] parts = command.split(" ", 3);
      if (!command.startsWith("hm") || parts.length < 2) {
         return;
      }

      StateService state = this.serviceContext.getStateService();
      if (parts[1].equals("spy")) {
         if (parts.length == 3 && this.isSpyFreezeArg(parts[2])) {
            this.handleSpyFreeze(state);
            return;
         }

         if (parts.length == 2) {
            if (!state.getSpyPlayer().isEmpty()) {
               this.toggleSpy();
            } else {
               this.showWarning("Вы никого не отслеживаете.");
            }
            return;
         }

         if (state.getInHub()) {
            this.showWarning("В хабе этого делать нельзя.");
            return;
         }

         if (!state.getPlayer().isEmpty() && state.getPlayer().equals(parts[2])) {
            this.showWarning("Вы не можете начать следить за игроком на вашей проверке.");
            return;
         }

         if (!state.getSpyPlayer().isEmpty()) {
            this.showWarning("Вы уже следите за кем-то --> §6§l/hm spy§f§c§l.");
            return;
         }

         this.handleSpyCommand(parts[2]);
      } else if (parts[1].equals("spyfrz")) {
         if (state.getInHub()) {
            this.showWarning("В хабе этого делать нельзя.");
            return;
         }

         if (state.getSpyPlayer().isEmpty()) {
            this.serviceContext.getNotificationService().showToast(
               NotificationType.ERROR, "§c§lОшибка", "Вы ни за кем не следите.", 5.0F
            );
            return;
         }

         if (this.serviceContext.getCheckoutsService().startCheckout(state.getSpyPlayer(), this.serviceContext)) {
            this.toggleSpy();
         }
      }
   }

   private boolean isSpyFreezeArg(String arg) {
      return arg.equalsIgnoreCase("frz")
         || arg.equalsIgnoreCase("freeze")
         || arg.equalsIgnoreCase("freezing")
         || arg.equalsIgnoreCase("фриз");
   }

   private void handleSpyFreeze(StateService state) {
      if (state.getInHub()) {
         this.showWarning("В хабе этого делать нельзя.");
         return;
      }

      if (state.getSpyPlayer().isEmpty()) {
         this.serviceContext.getNotificationService().showToast(
            NotificationType.ERROR, "§c§lОшибка", "Вы ни за кем не следите.", 5.0F
         );
         return;
      }

      if (this.serviceContext.getCheckoutsService().startCheckout(state.getSpyPlayer(), this.serviceContext)) {
         this.toggleSpy();
      }
   }

   @Subscribe(priority = 99)
   public void onSpyChatMessage(ChatMessageEvent event) {
      String message = this.serviceContext.getChatService().stripFormatting(event.getMessage().getString());
      if (message == null) {
         return;
      }

      if (!this.checkingSpy) {
         return;
      }

      if (message.startsWith("----------")) {
         if (this.processingPlaytimeInfo) {
            this.checkingSpy = false;
            this.shouldUpdate = true;
            this.processingPlaytimeInfo = false;
         }
         this.processingPlaytimeInfo = true;
      }

      StateService state = this.serviceContext.getStateService();
      if (message.startsWith("Игрок") && !message.startsWith("Игрок " + state.getModerNickname())) {
         event.setCancelled(true);
         if (message.equals("Игрок оффлайн")) {
            state.setSpyPlayerStatus("offline");
         } else if (message.contains("сервере ")) {
            String server = message.split("сервере ", 2)[1];
            if (server.startsWith("lobby")) {
               state.setSpyPlayerStatus("lobby");
            } else {
               state.setSpyPlayerStatus(this.serviceContext.getChatService().normalizeServerLocation(server));
            }
         }
         state.setSpyPlayerActivity("");
         this.checkingSpy = false;
         this.shouldUpdate = true;
      }

      if (message.startsWith("Текущая")) {
         String location = this.serviceContext.getChatService().parsePlaytimeLocation(message);
         if (location != null) {
            if (location.equalsIgnoreCase("Оффлайн")) {
               state.setSpyPlayerStatus("offline");
               state.setSpyPlayerActivity("");
               this.instantUpdate = true;
            } else if (location.toLowerCase().startsWith("lobby")) {
               state.setSpyPlayerStatus("lobby");
               state.setSpyPlayerActivity("");
               this.instantUpdate = true;
            } else {
               String normalized = this.serviceContext.getChatService().normalizeServerLocation(location);
               if (this.lastKnownLocation.isEmpty()) {
                  this.lastKnownLocation = normalized;
               }
               if (!normalized.equals(this.lastKnownLocation)) {
                  state.setSpyPlayerStatus("");
                  this.lastKnownLocation = normalized;
               } else {
                  state.setSpyPlayerStatus(normalized);
               }
            }
         }
      }

      if (message.startsWith("Последняя") && !state.getSpyPlayerStatus().isEmpty()) {
         state.setSpyPlayerActivity(message.split(": ", 2)[1]);
      }

      if (message.startsWith("Активность")
         || message.startsWith("Общее время")
         || message.startsWith("Текущая")
         || message.startsWith("Время")
         || message.startsWith("Последняя")
         || message.startsWith("Последний")
         || message.startsWith("----------")
         || message.isEmpty()) {
         event.setCancelled(true);
      }

      if (this.shouldUpdate) {
         if (this.instantUpdate
            || (state.getModerLocation().equals(state.getSpyPlayerStatus()) && state.getSpyPlayerActivity().isEmpty())) {
            this.instantUpdate = true;
         }

         long delay = this.instantUpdate
            ? 500L
            : (long)this.serviceContext.getConfigManager().getState().getSpyDelay();
         TimeUnit unit = this.instantUpdate ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS;
         this.serviceContext.getSchedulerService().getExecutor().schedule(this::updateSpyTargets, delay, unit);
         this.instantUpdate = false;
         this.shouldUpdate = false;
      }
   }

   @Subscribe
   public void onSpyServerConnect(ServerConnectEvent event) {
      if (event.isSwitch()) {
         this.initSpy();
      }
   }

   @Subscribe
   public void onSpyTick(ClientTickEvent event) {
      if (this.enabled && this.serviceContext.getStateService().getSpyPlayer().isEmpty()) {
         this.toggleSpy();
      }
   }

   @Subscribe
   public void onSpyRenderHud(RenderHudEvent event) {
      this.anim += (this.animTarget - this.anim) * 0.15F;
      if (this.anim < 0.01F && this.display0.isEmpty() && this.display1.isEmpty()) {
         return;
      }

      String[] commands = this.getSpyCommands();
      if (!commands[0].isEmpty() || !commands[1].isEmpty()) {
         this.display0 = commands[0];
         this.display1 = commands[1];
      }

      GuiGraphics graphics = event.getGuiGraphics();
      Font font = this.serviceContext.getMinecraftService().getClient().font;

      int width0 = font.width(this.display0);
      int width1 = font.width(this.display1);
      float boxWidth = Math.max(width0, width1) + 16.0F;
      int lines = this.display1.isEmpty() ? 1 : 2;
      int contentHeight = lines * 9 + (lines == 2 ? 4 : 0);
      float boxHeight = contentHeight + 12.0F;

      this.currentWidth += (boxWidth - this.currentWidth) * 0.2F;
      this.currentHeight += (boxHeight - this.currentHeight) * 0.2F;

      float renderWidth = Math.max(1.0F, this.currentWidth * this.anim);
      float renderHeight = Math.max(1.0F, this.currentHeight * this.anim);
      float centerX = graphics.guiWidth() / 2.0F;
      float x = centerX - renderWidth / 2.0F;
      float y = 30.0F;

      Color background = new Color(10, 20, 40, 220);
      Color outline = new Color(60, 120, 220);
      Render2DService render = this.serviceContext.getRender2DService();
      render.drawSoftRoundedRectOutline(graphics, x, y, renderWidth, renderHeight, 10.0F, background, outline, 1.5F, 3.0F);

      float textY = y + 6.0F;
      render.drawText(font, this.display0, centerX - font.width(this.display0) / 2.0F, textY, -1, false, graphics);
      if (!this.display1.isEmpty()) {
         render.drawText(font, this.display1, centerX - font.width(this.display1) / 2.0F, textY + 13.0F, -1, false, graphics);
      }

      if (this.anim < 0.02F && this.animTarget == 0.0F && this.clearDisplayWhenHidden) {
         this.display0 = "";
         this.display1 = "";
         this.clearDisplayWhenHidden = false;
      }
   }

   @NotNull
   private String[] getSpyCommands() {
      StateService state = this.serviceContext.getStateService();
      String spyPlayer = state.getSpyPlayer();
      if (spyPlayer.isEmpty()) {
         return new String[]{"", ""};
      }

      String status = state.getSpyPlayerStatus();
      if (status.isEmpty()) {
         return new String[]{spyPlayer, ""};
      }

      return switch (status) {
         case "stop" -> new String[]{"Слежка приостановлена", ""};
         case "offline" -> new String[]{"Игрок " + spyPlayer + " оффлайн", ""};
         case "lobby" -> new String[]{"Игрок " + spyPlayer + " в лобби", ""};
         default -> {
            String activity = state.getSpyPlayerActivity();
            if (activity == null) {
               activity = "";
            }
            yield new String[]{
               "Игрок " + spyPlayer + " находится на " + status,
               activity.isEmpty() ? "" : "Активность: " + activity
            };
         }
      };
   }

   private void initSpy() {
      this.serviceContext.getSchedulerService().getExecutor().schedule(this::clearSpyData, 50L, TimeUnit.MILLISECONDS);
   }

   private void handleSpyCommand(String player) {
      this.resetSpyState();
      this.serviceContext.getStateService().setSpyPlayer(player);
      this.enabled = true;
      this.animTarget = 1.0F;

      String[] commands = this.getSpyCommands();
      this.display0 = commands[0];
      this.display1 = commands[1];
      this.currentWidth = Math.max(1.0F, this.currentWidth);
      this.currentHeight = Math.max(1.0F, this.currentHeight);

      this.serviceContext.getSchedulerService().getExecutor().schedule(this::updateSpyTargets, 250L, TimeUnit.MILLISECONDS);
      this.serviceContext.getNotificationService().showToast(
         NotificationType.SUCCESS, "§a§lУспех", "Слежка начата", 5.0F
      );
   }

   private void toggleSpy() {
      this.resetSpyState();
      this.serviceContext.getNotificationService().showToast(
         NotificationType.SUCCESS, "§a§lУспех", "Слежка остановлена.", 5.0F
      );
   }

   private void resetSpyState() {
      StateService state = this.serviceContext.getStateService();
      state.setSpyPlayer("");
      this.enabled = false;
      this.animTarget = 0.0F;

      String[] commands = this.getSpyCommands();
      this.display0 = commands[0];
      this.display1 = commands[1];
      this.lastKnownLocation = "";
      state.setSpyPlayerActivity("");
      state.setSpyPlayerStatus("");
      this.clearDisplayWhenHidden = true;
   }

   private void updateSpyTargets() {
      if (!this.enabled) {
         return;
      }

      this.checkingSpy = true;
      StateService state = this.serviceContext.getStateService();
      if (!state.getInHub() && state.getGameInitCompleted()) {
         this.serviceContext.getChatService().sendChatOrCommand("/playtime " + state.getSpyPlayer());
      }
   }

   private void clearSpyData() {
      StateService state = this.serviceContext.getStateService();
      if (!state.getGameInitCompleted()) {
         this.initSpy();
         return;
      }

      if (!this.enabled) {
         return;
      }

      if (state.getInHub()) {
         this.lastKnownLocation = "";
         state.setSpyPlayerActivity("");
         state.setSpyPlayerStatus("stop");
         this.serviceContext.getNotificationService().showToast(
            NotificationType.SUCCESS, "§a§lУспех", "Слежка приостановлена.", 5.0F
         );
      } else {
         if (!state.getModerLocation().isEmpty()) {
            this.updateSpyTargets();
         }
         this.serviceContext.getNotificationService().showToast(
            NotificationType.SUCCESS, "§a§lУспех", "Слежка возобновлена.", 5.0F
         );
      }

      this.initSpy();
   }

   private void showWarning(String message) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.WARNING, "§6§lПредупреждение", message, 5.0F
      );
   }

   static {
   }
}

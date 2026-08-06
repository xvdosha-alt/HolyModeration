package me.xv.holymoderation.command;

import java.util.concurrent.TimeUnit;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.ChatMessageEvent;
import me.xv.holymoderation.event.ClientTickEvent;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.RenderHudEvent;
import me.xv.holymoderation.event.ServerConnectEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.gui.HudPanelLayout;
import me.xv.holymoderation.gui.HudPanelRenderer;
import me.xv.holymoderation.gui.HudPanelStyle;
import me.xv.holymoderation.gui.HudPanelType;
import me.xv.holymoderation.service.ChatService;
import me.xv.holymoderation.service.DebugLogService;
import me.xv.holymoderation.service.Render2DService;
import me.xv.holymoderation.service.StateService;
import me.xv.holymoderation.service.TabLocationService;
import me.xv.holymoderation.util.NotificationType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

public class SpyModule extends BaseCommandHandler {
   private boolean enabled = false;
   private boolean checkingSpy = false;
   private boolean processingPlaytimeInfo = false;
   private boolean shouldUpdate = false;
   private String lastKnownLocation = "";
   private float anim = 0.0F;
   private float animTarget = 0.0F;
   private float currentWidth = 1.0F;
   private float currentHeight = 1.0F;
   private String display0 = "";
   private String display1 = "";
   private boolean clearDisplayWhenHidden = false;
   private boolean updateScheduled = false;

   private DebugLogService debug() {
      return ServiceRegistry.getDebugLogService();
   }

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

      if (!this.checkingSpy && !this.enabled) {
         return;
      }

      ChatService chat = this.serviceContext.getChatService();
      StateService state = this.serviceContext.getStateService();

      if (!this.checkingSpy) {
         return;
      }

      if (chat.isPlaytimeBlockLine(message)) {
         if (this.processingPlaytimeInfo) {
            this.checkingSpy = false;
            this.shouldUpdate = true;
            this.processingPlaytimeInfo = false;
            this.debug().write("spy", "playtime block end");
         } else {
            this.processingPlaytimeInfo = true;
            this.debug().write("spy", "playtime block start");
         }
      }

      if (message.startsWith("Лобби ▶") || message.startsWith("Вы были кикнуты")) {
         this.pauseSpyInHub(state);
      }

      if (message.startsWith("Текущая")) {
         String location = chat.parsePlaytimeLocation(message);
         if (location != null) {
            if (location.equalsIgnoreCase("Оффлайн")) {
               state.setSpyPlayerStatus("offline");
               state.setSpyPlayerActivity("");
            } else             if (location.toLowerCase().startsWith("lobby")) {
               state.setSpyPlayerStatus("lobby");
               state.setSpyPlayerActivity("");
            } else {
               String normalized = chat.normalizeServerLocation(location);
               this.lastKnownLocation = normalized;
               state.setSpyPlayerStatus(normalized);
            }
            this.debug().write("spy", "location=" + location + " status=" + state.getSpyPlayerStatus());
         }
      }

      if (message.startsWith("Последняя активность:")) {
         state.setSpyPlayerActivity(message.split(": ", 2)[1]);
         this.debug().write("spy", "activity=" + state.getSpyPlayerActivity());
      }

      if (this.shouldHideSpyLine(message, chat, state)) {
         event.setCancelled(true);
         this.debug().write("spy-chat", "hidden=" + message);
      }

      if (this.shouldUpdate) {
         this.shouldUpdate = false;
         this.scheduleNextSpyPoll(state);
      }
   }

   private boolean isSpyRelatedLine(String message, ChatService chat, StateService state) {
      return chat.isPlaytimeOutputLine(message) || chat.isPlaytimeBlockLine(message);
   }

   private boolean shouldHideSpyLine(String message, ChatService chat, StateService state) {
      if (!this.isSpyRelatedLine(message, chat, state)) {
         return false;
      }

      return this.checkingSpy || this.processingPlaytimeInfo;
   }

   private void pauseSpyInHub(StateService state) {
      if (!this.enabled) {
         return;
      }

      state.setInHub(true);
      this.checkingSpy = false;
      this.processingPlaytimeInfo = false;
      this.shouldUpdate = false;
      if (!"stop".equals(state.getSpyPlayerStatus())) {
         state.setSpyPlayerActivity("");
         state.setSpyPlayerStatus("stop");
      }
      this.debug().write("spy", "pause: hub/lobby detected");
   }

   private void scheduleNextSpyPoll(StateService state) {
      if (!this.enabled || this.isModerInHub(state) || "stop".equals(state.getSpyPlayerStatus())) {
         this.debug().write("spy", "skip schedule: enabled=" + this.enabled + " hub=" + this.isModerInHub(state));
         return;
      }

      long delaySeconds = Math.max(1L, (long)this.serviceContext.getConfigManager().getState().getSpyDelay());
      String status = state.getSpyPlayerStatus();
      if ("offline".equals(status) || "lobby".equals(status)) {
         delaySeconds = Math.max(delaySeconds, 5L);
      }

      this.scheduleSpyUpdate(delaySeconds, TimeUnit.SECONDS);
      this.debug().write("spy", "schedule update delay=" + delaySeconds + "S");
   }

   private void scheduleSpyUpdate(long delay, TimeUnit unit) {
      if (!this.enabled) {
         return;
      }

      StateService state = this.serviceContext.getStateService();
      if (this.isModerInHub(state) || "stop".equals(state.getSpyPlayerStatus())) {
         this.debug().write("spy", "skip schedule: paused");
         return;
      }

      if (this.updateScheduled) {
         this.debug().write("spy", "skip duplicate schedule");
         return;
      }

      this.updateScheduled = true;
      this.serviceContext.getSchedulerService().getExecutor().schedule(() -> {
         this.updateScheduled = false;
         this.updateSpyTargets();
      }, delay, unit);
   }

   @Subscribe(priority = 102)
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
      if (me.xv.holymoderation.core.ModBuild.BARE) {
         return;
      }
      this.anim += (this.animTarget - this.anim) * 0.15F;
      if (this.anim < 0.01F && this.display0.isEmpty() && this.display1.isEmpty()) {
         return;
      }

      HudPanelRenderer.Content content = this.buildSpyHudContent();
      if (!content.primary().isEmpty()) {
         this.display0 = content.primary();
         this.display1 = content.secondary() == null ? "" : content.secondary();
      }

      GuiGraphics graphics = event.getGuiGraphics();
      Font font = this.serviceContext.getMinecraftService().getClient().font;
      Render2DService render = this.serviceContext.getRender2DService();

      float boxWidth = HudPanelRenderer.measureWidth(font, content);
      float boxHeight = HudPanelRenderer.measureHeight(font, content);
      this.currentWidth += (boxWidth - this.currentWidth) * 0.2F;
      this.currentHeight += (boxHeight - this.currentHeight) * 0.2F;

      float renderWidth = Math.max(1.0F, this.currentWidth * this.anim);
      float renderHeight = Math.max(1.0F, this.currentHeight * this.anim);
      float screenWidth = graphics.guiWidth();
      float screenHeight = graphics.guiHeight();
      HudPanelLayout.Bounds bounds = HudPanelLayout.resolve(
         HudPanelType.SPY,
         screenWidth,
         screenHeight,
         renderWidth,
         renderHeight
      );

      HudPanelRenderer.drawCentered(
         render,
         graphics,
         font,
         bounds.centerX(),
         bounds.topY(),
         renderWidth,
         renderHeight,
         HudPanelStyle.spy(),
         content,
         HudPanelType.SPY
      );

      if (this.anim < 0.02F && this.animTarget == 0.0F && this.clearDisplayWhenHidden) {
         this.display0 = "";
         this.display1 = "";
         this.clearDisplayWhenHidden = false;
      }
   }

   private HudPanelRenderer.Content buildSpyHudContent() {
      StateService state = this.serviceContext.getStateService();
      String spyPlayer = state.getSpyPlayer();
      if (spyPlayer.isEmpty() && this.display0.isEmpty()) {
         return new HudPanelRenderer.Content("SPY", "", "");
      }

      if (spyPlayer.isEmpty()) {
         return new HudPanelRenderer.Content("SPY", this.display0, this.display1);
      }

      String status = state.getSpyPlayerStatus();
      if (status.isEmpty()) {
         return new HudPanelRenderer.Content("SPY", "§f" + spyPlayer, "§7Получение данных...");
      }

      return switch (status) {
         case "stop" -> new HudPanelRenderer.Content("SPY", "§f" + spyPlayer, "§6Слежка приостановлена");
         case "offline" -> new HudPanelRenderer.Content("SPY", "§f" + spyPlayer, "§cОффлайн");
         case "lobby" -> new HudPanelRenderer.Content("SPY", "§f" + spyPlayer, "§eВ лобби");
         default -> {
            String activity = state.getSpyPlayerActivity();
            if (activity == null || activity.isEmpty()) {
               yield new HudPanelRenderer.Content("SPY", "§f" + spyPlayer + " §8→ §b" + status, "§7Активность неизвестна");
            }
            yield new HudPanelRenderer.Content("SPY", "§f" + spyPlayer + " §8→ §b" + status, "§7Активность: §f" + activity);
         }
      };
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
      this.debug().write("spy", "start player=" + player);

      String[] commands = this.getSpyCommands();
      this.display0 = commands[0];
      this.display1 = commands[1];
      this.currentWidth = Math.max(1.0F, this.currentWidth);
      this.currentHeight = Math.max(1.0F, this.currentHeight);

      this.scheduleSpyUpdate(250L, TimeUnit.MILLISECONDS);
      this.serviceContext.getNotificationService().showToast(
         NotificationType.SUCCESS, "§a§lУспех", "Слежка начата", 5.0F
      );
   }

   private void toggleSpy() {
      this.debug().write("spy", "stop");
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
      this.checkingSpy = false;
      this.processingPlaytimeInfo = false;
      this.shouldUpdate = false;
      this.updateScheduled = false;

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

      StateService state = this.serviceContext.getStateService();
      if (this.isModerInHub(state) || "stop".equals(state.getSpyPlayerStatus())) {
         this.checkingSpy = false;
         this.debug().write("spy", "skip playtime: paused hub=" + state.getInHub() + " status=" + state.getSpyPlayerStatus());
         return;
      }

      if (this.checkingSpy) {
         this.debug().write("spy", "skip playtime: waiting for response");
         return;
      }

      if (!state.getGameInitCompleted()) {
         this.debug().write("spy", "skip playtime: init=false");
         return;
      }

      this.checkingSpy = true;
      this.processingPlaytimeInfo = false;
      this.debug().write("spy", "send /playtime " + state.getSpyPlayer());
      this.serviceContext.getChatService().sendChatOrCommand("/playtime " + state.getSpyPlayer());
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

      if (this.isModerInHub(state)) {
         if (!"stop".equals(state.getSpyPlayerStatus())) {
            this.lastKnownLocation = "";
            state.setSpyPlayerActivity("");
            state.setSpyPlayerStatus("stop");
            this.serviceContext.getNotificationService().showToast(
               NotificationType.SUCCESS, "§a§lУспех", "Слежка приостановлена.", 5.0F
            );
         }
         return;
      }

      TabLocationService.updateModerLocation(this.serviceContext);
      boolean wasPaused = "stop".equals(state.getSpyPlayerStatus());
      if (wasPaused) {
         state.setSpyPlayerStatus("");
         if (!state.getModerLocation().isEmpty()) {
            this.serviceContext.getNotificationService().showToast(
               NotificationType.SUCCESS, "§a§lУспех", "Слежка возобновлена.", 5.0F
            );
         }
      }

      this.updateSpyTargets();
   }

   private boolean isModerInHub(StateService state) {
      if (state.getInHub()) {
         return true;
      }

      Minecraft client = this.serviceContext.getMinecraftService().getClient();
      return client.gameMode != null && client.gameMode.getPlayerMode() == GameType.SPECTATOR;
   }

   private void showWarning(String message) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.WARNING, "§6§lПредупреждение", message, 5.0F
      );
   }

   static {
   }
}

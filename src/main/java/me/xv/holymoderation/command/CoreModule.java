package me.xv.holymoderation.command;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import me.xv.holymoderation.service.NetService;
import me.xv.holymoderation.config.ModState;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.service.DebugLogService;
import me.xv.holymoderation.event.ChatMessageEvent;
import me.xv.holymoderation.event.ClientTickEvent;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.RenderHudEvent;
import me.xv.holymoderation.event.ServerConnectEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.service.ChatService;
import me.xv.holymoderation.service.ModerPlaytimeService;
import me.xv.holymoderation.service.StateService;
import me.xv.holymoderation.service.TabLocationService;
import me.xv.holymoderation.util.NotificationType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

public class CoreModule extends BaseCommandHandler {
   private int tabLocationScanTicks;

   @Subscribe(priority = 101)
   public void onServerConnect(ServerConnectEvent event) {
      if (event.isSwitch()) {
         return;
      }

      var minecraft = this.serviceContext.getMinecraftService();
      StateService state = this.serviceContext.getStateService();
      if (minecraft.getPlayer() != null) {
         state.setModerNickname(minecraft.getPlayer().getName().getString());
      }

      state.setConnected(true);
      this.onReconnect(event);

      if (!state.isOnHW()) {
         state.setBlocked(true);
      } else if (state.getBlocked() && state.getEnabled()) {
         state.setBlocked(false);
         this.serviceContext.getEventBus().post(event);
      }
   }

   @Subscribe(priority = 100)
   public void onCoreServerConnect(ServerConnectEvent event) {
      StateService state = this.serviceContext.getStateService();
      if (state.getBlocked()) {
         return;
      }

      state.setGameInitCompleted(false);
      Minecraft client = this.serviceContext.getMinecraftService().getClient();
      if (client.gameMode != null
         && client.gameMode.getPlayerMode() == GameType.SPECTATOR) {
         state.setInHub(true);
         state.setModerLocation("");
      } else {
         state.setInHub(false);
         if (state.getModerLocation().isEmpty() || event.isSwitch()) {
            if (event.isSwitch()) {
               state.setModerLocation("");
               state.setModerLocationTrusted(false);
            }
            TabLocationService.updateModerLocation(this.serviceContext);
            if (state.getModerLocation().isEmpty()) {
               ModerPlaytimeService.requestModerLocation(this.serviceContext, true);
            }
         }
      }

      if (event.isSwitch() && !state.getInHub()) {
         this.applyAutoModerSettings(state);
      }

      state.setGameInitCompleted(true);
   }

   @Subscribe(priority = 100)
   public void onClientTick(ClientTickEvent event) {
      StateService state = this.serviceContext.getStateService();
      if (state.getConnected()) {
         state.setConnected(false);
         this.serviceContext.getNotificationService().clearToasts();
      }

      if (!state.isOnHW() || state.getBlocked() || !state.getGameInitCompleted()) {
         return;
      }

      this.tabLocationScanTicks++;
      if (this.tabLocationScanTicks % 10 != 0) {
         return;
      }

      if (state.getModerLocation().isEmpty()) {
         TabLocationService.updateModerLocation(this.serviceContext);
      }

      if (state.getModerLocation().isEmpty() && !state.getInHub() && this.tabLocationScanTicks % 20 == 0) {
         ModerPlaytimeService.requestModerLocation(this.serviceContext);
      }
   }

   @Subscribe(priority = 120)
   public void onGlobalCommand(CommandEvent event) {
      String command = event.getCommand();
      String[] parts = command.split(" ");
      if (!command.startsWith("hm")) {
         return;
      }

      if (parts.length == 1) {
         this.serviceContext.getChatService().sendMessage(
            Component.literal("§eHolyModeration: §6/hm enable§f, §6/hm stats§f, §6/hm setvk <vk.com/id>§f, §6/hm setapitoken <token>")
         );
         return;
      }

      String subCommand = parts[1];
      switch (subCommand) {
         case "setapitoken":
            this.handleSetApiTokenCommand(parts);
            return;
         case "setvk":
            this.handleSetVkCommand(parts);
            return;
         case "cleartoasts":
            this.serviceContext.getNotificationService().clearToasts();
            this.serviceContext.getChatService().sendMessage(Component.literal("§aУведомления очищены."));
            return;
         case "enable":
            this.handleEnableCommand(this.serviceContext.getStateService());
            return;
         case "disable":
            this.handleDisableCommand(event, this.serviceContext.getStateService());
            return;
         case "debug":
            this.handleDebugCommand(parts);
            return;
         default:
            break;
      }
   }

   @Subscribe(priority = 100)
   public void onCommand(CommandEvent event) {
      if (!this.serviceContext.getStateService().isOnHW()) {
         return;
      }

      StateService state = this.serviceContext.getStateService();
      if (!state.getGameInitCompleted()) {
         this.showError("Не спеши, инициализация игры ещё не завершилась!");
         return;
      }

      if (state.getCheckingTwinks()) {
         this.showError("Дождитесь окончания проверки твинков.");
         event.setCancelled(true);
         return;
      }

      String command = event.getCommand();
      String[] parts = command.split(" ");
      String subCommand = command.startsWith("hm") ? parts[1] : parts[0];
      ModState modState = this.serviceContext.getConfigManager().getState();

      switch (subCommand) {
         case "v":
            this.handleVanishCommand(parts, state);
            break;
         case "gamemode":
         case "gm":
            this.handleGamemodeCommand(parts, state);
            break;
         case "fly":
            this.handleFlyCommand(parts, state, modState);
            break;
         case "god":
            this.handleGodCommand(parts, state, modState);
            break;
         case "hac":
            this.handleHacCommand(parts, state, modState, event);
            break;
         default:
            break;
      }
   }

   @Subscribe(priority = -100)
   public void onHmCommand(CommandEvent event) {
      if (event.getCommand().startsWith("hm")) {
         event.setCancelled(true);
      }
   }

   @Subscribe(priority = 100)
   public void onChatMessage(ChatMessageEvent event) {
      String message = this.serviceContext.getChatService().stripFormatting(event.getMessage().getString());
      if (message == null) {
         return;
      }

      StateService state = this.serviceContext.getStateService();
      if (message.equals("▶ Ожидайте завершения проверки... Пожалуйста, не двигайтесь.")
         || message.equals("▶ Введите цифры с картинки в чат! Для открытия чата, нажмите <T>")) {
         state.setInHub(true);
         state.setGameInitCompleted(true);
         state.setModerLocation("");
         state.setModerLocationTrusted(false);
      }

      ChatService chat = this.serviceContext.getChatService();
      if (ModerPlaytimeService.tryParseModerPlaytimeResponse(message, state, chat, false)) {
         return;
      }

      if (ModerPlaytimeService.shouldHideModerPlaytimeLine(message, chat)) {
         event.setCancelled(true);
      }
   }

   @Subscribe(priority = 100)
   public void onRenderHud(RenderHudEvent event) {
      this.serviceContext.getNotificationService().renderToasts(event.getGuiGraphics());
   }

   private void onReconnect(ServerConnectEvent event) {
      ServerData serverInfo = event.getServerData();
      boolean onHolyWorld = serverInfo.ip.matches("(?i).*hol(l)?yworld.*");
      this.serviceContext.getStateService().setIsOnHW(onHolyWorld);
   }

   private void applyAutoModerSettings(StateService state) {
      ModState modState = this.serviceContext.getConfigManager().getState();
      Minecraft client = this.serviceContext.getMinecraftService().getClient();
      state.setVanishEnabled(true);
      state.setGm3Enabled(client.gameMode.getPlayerMode() == GameType.SPECTATOR);

      if (this.shouldToggleVanish(modState, state)) {
         this.serviceContext.getChatService().sendChatOrCommand("/v");
         state.setVanishEnabled(!state.getVanishEnabled());
      }

      if (this.shouldToggleGm3(modState, state)) {
         this.serviceContext.getChatService().sendChatOrCommand("/gm 3");
         state.setGm3Enabled(!state.getGm3Enabled());
      }

      if (this.shouldToggleFly(modState, state)) {
         this.serviceContext.getChatService().sendChatOrCommand("/fly");
         state.setFlyEnabled(!state.getFlyEnabled());
      }

      if (this.shouldToggleGod(modState, state)) {
         this.serviceContext.getChatService().sendChatOrCommand("/god");
         state.setGodEnabled(!state.getGodEnabled());
      }

      if (this.shouldToggleHacAlerts(modState, state)) {
         this.serviceContext.getChatService().sendChatOrCommand("/hac alerts");
         state.setHacAlertsEnabled(!state.getHacAlertsEnabled());
      }
   }

   private boolean shouldToggleVanish(ModState modState, StateService state) {
      return modState.getAutoVanishEnabled() == state.getVanishEnabled();
   }

   private boolean shouldToggleGm3(ModState modState, StateService state) {
      return modState.getAutoGm3Enabled() == state.getGm3Enabled();
   }

   private boolean shouldToggleFly(ModState modState, StateService state) {
      return (modState.getAutoFlyEnabled() == state.getFlyEnabled()) && !state.getGm3Enabled();
   }

   private boolean shouldToggleGod(ModState modState, StateService state) {
      return modState.getAutoGodEnabled() == state.getGodEnabled();
   }

   private boolean shouldToggleHacAlerts(ModState modState, StateService state) {
      return modState.getAutoHacAlertsEnabled() == state.getHacAlertsEnabled();
   }

   private void handleVanishCommand(String[] parts, StateService state) {
      if (parts.length > 1) {
         if (parts[1].equals("enable")) {
            state.setVanishEnabled(true);
         } else if (parts[1].equals("disable")) {
            state.setVanishEnabled(false);
         }
      }
      state.setVanishEnabled(!state.getVanishEnabled());
      this.applyGamemodeArg(parts, state);
   }

   private void handleGamemodeCommand(String[] parts, StateService state) {
      this.applyGamemodeArg(parts, state);
   }

   private void applyGamemodeArg(String[] parts, StateService state) {
      if (parts.length > 1) {
         String mode = parts[1];
         if (mode.equals("3") || mode.equals("spectator")) {
            state.setGm3Enabled(true);
         } else if (mode.equals("0") || mode.equals("1") || mode.equals("2")
            || mode.equals("survival") || mode.equals("creative") || mode.equals("adventure")) {
            state.setGm3Enabled(false);
         }
      }
   }

   private void handleFlyCommand(String[] parts, StateService state, ModState modState) {
      if (parts.length > 1) {
         if (parts[1].equals("enable")) {
            state.setFlyEnabled(true);
         } else if (parts[1].equals("disable")) {
            state.setFlyEnabled(false);
         }
      }
      state.setFlyEnabled(!state.getFlyEnabled());
   }

   private void handleGodCommand(String[] parts, StateService state, ModState modState) {
      if (parts.length > 1) {
         if (parts[1].equals("enable")) {
            state.setGodEnabled(true);
         } else if (parts[1].equals("disable")) {
            state.setGodEnabled(false);
         }
      }
      state.setGodEnabled(!state.getGodEnabled());
   }

   private void handleHacCommand(String[] parts, StateService state, ModState modState, CommandEvent event) {
      if (parts.length > 1 && parts[1].equals("alerts")) {
         state.setHacAlertsEnabled(!state.getHacAlertsEnabled());
         event.setCancelled(true);
      }
   }

   private void handleDisableCommand(CommandEvent event, StateService state) {
      event.setCancelled(true);
      state.setEnabled(false);
      state.setBlocked(true);
      this.serviceContext.getNotificationService().clearToasts();
      this.serviceContext.getNotificationService().showToast(
         NotificationType.SUCCESS, "§a§lУспех", "Мод выключен!", 5.0F
      );
      this.serviceContext.getChatService().sendMessage(Component.literal("§cМод выключен."));
   }

   private void handleEnableCommand(StateService state) {
      state.setEnabled(true);
      state.setBlocked(false);
      this.serviceContext.getNotificationService().showToast(
         NotificationType.SUCCESS, "§a§lУспех", "Мод включен!", 5.0F
      );
      this.serviceContext.getChatService().sendMessage(Component.literal("§aМод включен."));
   }

   private void handleSetApiTokenCommand(String[] parts) {
      if (parts.length < 3) {
         this.showError("Вы не ввели API ключ.");
         return;
      }

      String token = NetService.sanitizeApiToken(String.join(" ", Arrays.copyOfRange(parts, 2, parts.length)));
      if (token.isBlank()) {
         this.showError("Вы не ввели API ключ.");
         return;
      }

      this.serviceContext.getNotificationService().showToast(
         NotificationType.WARNING,
         "§6§lПроверка",
         "Проверяю API ключ журнала...",
         5.0F
      );

      CompletableFuture.runAsync(() -> {
         boolean valid = this.serviceContext.getNetService().validateApiToken(token);
         Minecraft client = this.serviceContext.getMinecraftService().getClient();
         client.execute(() -> {
            if (!valid) {
               this.showError(
                  "API ключ не принят журналом (401). Открой journal.holyworld.me/api, нажми copy у поля «Ваш API ключ» и вставь его в /hm setapitoken. Не используй auth_token из cookies."
               );
               return;
            }

            ModState modState = this.serviceContext.getConfigManager().getState();
            modState.setApiToken(token);
            this.serviceContext.getConfigManager().save(modState);
            this.serviceContext.getStateService().setBlocked(false);
            this.serviceContext.getNotificationService().showToast(
               NotificationType.SUCCESS,
               "§a§lУспех",
               "API ключ сохранён. Перезайди на сервер.",
               8.0F
            );
            this.serviceContext.getChatService().sendMessage(Component.literal("§aAPI ключ сохранён. Перезайди на сервер."));
            this.serviceContext.getSoundService().playSound("success.wav");

            CompletableFuture.delayedExecutor(2L, TimeUnit.SECONDS).execute(() -> client.execute(() -> {
               ClientPacketListener handler = client.getConnection();
               if (handler != null) {
                  handler.getConnection().disconnect(
                     Component.literal("§b§lВы успешно установили API ключ. Пожалуйста, перезайдите на сервер.")
                  );
               }
            }));
         });
      });
   }

   private void handleSetVkCommand(String[] parts) {
      if (parts.length < 3) {
         this.showError("Вы не ввели ссылку на VK.");
         return;
      }

      String vk = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length)).trim();
      vk = vk.replace("https://", "").replace("http://", "");
      if (vk.startsWith("www.")) {
         vk = vk.substring(4);
      }
      if (!vk.matches("(?i)vk\\.com/(id\\d+|[\\w.]+)")) {
         this.showError("Формат: vk.com/id123");
         return;
      }

      ModState modState = this.serviceContext.getConfigManager().getState();
      modState.setVkUrl(vk);
      this.serviceContext.getConfigManager().save(modState);
      this.serviceContext.getStateService().setVkUrl(vk);
      this.serviceContext.getNotificationService().showToast(
         NotificationType.SUCCESS,
         "§a§lУспех",
         "VK сохранён: " + vk,
         5.0F
      );
      this.serviceContext.getChatService().sendMessage(Component.literal("§aVK сохранён: §f" + vk));
      this.serviceContext.getSoundService().playSound("success.wav");
   }

   private void handleDebugCommand(String[] parts) {
      DebugLogService debug = ServiceRegistry.getDebugLogService();
      if (parts.length == 2) {
         this.serviceContext.getChatService().sendMessage(Component.literal(
            "§eHolyModeration debug: §6/hm debug on§f, §6/hm debug off§f, §6/hm debug clear§f, §6/hm debug path§f, §6/hm debug tab"
         ));
         return;
      }

      switch (parts[2]) {
         case "on":
            debug.setEnabled(true);
            this.serviceContext.getChatService().sendMessage(Component.literal("§aDebug-лог включён."));
            break;
         case "off":
            debug.setEnabled(false);
            this.serviceContext.getChatService().sendMessage(Component.literal("§cDebug-лог выключен."));
            break;
         case "clear":
            debug.clear();
            this.serviceContext.getChatService().sendMessage(Component.literal("§aDebug-лог очищен."));
            break;
         case "path":
            this.serviceContext.getChatService().sendMessage(Component.literal("§b" + debug.getLogPath()));
            break;
         case "tab":
            TabLocationService.dumpDiagnostics(this.serviceContext);
            this.serviceContext.getChatService().sendMessage(Component.literal("§aTab dump записан в debug-лог."));
            break;
         default:
            this.serviceContext.getChatService().sendMessage(Component.literal("§cНеизвестный аргумент. Используй on, off, clear, path или tab."));
            break;
      }
   }

   private void showError(String message) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.ERROR, "§c§lОшибка", message, 5.0F
      );
      this.serviceContext.getChatService().sendMessage(Component.literal("§c" + message));
   }

   static {
   }
}

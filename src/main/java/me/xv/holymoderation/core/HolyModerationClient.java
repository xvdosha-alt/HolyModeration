package me.xv.holymoderation.core;

import me.xv.holymoderation.gui.HudPanelDragSetup;
import me.xv.holymoderation.command.DupeIpModule;
import me.xv.holymoderation.command.ModCommands;
import me.xv.holymoderation.command.CoreModule;
import me.xv.holymoderation.command.FreezerModule;
import me.xv.holymoderation.command.GuiCommandModule;
import me.xv.holymoderation.command.KeyBindingModule;
import me.xv.holymoderation.command.PunishmentsModule;
import me.xv.holymoderation.command.ReportModule;
import me.xv.holymoderation.command.SettingsManager;
import me.xv.holymoderation.command.SpyModule;
import me.xv.holymoderation.command.StatsModule;
import me.xv.holymoderation.command.TwinksCheckModule;
import me.xv.holymoderation.command.UpdaterModule;
import me.xv.holymoderation.config.ConfigManager;
import me.xv.holymoderation.event.EventBus;
import me.xv.holymoderation.service.ChatService;
import me.xv.holymoderation.service.CheckoutsService;
import me.xv.holymoderation.service.DupeIpScannerService;
import me.xv.holymoderation.service.DebugLogService;
import me.xv.holymoderation.service.KeyBindingService;
import me.xv.holymoderation.service.MinecraftService;
import me.xv.holymoderation.service.NetService;
import me.xv.holymoderation.service.NotificationService;
import me.xv.holymoderation.service.PunishmentsService;
import me.xv.holymoderation.service.Render2DService;
import me.xv.holymoderation.service.SchedulerService;
import me.xv.holymoderation.service.SoundService;
import me.xv.holymoderation.service.StateService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.LoggerFactory;

public class HolyModerationClient implements ClientModInitializer {
   public void onInitializeClient() {
      ServiceRegistry.initialize(
         new ConfigManager(),
         new EventBus(),
         new ChatService(),
         new CheckoutsService(),
         new DupeIpScannerService(),
         new DebugLogService(),
         new KeyBindingService(),
         new MinecraftService(),
         new NetService(),
         new NotificationService(),
         new PunishmentsService(),
         new Render2DService(),
         new SchedulerService(),
         new SoundService(),
         new StateService(),
         LoggerFactory.getLogger("HolyModeration/Client")
      );
      this.registerModules();
      HudPanelDragSetup.register();
      ClientCommandRegistrationCallback.EVENT.register(ModCommands::register);
      ServiceRegistry.getDebugLogService().write("system", "log file: " + ServiceRegistry.getDebugLogService().getLogPath());
      ServiceRegistry.getStateService().setVkUrl(ServiceRegistry.getConfigManager().getState().getVkUrl());
      ServiceRegistry.getLoggerService().logger().info("HolyModerationClient has been initialized");
   }

   private void registerModules() {
      EventBus eventBus = ServiceRegistry.getEventBus();
      registerEventSubscribers(eventBus);
      ServiceRegistry.getLoggerService().logger().info("Eventbus & modules has been initialized");
   }

   public static void registerEventSubscribers(EventBus eventBus) {
      eventBus.register(new GuiCommandModule());
      eventBus.register(new FreezerModule());
      eventBus.register(new KeyBindingModule());
      eventBus.register(new StatsModule());
      eventBus.register(new UpdaterModule());
      eventBus.register(new PunishmentsModule());
      eventBus.register(new ReportModule());
      eventBus.register(new SettingsManager());
      eventBus.register(new CoreModule());
      eventBus.register(new SpyModule());
      eventBus.register(new TwinksCheckModule());
      eventBus.register(new DupeIpModule());
   }
}

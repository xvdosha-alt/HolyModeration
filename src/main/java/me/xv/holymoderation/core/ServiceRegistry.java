package me.xv.holymoderation.core;

import lombok.Generated;
import me.xv.holymoderation.config.ConfigManager;
import me.xv.holymoderation.event.EventBus;
import me.xv.holymoderation.service.ChatService;
import me.xv.holymoderation.service.CheckoutsService;
import me.xv.holymoderation.service.DebugLogService;
import me.xv.holymoderation.service.KeyBindingService;
import me.xv.holymoderation.service.LoggerService;
import me.xv.holymoderation.service.MinecraftService;
import me.xv.holymoderation.service.NetService;
import me.xv.holymoderation.service.NotificationService;
import me.xv.holymoderation.service.PunishmentsService;
import me.xv.holymoderation.service.Render2DService;
import me.xv.holymoderation.service.SchedulerService;
import me.xv.holymoderation.service.SoundService;
import me.xv.holymoderation.service.StateService;
import org.slf4j.Logger;

public class ServiceRegistry {
   private static ConfigManager configManager;
   private static EventBus eventBus;
   private static ChatService chatService;
   private static CheckoutsService checkoutsService;
   private static DebugLogService debugLogService;
   private static KeyBindingService keyBindingService;
   private static MinecraftService minecraftService;
   private static NetService netService;
   private static NotificationService notificationService;
   private static PunishmentsService punishmentsService;
   private static Render2DService render2DService;
   private static SchedulerService schedulerService;
   private static SoundService soundService;
   private static StateService stateService;
   private static LoggerService loggerService;

   public static void initialize(
      ConfigManager configManager,
      EventBus eventBus,
      ChatService chatService,
      CheckoutsService checkoutsService,
      DebugLogService debugLogService,
      KeyBindingService keyBindingService,
      MinecraftService minecraftService,
      NetService netService,
      NotificationService notificationService,
      PunishmentsService punishmentsService,
      Render2DService render2DService,
      SchedulerService schedulerService,
      SoundService soundService,
      StateService stateService,
      Logger logger
   ) {
      ServiceRegistry.configManager = configManager;
      ServiceRegistry.eventBus = eventBus;
      ServiceRegistry.chatService = chatService;
      ServiceRegistry.checkoutsService = checkoutsService;
      ServiceRegistry.debugLogService = debugLogService;
      ServiceRegistry.keyBindingService = keyBindingService;
      ServiceRegistry.minecraftService = minecraftService;
      ServiceRegistry.netService = netService;
      ServiceRegistry.notificationService = notificationService;
      ServiceRegistry.punishmentsService = punishmentsService;
      ServiceRegistry.render2DService = render2DService;
      ServiceRegistry.schedulerService = schedulerService;
      ServiceRegistry.soundService = soundService;
      ServiceRegistry.stateService = stateService;
      logger.info("Base services has been initialized");
      configureLogger(logger);
   }

   private static void configureLogger(Logger logger) {
      loggerService = new LoggerService(logger);
      eventBus.setLogger(loggerService);
      chatService.init(loggerService);
      checkoutsService.init(loggerService);
      debugLogService.init(loggerService);
      keyBindingService.init(loggerService);
      minecraftService.init(loggerService);
      netService.init(loggerService);
      notificationService.init(loggerService);
      punishmentsService.init(loggerService);
      render2DService.init(loggerService);
      schedulerService.init(loggerService);
      soundService.init(loggerService);
      stateService.init(loggerService);
      loggerService.logger().info("Logger service has been initialized");
   }

   @Generated
   public static ConfigManager getConfigManager() {
      return configManager;
   }

   @Generated
   public static EventBus getEventBus() {
      return eventBus;
   }

   @Generated
   public static ChatService getChatService() {
      return chatService;
   }

   @Generated
   public static CheckoutsService getCheckoutsService() {
      return checkoutsService;
   }

   @Generated
   public static KeyBindingService getKeyBindingService() {
      return keyBindingService;
   }

   @Generated
   public static MinecraftService getMinecraftService() {
      return minecraftService;
   }

   @Generated
   public static NetService getNetService() {
      return netService;
   }

   @Generated
   public static NotificationService getNotificationService() {
      return notificationService;
   }

   @Generated
   public static PunishmentsService getPunishmentsService() {
      return punishmentsService;
   }

   @Generated
   public static Render2DService getRender2DService() {
      return render2DService;
   }

   @Generated
   public static SchedulerService getSchedulerService() {
      return schedulerService;
   }

   @Generated
   public static SoundService getSoundService() {
      return soundService;
   }

   @Generated
   public static StateService getStateService() {
      return stateService;
   }

   @Generated
   public static DebugLogService getDebugLogService() {
      return debugLogService;
   }

   @Generated
   public static LoggerService getLoggerService() {
      return loggerService;
   }
}

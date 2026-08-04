package me.xv.holymoderation.core;

import lombok.Generated;
import me.xv.holymoderation.service.LoggerService;
import me.xv.holymoderation.service.ChatService;
import me.xv.holymoderation.service.CheckoutsService;
import me.xv.holymoderation.service.StateService;
import me.xv.holymoderation.config.ConfigManager;
import me.xv.holymoderation.service.SoundService;
import me.xv.holymoderation.service.Render2DService;
import me.xv.holymoderation.service.NetService;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.service.SchedulerService;
import me.xv.holymoderation.service.NotificationService;
import me.xv.holymoderation.service.MinecraftService;
import me.xv.holymoderation.event.EventBus;
import me.xv.holymoderation.service.PunishmentsService;
import me.xv.holymoderation.service.KeyBindingService;

public class ServiceContext {
   private final ConfigManager configManager = ServiceRegistry.getConfigManager();
   private final EventBus eventBus = ServiceRegistry.getEventBus();
   private final ChatService chatService = ServiceRegistry.getChatService();
   private final CheckoutsService checkoutsService = ServiceRegistry.getCheckoutsService();
   private final KeyBindingService keyBindingService = ServiceRegistry.getKeyBindingService();
   private final MinecraftService minecraftService = ServiceRegistry.getMinecraftService();
   private final NetService netService = ServiceRegistry.getNetService();
   private final NotificationService notificationService = ServiceRegistry.getNotificationService();
   private final PunishmentsService punishmentsService = ServiceRegistry.getPunishmentsService();
   private final Render2DService render2DService = ServiceRegistry.getRender2DService();
   private final SchedulerService schedulerService = ServiceRegistry.getSchedulerService();
   private final SoundService soundService = ServiceRegistry.getSoundService();
   private final StateService stateService = ServiceRegistry.getStateService();
   private LoggerService loggerService;

   public void setLoggerService(LoggerService logger) {
      this.loggerService = logger;
   }

   @Generated
   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Generated
   public EventBus getEventBus() {
      return this.eventBus;
   }

   @Generated
   public ChatService getChatService() {
      return this.chatService;
   }

   @Generated
   public CheckoutsService getCheckoutsService() {
      return this.checkoutsService;
   }

   @Generated
   public KeyBindingService getKeyBindingService() {
      return this.keyBindingService;
   }

   @Generated
   public MinecraftService getMinecraftService() {
      return this.minecraftService;
   }

   @Generated
   public NetService getNetService() {
      return this.netService;
   }

   @Generated
   public NotificationService getNotificationService() {
      return this.notificationService;
   }

   @Generated
   public PunishmentsService getPunishmentsService() {
      return this.punishmentsService;
   }

   @Generated
   public Render2DService getRender2DService() {
      return this.render2DService;
   }

   @Generated
   public SchedulerService getSchedulerService() {
      return this.schedulerService;
   }

   @Generated
   public SoundService getSoundService() {
      return this.soundService;
   }

   @Generated
   public StateService getStateService() {
      return this.stateService;
   }

   @Generated
   public LoggerService getLoggerService() {
      return this.loggerService;
   }

   static {
   }
}

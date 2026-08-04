package me.xv.holymoderation.command;

import me.xv.holymoderation.service.LoggerService;
import me.xv.holymoderation.core.ServiceContext;

public abstract class BaseCommandHandler {
   protected final ServiceContext serviceContext = new ServiceContext();

   public void init(LoggerService logger) {
      this.serviceContext.setLoggerService(logger);
   }

   static {
   }
}

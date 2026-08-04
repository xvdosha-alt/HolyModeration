package me.xv.holymoderation.core;

import lombok.Generated;
import me.xv.holymoderation.service.LoggerService;

public class BaseService {
   protected LoggerService loggerService;

   @Generated
   public void init(LoggerService logger) {
      this.loggerService = logger;
   }

   static {
   }
}

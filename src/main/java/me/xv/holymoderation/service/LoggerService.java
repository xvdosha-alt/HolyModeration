package me.xv.holymoderation.service;

import org.slf4j.Logger;

public record LoggerService(Logger logger) {
   public Logger getLogger() {
      return this.logger;
   }

   public void log(Object message) {
      this.logger.error(String.valueOf(message));
   }

   public void error(Object message) {
      this.logger.error(String.valueOf(message));
   }
}

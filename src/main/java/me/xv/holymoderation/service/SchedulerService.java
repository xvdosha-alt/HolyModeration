package me.xv.holymoderation.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import me.xv.holymoderation.core.BaseService;

public class SchedulerService extends BaseService {
   private ScheduledExecutorService instance;

   public ScheduledExecutorService getExecutor() {
      if (this.instance == null || this.instance.isShutdown()) {
         this.instance = Executors.newScheduledThreadPool(1);
      }
      return this.instance;
   }

   public void shutdown() {
      if (this.instance != null) {
         this.instance.shutdown();
      }
   }

   static {
   }
}

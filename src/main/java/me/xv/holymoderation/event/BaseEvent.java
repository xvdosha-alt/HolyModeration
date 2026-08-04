package me.xv.holymoderation.event;

import lombok.Generated;

public abstract class BaseEvent {
   private boolean cancelled = false;

   @Generated
   public void setCancelled(boolean enabled) {
      this.cancelled = enabled;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   static {
   }
}

package me.xv.holymoderation.event;

import lombok.Generated;
import me.xv.holymoderation.event.BaseEvent;

public class CommandEvent extends BaseEvent {
   private final String command;

   public CommandEvent(String command) {
      this.command = command;
   }

   @Generated
   public String getCommand() {
      return this.command;
   }

   static {
   }
}

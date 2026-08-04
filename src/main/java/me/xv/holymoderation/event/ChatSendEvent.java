package me.xv.holymoderation.event;

import lombok.Generated;
import me.xv.holymoderation.event.BaseEvent;

public class ChatSendEvent extends BaseEvent {
   private final String content;

   public ChatSendEvent(String content) {
      this.content = content;
   }

   @Generated
   public String getContent() {
      return this.content;
   }

   static {
   }
}

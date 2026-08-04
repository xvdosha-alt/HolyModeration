package me.xv.holymoderation.event;

import lombok.Generated;
import net.minecraft.network.chat.Component;

public class ChatMessageEvent extends BaseEvent {
   private Component message;

   public ChatMessageEvent(Component message) {
      this.message = message;
   }

   @Generated
   public Component getMessage() {
      return this.message;
   }

   @Generated
   public void setMessage(Component message) {
      this.message = message;
   }
}

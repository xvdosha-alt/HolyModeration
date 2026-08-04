package me.xv.holymoderation.event;

import lombok.Generated;
import me.xv.holymoderation.event.BaseEvent;

public class KeyPressEvent extends BaseEvent {
   private final long window;
   private final int key;
   private final int scancode;
   private final int action;
   private final int modifiers;

   public KeyPressEvent(
      long window, int key, int scancode, int action, int modifiers
   ) {
      this.window = window;
      this.key = key;
      this.scancode = scancode;
      this.action = action;
      this.modifiers = modifiers;
   }

   @Generated
   public long getWindow() {
      return this.window;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public int getScancode() {
      return this.scancode;
   }

   @Generated
   public int getAction() {
      return this.action;
   }

   @Generated
   public int getModifiers() {
      return this.modifiers;
   }

   static {
   }
}

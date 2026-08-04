package me.xv.holymoderation.command;

import me.xv.holymoderation.event.KeyPressEvent;
import me.xv.holymoderation.event.Subscribe;

public class KeyBindingModule extends BaseCommandHandler {
   @Subscribe
   public void onKeyPress(KeyPressEvent event) {
      this.serviceContext.getKeyBindingService().registerBinding(event.getKey(), event.getAction());
   }

   static {
   }
}

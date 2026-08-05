package me.xv.holymoderation.gui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

public final class HudPanelDragSetup {
   private HudPanelDragSetup() {
   }

   public static void register() {
      ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
         if (!(screen instanceof ChatScreen)) {
            return;
         }

         registerForScreen(screen);
      });
   }

   private static void registerForScreen(Screen screen) {
      ScreenMouseEvents.allowMouseClick(screen).register(
         (current, event) -> !HudPanelLayout.onMouseClick(event.x(), event.y(), event.button())
      );
      ScreenMouseEvents.beforeMouseDrag(screen).register(
         (current, event, deltaX, deltaY) -> HudPanelLayout.onMouseDrag(event.x(), event.y())
      );
      ScreenMouseEvents.allowMouseDrag(screen).register(
         (current, event, deltaX, deltaY) -> !HudPanelLayout.isDragging()
      );
      ScreenMouseEvents.afterMouseRelease(screen).register(
         (current, event, consumed) -> {
            HudPanelLayout.onMouseRelease();
            return consumed;
         }
      );
   }
}

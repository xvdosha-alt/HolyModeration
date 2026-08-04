package me.xv.holymoderation.command;

import me.xv.holymoderation.event.RenderHudEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.gui.MainGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class GuiCommandModule extends BaseCommandHandler {
   private final MainGuiScreen mainGuiScreen = new MainGuiScreen(this.serviceContext);

   @Subscribe
   public void onGuiRenderHud(RenderHudEvent event) {
      if (!this.serviceContext.getKeyBindingService().matchesBinding("open_main_gui")) {
         return;
      }

      Minecraft client = this.serviceContext.getMinecraftService().getClient();
      Screen currentScreen = client.screen;
      if (currentScreen == null) {
         client.setScreen(this.mainGuiScreen);
      } else if (currentScreen.equals(this.mainGuiScreen)) {
         this.mainGuiScreen.closeScreen();
      }
   }

   static {
   }
}

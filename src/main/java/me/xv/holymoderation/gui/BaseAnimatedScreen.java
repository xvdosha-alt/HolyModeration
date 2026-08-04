package me.xv.holymoderation.gui;

import java.util.HashMap;
import lombok.Generated;
import me.xv.holymoderation.core.ServiceContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class BaseAnimatedScreen extends Screen {
   protected float x;
   protected float y;
   protected float width;
   protected float height;
   protected final ServiceContext serviceContext;
   protected final HashMap<String, GuiTab> tabs = new HashMap<>();

   protected BaseAnimatedScreen(Component title, ServiceContext serviceContext) {
      super(title);
      this.serviceContext = serviceContext;
   }

   @Override
   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.tabs.forEach((name, tab) -> tab.renderTab(context, mouseX, mouseY, delta));
      super.render(context, mouseX, mouseY, delta);
   }

   @Generated
   public float getOpenAnimation() {
      return this.x;
   }

   @Generated
   public float getCloseAnimation() {
      return this.y;
   }

   @Generated
   public float getTabSwitchAnimation() {
      return this.width;
   }

   @Generated
   public float getHoverAnimation() {
      return this.height;
   }
}

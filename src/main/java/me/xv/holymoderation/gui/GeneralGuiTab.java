package me.xv.holymoderation.gui;

import java.awt.Color;
import me.xv.holymoderation.core.ServiceContext;
import net.minecraft.client.gui.GuiGraphics;

public class GeneralGuiTab extends GuiTab {
   public GeneralGuiTab(MainGuiScreen parent, ServiceContext context) {
      super(parent, context);
      this.modules.put("ColorPicker", new ColorPickerWidget(this, context));
   }

   @Override
   public void renderTab(GuiGraphics context, int mouseX, int mouseY, float delta) {
      MainGuiScreen screen = (MainGuiScreen)this.parent;
      ColorPickerWidget colorPicker = (ColorPickerWidget)this.modules.get("ColorPicker");
      float size = 25.0F;
      colorPicker.renderColorPicker(
         context,
         screen.getOpenAnimation() + screen.getTabSwitchAnimation() / 2.0F,
         screen.getCloseAnimation() + screen.getHoverAnimation() / 2.0F,
         size * screen.getOpenProgress(),
         new Color(0),
         3.0F
      );
      colorPicker.onClick(mouseX, mouseY);
   }
}

package me.xv.holymoderation.gui;

import java.util.HashMap;
import me.xv.holymoderation.gui.BaseAnimatedScreen;
import me.xv.holymoderation.core.ServiceContext;
import me.xv.holymoderation.gui.GuiWidget;
import net.minecraft.client.gui.GuiGraphics;

public abstract class GuiTab {
   protected final BaseAnimatedScreen parent;
   protected final ServiceContext serviceContext;
   protected final HashMap<String, GuiWidget> modules = new HashMap<>();

   public GuiTab(BaseAnimatedScreen parent, ServiceContext serviceContext) {
      this.parent = parent;
      this.serviceContext = serviceContext;
   }

   public abstract void renderTab(GuiGraphics var1, int var2, int var3, float var4);

   static {
   }
}

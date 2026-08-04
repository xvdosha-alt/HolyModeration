package me.xv.holymoderation.gui;

import me.xv.holymoderation.gui.GuiTab;
import me.xv.holymoderation.core.ServiceContext;

public abstract class GuiWidget {
   protected final GuiTab parent;
   protected final ServiceContext serviceContext;

   public GuiWidget(GuiTab parent, ServiceContext serviceContext) {
      this.parent = parent;
      this.serviceContext = serviceContext;
   }

   static {
   }
}

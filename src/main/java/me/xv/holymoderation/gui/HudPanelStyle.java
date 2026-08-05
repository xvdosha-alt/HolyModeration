package me.xv.holymoderation.gui;

import java.awt.Color;

public record HudPanelStyle(
   Color background,
   Color outline,
   Color accent,
   Color accentSoft,
   int badgeText,
   int primaryText,
   int secondaryText
) {
   public static HudPanelStyle spy() {
      return new HudPanelStyle(
         new Color(6, 12, 24, 235),
         new Color(38, 96, 180, 200),
         new Color(64, 168, 255, 255),
         new Color(64, 168, 255, 55),
         0xFF081018,
         0xFFFFFFFF,
         0xFF9CB8DC
      );
   }

   public static HudPanelStyle checkout() {
      return new HudPanelStyle(
         new Color(14, 6, 22, 235),
         new Color(120, 58, 190, 200),
         new Color(210, 96, 255, 255),
         new Color(210, 96, 255, 55),
         0xFF120818,
         0xFFFFFFFF,
         0xFFE0B8FF
      );
   }
}

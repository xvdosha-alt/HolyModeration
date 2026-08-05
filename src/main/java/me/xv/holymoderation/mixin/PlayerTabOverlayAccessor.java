package me.xv.holymoderation.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface PlayerTabOverlayAccessor {
   @Accessor("header")
   Component hm$getHeader();

   @Accessor("footer")
   Component hm$getFooter();
}

package me.xv.holymoderation.mixin;

import me.xv.holymoderation.util.CheckoutMarkerService;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin<T extends Avatar & ClientAvatarEntity> {
   @Inject(method = "extractRenderState", at = @At("RETURN"))
   private void onExtractRenderState(T entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
      if (!(entity instanceof Player player)) {
         return;
      }

      if (!CheckoutMarkerService.isCheckoutPlayer(player.getGameProfile().name())) {
         return;
      }

      if (state.nameTag == null) {
         return;
      }

      state.nameTag = CheckoutMarkerService.prefixComponent(state.nameTag);
   }
}

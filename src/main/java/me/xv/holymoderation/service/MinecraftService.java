package me.xv.holymoderation.service;

import me.xv.holymoderation.core.BaseService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public class MinecraftService extends BaseService {
   public Minecraft getClient() {
      return Minecraft.getInstance();
   }

   @Nullable
   public LocalPlayer getPlayer() {
      return this.getClient().player;
   }

   @Nullable
   public ClientLevel getWorld() {
      return this.getClient().level;
   }
}

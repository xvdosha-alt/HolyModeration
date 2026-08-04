package me.xv.holymoderation.event;

import lombok.Generated;
import net.minecraft.client.multiplayer.ServerData;

public class ServerConnectEvent extends BaseEvent {
   private final ServerData serverInfo;
   private final boolean isSwitch;

   public ServerConnectEvent(ServerData serverInfo, boolean isSwitch) {
      this.serverInfo = serverInfo;
      this.isSwitch = isSwitch;
   }

   @Generated
   public ServerData getServerData() {
      return this.serverInfo;
   }

   @Generated
   public boolean isSwitch() {
      return this.isSwitch;
   }
}

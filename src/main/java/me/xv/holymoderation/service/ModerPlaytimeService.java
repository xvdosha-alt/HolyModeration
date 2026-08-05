package me.xv.holymoderation.service;

import me.xv.holymoderation.core.ServiceContext;
import me.xv.holymoderation.core.ServiceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;

public final class ModerPlaytimeService {
   private static long lastRequestMs;
   private static boolean awaitingModerPlaytime;

   private ModerPlaytimeService() {
   }

   public static void requestModerLocation(ServiceContext serviceContext) {
      requestModerLocation(serviceContext, false);
   }

   public static void requestModerLocation(ServiceContext serviceContext, boolean forced) {
      StateService state = serviceContext.getStateService();
      if (!state.isOnHW() || state.getBlocked() || state.getInHub()) {
         return;
      }

      Minecraft client = serviceContext.getMinecraftService().getClient();
      if (client.player != null
         && client.gameMode != null
         && client.gameMode.getPlayerMode() == GameType.SPECTATOR) {
         return;
      }

      String nickname = resolveModerNickname(state, client);
      if (nickname.isBlank()) {
         return;
      }

      long now = System.currentTimeMillis();
      if (!forced && now - lastRequestMs < 3000L) {
         return;
      }
      lastRequestMs = now;
      awaitingModerPlaytime = true;

      ServiceRegistry.getDebugLogService().write("loc", "send /playtime " + nickname);
      serviceContext.getChatService().sendChatOrCommand("/playtime " + nickname);
   }

   public static boolean tryParseModerPlaytimeResponse(
      String message,
      StateService state,
      ChatService chat,
      boolean hideFromChat
   ) {
      if (!awaitingModerPlaytime || message == null || !message.startsWith("Текущая")) {
         return false;
      }

      String raw = chat.parsePlaytimeLocation(message);
      if (raw == null || raw.isBlank()) {
         return false;
      }

      if (raw.equalsIgnoreCase("Оффлайн") || raw.toLowerCase().startsWith("lobby")) {
         awaitingModerPlaytime = false;
         state.setInHub(raw.toLowerCase().startsWith("lobby"));
         state.setModerLocation("");
         state.setModerLocationTrusted(false);
         return hideFromChat;
      }

      String location = resolveServerText(raw, chat);
      if (!TabLocationService.looksLikeLocation(location)) {
         ServiceRegistry.getDebugLogService().write("loc", "playtime unknown raw=" + raw);
         return false;
      }

      awaitingModerPlaytime = false;
      applyModerLocation(state, location, "playtime raw=" + raw);
      return hideFromChat;
   }

   public static boolean shouldHideModerPlaytimeLine(String message, ChatService chat) {
      return awaitingModerPlaytime && chat.isPlaytimeOutputLine(message);
   }

   private static String resolveServerText(String text, ChatService chat) {
      String location = TabLocationService.extractLocation(text, chat);
      if (location != null && !location.isBlank()) {
         return location;
      }

      location = chat.normalizeServerLocation(text);
      if (TabLocationService.looksLikeLocation(location)) {
         return location;
      }

      return null;
   }

   private static void applyModerLocation(StateService state, String location, String debug) {
      state.setInHub(false);
      state.setModerLocation(location);
      state.setModerLocationTrusted(true);
      state.setLastAnarchyLocation(location);
      ServiceRegistry.getDebugLogService().write("loc", "location=" + location + " " + debug);
   }

   private static String resolveModerNickname(StateService state, Minecraft client) {
      String nickname = state.getModerNickname();
      if (nickname != null && !nickname.isBlank()) {
         return nickname;
      }

      if (client != null && client.player != null) {
         return client.player.getName().getString();
      }

      return "";
   }
}

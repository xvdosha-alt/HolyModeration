package me.xv.holymoderation.service;

import me.xv.holymoderation.core.ServiceContext;
import me.xv.holymoderation.core.ServiceRegistry;

public final class ModerLocationResolver {
   private ModerLocationResolver() {
   }

   public static String resolve(ServiceContext serviceContext) {
      return resolve(serviceContext, null);
   }

   public static String resolve(ServiceContext serviceContext, String checkoutPlayer) {
      StateService state = serviceContext.getStateService();

      String location = normalize(state.getModerLocation());
      if (state.getModerLocationTrusted() && isAnarchyLocation(location)) {
         remember(serviceContext, location);
         return location;
      }

      if (checkoutPlayer != null && !checkoutPlayer.isBlank()) {
         if (checkoutPlayer.equalsIgnoreCase(state.getSpyPlayer())) {
            location = normalize(state.getSpyPlayerStatus());
            if (isAnarchyLocation(location)) {
               apply(serviceContext, location, "spy");
               return location;
            }
         }
      }

      ModerPlaytimeService.requestModerLocation(serviceContext, true);

      location = normalize(TabLocationService.scanScoreboard(serviceContext));
      if (isAnarchyLocation(location)) {
         apply(serviceContext, location, "scoreboard");
         return location;
      }

      if (state.getModerLocation().isEmpty()) {
         TabLocationService.updateModerLocation(serviceContext);
      }
      location = normalize(state.getModerLocation());
      if (isAnarchyLocation(location)) {
         remember(serviceContext, location);
         return location;
      }

      if (!state.getInHub()) {
         location = normalize(state.getLastAnarchyLocation());
         if (isAnarchyLocation(location)) {
            state.setModerLocation(location);
            journalLog("fallback lastAnarchyLocation=" + location);
            return location;
         }
      }

      journalLog(
         "resolve failed checkoutPlayer=" + checkoutPlayer
            + " spyPlayer=" + state.getSpyPlayer()
            + " spyStatus=" + state.getSpyPlayerStatus()
            + " inHub=" + state.getInHub()
      );
      TabLocationService.dumpDiagnostics(serviceContext);
      return "";
   }

   public static boolean isAnarchyLocation(String location) {
      return location != null && !location.isBlank() && TabLocationService.looksLikeLocation(location);
   }

   private static String normalize(String location) {
      if (location == null || location.isBlank()) {
         return "";
      }

      return location.trim().toLowerCase();
   }

   private static void apply(ServiceContext serviceContext, String location, String source) {
      StateService state = serviceContext.getStateService();
      state.setInHub(false);
      state.setModerLocation(location);
      state.setModerLocationTrusted(true);
      remember(serviceContext, location);
      journalLog("resolved source=" + source + " location=" + location);
   }

   private static void remember(ServiceContext serviceContext, String location) {
      serviceContext.getStateService().setLastAnarchyLocation(location);
   }

   private static void journalLog(String message) {
      ServiceRegistry.getDebugLogService().write("journal", message);
   }
}

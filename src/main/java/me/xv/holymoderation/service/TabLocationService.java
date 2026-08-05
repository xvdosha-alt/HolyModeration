package me.xv.holymoderation.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.xv.holymoderation.core.ServiceContext;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.mixin.PlayerTabOverlayAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Scoreboard;

public final class TabLocationService {
   private TabLocationService() {
   }

   private static long lastAutoDumpMs;
   private static final long AUTO_DUMP_INTERVAL_MS = 5000L;

   private static final Pattern FULL_ANARCHY = Pattern.compile(
      "(?i)(lite120-anarchy-\\d+|lite-anarchy-\\d+|classic-anarchy-\\d+|prime-anarchy-\\d+)"
   );
   private static final Pattern SHORT_ANARCHY = Pattern.compile(
      "(?i)(lite120-\\d+|lite-\\d+|classic-\\d+|prime-\\d+|lpvp)"
   );
   private static final Pattern BRACKET_CONTENT = Pattern.compile("[\\[(]([^\\]\\)]+)[\\]\\)]");
   private static final Pattern HUB = Pattern.compile("(?i)(^|[\\s|\\[\\]()«»·•])((хаб|hub|lobby|лobby)([\\s|\\[\\]()«»·•]|$))");
   private static final Pattern LITE_LABEL = Pattern.compile("(?i)(?:lite|лайт)\\s*(?:anarchy|анархия)?\\s*#?\\s*(\\d+)");
   private static final Pattern LITE120_LABEL = Pattern.compile("(?i)(?:lite120|l2)\\s*(?:anarchy|анархия)?\\s*#?\\s*(\\d+)");
   private static final Pattern CLASSIC_LABEL = Pattern.compile("(?i)(?:classic|классик)?\\s*(?:anarchy|анархия)\\s*#?\\s*(\\d+)");
   private static final Pattern PRIME_LABEL = Pattern.compile("(?i)(?:prime|прайм)\\s*(?:anarchy|анархия)?\\s*#?\\s*(\\d+)");
   private static final Pattern SERVER_CODE = Pattern.compile("(?i)(l2anarchy\\d+|lanarchy\\d+|anarchy\\d+|pranarchy\\d+)");

   public static boolean updateModerLocation() {
      return updateModerLocation(new ServiceContext());
   }

   public static boolean updateModerLocation(ServiceContext serviceContext) {
      StateService state = serviceContext.getStateService();
      if (!state.isOnHW() || state.getBlocked()) {
         return false;
      }

      Minecraft client = serviceContext.getMinecraftService().getClient();
      if (client.player != null
         && client.gameMode != null
         && client.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.SPECTATOR) {
         state.setInHub(true);
         state.setModerLocation("");
         state.setLastAnarchyLocation("");
         tabLog("spectator hub nick=" + resolveModerNickname(state, client));
         return true;
      }

      String nickname = resolveModerNickname(state, client);
      if (nickname.isBlank()) {
         tabLog("skip blank nick");
         return false;
      }

      TabScanData scanData = collectTabData(client, serviceContext.getChatService(), nickname);
      TabLocationResult result = resolveFromScan(scanData, serviceContext.getChatService());

      if (result == null) {
         tabLog("scan failed nick=" + nickname + " sources=" + scanData.sources().size());
         dumpDiagnostics(serviceContext, scanData, nickname, result, false);
         return false;
      }

      if (result.hub()) {
         if (state.getInHub() && state.getModerLocation().isEmpty()) {
            return false;
         }
         state.setInHub(true);
         state.setModerLocation("");
         state.setLastAnarchyLocation("");
         tabLog("hub detected nick=" + nickname);
         dumpDiagnostics(serviceContext, scanData, nickname, result, false);
         return true;
      }

      if (result.location() == null || result.location().isBlank()) {
         tabLog("empty location nick=" + nickname);
         dumpDiagnostics(serviceContext, scanData, nickname, result, false);
         return false;
      }

      if (!state.getInHub() && result.location().equalsIgnoreCase(state.getModerLocation())) {
         return false;
      }

      state.setInHub(false);
      state.setModerLocation(result.location());
      state.setModerLocationTrusted(false);
      tabLog("location=" + result.location() + " nick=" + nickname + " from=" + result.source());
      return true;
   }

   public static void dumpDiagnostics(ServiceContext serviceContext) {
      StateService state = serviceContext.getStateService();
      Minecraft client = serviceContext.getMinecraftService().getClient();
      String nickname = resolveModerNickname(state, client);
      TabScanData scanData = collectTabData(client, serviceContext.getChatService(), nickname);
      TabLocationResult result = resolveFromScan(scanData, serviceContext.getChatService());
      dumpDiagnostics(serviceContext, scanData, nickname, result, true);
   }

   public static String scanScoreboard(ServiceContext serviceContext) {
      Minecraft client = serviceContext.getMinecraftService().getClient();
      ClientLevel level = client.level;
      if (level == null) {
         tabLog("scoreboard level=null");
         return null;
      }

      ChatService chat = serviceContext.getChatService();
      Scoreboard scoreboard = level.getScoreboard();
      Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
      if (objective == null) {
         tabLog("scoreboard objective=null");
         return null;
      }

      String titleRaw = stripFormatting(objective.getDisplayName().getString());
      tabLog("scoreboard titleRaw=" + titleRaw);
      String titleLocation = extractLocation(titleRaw, chat);
      if (titleLocation != null && !titleLocation.isBlank()) {
         tabLog("scoreboard titleMatch=" + titleLocation);
         return titleLocation;
      }

      for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
         String owner = entry.owner();
         String display = entry.display() == null ? "" : stripFormatting(entry.display().getString());
         String ownerName = entry.ownerName() == null ? "" : stripFormatting(entry.ownerName().getString());
         tabLog("scoreboard line owner=" + owner + " display=" + display + " ownerName=" + ownerName + " hidden=" + entry.isHidden());

         if (owner == null || owner.isBlank() || owner.startsWith("#")) {
            continue;
         }

         String location = extractLocation(display, chat);
         if (location == null || location.isBlank()) {
            location = extractLocation(ownerName, chat);
         }
         if (location == null || location.isBlank()) {
            location = extractLocation(owner, chat);
         }
         if (location != null && !location.isBlank()) {
            tabLog("scoreboard lineMatch=" + location);
            return location;
         }
      }

      return null;
   }

   private static void dumpDiagnostics(
      ServiceContext serviceContext,
      TabScanData scanData,
      String nickname,
      TabLocationResult result,
      boolean forced
   ) {
      long now = System.currentTimeMillis();
      if (!forced && now - lastAutoDumpMs < AUTO_DUMP_INTERVAL_MS) {
         return;
      }
      lastAutoDumpMs = now;

      StateService state = serviceContext.getStateService();
      Minecraft client = serviceContext.getMinecraftService().getClient();
      DebugLogService log = ServiceRegistry.getDebugLogService();

      log.write("tab-dump", "===== begin forced=" + forced + " =====");
      log.write("tab-dump", "nick=" + nickname);
      log.write("tab-dump", "moderLocation=" + state.getModerLocation());
      log.write("tab-dump", "lastAnarchy=" + state.getLastAnarchyLocation());
      log.write("tab-dump", "inHub=" + state.getInHub());
      log.write("tab-dump", "spyPlayer=" + state.getSpyPlayer() + " spyStatus=" + state.getSpyPlayerStatus());

      LocalPlayer player = client.player;
      if (player != null && client.gameMode != null) {
         log.write("tab-dump", "gameMode=" + client.gameMode.getPlayerMode());
      }

      ServerData serverData = client.getCurrentServer();
      if (serverData != null) {
         log.write("tab-dump", "serverData.name=" + serverData.name);
         log.write("tab-dump", "serverData.ip=" + serverData.ip);
      }

      ClientPacketListener connection = client.getConnection();
      if (connection != null) {
         log.write("tab-dump", "connection.serverBrand=" + connection.serverBrand());
      }

      for (TabSource source : scanData.sources()) {
         String location = extractLocation(source.text(), serviceContext.getChatService());
         String hub = isHubText(source.text()) ? "hub" : "-";
         log.write(
            "tab-dump",
            "source kind=" + source.kind()
               + " match=" + (location == null ? "-" : location)
               + " hub=" + hub
               + " text=" + source.text()
         );
      }

      scanScoreboard(serviceContext);

      if (result == null) {
         log.write("tab-dump", "result=null");
      } else if (result.hub()) {
         log.write("tab-dump", "result=hub source=" + result.source());
      } else {
         log.write("tab-dump", "result=" + result.location() + " source=" + result.source());
      }

      log.write("tab-dump", "===== end =====");
   }

   private static String resolveModerNickname(StateService state, Minecraft client) {
      if (!state.getModerNickname().isBlank()) {
         return state.getModerNickname();
      }

      LocalPlayer player = client.player;
      if (player != null) {
         return player.getName().getString();
      }

      return "";
   }

   private static TabScanData collectTabData(Minecraft client, ChatService chat, String nickname) {
      ClientPacketListener connection = client.getConnection();
      List<TabSource> sources = new ArrayList<>();
      if (connection == null) {
         return new TabScanData(sources);
      }

      PlayerTabOverlay tabOverlay = client.gui.getTabList();
      if (tabOverlay instanceof PlayerTabOverlayAccessor accessor) {
         appendSource(sources, "header", accessor.hm$getHeader());
         appendSource(sources, "footer", accessor.hm$getFooter());
      } else {
         tabLog("tab accessor unavailable");
      }

      Collection<PlayerInfo> players = connection.getOnlinePlayers();
      if (players != null) {
         for (PlayerInfo info : players) {
            if (info == null || info.getProfile() == null) {
               continue;
            }

            String profileName = info.getProfile().name();
            boolean self = profileName.equalsIgnoreCase(nickname);
            String kind = self ? "tab-self-list" : "tab-player-list";
            appendSource(sources, kind, info.getTabListDisplayName());
            appendSource(sources, self ? "tab-self-display" : "tab-player-display", tabOverlay.getNameForDisplay(info));
            if (self) {
               appendSource(sources, "tab-self-profile", Component.literal(profileName));
            }
         }
      }

      return new TabScanData(sources);
   }

   private static final List<String> MODER_SOURCE_PRIORITY = List.of(
      "tab-self-display",
      "tab-self-list",
      "header"
   );

   private static TabLocationResult resolveFromScan(TabScanData scanData, ChatService chat) {
      for (String kind : MODER_SOURCE_PRIORITY) {
         for (TabSource source : scanData.sources()) {
            if (!source.kind().equals(kind)) {
               continue;
            }

            TabLocationResult parsed = parseSource(source.text(), chat);
            if (parsed == null) {
               continue;
            }

            if (parsed.hub()) {
               return new TabLocationResult("", true, source.kind());
            }

            if (parsed.location() != null && !parsed.location().isBlank()) {
               return new TabLocationResult(parsed.location(), false, source.kind());
            }
         }
      }

      return null;
   }

   private static void appendSource(List<TabSource> sources, String kind, Component component) {
      if (component == null) {
         sources.add(new TabSource(kind, ""));
         return;
      }

      String text = stripFormatting(component.getString()).trim();
      sources.add(new TabSource(kind, text));
   }

   private static TabLocationResult parseSource(String source, ChatService chat) {
      if (source == null || source.isBlank()) {
         return null;
      }

      String stripped = source.trim();
      if (stripped.isEmpty()) {
         return null;
      }

      String location = extractLocation(stripped, chat);
      if (location != null && !location.isBlank()) {
         return new TabLocationResult(location, false, "parse");
      }

      if (isHubText(stripped)) {
         return new TabLocationResult("", true, "parse");
      }

      return null;
   }

   static String extractLocation(String text, ChatService chat) {
      Matcher serverCodeMatcher = SERVER_CODE.matcher(text);
      if (serverCodeMatcher.find()) {
         return chat.normalizeServerLocation(serverCodeMatcher.group());
      }

      Matcher lite120Matcher = LITE120_LABEL.matcher(text);
      if (lite120Matcher.find()) {
         return "lite120-" + lite120Matcher.group(1);
      }

      Matcher liteMatcher = LITE_LABEL.matcher(text);
      if (liteMatcher.find()) {
         return "lite-" + liteMatcher.group(1);
      }

      Matcher classicMatcher = CLASSIC_LABEL.matcher(text);
      if (classicMatcher.find()) {
         return "classic-" + classicMatcher.group(1);
      }

      Matcher primeMatcher = PRIME_LABEL.matcher(text);
      if (primeMatcher.find()) {
         return "prime-" + primeMatcher.group(1);
      }

      Matcher fullMatcher = FULL_ANARCHY.matcher(text);
      if (fullMatcher.find()) {
         return chat.normalizeServerLocation(fullMatcher.group());
      }

      Matcher shortMatcher = SHORT_ANARCHY.matcher(text);
      if (shortMatcher.find()) {
         return chat.normalizeServerLocation(shortMatcher.group());
      }

      if (text.matches("(?i)l2anarchy\\d+")) {
         return chat.normalizeServerLocation(text);
      }
      if (text.matches("(?i)lanarchy\\d+")) {
         return chat.normalizeServerLocation(text);
      }
      if (text.matches("(?i)anarchy\\d+")) {
         return chat.normalizeServerLocation(text);
      }
      if (text.matches("(?i)pranarchy\\d+")) {
         return chat.normalizeServerLocation(text);
      }
      if (text.equalsIgnoreCase("l2anarchy")) {
         return "lite120-1";
      }
      if (text.equalsIgnoreCase("lanarchy")) {
         return "lite-1";
      }
      if (text.equalsIgnoreCase("anarchy")) {
         return "classic-1";
      }
      if (text.equalsIgnoreCase("pranarchy")) {
         return "prime-1";
      }
      if (text.equalsIgnoreCase("lpvp")) {
         return "lpvp";
      }

      Matcher bracketMatcher = BRACKET_CONTENT.matcher(text);
      while (bracketMatcher.find()) {
         String nested = extractLocation(bracketMatcher.group(1).trim(), chat);
         if (nested != null && !nested.isBlank()) {
            return nested;
         }
      }

      for (String part : text.split("[|»>·•]")) {
         String trimmed = part.trim();
         if (trimmed.isEmpty() || trimmed.equalsIgnoreCase(text)) {
            continue;
         }

         String nested = extractLocation(trimmed, chat);
         if (nested != null && !nested.isBlank()) {
            return nested;
         }

         String normalized = chat.normalizeServerLocation(trimmed);
         if (!normalized.isBlank() && looksLikeLocation(normalized)) {
            return normalized;
         }
      }

      String normalized = chat.normalizeServerLocation(text);
      if (!normalized.isBlank() && looksLikeLocation(normalized)) {
         return normalized;
      }

      return null;
   }

   static boolean isHubText(String text) {
      if (text == null || text.isBlank()) {
         return false;
      }

      String lower = text.toLowerCase(Locale.ROOT);
      if (FULL_ANARCHY.matcher(lower).find() || SHORT_ANARCHY.matcher(lower).find()) {
         return false;
      }

      return HUB.matcher(lower).find()
         || lower.equals("хаб")
         || lower.equals("hub")
         || lower.equals("lobby")
         || lower.contains("на хабе")
         || lower.contains("в хабе");
   }

   static boolean looksLikeLocation(String location) {
      return location.matches("(?i)(lite120-\\d+|lite-\\d+|classic-\\d+|prime-\\d+|lpvp)");
   }

   static String stripFormatting(String text) {
      return text == null ? "" : text.replaceAll("§[0-9a-zA-Z]", "");
   }

   private static void tabLog(String message) {
      ServiceRegistry.getDebugLogService().write("tab", message);
   }

   private record TabSource(String kind, String text) {
   }

   private record TabScanData(List<TabSource> sources) {
   }

   private record TabLocationResult(String location, boolean hub, String source) {
   }
}

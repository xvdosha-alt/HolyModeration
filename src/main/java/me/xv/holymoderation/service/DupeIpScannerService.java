package me.xv.holymoderation.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.xv.holymoderation.core.BaseService;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.gui.DupeIpBanConfirmScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class DupeIpScannerService extends BaseService {
   private enum State { IDLE, AWAITING_INITIAL_DUPE, AWAITING_REVERSE_DUPE, AWAITING_BAN_CHECK }

   private State currentState = State.IDLE;
   private String originalTarget = "";
   private String mainTarget = "";
   private String bannedTwink = "";
   private int timeoutTicks = 0;
   private Runnable scheduledAction = null;
   private boolean internalSend = false;

   private final Queue<String> pendingNicks = new LinkedList<>();
   private final Set<String> processedNicks = new HashSet<>();
   private int bulkDelayTicks = 0;

   private static final int BULK_DELAY = 3;
   private static final int TIMEOUT_TICKS = 200;
   private static final Pattern NICK_PATTERN = Pattern.compile("([a-zA-Z0-9_]+)");
   private static final Pattern BAN_TIME_PATTERN = Pattern.compile(
      "(\\d+)\\s*д[\\s\\D]*?(\\d+)\\s*ч", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
   );

   public boolean isInternalSend() {
      return this.internalSend;
   }

   public void startScan(String player) {
      if (player == null || player.isEmpty()) {
         return;
      }

      this.processedNicks.clear();
      this.mainTarget = player;
      this.originalTarget = player;
      this.processedNicks.add(player.toLowerCase());
      this.currentState = State.AWAITING_INITIAL_DUPE;
      this.timeoutTicks = TIMEOUT_TICKS;
      this.sendScannerCommand("dupeip " + player);
      this.log("start player=" + player);
   }

   public void handleMessage(Component message) {
      if (this.currentState == State.IDLE && this.pendingNicks.isEmpty()) {
         return;
      }

      String plainText = message.getString();
      switch (this.currentState) {
         case AWAITING_INITIAL_DUPE:
            if (plainText.contains(this.originalTarget)) {
               List<String> redNicks = this.extractAllRedNicks(message);
               if (!redNicks.isEmpty()) {
                  for (String nick : redNicks) {
                     String lower = nick.toLowerCase();
                     if (!this.processedNicks.contains(lower)) {
                        this.processedNicks.add(lower);
                        this.pendingNicks.add(nick);
                        this.log("queued nick=" + nick);
                     }
                  }
                  this.resetState();
               }
            }
            break;
         case AWAITING_REVERSE_DUPE:
            if (!this.mainTarget.isEmpty() && plainText.contains(this.mainTarget)) {
               this.sendScannerCommand("checkban " + this.bannedTwink);
               this.currentState = State.AWAITING_BAN_CHECK;
               this.timeoutTicks = TIMEOUT_TICKS;
               this.log("reverse confirmed twink=" + this.bannedTwink);
            }
            break;
         case AWAITING_BAN_CHECK:
            Matcher matcher = BAN_TIME_PATTERN.matcher(plainText);
            if (matcher.find()) {
               try {
                  int days = Integer.parseInt(matcher.group(1));
                  int hours = Integer.parseInt(matcher.group(2));
                  double totalDays = days + (hours / 24.0);
                  int newBanDays = (int)Math.ceil(totalDays * 2.0);
                  String banCommand = String.format(
                     "banip %s %dd 2.9(%s)", this.mainTarget, newBanDays, this.bannedTwink
                  );
                  this.pendingNicks.clear();
                  String finalizedMain = this.mainTarget;
                  this.mainTarget = "";
                  this.scheduledAction = () -> ServiceRegistry.getMinecraftService().getClient().setScreen(
                     new DupeIpBanConfirmScreen(banCommand)
                  );
                  this.log("suggest ban main=" + finalizedMain + " days=" + newBanDays);
               } catch (NumberFormatException ignored) {
                  this.log("ban time parse failed text=" + plainText);
               }
               this.resetState();
            }
            break;
         default:
            break;
      }
   }

   public void tick() {
      if (!this.pendingNicks.isEmpty() && this.currentState == State.IDLE) {
         if (this.bulkDelayTicks > 0) {
            this.bulkDelayTicks--;
         } else {
            this.processNextNick();
         }
      }

      if (this.pendingNicks.isEmpty() && this.currentState == State.IDLE) {
         this.mainTarget = "";
      }

      if (this.timeoutTicks > 0) {
         this.timeoutTicks--;
         if (this.timeoutTicks == 0 && this.currentState != State.IDLE) {
            this.log("timeout state=" + this.currentState);
            this.resetState();
         }
      }

      if (this.scheduledAction != null) {
         this.scheduledAction.run();
         this.scheduledAction = null;
      }
   }

   private void processNextNick() {
      String nick = this.pendingNicks.poll();
      if (nick == null) {
         return;
      }

      this.bannedTwink = nick;
      this.originalTarget = nick;
      this.currentState = State.AWAITING_REVERSE_DUPE;
      this.timeoutTicks = TIMEOUT_TICKS;
      this.sendScannerCommand("dupeip " + nick);
      this.bulkDelayTicks = BULK_DELAY;
      this.log("reverse nick=" + nick + " remaining=" + this.pendingNicks.size());
   }

   private void sendScannerCommand(String command) {
      this.internalSend = true;
      ServiceRegistry.getChatService().sendChatOrCommand("/" + command);
      this.internalSend = false;
   }

   private void resetState() {
      this.currentState = State.IDLE;
      this.originalTarget = "";
      this.bannedTwink = "";
      this.timeoutTicks = 0;
   }

   private List<String> extractAllRedNicks(Component component) {
      List<String> redTexts = new ArrayList<>();
      this.visitTextForRedColor(component, Style.EMPTY, redTexts);
      List<String> nicks = new ArrayList<>();
      for (String redText : redTexts) {
         String cleaned = redText.replace("*", "").trim();
         Matcher matcher = NICK_PATTERN.matcher(cleaned);
         if (matcher.find()) {
            String nick = matcher.group(1);
            if (!nicks.contains(nick)) {
               nicks.add(nick);
            }
         }
      }
      return nicks;
   }

   private void visitTextForRedColor(Component component, Style parentStyle, List<String> out) {
      Style currentStyle = component.getStyle();
      TextColor color = currentStyle.getColor() != null ? currentStyle.getColor() : parentStyle.getColor();
      if (color != null && this.isRedColor(color)) {
         String str = component.getString();
         if (!str.isEmpty() && !str.trim().isEmpty()) {
            out.add(str);
         }
      }
      for (Component sibling : component.getSiblings()) {
         this.visitTextForRedColor(sibling, currentStyle, out);
      }
   }

   private boolean isRedColor(TextColor color) {
      int rgb = color.getValue();
      int r = (rgb >> 16) & 0xFF;
      int g = (rgb >> 8) & 0xFF;
      int b = rgb & 0xFF;
      return r > 100 && r > (g + 50) && r > (b + 50);
   }

   private void log(String message) {
      ServiceRegistry.getDebugLogService().write("dupeip", message);
   }
}

package me.xv.holymoderation.util;

import java.util.regex.Pattern;
import me.xv.holymoderation.core.ServiceRegistry;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class CheckoutMarkerService {
   private CheckoutMarkerService() {
   }

   public static String getCheckoutPlayer() {
      return ServiceRegistry.getStateService().getPlayer();
   }

   public static boolean hasCheckoutPlayer() {
      return !getCheckoutPlayer().isEmpty();
   }

   public static boolean isCheckoutPlayer(String name) {
      if (name == null || name.isBlank()) {
         return false;
      }

      String checkoutPlayer = getCheckoutPlayer();
      return !checkoutPlayer.isEmpty() && checkoutPlayer.equalsIgnoreCase(name.trim());
   }

   public static boolean matchesPlayerInfo(PlayerInfo info) {
      if (info == null || !hasCheckoutPlayer()) {
         return false;
      }

      String checkoutPlayer = getCheckoutPlayer();
      if (isCheckoutPlayer(info.getProfile().name())) {
         return true;
      }

      Component tabName = info.getTabListDisplayName();
      if (tabName != null && containsPlayerName(stripFormatting(tabName.getString()), checkoutPlayer)) {
         return true;
      }

      return false;
   }

   public static boolean matchesChatSender(String senderName, String strippedMessage) {
      if (!hasCheckoutPlayer()) {
         return false;
      }

      String checkoutPlayer = getCheckoutPlayer();
      if (isCheckoutPlayer(senderName)) {
         return true;
      }

      if (strippedMessage == null || strippedMessage.isBlank()) {
         return false;
      }

      int colonIndex = findMessageColonIndex(strippedMessage);
      if (colonIndex <= 0) {
         return false;
      }

      return containsPlayerName(strippedMessage.substring(0, colonIndex), checkoutPlayer);
   }

   public static Component prefixComponent(Component original) {
      if (original == null) {
         return Component.empty();
      }

      String marker = ServiceRegistry.getConfigManager().getState().getPlayerMarker();
      MutableComponent prefixed = Component.empty()
         .append(ServiceRegistry.getChatService().legacyComponent(marker + " "))
         .append(original);
      return prefixed;
   }

   public static boolean containsPlayerName(String text, String player) {
      if (text == null || player == null || player.isBlank()) {
         return false;
      }

      return Pattern.compile("(^|\\s|\\[|\\|)" + Pattern.quote(player) + "(\\s|:|\\]|\\||$)", Pattern.CASE_INSENSITIVE)
         .matcher(text)
         .find();
   }

   public static String stripFormatting(String text) {
      return text == null ? "" : text.replaceAll("§[0-9a-zA-Z]", "");
   }

   public static int findMessageColonIndex(String message) {
      int index = message.indexOf(": ");
      if (index > 0) {
         return index;
      }

      index = message.indexOf(':');
      return index > 0 ? index : -1;
   }

   public static String extractMessageBody(String message) {
      int colonIndex = findMessageColonIndex(message);
      if (colonIndex < 0) {
         return message;
      }

      String body = message.substring(colonIndex + 1);
      if (body.startsWith(" ")) {
         body = body.substring(1);
      }
      return body;
   }
}

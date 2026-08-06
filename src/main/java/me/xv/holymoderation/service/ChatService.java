package me.xv.holymoderation.service;

import java.util.Arrays;
import java.util.List;
import me.xv.holymoderation.core.BaseService;
import me.xv.holymoderation.core.ModBuild;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.util.NotificationType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.ChatFormatting;

public class ChatService extends BaseService {
   public final Component HMTextComponent = Component.literal("§9§l[§3§lHM§9§l] §f");
   public final char[] Chars = new char[]{
      '!', '/', '#', '$', '%', '&', '\'', '(', ')', '*', '+', '-', ',', '.', ':', ';', '<', '>', '=', '?', '@', '[', ']', '^', '`', '|', '~', '{', '}'
   };
   public final String[] NoArgCommands = new String[]{
      "autoban",
      "autocopy",
      "autodupeip",
      "autofly",
      "autogm3",
      "autogod",
      "autoha",
      "autotp",
      "autovanish",
      "copy",
      "disable",
      "enable",
      "me",
      "net",
      "sounds",
      "spyfrz",
      "stats",
      "textsclear",
      "textslist",
      "unfreezing",
      "unfrz",
      "twinks",
      "cleartoasts"
   };
   public final String[] PlayerCommands = new String[]{"freezing", "frz", "sendtexts", "spy"};
   public final String[] OneArgCommands = new String[]{"setapitoken", "setvk", "setcopy", "setmarker", "setspydelay", "textadd", "textremove", "setsoundsvolume"};
   public final String[] TwoArgCommands = new String[]{};
   public final String[] FourArgCommands = new String[]{"endcheckout"};

   public void sendChatOrCommand(String text) {
      Minecraft client = ServiceRegistry.getMinecraftService().getClient();
      client.execute(() -> {
         ClientPacketListener handler = client.getConnection();
         if (handler == null) {
            return;
         }

         if (text.startsWith("/")) {
            handler.getConnection().send(new ServerboundChatCommandPacket(text.substring(1)));
         } else {
            handler.sendChat(text);
         }
      });
   }

   public void sendMessage(Component message) {
      if (ModBuild.BARE) {
         return;
      }
      Minecraft client = ServiceRegistry.getMinecraftService().getClient();
      client.execute(() -> {
         LocalPlayer player = client.player;
         if (player != null) {
            player.displayClientMessage(this.joinTexts(this.HMTextComponent, message), false);
         }
      });
   }

   public Component legacyComponent(String text) {
      MutableComponent result = Component.empty();
      Style style = Style.EMPTY;
      StringBuilder segment = new StringBuilder();

      for (int i = 0; i < text.length(); i++) {
         char ch = text.charAt(i);
         if (ch == '§' && i + 1 < text.length()) {
            if (!segment.isEmpty()) {
               result.append(Component.literal(segment.toString()).withStyle(style));
               segment.setLength(0);
            }
            ChatFormatting formatting = ChatFormatting.getByCode(text.charAt(++i));
            if (formatting != null) {
               style = formatting == ChatFormatting.RESET ? Style.EMPTY : style.applyFormat(formatting);
            }
         } else {
            segment.append(ch);
         }
      }

      if (!segment.isEmpty()) {
         result.append(Component.literal(segment.toString()).withStyle(style));
      }

      return result;
   }

   public String stripFormatting(String text) {
      text = text.replaceAll("§[0-9a-zA-Z]", "");
      for (String prefix : new String[]{
         "[ALL] ʟ",
         "[Тихий] ❖",
         "SC |",
         "HW >",
         " ▬▬▬",
         "▬▬▬",
         "[PMS]:",
         "◀",
         "[HM]",
         "[HAC]",
         "[я"
      }) {
         if (text.startsWith(prefix)) {
            return null;
         }
      }

      String copyButtonText = ServiceRegistry.getConfigManager().getState().getCopyButtonText().replaceAll("§[0-9a-zA-Z]", "");
      return text.replace(copyButtonText, "");
   }

   public String escapeCommandArg(String text) {
      if (text.matches("(?i)lite120-anarchy-(\\d+)")) {
         return "lite120-" + text.replaceAll("(?i)lite120-anarchy-", "");
      }
      if (text.matches("(?i)lite-anarchy-(\\d+)")) {
         return "lite-" + text.replaceAll("(?i)lite-anarchy-", "");
      }
      if (text.matches("(?i)classic-anarchy-(\\d+)")) {
         return "classic-" + text.replaceAll("(?i)classic-anarchy-", "");
      }
      if (text.matches("(?i)prime-anarchy-(\\d+)")) {
         return "prime-" + text.replaceAll("(?i)prime-anarchy-", "");
      }
      if (text.matches("(?i)l2anarchy\\d+")) {
         return "lite120-" + text.replaceAll("(?i)l2anarchy", "");
      }
      if (text.matches("(?i)lanarchy\\d+")) {
         return "lite-" + text.replaceAll("(?i)lanarchy", "");
      }
      if (text.matches("(?i)pranarchy\\d+")) {
         return "prime-" + text.replaceAll("(?i)pranarchy", "");
      }
      if (text.matches("(?i)anarchy\\d+")) {
         return "classic-" + text.replaceAll("(?i)anarchy", "");
      }
      if (text.equals("l2anarchy")) {
         return "lite120-1";
      }
      if (text.equals("lanarchy")) {
         return "lite-1";
      }
      if (text.equals("anarchy")) {
         return "classic-1";
      }
      if (text.equals("lpvp")) {
         return "lpvp";
      }
      if (text.equals("pranarchy")) {
         return "prime-1";
      }
      if (text.startsWith("l2") && text.contains("anarchy")) {
         return "lite120-" + text.split("anarchy")[1].replaceAll("\\D.*", "");
      }
      if (text.matches("(?i).*lite-anarchy-\\d+.*")) {
         return "lite-" + text.replaceAll("(?i).*lite-anarchy-(\\d+).*", "$1");
      }
      if (text.matches("(?i).*lite120-anarchy-\\d+.*")) {
         return "lite120-" + text.replaceAll("(?i).*lite120-anarchy-(\\d+).*", "$1");
      }
      if (text.matches("(?i).*prime-anarchy-\\d+.*")) {
         return "prime-" + text.replaceAll("(?i).*prime-anarchy-(\\d+).*", "$1");
      }
      if (text.matches("(?i).*classic-anarchy-\\d+.*")) {
         return "classic-" + text.replaceAll("(?i).*classic-anarchy-(\\d+).*", "$1");
      }
      if (text.startsWith("l") && text.contains("anarchy") && !text.toLowerCase().startsWith("prime")) {
         return "lite-" + text.split("anarchy")[1].replaceAll("\\D.*", "");
      }
      if (text.contains("anarchy")) {
         return "classic-" + text.split("anarchy")[1].replaceAll("\\D.*", "");
      }
      return text.toLowerCase();
   }

   public boolean isPlaytimeBlockLine(String message) {
      return message.contains("PlayTimeAPI") || message.matches("-{10,}.*");
   }

   public boolean isPlaytimeOutputLine(String message) {
      return this.isPlaytimeBlockLine(message)
         || message.startsWith("Текущая")
         || message.startsWith("Последняя активность")
         || message.startsWith("Последний")
         || message.startsWith("Активность ")
         || message.startsWith("Общее время")
         || message.startsWith("Время бездействия")
         || message.startsWith("Игрок:")
         || message.isEmpty();
   }

   public String parsePlaytimeLocation(String message) {
      if (!message.startsWith("Текущая")) {
         return null;
      }

      String[] parts = message.split(": ", 2);
      if (parts.length < 2) {
         return null;
      }

      String location = parts[1].trim();
      if (location.length() >= 2) {
         char first = location.charAt(0);
         char last = location.charAt(location.length() - 1);
         if (first == '(' && last == ')') {
            location = location.substring(1, location.length() - 1).trim();
         } else if (first == '[' && last == ']') {
            location = location.substring(1, location.length() - 1).trim();
         }
      }

      return location;
   }

   public String normalizeServerLocation(String location) {
      if (location == null || location.isBlank()) {
         return "";
      }

      location = location.trim();
      if (location.matches("(?i)[a-z0-9]+-\\d+") || location.equalsIgnoreCase("lpvp")) {
         return location.toLowerCase();
      }

      return this.escapeCommandArg(location);
   }

   public boolean matchesCommand(String[] commands, String text) {
      return Arrays.asList(commands).contains(text);
   }

   public boolean isHmCommand(String text) {
      try {
         Integer.parseInt(text);
         return true;
      } catch (Exception ignored) {
         return false;
      }
   }

   public boolean isValidDuration(String duration) {
      if (duration == null || duration.length() < 2 || duration.length() > 5) {
         return false;
      }

      char lastChar = duration.charAt(duration.length() - 1);
      if (!String.valueOf(lastChar).matches("(?i)[smhd]")) {
         return false;
      }

      return this.isHmCommand(duration.substring(0, duration.length() - 1));
   }

   public boolean isFrzCommand(String text) {
      try {
         Long.parseLong(text);
         return true;
      } catch (Exception ignored) {
         return false;
      }
   }

   public void copyToClipboard(String text) {
      try {
         String os = System.getProperty("os.name").toLowerCase();
         if (os.contains("win")) {
            Runtime.getRuntime().exec(new String[]{"powershell", "-command", "Set-Clipboard -Value '" + text + "'"});
         } else if (os.contains("mac")) {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo \"" + text + "\" | pbcopy"});
         } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo \"" + text + "\" | xclip -selection clipboard"});
         } else {
            throw new Exception("Неизвестная OS.");
         }
      } catch (Exception exception) {
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.EXCEPTION,
            "§3§lИсключение",
            "Исключение в ChatService/copyToClipboard: §4" + exception,
            5.0F
         );
      }
   }

   public MutableComponent text(String text) {
      return Component.literal(text).withStyle(style -> style
         .withHoverEvent(new HoverEvent.ShowText(Component.literal("Нажмите, чтобы подставить команду.")))
         .withClickEvent(new ClickEvent.SuggestCommand(text.replaceAll("§[0-9a-zA-Z]", "")))
      );
   }

   public MutableComponent textWithPrefix(String display, String hover, String command) {
      return Component.literal(display).withStyle(style -> style
         .withHoverEvent(new HoverEvent.ShowText(Component.literal(hover)))
         .withClickEvent(new ClickEvent.SuggestCommand(command))
      );
   }

   public MutableComponent textColored(String display, String hover) {
      return Component.literal(display).withStyle(style -> style
         .withHoverEvent(new HoverEvent.ShowText(Component.literal(hover)))
      );
   }

   public MutableComponent textStyled(String display, String hover, String value) {
      return Component.literal(display).withStyle(style -> style
         .withHoverEvent(new HoverEvent.ShowText(Component.literal(hover)))
         .withClickEvent(new ClickEvent.CopyToClipboard(value))
      );
   }

   public MutableComponent textNotification(String display, String hover, String url) {
      return Component.literal(display).withStyle(style -> style
         .withHoverEvent(new HoverEvent.ShowText(Component.literal(hover)))
         .withClickEvent(new ClickEvent.OpenUrl(java.net.URI.create(url)))
      );
   }

   public MutableComponent joinTexts(Component... texts) {
      MutableComponent result = Component.empty();
      for (Component part : texts) {
         result.append(part);
      }
      return result;
   }
}

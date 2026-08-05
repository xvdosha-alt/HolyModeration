package me.xv.holymoderation.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import me.xv.holymoderation.core.BaseService;
import me.xv.holymoderation.core.ServiceContext;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.util.NotificationType;
import me.xv.holymoderation.service.ModerLocationResolver;
import me.xv.holymoderation.service.ModerPlaytimeService;

public class CheckoutsService extends BaseService {
   public void init(ServiceContext context) {
      this.completeCheckout(context, true, true, 1000L);
   }

   public void completeCheckoutAfterLeave(ServiceContext context) {
      this.completeCheckout(context, false, false, 0L);
   }

   private void completeCheckout(
      ServiceContext context,
      boolean releaseOnServer,
      boolean showSuccessToast,
      long finalizeDelayMs
   ) {
      String player = context.getStateService().getPlayer();
      if (player.isEmpty()) {
         return;
      }

      if (releaseOnServer) {
         this.releaseCheckoutPlayer(context, player);
      } else {
         ServiceRegistry.getDebugLogService().write("checkout", "leave player=" + player);
         context.getChatService().sendChatOrCommand("/prova");
         restoreModerAfterCheckout(context);
      }

      if (showSuccessToast) {
         ServiceRegistry.getNotificationService().showToast(
            NotificationType.SUCCESS,
            "§a§lУспех",
            "Вы успешно закончили проверку.",
            5.0F
         );
      }

      String finalizedPlayer = new String(player.toCharArray());
      context.getStateService().setPlayer("");

      Runnable finalizeTask = () -> finalizeCheckout(context, finalizedPlayer);
      if (finalizeDelayMs <= 0L) {
         finalizeTask.run();
      } else {
         context.getSchedulerService().getExecutor().schedule(
            finalizeTask,
            finalizeDelayMs,
            TimeUnit.MILLISECONDS
         );
      }
   }

   public boolean releaseActiveCheckout(ServiceContext context) {
      String player = context.getStateService().getPlayer();
      if (player.isEmpty()) {
         return false;
      }

      this.releaseCheckoutPlayer(context, player);
      context.getStateService().setPlayer("");
      return true;
   }

   private void releaseCheckoutPlayer(ServiceContext context, String player) {
      ServiceRegistry.getDebugLogService().write("checkout", "release player=" + player + " cmd=/freezing");
      context.getChatService().sendChatOrCommand("/freezing " + player);
      context.getChatService().sendChatOrCommand("/prova");
      restoreModerAfterCheckout(context);
   }

   private static void restoreModerAfterCheckout(ServiceContext context) {
      if (context.getConfigManager().getState().getAutoVanishEnabled()
         && !context.getStateService().getVanishEnabled()) {
         context.getChatService().sendChatOrCommand("/v");
         context.getStateService().setVanishEnabled(true);
      }
      if (context.getConfigManager().getState().getAutoGm3Enabled()
         && !context.getStateService().getGm3Enabled()) {
         context.getChatService().sendChatOrCommand("/gm 3");
         context.getStateService().setGm3Enabled(true);
      }
   }

   public boolean startCheckout(String player, ServiceContext context) {
      if (!context.getStateService().getPlayer().isEmpty()) {
         context.getNotificationService().showToast(
            NotificationType.ERROR,
            "§c§lОшибка",
            "Вы уже проверяете какого-то игрока. Сначала закончите текущую проверку --> §6§6§l/unfreezing§f или §6§6§l/unfrz§f",
            5.0F
         );
         return false;
      }

      ServiceRegistry.getNotificationService().showToast(
         NotificationType.SUCCESS,
         "§a§lУспех",
         "Вы успешно начали проверку.",
         5.0F
      );
      context.getStateService().setPlayer(player);
      ModerPlaytimeService.requestModerLocation(context, true);
      String location = ModerLocationResolver.resolve(context, player);
      if (!location.isBlank()) {
         context.getStateService().setModerLocation(location);
      }
      ServiceRegistry.getDebugLogService().write(
         "journal",
         "checkout started player=" + player
            + " moderLocation=" + context.getStateService().getModerLocation()
            + " lastAnarchy=" + context.getStateService().getLastAnarchyLocation()
            + " inHub=" + context.getStateService().getInHub()
      );
      context.getChatService().sendChatOrCommand("/freezing " + context.getStateService().getPlayer());
      if (context.getConfigManager().getState().getAutoTpEnabled()) {
         context.getChatService().sendChatOrCommand("/warp logo");
      }
      context.getChatService().sendChatOrCommand("/prova");
      if (context.getConfigManager().getState().getDupeIpEnabled()) {
         ServiceRegistry.getDupeIpScannerService().startScan(player);
      }
      this.scheduleCheckoutStart(context);
      return true;
   }

   private void scheduleCheckoutStart(ServiceContext context) {
      List<String> textsList = context.getConfigManager().getState().getTextsList();
      long startDelay = textsList.isEmpty() ? 0L : 5L;
      TimeUnit startUnit = textsList.isEmpty() ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS;

      context.getSchedulerService().getExecutor().schedule(() -> {
         if (context.getStateService().getPlayer().isEmpty()) {
            return;
         }

         if (!textsList.isEmpty()) {
            this.endCheckout(context.getStateService().getPlayer(), context);
         }

         broadcastCheckout(context);
      }, startDelay, startUnit);

      context.getSchedulerService().getExecutor().schedule(
         () -> scheduleCheckoutTimeout(context),
         8L,
         TimeUnit.SECONDS
      );
   }

   public void endCheckout(String player, ServiceContext context) {
      List<String> textsList = context.getConfigManager().getState().getTextsList();
      if (textsList.isEmpty()) {
         context.getNotificationService().showToast(
            NotificationType.ERROR,
            "§c§lОшибка",
            "У вас нет настроенных текстов для отправки. Добавить текст --> §6§6§l/hm textadd§f",
            5.0F
         );
      }

      for (int index = 0; index < textsList.size(); index++) {
         String message = textsList.get(index);
         context.getSchedulerService().getExecutor().schedule(
            () -> sendCheckoutMessage(context, player, message),
            (long)index * 200L,
            TimeUnit.MILLISECONDS
         );
      }
   }

   private static void sendCheckoutMessage(ServiceContext context, String player, String message) {
      context.getChatService().sendChatOrCommand(
         "/msg " + player + " " + message.replace("§", "&")
      );
   }

   private static void broadcastCheckout(ServiceContext context) {
      if (context.getStateService().getPlayer().isEmpty()) {
         return;
      }

      String player = context.getStateService().getPlayer();
      sendClickableMessages(context, new String[][]{
         {
            "§b§lВнести проверку по репорту",
            "Нажмите, чтобы внести проверку по репорту",
            "/hm startcheckout " + player + " report"
         },
         {
            "§b§lВнести обычную проверку",
            "Нажмите, чтобы внести обычную проверку",
            "/hm startcheckout " + player + " checkout"
         },
         {
            "§b§lВнести проверку автобаера",
            "Нажмите, чтобы внести проверку автобаера",
            "/hm startcheckout " + player + " autobuy"
         },
         {
            "§b§lВнести проверку автоселлера",
            "Нажмите, чтобы внести проверку автоселлера",
            "/hm startcheckout " + player + " autosell"
         },
         {
            "§b§lВнести проверку кандидата",
            "Нажмите, чтобы внести проверку кандидата",
            "/hm startcheckout " + player + " candidate"
         },
         {
            "§b§lВнести проверку кастомки",
            "Нажмите, чтобы внести проверку кастомки",
            "/hm startcheckout " + player + " customka"
         },
         {
            "§b§lВнести проверку персонала",
            "Нажмите, чтобы внести проверку персонала",
            "/hm startcheckout " + player + " personal"
         },
         {
            "§b§lВнести проверку игрока, у которого много пройденных проверок",
            "Нажмите, чтобы внести проверку игрока, у которого много пройденных проверок",
            "/hm startcheckout " + player + " toManyChecks"
         }
      });
   }

   private static void scheduleCheckoutTimeout(ServiceContext context) {
      if (context.getStateService().getPlayer().isEmpty()) {
         return;
      }

      context.getChatService().sendChatOrCommand("/checkmute " + context.getStateService().getPlayer());
      if (context.getConfigManager().getState().getAutoVanishEnabled()
         && context.getStateService().getVanishEnabled()) {
         context.getChatService().sendChatOrCommand("/v");
         context.getStateService().setVanishEnabled(false);
      }
      if (context.getConfigManager().getState().getAutoGm3Enabled()
         && context.getStateService().getGm3Enabled()) {
         context.getChatService().sendChatOrCommand("/gm 0");
         context.getStateService().setGm3Enabled(false);
      }
   }


   private static void finalizeCheckout(ServiceContext context, String player) {
      sendClickableMessages(context, new String[][]{
         {
            "§b§lЗакончить проверку с результатом 'чистый'",
            "Нажмите, чтобы закончить проверку с результатом 'чистый'",
            "/hm endcheckout clean"
         },
         {
            "§b§lЗакончить проверку с результатом 'бан' + снести стеш",
            "Нажмите, чтобы закончить проверку с результатом 'бан' + снести стеш",
            "/hm endcheckout ban " + player + " true"
         },
         {
            "§b§lЗакончить проверку с результатом 'бан' + не сносить стеш",
            "Нажмите, чтобы закончить проверку с результатом 'бан' + не сносить стеш",
            "/hm endcheckout ban " + player + " false"
         },
         {
            "§b§lЗакончить проверку с результатом 'автобай'",
            "Нажмите, чтобы закончить проверку с результатом 'автобай'",
            "/hm endcheckout autobuy"
         },
         {
            "§b§lЗакончить проверку с результатом 'автоселл'",
            "Нажмите, чтобы закончить проверку с результатом 'автоселл'",
            "/hm endcheckout autosell"
         }
      });
   }

   private static void sendClickableMessages(ServiceContext context, String[][] entries) {
      ChatService chat = context.getChatService();
      for (String[] entry : entries) {
         chat.sendMessage(chat.textWithPrefix(entry[0], entry[1], entry[2]));
      }
   }

   static {
   }
}

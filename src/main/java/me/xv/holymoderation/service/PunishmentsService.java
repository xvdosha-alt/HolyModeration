package me.xv.holymoderation.service;

import me.xv.holymoderation.core.ServiceContext;
import me.xv.holymoderation.core.BaseService;
import me.xv.holymoderation.util.NotificationType;

public class PunishmentsService extends BaseService {
   public void executePunishment(String command, String player, String reason, boolean appendVk, ServiceContext context) {
      if (!appendVk) {
         context.getChatService().sendChatOrCommand(command + " " + player + " " + reason + " -s");
         return;
      }

      if (context.getStateService().getVkUrl().isEmpty()
         && !command.equals("/tempmute")
         && !command.equals("/tempmuteip")) {
         context.getNotificationService().showToast(
            NotificationType.ERROR,
            "§c§lОшибка",
            "Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.",
            5.0F
         );
         return;
      }

      String suffix = appendVk
         ? " | Вопросы? " + context.getStateService().getVkUrl() + " -s"
         : " -s";
      context.getChatService().sendChatOrCommand(command + " " + player + " " + reason + suffix);
   }

   public boolean shouldExecutePunishment(
      String command, String player, String duration, String reason, boolean appendVk, ServiceContext context
   ) {
      if (!context.getChatService().isHmCommand(duration)) {
         context.getNotificationService().showToast(
            NotificationType.ERROR,
            "§c§lОшибка",
            "Неверный формат времени. Должно быть 1-9999s/S, 1-9999m/M, 1-9999h/H, 1-9999d/D",
            5.0F
         );
         return false;
      }

      if (reason.contains(" | Вопросы")) {
         context.getChatService().sendChatOrCommand(command + " " + player + " " + duration + " " + reason + " -s");
         return true;
      }

      if (context.getStateService().getVkUrl().isEmpty()
         && !command.equals("/tempmute")
         && !command.equals("/tempmuteip")) {
         context.getNotificationService().showToast(
            NotificationType.ERROR,
            "§c§lОшибка",
            "Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.",
            5.0F
         );
         return false;
      }

      String suffix = appendVk
         ? " | Вопросы? " + context.getStateService().getVkUrl() + " -s"
         : " -s";
      context.getChatService().sendChatOrCommand(command + " " + player + " " + duration + " " + reason + suffix);
      return true;
   }

   static {
   }
}

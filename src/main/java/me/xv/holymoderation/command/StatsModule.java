package me.xv.holymoderation.command;

import me.xv.holymoderation.config.ModState;
import me.xv.holymoderation.event.ChatMessageEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.service.ChatService;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

public class StatsModule extends BaseCommandHandler {
   @Subscribe(priority = 96)
   public void onStatsChatMessage(ChatMessageEvent event) {
      Component original = event.getMessage();
      String stripped = original.getString().replaceAll("§[0-9a-zA-Z]", "");
      String player = this.serviceContext.getStateService().getPlayer();
      ChatService chat = this.serviceContext.getChatService();
      ModState modState = this.serviceContext.getConfigManager().getState();

      if (!player.isEmpty()
         && stripped.contains(":")
         && stripped.split(":")[0].contains(player)
         && !stripped.startsWith("Игрок")
         && !stripped.startsWith("История")
         && !stripped.startsWith("[я ->")) {
         String[] colonParts = stripped.split(": ");
         String messageBody = colonParts[colonParts.length - 1];
         MutableComponent marker = Component.literal(modState.getPlayerMarker() + " §f" + player + " §5-> ");
         MutableComponent styled = chat.textStyled(
            messageBody,
            "Оригинальное сообщение: " + stripped + "\nНажмите, чтобы скопировать сообщение игрока.",
            colonParts[1]
         );
         event.setMessage(chat.joinTexts(marker, styled));
      }

      if (modState.getCopyButtonEnabled() && !stripped.startsWith("[HM]")) {
         MutableComponent copyButton = Component.literal(" ").append(
            chat.textStyled(
               modState.getCopyButtonText(),
               "Нажмите, чтобы скопировать сообщение.",
               stripped
            )
         );
         event.setMessage(chat.joinTexts(original, copyButton));
      }
   }

   static {
   }
}

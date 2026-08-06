package me.xv.holymoderation.command;

import java.util.Arrays;
import java.util.List;
import me.xv.holymoderation.config.ModState;
import me.xv.holymoderation.event.ChatMessageEvent;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.event.RenderHudEvent;
import me.xv.holymoderation.event.Subscribe;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.gui.HudPanelLayout;
import me.xv.holymoderation.gui.HudPanelRenderer;
import me.xv.holymoderation.gui.HudPanelStyle;
import me.xv.holymoderation.gui.HudPanelType;
import me.xv.holymoderation.service.Render2DService;
import me.xv.holymoderation.service.StateService;
import me.xv.holymoderation.util.NotificationType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class FreezerModule extends BaseCommandHandler {
   private static final List<String> CHECKOUT_REASONS = List.of(
      "report", "checkout", "autobuy", "autosell", "customka", "personal", "toManyChecks", "candidate"
   );
   private static final List<String> CHECKOUT_RESULTS = List.of("clean", "ban", "autobuy", "autosell");
   private static final List<String> DESTROY_STASH_VALUES = List.of("true", "false");

   private final String[] freezerCommands = new String[]{
      "/freezing", "/frz", "freezing", "frz", "sban", "sendtexts", "unfreezing", "unfrz"
   };
   private final String[] apiCommands = new String[]{"endcheckout", "startcheckout"};
   private boolean banChecking = false;
   private boolean destroyStash;
   private boolean messageIsCheckbanInfo;
   private String banReason;
   private float coAnim = 0.0F;
   private float coAnimTarget = 0.0F;
   private float coCurrentWidth = 1.0F;
   private float coCurrentHeight = 1.0F;
   private long checkoutStartMillis = 0L;
   private String coLastPlayer = "";
   private boolean coClearDisplayWhenHidden = false;

   @Subscribe
   public void onFrzCommand(CommandEvent event) {
      String command = event.getCommand();
      String[] parts = command.split(" ");
      if (command.startsWith("hm") && parts.length < 2) {
         return;
      }

      String subCommand = command.startsWith("hm") ? parts[1] : "/" + parts[0];
      var chatService = this.serviceContext.getChatService();
      if ((chatService.matchesCommand(this.freezerCommands, subCommand)
            || chatService.matchesCommand(this.apiCommands, subCommand))
         && this.serviceContext.getStateService().getInHub()) {
         this.showWarning("В хабе этого делать нельзя.");
         return;
      }

      if (chatService.matchesCommand(this.freezerCommands, subCommand)) {
         this.handleFreezerCommand(event, command, subCommand);
         return;
      }

      if (chatService.matchesCommand(this.apiCommands, subCommand)) {
         this.handleApiCommand(command, subCommand);
      }
   }

   private void handleFreezerCommand(CommandEvent event, String command, String subCommand) {
      switch (subCommand) {
         case "/freezing":
         case "/frz":
            this.handleFreezeCommand(event, command);
            break;
         case "unfreezing":
         case "unfrz":
            this.handleUnfreezeCommand();
            break;
         case "sban":
            this.handleSbanCommand(command);
            break;
         case "freezing":
         case "frz":
            this.handleStartCheckoutCommand(command);
            break;
         case "sendtexts":
            this.handleEndCheckoutCommand(command);
            break;
         default:
            break;
      }
   }

   private void handleFreezeCommand(CommandEvent event, String command) {
      event.setCancelled(true);
      String[] parts = command.split(" ", 2);
      if (parts.length == 1) {
         this.showError("Вы не указали ник игрока.");
         return;
      }

      String target = parts[1];
      StateService state = this.serviceContext.getStateService();
      if (target.equals(state.getPlayer())) {
         this.showWarning(
            "Этот игрок находиться у вас на проверке. Для его разморозки используйте §6§6§l/unfreezing§f или §6§6§l/unfrz§f"
         );
         return;
      }

      if (state.getPlayer().isEmpty()) {
         if (this.serviceContext.getCheckoutsService().startCheckout(target, this.serviceContext)) {
            this.setCheckoutStartMillis(target);
         }
         return;
      }

      this.serviceContext.getChatService().sendChatOrCommand("/freezing " + target);
   }

   private void handleUnfreezeCommand() {
      if (this.serviceContext.getStateService().getPlayer().isEmpty()) {
         this.showWarning("Вы никого не проверяете.");
         return;
      }

      ServiceRegistry.getDebugLogService().write(
         "checkout",
         "unfrz player=" + this.serviceContext.getStateService().getPlayer()
      );
      this.hideCheckoutHud();
      this.serviceContext.getCheckoutsService().init(this.serviceContext);
   }

   private void handleSbanCommand(String command) {
      String[] parts = command.split(" ", 4);
      if (this.serviceContext.getStateService().getPlayer().isEmpty()) {
         this.showWarning("Вы никого не проверяете.");
         return;
      }

      if (parts.length == 2) {
         this.showError("Вы не указали время и причину бана.");
         return;
      }
      if (parts.length == 3) {
         this.showError("Вы не указали причину бана.");
         return;
      }

      String duration = parts[2];
      String reason = "2.4 (" + parts[3] + ")";
      if (!this.serviceContext.getPunishmentsService().shouldExecutePunishment(
         "/banip", this.serviceContext.getStateService().getPlayer(), duration, reason, true, this.serviceContext
      )) {
         return;
      }

      this.serviceContext.getCheckoutsService().init(this.serviceContext);
   }

   private void handleStartCheckoutCommand(String command) {
      String[] parts = command.split(" ", 3);
      if (parts.length < 3) {
         this.showError("Вы не указали ник игрока.");
         return;
      }

      if (this.serviceContext.getCheckoutsService().startCheckout(parts[2], this.serviceContext)) {
         this.setCheckoutStartMillis(parts[2]);
      }
   }

   private void handleEndCheckoutCommand(String command) {
      String[] parts = command.split(" ", 3);
      if (parts.length == 2) {
         this.showError("Вы не указали ник игрока.");
         return;
      }

      this.serviceContext.getCheckoutsService().endCheckout(parts[2], this.serviceContext);
   }

   private void handleApiCommand(String command, String subCommand) {
      if (subCommand.equals("startcheckout")) {
         this.handleApiStartCheckout(command);
      } else if (subCommand.equals("endcheckout")) {
         this.handleApiEndCheckout(command);
      }
   }

   private void handleApiStartCheckout(String command) {
      String[] parts = command.split(" ", 4);
      if (parts.length == 2) {
         this.showError("Вы не указали ник игрока и причину проверки.");
         return;
      }
      if (parts.length == 3) {
         this.showError("Вы не указали причину проверки.");
         return;
      }

      String player = parts[2];
      String reason = parts[3];
      if (!CHECKOUT_REASONS.contains(reason)) {
         this.showError("Некорректная причина проверки.");
         return;
      }

      this.serviceContext.getChatService().copyToClipboard(player);
      this.serviceContext.getNotificationService().showToast(
         NotificationType.SUCCESS,
         "§a§lУспех",
         "Ник проверяемого скопирован: " + player,
         5.0F
      );
      this.setCheckoutStartMillis(player);
   }

   private void handleApiEndCheckout(String command) {
      String[] parts = command.split(" ", 6);
      if (parts.length == 2) {
         this.showError("Вы не указали результат проверки.");
         return;
      }

      String result = parts[2];
      if (!CHECKOUT_RESULTS.contains(result)) {
         this.showError("Некорректный результат проверки.");
         return;
      }

      if (this.serviceContext.getCheckoutsService().releaseActiveCheckout(this.serviceContext)) {
         ServiceRegistry.getDebugLogService().write("checkout", "released on endcheckout result=" + result);
      }

      if ("ban".equals(result)) {
         if (parts.length < 5) {
            this.showError("Вы не указали ник игрока и необходимость снести стеш.");
            return;
         }

         String destroyStashValue = parts[4];
         if (!DESTROY_STASH_VALUES.contains(destroyStashValue)) {
            this.showError("Некорректная необходимость снести стеш.");
            return;
         }

         this.destroyStash = "true".equals(destroyStashValue);
         this.banChecking = true;
         this.serviceContext.getChatService().sendChatOrCommand("/checkban " + parts[3]);
      } else {
         this.serviceContext.getNotificationService().showToast(
            NotificationType.SUCCESS,
            "§a§lУспех",
            "Результат проверки: " + result,
            5.0F
         );
      }

      this.hideCheckoutHud();
   }

   @Subscribe
   public void onFrzChatMessage(ChatMessageEvent event) {
      String message = this.serviceContext.getChatService().stripFormatting(event.getMessage().getString());
      if (message == null) {
         return;
      }

      StateService state = this.serviceContext.getStateService();
      ModState modState = this.serviceContext.getConfigManager().getState();

      if (!state.getPlayer().isEmpty() && this.isCheckoutPlayerLeave(message, state.getPlayer())) {
         String checkoutPlayer = state.getPlayer();
         ServiceRegistry.getDebugLogService().write("checkout", "detected leave player=" + checkoutPlayer);
         this.serviceContext.getNotificationService().showToast(
            NotificationType.WARNING,
            "§6§lПредупреждение",
            "Игрок " + checkoutPlayer + " покинул сервер. Выберите результат проверки.",
            5.0F
         );
         if (modState.getAutoBanEnabled()) {
            this.serviceContext.getPunishmentsService().shouldExecutePunishment(
               "/banip", checkoutPlayer, "30d", "2.4 (Лив с проверки)", true, this.serviceContext
            );
         }
         this.hideCheckoutHud();
         this.serviceContext.getCheckoutsService().completeCheckoutAfterLeave(this.serviceContext);
      }

      if (modState.getAutoAnyDeskEnabled() && !state.getPlayer().isEmpty() && message.contains(state.getPlayer())) {
         this.tryCopyAnyDeskFromPm(message, state.getPlayer());
         this.tryCopyAnyDeskFromChat(message);
      }

      if (!this.banChecking) {
         return;
      }

      if (message.equals("Цель не забанена!") || message.equals("История не найдена.")) {
         event.setCancelled(true);
         this.showError(
            "Проверка не была закончена, т.к. не удалось определить причину бана игрока. Пожалуйста, допишите причину вручную."
         );
         this.banChecking = false;
      }

      if (message.startsWith("Игрок [")) {
         this.messageIsCheckbanInfo = true;
      }

      if (message.startsWith("Причина:")) {
         this.banReason = message.split("Причина: ")[1].split(" \\| ")[0];
      }

      if (this.messageIsCheckbanInfo) {
         event.setCancelled(true);
      }

      if (message.startsWith("IP бан:")) {
         this.messageIsCheckbanInfo = false;
         this.banChecking = false;
      }
   }

   private void tryCopyAnyDeskFromPm(String message, String player) {
      if (!message.startsWith("[" + player + " ->")) {
         return;
      }

      String code = message.split("я]", 2)[1].replace(" ", "");
      if (this.serviceContext.getChatService().isFrzCommand(code)
         && code.length() >= 9 && code.length() <= 11) {
         this.serviceContext.getChatService().copyToClipboard(code);
         this.serviceContext.getNotificationService().showToast(
            NotificationType.SUCCESS, "§a§lУспех", "Скопирован анидеск из лс: " + code, 5.0F
         );
      }
   }

   private void tryCopyAnyDeskFromChat(String message) {
      String[] chatParts = message.split(":", 2);
      if (chatParts.length < 2) {
         return;
      }

      String code = chatParts[1].replace(" ", "");
      if (this.serviceContext.getChatService().isFrzCommand(code)
         && code.length() >= 9 && code.length() <= 11) {
         this.serviceContext.getChatService().copyToClipboard(code);
         this.serviceContext.getNotificationService().showToast(
            NotificationType.SUCCESS, "§a§lУспех", "Скопирован анидеск из чата: " + code, 5.0F
         );
      }
   }

   @Subscribe
   public void onFrzRenderHud(RenderHudEvent event) {
      if (me.xv.holymoderation.core.ModBuild.BARE) {
         return;
      }
      this.coAnim += (this.coAnimTarget - this.coAnim) * 0.15F;

      String player = this.serviceContext.getStateService().getPlayer();
      if (!this.coLastPlayer.equals(player)) {
         if (this.coLastPlayer.isEmpty() && !player.isEmpty()) {
            this.checkoutStartMillis = System.currentTimeMillis();
            this.coAnimTarget = 1.0F;
         }
         if (!this.coLastPlayer.isEmpty() && player.isEmpty()) {
            this.coAnimTarget = 0.0F;
            this.coClearDisplayWhenHidden = true;
         }
         this.coLastPlayer = player;
      }

      if (this.coAnim < 0.01F && player.isEmpty()) {
         return;
      }

      GuiGraphics graphics = event.getGuiGraphics();
      Font font = this.serviceContext.getMinecraftService().getClient().font;
      Render2DService render = this.serviceContext.getRender2DService();

      long elapsedSeconds = this.checkoutStartMillis == 0L
         ? 0L
         : (System.currentTimeMillis() - this.checkoutStartMillis) / 1000L;
      String timer = String.format("%d:%02d", elapsedSeconds / 60L, elapsedSeconds % 60L);
      String displayPlayer = player.isEmpty() ? this.coLastPlayer : player;
      HudPanelRenderer.Content content = new HudPanelRenderer.Content(
         "CHECK",
         "§f" + displayPlayer,
         "§d§l" + timer + " §7| проверка"
      );

      float boxWidth = HudPanelRenderer.measureWidth(font, content);
      float boxHeight = HudPanelRenderer.measureHeight(font, content);
      this.coCurrentWidth += (boxWidth - this.coCurrentWidth) * 0.2F;
      this.coCurrentHeight += (boxHeight - this.coCurrentHeight) * 0.2F;

      float renderWidth = Math.max(1.0F, this.coCurrentWidth * this.coAnim);
      float renderHeight = Math.max(1.0F, this.coCurrentHeight * this.coAnim);
      float screenWidth = graphics.guiWidth();
      float screenHeight = graphics.guiHeight();
      HudPanelLayout.Bounds bounds = HudPanelLayout.resolve(
         HudPanelType.CHECKOUT,
         screenWidth,
         screenHeight,
         renderWidth,
         renderHeight
      );

      HudPanelRenderer.drawCentered(
         render,
         graphics,
         font,
         bounds.centerX(),
         bounds.topY(),
         renderWidth,
         renderHeight,
         HudPanelStyle.checkout(),
         content,
         HudPanelType.CHECKOUT
      );

      if (this.coAnim < 0.02F && this.coAnimTarget == 0.0F && this.coClearDisplayWhenHidden) {
         this.checkoutStartMillis = 0L;
         this.coClearDisplayWhenHidden = false;
         this.coLastPlayer = "";
      }
   }

   private void setCheckoutStartMillis(String player) {
      this.checkoutStartMillis = System.currentTimeMillis();
      this.coLastPlayer = player;
      this.coAnimTarget = 1.0F;
   }

   private void hideCheckoutHud() {
      this.coAnimTarget = 0.0F;
      this.coClearDisplayWhenHidden = true;
   }

   private boolean isCheckoutPlayerLeave(String message, String player) {
      if (player.isEmpty()) {
         return false;
      }

      if (message.startsWith("▶ Замороженный игрок " + player)) {
         return true;
      }

      if (!message.contains(player)) {
         return false;
      }

      String lower = message.toLowerCase();
      boolean leaveHint = lower.contains("покинул")
         || lower.contains("вышел")
         || lower.contains("отключ")
         || lower.contains("disconnect")
         || lower.contains("лив");
      boolean checkoutHint = lower.contains("заморож") || lower.contains("провер");
      return leaveHint && checkoutHint;
   }

   private void showWarning(String message) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.WARNING, "§6§lПредупреждение", message, 5.0F
      );
   }

   private void showError(String message) {
      this.serviceContext.getNotificationService().showToast(
         NotificationType.ERROR, "§c§lОшибка", message, 5.0F
      );
   }

   static {
   }
}

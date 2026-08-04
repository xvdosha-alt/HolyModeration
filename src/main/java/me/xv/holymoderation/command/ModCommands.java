package me.xv.holymoderation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.service.ChatService;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public final class ModCommands {
   private static final SuggestionProvider<FabricClientCommandSource> ONLINE_PLAYERS = (context, builder) -> {
      ClientPacketListener handler = context.getSource().getClient().getConnection();
      if (handler != null) {
         for (PlayerInfo info : handler.getOnlinePlayers()) {
            String name = info.getProfile().name();
            if (SharedSuggestionProvider.matchesSubStr(builder.getRemaining(), name)) {
               builder.suggest(name);
            }
         }
      }
      return builder.buildFuture();
   };

   private static final SuggestionProvider<FabricClientCommandSource> CHECKOUT_REASONS = (context, builder) -> {
      for (String reason : new String[]{
         "report", "checkout", "autobuy", "autosell", "customka", "personal", "toManyChecks", "candidate"
      }) {
         if (SharedSuggestionProvider.matchesSubStr(builder.getRemaining(), reason)) {
            builder.suggest(reason);
         }
      }
      return builder.buildFuture();
   };

   private static final SuggestionProvider<FabricClientCommandSource> DURATIONS = (context, builder) -> {
      for (String duration : new String[]{"1d", "7d", "14d", "20d", "30d", "60d", "90d"}) {
         if (SharedSuggestionProvider.matchesSubStr(builder.getRemaining(), duration)) {
            builder.suggest(duration);
         }
      }
      return builder.buildFuture();
   };

   private ModCommands() {
   }

   public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
      LiteralArgumentBuilder<FabricClientCommandSource> hm = ClientCommandManager.literal("hm").executes(ctx -> dispatch("hm"));
      ChatService chatService = ServiceRegistry.getChatService();

      for (String command : chatService.NoArgCommands) {
         hm.then(ClientCommandManager.literal(command).executes(ctx -> dispatch("hm " + command)));
      }

      for (String command : chatService.PlayerCommands) {
         registerPlayerCommand(hm, command);
      }

      for (String command : chatService.OneArgCommands) {
         hm.then(
            ClientCommandManager.literal(command)
               .then(
                  ClientCommandManager.argument("arg1", StringArgumentType.greedyString())
                     .executes(ctx -> dispatch("hm " + command + " " + StringArgumentType.getString(ctx, "arg1")))
               )
         );
      }

      hm.then(
         ClientCommandManager.literal("sban")
            .then(
               ClientCommandManager.argument("duration", StringArgumentType.word())
                  .suggests(DURATIONS)
                  .then(
                     ClientCommandManager.argument("reason", StringArgumentType.greedyString())
                        .executes(ctx -> dispatch(
                           "hm sban "
                              + StringArgumentType.getString(ctx, "duration") + " "
                              + StringArgumentType.getString(ctx, "reason")
                        ))
                  )
            )
      );

      hm.then(
         ClientCommandManager.literal("startcheckout")
            .then(
               ClientCommandManager.argument("player", StringArgumentType.word())
                  .suggests(ONLINE_PLAYERS)
                  .then(
                     ClientCommandManager.argument("reason", StringArgumentType.word())
                        .suggests(CHECKOUT_REASONS)
                        .executes(ctx -> dispatch(
                           "hm startcheckout "
                              + StringArgumentType.getString(ctx, "player") + " "
                              + StringArgumentType.getString(ctx, "reason")
                        ))
                  )
            )
      );

      hm.then(
         ClientCommandManager.literal("textedit")
            .then(
               ClientCommandManager.argument("index", StringArgumentType.word())
                  .then(
                     ClientCommandManager.argument("text", StringArgumentType.greedyString())
                        .executes(ctx -> dispatch(
                           "hm textedit "
                              + StringArgumentType.getString(ctx, "index") + " "
                              + StringArgumentType.getString(ctx, "text")
                        ))
                  )
            )
      );

      for (String command : chatService.FourArgCommands) {
         hm.then(
            ClientCommandManager.literal(command)
               .then(
                  ClientCommandManager.argument("arg1", StringArgumentType.greedyString())
                     .then(
                        ClientCommandManager.argument("arg2", StringArgumentType.greedyString())
                           .then(
                              ClientCommandManager.argument("arg3", StringArgumentType.greedyString())
                                 .then(
                                    ClientCommandManager.argument("arg4", StringArgumentType.greedyString())
                                       .executes(ctx -> dispatch(
                                          "hm " + command + " "
                                             + StringArgumentType.getString(ctx, "arg1") + " "
                                             + StringArgumentType.getString(ctx, "arg2") + " "
                                             + StringArgumentType.getString(ctx, "arg3") + " "
                                             + StringArgumentType.getString(ctx, "arg4")
                                       ))
                                 )
                           )
                     )
               )
         );
      }

      dispatcher.register(hm);
      registerRootPlayerCommand(dispatcher, "frz");
      registerRootPlayerCommand(dispatcher, "freezing");
   }

   private static void registerRootPlayerCommand(
      CommandDispatcher<FabricClientCommandSource> dispatcher,
      String command
   ) {
      dispatcher.register(
         ClientCommandManager.literal(command)
            .then(
               ClientCommandManager.argument("player", StringArgumentType.word())
                  .suggests(ONLINE_PLAYERS)
                  .executes(ctx -> dispatch(command + " " + StringArgumentType.getString(ctx, "player")))
            )
      );
   }

   private static void registerPlayerCommand(
      LiteralArgumentBuilder<FabricClientCommandSource> hm,
      String command
   ) {
      hm.then(
         ClientCommandManager.literal(command)
            .then(
               ClientCommandManager.argument("player", StringArgumentType.word())
                  .suggests(ONLINE_PLAYERS)
                  .executes(ctx -> dispatch("hm " + command + " " + StringArgumentType.getString(ctx, "player")))
            )
      );
   }

   private static int dispatch(String command) {
      ServiceRegistry.getEventBus().post(new CommandEvent(command));
      return Command.SINGLE_SUCCESS;
   }
}

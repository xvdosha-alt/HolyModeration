package me.xv.holymoderation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.xv.holymoderation.core.ServiceRegistry;
import me.xv.holymoderation.event.CommandEvent;
import me.xv.holymoderation.service.ChatService;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

public final class ModCommands {
   private ModCommands() {
   }

   public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
      var hm = ClientCommandManager.literal("hm").executes(ctx -> dispatch("hm"));
      ChatService chatService = ServiceRegistry.getChatService();

      for (String command : chatService.NoArgCommands) {
         hm.then(ClientCommandManager.literal(command).executes(ctx -> dispatch("hm " + command)));
      }

      for (String command : chatService.PlayerCommands) {
         hm.then(
            ClientCommandManager.literal(command)
               .then(
                  ClientCommandManager.argument("player", StringArgumentType.word())
                     .executes(ctx -> dispatch("hm " + command + " " + StringArgumentType.getString(ctx, "player")))
               )
         );
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

      for (String command : chatService.TwoArgCommands) {
         hm.then(
            ClientCommandManager.literal(command)
               .then(
                  ClientCommandManager.argument("arg1", StringArgumentType.greedyString())
                     .then(
                        ClientCommandManager.argument("arg2", StringArgumentType.greedyString())
                           .executes(ctx -> dispatch(
                              "hm " + command + " "
                                 + StringArgumentType.getString(ctx, "arg1") + " "
                                 + StringArgumentType.getString(ctx, "arg2")
                           ))
                     )
               )
         );
      }

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
      dispatcher.register(
         ClientCommandManager.literal("frz")
            .then(
               ClientCommandManager.argument("player", StringArgumentType.word())
                  .executes(ctx -> dispatch("frz " + StringArgumentType.getString(ctx, "player")))
            )
      );
   }

   private static int dispatch(String command) {
      ServiceRegistry.getEventBus().post(new CommandEvent(command));
      return Command.SINGLE_SUCCESS;
   }
}

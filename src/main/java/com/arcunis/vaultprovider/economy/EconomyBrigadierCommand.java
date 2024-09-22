package com.arcunis.vaultprovider.economy;

import com.arcunis.vaultprovider.Main;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EconomyBrigadierCommand {

    public EconomyBrigadierCommand(Commands commands) {
        commands.register(
                Commands.literal("economy")
                        .requires(source -> source.getSender().hasPermission("vaultprovider.economy.manage"))
                        .then(
                                Commands.literal("bank")
                                        .then(
                                                Commands.argument("account", ArgumentTypes.player())
                                                        .then(
                                                                Commands.literal("deposit")
                                                                        .then(
                                                                                Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                                                                        .executes(PlayerExecutions::deposit)
                                                                        )
                                                        ).then(
                                                                Commands.literal("withdraw")
                                                                        .then(
                                                                                Commands.argument("amount",  DoubleArgumentType.doubleArg(0))
                                                                                        .executes(PlayerExecutions::withdraw)
                                                                        )
                                                        ).then(
                                                                Commands.literal("getBal")
                                                                        .executes(PlayerExecutions::getBal)
                                                        ).then(
                                                                Commands.literal("setBal")
                                                                        .then(
                                                                                Commands.argument("value",  DoubleArgumentType.doubleArg(0))
                                                                                        .executes(PlayerExecutions::setBal)
                                                                        )
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("account")
                                        .then(
                                                Commands.argument("bank", StringArgumentType.string())
                                                        .suggests((context, builder) -> bankSuggestion(builder))
                                                        .then(
                                                                Commands.literal("deposit")
                                                        ).then(
                                                                Commands.literal("withdraw")
                                                        ).then(
                                                                Commands.literal("getBal")
                                                        ).then(
                                                                Commands.literal("setBal")
                                                        ).then(
                                                                Commands.literal("getOwner")
                                                        ).then(
                                                                Commands.literal("setOwner")
                                                        ).then(
                                                                Commands.literal("getMembers")
                                                        ).then(
                                                                Commands.literal("addMember")
                                                        ).then(
                                                                Commands.literal("removeMember")
                                                        )
                                        )
                        ).build(),
                "Manage player accounts and banks",
                List.of("eco")
        );
    }

    public CompletableFuture<Suggestions> bankSuggestion(SuggestionsBuilder builder) {
        for (String bank : EconomyManager.getAllBankNames()) {
            builder.suggest(bank);
        }
        return builder.buildFuture();
    }

    private static class PlayerExecutions {

        public static int deposit(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            double amount = DoubleArgumentType.getDouble(ctx, "amount");
            double newBal = EconomyManager.depositAcc(player.getUniqueId(), amount);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                    "Deposited %s into %s's account. New balance: %s"
                            .formatted(
                                    Main.econ.format(amount),
                                    player.getName(),
                                    Main.econ.format(newBal)
                            )
                ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int withdraw(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            double amount = DoubleArgumentType.getDouble(ctx, "amount");
            double newBal = EconomyManager.withdrawAcc(player.getUniqueId(), amount);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            "Withdrawn %s from %s's account. New balance: %s"
                                    .formatted(
                                            Main.econ.format(amount),
                                            player.getName(),
                                            Main.econ.format(newBal)
                                    )
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int getBal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            double balance = EconomyManager.getAccBal(player.getUniqueId());

            ctx.getSource().getSender().sendMessage(Component.text("%s's balance is %s".formatted(player.getName(), Main.econ.format(balance))));

            return Command.SINGLE_SUCCESS;
        }
        public static int setBal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            double newBal = DoubleArgumentType.getDouble(ctx, "value");
            EconomyManager.setAccBal(player.getUniqueId(), newBal);

            ctx.getSource().getSender().sendMessage(Component.text("Set %s's account to %s".formatted(player.getName(), Main.econ.format(newBal))).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }


    }

    private static class BankExecutions {



    }

}

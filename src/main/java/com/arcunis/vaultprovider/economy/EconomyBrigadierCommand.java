package com.arcunis.vaultprovider.economy;

import com.arcunis.vaultprovider.Main;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EconomyBrigadierCommand {

    public EconomyBrigadierCommand(Commands commands) {
        commands.register(
                Commands.literal("economy")
                        .requires(source -> source.getSender().hasPermission("vaultprovider.economy.manage"))
                        .then(
                                Commands.literal("account")
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
                                Commands.literal("bank")
                                        .then(
                                                Commands.argument("bank", StringArgumentType.string())
                                                        .suggests(this::bankSuggestion)
                                                        .then(
                                                                Commands.literal("deposit")
                                                                        .then(
                                                                                Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                                                                        .executes(BankExecutions::deposit)
                                                                        )
                                                        ).then(
                                                                Commands.literal("withdraw")
                                                                        .then(
                                                                                Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                                                                        .executes(BankExecutions::withdraw)
                                                                        )
                                                        ).then(
                                                                Commands.literal("getBal")
                                                                        .executes(BankExecutions::getBal)
                                                        ).then(
                                                                Commands.literal("setBal")
                                                                        .then(
                                                                                Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                                                        .executes(BankExecutions::setBal)
                                                                        )
                                                        ).then(
                                                                Commands.literal("getOwner")
                                                                        .executes(BankExecutions::getOwner)
                                                        ).then(
                                                                Commands.literal("setOwner")
                                                                        .then(
                                                                                Commands.argument("player", ArgumentTypes.player())
                                                                                        .executes(BankExecutions::setOwner)
                                                                                        .then(
                                                                                                Commands.argument("confirmed", BoolArgumentType.bool())
                                                                                                        .executes(BankExecutions::setOwner)
                                                                                        )
                                                                        )
                                                        ).then(
                                                                Commands.literal("getMembers")
                                                                        .executes(BankExecutions::getMembers)
                                                        ).then(
                                                                Commands.literal("addMember")
                                                                        .then(
                                                                                Commands.argument("player", ArgumentTypes.player())
                                                                                        .executes(BankExecutions::addMember)
                                                                        )
                                                        ).then(
                                                                Commands.literal("removeMember")
                                                                        .then(
                                                                                Commands.argument("member", StringArgumentType.string())
                                                                                        .suggests(this::bankMemberSuggestion)
                                                                                        .executes(BankExecutions::removeMember)
                                                                        )
                                                        )
                                                        .then(
                                                                Commands.literal("delete")
                                                                        .executes(BankExecutions::delete)
                                                                        .then(
                                                                                Commands.argument("confirmed", BoolArgumentType.bool())
                                                                                        .executes(BankExecutions::delete)
                                                                        )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("create")
                                                        .then(
                                                                Commands.argument("bank", StringArgumentType.string())
                                                                        .executes(BankExecutions::create)
                                                                        .then(
                                                                                Commands.argument("owner", ArgumentTypes.player())
                                                                                        .executes(BankExecutions::create)
                                                                        )
                                                        )
                                        )
                        ).build(),
                "Manage player accounts and banks",
                List.of("eco")
        );
    }

    public CompletableFuture<Suggestions> bankSuggestion(CommandContext<CommandSourceStack> _context, SuggestionsBuilder builder) {
        for (String bank : EconomyManager.getAllBankNames()) {
            builder.suggest(bank);
        }
        return builder.buildFuture();
    }

    public CompletableFuture<Suggestions> bankMemberSuggestion(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String bank = StringArgumentType.getString(context, "bank");
        for (UUID uuid : EconomyManager.getBankMembers(bank)) {
            builder.suggest(Bukkit.getOfflinePlayer(uuid).getName());
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

            ctx.getSource().getSender().sendMessage(Component.text("Set %s's balance to %s".formatted(player.getName(), Main.econ.format(newBal))).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }


    }

    private static class BankExecutions {


        public static int deposit(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");
            Double amount = DoubleArgumentType.getDouble(ctx, "amount");

            double newBal = EconomyManager.depositBank(bank, amount);

            ctx.getSource().getSender().sendMessage(Component.text("Set %s's balance to %s".formatted(bank, Main.econ.format(newBal))).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }

        public static int withdraw(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");
            Double amount = DoubleArgumentType.getDouble(ctx, "amount");

            double newBal = EconomyManager.withdrawBank(bank, amount);

            ctx.getSource().getSender().sendMessage(Component.text("Set %s's balance to %s".formatted(bank, Main.econ.format(newBal))).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }

        public static int getBal(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");

            double balance = EconomyManager.getBankBal(bank);

            ctx.getSource().getSender().sendMessage(Component.text("%s's balance is %s".formatted(bank, Main.econ.format(balance))).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }

        public static int setBal(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");
            Double newBal = DoubleArgumentType.getDouble(ctx, "value");

            EconomyManager.setBankBal(bank, newBal);

            ctx.getSource().getSender().sendMessage(Component.text("Set %s's balance to %s".formatted(bank, Main.econ.format(newBal))).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }

        public static int getOwner(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");
            OfflinePlayer owner = Bukkit.getOfflinePlayer(EconomyManager.getBankOwner(bank));

            ctx.getSource().getSender().sendMessage(Component.text("%s's owner is %s".formatted(bank, owner.getName())).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }

        public static int setOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            String bank = StringArgumentType.getString(ctx, "bank");
            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

            if (!BoolArgumentType.getBool(ctx, "confirmed")) {

                ctx.getSource().getSender().sendMessage(Component.text("Are you sure you want to change the owner of %s?".formatted(bank)).color(NamedTextColor.GOLD));
                ctx.getSource().getSender().sendMessage(Component.text("Click here to confirm").color(NamedTextColor.GREEN).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, ctx.getInput() + " confirm")));

                return Command.SINGLE_SUCCESS;
            }

            EconomyManager.setBankOwner(bank, player.getUniqueId());

            ctx.getSource().getSender().sendMessage(Component.text("Set owner of %s to %s".formatted(bank, player.getName())).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }

        public static int getMembers(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");

            Set<OfflinePlayer> members = new HashSet<>();
            for (UUID uuid : EconomyManager.getBankMembers(bank)) {
                members.add(Bukkit.getOfflinePlayer(uuid));
            }
            Component message = Component.text("%s's members".formatted(bank));
            for (Iterator<OfflinePlayer> it = members.iterator(); it.hasNext();) {

                OfflinePlayer offlinePlayer = it.next();

                if (offlinePlayer.getPlayer() != null) {
                    message = message.append(Component.text(offlinePlayer.getName()).hoverEvent(offlinePlayer.getPlayer().asHoverEvent()));
                } else {
                    message = message.append(Component.text(offlinePlayer.getName()));
                }

                if (it.hasNext()) {
                    message = message.append(Component.text(", "));
                }

            }

            return Command.SINGLE_SUCCESS;
        }

        public static int addMember(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            String bank = StringArgumentType.getString(ctx, "bank");
            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

            EconomyManager.addBankMember(bank, player.getUniqueId());

            ctx.getSource().getSender().sendMessage(Component.text("Added %s to %s".formatted(player.getName(), bank)).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }

        public static int removeMember(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            String bank = StringArgumentType.getString(ctx, "bank");
            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

            EconomyManager.removeBankMember(bank, player.getUniqueId());

            ctx.getSource().getSender().sendMessage(Component.text("Removed %s from %s".formatted(player.getName(), bank)).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }

        public static int delete(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");

            if (!BoolArgumentType.getBool(ctx, "confirmed")) {

                ctx.getSource().getSender().sendMessage(Component.text("Are you sure you want to delete %s? All the money in it will be deleted.".formatted(bank)).color(NamedTextColor.GOLD));
                ctx.getSource().getSender().sendMessage(Component.text("Click here to confirm").color(NamedTextColor.GREEN).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, ctx.getInput() + " confirm")));

                return Command.SINGLE_SUCCESS;
            }

            EconomyManager.deleteBank(bank);
            ctx.getSource().getSender().sendMessage(Component.text("Deleted %s".formatted(bank)).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }

        public static int create(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            String bank = StringArgumentType.getString(ctx, "name");
            Player owner = ctx.getArgument("owner", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

            if (EconomyManager.hasBank(bank)) {
                ctx.getSource().getSender().sendMessage(Component.text("A bank called %s already exists.".formatted(bank)).color(NamedTextColor.DARK_RED));
            }

            if (owner.isEmpty()) {
                if (ctx.getSource().getSender() instanceof Player) {
                    owner = (Player) ctx.getSource().getSender();
                    EconomyManager.createBank(bank, ((Player) ctx.getSource().getSender()).getUniqueId());
                } else {
                    ctx.getSource().getSender().sendMessage(Component.text("Cannot create a bank account without an owner. Please specify an owner").color(NamedTextColor.DARK_RED));
                    return Command.SINGLE_SUCCESS;
                }
            } else {
                EconomyManager.createBank(bank, owner.getUniqueId());
            }

            ctx.getSource().getSender().sendMessage(Component.text("Created %s with owner %s".formatted(bank, owner.getName())).color(NamedTextColor.GOLD));

            return Command.SINGLE_SUCCESS;
        }
    }

}

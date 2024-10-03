package com.arcunis.vaultprovider.commands;

import com.arcunis.vaultprovider.Main;
import com.arcunis.vaultprovider.EconomyManager;
import com.arcunis.vaultprovider.utils.Formatter;
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
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EconomyAdminCommand {

    public EconomyAdminCommand(Commands commands) {
        commands.register(
                // Register economy command
                Commands.literal("economy")
                        .requires(source -> source.getSender().hasPermission("vaultprovider.economy.manage"))
                        .then(
                                // Add account subcommand
                                Commands.literal("account")
                                        .then(
                                                // Player argument for the account
                                                Commands.argument("account", ArgumentTypes.player())
                                                        .then(
                                                                // Deposit money into the account
                                                                Commands.literal("deposit")
                                                                        .then(
                                                                                Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                                                                        .executes(PlayerExecutions::deposit)
                                                                        )
                                                        ).then(
                                                                // Withdraw money from the account
                                                                Commands.literal("withdraw")
                                                                        .then(
                                                                                Commands.argument("amount",  DoubleArgumentType.doubleArg(0))
                                                                                        .executes(PlayerExecutions::withdraw)
                                                                        )
                                                        ).then(
                                                                // Get account balance
                                                                Commands.literal("getBal")
                                                                        .executes(PlayerExecutions::getBal)
                                                        ).then(
                                                                // Set account balance
                                                                Commands.literal("setBal")
                                                                        .then(
                                                                                Commands.argument("value",  DoubleArgumentType.doubleArg(0))
                                                                                        .executes(PlayerExecutions::setBal)
                                                                        )
                                                        )
                                        )
                        )
                        .then(
                                // Add bank subcommand
                                Commands.literal("bank")
                                        .then(
                                                // Bank name argument
                                                Commands.argument("bank", StringArgumentType.string())
                                                        .suggests(this::bankSuggestion)
                                                        .then(
                                                                // Deposit money into the bank
                                                                Commands.literal("deposit")
                                                                        .then(
                                                                                Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                                                                        .executes(BankExecutions::deposit)
                                                                        )
                                                        ).then(
                                                                // Withdraw money from the bank
                                                                Commands.literal("withdraw")
                                                                        .then(
                                                                                Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                                                                        .executes(BankExecutions::withdraw)
                                                                        )
                                                        ).then(
                                                                // Get bank balance
                                                                Commands.literal("getBal")
                                                                        .executes(BankExecutions::getBal)
                                                        ).then(
                                                                // Set bank balance
                                                                Commands.literal("setBal")
                                                                        .then(
                                                                                Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                                                        .executes(BankExecutions::setBal)
                                                                        )
                                                        ).then(
                                                                // Get bank owner
                                                                Commands.literal("getOwner")
                                                                        .executes(BankExecutions::getOwner)
                                                        ).then(
                                                                // Set bank owner
                                                                Commands.literal("setOwner")
                                                                        .then(
                                                                                // New owner
                                                                                Commands.argument("player", ArgumentTypes.player())
                                                                                        .executes(BankExecutions::setOwner)
                                                                                        .then(
                                                                                                // Confirm argument to make sure admins dont change an owner on accident
                                                                                                Commands.argument("confirmed", BoolArgumentType.bool())
                                                                                                        .executes(BankExecutions::setOwnerConfirmed)
                                                                                        )
                                                                        )
                                                        ).then(
                                                                // Get bank members
                                                                Commands.literal("getMembers")
                                                                        .executes(BankExecutions::getMembers)
                                                        ).then(
                                                                // Add member
                                                                Commands.literal("addMember")
                                                                        .then(
                                                                                Commands.argument("player", ArgumentTypes.player())
                                                                                        .executes(BankExecutions::addMember)
                                                                        )
                                                        ).then(
                                                                // Remove member
                                                                Commands.literal("removeMember")
                                                                        .then(
                                                                                Commands.argument("member", StringArgumentType.string())
                                                                                        .suggests(this::bankMemberSuggestion)
                                                                                        .executes(BankExecutions::removeMember)
                                                                        )
                                                        )
                                                        .then(
                                                                // Delete bank
                                                                Commands.literal("delete")
                                                                        .executes(BankExecutions::delete)
                                                                        .then(
                                                                                // Confirm argument to make sure admins dont delete the bank on accident
                                                                                Commands.argument("confirmed", BoolArgumentType.bool())
                                                                                        .executes(BankExecutions::deleteConfirmed)
                                                                        )
                                                        )
                                        )
                        )
                        .then(
                                // Create subcommand to create banks
                                Commands.literal("createBank")
                                        .then(
                                                // Bankname
                                                Commands.argument("bank", StringArgumentType.string())
                                                        .executes(BankExecutions::createSelf)
                                                        .then(
                                                                // Owner
                                                                Commands.argument("owner", ArgumentTypes.player())
                                                                        .executes(BankExecutions::createOther)
                                                        )
                                        )
                        )
                        .build(),
                "Manage player accounts and banks",
                List.of("eco")
        );
    }

    // Suggest all banks
    public CompletableFuture<Suggestions> bankSuggestion(CommandContext<CommandSourceStack> _context, SuggestionsBuilder builder) {
        for (String bank : EconomyManager.getAllBankNames()) {
            builder.suggest(bank);
        }
        return builder.buildFuture();
    }

    // Suggest all bank members from a specified bank
    public CompletableFuture<Suggestions> bankMemberSuggestion(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String bank = StringArgumentType.getString(context, "bank");
        for (UUID uuid : EconomyManager.getBankMembers(bank)) {
            builder.suggest(Bukkit.getOfflinePlayer(uuid).getName());
        }
        return builder.buildFuture();
    }

    private static class PlayerExecutions {

        public static int deposit(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            Player player = ctx.getArgument("account", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            double amount = DoubleArgumentType.getDouble(ctx, "amount");
            double newBal = EconomyManager.depositAcc(player.getUniqueId(), amount);

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("amount", amount);
            valuesMap.put("player", player.getName());
            valuesMap.put("player_balance", newBal);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("player-deposit"), valuesMap)
                ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int withdraw(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            Player player = ctx.getArgument("account", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            double amount = DoubleArgumentType.getDouble(ctx, "amount");
            double newBal = EconomyManager.withdrawAcc(player.getUniqueId(), amount);

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("amount", amount);
            valuesMap.put("player", player.getName());
            valuesMap.put("player_balance", newBal);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("player-withdraw"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int getBal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            Player player = ctx.getArgument("account", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            double balance = EconomyManager.getAccBal(player.getUniqueId());

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("player", player.getName());
            valuesMap.put("player_balance", balance);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("player-balance"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }
        public static int setBal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            Player player = ctx.getArgument("account", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            double newBal = DoubleArgumentType.getDouble(ctx, "value");
            EconomyManager.setAccBal(player.getUniqueId(), newBal);

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("player", player.getName());
            valuesMap.put("player_balance", newBal);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("player-set-balance"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }


    }

    private static class BankExecutions {


        public static int deposit(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");
            Double amount = DoubleArgumentType.getDouble(ctx, "amount");

            double newBal = EconomyManager.depositBank(bank, amount);

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("amount", amount);
            valuesMap.put("bank", bank);
            valuesMap.put("bank_balance", newBal);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-deposit"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int withdraw(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");
            Double amount = DoubleArgumentType.getDouble(ctx, "amount");

            double newBal = EconomyManager.withdrawBank(bank, amount);

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("amount", amount);
            valuesMap.put("bank", bank);
            valuesMap.put("bank_balance", newBal);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-withdraw"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int getBal(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");

            double balance = EconomyManager.getBankBal(bank);

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("bank_balance", balance);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-balance"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int setBal(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");
            Double newBal = DoubleArgumentType.getDouble(ctx, "value");

            EconomyManager.setBankBal(bank, newBal);

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("bank_balance", newBal);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-set-balance"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int getOwner(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");
            OfflinePlayer owner = Bukkit.getOfflinePlayer(EconomyManager.getBankOwner(bank));

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("owner", owner.getName());

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-owner"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int setOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            String bank = StringArgumentType.getString(ctx, "bank");
            OfflinePlayer owner = Bukkit.getOfflinePlayer(EconomyManager.getBankOwner(bank));
            Player newOwner = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("old_owner", owner.getName());
            valuesMap.put("new_owner", newOwner.getName());

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-owner-conformation"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );
            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("confirm-action"), valuesMap)
                    ).color(NamedTextColor.GREEN)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int setOwnerConfirmed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            String bank = StringArgumentType.getString(ctx, "bank");
            OfflinePlayer owner = Bukkit.getOfflinePlayer(EconomyManager.getBankOwner(bank));
            Player newOwner = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("old_owner", owner.getName());
            valuesMap.put("new_owner", newOwner.getName());

            EconomyManager.setBankOwner(bank, newOwner.getUniqueId());

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-set-owner"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int getMembers(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");

            Set<String> members = new HashSet<>();
            for (UUID uuid : EconomyManager.getBankMembers(bank)) {
                members.add(Bukkit.getOfflinePlayer(uuid).getName());
            }

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("members", String.join(", ", members));

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(
                                    Main.getMessage("bank-members"),
                                    valuesMap
                            )
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int addMember(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            String bank = StringArgumentType.getString(ctx, "bank");
            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("player", player.getName());

            if (EconomyManager.getBankMembers(bank).contains(player.getUniqueId())) {
                ctx.getSource().getSender().sendMessage(
                        Component.text(
                                Formatter.formatString(Main.getMessage("member-already-exists"), valuesMap)
                        ).color(NamedTextColor.DARK_RED)
                );
            }

            EconomyManager.addBankMember(bank, player.getUniqueId());

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-add-member"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int removeMember(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            String bank = StringArgumentType.getString(ctx, "bank");
            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

            EconomyManager.removeBankMember(bank, player.getUniqueId());

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("player", player.getName());

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-remove-member"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int delete(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-delete-confirmation"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );
            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("confirm-action"), valuesMap)
                    ).color(NamedTextColor.GREEN)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int deleteConfirmed(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);

            EconomyManager.deleteBank(bank);
            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-deleted"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int createOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

            String bank = StringArgumentType.getString(ctx, "bank");
            Player owner = ctx.getArgument("owner", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("owner", owner.getName());

            if (EconomyManager.hasBank(bank)) {
                ctx.getSource().getSender().sendMessage(
                        Component.text(
                                Formatter.formatString(Main.getMessage("bank-already-exists"), valuesMap)
                        ).color(NamedTextColor.DARK_RED)
                );
            }

            EconomyManager.createBank(bank, owner.getUniqueId());

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-create"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }

        public static int createSelf(CommandContext<CommandSourceStack> ctx) {

            String bank = StringArgumentType.getString(ctx, "bank");
            Player owner = (Player) ctx.getSource().getSender();

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("bank", bank);
            valuesMap.put("owner", owner.getName());

            if (EconomyManager.hasBank(bank)) {
                ctx.getSource().getSender().sendMessage(
                        Component.text(
                                Formatter.formatString(Main.getMessage("bank-already-exists"), valuesMap)
                        ).color(NamedTextColor.DARK_RED)
                );
            }

            EconomyManager.createBank(bank, owner.getUniqueId());

            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("bank-create"), valuesMap)
                    ).color(NamedTextColor.GOLD)
            );

            return Command.SINGLE_SUCCESS;
        }
    }

}

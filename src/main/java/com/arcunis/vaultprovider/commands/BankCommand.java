package com.arcunis.vaultprovider.commands;

import com.arcunis.vaultprovider.EconomyManager;
import com.arcunis.vaultprovider.Main;
import com.arcunis.vaultprovider.utils.Formatter;
import com.mojang.brigadier.Command;
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

public class BankCommand {

    private static final Map<String, List<UUID>> invites = new HashMap<>();

    public BankCommand(Commands commands) {
        commands.register(
                Commands.literal("bank")
                        .requires(sourceStack ->
                                sourceStack.getSender() instanceof Player && sourceStack.getSender().hasPermission("vaultprovider.bank")
                        )
                        .then(
                                Commands.argument("bank", StringArgumentType.string())
                                        .suggests(this::bankSuggestion)
                                        .then(
                                                Commands.literal("balance")
                                                        .requires(sourceStack ->
                                                            sourceStack.getSender().hasPermission("vaultprovider.balance.bank")
                                                        )
                                                        .executes(this::balance)
                                        )
                                        .then(
                                                Commands.literal("getMembers")
                                                        .requires(sourceStack ->
                                                                sourceStack.getSender().hasPermission("vaultprovider.bank.members.get")
                                                        )
                                                        .executes(this::getMembers)
                                        )
                                        .then(
                                                Commands.literal("invite")
                                                        .requires(sourceStack ->
                                                                sourceStack.getSender().hasPermission("vaultprovider.bank.members.invite")
                                                        )
                                                        .then(
                                                                Commands.argument("player", ArgumentTypes.player())
                                                                        .executes(this::inviteMember)
                                                        )
                                        )
                                        .then(
                                                Commands.literal("revokeInvite")
                                                        .requires(sourceStack ->
                                                                sourceStack.getSender().hasPermission("vaultprovider.bank.members.invite")
                                                        )
                                                        .then(
                                                            Commands.argument("player", StringArgumentType.string())
                                                                .suggests(this::invitationSuggestion)
                                                                .executes(this::revokeInvite)
                                                        )

                                        )
                                        .then(
                                                Commands.literal("removeMember")
                                                        .requires(sourceStack ->
                                                                sourceStack.getSender().hasPermission("vaultprovider.bank.members.remove")
                                                        )
                                                        .then(
                                                                Commands.argument("member", StringArgumentType.string())
                                                                        .executes(this::removeMember)
                                                        )
                                        )
                                        .then(
                                                Commands.literal("acceptInvite")
                                                        .executes(this::acceptInvite)
                                        )
                        )
                        .build(),
                "Manage your banks"
        );
    }

    public CompletableFuture<Suggestions> bankSuggestion(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Player player = (Player) ctx.getSource().getSender();

        for (String bank : EconomyManager.getAllBankNames()) {
            List<UUID> bankInvites = invites.get(bank);
            if (!EconomyManager.getBankOwner(bank).equals(player.getUniqueId()) && (bankInvites == null || !bankInvites.contains(player.getUniqueId()))) continue;
            builder.suggest(bank);
        }
        return builder.buildFuture();
    }

    public CompletableFuture<Suggestions> bankMemberSuggestion(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String bank = StringArgumentType.getString(ctx, "bank");
        for (UUID uuid : EconomyManager.getBankMembers(bank)) {
            builder.suggest(Bukkit.getOfflinePlayer(uuid).getName());
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> invitationSuggestion(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String bank = StringArgumentType.getString(ctx, "bank");
        for (UUID uuid : invites.get(bank)) {
            builder.suggest(Bukkit.getOfflinePlayer(uuid).getName());
        }
        return builder.buildFuture();
    }

    private int balance(CommandContext<CommandSourceStack> ctx) {

        String bank = StringArgumentType.getString(ctx, "bank");

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("bank", bank);
        valuesMap.put("bank_balance", EconomyManager.getBankBal(bank));

        ctx.getSource().getSender().sendMessage(
                Component.text(
                        Formatter.formatString(Main.getMessage("bank-balance"), valuesMap)
                ).color(NamedTextColor.GOLD)
        );

        return Command.SINGLE_SUCCESS;
    }

    private int removeMember(CommandContext<CommandSourceStack> ctx) {

        String bank = StringArgumentType.getString(ctx, "bank");
        OfflinePlayer player = Bukkit.getOfflinePlayer(StringArgumentType.getString(ctx, "member"));

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("bank", bank);
        valuesMap.put("player", player.getName());

        Player executor = (Player) ctx.getSource().getSender();
        if (!EconomyManager.getBankOwner(bank).equals(executor.getUniqueId())) {
            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("not-owner-of-bank"), valuesMap)
                    ).color(NamedTextColor.DARK_RED)
            );
            return Command.SINGLE_SUCCESS;
        }

        ctx.getSource().getSender().sendMessage(
                Component.text(
                        Formatter.formatString(Main.getMessage("bank-remove-member"), valuesMap)
                ).color(NamedTextColor.GOLD)
        );

        return Command.SINGLE_SUCCESS;
    }

    private int getMembers(CommandContext<CommandSourceStack> ctx) {

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

    private int inviteMember(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

        String bank = StringArgumentType.getString(ctx, "bank");
        Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("bank", bank);
        valuesMap.put("player", player.getName());

        Player executor = (Player) ctx.getSource().getSender();
        if (!EconomyManager.getBankOwner(bank).equals(executor.getUniqueId())) {
            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("not-owner-of-bank"), valuesMap)
                    ).color(NamedTextColor.DARK_RED)
            );

            return Command.SINGLE_SUCCESS;
        }

        if (EconomyManager.getBankMembers(bank).contains(player.getUniqueId())) {
            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("member-already-exists"), valuesMap)
                    ).color(NamedTextColor.DARK_RED)
            );

            return Command.SINGLE_SUCCESS;
        }

        if (invites.get(bank).contains(player.getUniqueId())) {
            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("invitation-already-exists"), valuesMap)
                    ).color(NamedTextColor.DARK_RED)
            );

            return Command.SINGLE_SUCCESS;
        }

        if (!invites.containsKey(bank)) invites.put(bank, new ArrayList<>());
        invites.get(bank).add(player.getUniqueId());

        ctx.getSource().getSender().sendMessage(
                Component.text(
                        Formatter.formatString(
                                Main.getMessage("bank-invite-sent"),
                                valuesMap
                        )
                ).color(NamedTextColor.GOLD)
        );

        player.sendMessage(
                Component.text(
                        Formatter.formatString(
                                Main.getMessage("bank-invitation"),
                                valuesMap
                        )
                ).color(NamedTextColor.GOLD)
        );

        player.sendMessage(
                Component.text(
                        Formatter.formatString(
                                Main.getMessage("accept-action"),
                                new HashMap<>()
                        )
                ).clickEvent(
                        ClickEvent.runCommand("bank %s acceptInvite".formatted(bank))
                ).color(NamedTextColor.GREEN)
        );

        return Command.SINGLE_SUCCESS;
    }

    private int revokeInvite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

        String bank = StringArgumentType.getString(ctx, "bank");
        String playerName = StringArgumentType.getString(ctx, "player");
        Player executor = (Player) ctx.getSource().getSender();

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("bank", bank);
        valuesMap.put("player", executor.getName());

        if (!EconomyManager.getBankOwner(bank).equals(executor.getUniqueId())) {
            ctx.getSource().getSender().sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("not-owner-of-bank"), valuesMap)
                    ).color(NamedTextColor.DARK_RED)
            );
            return Command.SINGLE_SUCCESS;
        }

        invites.get(bank).remove(Bukkit.getPlayerUniqueId(playerName));

        ctx.getSource().getSender().sendMessage(
                Component.text(
                        Formatter.formatString(
                                Main.getMessage("revoked-invitation"),
                                valuesMap
                        )
                ).color(NamedTextColor.GOLD)
        );

        return Command.SINGLE_SUCCESS;
    }

    private int acceptInvite(CommandContext<CommandSourceStack> ctx) {

        String bank = StringArgumentType.getString(ctx, "bank");
        Player player = (Player) ctx.getSource().getSender();

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("bank", bank);
        valuesMap.put("player", player.getName());
        valuesMap.put("owner", Bukkit.getOfflinePlayer(EconomyManager.getBankOwner(bank)).getName());

        if (!invites.get(bank).contains(player.getUniqueId())) {
            player.sendMessage(
                    Component.text(
                            Formatter.formatString(Main.getMessage("accept-invalid-invite"), valuesMap)
                    ).color(NamedTextColor.DARK_RED)
            );

            return Command.SINGLE_SUCCESS;
        }

        EconomyManager.addBankMember(bank, player.getUniqueId());

        player.sendMessage(
                Component.text(
                        Formatter.formatString(
                                Main.getMessage("accept-invite"),
                                valuesMap
                        )
                ).color(NamedTextColor.GOLD)
        );

        return Command.SINGLE_SUCCESS;
    }

}

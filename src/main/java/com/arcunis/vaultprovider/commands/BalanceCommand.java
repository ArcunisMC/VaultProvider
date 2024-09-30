package com.arcunis.vaultprovider.commands;

import com.arcunis.vaultprovider.Main;
import com.arcunis.vaultprovider.EconomyManager;
import com.arcunis.vaultprovider.utils.Formatter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceCommand {

    public BalanceCommand(Commands commands) {
        commands.register(
                Commands.literal("balance")
                        .requires(sourceStack -> sourceStack.getSender() instanceof Player)
                        .executes(this::executeSelf)
                        .then(
                                Commands.argument("player", ArgumentTypes.player())
                                        .executes(this::executeOther)
                        )
                        .build(),
                "Get your or someone else's balance",
                List.of("bal")
        );
    }

    private int executeSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

        Player player = (Player) ctx.getSource().getSender();

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("player", player.getName());
        valuesMap.put("balance", Main.econ.format(EconomyManager.getAccBal(player.getUniqueId())));


        player.sendMessage(
                Component.text(
                        Formatter.format(Main.getMessage("balance"), valuesMap)
                ).color(NamedTextColor.GOLD)
        );

        return Command.SINGLE_SUCCESS;
    }

    private int executeOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

        Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("player", player.getName());
        valuesMap.put("balance", Main.econ.format(EconomyManager.getAccBal(player.getUniqueId())));


        player.sendMessage(
                Component.text(
                        Formatter.format(Main.getMessage("balance"), valuesMap)
                ).color(NamedTextColor.GOLD)
        );

        return Command.SINGLE_SUCCESS;
    }

}

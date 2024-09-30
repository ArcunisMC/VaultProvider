package com.arcunis.vaultprovider.economy.commands;

import com.arcunis.vaultprovider.Main;
import com.arcunis.vaultprovider.economy.EconomyManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceCommand {

    public BalanceCommand(Commands commands) {
        commands.register(
                Commands.literal("balance")
                        .requires(sourceStack -> sourceStack.getSender() instanceof Player)
                        .executes(this::execute)
                        .then(
                                Commands.argument("player", ArgumentTypes.player())
                                        .executes(this::execute)
                        )
                        .build(),
                "Get your or someone else's balance",
                List.of("bal")
        );
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

        Player executor = (Player) ctx.getSource().getSender();
        Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
        if (player == null) player = executor;

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("player", player.getName());
        valuesMap.put("balance", Main.econ.format(EconomyManager.getAccBal(executor.getUniqueId())));

        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        executor.sendMessage(
                Component.text(
                        sub.replace(Main.getMessage("balance"))
                ).color(NamedTextColor.GOLD)
        );

        return Command.SINGLE_SUCCESS;
    }

}

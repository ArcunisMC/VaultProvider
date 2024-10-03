package com.arcunis.vaultprovider.commands;

import com.arcunis.vaultprovider.Main;
import com.arcunis.vaultprovider.EconomyManager;
import com.arcunis.vaultprovider.utils.Formatter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
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
import java.util.Map;

public class PayCommand {

    public PayCommand(Commands commands) {
        commands.register(
                Commands.literal("pay")
                        .requires(sourceStack -> sourceStack.getSender() instanceof Player)
                        .then(
                                Commands.argument("player", ArgumentTypes.player())
                                        .then(
                                                Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                                        .executes(this::execute)
                                        )
                        )
                        .build()
        );
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

        Player executor = (Player) ctx.getSource().getSender();
        Player receiver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
        double amount = DoubleArgumentType.getDouble(ctx, "amount");

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("sender", executor.getName());
        valuesMap.put("receiver", receiver.getName());
        valuesMap.put("amount", Main.econ.format(amount));
        valuesMap.put("player", executor.getName());

        if (EconomyManager.getAccBal(executor.getUniqueId()) < amount) {
            executor.sendMessage(
                    Component.text(
                        Formatter.formatString(Main.getMessage("insufficient-funds-player"), valuesMap)
                    ).color(NamedTextColor.DARK_RED)
            );
            return Command.SINGLE_SUCCESS;
        }

        EconomyManager.withdrawAcc(executor.getUniqueId(), amount);
        EconomyManager.depositAcc(receiver.getUniqueId(), amount);

        executor.sendMessage(
                Component.text(
                        Formatter.formatString(Main.getMessage("player-sent"), valuesMap)
                ).color(NamedTextColor.GOLD)
        );
        receiver.sendMessage(
                Component.text(
                        Formatter.formatString(Main.getMessage("player-received"), valuesMap)
                ).color(NamedTextColor.GOLD)
        );

        return Command.SINGLE_SUCCESS;
    }

}

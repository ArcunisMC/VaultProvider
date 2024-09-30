package com.arcunis.vaultprovider.economy.commands;

import com.arcunis.vaultprovider.Main;
import com.arcunis.vaultprovider.economy.EconomyManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WithdrawCommand {

    public WithdrawCommand(Commands commands) {
        commands.register(
                Commands.literal("withdraw")
                        .requires(sourceStack -> sourceStack.getSender() instanceof Player)
                        .then(
                                Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .then(
                                                Commands.argument("bank", StringArgumentType.string())
                                                        .suggests(this::bankSuggestion)
                                                        .executes(this::execute)
                                        )
                        )
                        .build()
        );
    }

    public CompletableFuture<Suggestions> bankSuggestion(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (String bank : EconomyManager.getAllBankNames()) {
            if (!EconomyManager.getBankMembers(bank).contains(((Player) ctx.getSource().getSender()).getUniqueId())) continue;
            builder.suggest(bank);
        }
        return builder.buildFuture();
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {

        Player executor = (Player) ctx.getSource().getSender();
        String bank = StringArgumentType.getString(ctx, "bank");
        double amount = DoubleArgumentType.getDouble(ctx, "amount");

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("bank", bank);
        valuesMap.put("bank-balance", Main.econ.format(EconomyManager.getBankBal(bank)));
        valuesMap.put("amount", Main.econ.format(amount));

        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        if (!EconomyManager.getBankMembers(bank).contains(executor.getUniqueId())) {
            executor.sendMessage(
                    Component.text(
                            sub.replace(Main.getMessage("not-member-of-bank"))
                    ).color(NamedTextColor.DARK_RED)
            );
            return Command.SINGLE_SUCCESS;
        }

        if (EconomyManager.getBankBal(bank) < amount) {
            executor.sendMessage(
                    Component.text(
                            sub.replace(Main.getMessage("insufficient-funds-bank"))
                    ).color(NamedTextColor.DARK_RED)
            );
            return Command.SINGLE_SUCCESS;
        }

        EconomyManager.withdrawBank(bank, amount);
        EconomyManager.depositAcc(executor.getUniqueId(), amount);

        executor.sendMessage(
                Component.text(
                        sub.replace(Main.getMessage("withdrawn"))
                ).color(NamedTextColor.GOLD)
        );

        return Command.SINGLE_SUCCESS;
    }

}

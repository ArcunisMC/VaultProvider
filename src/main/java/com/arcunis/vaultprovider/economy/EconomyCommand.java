package com.arcunis.vaultprovider.economy;

import com.arcunis.vaultprovider.Main;
import com.arcunis.vaultprovider.utils.Command;
import com.arcunis.vaultprovider.utils.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EconomyCommand extends Command {

    public EconomyCommand(@NotNull JavaPlugin plugin) {
        super(plugin, "economy-old", "Manage player accounts and banks", "/eco [acc/bank] <name> [deposit/withdraw/getBal/setBal/...] ...", List.of("eco-old"));
        setPermission(new Permission("vaultprovider.manage", PermissionDefault.OP).name);
        register();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, String label, @NotNull String[] args) {

        // =====================================================
        // WARNING!
        // EXTREME SPAGHETTI CODE AHEAD
        // =====================================================

        if (args.length < 2) {
            sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
            return;
        }

        if (args[0].startsWith("acc")) {

            UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            double bal = EconomyManager.getAccBal(uuid);

            if (args[2].equalsIgnoreCase("getBal")) {
                sender.sendMessage(Component.text("%s's Balance: %s".formatted(playerName, Main.econ.format(bal))).color(NamedTextColor.GOLD));
                return;
            }

            if (args[2].equalsIgnoreCase("setBal")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

                try {
                    double newBal = Double.parseDouble(args[3]);
                    if (newBal > bal) EconomyManager.depositAcc(uuid, newBal - bal);
                    else EconomyManager.withdrawAcc(uuid, bal - newBal);
                    sender.sendMessage(Component.text("%s's new Balance: %s".formatted(playerName, Main.econ.format(EconomyManager.getAccBal(uuid)))).color(NamedTextColor.GOLD));
                    return;
                } catch (NumberFormatException exception) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

            }

            if (args[2].equalsIgnoreCase("deposit")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

                try {
                    double value = Double.parseDouble(args[3]);
                    EconomyManager.depositAcc(uuid, value);
                    sender.sendMessage(Component.text("%s's new Balance: %s".formatted(playerName, Main.econ.format(EconomyManager.getAccBal(uuid)))).color(NamedTextColor.GOLD));
                    return;
                } catch (NumberFormatException exception) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

            }

            if (args[2].equalsIgnoreCase("withdraw")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

                try {
                    double value = Double.parseDouble(args[3]);
                    EconomyManager.withdrawAcc(uuid, value);
                    sender.sendMessage(Component.text("%s's new Balance: %s".formatted(playerName, Main.econ.format(EconomyManager.getAccBal(uuid)))).color(NamedTextColor.GOLD));
                } catch (NumberFormatException exception) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                }

            }

        } else if (args[0].equalsIgnoreCase("bank")) {

            String bank = args[1];
            UUID ownerUUID = EconomyManager.getBankOwner(bank);
            String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
            double bal = EconomyManager.getBankBal(bank);

            if (args[2].equalsIgnoreCase("getBal")) {
                sender.sendMessage(Component.text("%s's Balance: %s".formatted(bank, Main.econ.format(bal))).color(NamedTextColor.GOLD));

            } else if (args[2].equalsIgnoreCase("setBal")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

                try {
                    double newBal = Double.parseDouble(args[3]);
                    if (newBal > bal) EconomyManager.depositBank(bank, newBal - bal);
                    else EconomyManager.withdrawBank(bank, bal - newBal);
                    sender.sendMessage(Component.text("%s's new Balance: %s".formatted(bank, Main.econ.format(EconomyManager.getBankBal(bank)))).color(NamedTextColor.GOLD));
                } catch (NumberFormatException exception) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                }

            } else if (args[2].equalsIgnoreCase("deposit")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

                try {
                    double value = Double.parseDouble(args[3]);
                    EconomyManager.depositBank(bank, value);
                    sender.sendMessage(Component.text("%s's new Balance: %s".formatted(bank, Main.econ.format(EconomyManager.getBankBal(bank)))).color(NamedTextColor.GOLD));
                } catch (NumberFormatException exception) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                }

            } else if (args[2].equalsIgnoreCase("withdraw")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

                try {
                    double value = Double.parseDouble(args[3]);
                    EconomyManager.withdrawBank(bank, value);
                    sender.sendMessage(Component.text("%s's new Balance: %s".formatted(bank, Main.econ.format(EconomyManager.getBankBal(bank)))).color(NamedTextColor.GOLD));
                } catch (NumberFormatException exception) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                }

            } else if (args[2].equalsIgnoreCase("getMembers")) {
                Component message = Component.text("%s's members:".formatted(bank)).color(NamedTextColor.GOLD);
                message = message.appendNewline();

                List<UUID> members = new ArrayList<>(EconomyManager.getBankMembers(bank));

                int i = 0;
                while (i < members.size()) {
                    i ++;
                    UUID uuid = members.get(i);
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    if (name == null) continue;
                    message = message.append(Component.text(name));
                    if (i < members.size() - 1) message = message.append(Component.text(", "));
                }

                sender.sendMessage(message);

            } else if (args[2].equalsIgnoreCase("addMember")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

                OfflinePlayer member = Bukkit.getOfflinePlayer(args[3]);
                EconomyManager.addBankMember(bank, member.getUniqueId());
                sender.sendMessage(Component.text("Added %s to %s's members".formatted(member.getName(), bank)).color(NamedTextColor.GOLD));

            } else if (args[2].equalsIgnoreCase("removeMember")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

                OfflinePlayer member = Bukkit.getOfflinePlayer(args[3]);
                EconomyManager.removeBankMember(bank, member.getUniqueId());
                sender.sendMessage(Component.text("Removed %s from %s's members".formatted(member.getName(), bank)).color(NamedTextColor.GOLD));

            } else if (args[2].equalsIgnoreCase("getOwner")) {
                sender.sendMessage(Component.text("%s's owner is %s".formatted(bank, ownerName)).color(NamedTextColor.GOLD));

            } else if (args[2].equalsIgnoreCase("setOwner")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid usage.").color(NamedTextColor.DARK_RED));
                    return;
                }

                OfflinePlayer newOwner = Bukkit.getOfflinePlayer(args[3]);
                EconomyManager.setBankOwner(bank, newOwner.getUniqueId());
                sender.sendMessage(Component.text("%s's new owner is %s".formatted(bank, ownerName)).color(NamedTextColor.GOLD));
                if (newOwner.isOnline()) {
                    ((Player) newOwner).sendMessage(Component.text("You have been made bank owner of %s".formatted(bank)).color(NamedTextColor.GOLD));
                }
            }

        } else if (args[0].equalsIgnoreCase("createBank")) {
            String name = args[1];
            UUID owner;
            if (args.length < 3) {
                if (sender instanceof Player) {
                    owner = ((Player) sender).getUniqueId();
                } else {
                    sender.sendMessage(Component.text("Please provide an owner. Cannot auto assign owner when running as console").color(NamedTextColor.DARK_RED));
                    return;
                }
            } else {
                owner = Bukkit.getPlayerUniqueId(args[2]);
                if (owner == null) {
                    sender.sendMessage(Component.text("Could not get player %s".formatted(Bukkit.getOfflinePlayer(args[2]).getName())).color(NamedTextColor.DARK_RED));
                    return;
                }
            }

            try {
                EconomyManager.createBank(name, owner);
                sender.sendMessage(Component.text("Created bank %s".formatted(name)).color(NamedTextColor.GOLD));
                OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
                if (player.isOnline()) {
                    ((Player) player).sendMessage(Component.text("You have been made bank owner of %s".formatted(name)).color(NamedTextColor.GOLD));
                }
            } catch (RuntimeException e) {
                sender.sendMessage(Component.text("A bank with that name already exists").color(NamedTextColor.DARK_RED));
            }
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of(
                    "account", "acc",
                    "bank",
                    "createBank"
            ));
        } else if (args.length == 2) {
            if (args[0].startsWith("acc")) {
                options.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
            } else if (args[0].equalsIgnoreCase("bank")) {
                options.addAll(EconomyManager.getAllBankNames());
            } else if (args[0].equalsIgnoreCase("createBank")) {
                options.add("<name>");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("createBank")) {
                options.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
            } else {
                options.addAll(List.of(
                        "deposit",
                        "withdraw",
                        "getBal",
                        "setBal"
                ));
                if (args[0].equalsIgnoreCase("bank")) {
                    options.addAll(List.of(
                            "getMembers",
                            "addMember",
                            "removeMember",
                            "getOwner",
                            "setOwner"
                    ));
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("bank")) {
                if (args[2].equalsIgnoreCase("addMember") || args[2].equalsIgnoreCase("setOwner") || args[2].equalsIgnoreCase("create")) {
                    options.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
                } else if (args[2].equalsIgnoreCase("removeMember")) {
                    for (UUID uuid : EconomyManager.getBankMembers(args[1])) {
                        options.add(Bukkit.getOfflinePlayer(uuid).getName());
                    }
                }
            }
        }
        return options.stream().filter(s -> s.startsWith(args[args.length - 1])).toList();
    }

}

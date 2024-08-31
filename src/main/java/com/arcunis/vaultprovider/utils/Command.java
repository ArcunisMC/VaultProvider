package com.arcunis.vaultprovider.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Command extends org.bukkit.command.Command {

    protected JavaPlugin plugin;
    private boolean playerOnly = false;
    private final Map<Integer, Class<?>> argumentTypes = new HashMap<>();

    /**
     * Constructor to create a command with a specified name.
     *
     * @param plugin The plugin instance.
     * @param name The name of the command.
     */
    public Command(@NotNull JavaPlugin plugin, @NotNull String name) {
        super(name);
        this.plugin = plugin;
    }

    /**
     * Constructor to create a command with specified properties.
     *
     * @param plugin The plugin instance.
     * @param name The name of the command.
     * @param description The description of the command.
     * @param usage The usage message of the command.
     * @param aliases The aliases for the command.
     */
    public Command(@NotNull JavaPlugin plugin, @NotNull String name, @NotNull String description, @NotNull String usage, @NotNull List<String> aliases) {
        super(name, description, usage, aliases);
        this.plugin = plugin;
        register();
    }

    //Registers the command with the Bukkit command map.
    protected void register() {
        Bukkit.getCommandMap().register(plugin.getName(), this);
    }

    /**
     * Sets whether the command can only be executed by players.
     *
     * @param playerOnly True if the command is player-only, false otherwise.
     */
    protected void setPlayerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
    }

    /**
     * Sets the expected argument type for a specific argument index.
     *
     * @param index The index of the argument.
     * @param type The expected type of the argument.
     */
    protected void setArgumentType(int index, Class<?> type) {
        argumentTypes.put(index, type);
    }

    /**
     * Sets the expected argument types for the command using a list.
     *
     * @param types A list of classes representing the expected argument types.
     */
    protected void setArgumentTypes(List<Class<?>> types) {
        for (int i = 0; i < types.size(); i++) {
            argumentTypes.put(i, types.get(i));
        }
    }

    // Handles initial command execution and filters out invalid executions.
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        // Check if the command is player-only and if the sender is not a player
        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(Component.text("This is a player only command"));
            return false;
        }

        // Check if the sender has the required permission to execute the command
        if (this.getPermission() != null && !sender.hasPermission(this.getPermission())) {
            sender.sendMessage(Component.text("You are not permitted to use this command"));
            return false;
        }

        // Validate the argument types
        if (!validateArgumentTypes(args)) {
            sender.sendMessage(Component.text("Invalid arguments"));
            return false;
        }

        // Execute the command
        onCommand(sender, commandLabel, args);
        return false;
    }

    // Validates the types of the arguments against the expected types.
    private boolean validateArgumentTypes(String[] args) {
        for (int i = 0; i < args.length; i++) {
            Class<?> expectedType = argumentTypes.get(i);
            if (expectedType != null && !isType(args[i], expectedType)) {
                return false;
            }
        }
        return true;
    }

    // Checks if a given argument matches the expected type.
    private boolean isType(String arg, Class<?> type) {

        try {
            if (type == Integer.class) {
                Integer.parseInt(arg);
            } else if (type == Double.class) {
                Double.parseDouble(arg);
            } else if (type == Boolean.class) {
                if (!arg.equalsIgnoreCase("true") && !arg.equalsIgnoreCase("false")) {
                    return false;
                }
            } else return type == String.class;
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Abstract method to be implemented by subclasses to define the command's functionality.
     *
     * @param sender The sender of the command.
     * @param args The arguments passed to the command.
     */
    public abstract void onCommand(@NotNull CommandSender sender, String label, @NotNull String[] args);

    public abstract @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException;
}
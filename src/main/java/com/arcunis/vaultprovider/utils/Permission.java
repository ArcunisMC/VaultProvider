package com.arcunis.vaultprovider.utils;

import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Permission {

    /**
     * The name of the permission.
     */
    public final String name;

    /**
     * The description of the permission.
     */
    public final String description;

    /**
     * The default value of the permission.
     */
    public PermissionDefault defaultValue;

    /**
     * @param name the name of the permission.
     */
    public Permission(@NotNull String name) {
        this(name, null, null, null);
    }

    /**
     * @param name        the name of the permission.
     * @param description the description of the permission, can be null.
     */
    public Permission(@NotNull String name, @NotNull String description) {
        this(name, description, null, null);
    }

    /**
     * @param name         the name of the permission.
     * @param defaultValue the default value of the permission, can be null.
     */
    public Permission(@NotNull String name, @NotNull PermissionDefault defaultValue) {
        this(name, null, defaultValue, null);
    }

    /**
     * @param name         the name of the permission.
     * @param description  the description of the permission, can be null.
     * @param defaultValue the default value of the permission, can be null.
     */
    public Permission(@NotNull String name, @NotNull String description, @NotNull PermissionDefault defaultValue) {
        this(name, description, defaultValue, null);
    }

    /**
     * @param name     the name of the permission.
     * @param children the children of the permission, can be null.
     */
    public Permission(@NotNull String name, @NotNull Map<String, Boolean> children) {
        this(name, null, null, children);
    }

    /**
     * @param name        the name of the permission.
     * @param description the description of the permission, can be null.
     * @param children    the children of the permission, can be null.
     */
    public Permission(@NotNull String name, @NotNull String description, @NotNull Map<String, Boolean> children) {
        this(name, description, null, children);
    }

    /**
     * @param name         the name of the permission.
     * @param defaultValue the default value of the permission, can be null.
     * @param children     the children of the permission, can be null.
     */
    public Permission(@NotNull String name, @NotNull PermissionDefault defaultValue, @NotNull Map<String, Boolean> children) {
        this(name, null, defaultValue, children);
    }

    /**
     * @param name         the name of the permission.
     * @param description  the description of the permission, can be null.
     * @param defaultValue the default value of the permission, can be null.
     * @param children     the children of the permission, can be null.
     */
    Permission(@NotNull String name, @Nullable String description, @Nullable PermissionDefault defaultValue, @Nullable Map<String, Boolean> children) {
        this.name = name;
        this.description = (description == null) ? "" : description;
        this.defaultValue = (defaultValue == null) ? PermissionDefault.OP : defaultValue;

        // Create a new Bukkit permission and add it to the plugin manager
        org.bukkit.permissions.Permission perm = new org.bukkit.permissions.Permission(name, description, defaultValue, children);
        Bukkit.getPluginManager().addPermission(perm);
    }

}

package com.arcunis.vaultprovider.economy;

import com.arcunis.vaultprovider.Main;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class EconomyProvider implements Economy {

    private final Main main;

    public EconomyProvider(Main main) {
        this.main = main;
    }

    @Override
    public boolean isEnabled() {
        return main.getConfig().getBoolean("economy.enabled");
    }

    @Override
    public String getName() {
        return "VaultProvider Economy";
    }

    @Override
    public boolean hasBankSupport() {
        return true;
    }

    @Override
    public int fractionalDigits() {
        return 15;
    }

    @Override
    public String format(double v) {
        return main.getConfig().getString("symbol") + v;
    }

    @Override
    public String currencyNamePlural() {
        return main.getConfig().getString("name-plural");
    }

    @Override
    public String currencyNameSingular() {
        return main.getConfig().getString("name-singular");
    }

    @Override
    @Deprecated
    public boolean hasAccount(String s) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return EconomyManager.hasAcc(offlinePlayer.getUniqueId());
    }

    @Override
    @Deprecated
    public boolean hasAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return hasAccount(offlinePlayer);
    }

    @Override
    @Deprecated
    public double getBalance(String s) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return EconomyManager.getAccBal(offlinePlayer.getUniqueId());
    }

    @Override
    @Deprecated
    public double getBalance(String s, String s1) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return getBalance(offlinePlayer);
    }

    @Override
    @Deprecated
    public boolean has(String s, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double v) {
        return getBalance(offlinePlayer) >= v;
    }

    @Override
    @Deprecated
    public boolean has(String s, String s1, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return has(offlinePlayer, v);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        double newBal = EconomyManager.withdrawAcc(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, newBal, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return withdrawPlayer(offlinePlayer, v);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        double newBal = EconomyManager.depositAcc(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, newBal, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return depositPlayer(offlinePlayer, v);
    }

    @Override
    @Deprecated
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        if (EconomyManager.hasBank(s)) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "A bank with that name already exists.");
        EconomyManager.createBank(s, offlinePlayer.getUniqueId());
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        EconomyManager.deleteBank(s);
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, EconomyManager.getBankBal(s), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        EconomyResponse.ResponseType status;
        if (EconomyManager.getBankBal(s) >= v) status = EconomyResponse.ResponseType.SUCCESS;
        else status = EconomyResponse.ResponseType.FAILURE;
        return new EconomyResponse(0, EconomyManager.getBankBal(s), status, null);
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        double newBal = EconomyManager.withdrawBank(s, v);
        return new EconomyResponse(v, newBal, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        double newBal = EconomyManager.depositBank(s, v);
        return new EconomyResponse(v, newBal, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    @Deprecated
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        EconomyResponse.ResponseType status;
        if (EconomyManager.getBankOwner(s).equals(offlinePlayer.getUniqueId())) status = EconomyResponse.ResponseType.SUCCESS;
        else status = EconomyResponse.ResponseType.FAILURE;
        return new EconomyResponse(0, EconomyManager.getBankBal(s), status, null);
    }

    @Override
    @Deprecated
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        EconomyResponse.ResponseType status;
        if (EconomyManager.getBankMembers(s).contains(offlinePlayer.getUniqueId())) status = EconomyResponse.ResponseType.SUCCESS;
        else status = EconomyResponse.ResponseType.FAILURE;
        return new EconomyResponse(0, EconomyManager.getBankBal(s), status, null);
    }

    @Override
    public List<String> getBanks() {
        return EconomyManager.getAllBankNames().stream().toList();
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        if (EconomyManager.hasAcc(offlinePlayer.getUniqueId())) return false;
        EconomyManager.createAcc(offlinePlayer.getUniqueId());
        return true;
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return createPlayerAccount(offlinePlayer);
    }
}

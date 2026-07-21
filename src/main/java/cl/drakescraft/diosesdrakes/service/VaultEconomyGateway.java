package cl.drakescraft.diosesdrakes.service;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public final class VaultEconomyGateway {
    private final Economy economy;

    public VaultEconomyGateway(Economy economy) {
        this.economy = economy;
    }

    public double balance(Player player) {
        return economy.getBalance(player);
    }

    public boolean withdraw(Player player, double amount) {
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    public boolean deposit(Player player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }
}

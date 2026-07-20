package cl.drakescraft.diosesdrakes;

import cl.drakescraft.diosesdrakes.command.DiosesCommand;
import cl.drakescraft.diosesdrakes.api.DivineAccess;
import cl.drakescraft.diosesdrakes.api.DiosesDrakesAccess;
import cl.drakescraft.diosesdrakes.menu.PantheonMenuListener;
import cl.drakescraft.diosesdrakes.listener.HephaestusListener;
import cl.drakescraft.diosesdrakes.service.DivineAuditLogger;
import cl.drakescraft.diosesdrakes.service.DivineTransactionService;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import cl.drakescraft.diosesdrakes.service.LoadoutService;
import cl.drakescraft.diosesdrakes.service.SkillService;
import cl.drakescraft.diosesdrakes.service.CooldownService;
import cl.drakescraft.diosesdrakes.service.VaultEconomyGateway;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.sql.SQLException;
import java.time.Duration;

public final class DiosesDrakes extends JavaPlugin {
    private DivineRepository repository;
    private ProfileService profiles;
    private SkillService skills;
    private DivineTransactionService transactions;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!initializeServices()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DiosesCommand command = new DiosesCommand(profiles, skills);
        PluginCommand dioses = getCommand("dioses");
        if (dioses == null) {
            getLogger().severe("No se pudo registrar el comando /dioses.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        dioses.setExecutor(command);
        dioses.setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new PantheonMenuListener(profiles, skills), this);
        getServer().getPluginManager().registerEvents(new HephaestusListener(skills), this);
        getServer().getServicesManager().register(DivineAccess.class, new DiosesDrakesAccess(profiles, skills), this, ServicePriority.Normal);
        getLogger().info("DiosesDrakes core 0.1.0 habilitado.");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
        if (repository == null) {
            return;
        }
        try {
            repository.close();
        } catch (SQLException exception) {
            getLogger().warning("No se pudo cerrar la base de datos divina: " + exception.getMessage());
        }
    }

    public ProfileService profiles() {
        return profiles;
    }

    public DivineTransactionService transactions() {
        return transactions;
    }

    private boolean initializeServices() {
        try {
            Files.createDirectories(getDataFolder().toPath());
            repository = new DivineRepository(getDataFolder().toPath().resolve("diosesdrakes.db"));
            profiles = new ProfileService(
                    repository,
                    Duration.ofHours(getConfig().getLong("renunciation.cooldown-hours", 48)),
                    Duration.ofDays(7)
            );
            skills = new SkillService(repository, profiles, new LoadoutService(repository), new CooldownService());
        } catch (SQLException | java.io.IOException exception) {
            getLogger().severe("No se pudo iniciar la persistencia divina: " + exception.getMessage());
            return false;
        }

        if (!getConfig().getBoolean("economy.enabled", false)) {
            getLogger().info("Economia divina desactivada por configuracion.");
            return true;
        }

        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null || provider.getProvider() == null) {
            getLogger().severe("La economia divina esta activa, pero Vault no tiene proveedor.");
            return false;
        }

        transactions = new DivineTransactionService(
                repository,
                new VaultEconomyGateway(provider.getProvider()),
                new DivineAuditLogger(getDataFolder().toPath().resolve("audit"))
        );
        return true;
    }
}

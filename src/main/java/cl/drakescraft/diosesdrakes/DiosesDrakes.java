package cl.drakescraft.diosesdrakes;

import cl.drakescraft.diosesdrakes.command.DiosesCommand;
import cl.drakescraft.diosesdrakes.menu.PantheonMenuListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiosesDrakes extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        DiosesCommand command = new DiosesCommand();
        PluginCommand dioses = getCommand("dioses");
        if (dioses == null) {
            getLogger().severe("No se pudo registrar el comando /dioses.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        dioses.setExecutor(command);
        dioses.setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new PantheonMenuListener(), this);
        getLogger().info("DiosesDrakes core 0.1.0 habilitado.");
    }
}

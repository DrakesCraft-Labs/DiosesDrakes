package cl.drakescraft.diosesdrakes.integration;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

/** Uses WorldGuard's own interaction check, which also covers ProtectionStones regions. */
public final class ProtectionGate {
    private final PluginManager plugins;

    public ProtectionGate(PluginManager plugins) {
        this.plugins = plugins;
    }

    public boolean canInteract(Player player, Block block) {
        if (plugins.getPlugin("WorldGuard") == null) {
            return true;
        }
        try {
            Class<?> pluginType = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
            Object worldGuard = pluginType.getMethod("inst").invoke(null);
            Object query = pluginType.getMethod("createProtectionQuery").invoke(worldGuard);
            Object allowed = query.getClass().getMethod("testBlockInteract", Object.class, Block.class).invoke(query, player, block);
            return Boolean.TRUE.equals(allowed);
        } catch (ReflectiveOperationException | LinkageError exception) {
            // A protection integration that cannot answer must never grant a bypass.
            return false;
        }
    }
}

package cl.drakescraft.diosesdrakes.integration;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;

/** Reflective bridge keeps the core loadable when Slimefun is absent or its API changes. */
public final class SlimefunEnergyBridge {
    private static final String BLOCK_STORAGE = "me.mrCookieSlime.Slimefun.api.BlockStorage";
    private static final String ENERGY_COMPONENT = "com.github.drakescraft_labs.slimefun4.core.attributes.EnergyNetComponent";
    private final Set<String> allowedIds;

    public SlimefunEnergyBridge(Set<String> allowedIds) {
        this.allowedIds = allowedIds.stream().map(id -> id.toUpperCase(Locale.ROOT)).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public int addEnergy(Block block, int requested) {
        if (requested <= 0 || !canReceiveEnergy(block)) {
            return 0;
        }
        try {
            Object item = itemAt(block);
            Class<?> component = Class.forName(ENERGY_COMPONENT);
            Location location = block.getLocation();
            Method capacity = component.getMethod("getCapacity");
            Method charge = component.getMethod("getCharge", Location.class);
            Method addCharge = component.getMethod("addCharge", Location.class, int.class);
            int room = ((Integer) capacity.invoke(item)) - ((Integer) charge.invoke(item, location));
            int granted = Math.min(Math.max(room, 0), requested);
            if (granted > 0) {
                addCharge.invoke(item, location, granted);
            }
            return granted;
        } catch (ReflectiveOperationException | LinkageError exception) {
            return 0;
        }
    }

    public boolean canReceiveEnergy(Block block) {
        if (allowedIds.isEmpty()) {
            return false;
        }
        try {
            Class<?> storage = Class.forName(BLOCK_STORAGE);
            String id = (String) storage.getMethod("checkID", Block.class).invoke(null, block);
            Object item = itemAt(block);
            Class<?> component = Class.forName(ENERGY_COMPONENT);
            if (id == null || item == null || !allowedIds.contains(id.toUpperCase(Locale.ROOT)) || !component.isInstance(item)) {
                return false;
            }
            Location location = block.getLocation();
            int capacity = (Integer) component.getMethod("getCapacity").invoke(item);
            int charge = (Integer) component.getMethod("getCharge", Location.class).invoke(item, location);
            return charge < capacity;
        } catch (ReflectiveOperationException | LinkageError exception) {
            return false;
        }
    }

    private Object itemAt(Block block) throws ReflectiveOperationException {
        Class<?> storage = Class.forName(BLOCK_STORAGE);
        return storage.getMethod("check", Block.class).invoke(null, block);
    }
}

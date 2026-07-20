package cl.drakescraft.diosesdrakes.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;

public final class PantheonMenuHolder implements InventoryHolder {
    private final View view;
    private final Map<Integer, String> actionBySlot;

    public PantheonMenuHolder(View view, Map<Integer, String> actionBySlot) {
        this.view = view;
        this.actionBySlot = actionBySlot;
    }

    public View view() {
        return view;
    }

    public String actionAt(int slot) {
        return actionBySlot.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public enum View {
        PANTHEONS,
        DEITIES,
        SKILLS
    }
}

package cl.drakescraft.diosesdrakes.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;

public final class PantheonMenuHolder implements InventoryHolder {
    private final View view;
    private final Map<Integer, String> skillBySlot;

    public PantheonMenuHolder(View view, Map<Integer, String> skillBySlot) {
        this.view = view;
        this.skillBySlot = skillBySlot;
    }

    public View view() {
        return view;
    }

    public String skillAt(int slot) {
        return skillBySlot.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public enum View {
        SELECTION,
        SKILLS
    }
}

package com.projectkorra.items.processors;

import com.projectkorra.items.attribute.PKIAttribute;
import com.projectkorra.items.customs.PKItem;
import com.projectkorra.projectkorra.BendingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class EquipmentProcessor extends PoolProcessor {

    public EquipmentProcessor(Plugin plugin) {
        super(plugin);
    }

    public void add(UUID id){
        add(() -> process(id));
    }

    private boolean hasWaterPouchAttr(ItemStack itemStack) {
        PKItem item = PKItem.getCustomItem(itemStack);
        if (item == null)
            return false;
        PKIAttribute attr = item.getAttribute("WaterSource");
        return attr != null && (boolean) attr.getValues().getFirst();
    }

    private boolean hasWaterPouchAttr(ItemStack[] itemStacks) {
        for (ItemStack stack : itemStacks) {
            if (hasWaterPouchAttr(stack))
                return true;
        }
        return false;
    }

    public void process(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null)
            return;

        BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(player);
        if (bendingPlayer == null)
            return;

        PlayerInventory equipment = player.getInventory();

        boolean hasWaterPouch = hasWaterPouchAttr(equipment.getArmorContents())
                || hasWaterPouchAttr(equipment.getStorageContents())
                || hasWaterPouchAttr(equipment.getExtraContents());
        bendingPlayer.setWaterPouch(hasWaterPouch);
    }
}

package com.projectkorra.items.listeners;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.items.attribute.Action;
import com.projectkorra.items.attribute.Attribute;
import com.projectkorra.items.customs.PKItem;
import com.projectkorra.items.processors.EquipmentProcessor;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import com.projectkorra.items.items.Glider;
import com.projectkorra.items.utils.AttributeUtils;
import com.projectkorra.items.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class AttributeListener implements Listener {
	/**
	 * A map of player names that holds their current bending potion effects.
	 **/
	public static final Map<String, Map<String, Attribute>> currentBendingEffects = new ConcurrentHashMap<>();
	private final EquipmentProcessor processor;
	private final Plugin plugin;

	public AttributeListener(Plugin plugin) {
		this.plugin = plugin;
		processor = new EquipmentProcessor(plugin);
		processor.start();
	}

	public void stop() {
		processor.stop();
	}

	/**
	 * When the player sneaks we should attempt to let them Glide. The Glider
	 * class will handle whether or not they can actually glide. Attempt to
	 * confirm an ability to decrease charges.
	 * 
	 * @param event a sneak event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		new Glider(player);

		// Handles the Charges, and ShiftCharges attribute
		if (!player.isSneaking()) {
			ItemUtils.updateOnActionEffects(player, Action.SHIFT);
		}
	}

	/**
	 * Confirm if an ability was executed via clicking. Also handle specific
	 * stats that related to left clicking.
	 * 
	 * @param event a player animation event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerSwing(PlayerAnimationEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		ItemUtils.updateOnActionEffects(player, Action.LEFT_CLICK);
	}

	/**
	 * Make a call to allow all consume based affects for the custom item that
	 * the player ate.
	 * 
	 * @param event a consume event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerConsume(PlayerItemConsumeEvent event) {
		if (event.isCancelled())
			return;
		ItemUtils.updateOnActionEffects(event.getPlayer(), Action.CONSUME);
	}

	/**
	 * Because of the "AirGliderAutomatic" stat, we need to attempt to glide
	 * whenever the user switches their item.
	 * 
	 * @param event item change event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChangeItem(PlayerItemHeldEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		Map<String, Double> attribs = AttributeUtils.getSimplePlayerAttributeMap(player);
		boolean auto = Attribute.getBooleanValue("AirGlideAutomatic", attribs);
		if (auto) {
			new Glider(player, true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
			if (bendingPlayer == null || !bendingPlayer.hasElement(Element.WATER)) {
				return;
			}
			List<ItemStack> equipment = ItemUtils.getPlayerValidEquipment(event.getPlayer());
			for (ItemStack istack : equipment) {
				PKItem citem = PKItem.getCustomItem(istack);
				if (citem == null)
					continue;
				for (Attribute attr : citem.getAttributes()) {
					if (attr.getName().equals("WaterSource")) {
						bendingPlayer.setWaterPouch(true);
						return;
					}
				}
			}
		}, 20);
	}
}

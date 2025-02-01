package com.projectkorra.items.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.items.Messages;
import com.projectkorra.items.attribute.Action;
import com.projectkorra.items.attribute.PKIAttribute;
import com.projectkorra.items.attribute.AttributeList;
import com.projectkorra.items.customs.PKItem;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AttributeUtils {
	private static final Map<UUID, Integer> lastCheck = new ConcurrentHashMap<>();
	private static final Map<UUID, List<PKItem>> currentPKItems = new ConcurrentHashMap<>();

	/**
	 * Generates a map containing all of the attributes on the players armor and
	 * item in hand. Doesn't return values with multiple commas. Doesn't return
	 * non numerical values.
	 * 
	 * @param player the player to create the effects of
	 * @return a map containing attribute effects
	 */
	public static Collection<PKItem> getActivePKitems(Player player) {
		UUID uuid = player.getUniqueId();
		int lastTick = lastCheck.getOrDefault(uuid, -1);
		int currentTick = Bukkit.getCurrentTick();
		if (lastTick != -1 && currentTick == lastTick)
			return currentPKItems.get(uuid);
		List<ItemStack> equipment = ItemUtils.getPlayerValidEquipment(player);
		List<PKItem> items = equipment.stream().map(PKItem::getCustomItem).filter(Objects::nonNull).toList();
		currentPKItems.put(uuid, items);
		lastCheck.put(uuid, currentTick);
		return items;
	}

	/**
	 * Takes an attribute stat and tries to split its values into a list of
	 * PotionEffects.
	 * 
	 * @param potionData the attribute to split
	 * @return a list of the new PotionEffects
	 */
	public static @Nullable PotionEffect parsePotionEffect(@NonNull String potionData) {
		PotionEffect effect = null;
		String[] colSplit = potionData.split(":");
		PotionEffectType type = PotionEffectType.getByName(colSplit[0].trim());
		if (type != null) {
			int strength = Integer.parseInt(colSplit[1].trim());
			double duration = Double.parseDouble(colSplit[2].trim());
			effect = new PotionEffect(type, (int) (duration * 20), strength - 1);
		}
		return effect;
	}

	/**
	 * Decreases the charges on all of the player's items by 1, if the charge
	 * type on the item matches the Action type.
	 * 
	 * @param player the player to decrease charges
	 * @param type the action that caused the charge decrease
	 */
	
	public static void decreaseCharges(Player player, Action type) {
		if (player == null)
			return;

		List<ItemStack> istacks = ItemUtils.getPlayerValidEquipment(player);
		for (ItemStack istack : istacks) {
			PKItem citem = PKItem.getCustomItem(istack);
			if (citem == null)
				continue;

			ItemMeta meta = istack.getItemMeta();
			if (meta == null)
				continue;
			List<String> lore = meta.getLore();
			if (lore == null)
				continue;

			boolean displayDestroyMsg = false;
			List<String> newLore = new ArrayList<>();
			for (String line : lore) {
				String newLine = line;
				if (line.startsWith(AttributeList.CHARGES_STR) || (line.startsWith(AttributeList.CLICK_CHARGES_STR) && (type == Action.LEFT_CLICK || type == Action.RIGHT_CLICK || type == null)) || (line.startsWith(AttributeList.SNEAK_CHARGES_STR) && (type == Action.SHIFT || type == null))) {
					String start = line.substring(0, line.indexOf(": "));
					String end = line.substring(line.indexOf(": ") + 1);
					end = end.trim();
					int val = Integer.parseInt(end) - 1;
					if (val == 0)
						displayDestroyMsg = true;
					if (val >= 0)
						newLine = start + ": " + val;
				}
				newLore.add(newLine);
			}
			meta.setLore(newLore);
			istack.setItemMeta(meta);

			// Check if we need to destroy the item
			boolean hasDestroyAttr = false;
			boolean hasIgnoreDestroyMsg = false;
			if (citem.getBooleanAttributeValue("DestroyAfterCharges"))
				hasDestroyAttr = true;
			if (citem.getBooleanAttributeValue("IgnoreDestroyMessage"))
				hasIgnoreDestroyMsg = true;

			boolean hasChargesLeft = true;
			for (String line : newLore) {
				if (line.startsWith(AttributeList.CHARGES_STR) || line.startsWith(AttributeList.CLICK_CHARGES_STR) || line.startsWith(AttributeList.SNEAK_CHARGES_STR)) {
					String tmpStr = line.substring(line.indexOf(": ") + 1).trim();
					int value = Integer.parseInt(tmpStr);
					if (value <= 0)
						hasChargesLeft = false;
					else {
						hasChargesLeft = true;
						break;
					}
				}
			}

			/*
			 * When we go to destroy an item we need to check that there were
			 * not multiple items in that stack. If there were multiple items
			 * then we need to just remove 1 and change the lore back to the
			 * start.
			 */
			if (!hasChargesLeft && !hasIgnoreDestroyMsg && displayDestroyMsg)
				player.sendMessage(citem.getDisplayName() + " " + Messages.ITEM_DESTROYED);
			if (hasDestroyAttr && !hasChargesLeft) {
				if (player.getInventory().contains(istack)) {
					if (istack.getAmount() > 1) {
						istack.setAmount(istack.getAmount() - 1);
						ItemStack newStack = citem.generateItem();
						List<String> aux = null;
						if (newStack.getItemMeta() != null) {
							aux = newStack.getItemMeta().getLore();
						}
						ItemUtils.setLore(istack, aux);
					} else
						player.getInventory().remove(istack);
				} else {
					ItemStack[] armor = player.getInventory().getArmorContents();
					for (int i = 0; i < armor.length; i++) {
						if (armor[i].equals(istack)) {
							if (istack.getAmount() > 1) {
								armor[i].setAmount(armor[i].getAmount() - 1);
								ItemStack newStack = citem.generateItem();
								List<String> aux = null;
								if (newStack.getItemMeta() != null) {
									aux = newStack.getItemMeta().getLore();
								}
								ItemUtils.setLore(armor[i], aux);
							} else
								armor[i] = new ItemStack(Material.AIR);
							break;
						}
					}
					player.getInventory().setArmorContents(armor);
				}
			}
		}
	}

	/**
	 * Determines if a player is allowed to use a specific CustomItem, depending
	 * on if the CustomItem has the "RequireElement" Attribute. If the item has
	 * the Attribute then it compares the elements to the player's element.
	 * 
	 * @param player the player using the custom item
	 * @param citem a custom item in the player's inventory
	 * @return true if the player can use this custom item
	 */
	public static boolean hasRequiredElement(Player player, PKItem citem) {
		if (player == null || citem == null) {
			return false;
		}
		PKIAttribute requireElem = citem.getAttribute("RequireElement");
		if (requireElem != null) {
			boolean allowed = false;
			for (Object rawVal : requireElem.getValues()) {
				String val = (String) rawVal;
				try {
					if (ElementUtils.hasElement(player, val)) {
						allowed = true;
						break;
					}
				}
				catch (IllegalArgumentException e) {
					Messages.logTimedMessage(e.getMessage());
				}
			}

			return allowed;
		}
		return true;
	}

	/**
	 * Determines if a player is allowed to use a specific CustomItem, depending
	 * on if the CustomItem has the "RequireWorld" Attribute. If the item has
	 * the Attribute then it compares the player's current world to any of the
	 * item's possible worlds.
	 * 
	 * @param player the player using the custom item
	 * @param citem a custom item in the player's inventory
	 * @return true if the player can use this custom item
	 */
	public static boolean hasRequiredWorld(Player player, PKItem citem) {
		if (player == null || citem == null) {
			return false;
		}
		PKIAttribute require = citem.getAttribute("RequireWorld");
		if (require != null) {
			return require.getValues().contains(player.getWorld().getName());
		}
		return true;
	}

	/**
	 * Determines if a player is allowed to use a specific CustomItem, depending
	 * on if the CustomItem has the "RequirePermission" Attribute. If the item
	 * has the Attribute then it compares all of the CustomItem's permissions to
	 * see if the player has at least one of them.
	 * 
	 * @param player the player using the custom item
	 * @param citem a custom item in the player's inventory
	 * @return true if the player can use this custom item
	 */
	public static boolean hasRequiredPermission(Player player, PKItem citem) {
		if (player == null || citem == null) {
			return false;
		}
		PKIAttribute require = citem.getAttribute("RequirePermission");
		if (require != null) {
			for (Object rawPerm : require.getValues()) {
				String perm = (String) rawPerm;
				if (player.hasPermission(perm))
					return true;
			}
			return false;
		}
		return true;
	}
}

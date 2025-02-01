package com.projectkorra.items;

import com.projectkorra.items.attribute.PKIAttribute;
import com.projectkorra.items.customs.PKItem;
import com.projectkorra.items.utils.AttributeUtils;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.AttributeModification;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {
	public static final String DNAME_PREF = "displayname";
	public static final String LORE_PREF = "lore";
	public static final String SHAPED_RECIPE_PREF = "shapedrecipe";
	public static final String UNSHAPED_RECIPE_PREF = "unshapedrecipe";
	public static final String MATERIAL_PREF = "material";
	public static final String AMT_PREF = "amount";
	public static final String ATTR_PREF = "stats";
	public static final String GLOW_PREF = "glow";


	public ConfigManager() {
		ProjectKorraItems.getInstance().saveDefaultConfig();
		reloadConfig();
	}

	/**
	 * Reloads the config file.
	 */
	private void reloadConfig() {
		PKItem.getItems().clear();
		PKItem.itemList.clear();
		try {
			FileConfiguration config = ProjectKorraItems.getInstance().getConfig();
			// if (config == null) {
			// 	ProjectKorraItems.log.info(Messages.NO_CONFIG);
			// }
			Set<String> itemNames = config.getKeys(false);
			for (String itemName : itemNames) {
				ConfigurationSection itemSection = config.getConfigurationSection(itemName);
				if (itemSection == null) {
					ProjectKorraItems.warning(Messages.BAD_ITEM + " " + itemName + ": " + Messages.BAD_VALUE);
					continue;
				}
				try {
					loadItem(itemName, itemNames, itemSection);
				}
				catch (Exception ex) {
					ProjectKorraItems.warning(Messages.BAD_ITEM + " " + itemName);
					ex.printStackTrace();
				}
			}
		}
		catch (Exception ex) {
			ProjectKorraItems.severe(Messages.BAD_FILE);
			ex.printStackTrace();
		}
	}

	private void loadItem(@NonNull String itemName, @NonNull Set<String> customItemNames, @NonNull ConfigurationSection itemConfig) {
		PKItem item = new PKItem();
		item.updateName(itemName);
		Set<String> keys = itemConfig.getKeys(false);
		boolean hasRecipe = false;
		for (String key : keys) {
			switch(key.toLowerCase()) {
				case DNAME_PREF:
					item.updateDisplayName(itemConfig.getString(key));
					break;
				case LORE_PREF:
					item.updateLore(itemConfig.getString(key));
					break;
				case SHAPED_RECIPE_PREF:
					if (hasRecipe) {
						ProjectKorraItems.warning(Messages.DUPLICATED_RECIPE + " (" + itemName + ")");
						return;
					}
					item.updateRecipe(itemConfig.getString(key), customItemNames);
					item.setUnshapedRecipe(false);
					hasRecipe = true;
					break;
				case UNSHAPED_RECIPE_PREF:
					if (hasRecipe) {
						ProjectKorraItems.warning(Messages.DUPLICATED_RECIPE + " (" + itemName + ")");
						return;
					}
					item.updateDisplayName(itemConfig.getString(key));
					item.setUnshapedRecipe(true);
					hasRecipe = true;
					break;
				case MATERIAL_PREF:
					item.updateMaterial(itemConfig.getString(key));
					break;
				case AMT_PREF:
					item.updateQuantity(itemConfig.getInt(key));
					break;
				case ATTR_PREF:
					updateAttributes(item, Objects.requireNonNull(itemConfig.getConfigurationSection(key)));
					break;
				case GLOW_PREF:
					item.updateGlow(itemConfig.getBoolean(key));
					break;
			}
		}
		item.build();
	}

	private void updateAttributes(@NonNull PKItem item, @NonNull ConfigurationSection cs) {
		for (String attributeName : cs.getKeys(false)) {
			PKIAttribute att = PKIAttribute.getAttribute(attributeName);
			if (att == null) {
				ProjectKorraItems.info(Messages.BAD_ATTRIBUTE + ": " + attributeName);
			}
			if (att != null && !att.isAbility()) {
				String valueStr = cs.getString(attributeName);
				if (valueStr == null)
					valueStr = "";
				String[] commaSplit = valueStr.split(",");
				if (commaSplit.length == 0 || valueStr.isEmpty()) {
					ProjectKorraItems.info(Messages.MISSING_VALUES + ": " + valueStr);
					continue;
				}
				for (String rawValue : commaSplit) {
					rawValue = rawValue.trim();
					Object value;
					try {
						if (rawValue.contains(".") || rawValue.contains("e"))
							value = Float.parseFloat(rawValue);
						else
							value = Integer.parseInt(rawValue);
					}
					catch (NumberFormatException ex) {
						String aux = rawValue.toLowerCase();
						if (aux.equals("true"))
							value = true;
						else if (aux.equals("false"))
							value = false;
						else
							value = AttributeUtils.parsePotionEffect(rawValue);
						if (value == null)
							value = rawValue;
					}
					att.addValue(value);
				}
			}
			else {
				ConfigurationSection data = cs.getConfigurationSection(attributeName);
				parseAbilityAttribute(item, attributeName, Objects.requireNonNull(data));
			}
			item.addAttribute(att);
		}
	}

	private void parseAbilityAttribute(@NonNull PKItem item, @NonNull String abilityName, @NonNull ConfigurationSection cs) {
		Map<String, AttributeModification> modifiers = item.getModifiers(abilityName);
		for (String attribute : cs.getKeys(false)) {
			String rawValue = cs.getString(attribute);
            char first = rawValue.charAt(0);
			AttributeModifier op = switch(first) {
				case '+' -> AttributeModifier.ADDITION;
				case '-' -> AttributeModifier.SUBTRACTION;
				case '*', 'x' -> AttributeModifier.MULTIPLICATION;
				case '/' -> AttributeModifier.DIVISION;
                default -> AttributeModifier.SET;
            };
			if (op != AttributeModifier.SET)
				rawValue = rawValue.substring(1);
			float value;
			try {
				value = Float.parseFloat(rawValue);
			}
			catch (NumberFormatException ex) {
				if (op != AttributeModifier.SET)
					rawValue = first + rawValue;
				ProjectKorraItems.info(Messages.BAD_VALUE + ": " + rawValue);
				continue;
			}
			CoreAbility ability = CoreAbility.getAbility(abilityName);
			if (ability != null) {  // We are modifying a single ability
				if (!CoreAbility.getAttributeCache(ability).containsKey(attribute)) {
					ProjectKorraItems.info(Messages.BAD_ATTRIBUTE + ": " + abilityName + " - " + attribute);
					for (String attrName : CoreAbility.getAttributeCache(ability).keySet())
						ProjectKorraItems.info(" - " + attrName);
				}
			}
			NamespacedKey key = new NamespacedKey(ProjectKorraItems.getInstance(), item.getName() + "_" + abilityName + attribute);
			AttributeModification modifier = AttributeModification.of(op, value, key);
			modifiers.put(attribute, modifier);
		}
	}

	/**
	 * Gathers the names of all of the custom items within the config file.
	 *
	 * @return a set containing the item names
	 */
	public Set<String> getConfigItemNames() {
		return ProjectKorraItems.getInstance().getConfig().getKeys(false);
	}
}

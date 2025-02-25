package com.projectkorra.items.customs;

import com.projectkorra.items.Messages;
import com.projectkorra.items.ProjectKorraItems;
import com.projectkorra.items.attribute.PKIAttribute;
import com.projectkorra.items.attribute.AttributeList;

import com.projectkorra.projectkorra.attribute.AttributeModification;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PKItem {
	private static final Map<String, PKItem> items = new ConcurrentHashMap<>();
	public static List<PKItem> itemList = new ArrayList<>();
	public static NamespacedKey PKI_KEY = new NamespacedKey(ProjectKorraItems.getInstance(), "name");

	private String name;
	private String displayName;
	private List<String> lore;
	private boolean isOraxen;
	private String oraxenId;
	private Material material;
	private int quantity;
	// private short damage;
	private List<RecipeIngredient> recipe;
	private boolean unshapedRecipe;
	private boolean valid;
	private boolean alreadyFinal;
	private Boolean glow;
	private Map<String, PKIAttribute> attributes;
	private final Map<String, Map<String, AttributeModification>> modifiers = new ConcurrentHashMap<>();

	public PKItem() {
		name = "";
		displayName = "";
		lore = new ArrayList<>();
		isOraxen = false;
		oraxenId = null;
		material = null;
		quantity = 1;
		// damage = (short) 0;
		recipe = new ArrayList<>();
		valid = true;
		unshapedRecipe = true;
		glow = null;
		attributes = new ConcurrentHashMap<>();
	}

	public void updateName(String s) {
		if (s == null || s.isEmpty() || s.contains(" ")) {
			valid = false;
			ProjectKorraItems.info(Messages.BAD_NAME + ": " + toString());
			if (s != null)
				name = s;
		} else {
			name = s;
		}
	}

	public void updateDisplayName(String s) {
		if (s == null || s.isEmpty()) {
			valid = false;
			ProjectKorraItems.info(Messages.BAD_DNAME + ": " + toString());
		} else {
			if (!s.contains("<&"))
				s = "<&f>" + s;
			displayName = colorizeString(s);
		}
	}

	public void updateLore(String s) {
		if (s == null || s.isEmpty()) {
			valid = false;
			ProjectKorraItems.info(Messages.BAD_LORE + ": " + toString());
		} else {
			String[] lines = s.split("<n>");
			for (String line : lines)
				lore.add(colorizeString(line));
		}
	}

	public void updateMaterial(String s) {
		if (s == null || s.isEmpty()) {
			valid = false;
			ProjectKorraItems.info(Messages.BAD_MATERIAL + "(95): " + toString());
		} else {
			if (s.toLowerCase().startsWith("oraxen:")) {
				s = s.split(":")[1];
				if (OraxenItems.exists(s)) {
					oraxenId = s;
					isOraxen = true;
				}
				else {
					valid = false;
					ProjectKorraItems.info(Messages.BAD_MATERIAL + "(105): oraxen:" + s);
				}
			}
			else {
				material = Material.getMaterial(s);
				if (material == null) {
					valid = false;
					ProjectKorraItems.info(Messages.BAD_MATERIAL + "(112): " + s);
				}
			}
		}
	}

	public void updateQuantity(int qty) {
		quantity = qty;
	}

	/*
	public void updateDamage(String s) {
		try {
			damage = (short) Integer.parseInt(s);
		}
		catch (Exception e) {
			valid = false;
			ProjectKorraItems.log.info(Messages.BAD_DAMAGE + ": " + toString());
		}
	}
	*/

	public void updateGlow(Boolean glow) {
		this.glow = glow;
	}

	/**
	 * Updates this CustomItem with a new Recipe. The recipe will consist of
	 * comma and colon separated entries to define the recipe ingredients. For
	 * example: <i>UnshapedRecipe: WOOL, WOOL:3:14, WOOL</i> Recipe ingredients
	 * can be both Material and other CustomItems.
	 * 
	 * @param recipeStr the string containing the recipe
	 * @param customItemNames the set with all custom item names
	 */
	public void updateRecipe(String recipeStr, Set<String> customItemNames) {
		try {
			recipeStr = recipeStr.replaceAll(" ", "");
			String[] commas = recipeStr.split(",");

			for (String comma : commas) {
				String[] colons = comma.split(":");
				Material mat = Material.getMaterial(colons[0]);

				// Try to get the Material by id, not anymore
				/*
				if (mat == null) {
					try {
						mat = Material.getMaterial(Integer.parseInt(colons[0]));
					}
					catch (NumberFormatException e) {
					}
				} */

				int quantity = 1;
				PotionType potionType = null;
				if (colons.length > 1)
					quantity = Integer.parseInt(colons[1]);
				if (colons.length > 2)
					potionType = PotionType.valueOf(colons[2]);

				// The material is either invalid or it is a custom item name
				if (mat != null) {
					recipe.add(new RecipeIngredient(mat, quantity, potionType));
				} else if (customItemNames.contains(colons[0])) {
					recipe.add(new RecipeIngredient(colons[0], quantity, potionType));
				} else {
					ProjectKorraItems.info(Messages.BAD_RECIPE_MAT + ": " + colons[0]);
					valid = false;
				}
			}
			while (recipe.size() < 9) {
				recipe.add(new RecipeIngredient(Material.AIR));
			}
		}
		catch (Exception e) {
			ProjectKorraItems.info(Messages.BAD_RECIPE + ": " + recipeStr);
			valid = false;
		}
	}

	public void build() {
		if (alreadyFinal)
			return;
		alreadyFinal = true;
		if (!valid)
			ProjectKorraItems.info(Messages.BAD_ITEM + ": " + this);
		else if (items.containsKey(name.toLowerCase()))
			ProjectKorraItems.info(Messages.DUPLICATE_ITEM + ": " + this);
		else if (name.isEmpty())
			ProjectKorraItems.info(Messages.BAD_NAME + ": " + this);
		else if (displayName.isEmpty())
			ProjectKorraItems.info(Messages.BAD_DNAME + ": " + this);
		else if ((!isOraxen && material == null) || (isOraxen && oraxenId == null)) {
			ProjectKorraItems.info(Messages.BAD_MATERIAL + "(219): " + this);
		}
		else {
			items.put(name.toLowerCase(), this);
			itemList.add(this);
		}
	}

	public ItemStack generateItem() {
		ItemStack istack;
		if (!isOraxen) {
			istack = new ItemStack(material, quantity);
		}
		else {
			istack = OraxenItems.getItemById(oraxenId).setAmount(1).build().clone();
			istack.setAmount(1);
		}
		istack.editMeta((meta) -> {
			meta.setDisplayName(displayName);
			List<String> tempLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
			if (tempLore == null) {
				tempLore = new ArrayList<>();
			}
			tempLore.addAll(lore);

			for (Map.Entry<String, PKIAttribute> entry : attributes.entrySet()) {
				String name = entry.getKey();
				PKIAttribute attr = entry.getValue();
				try {
					if (name.equalsIgnoreCase("Charges"))
						tempLore.add(AttributeList.CHARGES_STR + attr.getValues().getFirst());
					else if (name.equalsIgnoreCase("ClickCharges"))
						tempLore.add(AttributeList.CLICK_CHARGES_STR + attr.getValues().getFirst());
					else if (name.equalsIgnoreCase("SneakCharges"))
						tempLore.add(AttributeList.SNEAK_CHARGES_STR + attr.getValues().getFirst());
				} catch (Exception ignored) { }

				try {
					if (name.equalsIgnoreCase("LeatherColor")) {
						LeatherArmorMeta lmeta = (LeatherArmorMeta) meta;
						lmeta.setColor(Color.fromRGB((int) attr.getValues().get(0), (int) attr.getValues().get(1), (int) attr.getValues().get(2)));
					}
				} catch (Exception ignored) { }
			}

			meta.setLore(tempLore);

			PersistentDataContainer itemDC = meta.getPersistentDataContainer();
			itemDC.set(PKI_KEY, PersistentDataType.STRING, name);

			meta.setEnchantmentGlintOverride(glow);
		});

		return istack;
	}

	/**
	 * Checks if a CustomItem has a specific attribute, and also that the
	 * attribute has a boolean value of true.
	 * If the value is false, or it was not found, then this returns false.
	 * 
	 * @param attrib the name of the attribute
	 * @return true if the attribute was found and true
	 */
	public boolean getBooleanAttributeValue(String attrib) {
		PKIAttribute attr = attributes.get(attrib);
		if (attr != null)
			return (boolean) attr.getValues().getFirst();
		return false;
	}

	public static Map<String, PKItem> getItems() {
		return items;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public List<String> getLore() {
		return lore;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public Material getMaterial() {
		return material;
	}

	public void addAttribute(@NonNull PKIAttribute attribute) {
		attributes.put(attribute.getName(), attribute);
	}

	public @NonNull Map<String, AttributeModification> getModifiers(@NonNull String ability) {
		return modifiers.computeIfAbsent(ability, k -> new ConcurrentHashMap<>());
	}

	/**
	 * Given an attribute name, returns the attribute if this item has it, else
	 * returns null.
	 *
	 * @return an attribute or null
	 */
	public Map<String, PKIAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Given an attribute name, returns the attribute if this item has it, else
	 * returns null.
	 * 
	 * @param attrName the name of the attribute
	 * @return an attribute or null
	 */
	public PKIAttribute getAttribute(String attrName) {
		return attributes.get(attrName);
	}

	/*
	public void setDamage(short damage) {
		this.damage = damage;
	}
	*/

	public void setMaterial(Material material) {
		this.material = material;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/*
	public short getDamage() {
		return damage;
	}

	public void setData(short damage) {
		this.damage = damage;
	}
	*/

	public List<RecipeIngredient> getRecipe() {
		return recipe;
	}

	public void setRecipe(List<RecipeIngredient> recipe) {
		this.recipe = recipe;
	}

	public boolean isUnshapedRecipe() {
		return unshapedRecipe;
	}

	public void setUnshapedRecipe(boolean unshapedRecipe) {
		this.unshapedRecipe = unshapedRecipe;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public String toString() {
		// return "CustomItem [name=" + name + ", displayName=" + displayName + ", lore=" + lore + ", material=" + material + ", quantity=" + quantity + ", damage=" + damage;
		return "CustomItem [name=" + name + ", displayName=" + displayName + ", lore=" + lore + ", material=" + material + ", isOraxen=" + isOraxen + ", oraxenId=" + oraxenId + ", quantity=" + quantity + "]";
	}

	public static String colorizeString(String s) {
		s = s.replaceAll("<", "");
		s = s.replaceAll(">", "");
		s = ChatColor.translateAlternateColorCodes('&', s);
		return s;
	}

	public static PKItem getCustomItem(ItemStack istack) {
		String name = getIdByItem(istack);

		if (name == null)
			return null;

		return getCustomItem(name);
	}

	public static String getIdByItem(ItemStack istack) {
		if (istack == null || istack.getItemMeta() == null)
			return null;

		PersistentDataContainer itemDC = istack.getItemMeta().getPersistentDataContainer();

        return itemDC.get(PKI_KEY, PersistentDataType.STRING);
	}

	public static PKItem getCustomItem(String itemName) {
		if (items.containsKey(itemName.toLowerCase()))
			return items.get(itemName.toLowerCase());
		return null;
	}
}

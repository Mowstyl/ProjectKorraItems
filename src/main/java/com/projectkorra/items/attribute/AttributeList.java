package com.projectkorra.items.attribute;

import com.projectkorra.projectkorra.Element;

import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.ChatColor;

import java.util.ArrayList;

public class AttributeList {
	public static final double AIR_GLIDE_SPEED = 0.6;
	public static final double AIR_GLIDE_FALL = -0.12;
	public static final String CHARGES_STR = ChatColor.GOLD + "Charges: ";
	public static final String SNEAK_CHARGES_STR = ChatColor.GOLD + "SneakCharges: ";
	public static final String CLICK_CHARGES_STR = ChatColor.GOLD + "ClickCharges: ";

	public static final ArrayList<PKIAttribute> ATTRIBUTES = new ArrayList<>();
	static {
		// MISC
		//add(new Attribute("GrapplingHook", "Lets the user shoot out a grappling hook by left clicking. They can then left click to launch themselves toward the destination, or sneak to slowly move to the destination."));
		ATTRIBUTES.add(new PKIAttribute("ParticleEffects", "Plays a particle effect around a player whenever they use an ability. " + "Uses the form 'ParticleEffect: NAME:<AMOUNT>:<RADIUS>:<DURATION>:<SPEED>', " + "amount (1 - inf), radius (0 - 100), duration (1 - inf), and speed (0 - 100) are optional. " + "Click http://pastebin.com/Trbh34WG to see the list of available particles. " + "For example: 'ParticleEffects: flame:5:100:10:100"));

		ATTRIBUTES.add(new PKIAttribute("AllowFromInventory", "This item will work from the players inventory, even if it is not being held or worn."));
		ATTRIBUTES.add(new PKIAttribute("RequireElement", "The user must have one of these elements to use this item. Works with Air, Water, Earth, Fire, Chi, Flight, Spiritual, Blood, Healing, Ice, Plant, Metal, Sand, Lava, Combustion, and Lightning. For example: 'RequireElement: Fire, Air, Chi'"));
		ATTRIBUTES.add(new PKIAttribute("RequireWorld", "The user must be located in one of these specific worlds to use this item. For example: 'RequireWorld: bendingworld, bendingarena, bendingrpg'"));
		ATTRIBUTES.add(new PKIAttribute("RequirePermission", "The user must have one of these permissions to use this item. For example: 'RequirePermission: bending.admin.*, essentials.command.give'"));

		ATTRIBUTES.add(new PKIAttribute("WaterSource", "acts as a temporary water source for water abilities"));

		ATTRIBUTES.add(new PKIAttribute("Effects", "adds potion and bending effects whenever the player clicks, sneaks, or consumes the item. For example: Effects: JUMP:1:30, BlazeRange:25:30 would add Jump 1 and increase Blaze range by 25% for 30 seconds."));
		ATTRIBUTES.add(new PKIAttribute("ClickEffects", "adds potion and bending effects when the player clicks"));
		ATTRIBUTES.add(new PKIAttribute("SneakEffects", "adds potion and bending effects when the player sneaks"));
		ATTRIBUTES.add(new PKIAttribute("ConsumeEffects", "adds potion and bending effects when this item is consumed"));

		ATTRIBUTES.add(new PKIAttribute("Charges", "the number of charges remaining, charges are decreased by both clicking and sneaking, and an item stops working if it runs out of charges"));
		ATTRIBUTES.add(new PKIAttribute("ClickCharges", "charges are only decreased by clicking"));
		ATTRIBUTES.add(new PKIAttribute("SneakCharges", "charges are only decreased by sneaking"));
		ATTRIBUTES.add(new PKIAttribute("DestroyAfterCharges", "the item will be destroyed when the charges run out"));
		ATTRIBUTES.add(new PKIAttribute("IgnoreDestroyMessage", "the player will not receive a message that the item was destroyed when the charges run out"));

		ATTRIBUTES.add(new PKIAttribute("WearOnly", "the item will only work if it is being worn as armor"));
		ATTRIBUTES.add(new PKIAttribute("HoldOnly", "the item will only work if it is being held"));
		ATTRIBUTES.add(new PKIAttribute("LeatherColor", "gives leather armor a specific color, specified as R,G,B. For example, LeatherColor:255,0,0 would be red armor"));

		ATTRIBUTES.add(new PKIAttribute("AirGlide", "(true/false) allows an Airbender to glide through the air by sneaking"));
		ATTRIBUTES.add(new PKIAttribute("AirGlideSpeed", "modifies the air gliding speed"));
		ATTRIBUTES.add(new PKIAttribute("AirGlideFallSpeed", "modifies the air gliding fall speed", -1));
		ATTRIBUTES.add(new PKIAttribute("AirGlideAutomatic", "(true/false) gliding will start the moment that the user switches to the slot, they don't have to sneak"));

		// All abilities from an element
		ATTRIBUTES.add(new PKIAttribute("Air", "improves all Air stats", true));
		ATTRIBUTES.add(new PKIAttribute("Water", "improves all Water stats", true));
		ATTRIBUTES.add(new PKIAttribute("Earth", "improves all Earth stats", true));
		ATTRIBUTES.add(new PKIAttribute("Fire", "improves all Fire stats", true));
		ATTRIBUTES.add(new PKIAttribute("Chi", "improves all Chi stats", true));

		// Specific Abilities
		for (CoreAbility ability : CoreAbility.getAbilities()) {
			String name = ability.getName();
			/*
			Map<String, AttributeCache> attributes = CoreAbility.getAttributeCache(ability);
			for (String attribute : attributes.keySet()) {
				String itemName = name + attribute;
				int benefit = 1;
				if (attribute.equals(Attribute.COOLDOWN))
					benefit = -1;
				ATTRIBUTES.add(new Attribute(itemName, attribute, ability.getElement(), benefit));
			}
			 */
			ATTRIBUTES.add(new PKIAttribute(name, "single ability", true));
		}
	}
}

package com.projectkorra.items.abilityupdater;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.items.Messages;
import com.projectkorra.items.ProjectKorraItems;
import com.projectkorra.items.attribute.PKIAttribute;
import com.projectkorra.items.customs.PKItem;
import com.projectkorra.items.utils.ItemUtils;
import com.projectkorra.projectkorra.util.ParticleEffect;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityUpdater implements Listener {
	private static final Map<UUID, Integer> lastCheck = new ConcurrentHashMap<>();
	/**
	 * If the player has an item with the stat "ParticleEffects" then we will
	 * parse the information and display a particle effect around the player.
	 * 
	 * @param player the player to update
	 */
	public static void updatePlayerParticles(Player player) {
		UUID uuid = player.getUniqueId();
		int lastTick = lastCheck.getOrDefault(uuid, -1);
		int currentTick = Bukkit.getCurrentTick();
		if (lastTick != -1 && currentTick == lastTick)
			return;
		lastCheck.put(uuid, currentTick);
		List<ItemStack> equipment = ItemUtils.getPlayerValidEquipment(player);
		for (ItemStack istack : equipment) {
			PKItem citem = PKItem.getCustomItem(istack);
			if (citem == null)
				continue;

			PKIAttribute attr = citem.getAttribute("ParticleEffects");
			if (attr == null)
				continue;

			List<Object> rawValues = attr.getValues();
			for (Object rawValue : rawValues) {
				String value = (String) rawValue;
				String[] colonSplit = value.split(":");
				if (colonSplit.length == 0)
					continue;

				String particleName = colonSplit[0];
				ParticleEffect effect;
				try {
					effect = ParticleEffect.valueOf(particleName.trim());
				} catch (IllegalArgumentException ex) {
					Messages.logTimedMessage(Messages.BAD_PARTICLE_EFFECT + ": " + particleName);
					continue;
				}

				double amount = 1;
				double radius = 100;
				double duration = 1;
				double speed = 0;
				try {
					if (colonSplit.length >= 2)
						amount = Double.parseDouble(colonSplit[1]);
					if (colonSplit.length >= 3)
						radius = Double.parseDouble(colonSplit[2]);
					if (colonSplit.length >= 4)
						duration = Double.parseDouble(colonSplit[3]);
					if (colonSplit.length >= 5)
						speed = Double.parseDouble(colonSplit[4]);
				}
				catch (NumberFormatException ignore) {}

				radius /= 100;
				speed /= 100;
				final ParticleEffect feffect = effect;
				final double fradius = radius;
				final double famount = amount;
				final double fspeed = speed;
				final Player fplayer = player;
				for (int i = 0; i < duration; i++) {
					Bukkit.getScheduler().runTaskLater(
							ProjectKorraItems.getInstance(),
							() -> feffect.display(
									fplayer.getEyeLocation(),
									(int) famount,
									(float) fradius,
									(float) fradius,
									(float) fradius,
									(float) fspeed),
							i);
				}
			}
		}
	}
}

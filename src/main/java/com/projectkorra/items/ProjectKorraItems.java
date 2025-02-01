package com.projectkorra.items;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.projectkorra.items.listeners.PKIListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.items.abilityupdater.AbilityUpdater;
import com.projectkorra.items.listeners.AttributeListener;
import com.projectkorra.items.command.BaseCommand;
import com.projectkorra.items.command.EquipCommand;
import com.projectkorra.items.command.GiveCommand;
import com.projectkorra.items.command.ListCommand;
import com.projectkorra.items.command.ReloadCommand;
import com.projectkorra.items.command.StatsCommand;
import com.projectkorra.items.customs.PKIDisplay;

public class ProjectKorraItems extends JavaPlugin {
	private static ProjectKorraItems plugin;
	private static Logger log;
	private AttributeListener attrListener;

	public static ProjectKorraItems getInstance() {
		return plugin;
	}

	public static void log(Level level, String record) {
		log.log(level, record);
	}

	public static void info(String record) {
		log.info(record);
	}

	public static void warning(String record) {
		log.warning(record);
	}

	public static void severe(String record) {
		log.severe(record);
	}

	@Override
	public void onEnable() {
		plugin = this;
		ProjectKorraItems.log = this.getLogger();
		
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has Been Enabled!");
		
		new BaseCommand();
		new EquipCommand();
		new GiveCommand();
		new ListCommand();
		new ReloadCommand();
		new StatsCommand();
		
		new ConfigManager();
		PKIDisplay.displays = new ConcurrentHashMap<>();
		
		PluginManager pm = this.getServer().getPluginManager();
		
		//
		
		pm.registerEvents(new PKIListener(), this);
		attrListener = new AttributeListener(this);
		pm.registerEvents(attrListener, this);
		pm.registerEvents(new AbilityUpdater(), this);

		// ArmorEquipEvent.registerListener(this);
		
		//

		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		attrListener.stop();
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " Has Been Disabled!");
		
		if (PKIDisplay.displays != null && !PKIDisplay.displays.isEmpty()) {
			PKIDisplay.cleanup();
		}
	}
}

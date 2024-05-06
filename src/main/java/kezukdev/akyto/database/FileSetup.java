package kezukdev.akyto.database;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;

import kezukdev.akyto.Practice;
import kezukdev.akyto.profile.kiteditor.Edited;
import kezukdev.akyto.utils.inventory.BukkitSerialization;

public class FileSetup {
	
	private final Practice main;
	
	public FileSetup(final Practice main) { 
		this.main = main;
	}
	
	public void loadEditor() {
		final long timeUnit = System.currentTimeMillis();
		final File dir = new File(this.main.getDataFolder() + "/players/");
		File[] files = dir.listFiles();
		if (files != null && dir.exists()) {
			for (File file : files) {
				YamlConfiguration configFile = YamlConfiguration.loadConfiguration(file);
				final String str = file.getName().replace(".yml", "");
				Map<String, Edited> mapLadder = new HashMap<>();
				for (String strs : configFile.getConfigurationSection("ladders").getKeys(false)) {
					try {
						mapLadder.putIfAbsent(strs, new Edited(strs, BukkitSerialization.itemStackArrayFromBase64(configFile.getString("ladders." + strs + ".content")), BukkitSerialization.itemStackArrayFromBase64(configFile.getString("ladders." + strs + ".armorContent"))));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				this.main.getManagerHandler().getProfileManager().getEditor().putIfAbsent(UUID.fromString(str), mapLadder);
			}	
			long endTime = System.currentTimeMillis();
			System.out.println("[AkytoPractice] Edited-Kit: Succesfuly loaded in " + (endTime - timeUnit) + "ms!");
		}
	}

	public void saveEditor() {
		if (!this.main.getManagerHandler().getProfileManager().getEditor().isEmpty()) {
			final long startTime = System.currentTimeMillis();
			for (UUID uuid : this.main.getManagerHandler().getProfileManager().getEditor().keySet()) {
				File file = new File(this.main.getDataFolder() + "/players/" + uuid.toString() + ".yml");
				if (!file.exists()) {
					try {
		                file.getParentFile().mkdirs();
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				YamlConfiguration configFile = YamlConfiguration.loadConfiguration(file);
				for (Entry<String, Edited> str : this.main.getManagerHandler().getProfileManager().getEditor().get(uuid).entrySet()) {
					configFile.createSection("ladders." + str.getKey());
					configFile.createSection("ladders." + str.getKey() + ".content");
					configFile.createSection("ladders." + str.getKey() + ".armorContent");
					configFile.set("ladders." + str.getKey() + ".content", BukkitSerialization.itemStackArrayToBase64(str.getValue().getContent()));
					configFile.set("ladders." + str.getKey() + ".armorContent", BukkitSerialization.itemStackArrayToBase64(str.getValue().getArmorContent()));
				}
				try {
					configFile.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
			final long endTime = System.currentTimeMillis();
			System.out.println("[AkytoPractice] Edited-Kit: Succesfully saved in: " + (endTime - startTime) + "ms!");
		}
	}

}

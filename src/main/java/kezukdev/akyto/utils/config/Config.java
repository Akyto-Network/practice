package kezukdev.akyto.utils.config;

import lombok.Getter;
import org.bukkit.plugin.java.*;
import java.io.*;
import org.bukkit.configuration.file.*;

@Getter
public class Config
{
    private final FileConfiguration config;
    private final File configFile;
    protected boolean wasCreated;
    
    public Config(final String name, final JavaPlugin plugin) {
        this.configFile = new File(plugin.getDataFolder() + "/" + name + ".yml");
        if (!this.configFile.exists()) {
            try {
                this.configFile.getParentFile().mkdirs();
                this.configFile.createNewFile();
                this.wasCreated = true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }
    
    public void save() {
        try {
            this.config.save(this.configFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}

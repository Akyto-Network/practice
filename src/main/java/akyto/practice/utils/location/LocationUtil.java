package akyto.practice.utils.location;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import akyto.practice.Practice;
import lombok.Getter;
import lombok.Setter;

public @Getter @Setter class LocationUtil {

    @Getter static HashMap<String, LocationUtil> all = new HashMap<>();

    private String name;
    private Location location;

    public LocationUtil(String name, Location location) {
        this.name = name;
        this.location = location;

        all.put(name, this);
    }

    public LocationUtil(String name) {
        this(name, null);
    }

    public void save(final Practice main) {
        if (location == null) return;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(location.getWorld().getName());
        stringBuilder.append(":");
        stringBuilder.append(location.getX());
        stringBuilder.append(":");
        stringBuilder.append(location.getY());
        stringBuilder.append(":");
        stringBuilder.append(location.getZ());
        stringBuilder.append(":");
        stringBuilder.append(location.getYaw());
        stringBuilder.append(":");
        stringBuilder.append(location.getPitch());
        if (stringBuilder.toString().equals(main.locationConfig.get("locations." + name))) return;
        main.locationConfig.set("locations." + name, stringBuilder.toString());
    }

    public boolean load(final Practice main) {
        if (main.locationConfig.get("locations." + name) == null) return false;
        String[] part = main.locationConfig.getString("locations." + name).split(":");
        this.location = new Location(Bukkit.getWorld(part[0]), Double.parseDouble(part[1]), Double.parseDouble(part[2]), Double.parseDouble(part[3]), Float.parseFloat(part[4]), Float.parseFloat(part[5]));
        return true;
    }

    public static LocationUtil getLocationHelper(String name) {
        return all.get(name);
    }
}
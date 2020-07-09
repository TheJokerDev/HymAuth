package me.TheJokerDev.HymAuth;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
@SuppressWarnings("all")
public class LocationManager {
    private YamlConfiguration locs;
    private File file;

    public LocationManager(File var1) {
        if (!var1.exists()) {
            try {
                var1.createNewFile();
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

        this.file = var1;
        this.locs = YamlConfiguration.loadConfiguration(this.file);
    }

    public void saveLoc(Location var1, String var2, boolean var3) {
        this.locs.set(var2, this.locToString(var1, var3));
    }

    public void saveLocs(Location var1, String var2, boolean var3) {
        Object var4 = new ArrayList();
        if (this.locs.getStringList(var2) != null) {
            var4 = this.locs.getStringList(var2);
        }

        ((List)var4).add(this.locToString(var1, var3));
        this.locs.set(var2, var4);
    }

    public Location getLoc(String var1) {
        return this.locFromString(this.locs.getString(var1));
    }

    public List<Location> getLocs(String var1) {
        if (this.locs.getStringList(var1) == null) {
            return null;
        } else {
            ArrayList var2 = new ArrayList();
            Iterator var3 = this.locs.getStringList(var1).iterator();

            while(var3.hasNext()) {
                String var4 = (String)var3.next();
                var2.add(this.locFromString(var4));
            }

            return var2;
        }
    }

    public void save() {
        try {
            this.locs.save(this.file);
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    private String locToString(Location var1, boolean var2) {
        return var1.getWorld().getName() + "#" + var1.getX() + "#" + var1.getY() + "#" + var1.getZ() + (var2 ? "#" + var1.getYaw() + "#" + var1.getPitch() : "");
    }

    private Location locFromString(String var1) {
        if (var1 == null) {
            return null;
        } else {
            String[] var2 = var1.split("#");
            if (var2.length == 4) {
                return new Location(Bukkit.getWorld(var2[0]), Double.valueOf(var2[1]), Double.valueOf(var2[2]), Double.valueOf(var2[3]));
            } else {
                return var2.length == 6 ? new Location(Bukkit.getWorld(var2[0]), Double.valueOf(var2[1]), Double.valueOf(var2[2]), Double.valueOf(var2[3]), Float.valueOf(var2[4]), Float.valueOf(var2[5])) : null;
            }
        }
    }
}

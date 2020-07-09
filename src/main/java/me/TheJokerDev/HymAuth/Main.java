package me.TheJokerDev.HymAuth;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.TheJokerDev.HymAuth.utils.Test;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
@SuppressWarnings("all")
public class Main extends JavaPlugin implements Listener {
    private static Main instance = null;
    public Test vInterface;
    String ver = "";
    File confFile = new File(this.getDataFolder().getPath(), "config.yml");
    public FileConfiguration conf;
    File codeFile = new File(this.getDataFolder().getPath(), "codes.yml");
    YamlConfiguration fCodes;
    LocationManager lm;
    boolean joinLoc;
    boolean pinCommand;
    static String homeServer = "none";
    public Map<String, String> ecode;
    public Map<String, String> sCodes;
    public static List<String> bpCommands;
    public static String INV_TITLE = "§c§lLogueo§8: Introduce el PIN";
    Map<String, String> ips;
    public List<String> logingin;
    public int cLength;

    public Main() {
        this.fCodes = YamlConfiguration.loadConfiguration(this.codeFile);
        this.joinLoc = false;
        this.pinCommand = false;
        this.ecode = new HashMap();
        this.sCodes = new HashMap();
        this.ips = new HashMap();
        this.logingin = new ArrayList();
        this.cLength = 4;
    }

    public static Main get() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        if (!this.confFile.exists()) {
            this.saveResource("config.yml", true);
        }
        registerVersion();
        conf = YamlConfiguration.loadConfiguration(this.confFile);
        lm = new LocationManager(new File(this.getDataFolder(), "loc.yml"));
        this.joinLoc = this.conf.getBoolean("HideLocation", false);
        this.pinCommand = this.conf.getBoolean("EnablePinCommand", false);
        homeServer = this.conf.getString("LobbyServer", "none");
        bpCommands = this.conf.getStringList("BypassCommands");
        Iterator var4;
        if (conf.getBoolean("MySQL.UseMySQL", false)) {
            System.out.println("Conectando a la MySQL");
            if (!MySQLManager.get().registerMySQL(this.conf.getString("MySQL.IP", "Unknown"), this.conf.getString("MySQL.DB-Name", "Unknown"), this.conf.getString("MySQL.Username", "Unknown"), this.conf.getString("MySQL.Password", "Unknown"))) {
                System.out.println("Se necesita MySQL! Desactivando plugin :(");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            try {
                MySQLManager.get().checkDB("PinCodes", "CREATE TABLE `PinCodes` (`id` int NOT NULL AUTO_INCREMENT,`uuid` TEXT NULL DEFAULT NULL,`pin` TEXT NULL DEFAULT NULL,PRIMARY KEY (id))");
                ResultSet var1 = MySQLManager.mConn.prepareStatement("SELECT * FROM `PinCodes`").executeQuery();

                while(var1.next()) {
                    this.sCodes.put(var1.getString("uuid"), var1.getString("pin"));
                }
            } catch (SQLException var3) {
                var3.printStackTrace();
            }

            System.out.println("Hecho!");
        } else {
            System.out.println("Activando archivos de sistema!");
            var4 = get().fCodes.getKeys(false).iterator();

            while(var4.hasNext()) {
                String var2 = (String)var4.next();
                this.sCodes.put(var2, this.fCodes.getString(var2));
            }
        }

        this.cLength = this.conf.getInt("PIN-Length", 4);
        System.out.println("Verificando PINs!");
        var4 = this.sCodes.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry var5 = (Map.Entry)var4.next();
            if (((String)var5.getValue()).length() != this.cLength) {
                this.deletePin((String)var5.getKey());
                System.out.println("El PIN de " + var5.getKey() + " fué invalido y por eso será removido");
            }
        }

        System.out.println("Hecho!");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getPluginManager().registerEvents(Events.get(), this);
        this.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public void run() {
                try {
                    if (conf.getBoolean("MySQL.UseMySQL", false)) {
                        ResultSet var1 = MySQLManager.mConn.prepareStatement("SELECT * FROM `PinCodes`").executeQuery();
                        if (var1 != null) {
                            while(var1.next()) {
                                sCodes.put(var1.getString("uuid"), var1.getString("pin"));
                            }
                        } else {
                            System.out.println("PRECAUCIÓn! Fallo en guardar los PINs en MySQL");
                        }
                    }
                } catch (SQLException var2) {
                    var2.printStackTrace();
                }

            }
        }, (long)(1200 * this.conf.getInt("AutoLoad", 5)), (long)(1200 * this.conf.getInt("AutoLoad", 5)));
        var4 = Bukkit.getOnlinePlayers().iterator();

        while(var4.hasNext()) {
            Player var6 = (Player)var4.next();
            Events.get().onJoin(new PlayerJoinEvent(var6, (String)null));
        }

    }
    public FileConfiguration getConf(){
        return this.conf;
    }

    public void onDisable() {
        super.onDisable();
    }

    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        if (var2.getName().equalsIgnoreCase("hymauth")) {
            if (var4.length == 1) {
                if (var4[0].equalsIgnoreCase("reload")) {
                    if (var1.hasPermission("al.admin.reload")) {
                        this.conf = YamlConfiguration.loadConfiguration(this.confFile);
                        var1.sendMessage(this.getMSG(3, "ConfReload"));
                    } else {
                        var1.sendMessage(this.getMSG(3, "NoPerms"));
                    }
                    return true;
                }
                return true;
            }
            return true;
        } else if (var2.getName().equalsIgnoreCase("setjoinloc")) {
            if (!(var1 instanceof Player)) {
                var1.sendMessage("Este comando es solo para jugadores!");
                return true;
            } else {
                if (this.joinLoc) {
                    if (var1.hasPermission("hymauth.admin.setloc")) {
                        this.lm.saveLoc(((Player)var1).getLocation(), "HideLocation", true);
                        this.lm.save();
                        var1.sendMessage("§aUbicación oculta guardada");
                    } else {
                        var1.sendMessage(this.getMSG(3, "NoPerms"));
                    }
                } else {
                    var1.sendMessage("§cNecesitas activar 'HideLocation' en la config para poder usar esto!");
                }

                return true;
            }
        } else if (var2.getName().equalsIgnoreCase("pin")) {
            if (this.pinCommand && this.logingin.contains(var1.getName().toLowerCase())) {
                if (!(var1 instanceof Player)) {
                    var1.sendMessage("Este comando es solo para jugadores!");
                    return true;
                }

                if (var4.length == 1) {
                    if (var4[0].equals(this.sCodes.get(var1.getName().toLowerCase()))) {
                        Events.get().login((Player)var1);
                        var1.sendMessage(this.getMSG(1, "Loggedin.Line1"));
                    } else {
                        var1.sendMessage(this.getMSG(1, "WrongPIN.Line1"));
                    }
                }
            } else {
                var1.sendMessage("§cDesactivado.");
            }

            return true;
        } else if (var2.getName().equalsIgnoreCase("ResetPIN")) {
            if (var4.length == 0) {
                if (!(var1 instanceof Player)) {
                    var1.sendMessage("Este comando es solo para jugadores!");
                    var1.sendMessage("Usa >> /resetpin NICK");
                    return true;
                }
                this.deletePin(var1.getName().toLowerCase());
                this.unregisterIP(var1.getName().toLowerCase());
                this.preLoginScreen((Player)var1, false);
                new Test().getLoginScreen((Player)var1);
            } else if (var1.hasPermission("hymauth.admin.resetpin")) {
                if (this.sCodes.containsKey(var4[0].toLowerCase())) {
                    this.deletePin(var4[0].toLowerCase());
                    this.unregisterIP(var4[0].toLowerCase());
                    var1.sendMessage(this.getMSG(3, "AdminPINReset.Admin").replace("%PLAYER%", var4[0]));
                    if (Bukkit.getOfflinePlayer(var4[0]).isOnline()) {
                        Bukkit.getPlayer(var4[0]).kickPlayer(this.getMSG(3, "AdminPINReset.User"));
                    }
                } else {
                    var1.sendMessage(this.getMSG(3, "AdminPINReset.HasNoPin"));
                }
            } else {
                var1.sendMessage(this.getMSG(3, "NoPerms"));
            }

            return true;
        } else if (!var2.getName().equalsIgnoreCase("setpin")) {
            return false;
        } else {
            if (var4.length != 0 && var4.length != 1) {
                if (var1.hasPermission("hymauth.admin.setpin")) {
                    try {
                        Integer.valueOf(var4[1]);
                        if (var4[1].length() != Events.get().cLength) {
                            Integer.valueOf("NotAnInt:D");
                        }
                    } catch (Exception var7) {
                        var1.sendMessage(this.getMSG(3, "AdminPINSet.NoValidPIN"));
                        return true;
                    }

                    if (this.sCodes.containsKey(var4[0].toLowerCase())) {
                        this.deletePin(var4[0].toLowerCase());
                    }

                    this.savePin(var4[0].toLowerCase(), var4[1]);
                    var1.sendMessage(this.getMSG(3, "AdminPINSet.Admin").replace("%PLAYER%", var4[0]).replace("%PIN%", var4[1]));
                    this.unregisterIP(var4[0]);
                    if (Bukkit.getOfflinePlayer(var4[0]).isOnline()) {
                        Bukkit.getPlayer(var4[0]).kickPlayer(this.getMSG(3, "AdminPINSet.User"));
                    }
                } else {
                    var1.sendMessage(this.getMSG(3, "NoPerms"));
                }
            } else {
                var1.sendMessage("§cUso§8:§7 /setpin [NICK] [PIN]");
            }

            return true;
        }
    }

    public void saveCode() {
        try {
            this.fCodes.save(this.codeFile);
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public void unregisterIP(String var1) {
        var1 = var1.toLowerCase();
        if (this.ips.containsKey(var1)) {
            this.ips.remove(var1);
        }

    }

    public boolean registerVersion() {
        String var1 = this.getServer().getClass().getPackage().getName();
        String var2 = var1.substring(var1.lastIndexOf(46) + 1);
        this.ver = var2;

        try {
            return true;
        } catch (Exception var4) {
            return false;
        }
    }

    public Sound getSound(int var1) {
        if (var1 == 1) {
            return this.ver.split("_")[1].equals("8") ? Sound.valueOf("CHICKEN_EGG_POP") : Sound.valueOf("ENTITY_CHICKEN_EGG");
        } else if (var1 == 2) {
            return this.ver.split("_")[1].equals("8") ? Sound.valueOf("LEVEL_UP") : Sound.valueOf("ENTITY_PLAYER_LEVELUP");
        } else if (var1 == 3) {
            return this.ver.split("_")[1].equals("8") ? Sound.valueOf("BAT_HURT") : Sound.valueOf("ENTITY_BAT_HURT");
        } else {
            return null;
        }
    }

    public void deletePin(String var1) {
        if (this.conf.getBoolean("MySQL.UseMySQL", false)) {
            try {
                ResultSet var2 = MySQLManager.mConn.prepareStatement("SELECT * FROM `PinCodes` WHERE `uuid` = '" + var1 + "'").executeQuery();
                if (var2.next()) {
                    MySQLManager.mConn.prepareStatement("DELETE FROM `PinCodes` WHERE `uuid` = '" + var1 + "'").execute();
                }
            } catch (SQLException var3) {
                var3.printStackTrace();
            }
        } else {
            this.fCodes.set(var1, (Object)null);
            this.saveCode();
        }

        if (this.sCodes.containsKey(var1)) {
            this.sCodes.remove(var1);
        }

    }

    public void savePin(String var1, String var2) {
        if (this.conf.getBoolean("MySQL.UseMySQL", false)) {
            try {
                ResultSet var3 = MySQLManager.mConn.prepareStatement("SELECT * FROM `PinCodes` WHERE `uuid` = '" + var1 + "'").executeQuery();
                if (!var3.next()) {
                    MySQLManager.mConn.prepareStatement("INSERT INTO `PinCodes` (`uuid`, `pin`) VALUES ('" + var1 + "','" + var2 + "')").execute();
                }
            } catch (SQLException var4) {
                var4.printStackTrace();
            }
        } else {
            this.fCodes.set(var1, var2);
            this.saveCode();
        }

        this.sCodes.put(var1, var2);
    }

    public void preLoginScreen(Player var1, boolean var2) {
        PlayerInventory var3 = var1.getInventory();
        Inventory var4 = Bukkit.createInventory((InventoryHolder)null, 36);

        for(Integer var5 = 0; var5 != 36; var5 = var5 + 1) {
            ItemStack var6 = var3.getItem(var5);
            if (var6 == null) {
                var4.setItem(var5, (ItemStack)null);
            } else {
                var4.setItem(var5, var6.clone());
            }

            var3.clear(var5);
        }

        if (!Events.get().pInvs.containsKey(var1.getName().toLowerCase())) {
            Events.get().pInvs.put(var1.getName().toLowerCase(), var4);
        }

        if (var2) {
            Location var9 = this.lm.getLoc("HideLocation");
            if (this.joinLoc && var9 != null) {
                if (!Events.get().pLocs.containsKey(var1.getName())) { 
                    Events.get().pLocs.put(var1.getName(), var1.getLocation().clone());
                }

                var1.teleport(var9);
            }
        }

        if (this.conf.getBoolean("SpectatorOnLogin", false)) {
            if (!Events.get().pGM.containsKey(var1.getName())) {
                Events.get().pGM.put(var1.getName(), var1.getGameMode());
            }

            var1.setGameMode(GameMode.SPECTATOR);
        }

        var1.setWalkSpeed(0.0F);
    }

    public String getMSG(Integer var1, String var2) {
        if (var1 == 1) {
            var2 = "Messages-Title." + var2;
        } else if (var1 == 2) {
            var2 = "Messages-Items." + var2;
        } else {
            var2 = "Messages-Chat." + var2;
        }

        String var3 = null;

        try {
            var3 = this.conf.getString(var2);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        if (var3 == null) {
            System.out.println("\n \n--===[HymAuth-ERROR]===--\nTipo de Error: Config-Error\nError: No se pudo encontrar el archivo bajo la dirección: " + var2.replace('.', '>') + "\nLo reporto?: No" + "\nEscríbele a TheJokerDev#9223" + "\n--============================--\n ");
            return "§4ERROR! Mira la consola para el LogERROR";
        } else {
            return var3.replace('&', '§');
        }
    }

    public static boolean isAllowed(String var0) {
        if (bpCommands != null) {
            Iterator var1 = bpCommands.iterator();

            while(var1.hasNext()) {
                String var2 = (String)var1.next();
                if (var0.startsWith(var2)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void sendToServer(Player var0) {
        ByteArrayDataOutput var1 = ByteStreams.newDataOutput();
        var1.writeUTF("hymauth");
        var1.writeUTF("grantAccess");
        var1.writeUTF(var0.getName());
        var0.sendPluginMessage(get(), "BungeeCord", var1.toByteArray());
        if (!homeServer.equals("none")) {
            ByteArrayDataOutput var2 = ByteStreams.newDataOutput();
            var2.writeUTF("Connect");
            var2.writeUTF(homeServer);
            var0.sendPluginMessage(get(), "BungeeCord", var2.toByteArray());
        }
    }
}

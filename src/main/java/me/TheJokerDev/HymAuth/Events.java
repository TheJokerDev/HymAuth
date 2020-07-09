package me.TheJokerDev.HymAuth;

import me.TheJokerDev.HymAuth.utils.Test;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
@SuppressWarnings("all")
public class Events implements Listener {
    private static Events instance = null;
    private Main pl = Main.get();
    public Map<String, Inventory> pInvs = new HashMap();
    public Map<String, Location> pLocs = new HashMap();
    public Map<String, GameMode> pGM = new HashMap();
    public Map<String, Integer> tries = new HashMap();
    public Map<String, Integer> loginID = new HashMap();
    public int cLength = 6;

    public static Events get() {
        if (instance == null) {
            instance = new Events();
        }

        return instance;
    }

    public Events() {
        this.cLength = pl.conf.getInt("PIN-Length", 4);
    }

    private void showPin(Player var1) {
        if (this.pl.ecode.containsKey(var1.getName().toLowerCase())) {
            String var2 = (String)this.pl.ecode.get(var1.getName().toLowerCase());
            Object var3 = null;
            Object var4 = null;
            Object var5 = null;

            for(int var6 = var2.length() - 1; var6 >= 0; --var6) {
                int var7 = Integer.valueOf(String.valueOf(var2.charAt(var6)));
                var1.getInventory().setItem(21 + var6, this.getItem(Material.STAINED_GLASS_PANE, var7, 14, "§8§l" + var7));
            }
        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent var1) {
        if (Main.INV_TITLE.equals(var1.getView().getTitle()) || this.isNewVersion(var1.getInventory())) {
            Player var2;
            if (var1.getRawSlot() == 0) {
                var1.getWhoClicked().setItemOnCursor((ItemStack)null);
                var2 = (Player)var1.getWhoClicked();
                var2.updateInventory();
                this.showPin(var2);
            }

            var1.setCancelled(true);
            if (var1.getCurrentItem() != null && var1.getCurrentItem().hasItemMeta()) {
                var2 = (Player)var1.getWhoClicked();
                String var3 = var1.getCurrentItem().getItemMeta().getDisplayName();
                if (var3.startsWith(Main.get().getMSG(2, "ShowPIN"))) {
                    this.showPin(var2);
                }

                if (var3.startsWith("§c§o§n§r")) {
                    if (Main.get().conf.getBoolean("Sounds")) {
                        var2.playSound(var2.getLocation(), Main.get().getSound(1), 1.0F, 1.0F);
                    }

                    if (this.pl.ecode.containsKey(var2.getName().toLowerCase())) {
                        String var4 = (String)this.pl.ecode.get(var2.getName().toLowerCase());
                        Integer var5 = var4.length();
                        Integer var6 = 0;
                        var4 = var4 + String.valueOf(var1.getCurrentItem().getAmount());
                        this.pl.ecode.remove(var2.getName().toLowerCase());
                        this.pl.ecode.put(var2.getName().toLowerCase(), var4);
                        if (var5 < this.cLength - 1) {
                            var2.getInventory().setItem(21 + var5, this.getItem(Material.BARRIER, 1, 0, Main.get().getMSG(2, "HiddenNumber")));
                        } else if (this.pl.sCodes.containsKey(var2.getName().toLowerCase())) {
                            if (var4.equals(this.pl.sCodes.get(var2.getName().toLowerCase()))) {
                                this.login(var2);
                            } else {
                                this.pl.ecode.remove(var2.getName().toLowerCase());
                                var2.closeInventory();
                                new Test().sendTitle(var2, Main.get().getMSG(1, "WrongPIN.Line1"), Main.get().getMSG(1, "WrongPIN.Line2"), 10, Main.get().conf.getInt("WrongPIN.Stay", 70), 10);
                                if (this.pl.conf.getBoolean("Sounds")) {
                                    var2.playSound(var2.getLocation(), Main.get().getSound(3), 1.0F, 1.0F);
                                }

                                Integer var7 = 1;
                                if (this.tries.containsKey(var2.getName().toLowerCase())) {
                                    var7 = (Integer)this.tries.get(var2.getName().toLowerCase()) + 1;
                                }

                                this.tries.remove(var2.getName().toLowerCase());
                                this.tries.put(var2.getName().toLowerCase(), var7);
                                if (this.pl.conf.getInt("KickAfterTries.Tries", -1) != -1 && this.pl.conf.getInt("KickAfterTries.Tries", -1) == var7) {
                                    this.tries.remove(var2.getName().toLowerCase());
                                    var2.kickPlayer(this.pl.conf.getString("KickAfterTries.KickMessage").replace('&', '§'));
                                } else if (this.pl.conf.getInt("CommandAfterTries.Tries", -1) != -1 && this.pl.conf.getInt("CommandAfterTries.Tries", -1) == var7) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), this.pl.conf.getString("CommandAfterTries.Command").replaceAll("%PLAYER%", var2.getName()).replaceAll("%IP%", var2.getAddress().toString().split("/")[var2.getAddress().toString().split("/").length - 1].split(":")[0]));
                                }
                            }
                        } else {
                            this.pl.savePin(var2.getName().toLowerCase(), var4);
                            this.pl.logingin.remove(var2.getName().toLowerCase());
                            var2.closeInventory();
                            new Test().sendTitle(var2, Main.get().getMSG(1, "Registered.Line1"), Main.get().getMSG(1, "Registered.Line2").replace("%PIN%", var4), 20, Main.get().conf.getInt("Registered.Stay", 70), 20);
                            this.pl.saveCode();
                            this.resetInv(var2);
                            var2.setWalkSpeed(0.2F);
                            Main.sendToServer(var2);
                        }
                    } else {
                        var2.getInventory().setItem(21, this.getItem(Material.BARRIER, 1, 0, Main.get().getMSG(2, "HiddenNumber")));
                        this.pl.ecode.put(var2.getName().toLowerCase(), String.valueOf(var1.getCurrentItem().getAmount()));
                    }
                }
            }
        }

    }

    private boolean isNewVersion(Inventory var1) {
        ItemStack var2 = var1.getItem(0);
        return var2 != null && var2.getType() == Material.NAME_TAG && var2.hasItemMeta() && Main.get().getMSG(2, "ShowPIN").equals(var2.getItemMeta().getDisplayName());
    }

    @EventHandler
    public void onClose(final InventoryCloseEvent var1) {
        if (Main.get().logingin.contains(var1.getPlayer().getName().toLowerCase())) {
            var1.getInventory().clear();
            if (this.pl.ecode.containsKey(var1.getPlayer().getName().toLowerCase())) {
                this.pl.ecode.remove(var1.getPlayer().getName().toLowerCase());
            }

            final String var2 = var1.getPlayer().getName().toLowerCase();
            if (this.pl.logingin.contains(var1.getPlayer().getName().toLowerCase())) {
                Bukkit.getScheduler().scheduleAsyncDelayedTask(this.pl, new Runnable() {
                    Player p = (Player)var1.getPlayer();

                    public void run() {
                        if (this.p.isOnline() && Events.this.pl.logingin.contains(var2)) {
                            new Test().getLoginScreen(this.p);
                        }

                    }
                }, 20L);
            }
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent var1) {
        String var2 = var1.getPlayer().getName().toLowerCase();
        if (this.loginID.containsKey(var2)) {
            Bukkit.getScheduler().cancelTask((Integer)this.loginID.get(var2));
            this.loginID.remove(var2);
        }

        this.resetInv(var1.getPlayer());
        if (this.pl.ecode.containsKey(var1.getPlayer().getName().toLowerCase())) {
            this.pl.ecode.remove(var1.getPlayer().getName().toLowerCase());
        }

        var1.getPlayer().setWalkSpeed(0.2F);
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onJoin(final PlayerJoinEvent var1) {
        if (Main.get().conf.getBoolean("PermsLogin", false) && !var1.getPlayer().hasPermission("al.login")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.get(), new Runnable() {
                public void run() {
                    Main.sendToServer(var1.getPlayer());
                }
            }, 30L);
        } else {
            try {
                if (var1.getPlayer().isDead()) {
                    var1.getPlayer().spigot().respawn();
                }
            } catch (Exception var6) {
            }

            boolean var2 = true;
            boolean var3 = true;
            int var4 = 10;
            if (!this.pl.sCodes.containsKey(var1.getPlayer().getName().toLowerCase())) {
                new Test().sendTitle(var1.getPlayer(), Main.get().getMSG(1, "Register.Line1"), Main.get().getMSG(1, "Register.Line2"), 5, Main.get().conf.getInt("Register.Stay", 70), 5);
                var4 = Main.get().conf.getInt("Register.Stay", 70);
                var3 = false;
            }

            if (this.pl.ips.containsKey(var1.getPlayer().getName().toLowerCase()) && this.pl.conf.getBoolean("SaveIP") && var1.getPlayer().getAddress().getHostString().toString().equals(this.pl.ips.get(var1.getPlayer().getName().toLowerCase()))) {
                new Test().sendTitle(var1.getPlayer(), Main.get().getMSG(1, "AutoLoggedin.Line1"), Main.get().getMSG(1, "AutoLoggedin.Line2"), 5, Main.get().conf.getInt("AutoLoggedin.Stay", 70), 5);
                var2 = false;
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.get(), new Runnable() {
                    public void run() {
                        Main.sendToServer(var1.getPlayer());
                    }
                }, 30L);
            }

            if (var2) {
                if (Main.get().pinCommand) {
                    var1.getPlayer().sendMessage(Main.get().getMSG(3, "PinCommand"));
                }

                final String var5 = var1.getPlayer().getName().toLowerCase();
                if (this.pl.conf.getInt("KickAfterSec.Seconds") != -1) {
                    this.loginID.put(var5, Bukkit.getScheduler().scheduleSyncDelayedTask(this.pl, new Runnable() {
                        public void run() {
                            if (Events.this.loginID.containsKey(var5)) {
                                Events.this.loginID.remove(var5);
                            }

                            if (Bukkit.getOfflinePlayer(var5).isOnline() && Events.this.pl.logingin.contains(var5)) {
                                Bukkit.getPlayer(var5).kickPlayer(Events.this.pl.conf.getString("KickAfterSec.KickMessage").replace('&', '§'));
                            }

                        }
                    }, (long)(20 * this.pl.conf.getInt("KickAfterSec.Seconds"))));
                }

                Main.get().preLoginScreen(var1.getPlayer(), false);
                if (!this.pl.logingin.contains(var1.getPlayer().getName().toLowerCase())) {
                    this.pl.logingin.add(var1.getPlayer().getName().toLowerCase());
                }

                Bukkit.getScheduler().scheduleAsyncDelayedTask(this.pl, new Runnable() {
                    Player p = var1.getPlayer();

                    public void run() {
                        if (Events.this.pl.logingin.contains(this.p.getName().toLowerCase())) {
                            new Test().getLoginScreen(this.p);
                        }

                    }
                }, (long)var4);
            }

        }
    }

    public ItemStack getItem(Material var1, Integer var2, int var3, String var4) {
        ItemStack var5 = new ItemStack(var1, var2, (short)var3);
        ItemMeta var6 = var5.getItemMeta();
        var6.setDisplayName(var4);
        var5.setItemMeta(var6);
        return var5;
    }

    public void resetInv(Player var1) {
        if (this.pGM.containsKey(var1.getName())) {
            var1.setGameMode((GameMode)this.pGM.get(var1.getName()));
            this.pGM.remove(var1.getName());
        }

        if (this.pInvs.containsKey(var1.getName().toLowerCase())) {
            Inventory var2 = (Inventory)this.pInvs.get(var1.getName().toLowerCase());

            for(Integer var3 = 0; var3 != 36; var3 = var3 + 1) {
                ItemStack var4 = var2.getItem(var3);
                if (var4 == null) {
                    var1.getInventory().setItem(var3, (ItemStack)null);
                } else {
                    var1.getInventory().setItem(var3, var4.clone());
                }
            }

            this.pInvs.remove(var1.getName().toLowerCase());
        }

        if (this.pLocs.containsKey(var1.getName())) {
            var1.teleport((Location)this.pLocs.get(var1.getName()));
            this.pLocs.remove(var1.getName());
        }

        var1.updateInventory();
    }

    public void login(Player var1) {
        this.pl.ecode.remove(var1.getName().toLowerCase());
        this.pl.logingin.remove(var1.getName().toLowerCase());
        var1.closeInventory();
        new Test().sendTitle(var1, Main.get().getMSG(1, "Loggedin.Line1"), Main.get().getMSG(1, "Loggedin.Line2"), 10, Main.get().conf.getInt("Loggedin.Stay", 70), 10);
        var1.setWalkSpeed(0.2F);
        if (this.pl.ips.containsKey(var1.getName().toLowerCase())) {
            this.pl.ips.remove(var1.getName().toLowerCase());
        }

        this.resetInv(var1);
        this.pl.ips.put(var1.getName().toLowerCase(), var1.getAddress().getHostString().toString());
        final String var2 = var1.getName().toLowerCase();
        if (this.pl.conf.getInt("SaveDuration", -1) != -1) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.pl, new Runnable() {
                public void run() {
                    if (Events.this.pl.ips.containsKey(var2)) {
                        Events.this.pl.ips.remove(var2);
                    }

                }
            }, (long)(1200 * this.pl.conf.getInt("SaveDuration")));
        }

        if (this.pl.conf.getBoolean("Sounds")) {
            var1.playSound(var1.getLocation(), Main.get().getSound(2), 1.0F, 1.0F);
        }

        Main.sendToServer(var1);
    }

    @EventHandler
    public void onPreLogin(PlayerPreLoginEvent var1) {
        if (var1.getName().matches("[a-zA-Z_0-9]*") && var1.getName().length() <= 16) {
            Iterator var2 = Bukkit.getOnlinePlayers().iterator();

            while(var2.hasNext()) {
                Player var3 = (Player)var2.next();
                if (var3.getName().equalsIgnoreCase(var1.getName())) {
                    var1.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§c¡Un usuario con tu nombre ya está en el servidor!");
                    break;
                }
            }

        } else {
            var1.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cTu nombre no es válido!\n \n §cRequisitos: \n§cEntre: [a-zA-Z_0-9]* \n§cCaractéres: 16");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent var1) {
        Player var2 = var1.getPlayer();
        if (this.pl.logingin.contains(var2.getName().toLowerCase())) {
            Material var3 = var1.getFrom().getBlock().getType();
            Location var4;
            if (var3 != Material.WATER && var3 != Material.STATIONARY_WATER) {
                if (var1.getTo().getX() != var1.getFrom().getX()) {
                    var4 = var1.getTo();
                    var4.setX(var1.getFrom().getX());
                    var2.teleport(var4);
                }

                if (var1.getTo().getZ() != var1.getFrom().getZ()) {
                    var4 = var1.getTo();
                    var4.setZ(var1.getFrom().getZ());
                    var2.teleport(var4);
                }
            }

            if (var2.getGameMode() == GameMode.SPECTATOR && var1.getTo().getY() != var1.getFrom().getY()) {
                var4 = var1.getTo();
                var4.setY(var1.getFrom().getY());
                var2.teleport(var4);
            }
        }

    }

    @EventHandler
    public void onChat(PlayerChatEvent var1) {
        if (this.pl.logingin.contains(var1.getPlayer().getName().toLowerCase())) {
            var1.setCancelled(true);
        }

    }

    @EventHandler
    public void onIPickUp(PlayerPickupItemEvent var1) {
        if (this.pl.logingin.contains(var1.getPlayer().getName().toLowerCase())) {
            var1.setCancelled(true);
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent var1) {
        if (this.pl.logingin.contains(var1.getPlayer().getName().toLowerCase())) {
            var1.setCancelled(true);
        }

    }

    @EventHandler
    public void onCommand(final PlayerCommandPreprocessEvent var1) {
        if (this.pl.logingin.contains(var1.getPlayer().getName().toLowerCase())) {
            if (var1.getMessage().startsWith("/pin") || Main.isAllowed(var1.getMessage())) {
                Bukkit.getScheduler().scheduleAsyncDelayedTask(this.pl, new Runnable() {
                    Player p = var1.getPlayer();

                    public void run() {
                        if (Events.this.pl.logingin.contains(this.p.getName().toLowerCase())) {
                            Main.get().vInterface.getLoginScreen(this.p);
                        }

                    }
                }, 5L);
                return;
            }

            var1.setCancelled(true);
        }

    }

    @EventHandler
    public void onDamage(EntityDamageEvent var1) {
        if (var1.getEntity() instanceof Player && this.pl.logingin.contains(var1.getEntity().getName().toLowerCase())) {
            var1.setCancelled(true);
        }

    }
}

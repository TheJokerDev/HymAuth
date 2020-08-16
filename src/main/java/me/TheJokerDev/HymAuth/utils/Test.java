package me.TheJokerDev.HymAuth.utils;

import me.TheJokerDev.HymAuth.Events;
import me.TheJokerDev.HymAuth.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;

public class Test {

    /**public void sendTitle(Player var1, String var2, String var3, int var4, int var5, int var6) {
        try {
            Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\" : \"" + ChatColor.translateAlternateColorCodes('&', var2) + "\"}");
            Object subTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\" : \"" + ChatColor.translateAlternateColorCodes('&', var3) + "\"}");

            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object packet = titleConstructor.newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle, var4, var5, var6);
            Object packet2 = titleConstructor.newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null), subTitle, var4, var5, var6);

            sendPacket(var1, packet);
            sendPacket(var1, packet2);
        } catch (Exception ex) {
            //Do something
        }
    }**/

    public void sendTitle(Player var1, String var2, String var3, int var4, int var5, int var6) {
        var1.sendTitle(ChatColor.translateAlternateColorCodes('&',var2), ChatColor.translateAlternateColorCodes('&',var3));
    }

    private void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception ex) {
            //Do something
        }
    }

    /**
     * Get NMS class using reflection
     *
     * @param name Name of the class
     * @return Class
     */
    private Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
        } catch (ClassNotFoundException ex) {
            //Do something
        }
        return null;
    }
    public void getLoginScreen(Player var1) {
        Inventory var2 = Bukkit.createInventory(null, InventoryType.WORKBENCH, Main.INV_TITLE);
        var2.setItem(0, this.getItem(Material.NAME_TAG, 1, 0, Main.get().getMSG(2, "ShowPIN")));
        var2.setItem(1, this.getItem(Material.STAINED_GLASS_PANE, 1, 15, "§c§o§n§r " + Main.get().getMSG(2, "Numbers.1")));
        var2.setItem(2, this.getItem(Material.STAINED_GLASS_PANE, 2, 15, "§c§o§n§r " + Main.get().getMSG(2, "Numbers.2")));
        var2.setItem(3, this.getItem(Material.STAINED_GLASS_PANE, 3, 15, "§c§o§n§r " + Main.get().getMSG(2, "Numbers.3")));
        var2.setItem(4, this.getItem(Material.STAINED_GLASS_PANE, 4, 15, "§c§o§n§r " + Main.get().getMSG(2, "Numbers.4")));
        var2.setItem(5, this.getItem(Material.STAINED_GLASS_PANE, 5, 15, "§c§o§n§r " + Main.get().getMSG(2, "Numbers.5")));
        var2.setItem(6, this.getItem(Material.STAINED_GLASS_PANE, 6, 15, "§c§o§n§r " + Main.get().getMSG(2, "Numbers.6")));
        var2.setItem(7, this.getItem(Material.STAINED_GLASS_PANE, 7, 15, "§c§o§n§r " + Main.get().getMSG(2, "Numbers.7")));
        var2.setItem(8, this.getItem(Material.STAINED_GLASS_PANE, 8, 15, "§c§o§n§r " + Main.get().getMSG(2, "Numbers.8")));
        var2.setItem(9, this.getItem(Material.STAINED_GLASS_PANE, 9, 15, "§c§o§n§r " + Main.get().getMSG(2, "Numbers.9")));
        var1.openInventory(var2);
        PlayerInventory var3 = var1.getInventory();
        Inventory var4 = Bukkit.createInventory((InventoryHolder)null, 36);

        Integer var5;
        for(var5 = 0; var5 != 36; var5 = var5 + 1) {
            ItemStack var6 = var3.getItem(var5);
            if (var6 == null) {
                var4.setItem(var5, null);
            } else {
                var4.setItem(var5, var6.clone());
            }
        }

        if (!new Events().pInvs.containsKey(var1.getName())) {
            new Events().pInvs.put(var1.getName(), var4);
        }

        for(var5 = 0; var5 != 36; var5 = var5 + 1) {
            if (var5 < 9) {
                var3.setItem(var5, (ItemStack)null);
            } else {
                var3.setItem(var5, this.getItem(Material.STAINED_GLASS_PANE, 1, 15, "§7"));
            }
        }

        var3.setItem(19, this.getItem(Material.NAME_TAG, 1, 0, Main.get().getMSG(2, "PIN")));

        for(int var9 = new Events().cLength; var9 > 0; --var9) {
            var3.setItem(20 + var9, this.getItem(Material.STAINED_GLASS_PANE, 1, 0, "§7"));
        }

        var1.updateInventory();
    }
    public ItemStack getItem(Material var1, Integer var2, int var3, String var4) {
        ItemStack var5 = new ItemStack(var1, var2, (short)var3);
        ItemMeta var6 = var5.getItemMeta();
        var6.setDisplayName(var4);
        var5.setItemMeta(var6);
        return var5;
    }
}
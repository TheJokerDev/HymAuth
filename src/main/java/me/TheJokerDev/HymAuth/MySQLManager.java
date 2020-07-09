package me.TheJokerDev.HymAuth;

import org.bukkit.Bukkit;

import java.sql.*;
@SuppressWarnings("all")
public class MySQLManager {
    public static Connection mConn;
    private static MySQLManager instance = null;

    public MySQLManager() {
    }

    public static MySQLManager get() {
        if (instance == null) {
            instance = new MySQLManager();
        }

        return instance;
    }

    public boolean registerMySQL(final String var1, final String var2, final String var3, final String var4) {
        try {
            mConn = DriverManager.getConnection("jdbc:mysql://" + var1 + ":3306/" + var2 + "?verifyServerCertificate=false&useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=utf8", var3, var4);
        } catch (Exception var7) {
            System.out.println("\n \n \nMySQL-Error\nCould not connect to MySQL-Server!\nDisabeling plugin!\nCheck your config.yml \nDiscord: https://discord.gg/ycDG6rS \n \n");
            return false;
        }

        try {
            Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.get(), new Runnable() {
                public void run() {
                    try {
                        MySQLManager.mConn.close();
                        MySQLManager.mConn = DriverManager.getConnection("jdbc:mysql://" + var1 + ":3306/" + var2 + "?verifyServerCertificate=false&useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=utf8", var3, var4);
                    } catch (SQLException var2x) {
                        var2x.printStackTrace();
                    }

                }
            }, 72000L, 72000L);
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return true;
    }

    public void checkDB(String var1, String var2) throws SQLException {
        DatabaseMetaData var3 = mConn.getMetaData();
        ResultSet var4 = var3.getTables((String)null, (String)null, var1, (String[])null);
        if (!var4.next()) {
            Statement var5 = mConn.createStatement();
            var5.executeUpdate(var2);
        }

    }
}

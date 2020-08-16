package me.TheJokerDev.HymAuth.utils;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static String ct(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String[] ct(String... array) {
        return (String[]) Arrays.stream(array).map(Utils::ct).toArray(String[]::new);
    }

    public static List<String> ct(List<String> list) {
        return (List<String>)list.stream().map(Utils::ct).collect(Collectors.toList());
    }
    public static String encode(String input){
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        String encoded = encoder.encodeToString(input.getBytes());
        return encoded;
    }
    public static String decode(String input){
        java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
        String decoded = new String(decoder.decode(input));
        return decoded;
    }
}

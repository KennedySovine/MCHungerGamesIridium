package io.github.KennedySovine.hungerGames.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

public final class MessageUtils {
    private MessageUtils() {}

    public static String color(String s) {
        if (s == null) return null;
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void send(CommandSender to, String message) {
        to.sendMessage(color(Objects.requireNonNull(message)));
    }

    public static void sendLines(CommandSender to, List<String> lines) {
        if (lines == null) return;
        for (String l : lines) {
            to.sendMessage(color(l));
        }
    }
}


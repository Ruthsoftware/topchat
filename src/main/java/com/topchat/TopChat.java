package com.topchat;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Ayarlar: .minecraft/config/topchat.properties
 *   keyboardFraction = klavye acilinca yazi kutusunun yukari kayma orani (ekran yuksekliginin yuzdesi, 0.0 - 0.9)
 *   topMargin        = chat'in ust kenardan boslugu (piksel)
 *   enabled          = false yaparsan mod kapanir
 * Dosyayi degistirip chat'i tekrar acman yeterli, oyunu kapatmaya gerek yok.
 * Chat genisligi/yuksekligi: Ayarlar > Sohbet Ayarlari (vanilla sliderlari).
 */
public final class TopChat {
    public static volatile boolean enabled = true;
    public static volatile float keyboardFraction = 0.45f;
    public static volatile int topMargin = 6;

    // Yetkili sohbeti sesi
    public static volatile boolean staffSound = true;
    public static volatile String[] staffMatch = new String[] { "yetkili |", "yetkili|" };
    public static volatile float staffPitch = 1.0f;
    public static volatile float staffVolume = 1.0f;
    public static volatile boolean logChat = true;
    // /msg ile bana gelen ozel mesajlar: "nick -> sen mesaj"
    public static volatile boolean msgSound = true;
    public static volatile java.util.regex.Pattern msgPattern =
        java.util.regex.Pattern.compile("->\\s*sen\\b", java.util.regex.Pattern.CASE_INSENSITIVE);

    /** ChatScreen cizilirken true. Chat ic ice cizilirse cift kaymayi onlemek icin. */
    public static boolean inChatScreen = false;
    public static float currentKeyboardOffset = 0f;

    private TopChat() {}

    public static float keyboardOffset(int guiHeight) {
        float f = Math.max(0f, Math.min(0.9f, keyboardFraction));
        return guiHeight * f;
    }

    private static boolean putIfMissing(Properties p, String key, String value) {
        if (p.containsKey(key)) return false;
        p.setProperty(key, value);
        return true;
    }

    public static void loadConfig() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("topchat.properties");
        Properties p = new Properties();
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
            } catch (IOException ignored) {}
        }
        boolean changed = !Files.exists(file);
        changed |= putIfMissing(p, "enabled", "true");
        changed |= putIfMissing(p, "keyboardFraction", "0.45");
        changed |= putIfMissing(p, "topMargin", "6");
        changed |= putIfMissing(p, "staffSound", "true");
        // Virgulle ayirarak birden fazla yazabilirsin. Mesaj bunlardan birini ICERIYORSA ses calar.
        changed |= putIfMissing(p, "staffMatch", "yetkili |,yetkili|");
        changed |= putIfMissing(p, "staffPitch", "1.0");
        changed |= putIfMissing(p, "staffVolume", "1.0");
        // true ise gelen her chat satiri latest.log'a "[TopChat] chat: ..." diye yazilir (formati gormek icin)
        changed |= putIfMissing(p, "logChat", "true");
        // true ise bana gelen ozel mesajlarda (/msg) da ses calar. Baskalarinin msg'lerinde calmaz.
        changed |= putIfMissing(p, "msgSound", "true");
        // Bana gelen msg satirini taniyan duzenli ifade. Varsayilan: "-> sen" (orta tire + buyuktur + sen)
        changed |= putIfMissing(p, "msgRegex", "->\\s*sen\\b");
        if (changed) {
            try (OutputStream out = Files.newOutputStream(file)) {
                p.store(out, "Top Chat ayarlari");
            } catch (IOException ignored) {}
        }
        try {
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true").trim());
            keyboardFraction = Float.parseFloat(p.getProperty("keyboardFraction", "0.45").trim());
            topMargin = Integer.parseInt(p.getProperty("topMargin", "6").trim());
            staffSound = Boolean.parseBoolean(p.getProperty("staffSound", "true").trim());
            staffPitch = Float.parseFloat(p.getProperty("staffPitch", "1.0").trim());
            staffVolume = Float.parseFloat(p.getProperty("staffVolume", "1.0").trim());
            logChat = Boolean.parseBoolean(p.getProperty("logChat", "true").trim());
            msgSound = Boolean.parseBoolean(p.getProperty("msgSound", "true").trim());
            try {
                msgPattern = java.util.regex.Pattern.compile(
                    p.getProperty("msgRegex", "->\\s*sen\\b"), java.util.regex.Pattern.CASE_INSENSITIVE);
            } catch (java.util.regex.PatternSyntaxException ignored) {}
            String raw = p.getProperty("staffMatch", "yetkili |,yetkili|");
            java.util.List<String> parts = new java.util.ArrayList<>();
            for (String part : raw.split(",")) {
                String t = part.trim();
                if (!t.isEmpty()) parts.add(t.toLowerCase(java.util.Locale.ROOT));
            }
            staffMatch = parts.toArray(new String[0]);
        } catch (NumberFormatException ignored) {}
    }
}

package com.example.shutdowntimer;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;

/** config/shutdown-timer.properties 에 설정 저장 */
public class TimerConfig {
    public static boolean enabled = true;
    public static LocalTime shutdownTime = LocalTime.MIDNIGHT; // 기본: 24:00 (자정)

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve("shutdown-timer.properties");
    }

    public static void load() {
        try {
            Path p = configPath();
            if (!Files.exists(p)) { save(); return; }
            Properties props = new Properties();
            props.load(Files.newBufferedReader(p));
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            shutdownTime = LocalTime.parse(props.getProperty("shutdownTime", "00:00"));
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            Files.createDirectories(configPath().getParent());
            Files.write(configPath(), List.of(
                "enabled=" + enabled,
                "shutdownTime=" + shutdownTime.toString()
            ));
        } catch (IOException ignored) {}
    }
}

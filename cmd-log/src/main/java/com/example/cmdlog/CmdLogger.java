package com.example.cmdlog;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/** command-logs/YYYY-MM-DD.log 에 명령어 사용 기록 저장 */
public class CmdLogger {
    private static final int MAX_MEMORY = 1000;
    private static final Deque<String> memory = new ArrayDeque<>();
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static Path logDir() {
        return FMLPaths.GAMEDIR.get().resolve("command-logs");
    }

    public static synchronized void log(String message) {
        String line = "[" + LocalDateTime.now().format(TIME) + "] " + message;
        memory.addLast(line);
        while (memory.size() > MAX_MEMORY) memory.removeFirst();
        try {
            Files.createDirectories(logDir());
            Path file = logDir().resolve(LocalDate.now() + ".log");
            Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    public static synchronized List<String> recent(int n) {
        List<String> out = new ArrayList<>();
        var it = memory.descendingIterator();
        while (it.hasNext() && out.size() < n) out.add(it.next());
        java.util.Collections.reverse(out);
        return out;
    }

    public static synchronized List<String> search(String keyword, int n) {
        String kw = keyword.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        var it = memory.descendingIterator();
        while (it.hasNext() && out.size() < n) {
            String l = it.next();
            if (l.toLowerCase(Locale.ROOT).contains(kw)) out.add(l);
        }
        java.util.Collections.reverse(out);
        return out;
    }
}

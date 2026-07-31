package com.example.shutdowntimer;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Mod(ShutdownTimerMod.MODID)
public class ShutdownTimerMod {
    public static final String MODID = "shutdowntimer";

    public ShutdownTimerMod() {
        TimerConfig.load();
        MinecraftForge.EVENT_BUS.register(this);
    }

    // ---- 클라이언트 명령어 ----
    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("shutdowntimer")
            .then(net.minecraft.commands.Commands.literal("on").executes(ctx -> {
                TimerConfig.enabled = true; TimerConfig.save();
                reply(ctx.getSource(), "§a[ShutdownTimer] 타이머 표시를 켰습니다.");
                return 1;
            }))
            .then(net.minecraft.commands.Commands.literal("off").executes(ctx -> {
                TimerConfig.enabled = false; TimerConfig.save();
                reply(ctx.getSource(), "§c[ShutdownTimer] 타이머 표시를 껐습니다.");
                return 1;
            }))
            .then(net.minecraft.commands.Commands.literal("settime")
                .then(net.minecraft.commands.Commands.argument("time", StringArgumentType.word())
                    .executes(ctx -> {
                        String input = StringArgumentType.getString(ctx, "time");
                        try {
                            LocalTime t = parseTime(input);
                            TimerConfig.shutdownTime = t; TimerConfig.save();
                            reply(ctx.getSource(), "§a[ShutdownTimer] 서버 종료 시각을 §e" + formatTime(t) + "§a(으)로 설정했습니다.");
                        } catch (Exception e) {
                            reply(ctx.getSource(), "§c[ShutdownTimer] 시간 형식 오류. 예: /shutdowntimer settime 24:00");
                        }
                        return 1;
                    })))
            .then(net.minecraft.commands.Commands.literal("status").executes(ctx -> {
                reply(ctx.getSource(), "§b[ShutdownTimer] 표시: " + (TimerConfig.enabled ? "§aON" : "§cOFF")
                    + " §b/ 종료 시각: §e" + formatTime(TimerConfig.shutdownTime));
                return 1;
            }))
        );
    }

    private static void reply(CommandSourceStack src, String msg) {
        src.sendSuccess(() -> Component.literal(msg), false);
    }

    private static LocalTime parseTime(String input) {
        if (input.equals("24:00") || input.equals("24")) return LocalTime.MIDNIGHT;
        if (!input.contains(":")) input = input + ":00";
        return LocalTime.parse(input, DateTimeFormatter.ofPattern("H:mm"));
    }

    private static String formatTime(LocalTime t) {
        if (t.equals(LocalTime.MIDNIGHT)) return "24:00";
        return t.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // ---- HUD 렌더링 (우측 상단) ----
    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (!TimerConfig.enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug) return; // F3 화면에서는 숨김

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.toLocalDate().atTime(TimerConfig.shutdownTime);
        if (!target.isAfter(now)) target = target.plusDays(1);

        Duration remaining = Duration.between(now, target);
        long h = remaining.toHours();
        long m = remaining.toMinutesPart();
        long s = remaining.toSecondsPart();

        String line1 = String.format("서버 종료까지 %02d:%02d:%02d", h, m, s);
        String line2 = formatTime(TimerConfig.shutdownTime) + "에 서버가 닫힙니다";

        GuiGraphics g = event.getGuiGraphics();
        int screenWidth = g.guiWidth();
        int color = remaining.toMinutes() < 10 ? 0xFFFF5555 : 0xFFFFFFFF;

        int x1 = screenWidth - mc.font.width(line1) - 5;
        int x2 = screenWidth - mc.font.width(line2) - 5;
        g.drawString(mc.font, line1, x1, 5, color, true);
        g.drawString(mc.font, line2, x2, 16, 0xFFAAAAAA, true);
    }
}

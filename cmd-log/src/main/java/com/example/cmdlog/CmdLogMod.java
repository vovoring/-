package com.example.cmdlog;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod(CmdLogMod.MODID)
public class CmdLogMod {
    public static final String MODID = "cmdlog";

    public CmdLogMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    // 모든 명령어 실행 직전에 호출됨 → 기록
    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        try {
            String raw = event.getParseResults().getReader().getString();
            CommandSourceStack source = event.getParseResults().getContext().getSource();
            String name = source.getTextName();
            // 자기 자신의 조회 명령은 기록하지 않음
            if (raw.startsWith("cmdlog")) return;
            CmdLogger.log(name + " → /" + raw);
        } catch (Exception ignored) {}
    }

    // 조회 명령어 (OP/호스트 전용)
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cmdlog")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("recent")
                .then(Commands.argument("n", IntegerArgumentType.integer(1, 50))
                    .executes(ctx -> show(ctx.getSource(),
                        CmdLogger.recent(IntegerArgumentType.getInteger(ctx, "n"))))))
            .then(Commands.literal("search")
                .then(Commands.argument("keyword", StringArgumentType.greedyString())
                    .executes(ctx -> show(ctx.getSource(),
                        CmdLogger.search(StringArgumentType.getString(ctx, "keyword"), 20)))))
            .then(Commands.literal("where")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§b[CmdLog] 로그 파일 위치: §e" + CmdLogger.logDir()), false);
                    return 1;
                }))
        );
    }

    private static int show(CommandSourceStack src, List<String> lines) {
        src.sendSuccess(() -> Component.literal("§b[CmdLog] 명령어 기록 " + lines.size() + "건:"), false);
        for (String l : lines) {
            src.sendSuccess(() -> Component.literal("§6" + l), false);
        }
        return 1;
    }
}

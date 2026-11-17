package com.aotmusic.server;

import com.aotmusic.Track;
import com.aotmusic.Tracks;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

/** /aotmusic play <трек> | list | stop — только для операторов (уровень 2). */
public final class MusicCommands {
    private MusicCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("aotmusic")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("play")
                        .then(CommandManager.argument("track", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(Tracks.commandIds(), builder))
                                .executes(ctx -> play(ctx.getSource(), StringArgumentType.getString(ctx, "track")))))
                .then(CommandManager.literal("list").executes(ctx -> list(ctx.getSource())))
                .then(CommandManager.literal("stop").executes(ctx -> stop(ctx.getSource()))));
    }

    private static int play(ServerCommandSource src, String id) {
        Track track = Tracks.byId(id);
        if (track == null || track.background()) {
            src.sendError(Text.literal("Нет такого командного трека: " + id));
            return 0;
        }
        MusicController.play(src.getServer(), track);
        src.sendFeedback(() -> Text.literal("♪ Играет для всех: " + track.title()), true);
        return 1;
    }

    private static int list(ServerCommandSource src) {
        src.sendFeedback(() -> Text.literal("Командные треки:"), false);
        for (String id : Tracks.commandIds()) {
            Track t = Tracks.byId(id);
            if (t == null) continue;
            src.sendFeedback(() -> Text.literal(" • " + id + " — " + t.title()), false);
        }
        return 1;
    }

    private static int stop(ServerCommandSource src) {
        if (MusicController.stop(src.getServer())) {
            src.sendFeedback(() -> Text.literal("♪ Музыка остановлена. Фон вернётся по обычному расписанию."), true);
            return 1;
        }
        src.sendError(Text.literal("Сейчас ничего не играет"));
        return 0;
    }
}

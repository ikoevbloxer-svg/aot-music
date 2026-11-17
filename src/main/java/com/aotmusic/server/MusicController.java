package com.aotmusic.server;

import com.aotmusic.Net;
import com.aotmusic.Track;
import com.aotmusic.Tracks;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Один источник правды для всех игроков. Сервер решает, что играет и когда,
 * и шлёт одно и то же всем сразу, поэтому звучит одинаково у всех.
 *
 * Фоновая логика как у ванили: трек, затем пауза 10–20 минут, потом случайный трек
 * (не тот же, что только что играл). Команда перебивает текущий трек и не сбивает паузы.
 */
public final class MusicController {
    /** Пауза между треками в тиках, как у ванильной музыки: 10–20 минут. */
    private static final int MIN_PAUSE = 12000;
    private static final int MAX_PAUSE = 24000;

    private static final Random RANDOM = new Random();
    private static Track current;
    private static long startedAt;
    /** Когда начнётся следующий фоновый трек. -1 — ещё не выбрано. */
    private static long nextBackgroundAt = -1;
    private static Track lastBackground;

    private MusicController() {}

    /** Вызывается каждый серверный тик. */
    public static void tick(MinecraftServer server) {
        long now = server.getTicks();
        if (current != null) {
            if (now >= startedAt + current.durationTicks()) {
                current = null;
                nextBackgroundAt = now + pause();
            }
            return;
        }
        if (nextBackgroundAt < 0) {
            nextBackgroundAt = now + pause();
            return;
        }
        if (now < nextBackgroundAt) return;
        if (server.getPlayerManager().getPlayerList().isEmpty()) {
            nextBackgroundAt = now + pause();
            return;
        }
        Track next = pickBackground();
        if (next != null) play(server, next);
    }

    /** Запустить трек для всех игроков прямо сейчас. */
    public static void play(MinecraftServer server, Track track) {
        current = track;
        startedAt = server.getTicks();
        send(server, Net.PLAY, track.id());
    }

    /** Остановить текущий трек. Возвращает false, если ничего не играло. */
    public static boolean stop(MinecraftServer server) {
        if (current == null) return false;
        current = null;
        nextBackgroundAt = server.getTicks() + pause();
        send(server, Net.STOP, "");
        return true;
    }

    public static void reset() {
        current = null;
        nextBackgroundAt = -1;
        lastBackground = null;
    }

    private static long pause() {
        return MIN_PAUSE + RANDOM.nextInt(MAX_PAUSE - MIN_PAUSE + 1);
    }

    /** Случайный фоновый трек, не равный предыдущему. */
    private static Track pickBackground() {
        List<Track> pool = new ArrayList<>();
        for (Track t : Tracks.background()) {
            if (t != lastBackground) pool.add(t);
        }
        if (pool.isEmpty()) return null;
        Track picked = pool.get(RANDOM.nextInt(pool.size()));
        lastBackground = picked;
        return picked;
    }

    private static void send(MinecraftServer server, Identifier channel, String trackId) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(trackId);
            ServerPlayNetworking.send(player, channel, buf);
        }
    }
}

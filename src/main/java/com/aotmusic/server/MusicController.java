package com.aotmusic.server;

import com.aotmusic.Net;
import com.aotmusic.Track;
import com.aotmusic.Tracks;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Сервер управляет общим расписанием и синхронно запускает треки у игроков. */
public final class MusicController {
    private static final int MIN_PAUSE = 12000;
    private static final int MAX_PAUSE = 24000;

    private static final Random RANDOM = new Random();
    private static Track current;
    private static long startedAt;
    private static long nextBackgroundAt = -1;
    private static Track lastBackground;

    private MusicController() {}

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

    public static void play(MinecraftServer server, Track track) {
        current = track;
        startedAt = server.getTicks();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new Net.PlayPayload(track.id()));
        }
    }

    public static boolean stop(MinecraftServer server) {
        if (current == null) return false;
        current = null;
        nextBackgroundAt = server.getTicks() + pause();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new Net.StopPayload());
        }
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
}

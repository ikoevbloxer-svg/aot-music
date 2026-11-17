package com.aotmusic.client;

import com.aotmusic.Net;
import com.aotmusic.Track;
import com.aotmusic.Tracks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SimpleSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;

/**
 * Клиент: проигрывает трек, который прислал сервер, глушит ванильную музыку,
 * пока мод активен, и показывает название трека справа сверху на 5 секунд.
 */
public class AotMusicClient implements ClientModInitializer {
    /** 5 секунд при 20 тиках в секунду. */
    private static final int TOAST_TICKS = 100;

    private static SoundInstance playing;
    private static int clientTick;
    private static int toastUntil = -1;
    private static String toastTitle = "";

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(Net.PLAY, (client, handler, buf, sender) -> {
            String id = buf.readString();
            client.execute(() -> startTrack(client, id));
        });
        ClientPlayNetworking.registerGlobalReceiver(Net.STOP, (client, handler, buf, sender) ->
                client.execute(() -> stopTrack(client)));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            clientTick++;
            if (mc.world == null) {
                stopTrack(mc); // вышли из мира — выключаем свою музыку
                return;
            }
            // ванильная музыка молчит, пока играют саундтреки мода
            mc.getMusicTracker().stop();
        });

        HudRenderCallback.EVENT.register(AotMusicClient::renderToast);
    }

    private static void startTrack(MinecraftClient mc, String id) {
        Track track = Tracks.byId(id);
        if (track == null) return;
        stopTrack(mc);
        SoundEvent event = Registries.SOUND_EVENT.get(track.soundId());
        if (event == null) return;
        playing = SimpleSoundInstance.forMusic(event);
        mc.getSoundManager().play(playing);
        toastTitle = track.title();
        toastUntil = clientTick + TOAST_TICKS;
    }

    private static void stopTrack(MinecraftClient mc) {
        if (playing != null) {
            mc.getSoundManager().stop(playing);
            playing = null;
        }
        toastUntil = -1;
    }

    private static void renderToast(DrawContext ctx, float tickDelta) {
        if (toastUntil < 0 || clientTick >= toastUntil || toastTitle.isEmpty()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer font = mc.textRenderer;
        float left = toastUntil - clientTick - tickDelta;
        int alpha = Math.max(0, Math.min(255, (int) (Math.min(1F, left / 10F) * 255)));

        String head = "♪ Сейчас играет";
        int w = Math.max(font.getWidth(toastTitle), font.getWidth(head)) + 24;
        int x = mc.getWindow().getScaledWidth() - w - 8;
        int y = 8;
        ctx.fill(x, y, x + w, y + 32, (Math.min(alpha, 200) << 24) | 0x0C0C14);
        ctx.fill(x, y, x + 2, y + 32, (alpha << 24) | 0xB55CFF);
        ctx.drawText(font, head, x + 10, y + 5, (alpha << 24) | 0xB4B4C4, false);
        ctx.drawText(font, toastTitle, x + 10, y + 17, (alpha << 24) | 0xFFFFFF, false);
    }
}

package com.aotmusic;

import com.aotmusic.server.MusicCommands;
import com.aotmusic.server.MusicController;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class AotMusic implements ModInitializer {
    public static final String MODID = "aotmusic";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        for (Track t : Tracks.ALL) {
            Registry.register(Registries.SOUND_EVENT, t.soundId(), SoundEvent.of(t.soundId()));
        }
        ServerTickEvents.END_SERVER_TICK.register(MusicController::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> MusicController.reset());
        CommandRegistrationCallback.EVENT.register(MusicCommands::register);
    }
}

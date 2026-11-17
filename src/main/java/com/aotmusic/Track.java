package com.aotmusic;

import net.minecraft.util.Identifier;

/** Один трек: id совпадает с файлом sounds/<id>.ogg, durationTicks — длина плюс запас. */
public record Track(String id, String title, int durationTicks, boolean background) {
    public Identifier soundId() {
        return AotMusic.id(id);
    }
}

package com.aotmusic;

import net.minecraft.util.Identifier;

/** Каналы сети. Сервер шлёт id трека; клиент сам проигрывает его из своих файлов. */
public final class Net {
    public static final Identifier PLAY = AotMusic.id("play");
    public static final Identifier STOP = AotMusic.id("stop");

    private Net() {}
}

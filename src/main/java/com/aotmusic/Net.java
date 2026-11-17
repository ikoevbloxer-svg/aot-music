package com.aotmusic;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Серверные сообщения для запуска и остановки музыки на клиентах. */
public final class Net {
    public record PlayPayload(String trackId) implements CustomPayload {
        public static final Id<PlayPayload> ID = new Id<>(AotMusic.id("play"));
        public static final PacketCodec<RegistryByteBuf, PlayPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.STRING, PlayPayload::trackId, PlayPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record StopPayload() implements CustomPayload {
        public static final Id<StopPayload> ID = new Id<>(AotMusic.id("stop"));
        public static final PacketCodec<RegistryByteBuf, StopPayload> CODEC =
                PacketCodec.unit(new StopPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private Net() {}
}

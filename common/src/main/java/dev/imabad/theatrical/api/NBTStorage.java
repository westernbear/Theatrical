package dev.imabad.theatrical.api;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.nio.ByteBuffer;

public interface NBTStorage {
    Codec<byte[]> BYTE_ARRAY_CODEC = Codec.BYTE_BUFFER.xmap(buffer -> {
        ByteBuffer copy = buffer.duplicate();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return bytes;
    }, ByteBuffer::wrap);

    void write(ValueOutput output);

    void read(ValueInput input);
}

package dev.imabad.theatrical.client.dmx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.UUID;

public class ArtNetToNetworkClientData extends SavedData {

    private static ArtNetToNetworkClientData INSTANCE;
    private static final String KEY = "artnet_network_map";

    public static void unload(){
        INSTANCE = null;
    }

    private static final Codec<ArtNetToNetworkClientData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            net.minecraft.core.UUIDUtil.CODEC.optionalFieldOf("networkId", UUIDUtil.NULL)
                    .forGetter(data -> data.networkId)
    ).apply(instance, ArtNetToNetworkClientData::new));
    private static final SavedDataType<ArtNetToNetworkClientData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Theatrical.MOD_ID, KEY),
            ArtNetToNetworkClientData::new,
            CODEC,
            null
    );
    public static ArtNetToNetworkClientData getInstance(Level level){
        if(INSTANCE == null){
            INSTANCE = level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
        }
        return INSTANCE;
    }

    public ArtNetToNetworkClientData() {
    }

    private UUID networkId = UUIDUtil.NULL;

    private ArtNetToNetworkClientData(UUID networkId) {
        this.networkId = networkId;
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public void setNetworkId(UUID networkId) {
        this.networkId = networkId;
        setDirty(true);
    }
}

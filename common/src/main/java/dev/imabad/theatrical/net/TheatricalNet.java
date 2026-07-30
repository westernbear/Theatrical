package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.net.artnet.ListConsumers;
import dev.imabad.theatrical.net.artnet.NotifyConsumerChange;
import dev.imabad.theatrical.net.artnet.NotifyNetworks;
import dev.imabad.theatrical.net.artnet.RDMUpdateConsumer;
import dev.imabad.theatrical.net.artnet.RequestConsumers;
import dev.imabad.theatrical.net.artnet.RequestNetworks;
import dev.imabad.theatrical.net.artnet.SendArtNetData;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class TheatricalNet {
    private static final Map<Identifier, MessageType> C2S_MESSAGES = new HashMap<>();
    private static final Map<Identifier, MessageType> S2C_MESSAGES = new HashMap<>();

    public static final MessageType SEND_ARTNET_TO_SERVER = registerC2S("send_artnet_to_server", SendArtNetData::new);
    public static final MessageType UPDATE_DMX_FIXTURE = registerC2S("update_dmx_fixture", UpdateDMXFixture::new);
    public static final MessageType UPDATE_FIXTURE_POS = registerC2S("update_fixture_pos", UpdateFixturePosition::new);
    public static final MessageType RDM_UPDATE_FIXTURE = registerC2S("rdm_update_fixture", RDMUpdateConsumer::new);
    public static final MessageType REQUEST_CONSUMERS = registerC2S("request_consumers", RequestConsumers::new);
    public static final MessageType UPDATE_CONSOLE_FADER = registerC2S("update_console_fader", ControlUpdateFader::new);
    public static final MessageType CONTROL_MOVE_STEP = registerC2S("control_move_step", ControlMoveStep::new);
    public static final MessageType CONTROL_MODE_TOGGLE = registerC2S("control_mode_toggle", ControlModeToggle::new);
    public static final MessageType CONTROL_GO = registerC2S("control_go", ControlGo::new);
    public static final MessageType REQUEST_NETWORKS = registerC2S("request_networks", RequestNetworks::new);
    public static final MessageType UPDATE_NETWORK_ID = registerC2S("update_network_id", UpdateNetworkId::new);
    public static final MessageType CONFIGURE_CONFIGURATION_CARD = registerC2S("configure_configuration_card", ConfigureConfigurationCard::new);

    public static final MessageType NOTIFY_CONSUMER_CHANGE = registerS2C("notify_consumer_change", NotifyConsumerChange::new);
    public static final MessageType LIST_CONSUMERS = registerS2C("list_consumers", ListConsumers::new);
    public static final MessageType NOTIFY_NETWORKS = registerS2C("notify_networks", NotifyNetworks::new);
    public static final MessageType OPEN_SCREEN = registerS2C("open_screen", OpenScreen::new);

    private static final CustomPacketPayload.Type<C2SPayload> C2S_TYPE =
            new CustomPacketPayload.Type<>(id("c2s"));
    private static final CustomPacketPayload.Type<S2CPayload> S2C_TYPE =
            new CustomPacketPayload.Type<>(id("s2c"));
    private static final StreamCodec<RegistryFriendlyByteBuf, C2SPayload> C2S_CODEC =
            CustomPacketPayload.codec(C2SPayload::write, C2SPayload::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, S2CPayload> S2C_CODEC =
            CustomPacketPayload.codec(S2CPayload::write, S2CPayload::new);

    private TheatricalNet() {
    }

    public static void init() {
        NetworkManager.registerC2S(C2S_TYPE, C2S_CODEC,
                (payload, context) -> context.queue(() -> payload.message.handle(context)));
        NetworkManager.registerS2C(S2C_TYPE, S2C_CODEC,
                (payload, context) -> context.queue(() -> payload.message.handle(context)));
    }

    private static MessageType registerC2S(String path, Function<FriendlyByteBuf, ? extends BaseMessage> decoder) {
        return register(C2S_MESSAGES, path, decoder);
    }

    private static MessageType registerS2C(String path, Function<FriendlyByteBuf, ? extends BaseMessage> decoder) {
        return register(S2C_MESSAGES, path, decoder);
    }

    private static MessageType register(Map<Identifier, MessageType> messages, String path,
                                        Function<FriendlyByteBuf, ? extends BaseMessage> decoder) {
        MessageType type = new MessageType(id(path), decoder);
        if (messages.put(type.id, type) != null) {
            throw new IllegalStateException("Duplicate network message " + type.id);
        }
        return type;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Theatrical.MOD_ID, path);
    }

    private static void write(RegistryFriendlyByteBuf buf, BaseMessage message) {
        buf.writeIdentifier(message.getType().id);
        message.write(buf);
    }

    private static BaseMessage read(RegistryFriendlyByteBuf buf, Map<Identifier, MessageType> messages) {
        Identifier id = buf.readIdentifier();
        MessageType type = messages.get(id);
        if (type == null) {
            throw new DecoderException("Unknown Theatrical network message " + id);
        }
        return type.decoder.apply(buf);
    }

    public static final class MessageType {
        private final Identifier id;
        private final Function<FriendlyByteBuf, ? extends BaseMessage> decoder;

        private MessageType(Identifier id, Function<FriendlyByteBuf, ? extends BaseMessage> decoder) {
            this.id = id;
            this.decoder = decoder;
        }
    }

    public abstract static class BaseMessage {
        public abstract MessageType getType();

        public abstract void write(FriendlyByteBuf buf);

        public abstract void handle(NetworkManager.PacketContext context);
    }

    public abstract static class BaseC2SMessage extends BaseMessage {
        public final void sendToServer() {
            NetworkManager.sendToServer(new C2SPayload(this));
        }
    }

    public abstract static class BaseS2CMessage extends BaseMessage {
        public final void sendTo(ServerPlayer player) {
            NetworkManager.sendToPlayer(player, new S2CPayload(this));
        }

        public final void sendTo(Iterable<ServerPlayer> players) {
            NetworkManager.sendToPlayers(players, new S2CPayload(this));
        }
    }

    private record C2SPayload(BaseC2SMessage message) implements CustomPacketPayload {
        private C2SPayload(RegistryFriendlyByteBuf buf) {
            this((BaseC2SMessage) read(buf, C2S_MESSAGES));
        }

        private void write(RegistryFriendlyByteBuf buf) {
            TheatricalNet.write(buf, message);
        }

        @Override
        public Type<C2SPayload> type() {
            return C2S_TYPE;
        }
    }

    private record S2CPayload(BaseS2CMessage message) implements CustomPacketPayload {
        private S2CPayload(RegistryFriendlyByteBuf buf) {
            this((BaseS2CMessage) read(buf, S2C_MESSAGES));
        }

        private void write(RegistryFriendlyByteBuf buf) {
            TheatricalNet.write(buf, message);
        }

        @Override
        public Type<S2CPayload> type() {
            return S2C_TYPE;
        }
    }
}

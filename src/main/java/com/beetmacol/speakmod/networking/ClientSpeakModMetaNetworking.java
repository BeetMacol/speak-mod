package com.beetmacol.speakmod.networking;

import com.beetmacol.speakmod.SpeakMod;
import com.beetmacol.speakmod.SpeakModClient;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.LineUnavailableException;

import java.io.IOException;
import java.net.SocketException;
import java.util.Objects;

import static com.beetmacol.speakmod.SpeakMod.META_PLUGIN_CHANNEL;
import static com.beetmacol.speakmod.SpeakMod.PROTOCOL_VERSION;

@Environment(EnvType.CLIENT)
public class ClientSpeakModMetaNetworking {
	public static final Logger LOGGER = LogManager.getLogger("Speak Mod Networking");

	public static void initialize() {
		ClientPlayNetworking.registerGlobalReceiver(META_PLUGIN_CHANNEL, ClientSpeakModMetaNetworking::handleChannelPayload);
		// FIXME FAPI's disconnect event doesn't work :( https://github.com/FabricMC/fabric/issues/1300
		// ClientPlayConnectionEvents.DISCONNECT.register(ClientSpeakModMetaNetworking::leaveServer);
	}

	private static void handleChannelPayload(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
		LOGGER.info("Speak Mod is present on the connected server.");
		switch (packetByteBuf.readVarInt()) {
			case 0x00: {
				int serverProtocolVersion = packetByteBuf.readVarInt();
				String serverModVersion = packetByteBuf.readString();
				int serverVoiceChatPort = packetByteBuf.readShort();
				if (serverProtocolVersion != PROTOCOL_VERSION) {
					LOGGER.error("The connected server uses a different Speak Mod protocol version. You will not be able to use the voice chat on this server. Please download a different mod version with a matching protocol version (server uses {}). Your protocol version: {}, server's protocol version: {}.", serverModVersion, PROTOCOL_VERSION, serverProtocolVersion);
					// Make speaking impossible
				} else {
					if (!serverModVersion.equals(SpeakMod.VERSION)) {
						LOGGER.warn("The connected server uses a different Speak Mod version (your version: {}, server's version: {}). It is recommended to use the same version on both sides.", SpeakMod.VERSION, serverModVersion);
					}
					try {
						SpeakModClient.voiceChatClient = new VoiceChatClient(Objects.requireNonNull(minecraftClient.getCurrentServerEntry(), "Unexpected null value in Speak Mod. Please report this issue.").address, serverVoiceChatPort);
					} catch (LineUnavailableException | SocketException exception) {
						LOGGER.error("Could not initialize the voice chat client.", exception);
						return;
					}
					packetSender.sendPacket(META_PLUGIN_CHANNEL, new PacketByteBuf(Unpooled.buffer()).writeVarInt(0x00).writeVarInt(PROTOCOL_VERSION).writeString(SpeakMod.VERSION));
				}
				break;
			}
			case 0x01: {
				try {
					SpeakModClient.voiceChatClient.sendConnectionInitPacket(packetByteBuf.readVarLong());
				} catch (IOException exception) {
					LOGGER.error(exception);
				}
				break;
			}
		}
	}

	private static void leaveServer(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient minecraftClient) {
		if (SpeakModClient.voiceChatClient != null) {
			SpeakModClient.voiceChatClient.terminate();
			SpeakModClient.voiceChatClient = null;
		}
	}
}

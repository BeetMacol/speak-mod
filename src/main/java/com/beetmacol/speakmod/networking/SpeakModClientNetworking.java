package com.beetmacol.speakmod.networking;

import com.beetmacol.speakmod.SpeakMod;
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

import static com.beetmacol.speakmod.SpeakMod.HANDSHAKE_CHANNEL;
import static com.beetmacol.speakmod.SpeakMod.PROTOCOL_VERSION;

@Environment(EnvType.CLIENT)
public class SpeakModClientNetworking {
	public static final Logger LOGGER = LogManager.getLogger("Speak Mod Networking");

	public static void initialize() {
		ClientPlayNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL, SpeakModClientNetworking::handleChannelPayload);
	}

	private static void handleChannelPayload(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
		LOGGER.info("Detected Speak Mod on the connected server.");
		int serverProtocolVersion = packetByteBuf.readVarInt();
		String serverModVersion = packetByteBuf.readString();
		if (serverProtocolVersion != PROTOCOL_VERSION) {
			LOGGER.error("The connected server uses a different Speak Mod protocol version. You will not be able to use the voice chat on this server. Please download a different mod version with a matching protocol version (server uses {}). Your protocol version: {}, server's protocol version: {}.", serverModVersion, PROTOCOL_VERSION, serverProtocolVersion);
			// Make speaking impossible
		} else {
			if (!serverModVersion.equals(SpeakMod.VERSION)) {
				LOGGER.warn("The connected server uses a different Speak Mod version (your version: {}, server's version: {}). It is recommended to use the same version on both sides.", SpeakMod.VERSION, serverModVersion);
			}
			packetSender.sendPacket(HANDSHAKE_CHANNEL, new PacketByteBuf(Unpooled.buffer()).writeVarInt(PROTOCOL_VERSION).writeString(SpeakMod.VERSION));
		}
	}
}

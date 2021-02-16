package com.beetmacol.speakmod.networking;

import com.beetmacol.speakmod.SpeakMod;
import com.beetmacol.speakmod.SpeakModServer;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.beetmacol.speakmod.SpeakMod.HANDSHAKE_CHANNEL;
import static com.beetmacol.speakmod.SpeakMod.PROTOCOL_VERSION;

@Environment(EnvType.SERVER)
public class ServerSpeakModMetaNetworking {
	public static final Logger LOGGER = LogManager.getLogger("Speak Mod Networking");

	public static void initialize() {
		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> playerJoin(serverPlayNetworkHandler));
		ServerPlayNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL, ServerSpeakModMetaNetworking::handleChannelPayload);
	}

	private static void handleChannelPayload(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler networkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
		int clientProtocolVersion = packetByteBuf.readVarInt();
		//String clientModVersion = packetByteBuf.readString();
		if (clientProtocolVersion != PROTOCOL_VERSION) {
			LOGGER.info("Player {} uses an unsupported Speak Mod protocol version. They will not be able to use the voice chat.", playerEntity.getName().asString());
		} else {
			LOGGER.info("Player {} joined with a supported Speak Mod version (matching protocol version).", playerEntity.getName().asString());
		}
	}

	private static void playerJoin(ServerPlayNetworkHandler networkHandler) {
		PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer()).writeVarInt(PROTOCOL_VERSION).writeString(SpeakMod.VERSION);
		packetByteBuf.writeShort(SpeakModServer.getPort());
		ServerPlayNetworking.send(networkHandler.player, HANDSHAKE_CHANNEL, packetByteBuf);
	}
}

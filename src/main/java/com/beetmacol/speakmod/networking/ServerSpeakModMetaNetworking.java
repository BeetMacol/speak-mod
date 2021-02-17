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

import java.util.Random;

import static com.beetmacol.speakmod.SpeakMod.META_PLUGIN_CHANNEL;
import static com.beetmacol.speakmod.SpeakMod.PROTOCOL_VERSION;

@Environment(EnvType.SERVER)
public class ServerSpeakModMetaNetworking {
	public static final Logger LOGGER = LogManager.getLogger("Speak Mod Networking");

	public static void initialize() {
		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> playerJoin(serverPlayNetworkHandler));
		ServerPlayConnectionEvents.DISCONNECT.register((networkHandler, minecraftServer) -> playerLeave(networkHandler));
		ServerPlayNetworking.registerGlobalReceiver(META_PLUGIN_CHANNEL, ServerSpeakModMetaNetworking::handleChannelPayload);
	}

	private static void handleChannelPayload(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler networkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
		if (packetByteBuf.readVarInt() == 0x00) {
			int clientProtocolVersion = packetByteBuf.readVarInt();
			//String clientModVersion = packetByteBuf.readString();
			if (clientProtocolVersion != PROTOCOL_VERSION) {
				LOGGER.info("Player {} uses an unsupported Speak Mod protocol version. They will not be able to use the voice chat.", playerEntity.getName().asString());
			} else {
				LOGGER.info("Player {} joined with a supported Speak Mod version (matching protocol version).", playerEntity.getName().asString());
				long udpAuthCode = new Random().nextLong(); // This code is sent back to the server in the first UDP packet to identify and authenticate the udp socket with the player
				SpeakModServer.voiceChatServer.addUdpAwaitedPlayer(udpAuthCode, networkHandler.player);
				PacketByteBuf udpInitPacketBuf = new PacketByteBuf(Unpooled.buffer());
				udpInitPacketBuf.writeVarInt(0x01);
				udpInitPacketBuf.writeVarLong(udpAuthCode);
				ServerPlayNetworking.send(networkHandler.player, META_PLUGIN_CHANNEL, udpInitPacketBuf);
			}
		}
	}

	private static void playerJoin(ServerPlayNetworkHandler networkHandler) {
		PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer()).writeVarInt(0x00).writeVarInt(PROTOCOL_VERSION).writeString(SpeakMod.VERSION);
		packetByteBuf.writeShort(SpeakModServer.getVoiceServerPort());
		ServerPlayNetworking.send(networkHandler.player, META_PLUGIN_CHANNEL, packetByteBuf);
	}

	private static void playerLeave(ServerPlayNetworkHandler networkHandler) {
		SpeakModServer.voiceChatServer.removePlayer(networkHandler.player);
	}
}

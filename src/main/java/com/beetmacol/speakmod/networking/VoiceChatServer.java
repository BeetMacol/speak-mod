package com.beetmacol.speakmod.networking;

import com.beetmacol.speakmod.SpeakMod;
import com.beetmacol.speakmod.SpeakModServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.SERVER)
public class VoiceChatServer {
	private final String ip;
	private final DatagramSocket socket;

	private final HashMap<Long, ServerPlayerEntity> unconfirmedPlayers = new HashMap<>();
	private final HashMap<SocketAddress, ServerPlayerEntity> clients = new HashMap<>();

	private final Thread udpListeningThread;

	public VoiceChatServer(MinecraftServer minecraftServer) throws SocketException {
		this.ip = minecraftServer.getServerIp();
		this.socket = new DatagramSocket(SpeakModServer.getVoiceServerPort());
		this.udpListeningThread = new Thread(this::listen, "Voice Chat Packet Lister");
		this.udpListeningThread.start();
	}

	public void terminate() {
		this.socket.close();
		this.udpListeningThread.interrupt();
	}

	private void listen() {
		byte[] data = new byte[4096];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		while (!Thread.interrupted()) {
			try {
				socket.receive(packet);
				switch (packet.getData()[0]) {
					case 0x00: {
						long auth;
						ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
						for (int i = 0; i < Long.BYTES; i++) {
							buffer.put(data[i+1]);
						}
						buffer.flip();
						auth = buffer.getLong();
						if (unconfirmedPlayers.containsKey(auth)) {
							ServerPlayerEntity player = unconfirmedPlayers.get(auth);
							unconfirmedPlayers.remove(auth);
							clients.put(packet.getSocketAddress(), player);
						}
						break;
					}
					case 0x01: {
						ServerPlayerEntity speakingPlayer = clients.get(packet.getSocketAddress());
						// Sound packets
						break;
					}
				}
			} catch (IOException ignored) {
			}
		}
	}

	public void addUdpAwaitedPlayer(long auth, ServerPlayerEntity player) {
		unconfirmedPlayers.put(auth, player);
	}

	public void removeUdpAwaitedPlayer(ServerPlayerEntity checked) {
		for (Map.Entry<Long, ServerPlayerEntity> player : unconfirmedPlayers.entrySet()) {
			if (player.getValue() == checked) {
				unconfirmedPlayers.remove(player.getKey());
				break;
			}
		}
	}
}

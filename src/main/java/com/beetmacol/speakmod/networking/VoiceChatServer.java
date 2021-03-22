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
		ByteBuffer data = ByteBuffer.allocate(SpeakMod.UDP_PACKET_SIZE);
		DatagramPacket packet = new DatagramPacket(data.array(), SpeakMod.UDP_PACKET_SIZE);
		while (!Thread.interrupted()) {
			try {
				data.position(0);
				socket.receive(packet);
				data.get();
				switch (data.array()[0]) {
					case 0x00: {
						long auth = data.getLong();
						if (unconfirmedPlayers.containsKey(auth)) {
							ServerPlayerEntity player = unconfirmedPlayers.get(auth);
							unconfirmedPlayers.remove(auth);
							clients.put(packet.getSocketAddress(), player);
						}
						break;
					}
					case 0x01: {
						ServerPlayerEntity speakingPlayer = clients.get(packet.getSocketAddress());
						for (Map.Entry<SocketAddress, ServerPlayerEntity> client : clients.entrySet()) {
							ServerPlayerEntity player = client.getValue();
							if (player != speakingPlayer && player.getPos().isInRange(speakingPlayer.getPos(), SpeakMod.getVoiceChatRange()) && player.getServerWorld() == speakingPlayer.getServerWorld()) {
								byte[] audio = new byte[SpeakMod.AUDIO_BUFFER_SIZE];
								data.get(audio);
								sendVoicePacket(speakingPlayer, audio, client.getKey());
							}
						}
						break;
					}
				}
			} catch (SocketException ignored) {
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	public void sendVoicePacket(ServerPlayerEntity speakingPlayer, byte[] audio, SocketAddress client) throws IOException {
		ByteBuffer data = ByteBuffer.allocate(SpeakMod.UDP_PACKET_SIZE);
		data.put((byte) 0x00);
		data.putLong(speakingPlayer.getUuid().getMostSignificantBits());
		data.putLong(speakingPlayer.getUuid().getLeastSignificantBits());
		data.put(audio);
		socket.send(new DatagramPacket(data.array(), SpeakMod.UDP_PACKET_SIZE, client));
	}

	public void addUdpAwaitedPlayer(long auth, ServerPlayerEntity player) {
		unconfirmedPlayers.put(auth, player);
	}

	public void removePlayer(ServerPlayerEntity checked) {
		for (Map.Entry<Long, ServerPlayerEntity> player : unconfirmedPlayers.entrySet()) {
			if (player.getValue() == checked) {
				unconfirmedPlayers.remove(player.getKey());
				return;
			}
		}
		for (Map.Entry<SocketAddress, ServerPlayerEntity> player : clients.entrySet()) {
			if (player.getValue() == checked) {
				clients.remove(player.getKey());
				return;
			}
		}
	}
}

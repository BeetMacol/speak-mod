package com.beetmacol.speakmod.networking;

import com.beetmacol.speakmod.AudioInput;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

@Environment(EnvType.CLIENT)
public class VoiceChatClient {
	private final DatagramSocket serverSocket;
	private final SocketAddress serverSocketAddress;
	private final int voiceServerPort;
	private final AudioInput audioInput;

	public VoiceChatClient(String address, int port) throws LineUnavailableException, SocketException {
		this.voiceServerPort = port;
		this.audioInput = new AudioInput();
		serverSocket = new DatagramSocket();
		this.serverSocketAddress = new InetSocketAddress(address, port);
	}

	public void terminate() {
		audioInput.terminate();
	}

	public void sendConnectionInitPacket(long auth) throws IOException {
		byte[] data = new byte[4096];
		data[0] = 0x00;
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(auth);
		for (int i = 0; i < Long.BYTES; i++) {
			data[i+1] = buffer.get(i);
		}
		DatagramPacket packet = new DatagramPacket(data, data.length, serverSocketAddress);
		serverSocket.send(packet);
	}

	public int getVoiceServerPort() {
		return voiceServerPort;
	}
}

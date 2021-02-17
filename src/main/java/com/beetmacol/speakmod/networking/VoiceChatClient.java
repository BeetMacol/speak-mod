package com.beetmacol.speakmod.networking;

import com.beetmacol.speakmod.AudioInput;
import com.beetmacol.speakmod.SpeakMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

@Environment(EnvType.CLIENT)
public class VoiceChatClient {
	//private final DatagramSocket socket;
	private final DatagramSocket serverSocket;
	private final SocketAddress serverSocketAddress;
	private final int voiceServerPort;

	private final AudioInput audioInput;

	private final Thread udpListeningThread;

	private final SourceDataLine outputTest;

	public VoiceChatClient(String address, int port) throws LineUnavailableException, SocketException {
		this.voiceServerPort = port;
		this.audioInput = new AudioInput();
		this.serverSocket = new DatagramSocket();
		this.serverSocketAddress = new InetSocketAddress(address, port);
		//this.socket = new DatagramSocket(serverSocket.getLocalSocketAddress());

		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, format);
		this.outputTest = (SourceDataLine) AudioSystem.getLine(outputInfo);
		this.outputTest.open(format, 4096);

		this.udpListeningThread = new Thread(this::listen, "Voice Chat Packet Lister");
		this.udpListeningThread.start();
	}

	public void terminate() {
		audioInput.terminate();
		udpListeningThread.interrupt();
	}

	private void listen() {
		byte[] data = new byte[4096];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		outputTest.start();
		while (!Thread.interrupted()) {
			try {
				SpeakMod.LOGGER.info("Listening for voice packets from server.");
				serverSocket.receive(packet);
				if (packet.getData()[0] == 0x00) {
					outputTest.write(packet.getData(), 0, packet.getLength());
				}
			} catch (IOException ignored) {
			}
		}
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

	public void sendVoicePacket(byte[] audio) throws IOException {
		audio[0] = 0x01;
		DatagramPacket packet = new DatagramPacket(audio, audio.length, serverSocketAddress);
		serverSocket.send(packet);
	}

	public int getVoiceServerPort() {
		return voiceServerPort;
	}
}

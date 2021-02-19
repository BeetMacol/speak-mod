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

		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, SpeakMod.AUDIO_FRAME_SIZE, 44100, false);
		DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, format);
		this.outputTest = (SourceDataLine) AudioSystem.getLine(outputInfo);
		this.outputTest.open(format, SpeakMod.AUDIO_BUFFER_SIZE);

		this.udpListeningThread = new Thread(this::listen, "Voice Chat Packet Lister");
		this.udpListeningThread.start();
	}

	public void terminate() {
		audioInput.terminate();
		serverSocket.close();
		udpListeningThread.interrupt();
	}

	private void listen() {
		ByteBuffer data = ByteBuffer.allocate(SpeakMod.UDP_PACKET_SIZE);
		DatagramPacket packet = new DatagramPacket(data.array(), SpeakMod.UDP_PACKET_SIZE);
		outputTest.start();
		SpeakMod.LOGGER.info("Listening for voice packets from server.");
		while (!Thread.interrupted()) {
			try {
				data.position(0);
				serverSocket.receive(packet);
				data.get();
				if (data.array()[0] == 0) {
					byte[] audio = new byte[SpeakMod.AUDIO_BUFFER_SIZE];
					data.get(audio);
					outputTest.write(audio, 0, SpeakMod.AUDIO_BUFFER_SIZE);
				}
			} catch (IOException ignored) {
			} catch (Exception exception) {
				SpeakMod.LOGGER.error("Unexpected error while reading an audio packet.", exception);
			}
		}
	}

	public void sendConnectionInitPacket(long auth) throws IOException {
		ByteBuffer data = ByteBuffer.allocate(SpeakMod.UDP_PACKET_SIZE);
		data.put((byte) 0x00);
		data.putLong(auth);
		SpeakMod.LOGGER.info("Auth: {}, Data: {}", auth, data.array());
		serverSocket.send(new DatagramPacket(data.array(), SpeakMod.UDP_PACKET_SIZE, serverSocketAddress));
	}

	public void sendVoicePacket(byte[] audio) throws IOException {
		ByteBuffer data = ByteBuffer.allocate(SpeakMod.UDP_PACKET_SIZE);
		data.put((byte) 0x01);
		data.put(audio);
		serverSocket.send(new DatagramPacket(data.array(), SpeakMod.UDP_PACKET_SIZE, serverSocketAddress));
	}

	public int getVoiceServerPort() {
		return voiceServerPort;
	}
}

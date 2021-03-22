package com.beetmacol.speakmod.networking;

import com.beetmacol.speakmod.AudioInput;
import com.beetmacol.speakmod.SpeakMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class VoiceChatClient {
	private final MinecraftClient client;

	//private final DatagramSocket socket;
	private final DatagramSocket serverSocket;
	private final SocketAddress serverSocketAddress;
	private final int voiceServerPort;

	private final AudioInput audioInput;

	private final Thread udpListeningThread;

	private final SourceDataLine outputTest;

	public VoiceChatClient(String address, int port) throws LineUnavailableException, SocketException {
		this.client = MinecraftClient.getInstance();
		this.voiceServerPort = port;
		this.audioInput = new AudioInput();
		this.serverSocket = new DatagramSocket();
		this.serverSocketAddress = new InetSocketAddress(address, port);
		//this.socket = new DatagramSocket(serverSocket.getLocalSocketAddress());
		DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, SpeakMod.AUDIO_FORMAT);
		this.outputTest = (SourceDataLine) AudioSystem.getLine(outputInfo);
		this.outputTest.open(SpeakMod.AUDIO_FORMAT, SpeakMod.AUDIO_BUFFER_SIZE);

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
					PlayerEntity speakingPlayer = Objects.requireNonNull(MinecraftClient.getInstance().world).getPlayerByUuid(new UUID(data.getLong(), data.getLong()));
					byte[] audio = new byte[SpeakMod.AUDIO_BUFFER_SIZE];
					data.get(audio);
					if (SpeakMod.isScaleVoiceVolume()) {
						double volume = Objects.requireNonNull(client.player).getPos().distanceTo(Objects.requireNonNull(speakingPlayer).getPos()) / SpeakMod.getVoiceChatRange();
						FloatControl volumeControl = (FloatControl) outputTest.getControl(FloatControl.Type.MASTER_GAIN);
						float originalVolume = volumeControl.getValue();
						volumeControl.setValue((float) (volume * -5)); // TODO
						outputTest.write(audio, 0, SpeakMod.AUDIO_BUFFER_SIZE);
						volumeControl.setValue(originalVolume);
					} else {
						outputTest.write(audio, 0, SpeakMod.AUDIO_BUFFER_SIZE);
					}
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

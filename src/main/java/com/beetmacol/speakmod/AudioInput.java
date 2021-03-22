package com.beetmacol.speakmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

@Environment(EnvType.CLIENT)
public class AudioInput {
	private final Thread listeningThread;

	private final TargetDataLine input;

	public AudioInput() throws LineUnavailableException {
		DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, SpeakMod.AUDIO_FORMAT);
		input = (TargetDataLine) AudioSystem.getLine(inputInfo);
		input.open(SpeakMod.AUDIO_FORMAT, SpeakMod.AUDIO_BUFFER_SIZE);

		listeningThread = new Thread(this::listen, "Audio Input Thread");
		listeningThread.start();
	}

	public void terminate() {
		listeningThread.interrupt();
	}

	private void listen() {
		byte[] buff = new byte[SpeakMod.AUDIO_BUFFER_SIZE];

		boolean wasPressed = false;

		while (!Thread.interrupted()) {
			if (SpeakModClient.keyBinding.isPressed()) {
				if (!wasPressed) {
					input.start();
					input.flush();
				}
				input.read(buff, 0, SpeakMod.AUDIO_BUFFER_SIZE);
				try {
					SpeakModClient.voiceChatClient.sendVoicePacket(buff);
				} catch (IOException ignored) {
				}
				wasPressed = true;
			} else {
				if (wasPressed) {
					input.stop();
				}
				wasPressed = false;
			}
		}
	}
}

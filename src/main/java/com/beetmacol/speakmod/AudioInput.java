package com.beetmacol.speakmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

@Environment(EnvType.CLIENT)
public class AudioInput {
	private final Thread listeningThread;
	private final int buffSize = 1024 * 4;

	private final TargetDataLine input;

	public AudioInput() throws LineUnavailableException {
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

		DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, format);
		input = (TargetDataLine) AudioSystem.getLine(inputInfo);
		input.open(format, buffSize);

		listeningThread = new Thread(this::listen, "Audio Input Thread");
		listeningThread.start();
	}

	public void terminate() {
		listeningThread.interrupt();
	}

	private void listen() {
		byte[] buff = new byte[buffSize];

		boolean wasPressed = false;

		while (!Thread.interrupted()) {
			if (SpeakModClient.keyBinding.isPressed()) {
				if (!wasPressed) {
					input.start();
					input.flush();
				}
				input.read(buff, 0, buff.length);
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

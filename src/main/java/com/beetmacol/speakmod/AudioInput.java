package com.beetmacol.speakmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

@Environment(EnvType.CLIENT)
public class AudioInput {
	private final Thread listeningThread;
	private final int buffSize = 1024 * 4;

	private final TargetDataLine input;
	private final SourceDataLine outputTest; // This is a temporary output used to test if the input works correctly. It will be removed as soon as the voice will be sent to other clients

	public AudioInput() throws LineUnavailableException {
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

		DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, format);
		outputTest = (SourceDataLine) AudioSystem.getLine(outputInfo);
		outputTest.open(format, buffSize);

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
					outputTest.start();
					input.flush();
					outputTest.flush();
				}
				outputTest.write(buff, 0, input.read(buff, 0, buff.length));
				wasPressed = true;
			} else {
				if (wasPressed) {
					input.stop();
					outputTest.stop();
				}
				wasPressed = false;
			}
		}
	}
}

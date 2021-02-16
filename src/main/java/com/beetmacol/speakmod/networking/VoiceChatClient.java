package com.beetmacol.speakmod.networking;

import com.beetmacol.speakmod.AudioInput;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.sound.sampled.LineUnavailableException;

@Environment(EnvType.CLIENT)
public class VoiceChatClient {
	public final AudioInput audioInput;

	public VoiceChatClient(int port) throws LineUnavailableException {
		audioInput = new AudioInput();
	}

	public void terminate() {
		audioInput.terminate();
	}
}

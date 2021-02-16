package com.beetmacol.speakmod;

import com.beetmacol.speakmod.networking.ServerSpeakModMetaNetworking;
import com.beetmacol.speakmod.networking.VoiceChatServer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.SERVER)
public class SpeakModServer implements DedicatedServerModInitializer {
	public static VoiceChatServer voiceChatServer;

	@Override
	public void onInitializeServer() {
		ServerSpeakModMetaNetworking.initialize();
		voiceChatServer = new VoiceChatServer();
	}

	@Environment(EnvType.SERVER)
	public static int getPort() {
		// TODO add the port to `server.properties` somehow
		return 25566;
	}
}

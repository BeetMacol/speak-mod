package com.beetmacol.speakmod;

import com.beetmacol.speakmod.networking.ServerSpeakModMetaNetworking;
import com.beetmacol.speakmod.networking.VoiceChatServer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.net.SocketException;

@Environment(EnvType.SERVER)
public class SpeakModServer implements DedicatedServerModInitializer {
	public static VoiceChatServer voiceChatServer;

	@Override
	public void onInitializeServer() {
		ServerSpeakModMetaNetworking.initialize();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			try {
				voiceChatServer = new VoiceChatServer(server);
			} catch (SocketException exception) {
				SpeakMod.LOGGER.error("Could not start voice chat server.", exception);
			}
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> voiceChatServer.terminate());
	}

	@Environment(EnvType.SERVER)
	public static int getVoiceServerPort() {
		// TODO add the port to `server.properties` somehow
		return 25566;
	}
}

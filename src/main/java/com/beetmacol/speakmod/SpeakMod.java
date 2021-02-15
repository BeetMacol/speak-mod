package com.beetmacol.speakmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpeakMod {
	public static final Logger LOGGER = LogManager.getLogger("Speak Mod");

	public void init() {
		LOGGER.debug("Speak Mod initialized.");
	}

	public void clientInit() {
	}

	public void serverInit() {
	}
}

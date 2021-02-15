package com.beetmacol.speakmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class SpeakMod {
	public static final String MOD_ID = "speak-mod";
	public static final Logger LOGGER = LogManager.getLogger("Speak Mod");

	@Environment(EnvType.CLIENT)
	public static KeyBinding keyBinding;
	@Environment(EnvType.CLIENT)
	public static final Identifier SPEACH_INDICATOR_TEXTURE = new Identifier(MOD_ID, "textures/gui/speach_indicator.png");

	public void init() {
		LOGGER.debug("Speak Mod initialized.");
	}

	public void clientInit() {
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.speak-mod.speak", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.multiplayer"));
		HudRenderCallback.EVENT.register(new SpeachIndicatorRenderer());
	}

	public void serverInit() {
	}
}

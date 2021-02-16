package com.beetmacol.speakmod;

import com.beetmacol.speakmod.networking.ClientSpeakModMetaNetworking;
import com.beetmacol.speakmod.networking.VoiceChatClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.beetmacol.speakmod.SpeakMod.ID;

@Environment(EnvType.CLIENT)
public class SpeakModClient implements ClientModInitializer {

	public static VoiceChatClient voiceChatClient;

	public static KeyBinding keyBinding;
	public static final Identifier SPEACH_INDICATOR_TEXTURE = new Identifier(ID, "textures/gui/speach_indicator.png");

	@Override
	public void onInitializeClient() {
		ClientSpeakModMetaNetworking.initialize();
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.speak-mod.speak", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.multiplayer"));
		HudRenderCallback.EVENT.register(new SpeachIndicatorRenderer());
	}
}

package com.beetmacol.speakmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class SpeakMod {
	public static final String MOD_ID = "speak-mod";
	public static final Logger LOGGER = LogManager.getLogger("Speak Mod");

	public static final CustomGameRuleCategory VOICE_CHAT_GAME_RULE_CATEGORY = new CustomGameRuleCategory(new Identifier(MOD_ID, "voice_chat"), new TranslatableText("gamerule.category.voice_chat").styled(style -> style.withBold(true).withColor(Formatting.YELLOW)));
	public static final GameRules.Key<GameRules.BooleanRule> REQUIRE_SPEAK_MOD_GAME_RULE = GameRuleRegistry.register("requireSpeakMod", VOICE_CHAT_GAME_RULE_CATEGORY, GameRuleFactory.createBooleanRule(false));
	public static final GameRules.Key<DoubleRule> VOICE_CHAT_RANGE_GAME_RULE = GameRuleRegistry.register("voiceChatRange", VOICE_CHAT_GAME_RULE_CATEGORY, GameRuleFactory.createDoubleRule(20d, 0d));
	public static final GameRules.Key<GameRules.BooleanRule> SCALE_VOICE_VELOCITY_GAME_RULE = GameRuleRegistry.register("scaleVoiceVelocity", VOICE_CHAT_GAME_RULE_CATEGORY, GameRuleFactory.createBooleanRule(true));

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

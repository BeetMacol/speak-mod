package com.beetmacol.speakmod;

import com.beetmacol.speakmod.networking.SpeakModServerNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpeakMod {
	public static final String ID = "speak-mod";
	public static final String VERSION = "1.0.0";
	public static final Logger LOGGER = LogManager.getLogger("Speak Mod");
	public static final Identifier HANDSHAKE_CHANNEL = new Identifier(ID, "meta"); // This plugin channel is used for checking if the other side has Speak Mod installed and if the version protocol is correct
	public static final int PROTOCOL_VERSION = 0;

	public static final CustomGameRuleCategory VOICE_CHAT_GAME_RULE_CATEGORY = new CustomGameRuleCategory(new Identifier(ID, "voice_chat"), new TranslatableText("gamerule.category.voice_chat").styled(style -> style.withBold(true).withColor(Formatting.YELLOW)));
	public static final GameRules.Key<GameRules.BooleanRule> REQUIRE_SPEAK_MOD_GAME_RULE = GameRuleRegistry.register("requireSpeakMod", VOICE_CHAT_GAME_RULE_CATEGORY, GameRuleFactory.createBooleanRule(false));
	public static final GameRules.Key<DoubleRule> VOICE_CHAT_RANGE_GAME_RULE = GameRuleRegistry.register("voiceChatRange", VOICE_CHAT_GAME_RULE_CATEGORY, GameRuleFactory.createDoubleRule(20d, 0d));
	public static final GameRules.Key<GameRules.BooleanRule> SCALE_VOICE_VELOCITY_GAME_RULE = GameRuleRegistry.register("scaleVoiceVelocity", VOICE_CHAT_GAME_RULE_CATEGORY, GameRuleFactory.createBooleanRule(true));

	public static void initialize() {
		LOGGER.debug("Speak Mod initialized.");
	}

	@Environment(EnvType.SERVER)
	public static void initializeServer() {
		SpeakModServerNetworking.initialize();
	}
}

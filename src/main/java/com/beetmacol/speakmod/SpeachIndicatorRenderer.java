package com.beetmacol.speakmod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class SpeachIndicatorRenderer implements HudRenderCallback {
	static int frame = 0;
	static float timer = 0f;

	@Override
	public void onHudRender(MatrixStack matrices, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (SpeakMod.keyBinding.isPressed()) {
			int scaledWidth = client.getWindow().getScaledWidth();
			int scaledHeight = client.getWindow().getScaledHeight();
			int x = scaledWidth / 2 - 8;
			int y = scaledHeight / 2 + 16;

			timer += tickDelta;
			if (timer > 7.5f) {
				if (frame < 2) frame++;
				else frame = 0;
				timer -= 7.5f;
			}

			client.getTextureManager().bindTexture(SpeakMod.SPEACH_INDICATOR_TEXTURE);
			RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
			DrawableHelper.drawTexture(matrices, x, y, 0, frame * 16, 16, 16, 16, 80);
		}
	}
}

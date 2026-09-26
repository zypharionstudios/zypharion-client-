package com.example.client.mixin;

import com.example.client.ClientModules;
import com.example.client.FreecamCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class FreecamLookMixin {
	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	private void directLookToFreecam(double mouseX, double mouseY, CallbackInfo callback) {
		if (!((Object) this instanceof LocalPlayer)
				|| !ClientModules.isEnabled(ClientModules.Module.FREECAM)) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.gameRenderer.mainCamera().entity() instanceof FreecamCamera camera) {
			camera.turn(mouseX, mouseY);
			callback.cancel();
		}
	}
}
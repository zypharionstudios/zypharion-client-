package com.example.client.mixin;

import com.example.client.ClientModules;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class XrayOcclusionMixin {
	@Inject(method = "isSolidRender", at = @At("HEAD"), cancellable = true)
	private void disableChunkOcclusion(CallbackInfoReturnable<Boolean> callback) {
		if (ClientModules.isEnabled(ClientModules.Module.XRAY)) {
			callback.setReturnValue(false);
		}
	}
}
package com.example.client.mixin;

import com.example.client.ClientModules;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBlockRenderer.class)
public abstract class XrayMixin {
	@Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
	private void revealOreFaces(BlockAndTintGetter level, BlockState state, Direction direction,
			BlockPos neighborPos, CallbackInfoReturnable<Boolean> callback) {
		if (ClientModules.isEnabled(ClientModules.Module.XRAY) && isOre(state)) {
			callback.setReturnValue(true);
		}
	}

	@Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
	private void hideNonOres(BlockQuadOutput output, float red, float green, float blue,
			BlockAndTintGetter level, BlockPos pos, BlockState state, BlockStateModel model,
			long seed, CallbackInfo callback) {
		if (ClientModules.isEnabled(ClientModules.Module.XRAY) && !isOre(state)) {
			callback.cancel();
		}
	}

	private static boolean isOre(BlockState state) {
		return state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)
				|| state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)
				|| state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)
				|| state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)
				|| state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
				|| state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
				|| state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
				|| state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)
				|| state.is(Blocks.NETHER_QUARTZ_ORE) || state.is(Blocks.NETHER_GOLD_ORE)
				|| state.is(Blocks.ANCIENT_DEBRIS);
	}
}
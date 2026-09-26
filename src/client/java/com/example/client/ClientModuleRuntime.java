package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class ClientModuleRuntime {
	private static double previousGamma;
	private static boolean capturedGamma;
	private static boolean enabledFlight;
	private static int totemTick;
	private static FreecamCamera camera;
	private static Vec3 playerAnchor;
	private static LocalPlayer freecamOwner;
	private static ClientLevel freecamLevel;

	private ClientModuleRuntime() {
	}

	public static void onToggle(ClientModules.Module module) {
		if (module == ClientModules.Module.XRAY) {
			refreshChunks();
		}
	}

	public static void tick(Minecraft client) {
		updateFullbright(client);
		if (client.player == null) {
			stopFreecam(client);
			return;
		}

		updateFlight(client);
		updateAutoTotem(client);
		updateFreecam(client);
	}

	private static void updateFullbright(Minecraft client) {
		if (ClientModules.isEnabled(ClientModules.Module.FULLBRIGHT)) {
			if (!capturedGamma) {
				previousGamma = client.options.gamma().get();
				capturedGamma = true;
			}
			client.options.gamma().set(16.0);
		} else if (capturedGamma) {
			client.options.gamma().set(previousGamma);
			capturedGamma = false;
		}
	}

	private static void updateFlight(Minecraft client) {
		boolean enabled = ClientModules.isEnabled(ClientModules.Module.FLY);
		if (enabled && client.player.getAbilities().mayfly && !client.player.getAbilities().flying) {
			client.player.getAbilities().flying = true;
			enabledFlight = true;
		} else if (!enabled && enabledFlight) {
			if (client.player.getAbilities().mayfly) {
				client.player.getAbilities().flying = false;
			}
			enabledFlight = false;
		}
	}

	private static void updateAutoTotem(Minecraft client) {
		if (!ClientModules.isEnabled(ClientModules.Module.AUTO_TOTEM) || ++totemTick % 10 != 0
				|| client.gameMode == null) {
			return;
		}

		var inventory = client.player.getInventory();
		if (inventory.getItem(Inventory.SLOT_OFFHAND).is(Items.TOTEM_OF_UNDYING)) {
			return;
		}

		for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
			if (!inventory.getItem(inventorySlot).is(Items.TOTEM_OF_UNDYING)) {
				continue;
			}

			int menuSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
			client.gameMode.handleContainerInput(
					client.player.inventoryMenu.containerId,
					menuSlot,
					Inventory.SLOT_OFFHAND,
					ContainerInput.SWAP,
					client.player
			);
			return;
		}
	}

	private static void updateFreecam(Minecraft client) {
		if (camera != null && (client.player != freecamOwner || client.level != freecamLevel)) {
			stopFreecam(client);
		}
		if (!ClientModules.isEnabled(ClientModules.Module.FREECAM)) {
			stopFreecam(client);
			return;
		}

		if (camera == null) {
			freecamOwner = client.player;
			freecamLevel = client.level;
			playerAnchor = client.player.position();
			camera = new FreecamCamera(client.player);
			client.setCameraEntity(camera);
		}

		client.player.setPos(playerAnchor);
		client.player.setDeltaMovement(Vec3.ZERO);
		if (client.gui.screen() != null) {
			return;
		}
		float yawRadians = camera.getYRot() * ((float) Math.PI / 180.0F);
		Vec3 forward = new Vec3(-Math.sin(yawRadians), 0.0, Math.cos(yawRadians));
		Vec3 right = new Vec3(Math.cos(yawRadians), 0.0, Math.sin(yawRadians));
		Vec3 movement = Vec3.ZERO;
		if (client.options.keyUp.isDown()) movement = movement.add(forward);
		if (client.options.keyDown.isDown()) movement = movement.subtract(forward);
		if (client.options.keyRight.isDown()) movement = movement.add(right);
		if (client.options.keyLeft.isDown()) movement = movement.subtract(right);
		if (client.options.keyJump.isDown()) movement = movement.add(0.0, 1.0, 0.0);
		if (client.options.keyShift.isDown()) movement = movement.subtract(0.0, 1.0, 0.0);

		if (movement.lengthSqr() > 0.0) {
			double speed = client.options.keySprint.isDown() ? 1.0 : 0.35;
			camera.setPos(camera.position().add(movement.normalize().scale(speed)));
		}
	}

	private static void stopFreecam(Minecraft client) {
		if (camera == null) {
			return;
		}
		if (client.getCameraEntity() == camera) {
			client.setCameraEntity(client.player);
		}
		if (client.player == freecamOwner && playerAnchor != null) {
			freecamOwner.setPos(playerAnchor);
			freecamOwner.setDeltaMovement(Vec3.ZERO);
		}
		camera = null;
		playerAnchor = null;
		freecamOwner = null;
		freecamLevel = null;
	}

	private static void refreshChunks() {
		Minecraft client = Minecraft.getInstance();
		if (client.level != null) {
			client.levelRenderer.invalidateCompiledGeometry(
					client.level,
					client.options,
					client.gameRenderer.mainCamera(),
					client.getBlockColors()
			);
		}
	}
}
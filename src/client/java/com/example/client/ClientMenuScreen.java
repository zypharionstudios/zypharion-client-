package com.example.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;

public final class ClientMenuScreen extends Screen {
	private enum Category {
		COMBAT("Combat"),
		PLAYER("Player"),
		MOVEMENT("Movement"),
		RENDER("Render"),
		WORLD("World"),
		MISC("Misc");

		private final String label;

		Category(String label) {
			this.label = label;
		}
	}

	private final Map<ClientModules.Module, Button> moduleButtons = new EnumMap<>(ClientModules.Module.class);
	private EditBox searchBox;

	public ClientMenuScreen() {
		super(Component.literal("Zypharion Client"));
	}

	@Override
	protected void init() {
		moduleButtons.clear();
		int panelWidth = panelWidth();
		int left = (this.width - panelWidth) / 2;
		int top = (this.height - panelHeight()) / 2;
		int columns = panelWidth >= 660 ? 6 : panelWidth >= 430 ? 3 : 2;
		int gap = 6;
		int innerWidth = panelWidth - 32;
		int columnWidth = (innerWidth - gap * (columns - 1)) / columns;
		int categoryTop = top + 62;
		int categoryBlockHeight = 94;

		searchBox = new EditBox(this.font, left + panelWidth - 194, top + 17, 174, 18,
				Component.literal("Search modules"));
		searchBox.setMaxLength(32);
		searchBox.setHint(Component.literal("Search modules"));
		searchBox.setResponder(value -> filterModules());
		this.addRenderableWidget(searchBox);

		Category[] categories = Category.values();
		for (int index = 0; index < categories.length; index++) {
			Category category = categories[index];
			int column = index % columns;
			int row = index / columns;
			int x = left + 16 + column * (columnWidth + gap);
			int y = categoryTop + row * categoryBlockHeight;
			int moduleRow = 0;
			for (ClientModules.Module module : ClientModules.Module.values()) {
				if (categoryFor(module) != category) {
					continue;
				}
				int buttonY = y + 23 + moduleRow * 21;
				Button button = Button.builder(moduleLabel(module), widget -> {
					ClientModules.toggle(module);
					widget.setMessage(moduleLabel(module));
				}).bounds(x, buttonY, columnWidth, 18).build();
				moduleButtons.put(module, button);
				this.addRenderableWidget(button);
				moduleRow++;
			}
		}

		this.addRenderableWidget(Button.builder(Component.literal("CLOSE"), button -> this.onClose())
				.bounds(left + panelWidth - 82, top + panelHeight() - 27, 66, 18)
				.build());
		filterModules();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int panelWidth = panelWidth();
		int panelHeight = panelHeight();
		int left = (this.width - panelWidth) / 2;
		int top = (this.height - panelHeight) / 2;
		int columns = panelWidth >= 660 ? 6 : panelWidth >= 430 ? 3 : 2;
		int gap = 6;
		int innerWidth = panelWidth - 32;
		int columnWidth = (innerWidth - gap * (columns - 1)) / columns;
		int categoryTop = top + 62;
		int categoryBlockHeight = 94;

		graphics.fill(0, 0, this.width, this.height, 0x9504070B);
		graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xE80A0D13);
		graphics.fill(left, top, left + panelWidth, top + 2, 0xFF9D43F5);
		graphics.outline(left, top, panelWidth, panelHeight, 0xFF443252);
		graphics.fill(left + 15, top + 15, left + 18, top + 42, 0xFF9D43F5);
		graphics.text(this.font, "ZYPHARION", left + 25, top + 14, 0xFFF4ECFF, true);
		graphics.text(this.font, "CLIENT  /  26.2", left + 25, top + 30, 0xFFAA9BB9, false);
		graphics.fill(left + 15, top + 53, left + panelWidth - 15, top + 54, 0xFF34283E);

		Category[] categories = Category.values();
		for (int index = 0; index < categories.length; index++) {
			int column = index % columns;
			int row = index / columns;
			int x = left + 16 + column * (columnWidth + gap);
			int y = categoryTop + row * categoryBlockHeight;
			graphics.fill(x, y, x + columnWidth, y + 18, 0xFF7031B3);
			graphics.text(this.font, categories[index].label, x + 5, y + 5, 0xFFFFFFFF, true);
		}

		graphics.text(this.font, "" + enabledCount() + " ACTIVE", left + 16,
				top + panelHeight - 22, 0xFFB9A8C8, false);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void filterModules() {
		String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase();
		for (Map.Entry<ClientModules.Module, Button> entry : moduleButtons.entrySet()) {
			boolean matches = query.isEmpty() || entry.getKey().label().toLowerCase().contains(query);
			entry.getValue().visible = matches;
		}
	}

	private int panelWidth() {
		return Math.min(760, Math.max(280, this.width - 16));
	}

	private int panelHeight() {
		return Math.min(390, Math.max(230, this.height - 16));
	}

	private static Category categoryFor(ClientModules.Module module) {
		return switch (module) {
			case AUTO_TOTEM -> Category.PLAYER;
			case FREECAM, FLY -> Category.MOVEMENT;
			case XRAY, FULLBRIGHT, NO_FOG -> Category.RENDER;
		};
	}

	private static Component moduleLabel(ClientModules.Module module) {
		return Component.literal((ClientModules.isEnabled(module) ? "+ " : "- ") + module.label());
	}

	private static int enabledCount() {
		int count = 0;
		for (ClientModules.Module module : ClientModules.Module.values()) {
			if (ClientModules.isEnabled(module)) {
				count++;
			}
		}
		return count;
	}
}
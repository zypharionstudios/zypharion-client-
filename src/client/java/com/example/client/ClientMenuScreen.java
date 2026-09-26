package com.example.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ClientMenuScreen extends Screen {
	private enum Category {
		PLAYER("Player"),
		MOVEMENT("Movement"),
		RENDER("Render");

		private final String label;

		Category(String label) {
			this.label = label;
		}
	}

	private final Map<ClientModules.Module, Button> moduleButtons = new EnumMap<>(ClientModules.Module.class);
	private final Map<Category, Button> categoryButtons = new EnumMap<>(Category.class);
	private Category selectedCategory = Category.MOVEMENT;
	private EditBox searchBox;

	public ClientMenuScreen() {
		super(Component.literal("Zypharion Client"));
	}

	@Override
	protected void init() {
		moduleButtons.clear();
		categoryButtons.clear();
		int left = left();
		int top = top();
		int width = panelWidth();
		int categoryY = top + 56;
		int gap = 5;
		int tabWidth = (width - 30 - gap * (Category.values().length - 1)) / Category.values().length;

		searchBox = new EditBox(this.font, left + 16, top + 91, width - 32, 20,
				Component.literal("Search modules"));
		searchBox.setMaxLength(32);
		searchBox.setHint(Component.literal("Search modules"));
		searchBox.setResponder(value -> filterModules());
		this.addRenderableWidget(searchBox);

		for (int index = 0; index < Category.values().length; index++) {
			Category category = Category.values()[index];
			int x = left + 15 + index * (tabWidth + gap);
			Button button = Button.builder(categoryLabel(category), widget -> {
				selectedCategory = category;
				refreshCategoryButtons();
				filterModules();
			}).bounds(x, categoryY, tabWidth, 20).build();
			categoryButtons.put(category, button);
			this.addRenderableWidget(button);
		}

		int contentTop = top + 126;
		int contentWidth = width - 32;
		int columnGap = 8;
		int columnWidth = (contentWidth - columnGap) / 2;
		for (int index = 0; index < ClientModules.Module.values().length; index++) {
			ClientModules.Module module = ClientModules.Module.values()[index];
			int column = index % 2;
			int row = index / 2;
			int x = left + 16 + column * (columnWidth + columnGap);
			int y = contentTop + row * 25;
			Button button = Button.builder(moduleLabel(module), widget -> {
				ClientModules.toggle(module);
				widget.setMessage(moduleLabel(module));
			}).bounds(x, y, columnWidth, 21).build();
			moduleButtons.put(module, button);
			this.addRenderableWidget(button);
		}

		this.addRenderableWidget(Button.builder(Component.literal("CLOSE"), button -> this.onClose())
				.bounds(left + width - 80, top + panelHeight() - 29, 64, 20)
				.build());
		filterModules();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int left = left();
		int top = top();
		int width = panelWidth();
		int height = panelHeight();

		graphics.fill(left - 5, top - 5, left + width + 5, top + height + 5, 0x70000000);
		graphics.fill(left, top, left + width, top + height, 0xEE090B11);
		graphics.fill(left, top, left + width, top + 2, 0xFF9D43F5);
		graphics.outline(left, top, width, height, 0xFF493258);
		graphics.fill(left + 14, top + 13, left + 17, top + 40, 0xFF9D43F5);
		graphics.text(this.font, "ZYPHARION", left + 24, top + 12, 0xFFF5EDFF, true);
		graphics.text(this.font, "CLIENT  /  26.2", left + 24, top + 29, 0xFFB3A3C0, false);
		graphics.text(this.font, "X", left + width - 23, top + 17, 0xFFB3A3C0, false);
		graphics.fill(left + 14, top + 46, left + width - 14, top + 47, 0xFF34283E);
		graphics.text(this.font, enabledCount() + " ACTIVE", left + 16,
				top + height - 23, 0xFFB9A8C8, false);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void filterModules() {
		String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase();
		List<ClientModules.Module> matching = new ArrayList<>();
		for (ClientModules.Module module : ClientModules.Module.values()) {
			boolean categoryMatches = categoryFor(module) == selectedCategory;
			boolean queryMatches = query.isEmpty() || module.label().toLowerCase().contains(query);
			if (categoryMatches && queryMatches) {
				matching.add(module);
			}
		}

		for (Map.Entry<ClientModules.Module, Button> entry : moduleButtons.entrySet()) {
			entry.getValue().visible = matching.contains(entry.getKey());
		}
	}

	private void refreshCategoryButtons() {
		for (Map.Entry<Category, Button> entry : categoryButtons.entrySet()) {
			entry.getValue().setMessage(categoryLabel(entry.getKey()));
		}
	}

	private int panelWidth() {
		return Math.min(460, Math.max(300, this.width - 16));
	}

	private int panelHeight() {
		return Math.min(430, Math.max(250, this.height - 16));
	}

	private int left() {
		return this.width - panelWidth() - 18;
	}

	private int top() {
		return Math.max(8, (this.height - panelHeight()) / 2);
	}

	private static Category categoryFor(ClientModules.Module module) {
		return switch (module) {
			case AUTO_TOTEM -> Category.PLAYER;
			case FREECAM, FLY -> Category.MOVEMENT;
			case XRAY, FULLBRIGHT, NO_FOG -> Category.RENDER;
		};
	}

	private static Component moduleLabel(ClientModules.Module module) {
		return Component.literal((ClientModules.isEnabled(module) ? "[ON] " : "[OFF] ") + module.label());
	}

	private Component categoryLabel(Category category) {
		return Component.literal((category == selectedCategory ? "> " : "") + category.label);
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
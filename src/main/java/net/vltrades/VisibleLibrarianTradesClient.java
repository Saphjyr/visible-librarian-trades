package net.vltrades;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ClientModInitializer;


public class VisibleLibrarianTradesClient implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static EnchantmentManager enchantmentManager = null;
	public static LecternManager lecternManager = null;
	public static Boolean displayIcons = false;

	@Environment(EnvType.CLIENT)
	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		enchantmentManager = new EnchantmentManager();
		lecternManager = new LecternManager();
	}

	public static EnchantmentManager getEnchantmentManager() {
		return enchantmentManager;
	}

	public static LecternManager getLecternManager() {
		return lecternManager;
	}

	public static Boolean getDisplayIcons() {
		return displayIcons;
	}

	public static void toggleDisplayIcons() {
		displayIcons = !displayIcons;
	}

}

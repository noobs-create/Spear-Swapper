package com.s0ftpulse.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SpearHelperClient implements ClientModInitializer {
	public KeyMapping swapAndClickKey;
	private int originalSlot = -1;

	public static final Logger LOGGER = LoggerFactory.getLogger("spearhelper");

	@Override
	public void onInitializeClient() {
		// Добавляем кнопку в меню настроеук управление

		Identifier categoryId = Identifier.fromNamespaceAndPath("spearhelper", "main");
		KeyMapping.Category customCategory = new KeyMapping.Category(categoryId);

		swapAndClickKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.spearhelper.swap_click",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				customCategory
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) return;

			if (swapAndClickKey.isDown()) {
				int spearSlot = findLungeSpear(client);
				if (originalSlot == -1) {
					originalSlot = client.player.getInventory().getSelectedSlot();
				}

				if (client.player.getAttackStrengthScale(0) >= 0.75f) {
					if (spearSlot != -1) {
						client.player.getInventory().setSelectedSlot(spearSlot);
						try {
							java.lang.reflect.Method method = Minecraft.class.getDeclaredMethod("startAttack");
							method.setAccessible(true);
							method.invoke(client);
						} catch (Exception e) {
							LOGGER.error("Failed to call startAttack: " + e.getMessage());
						}
						client.player.getInventory().setSelectedSlot(originalSlot);
						originalSlot = -1;
					}
				}
			} else {
				originalSlot = -1;
			}
		});
	}

	private int findLungeSpear(Minecraft client) {

		Item[] spears = {Items.WOODEN_SPEAR, Items.STONE_SPEAR, Items.COPPER_SPEAR, Items.GOLDEN_SPEAR, Items.IRON_SPEAR, Items.DIAMOND_SPEAR, Items.NETHERITE_SPEAR};

		for (int i = 0; i < 9; i++) {
			if (client.player != null) {
				ItemStack stack = client.player.getInventory().getItem(i);

			if (Arrays.asList(spears).contains(stack.getItem())) {
				return i;
			}
			}
		}

		return -1;
	}
}
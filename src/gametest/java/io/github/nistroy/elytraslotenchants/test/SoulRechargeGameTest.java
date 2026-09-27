package io.github.nistroy.elytraslotenchants.test;

import dev.emi.trinkets.api.TrinketsApi;
import io.github.nistroy.elytraslotenchants.SoulRecharge;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

public class SoulRechargeGameTest implements FabricGameTest {
	// Recharge du boost de Deeper and Darker (`soulElytraCooldown`, 600 par défaut) : les tests passent la même.
	private static final int BOOST_COOLDOWN = 600;

	@GameTest(template = EMPTY_STRUCTURE)
	public void soldByLibrariansForSoulElytraOnly(GameTestHelper helper) {
		Holder<Enchantment> recharge = soulRecharge(helper);

		helper.assertTrue(recharge.is(EnchantmentTags.TRADEABLE), "les bibliothécaires doivent pouvoir le vendre");
		helper.assertFalse(recharge.is(EnchantmentTags.IN_ENCHANTING_TABLE), "pas à la table d'enchantement");
		helper.assertTrue(recharge.value().getMaxLevel() == 3, "3 niveaux");
		helper.assertTrue(recharge.value().isSupportedItem(new ItemStack(soulElytra())), "Élytre des âmes acceptée");
		helper.assertFalse(recharge.value().isSupportedItem(new ItemStack(Items.ELYTRA)), "élytre vanilla refusée");
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void boostCooldownUnchangedWithoutEnchantment(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(soulElytra()));

		assertBoostCooldown(helper, player, BOOST_COOLDOWN);
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void boostCooldownShortenedByLevelOnChest(GameTestHelper helper) {
		int[] expected = {440, 300, 160}; // 22 s, 15 s, 8 s
		for (int level = 1; level <= 3; level++) {
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			player.setItemSlot(EquipmentSlot.CHEST, enchantedSoulElytra(helper, level));

			assertBoostCooldown(helper, player, expected[level - 1]);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void boostCooldownShortenedInElytraSlot(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		TrinketsApi.getTrinketComponent(player).orElseThrow().getInventory().get("chest").get("cape")
				.setItem(0, enchantedSoulElytra(helper, 3));

		assertBoostCooldown(helper, player, 160);
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void otherItemCooldownsUntouched(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setItemSlot(EquipmentSlot.CHEST, enchantedSoulElytra(helper, 3));

		ItemCooldowns cooldowns = player.getCooldowns();
		cooldowns.addCooldown(Items.ENDER_PEARL, 20);
		int ticks = ticksUntilReady(cooldowns, Items.ENDER_PEARL);
		helper.assertTrue(ticks == 20, "la perle de l'Ender garde 20 ticks, obtenu " + ticks);
		helper.succeed();
	}

	// Même appel que le boost de Deeper and Darker (DDNetworking, relu au javap) : addCooldown(SOUL_ELYTRA, config).
	private static void assertBoostCooldown(GameTestHelper helper, ServerPlayer player, int expected) {
		ItemCooldowns cooldowns = player.getCooldowns();
		cooldowns.addCooldown(soulElytra(), BOOST_COOLDOWN);
		int ticks = ticksUntilReady(cooldowns, soulElytra());
		helper.assertTrue(ticks == expected, "recharge attendue " + expected + " ticks, obtenu " + ticks);
	}

	private static int ticksUntilReady(ItemCooldowns cooldowns, Item item) {
		int ticks = 0;
		while (cooldowns.isOnCooldown(item) && ticks <= BOOST_COOLDOWN) {
			cooldowns.tick();
			ticks++;
		}
		return ticks;
	}

	private static ItemStack enchantedSoulElytra(GameTestHelper helper, int level) {
		ItemStack elytra = new ItemStack(soulElytra());
		elytra.enchant(soulRecharge(helper), level);
		return elytra;
	}

	private static Holder<Enchantment> soulRecharge(GameTestHelper helper) {
		return helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
				.getHolderOrThrow(SoulRecharge.ENCHANTMENT);
	}

	private static Item soulElytra() {
		return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("deeperdarker", "soul_elytra"));
	}
}

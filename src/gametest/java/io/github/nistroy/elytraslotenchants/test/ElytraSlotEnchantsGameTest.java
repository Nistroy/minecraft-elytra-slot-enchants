package io.github.nistroy.elytraslotenchants.test;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.event.TrinketUnequipCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Optional;

public class ElytraSlotEnchantsGameTest implements FabricGameTest {
	// Enchantement de test (données du jeu de tests) : effet location_changed comme Graviole, sans condition de vol.
	private static final ResourceKey<Enchantment> LIGHTER = ResourceKey.create(Registries.ENCHANTMENT,
			ResourceLocation.fromNamespaceAndPath("elytraslotenchants_test", "lighter"));
	// EnchantmentAttributeEffect suffixe l'id avec l'emplacement : l'élytre du slot est vue au torse.
	private static final ResourceLocation LIGHTER_MODIFIER =
			ResourceLocation.fromNamespaceAndPath("elytraslotenchants_test", "lighter/chest");

	@GameTest(template = EMPTY_STRUCTURE)
	public void protectionOnSlotElytraCounts(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ServerLevel level = helper.getLevel();
		helper.assertTrue(EnchantmentHelper.getDamageProtection(level, player, level.damageSources().generic()) == 0,
				"aucune protection sans élytre");

		equipInSlot(player, elytraWith(helper, Enchantments.PROTECTION, 4));

		float protection = EnchantmentHelper.getDamageProtection(level, player, level.damageSources().generic());
		helper.assertTrue(protection == 4, "Protection IV sur l'élytre du slot doit compter 4, obtenu " + protection);
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void mendingRepairsSlotElytra(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ItemStack elytra = elytraWith(helper, Enchantments.MENDING, 1);
		elytra.setDamageValue(100);
		equipInSlot(player, elytra);

		Optional<EnchantedItemInUse> picked =
				EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, player, ItemStack::isDamaged);

		helper.assertTrue(picked.isPresent() && picked.get().itemStack() == elytra,
				"Mending doit choisir l'élytre du slot");
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void locationEffectStartsAndStopsWithSlotElytra(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ServerLevel level = helper.getLevel();
		ItemStack elytra = elytraWith(helper, LIGHTER, 1);
		equipInSlot(player, elytra);

		EnchantmentHelper.runLocationChangedEffects(level, player);
		helper.assertTrue(player.getAttribute(Attributes.GRAVITY).hasModifier(LIGHTER_MODIFIER),
				"l'effet de position doit s'appliquer avec l'élytre du slot");

		equipInSlot(player, ItemStack.EMPTY);
		// Trinkets lance cet événement depuis LivingEntity.tick, mais saute les entités retirées : le joueur
		// factice l'est dès son arrivée (sa connexion factice refuse le paquet de synchro de Cardinal Components).
		TrinketUnequipCallback.EVENT.invoker().onUnequip(elytra, new SlotReference(capeSlot(player), 0), player);
		helper.assertFalse(player.getAttribute(Attributes.GRAVITY).hasModifier(LIGHTER_MODIFIER),
				"l'effet doit s'arrêter quand l'élytre quitte le slot");
		helper.succeed();
	}

	private static ItemStack elytraWith(GameTestHelper helper, ResourceKey<Enchantment> key, int enchantmentLevel) {
		Holder<Enchantment> enchantment = helper.getLevel().registryAccess()
				.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
		ItemStack elytra = new ItemStack(Items.ELYTRA);
		elytra.enchant(enchantment, enchantmentLevel);
		return elytra;
	}

	// Slot `chest/cape` de Trinkets : celui qu'Elytra Slot ouvre aux élytres (data/trinkets/entities/elytra.json).
	private static void equipInSlot(ServerPlayer player, ItemStack stack) {
		capeSlot(player).setItem(0, stack);
	}

	private static TrinketInventory capeSlot(ServerPlayer player) {
		return TrinketsApi.getTrinketComponent(player).orElseThrow().getInventory().get("chest").get("cape");
	}
}

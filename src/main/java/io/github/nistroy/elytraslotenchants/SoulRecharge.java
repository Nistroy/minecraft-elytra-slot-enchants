package io.github.nistroy.elytraslotenchants;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;

/** Enchantement « Recharge des âmes » : raccourcit la recharge du boost de l'Élytre des âmes. */
public final class SoulRecharge {
	public static final ResourceKey<Enchantment> ENCHANTMENT = ResourceKey.create(Registries.ENCHANTMENT,
			ResourceLocation.fromNamespaceAndPath("elytraslotenchants", "soul_recharge"));

	private static final ResourceLocation SOUL_ELYTRA =
			ResourceLocation.fromNamespaceAndPath("deeperdarker", "soul_elytra");

	// Part de la recharge gardée par niveau : 22 s, 15 s, 8 s avec les 30 s par défaut de Deeper and Darker.
	private static final float[] KEPT = {22f / 30, 15f / 30, 8f / 30};

	private SoulRecharge() {
	}

	/**
	 * Recharge à poser sur un objet du joueur. Deeper and Darker ne pose de recharge sur l'Élytre des âmes
	 * qu'au boost, avec la durée de sa config (`soulElytraCooldown`).
	 */
	public static int cooldown(ServerPlayer player, Item item, int ticks) {
		if (!BuiltInRegistries.ITEM.getKey(item).equals(SOUL_ELYTRA)) {
			return ticks;
		}
		int level = wornLevel(player, item);
		return level == 0 ? ticks : Math.round(ticks * KEPT[Math.min(level, KEPT.length) - 1]);
	}

	// Élytre au torse ou dans le slot d'Elytra Slot : Deeper and Darker accepte le boost depuis les deux.
	private static int wornLevel(ServerPlayer player, Item soulElytra) {
		Holder<Enchantment> recharge = player.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
				.getHolderOrThrow(ENCHANTMENT);
		int level = levelOn(player.getItemBySlot(EquipmentSlot.CHEST), soulElytra, recharge);
		List<Tuple<SlotReference, ItemStack>> inSlot = TrinketsApi.getTrinketComponent(player)
				.map(component -> component.getEquipped(soulElytra))
				.orElse(List.of());
		for (Tuple<SlotReference, ItemStack> slotAndStack : inSlot) {
			level = Math.max(level, levelOn(slotAndStack.getB(), soulElytra, recharge));
		}
		return level;
	}

	private static int levelOn(ItemStack stack, Item soulElytra, Holder<Enchantment> recharge) {
		return stack.is(soulElytra) ? EnchantmentHelper.getItemEnchantmentLevel(recharge, stack) : 0;
	}
}

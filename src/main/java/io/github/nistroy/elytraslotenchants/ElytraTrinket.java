package io.github.nistroy.elytraslotenchants;

import dev.emi.trinkets.TrinketSlotTarget;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

/** Élytre portée dans le slot Trinkets d'Elytra Slot, vue comme si elle était au torse. */
public final class ElytraTrinket {
	/** Graviole (Enchants+) déclare `chest` : c'est l'emplacement sous lequel l'élytre du slot est vue. */
	public static final EquipmentSlot SEEN_AS = EquipmentSlot.CHEST;

	// Tag d'Elytra Slot : les items qu'il accepte dans son slot (élytre vanilla, Élytre des âmes…).
	private static final TagKey<Item> ELYTRA =
			TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("elytraslot", "elytra"));

	private ElytraTrinket() {
	}

	public static boolean isElytra(ItemStack stack) {
		return stack.is(ELYTRA);
	}

	/**
	 * Donne au visiteur les enchantements des élytres du slot que Trinkets laisse de côté. Trinkets applique
	 * déjà ceux dont les emplacements contiennent `any` ou `armor` (Mending, Skyguard, Protection…) ou qui
	 * listent le slot dans `trinkets:slots` (EnchantmentHelperMixin de Trinkets 3.10.0) : les reprendre les
	 * compterait deux fois.
	 */
	public static void forEachEnchantmentTrinketsSkips(LivingEntity entity,
			EnchantmentHelper.EnchantmentInSlotVisitor visitor) {
		// Elytra Slot n'ouvre son slot qu'aux joueurs (data/trinkets/entities/elytra.json) : inutile de
		// chercher sur les centaines de mobs, dont les enchantements sont parcourus à chaque tick.
		if (!(entity instanceof Player)) {
			return;
		}
		List<Tuple<SlotReference, ItemStack>> equipped = TrinketsApi.getTrinketComponent(entity)
				.map(component -> component.getEquipped(ElytraTrinket::isElytra))
				.orElse(List.of());
		for (Tuple<SlotReference, ItemStack> slotAndStack : equipped) {
			ItemStack stack = slotAndStack.getB();
			ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
			if (enchantments.isEmpty()) {
				continue;
			}
			String slotId = slotAndStack.getA().inventory().getSlotType().getId();
			EnchantedItemInUse inUse = new EnchantedItemInUse(stack, SEEN_AS, entity);
			for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
				Enchantment enchantment = entry.getKey().value();
				if (enchantment.matchingSlot(SEEN_AS) && !appliedByTrinkets(enchantment, slotId)) {
					visitor.accept(entry.getKey(), entry.getIntValue(), inUse);
				}
			}
		}
	}

	// Même règle que le mixin de Trinkets (lambda forEachTrinket, relue au javap sur trinkets-3.10.0.jar).
	private static boolean appliedByTrinkets(Enchantment enchantment, String slotId) {
		List<EquipmentSlotGroup> groups = enchantment.definition().slots();
		return groups.contains(EquipmentSlotGroup.ANY)
				|| groups.contains(EquipmentSlotGroup.ARMOR)
				|| ((TrinketSlotTarget) (Object) enchantment.definition()).trinkets$slots().contains(slotId);
	}
}

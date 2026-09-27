package io.github.nistroy.elytraslotenchants;

import dev.emi.trinkets.api.event.TrinketUnequipCallback;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class ElytraSlotEnchants implements ModInitializer {
	@Override
	public void onInitialize() {
		// Vanilla coupe les effets de position quand un objet quitte un emplacement d'équipement
		// (LivingEntity.collectEquipmentChanges) ; le slot Trinkets n'en est pas un, donc on le fait ici.
		TrinketUnequipCallback.EVENT.register((stack, slot, entity) -> {
			if (!entity.level().isClientSide() && ElytraTrinket.isElytra(stack)) {
				EnchantmentHelper.stopLocationBasedEffects(stack, entity, ElytraTrinket.SEEN_AS);
			}
		});
	}
}

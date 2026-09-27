package io.github.nistroy.elytraslotenchants.mixin;

import io.github.nistroy.elytraslotenchants.ElytraTrinket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {
	// Chemin commun des effets d'enchantement portés : position (Graviole), protection, tick, dégâts…
	@Inject(method = "runIterationOnEquipment", at = @At("TAIL"))
	private static void elytraslotenchants$visitSlotElytra(LivingEntity entity,
			EnchantmentHelper.EnchantmentInSlotVisitor visitor, CallbackInfo ci) {
		ElytraTrinket.forEachEnchantmentTrinketsSkips(entity, visitor);
	}
}

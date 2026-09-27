package io.github.nistroy.elytraslotenchants.mixin;

import io.github.nistroy.elytraslotenchants.SoulRecharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ServerItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemCooldowns.class)
abstract class ItemCooldownsMixin {
	// Côté serveur seulement : la durée part ensuite au client (paquet de ServerItemCooldowns), barre du HUD comprise.
	// Mixin sur vanilla plutôt que sur le lambda réseau de Deeper and Darker, dont le nom change à la compilation.
	@ModifyVariable(method = "addCooldown", at = @At("HEAD"), argsOnly = true)
	private int elytraslotenchants$shortenSoulElytraBoost(int ticks, Item item) {
		if ((Object) this instanceof ServerItemCooldowns server) {
			return SoulRecharge.cooldown(server.player, item, ticks);
		}
		return ticks;
	}
}

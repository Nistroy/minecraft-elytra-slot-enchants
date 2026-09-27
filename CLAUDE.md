# minecraft-elytra-slot-enchants — instructions agents

Mod Fabric 1.21.1, serveur seul (joueurs : rien). Serveur `Nistroy/minecraft-server` (`MODS.md`).
Public, GPL-3.0. Docs `.md` = notes denses pour agents, sauf `README.md` (humains).

## But
Élytre dans le slot Trinkets `chest/cape` d'Elytra Slot → ses enchantements « torse seul » (Graviole
d'Enchants+, `slots: ["chest"]`, effet `location_changed` attribut `generic.gravity`) comptent comme au torse.

## Carte
- `ElytraTrinket` — élytres du slot (tag `elytraslot:elytra`, joueurs seulement), enchantements que Trinkets saute.
- `mixin/EnchantmentHelperMixin` — `TAIL` de `runIterationOnEquipment` (position, protection, tick, dégâts…).
- `ElytraSlotEnchants` — `TrinketUnequipCallback` → `stopLocationBasedEffects(stack, entity, CHEST)`.
- `elytraslotenchants.accesswidener` — `EnchantmentHelper$EnchantmentInSlotVisitor` package-private.
- `src/gametest/` — tests en jeu + enchantement de test `elytraslotenchants_test:lighter` (clone de Graviole sans condition).

## Trinkets 3.10.0 (relevé au javap, 2026-09-27)
- `EnchantmentHelperMixin` de Trinkets : applique déjà aux objets de ses slots les enchantements dont `slots`
  contient `any` ou `armor`, ou dont `trinkets:slots` (champ JSON ajouté par `EnchantmentDefinitionMixin`)
  contient l'id du slot (`group/name`). → Mending, Solidité, Skyguard, Protection OK sans ce mod.
  `ElytraTrinket.appliedByTrinkets` = même règle, sinon double compte (test Protection = 4, pas 8).
- Trinkets passe `inSlot = null` → `EnchantmentAttributeEffect.idForSlot(null)` = NPE. D'où : **ne pas** ajouter
  `trinkets:slots` à Graviole par datapack ; ce mod passe `CHEST`.
- Déséquipement : événement lancé dans `LivingEntity.tick`, sauté si `isRemoved()`.

## Tests — TDD obligatoire
- `./gradlew build runGameTest` (CI idem). Rouge d'abord pour tout nouveau comportement.
- Joueur factice (`makeMockServerPlayerInLevel`) déconnecté d'office (paquet CCA refusé) → retiré → Trinkets ne
  le tick pas : les tests lancent `TrinketUnequipCallback` à la main.
- CCA `6.1.0` en dépendance de compilation : embarqué dans le jar Trinkets, absent de son POM Modrinth.

## Release
Tag `vX.Y.Z` = `version` de `gradle.properties` → workflow `release` → jar sur la release GitHub.

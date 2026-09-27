# minecraft-elytra-slot-enchants

Petit mod **Fabric 1.21.1**, côté serveur, pour le serveur entre copains. Avec
[Elytra Slot](https://modrinth.com/mod/elytra-slot), on peut porter l'élytre dans un emplacement à part
et garder son plastron. Mais certains enchantements de l'élytre ne marchaient plus dans cet
emplacement : ceux qui ne s'appliquent qu'au torse, comme **Graviole** (Enchants+).

Ce mod les fait compter comme si l'élytre était au torse.

- Rien à installer chez les joueurs : tout se passe sur le serveur.
- Mending, Solidité, Skyguard et Protection marchaient déjà dans l'emplacement (Trinkets s'en charge) ;
  le mod ne les compte pas une deuxième fois.
- Retirer l'élytre de l'emplacement coupe aussitôt l'effet de Graviole.

## Recharge des âmes

Nouvel enchantement pour l'**Élytre des âmes** (Deeper and Darker) : le boost revient plus vite.

| Niveau | Recharge du boost |
| --- | --- |
| sans | 30 s |
| I | 22 s |
| II | 15 s |
| III | 8 s |

- S'achète chez les bibliothécaires (livre de niveau I à III), pas à la table d'enchantement.
- Livre I + livre I → II à l'enclume, II + II → III.
- Marche au torse comme dans l'emplacement d'Elytra Slot.

Dépend de Trinkets `3.10.0`, d'Elytra Slot `9.0.1+1.21.1` et de Deeper and Darker.

## Construire

```
./gradlew build runGameTest
```

Le jar est dans `build/libs/`. Licence GPL-3.0.

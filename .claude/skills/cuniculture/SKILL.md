---
name: cuniculture
description: Référentiel métier de l'élevage de lapins (cuniculture) pour Lapino — calendrier de reproduction, seuils d'alerte, prophylaxie, alimentation et formules des indicateurs zootechniques. Utiliser avant d'ajouter ou modifier une règle métier, une alerte, un calcul de KPI ou un texte de conseil (CuniTipBox).
---

# Référentiel cunicole (valeurs indicatives, élevage de rapport)

Ces valeurs sont des repères usuels ; l'utilisateur (éleveur) a le dernier mot. Toute nouvelle constante métier doit être nommée et centralisée, pas codée en dur à plusieurs endroits.

## Cycle de reproduction (J = jours depuis la saillie)
| Étape | Repère | Dans Lapino |
|---|---|---|
| Saillie | J0 ; lapine présentée au mâle (dans la cage du mâle) | `startCycle`, statut `saillie` |
| Palpation | J10–J14 (ampoules fœtales) ; négatif → représenter au mâle | prévue J+11, alerte dès J10 → `verifyGestation` |
| Boîte à nid | J27–J28, paille/copeaux propres | prévue J+28, alerte dès J27 → `confirmNestBox` |
| Mise bas | gestation 30–32 j (moy. 31) ; > J33 = anormal | prévue J+31, alerte à ≤ 2 j → `confirmBirth`, statut `allaitante` |
| Contrôle du nid | J+1 après mise bas : retirer les morts, égaliser les portées (adoption 8–10/lapine) | — (idée de fonctionnalité) |
| Sevrage | 28–35 j après mise bas (Lapino : prévu +35, alerte dès +30) | `confirmWeaning`, statut `termine` |
| Rythmes | intensif : re-saillie J+11 post-partum (cycle 42 j) ; semi-intensif : J+18–25 ; extensif : après sevrage | — |
| Mise à la reproduction | femelle ~4,5–5 mois (≈ 3,5–4 kg selon souche), mâle ~5–6 mois | `calculateAgeLabel` |
| Réforme | ~8–12 portées, ou 3 échecs de palpation consécutifs, mammites, maigreur | — |

## Indicateurs zootechniques (formules)
- **Fertilité (%)** = mises bas / saillies × 100 (objectif > 75–80 %)
- **Prolificité** = nés totaux (vivants + morts) / mise bas (≈ 9–11) ; nés vivants / mise bas (≈ 8–10)
- **Mortinatalité (%)** = morts-nés / nés totaux × 100 (< 8 %)
- **Survie au nid (%)** = sevrés / nés vivants × 100 (objectif > 85 %)
- **Sevrés par mise bas** (≈ 7–8) ; **sevrés / femelle / an** (≈ 40–50 en intensif)
- **Intervalle entre mises bas (j)** = moyenne des écarts entre mises bas successives (42 j intensif)
- Garder les divisions protégées contre 0 et afficher « — » quand il n'y a pas de données.

## Prophylaxie (à adapter avec le vétérinaire)
- **VHD / RHDV1 + RHDV2** : primo dès ~5–10 semaines selon vaccin, rappel annuel (souvent tous les 6 mois en zone à risque).
- **Myxomatose** : dès 4–6 semaines, rappel tous les 4–6 mois.
- Vaccins combinés myxo-VHD existent (rappel annuel). Dans Lapino : table `vaccins.duree_jours` → `date_prochain`.
- Points de vigilance : coccidiose, entérocolite (diarrhées post-sevrage), pasteurellose (coryza), mammites, gale des oreilles.

## Alimentation (repères de consommation de granulés)
| Animal | g/jour |
|---|---|
| Mâle / femelle à l'entretien | 130–180 |
| Femelle gestante | 150–200 |
| Femelle allaitante + portée (pic J21–J30) | 350–500+ |
| Lapereau en engraissement (sevrage → 2,4–2,6 kg vers 70–77 j) | 100–150 |
- Eau à volonté (une allaitante boit 0,5–1 L/j). Foin à volonté.
- Autonomie stock (j) = stock_kg × 1000 / consommation_totale_g_par_jour (cf. `Aliment`).
- Indice de consommation engraissement ≈ 3–3,5 kg aliment / kg de gain.

## Idées de fonctionnalités à forte valeur
Pesées des lapereaux / courbe de croissance, gestion des mâles (fertilité par mâle), consanguinité (éviter frère×sœur, parent×descendant), engraissement et ventes (recettes / coût aliment), traitements & délais d'attente, export/sauvegarde CSV de la base, notifications locales des alertes.

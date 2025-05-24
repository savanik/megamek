/*
 * MegaMek - Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package megamek.common.weapons;

import java.io.Serial;
import java.util.Vector;

import megamek.common.*;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.equipment.ArmorType;
import megamek.common.options.OptionsConstants;
import megamek.server.totalwarfare.TWGameManager;

public class PlasmaRifleHandler extends AmmoWeaponHandler {
    @Serial
    private static final long serialVersionUID = -2092721653693187140L;

    /**
     * @param toHitData          The {@link ToHitData} to use.
     * @param weaponAttackAction The {@link WeaponAttackAction} to use.
     * @param game               The {@link Game} object to use.
     * @param twGameManager      A {@link TWGameManager} to use.
     */
    public PlasmaRifleHandler(ToHitData toHitData, WeaponAttackAction weaponAttackAction, Game game,
          TWGameManager twGameManager) {
        super(toHitData, weaponAttackAction, game, twGameManager);
        generalDamageType = HitData.DAMAGE_ENERGY;

    }

    @Override
    protected void handleEntityDamage(Entity entityTarget, Vector<Report> vPhaseReport, Building bldg, int hits,
          int nCluster, int bldgAbsorbs) {
        super.handleEntityDamage(entityTarget, vPhaseReport, bldg, hits, nCluster, bldgAbsorbs);
        if (!missed && entityTarget.tracksHeat()) {
            Report report = new Report(3400);
            report.subject = subjectId;
            report.indent(2);
            int extraHeat = 0;
            // if this is a fighter squadron, we need to account for the number of weapons should default to one for
            // non-squadrons
            for (int i = 0; i < nweaponsHit; i++) {
                extraHeat += Compute.d6();
            }

            if (entityTarget.getArmor(hit) > 0 &&
                      (entityTarget.getArmorType(hit.getLocation()) == EquipmentType.T_ARMOR_REFLECTIVE)) {
                entityTarget.heatFromExternal += Math.max(1, extraHeat / 2);
                report.add(Math.max(1, extraHeat / 2));
                report.choose(true);
                report.messageId = 3406;
                report.add(extraHeat);
                report.add(ArmorType.forEntity(entityTarget, hit.getLocation()).getName());
            } else if (entityTarget.getArmor(hit) > 0 &&
                             (entityTarget.getArmorType(hit.getLocation()) == EquipmentType.T_ARMOR_HEAT_DISSIPATING)) {
                entityTarget.heatFromExternal += extraHeat / 2;
                report.add(extraHeat / 2);
                report.choose(true);
                report.messageId = 3406;
                report.add(extraHeat);
                report.add(ArmorType.forEntity(entityTarget, hit.getLocation()).getName());
            } else {
                entityTarget.heatFromExternal += extraHeat;
                report.add(extraHeat);
                report.choose(true);
            }
            vPhaseReport.addElement(report);
        }
    }

    @Override
    protected int calcDamagePerHit() {
        if (target.tracksHeat()) {
            int toReturn = 10;
            toReturn = applyGlancingBlowModifier(toReturn, false);
            if (game.getOptions().booleanOption(OptionsConstants.ADVCOMBAT_TACOPS_RANGE) &&
                      (nRange > wtype.getRanges(weapon)[RangeType.RANGE_LONG])) {
                toReturn -= 1;
            }
            if (game.getOptions().booleanOption(OptionsConstants.ADVCOMBAT_TACOPS_LOS_RANGE) &&
                      (nRange > wtype.getRanges(weapon)[RangeType.RANGE_EXTREME])) {
                toReturn = (int) Math.floor(toReturn / 2.0);
            }
            return toReturn;
        }
        return 1;
    }

    @Override
    protected int calcnCluster() {
        if (target.tracksHeat()) {
            bSalvo = false;
            return 1;
        }

        int toReturn = 5;

        if (target.isConventionalInfantry()) {
            toReturn = Compute.d6(2);
        }

        bSalvo = true;
        // pain-shunted infantry gets half-damage
        if ((target instanceof Infantry) && ((Entity) target).hasAbility(OptionsConstants.MD_PAIN_SHUNT)) {
            toReturn = Math.max(toReturn / 2, 1);
        }

        return toReturn;
    }

    @Override
    protected int calcHits(Vector<Report> vPhaseReport) {
        int toReturn;

        // against meks, 1 hit with 10 damage, plus heat
        if (target.tracksHeat()) {
            toReturn = 1;
            // otherwise, 10+2d6 damage but fire-resistant BA armor gets no damage from heat, and half the normal
            // one, so only 5 damage
        } else {
            if ((target instanceof BattleArmor) && ((BattleArmor) target).isFireResistant()) {
                toReturn = 5;
            } else {
                toReturn = 10 + Compute.d6(2);
            }
            toReturn = applyGlancingBlowModifier(toReturn, false);
        }
        return toReturn;
    }

    @Override
    protected void handleIgnitionDamage(Vector<Report> vPhaseReport, Building bldg, int hits) {
        if (!bSalvo) {
            // hits!
            Report report = new Report(2270);
            report.subject = subjectId;
            report.newlines = 0;
            vPhaseReport.addElement(report);
        }

        TargetRoll targetRoll = new TargetRoll(wtype.getFireTN(), wtype.getName());
        if (targetRoll.getValue() != TargetRoll.IMPOSSIBLE) {
            Report.addNewline(vPhaseReport);
            gameManager.tryIgniteHex(target.getPosition(), subjectId, true, false, targetRoll, true, -1, vPhaseReport);
        }
    }

    @Override
    protected void handleClearDamage(Vector<Report> vPhaseReport, Building bldg, int nDamage) {
        if (!bSalvo) {
            // hits!
            Report report = new Report(2270);
            report.subject = subjectId;
            vPhaseReport.addElement(report);
        }

        nDamage *= 2; // Plasma weapons deal double damage to woods.

        // report that damage was "applied" to terrain
        Report report = new Report(3385);
        report.indent(2);
        report.subject = subjectId;
        report.add(nDamage);
        vPhaseReport.addElement(report);

        // Any clear attempt can result in accidental ignition, even weapons that can't normally start fires. that's
        // weird. Buildings can't be accidentally ignited.
        // TODO: change this for TacOps - now you roll another 2d6 first and on a 5 or less you do a normal ignition
        //  as though for intentional fires
        if ((bldg != null) &&
                  gameManager.tryIgniteHex(target.getPosition(),
                        subjectId,
                        true,
                        false,
                        new TargetRoll(wtype.getFireTN(), wtype.getName()),
                        5,
                        vPhaseReport)) {
            return;
        }

        Vector<Report> clearReports = gameManager.tryClearHex(target.getPosition(), nDamage, subjectId);

        if (!clearReports.isEmpty()) {
            vPhaseReport.lastElement().newlines = 0;
        }

        vPhaseReport.addAll(clearReports);
    }

    @Override
    protected void handleBuildingDamage(Vector<Report> vPhaseReport, Building bldg, int nDamage, Coords coords) {
        // Plasma weapons deal double damage to buildings.
        super.handleBuildingDamage(vPhaseReport, bldg, nDamage * 2, coords);
    }
}

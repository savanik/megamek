/*
 * MegaMek - Copyright (C) 2004, 2005 Ben Mazur (bmazur@sev.org)
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
package megamek.common.weapons.lasers;

import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.SimpleTechLevel;
import megamek.common.WeaponType;

/**
 * @author Jason Tighe
 * @since Sep 12, 2004
 */
public class ISVariableSpeedPulseLaserSmall extends VariableSpeedPulseLaserWeapon {
    private static final long serialVersionUID = 2676144961105838316L;

    public ISVariableSpeedPulseLaserSmall() {
        super();
        name = "Small VSP Laser";
        setInternalName("ISSmallVSPLaser");
        addLookupName("ISSVSPL");
        addLookupName("ISSmallVariableSpeedLaser");
        addLookupName("ISSmallVSP");
        sortingName = "Laser VSP B";
                heat = 3;
        damage = WeaponType.DAMAGE_VARIABLE;
        toHitModifier = -4;
        shortRange = 2;
        mediumRange = 4;
        longRange = 6;
        extremeRange = 8;
        waterShortRange = 1;
        waterMediumRange = 2;
        waterLongRange = 3;
        waterExtremeRange = 4;
        damageShort = 5;
        damageMedium = 4;
        damageLong = 3;
        tonnage = 2.0;
        criticals = 1;
        bv = 22;
        cost = 60000;
        shortAV = 4;
        maxRange = RANGE_SHORT;
        rulesRefs = "321, TO";
        //Nov 22 - CGL requested we move to Standard for Simple Tech Level 
        techAdvancement.setTechBase(TechBase.IS).setTechRating(TechRating.E)
                .setAvailability(AvailabilityValue.X, AvailabilityValue.X, AvailabilityValue.E, AvailabilityValue.D)
                .setISAdvancement(3070, 3072, 3080).setPrototypeFactions(Faction.FW, Faction.WB)
                .setProductionFactions(Faction.FW, Faction.WB).setStaticTechLevel(SimpleTechLevel.STANDARD);
    }

    @Override
    public double getBattleForceDamage(int range) {
        if (range == AlphaStrikeElement.SHORT_RANGE) {
            return 0.575;
        } else if (range == AlphaStrikeElement.MEDIUM_RANGE) {
            return 0.378;
        } else {
            return 0;
        }
    }
}
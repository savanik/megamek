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

import megamek.common.SimpleTechLevel;

/**
 * @author Andrew Hunter
 * @since Sep 12, 2004
 */
public class CLImprovedLaserLarge extends LaserWeapon {
    private static final long serialVersionUID = 7184746139915905374L;

    public CLImprovedLaserLarge() {
        super();
        this.name = "Improved Large Laser";
        this.setInternalName(this.name);
        this.addLookupName("Improved Large Laser");
        this.addLookupName("ImpLargeLaser");
        sortingName = "Laser Imp D";
        this.heat = 8;
        this.damage = 8;
        this.shortRange = 5;
        this.mediumRange = 10;
        this.longRange = 15;
        this.extremeRange = 20;
        this.waterShortRange = 3;
        this.waterMediumRange = 6;
        this.waterLongRange = 9;
        this.waterExtremeRange = 12;
        this.tonnage = 4.0;
        this.criticals = 1;
        this.bv = 123;
        this.cost = 100000;
        this.shortAV = 8;
        this.medAV = 8;
        this.maxRange = RANGE_MED;
        rulesRefs = "95, IO";
        techAdvancement.setTechBase(TechBase.CLAN).setTechRating(TechRating.E)
                .setAvailability(AvailabilityValue.X, AvailabilityValue.D, AvailabilityValue.X, AvailabilityValue.X)
                .setClanAdvancement(2812, 2815, 2818, 2830, 3080)
                .setClanApproximate(true, true, false, false, false)
                .setPrototypeFactions(Faction.CNC).setProductionFactions(Faction.CNC)
                .setStaticTechLevel(SimpleTechLevel.EXPERIMENTAL);
    }
}

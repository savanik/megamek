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
package megamek.common.weapons.capitalweapons;

import megamek.common.AmmoType;

/**
 * @author Jay Lawson
 * @since Sep 25, 2004
 */
public class NGaussWeaponHeavy extends NGaussWeapon {
    private static final long serialVersionUID = 8756042527483383101L;

    public NGaussWeaponHeavy() {
        super();
        name = "Naval Gauss (Heavy)";
        setInternalName(this.name);
        addLookupName("HeavyNGauss");
        addLookupName("CLHeavyNGauss");
        addLookupName("Heavy N-Gauss (Clan)");
        sortingName = "Gauss Naval D";
        shortName = "Heavy NGauss";
        heat = 18;
        damage = 30;
        ammoType = AmmoType.T_HEAVY_NGAUSS;
        shortRange = 12;
        mediumRange = 24;
        longRange = 36;
        extremeRange = 48;
        tonnage = 7000;
        bv = 6048;
        cost = 50050000;
        shortAV = 30;
        medAV = 30;
        longAV = 30;
        extAV = 30;
        maxRange = RANGE_EXT;
        rulesRefs = "333, TO";
        techAdvancement.setTechBase(TechBase.ALL)
                .setIntroLevel(false)
                .setUnofficial(false)
                .setTechRating(TechRating.E)
                .setAvailability(AvailabilityValue.E, AvailabilityValue.X, AvailabilityValue.E, AvailabilityValue.X)
                .setISAdvancement(2440, 2448, DATE_NONE, 2950, 3052)
                .setISApproximate(true, true, false, true, false)
                .setClanAdvancement(2440, 2448, DATE_NONE, DATE_NONE, DATE_NONE)
                .setClanApproximate(true, true, false, false, false)
                .setPrototypeFactions(Faction.TH)
                .setProductionFactions(Faction.TH)
                .setReintroductionFactions(Faction.DC);
    }
}

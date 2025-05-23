/*
 * Copyright (c) 2005 - Ben Mazur (bmazur@sev.org)
 * Copyright (c) 2022 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.common.weapons.lrms;

/**
 * @author Sebastian Brocks
 */
public class CLLRT5OS extends LRTWeapon {
    private static final long serialVersionUID = -3651580547253252279L;

    public CLLRT5OS() {
        super();
        name = "LRT 5 (OS)";
        setInternalName("CLLRTorpedo5 (OS)");
        addLookupName("Clan OS LRT-5");
        addLookupName("Clan LRT 5 (OS)");
        addLookupName("CLLRT5OS");
        heat = 2;
        rackSize = 5;
        minimumRange = WEAPON_NA;
        waterShortRange = 7;
        waterMediumRange = 14;
        waterLongRange = 21;
        waterExtremeRange = 28;
        tonnage = 1.5;
        criticals = 1;
        bv = 11;
        flags = flags.or(F_ONESHOT).andNot(F_PROTO_WEAPON);
        cost = 15000;
        rulesRefs = "230, TM";
        techAdvancement.setTechBase(TechBase.CLAN)
                .setIntroLevel(false)
                .setUnofficial(false)
                .setTechRating(TechRating.C)
                .setAvailability(AvailabilityValue.X, AvailabilityValue.D, AvailabilityValue.C, AvailabilityValue.C)
                .setClanAdvancement(2820, 2824, 2825, DATE_NONE, DATE_NONE)
                .setClanApproximate(true, false, false, false, false)
                .setPrototypeFactions(Faction.CSF)
                .setProductionFactions(Faction.CSF);
    }
}

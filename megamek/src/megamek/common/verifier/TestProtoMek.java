/*
 * Copyright (C) 2018 - The MegaMek Team
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
package megamek.common.verifier;

import java.util.HashMap;
import java.util.Map;

import megamek.codeUtilities.MathUtility;
import megamek.common.AmmoType;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.MiscType;
import megamek.common.Mounted;
import megamek.common.ProtoMek;
import megamek.common.WeaponType;
import megamek.common.annotations.Nullable;
import megamek.common.equipment.ArmorType;
import megamek.common.options.OptionsConstants;
import megamek.common.util.StringUtil;

/**
 * @author Neoancient
 */
public class TestProtoMek extends TestEntity {

    /**
     * Minimum tonnage for a ProtoMek
     */
    public static final double MIN_TONNAGE = 2.0;

    /**
     * Any ProtoMek with a larger mass than this is ultra-heavy
     */
    public static final double MAX_STD_TONNAGE = 9.0;

    /**
     * Maximum weight for a ProtoMek
     */
    public static final double MAX_TONNAGE = 15.0;

    /**
     * Minimum walk MP for glider ProtoMek
     */
    public static final int GLIDER_MIN_MP = 4;
    /**
     * Minimum walk MP for a quad ProtoMek
     */
    public static final int QUAD_MIN_MP = 3;

    private final ProtoMek proto;
    private final String fileString;

    public static int maxJumpMP(ProtoMek proto) {
        if (proto.getMisc().stream().map(Mounted::getType)
                .anyMatch(eq -> eq.hasFlag(MiscType.F_JUMP_JET)
                        && eq.hasSubType(MiscType.S_IMPROVED))) {
            return (int) Math.ceil(proto.getOriginalWalkMP() * 1.5);
        }
        return proto.getOriginalWalkMP();
    }

    public TestProtoMek(ProtoMek proto, TestEntityOption option, String fileString) {
        super(option, proto.getEngine(), null);
        this.proto = proto;
        this.fileString = fileString;
    }

    @Override
    public Entity getEntity() {
        return proto;
    }

    @Override
    public boolean isTank() {
        return false;
    }

    @Override
    public boolean isMek() {
        return false;
    }

    @Override
    public boolean isAero() {
        return false;
    }

    @Override
    public boolean isSmallCraft() {
        return false;
    }

    @Override
    public boolean isAdvancedAerospace() {
        return false;
    }

    @Override
    public boolean isProtoMek() {
        return true;
    }

    @Override
    public double getWeightStructure() {
        return round(proto.getWeight() * 0.1, Ceil.KILO);
    }

    @Override
    public double getWeightEngine() {
        return proto.getEngine().getWeightEngine(proto);
    }

    @Override
    public double getWeightControls() {
        return (proto.getWeight() > MAX_STD_TONNAGE) ? 0.75 : 0.5;
    }

    @Override
    public double getWeightMisc() {
        return 0;
    }

    @Override
    public double getWeightHeatSinks() {
        return getCountHeatSinks() * 0.25;
    }

    @Override
    public boolean hasDoubleHeatSinks() {
        return false;
    }

    @Override
    public int getCountHeatSinks() {
        return heatNeutralHSRequirement();
    }

    @Override
    public double calculateWeight() {
        // Deal with some floating point precision issues
        return round(super.calculateWeightExact(), Ceil.KILO);
    }

    @Override
    public double getWeightAllocatedArmor() {
        return proto.getTotalArmor() * ArmorType.forEntity(proto).getWeightPerPoint();
    }

    @Override
    public String printWeightStructure() {
        return StringUtil.makeLength("Structure: " + getEntity().getTotalOInternal(),
                getPrintSize() - 5)
                + TestEntity.makeWeightString(getWeightStructure(), true) + "\n";
    }

    @Override
    public String printWeightMisc() {
        return "";
    }

    @Override
    public String printWeightControls() {
        return StringUtil.makeLength("Controls:", getPrintSize() - 5)
                + makeWeightString(getWeightControls(), true) + "\n";
    }

    @Override
    public StringBuffer printMiscEquip(StringBuffer buff, int posLoc, int posWeight) {
        for (Mounted<?> m : getEntity().getMisc()) {
            buff.append(StringUtil.makeLength(m.getName(), 20));
            buff.append(
                    StringUtil.makeLength(getLocationAbbr(m.getLocation()),
                            getPrintSize() - 5 - 20))
                    .append(
                            TestEntity.makeWeightString(round(m.getTonnage(), Ceil.KILO), true));
            buff.append("\n");
        }
        return buff;
    }

    @Override
    public StringBuffer printWeapon(StringBuffer buff, int posLoc, int posWeight) {
        for (Mounted<?> m : getEntity().getWeaponList()) {
            buff.append(StringUtil.makeLength(m.getName(), 20));
            buff.append(
                    StringUtil.makeLength(getLocationAbbr(m.getLocation()),
                            getPrintSize() - 5 - 20))
                    .append(TestEntity.makeWeightString(m.getTonnage(), true)).append("\n");
        }
        return buff;
    }

    @Override
    public StringBuffer printAmmo(StringBuffer buff, int posLoc, int posWeight) {
        for (Mounted<?> m : getEntity().getAmmo()) {
            AmmoType mt = (AmmoType) m.getType();

            buff.append(StringUtil.makeLength(mt.getName(), 20));
            buff.append(" ").append(
                    StringUtil.makeLength(getLocationAbbr(m.getLocation()),
                            getPrintSize() - 5 - 20))
                    .append(TestEntity.makeWeightString(
                            Math.ceil(mt.getKgPerShot() * m.getBaseShotsLeft()) / 1000.0, true))
                    .append("\n");
        }
        return buff;
    }

    @Override
    public double getWeightCarryingSpace() {
        return 0.0;
    }

    @Override
    public boolean correctEntity(StringBuffer buff, int ammoTechLvl) {
        boolean correct = true;
        if (skip()) {
            return correct;
        }
        if (!allowOverweightConstruction() && !correctWeight(buff)) {
            buff.insert(0, printTechLevel() + printShortMovement());
            buff.append(printWeightCalculation());
            correct = false;
        }
        if (!engine.engineValid) {
            buff.append(engine.problem.toString()).append("\n\n");
            correct = false;
        }
        if (showCorrectArmor() && !correctArmor(buff)) {
            correct = false;
        }
        if (showFailedEquip() && hasFailedEquipment(buff)) {
            correct = false;
        }
        if (hasIllegalTechLevels(buff, ammoTechLvl)) {
            correct = false;
        }
        if (showIncorrectIntroYear() && hasIncorrectIntroYear(buff)) {
            correct = false;
        }
        if (hasIllegalEquipmentCombinations(buff)) {
            correct = false;
        }
        correct = correct && correctMovement(buff);
        if (getEntity().hasQuirk(OptionsConstants.QUIRK_NEG_ILLEGAL_DESIGN) || getEntity().canonUnitWithInvalidBuild()) {
            correct = true;
        }
        return correct;
    }

    @Override
    public boolean correctWeight(StringBuffer buff) {
        boolean correct = super.correctWeight(buff);
        if (proto.getWeight() > MAX_TONNAGE) {
            buff.append("Exceeds maximum weight of ").append(MAX_TONNAGE).append("\n");
            correct = false;
        }
        return correct;
    }

    @Override
    public boolean hasIllegalEquipmentCombinations(StringBuffer buff) {
        boolean illegal = false;
        Map<Integer, Integer> slotsByLoc = new HashMap<>();
        Map<Integer, Double> weightByLoc = new HashMap<>();
        int meleeWeapons = 0;
        for (Mounted<?> mount : proto.getEquipment()) {
            if (!requiresSlot(mount.getType()) || (mount.getType() instanceof ArmorType)) {
                // armor is added separately
                continue;
            }
            slotsByLoc.merge(mount.getLocation(), 1, Integer::sum);
            weightByLoc.merge(mount.getLocation(), mount.getTonnage(), Double::sum);
            if (mount.isRearMounted() && (mount.getLocation() != ProtoMek.LOC_TORSO)) {
                buff.append("Equipment can only be rear-mounted on the torso\n");
                illegal = true;
            }
            if ((mount.getType() instanceof WeaponType)
                    && !mount.getType().hasFlag(WeaponType.F_PROTO_WEAPON)) {
                buff.append(mount).append(" is not a legal ProtoMek weapon.\n");
                illegal = true;
            } else if ((mount.getType() instanceof MiscType)
                    && !mount.getType().hasFlag(MiscType.F_PROTOMEK_EQUIPMENT)) {
                buff.append(mount).append(" is not legal ProtoMek equipment.\n");
                illegal = true;
            }

            if ((mount.getType() instanceof MiscType) && mount.getType().hasFlag(MiscType.F_MAGNETIC_CLAMP)) {
                if (proto.isGlider() || proto.isQuad()) {
                    buff.append("Quad and glider ProtoMeks cannot use a magnetic clamp system.\n");
                    illegal = true;
                }
            }

            if ((mount.getType() instanceof MiscType) && mount.getType().hasFlag(MiscType.F_PROTOMEK_MELEE)) {
                meleeWeapons++;
                if (meleeWeapons == 2) {
                    buff.append("Cannot mount multiple melee weapons.\n");
                    illegal = true;
                }
                if (mount.getType().hasSubType(MiscType.S_PROTO_QMS) && !proto.isQuad()) {
                    buff.append(mount.getType().getName() + "can only be used by quad ProtoMeks.\n");
                    illegal = true;
                }
                if (mount.getType().hasSubType(MiscType.S_PROTOMEK_WEAPON) && proto.isQuad()) {
                    buff.append(mount.getType().getName() + "cannot be used by quad ProtoMeks.\n");
                    illegal = true;
                }
            }
        }
        ArmorType armor = ArmorType.forEntity(proto);
        if (!armor.hasFlag(MiscType.F_PROTOMEK_EQUIPMENT)) {
            buff.append("Does not have legal armor type.\n");
            illegal = true;
        } else {
            slotsByLoc.merge(ProtoMek.LOC_TORSO, armor.getCriticals(proto), Integer::sum);
        }

        for (int loc = 0; loc < proto.locations(); loc++) {
            if (slotsByLoc.getOrDefault(loc, 0) > maxSlotsByLocation(loc, proto)) {
                buff.append("Exceeds ").append(maxSlotsByLocation(loc, proto))
                        .append(" slot limit in ").append(proto.getLocationName(loc)).append("\n");
                illegal = true;
            }

            if (weightByLoc.getOrDefault(loc, 0.0) > maxWeightByLocation(loc, proto)) {
                buff.append("Exceeds ").append(maxWeightByLocation(loc, proto) * 1000)
                        .append(" kg limit in ").append(proto.getLocationName(loc)).append("\n");
                illegal = true;
            }
        }
        if (proto.isGlider() && proto.isQuad()) {
            buff.append("Glider ProtoMeks cannot be quads.\n");
            illegal = true;
        }

        return illegal;
    }

    /**
     * @param protoMek The ProtoMek
     * @param eq       The equipment
     * @param location A location index on the Entity
     * @param buffer   If non-null and the location is invalid, will be appended
     *                 with an explanation
     * @return Whether the equipment can be mounted in the location on the ProtoMek
     */
    public static boolean isValidProtoMekLocation(ProtoMek protoMek, EquipmentType eq, int location,
            @Nullable StringBuffer buffer) {
        if (eq instanceof MiscType) {
            if (eq.hasFlag(MiscType.F_PROTOMEK_MELEE) && eq.hasSubType(MiscType.S_PROTOMEK_WEAPON)
                    && (location != ProtoMek.LOC_LARM) && (location != ProtoMek.LOC_RARM)) {
                if (buffer != null) {
                    buffer.append(eq.getName()).append(" must be mounted in an arm.\n");
                }
                return false;
            }
            if ((eq.hasFlag(MiscType.F_MAGNETIC_CLAMP)
                    || (eq.hasFlag(MiscType.F_PROTOMEK_MELEE) && eq.hasSubType(MiscType.S_PROTO_QMS)))
                    && (location != ProtoMek.LOC_TORSO)) {
                if (buffer != null) {
                    buffer.append(eq.getName()).append(" must be mounted in the torso.\n");
                }
                return false;
            }
        }
        if (!TestProtoMek.eqRequiresLocation(protoMek, eq) && (location != ProtoMek.LOC_BODY)) {
            if (buffer != null) {
                buffer.append(eq.getName()).append(" must be mounted in the body.\n");
            }
            return false;
        } else if (TestProtoMek.maxSlotsByLocation(location, protoMek) == 0) {
            if (buffer != null) {
                buffer.append(eq.getName()).append(" cannot be mounted in the ")
                        .append(protoMek.getLocationName(location)).append("\n");
            }
            return false;
        }
        return true;
    }

    /**
     * Checks for exceeding the maximum number of armor points by location for the
     * tonnage.
     *
     * @param buffer A string buffer for appending error messages.
     * @return Whether the number of armor points is legal
     */
    public boolean correctArmor(StringBuffer buffer) {
        boolean correct = true;
        for (int loc = 0; loc < proto.locations(); loc++) {
            if (proto.getOArmor(loc) > maxArmorFactor(proto, loc)) {
                buffer.append(proto.getLocationAbbr(loc))
                        .append(" exceeds maximum of ")
                        .append(maxArmorFactor(proto, loc)).append(" armor points.\n");
                correct = false;
            }
        }

        correct &= correctArmorOverAllocation(proto, buffer);
        return correct;
    }

    /**
     * Checks whether the protoMek meets the minimum MP requirements for the
     * configuration.
     *
     * @param buffer A buffer for error messages
     * @return Whether the MP is legal.
     */
    public boolean correctMovement(StringBuffer buffer) {
        boolean correct = true;
        if (proto.isGlider() && (proto.getOriginalWalkMP() < GLIDER_MIN_MP)) {
            buffer.append("Glider ProtoMeks have a minimum cruising MP of " + GLIDER_MIN_MP + ".\n");
            correct = false;
        } else if (proto.isQuad() && (proto.getOriginalWalkMP() < QUAD_MIN_MP)) {
            buffer.append("Quad ProtoMeks have a minimum walk MP of " + QUAD_MIN_MP + ".\n");
            correct = false;
        }
        int maxJump = maxJumpMP(proto);
        if (proto.getOriginalJumpMP() > maxJump) {
            buffer.append("Exceeds maximum jump MP.\n");
            correct = false;
        }
        if (proto.getAllUMUCount() > maxJump) {
            buffer.append("Exceeds maximum UMU MP.\n");
            correct = false;
        }
        return correct;
    }

    @Override
    public StringBuffer printEntity() {
        StringBuffer buff = new StringBuffer();
        buff.append("Protomek: ").append(proto.getDisplayName()).append("\n");
        buff.append("Found in: ").append(fileString).append("\n");
        buff.append(printTechLevel());
        buff.append("Intro year: ").append(proto.getYear()).append("\n");
        buff.append(printSource());
        buff.append(printShortMovement());
        if (correctWeight(buff, true, true)) {
            buff.append("Weight: ").append(getWeight() * 1000).append(" kg (")
                    .append(calculateWeight() * 1000).append(" kg)\n");
        }
        buff.append(printWeightCalculation()).append("\n");
        buff.append(printArmorPlacement());
        correctArmor(buff);
        buff.append(printLocations());
        printFailedEquipment(buff);
        return buff;
    }

    @Override
    public String getName() {
        return "ProtoMek: " + proto.getDisplayName();
    }

    @Override
    public double getWeightAmmo() {
        double weight = 0.0;
        for (Mounted<?> m : getEntity().getAmmo()) {
            if (!m.isOneShotAmmo()) {
                AmmoType mt = (AmmoType) m.getType();
                weight += ceil(mt.getKgPerShot() * m.getBaseShotsLeft() / 1000, Ceil.KILO);
            }
        }
        return weight;
    }

    @Override
    public double getWeightPowerAmp() {
        return 0;
    }

    /**
     * Determine the minimum walk MP for the ProtoMek based on configuration
     *
     * @param proto The ProtoMek
     * @return The minimum walk MP
     */
    public int getMinimumWalkMP(ProtoMek proto) {
        if (proto.isGlider()) {
            return 4;
        } else if (proto.isQuad()) {
            return 3;
        } else {
            return 1;
        }
    }

    /**
     * Computes the required engine rating
     *
     * @param proto The ProtoMek
     * @return The engine rating required for the weight, speed, and configuration
     */
    public static int calcEngineRating(ProtoMek proto) {
        return calcEngineRating(proto.getOriginalWalkMP(),
                proto.getWeight(), proto.isQuad() || proto.isGlider());
    }

    /**
     * Computes the required engine rating
     *
     * @param walkMP       The base walking MP
     * @param tonnage      The weight of the ProtoMek in tons
     * @param quadOrGlider Whether the ProtoMek is a quad or glider configuration
     * @return The engine rating required for the weight, speed, and configuration
     */
    public static int calcEngineRating(int walkMP, double tonnage, boolean quadOrGlider) {
        int moveFactor = (int) Math.ceil(walkMP * 1.5);
        // More efficient engine use for gliders and quads
        if (quadOrGlider) {
            moveFactor -= 2;
        }
        int rating = MathUtility.clamp((int) (moveFactor * tonnage), 1, 400);
        if (rating > 40) {
            int modFive = rating % 5;
            if (modFive > 0) {
                // Round up to the nearest valid engine rating, i.e. to the next xx5 or xx0
                rating += 5 - modFive;
            }
        }
        return rating;
    }

    /**
     * Determines whether a piece of equipment counts toward the slot and weight
     * limits of a location.
     *
     * @param etype The equipment
     * @return Whether the equipment takes a slot.
     */
    public static boolean requiresSlot(EquipmentType etype) {
        if (etype instanceof AmmoType) {
            return false;
        }
        if (etype instanceof MiscType) {
            return !(etype.hasFlag(MiscType.F_MASC)
                    || etype.hasFlag(MiscType.F_UMU)
                    || etype.hasFlag(MiscType.F_JUMP_JET));
        }
        return true;
    }

    /**
     * Equipment slot limit by location
     *
     * @param loc   The ProtoMek location
     * @param proto The ProtoMek
     * @return The number of equipment slots in the location
     */
    public static int maxSlotsByLocation(int loc, ProtoMek proto) {
        return maxSlotsByLocation(loc, proto.isQuad(), proto.getWeight() > MAX_STD_TONNAGE);
    }

    /**
     * Equipment slot limit by location
     *
     * @param loc   The ProtoMek location
     * @param quad  Whether the ProtoMek is a quad
     * @param ultra Whether the ProtoMek is ultraheavy
     * @return The number of equipment slots in the location
     */
    public static int maxSlotsByLocation(int loc, boolean quad, boolean ultra) {
        switch (loc) {
            case ProtoMek.LOC_TORSO: {
                int slots = 2;
                if (ultra) {
                    slots++;
                }
                if (quad) {
                    slots += slots;
                }
                return slots;
            }
            case ProtoMek.LOC_LARM:
            case ProtoMek.LOC_RARM:
                return quad ? 0 : 1;
            case ProtoMek.LOC_MAINGUN:
                return (quad && ultra) ? 2 : 1;
            case ProtoMek.LOC_HEAD:
            case ProtoMek.LOC_LEG:
            default:
                return 0;
        }
    }

    /**
     * The maximum total weight that can be mounted in a given location.
     *
     * @param loc   The Protomek location
     * @param proto The Protomek
     * @return The weight limit for that location, in tons.
     */
    public static double maxWeightByLocation(int loc, ProtoMek proto) {
        return maxWeightByLocation(loc, proto.isQuad(), proto.getWeight() > MAX_STD_TONNAGE);
    }

    /**
     * The maximum total weight that can be mounted in a given location.
     *
     * @param loc   The Protomek location
     * @param quad  Whether the protoMek is a quad
     * @param ultra Whether the protoMek is ultraheavy
     * @return The weight limit for that location, in tons.
     */
    public static double maxWeightByLocation(int loc, boolean quad, boolean ultra) {
        switch (loc) {
            case ProtoMek.LOC_TORSO:
                if (quad) {
                    return ultra ? 8.0 : 5.0;
                } else {
                    return ultra ? 4.0 : 2.0;
                }
            case ProtoMek.LOC_LARM:
            case ProtoMek.LOC_RARM:
                if (quad) {
                    return 0;
                }
                return ultra ? 1.0 : 0.5;
            case ProtoMek.LOC_MAINGUN:
                return Double.MAX_VALUE;
            case ProtoMek.LOC_HEAD:
            case ProtoMek.LOC_LEG:
            default:
                return 0.0;
        }
    }

    private static final int[] MAX_ARMOR_FACTOR = { 15, 17, 22, 24, 33, 35, 40, 42, 51, 53, 58, 60, 65, 67 };

    /**
     * Calculate the maximum armor factor based on weight and whether there is a
     * main gun location
     *
     * @param proto The protoMek
     * @return The maximum total number of armor points
     */
    public static int maxArmorFactor(ProtoMek proto) {
        return maxArmorFactor(proto.getWeight(), proto.hasMainGun());
    }

    /**
     * Calculate the maximum armor factor based on weight and whether there is a
     * main gun location
     *
     * @param weight  The weight of the protoMek in tons
     * @param mainGun Whether the protoMek has a main gun location
     * @return The maximum total number of armor points
     */
    public static int maxArmorFactor(double weight, boolean mainGun) {
        final int weightIndex = Math.max(0, (int) weight - 2);
        int base = MAX_ARMOR_FACTOR[Math.min(weightIndex, MAX_ARMOR_FACTOR.length - 1)];
        if (mainGun) {
            return base + ((weight > MAX_STD_TONNAGE) ? 6 : 3);
        }
        return base;
    }

    /**
     * Determine the maximum amount of armor in a location based on unit weight.
     *
     * @param proto    The protoMek
     * @param location The location index
     * @return The maximum total number of armor points
     */
    public static int maxArmorFactor(ProtoMek proto, int location) {
        if (location == ProtoMek.LOC_HEAD) {
            return 2 + (int) proto.getWeight() / 2;
        } else if (location == ProtoMek.LOC_MAINGUN) {
            if (proto.hasMainGun()) {
                return proto.getOInternal(location) * 3;
            } else {
                return 0;
            }
        } else if ((location == ProtoMek.LOC_LARM)
                || (location == ProtoMek.LOC_RARM)) {
            if (proto.isQuad()) {
                return 0;
            } else if (proto.getWeight() < 6) {
                return 2;
            } else if (proto.getWeight() < 10) {
                return 4;
            } else {
                return 6;
            }
        } else if (location == ProtoMek.LOC_BODY) {
            return 0;
        }
        return proto.getOInternal(location) * 2;
    }

}

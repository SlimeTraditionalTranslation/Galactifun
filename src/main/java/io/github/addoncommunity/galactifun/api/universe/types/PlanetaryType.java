package io.github.addoncommunity.galactifun.api.universe.types;

import io.github.addoncommunity.galactifun.api.universe.PlanetaryObject;

/**
 * Type of an {@link PlanetaryObject}
 *
 * @author Mooy1
 */
public final class PlanetaryType extends UniversalType {

    /**
     * Orbit, asteroid belts, etc
     */
    public static final PlanetaryType SPACE = new PlanetaryType("太空");

    /**
     * Gaseous planets ex: jupiter
     */
    public static final PlanetaryType GAS_GIANT = new PlanetaryType("氣態巨行星");

    /**
     * Frozen planets ex: neptune
     */
    public static final PlanetaryType FROZEN = new PlanetaryType("凍結");

    /**
     * Mostly liquid planets
     */
    public static final PlanetaryType OCEANIC = new PlanetaryType("海洋");

    /**
     * Rocky/Solid planets ex: earth, mars, moon
     */
    public static final PlanetaryType TERRESTRIAL = new PlanetaryType("陸地");

    /**
     * Unknown
     */
    public static final PlanetaryType UNKNOWN = new PlanetaryType("未知");

    public PlanetaryType(String name) {
        super(name);
    }

}

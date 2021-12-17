package io.github.addoncommunity.galactifun.api.universe.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import lombok.NonNull;

import com.google.common.collect.ImmutableSet;
import io.github.addoncommunity.galactifun.api.universe.PlanetaryObject;

/**
 * Type of an {@link PlanetaryObject}
 *
 * @author Mooy1
 */
public final class PlanetaryType extends UniversalType {

    private static final Map<String, PlanetaryType> allTypes = new HashMap<>();

    /**
     * Orbit, asteroid belts, etc
     */
    public static final PlanetaryType SPACE = new PlanetaryType("太空", "SPACE");

    /**
     * Gaseous planets ex: jupiter
     */
    public static final PlanetaryType GAS_GIANT = new PlanetaryType("氣態巨行星", "GAS_GIANT");

    /**
     * Frozen planets ex: neptune
     */
    public static final PlanetaryType FROZEN = new PlanetaryType("凍結", "FROZEN");

    /**
     * Mostly liquid planets
     */
    public static final PlanetaryType OCEANIC = new PlanetaryType("海洋", "OCEANIC");

    /**
     * Rocky/Solid planets ex: earth, mars, moon
     */
    public static final PlanetaryType TERRESTRIAL = new PlanetaryType("陸地", "TERRESTRIAL");

    /**
     * Unknown
     */
    public static final PlanetaryType UNKNOWN = new PlanetaryType("未知", "UNKNOWN");

    public PlanetaryType(String name, String id) {
        super(name, id);
        allTypes.put(id, this);
    }

    public static PlanetaryType getById(@NonNull String id) {
        return allTypes.get(id);
    }

    @Nonnull
    public static Set<PlanetaryType> allTypes() {
        return ImmutableSet.copyOf(allTypes.values());
    }

}

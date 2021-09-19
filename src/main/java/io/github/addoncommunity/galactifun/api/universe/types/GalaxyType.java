package io.github.addoncommunity.galactifun.api.universe.types;

/**
 * Types of galaxies
 *
 * @author Mooy1
 */
public final class GalaxyType extends UniversalType {

    public static final GalaxyType ELLIPTICAL = new GalaxyType("橢圓形");
    public static final GalaxyType SPIRAL = new GalaxyType("螺旋");
    public static final GalaxyType IRREGULAR = new GalaxyType("不規則");

    public GalaxyType(String name) {
        super(name);
    }

}

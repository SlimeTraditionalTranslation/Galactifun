package io.github.addoncommunity.galactifun.api.items.spacesuit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public final class SpaceSuitStat {

    public static final SpaceSuitStat HEAT_RESISTANCE = new SpaceSuitStat("&c耐熱性");
    public static final SpaceSuitStat COLD_RESISTANCE = new SpaceSuitStat("&b耐冷性");
    public static final SpaceSuitStat RADIATION_RESISTANCE = new SpaceSuitStat("&4耐輻射性");

    private final String name;

}

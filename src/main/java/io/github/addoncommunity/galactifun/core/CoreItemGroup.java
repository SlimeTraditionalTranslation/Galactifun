package io.github.addoncommunity.galactifun.core;

import lombok.experimental.UtilityClass;

import org.bukkit.Material;

import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.base.GalactifunHead;
import io.github.addoncommunity.galactifun.core.categories.AssemblyItemGroup;
import io.github.addoncommunity.galactifun.core.categories.GalacticItemGroup;
import io.github.mooy1.infinitylib.groups.MultiGroup;
import io.github.mooy1.infinitylib.groups.SubGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;

/**
 * Slimefun item categories
 *
 * @author Mooy1
 */
// TODO move these categories somewhere not public, addons should use their own
@UtilityClass
public final class CoreItemGroup {

    /* cheat categories */
    public static final ItemGroup ASSEMBLY = new SubGroup(
            "assembly", new CustomItemStack(Material.SMITHING_TABLE, "&f裝配台配方")
    );

    /* normal categories */
    public static final ItemGroup EQUIPMENT = new SubGroup(
            "equipment", new CustomItemStack(Material.IRON_HELMET, "&f裝備")
    );
    public static final ItemGroup ITEMS = new SubGroup(
            "items", new CustomItemStack(GalactifunHead.ROCKET, "&f銀河科技")
    );
    public static final ItemGroup COMPONENTS = new SubGroup(
            "components", new CustomItemStack(Material.IRON_INGOT, "&f銀河科技 - 組件")
    );
    public static final ItemGroup MACHINES = new SubGroup(
            "machines", new CustomItemStack(Material.REDSTONE_LAMP, "&f銀河科技 - 機器")
    );
    public static final ItemGroup BLOCKS = new SubGroup(
            "blocks", new CustomItemStack(Material.COBBLESTONE, "&f銀河科技 - 方塊")
    );
    public static final ItemGroup RELICS = new SubGroup(
            "relics", new CustomItemStack(Material.CHISELED_POLISHED_BLACKSTONE, "&f銀河科技 - 古遺物")
    );

    public static final AssemblyItemGroup ASSEMBLY_CATEGORY = new AssemblyItemGroup(
            Galactifun.createKey("assembly_flex"),
            new CustomItemStack(Material.SMITHING_TABLE, "&f裝配台配方"));

    public static void setup(Galactifun galactifun) {
        ItemGroup universe = new GalacticItemGroup(Galactifun.createKey("galactic_flex"),
                new CustomItemStack(Material.END_STONE, "&b宇宙"));

        new MultiGroup("main",
                new CustomItemStack(Material.BEACON, "&b銀河科技"),
                EQUIPMENT, ITEMS, COMPONENTS, MACHINES, BLOCKS, universe, ASSEMBLY_CATEGORY, RELICS
        ).register(galactifun);
    }

}

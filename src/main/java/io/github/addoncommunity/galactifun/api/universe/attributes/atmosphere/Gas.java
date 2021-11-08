package io.github.addoncommunity.galactifun.api.universe.attributes.atmosphere;

import javax.annotation.Nonnull;

import lombok.Getter;

import org.bukkit.inventory.ItemStack;

import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.base.items.DiamondAnvil;
import io.github.addoncommunity.galactifun.core.CoreItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.generators.CombustionGenerator;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.Freezer;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineFuel;

public enum Gas {
    OXYGEN("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2"),
    NITROGEN("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2"),
    CARBON_DIOXIDE("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2"),
    WATER("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2"),
    HELIUM("93dfa904fe3d0306666a573c22eec1dd0a8051e32a38ea2d19c4b5867e232a49"),
    ARGON("ea005531b6167a86fb09d6c0f3db60f2650162d0656c2908d07b377111d8f2a2"),
    METHANE("ea005531b6167a86fb09d6c0f3db60f2650162d0656c2908d07b377111d8f2a2"),
    HYDROCARBONS("725691372e0734bfb57bb03690490661a83f053a3488860df3436ce1caa24d11"),
    HYDROGEN("725691372e0734bfb57bb03690490661a83f053a3488860df3436ce1caa24d11"),
    SULFUR("c7a1ece691ad28d17bbbcecb22270c85e1c9581485806264c676de67c272e2d0"),
    AMMONIA("c7a1ece691ad28d17bbbcecb22270c85e1c9581485806264c676de67c272e2d0"),
    OTHER;

    static {
        if (SlimefunItems.FREEZER_2.getItem() instanceof Freezer freezer) {
            freezer.registerRecipe(10, Gas.NITROGEN.item(), SlimefunItems.REACTOR_COOLANT_CELL.asQuantity(4));
        }
        if (SlimefunItems.COMBUSTION_REACTOR.getItem() instanceof CombustionGenerator generator) {
            generator.registerFuel(new MachineFuel(15, Gas.HYDROGEN.item()));
            generator.registerFuel(new MachineFuel(20, Gas.SULFUR.item()));
            generator.registerFuel(new MachineFuel(30, Gas.HYDROCARBONS.item()));
            generator.registerFuel(new MachineFuel(70, Gas.AMMONIA.item()));
            generator.registerFuel(new MachineFuel(200, Gas.METHANE.item()));
        }
        if (BaseItems.DIAMOND_ANVIL.getItem() instanceof DiamondAnvil anvil) {
            anvil.registerRecipe(10, Gas.HYDROGEN.item().asQuantity(4), Gas.HELIUM.item());
        }
    }

    @Getter
    private final SlimefunItemStack item;

    Gas(String texture) {
        this.item = new SlimefunItemStack(
                "ATMOSPHERIC_GAS_" + this.name(),
                SlimefunUtils.getCustomHead(texture),
                "&f" + ChatUtils.humanize(this.name()) + " 煤氣罐",
                "",
                "&f&o材質由 Sefiraat 製作"
        );

        new SlimefunItem(CoreItemGroup.ITEMS, this.item, RecipeType.NULL, new ItemStack[9]).register(Galactifun.instance());
    }

    Gas() {
        this.item = null;
    }


    @Nonnull
    @Override
    public String toString() {
        return ChatUtils.humanize(this.name());
    }

}

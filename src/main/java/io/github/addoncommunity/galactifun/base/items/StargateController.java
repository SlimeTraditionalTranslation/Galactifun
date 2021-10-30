package io.github.addoncommunity.galactifun.base.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.util.BSUtils;
import io.github.mooy1.infinitylib.common.Events;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.mooy1.infinitylib.machines.MenuBlock;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

// TODO clean up if possible
public final class StargateController extends SlimefunItem implements Listener {

    private static final int[] BACKGROUND = new int[] { 1, 2, 6, 7, 8 };
    private static final int ADDRESS_SLOT = 3;
    private static final int DESTINATION_SLOT = 4;
    private static final int DEACTIVATE_SLOT = 5;

    private static final ComponentPosition[] RING_POSITIONS = new ComponentPosition[] {
            // bottom
            new ComponentPosition(0, 1),
            new ComponentPosition(0, -1),

            // corners
            new ComponentPosition(1, -2),
            new ComponentPosition(1, 2),
            new ComponentPosition(5, -2),
            new ComponentPosition(5, 2),

            // left side
            new ComponentPosition(2, 3),
            new ComponentPosition(3, 3),
            new ComponentPosition(4, 3),

            // right side
            new ComponentPosition(2, -3),
            new ComponentPosition(3, -3),
            new ComponentPosition(4, -3),

            // top
            new ComponentPosition(6, -1),
            new ComponentPosition(6, 0),
            new ComponentPosition(6, 1),
    };

    private static final ComponentPosition[] PORTAL_POSITIONS;
    private static final int GATEWAY_TICKS = 201;

    static {
        List<ComponentPosition> portalPositions = new LinkedList<>(Arrays.asList(
                new ComponentPosition(1, -1),
                new ComponentPosition(1, 0),
                new ComponentPosition(1, 1)
        ));
        for (int y = 2; y <= 4; y++) {
            for (int z = -2; z <= 2; z++) {
                portalPositions.add(new ComponentPosition(y, z));
            }
        }
        portalPositions.add(new ComponentPosition(5, -1));
        portalPositions.add(new ComponentPosition(5, 0));
        portalPositions.add(new ComponentPosition(5, 1));

        PORTAL_POSITIONS = portalPositions.toArray(new ComponentPosition[0]);
    }

    public StargateController(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        Events.registerListener(this);

        addItemHandler((BlockUseHandler) e -> e.getClickedBlock().ifPresent(b -> onUse(e, e.getPlayer(), b)));

        addItemHandler(new BlockBreakHandler(true, true) {
            @Override
            @ParametersAreNonnullByDefault
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                if (Boolean.parseBoolean(BlockStorage.getLocationInfo(e.getBlock().getLocation(), "locked"))) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "在摧毀星際之門之前, 請先停用星際之門");
                }
            }
        });
    }

    public static boolean isPartOfStargate(@Nonnull Block b) {
        for (ComponentPosition position : RING_POSITIONS) {
            if (!position.isInSameRing(b)) {
                return false;
            }
        }

        return true;
    }

    @Nonnull
    public static Optional<List<Block>> getRingBlocks(@Nonnull Block b) {
        List<Block> rings = new ArrayList<>();
        for (ComponentPosition position : RING_POSITIONS) {
            if (position.isInSameRing(b)) {
                rings.add(position.getBlock(b));
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(rings);
    }

    @Nonnull
    public static Optional<List<Block>> getPortalBlocks(@Nonnull Block b) {
        List<Block> portals = new ArrayList<>();
        for (ComponentPosition position : PORTAL_POSITIONS) {
            if (position.isPortal(b)) {
                portals.add(position.getBlock(b));
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(portals);
    }

    public static void lockBlocks(Block controller, boolean lock) {
        String data = Boolean.toString(lock);
        getRingBlocks(controller).ifPresent(l -> l.forEach(b -> BlockStorage.addBlockInfo(b, "locked", data)));
        getPortalBlocks(controller).ifPresent(l -> l.forEach(b -> BlockStorage.addBlockInfo(b, "locked", data)));
    }

    private void onUse(PlayerRightClickEvent event, Player p, Block b) {
        if (!isPartOfStargate(b)) {
            p.sendMessage(ChatColor.RED + "星際之門尚未組裝!");
            return;
        }
        event.cancel();
        if (getPortalBlocks(b).isEmpty()) {
            for (ComponentPosition position : PORTAL_POSITIONS) {
                Block portal = position.getBlock(b);
                portal.setType(Material.END_GATEWAY);
                EndGateway gateway = (EndGateway) portal.getState();
                gateway.setAge(GATEWAY_TICKS);
                gateway.setExitLocation(b.getLocation());
                gateway.update(false, false);
                BlockStorage.addBlockInfo(portal, "portal", "true");
            }

            String destAddress = BlockStorage.getLocationInfo(b.getLocation(), "destination");
            if (destAddress != null) {
                setDestination(destAddress, b, p);
            }

            lockBlocks(b, true);
            p.sendMessage(ChatColor.YELLOW + "星際之門已啟用!");
            return;
        }

        ChestMenu menu = getMenu(b);
        menu.open(p);
    }

    @Nonnull
    private ChestMenu getMenu(@Nonnull Block b) {
        ChestMenu menu = new ChestMenu(this.getItemName());
        for (int i : BACKGROUND) {
            menu.addItem(i, MenuBlock.BACKGROUND_ITEM, ChestMenuUtils.getEmptyClickHandler());
        }

        Location l = b.getLocation();

        String address = BlockStorage.getLocationInfo(l, "gfsgAddress");
        if (address == null) {
            String lString = String.format(
                    "%s-%d-%d-%d",
                    b.getWorld().getName(),
                    l.getBlockX(),
                    l.getBlockY(),
                    l.getBlockZ()
            );
            address = Integer.toHexString(lString.hashCode());
            BlockStorage.addBlockInfo(b, "gfsgAddress", address);
        }

        String destination = BlockStorage.getLocationInfo(l, "destination");
        destination = destination == null ? "" : destination;

        String temp = address;
        menu.addItem(ADDRESS_SLOT, new CustomItemStack(
                Material.BOOK,
                "&f地址: " + address,
                "&7點擊發送地址至聊天室"
        ), (p, i, s, c) -> {
            p.sendMessage(ChatColor.YELLOW + "地址: " + temp);
            p.closeInventory();
            return false;
        });

        menu.addItem(DEACTIVATE_SLOT, new CustomItemStack(
                Material.BARRIER,
                "&f點擊停用星際之門"
        ), (p, i, s, c) -> {
            getPortalBlocks(b).ifPresent(li -> {
                for (Block block : li) {
                    block.setType(Material.AIR);
                    BlockStorage.clearBlockInfo(block);
                }
            });
            lockBlocks(b, false);
            p.closeInventory();
            return false;
        });

        menu.addItem(DESTINATION_SLOT, new CustomItemStack(
                Material.RAIL,
                "&f點擊設置目的地",
                "&7目前的目的地: " + destination
        ), (p, i, s, c) -> {
            p.sendMessage(ChatColor.YELLOW + "輸入目標地址");
            ChatUtils.awaitInput(p, st -> setDestination(st, b, p));
            p.closeInventory();
            return false;
        });

        return menu;
    }

    private static void setDestination(String destination, Block b, Player p) {
        Location dest;
        worldLoop:
        {
            for (BlockStorage storage : Slimefun.getRegistry().getWorlds().values()) {
                for (Map.Entry<Location, Config> configEntry : storage.getRawStorage().entrySet()) {
                    String bAddress = configEntry.getValue().getString("gfsgAddress");
                    if (bAddress != null && bAddress.equals(destination)) {
                        dest = configEntry.getKey();
                        break worldLoop;
                    }
                }
            }
            p.sendMessage(ChatColor.RED + "沒有找到目的地!");
            return;
        }

        Optional<List<Block>> portalOptional = getPortalBlocks(b);
        if (portalOptional.isEmpty()) {
            p.sendMessage(ChatColor.RED + "星際之門由於某些原因沒有點亮...");
            return;
        }

        BSUtils.setStoredLocation(b.getLocation(), "dest", dest);

        p.sendMessage(ChatColor.YELLOW + String.format(
                "設置星際之門目的地為 %d %d %d 在 %s",
                dest.getBlockX(),
                dest.getBlockY(),
                dest.getBlockZ(),
                dest.getWorld().getName()
        ));

        BlockStorage.addBlockInfo(b, "destination", destination);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onGateBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getType() == Material.END_GATEWAY &&
                Boolean.parseBoolean(BlockStorage.getLocationInfo(b.getLocation(), "locked"))) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "在摧毀星際之門之前, 請先停用星際之門");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onUsePortal(PlayerTeleportEndGatewayEvent e) {
        if (!BSUtils.getStoredBoolean(e.getGateway().getLocation(), "portal")) return;
        Location dest = BSUtils.getStoredLocation(e.getGateway().getExitLocation(), "dest");
        if (dest == null) return;

        e.setCancelled(true);

        Player p = e.getPlayer();

        if (p.hasMetadata("disableStargate")) return;

        Block b = dest.getBlock();
        if (BlockStorage.check(b, BaseItems.STARGATE_CONTROLLER.getItemId()) &&
                StargateController.getPortalBlocks(b).isEmpty()) {
            e.getPlayer().sendMessage(ChatColor.RED + "目的地的星際之門尚未被啟用");
            return;
        }

        Block destBlock = b.getRelative(1, 0, 0);
        if (destBlock.getType().isEmpty()) {
            PaperLib.teleportAsync(e.getPlayer(), destBlock.getLocation());
            p.setMetadata("disableStargate", new FixedMetadataValue(Galactifun.instance(), true));
            Scheduler.run(10, () -> p.removeMetadata("disableStargate", Galactifun.instance()));
        } else {
            e.getPlayer().sendMessage(ChatColor.RED + "目的地已被封鎖");
        }
    }

    private static final record ComponentPosition(int y, int z) {

        public boolean isInSameRing(@Nonnull Block b) {
            return BlockStorage.check(b.getRelative(0, this.y, this.z)) instanceof StargateRing;
        }

        @Nonnull
        public Block getBlock(@Nonnull Block b) {
            return b.getRelative(0, this.y, this.z);
        }

        public boolean isPortal(@Nonnull Block b) {
            return b.getRelative(0, this.y, this.z).getType() == Material.END_GATEWAY;
        }

    }

}

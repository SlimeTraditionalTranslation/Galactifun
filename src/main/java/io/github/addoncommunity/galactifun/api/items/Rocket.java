package io.github.addoncommunity.galactifun.api.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;

import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.api.worlds.PlanetaryWorld;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.base.items.knowledge.KnowledgeLevel;
import io.github.addoncommunity.galactifun.core.WorldSelector;
import io.github.addoncommunity.galactifun.core.managers.WorldManager;
import io.github.addoncommunity.galactifun.util.ChunkStorage;
import io.github.addoncommunity.galactifun.util.Util;
import io.github.mooy1.infinitylib.common.PersistentType;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.mooy1.infinitylib.common.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class Rocket extends SlimefunItem implements RecipeDisplayItem {

    public static final NamespacedKey CARGO_KEY = Galactifun.createKey("cargo");

    // todo Move static to some sort of RocketManager
    private static final List<String> LAUNCH_MESSAGES = Galactifun.instance().getConfig().getStringList("rockets.launch-msgs");
    private static final double DISTANCE_PER_FUEL = 2_000_000 / Util.KM_PER_LY;

    @Getter
    private final int fuelCapacity;
    @Getter
    private final int storageCapacity;
    @Getter
    private final Map<String, Double> allowedFuels = new HashMap<>();

    public Rocket(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int fuelCapacity, int storageCapacity) {
        super(category, item, recipeType, recipe);

        this.fuelCapacity = fuelCapacity;
        this.storageCapacity = storageCapacity;
        for (Map.Entry<ItemStack, Double> entry : getAllowedFuels().entrySet()) {
            allowedFuels.put(StackUtils.getIdOrType(entry.getKey()), entry.getValue());
        }

        addItemHandler((BlockUseHandler) e -> e.getClickedBlock().ifPresent(block -> {
            e.cancel();
            openGUI(e.getPlayer(), block);
        }));

        addItemHandler(new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                Block b = e.getBlock();
                BlockData data = b.getBlockData();
                if (data instanceof Rotatable) {
                    ((Rotatable) data).setRotation(BlockFace.NORTH);
                }
                b.setBlockData(data, true);
            }
        });
    }

    private static Runnable sendRandomMessage(Player p) {
        return () -> p.sendMessage(ChatColor.GOLD + LAUNCH_MESSAGES.get(ThreadLocalRandom.current().nextInt(LAUNCH_MESSAGES.size())) + "...");
    }

    private void openGUI(@Nonnull Player p, @Nonnull Block b) {
        if (!BlockStorage.check(b, this.getId())) return;

        if (ChunkStorage.isTagged(b, "isLaunching")) {
            p.sendMessage(ChatColor.RED + "The rocket is already launching!");
            return;
        }

        WorldManager worldManager = Galactifun.worldManager();
        PlanetaryWorld world = worldManager.getWorld(p.getWorld());
        if (world == null) {
            p.sendMessage(ChatColor.RED + "You cannot travel to space from this world!");
            return;
        }

        String string = Objects.requireNonNullElse(BlockStorage.getLocationInfo(b.getLocation(), "fuel"), "0");
        int fuel = Integer.parseInt(string);
        if (fuel == 0) {
            p.sendMessage(ChatColor.RED + "The rocket has no fuel!");
            return;
        }

        string = BlockStorage.getLocationInfo(b.getLocation(), "fuelType");
        if (string == null) return;
        String fuelType = string;
        double eff = allowedFuels.get(string);

        // ly
        double maxDistance = fuel * DISTANCE_PER_FUEL * eff;

        new WorldSelector((player, obj, lore) -> {
            if (obj instanceof PlanetaryWorld) {
                // ly
                double dist = obj.distanceTo(world);
                if (dist > maxDistance) return false;

                lore.add(Component.empty());
                lore.add(Component.text()
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text("Distance: "))
                        .append(Component.text((long) Math.ceil(dist / Util.KM_PER_LY)))
                        .build()
                );
                lore.add(Component.text()
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text("Fuel: "))
                        .append(Component.text((long) Math.ceil(dist / (DISTANCE_PER_FUEL * eff))))
                        .build()
                );
            }
            return true;
        }, (player, pw) -> {
            player.closeInventory();
            long usedFuel =(long) Math.ceil(pw.distanceTo(world) / (DISTANCE_PER_FUEL * eff));
            player.sendMessage(ChatColor.YELLOW + "You are going to " + pw.name() + " and will use " +
                    usedFuel + " fuel. Are you sure you want to do that? (yes/no)");
            ChatUtils.awaitInput(player, (input) -> {
                if (input.equalsIgnoreCase("yes")) {
                    p.sendMessage(ChatColor.YELLOW + "Please enter destination coordinates in the form of <x> <z> (i.e. -123 456):");
                    ChatUtils.awaitInput(p, (response) -> {
                        String trimmed = response.trim();
                        if (Util.COORD_PATTERN.matcher(trimmed).matches()) {
                            String[] split = Util.SPACE_PATTERN.split(trimmed);
                            int x = Integer.parseInt(split[0]);
                            int z = Integer.parseInt(split[1]);
                            WorldBorder border = pw.world().getWorldBorder();
                            if (border.isInside(new Location(pw.world(), x, 0, z))) {
                                launch(player, b, pw, fuel - usedFuel, fuelType, x, z);
                            } else {
                                player.sendMessage(ChatColor.RED + "The coordinates you entered are outside of the world border");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Invalid coordinate format! Please use the format <x> <z>");
                        }
                    });
                }
            });
        }).open(p);
    }

    public void launch(@Nonnull Player p, @Nonnull Block rocket, PlanetaryWorld worldTo, long fuelLeft, String fuelType, int x, int z) {
        ChunkStorage.tag(rocket, "isLaunching");

        World world = p.getWorld();

        new BukkitRunnable() {
            private final Block pad = rocket.getRelative(BlockFace.DOWN);
            private int times = 0;

            @Override
            public void run() {
                if (this.times++ < 20) {
                    for (BlockFace face : Util.SURROUNDING_FACES) {
                        Block block = this.pad.getRelative(face);
                        world.spawnParticle(Particle.ASH, block.getLocation(), 100, 0.5, 0.5, 0.5);
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Galactifun.instance(), 0, 10);

        World to = worldTo.world();

        Chunk destChunk = to.getChunkAt(x >> 4, z >> 4);
        if (!destChunk.isLoaded()) {
            destChunk.load(true);
        }

        Scheduler.run(40, sendRandomMessage(p));
        Scheduler.run(80, sendRandomMessage(p));
        Scheduler.run(120, sendRandomMessage(p));
        Scheduler.run(160, sendRandomMessage(p));
        Scheduler.run(200, () -> {
            p.sendMessage(ChatColor.GOLD + "Verifying blast awesomeness...");

            Block destBlock = null;
            for (int y = to.getMaxHeight(); y > to.getMinHeight(); y--) {
                Block b = to.getBlockAt(x, y, z);
                if ((b.isBuildable() || b.isLiquid()) && !BlockStorage.check(b, BaseItems.LANDING_HATCH.getItemId())) {
                    destBlock = b.getRelative(BlockFace.UP);
                    break;
                }
            }

            if (destBlock == null) {
                destBlock = to.getBlockAt(x, to.getMaxHeight() - 4, z);
            }

            if (!Slimefun.getProtectionManager().hasPermission(p, destBlock, Interaction.PLACE_BLOCK)) {
                p.sendMessage(ChatColor.RED + "Launch was not successful! You do not have permission to land there.");
                ChunkStorage.untag(rocket, "isLaunching");
                return;
            }

            // check if we haven't dropped the chest yet
            if (destBlock.getRelative(BlockFace.DOWN).getType() != Material.CHEST) {
                destBlock.setType(Material.CHEST);
                BlockState state = PaperLib.getBlockState(destBlock, false).getState();
                if (state instanceof Chest chest) {
                    Inventory inv = chest.getInventory();
                    inv.addItem(this.getItem().clone());

                    ItemStack fuel = StackUtils.itemByIdOrType(fuelType);
                    fuel = fuel.clone();
                    fuel.setAmount((int) fuelLeft);
                    inv.addItem(fuel);

                    PersistentDataContainer container = ((Skull) rocket.getState()).getPersistentDataContainer();
                    List<ItemStack> cargo = container.getOrDefault(CARGO_KEY, PersistentType.ITEM_STACK_LIST, new ArrayList<>());

                    for (ItemStack item : cargo) {
                        HashMap<Integer, ItemStack> notFit = inv.addItem(item);
                        for (ItemStack nf : notFit.values()) {
                            to.dropItemNaturally(destBlock.getLocation().add(0, 1, 0), nf);
                        }
                    }
                }
                state.update();
            }

            boolean showLaunchAnimation = false;
            for (Entity entity : world.getEntities()) {
                if ((entity instanceof LivingEntity && !(entity instanceof ArmorStand)) || entity instanceof Item) {
                    if (entity.getLocation().distanceSquared(rocket.getLocation()) <= 25) {
                        if (entity instanceof Player) {
                            entity.setMetadata("CanTpAlienWorld", new FixedMetadataValue(Galactifun.instance(), true));
                        }
                        PaperLib.teleportAsync(entity, destBlock.getLocation().add(0, 1, 0));

                        if (KnowledgeLevel.get(p, worldTo) == KnowledgeLevel.NONE) {
                            KnowledgeLevel.BASIC.set(p, worldTo);
                        }

                    } else if (entity.getLocation().distance(rocket.getLocation()) <= 64) {
                        if (entity instanceof Player) {
                            showLaunchAnimation = true;
                        }
                    }
                }
            }

            // launch animation
            if (showLaunchAnimation) {
                Location rocketLocation = rocket.getLocation().add(0.5, -1, 0.5);
                ArmorStand armorStand = rocketLocation.getWorld().spawn(rocketLocation, ArmorStand.class);

                Skull skull = (Skull) rocket.getState();
                ItemStack stack = new ItemStack(skull.getType());
                stack.editMeta(meta -> ((SkullMeta) meta).setPlayerProfile(skull.getPlayerProfile()));

                armorStand.getEquipment().setHelmet(stack);
                armorStand.setInvisible(true);
                armorStand.setInvulnerable(true);
                armorStand.setMarker(false);
                armorStand.setBasePlate(false);

                new BukkitRunnable() {
                    int i = 0;

                    @Override
                    public void run() {
                        i++;
                        armorStand.setVelocity(new Vector(0, 0.8 + i / 10D, 0));
                        rocketLocation.getWorld().spawnParticle(getLaunchParticles(), armorStand.getLocation(), 10);
                        if (i > 40) {
                            armorStand.remove();
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Galactifun.instance(), 0, 8);
            }

            rocket.setType(Material.AIR);
            BlockStorage.clearBlockInfo(rocket);
            ChunkStorage.untag(rocket, "isLaunching");
        });
    }

    protected abstract Map<ItemStack, Double> getAllowedFuels();

    @Nonnull
    protected Particle getLaunchParticles() {
        return Particle.ASH;
    }

    @Nonnull
    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> ret = new ArrayList<>();

        for (Map.Entry<ItemStack, Double> entry : getAllowedFuels().entrySet()) {
            ret.add(new CustomItemStack(
                    entry.getKey(),
                    ItemUtils.getItemName(entry.getKey()),
                    "&7Efficiency: " + entry.getValue() + 'x'
            ));
        }

        return ret;
    }
}

package io.github.addoncommunity.galactifun.core.commands;

import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.api.worlds.AlienWorld;
import io.github.mooy1.infinitylib.commands.SubCommand;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;

public final class ChunkverCommand extends SubCommand {

    public ChunkverCommand() {
        super("chunkver", "獲得當前區塊版本", false);
    }

    @Override
    public void execute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (!(sender instanceof Player p)) return;

        Chunk chunk = p.getChunk();
        if (Galactifun.worldManager().getAlienWorld(chunk.getWorld()) == null) {
            p.sendMessage(ChatColor.RED + "你並不在Galactifun星球上!");
            return;
        }

        p.sendMessage(ChatColor.GOLD + "這個區塊生成由 " + PersistentDataAPI.getString(
                chunk,
                AlienWorld.CHUNK_VER_KEY,
                "Galactifun vALPHA"
        ));
    }

    @Override
    public void complete(@Nonnull CommandSender sender, @Nonnull String[] args, @Nonnull List<String> tabs) {

    }

}

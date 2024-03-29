package me.jofri.roguelikespigot.command;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import me.jofri.roguelikespigot.DungeonManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearDungeonCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        World world = BukkitAdapter.adapt(Bukkit.getWorld("roguelike"));

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            // Create a CuboidRegion based on the player's coordinates
            CuboidRegion region = new CuboidRegion(
                    BukkitAdapter.adapt(Bukkit.getWorld("roguelike")),
                    BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("roguelike"), -6, -55, -5)),
                    BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("roguelike"), 232, -62, 232))
            );

            BlockState state = BukkitAdapter.adapt(Material.AIR.createBlockData());

            // Set blocks within the region to create a wall
            editSession.setBlocks(region, state);
        } catch (Exception e) {
            e.printStackTrace();

        }
        DungeonManager.resetDungeon();
        return true;
    }
}

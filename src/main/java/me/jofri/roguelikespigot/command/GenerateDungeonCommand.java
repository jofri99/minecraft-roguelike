package me.jofri.roguelikespigot.command;

import me.jofri.roguelikespigot.DungeonManager;
import me.jofri.roguelikespigot.dungeon.Dungeon;
import me.jofri.roguelikespigot.mobs.enemies.CustomZombie;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GenerateDungeonCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 1) {
            Dungeon dungeon = new Dungeon(Integer.parseInt(args[0]));
            DungeonManager.setDungeon(dungeon);
        }
        if (args.length == 2) {
            Dungeon dungeon = new Dungeon(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            DungeonManager.setDungeon(dungeon);
        }
        return true;
    }
}

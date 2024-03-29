package me.jofri.roguelikespigot;

import me.jofri.roguelikespigot.command.ClearDungeonCommand;
import me.jofri.roguelikespigot.command.GenerateDungeonCommand;
import me.jofri.roguelikespigot.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class RoguelikeSpigot extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getCommand("generatedungeon").setExecutor(new GenerateDungeonCommand());
        this.getCommand("cleardungeon").setExecutor(new ClearDungeonCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

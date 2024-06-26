package me.jofri.roguelikespigot.mobs.enemies;

import me.jofri.roguelikespigot.mobs.EnemyType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.persistence.PersistentDataType;

public class CustomZombie extends Zombie {
    public CustomZombie(Location spawnLocation, int id) {
        super(EntityType.ZOMBIE, ((CraftWorld) Bukkit.getWorld("roguelike")).getHandle());
        Level level = ((CraftWorld) Bukkit.getWorld("roguelike")).getHandle();
        this.setPos(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
        this.setId(id);
        this.getBukkitEntity().getPersistentDataContainer().set(
                new NamespacedKey("roguelike", "mob_type"),
                PersistentDataType.STRING,
                EnemyType.NORMAL_ZOMBIE.toString());

        level.addFreshEntity(this);
    }
}

package me.jofri.roguelikespigot.mobs.enemies;

import me.jofri.roguelikespigot.mobs.EnemyType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.persistence.PersistentDataType;

public class CustomSkeleton extends Skeleton {
    public String type = "NORMAL_SKELETON";

    public CustomSkeleton(Location spawnLocation, int id) {
        super(EntityType.SKELETON, ((CraftWorld) Bukkit.getWorld("roguelike")).getHandle());
        Level level = ((CraftWorld) Bukkit.getWorld("roguelike")).getHandle();

        this.setPos(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
        this.setId(id);
        this.getBukkitEntity().getPersistentDataContainer().set(
                new NamespacedKey("roguelike", "mob_type"),
                PersistentDataType.STRING,
                EnemyType.NORMAL_SKELETON.toString());

        level.addFreshEntity(this);
    }
}

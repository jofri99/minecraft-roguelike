package me.jofri.roguelikespigot.mobs;

import me.jofri.roguelikespigot.mobs.enemies.CustomSkeleton;
import me.jofri.roguelikespigot.mobs.enemies.CustomZombie;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomEntity {

    public static void spawn(EnemyType type, Location spawnLoc, int id) {
        switch (type) {
            case NORMAL_ZOMBIE:
                new CustomZombie(spawnLoc, id);
                break;
            case NORMAL_SKELETON:
                new CustomSkeleton(spawnLoc, id);
                break;
        }
    }

    public static void setDrops(List<ItemStack> drops, EnemyType type) {
        drops.clear();
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();

        LootTable lootTable = new LootTable(LootTableType.NORMAL);
        ArrayList<ItemStack> newDrops = lootTable.getDrops();
        drops.addAll(newDrops);
    }

}

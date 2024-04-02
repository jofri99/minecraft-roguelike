package me.jofri.roguelikespigot.listener;

import me.jofri.roguelikespigot.DungeonManager;
import me.jofri.roguelikespigot.dungeon.Dungeon;

import me.jofri.roguelikespigot.mobs.CustomEntity;
import me.jofri.roguelikespigot.mobs.EnemyType;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.persistence.PersistentDataType;

public class EntityListener implements Listener {

    @EventHandler
    public void onEnemyDeath(EntityDeathEvent event) {
        Dungeon dungeon = DungeonManager.getDungeon();
        int id = event.getEntity().getEntityId();

        String type = event.getEntity().getPersistentDataContainer().get(new NamespacedKey("roguelike", "mob_type"), PersistentDataType.STRING);

        CustomEntity.setDrops(event.getDrops(), EnemyType.valueOf(type));
        dungeon.removeEnemyId(id);

        if (dungeon.isEnemyIdsEmpty()) {
            dungeon.openDoors();
        }
    }
}

package me.jofri.roguelikespigot.dungeon;

import lombok.Getter;
import lombok.Setter;
import me.jofri.roguelikespigot.DungeonManager;
import me.jofri.roguelikespigot.mobs.CustomEntity;
import me.jofri.roguelikespigot.mobs.EnemyType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;

@Getter
@Setter
public class Room {
    private static int nextId = 1;
    private int id;
    private RoomType type;
    private String path;
    private int x;
    private int z;

    private boolean isCleared = false;

    private ArrayList<EnemyType> enemies = null;

    public Room(RoomType type, String fileName, int x, int z) {
        this.type = type;
        this.path = fileName;
        this.x = x;
        this.z = z;
        this.id = nextId;
        nextId++;
    }

    public void isEntered() {
        if (!this.isCleared && enemies != null && type != RoomType.STARTER && type != RoomType.SHOP && type != RoomType.TREASURE && type != RoomType.BOSS) {
            spawnEnemies();
            DungeonManager.getDungeon().lockDoors();
            this.isCleared = true;
        }
    }

    private void spawnEnemies() {
        World world = Bukkit.getWorld("roguelike");
        if (world != null) {
            int enemyId = 0;
            for (EnemyType enemyType : enemies) {
                addEnemy(enemyType, world, enemyId);
                enemyId++;
            }
        }
    }

    private void addEnemy(EnemyType enemyType, World world, int enemyId) {
        Location roomLoc = Dungeon.convertCoords(x, z);
        CustomEntity.spawn(enemyType, new Location(world, roomLoc.getX() + 7, -55, roomLoc.getZ() + 7), enemyId);
        DungeonManager.getDungeon().addEnemyId(enemyId);
    }
}
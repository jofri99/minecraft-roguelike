package me.jofri.roguelikespigot.dungeon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Dungeon {
    private ArrayList<Room> generatedRooms = new ArrayList<>();
    private ArrayList<Location> doorLocations = new ArrayList<>();
    private long seed;
    private Random random;

    public Dungeon(int stage, long seed) {
        this.random = new Random(seed);
        this.seed = seed;
        RoomGenerator roomGenerator = new RoomGenerator(random, stage);
        DungeonLayout dungeonLayout = roomGenerator.generate();
        generatedRooms = dungeonLayout.getRooms();
        doorLocations = dungeonLayout.getDoorLocations();
        Enemy zombie = new Enemy(EntityType.ZOMBIE);
        Enemy skeleton = new Enemy(EntityType.SKELETON);
        ArrayList<Enemy> types = new ArrayList<>(Arrays.asList(zombie, zombie, skeleton, skeleton));
        for(Room room: generatedRooms) {
            RoomType type = room.getType();
            if(type != RoomType.SHOP && type != RoomType.STARTER && type != RoomType.TREASURE && type != RoomType.BOSS) {
                room.setEnemies(types);
            }
        }
        openDoors();
    }

    public static boolean isPlayerInRoom(Room room, Location playerLocation) {
        Location roomLoc = convertCoords(room.getX(), room.getZ());
        double minX = 0;
        double minZ = 0;
        double maxX = 0;
        double maxZ = 0;

        switch (room.getType()) {
            case SHOP:
            case BOSS:
            case TREASURE:
            case STARTER:
            case ONE_BY_ONE:
                minX = roomLoc.getX();
                minZ = roomLoc.getZ();
                maxX = minX + 15;
                maxZ = minZ + 15;

                if (playerLocation.getX() > minX && playerLocation.getZ() > minZ && playerLocation.getX() < maxX && playerLocation.getZ() < maxZ) {
                    return true;
                }
                return false;
            case ONE_BY_TWO:
                roomLoc = convertCoords(room.getX(), room.getZ());

                minX = roomLoc.getX();
                minZ = roomLoc.getZ();
                maxX = minX + 15;
                maxZ = minZ +32;
                if (playerLocation.getX() > minX && playerLocation.getZ() > minZ && playerLocation.getX() < maxX && playerLocation.getZ() < maxZ) {
                    return true;
                }
                return false;
            case TWO_BY_ONE:
                roomLoc = convertCoords(room.getX(), room.getZ());

                minX = roomLoc.getX();
                minZ = roomLoc.getZ();
                maxX = minX + 32;
                maxZ = minZ +15;

                if (playerLocation.getX() > minX && playerLocation.getZ() > minZ && playerLocation.getX() < maxX && playerLocation.getZ() < maxZ) {
                    return true;
                }
                return false;
            case TWO_BY_TWO:
                roomLoc = convertCoords(room.getX(), room.getZ());

                minX = roomLoc.getX();
                minZ = roomLoc.getZ();
                maxX = minX + 32;
                maxZ = minZ +32;
                if (playerLocation.getX() > minX && playerLocation.getZ() > minZ && playerLocation.getX() < maxX && playerLocation.getZ() < maxZ) {
                    return true;
                }
                return false;

        }
        return false;
    }

    public void lockDoors() {
        for(Location doorLoc: doorLocations) {
            Block bottomBlock = doorLoc.getBlock();
            Block topBlock = doorLoc.getBlock().getRelative(BlockFace.UP);
            bottomBlock.setType(Material.OAK_PLANKS);
            topBlock.setType(Material.OAK_PLANKS);
        }
    }

    public void openDoors() {
        for(Location doorLoc: doorLocations) {
            Block bottomBlock = doorLoc.getBlock();
            Block topBlock = doorLoc.getBlock().getRelative(BlockFace.UP);
            bottomBlock.setType(Material.AIR);
            topBlock.setType(Material.AIR);
        }
    }

    public Dungeon(int stage) {
        this(stage, System.currentTimeMillis());
    }

    public ArrayList<Room> getGeneratedRooms() {
        return generatedRooms;
    }

    public ArrayList<Location> getDoorLocations() {
        return doorLocations;
    }

    public static Location convertCoords(int x, int y) {
        return new Location(Bukkit.getWorld("roguelike"), x * 17, -60, y * 17);
    }

}


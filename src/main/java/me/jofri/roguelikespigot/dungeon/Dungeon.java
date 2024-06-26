package me.jofri.roguelikespigot.dungeon;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import me.jofri.roguelikespigot.mobs.EnemyType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Dungeon {

  private ArrayList<Room> generatedRooms = new ArrayList<>();
  private ArrayList<Location> doorLocations = new ArrayList<>();
  private ArrayList<Integer> enemyIds = new ArrayList<>();
  private long seed;
  private Random random;
  private Room currentRoom;

  public Dungeon(int stage, long seed) {
    this.random = new Random(seed);
    this.seed = seed;
    RoomGenerator roomGenerator = new RoomGenerator(random, stage);
    DungeonLayout dungeonLayout = roomGenerator.generate();
    generatedRooms = dungeonLayout.getRooms();
    doorLocations = dungeonLayout.getDoorLocations();

    ArrayList<EnemyType> types = new ArrayList<>(
        Arrays.asList(EnemyType.NORMAL_ZOMBIE, EnemyType.NORMAL_SKELETON));
    for (Room room : generatedRooms) {
      RoomType type = room.getType();
      if (type != RoomType.SHOP && type != RoomType.STARTER && type != RoomType.TREASURE
          && type != RoomType.BOSS) {
        room.setEnemies(types);
      }
    }
    placeTintedRocks();
    openDoors();
  }

  public void addEnemyId(int id) {
    enemyIds.add(id);
  }

  public void removeEnemyId(int id) {
    enemyIds.remove(enemyIds.indexOf(id));
  }

  public boolean isEnemyIdsEmpty() {
    if (enemyIds.size() == 0) {
      return true;
    }
    return false;
  }

  private void placeTintedRocks() {
    World world = BukkitAdapter.adapt(Bukkit.getWorld("roguelike"));
    // Create a CuboidRegion based on the player's coordinates
    CuboidRegion region = new CuboidRegion(
        BukkitAdapter.adapt(Bukkit.getWorld("roguelike")),
        BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("roguelike"), -6, -55, -5)),
        BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("roguelike"), 232, -62, 232))
    );

    ArrayList<Location> rockLocations = new ArrayList<>();
    for (BlockVector3 blockVector3 : region) {
      BlockType blockType = world.getBlock(blockVector3).getBlockType();
      Material material = BukkitAdapter.adapt(blockType);
      if (material == Material.COBBLESTONE) {
        rockLocations.add(
            new Location(Bukkit.getWorld("roguelike"), blockVector3.getX(), blockVector3.getY(),
                blockVector3.getZ()));
      }
    }
    int numTintedRocks = Math.round(rockLocations.size() / 50);
    ArrayList<Integer> tintedRockPositions = new ArrayList<>();
    for (int i = 0; i < numTintedRocks; i++) {
      tintedRockPositions.add(random.nextInt(rockLocations.size()));
    }
    for (int pos : tintedRockPositions) {
      Location tintedRockPos = rockLocations.get(pos);
      Bukkit.getWorld("roguelike").getBlockAt(tintedRockPos).setType(Material.DEEPSLATE);
    }
  }

  public boolean isPlayerInRoom(Room room, Location playerLocation) {
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

        if (isPlayerInsideRoom(minX, minZ, maxX, maxZ, playerLocation)) {
          currentRoom = room;
          return true;
        }
        return false;
      case ONE_BY_TWO:
        roomLoc = convertCoords(room.getX(), room.getZ());

        minX = roomLoc.getX();
        minZ = roomLoc.getZ();
        maxX = minX + 15;
        maxZ = minZ + 32;
        if (isPlayerInsideRoom(minX, minZ, maxX, maxZ, playerLocation)) {
          currentRoom = room;
          return true;
        }
        return false;
      case TWO_BY_ONE:
        roomLoc = convertCoords(room.getX(), room.getZ());

        minX = roomLoc.getX();
        minZ = roomLoc.getZ();
        maxX = minX + 32;
        maxZ = minZ + 15;

        if (isPlayerInsideRoom(minX, minZ, maxX, maxZ, playerLocation)) {
          currentRoom = room;
          return true;
        }
        return false;
      case TWO_BY_TWO:
        roomLoc = convertCoords(room.getX(), room.getZ());

        minX = roomLoc.getX();
        minZ = roomLoc.getZ();
        maxX = minX + 32;
        maxZ = minZ + 32;
        if (isPlayerInsideRoom(minX, minZ, maxX, maxZ, playerLocation)) {
          currentRoom = room;
          return true;
        }
        return false;

    }
    return false;
  }

  private static boolean isPlayerInsideRoom(double minX, double minZ, double maxX, double maxZ,
      Location playerLocation) {
    return playerLocation.getX() > minX && playerLocation.getZ() > minZ
        && playerLocation.getX() < maxX && playerLocation.getZ() < maxZ;
  }

  public Room getCurrentRoom() {
    return currentRoom;
  }

  public void lockDoors() {
    for (Location doorLoc : doorLocations) {
      Block bottomBlock = doorLoc.getBlock();
      Block topBlock = doorLoc.getBlock().getRelative(BlockFace.UP);
      bottomBlock.setType(Material.OAK_PLANKS);
      topBlock.setType(Material.OAK_PLANKS);
    }
  }

  public void openDoors() {
    for (Location doorLoc : doorLocations) {
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


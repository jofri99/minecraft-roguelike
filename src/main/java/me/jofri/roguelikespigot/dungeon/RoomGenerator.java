package me.jofri.roguelikespigot.dungeon;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class RoomGenerator {
    private int numRooms;

    private static final int GRID_SIZE = 10; // Number of rooms per row and column

    private ArrayList<Location> openDoorLocations = new ArrayList<>();
    private static final int SPECIAL_ROOM_CHANCE = 10;
    private ArrayList<ArrayList<Integer>> roomLayout = new ArrayList<>();
    private ArrayList<Room> generatedRooms = new ArrayList<>();

    Random random;

    public RoomGenerator(Random random, int stage) {
        this.random = random;
        this.numRooms = (int) (Math.floor((3.33 * stage) + (random.nextDouble() * 2 + 4)) - 1);

    }

    public DungeonLayout generate() {
        DungeonLayout dungeonLayout = new DungeonLayout();
        boolean dungeonGenerated = false;
        generatedRooms = new ArrayList<>();
        int generationCount = 0;

        while (!dungeonGenerated && generationCount < 500) {
            resetRoomLayout();
            generatedRooms = new ArrayList<>();
            dungeonGenerated = generateDungeon(numRooms);
            generationCount++;
        }
        if (!dungeonGenerated) {
            return dungeonLayout;
        }

        for (Room room : generatedRooms) {
            pasteRoom(Dungeon.convertCoords(room.getX(), room.getZ()), room.getPath());
            generateWalls(room);
            generateDoors(room);
        }
        dungeonLayout.setRooms(generatedRooms);
        dungeonLayout.setDoorLocations(openDoorLocations);
        return dungeonLayout;
    }

    private void resetRoomLayout() {
        ArrayList<ArrayList<Integer>> zeroRoomLayout = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            ArrayList<Integer> row = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            zeroRoomLayout.add(row);
        }
        roomLayout = zeroRoomLayout;
    }

    private boolean generateDungeon(int roomCount) {
        int failedAttempts = 0;
        Room currentRoom = new Room(RoomType.STARTER, "1by1.schem", 0, 0);
        generatedRooms.add(currentRoom);
        roomLayout.get(0).set(0, 1);

        ArrayList<RoomType> specialRooms = new ArrayList<>(Arrays.asList(RoomType.ONE_BY_TWO, RoomType.TWO_BY_TWO, RoomType.TWO_BY_ONE));

        while (generatedRooms.size() < roomCount) {
            Offset nextOffset = getNextOffset(currentRoom.getType());
            RoomType shape = RoomType.ONE_BY_ONE;
            if (random.nextInt(100) < SPECIAL_ROOM_CHANCE && specialRooms.size() > 0) {
                int specialRoomIndex = random.nextInt(specialRooms.size());
                shape = specialRooms.get(specialRoomIndex);
                specialRooms.remove(specialRoomIndex);
            }
            if (isOnGrid(currentRoom.getX() + nextOffset.x, currentRoom.getZ() + nextOffset.y, shape) && isFree(currentRoom.getX() + nextOffset.x, currentRoom.getZ() + nextOffset.y, shape)) {
                currentRoom = placeRoom(currentRoom.getX() + nextOffset.x, currentRoom.getZ() + nextOffset.y, shape);
                generatedRooms.add(currentRoom);
            } else {
                failedAttempts++;
                if (failedAttempts > 20) {
                    return false;
                }
            }
        }
        placeSpecialRooms();

        return true;
    }

    private void generateWalls(Room room) {
        Location anker = Dungeon.convertCoords(room.getX(), room.getZ());
        switch (room.getType()) {
            case BOSS:
            case TREASURE:
            case SHOP:
            case STARTER:
            case ONE_BY_ONE:
                pasteWall((int) anker.getX() - 1, (int) anker.getZ(), anker.getBlockX() - 1, (int) anker.getZ() + 16);
                pasteWall((int) anker.getX(), (int) anker.getZ() - 1, anker.getBlockX() + 16, anker.getBlockZ() - 1);
                pasteWall((int) anker.getX() + 16, (int) anker.getZ(), anker.getBlockX() + 16, anker.getBlockZ() + 16);
                pasteWall((int) anker.getX(), (int) anker.getZ() + 16, anker.getBlockX() + 16, anker.getBlockZ() + 16);
                break;
            case ONE_BY_TWO:
                pasteWall((int) anker.getX() - 1, (int) anker.getZ(), anker.getBlockX() - 1, (int) anker.getZ() + 33);
                pasteWall((int) anker.getX(), (int) anker.getZ() - 1, anker.getBlockX() + 16, anker.getBlockZ() - 1);
                pasteWall((int) anker.getX() + 16, (int) anker.getZ(), anker.getBlockX() + 16, anker.getBlockZ() + 33);
                pasteWall((int) anker.getX(), (int) anker.getZ() + 33, anker.getBlockX() + 16, anker.getBlockZ() + 33);
                break;
            case TWO_BY_ONE:
                pasteWall((int) anker.getX() - 1, (int) anker.getZ(), anker.getBlockX() - 1, (int) anker.getZ() + 16);
                pasteWall((int) anker.getX(), (int) anker.getZ() - 1, anker.getBlockX() + 33, anker.getBlockZ() - 1);
                pasteWall((int) anker.getX() + 33, (int) anker.getZ(), anker.getBlockX() + 33, anker.getBlockZ() + 16);
                pasteWall((int) anker.getX(), (int) anker.getZ() + 16, anker.getBlockX() + 33, anker.getBlockZ() + 16);
                break;
            case TWO_BY_TWO:
                pasteWall((int) anker.getX() - 1, (int) anker.getZ(), anker.getBlockX() - 1, (int) anker.getZ() + 33);
                pasteWall((int) anker.getX(), (int) anker.getZ() - 1, anker.getBlockX() + 33, anker.getBlockZ() - 1);
                pasteWall((int) anker.getX() + 33, (int) anker.getZ(), anker.getBlockX() + 33, anker.getBlockZ() + 33);
                pasteWall((int) anker.getX(), (int) anker.getZ() + 33, anker.getBlockX() + 33, anker.getBlockZ() + 33);
                break;
        }
    }

    private void generateDoors(Room room) {
        Location anker = Dungeon.convertCoords(room.getX(), room.getZ());

        switch (room.getType()) {
            case TREASURE:
            case SHOP:
                if (room.getZ() - 1 >= 0 && roomLayout.get(room.getZ() - 1).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() - 1, Material.IRON_BLOCK);
                }
                if (room.getX() - 1 >= 0 && roomLayout.get(room.getZ()).get(room.getX() - 1) == 1) {
                    pasteDoor((int) anker.getX() - 1, (int) anker.getZ() + 7, Material.IRON_BLOCK);
                }
                if (room.getZ() + 1 < roomLayout.size() && roomLayout.get(room.getZ() + 1).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() + 16, Material.IRON_BLOCK);
                }
                if (room.getX() + 1 < roomLayout.get(0).size() && roomLayout.get(room.getZ()).get(room.getX() + 1) == 1) {
                    pasteDoor((int) anker.getX() + 16, (int) anker.getZ() + 7, Material.IRON_BLOCK);
                }
                break;
            case STARTER:
            case BOSS:
            case ONE_BY_ONE:
                if (room.getZ() - 1 >= 0 && roomLayout.get(room.getZ() - 1).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() - 1, Material.OAK_PLANKS);
                }
                if (room.getX() - 1 >= 0 && roomLayout.get(room.getZ()).get(room.getX() - 1) == 1) {
                    pasteDoor((int) anker.getX() - 1, (int) anker.getZ() + 7, Material.OAK_PLANKS);
                }
                if (room.getZ() + 1 < roomLayout.size() && roomLayout.get(room.getZ() + 1).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() + 16, Material.OAK_PLANKS);
                }
                if (room.getX() + 1 < roomLayout.get(0).size() && roomLayout.get(room.getZ()).get(room.getX() + 1) == 1) {
                    pasteDoor((int) anker.getX() + 16, (int) anker.getZ() + 7, Material.OAK_PLANKS);
                }
                break;
            case ONE_BY_TWO:
                if (room.getZ() - 1 >= 0 && roomLayout.get(room.getZ() - 1).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() - 1, Material.OAK_PLANKS);
                }
                if (room.getZ() + 2 < roomLayout.size() && roomLayout.get(room.getZ() + 2).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() + 33, Material.OAK_PLANKS);
                }
                if (room.getX() - 1 >= 0 && roomLayout.get(room.getZ()).get(room.getX() - 1) == 1) {
                    pasteDoor((int) anker.getX() - 1, (int) anker.getZ() + 7, Material.OAK_PLANKS);
                }
                if (room.getX() + 1 < roomLayout.get(0).size() && roomLayout.get(room.getZ()).get(room.getX() + 1) == 1) {
                    pasteDoor((int) anker.getX() + 16, (int) anker.getZ() + 7, Material.OAK_PLANKS);
                }
                if (room.getX() + 1 < roomLayout.get(0).size() && roomLayout.get(room.getZ() + 1).get(room.getX() + 1) == 1) {
                    pasteDoor((int) anker.getX() + 16, (int) anker.getZ() + 24, Material.OAK_PLANKS);
                }
                if (room.getX() - 1 >= 0 && roomLayout.get(room.getZ() + 1).get(room.getX() - 1) == 1) {
                    pasteDoor((int) anker.getX() - 1, (int) anker.getZ() + 24, Material.OAK_PLANKS);
                }
                break;
            case TWO_BY_ONE:
                if (room.getX() - 1 >= 0 && roomLayout.get(room.getZ()).get(room.getX() - 1) == 1) {
                    pasteDoor((int) anker.getX() - 1, (int) anker.getZ() + 7, Material.OAK_PLANKS);
                }
                if (room.getX() + 2 < roomLayout.get(0).size() && roomLayout.get(room.getZ()).get(room.getX() + 2) == 1) {
                    pasteDoor((int) anker.getX() + 33, (int) anker.getZ() + 7, Material.OAK_PLANKS);
                }
                if (room.getZ() - 1 >= 0 && roomLayout.get(room.getZ() - 1).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() - 1, Material.OAK_PLANKS);
                }
                if (room.getZ() + 1 < roomLayout.size() && roomLayout.get(room.getZ() + 1).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() + 16, Material.OAK_PLANKS);
                }
                if (room.getZ() - 1 >= 0 && roomLayout.get(room.getZ() - 1).get(room.getX() + 1) == 1) {
                    pasteDoor((int) anker.getX() + 24, (int) anker.getZ() - 1, Material.OAK_PLANKS);
                }
                if (room.getZ() + 1 < roomLayout.size() && roomLayout.get(room.getZ() + 1).get(room.getX() + 1) == 1) {
                    pasteDoor((int) anker.getX() + 24, (int) anker.getZ() + 16, Material.OAK_PLANKS);
                }
                break;
            case TWO_BY_TWO:
                if (room.getZ() - 1 >= 0 && roomLayout.get(room.getZ() - 1).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() - 1, Material.OAK_PLANKS);
                }
                if (room.getZ() - 1 >= 0 && roomLayout.get(room.getZ() - 1).get(room.getX() + 1) == 1) {
                    pasteDoor((int) anker.getX() + 24, (int) anker.getZ() - 1, Material.OAK_PLANKS);
                }
                if (room.getX() + 2 < roomLayout.get(0).size() && roomLayout.get(room.getZ()).get(room.getX() + 2) == 1) {
                    pasteDoor((int) anker.getX() + 33, (int) anker.getZ() + 7, Material.OAK_PLANKS);
                }
                if (room.getX() + 2 < roomLayout.get(0).size() && roomLayout.get(room.getZ() + 1).get(room.getX() + 2) == 1) {
                    pasteDoor((int) anker.getX() + 33, (int) anker.getZ() + 24, Material.OAK_PLANKS);
                }
                if (room.getZ() + 2 < roomLayout.size() && roomLayout.get(room.getZ() + 2).get(room.getX() + 1) == 1) {
                    pasteDoor((int) anker.getX() + 24, (int) anker.getZ() + 33, Material.OAK_PLANKS);
                }
                if (room.getZ() + 2 < roomLayout.size() && roomLayout.get(room.getZ() + 2).get(room.getX()) == 1) {
                    pasteDoor((int) anker.getX() + 7, (int) anker.getZ() + 33, Material.OAK_PLANKS);
                }
                if (room.getX() - 1 >= 0 && roomLayout.get(room.getZ() + 1).get(room.getX() - 1) == 1) {
                    pasteDoor((int) anker.getX() - 1, (int) anker.getZ() + 24, Material.OAK_PLANKS);
                }
                if (room.getX() - 1 >= 0 && roomLayout.get(room.getZ()).get(room.getX() - 1) == 1) {
                    pasteDoor((int) anker.getX() - 1, (int) anker.getZ() + 7, Material.OAK_PLANKS);
                }
                break;
        }
    }

    private Room placeRoom(int x, int y, RoomType shape) {
        switch (shape) {
            case STARTER:
                roomLayout.get(y).set(x, 1);
                return new Room(RoomType.STARTER, "1by1.schem", x, y);
            case ONE_BY_ONE:
                roomLayout.get(y).set(x, 1);
                return new Room(RoomType.ONE_BY_ONE, "1by1.schem", x, y);
            case ONE_BY_TWO:
                roomLayout.get(y).set(x, 1);
                roomLayout.get(y + 1).set(x, 1);
                return new Room(RoomType.ONE_BY_TWO, "1by2.schem", x, y);
            case TWO_BY_ONE:
                roomLayout.get(y).set(x, 1);
                roomLayout.get(y).set(x + 1, 1);
                return new Room(RoomType.TWO_BY_ONE, "2by1.schem", x, y);
            case TWO_BY_TWO:
                roomLayout.get(y).set(x, 1);
                roomLayout.get(y + 1).set(x, 1);
                roomLayout.get(y).set(x + 1, 1);
                roomLayout.get(y + 1).set(x + 1, 1);
                return new Room(RoomType.TWO_BY_TWO, "2by2.schem", x, y);
            default:
                return new Room(RoomType.ERROR, "", x, y);
        }

    }

    private boolean placeSpecialRooms() {
        ArrayList<ArrayList<Integer>> singleCells = findEmptySpotsWithOneAdjacentActiveCell();
        if (singleCells.size() > 0) {
            ArrayList<Integer> bossCords = furthestCoordsFromOrigin(singleCells);
            singleCells.remove(singleCells.indexOf(bossCords));

            roomLayout.get(bossCords.get(0)).set(bossCords.get(1), 1);
            Room bossRoom = new Room(RoomType.BOSS, "boss.schem", bossCords.get(1), bossCords.get(0));
            generatedRooms.add(bossRoom);
            singleCells = removeMoreThanOneAdjacent(singleCells);
        }

        if (singleCells.size() > 0) {
            int ind = (int) Math.round(Math.random() * singleCells.size());
            ArrayList<Integer> treasureCoords = singleCells.get(ind);
            singleCells.remove(singleCells.indexOf(treasureCoords));
            roomLayout.get(treasureCoords.get(0)).set(treasureCoords.get(1), 1);
            Room treasureRoom = new Room(RoomType.TREASURE, "treasure.schem", treasureCoords.get(1), treasureCoords.get(0));
            generatedRooms.add(treasureRoom);
            singleCells = removeMoreThanOneAdjacent(singleCells);
        }

        if (singleCells.size() > 0) {
            int ind = (int) Math.round(Math.random() * singleCells.size());
            ArrayList<Integer> shopCoords = singleCells.get(ind);
            singleCells.remove(singleCells.indexOf(shopCoords));
            roomLayout.get(shopCoords.get(0)).set(shopCoords.get(1), 1);
            Room shopRoom = new Room(RoomType.SHOP, "shop.schem", shopCoords.get(1), shopCoords.get(0));
            generatedRooms.add(shopRoom);
        }
        return false;
    }

    private ArrayList<ArrayList<Integer>> removeMoreThanOneAdjacent(ArrayList<ArrayList<Integer>> coords) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        for (ArrayList<Integer> coord : coords) {
            if (countAdjacentOnes(coord.get(0), coord.get(1)) <= 1) {
                result.add(coord);
            }
        }
        return result;
    }

    public int countAdjacentOnes(int x, int y) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        int count = 0;
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < roomLayout.size() && ny >= 0 && ny < roomLayout.get(0).size() && roomLayout.get(nx).get(ny) == 1) {
                count++;
            }
        }
        return count;
    }

    private static double distanceFromOrigin(ArrayList<Integer> coord) {
        return Math.sqrt(Math.pow(coord.get(0), 2) + Math.pow(coord.get(1), 2));
    }

    private static ArrayList<Integer> furthestCoordsFromOrigin(ArrayList<ArrayList<Integer>> coords) {
        double maxDistance = -999999999;
        ArrayList<Integer> furthestCoords = null;

        for (ArrayList<Integer> coord : coords) {
            double distance = distanceFromOrigin(coord);
            if (distance > maxDistance) {
                maxDistance = distance;
                furthestCoords = coord;
            }
        }

        return furthestCoords;
    }

    private ArrayList<ArrayList<Integer>> findEmptySpotsWithOneAdjacentActiveCell() {
        int rows = roomLayout.size();
        int cols = roomLayout.get(0).size();

        ArrayList<ArrayList<Integer>> result = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (roomLayout.get(i).get(j) == 0) {
                    int adjacentActiveCount = 0;
                    if (i > 0 && roomLayout.get(i - 1).get(j) == 1) {
                        adjacentActiveCount++; // Check above
                    }
                    if (i < rows - 1 && roomLayout.get(i + 1).get(j) == 1) {
                        adjacentActiveCount++; // Check below
                    }
                    if (j > 0 && roomLayout.get(i).get(j - 1) == 1) {
                        adjacentActiveCount++; // Check left
                    }
                    if (j < cols - 1 && roomLayout.get(i).get(j + 1) == 1) {
                        adjacentActiveCount++; // Check right
                    }

                    if (adjacentActiveCount == 1) {
                        result.add(new ArrayList<>(Arrays.asList(i, j)));
                    }
                }
            }
        }
        return result;
    }

    private boolean isOnGrid(int x, int y, RoomType shape) {
        switch (shape) {
            case STARTER:
            case ONE_BY_ONE:
                return x >= 0
                        && y >= 0
                        && x < GRID_SIZE
                        && y < GRID_SIZE;
            case ONE_BY_TWO:
                return x >= 0
                        && y >= 0
                        && y + 1 < GRID_SIZE;
            case TWO_BY_ONE:
                return x >= 0
                        && y >= 0
                        && x + 1 < GRID_SIZE;
            case TWO_BY_TWO:
                return x >= 0
                        && y >= 0
                        && x + 1 < GRID_SIZE
                        && y + 1 < GRID_SIZE;
            default:
                return false;
        }
    }

    private boolean isFree(int x, int y, RoomType shape) {
        switch (shape) {
            case STARTER:
            case ONE_BY_ONE:
                return roomLayout.get(y).get(x) == 0;
            case ONE_BY_TWO:
                return roomLayout.get(y).get(x) == 0
                        && roomLayout.get(y + 1).get(x) == 0;
            case TWO_BY_ONE:
                return roomLayout.get(y).get(x) == 0
                        && roomLayout.get(y).get(x + 1) == 0;
            case TWO_BY_TWO:
                return roomLayout.get(y).get(x) == 0
                        && roomLayout.get(y).get(x + 1) == 0
                        && roomLayout.get(y + 1).get(x) == 0
                        && roomLayout.get(y + 1).get(x + 1) == 0;
            default:
                return false;
        }
    }

    class Offset {
        public int x;
        public int y;

        public Offset(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private Offset getNextOffset(RoomType roomType) {

        switch (roomType) {
            case STARTER:
            case ONE_BY_ONE:
                return getOneByOne(random.nextInt(4));
            case ONE_BY_TWO:
                return getOneByTwo(random.nextInt(6));
            case TWO_BY_ONE:
                return getTwoByOne(random.nextInt(6));
            case TWO_BY_TWO:
                return getTwoByTwo(random.nextInt(8));
            default:
                return new Offset(0, 0);
        }
    }


    private Offset getOneByOne(int direction) {
        Offset offset;
        switch (direction) {
            case 0:
                offset = new Offset(1, 0);
                break;
            case 1:
                offset = new Offset(0, -1);
                break;
            case 2:
                offset = new Offset(-1, 0);
                break;
            case 3:
                offset = new Offset(0, 1);
                break;
            default:
                offset = new Offset(0, 0);
        }
        return offset;
    }

    private Offset getTwoByOne(int direction) {
        Offset offset;
        switch (direction) {
            case 0:
                offset = new Offset(-1, 0);
                break;
            case 1:
                offset = new Offset(0, -1);
                break;
            case 2:
                offset = new Offset(1, -1);
                break;
            case 3:
                offset = new Offset(2, 0);
                break;
            case 4:
                offset = new Offset(1, 1);
                break;
            case 5:
                offset = new Offset(0, 1);
                break;
            default:
                offset = new Offset(0, 0);
        }
        return offset;
    }

    private Offset getOneByTwo(int direction) {
        Offset offset;
        switch (direction) {
            case 0:
                offset = new Offset(-1, 0);
                break;
            case 1:
                offset = new Offset(-1, 1);
                break;
            case 2:
                offset = new Offset(0, 2);
                break;
            case 3:
                offset = new Offset(1, 1);
                break;
            case 4:
                offset = new Offset(1, 0);
                break;
            case 5:
                offset = new Offset(0, -1);
                break;
            default:
                offset = new Offset(0, 0);
        }
        return offset;
    }

    private Offset getTwoByTwo(int direction) {
        Offset offset;
        switch (direction) {
            case 0:
                offset = new Offset(-1, 0);
                break;
            case 1:
                offset = new Offset(-1, 1);
                break;
            case 2:
                offset = new Offset(0, 2);
                break;
            case 3:
                offset = new Offset(1, 2);
                break;
            case 4:
                offset = new Offset(2, 1);
                break;
            case 5:
                offset = new Offset(2, 0);
                break;
            case 6:
                offset = new Offset(1, -1);
                break;
            case 7:
                offset = new Offset(0, -1);
                break;
            default:
                offset = new Offset(0, 0);
        }
        return offset;
    }

    private Location pasteRoom(Location location, String fileName) {
        File myFile = new File(Bukkit.getPluginManager().getPlugin("WorldEdit").getDataFolder().getAbsolutePath() + "/schematics/" + fileName);
        ClipboardFormat format = ClipboardFormats.findByFile(myFile);

        try {
            ClipboardReader reader = format.getReader(new FileInputStream(myFile));
            Clipboard clipboard = reader.read();

            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(location.getWorld());

            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld,
                    -1);

// Saves our operation and builds the paste - ready to be completed.
            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ())).ignoreAirBlocks(true).build();

            try { // This simply completes our paste and then cleans up.
                Operations.complete(operation);
                editSession.flushSession();

            } catch (WorldEditException e) { // If worldedit generated an exception it will go here
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return location;
    }

    private void pasteDoor(int x, int z, Material material) {
        if (material == Material.OAK_PLANKS) {
            openDoorLocations.add(new Location(Bukkit.getWorld("roguelike"), x, -60, z));
        }
        if (material == Material.IRON_BLOCK) {
            openDoorLocations.remove(openDoorLocations.indexOf(new Location(Bukkit.getWorld("roguelike"), x, -60, z)));
        }
        final Block bottom = Bukkit.getWorld("roguelike").getBlockAt(x, -60, z);
        final Block top = bottom.getRelative(BlockFace.UP, 1);
        bottom.setType(material);
        top.setType(material);
    }

    public void pasteWall(int x1, int z1, int x2, int z2) {
        World world = BukkitAdapter.adapt(Bukkit.getWorld("roguelike"));

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            // Create a CuboidRegion based on the player's coordinates
            CuboidRegion region = new CuboidRegion(
                    BukkitAdapter.adapt(Bukkit.getWorld("roguelike")),
                    BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("roguelike"), x1, -61, z1)),
                    BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("roguelike"), x2, -57, z2))
            );

            Pattern pattern = new RandomPattern();
            BlockState state = BukkitAdapter.adapt(Material.STONE.createBlockData());

            // Set blocks within the region to create a wall
            editSession.setBlocks(region, state);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}

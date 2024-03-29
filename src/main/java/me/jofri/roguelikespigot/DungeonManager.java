package me.jofri.roguelikespigot;

import me.jofri.roguelikespigot.dungeon.Dungeon;

public class DungeonManager {
    private static Dungeon dungeonInstance;

    public static Dungeon getDungeon() {
        return dungeonInstance;
    }

    public static void resetDungeon() {
        dungeonInstance = null;
    }

    public static void setDungeon(Dungeon dungeon) {
        dungeonInstance = dungeon;
    }
}
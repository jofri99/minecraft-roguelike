package me.jofri.roguelikespigot.dungeon;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;

@Getter
@Setter
public class DungeonLayout {
    private ArrayList<Room> rooms;
    private ArrayList<Location> doorLocations;
}

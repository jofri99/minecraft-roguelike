package me.jofri.roguelikespigot.listener;

import lombok.Getter;
import me.jofri.roguelikespigot.DungeonManager;
import me.jofri.roguelikespigot.dungeon.Dungeon;
import me.jofri.roguelikespigot.dungeon.Room;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

public class PlayerListener implements Listener {
    private int callCount=0;
    private Room lastRoom = null;
    @Getter
    private static final PlayerListener instance = new PlayerListener();


    @EventHandler
    public void moveEvent(PlayerMoveEvent event) {
        if(callCount>10 && DungeonManager.getDungeon() != null){

            ArrayList<Room> rooms = DungeonManager.getDungeon().getGeneratedRooms();
            for(Room room:rooms){
                boolean isInRoom = Dungeon.isPlayerInRoom(room, event.getTo());
                if(isInRoom) {
                    if(lastRoom == null || room.getId() != lastRoom.getId()){
                        lastRoom = room;
                        room.isEntered();
                    }
                    break;
                }
            }


            callCount = 0;
        }
        callCount++;
    }
}

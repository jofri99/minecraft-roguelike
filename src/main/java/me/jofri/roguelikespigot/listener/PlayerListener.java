package me.jofri.roguelikespigot.listener;

import lombok.Getter;
import me.jofri.roguelikespigot.DungeonManager;
import me.jofri.roguelikespigot.dungeon.Dungeon;
import me.jofri.roguelikespigot.dungeon.Room;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

public class PlayerListener implements Listener {
    private int callCount = 0;
    private Room lastRoom = null;
    @Getter
    private static final PlayerListener instance = new PlayerListener();

    @EventHandler
    public void moveEvent(PlayerMoveEvent event) {
        if (callCount > 10 && DungeonManager.getDungeon() != null) {
            ArrayList<Room> rooms = DungeonManager.getDungeon().getGeneratedRooms();
            for (Room room : rooms) {
                boolean isInRoom = Dungeon.isPlayerInRoom(room, event.getTo());
                if (isInRoom) {
                    if (lastRoom == null || room.getId() != lastRoom.getId()) {
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

    @EventHandler
    public void pickupHeartEvent(EntityPickupItemEvent event) {
        if ((event.getEntity() instanceof Player)) {
            if (event.getItem().getItemStack().getType() == Material.REDSTONE) {
                if (canPlayerPickupHeart((Player) event.getEntity())) {
                    event.getItem().remove();
                    event.setCancelled(true);
                    double currentHealth = event.getEntity().getHealth();
                    event.getEntity().setHealth(currentHealth + 1);
                    ((Player) event.getEntity()).playSound(event.getEntity(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
                }
                event.setCancelled(true);
            }
        }
    }

    private boolean canPlayerPickupHeart(Player player) {
        return player.getHealth() + 1 <= player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
    }

    @EventHandler
    public void useKeyEvent(PlayerInteractEvent event) {
        if (isDoorClickedWithKey(event)) {
            removeDoorBlocks(event.getClickedBlock());
            event.getItem().setAmount(event.getItem().getAmount() - 1);
            event.setCancelled(true);
        }
    }

    private void removeDoorBlocks(Block doorBlock1) {
        doorBlock1.setType(Material.AIR);
        if (doorBlock1.getRelative(BlockFace.UP).getType() == Material.IRON_BLOCK) {
            doorBlock1.getRelative(BlockFace.UP).setType(Material.AIR);
        } else if (doorBlock1.getRelative(BlockFace.DOWN).getType() == Material.IRON_BLOCK) {
            doorBlock1.getRelative(BlockFace.DOWN).setType(Material.AIR);
        }
    }

    private boolean isDoorClickedWithKey(PlayerInteractEvent event) {
        return (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
                && event.getItem().getType() == Material.TRIPWIRE_HOOK
                && event.getClickedBlock().getType() == Material.IRON_BLOCK;
    }

    @EventHandler
    public void regenHealthEvent(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void foodLevelEvent(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }
}

package me.jofri.roguelikespigot.dungeon;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.EntityType;

@Getter
@Setter
public class Enemy {
    private EntityType entityType;

    public Enemy(EntityType entityType) {
        this.entityType = entityType;
    }
}

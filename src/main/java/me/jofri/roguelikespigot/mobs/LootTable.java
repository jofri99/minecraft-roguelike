package me.jofri.roguelikespigot.mobs;

import com.google.gson.Gson;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class LootTable {
    private ArrayList<ItemStack> drops = new ArrayList<>();

    class Entry {
        String item;
        int min;
        int max;
    }

    class Entries {
        ArrayList<Entry> pool;
    }

    public LootTable(LootTableType type) {
        Entries lootTable = loadJSON(type.toString().toLowerCase());
        generateDrops(lootTable);
    }

    private void generateDrops(Entries lootTable) {
        for (Entry entry : lootTable.pool) {
            ItemStack itemStack = new ItemStack(Material.valueOf(entry.item));
            itemStack.setAmount(1);
            drops.add(itemStack);
        }
    }

    private Entries loadJSON(String lootTable) {
        try (InputStream is = getClass().getResourceAsStream("/lootTables/" + lootTable + ".json");
             Reader rd = new InputStreamReader(is, "UTF-8")) {
            Gson gson = new Gson();
            return gson.fromJson(rd, Entries.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ItemStack> getDrops() {
        return drops;
    }
}

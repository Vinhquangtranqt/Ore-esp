package com.kodari.oreoutlines;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OreConfig {
    public static final int MIN_SCAN_RADIUS = 4;
    public static final int MAX_SCAN_RADIUS = 128;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ore_outlines.json");
    public static OreConfig INSTANCE = new OreConfig();
    public boolean enabled = true;
    public int radius = 16;
    public Map<String, Setting> types = defaults();

    public static final class Setting {
        public boolean enabled = true;
        public int color;
        public Setting(int color) { this.color = color; }
    }

    private static Map<String, Setting> defaults() {
        Map<String, Setting> map = new LinkedHashMap<>();
        map.put("Coal", new Setting(0x555555));
        map.put("Iron", new Setting(0xd8b09a));
        map.put("Copper", new Setting(0xe38755));
        map.put("Gold", new Setting(0xffd13b));
        map.put("Redstone", new Setting(0xff3030));
        map.put("Emerald", new Setting(0x38e66d));
        map.put("Lapis", new Setting(0x386bff));
        map.put("Diamond", new Setting(0x4bf5ed));
        map.put("Nether Quartz", new Setting(0xf4e9dd));
        map.put("Ancient Debris", new Setting(0x9e6557));
        map.put("Lava", new Setting(0xff8000));
        map.put("Gravel", new Setting(0x99938e));
        map.put("Sand", new Setting(0xe5d39a));
        return map;
    }

    public static String type(Block block) {
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return "Coal";
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return "Iron";
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return "Copper";
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) return "Gold";
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return "Redstone";
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return "Emerald";
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return "Lapis";
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return "Diamond";
        if (block == Blocks.NETHER_QUARTZ_ORE) return "Nether Quartz";
        if (block == Blocks.ANCIENT_DEBRIS) return "Ancient Debris";
        if (block == Blocks.LAVA) return "Lava";
        if (block == Blocks.GRAVEL) return "Gravel";
        if (block == Blocks.SAND) return "Sand";
        return null;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                OreConfig loaded = GSON.fromJson(reader, OreConfig.class);
                if (loaded != null) INSTANCE = loaded;
            } catch (IOException | RuntimeException e) {
                System.err.println("Ore Outlines: could not read config: " + e.getMessage());
            }
        }
        Map<String, Setting> defaults = defaults();
        if (INSTANCE.types == null) INSTANCE.types = defaults;
        else defaults.forEach((key, value) -> { if (INSTANCE.types.get(key) == null) INSTANCE.types.put(key, value); });
        INSTANCE.radius = Math.clamp(INSTANCE.radius, MIN_SCAN_RADIUS, MAX_SCAN_RADIUS);
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) { GSON.toJson(INSTANCE, writer); }
        } catch (IOException e) {
            System.err.println("Ore Outlines: could not save config: " + e.getMessage());
        }
    }
}
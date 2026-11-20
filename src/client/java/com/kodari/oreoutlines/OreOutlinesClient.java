package com.kodari.oreoutlines;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.HashMap;
import java.util.Map;

public final class OreOutlinesClient implements ClientModInitializer {
    private static final Map<BlockPos, String> visible = new HashMap<>();
    private static ClientLevel scanLevel;
    private static BlockPos center;
    private static int index;
    private static int scanRadius;
    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        OreConfig.load();
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.ore_outlines.toggle",
                InputConstants.KEY_X,
                KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(OreOutlinesClient::tick);
    }

    private static void tick(Minecraft client) {
        if (toggleKey.consumeClick()) {
            OreConfig.INSTANCE.enabled = !OreConfig.INSTANCE.enabled;
            OreConfig.save();
            if (!OreConfig.INSTANCE.enabled) {
                visible.clear();
                scanLevel = null;
            }
        }
        if (client.level == null || client.player == null || !OreConfig.INSTANCE.enabled) {
            visible.clear();
            scanLevel = null;
            return;
        }
        int radius = Math.clamp(OreConfig.INSTANCE.radius, OreConfig.MIN_SCAN_RADIUS, OreConfig.MAX_SCAN_RADIUS);
        BlockPos playerPos = client.player.blockPosition();
        if (scanLevel != client.level || center == null || scanRadius != radius) {
            visible.clear();
            scanLevel = client.level;
            center = playerPos;
            scanRadius = radius;
            index = 0;
        }
        visible.keySet().removeIf(pos -> Math.abs(pos.getX() - playerPos.getX()) > radius
                || Math.abs(pos.getY() - playerPos.getY()) > radius
                || Math.abs(pos.getZ() - playerPos.getZ()) > radius);
        if (Math.abs(center.getX() - playerPos.getX()) > radius
                || Math.abs(center.getY() - playerPos.getY()) > radius
                || Math.abs(center.getZ() - playerPos.getZ()) > radius) {
            center = playerPos;
            index = 0;
        }
        int side = radius * 2 + 1;
        int total = side * side * side;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int end = Math.min(index + 8192, total); index < end; index++) {
            int x = center.getX() + index % side - radius;
            int z = center.getZ() + index / side % side - radius;
            int y = center.getY() + index / (side * side) - radius;
            LevelChunk chunk = client.level.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false);
            pos.set(x, y, z);
            String type = chunk == null ? null : OreConfig.type(chunk.getBlockState(pos).getBlock());
            if (type == null || Math.abs(x - playerPos.getX()) > radius
                    || Math.abs(y - playerPos.getY()) > radius || Math.abs(z - playerPos.getZ()) > radius) {
                visible.remove(pos);
            } else {
                visible.put(new BlockPos(x, y, z), type);
            }
        }
        if (index == total) {
            index = 0;
            center = playerPos;
        }
    }

    public static void draw() {
        if (!OreConfig.INSTANCE.enabled) return;
        for (var found : visible.entrySet()) {
            OreConfig.Setting setting = OreConfig.INSTANCE.types.get(found.getValue());
            if (setting != null && setting.enabled) {
                Gizmos.cuboid(found.getKey(), GizmoStyle.stroke(0xff000000 | (setting.color & 0xffffff), 2.0f)).setAlwaysOnTop();
            }
        }
    }
}
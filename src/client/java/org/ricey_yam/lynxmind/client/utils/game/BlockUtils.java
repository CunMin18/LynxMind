package org.ricey_yam.lynxmind.client.utils.game;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class BlockUtils {
    /// 搜索最近的方块的位置
    public static BlockPos findNearestBlock(LivingEntity livingEntity, List<String> targetBlockNameList, int radius) {
        if(livingEntity == null || targetBlockNameList == null || targetBlockNameList.isEmpty()) return null;
        var startPos = livingEntity.getBlockPos();
        var world = livingEntity.getEntityWorld();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        int[][] directions = {{0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            var blockName = getBlockName(currentPos);
            for (String targetName : targetBlockNameList) {
                if (ItemUtils.ItemTagMatcher.isFuzzyMatch(targetName, blockName)){
                    return currentPos;
                }
            }
            if (currentPos.getManhattanDistance(startPos) > radius) {
                continue;
            }
            for (int[] dir : directions) {
                BlockPos neighborPos = currentPos.add(dir[0], dir[1], dir[2]);
                if (!visited.contains(neighborPos)) {
                    visited.add(neighborPos);
                    queue.add(neighborPos);
                }
            }
        }

        return null;
    }

    /// 获取方块状态
    public static BlockState getBlockState(BlockPos pos) {
        if (MinecraftClient.getInstance().world != null) {
            return MinecraftClient.getInstance().world.getBlockState(pos);
        }
        return null;
    }

    /// 获取方块ID
    public static String getBlockName(BlockPos pos) {
        var world = MinecraftClient.getInstance().world;
        if (world == null || pos == null) {
            return null;
        }
        Block block = world.getBlockState(pos).getBlock();
        Identifier blockId = Registries.BLOCK.getId(block);
        return blockId.toString();
    }
}

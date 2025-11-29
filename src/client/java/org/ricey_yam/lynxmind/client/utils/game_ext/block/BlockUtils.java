package org.ricey_yam.lynxmind.client.utils.game_ext.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.ricey_yam.lynxmind.client.utils.game_ext.ClientUtils;

import java.util.*;
import java.util.function.Predicate;

public class BlockUtils {
    /// 搜索最近的方块的位置
    public static BlockPos findNearestBlock(LivingEntity livingEntity, int radius, Predicate<BlockPos> predicate) {
        if(livingEntity == null) return null;
        var startPos = livingEntity.getBlockPos();
        var world = livingEntity.getEntityWorld();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        int[][] directions = {{0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            var blockName = getBlockID(currentPos);
            if (predicate.test(currentPos)) {
                return currentPos;
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

    /// 扫描附近的全部方块
    public static List<BlockLite> scanAllBlocks(LivingEntity livingEntity,List<String> targetBlockIDList, int radius){
        var entityPos = livingEntity.getBlockPos();
        var result = new ArrayList<BlockLite>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    var pos = entityPos.add(x, y, z);
                    var blockState = BlockUtils.getBlockState(pos);
                    if(targetBlockIDList.contains(BlockUtils.getBlockID(pos))){
                        result.add(new BlockLite(pos));
                    }
                }
            }
        }
        return result;
    }

    /// 寻找最近的工作台的放置点
    public static BlockPos findCraftingTablePlacePoint(LivingEntity livingEntity, int range) {
        var world = ClientUtils.getWorld();
        return findNearestBlock(livingEntity, range,pos -> {
            var state = Objects.requireNonNull(BlockUtils.getBlockState(pos));
            var upState = Objects.requireNonNull(BlockUtils.getBlockState(pos.up()));
            return Blocks.CRAFTING_TABLE.getDefaultState().canPlaceAt(world, pos) &&
                    world.isInBuildLimit(pos) &&
                    isInRange(livingEntity.getBlockPos(), pos, range) &&
                    !isInRange(livingEntity.getBlockPos(), pos, 1) &&
                    Math.abs(pos.getY() - livingEntity.getBlockPos().getY()) <= 1 &&
                    upState.isAir() &&
                    state.isSolidBlock(world, pos);
        });
    }

    /// 校验位置是否在 range 范围内（欧氏距离）
    private static boolean isInRange(BlockPos center, BlockPos target, int range) {
        return center.getSquaredDistance(target) <= range * range;
    }

    /// 获取方块状态
    public static BlockState getBlockState(BlockPos pos) {
        if (MinecraftClient.getInstance().world != null) {
            return MinecraftClient.getInstance().world.getBlockState(pos);
        }
        return null;
    }

    /// 获取方块
    public static Block getTargetBlock(BlockPos pos) {
        if (pos == null) return null;
        var state = BlockUtils.getBlockState(pos);
        return state != null ? state.getBlock() : null;
    }

    /// 获取方块ID
    public static String getBlockID(BlockPos pos) {
        var world = MinecraftClient.getInstance().world;
        if (world == null || pos == null) {
            return null;
        }
        Block block = world.getBlockState(pos).getBlock();
        Identifier blockId = Registries.BLOCK.getId(block);
        return blockId.toString();
    }
    public static String getBlockID(Block block) {
        if(block == null) return null;
        Identifier blockId = Registries.BLOCK.getId(block);
        return blockId.toString();
    }

    /// 是否为有效的方块ID
    public static boolean isValidBlockID(String blockID){
        if (blockID == null || blockID.isEmpty()) return false;
        return Registries.BLOCK.containsId(new Identifier(blockID));
    }

    /// 是否一定要镐子挖掘(石头类居多)
    public static boolean isPickaxeRequired(BlockState blockState) {
        return blockState.isIn(BlockTags.PICKAXE_MINEABLE);
    }
}

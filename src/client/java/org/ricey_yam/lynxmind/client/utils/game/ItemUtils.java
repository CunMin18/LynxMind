package org.ricey_yam.lynxmind.client.utils.game;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class ItemUtils {
    /// 获取物品ID
    public static String getItemName(ItemStack itemStack) {
        Item item = itemStack.getItem();
        Identifier itemIdentifier = Registries.ITEM.getId(item);
        return itemIdentifier.toString();
    }

    /**
     * 昵称-物品标签匹配工具
     */
    public static class ItemTagMatcher {

        private static final List<String> COMMON_ITEM_TAGS = Arrays.asList(
                // 基础方块
                "minecraft:dirt",
                "minecraft:grass_block",
                "minecraft:stone",
                "minecraft:cobblestone",
                "minecraft:oak_planks",
                "minecraft:spruce_planks",
                // 矿石
                "minecraft:diamond_ore",
                "minecraft:deepslate_diamond_ore",
                "minecraft:iron_ore",
                "minecraft:deepslate_iron_ore",
                // 其他常用物品
                "minecraft:coal",
                "minecraft:redstone",
                "minecraft:lapis_lazuli",
                "minecraft:emerald",
                "minecraft:obsidian"
        );

        /**
         * 精确匹配：关键词是否等于物品标签的“物品名”部分（忽略大小写）
         * 例：dirt → minecraft:dirt（匹配）；dirt → minecraft:grass_block（不匹配）
         * @param keyword 输入关键词（如 dirt、Dirt）
         * @param fullTag 完整物品标签（如 minecraft:dirt）
         * @return true：匹配；false：不匹配
         */
        public static boolean isExactMatch(String keyword, String fullTag) {
            if (keyword == null || fullTag == null || keyword.trim().isEmpty()) {
                return false;
            }
            // 提取完整标签的“物品名”部分（如 minecraft:dirt → dirt）
            String itemName = fullTag.contains(":") ? fullTag.split(":")[1] : fullTag;
            // 忽略大小写匹配
            return itemName.equalsIgnoreCase(keyword.trim());
        }

        /**
         * 模糊匹配：关键词是否包含在物品标签中（忽略大小写）
         * 例：dir → minecraft:dirt（匹配）；dia → minecraft:diamond_ore（匹配）
         * @param keyword 输入关键词（如 dir、Dia）
         * @param fullTag 完整物品标签（如 minecraft:dirt）
         * @return true：匹配；false：不匹配
         */
        public static boolean isFuzzyMatch(String keyword, String fullTag) {
            if (keyword == null || fullTag == null || keyword.trim().isEmpty()) {
                return false;
            }
            if(keyword.equals(fullTag)) return true;
            String lowerKeyword = keyword.trim().toLowerCase(Locale.ROOT);
            String lowerFullTag = fullTag.toLowerCase(Locale.ROOT);
            return lowerFullTag.contains(lowerKeyword);
        }

        /**
         * 根据关键词获取所有匹配的完整标签（默认精确匹配）
         * @param keyword 输入关键词（如 dirt）
         * @return 匹配的完整标签列表（如 [minecraft:dirt]）
         */
        public static List<String> getMatchedTags(String keyword) {
            return getMatchedTags(keyword, true);
        }

        /**
         * 根据关键词获取所有匹配的完整标签（可选择精确/模糊匹配）
         * @param keyword 输入关键词
         * @param isExact 是否精确匹配
         * @return 匹配的完整标签列表
         */
        public static List<String> getMatchedTags(String keyword, boolean isExact) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return List.of();
            }
            return COMMON_ITEM_TAGS.stream()
                    .filter(tag -> isExact ? isExactMatch(keyword, tag) : isFuzzyMatch(keyword, tag))
                    .collect(Collectors.toList());
        }

        /**
         * 动态添加新的物品标签到库中
         * @param newTag 新的完整物品标签
         */
        public static void addItemTag(String newTag) {
            if (newTag != null && !newTag.trim().isEmpty() && !COMMON_ITEM_TAGS.contains(newTag)) {
                COMMON_ITEM_TAGS.add(newTag);
            }
        }
    }
    /**
     * 物品制作分析工具
     */
    public static class CraftingHelper {

        /**
         * 判断物品是否需要工作台制作
         * @param itemId 物品ID
         * @return 是否需要工作台
         */
        public static boolean requiresWorkbench(String itemId) {
            var world = MinecraftClient.getInstance().world;
            if (world == null) return false;
            var targetItem = Registries.ITEM.get(new Identifier(itemId));

            for (var craftingRecipeRecipeEntry : world.getRecipeManager().listAllOfType(RecipeType.CRAFTING)) {
                var recipe = craftingRecipeRecipeEntry.value();
                var output = recipe.getResult(null);
                if (output.isOf(targetItem)) {
                    var isComplexRecipe = recipe.getGroup() != null && !recipe.getGroup().isEmpty();
                    int ingredientCount = 0;
                    for (var ingredient : recipe.getIngredients()) {
                        if (!ingredient.isEmpty()) ingredientCount++;
                    }
                    return ingredientCount > 4 || isComplexRecipe;
                }
            }
            return false;
        }

        public static boolean requiresWorkbench(Item item) {
            return requiresWorkbench(Registries.ITEM.getId(item).toString());
        }

        public static boolean requiresWorkbench(ItemStack stack) {
            return requiresWorkbench(stack.getItem());
        }
    }
}

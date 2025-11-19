package org.ricey_yam.lynxmind.client.utils.game_ext.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class RecipeHelper {
    private final static HashMap<String, List<RecipeEntry<?>>> mappedRecipes = new HashMap<>();
    /// 加载配方列表
    private static void mapRecipes() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() != null) {
            RecipeManager mgr = mc.getServer().getRecipeManager();
            for (var recipe : mgr.values()) {
                if (mc.world != null) {
                    Item output = recipe.value().getResult(mc.world.getRegistryManager()).getItem();
                    String key = Registries.ITEM.getId(output).toString();
                    mappedRecipes.computeIfAbsent(key, k -> new ArrayList<>()).add(recipe);
                }
            }
        }
    }
    /**
     * 判断物品是否需要工作台制作
     * @param itemId 物品ID
     * @return 是否需要工作台
     */
    public static boolean requiresCraftingTable(String itemId) {
        var world = MinecraftClient.getInstance().world;
        if (world == null) return false;
        var targetItem = Registries.ITEM.get(new Identifier(itemId));
        var recipe = getRecipe(itemId);
        if (recipe != null) {
            if(recipe instanceof ShapedRecipe shapedRecipe){
                return shapedRecipe.getHeight() >= 3 || shapedRecipe.getWidth() >= 3;
            }
            else if(recipe instanceof ShapelessRecipe shapelessRecipe) return false;
        }
        return false;
    }
    public static boolean requiresCraftingTable(Item item) {
        return requiresCraftingTable(Registries.ITEM.getId(item).toString());
    }
    public static boolean requiresCraftingTable(ItemStack stack) {
        return requiresCraftingTable(stack.getItem());
    }

    /// 获取指定物品制作配方
    public static Recipe<?> getRecipe(String itemId) {
        if(mappedRecipes.isEmpty()){
            mapRecipes();
        }
        if(mappedRecipes.containsKey(itemId)) {
            for (var checkRecipe : mappedRecipes.get(itemId)) {
                return checkRecipe.value();
            }
        }
        return null;
    }
}

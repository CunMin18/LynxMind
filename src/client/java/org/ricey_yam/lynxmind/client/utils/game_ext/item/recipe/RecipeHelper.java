package org.ricey_yam.lynxmind.client.utils.game_ext.item.recipe;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;

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
     */
    public static boolean requiresCraftingTable(Recipe<?> recipe) {
        if (recipe != null) {
            if(recipe instanceof ShapedRecipe shapedRecipe){
                return shapedRecipe.getHeight() >= 3 || shapedRecipe.getWidth() >= 3;
            }
            else if(recipe instanceof ShapelessRecipe shapelessRecipe) return false;
        }
        return false;
    }

    /// 获取指定物品制作配方
    public static List<RecipeEntry<?>> getRecipeEntries(String itemId) {
        if(mappedRecipes.isEmpty()){
            mapRecipes();
        }
        if(mappedRecipes.containsKey(itemId)) {
            return mappedRecipes.get(itemId);
        }
        return null;
    }

    /// 获取配方原料表
    public static DefaultedList<Ingredient> getActualIngredients(Recipe<?> recipe,boolean craftingTableNeeding) {
        var scale = craftingTableNeeding ? 3 : 2;
        DefaultedList<Ingredient> grid = DefaultedList.ofSize(scale * scale, Ingredient.EMPTY);

        if (recipe instanceof ShapedRecipe shaped) {
            int width = shaped.getWidth();
            int height = shaped.getHeight();
            DefaultedList<Ingredient> rawIngredients = recipe.getIngredients();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    var limit = scale - 1;
                    if (x > limit || y > limit) continue;
                    int listIndex = x + y * width;
                    int gridIndex = x + y * scale;
                    if (listIndex < rawIngredients.size()) {
                        grid.set(gridIndex, rawIngredients.get(listIndex));
                    }
                }
            }
            return grid;
        }
        else if (recipe instanceof ShapelessRecipe) {
            DefaultedList<Ingredient> raw = recipe.getIngredients();
            for (int i = 0; i < Math.min(raw.size(), scale * scale); i++) {
                grid.set(i, raw.get(i));
            }
            return grid;
        }

        return grid;
    }
}

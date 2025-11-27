package org.ricey_yam.lynxmind.client.utils.game_ext.item.recipe;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.slot.SlotActionType;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.PlayerUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ComplexContainerType;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemStackLite;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.SlotItemStack;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.CraftingTableItemSlot;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.InventoryCraftingSlot;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.LSlotType;
import static org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LFunctionHubTask.ClickSlotHostSubTask.*;
import static org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LFunctionHubTask.ClickSlotHostSubTask.ClickSlotTinyTask.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LRecipe{
    /// 需要从背包取出的物品
    private List<SlotItemStack> takeoutSlotInfo = new ArrayList<>();
    /// 需要放到工作台上的物品
    private List<SlotItemStack> placementSlotInfo = new ArrayList<>();
    private boolean craftingTableNeeding;
    private Recipe<?> linkedRecipe = null;
    private List<ClickSlotTinyTask> clickSlotTasks = new ArrayList<>();
    private ItemStackLite toCraftItemStack;
    private List<SlotItemStack> playerItems_C;
    private List<SlotItemStack> playerItems_P;

    /// LRecipe可根据想要制作的物品 自动生成合适的配方以及点击任务
    public LRecipe(ItemStackLite toCraftItemStack){
        /// 获取制作该物品的全部配方
        var recipeEntries = RecipeHelper.getRecipeEntries(toCraftItemStack.getItem_name());

        /// 读取配方列表 然后筛选出最佳配方
        if (recipeEntries != null && !recipeEntries.isEmpty()) {
            Recipe<?> confirmedRecipe;
            for(var recipeEntry : recipeEntries){
                if(recipeEntry == null) continue;

                confirmedRecipe = recipeEntry.value();
                if(confirmedRecipe == null) continue;

                /// 判断是否要工作台
                var requiresCraftingTable = RecipeHelper.requiresCraftingTable(confirmedRecipe);

                /// 获取背包内全部物品
                var playerItems = requiresCraftingTable ? getPlayerItems_C() : getPlayerItems_P();

                if(playerItems == null || playerItems.isEmpty()) return;

                /// 获取材料表
                var ingredients = RecipeHelper.getActualIngredients(confirmedRecipe,requiresCraftingTable);

                /// 获取计划需取出的材料/取出位置/放置位置
                outer:
                for (int i = 0; i < ingredients.size(); i++) {
                    var ingredient = ingredients.get(i);
                    if (ingredient.isEmpty()) {
                        continue;
                    }
                    /// 从背包找出匹配配方的物品 并获取其格子信息
                    System.out.println("ingredient size: " + ingredients.size());
                    for (var matchTarget : ingredient.getMatchingStacks()) {
                        var matchedItemStackInInventory = getMatchedSlotItemStack(matchTarget, playerItems);
                        var hasMatchedTargetItemInInventory = matchedItemStackInInventory != null;
                        if (hasMatchedTargetItemInInventory) {
                            var itemStack = matchedItemStackInInventory.getItem_stack().copy();
                            itemStack.setCount(1);
                            var placingSlotItemStack = new SlotItemStack(requiresCraftingTable ? new CraftingTableItemSlot(i + 1, ComplexContainerType.CRAFTING_TABLE) : new InventoryCraftingSlot(i + 1,ComplexContainerType.PLAYER_INFO),itemStack);
                            takeoutSlotInfo.add(matchedItemStackInInventory);
                            placementSlotInfo.add(placingSlotItemStack);
                            matchedItemStackInInventory.getItem_stack().setCount(matchedItemStackInInventory.getItem_stack().getCount() - 1);
                            if (matchedItemStackInInventory.getItem_stack().getCount() <= 0) {
                                playerItems.remove(matchedItemStackInInventory);
                            }
                            continue outer;
                        }
                    }
                    confirmedRecipe = null;
                    System.out.println("材料不足!!尝试寻找新的配方!!");
                    break;
                }
                if(confirmedRecipe != null) {
                    /// 生成 -> 放置材料点击任务（UTask）
                    if(takeoutSlotInfo.size() != placementSlotInfo.size() || takeoutSlotInfo.isEmpty()) {
                        clickSlotTasks = null;
                        System.out.println("生成点击方案失败！");
                        break;
                    }
                    for (int i = 0; i < takeoutSlotInfo.size(); i++) {
                        clickSlotTasks.addAll(getCraftingPutItemInClickSlotTasks(i));
                    }

                    /// 生成 -> 拿出产物的点击任务 (UTask)
                    var resultLSlot = requiresCraftingTable ? new CraftingTableItemSlot(0,ComplexContainerType.CRAFTING_TABLE) : new InventoryCraftingSlot(0,ComplexContainerType.PLAYER_INFO);
                    var takeOutResultItemUTask = new ClickSlotTinyTask(resultLSlot,0,SlotActionType.QUICK_MOVE,Aim.NONE);
                    clickSlotTasks.add(takeOutResultItemUTask);
                    craftingTableNeeding = requiresCraftingTable;
                    System.out.println("成功生成点击方案");
                    linkedRecipe = confirmedRecipe;
                    break;
                }
            }
            if(linkedRecipe == null) System.out.println("找不到配方1");
        }
        if(linkedRecipe == null) System.out.println("找不到配方2");
    }

    /// 根据给出的物品，从玩家已获得的物品中输出匹配的物品
    private SlotItemStack getMatchedSlotItemStack(ItemStack targetItemStack, List<SlotItemStack> obtainedItem) {
        for(var item : obtainedItem) {
            if(item == null) continue;
            if(item.getItem_stack().getItem_name().equals(ItemUtils.getItemID(targetItemStack))) {
                return item;
            }
        }
        return null;
    }

    /// 生成交互任务
    private List<ClickSlotTinyTask> getCraftingPutItemInClickSlotTasks(int slotIndex) {
        var TSlotItemStack = takeoutSlotInfo.get(slotIndex);
        var PSlotItemStack = placementSlotInfo.get(slotIndex);

        var takeOutUTask = new ClickSlotTinyTask(TSlotItemStack.getL_slot(),0, SlotActionType.PICKUP, Aim.PICKUP);
        var placeUTask = new ClickSlotTinyTask(PSlotItemStack.getL_slot(),1, SlotActionType.PICKUP, Aim.PUT);
        var takeBackUTask = new ClickSlotTinyTask(TSlotItemStack.getL_slot(),0, SlotActionType.PICKUP, Aim.NONE);

        return List.of(takeOutUTask,placeUTask,takeBackUTask);
    }

    /// 获取玩家背包物品
    private List<SlotItemStack> getPlayerItems(ComplexContainerType complexContainerType) {
        var playerItemsInner = PlayerUtils.getClientPlayerInventoryItems(LSlotType.INVENTORY_INNER,complexContainerType);
        var playerItemsHotBar = PlayerUtils.getClientPlayerInventoryItems(LSlotType.INVENTORY_HOTBAR,complexContainerType);
        playerItemsInner.addAll(playerItemsHotBar);
        return playerItemsInner;
    }
    private List<SlotItemStack> getPlayerItems_P() {
        if(playerItems_P == null || playerItems_P.isEmpty()){
            playerItems_P = getPlayerItems(ComplexContainerType.PLAYER_INFO);
        }
        return playerItems_P;
    }
    private List<SlotItemStack> getPlayerItems_C() {
        if(playerItems_C == null || playerItems_C.isEmpty()){
            playerItems_C = getPlayerItems(ComplexContainerType.CRAFTING_TABLE);
        }
        return playerItems_C;
    }
}

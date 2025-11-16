package org.ricey_yam.lynxmind.client.task.baritone;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import org.ricey_yam.lynxmind.client.ai.AIServiceManager;
import org.ricey_yam.lynxmind.client.ai.ChatManager;
import org.ricey_yam.lynxmind.client.ai.LynxJsonHandler;
import org.ricey_yam.lynxmind.client.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.ai.message.event.player.sub.PlayerBaritoneTaskStop;
import org.ricey_yam.lynxmind.client.ai.message.game_info.item.ItemStackLite;
import org.ricey_yam.lynxmind.client.ai.message.game_info.ui.SlotItemStack;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.ui.*;
import org.ricey_yam.lynxmind.client.utils.game_ext.*;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ContainerHelper;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.RecipeHelper;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.CraftingTableItemSlot;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.InventoryCraftingSlot;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.LSlotType;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.SlotHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
public class BCraftingTask extends BTask {
    public enum CraftingState {
        FINDING_CRAFTING_WAY,
        CRAFTING_ITEM,
        PUTTING_CRAFTING_TABLE,
        PATHING_TO_CRAFTING_TABLE
    }
    private IBaritone baritone;
    /// 工作台位置
    private BlockPos craftingTablePos;
    /// 需要制作的物品列表
    private List<ItemStackLite> to_craft;
    /// 制作任务状态
    private CraftingState craftingState;
    /// 当前的制作计划
    private CraftingSingleSubTask currentCraftingSingleSubTask;

    public BCraftingTask(List<ItemStackLite> to_craft, Action linkedAction) {
        this.craftingState = CraftingState.FINDING_CRAFTING_WAY;
        this.taskType = BTaskType.CRAFTING;
        this.baritone = BaritoneManager.getClientBaritone();
        this.to_craft = to_craft;
        this.linkedAction = linkedAction;
    }

    @Override
    public void start() {
        if(to_craft.isEmpty()) {
            stop("制作列表为空，停止BTask");
        }
    }

    @Override
    public void tick(){
        tickTimer++;
        ClientUtils.getOptions().rightKey.setPressed(false);
        if(tickTimer < 5){
            return;
        }
        else{
            var player = MinecraftClient.getInstance().player;
            if(player == null) return;
            if(to_craft.isEmpty()) {
                stop("完成制作！");
                return;
            }
            switch(this.craftingState) {
                /// 寻找制作方法
                case FINDING_CRAFTING_WAY ->{
                    /// 获取即将制作的物品 并制定计划 然后判定是否需要工作台
                    currentCraftingSingleSubTask = new CraftingSingleSubTask(to_craft.get(0).getItem_name());
                    /// 需要工作台，尝试寻找/制作工作台然后制作物品
                    if(currentCraftingSingleSubTask.craftingTableNeeding()) {
                        /// 尝试寻找附近的工作台
                        craftingTablePos = BlockUtils.findNearestBlock(player, List.of("minecraft:crafting_table"),20);
                        /// 没有工作台在20格内，制作一个
                        if(craftingTablePos == null){
                            System.out.println("未发现工作台，正在制作新工作台...");
                            to_craft.add(0,new ItemStackLite(1,"minecraft:crafting_table"));
                        }
                        /// 找到工作台 寻路到那
                        else{
                            craftingState = CraftingState.PATHING_TO_CRAFTING_TABLE;
                            var newGoal = new GoalBlock(craftingTablePos.getX(),craftingTablePos.getY(),craftingTablePos.getZ());
                            baritone.getCustomGoalProcess().setGoal(newGoal);
                            baritone.getCustomGoalProcess().path();
                        }
                    }
                    /// 无需工作台 打开背包制作物品
                    else{
                        this.craftingState = CraftingState.CRAFTING_ITEM;
                        ContainerHelper.openContainer(InventoryScreen.class);
                    }
                }
                /// 寻找合适的工作台放置点
                case PUTTING_CRAFTING_TABLE -> {

                    craftingTablePos = BlockUtils.findNearestBlock(player, List.of("minecraft:crafting_table"),20);
                    if(craftingTablePos != null) {
                        this.craftingState = CraftingState.FINDING_CRAFTING_WAY;
                        return;
                    }

                    var placePoint = BlockUtils.findCraftingTablePlacePoint(player);
                    /// 打开背包拿取工作台
                    if(placePoint != null) {
                        if(inSuitableCraftingUI()) {
                            var craftingTableLSlot = SlotHelper.getLSlotByItemID("minecraft:crafting_table",false);
                            if(craftingTableLSlot != null) {
                                SlotHelper.clickContainerSlot(craftingTableLSlot,8,SlotActionType.SWAP);
                                var isCraftingTableInHotbar = Objects.requireNonNull(SlotHelper.getLSlotByItemID("minecraft:crafting_table", false)).getSlotType() == LSlotType.INVENTORY_HOTBAR;
                                if(isCraftingTableInHotbar){
                                    if(SlotHelper.switchToHotbarItem("minecraft:crafting_table")){
                                        var targetRotation = TransformUtils.calcRotationFromVec3d(player.getBlockPos(),placePoint);
                                        baritone.getLookBehavior().updateTarget(targetRotation,true);
                                        if(TransformUtils.isLookingAt(placePoint)){
                                            ClientUtils.getOptions().rightKey.setPressed(true);
                                        }
                                    }
                                }
                            }
                            else this.craftingState = CraftingState.FINDING_CRAFTING_WAY;
                        }
                        else {
                            ContainerHelper.openContainer(InventoryScreen.class);
                        }
                    }
                    else stop("该环境无法使用工作台。");
                }
                /// 正在走向工作台
                case PATHING_TO_CRAFTING_TABLE ->{

                    /// 没摸到工作台，继续等待
                    if(!isCraftingTableInRange()){
                        var goal = baritone.getPathingBehavior().getGoal();
                        /// 若寻路任务意外丢失 重新创建
                        if(goal == null){
                            var newGoal = new GoalBlock(craftingTablePos.getX(),craftingTablePos.getY(),craftingTablePos.getZ());
                            baritone.getCustomGoalProcess().setGoal(newGoal);
                            baritone.getCustomGoalProcess().path();
                        }
                    }
                    /// 靠近工作台 尝试看向工作台
                    else if(!TransformUtils.isLookingAt(craftingTablePos)){
                        baritone.getCustomGoalProcess().setGoal(null);
                        baritone.getPathingBehavior().cancelEverything();
                        var targetRotation = TransformUtils.calcRotationFromVec3d(player.getBlockPos(),craftingTablePos);
                        baritone.getLookBehavior().updateTarget(targetRotation,true);
                    }
                    /// 看向工作台 点击工作台开始制作
                    else{
                        this.craftingState = CraftingState.CRAFTING_ITEM;
                        if (!ContainerHelper.isContainerOpen()) {
                            ContainerHelper.closeContainer();
                        }
                        ClientUtils.getOptions().rightKey.setPressed(true);
                    }
                }
                /// 制作阶段
                case CRAFTING_ITEM ->{
                    /// 判断是否在合成页面
                    if(inSuitableCraftingUI()){
                        /// 判断当前合成子任务状态
                        if(currentCraftingSingleSubTask.getState() != CraftingSingleSubTask.State.IDLE){
                            if(currentCraftingSingleSubTask.getState() == CraftingSingleSubTask.State.FAILED){
                                System.out.println("失败");
                            }
                            if(currentCraftingSingleSubTask.getCrafting_item().equals("minecraft:crafting_table")){
                                this.craftingState = CraftingState.PUTTING_CRAFTING_TABLE;
                            }
                            else this.craftingState = CraftingState.FINDING_CRAFTING_WAY;
                            var currentCrafting = to_craft.get(0);
                            if(currentCrafting.getCount() > 1){
                                currentCrafting.setCount(currentCrafting.getCount() - 1);
                            }
                            else to_craft.remove(0);
                            currentCraftingSingleSubTask = null;

                            ContainerHelper.closeContainer();
                        }
                        else {
                            currentCraftingSingleSubTask.craftTick();
                        }
                    }
                    else{
                        System.out.println("不在制作界面!");
                        ContainerHelper.openContainer(InventoryScreen.class);
                    }
                }
            }
        }

    }

    @Override
    public void stop(String stopReason) {
        currentTaskState = TaskState.FINISHED;
        currentCraftingSingleSubTask = null;
        craftingTablePos = null;
        to_craft.clear();

        /// 发送任务停止事件给AI
        if(stopReason != null && !stopReason.isEmpty() && AIServiceManager.isServiceActive && AIServiceManager.isTaskActive() && linkedAction != null){
            var bTaskStopEvent = new PlayerBaritoneTaskStop(linkedAction,stopReason);
            var serialized = LynxJsonHandler.serialize(bTaskStopEvent);
            Objects.requireNonNull(AIServiceManager.sendAndReceiveReplyAsync(serialized)).whenComplete((reply, error) -> ChatManager.handleAIReply(reply));
        }

        System.out.println("制作任务已停止：" + stopReason);
    }

    /// 是否能够到工作台
    private boolean isCraftingTableInRange(){
        var player = MinecraftClient.getInstance().player;
        if(craftingTablePos == null) return false;
        return baritone.getPlayerContext().player().getPos().distanceTo(craftingTablePos.toCenterPos()) < 4D;
    }

    /// 是否处于合适的制作界面
    public boolean inSuitableCraftingUI(){
        if(currentCraftingSingleSubTask == null) {
            System.out.println("sub task is null!");
            return false;
        }
        if(currentCraftingSingleSubTask.craftingTableNeeding()) return ContainerHelper.isContainerOpen(CraftingScreen.class);
        else return ContainerHelper.isContainerOpen(InventoryScreen.class);
    }

    @Setter
    @Getter
    static class CraftingSingleSubTask {
        public enum State{
            IDLE,
            FAILED,
            FINISHED
        }
        /// 子任务状态
        private State state;
        /// 正在制作的物品ID
        private String crafting_item;
        /// 需要从背包取出的物品
        private List<SlotItemStack> takeout = new ArrayList<>();
        /// 需要放到工作台上的物品
        private List<SlotItemStack> placement = new ArrayList<>();
        private List<UTask> uTasks = new ArrayList<>();
        private UTask performingUTask;

        public CraftingSingleSubTask(String crafting_item) {
            this.state = State.IDLE;
            this.crafting_item = crafting_item;
            var recipe = RecipeHelper.getRecipe(crafting_item);
            var playerItemsInner = ItemUtils.getClientPlayerInventoryItems(LSlotType.INVENTORY_INNER,craftingTableNeeding());
            var playerItemsHotBar = ItemUtils.getClientPlayerInventoryItems(LSlotType.INVENTORY_HOTBAR,craftingTableNeeding());
            playerItemsInner.addAll(playerItemsHotBar);
            var playerItems = playerItemsHotBar;
            if (recipe != null) {
                var ingredients = recipe.getIngredients();
                var craftingTableNeeding = craftingTableNeeding();
                var targetSlotIndex = 0;

                /// 获取计划需取出的材料/取出位置/放置位置
                outer:
                for (int i = 0; i < ingredients.size(); i++) {
                    var ingredient = ingredients.get(i);
                    if (ingredient.isEmpty()) {
                        continue;
                    }
                    for (var matchTarget : ingredient.getMatchingStacks()) {
                        var matchedItemStackInInventory = getMatchedSlotItemStack(matchTarget, playerItems);
                        var hasMatchedTargetItemInInventory = matchedItemStackInInventory != null;
                        if (hasMatchedTargetItemInInventory) {
                            var itemStack = matchedItemStackInInventory.getItem_stack().copy();
                            itemStack.setCount(1);
                            var placingSlotItemStack = new SlotItemStack(craftingTableNeeding() ? new CraftingTableItemSlot(i,true) : new InventoryCraftingSlot(targetSlotIndex,false),itemStack);
                            takeout.add(matchedItemStackInInventory);
                            placement.add(placingSlotItemStack);
                            matchedItemStackInInventory.getItem_stack().setCount(matchedItemStackInInventory.getItem_stack().getCount() - 1);
                            if(matchedItemStackInInventory.getItem_stack().getCount() <= 0){
                                playerItems.remove(matchedItemStackInInventory);
                            }
                            targetSlotIndex++;
                            continue outer;
                        }
                    }
                    this.state = State.FAILED;
                    System.out.println("材料不足！");
                    return;
                }

                /// 生成 -> 放置材料点击任务（UTask）
                if(takeout.size() != placement.size() || takeout.isEmpty()) {
                    uTasks = null;
                    return;
                }
                for (int i = 0; i < takeout.size(); i++) {
                    var TSlotItemStack = takeout.get(i);
                    var PSlotItemStack = placement.get(i);
                    var takeOutUTask = new UClickSlotTask(TSlotItemStack.getL_slot(),0, SlotActionType.PICKUP);
                    var placeUTask = new UClickSlotTask(PSlotItemStack.getL_slot(),1, SlotActionType.PICKUP);
                    var takeBackUTask = new UClickSlotTask(TSlotItemStack.getL_slot(),0, SlotActionType.PICKUP);
                    uTasks.add(takeOutUTask);
                    uTasks.add(placeUTask);
                    uTasks.add(takeBackUTask);
                }
                /// 生成 -> 拿出产物的点击任务 (UTask)
                var resultLSlot = craftingTableNeeding ? new CraftingTableItemSlot(9,true) : new InventoryCraftingSlot(4,false);
                var takeOutResultItemUTask = new UClickSlotTask(resultLSlot,0,SlotActionType.QUICK_MOVE);
                uTasks.add(takeOutResultItemUTask);
            }
        }

        /// 交互逻辑
        public void craftTick(){
            if(state != State.IDLE) return;

            if(uTasks != null && !uTasks.isEmpty()){
                uTasks.removeIf(uTask -> uTask.getCurrentTaskState() != TaskState.IDLE);
                /// 若当前无操作，进行下一步操作
                if(performingUTask == null){
                    performingUTask = uTasks.get(0);
                    LynxMindEndTickEventManager.registerTask(performingUTask);
                }
                /// 若当前有操作，判断操作结果，来决定是否下一步
                else if(performingUTask.getResult() != UTask.UTaskResult.NONE){
                    switch (performingUTask.getResult()) {
                        /// 失败：停止子任务
                        case FAILED -> this.state = State.FAILED;
                        /// 成功：继续下一个操作
                        case SUCCESS -> performingUTask = null;
                    }
                }
            }
            /// 若没有操作 说明该物品制作完成
            else {
                this.state = State.FINISHED;
            }
        }

        /// 是否需要工作台
        public boolean craftingTableNeeding(){
            return RecipeHelper.craftingTableNeeding(crafting_item);
        }

        /// 根据给出的物品，从玩家已获得的物品中输出匹配的物品
        private SlotItemStack getMatchedSlotItemStack(ItemStack targetItemStack, List<SlotItemStack> obtainedItem) {
            for(var item : obtainedItem) {
                if(item == null) continue;
                if(item.getItem_stack().getItem_name().equals(ItemUtils.getItemName(targetItemStack))) {
                    return item;
                }
            }
            return null;
        }
    }
}

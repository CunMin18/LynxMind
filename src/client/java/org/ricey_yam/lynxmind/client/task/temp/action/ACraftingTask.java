package org.ricey_yam.lynxmind.client.task.temp.action;

import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.ItemEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.service.AIServiceManager;
import org.ricey_yam.lynxmind.client.module.ai.message.AIChatManager;
import org.ricey_yam.lynxmind.client.module.ai.message.AIJsonHandler;
import org.ricey_yam.lynxmind.client.module.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.module.ai.message.action.sub.PlayerCraftingAction;
import org.ricey_yam.lynxmind.client.module.ai.message.event.player.sub.PlayerATaskStop;
import org.ricey_yam.lynxmind.client.utils.game_ext.block.BlockUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemStackLite;
import org.ricey_yam.lynxmind.client.utils.game_ext.*;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.PlayerUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ComplexContainerType;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ContainerHelper;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.recipe.LRecipe;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.*;
import static org.ricey_yam.lynxmind.client.task.non_temp.life.sub.LFunctionHubTask.*;
import static org.ricey_yam.lynxmind.client.task.non_temp.life.sub.LFunctionHubTask.ClickSlotHostSubTask.*;
import static org.ricey_yam.lynxmind.client.task.non_temp.life.sub.LFunctionHubTask.ClickSlotHostSubTask.ClickSlotTinyTask.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
public class ACraftingTask extends ATask {
    public enum CraftingState {
        FINDING_CRAFTING_WAY,
        CRAFTING_ITEM,
        PUTTING_CRAFTING_TABLE,
        PATHING_TO_CRAFTING_TABLE
    }
    /// 工作台位置
    private BlockPos craftingTablePos;
    /// 工作台掉落物实体
    private ItemEntity craftingTableLootEntity;
    /// 需要制作的物品列表
    private List<ItemStackLite> to_craft;
    /// 制作任务状态
    private CraftingState craftingState;
    /// 当前的制作任务
    private CraftingSingleSubTask currentCraftingSingleSubTask;
    /// 当前的放置工作台任务
    private PuttingCraftingTableSubTask currentPuttingCraftingTableSubTask;
    /// 制作失败的物品
    private List<ItemStackLite> craft_failed;
    /// 制作成功的物品
    private List<ItemStackLite> craft_success;

    public ACraftingTask(List<ItemStackLite> to_craft, Action linkedAction) {
        super();
        this.craftingState = CraftingState.FINDING_CRAFTING_WAY;
        this.taskType = ATaskType.CRAFTING;
        this.to_craft = new ArrayList<>(to_craft);
        this.craft_failed = new ArrayList<>();
        this.craft_success = new ArrayList<>();
        this.linkedAction = linkedAction;
    }

    @Override
    public void start() {
        this.currentTaskState = TaskState.IDLE;
        if(to_craft.isEmpty()) {
            stop("制作列表为空，该任务无法开始!");
        }
    }
    @Override
    public void tick(){
        tickTimer++;

        ClientUtils.getOptions().useKey.setPressed(false);

        var player = MinecraftClient.getInstance().player;
        if(player == null) return;

        if(to_craft.isEmpty()) {
            stop("完成制作！");
            return;
        }

        switch(this.craftingState) {
            /// 寻找制作方法
            case FINDING_CRAFTING_WAY -> findingCraftingWayTick();

            /// 寻找合适的工作台放置点
            case PUTTING_CRAFTING_TABLE -> puttingCraftingTableTick();

            /// 正在走向工作台
            case PATHING_TO_CRAFTING_TABLE -> pathingToCraftingTableTick();

            /// 制作阶段
            case CRAFTING_ITEM -> craftingItemTick();
        }
    }
    @Override
    public void stop(String stopReason) {
        currentTaskState = TaskState.STOPPED;

        currentCraftingSingleSubTask = null;
        currentPuttingCraftingTableSubTask = null;
        craftingTablePos = null;

        to_craft.clear();

        ClientUtils.getOptions().useKey.setPressed(false);
        ContainerHelper.closeContainer();

        getPathingSubTask().disable();
        getClickSlotHostSubTask().disable();

        /// 发送任务停止事件给AI
        sendATaskStopMessage(stopReason);

        LynxMindClient.sendModMessage(stopReason);

        System.out.println("制作任务已停止：" + stopReason);
    }
    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
        currentCraftingSingleSubTask = null;
        currentPuttingCraftingTableSubTask = null;
        getPathingSubTask().disable();
        getClickSlotHostSubTask().disable();
        ClientUtils.getOptions().useKey.setPressed(false);
        ContainerHelper.closeContainer();
    }

    @Override
    protected void sendATaskStopMessage(String stopReason) {
        if(stopReason != null && !stopReason.isEmpty() && AIServiceManager.isServiceActive() && AIServiceManager.isTaskActive() && linkedAction != null){
            if(linkedAction instanceof PlayerCraftingAction createAction){
                createAction.setCraft_failed(craft_failed);
                createAction.setCraft_success(craft_success);
            }
            var bTaskStopEvent = new PlayerATaskStop(linkedAction,stopReason);
            var serialized = AIJsonHandler.serialize(bTaskStopEvent);
            Objects.requireNonNull(AIServiceManager.sendMessageAndReceiveReplyAsync(serialized)).whenComplete((reply, error) -> AIChatManager.handleAIReply(reply));
        }
    }

    private void transitionToFindingCraftingWay(){
        this.craftingState = CraftingState.FINDING_CRAFTING_WAY;
    }
    private void transitionToPuttingCraftingTable(){
        this.craftingState = CraftingState.PUTTING_CRAFTING_TABLE;
    }
    private void transitionToPathingToCraftingTable(){
        craftingState = CraftingState.PATHING_TO_CRAFTING_TABLE;
    }
    private void transitionToCraftingItem(){
        this.craftingState = CraftingState.CRAFTING_ITEM;
    }

    private void findingCraftingWayTick(){
        if(currentCraftingSingleSubTask == null){
            System.out.println("制作子任务失效，重新创建。");
            currentCraftingSingleSubTask = new CraftingSingleSubTask(to_craft.get(0),this);
            return;
        }

        /// 需要工作台，尝试寻找/制作工作台然后制作物品
        if(isCraftingTableNeeding()) {
            /// 如果在制作界面 则直接开始制作
            if(inSuitableCraftingUI()){
                transitionToCraftingItem();
                return;
            }

            /// 尝试寻找附近现成的工作台
            craftingTablePos = BlockUtils.findNearestBlock(getPlayer(), 20,pos -> Objects.equals(BlockUtils.getBlockID(pos), "minecraft:crafting_table"));

            /// 没有工作台在20格内
            if(craftingTablePos == null){
                /// 先寻找是否附近有工作台掉落物
                var nearestLootEntity = EntityUtils.findNearestEntity(getPlayer(), ItemEntity.class,15, ie -> ie.getStack().getCount() > 0 && ItemUtils.getItemID(ie.getStack()).equals("minecraft:crafting_table"));
                if(nearestLootEntity != null) {
                    craftingTableLootEntity = nearestLootEntity;
                }

                /// 如果有工作台掉落物目标，优先寻路捡起掉落物
                if(craftingTableLootEntity != null && craftingTableLootEntity.getStack().getCount() > 0){
                    var newGoal = new GoalBlock(craftingTableLootEntity.getBlockPos());
                    getPathingSubTask().enable(newGoal);
                    return;
                }
                else{
                    getPathingSubTask().disable();
                }

                /// 判断背包是否有工作台
                if(PlayerUtils.hasItem("minecraft:crafting_table")){
                    System.out.println("有工作台，尝试放置工作台...");
                    transitionToPuttingCraftingTable();
                }
                else{
                    System.out.println("没有工作台，正在制作新工作台...");
                    to_craft.add(0,new ItemStackLite(1,"minecraft:crafting_table"));
                    currentCraftingSingleSubTask = null;
                }
            }
            /// 找到工作台 寻路到那
            else{
                transitionToPathingToCraftingTable();
                var newGoal = new GoalBlock(craftingTablePos);
                getPathingSubTask().enable(newGoal);
            }
        }
        /// 无需工作台 打开背包制作物品
        else{
            this.craftingState = CraftingState.CRAFTING_ITEM;
        }
    }
    private void puttingCraftingTableTick(){
        if(currentPuttingCraftingTableSubTask != null){
            currentPuttingCraftingTableSubTask.puttingTick();
        }
        else{
            System.out.println("放置工作台子任务失效，重新创建。");
            currentPuttingCraftingTableSubTask = new PuttingCraftingTableSubTask(this);
        }
    }
    private void pathingToCraftingTableTick(){
        /// 没摸到工作台，继续等待
        if(!isCraftingTableInRange()){
            var goal = baritone.getPathingBehavior().getGoal();
            /// 若寻路任务意外丢失 重新创建
            if(goal == null){
                var newGoal = new GoalBlock(craftingTablePos);
                getPathingSubTask().enable(newGoal);
            }
        }
        /// 看向工作台 点击工作台开始制作
        else{
            getPathingSubTask().disable();
            var targetRotation = PlayerUtils.calcLookRotationFromVec3d(getPlayer(),craftingTablePos);
            baritone.getLookBehavior().updateTarget(targetRotation,true);
            ContainerHelper.closeContainer();
            System.out.println("点击工作台!");
            ClientUtils.getOptions().useKey.setPressed(true);
            transitionToCraftingItem();
        }
    }
    private void craftingItemTick(){
        ClientUtils.getOptions().useKey.setPressed(false);
        getPathingSubTask().disable();

        /// 判断当前合成子任务状态
        if(currentCraftingSingleSubTask == null){
            System.out.println("当前子任务失效，重新创建。");
            currentCraftingSingleSubTask = new CraftingSingleSubTask(to_craft.get(0),this);
            return;
        }

        /// 子任务结束 判断合成结果
        if(currentCraftingSingleSubTask.getResult() != CraftingSingleSubTask.CraftingResult.NONE){
            var toCraftItemStack = currentCraftingSingleSubTask.getToCraftStack();
            var success = currentCraftingSingleSubTask.getResult() == CraftingSingleSubTask.CraftingResult.SUCCESS;
            updateCraftingResult(toCraftItemStack,success);

            if(to_craft.isEmpty()){
                stop("完成制作!2");
                return;
            }

            /// 如果连工作台都做不出来 则直接放弃任务
            if(!success && toCraftItemStack.getItem_name().equals("minecraft:crafting_table")){
                stop("没有足够的木板制作工作台");
                return;
            }

            /// 判断是否合成的是工作台
            if(toCraftItemStack.getItem_name().equals("minecraft:crafting_table")){
                transitionToPuttingCraftingTable();
            }
            else {
                transitionToFindingCraftingWay();
            }

            currentCraftingSingleSubTask = null;
        }
        else{
            /// 开始合成
            if(!isClickControllerOnBusy()){
                currentCraftingSingleSubTask.craftTick();
            }
            /// 不在工作台界面 需额外逻辑帮助打开工作台
            if(!inSuitableCraftingUI() && isCraftingTableNeeding()){
                transitionToFindingCraftingWay();
            }
        }
    }

    /// 是否能够到工作台
    private boolean isCraftingTableInRange(){
        if(craftingTablePos == null) return false;
        return baritone.getPlayerContext().player().getPos().distanceTo(craftingTablePos.toCenterPos()) < 3D;
    }

    /// 是否处于合适的制作界面
    private boolean inSuitableCraftingUI(){
        if(currentCraftingSingleSubTask == null || currentCraftingSingleSubTask.getCurrentLRecipe() == null || currentCraftingSingleSubTask.getCurrentLRecipe().getLinkedRecipe() == null){
            System.out.println("sub task or recipe is null!");
            return false;
        }
        if(currentCraftingSingleSubTask.getCurrentLRecipe().isCraftingTableNeeding()) return ContainerHelper.isContainerOpen(CraftingScreen.class);
        else return ContainerHelper.isContainerOpen(InventoryScreen.class);
    }

    /// 当前是否需要工作台
    private boolean isCraftingTableNeeding(){
        if(currentCraftingSingleSubTask == null || currentCraftingSingleSubTask.getCurrentLRecipe() == null || currentCraftingSingleSubTask.getCurrentLRecipe().getLinkedRecipe() == null) return false;
        return currentCraftingSingleSubTask.getCurrentLRecipe().isCraftingTableNeeding();
    }

    /// 更新制作结果
    private void updateCraftingResult(ItemStackLite itemStack,boolean successful){
        /// 准备制作下一个物品
        var currentCrafting = to_craft.get(0);
        if(currentCrafting.getCount() > 1){
            currentCrafting.setCount(currentCrafting.getCount() - 1);
        }
        else {
            to_craft.remove(0);
        }

        /// 加入制作成功/失败名单
        var actualList = successful ? craft_success : craft_failed;
        for(var item : actualList){
            if(item.getItem_name().equals(itemStack.getItem_name())){
                item.setCount(item.getCount() + 1);
                return;
            }
        }
        actualList.add(new ItemStackLite(itemStack.getCount(),itemStack.getItem_name()));
    }

    protected static boolean isClickControllerOnBusy(){
        var clickSlotHostSubTask = Objects.requireNonNull(getActiveTask()).getClickSlotHostSubTask();
        return clickSlotHostSubTask.isEnabled() && clickSlotHostSubTask.getResult() == ClickSlotHostSubTask.Result.NONE;
    }

    @Setter
    @Getter
    static class CraftingSingleSubTask {
        public enum CraftingResult{
            NONE,
            SUCCESS,
            FAILED
        }
        private ACraftingTask parentTask;
        /// 子任务状态
        private CraftingResult result;
        /// 该子任务对应的配方
        private LRecipe currentLRecipe;
        /// 需制作的物品
        private ItemStackLite toCraftStack;

        public CraftingSingleSubTask(ItemStackLite toCraftStack,ACraftingTask parentTask) {
            this.parentTask = parentTask;
            this.result = CraftingResult.NONE;
            this.toCraftStack = toCraftStack.copy();
            var confirmedLRecipe = new LRecipe(toCraftStack);
            if(confirmedLRecipe.getLinkedRecipe() == null){
                this.result = CraftingResult.FAILED;
                System.out.println("无法找到该物品的配方，跳过制作！");
            }
            else{
                this.currentLRecipe = confirmedLRecipe;
            }
        }

        /// 交互逻辑
        public void craftTick(){
            var craftingClickSlotTasks = currentLRecipe.getClickSlotTasks();
            /// 检查子任务是否正常
            if(result != CraftingResult.NONE) {
                ContainerHelper.closeContainer();
                return;
            }

            /// 点击格子开始制作
            if(craftingClickSlotTasks != null && !craftingClickSlotTasks.isEmpty() && !isClickControllerOnBusy()){
                parentTask.getClickSlotHostSubTask().enable(craftingClickSlotTasks);
            }
            /// 更新结果
            else if (parentTask.getClickSlotHostSubTask().getResult() != ClickSlotHostSubTask.Result.NONE) {
                var createSuccess = parentTask.getClickSlotHostSubTask().getResult() == ClickSlotHostSubTask.Result.SUCCESS;
                currentLRecipe = null;
                this.result = createSuccess ? CraftingResult.SUCCESS : CraftingResult.FAILED;
            }
        }
    }

    @Setter
    @Getter
    static class PuttingCraftingTableSubTask{
        private ACraftingTask parentTask;
        private TaskState state;
        /// 工作台放置点
        private BlockPos cTPlacingPointPos;
        /// 放置点的搜索范围
        private int craftingTablePlacingPointSearchingRange;
        /// 把工作台移到快捷栏的点击任务
        private List<ClickSlotTinyTask> moveCraftingTableClickSlotTasks;

        private boolean isCraftingTableInHotbar = false;

        public PuttingCraftingTableSubTask(ACraftingTask parentTask){
            this.moveCraftingTableClickSlotTasks = new ArrayList<>();
            this.state = TaskState.IDLE;
            this.parentTask = parentTask;
            this.craftingTablePlacingPointSearchingRange = 4;

            /// 是否有工作台，若没有则返回
            if(!PlayerUtils.hasItem("minecraft:crafting_table")){
                parentTask.setCraftingState(CraftingState.FINDING_CRAFTING_WAY);
                this.state = TaskState.STOPPED;
                return;
            }

            /// 寻找工作台的格子
            var currentCraftingTableLSlot = SlotHelper.getLSlotByItemID("minecraft:crafting_table", ComplexContainerType.PLAYER_INFO);
            if(currentCraftingTableLSlot == null) {
                this.state = TaskState.STOPPED;
                return;
            }

            /// 判断当前工作台是否处于快捷栏
            this.isCraftingTableInHotbar = currentCraftingTableLSlot.getSlotType() == LSlotType.INVENTORY_HOTBAR;
            /// 不是：创建打开背包把工作台移动到快捷栏的UTask
            if(!this.isCraftingTableInHotbar){
                /// 重置已经有的制作子任务 防止拿错物品
                parentTask.setCurrentCraftingSingleSubTask(null);

                var takeoutUTask = new ClickSlotTinyTask(currentCraftingTableLSlot,0,SlotActionType.PICKUP,Aim.PICKUP);
                var putUTask = new ClickSlotTinyTask(new InventoryHotBarSlot(8,ComplexContainerType.PLAYER_INFO),0,SlotActionType.PICKUP,Aim.PUT);
                var takeCursorItemBack = new ClickSlotTinyTask(currentCraftingTableLSlot,0,SlotActionType.PICKUP,Aim.NONE);

                moveCraftingTableClickSlotTasks.add(takeoutUTask);
                moveCraftingTableClickSlotTasks.add(putUTask);
                moveCraftingTableClickSlotTasks.add(takeCursorItemBack);
            }
            if(!moveCraftingTableClickSlotTasks.isEmpty()){
                parentTask.getClickSlotHostSubTask().enable(moveCraftingTableClickSlotTasks);
            }
        }

        public void puttingTick(){
            /// 如果在工作台界面 则无需放置
            if(getParentTask().inSuitableCraftingUI()) {
                parentTask.setCraftingState(CraftingState.CRAFTING_ITEM);
                this.state = TaskState.STOPPED;
                return;
            }

            var craftingState = parentTask.getCraftingState();
            var craftingTablePos = parentTask.getCraftingTablePos();
            var player = ClientUtils.getPlayer();
            var baritone = parentTask.getBaritone();

            /// 检查附近是否有工作台 有则无需放置
            craftingTablePos = BlockUtils.findNearestBlock(player, 20,pos -> Objects.equals(BlockUtils.getBlockID(pos), "minecraft:crafting_table"));
            if(craftingTablePos != null && !Objects.requireNonNull(BlockUtils.getBlockState(craftingTablePos)).isAir()) {
                parentTask.setCraftingState(CraftingState.FINDING_CRAFTING_WAY);
                this.state = TaskState.STOPPED;
                return;
            }

            /// 寻找放置点
            cTPlacingPointPos = BlockUtils.findCraftingTablePlacePoint(player,craftingTablePlacingPointSearchingRange);
            if(cTPlacingPointPos == null) {
                if(craftingTablePlacingPointSearchingRange > 16){
                    parentTask.stop("该环境无法使用工作台。");
                }
                else{
                    craftingTablePlacingPointSearchingRange *= 2;
                    System.out.println("未找到放置工作台位置，正在扩大搜索范围：" + craftingTablePlacingPointSearchingRange);
                }
                return;
            }

            /// 放置点太远，尝试寻路
            if(!isCTPlacingPointInRange()){
                parentTask.getPathingSubTask().enable(new GoalNear(cTPlacingPointPos,1));
            }
            else{
                parentTask.getPathingSubTask().disable();
                var currentCraftingTableLSlot = SlotHelper.getLSlotByItemID("minecraft:crafting_table", ComplexContainerType.PLAYER_INFO);
                if(currentCraftingTableLSlot == null) {
                    System.out.println("CurrentCraftingTableLSlot is null!");
                    return;
                }

                /// 随时判断工作台是否在快捷栏
                this.isCraftingTableInHotbar = currentCraftingTableLSlot.getSlotType() == LSlotType.INVENTORY_HOTBAR;
                if(this.isCraftingTableInHotbar){
                    parentTask.getClickSlotHostSubTask().disable();
                    ContainerHelper.closeContainer();
                    if(SlotHelper.switchToHotbarItem("minecraft:crafting_table")){
                        System.out.println("正在看向放置点！");
                        var targetRotation = PlayerUtils.calcLookRotationFromVec3d(player,cTPlacingPointPos.down());
                        baritone.getLookBehavior().updateTarget(targetRotation,true);
                        System.out.println("放下工作台!");
                        ClientUtils.getOptions().useKey.setPressed(true);
                    }
                }
                /// 把工作台从背包内拿到快捷栏
                else if(!isClickControllerOnBusy()){
                    parentTask.getClickSlotHostSubTask().enable(moveCraftingTableClickSlotTasks);
                }
            }
        }

        /// 工作台放置点在合适范围内
        private boolean isCTPlacingPointInRange(){
            var baritone = parentTask.getBaritone();
            if(cTPlacingPointPos == null) return false;
            return baritone.getPlayerContext().player().getPos().distanceTo(cTPlacingPointPos.toCenterPos()) < 3D;
        }
    }
}

package org.ricey_yam.lynxmind.client.task.non_temp.life.sub;

import baritone.api.IBaritone;
import baritone.api.behavior.IPathingBehavior;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.ICustomGoalProcess;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.ricey_yam.lynxmind.client.module.pathing.BaritoneManager;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTask;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTaskType;
import org.ricey_yam.lynxmind.client.utils.game_ext.ClientUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.block.BlockUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.PlayerUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ContainerHelper;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.LSlot;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.SlotHelper;

import java.util.*;

@Getter
@Setter
public class LFunctionHubTask extends LTask {
    public static LFunctionHubTask getActiveTask(){
        return (LFunctionHubTask) LTask.getActiveLTask(LTaskType.FUNCTION_HUB, LFunctionHubTask.class);
    }
    private List<LFunctionHubSubTask> functionSubTasks = new ArrayList<>();
    private AttackSubTask attackSubTask;
    private PathingSubTask pathingSubTask;
    private ClickSlotHostSubTask clickSlotHostSubTask;
    private MineSubTask mineSubTask;
    public LFunctionHubTask(Object... args) {
        super(args);
        setTaskType(LTaskType.FUNCTION_HUB);
        this.attackSubTask = new AttackSubTask(3);
        this.pathingSubTask = new PathingSubTask();
        this.clickSlotHostSubTask = new ClickSlotHostSubTask();
        this.mineSubTask = new MineSubTask();

        this.functionSubTasks.addAll(List.of(attackSubTask,pathingSubTask,clickSlotHostSubTask,mineSubTask));
    }

    @Override
    public void start() {
        this.currentTaskState = TaskState.IDLE;
    }

    @Override
    public void tick() {
        functionSubTasks.sort(Comparator.comparing(LFunctionHubSubTask::getWeight));
        LFunctionHubSubTask lastTickedSubTask = null;
        for (int i = 0; i < functionSubTasks.size(); i++) {
            var subTask = functionSubTasks.get(i);
            if(subTask == null || !subTask.enabled) continue;
            if(lastTickedSubTask != null && lastTickedSubTask.getWeight() > subTask.getWeight()){
                return;
            }
            subTask.tick();
            lastTickedSubTask = subTask;
        }
    }

    @Override
    public void stop(String cancelReason) {
        this.currentTaskState = TaskState.STOPPED;
        disableAllSubTasks();
        attackSubTask = null;
        pathingSubTask = null;
        clickSlotHostSubTask = null;
        mineSubTask = null;
    }

    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
        disableAllSubTasks();
    }

    private void disableAllSubTasks(){
        attackSubTask.disable();
        pathingSubTask.disable();
        clickSlotHostSubTask.disable();
        mineSubTask.disable();
    }

    @Getter
    @Setter
    public static abstract class LFunctionHubSubTask {
        protected int weight;
        protected boolean enabled;

        public void enable(Object... args){
            this.enabled = true;
        }

        public void disable(Object... args){
            this.enabled = false;
        }

        protected abstract void tick();

        protected PlayerEntity getPlayer(){
            return ClientUtils.getPlayer();
        }

        protected IBaritone getBaritone(){
            return BaritoneManager.getClientBaritone();
        }
    }


    /**
     * 挖掘子任务
     */
    @Getter
    @Setter
    public static class MineSubTask extends LFunctionHubSubTask {
        /// 真正的挖掘目标
        private BlockPos targetBlockPos;
        /// 正在挖掘的目标(因为有遮挡 不一定为真正目标)
        private BlockPos miningBlockPos;
        /// 正在挖掘的方块ID
        private String miningBlockID;

        public MineSubTask(){
            this.weight = 0;
        }

        @Override
        protected void tick() {
            if(enabled && targetBlockPos != null && getPlayer().isOnGround()){
                try{
                    if(ContainerHelper.isContainerOpen()){
                        var client = MinecraftClient.getInstance();
                        client.execute(this::resetMiningButton);
                        return;
                    }

                    var options = ClientUtils.getOptions();
                    if(options == null) {
                        return;
                    }

                    var targetBlock = BlockUtils.getTargetBlock(targetBlockPos);
                    if (targetBlock == null || getBaritone() == null || getPlayer() == null) {
                        resetMiningButton();
                        return;
                    }

                    if(!isBlockGone(targetBlockPos)){
                        if(!PlayerUtils.isLookingAt(targetBlockPos)) {
                            var targetRotation = PlayerUtils.calcLookRotationFromVec3d(getPlayer(),targetBlockPos);
                            getBaritone().getLookBehavior().updateTarget(targetRotation,true);
                        }

                        options.attackKey.setPressed(true);

                        var selectedBlock = PlayerUtils.getSelectedBlock();
                        if(selectedBlock != null){
                            miningBlockID = PlayerUtils.getSelectedBlockID();
                            miningBlockPos = PlayerUtils.getSelectedBlockPos();
                        }
                    }
                    else {
                        miningBlockID = "";
                        miningBlockPos = null;
                        targetBlockPos = null;
                        resetMiningButton();
                    }
                }
                catch (Exception e) {
                    System.out.println("挖掘方块出现错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private boolean isBlockGone(BlockPos pos) {
            if (pos == null) return true;
            var state = BlockUtils.getBlockState(pos);
            return state == null || state.isAir();
        }

        private void resetMiningButton(){
            var options = ClientUtils.getOptions();
            if(options == null) {
                return;
            }
            options.attackKey.setPressed(false);
        }

        @Override
        public void enable(Object... args) {
            super.enable(args);
            targetBlockPos = (BlockPos) args[0];
        }

        @Override
        public void disable(Object... args) {
            super.disable(args);
            resetMiningButton();
            targetBlockPos = null;
        }
    }


    /**
     * 攻击实体子任务
     * @attackRange 攻击范围
     */
    @Getter
    @Setter
    public static class AttackSubTask extends LFunctionHubSubTask {
        /// 攻击范围
        private float attackRange;
        /// 攻击对象
        private List<UUID> attackTargetList = new ArrayList<>();
        /// 当前攻击目标
        private LivingEntity currentTarget;

        private float hitTickTimer;
        /// 攻击间隔(取决于当前武器)
        private float hitTickDelay;

        /// 最佳武器格子
        private LSlot bestWeaponLSlot;
        /// 最佳武器物品ID
        private String bestWeaponID;
        /// 武器是否在快捷栏
        private boolean weaponInHotbar;

        public AttackSubTask(float attackRange){
            this.weight = 1;
            this.hitTickDelay = 5;
            attackTargetList.clear();
            this.attackRange = attackRange;
        }
        public void tick() {
            hitTickTimer++;

            if(!this.enabled || attackTargetList == null || attackTargetList.isEmpty()) {
                return;
            }

            if(targetDied() || !isNearbyTarget()){
                nextTarget();
                return;
            }

            var goalProcess = getBaritone().getCustomGoalProcess();
            if(goalProcess != null && goalProcess.isActive()) {
                goalProcess.setGoal(null);
                getBaritone().getPathingBehavior().cancelEverything();
            }

            var options = ClientUtils.getOptions();
            if(options == null) return;

            if(hitTickTimer >= hitTickDelay){
                ClientUtils.getController().attackEntity(getPlayer(), currentTarget);
                getPlayer().swingHand(Hand.MAIN_HAND);
                resetAttackCooldown(PlayerUtils.getHoldingItemStack());
            }
        }

        @Override
        public void enable(Object... args){
            super.enable(args);
            this.attackTargetList = new ArrayList<>((List<UUID>) args[0]);
        }

        /// 重置攻击间隔
        private void resetAttackCooldown(ItemStack holdingItem){
            var itemID = ItemUtils.getItemID(holdingItem);
            this.hitTickDelay = PlayerUtils.getAttackingCooldownTick(itemID);
            this.hitTickTimer = 0;
        }

        /// 目标是否在攻击范围内
        public boolean isNearbyTarget(){
            if(currentTarget == null) return false;
            return getPlayer().distanceTo(currentTarget) <= attackRange;
        }

        /// 切换攻击目标
        private void nextTarget(){
            currentTarget = EntityUtils.findNearestEntity(getPlayer(),LivingEntity.class,10,e -> attackTargetList.contains(e.getUuid()) && e.distanceTo(getPlayer()) <= attackRange);
        }

        /// 目标是否死亡
        private boolean targetDied(){
            if(currentTarget != null && currentTarget.getHealth() <= 0){
                attackTargetList.remove(currentTarget.getUuid());
                currentTarget = null;
            }
            return currentTarget == null || currentTarget.getHealth() <= 0;
        }
    }


    /**
     * 操作格子子任务
     */
    @Getter
    @Setter
    public static class ClickSlotHostSubTask extends LFunctionHubSubTask{
        public enum Result{
            NONE,
            SUCCESS,
            FAILED
        }
        private Result result;
        private List<ClickSlotTinyTask> clickSlotTinyTasks = new ArrayList<>();
        private ClickSlotTinyTask performingClickSlotTinyTask;
        private boolean autoCloseCurrentContainer;

        public ClickSlotHostSubTask(){
            this.weight = 3;
        }

        @Override
        protected void tick() {
            if(!enabled) return;
            if(clickSlotTinyTasks == null || clickSlotTinyTasks.isEmpty()){
                result = clickSlotTinyTasks == null ? Result.FAILED : Result.SUCCESS;
                disable();
                return;
            }
            if(inSuitableContainer()) {
                clickSlotTinyTasks.removeIf(clickSlotTinyTask -> clickSlotTinyTask.getResult() != ClickSlotTinyTask.Result.NONE);
                /// 若当前无操作，进行下一步操作
                if(performingClickSlotTinyTask == null){
                    performingClickSlotTinyTask = clickSlotTinyTasks.get(0);
                    performingClickSlotTinyTask.invoke();
                }
                /// 若当前有操作，判断操作结果，来决定是否下一步
                else if(performingClickSlotTinyTask.getResult() != ClickSlotTinyTask.Result.NONE){
                    switch (performingClickSlotTinyTask.getResult()) {
                        /// 成功：继续下一个操作
                        case SUCCESS -> performingClickSlotTinyTask = null;
                        /// 失败: 终止任务
                        case FAILED -> {
                            System.out.println("点击格子出现错误!已中断ClickSlotHostSubTask任务!");
                            ContainerHelper.closeContainer();
                            clickSlotTinyTasks.clear();
                            result = Result.FAILED;
                            performingClickSlotTinyTask = null;
                            disable();
                        }
                    }
                }
            }
            else {
                openContainer();
            }
        }

        @Override
        public void enable(Object... args){
            super.enable(args);
            this.enabled = true;
            this.clickSlotTinyTasks = (List<ClickSlotTinyTask>) args[0];
            this.autoCloseCurrentContainer = args.length >= 2 && (boolean) args[1];
            result = Result.NONE;
        }

        @Override
        public void disable(Object... args){
            super.disable(args);
            this.clickSlotTinyTasks.clear();
            if(autoCloseCurrentContainer){
                ContainerHelper.closeContainer();
                autoCloseCurrentContainer = false;
            }
        }

        /// 是否在特定容器
        private boolean inSuitableContainer(){
            return ContainerHelper.isContainerOpen(getContainerClass());
        }

        /// 打开特定容器
        private void openContainer(){
            /// 只有玩家背包能直接打开 其它需要特殊处理
            if(getContainerClass() != InventoryScreen.class) return;

            ContainerHelper.openContainer(getContainerClass());
        }

        ///获取容器的类(判断打开何种容器)
        private Class<? extends Screen> getContainerClass(){
            if(clickSlotTinyTasks == null || clickSlotTinyTasks.isEmpty()) return null;
            var complexContainerType = clickSlotTinyTasks.get(0).getL_slot().getComplexContainerType();
            switch (complexContainerType){
                case PLAYER_INFO -> {
                    return InventoryScreen.class;
                }
                case CRAFTING_TABLE -> {
                    return CraftingScreen.class;
                }
                case FURNACE -> {
                    return FurnaceScreen.class;
                }
                case BREWING_STAND -> {
                    return BrewingStandScreen.class;
                }
                case SMITHING_TABLE -> {
                    return SmithingScreen.class;
                }
                case CHEST -> {
                    return GenericContainerScreen.class;
                }
                case CHEST_BIG -> {
                    return GenericContainerScreen.class;
                }
            }
            return null;
        }

        @Getter
        @Setter
        public static class ClickSlotTinyTask{
            public enum Aim{
                NONE,
                PICKUP,
                PUT
            }
            public enum Result {
                NONE,
                SUCCESS,
                FAILED
            }
            protected LSlot l_slot;
            protected Aim aim;
            protected Result result;
            protected boolean clicked;
            protected int button;
            protected SlotActionType slotActionType;
            public ClickSlotTinyTask(LSlot l_slot,int button,SlotActionType slotActionType,Aim aim) {
                this.result = Result.NONE;
                this.l_slot = l_slot;
                this.aim = aim;
                this.button = button;
                this.slotActionType = slotActionType;
            }
            public void invoke() {
                if(result != Result.NONE) {
                    return;
                }

                clicked = click();
                if(clicked){
                    result = aimGotten() ? Result.SUCCESS : Result.FAILED;
                }
            }

            private boolean click(){
                return SlotHelper.clickContainerSlot(l_slot,button, slotActionType);
            }

            private boolean aimGotten(){
                var slotItemStack = SlotHelper.getSlotStack(l_slot.getSlotId());
                switch (aim){
                    case PICKUP ->{
                        return  slotItemStack == null || slotItemStack.isEmpty();
                    }
                    case PUT -> {
                        return slotItemStack != null && !slotItemStack.isEmpty();
                    }
                    default -> {
                        return true;
                    }
                }
            }
        }
    }


    /**
     * 寻路子任务
     */
    @Getter
    @Setter
    public static class PathingSubTask extends LFunctionHubSubTask{
        private Goal goal;
        private ICustomGoalProcess customGoalProcess;
        private IPathingBehavior pathingBehavior;

        public PathingSubTask(){
            this.weight = 1;
            this.customGoalProcess = getBaritone().getCustomGoalProcess();
            this.pathingBehavior = getBaritone().getPathingBehavior();
        }

        @Override
        protected void tick() {
            if(!enabled || goal == null || customGoalProcess == null) return;
            customGoalProcess.setGoalAndPath(goal);
        }

        @Override
        public void enable(Object... args) {
            super.enable(args);
            this.goal = (Goal) args[0];
            customGoalProcess.setGoalAndPath(goal);
        }

        @Override
        public void disable(Object... args) {
            if(!enabled) return;
            super.disable(args);
            this.goal = null;
            customGoalProcess.setGoal(null);
            pathingBehavior.cancelEverything();
        }
    }
}

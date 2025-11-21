package org.ricey_yam.lynxmind.client.task.temp.baritone;

import baritone.api.pathing.goals.GoalNear;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.temp.ui.UClickSlotTask;
import org.ricey_yam.lynxmind.client.task.temp.ui.UTask;
import org.ricey_yam.lynxmind.client.utils.game_ext.ClientUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.PlayerUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ComplexContainerType;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ContainerHelper;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.InventoryHotBarSlot;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.LSlot;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.LSlotType;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.SlotHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BKillauraTask extends BTask{
    public enum KillauraState {
        ATTACKING,
        PATHING_TO_TARGET,
        SWITCHING_TO_WEAPON
    }
    private KillauraState killauraState;

    private float attackRange;
    private List<UUID> attackingTarget = new ArrayList<>();
    private LivingEntity currentTarget;

    private float hitTickTimer;
    private float hitTickDelay;

    private LSlot bestWeaponLSlot;
    private String bestWeaponID;
    private boolean weaponInHotbar;
    private List<UTask> switchingToWeaponUTasks = new ArrayList<>();
    private UTask performingUTask;

    public BKillauraTask(float attackRange){
        super();
        this.weight = 2;
        this.hitTickDelay = 5;
        attackingTarget.clear();
        killauraState = KillauraState.ATTACKING;
        setTaskType(BTaskType.KILLAURA);
        this.attackRange = attackRange;
    }
    @Override
    public void start() {
        this.currentTaskState = TaskState.IDLE;
    }
    @Override
    public void tick() {
        tickTimer++;
        switch(killauraState){
            /// 寻路到目标附近
            case PATHING_TO_TARGET -> pathingToTargetTick();

            /// 开始攻击
            case ATTACKING -> attackingTick();

            /// 切换到武器
            case SWITCHING_TO_WEAPON -> switchingToWeaponTick();
        }
    }
    @Override
    public void stop(String cancelReason) {
        this.currentTaskState = TaskState.FINISHED;
        attackingTarget.clear();
        attackRange = 0;
    }
    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
        baritone.getCustomGoalProcess().setGoal(null);
        baritone.getPathingBehavior().cancelEverything();
    }

    private void transitionToPathingToTarget(){
        this.killauraState = KillauraState.PATHING_TO_TARGET;
    }
    private void transitionToAttacking(){
        ContainerHelper.closeContainer();
        this.killauraState = KillauraState.ATTACKING;
    }
    private void transitionToSwitchingToWeapon(){
        this.killauraState = KillauraState.SWITCHING_TO_WEAPON;
        if(holdingBestWeapon()) {
            transitionToAttacking();
            return;
        }
        bestWeaponID = PlayerUtils.getBestWeaponIDInInventory();
        bestWeaponLSlot = SlotHelper.getLSlotByItemID(bestWeaponID,ComplexContainerType.PLAYER_INFO);
        if(bestWeaponLSlot != null) {
            weaponInHotbar = bestWeaponLSlot.getSlotType() == LSlotType.INVENTORY_HOTBAR;
            if(!weaponInHotbar){
                createClickSlotUTask();
            }
        }
    }
    private void pathingToTargetTick(){
        if(attackingTarget == null || attackingTarget.isEmpty() || baritone == null || baritone.getLookBehavior() == null) return;
        var nearbyEnemies = EntityUtils.scanAllEntity(getPlayer(), LivingEntity.class, 6, e -> e.distanceTo(getPlayer()) <= 6 && attackingTarget.contains(e.getUuid()));
        currentTarget = nearbyEnemies.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(getPlayer())))
                .orElse(null);
        var goalProcess = baritone.getCustomGoalProcess();
        if(isNearbyTarget()){
            goalProcess.setGoal(null);
            baritone.getPathingBehavior().cancelEverything();
            this.killauraState = KillauraState.ATTACKING;
        }
        else if(currentTarget != null && goalProcess != null && !goalProcess.isActive()){
            var targetGoal = new GoalNear(currentTarget.getBlockPos(), (int) attackRange);
            goalProcess.setGoalAndPath(targetGoal);
        }
    }
    private void attackingTick(){
        if(!isNearbyTarget()){
            this.killauraState = KillauraState.PATHING_TO_TARGET;
            return;
        }

        var goalProcess = baritone.getCustomGoalProcess();
        if(goalProcess != null && goalProcess.isActive()) {
            goalProcess.setGoal(null);
            baritone.getPathingBehavior().cancelEverything();
        }

        var options = ClientUtils.getOptions();
        if(options == null) return;
        resetAttackingButton();

        if(!holdingBestWeapon()){
            transitionToSwitchingToWeapon();
            return;
        }
        hitTickTimer++;
        if(currentTarget != null){
            if(hitTickTimer >= hitTickDelay){
                ClientUtils.getController().attackEntity(getPlayer(), currentTarget);
                getPlayer().swingHand(Hand.MAIN_HAND);
                resetAttackCooldown(PlayerUtils.getHoldingItemStack());
            }
        }
        else transitionToPathingToTarget();
    }
    private void switchingToWeaponTick(){
        resetAttackingButton();
        if(holdingBestWeapon()){
            transitionToAttacking();
            return;
        }
        if(bestWeaponLSlot == null) {
            return;
        }
        weaponInHotbar = bestWeaponLSlot.getSlotType() == LSlotType.INVENTORY_HOTBAR;
        if(!weaponInHotbar){
            if(ContainerHelper.isContainerOpen(InventoryScreen.class)) {
                if(switchingToWeaponUTasks != null && !switchingToWeaponUTasks.isEmpty()){
                    switchingToWeaponUTasks.removeIf(uTask -> uTask.getCurrentTaskState() != TaskState.IDLE);
                    if(performingUTask == null){
                        performingUTask = switchingToWeaponUTasks.get(0);
                        LynxMindEndTickEventManager.registerTask(performingUTask);
                    }
                    else if(performingUTask.getResult() != UTask.UTaskResult.NONE){
                        switch (performingUTask.getResult()) {
                            case SUCCESS -> performingUTask = null;
                            case FAILED -> createClickSlotUTask();
                        }
                    }
                }
                else{
                    createClickSlotUTask();
                }
            }
            else {
                createClickSlotUTask();
                ContainerHelper.openContainer(InventoryScreen.class);
            }
        }
        else{
            ContainerHelper.closeContainer();
            SlotHelper.switchToHotbarItem(bestWeaponID);
        }
    }

    /// 抬起攻击键
    private void resetAttackingButton(){
        var options = ClientUtils.getOptions();
        if(options == null) return;
        options.attackKey.setPressed(false);
    }

    /// 重置攻击间隔
    private void resetAttackCooldown(ItemStack holdingItem){
        var itemID = ItemUtils.getItemID(holdingItem);
        this.hitTickDelay = PlayerUtils.getAttackingCooldownTick(itemID);
        this.hitTickTimer = 0;
    }

    /// 背包有合适的武器
    private boolean hasSuitableWeapon(){
        bestWeaponID = PlayerUtils.getBestWeaponIDInInventory();
        return bestWeaponID != null && !bestWeaponID.isEmpty() && !bestWeaponID.contains("air");
    }

    /// 创建UTask来拿出武器
    private void createClickSlotUTask(){
        ContainerHelper.closeContainer();
        bestWeaponLSlot = SlotHelper.getLSlotByItemID(bestWeaponID, ComplexContainerType.PLAYER_INFO);
        performingUTask = null;
        switchingToWeaponUTasks.clear();
        var quickLSlotID = SlotHelper.getQuickHotbarLSlotIDForTool(bestWeaponID);
        var u1 = new UClickSlotTask(bestWeaponLSlot,0, SlotActionType.PICKUP);
        var u2 =  new UClickSlotTask(new InventoryHotBarSlot(quickLSlotID,ComplexContainerType.PLAYER_INFO),0, SlotActionType.PICKUP);
        var u3 = new UClickSlotTask(bestWeaponLSlot,0, SlotActionType.PICKUP);
        switchingToWeaponUTasks.add(u1);
        switchingToWeaponUTasks.add(u2);
        switchingToWeaponUTasks.add(u3);
    }

    /// 是否握着最佳武器
    private boolean holdingBestWeapon(){
        if(!hasSuitableWeapon()) return true;
        var holdingItemStack = PlayerUtils.getHoldingItemStack();
        return ItemUtils.getItemID(holdingItemStack).equals(bestWeaponID);
    }

    /// 是否在攻击范围内
    private boolean isNearbyTarget(){
        if(currentTarget == null) return false;
        return getPlayer().distanceTo(currentTarget) <= attackRange;
    }
}

package org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub;

import net.minecraft.screen.slot.SlotActionType;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.LTask;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.LTaskType;
import org.ricey_yam.lynxmind.client.utils.game_ext.ClientUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.PlayerUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ComplexContainerType;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ContainerHelper;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.InventoryHotBarSlot;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.LSlot;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.LSlotType;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.SlotHelper;

import java.util.List;
import java.util.Objects;
import static org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LFunctionHubTask.ClickSlotHostSubTask.ClickSlotTinyTask;
import static org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LFunctionHubTask.ClickSlotHostSubTask.ClickSlotTinyTask.*;

public class LAutoGearSwitcher extends LTask {
    enum GearType{
        TOOL,
        WEAPON
    }
    public LAutoGearSwitcher(){
        setTaskType(LTaskType.AUTO_GEAR_SWITCHER);
    }

    @Override
    public void start() {
        this.currentTaskState = TaskState.IDLE;
    }

    @Override
    public void tick() {
        if(isAutomaticallyAttacking() && !holdingBestWeapon()){
            switchToSuitableItem(GearType.WEAPON);
        }
        else if(isAutomaticallyMiningSafely() && !holdingBestTool()){
            switchToSuitableItem(GearType.TOOL);
        }
    }

    @Override
    public void stop(String cancelReason) {
        this.currentTaskState = TaskState.STOPPED;
    }

    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
    }

    private LFunctionHubTask.ClickSlotHostSubTask getClickControllerSubTask(){
        return Objects.requireNonNull(LFunctionHubTask.getActiveTask()).getClickSlotHostSubTask();
    }
    private boolean isClickControllerOnBusy(){
        return getClickControllerSubTask().enabled
                || getClickControllerSubTask().getClickSlotTinyTasks() != null
                || !getClickControllerSubTask().getClickSlotTinyTasks().isEmpty()
                || ContainerHelper.isContainerOpen();
    }

    private void switchToSuitableItem(GearType gearType){
        var clickSubTask = getClickControllerSubTask();
        var baritone = getBaritone();

        var targetPos = getMineSubTask().getMiningBlockPos();
        if(targetPos == null && gearType == GearType.TOOL) {
            System.out.println("Mining block is null");
            return;
        }

        var suitableTargetItemID = gearType == GearType.TOOL ? PlayerUtils.getBestToolIDInInventory(targetPos) : PlayerUtils.getBestWeaponIDInInventory();
        if(suitableTargetItemID == null){
            System.out.println("target item is null!");
            return;
        }

        var targetItemOrigalLSlot = SlotHelper.getLSlotByItemID(suitableTargetItemID, ComplexContainerType.PLAYER_INFO);
        if(targetItemOrigalLSlot == null) {
            System.out.println("Cant find suitable item!");
            return;
        }

        var isTargetItemInHotbar = targetItemOrigalLSlot.getSlotType() == LSlotType.INVENTORY_HOTBAR;
        if(isTargetItemInHotbar){
            ContainerHelper.closeContainer();
            SlotHelper.switchToHotbarItem(suitableTargetItemID);
        }
        else{
            var targetHotbarLSlotID = SlotHelper.getQuickHotbarLSlotIDForTool(suitableTargetItemID);
            var targetLSlot = new InventoryHotBarSlot(targetHotbarLSlotID,ComplexContainerType.PLAYER_INFO);
            var clickingSlotTinyTasks = summonSwitchingItemClickingSlotTasks(suitableTargetItemID,targetItemOrigalLSlot,targetLSlot);
            clickSubTask.enable(clickingSlotTinyTasks,true);
        }
    }
    private List<ClickSlotTinyTask> summonSwitchingItemClickingSlotTasks(String switchingItemID, LSlot orignalLSlot, LSlot targetLSlot){
        var result = getClickControllerSubTask().getClickSlotTinyTasks();
        result.clear();
        var toolQuickLSlotID = SlotHelper.getQuickHotbarLSlotIDForTool(switchingItemID);
        var u1 = new ClickSlotTinyTask(orignalLSlot,0, SlotActionType.PICKUP, Aim.PICKUP);
        var u2 =  new ClickSlotTinyTask(targetLSlot,0, SlotActionType.PICKUP, Aim.PUT);
        var u3 = new ClickSlotTinyTask(orignalLSlot,0, SlotActionType.PICKUP, Aim.NONE);
        result.add(u1);
        result.add(u2);
        result.add(u3);
        return result;
    }

    /// 获取挖掘子任务
    private LFunctionHubTask.MineSubTask getMineSubTask(){
        return Objects.requireNonNull(LFunctionHubTask.getActiveTask()).getMineSubTask();
    }
    /// 是否处于挖掘状态
    private boolean isAutomaticallyMiningSafely(){
        return ClientUtils.getController().isBreakingBlock() && getPlayer().isOnGround() && !isAutomaticallyAttacking();
    }
    /// 是否有合适的挖掘工具
    private boolean hasSuitableTool(){
        var miningBlockPos = getMineSubTask().getMiningBlockPos();
        var bestToolID = PlayerUtils.getBestToolIDInInventory(miningBlockPos);
        return bestToolID != null && !bestToolID.isEmpty() && !bestToolID.contains("air");
    }
    /// 是否握着最佳挖掘工具
    private boolean holdingBestTool(){
        if(!hasSuitableTool()) return true;
        var holdingItemStack = PlayerUtils.getHoldingItemStack();
        var miningBlockPos = getMineSubTask().getMiningBlockPos();
        var bestToolID = PlayerUtils.getBestToolIDInInventory(miningBlockPos);
        return ItemUtils.getItemID(holdingItemStack).equals(bestToolID);
    }

    /// 获取攻击子任务
    private LFunctionHubTask.AttackSubTask getAttackingSubTask(){
        return Objects.requireNonNull(LFunctionHubTask.getActiveTask()).getAttackSubTask();
    }
    /// 是否在攻击状态
    private boolean isAutomaticallyAttacking(){
        var functionHubTask = Objects.requireNonNull(LFunctionHubTask.getActiveTask());
        var attackSubTask = functionHubTask.getAttackSubTask();
        return attackSubTask.isEnabled() && attackSubTask.getAttackTargetList() != null && !attackSubTask.getAttackTargetList().isEmpty();
    }
    /// 背包有合适的武器
    private boolean hasSuitableWeapon(){
        var bestWeaponID = PlayerUtils.getBestWeaponIDInInventory();
        return bestWeaponID != null && !bestWeaponID.isEmpty() && !bestWeaponID.contains("air");
    }
    /// 是否握着最佳武器
    private boolean holdingBestWeapon(){
        if(!hasSuitableWeapon()) return true;
        var holdingItemStack = PlayerUtils.getHoldingItemStack();
        var bestWeaponID = PlayerUtils.getBestWeaponIDInInventory();
        return ItemUtils.getItemID(holdingItemStack).equals(bestWeaponID);
    }
}

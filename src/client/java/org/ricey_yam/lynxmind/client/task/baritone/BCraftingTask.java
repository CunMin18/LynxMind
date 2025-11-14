package org.ricey_yam.lynxmind.client.task.baritone;

import baritone.api.IBaritone;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import org.ricey_yam.lynxmind.client.ai.json.game_info.item.ItemStackLite;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;
import org.ricey_yam.lynxmind.client.utils.game.BlockUtils;
import org.ricey_yam.lynxmind.client.utils.game.ItemUtils;

import java.util.List;

@Getter
@Setter
public class BCraftingTask extends BTask {
    private IBaritone baritone;
    /// 工作台位置
    private BlockPos craftingTablePos;
    /// 正在制作的物品ID
    private String crafting_item;
    /// 需要制作的物品列表
    private List<ItemStackLite> to_craft;

    public BCraftingTask(List<ItemStackLite> to_craft) {
        this.taskType = BTaskType.CRAFTING;
        this.baritone = BaritoneManager.getClientBaritone();
        this.to_craft = to_craft;
    }
    @Override
    public void start() {
        var player = MinecraftClient.getInstance().player;
        if(to_craft.isEmpty()) {
            stop("制作列表为空");
            return;
        }
        crafting_item = to_craft.get(0).getItem_name();
        if(craftTableNeeding(crafting_item)) {
            craftingTablePos = BlockUtils.findNearestBlock(player, List.of("minecraft:crafting_table"),20);
            if(craftingTablePos == null){
                System.out.println("未发现工作台，正在制作新工作台...");
                //todo crafting new ct in inventory
            }
            else{
                //todo path to crafting table
            }
        }

    }

    @Override
    public void tick() {

    }

    @Override
    public void stop(String cancelReason) {

    }

    private boolean craftTableNeeding(String itemId) {
        return ItemUtils.CraftingHelper.requiresWorkbench(itemId);
    }

    private boolean isCraftingTableInRange(){
        var player = MinecraftClient.getInstance().player;
        if(craftingTablePos == null) return false;
        return baritone.getPlayerContext().player().getPos().distanceTo(craftingTablePos.toCenterPos()) < 4D;
    }
}

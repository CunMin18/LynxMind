package org.ricey_yam.lynxmind.client.utils.game;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class InteractionUtils {
    public static ClientPlayerInteractionManager getController(){
        return MinecraftClient.getInstance().interactionManager;
    }

    /// 点击容器格子
    public static boolean clickContainerSlot(int slotId, int button, SlotActionType actionType){
        var player = MinecraftClient.getInstance().player;
        if(player == null) return false;
        var syncId = player.currentScreenHandler.syncId;
        try{
            getController().clickSlot(syncId,slotId,button,actionType,player);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("点击容器物品时出现错误：" + e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前鼠标光标上的物品
     * @return 光标上的物品栈，如果为空则返回一个空的 ItemStack
     */
    public static ItemStack getCursorItem() {
        var client = MinecraftClient.getInstance();

        // 聊天框、设置界面等没有物品栏的GUI
        if (!(client.currentScreen instanceof HandledScreen<?> handledScreen)) {
            return ItemStack.EMPTY;
        }

        ScreenHandler screenHandler = handledScreen.getScreenHandler();

        // 如果没有打开任何 GUI（即处于游戏世界中），则屏幕处理器为 null
        if (screenHandler == null) {
            return ItemStack.EMPTY;
        }

        // 从屏幕处理器中获取光标物品
        return screenHandler.getCursorStack();
    }

    /**
     * 判断鼠标光标上是否有物品
     * @return true 表示光标上有物品，false 表示没有
     */
    public static boolean hasCursorItem() {
        ItemStack cursorStack = getCursorItem();
        return !cursorStack.isEmpty();
    }
}

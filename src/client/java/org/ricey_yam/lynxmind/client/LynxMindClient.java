package org.ricey_yam.lynxmind.client;

import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.Text;
import org.ricey_yam.lynxmind.client.module.pathing.BaritoneManager;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.command.ModCommand;
import org.ricey_yam.lynxmind.client.config.ModConfig;

public class LynxMindClient implements ClientModInitializer {
    @Getter
    private final static String modID = "lynx_mind";
    @Override
    public void onInitializeClient() {
        /// 加载全部配置文件
        ModConfig.load();

        /// 初始化Task管理器
        LynxMindEndTickEventManager.init();

        /// 注册命令
        ModCommand.registerCommands();

        /// test only
        LynxMindBrain.wake();
    }

    /// 发送信息到玩家聊天栏并添加日志输出
    public static void sendModMessage(String message) {
        BaritoneManager.getPlayer().sendMessage(Text.of("§c§l[§6§lLynxMindClient§c§l]§e " + message));
    }
}

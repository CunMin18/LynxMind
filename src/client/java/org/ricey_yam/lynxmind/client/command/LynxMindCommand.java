package org.ricey_yam.lynxmind.client.command;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.command.argument.multi_argument.MultiBlockStateArgumentBuilder;
import org.ricey_yam.lynxmind.client.command.argument.multi_argument.MultiEntityArgumentBuilder;
import org.ricey_yam.lynxmind.client.command.argument.multi_argument.MultiItemStackArgumentBuilder;
import org.ricey_yam.lynxmind.client.command.argument.suggestions.AIServiceSuggestionsProvider;
import org.ricey_yam.lynxmind.client.config.ModConfig;
import org.ricey_yam.lynxmind.client.module.ai.service.AIServiceManager;
import org.ricey_yam.lynxmind.client.module.ai.message.AIChatManager;
import org.ricey_yam.lynxmind.client.module.ai.message.AIJsonHandler;
import org.ricey_yam.lynxmind.client.module.ai.message.action.sub.*;
import org.ricey_yam.lynxmind.client.module.ai.message.event.player.sub.PlayerScanBlockEvent;
import org.ricey_yam.lynxmind.client.module.ai.message.event.player.sub.PlayerScanEntityEvent;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.module.ai.service.AIServiceType;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTaskType;
import org.ricey_yam.lynxmind.client.task.non_temp.life.sub.LAutoStrikeBackTask;
import org.ricey_yam.lynxmind.client.task.temp.action.AEntityCollectionTask;
import org.ricey_yam.lynxmind.client.module.pathing.BaritoneManager;
import org.ricey_yam.lynxmind.client.module.ai.message.event.player.sub.PlayerStatusHeartBeatEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class LynxMindCommand {
    static class AIServiceExecutor {
        private static int connectToAIService(CommandContext<ServerCommandSource> context) {
            var serviceTypeStr = StringArgumentType.getString(context, "SERVICE_TYPE");
            var serviceType = AIServiceType.valueOf(serviceTypeStr);
            AIServiceManager.openServiceAsync(serviceType);
            return 1;
        }

        private static int disconnect(CommandContext<ServerCommandSource> context) {
            AIServiceManager.closeServiceAsync();
            return 1;
        }

        private static int setTaskForAI(CommandContext<ServerCommandSource> context){
            /// 停止先前的任务
            AIServiceManager.stopTask("终止了先前任务创建的 ATask");

            var taskDesc = StringArgumentType.getString(context,"你想做什么？");
            AIServiceManager.setCurrentTask(taskDesc);

            if(AIServiceManager.isServiceActive()){
                /// 新任务
                LynxMindClient.sendModMessage("创建新任务：" + taskDesc);
                AIChatManager.sendTaskMessageToAIAndReceiveReply().whenComplete((reply, throwable) ->{
                    if(throwable != null){
                        System.out.println("处理消息时异常！" + throwable.getMessage());
                    }
                    else{
                        AIChatManager.handleAIReply(reply);
                    }
                });
            }
            else{
                LynxMindClient.sendModMessage("未连接到AI服务，请输入/lynx start");
            }

            return 1;
        }

        private static int removeTaskForAI(CommandContext<ServerCommandSource> context){
            BaritoneManager.stopAllTasks("玩家手动取消了当前任务");
            if(AIServiceManager.isServiceActive()){
                AIChatManager.sendRemovingTaskMessageToAIAndReceiveReply();
                LynxMindClient.sendModMessage("已删除任务：" + AIServiceManager.getCurrentTask());
                AIServiceManager.setCurrentTask("");
            }
            else{
                LynxMindClient.sendModMessage("未连接到AI服务，请输入/lynx start");
            }

            return 1;
        }
    }
    static class FakeActionExecutor {
        private static int stop(CommandContext<ServerCommandSource> context){
            try{
                BaritoneManager.stopAllTasks("玩家手动取消了当前任务");
                return 1;
            }
            catch(Exception e){
                System.out.println("取消任务时出错：" + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
        private static int pathTo(CommandContext<ServerCommandSource> context){
            var pos = BlockPosArgumentType.getBlockPos(context,"pos");
            var x = pos.getX();
            var y = pos.getY();
            var z = pos.getZ();
            var newPathAction = new PlayerMoveAction(x,y,z);
            newPathAction.invoke();
            return 1;
        }
        private static int mine(CommandContext<ServerCommandSource> context){
            try {
                var collectingPlan = MultiItemStackArgumentBuilder.getItemStackLiteList(context);
                if (collectingPlan.isEmpty()) {
                    LynxMindClient.sendModMessage("§c错误: 方块列表不能为空！");
                    return 0;
                }
                var newMiningAction = new PlayerCollectBlockAction(collectingPlan);
                newMiningAction.invoke();
                return 1;
            }
            catch (Exception e) {
                LynxMindClient.sendModMessage("§c执行挖掘命令时出错: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
        private static int craft(CommandContext<ServerCommandSource> context){
            try {
                var craftingPlan = MultiItemStackArgumentBuilder.getItemStackLiteList(context);
                if (craftingPlan.isEmpty()) {
                    LynxMindClient.sendModMessage("§c错误: 物品列表不能为空！");
                    return 0;
                }
                var newCreateAction = new PlayerCraftingAction(craftingPlan);
                newCreateAction.invoke();
                return 1;
            }
            catch (Exception e) {
                LynxMindClient.sendModMessage("§c执行制作命令时出错: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
        private static int killingForCollection(CommandContext<ServerCommandSource> context){
            try {
                var gson = new Gson();
                var kqss = StringArgumentType.getString(context,"kq_json");
                if (kqss == null || kqss.trim().isEmpty()) {
                    LynxMindClient.sendModMessage("§c错误: 请指定JSON!");
                    return 0;
                }
                var kq_list = Arrays.asList(kqss.trim().split("\\s+"));
                if (kq_list.isEmpty()) {
                    LynxMindClient.sendModMessage("§c错误: JSON列表不能为空！");
                    return 0;
                }
                var kqs = new ArrayList<AEntityCollectionTask.EntityKillingQuota>();
                for (int i = 0; i < kq_list.size(); i++) {
                    var kq = kq_list.get(i);
                    var newKQ = gson.fromJson(kq, AEntityCollectionTask.EntityKillingQuota.class);
                    kqs.add(newKQ);
                }

                var newCollectLootAction = new PlayerCollectEntityLootAction(kqs);
                newCollectLootAction.invoke();
                return 1;
            }
            catch (Exception e) {
                LynxMindClient.sendModMessage("§c执行制作命令时出错: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
        private static int murder(CommandContext<ServerCommandSource> context){
            try {
                var uuids = StringArgumentType.getString(context,"uuids");
                if (uuids == null || uuids.trim().isEmpty()) {
                    LynxMindClient.sendModMessage("§c错误: 请指定要击杀的目标!");
                    return 0;
                }
                var uuid_list = Arrays.asList(uuids.trim().split("\\s+"));
                if (uuid_list.isEmpty()) {
                    LynxMindClient.sendModMessage("§c错误: UUID列表不能为空！");
                    return 0;
                }
                var newMurderAction = new PlayerMurderAction(uuid_list);
                newMurderAction.invoke();
                return 1;
            }
            catch (Exception e) {
                LynxMindClient.sendModMessage("§c执行制作命令时出错: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
    }
    static class DebugExecutor{
        private static int getStatus(CommandContext<ServerCommandSource> context){
            try{
                var playerStatusHeartBeatEvent = new PlayerStatusHeartBeatEvent();
                var serialized = AIJsonHandler.serialize(playerStatusHeartBeatEvent);
                LynxMindClient.sendModMessage("当前玩家状态\n" + serialized);
            }
            catch(Exception e){
                System.out.println("查询玩家状态时遇到错误：" + e.getMessage());
                e.printStackTrace();
            }
            return 1;
        }
        private static int scanBlockNearby(CommandContext<ServerCommandSource> context){
            try{
                var radius = IntegerArgumentType.getInteger(context,"radius");
                var blockIDs = MultiBlockStateArgumentBuilder.getBlockIDList(context);
                if(blockIDs.isEmpty()){
                    LynxMindClient.sendModMessage("§c错误: 方块列表不能为空！");
                    return 0;
                }
                var playerScanBlockEvent = new PlayerScanBlockEvent(radius,blockIDs);
                var serialized = AIJsonHandler.serialize(playerScanBlockEvent);
                LynxMindClient.sendModMessage("附近方块\n" + serialized);
            }
            catch(Exception e){
                System.out.println("扫描附近方块时遇到错误：" + e.getMessage());
                e.printStackTrace();
            }
            return 1;
        }
        private static int scanEntityNearby(CommandContext<ServerCommandSource> context){
            try{
                var radius = IntegerArgumentType.getInteger(context,"radius");
                var entityIDs = MultiEntityArgumentBuilder.getEntityIDList(context);
                if(entityIDs.isEmpty()){
                    LynxMindClient.sendModMessage("§c错误: 实体列表不能为空！");
                    return 0;
                }
                var playerScanEntityEvent = new PlayerScanEntityEvent(radius,entityIDs);
                var serialized = AIJsonHandler.serialize(playerScanEntityEvent);
                LynxMindClient.sendModMessage("附近实体\n" + serialized);
            }
            catch(Exception e){
                System.out.println("扫描附近实体时遇到错误：" + e.getMessage());
                e.printStackTrace();
            }
            return 1;
        }
        private static int autoKillaura(CommandContext<ServerCommandSource> context){
            try{
                var enabled = BoolArgumentType.getBool(context,"enabled");
                if(enabled){
                    LynxMindEndTickEventManager.registerTask(new LAutoStrikeBackTask(5,10));
                    LynxMindClient.sendModMessage("自动杀戮光环已开启!");
                }
                else{
                    LynxMindEndTickEventManager.unregisterTask(LTaskType.AUTO_STRIKE_BACK,"COMMAND");
                    LynxMindClient.sendModMessage("自动杀戮光环已关闭!");
                }
            }
            catch(Exception e){
                System.out.println("自动杀戮光环开启失败: " + e.getMessage());
                e.printStackTrace();
            }
            return 1;
        }
    }
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("lynx")

                .then(CommandManager.literal("start")
                        .then(CommandManager.argument("SERVICE_TYPE", StringArgumentType.string()).suggests(AIServiceSuggestionsProvider.getInstance())
                                .executes(AIServiceExecutor::connectToAIService)
                        )
                )
                .then(CommandManager.literal("stop")
                        .executes(AIServiceExecutor::disconnect)
                )
                .then(CommandManager.literal("pause")
                        .executes(AIServiceExecutor::removeTaskForAI)
                )
                .then(CommandManager.literal("task")
                        .then(CommandManager.argument("你想做什么？", StringArgumentType.string())
                                .executes(AIServiceExecutor::setTaskForAI))
                )

                .then(CommandManager.literal("reload")
                        .executes(LynxMindCommand::reloadConfigByCommand)
                )

                .then(CommandManager.literal("run")
                        .then(CommandManager.literal("cancel")
                                .executes(FakeActionExecutor::stop)
                        )
                        .then(CommandManager.literal("path")
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(FakeActionExecutor::pathTo))
                        )
                        .then(CommandManager.literal("mine")
                                .then(MultiItemStackArgumentBuilder.build(registryAccess, FakeActionExecutor::mine,10))
                        )
                        .then(CommandManager.literal("craft")
                                .then(MultiItemStackArgumentBuilder.build(registryAccess, FakeActionExecutor::craft,10))
                        )
                        .then(CommandManager.literal("kfc")
                                .then(CommandManager.argument("kq_json", StringArgumentType.greedyString())
                                        .executes(FakeActionExecutor::killingForCollection))
                        )
                        .then(CommandManager.literal("murder")
                                .then(CommandManager.argument("uuids", StringArgumentType.greedyString())
                                        .executes(FakeActionExecutor::murder))
                        )
                )

                .then(CommandManager.literal("debug")
                        .then(CommandManager.literal("status")
                                .executes(DebugExecutor::getStatus)
                        )
                        .then(CommandManager.literal("scan_block")
                                .then(CommandManager.argument("radius", IntegerArgumentType.integer())
                                        .then(MultiBlockStateArgumentBuilder.build(registryAccess, DebugExecutor::scanBlockNearby,10))
                                )
                        )
                        .then(CommandManager.literal("scan_entity")
                                .then(CommandManager.argument("radius", IntegerArgumentType.integer())
                                        .then(MultiEntityArgumentBuilder.build(registryAccess, DebugExecutor::scanEntityNearby,10))
                                )
                        )
                        .then(CommandManager.literal("auto_killaura")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(DebugExecutor::autoKillaura)
                                )
                        )
                )
        );
    }
    private static int reloadConfigByCommand(CommandContext<ServerCommandSource> context){
        ModConfig.load();
        LynxMindClient.sendModMessage("配置文件已重载！");
        return 1;
    }
}

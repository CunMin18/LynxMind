package org.ricey_yam.lynxmind.client.config.service;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.loader.api.FabricLoader;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.config.ModConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class LynxConfig {
    private static final Map<Class<? extends LynxConfig>, LynxConfig> INSTANCE_MAP = new HashMap<>();

    public String getFileName() {
        return "DUMMY.yml";
    }
    /// 加载配置文件
    protected LynxConfig load(){
        return ModConfig.loadConfig(getConfigDir(),this.getClass());
    }

    /// 保存配置文件
    protected void save(){
        ModConfig.saveConfig(getConfigDir(),this.getClass());
    }

    /// 配置文件路径
    protected String getConfigDir(){
        return FabricLoader.getInstance().getConfigDir().resolve(LynxMindClient.getModID()).resolve(getFileName()).toString();
    }

    public static void loadConfig(Class<? extends LynxConfig> configClass){
        INSTANCE_MAP.replace(configClass, Objects.requireNonNull(getInstance(configClass)).load());
    }

    public static void saveConfig(Class<? extends LynxConfig> configClass){
        Objects.requireNonNull(getInstance(configClass)).save();
    }

    protected static LynxConfig getInstance(Class<? extends LynxConfig> configClass){
        try{
            if(!INSTANCE_MAP.containsKey(configClass)) {
                var newInstance = configClass.getDeclaredConstructor().newInstance();
                INSTANCE_MAP.put(configClass,newInstance);
            }
            return INSTANCE_MAP.get(configClass);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

package org.ricey_yam.lynxmind.client.config;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.loader.api.FabricLoader;
import org.ricey_yam.lynxmind.client.LynxMindClient;

@Getter
@Setter
public class AIServiceConfig {

    @Getter
    private static AIServiceConfig instance;

    private static final String fileName = "AIServiceConfig.yml";
    private static String configDir;

    private String api_url;
    private String model;
    private String token;
    private boolean pseudo_continuous_dialogue;

    /// 保存配置文件
    public static void save(){
        ConfigManager.saveConfig(configDir,fileName);
    }

    /// 加载配置文件
    public static void load(){
        configDir = FabricLoader.getInstance().getConfigDir().resolve(LynxMindClient.getModID()).resolve(fileName).toString();
        instance = ConfigManager.loadConfig(configDir, AIServiceConfig.class);
    }
}

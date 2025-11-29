package org.ricey_yam.lynxmind.client.config.service;

import lombok.Getter;
import lombok.Setter;

/// 火山方舟
@Getter
@Setter
public class VolcEngineServiceConfig extends LynxConfig {
    private String api_url = "https://ark.cn-beijing.volces.com/api/v3";
    private String model;
    private String api_key;

    @Override
    public String getFileName() {
        return "VolcEngineServiceConfig.yml";
    }

    public static void loadConfig(){
        LynxConfig.loadConfig(VolcEngineServiceConfig.class);
    }

    public static void saveConfig(){
        LynxConfig.saveConfig(VolcEngineServiceConfig.class);
    }

    public static VolcEngineServiceConfig getInstance(){
        return (VolcEngineServiceConfig) LynxConfig.getInstance(VolcEngineServiceConfig.class);
    }
}

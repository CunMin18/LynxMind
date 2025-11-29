package org.ricey_yam.lynxmind.client.config.service;

import lombok.Getter;
import lombok.Setter;

/// OPENAI// 亦可用于兼容OPENAI接口的逆向API (需自建服务)
@Getter
@Setter
public class OpenAIServiceConfig extends LynxConfig{
    private String api_url;
    private String model;
    private String api_token;
    private boolean pseudo_continuous_dialogue;

    @Override
    public String getFileName() {
        return "OpenAIServiceConfig.yml";
    }

    public static void loadConfig(){
        LynxConfig.loadConfig(OpenAIServiceConfig.class);
    }

    public static void saveConfig(){
        LynxConfig.saveConfig(OpenAIServiceConfig.class);
    }

    public static OpenAIServiceConfig getInstance(){
        return (OpenAIServiceConfig) LynxConfig.getInstance(OpenAIServiceConfig.class);
    }
}

package org.ricey_yam.lynxmind.client.utils.game;

public class TransformUtils {
    /**
     * 将 Minecraft 原始偏航角（可能大于360或小于0）规范化到 -180 到 180 的范围。
     * @param yaw 原始偏航角
     * @return 规范化后的偏航角 (-180.0F <= yaw < 180.0F)
     */
    public static float normalizeYaw180(float yaw) {
        yaw = yaw % 360.0F;
        if (yaw >= 180.0F) {
            yaw -= 360.0F;
        } else if (yaw < -180.0F) {
            yaw += 360.0F;
        }
        return yaw;
    }
}

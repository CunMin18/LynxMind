package org.ricey_yam.lynxmind.client.utils.game_ext;

import baritone.api.utils.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

    public static Rotation getRotation(Vec3d vec3dForm, Vec3d vec3dTo) {
        var diff = vec3dTo.subtract(vec3dForm);
        var distance = diff.length();
        var xzDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y, xzDistance));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F;

        return new Rotation(yaw, pitch).normalize();
    }

    public static float getDistance(BlockPos pos1, BlockPos pos2) {
        var x = pos1.getX() - pos2.getX();
        var y = pos1.getY() - pos2.getY();
        var z = pos1.getZ() - pos2.getZ();
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

}

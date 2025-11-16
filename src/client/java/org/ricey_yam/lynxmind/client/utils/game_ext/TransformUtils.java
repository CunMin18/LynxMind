package org.ricey_yam.lynxmind.client.utils.game_ext;

import baritone.api.utils.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;

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

    /// 计算旋转角度
    public static Rotation calcRotationFromVec3d(BlockPos from, BlockPos to) {
        var vec3dForm = new  Vec3d(from.getX(), from.getY(), from.getZ());
        var vec3dTo = new Vec3d(to.getX(), to.getY(), to.getZ());
        var diff = vec3dTo.subtract(vec3dForm);
        var distance = diff.length();
        var xzDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y, xzDistance));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F;

        return new Rotation(yaw, pitch).normalize();
    }

    /// 是否玩家看向某个位置
    public static boolean isLookingAt(BlockPos pos) {
        var baritone = BaritoneManager.getClientBaritone();
        if(baritone == null) return false;
        return baritone.getPlayerContext().isLookingAt(pos);
    }
}

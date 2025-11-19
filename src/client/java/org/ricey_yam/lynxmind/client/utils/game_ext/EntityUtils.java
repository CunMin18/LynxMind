package org.ricey_yam.lynxmind.client.utils.game_ext;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class EntityUtils {
    public static <T extends Entity> T findNearestEntity(Entity entity,Class<T> targetEntityClass,int boxSize){
        if(entity == null) return null;
        var world = entity.getEntityWorld();
        if(world == null) return null;
        var minPos = entity.getBlockPos().add(-boxSize, -boxSize, -boxSize);
        var maxPos = entity.getBlockPos().add(boxSize, boxSize, boxSize);
        var minVec3 = new Vec3d(minPos.getX(), minPos.getY(), minPos.getZ());
        var maxVec3 = new Vec3d(maxPos.getX(), maxPos.getY(), maxPos.getZ());
        var targets = world.getEntitiesByClass(targetEntityClass,new Box(minVec3,maxVec3),e -> true);
        if(targets.isEmpty()) return null;

        var nearestEntity = targets.get(0);
        float minR = boxSize * 2;
        for(var target : targets){
            if(target.distanceTo(entity) < minR){
                minR = target.distanceTo(entity);
                nearestEntity = target;
            }
        }
        return nearestEntity;
    }
}

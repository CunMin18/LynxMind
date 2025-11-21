package org.ricey_yam.lynxmind.client.utils.game_ext.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;

@Getter
@Setter
public class EntityLite {
    private String id;
    private float max_health;
    private float current_health;
    private int x;
    private int y;
    private int z;
    public EntityLite(LivingEntity entity) {
        this.id = EntityUtils.getEntityID(entity);
        this.current_health = entity.getHealth();
        this.max_health = entity.getMaxHealth();
        this.x = entity.getBlockPos().getX();
        this.y = entity.getBlockPos().getY();
        this.z = entity.getBlockPos().getZ();
    }
    public EntityLite(String id, float maxHealth, float currentHealth, int x, int y, int z) {
        this.id = id;
        this.max_health = maxHealth;
        this.current_health = currentHealth;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

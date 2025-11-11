package org.ricey_yam.lynxmind.client.ai.json.event.player.child;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.PlayerEntity;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;
import org.ricey_yam.lynxmind.client.ai.json.event.player.PlayerEvent;
import org.ricey_yam.lynxmind.client.ai.json.event.player.PlayerEventType;
import org.ricey_yam.lynxmind.client.baritone.status.BStatus;
import org.ricey_yam.lynxmind.client.ai.json.game_info.item.ItemStackLite;
import org.ricey_yam.lynxmind.client.utils.game.TransformUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlayerStatusHeartBeatEvent extends PlayerEvent {

    private float health;
    private float maxHealth;

    private int hunger;
    private int maxHunger = 20;
    private float saturationLevel;

    private int posX;
    private int posY;
    private int posZ;

    private float yaw;
    private float pitch;

    private List<ItemStackLite> inventory = new ArrayList<>();

    private BStatus current_baritone_task;
    public PlayerStatusHeartBeatEvent(PlayerEntity player) {
        setType(PlayerEventType.EVENT_PLAYER_STATUS_HEARTBEAT);

        this.health = player.getHealth();
        this.maxHealth = player.getMaxHealth();

        this.hunger = player.getHungerManager().getFoodLevel();
        this.saturationLevel = player.getHungerManager().getSaturationLevel();

        this.posX = player.getBlockX();
        this.posY = player.getBlockY();
        this.posZ = player.getBlockZ();

        this.yaw = TransformUtils.normalizeYaw180(player.getHeadYaw());
        this.pitch = player.getPitch();

        var inventory = player.getInventory().main;
        if(inventory != null && !inventory.isEmpty()) {
            for (int i = 0; i < inventory.size(); i++) {
                var itemStack = inventory.get(i);
                if(itemStack.isEmpty()) continue;
                var itemStackLite = new ItemStackLite(itemStack);
                this.inventory.add(itemStackLite);
            }
        }

        current_baritone_task = BaritoneManager.getCurrentBStatus();
    }
}

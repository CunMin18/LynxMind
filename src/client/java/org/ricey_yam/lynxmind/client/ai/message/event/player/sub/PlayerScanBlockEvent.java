package org.ricey_yam.lynxmind.client.ai.message.event.player.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.message.event.player.PlayerEvent;
import org.ricey_yam.lynxmind.client.ai.message.event.player.PlayerEventType;
import org.ricey_yam.lynxmind.client.utils.game_ext.block.BlockLite;
import org.ricey_yam.lynxmind.client.utils.game_ext.block.BlockUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class PlayerScanBlockEvent extends PlayerEvent {
    private int radius;
    private List<String> scanning_id;
    private List<BlockLite> nearby_blocks;
    private String summary;
    public PlayerScanBlockEvent(int radius, List<String> scanning_id) {
        setType(PlayerEventType.EVENT_PLAYER_SCAN_BLOCK);
        this.radius = radius;
        this.scanning_id = scanning_id;
        this.nearby_blocks = BlockUtils.scanAllBlocks(getPlayer(),scanning_id,radius);

        if(nearby_blocks.size() > 50){
            Map<String, Long> id_Count = nearby_blocks.stream().collect(Collectors.groupingBy(BlockLite::getId, Collectors.counting()));
            summary = "附近的方块非常多: ";
            for(Map.Entry<String, Long> entry : id_Count.entrySet()){
                summary += entry.getKey() + "有" + entry.getValue() + "个 | ";
            }
            nearby_blocks.clear();
        }
    }
}

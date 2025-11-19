package org.ricey_yam.lynxmind.client.utils.game_ext.item;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签工具类
 */
public class ItemTagHelper {
    private static final List<TargetBlockTagMatcher> blockTagMatchers = new ArrayList<>();

    static {
        /// 矿物
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("diamond","钻石"),
                List.of("minecraft:diamond_ore", "minecraft:deepslate_diamond_ore", "minecraft:diamond_block")
        ));
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("redstone","红石"),
                List.of("minecraft:redstone_ore", "minecraft:deepslate_redstone_ore", "minecraft:redstone_block")
        ));
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("iron", "iron_ingot","铁","铁锭"),
                List.of("minecraft:iron_ore", "minecraft:deepslate_iron_ore", "minecraft:iron_block")
        ));
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("gold", "gold_ingot","金","金锭"),
                List.of("minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:gold_block")
        ));
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("emerald","绿宝石"),
                List.of("minecraft:emerald_ore", "minecraft:deepslate_emerald_ore", "minecraft:emerald_block")
        ));
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("coal","煤炭","煤"),
                List.of("minecraft:coal_ore", "minecraft:deepslate_coal_ore", "minecraft:coal_block")
        ));
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("lapis","青金石"),
                List.of("minecraft:lapis_ore", "minecraft:deepslate_lapis_ore", "minecraft:lapis_block")
        ));
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("copper", "copper_ingot","铜","cu","铜锭"),
                List.of("minecraft:copper_ore", "minecraft:deepslate_copper_ore", "minecraft:copper_block")
        ));
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("cobblestone","原石","圆石","原神"),
                List.of("minecraft:cobblestone", "minecraft:stone")
        ));

        /// 原木
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("log","原木","木头"),
                List.of("minecraft:oak_log", "minecraft:spruce_log", "minecraft:birch_log", "minecraft:jungle_log", "minecraft:acacia_log",
                        "minecraft:dark_oak_log", "minecraft:crimson_stem", "minecraft:warped_stem", "minecraft:mangrove_log", "minecraft:cherry_log",
                        "minecraft:pale_oak_log")
        ));

        /// 泥土
        blockTagMatchers.add(new TargetBlockTagMatcher(
                List.of("土","泥土","dirt"),
                List.of("minecraft:grass_block", "minecraft:dirt")
        ));
    }

    /**
     * 获取物品标签匹配列表
     */
    public static List<String> getTagList(String keyword) {
        for (var matcher : blockTagMatchers) {
            if (matcher == null) continue;
            if (matcher.match(keyword)) return matcher.getTags();
        }
        return null;
    }

    /**
     * 模糊匹配：关键词是否包含在物品标签中（忽略大小写）
     */
    public static boolean isFuzzyMatch(String keyword, String tag) {
        var fuzzyTags = getTagList(keyword);
        if (fuzzyTags != null) {
            return fuzzyTags.contains(tag);
        }
        return false;
    }

    @Getter
    @Setter
    static class TargetBlockTagMatcher {
        private final List<String> keywords;
        private final List<String> tags;

        public TargetBlockTagMatcher(List<String> keywords, List<String> tags) {
            this.keywords = keywords;
            this.tags = tags;
        }

        public boolean match(String keyword) {
            keyword = keyword.toLowerCase();
            return keywords.contains(keyword.replaceAll("minecraft:", "").replaceAll("\"",""));
        }
    }
}

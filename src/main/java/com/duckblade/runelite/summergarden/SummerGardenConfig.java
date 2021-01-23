package com.duckblade.runelite.summergarden;

import java.awt.Color;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclicksummergarden")
public interface SummerGardenConfig extends Config {
    @ConfigItem(
            keyName = "highlightGood",
            name = "On-Parity Color",
            description = "Color to highlight elementals whose parity is correct.",
            position = 1
    )
    default Color highlightGood() {
        return Color.green.darker();
    }

    @ConfigItem(
            keyName = "highlightBad",
            name = "Off-Parity Color",
            description = "Color to highlight elementals whose parity is incorrect.",
            position = 2
    )
    default Color highlightBad() {
        return Color.orange;
    }

    @ConfigItem(
            keyName = "highlightLaunch",
            name = "Launch Color",
            description = "Color to highlight elementals when it is time to click the Sq'irk tree.",
            position = 3
    )
    default Color highlightLaunch() {
        return Color.decode("#00ADFF");
    }

    @ConfigItem(
            keyName = "highlightLaunchTile",
            name = "Highlight Launch Tile",
            description = "Whether or not to highlight the tile at which the first elemental will be when you click the Sq'irk tree.",
            position = 4
    )
    default boolean highlightLaunchTile() {
        return true;
    }

    @ConfigItem(
            keyName = "showNumbers",
            name = "Show Numbers",
            description = "Whether to show numbers on Elementals, showing how many resets will be needed.",
            position = 5
    )
    default ShowNumbers showNumbers() {
        return ShowNumbers.YES;
    }

    @ConfigItem(
            keyName = "staminaWarning",
            name = "Low Stamina Threshold",
            description = "What stamina level to warn to use stamina potion (0 to disable).",
            position = 6
    )
    default int staminaThreshold() {
        return 25;
    }

}

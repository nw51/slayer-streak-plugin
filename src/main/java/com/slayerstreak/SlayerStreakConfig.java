package com.slayerstreak;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("slayerstreak")
public interface SlayerStreakConfig extends Config
{
    @ConfigItem(
            keyName = "hideSlayerMasters",
            name = "Hide masters before milestone",
            description = "In the NPC Contact interface, hides every slayer master except Konar when you're one task from a 50th/100th streak milestone."
    )
    default boolean hideSlayerMasters()
    {
        return true;
    }
}
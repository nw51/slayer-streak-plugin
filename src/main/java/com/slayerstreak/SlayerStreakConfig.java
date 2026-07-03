package com.slayerstreak;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("slayerstreak")
public interface SlayerStreakConfig extends Config
{
    @ConfigItem(
            keyName = "hideSlayerMasters",
            name = "Restrict masters before milestone",
            description = "One task before your chosen milestone, removes Talk-to/Assignment from other slayer masters (Konar stays available) so you don't accidentally break your streak bonus."
    )
    default boolean hideSlayerMasters()
    {
        return true;
    }

    @ConfigItem(
            keyName = "milestoneInterval",
            name = "Milestone interval",
            description = "Which streak milestone to protect."
    )
    default MilestoneInterval milestoneInterval()
    {
        return MilestoneInterval.EVERY_50;
    }
}
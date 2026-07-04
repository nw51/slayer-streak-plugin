package com.slayerstreak;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Units;

@ConfigGroup("slayerstreak")
public interface SlayerStreakConfig extends Config
{
    @ConfigSection(
            name = "General",
            description = "Core streak and milestone settings.",
            position = 0
    )
    String generalSection = "general";

    @ConfigSection(
            name = "Misc",
            description = "Infobox and notification settings.",
            position = 1
    )
    String miscSection = "misc";

    @ConfigItem(
            keyName = "milestoneInterval",
            name = "Milestone interval",
            description = "How often you want to be reminded to go to Konar.",
            section = generalSection,
            position = 0
    )
    default MilestoneInterval milestoneInterval()
    {
        return MilestoneInterval.EVERY_50;
    }

    @ConfigItem(
            keyName = "hideNonKonarAtMilestone",
            name = "Hide non-Konar at milestone",
            description = "Hides non-Konar Slayer masters at chosen milestone interval.",
            section = generalSection,
            position = 1
    )
    default boolean hideNonKonarAtMilestone()
    {
        return true;
    }

    @ConfigItem(
            keyName = "hideNonKonarNpcContact",
            name = "Hide non-Konar (NPC Contact)",
            description = "Hides non-Konar Slayer masters in NPC Contact menu at chosen milestone interval.",
            section = generalSection,
            position = 2
    )
    default boolean hideNonKonarNpcContact()
    {
        return true;
    }

    @ConfigItem(
            keyName = "highlightNonKonarAtMilestone",
            name = "Highlight non-Konar on milestone",
            description = "Outlines non-Konar Slayer masters in red at chosen milestone interval.",
            section = generalSection,
            position = 3
    )
    default boolean highlightNonKonarAtMilestone()
    {
        return false;
    }

    @ConfigItem(
            keyName = "wildySlayerStreak",
            name = "Wilderness Slayer streak",
            description = "Show your wilderness slayer streak instead of your normal one (Milestone interval and restricting slayer masters options will not work with this on).",
            section = generalSection,
            position = 3
    )
    default boolean wildySlayerStreak()
    {
        return false;
    }

    @ConfigItem(
            keyName = "infoboxTimeout",
            name = "Infobox timeout",
            description = "How long until the infobox disappears when doing non-Slayer activities. Input 0 for it to never disappear.",
            section = miscSection,
            position = 0
    )
    @Units(Units.MINUTES)
    default int infoboxTimeout()
    {
        return 10;
    }

    @ConfigItem(
            keyName = "desktopNotification",
            name = "Desktop notification",
            description = "Sends a desktop notification when your next task will be a milestone, so you don't miss it even if the game window isn't focused.",
            section = miscSection,
            position = 1
    )
    default boolean desktopNotification()
    {
        return true;
    }
}
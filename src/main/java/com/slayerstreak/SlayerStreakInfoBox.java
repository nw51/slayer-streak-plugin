package com.slayerstreak;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.infobox.InfoBox;

class SlayerStreakInfoBox extends InfoBox
{
    private final SlayerStreakPlugin plugin;

    SlayerStreakInfoBox(BufferedImage image, SlayerStreakPlugin plugin)
    {
        super(image, plugin);
        this.plugin = plugin;
    }

    @Override
    public String getText()
    {
        return String.valueOf(plugin.isWildernessMode() ? plugin.getWildyStreak() : plugin.getStreak());
    }

    @Override
    public Color getTextColor()
    {
        if (plugin.isWildernessMode())
        {
            return Color.GREEN;
        }

        return plugin.isOneTaskFromMilestone() ? Color.RED : Color.GREEN;
    }

    @Override
    public String getTooltip()
    {
        if (plugin.isWildernessMode())
        {
            return "Wilderness task streak: " + plugin.getWildyStreak()
                    + "</br>Slayer points: " + plugin.getPoints();
        }

        return "Task streak: " + plugin.getStreak()
                + "</br>" + plugin.getTasksUntilNextMilestone() + " tasks until next milestone (" + plugin.getNextMilestone() + ")"
                + "</br>Slayer points: " + plugin.getPoints();
    }
}
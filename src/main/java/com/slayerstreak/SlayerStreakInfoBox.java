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

    private static final String LABEL = "<col=ffff00>";
    private static final String VALUE = "<col=ffffff>";

    @Override
    public String getTooltip()
    {
        if (plugin.isWildernessMode())
        {
            return LABEL + "Wilderness Streak: " + VALUE + plugin.getWildyStreak()
                    + "</br>" + LABEL + "Points: " + VALUE + plugin.getPoints();
        }

        return LABEL + "Streak: " + VALUE + plugin.getStreak()
                + "</br>" + LABEL + "Next milestone: " + VALUE + plugin.getNextMilestone() + " (" + plugin.getTasksUntilNextMilestone() + ")"
                + "</br>" + LABEL + "Points: " + VALUE + plugin.getPoints();
    }
}
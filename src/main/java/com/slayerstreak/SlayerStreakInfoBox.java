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
        return String.valueOf(plugin.getStreak());
    }

    @Override
    public Color getTextColor()
    {
        int streak = plugin.getStreak();
        if ((streak + 1) % 50 == 0)
        {
            return Color.RED;
        }
        return Color.GREEN;
    }

    @Override
    public String getTooltip()
    {
        return "Task streak: " + plugin.getStreak();
    }
}
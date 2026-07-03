package com.slayerstreak;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@PluginDescriptor(
        name = "Slayer Streak",
        description = "Displays your current Slayer task streak in an infobox",
        tags = {"slayer", "streak", "combat", "infobox", "task"}
)
public class SlayerStreakPlugin extends Plugin
{
    private static final String SLAYER_CONFIG_GROUP = "slayer";
    private static final String STREAK_CONFIG_KEY = "streak";

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private SpriteManager spriteManager;

    private SlayerStreakInfoBox infoBox;
    private int streak = 0;

    @Override
    protected void startUp()
    {
        streak = getCurrentStreak();

        BufferedImage blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        infoBox = new SlayerStreakInfoBox(blankImage, this);
        infoBoxManager.addInfoBox(infoBox);
    }

    @Override
    protected void shutDown()
    {
        infoBoxManager.removeInfoBox(infoBox);
        infoBox = null;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!SLAYER_CONFIG_GROUP.equals(event.getGroup()) || !STREAK_CONFIG_KEY.equals(event.getKey()))
        {
            return;
        }

        streak = getCurrentStreak();
    }

    private int getCurrentStreak()
    {
        String value = configManager.getRSProfileConfiguration(SLAYER_CONFIG_GROUP, STREAK_CONFIG_KEY);
        if (value == null || value.isEmpty())
        {
            return 0;
        }

        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    int getStreak()
    {
        return streak;
    }
}
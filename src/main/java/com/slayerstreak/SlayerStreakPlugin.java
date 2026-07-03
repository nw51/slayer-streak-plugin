package com.slayerstreak;

import java.awt.image.BufferedImage;
import com.google.inject.Provides;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
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
    private static final int NPC_CONTACT_GROUP_ID = 75;
    private static final int[] SLAYER_MASTER_CHILD_IDS = { 20, 23, 26, 29, 32, 35, 38, 68 }; // all except Konar (65)

    @Inject
    private SlayerStreakConfig config;

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private ItemManager itemManager;

    private SlayerStreakInfoBox infoBox;
    private int streak = 0;

    @Provides
    SlayerStreakConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(SlayerStreakConfig.class);
    }

    @Override
    protected void startUp()

    {
        BufferedImage cape = itemManager.getImage(9787); // Slayer cape (t)
        infoBox = new SlayerStreakInfoBox(cape, this);
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

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)

    {
        if (event.getGroupId() != NPC_CONTACT_GROUP_ID)
        {
            return;
        }

        boolean nearMilestone = config.hideSlayerMasters() && (streak + 1) % 50 == 0;


        for (int childId : SLAYER_MASTER_CHILD_IDS)
        {
            Widget widget = client.getWidget(NPC_CONTACT_GROUP_ID, childId);
            if (widget != null)
            {
                widget.setHidden(nearMilestone);
            }
        }
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
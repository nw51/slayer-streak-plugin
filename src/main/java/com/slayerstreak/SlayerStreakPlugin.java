package com.slayerstreak;

import net.runelite.client.callback.ClientThread;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Set;
import javax.inject.Inject;

import com.google.inject.Provides;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
        name = "Slayer Streak",
        description = "Displays your current Slayer task streak in an infobox",
        tags = {"slayer", "streak", "combat", "infobox", "task"}
)
public class SlayerStreakPlugin extends Plugin
{
    // -- NPC Contact interface --
    private static final int NPC_CONTACT_GROUP_ID = 75;
    private static final int[] SLAYER_MASTER_CHILD_IDS = { 20, 23, 26, 29, 32, 35, 38, 68 }; // all except Konar (65)

    // -- In-person slayer masters (includes alternate forms each master can appear as) --
    private static final Set<String> RESTRICTED_SLAYER_MASTERS = Set.of(
            "Turael", "Aya",
            "Spria",
            "Mazchna", "Achtryn",
            "Vannaka",
            "Chaeldar",
            "Nieve", "Steve",
            "Duradel", "Kuradal",
            "Krystilia"
    );
    private static final Set<String> BLOCKED_OPTIONS = Set.of("Talk-to", "Assignment");

    @Inject
    private ClientThread clientThread;

    @Inject
    private Client client;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private SlayerStreakConfig config;

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
        clientThread.invoke(() -> streak = client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED));

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
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            streak = client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED);
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (event.getVarbitId() != VarbitID.SLAYER_TASKS_COMPLETED)
        {
            return;
        }

        streak = client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        if (event.getGroupId() != NPC_CONTACT_GROUP_ID)
        {
            return;
        }

        boolean hide = nearMilestone();

        for (int childId : SLAYER_MASTER_CHILD_IDS)
        {
            Widget widget = client.getWidget(NPC_CONTACT_GROUP_ID, childId);
            if (widget != null)
            {
                widget.setHidden(hide);
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!nearMilestone())
        {
            return;
        }

        if (!BLOCKED_OPTIONS.contains(event.getOption()))
        {
            return;
        }

        String npcName = Text.removeTags(event.getTarget());
        if (!RESTRICTED_SLAYER_MASTERS.contains(npcName))
        {
            return;
        }

        MenuEntry[] entries = client.getMenuEntries();
        client.setMenuEntries(Arrays.copyOf(entries, entries.length - 1));
    }

    private boolean nearMilestone()
    {
        if (!config.hideSlayerMasters())
        {
            return false;
        }

        int interval = config.milestoneInterval().getValue();
        return (streak + 1) % interval == 0;
    }

    int getStreak()
    {
        return streak;
    }

    int getMilestoneInterval()
    {
        return config.milestoneInterval().getValue();
    }
}
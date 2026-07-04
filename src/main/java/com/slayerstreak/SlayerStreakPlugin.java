package com.slayerstreak;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import javax.inject.Inject;

import com.google.inject.Provides;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
        name = "Slayer Streak",
        description = "Displays your Slayer task streak and can restrict Slayer Master interactions (includes NPC Contact) near milestones to prevent accidental streak loss. Optional Wilderness Slayer streak tracker included.",
        tags = {"slayer", "streak", "combat", "infobox", "task", "milestone", "wilderness", "points"}
)
public class SlayerStreakPlugin extends Plugin
{
    // -- NPC Contact interface --
    private static final int NPC_CONTACT_GROUP_ID = 75;
    private static final int[] SLAYER_MASTER_CHILD_IDS = { 20, 23, 26, 29, 32, 35, 38, 68 }; // all except Konar (65)

    // -- In-person slayer masters (includes alternate forms each master can appear as) --
    static final Set<String> RESTRICTED_SLAYER_MASTERS = Set.of(
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

    private static final String MILESTONE_REMINDER_TEXT = "Make sure to visit Konar for your next task!";
    private static final String STREAK_RESET_TEXT = "Your Slayer task streak has been reset to 0.";
    private static final Duration RESET_MESSAGE_COOLDOWN = Duration.ofHours(24);

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private SlayerStreakOverlay overlay;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private Notifier notifier;

    @Inject
    private SlayerStreakConfig config;

    private SlayerStreakInfoBox infoBox;
    private int streak = 0;
    private int wildyStreak = 0;
    private int points = 0;

    private boolean infoboxVisible = false;
    private Instant lastActivity;
    private Instant lastResetMessage;

    @Provides
    SlayerStreakConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(SlayerStreakConfig.class);
    }

    @Override
    protected void startUp()
    {
        clientThread.invoke(() ->
        {
            streak = client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED);
            wildyStreak = client.getVarbitValue(VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED);
            points = client.getVarbitValue(VarbitID.SLAYER_POINTS);
        });

        BufferedImage cape = itemManager.getImage(9787); // Slayer cape (t)
        infoBox = new SlayerStreakInfoBox(cape, this);
        infoBoxManager.addInfoBox(infoBox);
        infoboxVisible = true;
        lastActivity = Instant.now();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        infoBoxManager.removeInfoBox(infoBox);
        infoBox = null;
        infoboxVisible = false;
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            streak = client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED);
            wildyStreak = client.getVarbitValue(VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED);
            points = client.getVarbitValue(VarbitID.SLAYER_POINTS);
            registerActivity();
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        int varbitId = event.getVarbitId();

        if (varbitId == VarbitID.SLAYER_TASKS_COMPLETED)
        {
            int newStreak = client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED);

            if (streak > 0 && newStreak == 0)
            {
                announceStreakReset();
            }

            streak = newStreak;
            registerActivity();

            if (!config.wildySlayerStreak() && isOneTaskFromMilestone())
            {
                sendMilestoneReminder();
            }
        }
        else if (varbitId == VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED)
        {
            wildyStreak = client.getVarbitValue(VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED);
            registerActivity();
        }
        else if (varbitId == VarbitID.SLAYER_POINTS)
        {
            points = client.getVarbitValue(VarbitID.SLAYER_POINTS);
            registerActivity();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        if (event.getSkill() == Skill.SLAYER)
        {
            registerActivity();
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        int timeout = config.infoboxTimeout();

        if (!infoboxVisible || timeout == 0 || lastActivity == null)
        {
            return;
        }

        if (Duration.between(lastActivity, Instant.now()).compareTo(Duration.ofMinutes(timeout)) >= 0)
        {
            infoBoxManager.removeInfoBox(infoBox);
            infoboxVisible = false;
        }
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

    private void registerActivity()
    {
        lastActivity = Instant.now();

        if (!infoboxVisible && infoBox != null)
        {
            infoBoxManager.addInfoBox(infoBox);
            infoboxVisible = true;
        }
    }

    private void sendMilestoneReminder()
    {
        String message = new ChatMessageBuilder()
                .append(Color.MAGENTA, MILESTONE_REMINDER_TEXT)
                .build();

        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(message)
                .build());

        if (config.desktopNotification())
        {
            notifier.notify(MILESTONE_REMINDER_TEXT);
        }
    }

    private void announceStreakReset()
    {
        Instant now = Instant.now();

        if (lastResetMessage != null && Duration.between(lastResetMessage, now).compareTo(RESET_MESSAGE_COOLDOWN) < 0)
        {
            return;
        }

        lastResetMessage = now;

        String message = new ChatMessageBuilder()
                .append(Color.RED, STREAK_RESET_TEXT)
                .build();

        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(message)
                .build());
    }

    private boolean nearMilestone()
    {
        if (config.wildySlayerStreak())
        {
            return false;
        }

        return config.hideSlayerMasters() && isOneTaskFromMilestone();
    }

    boolean isOneTaskFromMilestone()
    {
        int interval = config.milestoneInterval().getValue();
        return (streak + 1) % interval == 0;
    }

    boolean isWildernessMode()
    {
        return config.wildySlayerStreak();
    }

    int getStreak()
    {
        return streak;
    }

    int getWildyStreak()
    {
        return wildyStreak;
    }

    int getPoints()
    {
        return points;
    }

    int getNextMilestone()
    {
        int interval = config.milestoneInterval().getValue();
        return ((streak / interval) + 1) * interval;
    }

    int getTasksUntilNextMilestone()
    {
        return getNextMilestone() - streak;
    }
}
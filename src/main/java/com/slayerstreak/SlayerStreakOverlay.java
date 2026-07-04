package com.slayerstreak;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

class SlayerStreakOverlay extends Overlay
{
    private static final String KONAR_NAME = "Konar quo Maten";
    private static final String KRYSTILIA_NAME = "Krystilia";
    private static final int OUTLINE_WIDTH = 2;
    private static final int OUTLINE_FEATHER = 2;

    private final Client client;
    private final SlayerStreakPlugin plugin;
    private final SlayerStreakConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private SlayerStreakOverlay(Client client, SlayerStreakPlugin plugin, SlayerStreakConfig config, ModelOutlineRenderer modelOutlineRenderer)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.wildySlayerStreak())
        {
            for (NPC npc : client.getTopLevelWorldView().npcs())
            {
                if (KRYSTILIA_NAME.equals(npc.getName()))
                {
                    modelOutlineRenderer.drawOutline(npc, OUTLINE_WIDTH, Color.BLACK, OUTLINE_FEATHER);
                }
            }

            return null;
        }

        if (!plugin.isOneTaskFromMilestone())
        {
            return null;
        }

        for (NPC npc : client.getTopLevelWorldView().npcs())
        {
            String name = npc.getName();
            if (name == null)
            {
                continue;
            }

            if (SlayerStreakPlugin.RESTRICTED_SLAYER_MASTERS.contains(name))
            {
                modelOutlineRenderer.drawOutline(npc, OUTLINE_WIDTH, Color.RED, OUTLINE_FEATHER);
            }
            else if (KONAR_NAME.equals(name))
            {
                modelOutlineRenderer.drawOutline(npc, OUTLINE_WIDTH, Color.GREEN, OUTLINE_FEATHER);
            }
        }

        return null;
    }
}
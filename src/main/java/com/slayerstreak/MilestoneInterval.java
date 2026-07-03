package com.slayerstreak;

public enum MilestoneInterval
{
    EVERY_10("Every 10th", 10),
    EVERY_50("Every 50th", 50),
    EVERY_100("Every 100th", 100),
    EVERY_250("Every 250th", 250);

    private final String label;
    private final int value;

    MilestoneInterval(String label, int value)
    {
        this.label = label;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return label;
    }

    public int getValue()
    {
        return value;
    }
}
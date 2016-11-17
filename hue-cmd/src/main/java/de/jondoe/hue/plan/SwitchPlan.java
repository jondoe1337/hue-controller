package de.jondoe.hue.plan;

import de.jondoe.hue.CommonColors;

public class SwitchPlan
{
    private final int startingHour;
    private final int endHour;
    private final int startingMinute;
    private final int endMinute;
    private final CommonColors color;
    private final boolean on;

    public SwitchPlan(int startingHour, int endHour, int startingMinute, int endMinute, CommonColors color, boolean on)
    {
        this.startingHour = startingHour;
        this.endHour = endHour;
        this.startingMinute = startingMinute;
        this.endMinute = endMinute;
        this.color = color;
        this.on = on;
    }

    public int getStartingHour()
    {
        return startingHour;
    }

    public int getEndHour()
    {
        return endHour;
    }

    public int getStartingMinute()
    {
        return startingMinute;
    }

    public int getEndMinute()
    {
        return endMinute;
    }

    public CommonColors getColor()
    {
        return color;
    }

    public boolean isOn()
    {
        return on;
    }

}

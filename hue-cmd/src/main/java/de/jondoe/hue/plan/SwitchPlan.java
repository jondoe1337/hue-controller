package de.jondoe.hue.plan;

import de.jondoe.hue.CommonColors;
import de.jondoe.hue.LightState;

public class SwitchPlan
{
    private final int startHour;
    private final int endHour;
    private final int startMinute;
    private final int endMinute;
    private final CommonColors color;
    private final LightState state;
    private final String lightId;

    public SwitchPlan(String lightId, int startHour, int endHour, int startMinute, int endMinute, CommonColors color, LightState state)
    {
        this.lightId = lightId;
        this.startHour = startHour;
        this.endHour = endHour;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.color = color;
        this.state = state;
    }

    public int getStartHour()
    {
        return startHour;
    }

    public int getEndHour()
    {
        return endHour;
    }

    public int getStartMinute()
    {
        return startMinute;
    }

    public int getEndMinute()
    {
        return endMinute;
    }

    public CommonColors getColor()
    {
        return color;
    }

    public LightState getState()
    {
        return state;
    }

    public String getLightId()
    {
        return lightId;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + endHour;
        result = prime * result + endMinute;
        result = prime * result + ((lightId == null) ? 0 : lightId.hashCode());
        result = prime * result + startHour;
        result = prime * result + startMinute;
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SwitchPlan other = (SwitchPlan) obj;
        if (color != other.color)
            return false;
        if (endHour != other.endHour)
            return false;
        if (endMinute != other.endMinute)
            return false;
        if (lightId == null)
        {
            if (other.lightId != null)
                return false;
        }
        else if (!lightId.equals(other.lightId))
            return false;
        if (startHour != other.startHour)
            return false;
        if (startMinute != other.startMinute)
            return false;
        if (state == null)
        {
            if (other.state != null)
                return false;
        }
        else if (!state.equals(other.state))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "SwitchPlan [startHour=" + startHour + ", endHour=" + endHour + ", startMinute=" + startMinute + ", endMinute=" + endMinute
                + ", color=" + color + ", state=" + state + ", lightId=" + lightId + "]";
    }

}

package de.jondoe.hue;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

public class LightState
{
    private static final String ERROR_MESSAGE = "Lightstate should be an integer of 0-255";
    public static final Integer MAX = 254;
    private static final Range<Integer> VALID_RANGE = Range.closedOpen(0, MAX);

    public static enum CommonState
    {
     ON, OFF, DIMMED;
    }

    private LightState()
    {
        /* use static factory method */
    }

    private int lightstate;

    public CommonState getCommonState()
    {
        if (lightstate <= 0)
        {
            return CommonState.OFF;
        }
        else if (lightstate >= MAX)
        {
            return CommonState.ON;
        }
        else
        {
            return CommonState.DIMMED;
        }
    }

    public int getBrightness()
    {
        return lightstate;
    }

    public static LightState from(String state)
    {
        Preconditions.checkNotNull(state, ERROR_MESSAGE);
        int integerState = Integer.valueOf(state);
        if (!VALID_RANGE.contains(integerState))
        {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
        return new LightState();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + lightstate;
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
        LightState other = (LightState) obj;
        if (lightstate != other.lightstate)
            return false;
        return true;
    }
}

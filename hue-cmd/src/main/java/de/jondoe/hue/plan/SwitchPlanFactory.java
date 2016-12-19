package de.jondoe.hue.plan;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import com.google.common.base.Preconditions;

import de.jondoe.hue.CommonColors;
import de.jondoe.hue.LightState;

public class SwitchPlanFactory
{

    private static final String DEFAULT_FILENAME = System.getProperty("basedir") + "conf/plans.xml";
    private static final String XML_PLAN = "plan";
    private static final String XML_START_HOUR = "starthour";
    private static final String XML_END_HOUR = "endhour";
    private static final String XML_START_MINUTE = "startminute";
    private static final String XML_END_MINUTE = "endminute";
    private static final String XML_COLOR = "color";
    private static final String XML_STATE = "state";
    private static final String XML_LIGHTID = "lightid";

    @SuppressWarnings("unchecked")
    public static List<SwitchPlan> readSwitchPlansFromXml()
    {
        try
        {
            XMLConfiguration xmlConfig = new XMLConfiguration(DEFAULT_FILENAME);
            List<HierarchicalConfiguration> xmlPlans = xmlConfig.configurationsAt(XML_PLAN);
            Preconditions.checkArgument(xmlPlans.size() > 0, "The 'schema' tags could not be found!");
            List<SwitchPlan> returnValue = new ArrayList<>();
            for (HierarchicalConfiguration plan : xmlPlans)
            {
                String lightId = plan.getString(XML_LIGHTID, null);
                Integer startHour = plan.getInteger(XML_START_HOUR, null);
                Integer endHour = plan.getInteger(XML_END_HOUR, null);
                Integer startMinute = plan.getInteger(XML_START_MINUTE, null);
                Integer endMinute = plan.getInteger(XML_END_MINUTE, null);
                String colorString = plan.getString(XML_COLOR, null);
                String stateString = plan.getString(XML_STATE, null);
                Preconditions.checkNotNull(startHour, XML_START_HOUR + " must not be empty!");
                Preconditions.checkNotNull(endHour, XML_END_HOUR + " must not be empty!");
                Preconditions.checkNotNull(startMinute, XML_START_MINUTE + " must not be empty!");
                Preconditions.checkNotNull(endMinute, XML_END_MINUTE + " must not be empty!");
                Preconditions.checkNotNull(colorString, XML_COLOR + " must not be empty!");
                Preconditions.checkNotNull(stateString, XML_STATE + " must not be empty!");
                Preconditions.checkNotNull(lightId, XML_LIGHTID + " must not be empty!");
                Preconditions.checkArgument(startHour < endHour, "Star-Hour must be before End-Hour");
                Preconditions.checkArgument(startMinute < endMinute, "Star-Minute must be before End-Minute");

                CommonColors color = CommonColors.valueOf(colorString.toUpperCase());
                LightState state = LightState.from(stateString);
                SwitchPlan newPlan = new SwitchPlan(lightId, startHour, endHour, startMinute, endMinute, color, state);
                returnValue.add(newPlan);
            }
            return returnValue;
        }
        catch (ConfigurationException e)
        {
            throw new IllegalStateException("The conf/plans.xml could not be read.");
        }
    }
}

package net.sourceforge.cruisecontrol.publishers;

public class KrazipOverrideGlobalLogging {

    private static String overrideValue = "nothing";

    public static String getOverrideValue() {
        return overrideValue;
    }

    public static void setOverrideValue(String overrideValue) {
        KrazipOverrideGlobalLogging.overrideValue = overrideValue;
    }
}
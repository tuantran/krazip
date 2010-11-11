package net.sourceforge.cruisecontrol.publishers;

public final class KrazipOverrideGlobalLogging {

    private static String overrideValue = "nothing";

    private KrazipOverrideGlobalLogging() {

    }

    protected static String getOverrideValue() {
        return overrideValue;
    }

    protected static void setOverrideValue(String overrideValue) {
        KrazipOverrideGlobalLogging.overrideValue = overrideValue;
    }
}
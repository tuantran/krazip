package net.sourceforge.cruisecontrol.publishers;

public final class KrazipOverrideGlobalLogging {

    private static String overrideValue = "nothing";

    protected final static String getOverrideValue() {
        return overrideValue;
    }

    protected final static void setOverrideValue(String overrideValue) {
        KrazipOverrideGlobalLogging.overrideValue = overrideValue;
    }
}
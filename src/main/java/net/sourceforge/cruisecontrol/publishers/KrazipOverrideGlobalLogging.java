package net.sourceforge.cruisecontrol.publishers;

/**
 * This is a utility class for overriding logging level from the file <code>config.xml</code>. We have to make sure there is only one
 * <code>overrideValue</code> in Krazip so we have to make it <code>static</code>
 */
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
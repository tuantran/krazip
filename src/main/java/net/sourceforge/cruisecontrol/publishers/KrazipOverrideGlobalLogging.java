/*
 * Copyright 2010 ABC Tech Ltd. (Thailand)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
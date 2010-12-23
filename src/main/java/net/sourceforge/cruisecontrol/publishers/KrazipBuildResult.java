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
 * This is just a bean class for keeping build result information. This bean will be stored in <code>krazipBuildList</code> list. 
 */
public class KrazipBuildResult {

        private String projectName;
        private String message;
        private String timeStamp;

        public KrazipBuildResult() {

        }

        public KrazipBuildResult(String projectName, String message, String timeStamp) {
            this.projectName = projectName;
            this.message = message;
            this.timeStamp = timeStamp;
        }

        public final String getProjectName() {
            return projectName;
        }

        public final void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public final String getTimeStamp() {
            return timeStamp;
        }

        public final void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public final String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

}
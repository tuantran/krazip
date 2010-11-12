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
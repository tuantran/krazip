package net.sourceforge.cruisecontrol.publishers;

public class BuildResult{

        private String projectName;
        private String message;
        private String timeStamp;

        public BuildResult() {

        }

        public BuildResult(String projectName, String message, String timeStamp) {
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
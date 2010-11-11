package net.sourceforge.cruisecontrol.publishers;

public class KrazipFollowProject {

    private String projectName;
    private String follower;

    public KrazipFollowProject(){

    }

    public KrazipFollowProject(String projectName, String follower){
        this.projectName = projectName;
        this.follower = follower;
    }

    public String getFollower() {
        return follower;
    }

    public void setFollower(String follower) {
        this.follower = follower;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

}

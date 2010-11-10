package net.sourceforge.cruisecontrol.publishers;

public class FollowProject {

    private String projectName;
    private String follower;

    public FollowProject(){

    }

    public FollowProject(String projectName, String follower){
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

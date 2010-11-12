package net.sourceforge.cruisecontrol.publishers;

/**
 * This is just a bean for keeping information about who following which project. This bean will be stored in <code>krazipFollowList</code> list
 */
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

/*
 * Copyright 2010 ABC Tech Ltd. (Thailand) / APDM (A-pressen digitale medier, Norway)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.sourceforge.cruisecontrol.publishers;

/**
 * This is just a bean for keeping information about who following which
 * project. This bean will be stored in <code>krazipFollowList</code> list
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

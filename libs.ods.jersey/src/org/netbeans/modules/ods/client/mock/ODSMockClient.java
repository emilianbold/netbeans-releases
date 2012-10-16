/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ods.client.mock;

import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.BuildSummary;
import com.tasktop.c2c.server.profile.domain.build.HudsonStatus;
import com.tasktop.c2c.server.profile.domain.build.JobDetails;
import com.tasktop.c2c.server.profile.domain.build.JobSummary;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codeviation.pojson.PojsonLoad;
import org.netbeans.modules.ods.client.api.ODSException;
import org.netbeans.modules.ods.client.api.ODSClient;
import org.openide.util.Exceptions;

public final class ODSMockClient implements ODSClient {

    private Profile currentProfile;
    private List<Project> projects = new ArrayList<Project>();
    private Map<String, HudsonStatus> hudsonStatuses = new HashMap<String, HudsonStatus>();
    private Map<String, List<ProjectActivity>> recentActivities = new HashMap<String, List<ProjectActivity>>();
    private File rootFolder;
    private final PojsonLoad jsonLoad;

    public ODSMockClient(String url) {
        jsonLoad = PojsonLoad.create();
        rootFolder = new File(url);
        loadData();        
    }

    @Override
    public BuildDetails getBuildDetails(String projectId, String jobName, int buildNumber) throws ODSException {
        List<JobSummary> jobs = getHudsonStatus(projectId).getJobs();
        for (JobSummary jobSummary : jobs) {
            if (jobSummary.getName().equals(jobName)) {
                List<BuildDetails> builds = jobSummary.getBuilds();
                for (BuildDetails buildDetails : builds) {
                    if (buildDetails.getNumber().equals(new Integer(buildNumber))) {
                        return buildDetails;
                    }
                }
            }
        }
        return new BuildDetails();
    }

    @Override
    public Profile getCurrentProfile() throws ODSException {
        return currentProfile;
    }

    @Override
    public HudsonStatus getHudsonStatus(String projectId) throws ODSException {
        return hudsonStatuses.get(projectId);
    }

    @Override
    public JobDetails getJobDetails(String projectId, String jobName) throws ODSException {
        List<JobSummary> jobs = getHudsonStatus(projectId).getJobs();
        for (JobSummary jobSummary : jobs) {
            if (jobSummary.getName().equals(jobName)) {
                return convertToJobDetails(jobSummary);
            }
        }
        return new JobDetails();
    }

    @Override
    public List<Project> getMyProjects() throws ODSException {
        return projects;
    }

    @Override
    public Project getProjectById(String projectId) throws ODSException {
        for (Project project : projects) {
            if (project.getIdentifier().equals(projectId)) {
                return project;
            }
        }
        return new Project();
    }

    @Override
    public List<ProjectActivity> getRecentActivities(String projectId) throws ODSException {
        return recentActivities.get(projectId);
    }

    @Override
    public List<ProjectActivity> getRecentShortActivities(String projectId) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWatchingProject(String projectId) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Project> searchProjects(String pattern) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unwatchProject(String projectId) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void watchProject(String projectId) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Project> getWatchedProjects () throws ODSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Project createProject (Project project) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SavedTaskQuery createQuery(String projectId, SavedTaskQuery query) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SavedTaskQuery updateQuery(String projectId, SavedTaskQuery query) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteQuery(String projectId, Integer queryId) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RepositoryConfiguration getRepositoryContext(String projectId) throws ODSException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private JobDetails convertToJobDetails(JobSummary jobSummary) {
        JobDetails jobDetails = new JobDetails();

        List<BuildSummary> buildSummaries = new ArrayList<BuildSummary>(jobSummary.getBuilds().size());
        for (BuildDetails buildDetails : jobSummary.getBuilds()) {
            buildSummaries.add(convertToBuildSummary(buildDetails));
        }
        jobDetails.setBuilds(buildSummaries);

        jobDetails.setColor(jobSummary.getColor());
        jobDetails.setName(jobSummary.getName());
        jobDetails.setUrl(jobSummary.getUrl());
        return jobDetails;
    }

    private BuildSummary convertToBuildSummary(BuildDetails buildDetails) {
        BuildSummary buildSummary = new BuildSummary();
        buildSummary.setNumber(buildDetails.getNumber());
        buildSummary.setUrl(buildDetails.getUrl());
        return buildSummary;
    }

    private void loadData() {
        loadProfile();
        loadProjects();
        loadHudson();
    }

    private void loadProfile() {
        File profileJson = new File(rootFolder, "profile.json");
        final FileInputStream fileInputStream;
        Profile profile = null;
        try {
            fileInputStream = new FileInputStream(profileJson);
            profile = jsonLoad.load(fileInputStream, Profile.class);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        currentProfile = profile;
    }

    private void loadProjects() {
        File projectsFolder = new File(rootFolder, "projects");
        File[] projectFiles = projectsFolder.listFiles();
        for (File projectFolder : projectFiles) {
            final FileInputStream projectIS;
            final FileInputStream servicesIS;
            Project project = null;
            ProjectService[] services = null;
            try {
                projectIS = new FileInputStream(new File(projectFolder, "project.json"));
                project = jsonLoad.load(projectIS, Project.class);
                servicesIS = new FileInputStream(new File(projectFolder, "projectServices.json"));
                services = jsonLoad.load(servicesIS, ProjectService[].class);
                project.setProjectServices(Arrays.asList(services));
                projects.add(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void loadHudson() {
        int i = 0;
        for (Project project : projects) {
            HudsonStatus hudsonStatus = new HudsonStatus();

            List<JobSummary> summaries = new ArrayList<JobSummary>();
            JobSummary summary1 = new JobSummary();
            summary1.setName("Job1");
            summary1.setColor("Red");

            List<BuildDetails> buildDetailsList = new ArrayList<BuildDetails>();
            BuildDetails buildDetails = new BuildDetails();
            buildDetails.setBuilding(Boolean.FALSE);
            buildDetails.setDuration(new Long(2000));
            buildDetails.setNumber(new Integer(i + 1));
            buildDetails.setResult(BuildDetails.BuildResult.SUCCESS);
            buildDetails.setTimestamp(System.currentTimeMillis());
            buildDetails.setUrl("");
            buildDetailsList.add(buildDetails);
            summary1.setBuilds(buildDetailsList);

            summaries.add(summary1);

            hudsonStatus.setJobs(summaries);
            hudsonStatuses.put(project.getIdentifier(), hudsonStatus);
            i++;
        }
    }
}

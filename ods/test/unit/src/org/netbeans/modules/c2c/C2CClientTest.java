/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.c2c;

import org.netbeans.modules.ods.client.api.ODSFactory;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.activity.TaskActivity;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.HudsonStatus;
import com.tasktop.c2c.server.profile.domain.build.JobDetails;
import com.tasktop.c2c.server.profile.domain.build.JobSummary;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.PasswordAuthentication;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ods.client.api.ODSClient;


/**
 *
 * @author tomas
 */
public class C2CClientTest extends NbTestCase  {

    public static Test suite() {
        return NbModuleSuite.create(C2CClientTest.class, null, null);
    }
    private static boolean firstRun = true;
    private static String uname;
    private static String passw;
    private static String proxy_host;
    private static String proxy_port;
    private static final String MY_PROJECT = "qa-dev_netbeans-test"; //NOI18N
    
    public C2CClientTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        if (firstRun) {
            if (uname == null) {
                BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-team2")));
                uname = br.readLine();
                passw = br.readLine();
                proxy_host = br.readLine();
                proxy_port = br.readLine();
                br.close();
            }
            if (firstRun) {
                firstRun = false;
            }
        }
        if (!proxy_host.isEmpty()) {
            System.setProperty("netbeans.system_http_proxy", proxy_host + ":" + (proxy_port == null ? "80" : proxy_port));
        }
    }
    
    public void testGetUserInfo () throws Exception {
        ODSClient client = getClient();
        Profile currentClient = client.getCurrentProfile();
        assertNotNull(currentClient.getFirstName());
        assertNotNull(currentClient.getLastName());
        assertNotNull(currentClient.getEmail());
        assertEquals(uname, currentClient.getUsername());
    }
    
    public void testGetMyProjects () throws Exception {
        ODSClient client = getClient();
        List<Project> projects = client.getMyProjects();
        assertNotNull(projects);
        assertFalse(projects.isEmpty());
        // anagram game should be there
        Project myProject = null;
        for (Project p : projects) {
            if (MY_PROJECT.equals(p.getIdentifier())) {
                myProject = p;
                break;
            }
        }
        assertNotNull(myProject);
    }
    
    public void testGetProjectById () throws Exception {
        ODSClient client = getClient();
        Project project = client.getProjectById(MY_PROJECT);
        assertNotNull(project);
        assertEquals(MY_PROJECT, project.getIdentifier());
    }
    
    public void testSearchProjects () throws Exception {
        ODSClient client = getClient();
        for (String pattern : new String[] { "netbeans", "dummy", "testing", "nb PROJECT" }) {
            List<Project> projects = client.searchProjects(pattern);
            assertNotNull(projects);
            assertFalse(projects.isEmpty());
            // anagram game should be there
            Project myProject = null;
            for (Project p : projects) {
                if (MY_PROJECT.equals(p.getIdentifier())) {
                    myProject = p;
                    break;
                }
            }
            assertNotNull(myProject);
        }
    }

    public void testGetProjectServices () throws Exception {
        ODSClient client = getClient();
        Project project = client.getProjectById(MY_PROJECT);
        assertNotNull(project);
        assertEquals(MY_PROJECT, project.getIdentifier());
        List<ProjectService> services = project.getProjectServices();
        Set<ServiceType> expectedServices = EnumSet.of(ServiceType.SCM, ServiceType.TASKS, ServiceType.WIKI, ServiceType.BUILD);
        for (ProjectService s : services) {
            if (expectedServices.remove(s.getServiceType())) {
                assertNotNull(s.getUrl());
                assertTrue(s.getServiceType().name(), s.isAvailable());
            }
        }
        assertTrue(expectedServices.isEmpty());
    }
    
    public void testWatchUnwatchProject () throws Exception {
        ODSClient client = getClient();
        String projectIdent = "qa-dev_getting-started";
        client.unwatchProject(projectIdent);
        assertFalse(client.isWatchingProject(projectIdent));
        List<Project> watchedProjects = client.getWatchedProjects();
        assertTrue(watchedProjects.isEmpty());
        client.watchProject(projectIdent);
        watchedProjects = client.getWatchedProjects();
        assertFalse(watchedProjects.isEmpty());
        assertTrue(client.isWatchingProject(projectIdent));
        client.unwatchProject(projectIdent);
        assertFalse(client.isWatchingProject(projectIdent));
        watchedProjects = client.getWatchedProjects();
        assertTrue(watchedProjects.isEmpty());
    }
    
    public void testGetRecentActivities () throws Exception {
        ODSClient client = getClient();
        Project project = client.getProjectById(MY_PROJECT);
        List<ProjectActivity> shortActivities = client.getRecentShortActivities(project.getIdentifier());
        assertNotNull(shortActivities);
        assertTrue(shortActivities.size() > 0);
        List<ProjectActivity> activities = client.getRecentActivities(project.getIdentifier());
        assertNotNull(activities);
        assertTrue(activities.size() > 0);
        // is it the same??
        for (int i = 0; i < shortActivities.size(); ++i) {
            ProjectActivity a1 = shortActivities.get(i);
            ProjectActivity a2 = activities.get(i);
            assertEquals(a1.getActivityDate(), a2.getActivityDate());
            assertEquals(a1.getProjectIdentifier(), a2.getProjectIdentifier());
            assertEquals(a1.getClass(), a2.getClass());
            if (a1 instanceof TaskActivity) {
                assertActivity((TaskActivity) a1, (TaskActivity) a2);
            }
        }
    }
    
    public void testGetHudsonStatus () throws Exception {
        ODSClient client = getClient();
        Project project = client.getProjectById(MY_PROJECT);
        HudsonStatus status = client.getHudsonStatus(project.getIdentifier());
        assertNotNull(status);
        assertTrue(status.getJobs().size() > 0);
        for (JobSummary summary : status.getJobs()) {
            JobDetails details = client.getJobDetails(MY_PROJECT, summary.getName());
            assertNotNull(details);
            assertEquals(summary.getName(), details.getName());
            assertEquals(summary.getColor(), details.getColor());
            assertEquals(summary.getUrl(), details.getUrl());
            // XXX uncomment when running against a properly set project
            // fails for some jobs on c2c. 
//            assertNotNull(summary.getBuilds());
//            assertTrue(details.getBuilds().size() > 0);
        }
    }
    
    public void testGetBuildDetails () throws Exception {
        ODSClient client = getClient();
        Project project = client.getProjectById(MY_PROJECT);
        HudsonStatus status = client.getHudsonStatus(project.getIdentifier());
        assertNotNull(status);
        assertTrue(status.getJobs().size() > 0);
        JobDetails jobDetails = client.getJobDetails(MY_PROJECT, "Sample Maven Build");
        BuildDetails details = client.getBuildDetails(MY_PROJECT, jobDetails.getName(), jobDetails.getBuilds().get(0).getNumber());
        assertNotNull(details);
    }

    public void testGetScmRepositories () throws Exception {
        ODSClient client = getClient();
        List<ScmRepository> repositories = client.getScmRepositories(MY_PROJECT);
        assertNotNull(repositories);
        assertFalse(repositories.isEmpty());
    }
//    
//    public void testCreateDeleteProject () throws Exception {
//        ODSClient client = getClient();
//        Project project = new Project();
//        project.setName("Test Project 2");
//        project.setDescription("Netbeans testing junit project for internal purposes");
//        project.setAccessibility(ProjectAccessibility.PRIVATE);
//        ProjectPreferences prefs = new ProjectPreferences();
//        prefs.setWikiLanguage(WikiMarkupLanguage.CONFLUENCE);
//        project.setProjectPreferences(prefs);
//        Project created = client.createProject(project);
//        assertEquals(project.getName(), created.getName());
//        assertEquals(project.getDescription(), created.getDescription());
//        assertEquals(project.getAccessibility(), created.getAccessibility());
//        assertEquals(WikiMarkupLanguage.CONFLUENCE, created.getProjectPreferences().getWikiLanguage());
//        assertFalse(created.getProjectServices().isEmpty());
//    }
    
    private ODSClient getClient () {
        return ODSFactory.getInstance().createClient("http://qa-dev.developer.us.oracle.com",
                new PasswordAuthentication(uname, passw.toCharArray()));
    }

    private void assertActivity (TaskActivity a1, TaskActivity a2) {
        com.tasktop.c2c.server.tasks.domain.TaskActivity ta1 = a1.getActivity();
        com.tasktop.c2c.server.tasks.domain.TaskActivity ta2 = a2.getActivity();
        assertEquals(ta1.getActivityDate(), ta2.getActivityDate());
        assertEquals(ta1.getActivityType(), ta2.getActivityType());
        assertEquals(ta1.getAuthor().getRealname(), ta2.getAuthor().getRealname());
        assertEquals(ta1.getDescription(), ta2.getDescription());
    }

}

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
package org.netbeans.modules.ods.hudson;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
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
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.RemoteFileSystem;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.ods.api.TestUtils;
import org.netbeans.modules.ods.client.api.ODSClient;
import org.netbeans.modules.ods.client.api.ODSException;
import org.netbeans.modules.ods.client.api.ODSFactory;
import org.netbeans.modules.team.ui.spi.JobHandle;
import org.netbeans.modules.team.ui.spi.ProjectHandle;

/**
 *
 * @author jhavlin
 */
public class ODSBuilderAccessorTest {

    private ODSBuilderAccessor accessor;
    private ProjectHandle<ODSProject> projectHandle;
    private MockBuilderConnector connector;

    public ODSBuilderAccessorTest() {
    }

    @Before
    public void setUp() throws MalformedURLException, ODSException {
        MockServices.setServices(MockODSFactory.class);
        projectHandle = TestUtils.createMockHandle(
                "http://test/", "mockProject");
        connector = new MockBuilderConnector();
        HudsonInstance hi = HudsonManager.addInstance("Mock Hudson Instance",
                "http://test/hudson/", 1, connector);
        accessor = new ODSBuilderAccessor();
    }

    @Test
    public void testMocks() throws ODSException, MalformedURLException,
            MalformedURLException {

        ODSClient client = ODSFactory.getInstance().createClient(
                "http://test/", new PasswordAuthentication(
                "mockUser", "password".toCharArray()));
        assertNotNull(client);
        Project project = client.getProjectById("mockProject");
        assertEquals("Mock Test Project", project.getDescription());

        ProjectHandle<ODSProject> handle = TestUtils.createMockHandle(
                "http://test/", "mockProject");
        assertNotNull(handle);
        assertEquals("Mock Project", handle.getDisplayName());
    }

    @Test
    public void testGetJobs() {
        List<JobHandle> jobs = accessor.getJobs(projectHandle);
        assertEquals(1, jobs.size());
        assertEquals("Job1", jobs.get(0).getDisplayName());
    }

    @Test
    public void testGetJob() {
        JobHandle job = accessor.getJob(projectHandle, "Job1");
        assertEquals("Job1", job.getDisplayName());
    }

    @Test
    public void testGetNewBuildAction() {
        Action newBuildAction = accessor.getNewBuildAction(projectHandle);
        assertNotNull(newBuildAction);
    }

    @Test
    public void testType() {
        assertEquals(ODSProject.class, accessor.type());
    }

    public class MockBuilderConnector extends BuilderConnector {

        @Override
        public InstanceData getInstanceData(boolean authentication) {
            JobData jd = new JobData();
            jd.setJobName("Job1");
            jd.setJobUrl("http://test/hudson/Job1/");
            jd.setBuildable(true);
            jd.setColor(HudsonJob.Color.yellow);
            jd.setDisplayName("Job1");
            jd.setInQueue(false);
            jd.setLastBuild(2);
            jd.setLastCompletedBuild(2);
            jd.setLastStableBuild(0);
            jd.setLastSuccessfulBuild(2);
            jd.setLastFailedBuild(1);
            jd.setSecured(false);
            InstanceData id = new InstanceData(Collections.singletonList(jd),
                    Collections.<ViewData>emptyList());
            return id;
        }

        @Override
        public Collection<BuildData> getJobBuildsData(HudsonJob job) {
            if (job.getName().equals("Job1")) {
                List<BuildData> l = new LinkedList<BuildData>();
                l.add(new BuildData(1, Result.FAILURE, false));
                l.add(new BuildData(2, Result.UNSTABLE, false));
                return l;
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public void getJobBuildResult(HudsonJobBuild build,
                AtomicBoolean building, AtomicReference<Result> result) {
            for (BuildData bd : getJobBuildsData(build.getJob())) {
                if (bd.getNumber() == build.getNumber()) {
                    result.set(bd.getResult());
                    building.set(false);
                }
            }
        }

        @Override
        public RemoteFileSystem getArtifacts(HudsonJobBuild build) {
            return null;
        }

        @Override
        public RemoteFileSystem getArtifacts(HudsonMavenModuleBuild build) {
            return null;
        }

        @Override
        public RemoteFileSystem getWorkspace(HudsonJob job) {
            return null;
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public boolean isForbidden() {
            return false;
        }

        @Override
        public HudsonVersion getHudsonVersion(boolean authentication) {
            return new HudsonVersion("2.2.1");
        }

        @Override
        public void startJob(HudsonJob job) {
        }

        @Override
        public ConsoleDisplayer getConsoleDisplayer() {
            return null;
        }

        @Override
        public FailureDisplayer getFailureDisplayer() {
            return null;
        }

        @Override
        public Collection<? extends HudsonJobChangeItem> getJobBuildChanges(HudsonJobBuild build) {
            return Collections.emptyList();
        }
    }

    public class MockProjectHandle extends ProjectHandle<ODSProject> {

        public MockProjectHandle(String id) {
            super(id);
        }

        @Override
        public String getDisplayName() {
            return "ODS Mock Project";
        }

        @Override
        public ODSProject getTeamProject() {
            return null;
        }

        @Override
        public boolean isPrivate() {
            return true;
        }
    }

    public static class MockODSFactory extends ODSFactory {

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public ODSClient createClient(String url, PasswordAuthentication auth) {
            return new ODSClient() {
                @Override
                public Project createProject(Project project)
                        throws ODSException {
                    throw new UnsupportedOperationException(
                            "Not supported by mock instance.");
                }

                @Override
                public BuildDetails getBuildDetails(String projectId,
                        String jobName, int buildNumber) throws ODSException {
                    BuildDetails bd = new BuildDetails();
                    bd.setBuilding(false);
                    bd.setResult(BuildDetails.BuildResult.UNSTABLE);
                    bd.setNumber(123);
                    bd.setNumber(456);
                    bd.setTimestamp(System.currentTimeMillis());
                    return bd;
                }

                @Override
                public Profile getCurrentProfile() throws ODSException {
                    Profile p = new Profile();
                    p.setAccountDisabled(false);
                    p.setEmail("mock@email.com");
                    p.setEmailVerfied(true);
                    p.setFirstName("Mock");
                    p.setId(1L);
                    p.setLastName("User");
                    p.setPassword("password");
                    p.setUsername("mockUser");
                    return p;
                }

                @Override
                public HudsonStatus getHudsonStatus(String projectId)
                        throws ODSException {
                    HudsonStatus hs = new HudsonStatus();
                    JobSummary js = new JobSummary();
                    BuildDetails bd = new BuildDetails();
                    js.setBuilds(Collections.singletonList(bd));
                    hs.setJobs(Collections.singletonList(js));
                    return hs;
                }

                @Override
                public JobDetails getJobDetails(String projectId,
                        String jobName) throws ODSException {
                    JobDetails jd = new JobDetails();
                    jd.setName(jobName);
                    jd.setColor(Color.YELLOW.toString());
                    BuildSummary bs = new BuildSummary();
                    bs.setNumber(1);
                    bs.setUrl("http://test/hudson/" + jobName + "/" + 1);
                    jd.setBuilds(Collections.singletonList(bs));
                    return jd;
                }

                @Override
                public List<Project> getMyProjects() throws ODSException {
                    Project p = new Project();
                    p.setDescription("Mock Test Project");
                    p.setIdentifier("mockProject");
                    p.setName("Mock Project");
                    ProjectService builder = new ProjectService();
                    builder.setServiceType(ServiceType.BUILD);
                    builder.setUrl("http://test/hudson/");
                    builder.setAvailable(true);
                    builder.setId(2L);
                    p.setProjectServices(Collections.singletonList(builder));
                    return Collections.singletonList(p);
                }

                @Override
                public Project getProjectById(String projectId)
                        throws ODSException {
                    if (projectId.equals("mockProject")) {
                        return getMyProjects().get(0);
                    } else {
                        return null;
                    }
                }

                @Override
                public List<ProjectActivity> getRecentActivities(
                        String projectId) throws ODSException {
                    return Collections.emptyList();
                }

                @Override
                public List<ProjectActivity> getRecentShortActivities(
                        String projectId) throws ODSException {
                    return Collections.emptyList();
                }

                @Override
                public List<ScmRepository> getScmRepositories(String projectId)
                        throws ODSException {
                    return Collections.emptyList();
                }

                @Override
                public List<Project> getWatchedProjects() throws ODSException {
                    return Collections.emptyList();
                }

                @Override
                public boolean isWatchingProject(String projectId)
                        throws ODSException {
                    return false;
                }

                @Override
                public List<Project> searchProjects(String pattern)
                        throws ODSException {
                    return Collections.emptyList();
                }

                @Override
                public void unwatchProject(String projectId)
                        throws ODSException {
                }

                @Override
                public void watchProject(String projectId) throws ODSException {
                }
            };
        }
    }
}

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
package org.netbeans.modules.odcs.hudson;

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
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.hudson.api.HudsonChangeAdapter;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
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
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.api.TestUtils;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.client.api.ODCSFactory;
import org.netbeans.modules.team.server.ui.spi.BuildHandle;
import org.netbeans.modules.team.server.ui.spi.BuildHandle.Status;
import org.netbeans.modules.team.server.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.server.ui.spi.JobHandle;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.Lookups;

/**
 * Tests for ODCSSBuilderAccessor.
 *
 * @author jhavlin
 */
public class ODCSBuilderAccessorTest {

    private ODCSBuilderAccessor accessor;
    private ProjectHandle<ODCSProject> projectHandle;
    private MockBuilderConnector connector;
    private HudsonInstance hudsonInstance;

    public ODCSBuilderAccessorTest() {
    }

    /**
     * Set up environemtn before each test. Prepare a hudson instance for url
     * "http://test/hudson/" with mock {@link BuilderConnector}, instance of
     * {@link BuilderAccessor} and a project handle.
     */
    @Before
    public void setUp() throws MalformedURLException, ODCSException,
            InterruptedException {
        MockServices.setServices(MockODCSSFactory.class);
        projectHandle = TestUtils.createMockHandle(
                "http://test/", "mockProject");
        connector = new MockBuilderConnector();
        hudsonInstance = HudsonManager.addInstance("Mock Hudson Instance",
                "http://test/hudson/", 1, connector);
        final Semaphore s = new Semaphore(0);
        HudsonChangeListener hcListener = new HudsonChangeAdapter() {
            @Override
            public void contentChanged() {
                s.release();
            }
        };
        hudsonInstance.addHudsonChangeListener(hcListener);
        s.tryAcquire(1, TimeUnit.SECONDS);
        hudsonInstance.removeHudsonChangeListener(hcListener);
        accessor = new ODCSBuilderAccessor();
    }

    /**
     * Remove previously created hudson instance.
     */
    @After
    public void tearDown() {
        HudsonManager.removeInstance(hudsonInstance);
    }

    /**
     * Check that the job handle for mock job has correct name.
     */
    @Test
    public void testGetJobs() {
        List<JobHandle> jobs = accessor.getJobs(projectHandle);
        assertEquals(1, jobs.size());
        assertEquals("Job1", jobs.get(0).getDisplayName());
    }

    /**
     * Check that job list contains mock job with correct status.
     */
    @Test
    public void testGetJob() throws InterruptedException {
        JobHandle job = accessor.getJob(projectHandle, "Job1");
        assertEquals("Job1", job.getDisplayName());
        Status status = job.getStatus();
        if (status.equals(BuildHandle.Status.UNKNOWN)) {
            waitForInitialization(job);
            status = job.getStatus(); // get initialized status now
        }
        assertEquals(BuildHandle.Status.UNSTABLE, status);
    }

    /**
     * Check that new build action is defined.
     */
    @Test
    public void testGetNewBuildAction() {
        Action newBuildAction = accessor.getNewBuildAction(projectHandle);
        assertNotNull(newBuildAction);
    }

    /**
     * Check type of ODCSS builder accessor is correct.
     */
    @Test
    public void testType() {
        assertEquals(ODCSProject.class, accessor.type());
    }

    /**
     * Check that listener added to a job handle is notified when status of the
     * job changes.
     */
    @Test
    public void testJobStatusChangeListener() throws InterruptedException {
        JobHandle job = accessor.getJob(projectHandle, "Job1");
        if (job.getStatus().equals(BuildHandle.Status.UNKNOWN)) {
            waitForInitialization(job);
        }
        final AtomicBoolean notified = new AtomicBoolean(false);
        final Semaphore s = new Semaphore(0);
        job.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(JobHandle.PROP_STATUS)) {
                    notified.set(true);
                    s.release();
                }
            }
        });
        connector.addBuild(); // simulate finishing a new build
        syncHudsonInstance();
        s.tryAcquire(1, TimeUnit.DAYS);
        assertTrue(notified.get());
        assertEquals(BuildHandle.Status.STABLE, job.getStatus());
        assertEquals(3, job.getBuilds().size());
    }

    /**
     * Check that listener added to a project handle is notified when list of
     * its builder jobs changes.
     */
    @Test
    public void testJobListChangeListener() throws InterruptedException {
        List<JobHandle> jobs = accessor.getJobs(projectHandle);
        assertEquals(1, jobs.size());
        final Semaphore s = new Semaphore(0);
        final AtomicBoolean notified = new AtomicBoolean(false);
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(
                        ProjectHandle.PROP_BUILD_LIST)) {
                    notified.set(true);
                    s.release();
                }
            }
        };
        projectHandle.addPropertyChangeListener(listener);
        connector.addJob();
        syncHudsonInstance();
        s.tryAcquire(1, TimeUnit.SECONDS);
        jobs = accessor.getJobs(projectHandle);
        assertTrue(notified.get());
        assertEquals(2, jobs.size());
        assertEquals("Job2", jobs.get(1).getDisplayName());
    }

    /**
     * Wait for initialization of job status.
     */
    private void waitForInitialization(JobHandle job)
            throws InterruptedException {
        final Semaphore s = new Semaphore(0);
        PropertyChangeListener pcl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(JobHandle.PROP_STATUS)) {
                    s.release();
                }
            }
        };
        job.addPropertyChangeListener(pcl);
        s.tryAcquire(1, TimeUnit.SECONDS);
        job.removePropertyChangeListener(pcl);
    }

    /**
     * Synchronize hudson instance.
     */
    private void syncHudsonInstance() {
        ContextAwareAction syncAction = FileUtil.getConfigObject(
                "org-netbeans-modules-hudson/Actions/instance/"
                + "org-netbeans-modules-hudson-ui-actions-SynchronizeAction"
                + ".shadow",
                ContextAwareAction.class);
        Action a = syncAction.createContextAwareInstance(
                Lookups.fixed(hudsonInstance));
        a.actionPerformed(new ActionEvent(this, 0, "sync")); // synchronize
    }

    /**
     * Builder connector that provides data for mock hudson instance.
     */
    public class MockBuilderConnector extends BuilderConnector {

        private List<JobData> jobsData;
        private List<BuildData> buildsData;

        public MockBuilderConnector() {
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
            this.jobsData = new LinkedList<JobData>();
            this.jobsData.add(jd);

            this.buildsData = new LinkedList<BuildData>();
            this.buildsData.add(new BuildData(1, Result.FAILURE, false));
            this.buildsData.add(new BuildData(2, Result.UNSTABLE, false));
        }

        @Override
        public InstanceData getInstanceData(boolean authentication) {

            InstanceData id = new InstanceData(jobsData,
                    Collections.<ViewData>emptyList());
            return id;
        }

        @Override
        public Collection<BuildData> getJobBuildsData(HudsonJob job) {
            if (job.getName().equals("Job1")) {
                return this.buildsData;
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

        /**
         * Add a new stable build to the first job.
         */
        public void addBuild() {
            BuildData bd = new BuildData(3, Result.SUCCESS, false);
            buildsData.add(bd);
            JobData jd = jobsData.get(0);
            jd.setLastBuild(3);
            jd.setLastCompletedBuild(3);
            jd.setLastStableBuild(3);
            jd.setLastSuccessfulBuild(3);
            jd.setColor(HudsonJob.Color.blue);
        }

        /**
         * Add a new job of name "Job2".
         */
        public void addJob() {
            JobData jd = new JobData();
            jd.setBuildable(true);
            jd.setColor(HudsonJob.Color.grey);
            jd.setJobName("Job2");
            jd.setDisplayName("Job2");
            jd.setJobUrl("http://test/hudson/Job2/");
            jd.setLastBuild(0);
            jd.setLastCompletedBuild(0);
            jd.setLastFailedBuild(0);
            jd.setLastStableBuild(0);
            jd.setLastSuccessfulBuild(0);
            jd.setSecured(false);
            jobsData.add(jd);
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
        public Collection<? extends HudsonJobChangeItem> getJobBuildChanges(HudsonJobBuild build) {
            return Collections.emptyList();
        }

        @Override
        public ConsoleDataProvider getConsoleDataProvider() {
            return null;
        }

        @Override
        public FailureDataProvider getFailureDataProvider() {
            return null;
        }
    }

    /**
     * ODCSS Factory providing data for mock project handle.
     */
    public static class MockODCSSFactory extends ODCSFactory {

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public ODCSClient createClient(String url, PasswordAuthentication auth) {
            return new ODCSClient() {
                @Override
                public Project createProject(Project project)
                        throws ODCSException {
                    throw unsupportedByMock();
                }

                public BuildDetails getBuildDetails(String projectId,
                        String jobName, int buildNumber) throws ODCSException {
                    BuildDetails bd = new BuildDetails();
                    bd.setBuilding(false);
                    bd.setResult(BuildDetails.BuildResult.UNSTABLE);
                    bd.setNumber(123);
                    bd.setNumber(456);
                    bd.setTimestamp(System.currentTimeMillis());
                    return bd;
                }

                @Override
                public Profile getCurrentProfile() throws ODCSException {
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

                public HudsonStatus getHudsonStatus(String projectId)
                        throws ODCSException {
                    HudsonStatus hs = new HudsonStatus();
                    JobSummary js = new JobSummary();
                    BuildDetails bd = new BuildDetails();
                    js.setBuilds(Collections.singletonList(bd));
                    hs.setJobs(Collections.singletonList(js));
                    return hs;
                }

                public JobDetails getJobDetails(String projectId,
                        String jobName) throws ODCSException {
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
                public List<Project> getMyProjects() throws ODCSException {
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
                        throws ODCSException {
                    if (projectId.equals("mockProject")) {
                        return getMyProjects().get(0);
                    } else {
                        return null;
                    }
                }

                @Override
                public List<ProjectActivity> getRecentActivities(
                        String projectId) throws ODCSException {
                    throw unsupportedByMock();
                }

                @Override
                public List<ProjectActivity> getRecentShortActivities(
                        String projectId) throws ODCSException {
                    throw unsupportedByMock();
                }

                @Override
                public List<ScmRepository> getScmRepositories(String projectId)
                        throws ODCSException {
                    throw unsupportedByMock();
                }

                @Override
                public List<Project> getWatchedProjects() throws ODCSException {
                    throw unsupportedByMock();
                }

                @Override
                public boolean isWatchingProject(String projectId)
                        throws ODCSException {
                    throw unsupportedByMock();
                }

                @Override
                public List<Project> searchProjects(String pattern) {
                    throw unsupportedByMock();
                }

                @Override
                public void unwatchProject(String projectId)
                        throws ODCSException {
                    throw unsupportedByMock();
                }

                @Override
                public void watchProject(String projectId) throws ODCSException {
                    throw unsupportedByMock();
                }

                private RuntimeException unsupportedByMock() {
                    return new UnsupportedOperationException(
                            "Not supported by mock instance.");
                }

                @Override
                public SavedTaskQuery createQuery(String projectId, SavedTaskQuery query) throws ODCSException {
                    throw unsupportedByMock();
                }

                @Override
                public SavedTaskQuery updateQuery(String projectId, SavedTaskQuery query) throws ODCSException {
                    throw unsupportedByMock();
                }

                @Override
                public void deleteQuery(String projectId, Integer queryId) throws ODCSException {
                    throw unsupportedByMock();
                }

                @Override
                public RepositoryConfiguration getRepositoryContext(String projectId) throws ODCSException {
                    throw unsupportedByMock();
                }
            };
        }
    }
}

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

package org.netbeans.modules.jira.repository;

import java.io.File;
import java.util.Date;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.JiraTestUtil;
import org.netbeans.modules.jira.client.spi.Component;
import org.netbeans.modules.jira.client.spi.ComponentFilter;
import org.netbeans.modules.jira.client.spi.ContentFilter;
import org.netbeans.modules.jira.client.spi.CurrentUserFilter;
import org.netbeans.modules.jira.client.spi.DateRangeFilter;
import org.netbeans.modules.jira.client.spi.EstimateVsActualFilter;
import org.netbeans.modules.jira.client.spi.FilterDefinition;
import org.netbeans.modules.jira.client.spi.IssueType;
import org.netbeans.modules.jira.client.spi.IssueTypeFilter;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.client.spi.JiraStatus;
import org.netbeans.modules.jira.client.spi.NobodyFilter;
import org.netbeans.modules.jira.client.spi.Priority;
import org.netbeans.modules.jira.client.spi.PriorityFilter;
import org.netbeans.modules.jira.client.spi.Project;
import org.netbeans.modules.jira.client.spi.ProjectFilter;
import org.netbeans.modules.jira.client.spi.Resolution;
import org.netbeans.modules.jira.client.spi.ResolutionFilter;
import org.netbeans.modules.jira.client.spi.SpecificUserFilter;
import org.netbeans.modules.jira.client.spi.StatusFilter;
import org.netbeans.modules.jira.client.spi.UserInGroupFilter;
import org.netbeans.modules.jira.client.spi.Version;
import org.netbeans.modules.jira.client.spi.VersionFilter;

/**
 *
 * @author tomas
 */
public class StorageManagerTest extends NbTestCase {

    private static String REPO_NAME;
    private static String QUERY_NAME = "Hilarious";
    private JiraRepository repo;

    public StorageManagerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir(), "userdir").getAbsolutePath());
        super.setUp();
        REPO_NAME = "Beautiful-" + System.currentTimeMillis();
        JiraTestUtil.initClient(getWorkDir());
        JiraTestUtil.cleanProject(JiraTestUtil.getProject());
    }

    public static Test suite () {
        return NbModuleSuite.createConfiguration(StorageManagerTest.class).gui(false).suite();
    }

    public void testAssignedToFilter() {
        checkFilter(new FilterProvider(CurrentUserFilter.class) {
            public CurrentUserFilter get(FilterDefinition fd) {
                return (CurrentUserFilter) fd.getAssignedToFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setAssignedToFilter(JiraConnectorSupport.getInstance().getConnector().createCurrentUserFilter());
            }
        });
        
        checkFilter(new FilterProvider(NobodyFilter.class) {
            public NobodyFilter get(FilterDefinition fd) {
                return (NobodyFilter) fd.getAssignedToFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setAssignedToFilter(JiraConnectorSupport.getInstance().getConnector().createNobodyFilter());
            }
        });
        
        final String someGroup = "some group";
        FilterDefinition fd = checkFilter(new FilterProvider(UserInGroupFilter.class) {
            public UserInGroupFilter get(FilterDefinition fd) {
                return (UserInGroupFilter) fd.getAssignedToFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setAssignedToFilter(JiraConnectorSupport.getInstance().getConnector().createUserInGroupFilter(someGroup));
            }
        });
        assertEquals(someGroup, ((UserInGroupFilter)fd.getAssignedToFilter()).getGroup());
        
        final String someUser = "some luser";
        fd = checkFilter(new FilterProvider(SpecificUserFilter.class) {
            public SpecificUserFilter get(FilterDefinition fd) {
                return (SpecificUserFilter) fd.getAssignedToFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setAssignedToFilter(JiraConnectorSupport.getInstance().getConnector().createSpecificUserFilter(someUser));
            }
        });
        assertEquals(someUser, ((SpecificUserFilter)fd.getAssignedToFilter( )).getUser());
    }
    
    public void testComponentFilter() {
        final JiraConfiguration config = getRepository().getConfiguration();
        final Component[] cmps = config.getComponents(config.getProjectByKey(JiraTestUtil.TEST_PROJECT));
        FilterDefinition fd = checkFilter(new FilterProvider(ComponentFilter.class) {
            public ComponentFilter get(FilterDefinition fd) {
                return fd.getComponentFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setComponentFilter(JiraConnectorSupport.getInstance().getConnector().createComponentFilter(cmps, false));
            }
        });
        assertEquals(cmps.length, fd.getComponentFilter().getComponents().length);
        assertEquals(cmps[0].getId(), fd.getComponentFilter().getComponents()[0].getId());
    }
    
    public void testContentFilter() {
        testContentFilter("sum", true, false, false, false);
        testContentFilter("desc", false, true, false, false);
        testContentFilter("env", false, false, true, false);
        testContentFilter("com", false, false, false, true);
        testContentFilter("all", true, true, true, true);
    }
    
    public void testCreatedDateFilterFilter() {
        final Date from = new Date(System.currentTimeMillis());
        final Date to = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        FilterDefinition fd = checkFilter( new FilterProvider(DateRangeFilter.class) {
            public DateRangeFilter get(FilterDefinition fd) {
                return (DateRangeFilter) fd.getCreatedDateFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setCreatedDateFilter(JiraConnectorSupport.getInstance().getConnector().createDateRangeFilter(from, to));
            }
        });
        assertEquals(from, ((DateRangeFilter)fd.getCreatedDateFilter()).getFromDate());
        assertEquals(to, ((DateRangeFilter)fd.getCreatedDateFilter()).getToDate());
    }
    
    public void testDueDateFilter() {
        final Date from = new Date(System.currentTimeMillis());
        final Date to = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        FilterDefinition fd = checkFilter(new FilterProvider(DateRangeFilter.class) {
            public DateRangeFilter get(FilterDefinition fd) {
                return (DateRangeFilter) fd.getDueDateFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setDueDateFilter(JiraConnectorSupport.getInstance().getConnector().createDateRangeFilter(from, to));
            }
        });
        assertEquals(from, ((DateRangeFilter)fd.getDueDateFilter()).getFromDate());
        assertEquals(to, ((DateRangeFilter)fd.getDueDateFilter()).getToDate());
    }    
    
    public void testEstimateVsActualFilter() {    
        final long min = 1;
        final long max = 2;
        FilterDefinition fd = checkFilter(new FilterProvider(EstimateVsActualFilter.class) {
            public EstimateVsActualFilter get(FilterDefinition fd) {
                return fd.getEstimateVsActualFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setEstimateVsActualFilter(JiraConnectorSupport.getInstance().getConnector().createEstimateVsActualFilter(min, max));
            }
        });
        assertEquals(min, fd.getEstimateVsActualFilter().getMinVariation());
        assertEquals(max, fd.getEstimateVsActualFilter().getMaxVariation());
    }
    
    public void testFixForVersionFilter() {    
        final JiraConfiguration config = getRepository().getConfiguration();
        final Version[] versions = config.getVersions(config.getProjectByKey(JiraTestUtil.TEST_PROJECT));
        FilterDefinition fd = checkFilter(new FilterProvider(VersionFilter.class) {
            public VersionFilter get(FilterDefinition fd) {
                return fd.getFixForVersionFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setFixForVersionFilter(JiraConnectorSupport.getInstance().getConnector().createVersionFilter(versions, false, false, false));
            }
        });
        assertEquals(versions.length, fd.getFixForVersionFilter().getVersions().length);
        assertEquals(versions[0], fd.getFixForVersionFilter().getVersions()[0]);
    }
    
    public void testIssueTypeFilter() {    
        final IssueType[] types = getRepository().getConfiguration().getIssueTypes();
        FilterDefinition fd = checkFilter(new FilterProvider(IssueTypeFilter.class) {
            public IssueTypeFilter get(FilterDefinition fd) {
                return fd.getIssueTypeFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setIssueTypeFilter(JiraConnectorSupport.getInstance().getConnector().createIssueTypeFilter(types));
            }
        });
        assertEquals(types.length, fd.getIssueTypeFilter().getIssueTypes().length);
        assertEquals(types[0], fd.getIssueTypeFilter().getIssueTypes()[0]);
    }
    
    public void testPriorityFilter() {    
        final Priority[] prios = getRepository().getConfiguration().getPriorities();
        FilterDefinition fd = checkFilter(new FilterProvider(PriorityFilter.class) {
            public PriorityFilter get(FilterDefinition fd) {
                return fd.getPriorityFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setPriorityFilter(JiraConnectorSupport.getInstance().getConnector().createPriorityFilter(prios));
            }
        });
        assertEquals(prios.length, fd.getPriorityFilter().getPriorities().length);
        assertEquals(prios[0], fd.getPriorityFilter().getPriorities()[0]);
    }
    
    public void testProjectFilter() {    
        final Project[] projects = getRepository().getConfiguration().getProjects();
        FilterDefinition fd = checkFilter(new FilterProvider(ProjectFilter.class) {
            public ProjectFilter get(FilterDefinition fd) {
                return fd.getProjectFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setProjectFilter(JiraConnectorSupport.getInstance().getConnector().createProjectFilter(projects));
            }
        });
        assertEquals(projects.length, fd.getProjectFilter().getProjects().length);
        assertEquals(projects[0], fd.getProjectFilter().getProjects()[0]);
    }
    
    public void testReportedByFilter() {    
        checkFilter(new FilterProvider(CurrentUserFilter.class) {
            public CurrentUserFilter get(FilterDefinition fd) {
                return (CurrentUserFilter) fd.getReportedByFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setReportedByFilter(JiraConnectorSupport.getInstance().getConnector().createCurrentUserFilter());
            }
        });
        
        checkFilter(new FilterProvider(NobodyFilter.class) {
            public NobodyFilter get(FilterDefinition fd) {
                return (NobodyFilter) fd.getReportedByFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setReportedByFilter(JiraConnectorSupport.getInstance().getConnector().createNobodyFilter());
            }
        });
        
        final String someGroup = "some group";
        FilterDefinition fd = checkFilter(new FilterProvider(UserInGroupFilter.class) {
            public UserInGroupFilter get(FilterDefinition fd) {
                return (UserInGroupFilter) fd.getReportedByFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setReportedByFilter(JiraConnectorSupport.getInstance().getConnector().createUserInGroupFilter(someGroup));
            }
        });
        assertEquals(someGroup, ((UserInGroupFilter)fd.getReportedByFilter()).getGroup());
        
        final String someUser = "some luser";
        fd = checkFilter(new FilterProvider(SpecificUserFilter.class) {
            public SpecificUserFilter get(FilterDefinition fd) {
                return (SpecificUserFilter) fd.getReportedByFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setReportedByFilter(JiraConnectorSupport.getInstance().getConnector().createSpecificUserFilter(someUser));
            }
        });
        assertEquals(someUser, ((SpecificUserFilter)fd.getReportedByFilter( )).getUser());
    }

    public void testReportedInVersionFilter() {    
        final JiraConfiguration config = getRepository().getConfiguration();
        final Version[] versions = config.getVersions(config.getProjectByKey(JiraTestUtil.TEST_PROJECT));
        FilterDefinition fd = checkFilter(new FilterProvider(VersionFilter.class) {
            public VersionFilter get(FilterDefinition fd) {
                return fd.getReportedInVersionFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setReportedInVersionFilter(JiraConnectorSupport.getInstance().getConnector().createVersionFilter(versions, false, false, false));
            }
        });
        assertEquals(versions.length, fd.getReportedInVersionFilter().getVersions().length);
        assertEquals(versions[0], fd.getReportedInVersionFilter().getVersions()[0]);
    }
    
    public void testResolutionFilter() {    
        final Resolution[] resolutions = getRepository().getConfiguration().getResolutions();
        FilterDefinition fd = checkFilter(new FilterProvider(ResolutionFilter.class) {
            public ResolutionFilter get(FilterDefinition fd) {
                return fd.getResolutionFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setResolutionFilter(JiraConnectorSupport.getInstance().getConnector().createResolutionFilter(resolutions));
            }
        });
        assertEquals(resolutions.length, fd.getResolutionFilter().getResolutions().length);
        assertEquals(resolutions[0], fd.getResolutionFilter().getResolutions()[0]);
    }
    
    public void testStatusFilter() {    
        final JiraStatus[] statuses = getRepository().getConfiguration().getStatuses();
        FilterDefinition fd = checkFilter(new FilterProvider(StatusFilter.class) {
            public StatusFilter get(FilterDefinition fd) {
                return fd.getStatusFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setStatusFilter(JiraConnectorSupport.getInstance().getConnector().createStatusFilter(statuses));
            }
        });
        assertEquals(statuses.length, fd.getStatusFilter().getStatuses().length);
        assertEquals(statuses[0], fd.getStatusFilter().getStatuses()[0]);
    }
    
    public void testUpdatedDateFilter() {
        final Date from = new Date(System.currentTimeMillis());
        final Date to = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        FilterDefinition fd = checkFilter(new FilterProvider(DateRangeFilter.class) {
            public DateRangeFilter get(FilterDefinition fd) {
                return (DateRangeFilter) fd.getUpdatedDateFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setUpdatedDateFilter(JiraConnectorSupport.getInstance().getConnector().createDateRangeFilter(from, to));
            }
        });
        assertEquals(from, ((DateRangeFilter)fd.getUpdatedDateFilter()).getFromDate());
        assertEquals(to, ((DateRangeFilter)fd.getUpdatedDateFilter()).getToDate());
    }
    
    private void testContentFilter(final String text, final boolean sum, final boolean desc, final boolean env, final boolean com) {
        FilterDefinition fd;
        fd = checkFilter(new FilterProvider(ContentFilter.class) {
            public ContentFilter get(FilterDefinition fd) {
                return fd.getContentFilter();
            }
            public void set(FilterDefinition fd) {
                fd.setContentFilter(JiraConnectorSupport.getInstance().getConnector().createContentFilter(text, sum, desc, env, com));
            }
        });
        assertEquals(text, fd.getContentFilter().getQueryString());
        assertEquals(sum, fd.getContentFilter().isSearchingSummary());
        assertEquals(desc, fd.getContentFilter().isSearchingDescription());
        assertEquals(env, fd.getContentFilter().isSearchingEnvironment());
        assertEquals(com, fd.getContentFilter().isSearchingComments());
    }

    private FilterDefinition checkFilter (FilterProvider fp) {
        FilterDefinition fd = JiraConnectorSupport.getInstance().getConnector().createFilterDefinition();
        assertNull(fp.get(fd));
        fp.set(fd);
        fd = storeFD(fd);
        assertNotNull(fd);
        assertNotNull(fp.get(fd));
        assertTrue(fp.type().isInstance(fp.get(fd)));
        return fd;
    }
    
    private abstract class FilterProvider<T> {
        private final Class<T> type;
        public FilterProvider(Class<T> type) {
            this.type = type;
        }
        public abstract T get(FilterDefinition fd);
        public abstract void set(FilterDefinition fd);
        public Class<T> type() {
            return type;
        };
    }
    
    private FilterDefinition storeFD(FilterDefinition fd) {
        JiraStorageManager sm1 = new JiraStorageManager();
        assertTrue(sm1.getFDs(getRepository()).isEmpty());        
        sm1.putQueryData(getRepository(), QUERY_NAME, -1, fd);
        
        assertEquals(1, sm1.getFDs(getRepository()).size());
        assertTrue(new JiraStorageManager().getFDs(getRepository()).isEmpty());
        
        // forces save
        sm1.shutdown();
        
        // create a new StorageManager to force rereading the stored data
        Map<String, FilterDefinition> fds = new JiraStorageManager().getFDs(getRepository());
        assertEquals(1, fds.size());
        fd = fds.values().iterator().next();
        assertNotNull(fd);
        
        // remove
        sm1.removeQuery(getRepository(), QUERY_NAME, QUERY_NAME);
        sm1.shutdown();
        
        return fd;
    }

    private JiraRepository getRepository() {
        if(repo == null) {
            RepositoryInfo info = new RepositoryInfo(REPO_NAME, JiraConnector.ID, JiraTestUtil.REPO_URL, REPO_NAME, REPO_NAME, JiraTestUtil.REPO_USER, null, JiraTestUtil.REPO_PASSWD.toCharArray() , null);
            repo = new JiraRepository(info);
        }
        return repo;
    }
}

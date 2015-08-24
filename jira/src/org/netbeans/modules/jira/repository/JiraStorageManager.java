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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.repository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.client.spi.Component;
import org.netbeans.modules.jira.client.spi.ComponentFilter;
import org.netbeans.modules.jira.client.spi.ContentFilter;
import org.netbeans.modules.jira.client.spi.CurrentUserFilter;
import org.netbeans.modules.jira.client.spi.DateFilter;
import org.netbeans.modules.jira.client.spi.DateRangeFilter;
import org.netbeans.modules.jira.client.spi.EstimateVsActualFilter;
import org.netbeans.modules.jira.client.spi.FilterDefinition;
import org.netbeans.modules.jira.client.spi.IssueType;
import org.netbeans.modules.jira.client.spi.IssueTypeFilter;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
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
import org.netbeans.modules.jira.client.spi.UserFilter;
import org.netbeans.modules.jira.client.spi.UserInGroupFilter;
import org.netbeans.modules.jira.client.spi.Version;
import org.netbeans.modules.jira.client.spi.VersionFilter;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.util.FileUtils;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.openide.modules.Places;

/**
 *
 * @author Ondra Vrabec
 */
public class JiraStorageManager {
    private static JiraStorageManager instance;

    private final Object QUERY_LOCK = new Object();
    private Map<String, JiraQueryData2> queriesData;
    private static final String QUERY_DELIMITER           = "<=>";      //NOI18N
    private static final String QUERIES_STORAGE_FILE = "queries.data";  //NOI18N
    private static final Level LOG_LEVEL = JiraUtils.isAssertEnabled() ? Level.SEVERE : Level.INFO;

    public JiraStorageManager () {

    }
    
    public static JiraStorageManager getInstance() {
        if (instance == null) {
            instance = new JiraStorageManager();
        }
        return instance;
    }

    /**
     * MAY access IO (on the first access)
     * @param repository
     * @param query
     */
    public void putQueryData(JiraRepository repository, String queryName, long lastRefresh, FilterDefinition fd) {
        JiraQueryData2 qd = new JiraQueryData2(queryName, lastRefresh, fd);
        getCachedQueries().put(getQueryKey(repository.getID(), queryName), qd);
    }

    private JiraQuery createQuery(JiraRepository repository, JiraQueryData2 data) {
        assert data != null;
        FilterDefinition filterDefinition = getFilterDefinition(repository, data);
        return filterDefinition != null ? repository.createPersistentQuery(data.queryName, filterDefinition) : null;
    }

    private Map<String, JiraQueryData2> getCachedQueries () {
        synchronized (QUERY_LOCK) {
            if (queriesData == null) {
                loadQueries();
            }
        }
        return queriesData;
    }

     /**
     * Removes a query with the given queryName.
     * MAY access IO (on the first access)
     * @param repository
     * @param queryName
     */
    public void removeQuery(JiraRepository repository, String queryName, String storedName) {
        getCachedQueries().remove(getQueryKey(repository.getID(), queryName));
        try {
            IRepositoryQuery iquery = storedName == null ? null 
                    : MylynSupport.getInstance().getRepositoryQuery(repository.getTaskRepository(), storedName);
            if (iquery != null) {
                MylynSupport.getInstance().deleteQuery(iquery);
            }
        } catch (CoreException ex) {
            Jira.LOG.log(LOG_LEVEL, null, ex);
        }
    }

     /**
     * Returns a set of queries registered with the given repository name
     * MAY access IO (on the first access)
     * @param repository a repository which's queries will be returned
     */
    public HashSet<JiraQuery> getQueries (JiraRepository repository) {
        HashSet<JiraQuery> queries = new HashSet<>(10);
        for (Entry<String, JiraQueryData2> e : getCachedQueries().entrySet()) {
            if (e.getKey().startsWith(repository.getID() + QUERY_DELIMITER)) {
                JiraQuery q = createQuery(repository, e.getValue());
                if(q != null) {
                    queries.add(q);
                }
            }
        }
        return queries;
    }

    /** Testing purposes only **/
    public Map<String, FilterDefinition> getFDs (JiraRepository repository) {
        HashMap<String, FilterDefinition> fds = new HashMap<>(10);
        for (Entry<String, JiraQueryData2> e : getCachedQueries().entrySet()) {
            if (e.getKey().startsWith(repository.getID() + QUERY_DELIMITER)) {
                fds.put(e.getValue().queryName, getFilterDefinition(repository, e.getValue()));
            }
        }
        return fds;
    }

    private void loadQueries () {
        Jira.LOG.fine("loadQueries: loading queries");                   //NOI18N

        File f = new File(getNBConfigPath());
        try {
            ObjectInputStream ois = null;
            File file = new File(f, QUERIES_STORAGE_FILE);
            if (!file.canRead()) {
                Jira.LOG.info("loadQueries: no saved data");             //NOI18N
                return;
            }
            try {
                ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                String version = ois.readUTF();
                if (!JiraQueryData2.VERSION.equals(version)) {
                    Jira.LOG.log(Level.INFO, "loadQueries: old data format: {0}", version); //NOI18N
                    return;
                }
                int size = ois.readInt();
                Jira.LOG.log(Level.FINE, "loadQueries: loading {0} queries", size); //NOI18N
                queriesData = new HashMap<>(size + 5);
                while (size-- > 0) {
                    String queryIdent = ois.readUTF();
                    Jira.LOG.log(Level.FINE, "loadQueries: loading data for {0}", queryIdent); //NOI18N
                    JiraQueryData2 data = (JiraQueryData2) ois.readObject();
                    queriesData.put(queryIdent, data);
                }
            } catch (IOException | ClassNotFoundException ex) {
                Jira.LOG.log(LOG_LEVEL, null, ex);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                    }
                }
            }
        } finally {
            if (queriesData == null) {
                queriesData = new HashMap<>(5);
            }
        }
    }

    private String getQueryKey(String repositoryName, String queryName) {
        return repositoryName + QUERY_DELIMITER + queryName;
    }

    private void storeQueries () {
        Jira.LOG.fine("storeQueries: saving queries");                  //NOI18N
        if (queriesData == null) {
            Jira.LOG.fine("storeQueries: no data loaded, no data saved"); //NOI18N
            return;
        }
        File f = new File(getNBConfigPath());
        f.mkdirs();
        if (!f.canWrite()) {
            Jira.LOG.warning("storeQueries: Cannot create perm storage"); //NOI18N
            return;
        }
        ObjectOutputStream out = null;
        File file = new File(f, QUERIES_STORAGE_FILE + ".tmp");
        boolean success = false;
        try {
            // saving to a temp file
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            out.writeUTF(JiraQueryData2.VERSION);
            out.writeInt(queriesData.size());
            for (Entry<String, JiraQueryData2> entry : queriesData.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeObject(entry.getValue());
            }
            success = true;
        } catch (IOException ex) {
            Jira.LOG.log(LOG_LEVEL, null, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        if (success) {
            success = false;
            // rename the temp file to the permanent one
            File newFile = new File(f, QUERIES_STORAGE_FILE);
            try {
                FileUtils.renameFile(file, newFile);
            } catch (IOException ex) {
                Jira.LOG.log(LOG_LEVEL, null, ex);
                success = false;
            }
        }
        if (!success) {
            file.deleteOnExit();
        }
    }

    /**
     * Returns the path for the Jira configuration directory.
     *
     * @return the path
     *
     */
    private static String getNBConfigPath() {
        //T9Y - nb jira confing should be changable
        String t9yNbConfigPath = System.getProperty("netbeans.t9y.jira.nb.config.path"); //NOI18N
        if (t9yNbConfigPath != null && t9yNbConfigPath.length() > 0) {
            return t9yNbConfigPath;
        }
        String nbHome = Places.getUserDirectory().getAbsolutePath();    //NOI18N
        return nbHome + "/config/jira/";                                //NOI18N
    }

    public void shutdown() {
        storeQueries();
    }

    private static class JiraQueryData implements Serializable {
        private static final String VERSION = "0.1";                    //NOI18N
        private final String queryName;
        private final long lastRefresh;
        private final FilterDefinition filterDefinition;

        private JiraQueryData(JiraQuery query) {
            queryName = query.getDisplayName();
            lastRefresh = query.getLastRefresh();
            filterDefinition = query.getFilterDefinition();
        }

        public FilterDefinition getFilterDefinition() {
            return filterDefinition;
        }

        public long getLastRefresh() {
            return lastRefresh;
        }

        public String getQueryName () {
            return queryName;
        }
    }
    
    private static class JiraQueryData2 implements Serializable  {
        private static final String VERSION = "0.2";                    //NOI18N
        
        private final String queryName;
        private final long lastRefresh;
        
        private static class ContentFilterData implements Serializable  {
            private String queryString;
            private boolean isSearchingComments;
            private boolean isSearchingDescription;
            private boolean isSearchingEnvironment;
            private boolean isSearchingSummary;
            
        }
        private final ContentFilterData contentFilterData;
                
        private final String[] projectFilterData;
        private final String[] issueTypeFilterData;
        private final String[] componentsData;
        private final String[] fixForVersionsData;
        private final String[] reportedInVersionFilterData;
        private final String[] statusFilterData;
        private final String[] resolutionFilterData;
        private final String[] priorityFilterData;

        private static class UserFilterData implements Serializable  {
            private String group;
            private String user;
            private boolean current;
            private boolean nobody;
        }
        private final UserFilterData reportedByFilterData;
        private final UserFilterData assignedToFilterData;

        private static class EstimateVsActualFilterData implements Serializable  {
            private long minVariation;
            private long maxVariation;
        }
        private final EstimateVsActualFilterData estimateVsActualFilterData;

        private static class DateRangeFilterData implements Serializable {
            private Date fromDate;
            private Date toDate;
        }
        private final DateRangeFilterData dueDateFilterData;
        private final DateRangeFilterData createdDateFilterData;
        private final DateRangeFilterData updatedDateFilterData;
        
        public JiraQueryData2(String queryName, long lastRefresh, FilterDefinition fd) {
            this.queryName = queryName;
            this.lastRefresh = lastRefresh;
            
            assignedToFilterData = createUserData(fd.getAssignedToFilter());
            
            ComponentFilter cmpf = fd.getComponentFilter();
            componentsData = cmpf != null? JiraUtils.toIds(cmpf.getComponents()): null;
            
            ContentFilter cntf = fd.getContentFilter();
            if(cntf != null) {
                contentFilterData = new ContentFilterData();
                contentFilterData.queryString = cntf.getQueryString();
                contentFilterData.isSearchingComments = cntf.isSearchingComments();
                contentFilterData.isSearchingDescription = cntf.isSearchingDescription();
                contentFilterData.isSearchingEnvironment = cntf.isSearchingEnvironment();
                contentFilterData.isSearchingSummary = cntf.isSearchingSummary();
            } else {
                contentFilterData = null;
            }
            
            createdDateFilterData = createDateRangeData(fd.getCreatedDateFilter());
            dueDateFilterData = createDateRangeData(fd.getDueDateFilter());
            updatedDateFilterData = createDateRangeData(fd.getUpdatedDateFilter());
            
            EstimateVsActualFilter evaf = fd.getEstimateVsActualFilter();
            if(evaf != null) {
                estimateVsActualFilterData = new EstimateVsActualFilterData();
                estimateVsActualFilterData.maxVariation = evaf.getMaxVariation();
                estimateVsActualFilterData.minVariation = evaf.getMinVariation();
            } else {
                estimateVsActualFilterData = null;
            }
            
            VersionFilter f4vf = fd.getFixForVersionFilter();
            fixForVersionsData = f4vf != null ? JiraUtils.toIds(f4vf.getVersions()) : null;
            
            IssueTypeFilter itf = fd.getIssueTypeFilter();
            issueTypeFilterData = itf != null ? JiraUtils.toIds(itf.getIssueTypes()) : null;
            
            PriorityFilter prif = fd.getPriorityFilter();
            priorityFilterData = prif != null ? JiraUtils.toIds(prif.getPriorities()) : null;
            
            ProjectFilter prof = fd.getProjectFilter();
            projectFilterData = prof != null ? JiraUtils.toIds(prof.getProjects()) : null;
            
            reportedByFilterData = createUserData(fd.getReportedByFilter());
            
            VersionFilter rivf = fd.getReportedInVersionFilter();
            reportedInVersionFilterData = rivf != null ? JiraUtils.toIds(rivf.getVersions()) : null;
            
            ResolutionFilter rf = fd.getResolutionFilter();
            resolutionFilterData = rf != null ? JiraUtils.toIds(rf.getResolutions()) : null;
            
            StatusFilter sf = fd.getStatusFilter();
            statusFilterData = sf != null ? JiraUtils.toIds(sf.getStatuses()) : null;
        }        
    }
    
    private static JiraQueryData2.DateRangeFilterData createDateRangeData(DateFilter df) {
        if(df != null && df instanceof DateRangeFilter) {
            DateRangeFilter drf = (DateRangeFilter) df;
            JiraQueryData2.DateRangeFilterData drd = new JiraQueryData2.DateRangeFilterData();
            drd.fromDate = drf.getFromDate();
            drd.toDate = drf.getToDate();
            return drd;
        } 
        return null;
    }
    
    private static JiraQueryData2.UserFilterData createUserData(UserFilter uf) {
        if(uf != null) {
            JiraQueryData2.UserFilterData ufd = new JiraQueryData2.UserFilterData();
            if(uf instanceof UserInGroupFilter) {
                ufd.group = ((UserInGroupFilter)uf).getGroup();
                return ufd;
            } else if(uf instanceof SpecificUserFilter) {
                ufd.user = ((SpecificUserFilter)uf).getUser();
                return ufd;
            } else if(uf instanceof CurrentUserFilter) {
                ufd.current = true;
            } else if(uf instanceof NobodyFilter) { 
                ufd.nobody = true;
            }
            return ufd;
        }
        return null;
    }
    
    private FilterDefinition getFilterDefinition(JiraRepository repository, JiraQueryData2 jqd) {
        JiraConnectorProvider connectorProvider = JiraConnectorSupport.getInstance().getConnector();
        FilterDefinition fd =  connectorProvider.createFilterDefinition();
        
        if(jqd.assignedToFilterData != null) {
            fd.setAssignedToFilter(createUserFilter(jqd.assignedToFilterData, connectorProvider));
        }
        
        JiraConfiguration conf = repository.getConfiguration();
        if(conf == null) {
            return null;
        }
        
        if(jqd.componentsData != null) {
            Component[] cmps = conf.getComponents(jqd.componentsData);
            fd.setComponentFilter(connectorProvider.createComponentFilter(cmps,cmps.length == 0));
        }
        
        if(jqd.projectFilterData != null) {
            List<Project> projects = new ArrayList(jqd.projectFilterData.length);
            for (String id : jqd.projectFilterData) {
                Project p = conf.getProjectById(id);
                if(p != null) {
                    projects.add(p);
                }
            }
            fd.setProjectFilter(connectorProvider.createProjectFilter(projects.toArray(new Project[projects.size()])));
        }
        
        if(jqd.contentFilterData != null) {
            fd.setContentFilter(connectorProvider.createContentFilter(
                jqd.contentFilterData.queryString,
                jqd.contentFilterData.isSearchingSummary,
                jqd.contentFilterData.isSearchingDescription,
                jqd.contentFilterData.isSearchingEnvironment,
                jqd.contentFilterData.isSearchingComments));
        }
        
        if(jqd.createdDateFilterData != null) {
            fd.setCreatedDateFilter(connectorProvider.createDateRangeFilter(jqd.createdDateFilterData.fromDate, jqd.createdDateFilterData.toDate));
        }
        
        if(jqd.dueDateFilterData != null) {
            fd.setDueDateFilter(connectorProvider.createDateRangeFilter(jqd.dueDateFilterData.fromDate, jqd.dueDateFilterData.toDate));
        }
        
        if(jqd.estimateVsActualFilterData != null) {
            fd.setEstimateVsActualFilter(connectorProvider.createEstimateVsActualFilter(jqd.estimateVsActualFilterData.minVariation, jqd.estimateVsActualFilterData.maxVariation));
        }
        
        if(jqd.fixForVersionsData != null) {
            Version[] versions = conf.getVersions(jqd.fixForVersionsData);
            fd.setFixForVersionFilter(connectorProvider.createVersionFilter(versions, versions.length == 0, true, false));
        }
        
        if(jqd.issueTypeFilterData != null) {
            List<IssueType> issueTypes = new ArrayList(jqd.issueTypeFilterData.length);
            for (String id : jqd.issueTypeFilterData) {
                IssueType it = conf.getIssueTypeById(id);
                if(it != null) {
                    issueTypes.add(it);
                }
            }
            fd.setIssueTypeFilter(connectorProvider.createIssueTypeFilter(issueTypes.toArray(new IssueType[issueTypes.size()])));
        }
        
        if(jqd.priorityFilterData != null) {
            List<Priority> priorities = new ArrayList(jqd.priorityFilterData.length);
            for (String id : jqd.priorityFilterData) {
                Priority p = conf.getPriorityById(id);
                if(p != null) {
                    priorities.add(p);
                }
            }
            fd.setPriorityFilter(connectorProvider.createPriorityFilter(priorities.toArray(new Priority[priorities.size()])));
        }
        
        if(jqd.reportedByFilterData != null) {
            fd.setReportedByFilter(createUserFilter(jqd.reportedByFilterData, connectorProvider));
        }        
        
        if(jqd.reportedInVersionFilterData != null) {
            Version[] versions = conf.getVersions(jqd.reportedInVersionFilterData);
            fd.setReportedInVersionFilter(connectorProvider.createVersionFilter(versions, versions.length == 0, true, false));
        }
        
        if(jqd.resolutionFilterData != null) {
            List<Resolution> resolutions = new ArrayList(jqd.resolutionFilterData.length);
            for (String id : jqd.resolutionFilterData) {
                Resolution r = conf.getResolutionById(id);
                if(r != null) {
                    resolutions.add(r);
                }
            }
            fd.setResolutionFilter(connectorProvider.createResolutionFilter(resolutions.toArray(new Resolution[resolutions.size()])));
        }
        
        if(jqd.statusFilterData != null) {
            List<JiraStatus> statuses = new ArrayList(jqd.statusFilterData.length);
            for (String id : jqd.statusFilterData) {
                JiraStatus r = conf.getStatusById(id);
                if(r != null) {
                    statuses.add(r);
                }
            }
            fd.setStatusFilter(connectorProvider.createStatusFilter(statuses.toArray(new JiraStatus[statuses.size()])));
        }
        
        if(jqd.updatedDateFilterData != null) {
            fd.setUpdatedDateFilter(connectorProvider.createDateRangeFilter(jqd.updatedDateFilterData.fromDate, jqd.updatedDateFilterData.toDate));
        }
        
        return fd;
    }

    private UserFilter createUserFilter(JiraQueryData2.UserFilterData ufd, JiraConnectorProvider connectorProvider) {
        if(ufd.current) {
            return connectorProvider.createCurrentUserFilter();
        } else if(ufd.nobody) {
            return connectorProvider.createNobodyFilter();
        } else if(ufd.group != null) {
            return connectorProvider.createUserInGroupFilter(ufd.group);
        } else if(ufd.user != null) {
            return connectorProvider.createSpecificUserFilter(ufd.user);
        }
        return null;
    }
}

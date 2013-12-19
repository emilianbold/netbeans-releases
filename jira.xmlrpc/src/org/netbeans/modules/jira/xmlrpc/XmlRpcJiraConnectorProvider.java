/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.xmlrpc;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.WorkLogConverter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.modules.jira.client.spi.Component;
import org.netbeans.modules.jira.client.spi.ComponentFilter;
import org.netbeans.modules.jira.client.spi.ContentFilter;
import org.netbeans.modules.jira.client.spi.CurrentUserFilter;
import org.netbeans.modules.jira.client.spi.DateRangeFilter;
import org.netbeans.modules.jira.client.spi.EstimateVsActualFilter;
import org.netbeans.modules.jira.client.spi.FilterDefinition;
import org.netbeans.modules.jira.client.spi.IssueType;
import org.netbeans.modules.jira.client.spi.IssueTypeFilter;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
import org.netbeans.modules.jira.client.spi.JiraConstants;
import org.netbeans.modules.jira.client.spi.JiraFilter;
import org.netbeans.modules.jira.client.spi.JiraStatus;
import org.netbeans.modules.jira.client.spi.JiraVersion;
import org.netbeans.modules.jira.client.spi.JiraWorkLog;
import static org.netbeans.modules.jira.client.spi.JiraWorkLog.AdjustEstimateMethod.AUTO;
import static org.netbeans.modules.jira.client.spi.JiraWorkLog.AdjustEstimateMethod.LEAVE;
import static org.netbeans.modules.jira.client.spi.JiraWorkLog.AdjustEstimateMethod.REDUCE;
import static org.netbeans.modules.jira.client.spi.JiraWorkLog.AdjustEstimateMethod.SET;
import org.netbeans.modules.jira.client.spi.NamedFilter;
import org.netbeans.modules.jira.client.spi.NobodyFilter;
import org.netbeans.modules.jira.client.spi.Priority;
import org.netbeans.modules.jira.client.spi.PriorityFilter;
import org.netbeans.modules.jira.client.spi.Project;
import org.netbeans.modules.jira.client.spi.ProjectFilter;
import org.netbeans.modules.jira.client.spi.Resolution;
import org.netbeans.modules.jira.client.spi.ResolutionFilter;
import org.netbeans.modules.jira.client.spi.SpecificUserFilter;
import org.netbeans.modules.jira.client.spi.StatusFilter;
import org.netbeans.modules.jira.client.spi.User;
import org.netbeans.modules.jira.client.spi.UserFilter;
import org.netbeans.modules.jira.client.spi.UserInGroupFilter;
import org.netbeans.modules.jira.client.spi.Version;
import org.netbeans.modules.jira.client.spi.VersionFilter;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Tomas Stupka
 */
@ServiceProviders({@ServiceProvider(service = JiraConnectorProvider.class)})
public class XmlRpcJiraConnectorProvider extends JiraConnectorProvider {

    private AbstractRepositoryConnector jrc;
    
    private final Map<TaskRepository, JiraClient> clients = new WeakHashMap<>(1);
    private XmlRpcJiraConstants jiraConstants;
    private WorkLogConverter workLogConverter;

    public XmlRpcJiraConnectorProvider() { }
    
    static Logger getLogger() {
        return LOG;
    }
    
    @Override
    public Type getType() {
        return Type.XMLRPC;
    }

    @Override
    public AbstractRepositoryConnector getRepositoryConnector() {
        if(jrc == null) {
            jrc = MylynRepositoryConnectorProvider.getInstance().getConnector();
            MylynSupport.getInstance().addRepositoryListener(JiraClientFactory.getDefault());
        }
        return jrc;
    }

    @Override
    public JiraClient getClient(TaskRepository repo) {
        JiraClient c = clients.get(repo);
        if(c == null) {
            c = new XmlRpcJiraClient(JiraClientFactory.getDefault().getJiraClient(repo));
            clients.put(repo, c);
        }
        return c;
    }

    @Override
    public void validateConnection(TaskRepository taskRepository) throws IOException {
        AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(taskRepository);
        try {
            JiraClientFactory.getDefault().validateConnection(location, new NullProgressMonitor());
        } catch (JiraException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    public boolean isJiraException(Throwable t) {
        return t instanceof JiraException;
    }

    @Override
    public boolean isJiraServiceUnavailableException(Throwable t) {
        return t instanceof JiraServiceUnavailableException;
    }

    @Override
    public FilterDefinition createFilterDefinition() {
        return createWrapper(FilterDefinition.class, new com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition());
    }

    @Override
    public ContentFilter createContentFilter(String queryString, boolean searchSummary, boolean searchDescription, boolean searchEnvironment, boolean searchComments) {
        return createWrapper(ContentFilter.class, new com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter(queryString, searchSummary, searchDescription, searchEnvironment, searchComments));
    }

    @Override
    public void setQuery(TaskRepository taskRepository, IRepositoryQuery iquery, JiraFilter fd) {
        com.atlassian.connector.eclipse.internal.jira.core.model.JiraFilter jq = getDelegate((Proxy)fd);
        JiraUtil.setQuery(taskRepository, iquery, jq);
    }
   
    @Override
    public JiraVersion createJiraVersion(String version) {
        return createWrapper(
            JiraVersion.class, 
            new com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion(version));
    }

    @Override
    public ProjectFilter createProjectFilter(Project project) {
        com.atlassian.connector.eclipse.internal.jira.core.model.Project p = getDelegate((Proxy)project);
        return createWrapper(ProjectFilter.class, new com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter(p));
    }
    
    @Override
    public ProjectFilter createProjectFilter(Project[] projects) {
        return createWrapper(
                ProjectFilter.class, 
                convert(com.atlassian.connector.eclipse.internal.jira.core.model.Project.class, (Proxy[])projects));
    }

    @Override
    public UserFilter createNobodyFilter() {
        return createWrapper(NobodyFilter.class, new com.atlassian.connector.eclipse.internal.jira.core.model.filter.NobodyFilter());
    }

    @Override
    public UserFilter createCurrentUserFilter() {
        return createWrapper(CurrentUserFilter.class, new com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter());
    }

    @Override
    public UserFilter createSpecificUserFilter(String user) {
        return createWrapper(SpecificUserFilter.class, new com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter(user));
    }

    @Override
    public UserFilter createUserInGroupFilter(String group) {
        return createWrapper(UserInGroupFilter.class, new com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter(group));
    }

    @Override
    public DateRangeFilter createDateRangeFilter(Date from, Date to) {
        return createWrapper(DateRangeFilter.class, new com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter(from, to));
    }

    @Override
    public IssueTypeFilter createIssueTypeFilter(IssueType[] issueTypes) {
        return createWrapper(
                IssueTypeFilter.class, 
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter(
                    convert(com.atlassian.connector.eclipse.internal.jira.core.model.IssueType.class, (Proxy[])issueTypes)));
    }
    
    @Override
    public ComponentFilter createComponentFilter(Component[] components, boolean empty) {
        return createWrapper(
                ComponentFilter.class, 
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter(
                    convert(com.atlassian.connector.eclipse.internal.jira.core.model.Component.class, (Proxy[])components), 
                    empty));
    }

    @Override
    public VersionFilter createVersionFilter(Version[] versions, boolean empty, boolean b, boolean b0) {
        return createWrapper(
                VersionFilter.class, 
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter(
                    convert(com.atlassian.connector.eclipse.internal.jira.core.model.Version.class, (Proxy[])versions), 
                    empty, b, b0));
    }

    @Override
    public StatusFilter createStatusFilter(JiraStatus[] statuses) {
        return createWrapper(
                StatusFilter.class, 
                convert(com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus.class, (Proxy[])statuses));
    }

    @Override
    public ResolutionFilter createResolutionFilter(Resolution[] resolutions) {
        return createWrapper(
                ResolutionFilter.class, 
                convert(com.atlassian.connector.eclipse.internal.jira.core.model.Resolution.class, (Proxy[])resolutions));
    }

    @Override
    public PriorityFilter createPriorityFilter(Priority[] priorities) {
        return createWrapper(
            PriorityFilter.class, 
                convert(com.atlassian.connector.eclipse.internal.jira.core.model.Priority.class, (Proxy[])priorities));
    }

    @Override
    public EstimateVsActualFilter createEstimateVsActualFilter(long l, long l0) {
        return createWrapper(EstimateVsActualFilter.class, new com.atlassian.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter(l, l0));
    }
        
    @Override
    public synchronized JiraConstants getJiraConstants() {
        if(jiraConstants == null) {
            jiraConstants = new XmlRpcJiraConstants(createJiraVersion(com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion.MIN_VERSION.toString()));
        }
        return jiraConstants;
    }

    @Override
    public JiraWorkLog createWorkLog() {
        return new XmlRpcJiraWorkLog();
    }

    @Override
    public JiraWorkLog createWorkLogFrom(TaskAttribute workLogTA) {
        return new XmlRpcJiraWorkLog(getWorkLogConverter().createFrom(workLogTA));
    }

    private synchronized WorkLogConverter getWorkLogConverter() {
        if (workLogConverter == null) {
            workLogConverter = new WorkLogConverter();
        }
        return workLogConverter;
    }

    private class XmlRpcJiraClient implements JiraClient {
        private final com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient delegate;
        
        public XmlRpcJiraClient(com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public NamedFilter[] getNamedFilters() throws IOException {
            try {
                com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter[] nfs = delegate.getNamedFilters(new NullProgressMonitor());
                if(nfs == null) {
                    return null;
                }
                List<NamedFilter> ret = new ArrayList<>(nfs.length);
                for (int i = 0; i < nfs.length; i++) {
                    ret.add(createWrapper(NamedFilter.class, nfs[i]));
                }
                return ret.toArray(new NamedFilter[ret.size()]);
            } catch (JiraException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public void setDateTimePattern(String value) {
            delegate.getLocalConfiguration().setDateTimePattern(value);
        }

        @Override
        public void setDatePattern(String value) {
            delegate.getLocalConfiguration().setDatePattern(value);
        }

        @Override
        public void setLocale(Locale locale) {
            delegate.getLocalConfiguration().setLocale(locale);
        }

        @Override
        public Project getProjectById(String id) {
            com.atlassian.connector.eclipse.internal.jira.core.model.Project project = delegate.getCache().getProjectById(id);
            return project != null ? createWrapper(Project.class, project) : null;
        }

        @Override
        public org.netbeans.modules.jira.client.spi.Project getProjectByKey(String key) {
            com.atlassian.connector.eclipse.internal.jira.core.model.Project project = delegate.getCache().getProjectByKey(key);
            return project != null ? createWrapper(Project.class, project) : null;
        }

        @Override
        public Project[] getProjects() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Project[] projects = delegate.getCache().getProjects();
            return convert(Project.class, new Project[projects.length], projects);
        }

        @Override
        public User getUser(String name) {
            com.atlassian.connector.eclipse.internal.jira.core.model.User user = delegate.getCache().getUser(name);
            return user != null ? createWrapper(User.class, user) : null;
        }

        @Override
        public JiraStatus[] getStatuses() {
            com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus[] statuses = delegate.getCache().getStatuses();
            return convert(JiraStatus.class, new JiraStatus[statuses.length], statuses);            
        }

        @Override
        public JiraStatus getStatusById(String id) {
            com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus status = delegate.getCache().getStatusById(id);
            return status != null ? createWrapper(JiraStatus.class, status) : null;
        }

        @Override
        public Priority getPriorityById(String id) {
            com.atlassian.connector.eclipse.internal.jira.core.model.Priority priority = delegate.getCache().getPriorityById(id);
            return priority != null ? createWrapper(Priority.class, priority) : null;
        }

        @Override
        public Resolution getResolutionById(String id) {
            com.atlassian.connector.eclipse.internal.jira.core.model.Resolution resolution = delegate.getCache().getResolutionById(id);
            return resolution != null ? createWrapper(Resolution.class, resolution) : null;
        }

        @Override
        public Priority[] getPriorities() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Priority[] priorities = delegate.getCache().getPriorities();
            return convert(Priority.class, new Priority[priorities.length], priorities);      
        }
        
        @Override
        public Resolution[] getResolutions() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Resolution[] resolutions = delegate.getCache().getResolutions();
            return convert(Resolution.class, new Resolution[resolutions.length], resolutions);      
        }

        @Override
        public IssueType getIssueTypeById(String id) {
            com.atlassian.connector.eclipse.internal.jira.core.model.IssueType issueType = delegate.getCache().getIssueTypeById(id);
            return issueType != null ? createWrapper(IssueType.class, issueType) : null;
        }

        @Override
        public IssueType[] getIssueTypes() {
            com.atlassian.connector.eclipse.internal.jira.core.model.IssueType[] issueTypes = delegate.getCache().getIssueTypes();
            return convert(IssueType.class, new IssueType[issueTypes.length], issueTypes);
        }

        @Override
        public String getServerVersion() {
            return delegate.getCache().getServerInfo().getVersion();
        }

        @Override
        public int getWorkHoursPerDay() {
            return delegate.getLocalConfiguration().getWorkHoursPerDay();
        }

        @Override
        public int getWorkDaysPerWeek() {
            return delegate.getLocalConfiguration().getWorkDaysPerWeek();
        }

        @Override
        public void refreshProjectDetails(String id) throws IOException {
            try {
                delegate.getCache().refreshProjectDetails(id, new NullProgressMonitor());
            } catch (JiraException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public boolean hasDetails() {
            return delegate.getCache().hasDetails();
        }

        @Override
        public void delete(String taskId) throws IOException {
            try {
                NullProgressMonitor npm = new NullProgressMonitor();
                String key = delegate.getKeyFromId(taskId, npm);
                delegate.deleteIssue(delegate.getIssueByKey(key, npm), npm);
            } catch (JiraException ex) {
                throw new IOException(ex);
            }
        }
    }

    // XXX reflect this as well    
    private class XmlRpcJiraWorkLog implements JiraWorkLog {
        private final com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog delegate;

        public XmlRpcJiraWorkLog() {
            delegate = new com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog();
        }
        
        public XmlRpcJiraWorkLog(com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog delegate) {
            this.delegate = delegate;
        }

        @Override
        public Date getStartDate() {
            return delegate.getStartDate();
        }

        @Override
        public String getAuthor() {
            return delegate.getAuthor();
        }

        @Override
        public long getTimeSpent() {
            return delegate.getTimeSpent();
        }

        @Override
        public String getComment() {
            return delegate.getComment();
        }

        @Override
        public JiraWorkLog.AdjustEstimateMethod getAdjustEstimate() {
            switch(delegate.getAdjustEstimate()) {
                case AUTO:
                    return AUTO;
                case LEAVE:
                    return LEAVE;
                case REDUCE:
                    return REDUCE;
                case SET:
                    return SET;
                default:
                    throw new IllegalStateException("not expected AdjustEstimateMethod");
            }
        }

        @Override
        public void setComment(String comment) {
            delegate.setComment(comment);
        }

        @Override
        public void setStartDate(Date startDate) {
            delegate.setStartDate(startDate);
        }

        @Override
        public void setTimeSpent(long timeSpent) {
            delegate.setTimeSpent(timeSpent);
        }

        @Override
        public void setAdjustEstimate(JiraWorkLog.AdjustEstimateMethod adjustEstimateMethod) {
            delegate.setAdjustEstimate(convert(adjustEstimateMethod));
        }

        @Override
        public void applyTo(TaskAttribute attribute) {
            getWorkLogConverter().applyTo(delegate, attribute);
        }
        
        private com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod convert(JiraWorkLog.AdjustEstimateMethod method) {
            switch(method) {
                 case AUTO:
                    return com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod.AUTO;
                case LEAVE:
                    return com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod.LEAVE;
                case REDUCE:
                    return com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod.REDUCE;
                case SET:
                    return com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod.SET;
                default:
                    throw new IllegalStateException("not expected AdjustEstimateMethod");
            }
        }
    }
    
}

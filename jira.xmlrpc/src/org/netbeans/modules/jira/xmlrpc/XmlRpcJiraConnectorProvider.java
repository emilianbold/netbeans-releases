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

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import com.atlassian.connector.eclipse.internal.jira.core.WorkLogConverter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
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
import org.netbeans.modules.jira.client.spi.DateFilter;
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

/**
 *
 * @author Tomas Stupka
 */
public class XmlRpcJiraConnectorProvider extends JiraConnectorProvider {

    private AbstractRepositoryConnector jrc;
    
    private final Map<TaskRepository, JiraClient> clients = new WeakHashMap<>(1);
    private JiraConstantsImpl jiraConstants;

    public XmlRpcJiraConnectorProvider() { }
    
        @Override
    public AbstractRepositoryConnector getRepositoryConnector() {
        if(jrc == null) {
            jrc = new JiraRepositoryConnector();
            MylynSupport.getInstance().addRepositoryListener(JiraClientFactory.getDefault());
        }
        return jrc;
    }

    @Override
    public JiraClient getClient(TaskRepository repo) {
        JiraClient c = clients.get(repo);
        if(c == null) {
            c = new JiraClientImpl(JiraClientFactory.getDefault().getJiraClient(repo));
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
        return new FilterDefinitionImpl(new com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition());
    }

    @Override
    public ContentFilter createContentFilter(String queryString, boolean searchSummary, boolean searchDescription, boolean searchEnvironment, boolean searchComments) {
        return new ContentFilterImpl(new com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter(queryString, searchSummary, searchDescription, searchEnvironment, searchComments));
    }

    @Override
    public void setQuery(TaskRepository taskRepository, IRepositoryQuery iquery, JiraFilter jiraFilter) {
        JiraUtil.setQuery(taskRepository, iquery, ((JiraFilterImpl<com.atlassian.connector.eclipse.internal.jira.core.model.JiraFilter>)jiraFilter).getDelegate());
    }
   
    @Override
    public JiraVersion createJiraVersion(String version) {
        return new JiraVersionImpl(new com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion(version));
    }

    @Override
    public ProjectFilter createProjectFilter(Project project) {
        return new ProjectFilterImpl(
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter(
                        ((ProjectImpl)project).getDelegate()));
    }
    
    @Override
    public ProjectFilter createProjectFilter(Project[] projects) {
        return new ProjectFilterImpl(
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter(
                        convert(
                            com.atlassian.connector.eclipse.internal.jira.core.model.Project.class, 
                            projects)));
    }

    @Override
    public UserFilter createNobodyFilter() {
        return new NobodyFilterImpl(new com.atlassian.connector.eclipse.internal.jira.core.model.filter.NobodyFilter());
    }

    @Override
    public UserFilter createCurrentUserFilter() {
        return new CurrentUserFilterImpl(new com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter());
    }

    @Override
    public UserFilter createSpecificUserFilter(String user) {
        return new SpecificUserFilterImpl(new com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter(user));
    }

    @Override
    public UserFilter createUserInGroupFilter(String group) {
        return new UserInGroupFilterImpl(new com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter(group));
    }

    @Override
    public DateRangeFilter createDateRangeFilter(Date from, Date to) {
        return new DateRangeFilterImpl(new com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter(from, to));
    }

    @Override
    public IssueTypeFilter createIssueTypeFilter(IssueType[] issueTypes) {
        return new IssueTypeFilterImpl(
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter(
                        convert(
                            com.atlassian.connector.eclipse.internal.jira.core.model.IssueType.class, 
                            issueTypes)));        
    }
    
    @Override
    public ComponentFilter createComponentFilter(Component[] components, boolean empty) {
        return new ComponentFilterImpl(
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter(
                        convert(
                            com.atlassian.connector.eclipse.internal.jira.core.model.Component.class, 
                            components),
                    empty));
    }

    @Override
    public VersionFilter createVersionFilter(Version[] versions, boolean empty, boolean b, boolean b0) {
        return new VersionFilterImpl(
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter(
                        convert(
                            com.atlassian.connector.eclipse.internal.jira.core.model.Version.class, 
                            versions),
                    empty, b, b0));
    }

    @Override
    public StatusFilter createStatusFilter(JiraStatus[] statuses) {
        return new StatusFilterImpl(
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter(
                        convert(
                            com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus.class, 
                            statuses)));
    }

    @Override
    public ResolutionFilter createResolutionFilter(Resolution[] resolutions) {
        return new ResolutionFilterImpl(
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter(
                        convert(
                            com.atlassian.connector.eclipse.internal.jira.core.model.Resolution.class, 
                            resolutions)));
    }

    @Override
    public PriorityFilter createPriorityFilter(Priority[] priorities) {
        return new PriorityFilterImpl(
                new com.atlassian.connector.eclipse.internal.jira.core.model.filter.PriorityFilter(
                        convert(
                            com.atlassian.connector.eclipse.internal.jira.core.model.Priority.class, 
                            priorities)));
    }

    @Override
    public EstimateVsActualFilter createEstimateVsActualFilter(long l, long l0) {
        return new EstimateVsActualFilterImpl(new com.atlassian.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter(l, l0));
    }
        
    @Override
    public synchronized JiraConstants getJiraConstants() {
        if(jiraConstants == null) {
            jiraConstants = new JiraConstantsImpl();
        }
        return jiraConstants;
    }

    @Override
    public JiraWorkLog createWorkLog() {
        return new RestJiraWorkLog();
    }

    @Override
    public JiraWorkLog createWorkLogFrom(TaskAttribute workLogTA) {
        return new RestJiraWorkLog(new WorkLogConverter().createFrom(workLogTA));
    }

    // <editor-fold desc="JiraClientImpl" defaultstate="collapsed">
    private class JiraClientImpl implements JiraClient {
        private final com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient delegate;
        
        public JiraClientImpl(com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof JiraClientImpl) {
                return delegate.equals(((JiraClientImpl)obj).delegate);
            }
            return false;
        }

        @Override
        public NamedFilter[] getNamedFilters() throws IOException {
            try {
                return convert(
                        NamedFilterImpl.class,
                        com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter.class,
                        delegate.getNamedFilters(new NullProgressMonitor()));
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
            com.atlassian.connector.eclipse.internal.jira.core.model.Project p = delegate.getCache().getProjectById(id);
            return p != null ? new ProjectImpl(p) : null;
        }

        @Override
        public Project getProjectByKey(String key) {
            com.atlassian.connector.eclipse.internal.jira.core.model.Project p = delegate.getCache().getProjectByKey(key);
            return p != null ? new ProjectImpl(p) : null;
        }

        @Override
        public Project[] getProjects() {
            return convert(
                    ProjectImpl.class, 
                    com.atlassian.connector.eclipse.internal.jira.core.model.Project.class,
                    delegate.getCache().getProjects());
        }

        @Override
        public User getUser(String name) {
            com.atlassian.connector.eclipse.internal.jira.core.model.User u = delegate.getCache().getUser(name);
            return u != null ? new UserImpl(u) : null;
        }

        @Override
        public JiraStatus[] getStatuses() {
            return convert(
                    JiraStatusImpl.class, 
                    com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus.class,
                    delegate.getCache().getStatuses());
        }

        @Override
        public JiraStatus getStatusById(String id) {
            com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus s = delegate.getCache().getStatusById(id);
            return s != null ? new JiraStatusImpl(s) : null;
        }

        @Override
        public Priority getPriorityById(String id) {
            com.atlassian.connector.eclipse.internal.jira.core.model.Priority p = delegate.getCache().getPriorityById(id);
            return p != null ? new PriorityImpl(p) : null;
        }

        @Override
        public Resolution getResolutionById(String id) {
            com.atlassian.connector.eclipse.internal.jira.core.model.Resolution r = delegate.getCache().getResolutionById(id);
            return r != null ? new ResolutionImpl(r) : null;
        }

        @Override
        public Priority[] getPriorities() {
            return convert(
                    PriorityImpl.class, 
                    com.atlassian.connector.eclipse.internal.jira.core.model.Priority.class,
                    delegate.getCache().getPriorities());
        }
        
        @Override
        public Resolution[] getResolutions() {
            return convert(
                    ResolutionImpl.class, 
                    com.atlassian.connector.eclipse.internal.jira.core.model.Resolution.class,
                    delegate.getCache().getResolutions());
        }

        @Override
        public IssueType getIssueTypeById(String id) {
            com.atlassian.connector.eclipse.internal.jira.core.model.IssueType it = delegate.getCache().getIssueTypeById(id);
            return it != null ? new IssueTypeImpl(it) : null;
        }

        @Override
        public IssueType[] getIssueTypes() {
            return convert(
                    IssueTypeImpl.class, 
                    com.atlassian.connector.eclipse.internal.jira.core.model.IssueType.class,
                    delegate.getCache().getIssueTypes());
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
    // </editor-fold>
    
    // <editor-fold desc="RestJiraWorkLog" defaultstate="collapsed">
    private class RestJiraWorkLog implements JiraWorkLog {
        private final com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog delegate;

        public RestJiraWorkLog() {
            delegate = new com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog();
        }
        
        public RestJiraWorkLog(com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog delegate) {
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
            new WorkLogConverter().applyTo(delegate, attribute);
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
    // </editor-fold>
    
    // <editor-fold desc="JiraConstantsImpl" defaultstate="collapsed">
    private class JiraConstantsImpl implements JiraConstants {

        @Override
        public String getATTRIBUTE_LINK_PREFIX() {
            return IJiraConstants.ATTRIBUTE_LINK_PREFIX;
        }

        @Override
        public String getMETA_TYPE() {
            return IJiraConstants.META_TYPE;
        }

        @Override
        public String getJiraAttribute_ISSUE_KEY_id() {
            return JiraAttribute.ISSUE_KEY.id();
        }

        @Override
        public String getJiraAttribute_SUMMARY_id() {
            return JiraAttribute.SUMMARY.id();
        }

        @Override
        public String getJiraAttribute_DESCRIPTION_id() {
            return JiraAttribute.DESCRIPTION.id();
        }

        @Override
        public String getJiraAttribute_PRIORITY_id() {
            return JiraAttribute.PRIORITY.id();
        }

        @Override
        public String getJiraAttribute_RESOLUTION_id() {
            return JiraAttribute.RESOLUTION.id();
        }

        @Override
        public String getJiraAttribute_PROJECT_id() {
            return JiraAttribute.PROJECT.id();
        }

        @Override
        public String getJiraAttribute_COMPONENTS_id() {
            return JiraAttribute.COMPONENTS.id();
        }

        @Override
        public String getJiraAttribute_AFFECTSVERSIONS_id() {
            return JiraAttribute.AFFECTSVERSIONS.id();
        }

        @Override
        public String getJiraAttribute_FIXVERSIONS_id() {
            return JiraAttribute.FIXVERSIONS.id();
        }

        @Override
        public String getJiraAttribute_ENVIRONMENT_id() {
            return JiraAttribute.ENVIRONMENT.id();
        }

        @Override
        public String getJiraAttribute_USER_REPORTER_id() {
            return JiraAttribute.USER_REPORTER.id();
        }

        @Override
        public String getJiraAttribute_USER_ASSIGNED_id() {
            return JiraAttribute.USER_ASSIGNED.id();
        }

        @Override
        public String getJiraAttribute_TYPE_id() {
            return JiraAttribute.TYPE.id();
        }

        @Override
        public String getJiraAttribute_CREATION_DATE_id() {
            return JiraAttribute.CREATION_DATE.id();
        }

        @Override
        public String getJiraAttribute_MODIFICATION_DATE_id() {
            return JiraAttribute.MODIFICATION_DATE.id();
        }

        @Override
        public String getJiraAttribute_DUE_DATE_id() {
            return JiraAttribute.DUE_DATE.id();
        }

        @Override
        public String getJiraAttribute_ESTIMATE_id() {
            return JiraAttribute.ESTIMATE.id();
        }

        @Override
        public String getJiraAttribute_INITIAL_ESTIMATE_id() {
            return JiraAttribute.INITIAL_ESTIMATE.id();
        }

        @Override
        public String getJiraAttribute_ACTUAL_id() {
            return JiraAttribute.ACTUAL.id();
        }

        @Override
        public String getJiraAttribute_PARENT_ID_id() {
            return JiraAttribute.PARENT_ID.id();
        }

        @Override
        public String getJiraAttribute_PARENT_KEY_id() {
            return JiraAttribute.PARENT_KEY.id();
        }

        @Override
        public String getJiraAttribute_SUBTASK_IDS_id() {
            return JiraAttribute.SUBTASK_IDS.id();
        }

        @Override
        public String getJiraAttribute_SUBTASK_KEYS_id() {
            return JiraAttribute.SUBTASK_KEYS.id();
        }

        @Override
        public String getATTRIBUTE_CUSTOM_PREFIX() {
            return IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX;
        }

        @Override
        public String getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW() {
            return WorkLogConverter.ATTRIBUTE_WORKLOG_NEW;
        }

        @Override
        public String getWorkLogConverter_TYPE_WORKLOG() {
            return WorkLogConverter.TYPE_WORKLOG;
        }

        @Override
        public String getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG() {
            return WorkLogConverter.ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG;
        }

        @Override
        public String getWorkLogConverter_AUTOR_key() {
            return WorkLogConverter.AUTOR.key();
        }

        @Override
        public String getWorkLogConverter_START_DATE_key() {
            return WorkLogConverter.START_DATE.key();
        }

        @Override
        public String getWorkLogConverter_TIME_SPENT_key() {
            return WorkLogConverter.TIME_SPENT.key();
        }

        @Override
        public String getWorkLogConverter_COMMENT_key() {
            return WorkLogConverter.COMMENT.key();
        }

        @Override
        public String getMETA_SUB_TASK_TYPE() {
            return IJiraConstants.META_SUB_TASK_TYPE;
        }

        @Override
        public String getPARENT_ID_id() {
            return JiraAttribute.PARENT_ID.id();
        }

        @Override
        public JiraVersion getMIN_VERSION() {
            return createJiraVersion(com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion.MIN_VERSION.toString());
        }
    }

    // </editor-fold>
    
    // <editor-fold desc="FilterDefinitionImpl" defaultstate="collapsed">
    private static class FilterDefinitionImpl extends JiraFilterImpl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition> implements FilterDefinition {

        public FilterDefinitionImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition delegate) {
            super(delegate);
        }

        @Override
        public void setProjectFilter(ProjectFilter projectFilter) {
            getDelegate().setProjectFilter(projectFilter != null ? ((ProjectFilterImpl)projectFilter).getDelegate() : null);
        }

        @Override
        public ProjectFilter getProjectFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter d = getDelegate().getProjectFilter();
            return d != null ? new ProjectFilterImpl(d) : null;
        }

        @Override
        public void setComponentFilter(ComponentFilter componentFilter) {
            getDelegate().setComponentFilter(componentFilter != null ? ((ComponentFilterImpl)componentFilter).getDelegate() : null);
        }

        @Override
        public ComponentFilter getComponentFilter() {
            com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter d = getDelegate().getComponentFilter();
            return d != null ? new ComponentFilterImpl(d) : null;
        }

        @Override
        public void setContentFilter(ContentFilter contentFilter) {
            getDelegate().setContentFilter(contentFilter != null ? ((ContentFilterImpl)contentFilter).getDelegate() : null);
        }

        @Override
        public ContentFilter getContentFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter d = getDelegate().getContentFilter();
            return d != null ? new ContentFilterImpl(d) : null;
        }

        @Override
        public void setIssueTypeFilter(IssueTypeFilter issueTypeFilter) {
            getDelegate().setIssueTypeFilter(issueTypeFilter != null ? ((IssueTypeFilterImpl)issueTypeFilter).getDelegate() : null);
        }

        @Override
        public IssueTypeFilter getIssueTypeFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter d = getDelegate().getIssueTypeFilter();
            return d != null ? new IssueTypeFilterImpl(d) : null;
        }

        @Override
        public void setAssignedToFilter(UserFilter assignedToFilter) {
            getDelegate().setAssignedToFilter(getUserFilter(assignedToFilter));
        }

        @Override
        public void setReportedByFilter(UserFilter reportedByFilter) {
            getDelegate().setReportedByFilter(getUserFilter(reportedByFilter));
        }

        private com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter getUserFilter(UserFilter uf) {
            return uf != null ? 
                    ((UserFilterImpl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter>)uf).getDelegate() 
                    : null;
        }
        
        @Override
        public UserFilter getAssignedToFilter() {
            return getUserFilter(getDelegate().getAssignedToFilter());
        }

        @Override
        public UserFilter getReportedByFilter() {
            return getUserFilter(getDelegate().getReportedByFilter());
        }

        private UserFilter getUserFilter(final com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter d) {
            if(d instanceof com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter) {
                return new SpecificUserFilterImpl((com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter)d);
            } else if(d instanceof com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter) {
                return new UserInGroupFilterImpl((com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter)d);
            } else if(d instanceof com.atlassian.connector.eclipse.internal.jira.core.model.filter.NobodyFilter) {
                return new NobodyFilterImpl((com.atlassian.connector.eclipse.internal.jira.core.model.filter.NobodyFilter)d);
            } else if(d instanceof com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter) {
                return new CurrentUserFilterImpl((com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter)d);
            } else if(d instanceof com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter) {
                return new UserFilterImpl((com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter)d);
            }
            return null;
        }
        
        @Override
        public void setPriorityFilter(PriorityFilter priorityFilter) {
            getDelegate().setPriorityFilter(priorityFilter != null ? ((PriorityFilterImpl)priorityFilter).getDelegate() : null);
        }

        @Override
        public PriorityFilter getPriorityFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.PriorityFilter d = getDelegate().getPriorityFilter();
            return d != null ? new PriorityFilterImpl(d) : null;
        }

        @Override
        public void setStatusFilter(StatusFilter statusFilter) {
            getDelegate().setStatusFilter(statusFilter != null ? ((StatusFilterImpl)statusFilter).getDelegate() : null);
        }

        @Override
        public StatusFilter getStatusFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter d = getDelegate().getStatusFilter();
            return d != null ? new StatusFilterImpl(d) : null;
        }

        @Override
        public void setResolutionFilter(ResolutionFilter resolutionFilter) {
            getDelegate().setResolutionFilter(resolutionFilter != null ? ((ResolutionFilterImpl)resolutionFilter).getDelegate() : null);
        }

        @Override
        public ResolutionFilter getResolutionFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter d = getDelegate().getResolutionFilter();
            return d != null ? new ResolutionFilterImpl(d) : null;
        }

        @Override
        public void setReportedInVersionFilter(VersionFilter reportedInVersionFilter) {
            getDelegate().setReportedInVersionFilter(reportedInVersionFilter != null ? ((VersionFilterImpl)reportedInVersionFilter).getDelegate() : null);
        }

        @Override
        public VersionFilter getReportedInVersionFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter d = getDelegate().getReportedInVersionFilter();
            return d != null ? new VersionFilterImpl(d) : null;
        }

        @Override
        public void setFixForVersionFilter(VersionFilter fixForVersionFilter) {
            getDelegate().setFixForVersionFilter(fixForVersionFilter != null ? ((VersionFilterImpl)fixForVersionFilter).getDelegate() : null);
        }

        @Override
        public VersionFilter getFixForVersionFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter d = getDelegate().getFixForVersionFilter();
            return d != null ? new VersionFilterImpl(d) : null;
        }

        @Override
        public EstimateVsActualFilter getEstimateVsActualFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter d = getDelegate().getEstimateVsActualFilter();
            return d != null ? new EstimateVsActualFilterImpl(d) : null;
        }

        @Override
        public void setEstimateVsActualFilter(EstimateVsActualFilter estimateVsActualFilter) {
            getDelegate().setEstimateVsActualFilter(estimateVsActualFilter != null ? ((EstimateVsActualFilterImpl)estimateVsActualFilter).getDelegate() : null);
        }

        @Override
        public DateFilter getCreatedDateFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateFilter d = getDelegate().getCreatedDateFilter();
            return d != null ? new DateRangeFilterImpl((com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter)d) : null;
        }

        @Override
        public void setCreatedDateFilter(DateFilter createdDateFilter) {
            getDelegate().setCreatedDateFilter(createdDateFilter != null ? ((DateRangeFilterImpl)createdDateFilter).getDelegate() : null);
        }

        @Override
        public DateFilter getDueDateFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateFilter d = getDelegate().getDueDateFilter();
            return d != null ? new DateRangeFilterImpl((com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter)d) : null;
        }

        @Override
        public void setDueDateFilter(DateFilter dueDateFilter) {
            getDelegate().setDueDateFilter(dueDateFilter != null ? ((DateRangeFilterImpl)dueDateFilter).getDelegate() : null);
        }

        @Override
        public DateFilter getUpdatedDateFilter() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateFilter d = getDelegate().getUpdatedDateFilter();
            return d != null ? new DateRangeFilterImpl((com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter)d) : null;
        }

        @Override
        public void setUpdatedDateFilter(DateFilter updatedDateFilter) {
            getDelegate().setUpdatedDateFilter(((DateRangeFilterImpl)updatedDateFilter).getDelegate());
        }

//        public Order[] getOrdering() {
//            return getDelegate().getOrdering();
//        }
//
//        public void setOrdering(Order[] ordering) {
//            getDelegate().setOrdering(ordering);
//        }
    }
    // </editor-fold>
    
    // <editor-fold desc="ProjectImpl" defaultstate="collapsed">
    private static class ProjectImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.Project> implements Project {

        public ProjectImpl(com.atlassian.connector.eclipse.internal.jira.core.model.Project delegate) {
            super(delegate);
        }
        
        public String getDescription() {
            return getDelegate().getDescription();
        }

        public void setDescription(String description) {
            getDelegate().setDescription(description);
        }

        @Override
        public String getId() {
            return getDelegate().getId();
        }

        public void setId(String id) {
            getDelegate().setId(id);
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        @Override
        public String getKey() {
            return getDelegate().getKey();
        }

        public void setKey(String key) {
            getDelegate().setKey(key);
        }

        public String getLead() {
            return getDelegate().getLead();
        }

        public void setLead(String lead) {
            getDelegate().setLead(lead);
        }

        public String getProjectUrl() {
            return getDelegate().getProjectUrl();
        }

        public void setProjectUrl(String projectUrl) {
            getDelegate().setProjectUrl(projectUrl);
        }

        public String getUrl() {
            return getDelegate().getUrl();
        }

        public void setUrl(String url) {
            getDelegate().setUrl(url);
        }

        public Component getComponent(String name) {
            return new ComponentImpl(getDelegate().getComponent(name));
        }

        @Override
        public Component[] getComponents() {
            return convert(
                    ComponentImpl.class, 
                    com.atlassian.connector.eclipse.internal.jira.core.model.Component.class, 
                    getDelegate().getComponents());
        }

        @Override
        public void setComponents(Component[] components) {
            getDelegate().setComponents(convert(
                com.atlassian.connector.eclipse.internal.jira.core.model.Component.class, 
                components));
        }

        public Version getVersion(String name) {
            return new VersionImpl(getDelegate().getVersion(name));
        }

        @Override
        public void setVersions(Version[] versions) {
            getDelegate().setVersions(convert(
                com.atlassian.connector.eclipse.internal.jira.core.model.Version.class, 
                versions));
        }

        public Version[] getArchivedVersions() {
            return convert(
                    VersionImpl.class, 
                    com.atlassian.connector.eclipse.internal.jira.core.model.Version.class, 
                    getDelegate().getArchivedVersions());
        }

        @Override
        public Version[] getVersions() {
            return convert(
                    VersionImpl.class, 
                    com.atlassian.connector.eclipse.internal.jira.core.model.Version.class, 
                    getDelegate().getVersions());
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ProjectImpl) {
                return getDelegate().equals(((ProjectImpl)obj).getDelegate());
            }
            return false;
        }

        @Override
        public int hashCode() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Project d = getDelegate();
            if (d != null) {
                return getDelegate().hashCode();
            }
            return super.hashCode();
        }

        @Override
        public String toString() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Project d = getDelegate();
            if (d != null) {
                return getDelegate().toString();
            }
            return super.toString();
        }

        @Override
        public IssueType[] getIssueTypes() {
            return convert(
                    IssueTypeImpl.class, 
                    com.atlassian.connector.eclipse.internal.jira.core.model.IssueType.class, 
                    getDelegate().getIssueTypes());
        }

        public void setIssueTypes(IssueType[] issueTypes) {
            getDelegate().setIssueTypes(convert(
                    com.atlassian.connector.eclipse.internal.jira.core.model.IssueType.class, 
                    issueTypes));
        }

//        public SecurityLevel[] getSecurityLevels() {
//            return delegate.getSecurityLevels();
//        }
//
//        public void setSecurityLevels(SecurityLevel[] securityLevels) {
//            delegate.setSecurityLevels(securityLevels);
//        }

        public void setDetails(boolean details) {
            getDelegate().setDetails(details);
        }

        @Override
        public boolean hasDetails() {
            return getDelegate().hasDetails();
        }

        public IssueType getIssueTypeById(String typeId) {
            return new IssueTypeImpl(getDelegate().getIssueTypeById(typeId));
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="ComponentImpl" defaultstate="collapsed">
    private static class ComponentImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.Component> implements Component {

        public ComponentImpl(com.atlassian.connector.eclipse.internal.jira.core.model.Component delegate) {
            super(delegate);
        }

        @Override
        public String getId() {
            return getDelegate().getId();
        }

        public void setId(String id) {
            getDelegate().setId(id);
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        @Override
        public String toString() {
            return getDelegate().toString();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ComponentImpl) {
                return getDelegate().equals(((ComponentImpl)obj).getDelegate());
            }
            return false;
        }

        @Override
        public int hashCode() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Component d = getDelegate();
            if(d != null) {
                return d.hashCode();
            }
            return super.hashCode();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="VersionImpl" defaultstate="collapsed">
    private static class VersionImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.Version> implements Version {

        public VersionImpl(com.atlassian.connector.eclipse.internal.jira.core.model.Version delegate) {
            super(delegate);
        }

        public boolean isArchived() {
            return getDelegate().isArchived();
        }

        public void setArchived(boolean archived) {
            getDelegate().setArchived(archived);
        }

        @Override
        public String getId() {
            return getDelegate().getId();
        }

        public void setId(String id) {
            getDelegate().setId(id);
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        public boolean isReleased() {
            return getDelegate().isReleased();
        }

        public void setReleased(boolean released) {
            getDelegate().setReleased(released);
        }

        public Date getReleaseDate() {
            return getDelegate().getReleaseDate();
        }

        public void setReleaseDate(Date releaseDate) {
            getDelegate().setReleaseDate(releaseDate);
        }

        public long getSequence() {
            return getDelegate().getSequence();
        }

        public void setSequence(long sequence) {
            getDelegate().setSequence(sequence);
        }
        
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof VersionImpl) {
                return getDelegate().equals(((VersionImpl)obj).getDelegate());
            }
            return false;
        }

        @Override
        public int hashCode() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.Version d = getDelegate();
            if (d != null) {
                return d.hashCode();
            }
            return super.hashCode();
        }

        @Override
        public String toString() {
            final com.atlassian.connector.eclipse.internal.jira.core.model.Version d = getDelegate();
            if (d != null) {
                return getDelegate().toString();
            }
            return super.toString();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="IssueTypeImpl" defaultstate="collapsed">
    private static class IssueTypeImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.IssueType> implements IssueType {

        public IssueTypeImpl(com.atlassian.connector.eclipse.internal.jira.core.model.IssueType delegate) {
            super(delegate);
        }

        public String getDescription() {
            return getDelegate().getDescription();
        }

        public void setDescription(String description) {
            getDelegate().setDescription(description);
        }

        public String getIcon() {
            return getDelegate().getIcon();
        }

        public void setIcon(String icon) {
            getDelegate().setIcon(icon);
        }

        @Override
        public String getId() {
            return getDelegate().getId();
        }

        public void setId(String id) {
            getDelegate().setId(id);
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        @Override
        public boolean isSubTaskType() {
            return getDelegate().isSubTaskType();
        }

        public void setSubTaskType(boolean subTaskType) {
            getDelegate().setSubTaskType(subTaskType);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof IssueTypeImpl) {
                return getDelegate().equals(((IssueTypeImpl)obj).getDelegate());
            }
            return false;
        }

        @Override
        public int hashCode() {
            com.atlassian.connector.eclipse.internal.jira.core.model.IssueType d = getDelegate();
            if(d != null) {
                return d.hashCode();
            }
            return super.hashCode();
        }

        @Override
        public String toString() {
            com.atlassian.connector.eclipse.internal.jira.core.model.IssueType d = getDelegate();
            if(d != null) {
                return d.toString();
            }
            return super.toString();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="JiraStatusImpl" defaultstate="collapsed">
    private static class JiraStatusImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus> implements JiraStatus {

        public JiraStatusImpl(com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus delegate) {
            super(delegate);
        }
        
        public String getDescription() {
            return getDelegate().getDescription();
        }

        public void setDescription(String description) {
            getDelegate().setDescription(description);
        }

        public String getIcon() {
            return getDelegate().getIcon();
        }

        public void setIcon(String icon) {
            getDelegate().setIcon(icon);
        }

        @Override
        public String getId() {
            return getDelegate().getId();
        }

        public void setId(String id) {
            getDelegate().setId(id);
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof JiraStatusImpl) {
                return getDelegate().equals(((JiraStatusImpl)obj).getDelegate());
            }
            return false;
        }

        @Override
        public int hashCode() {
            com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus d = getDelegate();
            if (d != null) {
                return d.hashCode();
            }
            return super.hashCode();
        }

        @Override
        public String toString() {
            com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus d = getDelegate();
            if (d != null) {
                return getDelegate().toString();
            }
            return super.toString();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="JiraVersionImpl" defaultstate="collapsed">
    private static class JiraVersionImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion> implements JiraVersion {

        public JiraVersionImpl(com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion delegate) {
            super(delegate);
        }

//        public boolean isSmallerOrEquals(JiraVersion v) {
//            return getDelegate().isSmallerOrEquals(((JiraVersionImpl)v).getDelegate());
//        }

        @Override
        public int compareTo(JiraVersion v) {
            return getDelegate().compareTo(((JiraVersionImpl)v).getDelegate());
        }
        
        @Override
        public String toString() {
            com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion d = getDelegate();
            if(d != null) {
                return d.toString();
            } 
            return super.toString();            
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof JiraVersionImpl) {
                return getDelegate().equals(((JiraVersionImpl)obj).getDelegate());
            } 
            return false;
        }

        @Override
        public int hashCode() {
            com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion d = getDelegate();
            if(d != null) {
                return d.hashCode();
            } 
            return super.hashCode();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="PriorityImpl" defaultstate="collapsed">
    private static class PriorityImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.Priority> implements Priority {

        public PriorityImpl(com.atlassian.connector.eclipse.internal.jira.core.model.Priority delegate) {
            super(delegate);
        }

        public String getDescription() {
            return getDelegate().getDescription();
        }

        public void setDescription(String description) {
            getDelegate().setDescription(description);
        }

        public String getIcon() {
            return getDelegate().getIcon();
        }

        public void setIcon(String icon) {
            getDelegate().setIcon(icon);
        }

        @Override
        public String getId() {
            return getDelegate().getId();
        }

        public void setId(String id) {
            getDelegate().setId(id);
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PriorityImpl) {
                return getDelegate().equals(((PriorityImpl)obj).getDelegate());
            }
            return false;
        }

        @Override
        public int hashCode() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Priority d = getDelegate();
            if (d != null) {
                return d.hashCode();
            }
            return super.hashCode();
        }

        @Override
        public String toString() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Priority d = getDelegate();
            if (d != null) {            
                return d.toString();
            }
            return super.toString();
        }
    }
    // </editor-fold>

    // <editor-fold desc="ResolutionImpl" defaultstate="collapsed">
    private static class ResolutionImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.Resolution> implements Resolution {

        public ResolutionImpl(com.atlassian.connector.eclipse.internal.jira.core.model.Resolution delegate) {
            super(delegate);
        }
        
        public String getDescription() {
            return getDelegate().getDescription();
        }

        public void setDescription(String description) {
            getDelegate().setDescription(description);
        }

        public String getIcon() {
            return getDelegate().getIcon();
        }

        public void setIcon(String icon) {
            getDelegate().setIcon(icon);
        }

        @Override
        public String getId() {
            return getDelegate().getId();
        }

        public void setId(String id) {
            getDelegate().setId(id);
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ResolutionImpl) {
                return getDelegate().equals(((ResolutionImpl)obj).getDelegate());
            }
            return false;
        }

        @Override
        public int hashCode() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Resolution d = getDelegate();
            if (d != null) {
                return d.hashCode();
            }
            return super.hashCode();
        }

        @Override
        public String toString() {
            com.atlassian.connector.eclipse.internal.jira.core.model.Resolution d = getDelegate();
            if (d != null) {
                return getDelegate().toString();
            }
            return super.toString();
        }
    }
    // </editor-fold>

    // <editor-fold desc="UserImpl" defaultstate="collapsed">
    private static class UserImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.User> implements User {

        public UserImpl(com.atlassian.connector.eclipse.internal.jira.core.model.User delegate) {
            super(delegate);
        }

        @Override
        public String getEmail() {
            return getDelegate().getEmail();
        }

        public void setEmail(String email) {
            getDelegate().setEmail(email);
        }

        @Override
        public String getFullName() {
            return getDelegate().getFullName();
        }

        public void setFullName(String fullName) {
            getDelegate().setFullName(fullName);
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        @Override
        public String toString() {
            com.atlassian.connector.eclipse.internal.jira.core.model.User d = getDelegate();
            if (d != null) {
                return getDelegate().toString();
            }
            return super.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof UserImpl) {
                return getDelegate().equals(((UserImpl)obj).getDelegate());
            }
            return false;
        }
        @Override
        public int hashCode() {
            com.atlassian.connector.eclipse.internal.jira.core.model.User d = getDelegate();
            if (d != null) {
                return d.hashCode();
            }
            return super.hashCode();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="NamedFilterImpl" defaultstate="collapsed">
    private static class NamedFilterImpl extends JiraFilterImpl<com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter> implements NamedFilter {

        public NamedFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter delegate) {
            super(delegate);
        }

        public String getDescription() {
            return getDelegate().getDescription();
        }

        public void setDescription(String description) {
            getDelegate().setDescription(description);
        }

        public String getId() {
            return getDelegate().getId();
        }

        public void setId(String id) {
            getDelegate().setId(id);
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        public String getAuthor() {
            return getDelegate().getAuthor();
        }

        public void setAuthor(String author) {
            getDelegate().setAuthor(author);
        }

        @Override
        public String toString() {
            return getDelegate().toString();
        }
    }
    // </editor-fold>

    // <editor-fold desc="EstimateVsActualFilterImpl" defaultstate="collapsed">
    private static class EstimateVsActualFilterImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter> implements EstimateVsActualFilter {

        public EstimateVsActualFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter delegate) {
            super(delegate);
        }
        
        @Override
        public long getMaxVariation() {
            return getDelegate().getMaxVariation();
        }

        @Override
        public long getMinVariation() {
            return getDelegate().getMinVariation();
        }
    }
    // </editor-fold>

    // <editor-fold desc="PriorityFilterImpl" defaultstate="collapsed">
    private static class PriorityFilterImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.PriorityFilter> implements PriorityFilter {
        public PriorityFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.PriorityFilter delegate) {
            super(delegate);
        }
        @Override
        public Priority[] getPriorities() {
            return convert(
                    PriorityImpl.class,
                    com.atlassian.connector.eclipse.internal.jira.core.model.Priority.class,
                    getDelegate().getPriorities());
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="ResolutionFilterImpl" defaultstate="collapsed">
    private static class ResolutionFilterImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter> implements ResolutionFilter {
        public ResolutionFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter delegate) {
            super(delegate);
        }
        
        @Override
        public Resolution[] getResolutions() {
            return convert(
                    ResolutionImpl.class,
                    com.atlassian.connector.eclipse.internal.jira.core.model.Resolution.class,
                    getDelegate().getResolutions());
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="StatusFilterImpl" defaultstate="collapsed">
    private static class StatusFilterImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter> implements StatusFilter {
        public StatusFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter delegate) {
            super(delegate);
        }
        @Override
        public JiraStatus[] getStatuses() {
            return convert(
                    JiraStatusImpl.class,
                    com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus.class,
                    getDelegate().getStatuses());
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="VersionFilterImpl" defaultstate="collapsed">
    private static class VersionFilterImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter> implements VersionFilter {
        public VersionFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter delegate) {
            super(delegate);
        }
        @Override
        public Version[] getVersions() {
            return convert(
                    VersionImpl.class,
                    com.atlassian.connector.eclipse.internal.jira.core.model.Version.class,
                    getDelegate().getVersions());
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="ProjectFilterImpl" defaultstate="collapsed">
    private static class ProjectFilterImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter> implements ProjectFilter {
        public ProjectFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter delegate) {
            super(delegate);
        }
        @Override
        public Project[] getProjects() {
            return convert(
                    ProjectImpl.class,
                    com.atlassian.connector.eclipse.internal.jira.core.model.Project.class,
                    getDelegate().getProjects());
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="ComponentFilterImpl" defaultstate="collapsed">
    private static class ComponentFilterImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter> implements ComponentFilter {
        public ComponentFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter delegate) {
            super(delegate);
        }
        @Override
        public Component[] getComponents() {
            return convert(
                    ComponentImpl.class,
                    com.atlassian.connector.eclipse.internal.jira.core.model.Component.class,
                    getDelegate().getComponents());
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="IssueTypeFilterImpl" defaultstate="collapsed">
    private static class IssueTypeFilterImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter> implements IssueTypeFilter {
        public IssueTypeFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter delegate) {
            super(delegate);
        }

        @Override
        public IssueType[] getIssueTypes() {
            return convert(
                    IssueTypeImpl.class,
                    com.atlassian.connector.eclipse.internal.jira.core.model.IssueType.class,
                    getDelegate().getIsueTypes());
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="DateFilterImpl" defaultstate="collapsed">
    private static class DateFilterImpl<D> extends Impl<D> implements DateFilter {
        public DateFilterImpl(D delegate) {
            super(delegate);
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="IssueTypeImpl" defaultstate="collapsed">
    private static class DateRangeFilterImpl extends DateFilterImpl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter> implements DateRangeFilter {
        public DateRangeFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter delegate) {
            super(delegate);
        }

        @Override
        public Date getFromDate() {
            return getDelegate().getFromDate();
        }

        @Override
        public Date getToDate() {
            return getDelegate().getToDate();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="UserFilterImpl" defaultstate="collapsed">
    private static class UserFilterImpl<D> extends Impl<D> implements UserFilter {
        public UserFilterImpl(D delegate) {
            super(delegate);
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="UserInGroupFilterImpl" defaultstate="collapsed">
    private static class UserInGroupFilterImpl extends UserFilterImpl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter> implements UserInGroupFilter {
        public UserInGroupFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter delegate) {
            super(delegate);
        }

        @Override
        public String getGroup() {
            return getDelegate().getGroup();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="SpecificUserFilterImpl" defaultstate="collapsed">
    private static class SpecificUserFilterImpl extends UserFilterImpl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter> implements SpecificUserFilter {
        public SpecificUserFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter delegate) {
            super(delegate);
        }

        @Override
        public String getUser() {
            return getDelegate().getUser();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="CurrentUserFilterImpl" defaultstate="collapsed">
    private static class CurrentUserFilterImpl extends UserFilterImpl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter> implements CurrentUserFilter {
        public CurrentUserFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter delegate) {
            super(delegate);
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="NobodyFilterImpl" defaultstate="collapsed">
    private static class NobodyFilterImpl extends UserFilterImpl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.NobodyFilter> implements NobodyFilter {
        public NobodyFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.NobodyFilter delegate) {
            super(delegate);
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="ContentFilterImpl" defaultstate="collapsed">
    private static class ContentFilterImpl extends Impl<com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter> implements ContentFilter {
        
        public ContentFilterImpl(com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter delegate) {
            super(delegate);
        }

        @Override
        public String getQueryString() {
            return getDelegate().getQueryString();
        }

        @Override
        public boolean isSearchingSummary() {
            return getDelegate().isSearchingSummary();
        }

        @Override
        public boolean isSearchingDescription() {
            return getDelegate().isSearchingDescription();
        }

        @Override
        public boolean isSearchingComments() {
            return getDelegate().isSearchingComments();
        }

        @Override
        public boolean isSearchingEnvironment() {
            return getDelegate().isSearchingEnvironment();
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="JiraFilterImpl<D>" defaultstate="collapsed">
    private static class JiraFilterImpl<D> extends Impl<D> implements JiraFilter {
        public JiraFilterImpl(D delegate) {
            super(delegate);
        }
    }
    // </editor-fold>
    
    private static <C> C[] convert(Class<C> toClass, Object[] d) {
        if(d == null) {
            return null;
        }
        Object a = Array.newInstance(toClass, d.length);
        for (int i = 0; i < d.length; i++) {
            try {
                assert d[i] instanceof Impl;
                Object del = ((Impl)d[i]).getDelegate();
                Array.set(a, i, del);
            } catch (SecurityException | IllegalArgumentException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        return (C[]) a;
    }

    private static <C, D> C[] convert(Class<C> toClass, Class<? extends D> fromClass, D[] d) {
        if(d == null) {
            return null;
        } 
        Object a = Array.newInstance(toClass, d.length);
        for (int i = 0; i < d.length; i++) {
            try {
                Array.set(a, i, convert(toClass, fromClass, d[i]));
            } catch (SecurityException | IllegalArgumentException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        return (C[]) a;
    }

    private static <C, D> C convert(Class<C> toClass, Class<? extends D> fromClass, D d) {
        if(d == null) {
            return null;
        }
        Constructor<C> c;
        try {
            c = toClass.getConstructor(fromClass);
            return c.newInstance(d);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return null;
    } 
}

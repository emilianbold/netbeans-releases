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

package org.netbeans.modules.jira.client.spi;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 *
 * @author Tomas Stupka
 */
public abstract class JiraConnectorProvider {

    protected static final Logger LOG = Logger.getLogger(JiraConnectorProvider.class.getName());

    public enum Type {
        XMLRPC("org.netbeans.modules.jira.xmlrpc", "JIRA XML-RPC"),
        REST("org.netbeans.modules.jira.rest", "JIRA REST");
        private final String cnb;
        private final String displayName;
        private Type(String cnb, String displayName) {
            this.cnb = cnb;
            this.displayName = displayName;
        }
        public String getCnb() {
            return cnb;
        }
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public interface JiraConnectorFactory {
        public JiraConnectorProvider create();
        public Type forType();
    } 

    public abstract AbstractRepositoryConnector getRepositoryConnector();
 
    public abstract JiraClient getClient(TaskRepository repo);

    public abstract void validateConnection(TaskRepository taskRepository) throws IOException;
 
    public abstract JiraConstants getJiraConstants();
    
    public abstract boolean isJiraException(Throwable t);
    
    public abstract boolean isJiraServiceUnavailableException(Throwable t);

    public abstract void setQuery(TaskRepository taskRepository, IRepositoryQuery iquery, JiraFilter fd);
    
    public abstract JiraWorkLog createWorkLog();
    public abstract JiraWorkLog createWorkLogFrom(TaskAttribute workLogTA);
    
    public abstract JiraVersion createJiraVersion(String version);
    
    public abstract ProjectFilter createProjectFilter(Project project);

    public abstract FilterDefinition createFilterDefinition();
    
    public abstract ContentFilter createContentFilter(
            String queryString, 
            boolean searchSummary, 
            boolean searchDescription,
            boolean searchEnvironment, 
            boolean searchComments);
    
    public abstract ProjectFilter createProjectFilter(Project[] toArray);

    public abstract UserFilter createNobodyFilter();

    public abstract UserFilter createCurrentUserFilter();

    public abstract UserFilter createSpecificUserFilter(String text);

    public abstract UserFilter createUserInGroupFilter(String text);

    public abstract DateRangeFilter createDateRangeFilter(Date from, Date to);

    public abstract IssueTypeFilter createIssueTypeFilter(IssueType[] toArray);

    public abstract ComponentFilter createComponentFilter(Component[] toArray, boolean empty);

    public abstract VersionFilter createVersionFilter(Version[] toArray, boolean empty, boolean b, boolean b0);

    public abstract StatusFilter createStatusFilter(JiraStatus[] toArray);

    public abstract ResolutionFilter createResolutionFilter(Resolution[] toArray);

    public abstract PriorityFilter createPriorityFilter(Priority[] toArray);

    public abstract EstimateVsActualFilter createEstimateVsActualFilter(long l, long l0);
    
    public interface JiraClient {

        public NamedFilter[] getNamedFilters() throws IOException;

        public void setDateTimePattern(String value);

        public void setDatePattern(String value);

        public void setLocale(Locale locale);

        public Project getProjectById(String id);

        public Project getProjectByKey(String key);
        
        public Project[] getProjects();

        public User getUser(String name);

        public JiraStatus[] getStatuses();

        public JiraStatus getStatusById(String id);

        public Priority[] getPriorities();

        public Priority getPriorityById(String id);

        public Resolution getResolutionById(String id);

        public Resolution[] getResolutions();

        public IssueType getIssueTypeById(String id);

        public IssueType[] getIssueTypes();

        public String getServerVersion();

        public int getWorkHoursPerDay();

        public int getWorkDaysPerWeek();

        public void refreshProjectDetails(String id) throws IOException;

        public boolean hasDetails();

        public void delete(String taskId) throws IOException;
        
    }
    
    protected static abstract class Impl<D> {
        private final D delegate;
        public Impl(D delegate) {
            this.delegate = delegate;
        }
        public D getDelegate() {
            return delegate;
        }
    }
    
}

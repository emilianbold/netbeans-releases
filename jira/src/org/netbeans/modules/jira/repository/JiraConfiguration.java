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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;
import org.netbeans.modules.jira.client.spi.Component;
import org.netbeans.modules.jira.client.spi.IssueType;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider.JiraClient;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.client.spi.JiraStatus;
import org.netbeans.modules.jira.client.spi.JiraVersion;
import org.netbeans.modules.jira.client.spi.Priority;
import org.netbeans.modules.jira.client.spi.Project;
import org.netbeans.modules.jira.client.spi.Resolution;
import org.netbeans.modules.jira.client.spi.User;
import org.netbeans.modules.jira.client.spi.Version;
import org.netbeans.modules.jira.util.PriorityComparator;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class JiraConfiguration {

    private JiraClient client;
    protected final JiraRepository repository;
    private List<Priority> priorities;

    public JiraConfiguration(JiraClient jiraClient, JiraRepository repository) {
        this.client = jiraClient;
        this.repository = repository;
        String value = System.getProperty("org.netbeans.modules.jira.datePattern"); // NOI18N
        if (value != null) {
            client.setDatePattern(value);
        }
        value = System.getProperty("org.netbeans.modules.jira.dateTimePattern"); // NOI18N
        if (value != null) {
            client.setDateTimePattern(value);
        }
        value = System.getProperty("org.netbeans.modules.jira.locale"); // NOI18N
        if (value != null) {
            StringTokenizer st = new StringTokenizer(value,"_"); // NOI18N
            String language = st.nextToken();
            String country = st.hasMoreTokens() ? st.nextToken() : ""; // NOI18N
            String variant = st.hasMoreTokens() ? st.nextToken() : ""; // NOI18N
            Locale locale = new Locale(language, country, variant);
            client.setLocale(locale);
        }
    }

    public IssueType getIssueTypeById(String id) {
        return client.getIssueTypeById(id);
    }

    public IssueType[] getIssueTypes() {
        return client.getIssueTypes();
    }

    public IssueType[] getIssueTypes(String projectId) {
        if(!supportsProjectIssueTypes(getServerVersion())) {
            return getIssueTypes();
        }
        return getProjectById(projectId).getIssueTypes();
    }

    public IssueType[] getIssueTypes(final Project project) {
        if(!supportsProjectIssueTypes(getServerVersion())) {
            return getIssueTypes();
        }
        ensureProjectLoaded(project);
        return project.getIssueTypes();
    }

    public Priority[] getPriorities() {
        List<Priority> ps = getPrioritiesIntern();
        return ps != null ? ps.toArray(new Priority[ps.size()]) : null;
    }
    
    public synchronized List<Priority> getPrioritiesIntern() {
        if(priorities == null) {
            Priority[] ps = client.getPriorities();
            if(ps != null) {
                Arrays.sort(ps, new PriorityComparator());
                priorities = Arrays.asList(ps);
            }
        }
        return priorities;
    }

    public Priority getPriorityById(String id) {
        return client.getPriorityById(id);
    }
    
    public int getPrioritySortOrder(Priority priority) {
        return getPrioritiesIntern().indexOf(priority);
    }

    public Resolution getResolutionById(String id) {
        return client.getResolutionById(id);
    }

    public Resolution[] getResolutions() {
        return client.getResolutions();
    }

    public String getServerVersion() {
        return client.getServerVersion();
    }
    
    public JiraStatus getStatusById(String id) {
        return client.getStatusById(id);
    }

    public JiraStatus[] getStatuses() {
        return client.getStatuses();
    }

    public User getUser(String name) {
        return client.getUser(name);
    }

    public Collection<User> getUsers() {
        return Collections.EMPTY_LIST; // XXX is there a way to get the users?
    }

    public Project getProjectById(String id) {
        return client.getProjectById(id);
    }

    public Project getProjectByKey(String key) {
        return client.getProjectByKey(key);
    }

    public Project[] getProjects() {
        return client.getProjects();
    }

    public Component[] getComponents(String projectId) {
        Component[] components = getProjectById(projectId).getComponents();
        return components != null ? components : new Component[0];
    }

    public Component[] getComponents(final Project project) {
        ensureProjectLoaded(project);
        return project.getComponents();
    }

    public Component getComponentById(String projectId, String componentId) {
        Component[] components = getComponents(projectId);
        if(components != null) {
            for (Component component : components) {
                if (componentId.equals(component.getId())) {
                    return component;
                }
            }
        }
        return null;
    }

    public Version[] getVersions(String projectId) {
        Version[] versions = getProjectById(projectId).getVersions();
        return versions != null ? versions : new Version[0];
    }

    public Version[] getVersions(final Project project) {
        ensureProjectLoaded(project);
        return project.getVersions();
    }

    public Version getVersionById(String projectId, String versionId) {
        Version[] versions = getVersions(projectId);
        if(versions != null) { 
            for (Version version : versions) {
                if (versionId.equals(version.getId())) {
                    return version;
                }
            }
        }
        return null;
    }

    public int getWorkDaysPerWeek() {
        return client.getWorkDaysPerWeek();
    }

    public int getWorkHoursPerDay() {
        return client.getWorkHoursPerDay();
    }

    public boolean supportsProjectIssueTypes(String version) {
        JiraConnectorProvider cp = JiraConnectorSupport.getInstance().getConnector();
        JiraVersion jv = cp.createJiraVersion(version);
        JiraVersion jv3_12 = cp.createJiraVersion("3.12");
        return jv.compareTo(jv3_12) > -1; 
    }

    public void ensureProjectLoaded(final Project project) {
        ensureProjectLoaded(project, false);
    }

    public void ensureProjectLoaded(final Project project, boolean force) {
        if(!force && project.hasDetails()) {
            return;
        }
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        BugtrackingCommand cmd = new BugtrackingCommand() {
            @Override
            public void execute() {
                try {
                    client.refreshProjectDetails(project.getId());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        repository.getExecutor().execute(cmd);
    }

    public void ensureIssueTypes(Project project) {
        if(!supportsProjectIssueTypes(getServerVersion())) {
            return;
        }
        if (project.getIssueTypes() == null) {
            // The cached project data had been created before we started
            // to use project specific issue types. Forcing reload.
            ensureProjectLoaded(project, true);
        }
    }

}

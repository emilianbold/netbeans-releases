/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.Group;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.User;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraClientCache;
import org.eclipse.mylyn.internal.jira.core.service.JiraClientData;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.commands.JiraCommand;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
// XXX rename - it actually the cache, not the configuration
public class JiraConfiguration extends JiraClientCache {

    private JiraClient client;
    private JiraRepository repository;
    private ConfigurationData data;
    private Set<String> loadedProjects = new HashSet<String>();

    private static final Object USER_LOCK = new Object();
    private static final Object PROJECT_LOCK = new Object();

    public JiraConfiguration(JiraClient jiraClient, JiraRepository repository) {
        super(jiraClient);
        this.client = jiraClient;
        this.repository = repository;        
    }

    protected void initialize() throws JiraException {
        NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();

        data = (ConfigurationData) getData();
        if(data.initialized) {
            return;
        }
        data.projects = client.getProjects(nullProgressMonitor);
        data.projectsById = new HashMap<String, Project>(data.projects.length);
        data.projectsByKey = new HashMap<String, Project>(data.projects.length);
        for (Project project : data.projects) {
            data.projectsById.put(project.getId(), project);
            data.projectsByKey.put(project.getKey(), project);
        }
        data.priorities = client.getPriorities(nullProgressMonitor);
        data.prioritiesById = new HashMap<String, Priority>(data.priorities.length);
        for (Priority priority : data.priorities) {
            data.prioritiesById.put(priority.getId(), priority);
        }
        data.resolutions = client.getResolutions(nullProgressMonitor);
        data.resolutionsById = new HashMap<String, Resolution>(data.resolutions.length);
        for (Resolution resolution : data.resolutions) {
            data.resolutionsById.put(resolution.getId(), resolution);
        }
        data.issueTypes = client.getIssueTypes(nullProgressMonitor);
        data.issueTypesById = new HashMap<String, IssueType>(data.issueTypes.length);
        for (IssueType type : data.issueTypes) {
            data.issueTypesById.put(type.getId(), type);
        }
        data.statuses = client.getStatuses(nullProgressMonitor);
        data.statusesById = new HashMap<String, JiraStatus>(data.statuses.length);
        for (JiraStatus status : data.statuses) {
            data.statusesById.put(status.getId(), status);
        }

        data.workDaysPerWeek = client.getConfiguration().getWorkDaysPerWeek();
        data.workHoursPerDay = client.getConfiguration().getWorkHoursPerDay();

        data.initialized = true;
        // XXX what else do we need?
        // XXX issue types by project

        hackJiraCache();
    }

    private void hackJiraCache() {
        try {
            Field f = client.getClass().getDeclaredField("cache");      // NOI18N
            f.setAccessible(true);
            f.set(client, this);
        } catch (IllegalArgumentException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean hasDetails() {
        return true; // always
    }

    @Override
    public JiraClientData getData() {
        if(data == null) {
             data = new ConfigurationData();
        }
        return data;
    }

    @Override
    public IssueType getIssueTypeById(String id) {
        return data.issueTypesById.get(id);
    }

    @Override
    public IssueType[] getIssueTypes() {
        return data.issueTypes;
    }

    @Override
    public Priority[] getPriorities() {
        return data.priorities;
    }

    @Override
    public Priority getPriorityById(String id) {
        return data.prioritiesById.get(id);
    }

    @Override
    public Resolution getResolutionById(String id) {
        return data.resolutionsById.get(id);
    }

    @Override
    public Resolution[] getResolutions() {
        return data.resolutions;
    }

    @Override
    public ServerInfo getServerInfo() {
        return data.serverInfo;
    }

    @Override
    public ServerInfo getServerInfo(IProgressMonitor monitor) throws JiraException {
        refreshServerInfo(monitor);
        return data.serverInfo;
    }

    @Override
    public JiraStatus getStatusById(String id) {
        return data.statusesById.get(id);
    }

    @Override
    public JiraStatus[] getStatuses() {
        return data.statuses;
    }

    @Override
    public User getUser(String name) {
        synchronized(USER_LOCK) {
            return data.usersByName.get(name);
        }
    }

    @Override
    public User putUser(String name, String fullName) {
	User user = new User();
        user.setName(name);
        user.setFullName(fullName);
        synchronized(USER_LOCK) {
            data.usersByName.put(name, user);
        }
        return user;
    }

    /**
     * This method should not EVER be called
     */
    @Override
    public synchronized void refreshDetails(IProgressMonitor monitor) throws JiraException {
        assert false;
    }

    @Override
    public synchronized void refreshServerInfo(IProgressMonitor monitor) throws JiraException {
        data.serverInfo = client.getServerInfo(monitor);
    }

    @Override
    public void setData(JiraClientData data) {
        this.data = (ConfigurationData) data;
    }

    @Override
    public Project getProjectById(String id) {
        synchronized(PROJECT_LOCK) {
            Project project = data.projectsById.get(id);
            ensureProjectLoaded(project);
            return project;
        }
    }

    @Override
    public Project getProjectByKey(String key) {
        synchronized(PROJECT_LOCK) {
            Project project = data.projectsByKey.get(key);
            ensureProjectLoaded(project);
            return project;
        }
    }

    @Override
    public Project[] getProjects() {
        return data.projects;
    }

    public void ensureProjectLoaded(Project project) {
        if (!loadedProjects.contains(project.getId())) {
            initProject(project);
            loadedProjects.add(project.getId());
        }
    }

    protected void initProject(final Project project) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        JiraCommand cmd = new JiraCommand() {
            @Override
            public void execute() throws JiraException, CoreException, IOException, MalformedURLException {
                Component[] components = client.getComponents(project.getKey(), new NullProgressMonitor());
                project.setComponents(components);

                Version[] versions = client.getVersions(project.getKey(), new NullProgressMonitor());
                project.setVersions(versions);

                // XXX what else !!!

            }
        };
        repository.getExecutor().execute(cmd);
    }

    public Component[] getComponents(String projectId) {
        return getProjectById(projectId).getComponents();
    }

    public Component[] getComponents(final Project project) {
        synchronized(PROJECT_LOCK) {
            ensureProjectLoaded(project);
            return project.getComponents();
        }
    }

    public Component getComponentById(String projectId, String componentId) {
        for (Component component : getComponents(projectId)) {
            if (componentId.equals(component.getId())) {
                return component;
            }
        }
        return null;
    }

    public Version[] getVersions(String projectId) {
        return getProjectById(projectId).getVersions();
    }

    public Version[] getVersions(final Project project) {
        synchronized(PROJECT_LOCK) {
            ensureProjectLoaded(project);
            return project.getVersions();
        }
    }

    public Version getVersionById(String projectId, String versionId) {
        for (Version version : getVersions(projectId)) {
            if (versionId.equals(version.getId())) {
                return version;
            }
        }
        return null;
    }

    public int getWorkDaysPerWeek() {
        return data.workDaysPerWeek;
    }

    public int getWorkHoursPerDay() {
        return data.workHoursPerDay;
    }

    protected class ConfigurationData extends JiraClientData {
	Group[] groups = new Group[0];
	IssueType[] issueTypes = new IssueType[0];
	Map<String, IssueType> issueTypesById = new HashMap<String, IssueType>();
	Priority[] priorities = new Priority[0];
	Map<String, Priority> prioritiesById = new HashMap<String, Priority>();
	Project[] projects = new Project[0];
	Map<String, Project> projectsById = new HashMap<String, Project>();
	Map<String, Project> projectsByKey = new HashMap<String, Project>();
	Resolution[] resolutions = new Resolution[0];
	Map<String, Resolution> resolutionsById = new HashMap<String, Resolution>();
	volatile ServerInfo serverInfo;
	JiraStatus[] statuses = new JiraStatus[0];
	Map<String, JiraStatus> statusesById = new HashMap<String, JiraStatus>();
	User[] users = new User[0];
	Map<String, User> usersByName = new HashMap<String, User>();
        int workDaysPerWeek;
        int workHoursPerDay;
	long lastUpdate;
        boolean initialized = false;
    }

}

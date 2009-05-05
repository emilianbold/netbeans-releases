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
import java.lang.reflect.Constructor;
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
public class JiraConfiguration extends JiraClientCache {

    private JiraClient client;
    private JiraRepository repository;
    protected LazyData data;
    private Set<String> loadedProjects = new HashSet<String>();

    private static final Object USER_LOCK = new Object();
    private static final Object PROJECT_LOCK = new Object();

    // XXX might be we want it private
    protected JiraConfiguration(JiraClient jiraClient, JiraRepository repository) {
        super(jiraClient);
        this.client = jiraClient;
        this.repository = repository;
        data = new LazyData();
    }

    public static <T extends JiraConfiguration> T create(final JiraRepository repository, final Class<T> clazz) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        // XXX need logging incl. consumed time

        class ConfigurationCommand extends JiraCommand {

            private T configuration;

            @Override
            public void execute() throws JiraException, CoreException, IOException, MalformedURLException {

                final JiraClient client = Jira.getInstance().getClient(repository.getTaskRepository());

                try {
                    Constructor<T> c = clazz.getDeclaredConstructor(JiraClient.class, JiraRepository.class);
                    c.setAccessible(true);
                    configuration = c.newInstance(client, repository);
                } catch (Exception ex) {
                    Jira.LOG.log(Level.SEVERE, null, ex);
                    return;
                }

                NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();

                configuration.data.projects = client.getProjects(nullProgressMonitor);
                configuration.data.projectsById = new HashMap<String, Project>(configuration.data.projects.length);
                configuration.data.projectsByKey = new HashMap<String, Project>(configuration.data.projects.length);
                for (Project project : configuration.data.projects) {
                    configuration.data.projectsById.put(project.getId(), project);
                    configuration.data.projectsByKey.put(project.getKey(), project);
                }
                configuration.data.priorities = client.getPriorities(nullProgressMonitor);
                configuration.data.prioritiesById = new HashMap<String, Priority>(configuration.data.priorities.length);
                for (Priority priority : configuration.data.priorities) {
                    configuration.data.prioritiesById.put(priority.getId(), priority);
                }
                configuration.data.resolutions = client.getResolutions(nullProgressMonitor);
                configuration.data.resolutionsById = new HashMap<String, Resolution>(configuration.data.resolutions.length);
                for (Resolution resolution : configuration.data.resolutions) {
                    configuration.data.resolutionsById.put(resolution.getId(), resolution);
                }
                configuration.data.issueTypes = client.getIssueTypes(nullProgressMonitor);
                configuration.data.issueTypesById = new HashMap<String, IssueType>(configuration.data.issueTypes.length);
                for (IssueType type : configuration.data.issueTypes) {
                    configuration.data.issueTypesById.put(type.getId(), type);
                }
                configuration.data.statuses = client.getStatuses(nullProgressMonitor);
                configuration.data.statusesById = new HashMap<String, JiraStatus>(configuration.data.statuses.length);
                for (JiraStatus status : configuration.data.statuses) {
                    configuration.data.statusesById.put(status.getId(), status);
                }
                // XXX what else do we need?
                // XXX issue types by project

                hackJiraCache(client);
            }

            private void hackJiraCache(JiraClient client) {
                try {
                    Field f = client.getClass().getDeclaredField("cache");      // NOI18N
                    f.setAccessible(true);
                    f.set(client, configuration);
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
        }
        ConfigurationCommand cmd = new ConfigurationCommand();

        repository.getExecutor().execute(cmd, true, false);
        if(!cmd.hasFailed()) {
            return cmd.configuration;
        }
        return null;
    }
    @Override
    public boolean hasDetails() {
        return true; // always
    }

    @Override
    public JiraClientData getData() {
        if(data == null) {
             data = new LazyData();
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

    @Override
    public synchronized void refreshDetails(IProgressMonitor monitor) throws JiraException {
        // ignore
    }

    @Override
    public synchronized void refreshServerInfo(IProgressMonitor monitor) throws JiraException {
        data.serverInfo = client.getServerInfo(monitor);
    }

    @Override
    public void setData(JiraClientData data) {
        this.data = (LazyData) data;
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

    protected class LazyData extends JiraClientData {
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
	long lastUpdate;
    }

}

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
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraTunnel;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.commands.JiraCommand;

/**
 *
 * @author Tomas Stupka
 */
public class JiraConfiguration {

    private Project[] projects;
    private Priority[] priorities;
    private IssueType[] types;
    private Resolution[] resolutions;
    private JiraStatus[] statuses;

    private JiraRepository repository;
    private JiraClient client;
    private Map<String, Priority> prioritiesMap;
    private Map<String, Project> projectsMap;
    private Map<String, IssueType> typesMap;
    private Map<String, Resolution> resolutionsMap;
    private Map<String, JiraStatus> statusMap;

    private final static Object INIT_LOCK = new Object();

    private JiraConfiguration(JiraRepository repository) {
        this.repository = repository;
    }

    public static JiraConfiguration create(final JiraRepository repository) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final JiraConfiguration configuration = new JiraConfiguration(repository);

        JiraCommand cmd = new JiraCommand() {
            @Override
            public void execute() throws JiraException, CoreException, IOException, MalformedURLException {
                JiraClient client = Jira.getInstance().getClient(repository.getTaskRepository());
                configuration.client = client;

                NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();

                configuration.projects = client.getProjects(nullProgressMonitor);
                configuration.projectsMap = new HashMap<String, Project>(configuration.projects.length);
                for (Project project : configuration.projects) {
                    configuration.projectsMap.put(project.getId(), project);
                }
                configuration.priorities = client.getPriorities(nullProgressMonitor);
                configuration.prioritiesMap = new HashMap<String, Priority>(configuration.priorities.length);
                for (Priority priority : configuration.priorities) {
                    configuration.prioritiesMap.put(priority.getId(), priority);
                }
                configuration.resolutions = client.getResolutions(nullProgressMonitor);
                configuration.resolutionsMap = new HashMap<String, Resolution>(configuration.resolutions.length);
                for (Resolution resolution : configuration.resolutions) {
                    configuration.resolutionsMap.put(resolution.getId(), resolution);
                }
                configuration.types = client.getIssueTypes(nullProgressMonitor);
                configuration.typesMap = new HashMap<String, IssueType>(configuration.types.length);
                for (IssueType type : configuration.types) {
                    configuration.typesMap.put(type.getId(), type);
                }                
                configuration.statuses = client.getStatuses(nullProgressMonitor);
                configuration.statusMap = new HashMap<String, JiraStatus>(configuration.statuses.length);
                for (JiraStatus status : configuration.statuses) {
                    configuration.statusMap.put(status.getId(), status);
                }

                // XXX what else do we need?
                // XXX issue types by project

            }
        };
        repository.getExecutor().execute(cmd);
        if(!cmd.hasFailed()) {
            configuration.initJiraCache();
            return configuration;
        }
        return null;
    }

    public Priority[] getPriorities() {
        return priorities;
    }

    public Project[] getProjects() {
        return projects;
    }

    public Resolution[] getResolutions() {
        return resolutions;
    }

    public IssueType[] getTypes() {
        return types;
    }

    public JiraStatus[] getStatuses() {
        return statuses;
    }

    public Priority getPriority(String id) {
        return prioritiesMap.get(id);
    }

    public Project getProject(String id) {
        return projectsMap.get(id);
    }

    public Resolution getResolution(String id) {
        return resolutionsMap.get(id);
    }

    public IssueType getType(String id) {
        return typesMap.get(id);
    }

    public JiraStatus getStatus(String id) {
        return statusMap.get(id);
    }

    public Component[] getComponents(String projectId) {
        return getComponents(getProject(projectId));
    }

    public Component[] getComponents(final Project project) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        synchronized(INIT_LOCK) {
            Component[] components = project.getComponents();
            if(components != null) {
                return components;
            }
            initProject(project);
        }

        return project.getComponents();
    }

    public Version[] getVersions(String projectId) {
        return getVersions(getProject(projectId));
    }

    public Version[] getVersions(final Project project) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        synchronized(INIT_LOCK) {
            Version[] versions = project.getVersions();
            if(versions != null) {
                return versions;
            }
            initProject(project);
        }

        return project.getVersions();
    }

    public void initilize(Project project) {
        synchronized(INIT_LOCK) {
            initProject(project);
        }
    }

    private void initJiraCache() {
        JiraTunnel.init(client, types, statuses, priorities, resolutions);
    }

    private void initProject(final Project project) {
        JiraCommand cmd = new JiraCommand() {
            @Override
            public void execute() throws JiraException, CoreException, IOException, MalformedURLException {
                Component[] components = client.getComponents(project.getKey(), new NullProgressMonitor());
                project.setComponents(components);

                Version[] versions = client.getVersions(project.getKey(), new NullProgressMonitor());
                project.setVersions(versions);

                patchJiraCache(project);
            }
        };
        repository.getExecutor().execute(cmd);
    }

    private void patchJiraCache(Project project) throws JiraException {
        JiraTunnel.patchProjects(
                Jira.getInstance().getClient(repository.getTaskRepository()),
                new Project[] {project});
    }


}

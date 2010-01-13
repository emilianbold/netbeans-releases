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

import java.util.Collection;
import java.util.Collections;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.User;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
// XXX rename - it actually the cache, not the configuration
// XXX Project MUST be somehow refreshed when a list of components changes on a server
public class JiraConfiguration {

    private JiraClient client;

    private static final Object PROJECT_LOCK = new Object();

    public JiraConfiguration(JiraClient jiraClient, JiraRepository repository) {
        this.client = jiraClient;
    }

    public IssueType getIssueTypeById(String id) {
        return client.getCache().getIssueTypeById(id);
    }

    public IssueType[] getIssueTypes() {
        return client.getCache().getIssueTypes();
    }

    public IssueType[] getIssueTypes(String projectId) {
        if(!supportsProjectIssueTypes(getServerInfo().getVersion())) {
            return getIssueTypes();
        }
        return getProjectById(projectId).getIssueTypes();
    }

    public IssueType[] getIssueTypes(final Project project) {
        if(!supportsProjectIssueTypes(getServerInfo().getVersion())) {
            return getIssueTypes();
        }
        return project.getIssueTypes();
    }

    public Priority[] getPriorities() {
        return client.getCache().getPriorities();
    }

    public Priority getPriorityById(String id) {
        return client.getCache().getPriorityById(id);
    }

    public Resolution getResolutionById(String id) {
        return client.getCache().getResolutionById(id);
    }

    public Resolution[] getResolutions() {
        return client.getCache().getResolutions();
    }

    public ServerInfo getServerInfo() {
        return client.getCache().getServerInfo();
    }

    public ServerInfo getServerInfo(IProgressMonitor monitor) throws JiraException {
        return client.getCache().getServerInfo(monitor);
    }

    public JiraStatus getStatusById(String id) {
        return client.getCache().getStatusById(id);
    }

    public JiraStatus[] getStatuses() {
        return client.getCache().getStatuses();
    }

    public User getUser(String name) {
        return client.getCache().getUser(name);
    }

    public Collection<User> getUsers() {
        return Collections.EMPTY_LIST; // XXX is there a way to get the users?
    }

    public Project getProjectById(String id) {
        return client.getCache().getProjectById(id);
    }

    public Project getProjectByKey(String key) {
        return client.getCache().getProjectByKey(key);
    }

    public Project[] getProjects() {
        return client.getCache().getProjects();
    }

    public Component[] getComponents(String projectId) {
        return getProjectById(projectId).getComponents();
    }

    public Component[] getComponents(final Project project) {
        return project.getComponents();
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
        return project.getVersions();
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
        return client.getConfiguration().getWorkDaysPerWeek();
    }

    public int getWorkHoursPerDay() {
        return client.getConfiguration().getWorkHoursPerDay();
    }

    public boolean supportsProjectIssueTypes(String version) {
        return new JiraVersion(version).compareTo(JiraVersion.JIRA_3_12) > -1;
    }

}

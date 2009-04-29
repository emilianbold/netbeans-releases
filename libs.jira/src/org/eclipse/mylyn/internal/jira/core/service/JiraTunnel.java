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

package org.eclipse.mylyn.internal.jira.core.service;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;

/**
 * Initializing the JiraCache for huge repositories (e.g Kenai.com) lasts too long.
 * Its quite less expesive to retrieve the needed project data in an on demand manner.
 *
 * @author Tomas Stupka
 */
public class JiraTunnel {
    public static void init(JiraClient client, IssueType[] types, JiraStatus[] statuses, Priority[] priorities, Resolution[] resolutions) {
        JiraClientData data = client.getCache().getData();
        data.lastUpdate = System.currentTimeMillis();
        data.priorities = priorities;
        for (Priority priority : priorities) {
            data.prioritiesById.put(priority.getId(), priority);
        }
        data.statuses = statuses;
        for (JiraStatus status : statuses) {
            data.statusesById.put(status.getId(), status);
        }
        data.resolutions = resolutions;
        for (Resolution resolution : resolutions) {
            data.resolutionsById.put(resolution.getId(), resolution);
        }
        data.issueTypes = types;
        for (IssueType type : types) {
            data.issueTypesById.put(type.getId(), type);
        }
        data.lastUpdate = System.currentTimeMillis();
    }

    public static void patchProjects(JiraClient client, Project[] addProjects) {
        JiraClientData data = client.getCache().getData();

        Project[] projects = data.projects;
        List<Project> l = new ArrayList();
        for (Project addProject : addProjects) {
            boolean found = false;
            for (Project p : projects) {
                if(addProject.getId().equals(p.getId())) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                l.add(addProject);
            }
        }
        if(l.size() == 0) {
            return;
        }
        addProjects = l.toArray(new Project[l.size()]);
        Project[] projects2 = new Project[projects.length + addProjects.length];
        System.arraycopy(projects, 0, projects2, 0, projects.length);
        System.arraycopy(addProjects, 0, projects2, projects.length, addProjects.length);
        data.projects = projects2;
        for (Project project : addProjects) {
            data.projectsById.put(project.getId(), project);
            data.projectsByKey.put(project.getKey(), project);
        }
    }
}

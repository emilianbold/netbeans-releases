/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.tasks.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.tasks.ui.dashboard.DashboardViewer;

/**
 *
 * @author jpeska
 */
public class Category {

    private String name;
    private List<Issue> tasks;
    private boolean loaded;

    public Category(String name, List<Issue> tasks) {
        this(name, tasks, true);
    }

    public Category(String name) {
        this(name, new ArrayList<Issue>(), false);
    }

    public Category(String name, List<Issue> tasks, boolean loaded) {
        this.name = name;
        this.tasks = tasks;
        this.loaded = loaded;
    }

    public void removeTask(Issue task) {
        tasks.remove(task);
    }

    public void addTask(Issue task) {
        tasks.add(task);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Issue> getTasks() {
        return tasks;
    }

    public void setTasks(List<Issue> tasks) {
        if (!loaded && tasks != null) {
            loaded = true;
        }
        this.tasks = tasks;
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Category other = (Category) obj;
        return name.equalsIgnoreCase(other.name);
    }
    
    public void refresh(){
        if (loaded) {
            refreshTasks();
        } else {
            DashboardViewer.getInstance().loadCategory(this);
        }
    }

    private void refreshTasks() {
        Map<Repository, List<String>> map = getTasksToRepository(this.getTasks());
        Set<Repository> repositoryKeys = map.keySet();
        for (Repository repository : repositoryKeys) {
            List<String> ids = map.get(repository);
            repository.getIssues(ids.toArray(new String[ids.size()]));
        }
    }

    private Map<Repository, List<String>> getTasksToRepository(List<Issue> tasks) {
        Map<Repository, List<String>> map = new HashMap<Repository, List<String>>();
        for (Issue issue : tasks) {
            Repository repositoryKey = issue.getRepository();
            if (map.containsKey(repositoryKey)) {
                map.get(repositoryKey).add(issue.getID());
            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(issue.getID());
                map.put(repositoryKey, list);
            }
        }
        return map;
    }
}

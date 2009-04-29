/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.jira.JiraTestUtil.TestTaskDataCollector;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;

/**
 *
 * @author tomas
 */
public class JiraPerTest extends NbTestCase {
    private static JiraClient client;
    private static JiraRepositoryConnector jrc;
    private static JiraRepository repository;

    public JiraPerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        JiraCorePlugin jcp = new JiraCorePlugin();
        try {
            jcp.start(null);
        } catch (Exception ex) {
            throw ex;
        }
        WebUtil.init();
        // need this to initialize cache -> server defined status values & co

    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testJira() throws Throwable {
//        getClient().getCache().refreshServerInfo(JiraTestUtil.nullProgressMonitor);
        System.out.println("getProject");
        Project p = getProject("Spring Batch");
        System.out.println("refresh cache");
        getClient().getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);


        System.out.println(" ==================== ");
        long t = System.currentTimeMillis();
        list(p);
        long now = System.currentTimeMillis();
        System.out.println(now - t);
        t = now;
        listJiraIssues(p);
        now = System.currentTimeMillis();
        System.out.println(now - t);
        System.out.println(" ==================== ");
    }

    public static JiraClient getClient() {
        if(client == null) {
            client = JiraClientFactory.getDefault().getJiraClient(getRepository().getTaskRepository());
        }
        return client;
    }

    public static JiraRepositoryConnector getRepositoryConnector() {
        if(jrc == null) {
            jrc = new JiraRepositoryConnector();
        }
        return jrc;
    }

    public void list(Project project) throws Throwable {

        FilterDefinition fd = new FilterDefinition();
        fd.setProjectFilter(new ProjectFilter(project));
        final RepositoryQuery repositoryQuery = new RepositoryQuery(getRepositoryConnector().getConnectorKind(), "query");
        JiraUtil.setQuery(repository.getTaskRepository(), repositoryQuery, fd);
        TestTaskDataCollector tdc = new TestTaskDataCollector();
        getRepositoryConnector().performQuery(repository.getTaskRepository(), repositoryQuery, tdc, null, JiraTestUtil.nullProgressMonitor);

        assertTrue(tdc.data.size() > 0);
    }

    public void listJiraIssues(Project project) throws Throwable {

        FilterDefinition fd = new FilterDefinition();
        fd.setProjectFilter(new ProjectFilter(project));
        JiraCollector jc = new JiraCollector();
        getClient().findIssues(fd, jc, JiraTestUtil.nullProgressMonitor);

        assertTrue(jc.issues.size() > 0);
    }

    public static JiraRepository getRepository() {
        if(repository == null) {
            repository = new JiraRepository("dil", "http://jira.springframework.org/", "rigoroz", "rigoroz", null, null);
        }
        return repository;
    }

    public static Project getProject(String name) throws JiraException {
        Project[] projects = getClient().getProjects(JiraTestUtil.nullProgressMonitor);
        for (Project p : projects) {
            return p;
//            if (p.getKey().equals(name)) {
//                return p;
//            }
        }
        return null;
    }

    private class JiraCollector implements IssueCollector {
        private List<org.eclipse.mylyn.internal.jira.core.model.JiraIssue> issues = new ArrayList<org.eclipse.mylyn.internal.jira.core.model.JiraIssue>();
        public void start() { }
        public void done() { }
        public void collectIssue(org.eclipse.mylyn.internal.jira.core.model.JiraIssue issue) {
            issues.add(issue);
        }
        public boolean isCancelled() {
            return false; // XXX
        }
        public int getMaxHits() {
            return -1; // XXX
        }
    }
}

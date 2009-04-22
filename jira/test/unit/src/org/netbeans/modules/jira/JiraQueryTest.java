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

import org.netbeans.modules.jira.repository.JiraRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.Issue;

/**
 *
 * @author tomas
 */
public class JiraQueryTest extends NbTestCase {

    public JiraQueryTest(String arg0) {
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
        // need this to initialize cache -> server defined status values & co
//        getClient().getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);
        JiraTestUtil.cleanProject(getRepositoryConnector(), getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()));
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    public void testQuery() throws Throwable {
        try {
            List<String> ids = new ArrayList<String>();
            ids.add(createIssue("1"));
            ids.add(createIssue("2"));

            FilterDefinition fd = new FilterDefinition();
            fd.setProjectFilter(new ProjectFilter(JiraTestUtil.getProject(getClient())));
            JiraQuery q = new JiraQuery(getRepositoryConnector(), new JiraRepository(getTaskRepository()));
            q.setFilterDefinition(fd);
            boolean ret = q.refresh();
            assertTrue(ret);
            Issue[] issues = q.getIssues();
            assertNotNull(issues);
            assertEquals(2, issues.length);

        } catch (Exception exception) {
            JiraTestUtil.handleException(exception);
        }
    }
    
    private String createIssue(String id) throws CoreException, JiraException {
        RepositoryResponse rr = JiraTestUtil.createIssue(getRepositoryConnector(), getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()), "Kaputt " + id, "Alles Kaputt! " + id, "Bug");
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
        assertNotNull(JiraTestUtil.getTaskData(getRepositoryConnector(), getTaskRepository(), rr.getTaskId()));
        return rr.getTaskId();
    }

    private JiraClient getClient() {
        return JiraTestUtil.getClient();
    }

    private JiraRepositoryConnector getRepositoryConnector() {
        return JiraTestUtil.getRepositoryConnector();
    }

    private JiraRepository getRepository() {
        return new JiraRepository(getTaskRepository());
    }

    private TaskRepository getTaskRepository() {
        return JiraTestUtil.getRepository();
    }
}

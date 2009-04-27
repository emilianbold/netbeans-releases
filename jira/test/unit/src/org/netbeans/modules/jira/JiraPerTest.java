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

import java.util.logging.Level;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author tomas
 */
public class JiraPerTest extends NbTestCase {
    private static JiraClient client;
    private static JiraRepositoryConnector jrc;
    private static TaskRepository repository;

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
        getClient().getCache().refreshServerInfo(JiraTestUtil.nullProgressMonitor);
        Project[] p = getClient().getCache().getProjects();
        getClient().getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);
//        JiraTestUtil.cleanProject(getRepositoryConnector(), getRepository(), getClient(), JiraTestUtil.getProject(getClient()));
    }

    public static JiraClient getClient() {
        if(client == null) {
            client = JiraClientFactory.getDefault().getJiraClient(getRepository());
        }
        return client;
    }

    public static JiraRepositoryConnector getRepositoryConnector() {
        if(jrc == null) {
            jrc = new JiraRepositoryConnector();
        }
        return jrc;
    }

    public static TaskRepository getRepository() {
        if(repository == null) {
            repository = new TaskRepository("jira", "http://jira.springframework.org/");
            AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials("rigoroz", "rigoroz");
            repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
            repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);
        }
        return repository;
    }


}

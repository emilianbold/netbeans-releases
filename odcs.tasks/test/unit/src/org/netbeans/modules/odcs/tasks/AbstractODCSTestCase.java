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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.odcs.tasks;

import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.logging.Level;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevAttribute;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevRepositoryConnector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author tomas
 */
public abstract class AbstractODCSTestCase extends NbTestCase  {

    private static String passw = null;
    protected static String proxyHost = null;
    protected static String proxyPort = null;
    
    protected static String TEST_PRODUCT = "Unit Test Product";
    protected static final String TEST_COMPONENT1 = "Component1";
    protected static final String TEST_COMPONENT2 = "Component2";
    protected static final String TEST_COMPONENT3 = "Component3";
    
    protected static final String TEST_USER1 = "odcs_testuser1@netbeans.org";
    protected static final String TEST_USER2 = "odcs_testuser2@netbeans.org";
    private static String username;
            
    protected TaskRepository taskRepository;
    protected CloudDevRepositoryConnector rc;
    protected TaskRepositoryManager trm;
    protected NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    
    static {
        if (passw == null) {
//            uname = System.getProperty("team.user.login");
            passw = System.getProperty("team.user.password");
        }
        if (passw == null) { // if it is still null, check the file in ~
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-team")));
                username = br.readLine(); // skip username
                username = TEST_USER1;
                passw = br.readLine();

                proxyHost = br.readLine();
                proxyPort = br.readLine();

                br.close();
                
                System.setProperty("http.proxyPort", proxyPort);
                System.setProperty("http.proxyHost", proxyHost);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } 
        }
            
    }

    public AbstractODCSTestCase(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        trm = new TaskRepositoryManager();
        rc = ODCS.getInstance().getRepositoryConnector(); // reuse the only one RC instance
        
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        taskRepository = new TaskRepository(rc.getConnectorKind(), "http://developer.us.oracle.com/qa-dev/s/qa-dev_netbeans-test/tasks");
        
        
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(username, passw);
        taskRepository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);
        taskRepository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        
        if(proxyHost != null && !proxyHost.isEmpty()) {
            taskRepository.setProperty(TaskRepository.PROXY_HOSTNAME, proxyHost);
        } 
        if(proxyPort != null && !proxyPort.isEmpty()) {
            taskRepository.setProperty(TaskRepository.PROXY_PORT, proxyPort);
        } 

        trm.addRepositoryConnector(rc);
        trm.addRepository(taskRepository);

        //        System.setProperty("httpclient.wire.level", "-1");
        
    }

    public TaskData createTaskData(String summary, String desc, String typeName) throws CoreException, MalformedURLException, IOException {
        TaskData data = ODCSUtil.createTaskData(taskRepository);
        RepositoryConfiguration conf = ODCS.getInstance().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(summary);
        ta = rta.getMappedAttribute(TaskAttribute.DESCRIPTION);
        ta.setValue(desc);
        ta = rta.getMappedAttribute(CloudDevAttribute.TASK_TYPE.getTaskName());
        ta.setValue(conf.getTaskTypes().iterator().next());
        Product product = conf.getProducts().get(0);
        ta = rta.getMappedAttribute(TaskAttribute.PRODUCT);
        ta.setValue(product.getName());
        ta = rta.getMappedAttribute(TaskAttribute.COMPONENT);
        
        Component component = product.getComponents().get(0);
        ta.setValue(product.getComponents().get(0).getName());
        ta = rta.getMappedAttribute(CloudDevAttribute.MILESTONE.getTaskName());
        ta.setValue(product.getMilestones().get(0).getValue());
        ta = rta.getMappedAttribute(CloudDevAttribute.ITERATION.getTaskName());
        Collection<Iteration> c = conf.getIterations();
        if (!c.isEmpty()) {
            ta.setValue(c.iterator().next().getValue());
        }
        ta = rta.getMappedAttribute(TaskAttribute.PRIORITY);
        ta.setValue(conf.getPriorities().get(0).getValue());
        ta = rta.getMappedAttribute(TaskAttribute.SEVERITY);
        ta.setValue(conf.getSeverities().get(0).getValue());
        ta = rta.getMappedAttribute(TaskAttribute.STATUS);
        ta.setValue(ODCSUtil.getStatusByValue(conf, "UNCONFIRMED").getValue());
        
        ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta.setValue(component.getInitialOwner().getLoginName());
        
        ta = rta.getMappedAttribute(CloudDevAttribute.REPORTER.getTaskName());
        ta.setValue(component.getInitialOwner().getLoginName());
        
        RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, data);
        assertEquals(RepositoryResponse.ResponseKind.TASK_CREATED, rr.getReposonseKind());
        String taskId = rr.getTaskId();
        assertNotNull(taskId);
        data = rc.getTaskData(taskRepository, taskId, nullProgressMonitor);
        assertFalse(data.isNew());
        ODCS.LOG.log(Level.FINE, " dataRoot after get {0}", data.getRoot().toString());
        return data;
    }

}

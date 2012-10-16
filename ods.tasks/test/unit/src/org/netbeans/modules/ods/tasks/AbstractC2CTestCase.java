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

package org.netbeans.modules.ods.tasks;

import com.tasktop.c2c.server.tasks.domain.Product;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;
import org.netbeans.modules.ods.tasks.util.C2CUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author tomas
 */
public abstract class AbstractC2CTestCase extends NbTestCase  {

    private static String uname = null;
    private static String passw = null;
    protected static String proxyHost = null;
    protected static String proxyPort = null;
    
    protected static String TEST_PRODUCT = "Unit Test Product";
    protected static final String TEST_COMPONENT1 = "Component1";
    protected static final String TEST_COMPONENT2 = "Component2";
    protected static final String TEST_COMPONENT3 = "Component3";
            
    protected TaskRepository taskRepository;
    protected AbstractRepositoryConnector rc;
    protected TaskRepositoryManager trm;
    protected NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    
    private TaskRepositoryLocationFactory trlf;
    
    static {
        if (uname == null) {
            uname = System.getProperty("team.user.login");
            passw = System.getProperty("team.user.password");
        }
        if (uname == null) { // if it is still null, check the file in ~
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-team")));
                uname = br.readLine();
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

    public AbstractC2CTestCase(String arg0) {
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
        rc = C2CExtender.create();
        trlf = new TaskRepositoryLocationFactory();
        C2CExtender.assignTaskRepositoryLocationFactory(rc, trlf);
        
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
//        taskRepository = new TaskRepository(rc.getConnectorKind(), "https://q.tasktop.com/alm/s/anagramgame/tasks");
        taskRepository = new TaskRepository(rc.getConnectorKind(), "http://qa-dev.developer.us.oracle.com/s/qa-dev_netbeans-test/tasks");
        
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(uname, passw);
        taskRepository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        
        if(proxyHost != null && !proxyHost.isEmpty()) {
            taskRepository.setProperty(TaskRepository.PROXY_HOSTNAME, proxyHost);
        } 
        if(proxyPort != null && !proxyPort.isEmpty()) {
            taskRepository.setProperty(TaskRepository.PROXY_PORT, proxyPort);
        } 

        trm.addRepositoryConnector(rc);
        trm.addRepository(taskRepository);

    }

    public TaskData createTaskData(String summary, String desc, String typeName) throws CoreException, MalformedURLException {
        AbstractRepositoryConnector rc = C2C.getInstance().getRepositoryConnector();
        TaskData data = C2CUtil.createTaskData(taskRepository);
        C2CData clientData = C2CExtender.getData(rc, taskRepository, false);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(summary);
        ta = rta.getMappedAttribute(TaskAttribute.DESCRIPTION);
        ta.setValue(desc);
        ta = rta.getMappedAttribute(C2CData.ATTR_TASK_TYPE);
        ta.setValue(clientData.getTaskTypes().iterator().next());
        Product product = clientData.getProducts().get(0);
        ta = rta.getMappedAttribute(TaskAttribute.PRODUCT);
        ta.setValue(product.getName());
        ta = rta.getMappedAttribute(TaskAttribute.COMPONENT);
        ta.setValue(product.getComponents().get(0).getName());
        ta = rta.getMappedAttribute(C2CData.ATTR_MILESTONE);
        ta.setValue(product.getMilestones().get(0).getValue());
        ta = rta.getMappedAttribute(C2CData.ATTR_ITERATION);
        Collection<String> c = clientData.getActiveIterations();
        if (!c.isEmpty()) {
            ta.setValue(c.iterator().next());
        }
        ta = rta.getMappedAttribute(TaskAttribute.PRIORITY);
        ta.setValue(clientData.getPriorities().get(0).getValue());
        ta = rta.getMappedAttribute(TaskAttribute.SEVERITY);
        ta.setValue(clientData.getSeverities().get(0).getValue());
        ta = rta.getMappedAttribute(TaskAttribute.STATUS);
        ta.setValue(clientData.getStatusByValue("UNCONFIRMED").getValue());
        RepositoryResponse rr = C2CUtil.postTaskData(rc, taskRepository, data);
        assertEquals(RepositoryResponse.ResponseKind.TASK_CREATED, rr.getReposonseKind());
        String taskId = rr.getTaskId();
        assertNotNull(taskId);
        data = rc.getTaskData(taskRepository, taskId, nullProgressMonitor);
        assertFalse(data.isNew());
        C2C.LOG.log(Level.FINE, " dataRoot after get {0}", data.getRoot().toString());
        return data;
    }


}

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

import org.netbeans.modules.ods.tasks.DummyUtils;
import com.tasktop.c2c.server.tasks.domain.Product;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;
import org.netbeans.modules.ods.tasks.util.C2CUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author tomas
 */
public class C2CTest extends NbTestCase  {

    private static String uname = null;
    private static String passw = null;
    private static String proxyHost = null;
    private static String proxyPort = null;
    
    static NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    private TaskRepository repository;
    private AbstractRepositoryConnector cfcrc;
    private TaskRepositoryManager trm;

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
    
    public static Test suite() {
        return NbModuleSuite.create(C2CTest.class, null, null);
    }
    private TaskRepositoryLocationFactory trlf;
    
    public C2CTest(String arg0) {
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
        cfcrc = C2CExtender.create();
        trlf = new TaskRepositoryLocationFactory();
        C2CExtender.assignTaskRepositoryLocationFactory(cfcrc, trlf);
        
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        repository = new TaskRepository(cfcrc.getConnectorKind(), "https://q.tasktop.com/alm/s/anagramgame/tasks");
        
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(uname, passw);
        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        
        if(proxyHost != null && !proxyHost.isEmpty()) {
            repository.setProperty(TaskRepository.PROXY_HOSTNAME, proxyHost);
        }
        if(proxyPort != null && !proxyPort.isEmpty()) {
            repository.setProperty(TaskRepository.PROXY_PORT, proxyPort);
        }
            
//        repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);

        trm.addRepository(repository);
        trm.addRepositoryConnector(cfcrc);

    }

    public void testC2CTasks() throws Throwable {
//        TaskDataState state;
//        TaskData d;
        try {
            //            ContextFactory.createContext(
            //                        "META-INF/spring/applicationContext-multiUserRestClient.xml",
            //                        new BaseProfileConfiguration().getClass().getClassLoader());
            //            ctx = ContextFactory.createContext(
            //                    "com/tasktop/c2c/internal/client/tasks/core/client/tasksApplicationContext.xml",
            //                    getClass().getClassLoader());
            URL r = Thread.currentThread().getContextClassLoader().getResource("com/tasktop/c2c/internal/client/tasks/core/client/tasksApplicationContext.xml");
            
            // create issue
            TaskData data = createIssue(repository, "this is a bug", "a bug", "bug");

//            TaskDataStore tds = new TaskDataStore(trm);
//
//            // update issue
//            updateTaskData(data, brc, repository);
//
//            // hours worked
//            timeTracking(data, brc, repository);
//            
//            // add atachment
//            data = addAttachement(data, brc, repository, "Adding attachement", "some file", "crap");
//            
//            // read attachment
//            readAttachement(data, brc, repository, "crap");
//
//            // add comment
//            String comment = "this is not a comment " + System.currentTimeMillis();
//            RepositoryResponse rr = TestUtil.addComment(repository, data, comment);
//            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());
//
//            // read comments
//            readComment(data, brc, repository, comment);
//
//            changeProduct(data, brc, repository);
//
//            // resolve
//            closeIssue(data, brc, repository);


        } catch (Exception e) {
            TestUtil.handleException(e);
        }
    }

    public TaskData createIssue(TaskRepository repository, String summary, String desc, String typeName) throws CoreException, MalformedURLException {
        TaskData data = C2CUtil.createTaskData(repository);
        
        C2CData clientData = DummyUtils.getClientData(repository);
        
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(summary);
        ta = rta.getMappedAttribute(TaskAttribute.DESCRIPTION);
        ta.setValue(desc);
        
        Product product = clientData.getProducts().get(0);
        ta = rta.getMappedAttribute(TaskAttribute.PRODUCT);
        ta.setValue(product.getName());
        
        ta = rta.getMappedAttribute(TaskAttribute.COMPONENT);
        ta.setValue(product.getComponents().get(0).getName());
        
        RepositoryResponse rr = C2CUtil.postTaskData(cfcrc, repository, data);
        String taskId = rr.getTaskId();
        data = cfcrc.getTaskData(repository, taskId, nullProgressMonitor);
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
        assertNotNull(data);    

        return data;
    }

    private void assertChanged(Task task, TaskData data, boolean changed) {
        boolean hasChanged = cfcrc.hasTaskChanged(repository, task, data);
        assertEquals(changed, hasChanged);
    }



    // XXX how to get task!!!
    // XXX TaskTask isn't working - returns taskId instead of taskKey
    private class Task extends AbstractTask {
        private String key;

        public Task(String repositoryUrl, String key, String taskId, String summary) {
            super(repositoryUrl, taskId, summary);
            this.key = key;
        }

        @Override
        public boolean isLocal() {
            return true;
        }

        @Override
        public String getConnectorKind() {
            return repository.getConnectorKind();
        }

        @Override
        public String getTaskKey() {
            return key;
        }
    }
}

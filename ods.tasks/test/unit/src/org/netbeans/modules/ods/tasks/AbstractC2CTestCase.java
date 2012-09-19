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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;
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
                                                                 //"https://q.tasktop.com/alm/s/anagramgame/tasks")
        taskRepository = new TaskRepository(rc.getConnectorKind(), "http://qa-dev.developer.us.oracle.com/s/qa-dev_netbeans-test/tasks");
        
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(uname, passw);
        taskRepository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        
        if(proxyHost != null && !proxyHost.isEmpty()) {
            taskRepository.setProperty(TaskRepository.PROXY_HOSTNAME, proxyHost);
        } else {
//            taskRepository.setProperty(TaskRepository.PROXY_HOSTNAME, "");
            
        }
        if(proxyPort != null && !proxyPort.isEmpty()) {
            taskRepository.setProperty(TaskRepository.PROXY_PORT, proxyPort);
        } else {
//            taskRepository.setProperty(TaskRepository.PROXY_PORT, "");
        }
            
//        repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);

        trm.addRepository(taskRepository);
        trm.addRepositoryConnector(rc);

    }


}

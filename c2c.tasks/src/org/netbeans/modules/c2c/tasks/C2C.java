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
package org.netbeans.modules.c2c.tasks;

import com.tasktop.c2c.internal.client.tasks.core.CfcRepositoryConnector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingFactory;
import org.netbeans.modules.c2c.tasks.issue.C2CIssue;
import org.netbeans.modules.c2c.tasks.query.C2CQuery;
import org.netbeans.modules.c2c.tasks.repository.C2CRepository;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author tomas
 */
public class C2C {
    
    private static C2C instance;
    public final static Logger LOG = Logger.getLogger("org.netbeans.modules.c2c.tasks"); // NOI18N
    
    private boolean firstRun = true;
    private String uname;
    private String passw;
    private String proxyHost;
    private String proxyPort;
    
    private TaskRepositoryManager trm;
    private CfcRepositoryConnector cfcrc;
    private TaskRepositoryLocationFactory trlf;
    
    private TaskRepository repository;
    
    private RequestProcessor rp;
    
    public static C2C getInstance() {
        if(instance == null) {
            instance = new C2C();
            instance.init();
        }
        return instance;
    }
    private C2CIssueProvider c2cip;
    private C2CQueryProvider c2cqp;
    private C2CRepositoryProvider c2crp;
    private BugtrackingFactory<C2CRepository, C2CQuery, C2CIssue> bf;

    private void init() {
        trm = new TaskRepositoryManager();
        cfcrc = new CfcRepositoryConnector();
        trlf = new TaskRepositoryLocationFactory();
        cfcrc.getClientManager().setTaskRepositoryLocationFactory(trlf);
        trm.addRepositoryConnector(cfcrc);
    }
    
    public TaskRepository getRepository() {
        if (repository == null) {
            repository = new TaskRepository(cfcrc.getConnectorKind(), "https://q.tasktop.com/alm/s/anagramgame/tasks");
            setupCredentials(repository);
            trm.addRepository(repository);
        } 
        return repository;
    }
    
    public CfcRepositoryConnector getRepositoryConnector() {
        return cfcrc;
    }
    
    private void setupCredentials(TaskRepository repository) {
        if(firstRun) {
            if (uname == null) {
                uname = System.getProperty("team.user.login");
                passw = System.getProperty("team.user.password");
            }
            if (uname == null) { 
                try {
                        // if it is still null, check the file in ~
                    BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-team")));
                    uname = br.readLine();
                    passw = br.readLine();

                    proxyHost = br.readLine();
                    proxyPort = br.readLine();

                    br.close();  
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (firstRun) {
                firstRun = false;
            }
        }
        
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(uname, passw);
        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        
        if(proxyHost != null && !proxyHost.isEmpty()) {
            repository.setProperty(TaskRepository.PROXY_HOSTNAME, proxyHost);
        }
        if(proxyPort != null && !proxyPort.isEmpty()) {
            repository.setProperty(TaskRepository.PROXY_PORT, proxyPort);
        }
        
    }

    public BugtrackingFactory<C2CRepository, C2CQuery, C2CIssue> getBugtrackingFactory() {
        if(bf == null) {
            bf = new BugtrackingFactory<C2CRepository, C2CQuery, C2CIssue>();
        }    
        return bf;
    }
    
    public C2CIssueProvider getIssueProvider() {
        if(c2cip == null) {
            c2cip = new C2CIssueProvider();
        }
        return c2cip; 
    }
    public C2CQueryProvider getQueryProvider() {
        if(c2cqp == null) {
            c2cqp = new C2CQueryProvider();
        }
        return c2cqp; 
    }
    public C2CRepositoryProvider getRepositoryProvider() {
        if(c2crp == null) {
            c2crp = new C2CRepositoryProvider();
        }
        return c2crp; 
    }    

    public RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("C2C", 1, true); // NOI18N
        }
        return rp;
    }
}

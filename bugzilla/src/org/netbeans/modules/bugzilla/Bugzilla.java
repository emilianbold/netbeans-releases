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

package org.netbeans.modules.bugzilla;

import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClientManager;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingFactory;
import org.netbeans.modules.bugtracking.util.UndoRedoSupport;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class Bugzilla {

    private BugzillaRepositoryConnector brc;
    private static Bugzilla instance;

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugzilla.Bugzilla"); // NOI18N

    private RequestProcessor rp;
    private BugzillaClientManager clientManager;

    private BugtrackingFactory<BugzillaRepository, BugzillaQuery, BugzillaIssue> bf;
    private BugzillaIssueProvider bip;
    private BugzillaQueryProvider bqp;
    private BugzillaRepositoryProvider brp;
    private IssueNode.ChangesProvider<BugzillaIssue> bcp;

    private Bugzilla() {
        brc = MylynRepositoryConnectorProvider.getInstance().getConnector();
        clientManager = brc.getClientManager();
        MylynSupport.getInstance().addRepositoryListener(clientManager);
    }

    public static synchronized Bugzilla getInstance() {
        if(instance == null) {
            instance = new Bugzilla();
        }
        return instance;
    }

    static synchronized void init() {
        getInstance();
    }

    public BugzillaRepositoryConnector getRepositoryConnector() {
        return brc;
    }

    public RepositoryConfiguration getRepositoryConfiguration(BugzillaRepository repository, boolean forceRefresh) throws CoreException, MalformedURLException {
        getClient(repository); // XXX mylyn 3.1.1 workaround. initialize the client, otherwise the configuration will be downloaded twice
        RepositoryConfiguration rc = brc.getRepositoryConfiguration(repository.getTaskRepository(), forceRefresh, new NullProgressMonitor());
        return rc;
    }

    /**
     * Returns a BugzillaClient for the given repository
     * @param repository
     * @return
     * @throws java.net.MalformedURLException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public BugzillaClient getClient(BugzillaRepository repository) throws MalformedURLException, CoreException {
        return clientManager.getClient(repository.getTaskRepository(), new NullProgressMonitor());
    }

    /**
     * Returns the request processor for common tasks in bugzilla.
     * Do not use this when accesing a remote repository.
     * 
     * @return
     */
    public final RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Bugzilla", 1, true); // NOI18N
        }
        return rp;
    }
    
    public BugtrackingFactory<BugzillaRepository, BugzillaQuery, BugzillaIssue> getBugtrackingFactory() {
        if(bf == null) {
            bf = new BugtrackingFactory<BugzillaRepository, BugzillaQuery, BugzillaIssue>();
        }    
        return bf;
    }
    
    public BugzillaIssueProvider getIssueProvider() {
        if(bip == null) {
            bip = new BugzillaIssueProvider();
        }
        return bip; 
    }
    public BugzillaQueryProvider getQueryProvider() {
        if(bqp == null) {
            bqp = new BugzillaQueryProvider();
        }
        return bqp; 
    }
    public BugzillaRepositoryProvider getRepositoryProvider() {
        if(brp == null) {
            brp = new BugzillaRepositoryProvider();
        }
        return brp; 
    }

    public IssueNode.ChangesProvider<BugzillaIssue> getChangesProvider() {
        if(bcp == null) {
            bcp = new IssueNode.ChangesProvider<BugzillaIssue>() {
                @Override
                public String getRecentChanges(BugzillaIssue i) {
                    return i.getRecentChanges();
                }
            };
        }
        return bcp;
    }
    
    public UndoRedoSupport getUndoRedoSupport(BugzillaIssue issue) {
        return getBugtrackingFactory().getUndoRedoSupport(BugzillaUtil.getRepository(issue.getRepository()), issue);
    }
}

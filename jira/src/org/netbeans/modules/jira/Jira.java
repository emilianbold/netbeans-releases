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

package org.netbeans.modules.jira;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingFactory;
import org.netbeans.modules.bugtracking.spi.IssuePriorityInfo;
import org.netbeans.modules.bugtracking.spi.IssuePriorityProvider;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.util.UndoRedoSupport;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.repository.JiraStorageManager;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class Jira {

    private JiraRepositoryConnector jrc;
    private static Jira instance;
    private JiraStorageManager storageManager;

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.jira.Jira");
    private RequestProcessor rp;

    private BugtrackingFactory<JiraRepository, JiraQuery, NbJiraIssue> bf;
    private JiraRepositoryProvider jrp;
    private JiraQueryProvider jqp;
    private JiraIssueProvider jip;
    private IssueStatusProvider<NbJiraIssue> isp;
    private IssueNode.ChangesProvider<NbJiraIssue> jcp;
    private IssuePriorityProvider<NbJiraIssue> ipp;
    
    
    private Jira() {
        ModuleLifecycleManager.instantiated = true;
    }

    public static synchronized Jira getInstance() {
        if(instance == null) {
            instance = new Jira();
        }
        return instance;
    }

    static void init() {
        getInstance();
    }

    /**
     * Returns the request processor for common tasks in Jira.
     * Do not use this when accesing a remote repository.
     *
     * @return
     */
    public RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Jira", 1, true); // NOI18N
        }
        return rp;
    }

    public JiraRepositoryConnector getRepositoryConnector() {
        if(jrc == null) {
            jrc = MylynRepositoryConnectorProvider.getInstance().getConnector();
            MylynSupport.getInstance().addRepositoryListener(JiraClientFactory.getDefault());
        }
        return jrc;
    }

    public JiraClient getClient(TaskRepository repo) {
        // XXX init repo connenction?
        return JiraClientFactory.getDefault().getJiraClient(repo);
    }

    public JiraStorageManager getStorageManager () {
        if (storageManager == null) {
            storageManager = JiraStorageManager.getInstance();
        }
        return storageManager;
    }

    public BugtrackingFactory<JiraRepository, JiraQuery, NbJiraIssue> getBugtrackingFactory() {
        if(bf == null) {
            bf = new BugtrackingFactory<JiraRepository, JiraQuery, NbJiraIssue>();
        }    
        return bf;
    }    
    
    void shutdown () {
        getStorageManager().shutdown();
    }    
    
    public JiraIssueProvider getIssueProvider() {
        if(jip == null) {
            jip = new JiraIssueProvider();
        }
        return jip; 
    }
    public JiraQueryProvider getQueryProvider() {
        if(jqp == null) {
            jqp = new JiraQueryProvider();
        }
        return jqp; 
    }
    public JiraRepositoryProvider getRepositoryProvider() {
        if(jrp == null) {
            jrp = new JiraRepositoryProvider();
        }
        return jrp; 
    }

    public synchronized IssueStatusProvider<NbJiraIssue> getStatusProvider() {
        if(isp == null) {
            isp = new IssueStatusProvider<NbJiraIssue>() {
                @Override
                public IssueStatusProvider.Status getStatus(NbJiraIssue issue) {
                    return issue.getStatus();
                }
                @Override
                public void setSeen(NbJiraIssue issue, boolean uptodate) {
                    issue.setUpToDate(uptodate);
                }
                @Override
                public void addPropertyChangeListener(NbJiraIssue issue, PropertyChangeListener listener) {
                    issue.addPropertyChangeListener(listener);
                }
                @Override
                public void removePropertyChangeListener(NbJiraIssue issue, PropertyChangeListener listener) {
                    issue.removePropertyChangeListener(listener);
                }
            };
        }
        return isp;
    }
    
    public synchronized IssuePriorityProvider<NbJiraIssue> getPriorityProvider(final JiraRepository repository) {
        if(ipp == null) {
            ipp = new IssuePriorityProvider<NbJiraIssue>() {
                private IssuePriorityInfo[] infos;
                @Override
                public String getPriorityID(NbJiraIssue i) {
                    return i.getPriorityID();
                }

                @Override
                public IssuePriorityInfo[] getPriorityInfos() {
                    if(infos == null) {
                        Priority[] priorities = repository.getConfiguration().getPriorities();
                        infos = new IssuePriorityInfo[priorities.length];
                        for (int i = 0; i < priorities.length; i++) {
                            infos[i] = new IssuePriorityInfo(priorities[i].getId(), priorities[i].getName());
                        }
                    }
                    return infos;
                }
            };
        }
        return ipp;
    }
    
    public IssueNode.ChangesProvider<NbJiraIssue> getChangesProvider() {
        if(jcp == null) {
            jcp = new IssueNode.ChangesProvider<NbJiraIssue>() {
                @Override
                public String getRecentChanges(NbJiraIssue i) {
                    return i.getRecentChanges();
                }
            };
        }
        return jcp;
    }    
}

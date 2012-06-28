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
package org.netbeans.modules.c2c.tasks.repository;

import java.awt.Image;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.c2c.tasks.C2C;
import org.netbeans.modules.c2c.tasks.C2CConnector;
import org.netbeans.modules.c2c.tasks.C2CExecutor;
import org.netbeans.modules.c2c.tasks.DummyUtils;
import org.netbeans.modules.c2c.tasks.issue.C2CIssue;
import org.netbeans.modules.c2c.tasks.query.C2CQuery;
import org.netbeans.modules.c2c.tasks.util.C2CUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
public class C2CRepository {

    private final Object INFO_LOCK = new Object();
    private RepositoryInfo info;
    private C2CRepositoryController controller;
    private TaskRepository taskRepository;
    private Lookup lookup;
    private Cache cache;
    private C2CExecutor executor;
    
    public C2CRepository() {
        
    }
    
    public C2CRepository(RepositoryInfo info) {
        this.info = info;
        
        String name = info.getDisplayName();
        String user = info.getUsername();
        if(user == null) {
            user = ""; // NOI18N
        }
        char[] password = info.getPassword();
        if(password == null) {
            password = new char[0]; 
        }
        String httpUser = info.getHttpUsername();
        if(httpUser == null) {
            httpUser = ""; // NOI18N
        }
        char[] httpPassword = info.getHttpPassword();
        if(httpPassword == null) {
            httpPassword = new char[0]; 
        }
        String url = info.getUrl();
        
        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
    }

    static TaskRepository createTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
//        TaskRepository repository = MylynUtils.createTaskRepository(
//                C2C.getInstance().getRepositoryConnector().getConnectorKind(),
//                name,
//                url,
//                user, password,
//                httpUser, httpPassword);
//        
//        // XXX dummy setup
//        DummyUtils.setup(repository);
//        
//        return repository;
        return DummyUtils.getRepository();
    }

    synchronized void setInfoValues(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        setTaskRepository(name, url, user, password, httpUser, httpPassword);
        String id = info != null ? info.getId() : name + System.currentTimeMillis();
        info = new RepositoryInfo(id, C2CConnector.ID, url, name, getTooltip(name, user, url), user, httpUser, password, httpPassword);
    }
    
    private void setTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {

        String oldUrl = taskRepository != null ? taskRepository.getUrl() : "";
        AuthenticationCredentials c = taskRepository != null ? taskRepository.getCredentials(AuthenticationType.REPOSITORY) : null;
        String oldUser = c != null ? c.getUserName() : "";
        String oldPassword = c != null ? c.getPassword() : "";

        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
        resetRepository(oldUrl.equals(url) && oldUser.equals(user) && oldPassword.equals(new String(password))); // XXX reset the configuration only if the host changed
                                                                                                     //     on psswd and user change reset only taskrepository
    }    
    
    synchronized void resetRepository(boolean keepConfiguration) {
//        XXX
//        if(!keepConfiguration) {
//            bc = null;
//        }
        if(getTaskRepository() != null) {
            C2C.getInstance()
                    .getRepositoryConnector()
                    .getClientManager()
                    .repositoryRemoved(getTaskRepository());
        }
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }
            
    public RepositoryInfo getInfo() {
        synchronized(INFO_LOCK) {
            return info;
        }
    }

    public String getDisplayName() {
        return info.getDisplayName();
    }
    
    public String getUrl() {
        return info.getUrl();
    }
    
    public Image getIcon() {
        return null;
    }

    public void remove() {
        Collection<C2CQuery> qs = getQueries();
        C2CQuery[] toRemove = qs.toArray(new C2CQuery[qs.size()]);
        for (C2CQuery q : toRemove) {
//            removeQuery(q);
        }
        resetRepository(true);
    }

    public C2CIssue getIssue(final String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        TaskData taskData = C2CUtil.getTaskData(this, id);
        if(taskData == null) {
            return null;
        }
        try {
            C2CIssue issue = (C2CIssue) getIssueCache().setIssueData(id, taskData);
            // XXX ensureConfigurationUptodate(issue);
            return issue;
        } catch (IOException ex) {
            C2C.LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    
    public RepositoryController getControler() {
        if(controller == null) {
            controller = new C2CRepositoryController(this);
        }
        return controller;
    }

    public Collection<C2CIssue> simpleSearch(String criteria) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Lookup getLookup() {
        if(lookup == null) {
            lookup = Lookups.fixed(getLookupObjects());
        }
        return lookup;
    }

    public Collection<C2CQuery> getQueries() {
        return Collections.EMPTY_LIST;
    }

    public C2CIssue createIssue() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public C2CQuery createQuery() {
        return new C2CQuery(this);
    }

    public C2CIssue[] getIssues(String[] ids) {
        return new C2CIssue[0];
    }
    
    private String getTooltip(String repoName, String user, String url) {
        return NbBundle.getMessage(C2CRepository.class, "LBL_RepositoryTooltip", new Object[] {repoName, user, url}); // NOI18N
    }

    private Object[] getLookupObjects() {
        return new Object[] { };
    }

    public void refreshConfiguration() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void saveQuery(C2CQuery query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getID() {
        return info.getId();
    }

    public IssueCache<C2CIssue, TaskData> getIssueCache() {
        if(cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    public C2CExecutor getExecutor() {
        if(executor == null) {
            executor = new C2CExecutor();
        }
        return executor;
    }

    private class Cache extends IssueCache<C2CIssue, TaskData> {
        Cache() {
            super(
                C2CRepository.this.getUrl(), 
                new IssueAccessorImpl(), 
                C2C.getInstance().getIssueProvider(), 
                C2CUtil.getRepository(C2CRepository.this));
        }
    }

    private class IssueAccessorImpl implements IssueCache.IssueAccessor<C2CIssue, TaskData> {
        @Override
        public C2CIssue createIssue(TaskData taskData) {
            C2CIssue issue = new C2CIssue(taskData, C2CRepository.this);
            return issue;
        }
        @Override
        public void setIssueData(C2CIssue issue, TaskData taskData) {
            assert issue != null && taskData != null;
            ((C2CIssue)issue).setTaskData(taskData);
        }
        @Override
        public String getRecentChanges(C2CIssue issue) {
            assert issue != null;
            return ((C2CIssue)issue).getRecentChanges();
        }
        @Override
        public long getLastModified(C2CIssue issue) {
            assert issue != null;
            return ((C2CIssue)issue).getLastModify();
        }
        @Override
        public long getCreated(C2CIssue issue) {
            assert issue != null;
            return ((C2CIssue)issue).getCreated();
        }
        @Override
        public String getID(TaskData issueData) {
            assert issueData != null;
            return C2CIssue.getID(issueData);
        }
        @Override
        public Map<String, String> getAttributes(C2CIssue issue) {
            assert issue != null;
            return ((C2CIssue)issue).getAttributes();
        }
    }    
}

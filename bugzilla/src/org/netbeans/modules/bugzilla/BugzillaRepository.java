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

package org.netbeans.modules.bugzilla;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.IssueCache;
import org.netbeans.modules.bugzilla.commands.BugzillaExecutor;
import org.netbeans.modules.bugzilla.commands.ValidateCommand;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.filesystems.RepositoryListener;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaRepository extends Repository {

    private static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/repository.png";

    private String name;
    private TaskRepository taskRepository;
    private Controller controller;
    private Set<Query> queries = null;
    private IssueCache cache;
    private BugzillaExecutor executor;
    private Image icon;

    private RepositoryConfiguration rc;

    BugzillaRepository() {
        icon = ImageUtilities.loadImage(ICON_PATH, true);
    }

    protected BugzillaRepository(String repoName, String url, String user, String password) {
        this();
        name = repoName;
        taskRepository = createTaskRepository(name, url, user, password);
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    @Override
    public Query createQuery() {
        BugzillaQuery q = new BugzillaQuery(this);        
        return q;
    }

    @Override
    public Issue createIssue() {
        TaskAttributeMapper attributeMapper =
                Bugzilla.getInstance()
                    .getRepositoryConnector()
                    .getTaskDataHandler()
                    .getAttributeMapper(taskRepository);
        TaskData data =
                new TaskData(
                    attributeMapper,
                    taskRepository.getConnectorKind(),
                    taskRepository.getRepositoryUrl(),
                    "");
        return new BugzillaIssue(data, this);
    }

    @Override
    public void remove() {
        BugzillaConfig.getInstance().removeRepository(this.getDisplayName());
        Query[] qs = getQueries();
        for (Query q : qs) {
            removeQuery((BugzillaQuery) q);
        }
        removeRepository();
    }

    public synchronized void removeRepository() {
        rc = null;
        Bugzilla.getInstance()
                .getRepositoryConnector()
                .getClientManager()
                .repositoryRemoved(getTaskRepository());
    }

    @Override
    public void fireQueryListChanged() {
        super.fireQueryListChanged();
    }

    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return name + " : " + taskRepository.getCredentials(AuthenticationType.REPOSITORY).getUserName() + "@" + taskRepository.getUrl();
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    public String getUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c.getUserName();
    }

    public String getPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c.getPassword();
    }

    public Issue getIssue(final String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";

        TaskData taskData = BugzillaUtil.getTaskData(BugzillaRepository.this, id);
        if(taskData == null) {
            return null;
        }
        try {
            return getIssueCache().setIssueData(id, taskData);
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    // XXX create repo wih product if kenai project and use in queries
    public Issue[] simpleSearch(final String criteria) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";
        String[] keywords = criteria.split(" ");

        List<Issue> issues = new ArrayList<Issue>();
        StringBuffer url = new StringBuffer();
        if(keywords.length == 1 && isNumber(keywords[0])) {
            // only one search criteria -> might be we are looking for the bug with id=values[0]
            url.append(IBugzillaConstants.URL_GET_SHOW_BUG);
            url.append("="); // XXX ???
            url.append(keywords[0]);

            issues.addAll(executeQuery(url.toString()));
        }

        url = new StringBuffer();
        url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST + "&short_desc_type=allwordssubstr&short_desc=");
        for (int i = 0; i < keywords.length; i++) {
            String val = keywords[i].trim();
            if(val.equals("")) continue;
            url.append(val);
            if(i < keywords.length - 1) {
                url.append("+");
            }
        }
        issues.addAll(executeQuery(url.toString()));
        return issues.toArray(new BugzillaIssue[issues.size()]);
    }

    private List<Issue> executeQuery(String queryUrl)  {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";
        final List<Issue> issues = new ArrayList<Issue>();
        TaskDataCollector collector = new TaskDataCollector() {
            public void accept(TaskData taskData) {
                try {
                    Issue issue = getIssueCache().setIssueData(BugzillaIssue.getID(taskData), taskData);
                    issues.add(issue); // XXX we don't cache this issues - why?
                } catch (IOException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex); // XXX handle errors
                }
            }
        };
        BugzillaUtil.performQuery(taskRepository, queryUrl, collector);
        return issues;
    }

    @Override
    public BugtrackingController getController() {
        if(controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    @Override
    public Query[] getQueries() {
        return getQueriesIntern().toArray(new Query[queries.size()]);
    }

    public IssueCache getIssueCache() {
        if(cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    public void removeQuery(BugzillaQuery query) {
        BugzillaConfig.getInstance().removeQuery(this, query);
        getQueriesIntern().remove(query);
    }

    public void saveQuery(BugzillaQuery query) {
        assert name != null;
        BugzillaConfig.getInstance().putQuery(this, query); // XXX display name ????
        getQueriesIntern().add(query);
    }

    private Set<Query> getQueriesIntern() {
        if(queries == null) {
            queries = new HashSet<Query>(10);
            String[] qs = BugzillaConfig.getInstance().getQueries(name);
            for (String queryName : qs) {
                BugzillaQuery q = BugzillaConfig.getInstance().getQuery(this, queryName);
                if(q != null ) {
                    queries.add(q);
                } else {
                    Bugzilla.LOG.warning("Couldn't find query with stored name " + queryName);
                }
            }
        }
        return queries;
    }

    static TaskRepository createTaskRepository(String name, String url, String user, String password) {
        TaskRepository repository = new TaskRepository(name, url);
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(user, password);

        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        return repository;
    }

    @Override
    public String getUrl() {
        return taskRepository != null ? taskRepository.getUrl() : null;
    }

    private boolean isNumber(String str) {
        for (int i = 0; i < str.length() -1; i++) {
            if(!Character.isDigit(str.charAt(i))) return false;
        }
        return true;
    }

    public BugzillaExecutor getExecutor() {
        if(executor == null) {
            executor = new BugzillaExecutor(this);
        }
        return executor;
    }

    private class Controller extends BugtrackingController implements DocumentListener, ActionListener {
        private RepositoryPanel panel = new RepositoryPanel();
        private String errorMessage;

        private Controller() {
            populate();
            panel.nameField.getDocument().addDocumentListener(this);
            panel.userField.getDocument().addDocumentListener(this);
            panel.urlField.getDocument().addDocumentListener(this);
            panel.psswdField.getDocument().addDocumentListener(this);

            panel.validateButton.addActionListener(this);
        }

        public JComponent getComponent() {
            return panel;
        }

        public HelpCtx getHelpContext() {
            return new HelpCtx(org.netbeans.modules.bugzilla.BugzillaRepository.class);
        }

        public boolean isValid() {
            errorMessage = null;

            String name = panel.nameField.getText().trim();
            if(name.equals("")) {
                errorMessage = "Missing name"; // XXX bundle me
                return false;
            }

            String[] repositories = null;
            if(taskRepository == null) {
                repositories = BugzillaConfig.getInstance().getRepositories();
                for (String repositoryName : repositories) {
                    if(name.equals(repositoryName)) {
                        errorMessage = "Repository with the same name alreay exists"; // XXX bundle me
                        return false;
                    }
                }
            }

            String url = panel.urlField.getText().trim();
            if(url.equals("")) {
                errorMessage = "Missing URL"; // XXX bundle me
                return false;
            }
            try {
                new URL(url);
            } catch (MalformedURLException ex) {
                errorMessage = "Wrong URL format"; // XXX bundle me
                return false;
            }

            if(taskRepository == null) {
                for (String repositoryName : repositories) {
                    BugzillaRepository repository = BugzillaConfig.getInstance().getRepository(repositoryName);
                    if(url.trim().equals(repository.getUrl())) {
                        errorMessage = "Repository with the same url alreay exists"; // XXX bundle me
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public void applyChanges() {
            String newName = panel.nameField.getText().trim();
            if(!newName.equals(BugzillaRepository.this.name)) {
                BugzillaConfig.getInstance().removeRepository(BugzillaRepository.this.name);
            }
            name = newName;
            taskRepository = createTaskRepository(panel.nameField.getText(), panel.urlField.getText(), panel.userField.getText(), new String(panel.psswdField.getPassword()));
            BugzillaConfig.getInstance().putRepository(name, BugzillaRepository.this);
            fireDataApplied();
            removeRepository(); // only on url, user or passwd change
        }

        void populate() {
            if(taskRepository != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        AuthenticationCredentials c = taskRepository.getCredentials(AuthenticationType.REPOSITORY);
                        panel.userField.setText(c.getUserName());
                        panel.psswdField.setText(c.getPassword());
                        panel.urlField.setText(taskRepository.getUrl());
                        panel.nameField.setText(BugzillaRepository.this.name);
                    }
                });
            }
        }

        public void insertUpdate(DocumentEvent e) {
            fireDataChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            fireDataChanged();
        }

        public void changedUpdate(DocumentEvent e) {
            fireDataChanged();
        }

        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == panel.validateButton) {
                onValidate();
            }
        }

        private void onValidate() {
            RequestProcessor rp = Bugzilla.getInstance().getRequestProcessor();

            final Task[] task = new Task[1];
            Cancellable c = new Cancellable() {
                public boolean cancel() {
                    panel.progressPanel.setVisible(false);
                    panel.validateLabel.setVisible(false);
                    if(task[0] != null) task[0].cancel();
                    return true;
                }
            };
            final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryPanel.class, "LBL_Validating"), c);
            JComponent comp = ProgressHandleFactory.createProgressComponent(handle);
            panel.progressPanel.removeAll();
            panel.progressPanel.add(comp, BorderLayout.CENTER);

            task[0] = rp.create(new Runnable() {
                public void run() {
                    handle.start();
                    panel.progressPanel.setVisible(true);
                    panel.validateLabel.setVisible(true);
                    panel.validateLabel.setText(NbBundle.getMessage(RepositoryPanel.class, "LBL_Validating"));
                    try {
                        TaskRepository taskRepo = BugzillaRepository.createTaskRepository(
                                panel.nameField.getText(),
                                panel.urlField.getText(),
                                panel.userField.getText(),
                                new String(panel.psswdField.getPassword()));
                        ValidateCommand cmd = new ValidateCommand(taskRepo);
                        getExecutor().execute(cmd);
                    } finally {
                        handle.finish();
                        panel.progressPanel.setVisible(false);
                        panel.validateLabel.setVisible(false);
                    }
                }
            });
            task[0].schedule(0);
        }

    }

    private class Cache extends IssueCache {
        Cache() {
            super(BugzillaRepository.this.getUrl());
        }
        protected Issue createIssue(TaskData taskData) {
            return new BugzillaIssue(taskData, BugzillaRepository.this);
        }
        protected void setTaskData(Issue issue, TaskData taskData) {
            ((BugzillaIssue)issue).setTaskData(taskData); 
        }
    }

    public synchronized RepositoryConfiguration getRepositoryConfiguration() throws CoreException, IOException {
        if(rc == null) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";
            rc = Bugzilla.getInstance()
                    .getRepositoryConnector()
                    .getClientManager()
                    .getClient(getTaskRepository(), new NullProgressMonitor())
                    .getRepositoryConfiguration(new NullProgressMonitor());
        }
        return rc;
    }

}

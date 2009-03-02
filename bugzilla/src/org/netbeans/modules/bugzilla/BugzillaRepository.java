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
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.tasks.core.TaskRepository;
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
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaRepository extends Repository {

    private String name;
    private TaskRepository taskRepository;
    private Controller controller;
    private Set<Query> queries = null;
    private IssueCache cache;

    BugzillaRepository() { }

    protected BugzillaRepository(String repoName, String url, String user, String password) {
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
    public void fireQueryListChanged() {
        super.fireQueryListChanged();
    }

    public String getDisplayName() {
        return name;
    }

    public String getUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c.getUserName();
    }

    public String getPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c.getPassword();
    }

    public Issue getIssue(String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accesing remote host. Do not call in awt";
        TaskData taskData;
        try {
            taskData = Bugzilla.getInstance().getRepositoryConnector().getTaskData(taskRepository, id, new NullProgressMonitor());
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
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
        assert !SwingUtilities.isEventDispatchThread() : "Accesing remote host. Do not call in awt";
        String[] keywords = criteria.split(" ");

        List<BugzillaIssue> issues = new ArrayList<BugzillaIssue>();
        StringBuffer url = new StringBuffer();
        if(keywords.length == 1 && isNumber(keywords[0])) {
            // only one search criteria -> might be we are looking for the bug with id=values[0]
            url.append(IBugzillaConstants.URL_GET_SHOW_BUG);
            url.append("="); // XXX ???
            url.append(keywords[0]);

            executeQuery(url.toString(), issues);
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
        executeQuery(url.toString(), issues);
        return issues.toArray(new BugzillaIssue[issues.size()]);
    }

    private void executeQuery(String queryUrl, final List<BugzillaIssue> issues)  {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accesing remote host. Do not call in awt";
        TaskDataCollector collector = new TaskDataCollector() {
            public void accept(TaskData taskData) {
                issues.add(new BugzillaIssue(taskData, BugzillaRepository.this)); // we don't cache this issues
            }
        };
        BugzillaUtil.performQuery(taskRepository, queryUrl, collector);
    }

    @Override
    public String getTooltip() {
        return name + " : " + taskRepository.getCredentials(AuthenticationType.REPOSITORY).getUserName() + "@" + taskRepository.getUrl();
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

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public Issue createIssue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private class Controller extends BugtrackingController implements DocumentListener, ActionListener {
        private RepositoryPanel panel = new RepositoryPanel();

        private Controller() {
            if(taskRepository != null) {
                AuthenticationCredentials c = taskRepository.getCredentials(AuthenticationType.REPOSITORY);
                panel.userField.setText(c.getUserName());
                panel.psswdField.setText(c.getPassword());
                panel.urlField.setText(taskRepository.getUrl());
                panel.nameField.setText(BugzillaRepository.this.name);
            }

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
            return !panel.nameField.getText().trim().equals("") &&
                   !panel.urlField.getText().trim().equals("") &&
                   !panel.userField.getText().trim().equals("") &&
                   !new String(panel.psswdField.getPassword()).equals("");
        }

        @Override
        public void applyChanges() {
            String newName = panel.nameField.getText().trim();
            if(!newName.equals(BugzillaRepository.this.name)) {
                BugzillaConfig.getInstance().removeRepository(BugzillaRepository.this.name);
            }
            BugzillaRepository.this.name = newName;
            BugzillaRepository.this.taskRepository = createTaskRepository(panel.nameField.getText(), panel.urlField.getText(), panel.userField.getText(), new String(panel.psswdField.getPassword()));
            BugzillaConfig.getInstance().putRepository(name, BugzillaRepository.this);
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
                        try {
                            BugzillaClient client = Bugzilla.getInstance().getRepositoryConnector().getClientManager().getClient(taskRepo, new NullProgressMonitor());
                            client.validate(new NullProgressMonitor());
                        } catch (IOException ex) {
                            Bugzilla.LOG.log(Level.SEVERE, null, ex); // XXX handle errors
                        } catch (CoreException ex) {
                            Bugzilla.LOG.log(Level.SEVERE, null, ex); // XXX handle errors
                        }
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
            return new BugzillaIssue(taskData, (BugzillaRepository) BugzillaRepository.this);
        }
        protected void setTaskData(Issue issue, TaskData taskData) {
            ((BugzillaIssue)issue).setTaskData(taskData); // XXX triggers events under lock
        }
    }

}

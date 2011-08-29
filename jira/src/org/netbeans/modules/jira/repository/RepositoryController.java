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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.repository;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.commands.ValidateCommand;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryController extends BugtrackingController implements DocumentListener, ActionListener {
    private JiraRepository repository;
    private RepositoryPanel panel;
    private String errorMessage;
    private boolean validateError;
    private boolean populated = false;
    private RequestProcessor rp;
    private TaskRunner taskRunner;

    RepositoryController(JiraRepository repository) {
        this.repository = repository;
        panel = new RepositoryPanel(this);
        panel.nameField.getDocument().addDocumentListener(this);
        panel.userField.getDocument().addDocumentListener(this);
        panel.urlField.getDocument().addDocumentListener(this);
        panel.psswdField.getDocument().addDocumentListener(this);

        panel.validateButton.addActionListener(this);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    public HelpCtx getHelpContext() {
        return new HelpCtx(JiraRepository.class);
    }

    @Override
    public boolean isValid() {
        return validate();
    }

    private String getUrl() {
        String url = panel.urlField.getText().trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url; // NOI18N
    }

    private String getName() {
        return panel.nameField.getText();
    }

    private String getUser() {
        return panel.userField.getText();
    }

    private String getPassword() {
        return new String(panel.psswdField.getPassword());
    }

    private String getHttpUser() {
        return panel.httpCheckBox.isSelected() ? panel.httpUserField.getText() : null;
    }

    private String getHttpPassword() {
        return panel.httpCheckBox.isSelected() ? new String(panel.httpPsswdField.getPassword()) : null;
    }

    private boolean validate() {
        if(validateError) {
            panel.validateButton.setEnabled(true);
            return false;
        }
        panel.validateButton.setEnabled(false);

        if(!populated) {
            return false;
        }
        errorMessage = null;

        // check name
        String name = panel.nameField.getText().trim();
        if(name.equals("")) { // NOI18N
            errorMessage = NbBundle.getMessage(RepositoryController.class, "MSG_MISSING_NAME");  // NOI18N
            return false;
        }

        // is name unique?
        String[] repositories = null;
        if(repository.getTaskRepository() == null) {
            repositories = JiraConfig.getInstance().getRepositories();
            for (String repoId : repositories) {
                if(name.equals(JiraConfig.getInstance().getRepositoryName(repoId))) {
                    errorMessage = NbBundle.getMessage(RepositoryController.class, "MSG_NAME_ALREADY_EXISTS");  // NOI18N
                    return false;
                }
            }
        }

        // check url
        String url = getUrl();
        if(url.equals("")) { // NOI18N
            errorMessage = NbBundle.getMessage(RepositoryController.class, "MSG_MISSING_URL");  // NOI18N
            return false;
        }
        try {
            new URL(url); // check this first even if URL is an URI
            new URI(url);
        } catch (Exception ex) {
            errorMessage = NbBundle.getMessage(RepositoryController.class, "MSG_WRONG_URL_FORMAT");  // NOI18N
            Jira.LOG.log(Level.FINEST, errorMessage, ex);
            return false;
        }

        // url ok - enable validate button
        panel.validateButton.setEnabled(true);

        // is url unique?
        if(repository.getTaskRepository() == null) {
            for (String repositoryName : repositories) {
                JiraRepository repo = Jira.getInstance().getRepository(repositoryName);
                if(repo == null) continue;
                if(url.trim().equals(repo.getUrl())) {
                    errorMessage = NbBundle.getMessage(RepositoryController.class, "MSG_URL_ALREADY_EXISTS");  // NOI18N
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void applyChanges() {
        String newName = panel.nameField.getText().trim();
        repository.setName(newName);
        repository.setTaskRepository(
            getName(),
            getUrl(),
            getUser(),
            getPassword(),
            getHttpUser(),
            getHttpPassword());
        Jira.getInstance().addRepository(repository);
        repository.getNode().setName(newName);
    }

    void populate() {
        taskRunner = new TaskRunner(NbBundle.getMessage(RepositoryPanel.class, "LBL_ReadingRepoData")) {  // NOI18N
            @Override
            protected void preRun() {
                panel.validateButton.setVisible(false);
                super.preRun();
            }
            @Override
            protected void postRun() {
                panel.validateButton.setVisible(true);
                super.postRun();
            }
            @Override
            void execute() {
                JiraConfig.getInstance().setupCredentials(repository);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        TaskRepository taskRepository = repository.getTaskRepository();
                        if(taskRepository != null) {
                            AuthenticationCredentials c = taskRepository.getCredentials(AuthenticationType.REPOSITORY);
                            if(c != null) {
                                panel.userField.setText(c.getUserName());
                                panel.psswdField.setText(c.getPassword());
                            }
                            c = taskRepository.getCredentials(AuthenticationType.HTTP);
                            if(c != null) {
                                String httpUser = c.getUserName();
                                String httpPsswd = c.getPassword();
                                if(httpUser != null && !httpUser.equals("") &&          // NOI18N
                                   httpPsswd != null && !httpPsswd.equals(""))          // NOI18N
                                {
                                    panel.httpCheckBox.setSelected(true);
                                    panel.httpUserField.setText(httpUser);
                                    panel.httpPsswdField.setText(httpPsswd);
                                }
                            }
                            panel.urlField.setText(taskRepository.getUrl());
                            panel.nameField.setText(repository.getDisplayName());
                        }
                        populated = true;
                        fireDataChanged();
                    }
                });
            }
        };
        taskRunner.startTask();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireDataChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireDataChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireDataChanged();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.validateButton) {
            onValidate();
        }
    }

    private void onValidate() {
        taskRunner = new TaskRunner(NbBundle.getMessage(RepositoryPanel.class, "LBL_Validating")) {  // NOI18N
            @Override
            public void execute() {
                validateError = false;

                repository.resetRepository(true); // reset mylyns caching

                String name = getName();
                String url = getUrl();
                String user = getUser();
                String httpUser = getHttpUser();
                String password = getPassword();
                String httpPassword = getHttpPassword();
                TaskRepository taskRepo = JiraRepository.createTaskRepository(
                        name,
                        url,
                        user,
                        password,
                        getHttpUser(),
                        httpPassword);

                ValidateCommand cmd = new ValidateCommand(taskRepo);
                repository.getExecutor().execute(cmd, false, false, false);
                if(cmd.hasFailed()) {
                    if(cmd.getErrorMessage() == null) {
                        logValidateMessage("validate for [{0},{1},{2},{3},{4},{5}] has failed, yet the returned error message is null.", // NOI18N
                                           Level.WARNING, name, url, user, password, httpUser, httpPassword);
                        errorMessage = NbBundle.getMessage(RepositoryController.class, "MSG_VALIDATION_FAILED");  // NOI18N
                    } else {
                        errorMessage = cmd.getErrorMessage();
                        logValidateMessage("validate for [{0},{1},{2},{3},{4},{5}] has failed: " + errorMessage, // NOI18N
                                           Level.WARNING, name, url, user, password, httpUser, httpPassword);
                    }
                    validateError = true;
                } else {
                    logValidateMessage("validate for [{0},{1},{2},{3},{4},{5}] worked.", // NOI18N
                                       Level.INFO, name, url, user, password, httpUser, httpPassword);
                    panel.connectionLabel.setVisible(true);
                }
                fireDataChanged();
            }

            private void logValidateMessage(String msg, Level level, String name, String url, String user, String password, String httpUser, String httpPassword) {
                Jira.LOG.log(level, msg, new Object[] {name, url, user, BugtrackingUtil.getPasswordLog(password), httpUser, BugtrackingUtil.getPasswordLog(httpPassword)});
            }
        };
        taskRunner.startTask();
    }

    private void validateErrorOff(DocumentEvent e) {
        if (e.getDocument() == panel.userField.getDocument() || e.getDocument() == panel.urlField.getDocument() || e.getDocument() == panel.psswdField.getDocument()) {
            validateError = false;
        }
    }

    void cancel() {
        if(taskRunner != null) {
            taskRunner.cancel();
        }
    }

    private abstract class TaskRunner implements Runnable, Cancellable, ActionListener {
        private Task task;
        private ProgressHandle handle;
        private String labelText;

        public TaskRunner(String labelText) {
            this.labelText = labelText;
        }

        final void startTask() {
            cancel();
            task = getRequestProcessor().create(this);
            task.schedule(0);
        }

        @Override
        final public void run() {
            preRun();
            try {
                execute();
            } finally {
                postRun();
            }
        }

        abstract void execute();

        protected void preRun() {
            handle = ProgressHandleFactory.createHandle(labelText, this);
            JComponent comp = ProgressHandleFactory.createProgressComponent(handle);
            panel.progressPanel.removeAll();
            panel.progressPanel.add(comp, BorderLayout.CENTER);
            panel.cancelButton.addActionListener(this);
            panel.connectionLabel.setVisible(false);
            handle.start();
            panel.progressPanel.setVisible(true);
            panel.validateLabel.setVisible(true);
            panel.cancelButton.setVisible(true);
            panel.validateButton.setVisible(false);
            panel.enableFields(false);
            panel.validateLabel.setText(labelText); // NOI18N
        }

        protected void postRun() {
            if(handle != null) {
                handle.finish();
            }
            panel.cancelButton.removeActionListener(this);
            panel.progressPanel.setVisible(false);
            panel.validateLabel.setVisible(false);
            panel.cancelButton.setVisible(false);
            panel.validateButton.setVisible(true);
            panel.enableFields(true);
        }

        @Override
        public boolean cancel() {
            boolean ret = true;
            postRun();
            if(task != null) {
                ret = task.cancel();
            }
            errorMessage = null;
            return ret;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == panel.cancelButton) {
                cancel();
            }
        }

    }

    private RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Jira Repository tasks", 1, true); // NOI18N
        }
        return rp;
    }
}

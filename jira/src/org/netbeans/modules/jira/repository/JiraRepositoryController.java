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

import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.commands.ValidateCommand;
import org.netbeans.modules.jira.util.JiraUtils;
import org.openide.util.*;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class JiraRepositoryController implements RepositoryController, DocumentListener, ActionListener {
    private JiraRepository repository;
    private RepositoryPanel panel;
    private String errorMessage;
    private boolean validateError;
    private boolean populated = false;
    private RequestProcessor rp;
    private TaskRunner taskRunner;
    private final ChangeSupport support = new ChangeSupport(this);
    
    JiraRepositoryController(JiraRepository repository) {
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
        return panel.nameField.getText().trim();
    }

    private String getUser() {
        return panel.userField.getText();
    }

    private char[] getPassword() {
        return panel.psswdField.getPassword();
    }

    private String getHttpUser() {
        return panel.httpCheckBox.isSelected() ? panel.httpUserField.getText() : null;
    }

    private char[] getHttpPassword() {
        return panel.httpCheckBox.isSelected() ? panel.httpPsswdField.getPassword() : new char[0];
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
            errorMessage = NbBundle.getMessage(JiraRepositoryController.class, "MSG_MISSING_NAME");  // NOI18N
            return false;
        }

        // is name unique?
        Collection<Repository> repositories = null;
        if(repository.getTaskRepository() == null) {
            repositories = RepositoryManager.getInstance().getRepositories(JiraConnector.ID);
            for (Repository rp : repositories) {
                if(name.equals(rp.getDisplayName())) {
                    errorMessage = NbBundle.getMessage(JiraRepositoryController.class, "MSG_NAME_ALREADY_EXISTS");  // NOI18N
                    return false;
                }
            }
        }

        // check url
        String url = getUrl();
        if(url.equals("")) { // NOI18N
            errorMessage = NbBundle.getMessage(JiraRepositoryController.class, "MSG_MISSING_URL");  // NOI18N
            return false;
        }
        try {
            new URL(url); // check this first even if URL is an URI
            new URI(url);
        } catch (Exception ex) {
            errorMessage = NbBundle.getMessage(JiraRepositoryController.class, "MSG_WRONG_URL_FORMAT");  // NOI18N
            Jira.LOG.log(Level.FINEST, errorMessage, ex);
            return false;
        }

        // url ok - enable validate button
        panel.validateButton.setEnabled(true);

        // is url unique?
        if(repository.getTaskRepository() == null) {
            for (Repository rp : repositories) {
                if(url.trim().equals(rp.getUrl())) {
                    errorMessage = NbBundle.getMessage(JiraRepositoryController.class, "MSG_URL_ALREADY_EXISTS");  // NOI18N
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
        repository.setInfoValues(
            getName(),
            getUrl(),
            getUser(),
            getPassword(),
            getHttpUser(),
            getHttpPassword());
    }

    @Override
    public void populate() {
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
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        RepositoryInfo info = repository.getInfo();
                        if(info != null) {
                            panel.userField.setText(info.getUsername());
                            char[] psswd = info.getPassword();
                            panel.psswdField.setText(psswd != null ? new String(psswd) : "");
                            String httpUser = info.getHttpUsername();
                            char[] httpPsswd = info.getHttpPassword();
                            if(httpUser != null && !httpUser.equals("")) {
                                panel.httpCheckBox.setSelected(true);
                                panel.httpUserField.setText(httpUser);
                            }
                            if(httpPsswd != null && httpPsswd.length > 0) {
                                panel.httpCheckBox.setSelected(true);
                                panel.httpPsswdField.setText(new String(httpPsswd));
                            }
                            panel.urlField.setText(info.getUrl());
                            panel.nameField.setText(repository.getDisplayName());
                        }
                        populated = true;
                        fireChange();
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
        fireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireChange();
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
                char[] password = getPassword();
                char[] httpPassword = getHttpPassword();
                TaskRepository taskRepo = JiraRepository.createTaskRepository(
                        name,
                        url,
                        user,
                        password,
                        getHttpUser(),
                        httpPassword);

                ValidateCommand cmd = new ValidateCommand(taskRepo);
                repository.getExecutor().execute(cmd, false, false, false, false);
                if(cmd.hasFailed()) {
                    if(cmd.getErrorMessage() == null) {
                        logValidateMessage("validate for [{0},{1},{2},{3},{4},{5}] has failed, yet the returned error message is null.", // NOI18N
                                           Level.WARNING, name, url, user, password, httpUser, httpPassword);
                        errorMessage = NbBundle.getMessage(JiraRepositoryController.class, "MSG_VALIDATION_FAILED");  // NOI18N
                    } else {
                        errorMessage = cmd.getErrorMessage();
                        logValidateMessage("validate for [{0},{1},{2},{3},{4},{5}] has failed: " + errorMessage, // NOI18N
                                           Level.WARNING, name, url, user, password, httpUser, httpPassword);
                    }
                    validateError = true;
                } else {
                    logValidateMessage("validate for [{0},{1},{2},{3},{4},{5}] worked.", // NOI18N
                                       Level.INFO, name, url, user, password, httpUser, httpPassword);
                    JiraUtils.runInAWT(new Runnable() {
                        @Override
                        public void run() {
                            panel.connectionLabel.setVisible(true);
                        }
                    });
                }
                fireChange();
            }

            private void logValidateMessage(String msg, Level level, String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
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
        private final String labelText;

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
            JiraUtils.runInAWT(new Runnable() {
                @Override
                public void run() {
                    preRun();
                }
            });
            try {
                execute();
            } finally {
                JiraUtils.runInAWT(new Runnable() {
                    @Override
                    public void run() {                
                        postRun();
                    }
                });
            }
        }

        abstract void execute();

        protected void preRun() {
            handle = ProgressHandleFactory.createHandle(labelText, this);
            final JComponent comp = ProgressHandleFactory.createProgressComponent(handle);
            handle.start();            
            panel.cancelButton.addActionListener(this);
            
            JiraUtils.runInAWT(new Runnable() {
                @Override
                public void run() {
                    panel.progressPanel.removeAll();
                    panel.progressPanel.add(comp, BorderLayout.CENTER);
                    panel.connectionLabel.setVisible(false);
                    panel.progressPanel.setVisible(true);
                    panel.validateLabel.setVisible(true);
                    panel.cancelButton.setVisible(true);
                    panel.validateButton.setVisible(false);
                    panel.enableFields(false);
                    panel.validateLabel.setText(labelText); // NOI18N
                }
            });

        }

        protected void postRun() {
            if(handle != null) {
                handle.finish();
            }
            panel.cancelButton.removeActionListener(this);
            JiraUtils.runInAWT(new Runnable() {
                @Override
                public void run() {
                    panel.progressPanel.setVisible(false);
                    panel.validateLabel.setVisible(false);
                    panel.cancelButton.setVisible(false);
                    panel.validateButton.setVisible(true);
                    panel.enableFields(true);
                }
             });
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
    
    
    @Override
    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }
    
    protected void fireChange() {
        support.fireChange();
    }        
}

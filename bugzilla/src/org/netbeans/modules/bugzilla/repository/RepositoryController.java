/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.repository;

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
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.commands.ValidateCommand;
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
    private BugzillaRepository repository;
    private RepositoryPanel panel;
    private String errorMessage;
    private boolean validateError;
    private boolean populated = false;
    private TaskRunner taskRunner;
    private RequestProcessor rp;

    RepositoryController(BugzillaRepository repository) {
        this.repository = repository;
        panel = new RepositoryPanel(this);
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
        return new HelpCtx(org.netbeans.modules.bugzilla.repository.BugzillaRepository.class);
    }

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

    private boolean isLocalUserEnabled () {
        return panel.cbEnableLocalUsers.isSelected();
    }

    private boolean validate() {
        if(!populated) {
            return true;
        }
        if(validateError) {
            return false;
        }
        errorMessage = null;

        panel.validateButton.setEnabled(false);

        // check name
        String name = panel.nameField.getText().trim();
        if(name.equals("")) { // NOI18N
            errorMessage = NbBundle.getMessage(RepositoryController.class, "MSG_MISSING_NAME");  // NOI18N
            return false;
        }

        // is name unique?
        String[] repositories = null;
        if(repository.getTaskRepository() == null) {
            repositories = BugzillaConfig.getInstance().getRepositories();
            for (String repositoryName : repositories) {
                if(name.equals(repositoryName)) {
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
            Bugzilla.LOG.log(Level.FINEST, errorMessage, ex);
            return false;
        }

        // the url format is ok - lets enable the validate button
        panel.validateButton.setEnabled(true);

        // is url unique?
        if(repository.getTaskRepository() == null) {
            for (String repositoryName : repositories) {
                BugzillaRepository repo = BugzillaConfig.getInstance().getRepository(repositoryName);
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
            getHttpPassword(),
            isLocalUserEnabled());
        Bugzilla.getInstance().addRepository(repository);
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
                BugzillaConfig.getInstance().setupCredentials(repository);
                if(repository.getTaskRepository() != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            AuthenticationCredentials c = repository.getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
                            panel.userField.setText(c.getUserName());
                            panel.psswdField.setText(c.getPassword());
                            c = repository.getTaskRepository().getCredentials(AuthenticationType.HTTP);
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
                            panel.urlField.setText(repository.getTaskRepository().getUrl());
                            panel.nameField.setText(repository.getDisplayName());
                            panel.cbEnableLocalUsers.setSelected(repository.isShortUsernamesEnabled());
                            populated = true;
                            fireDataChanged();
                        }
                    });
                } else {
                    populated = false;
                    fireDataChanged();
                }
            }
        };
        taskRunner.startTask();
    }

    public void insertUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireDataChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireDataChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireDataChanged();
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.validateButton) {
            onValidate();
        }
    }

    private void onValidate() {
        taskRunner = new TaskRunner(NbBundle.getMessage(RepositoryPanel.class, "LBL_Validating")) {  // NOI18N
            @Override
            void execute() {
                    repository.resetRepository(); // reset mylyns caching

                    String name = getName();
                    String url = getUrl();
                    String user = getUser();
                    String httpUser = getHttpUser();
                    TaskRepository taskRepo = BugzillaRepository.createTaskRepository(
                            name,
                            url,
                            user,
                            getPassword(),
                            httpUser,
                            getHttpPassword(),
                            isLocalUserEnabled());

                    ValidateCommand cmd = new ValidateCommand(taskRepo);
                    repository.getExecutor().execute(cmd, false, false);
                    if(cmd.hasFailed()) {
                        if(cmd.getErrorMessage() == null) {
                            logValidateMessage("validate for [{0},{1},{2},****{3},****] has failed, yet the returned error message is null.", // NOI18N
                                               Level.WARNING, name, url, user, httpUser);
                            errorMessage = NbBundle.getMessage(RepositoryController.class, "MSG_VALIDATION_FAILED"); // NOI18N
                        } else {
                            errorMessage = cmd.getErrorMessage();
                            logValidateMessage("validate for [{0},{1},{2},****{3},****] has failed: " + errorMessage, // NOI18N
                                               Level.WARNING, name, url, user, httpUser);
                        }
                        validateError = true;
                        fireDataChanged();
                    } else {
                        panel.connectionLabel.setVisible(true);
                        logValidateMessage("validate for [{0},{1},{2},****{3},****] worked.", // NOI18N
                                           Level.INFO, name, url, user, httpUser);
                    }
            }

            private void logValidateMessage(String msg, Level level, String name, String url, String user, String httpUser) {
                Bugzilla.LOG.log(level, msg, new Object[] {name, url, user, httpUser});
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

    private abstract class TaskRunner implements Runnable, Cancellable {
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

            panel.connectionLabel.setVisible(false);
            handle.start();
            panel.progressPanel.setVisible(true);
            panel.validateLabel.setVisible(true);
            panel.enableFields(false);
            panel.validateLabel.setText(labelText); // NOI18N
        }

        protected void postRun() {
            if(handle != null) {
                handle.finish();
            }
            panel.progressPanel.setVisible(false);
            panel.validateLabel.setVisible(false);
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
    }

    private RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Bugzilla Repository tasks", 1, true); // NOI18N
        }
        return rp;
    }

}

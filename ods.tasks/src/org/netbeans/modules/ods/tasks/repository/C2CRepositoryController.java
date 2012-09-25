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
package org.netbeans.modules.ods.tasks.repository;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.ods.tasks.C2C;
import org.netbeans.modules.ods.tasks.C2CConnector;
import org.openide.util.Cancellable;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author tomas
 */
public class C2CRepositoryController implements RepositoryController, DocumentListener, ActionListener {

    private C2CRepository repository;
    private TaskRunner taskRunner;
    private RequestProcessor rp;
    private final RepositoryPanel panel;
    private boolean populated;
    private boolean validateError;
    private String errorMessage;
    private final ChangeSupport support = new ChangeSupport(this);
    
    public C2CRepositoryController(C2CRepository repository) {
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

    @Override
    public HelpCtx getHelpCtx() {
        return null; // XXX
    }

    @Override
    public boolean isValid() {
        return validate();
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
                            panel.nameField.setText(repository.getInfo().getDisplayName());
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
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void applyChanges() throws IOException {
        repository.setInfoValues(
            getName(),
            getUrl(),
            getUser(),
            getPassword(),
            getHttpUser(),
            getHttpPassword());
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

    @Override
    public void insertUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if(!populated) {
            return;
        }
        validateErrorOff(e);
        fireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if(!populated) {
            return;
        }
        validateErrorOff(e);
        fireChange();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.validateButton) {
            onValidate();
        }
    }

    void cancel() {
        if(taskRunner != null) {
            taskRunner.cancel();
        }
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
    
    private void validateErrorOff(DocumentEvent e) {
        if (e.getDocument() == panel.userField.getDocument() || e.getDocument() == panel.urlField.getDocument() || e.getDocument() == panel.psswdField.getDocument()) {
            validateError = false;
        }
    }

    private void onValidate() {
        taskRunner = new TaskRunner(NbBundle.getMessage(RepositoryPanel.class, "LBL_Validating")) {  // NOI18N
            @Override
            void execute() {
                
                fireChange();
            }

            private void logValidateMessage(String msg, Level level, String name, String url, String user, char[] psswd, String httpUser, char[] httpPsswd) {
                C2C.LOG.log(level, msg, new Object[] {name, url, user, BugtrackingUtil.getPasswordLog(psswd), httpUser, BugtrackingUtil.getPasswordLog(httpPsswd)});
            }
        };
        taskRunner.startTask();
    }

    private String getUrl() {
        String url = panel.urlField.getText().trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url; // NOI18N
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
            errorMessage = NbBundle.getMessage(C2CRepositoryController.class, "MSG_MISSING_NAME");  // NOI18N
            return false;
        }

        // is name unique?
        Collection<Repository> repositories = null;
        if(repository.getTaskRepository() == null) {
            repositories = RepositoryManager.getInstance().getRepositories(C2CConnector.ID);
            for (Repository repo : repositories) {
                if(name.equals(repo.getDisplayName())) {
                    errorMessage = NbBundle.getMessage(C2CRepositoryController.class, "MSG_NAME_ALREADY_EXISTS");  // NOI18N
                    return false;
                }
            }
        }

        // check url
        String url = getUrl();
        if(url.equals("")) { // NOI18N
            errorMessage = NbBundle.getMessage(C2CRepositoryController.class, "MSG_MISSING_URL");  // NOI18N
            return false;
        }

        // XXX
//        if(!BugzillaClient.isValidUrl(url) || "http://".equals(url) || "https://".equals(url)) {
//            errorMessage = NbBundle.getMessage(C2CRepositoryController.class, "MSG_WRONG_URL_FORMAT");  // NOI18N
//            return false;
//        }

        // the url format is ok - lets enable the validate button
        panel.validateButton.setEnabled(true);

        // is url unique?
        if(repository.getTaskRepository() == null) {
            for (Repository repo : repositories) {
                if(url.trim().equals(repo.getUrl())) {
                    errorMessage = NbBundle.getMessage(C2CRepositoryController.class, "MSG_URL_ALREADY_EXISTS");  // NOI18N
                    return false;
                }
            }
        }

        return true;
    }

    private abstract class TaskRunner implements Runnable, Cancellable, ActionListener {
        private RequestProcessor.Task task;
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
            panel.cancelButton.setVisible(true);
            panel.validateButton.setVisible(false);
            panel.validateLabel.setVisible(true);
            panel.enableFields(false);
            panel.validateLabel.setText(labelText); // NOI18N
        }

        protected void postRun() {
            if(handle != null) {
                handle.finish();
            }
            panel.cancelButton.removeActionListener(this);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {            
                    panel.progressPanel.setVisible(false);
                    panel.validateLabel.setVisible(false);
                    panel.validateButton.setVisible(true);
                    panel.cancelButton.setVisible(false);
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
            rp = new RequestProcessor("C2C Repository tasks", 1, true); // NOI18N
        }
        return rp;
    }

}

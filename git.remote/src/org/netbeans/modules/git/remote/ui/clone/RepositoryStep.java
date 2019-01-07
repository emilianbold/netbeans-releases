/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.ui.clone;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitURI;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.GitModuleConfig;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.ui.repository.remote.RemoteRepository;
import org.netbeans.modules.git.remote.ui.wizards.AbstractWizardPanel;
import org.netbeans.modules.git.remote.utils.WizardStepProgressSupport;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class RepositoryStep extends AbstractWizardPanel implements ChangeListener, AsynchronousValidatingPanel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, DocumentListener {

    private RepositoryStepProgressSupport support;
    private Map<String, GitBranch> branches;
    
    private Map<String, GitBranch> remoteBranches;
    private final RepositoryStepPanel panel;
    private final RemoteRepository repository;
    private boolean destinationValid = true;
    private boolean validatingFinish;
    private final CloneWizard wiz;
    private final FileSystem fileSystem;

    public RepositoryStep (FileSystem fs, CloneWizard wiz, PasswordAuthentication pa, String forPath) {
        this.fileSystem = fs;
        this.wiz = wiz;
        repository = new RemoteRepository(fs, pa, forPath);
        repository.addChangeListener(this);
        panel = new RepositoryStepPanel(fs, repository.getPanel());
        panel.txtDestination.getDocument().addDocumentListener(this);
        validateRepository();
    }

    @Override
    protected final boolean validateBeforeNext () {
        waitPopulated();
        boolean valid = false;
        try {
            branches = null;
            if(!validateRepository()) {
                return false;
            }
            if (validatingFinish) {
                Message msg = null;
                try {
                    if ((msg = validateNoEmptyDestination()) != null) {
                        // cannot finish
                        destinationValid = false;
                        return false;
                    }
                    VCSFileProxy dest = getDestination();
                    if (dest.isFile()) {
                        setValid(false, msg = new Message(NbBundle.getMessage(CloneDestinationStep.class, "MSG_DEST_IS_FILE_ERROR"), false));
                        destinationValid = false;
                        return false;
                    }
                    VCSFileProxy[] files = dest.listFiles();
                    if (files != null && files.length > 0) {
                        setValid(false, msg = new Message(NbBundle.getMessage(CloneDestinationStep.class, "MSG_DEST_IS_NOT_EMPTY_ERROR"), false));
                        destinationValid = false;
                        return false;
                    }
                } finally {
                    if (msg != null) {
                        final Message message = msg;
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                setValid(true, message);
                            }
                        });
                    }
                }
                destinationValid = true;
            }

            final VCSFileProxy tempRepository;
            try {
                tempRepository = VCSFileProxy.createFileProxy(fileSystem.getTempFolder());
            } catch (IOException ex) {
                return false;
            }
            GitURI uri = repository.getURI();
            if (uri != null) {
                repository.store();
                support = new RepositoryStepProgressSupport(panel.progressPanel, uri);        
                RequestProcessor.Task task = support.start(Git.getInstance().getRequestProcessor(tempRepository), tempRepository, NbBundle.getMessage(RepositoryStep.class, "BK2012"));
                task.waitFinished();
                final Message message = support.message;
                valid = isValid();
                if (message != null) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            setValid(true, message);
                        }
                    });
                }
            }    
        } finally {
            support = null;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    enable(true);
                }
            });
        }
        return valid;
    }

    void waitPopulated () {
        repository.waitPopulated();
    }

    private boolean validateRepository() {
        boolean valid = repository.isValid();
        setValid(valid, repository.getMessage());
        return valid;
    }

    private Message validateNoEmptyDestination () throws MissingResourceException {
        String parent = panel.txtDestination.getText();
        if (parent.trim().isEmpty()) {
            destinationValid = false;
            Message msg = new Message(NbBundle.getMessage(CloneDestinationStep.class, "MSG_EMPTY_PARENT_ERROR"), true);
            setValid(true, msg);
            return msg;
        }
        String name = panel.lblCloneName.getText();
        if (name == null || name.trim().isEmpty()) {
            destinationValid = false;
            Message msg = new Message(NbBundle.getMessage(CloneDestinationStep.class, "MSG_EMPTY_NAME_ERROR"), true);
            setValid(true, msg);
            return msg;
        }
        destinationValid = true;
        setValid(true, null);
        return null;
    }

    public Map<String, GitBranch> getBranches() {
        return branches;
    }
        
    public GitURI getURI() {
        return repository.getURI();
    }

    @Override
    protected JComponent getJComponent () {
        return panel;
    }

    @Override
    public HelpCtx getHelp () {
        return new HelpCtx(RepositoryStep.class);
    }

    @Override
    public void prepareValidation () {
        validatingFinish = wiz.isFinishing();
        enable(false);
    }    
    
    public void cancelBackgroundTasks () {
        if (support != null) {
            support.cancel();
        }
    }

    public Map<String, GitBranch> getRemoteBranches () {
        return remoteBranches;
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        panel.lblCloneName.setText("/" + getCloneName(repository.getURI()));
        setValid(repository.isValid(), repository.getMessage());
    }

    @Override
    public boolean isFinishPanel () {
        return destinationValid;
    }

    void store() {
        repository.store();
    }

    String getDestinationFolder () {
        return panel.txtDestination.getText().trim();
    }

    private void enable (boolean enabled) {
        repository.setEnabled(enabled);
        panel.txtDestination.setEnabled(enabled);
        panel.btnBrowseDestination.setEnabled(enabled);
    }

    private String getCloneName (GitURI uri) {
        String lastElem = ""; //NOI18N
        if (uri != null) {
            String path = uri.getPath();
            // get the last path element
            String[] pathElements = path.split("[/\\\\]"); //NOI18N
            for (int i = pathElements.length - 1; i >= 0; --i) {
                lastElem = pathElements[i];
                if (!lastElem.isEmpty()) {
                    break;
                }
            }
            if (!lastElem.isEmpty()) {
                // is it of the usual form abcdrepository.git ?
                if (lastElem.endsWith(".git")) { //NOI18N
                    lastElem = lastElem.substring(0, lastElem.length() - 4);
                }
                if (!lastElem.isEmpty()) {
                    return lastElem;
                }
            }
        }
        return lastElem.trim();
    }

    VCSFileProxy getDestination () {
        return VCSFileProxySupport.getResource(fileSystem, panel.txtDestination.getText().trim() + "/" + panel.lblCloneName.getText());
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validateNoEmptyDestination();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validateNoEmptyDestination();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }

    @NbBundle.Messages({
        "# {0} - repository URL",
        "MSG_RepositoryStep.errorCredentials=Incorrect credentials for repository at {0}",
        "# {0} - repository URL",
        "MSG_RepositoryStep.errorCannotConnect=Cannot connect to repository at {0}"
    })
    private class RepositoryStepProgressSupport extends WizardStepProgressSupport {
        private final GitURI uri;
        private Message message;

        public RepositoryStepProgressSupport(JPanel panel, GitURI uri) {
            super(panel, true);
            this.uri = uri;
        }

        @Override
        public void perform() {
            GitClient client = null;
            try {
                client = Git.getInstance().getClient(getRepositoryRoot(), this, false);
                client.init(getProgressMonitor());
                branches = new HashMap<>();
                branches.putAll(client.listRemoteBranches(uri.toPrivateString(), getProgressMonitor()));
            } catch (GitException.AuthorizationException ex) {
                GitClientExceptionHandler.notifyException(ex, false);
                message = new Message(Bundle.MSG_RepositoryStep_errorCredentials(uri.toString()), false);
                setValid(false, message);
            } catch (final GitException ex) {
                GitClientExceptionHandler.notifyException(ex, false);
                message = new Message(Bundle.MSG_RepositoryStep_errorCannotConnect(uri.toString()), false);
                GitModuleConfig.getDefault().removeConnectionSettings(repository.getURI());
                setValid(false, message);
            } finally {
                if (client != null) {
                    client.release();
                }
                VCSFileProxySupport.delete(getRepositoryRoot());
                if (message == null && isCanceled()) {
                    message = new Message(NbBundle.getMessage(RepositoryStep.class, "MSG_RepositoryStep.validationCanceled"), true); //NOI18N
                    setValid(false, message);
                }
            }
        }

        @Override
        public void setEnabled(boolean editable) {
            enable(editable);
        }        
    };

}

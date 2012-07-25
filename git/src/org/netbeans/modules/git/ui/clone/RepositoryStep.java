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

package org.netbeans.modules.git.ui.clone;

import java.awt.EventQueue;
import org.netbeans.modules.git.ui.repository.remote.RemoteRepository;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.utils.WizardStepProgressSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryStep extends AbstractWizardPanel implements ActionListener, ChangeListener, AsynchronousValidatingPanel<WizardDescriptor> {

    private RepositoryStepProgressSupport support;
    private Map<String, GitBranch> branches;
    
    private Map<String, GitBranch> remoteBranches;
    private final RepositoryStepPanel panel;
    private final RemoteRepository repository;

    public RepositoryStep (PasswordAuthentication pa, String forPath) {
        repository = new RemoteRepository(pa, forPath);
        repository.addChangeListener(this);
        this.panel = new RepositoryStepPanel(repository.getPanel());
        validateRepository();
    }

    @Override
    public void actionPerformed (ActionEvent e) {

    }

    @Override
    protected final void validateBeforeNext () {
        waitPopulated();
        try {
            branches = null;
            if(!validateRepository()) return;

            final File tempRepository = Utils.getTempFolder();
            GitURI uri = repository.getURI();
            if (uri != null) {
                repository.store();
                support = new RepositoryStepProgressSupport(panel.progressPanel, uri);        
                RequestProcessor.Task task = support.start(Git.getInstance().getRequestProcessor(tempRepository), tempRepository, NbBundle.getMessage(RepositoryStep.class, "BK2012"));
                task.waitFinished();
                GitModuleConfig.getDefault().removeConnectionSettings(repository.getURI());
                final Message message = support.message;
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
            repository.setEnabled(true);
        }
    }

    void waitPopulated () {
        repository.waitPopulated();
    }

    private boolean validateRepository() {
        boolean valid = repository.isValid();
        setValid(valid, repository.getMessage());
        return valid;
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
        repository.setEnabled(false);
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
        setValid(repository.isValid(), repository.getMessage());
    }

    void store() {
        repository.store();
    }

    private class RepositoryStepProgressSupport extends WizardStepProgressSupport {
        private final GitURI uri;
        private Message message;

        public RepositoryStepProgressSupport(JPanel panel, GitURI uri) {
            super(panel, true);
            this.uri = uri;
        }

        @Override
        public void perform() {
            try {
                GitClient client = Git.getInstance().getClient(getRepositoryRoot(), this, false);
                client.init(getProgressMonitor());
                branches = new HashMap<String, GitBranch>();
                branches.putAll(client.listRemoteBranches(uri.toPrivateString(), getProgressMonitor()));
            } catch (final GitException ex) {
                GitClientExceptionHandler.notifyException(ex, false);
                message = new Message(ex.getMessage(), false);
                setValid(false, message);
            } finally {
                Utils.deleteRecursively(getRepositoryRoot());
                if (message == null && isCanceled()) {
                    message = new Message(NbBundle.getMessage(RepositoryStep.class, "MSG_RepositoryStep.validationCanceled"), true); //NOI18N
                    setValid(false, message);
                }
            }
        }

        @Override
        public void setEnabled(boolean editable) {
            repository.setEnabled(editable);
        }        
    };

}

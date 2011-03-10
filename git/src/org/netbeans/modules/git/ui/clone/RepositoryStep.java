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

import javax.swing.event.ChangeEvent;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.ui.repository.remote.*;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.Git;
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
    private final Repository repository;

    public RepositoryStep (String forPath) {
        repository = new Repository(forPath);
        repository.addChangeListener(this);
        this.panel = new RepositoryStepPanel(repository.getPanel());
    }

    @Override
    public void actionPerformed (ActionEvent e) {

    }

    @Override
    protected final void validateBeforeNext () {
        branches = null;

        repository.validateFields();
        if(!repository.isValid()) {
            setValid(false, repository.getMessage());
            return;
        }

        try {
            final File tempRepository = Utils.getTempFolder();
            String uri = repository.getUriString();
            if (uri != null && !uri.trim().isEmpty()) {
                support = new RepositoryStepProgressSupport(panel.progressPanel, repository.getUriString());        
                RequestProcessor.Task task = support.start(Git.getInstance().getRequestProcessor(tempRepository), tempRepository, NbBundle.getMessage(RepositoryStep.class, "BK2012"));
                task.waitFinished();
            }    
        } finally {
            support = null;
        }
    }

    public Map<String, GitBranch> getBranches() {
        return branches;
    }
        
    public String getUriString() {
        return repository.getUriString();
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
        repository.enableFields(false);
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

    // XXX remoteuri vs guri
    private static class RemoteUri implements Comparable<RemoteUri> {
        private final String label;
        private final String uri;
        private final String remoteName;

        public RemoteUri (String remoteName, String uri) {
            this.uri = uri;
            this.remoteName = remoteName;
            this.label = NbBundle.getMessage(SelectUriPanel.class, "SelectUriPanel.configuredRepository.uri", new Object[] { remoteName, uri }); //NOI18N
        }

        @Override
        public String toString () {
            return label;
        }

        @Override
        public int compareTo (RemoteUri other) {
            return toString().compareTo(other.toString());
        }
    }
    
    private class RepositoryStepProgressSupport extends WizardStepProgressSupport {
        private final String uri;

        public RepositoryStepProgressSupport(JPanel panel, String uri) {
            super(panel, true);
            this.uri = uri;
        }

        @Override
        public void perform() {
            try {
                GitClient client = getClient();
                client.init(this);
                branches = new HashMap<String, GitBranch>();
                branches.putAll(client.listRemoteBranches(uri, this));
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, false);
                setValid(false, new Message(ex.getMessage(), true));
                return;
            } finally {
                Utils.deleteRecursively(getRepositoryRoot());
            }
        }

        @Override
        public void setEnabled(boolean editable) {
            repository.enableFields(editable);
        }        
    };

}

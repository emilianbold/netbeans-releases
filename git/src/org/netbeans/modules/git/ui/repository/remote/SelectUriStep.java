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

package org.netbeans.modules.git.ui.repository.remote;

import java.awt.EventQueue;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class SelectUriStep extends AbstractWizardPanel implements ActionListener, DocumentListener, AsynchronousValidatingPanel<WizardDescriptor> {

    private final Map<String, GitRemoteConfig> remotes;
    private final SelectUriPanel panel;
    private final JComponent[] inputFields;
    private GitProgressSupport supp;
    private final File repository;
    private Map<String, GitBranch> remoteBranches;

    public SelectUriStep (File repository, Map<String, GitRemoteConfig> remotes) {
        this.repository = repository;
        this.panel = new SelectUriPanel();
        this.remotes = remotes;
        this.inputFields = new JComponent[] {
            panel.cmbConfiguredRepositories,
            panel.rbConfiguredUri,
            panel.rbCreateNew,
            panel.txtRemoteUri
        };
        fillPanel();
        attachListeners();
        enableFields();
        validateBeforeNext();
    }
    
    private void fillPanel () {
        LinkedList<RemoteUri> list = new LinkedList<RemoteUri>();
        for (Map.Entry<String, GitRemoteConfig> e : remotes.entrySet()) {
            for (String uri : e.getValue().getUris()) {
                list.add(new RemoteUri(e.getKey(), uri));
            }
        }
        RemoteUri[] uris = list.toArray(new RemoteUri[list.size()]);
        Arrays.sort(uris);
        panel.cmbConfiguredRepositories.removeAllItems();
        panel.cmbConfiguredRepositories.setModel(new DefaultComboBoxModel(uris));
        panel.rbCreateNew.setSelected(list.isEmpty());
        if (!list.isEmpty()) {
            panel.cmbConfiguredRepositories.setSelectedIndex(0);
        }
        panel.rbConfiguredUri.setSelected(!panel.rbCreateNew.isSelected());
    }

    private void attachListeners () {
        panel.rbCreateNew.addActionListener(this);
        panel.rbConfiguredUri.addActionListener(this);
        panel.cmbConfiguredRepositories.addActionListener(this);
        panel.txtRemoteUri.getDocument().addDocumentListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.rbCreateNew || e.getSource() == panel.rbConfiguredUri || e.getSource() == panel.cmbConfiguredRepositories) {
            enableFields();
            validateBeforeNext();
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validateBeforeNext();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validateBeforeNext();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }

    private void enableFields () {
        panel.txtRemoteUri.setEnabled(panel.rbCreateNew.isSelected());
        panel.cmbConfiguredRepositories.setEnabled(panel.rbConfiguredUri.isSelected());
    }

    @Override
    protected final void validateBeforeNext () {
        boolean valid = true;
        Message msg = null;
        if (panel.rbConfiguredUri.isSelected()) {
            if (panel.cmbConfiguredRepositories.getSelectedIndex() == -1) {
                msg = new Message(NbBundle.getMessage(SelectUriStep.class, "MSG_SelectUriStep.errorEmptySelection"), false); //NOI18N
            }
        } else if (panel.rbCreateNew.isSelected()) {
            String remoteUri = panel.txtRemoteUri.getText().trim();
            if (remoteUri.isEmpty()) {
                valid = false;
                msg = new Message(NbBundle.getMessage(SelectUriStep.class, "MSG_SelectUriStep.errorEmptyRemoteUri"), false); //NOI18N
            }
        }
        setValid(valid, msg);
    }

    @Override
    protected JComponent getJComponent () {
        return panel;
    }

    @Override
    public HelpCtx getHelp () {
        return new HelpCtx(SelectRemotePanel.class);
    }

    public String getSelectedUri () {
        String selectedUri;
        if (panel.rbConfiguredUri.isSelected()) {
            selectedUri = ((RemoteUri) panel.cmbConfiguredRepositories.getSelectedItem()).uri;
        } else {
            selectedUri = panel.txtRemoteUri.getText().trim();
        }
        return selectedUri;
    }

    public String getSelectedRemote () {
        String selectedRemote = null;
        if (panel.rbConfiguredUri.isSelected()) {
            selectedRemote = ((RemoteUri) panel.cmbConfiguredRepositories.getSelectedItem()).remoteName;
        }
        return selectedRemote;
    }

    @Override
    public void prepareValidation () {
        setEnabled(false);
    }

    @Override
    public void validate () throws WizardValidationException {
        super.validate();
        final Message[] message = new Message[1];
        supp = new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                String uri = getSelectedUri();
                try {
                    GitClient client = getClient();
                    remoteBranches = client.listRemoteBranches(uri, this);
                } catch (GitException ex) {
                    Logger.getLogger(SelectUriStep.class.getName()).log(Level.INFO, "Cannot connect to " + uri, ex); //NOI18N
                    message[0] = new Message(NbBundle.getMessage(SelectUriStep.class, "MSG_SelectUriStep.errorCannotConnect"), false); //NOI18N
                }                
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(SelectUriStep.class, "LBL_SelectUriStep.progressName")).waitFinished(); //NOI18N
        if (message[0] != null) {
            setValid(false, message[0]);
        }
        //enable input
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                setEnabled(true);
            }
        });
    }

    private void setEnabled (boolean enabled) {
        for (JComponent inputField : inputFields) {
            inputField.setEnabled(enabled);
        }
    }

    public void cancelBackgroundTasks () {
        if (supp != null) {
            supp.cancel();
        }
    }

    public Map<String, GitBranch> getRemoteBranches () {
        return remoteBranches;
    }
    
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
}

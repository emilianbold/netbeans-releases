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

import org.netbeans.modules.git.ui.repository.remote.*;
import java.awt.EventQueue;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryStep extends AbstractWizardPanel implements ActionListener, DocumentListener, AsynchronousValidatingPanel<WizardDescriptor> {

    private final RepositoryPanel panel;
    private final JComponent[] inputFields;
    private GitProgressSupport supp;
    private Map<String, GitBranch> remoteBranches;

    public RepositoryStep () {
        this.panel = new RepositoryPanel();
        this.inputFields = new JComponent[] {
            panel.urlComboBox,
            panel.userTextField,
            panel.userPasswordField,
            panel.savePasswordCheckBox,
            panel.chooseFolderButton,
            panel.proxySettingsButton
        };
        attachListeners();
        validateBeforeNext();
        setFieldsVisibility();
        initUrlComboValues();
    }
    
    private void attachListeners () {
        panel.proxySettingsButton.addActionListener(this);
        panel.chooseFolderButton.addActionListener(this);
        panel.urlComboBox.addActionListener(this);
        ((JTextComponent) panel.urlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);        
        panel.userPasswordField.getDocument().addDocumentListener(this);
        panel.userTextField.getDocument().addDocumentListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {

    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validateBeforeNext();
        setFieldsVisibility();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validateBeforeNext();
        setFieldsVisibility();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
        validateBeforeNext();
        setFieldsVisibility();
    }    

    @Override
    protected final void validateBeforeNext () {
        boolean valid = true;
        Message msg = null;
        
        String urlString = (String) panel.urlComboBox.getEditor().getItem();
        if(urlString == null || urlString.isEmpty()) {
            valid = false;
            msg = new Message("Url cannot be empty.", true);
        } else {
            // XXX check URI
        }
        
        setValid(valid, msg);
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
        setEnabled(false);
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
    
    private void setFieldsVisibility() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String urlString = (String) panel.urlComboBox.getEditor().getItem();
                final boolean isFile = urlString != null && urlString.toLowerCase().startsWith("file:///");
                
                panel.chooseFolderButton.setVisible(isFile);
                
                panel.passwordLabel.setVisible(!isFile);
                panel.userPasswordField.setVisible(!isFile);
                panel.userLabel.setVisible(!isFile);
                panel.userTextField.setVisible(!isFile);
                panel.proxySettingsButton.setVisible(!isFile);
                panel.savePasswordCheckBox.setVisible(!isFile);
                panel.leaveBlankLabel.setVisible(!isFile);
            }
        });
    }

    private void initUrlComboValues() {
//        String 
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

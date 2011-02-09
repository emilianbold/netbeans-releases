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

import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitRemoteConfig;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
class SelectRemoteStep extends AbstractWizardPanel implements ActionListener, DocumentListener {

    private final Map<String, GitRemoteConfig> remotes;
    private final SelectRemotePanel panel;
    private String selectedRemote;

    public SelectRemoteStep (Map<String, GitRemoteConfig> remotes, String selectedRemote) {
        this.panel = new SelectRemotePanel();
        this.remotes = remotes;
        fillPanel(selectedRemote);
        attachListeners();
        enableFields();
        validateBeforeNext();
    }
    
    private void fillPanel (String selectedRemote) {
        ArrayList<String> list = new ArrayList<String>(remotes.size());
        for (Map.Entry<String, GitRemoteConfig> remote : remotes.entrySet()) {
            list.add(remote.getKey());
        }
        Collections.sort(list);
        panel.cmbRemotes.removeAllItems();
        panel.cmbRemotes.setModel(new DefaultComboBoxModel(list.toArray(new String[list.size()])));
        panel.cmbRemotes.setSelectedItem(selectedRemote);
        panel.rbCreateNew.setSelected(selectedRemote == null);
        panel.rbUpdate.setSelected(selectedRemote != null);
    }

    private void attachListeners () {
        panel.rbCreateNew.addActionListener(this);
        panel.rbUpdate.addActionListener(this);
        panel.cmbRemotes.addActionListener(this);
        panel.txtNewRemoteName.getDocument().addDocumentListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.rbCreateNew || e.getSource() == panel.rbUpdate || e.getSource() == panel.cmbRemotes) {
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
        panel.txtNewRemoteName.setEnabled(panel.rbCreateNew.isSelected());
        panel.cmbRemotes.setEnabled(panel.rbUpdate.isSelected());
    }

    @Override
    protected final void validateBeforeNext () {
        boolean valid = true;
        Message msg = null;
        if (panel.rbUpdate.isSelected()) {
            valid = panel.cmbRemotes.getSelectedIndex() != -1;
            if (valid) {
                selectedRemote = (String) panel.cmbRemotes.getSelectedItem();
            } else {
                msg = new Message(NbBundle.getMessage(SelectRemoteStep.class, "MSG_SelectRemoteStep.errorEmptySelection"), true); //NOI18N
            }
        } else if (panel.rbCreateNew.isSelected()) {
            String remoteName = panel.txtNewRemoteName.getText().trim();
            if (remoteName.isEmpty()) {
                valid = false;
                msg = new Message(NbBundle.getMessage(SelectRemoteStep.class, "MSG_SelectRemoteStep.errorEmptyRemoteName"), true); //NOI18N
            } else if (remotes.containsKey(remoteName)) {
                valid = false;
                msg = new Message(NbBundle.getMessage(SelectRemoteStep.class, "MSG_SelectRemoteStep.errorExistingRemoteName"), false); //NOI18N
            } else {
                selectedRemote = remoteName;
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

    String getSelectedRemote () {
        return selectedRemote;
    }
}

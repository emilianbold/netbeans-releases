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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class FetchUrisPanelController extends AbstractWizardPanel implements ActionListener, ListSelectionListener, DocumentListener, FinishablePanel<WizardDescriptor> {
    private final FetchUrisPanel panel;
    private GitRemoteConfig remote;
    private JButton okButton;
    private DialogDescriptor dd;

    public FetchUrisPanelController (GitRemoteConfig remote) {
        this.panel = new FetchUrisPanel();
        this.remote = remote;
        fillPanel();
        attachListeners();
        validateBeforeNext();
    }
    
    void setRemote (GitRemoteConfig remote) {
        if (this.remote != remote && (this.remote == null || remote == null || !remote.getUris().equals(this.remote.getUris()))) {
            this.remote = remote;
            fillPanel();
            validateBeforeNext();
        }
    }

    @Override
    public FetchUrisPanel getJComponent () {
        return panel;
    }

    private void fillPanel () {
        DefaultListModel model = new DefaultListModel();
        if (remote != null) {
            model.setSize(remote.getUris().size());
            int i = 0;
            for (String uri : remote.getUris()) {
                model.set(i++, uri);
            }
        }
        panel.lstURIs.setModel(model);
    }

    private void attachListeners () {
        panel.btnAddNew.addActionListener(this);
        panel.btnRemoveSelected.addActionListener(this);
        panel.lstURIs.getSelectionModel().addListSelectionListener(this);
        panel.txtNewURI.getDocument().addDocumentListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.btnRemoveSelected) {
            panel.lstURIs.getSelectionModel().setValueIsAdjusting(true);
            for (Object toRemove : panel.lstURIs.getSelectedValues()) {
                ((DefaultListModel) panel.lstURIs.getModel()).removeElement(toRemove);
            }
            panel.lstURIs.getSelectionModel().setValueIsAdjusting(false);
        } else if (e.getSource() == panel.btnAddNew) {
            String addCandidate = panel.txtNewURI.getText().trim();
            if (!((DefaultListModel) panel.lstURIs.getModel()).contains(addCandidate)) {
                ((DefaultListModel) panel.lstURIs.getModel()).addElement(addCandidate);
            }
        }
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            panel.btnRemoveSelected.setEnabled(panel.lstURIs.getSelectedIndices().length > 0);
            validateBeforeNext();
        }
    }

    public boolean showDialog () {
        okButton = new JButton(NbBundle.getMessage(FetchUrisPanelController.class, "LBL_FetchUrisPanel.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(FetchUrisPanelController.class, "LBL_FetchUrisPanel.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(FetchUrisPanel.class), null);
        validateBeforeNext();
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return okButton == dd.getValue();
    }

    @Override
    protected final void validateBeforeNext () {
        panel.lblMessage.setVisible(false);
        if (dd == null) {
            if (panel.lstURIs.getModel().getSize() == 0) {
                setValid(false, new Message(NbBundle.getMessage(FetchUrisPanelController.class, "MSG_FetchUrisPanel.errorEmptyList"), true)); //NOI18N
            } else {
                setValid(true, null);
            }
        } else {
            boolean enabled = panel.lstURIs.getSelectedValues().length == 1;
            okButton.setEnabled(enabled);
            dd.setValid(enabled);
            panel.lblMessage.setVisible(!enabled);
        }
    }

    public String getSelectedURI () {
        return (String) panel.lstURIs.getSelectedValue();
    }

    public String[] getURIs () {
        DefaultListModel m = (DefaultListModel) panel.lstURIs.getModel();
        List<String> uris = new LinkedList<String>();
        for (Object uri : m.toArray()) {
            uris.add((String) uri);
        }
        return uris.toArray(new String[uris.size()]);
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validateAdd();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validateAdd();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }

    private void validateAdd () {
        panel.btnAddNew.setEnabled(!panel.txtNewURI.getText().trim().isEmpty());
    }

    @Override
    public boolean isFinishPanel () {
        return true;
    }
}

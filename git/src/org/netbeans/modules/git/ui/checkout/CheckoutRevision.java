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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.checkout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author ondra
 */
public class CheckoutRevision implements DocumentListener, ActionListener, PropertyChangeListener {
    private CheckoutRevisionPanel panel;
    private RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean revisionValid = true;
    private boolean nameValid;
    private String branchName;
    private final Map<String, GitBranch> branches;
    private boolean previouslySelected;

    public CheckoutRevision (File repository, RepositoryInfo info, String initialRevision) {
        revisionPicker = new RevisionDialogController(repository, new File[] { repository }, initialRevision);
        panel = new CheckoutRevisionPanel(revisionPicker.getPanel());
        info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
        this.branches = info.getBranches();
    }

    String getRevision () {
        return revisionPicker.getRevision();
    }
    
    String getBranchName () {
        return panel.branchNameField.getText();
    }
    
    boolean isCreateBranchSelected () {
        return panel.cbCheckoutAsNewBranch.isSelected();
    }

    boolean show() {
        okButton = new JButton(NbBundle.getMessage(CheckoutRevision.class, "LBL_CheckoutRevision.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(CheckoutRevision.class, "LBL_CheckoutRevision.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(CheckoutRevision.class), null);
        validateBranchCB();
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    setRevisionValid(Boolean.TRUE.equals(evt.getNewValue()));
                }
            }
        });
        panel.branchNameField.getDocument().addDocumentListener(this);
        panel.cbCheckoutAsNewBranch.addActionListener(this);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return okButton == dd.getValue();
    }
    
    private void setRevisionValid (boolean flag) {
        this.revisionValid = flag;
        if (flag) {
            validateBranchCB();
        } else {
            validate();
        }
    }

    private void validate () {
        boolean flag = revisionValid;
        if (panel.cbCheckoutAsNewBranch.isSelected() && !nameValid) {
            flag = false;
        }
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validateName();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validateName();
    }

    @Override
    public void changedUpdate (DocumentEvent e) { }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.cbCheckoutAsNewBranch) {
            panel.branchNameField.setEnabled(panel.cbCheckoutAsNewBranch.isSelected());
            validate();
        }
    }

    private void validateName () {
        nameValid = false;
        branchName = panel.branchNameField.getText();
        nameValid = !branches.containsKey(branchName);
        validate();
    }

    private void validateBranchCB () {
        String rev = revisionPicker.getRevision();
        if (rev.startsWith(GitUtils.PREFIX_R_HEADS)) {
            rev = rev.substring(GitUtils.PREFIX_R_HEADS.length());
        }
        boolean enabled = panel.cbCheckoutAsNewBranch.isEnabled();
        panel.cbCheckoutAsNewBranch.setEnabled(branches.containsKey(rev));
        if (panel.cbCheckoutAsNewBranch.isEnabled()) {
            if (!enabled) {
                panel.cbCheckoutAsNewBranch.setSelected(previouslySelected);
            }
            previouslySelected = panel.cbCheckoutAsNewBranch.isSelected();
        } else {
            previouslySelected = panel.cbCheckoutAsNewBranch.isSelected();
            panel.cbCheckoutAsNewBranch.setSelected(true);
        }
        if (enabled != panel.cbCheckoutAsNewBranch.isEnabled()) {
            actionPerformed(new ActionEvent(panel.cbCheckoutAsNewBranch, ActionEvent.ACTION_PERFORMED, null));
        }
        validate();
    }

    @Override
    public void propertyChange (final PropertyChangeEvent evt) {
        if (RepositoryInfo.PROPERTY_BRANCHES.equals(evt.getPropertyName())) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run () {
                    branches.clear();
                    branches.putAll((Map<String, GitBranch>) evt.getNewValue());
                    validateName();
                    validateBranchCB();
                }
            });
        }
    }
}

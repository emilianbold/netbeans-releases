/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mercurial.remote.ui.update;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.netbeans.modules.mercurial.remote.ui.repository.ChangesetPickerPanel;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class RevertModifications implements PropertyChangeListener {

    private RevertModificationsPanel panel;
    private JButton okButton;
    private JButton cancelButton;
    private final VCSFileProxy repository;
    
    /** Creates a new instance of RevertModifications */
    public RevertModifications(VCSFileProxy repository, VCSFileProxy[] files) {
        this (repository, files, null);
    }

    public RevertModifications(VCSFileProxy repository, VCSFileProxy[] files, String defaultRevision) {
        this.repository = repository;
        panel = new RevertModificationsPanel(repository, files);
        okButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(okButton, org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Revert")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Revert")); // NOI18N
        okButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSN_RevertForm_Action_Revert")); // NOI18N
        cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSN_RevertForm_Action_Cancel")); // NOI18N
        okButton.setEnabled(false);
        panel.addPropertyChangeListener(this);
    } 
    
    public boolean showDialog() {
        VCSFileProxy[] revertFiles = panel.getRootFiles();
        if (revertFiles == null) {
            revertFiles = new VCSFileProxy[] { repository };
        }
        DialogDescriptor dialogDescriptor;

        String title;
        if (revertFiles.length == 1) {
            title = org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertDialog", revertFiles[0].getName()); // NOI18N
        } else {
            title = org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_MultiRevertDialog"); // NOI18N
        }
        dialogDescriptor =
            new DialogDescriptor(panel,
                title,
                true,
                new Object[] {okButton, cancelButton},
                okButton, 
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(this.getClass()),
                null);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        if (revertFiles.length == 1) {
            dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertDialog", revertFiles[0].getName())); // NOI18N
        } else {
            dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_MultiRevertDialog")); // NOI18N
        }
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ChangesetPickerPanel.PROP_VALID.equals(evt.getPropertyName()) && okButton != null) {
            boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
            okButton.setEnabled(valid);
        }       
    }

    public String getSelectionRevision() {
        if (panel == null) {
            return null;
        }
        return panel.getSelectedRevisionCSetId();
    }
    
    public boolean isBackupRequested() {
        if (panel == null) {
            return false;
        }
        return panel.isBackupRequested();
    }

    boolean isRemoveNewFilesRequested () {
        if (panel == null) {
            return false;
        }
        return panel.isPurgeRequested();
    }

}

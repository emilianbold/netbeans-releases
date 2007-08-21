/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial.ui.merge;

import org.netbeans.modules.mercurial.ui.update.*;
import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import java.io.File;
import org.openide.util.NbBundle;

/**
 *
 * @author Padraig O'Briain
 */
public class MergeRevisions implements PropertyChangeListener {

    private MergeRevisionsPanel panel;
    private JButton okButton;
    private JButton cancelButton;
    
    public MergeRevisions(File repository) {
        panel = new MergeRevisionsPanel(repository);
         okButton = new JButton(NbBundle.getMessage(MergeRevisions.class, 
                     "CTL_MergeForm_Action_Merge")); // NOI18N
         okButton.getAccessibleContext().setAccessibleDescription(
                 NbBundle.getMessage(MergeRevisions.class, 
                 "ACSD_MergeForm_Action_Merge")); // NOI18N
         cancelButton = new JButton(NbBundle.getMessage(MergeRevisions.class, 
                 "CTL_MergeForm_Action_Cancel")); // NOI18N
         cancelButton.getAccessibleContext().setAccessibleDescription(
                 NbBundle.getMessage(MergeRevisions.class, 
                 "ACSD_MergeForm_Action_Cancel")); // NOI18N
    } 
    
    public boolean showDialog() {
        DialogDescriptor dialogDescriptor;
        dialogDescriptor = new DialogDescriptor(panel, 
                NbBundle.getMessage(MergeRevisions.class, "ASCD_MERGE_DIALOG")); // NOI18N

        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(MergeRevisions.class, "ASCD_MERGE_DIALOG")); // NOI18N
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(okButton != null) {
            boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
            okButton.setEnabled(valid);
        }       
    }

    public String getSelectionRevision() {
        if (panel == null) return null;
        return panel.getSelectedRevision();
    }
}

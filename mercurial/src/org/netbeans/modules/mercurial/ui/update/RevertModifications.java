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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial.ui.update;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import java.io.File;

/**
 *
 * @author Padraig O'Briain
 */
public class RevertModifications implements PropertyChangeListener {

    private RevertModificationsPanel panel;
    private JButton okButton;
    private JButton cancelButton;
    
    /** Creates a new instance of RevertModifications */
    public RevertModifications(File repository, File[] files) {
        this (repository, files, null);
    }

    public RevertModifications(File repository, File[] files, String defaultRevision) {
        panel = new RevertModificationsPanel(repository, files);
        okButton = new JButton(org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Revert")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Revert")); // NOI18N
        cancelButton = new JButton(org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Cancel")); // NOI18N
    } 
    
    public boolean showDialog() {
        File[] revertFiles = panel.getRevertFiles();
        DialogDescriptor dialogDescriptor;
        if (revertFiles.length == 1) {
            dialogDescriptor = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertDialog", revertFiles[0].getName())); // NOI18N
        } else {
            dialogDescriptor = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_MultiRevertDialog")); // NOI18N 
        }
        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
        
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

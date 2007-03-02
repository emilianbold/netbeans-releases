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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors.css;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import org.netbeans.modules.visualweb.propertyeditors.StandardUrlPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  Winston Prakash
 */
public class BackgroundImageUrlDialog extends StandardUrlPanel{
    
    private JDialog dialog;
    private DialogDescriptor dlg = null;
    private String okString =  NbBundle.getMessage(BackgroundImageUrlDialog.class, "OK");
    private String cancelString =  NbBundle.getMessage(BackgroundImageUrlDialog.class, "CANCEL");
    
    private JButton okButton = new JButton(okString);
    private JButton cancelButton = new JButton(cancelString);
   
    public void showDialog(){
        
        // Add a listener to the dialog's buttons
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Object o = evt.getSource();
                Object[] option = dlg.getOptions();
                
                if (o == option[0]) {
                    // Dismiss the dialog
                    dialog.hide();
                }
            }
        };
        this.initialize();
        dlg = new DialogDescriptor(this, NbBundle.getMessage(BackgroundImageUrlDialog.class, "SELECT_IMAGE_DIALOG_TITLE"), true, listener);
        dlg.setOptions(new Object[] { okButton, cancelButton });
        dlg.setClosingOptions(new Object[] {cancelButton});
        
        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setResizable(true);
        dialog.pack();
        dialog.show();
    }
}

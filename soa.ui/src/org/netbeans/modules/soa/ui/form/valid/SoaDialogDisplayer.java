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
package org.netbeans.modules.soa.ui.form.valid;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * This modified dialog displayer supports the AbstractDialogDescriptor.
 * 
 * 
 * @author nk160297
 */
public class SoaDialogDisplayer extends DialogDisplayer {
    
    private static SoaDialogDisplayer singleton = new SoaDialogDisplayer();
    
    protected SoaDialogDisplayer() {
        super();
    }
    
    public static synchronized SoaDialogDisplayer getDefault() {
        return singleton;
    }
    
    public Dialog createDialog(DialogDescriptor dialogDescriptor) {
        DialogDisplayer defaultDD = super.getDefault();
        Dialog dialog = defaultDD.createDialog(dialogDescriptor);
        //
        if (dialogDescriptor instanceof AbstractDialogDescriptor) {
            return createNodeEditorDialog(
                    dialog, (AbstractDialogDescriptor)dialogDescriptor);
        } else {
            return dialog;
        }
    }
    
    private Dialog createNodeEditorDialog(
            final Dialog dialog, final AbstractDialogDescriptor desctipror) {
        //
        if (dialog instanceof JDialog) {
            ((JDialog)dialog).setDefaultCloseOperation(
                    WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        //
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // The Ok and Cancel buttons are processed here
                desctipror.processWindowClose();
            }
            @Override
            public void windowClosing(WindowEvent e) {
                // The cross button is processed here.
                // The Ok and Cancel buttons are not processed here
                dialog.setVisible(false);
                desctipror.processWindowClose();
                dialog.dispose();
            }
//            public void windowOpened(WindowEvent e) {
//            }
        });
        //
        return dialog;
    }
    
    public Object notify(NotifyDescriptor notifyDescriptor) {
        if (notifyDescriptor instanceof AbstractDialogDescriptor) {
            Dialog dialog = createDialog((DialogDescriptor)notifyDescriptor);
            dialog.setModal(true);
            dialog.setVisible(true);
            return null;
        } else {
            // for other cases call the super implementaiton.
            return super.getDefault().notify(notifyDescriptor);
        }
    }
}

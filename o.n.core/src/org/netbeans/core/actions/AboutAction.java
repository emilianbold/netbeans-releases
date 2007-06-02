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

package org.netbeans.core.actions;

import java.awt.Dialog;
import javax.swing.JButton;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.Presenter;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/** The action that shows the AboutBox.
*
* @author Ian Formanek
*/
public class AboutAction extends CallableSystemAction {

    public AboutAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public void performAction () {
        DialogDescriptor descriptor = new DialogDescriptor(
            new org.netbeans.core.ui.ProductInformationPanel (),
            NbBundle.getMessage(AboutAction.class, "About_title"),
            true,
                new Object[0],
                null,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setResizable(false);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AboutAction.class);
    }

    public String getName() {
        return NbBundle.getMessage(AboutAction.class, "About");
    }

}

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


package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author ludo
 */
public class MasterPasswordInputDialog extends NotifyDescriptor {


    /**
     * The text field used to enter the input.
     */
    protected MasterPasswordInputPanel panel=new MasterPasswordInputPanel();
    static protected final String title =NbBundle.getMessage(MasterPasswordInputDialog.class, "MasterPasswordInputDialog_title");
    /** Construct dialog with the specified title and label text.
     * @param text label text
     * @param title title of the dialog
     */
    public MasterPasswordInputDialog() {
        super(null, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        super.setMessage(panel);
    }
    
    
    
    /**
     * Get the text which the user typed into the input line.
     * @return the text entered by the user
     */
    public String getInputText() {
        return panel.getMasterPassword();
    }
    
    
}
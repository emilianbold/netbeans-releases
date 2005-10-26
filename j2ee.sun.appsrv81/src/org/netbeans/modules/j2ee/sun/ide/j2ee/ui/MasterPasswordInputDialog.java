/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author ludo
 */
public class MasterPasswordInputDialog extends NotifyDescriptor {
    
    
    /**
     * The text field used to enter the input.
     */
    protected JPasswordField textField;
    protected MasterPasswordInputPanel panel=new MasterPasswordInputPanel();
    static protected String text =NbBundle.getMessage(MasterPasswordInputDialog.class, "MasterPasswordInputDialog_text");
    static protected String title =NbBundle.getMessage(MasterPasswordInputDialog.class, "MasterPasswordInputDialog_title");
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
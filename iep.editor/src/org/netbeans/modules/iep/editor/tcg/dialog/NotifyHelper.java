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


package org.netbeans.modules.iep.editor.tcg.dialog;

import java.awt.Component;

import javax.swing.JFrame;

import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.modules.iep.editor.tcg.exception.ErrorDisplay;
import org.netbeans.modules.iep.editor.tcg.dialog.Confirm;

/**
 * This class ...
 *
 * @author Bing Lu
 */
public class NotifyHelper {

    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(NotifyHelper.class.getName());
    
    /**
     * This is a convenience method when the user just needs to confirm or
     * cancel something.
     *
     * @param title The title for the dialog
     * @param msg The message to be confirmed
     *
     * @return true if user selects okay, false otherwise
     */
    public static boolean confirm(String title, String msg) {
        Confirm c = new Confirm(title, msg);
        DialogDescriptor dd = new DialogDescriptor(c.getInnerPane(),
                                                   c.getTitle(), true,
                                                   c.getActionListener());
        java.awt.Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return c.isOk();
    }

    /**
     * This method ...
     *
     * @param dap This ...
     */
    public static void reportError(Exception dap) {
        reportError(new JFrame(), dap);
    }     
     
    /**
     * This method ...
     *
     * @param c This ...
     * @param dap This ...
     */
    public static void reportError(Component c, Exception dap) {
            ErrorManager.getDefault().notify(dap);
        
    }

    /**
     * DOCUMENT ME!
     *
     * @param s The error message
     *
     * todo Document this method
     */
    public static void reportError(String s) {
        java.awt.Frame mainFrame = org.openide.windows.WindowManager.getDefault().getMainWindow();
        new ErrorDisplay(mainFrame, s);
        //reportError(new JFrame(), s);
    }

    /**
     * This method ...
     *
     * @param c This ...
     * @param s This ...
     */
    public static void reportError(Component c, String s) {
            NotifyDescriptor d = new NotifyDescriptor.Message(s,
                                     NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        
    }

    /**
     * This method ...
     *
     * @param s The info message
     *
     * @return Whether the user selected ok or cancel.
     */
    public static Object reportInfo(String s) {
            NotifyDescriptor d = new NotifyDescriptor.Message(s,
                                     NotifyDescriptor.INFORMATION_MESSAGE);
            return DialogDisplayer.getDefault().notify(d);
        
    }

    /**
     * This method ...
     *
     * @param c This ...
     * @param s This ...
     *
     * @return DOCUMENT ME!
     */
    public static Object reportInfo(Component c, String s) {
            NotifyDescriptor d = new NotifyDescriptor.Message(s,
                                     NotifyDescriptor.INFORMATION_MESSAGE);
            return DialogDisplayer.getDefault().notify(d);
        
    }
}


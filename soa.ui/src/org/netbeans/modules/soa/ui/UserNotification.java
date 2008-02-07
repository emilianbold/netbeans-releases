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
package org.netbeans.modules.soa.ui;

import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Utility for showing notification dialogs to the user
 *
 * @author nk160297
 */
public class UserNotification {
    
    public static void showMessage(Throwable ex, String message) {
        if (message == null || message.length() == 0) {
            ErrorManager.getDefault().notify(ex);
        } else {
            showMessage(message);
        }
    }
    
    public static void showMessage(String message) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(message);
        descriptor.setOptionType(NotifyDescriptor.DEFAULT_OPTION);
        DialogDisplayer.getDefault().notify(descriptor);
    }
    
    public static void showMessageAsinc(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showMessage(message);
            }
        });
    }
    
    /**
     * Shows a warning message.
     * @param message
     * @return the user's decision. 
     * If the user ignores the warning then the true is returned.
     */
    public static boolean showWarningMessage(String message) {
        String lineSeparator = System.getProperty("line.separator"); // NOI18N
        String warningMsg = 
                NbBundle.getMessage(UserNotification.class, "LBL_WarningMsg"); // NOI18N
        String ignoreMsg = 
                NbBundle.getMessage(UserNotification.class, "LBL_IgnoreMsg"); // NOI18N
        String msg = warningMsg + lineSeparator + message + 
                lineSeparator + lineSeparator + ignoreMsg;
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(msg);
        descriptor.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        descriptor.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(descriptor);
        return descriptor.getValue() == NotifyDescriptor.OK_OPTION;
    }
    
}

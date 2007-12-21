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
        Object[] options = new Object[] {NotifyDescriptor.CLOSED_OPTION};
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(message);
        DialogDisplayer.getDefault().notify(descriptor);
    }
    
    public static void showMessageAsinc(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showMessage(message);
            }
        });
    }
    
}

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
 *  
 * $Id$
 */
package org.netbeans.installer.utils;

import javax.swing.JOptionPane;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.helper.ErrorLevel;
import static org.netbeans.installer.utils.helper.ErrorLevel.CRITICAL;
import static org.netbeans.installer.utils.helper.ErrorLevel.ERROR;

/**
 *
 * @author Kirill Sorokin
 */
public class ErrorManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static synchronized void notify(String message) {
        notify(ErrorLevel.MESSAGE, message);
    }
    
    public static synchronized void notify(int level, String message) {
        notify(level, message, null);
    }
    
    public static synchronized void notifyError(String message) {
        notify(ERROR, message);
    }
    
    public static synchronized void notifyError(String message, Throwable e) {
        notify(ERROR, message, e);
    }
    
    public static synchronized void notifyCritical(String message) {
        notify(CRITICAL, message);
    }
    
    public static synchronized void notify(Throwable exception) {
        notify(ErrorLevel.ERROR, exception);
    }
    
    public static synchronized void notify(int level, Throwable exception) {
        notify(level, null, exception);
    }
    
    public static synchronized void notify(int level, String message, Throwable exception) {
        // parameters validation
        assert (message != null) || (exception != null);
        
        String dialogTitle;
        String dialogText  = "";
        int dialogType;
        
        if (message != null) {
            LogManager.log(level, message);
            dialogText += message + "\n";
        }
        if (exception != null) {
            LogManager.log(level, exception);
            dialogText += StringUtils.asString(exception);
            
            Throwable cause = exception.getCause();
            while (cause != null) {
                dialogText += "\nCaused by:\n";
                dialogText += StringUtils.asString(cause);
                cause = cause.getCause();
            }
        }
        
        switch (level) {
            case ErrorLevel.MESSAGE:
                JOptionPane.showMessageDialog(null, dialogText, "Message", JOptionPane.INFORMATION_MESSAGE);
                return;
            case ErrorLevel.WARNING:
                JOptionPane.showMessageDialog(null, dialogText, "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            case ErrorLevel.ERROR:
                JOptionPane.showMessageDialog(null, dialogText, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            case ErrorLevel.CRITICAL:
                JOptionPane.showMessageDialog(null, dialogText, "Critical Error", JOptionPane.ERROR_MESSAGE);
                Installer.getInstance().criticalExit();
                return;
            default:
                return;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private ErrorManager() {
    }
}
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
import org.netbeans.installer.utils.LogManager;

/**
 *
 * @author Kirill Sorokin
 */
public class ErrorManager {
    private static ErrorManager instance;
    
    public static ErrorManager getInstance() {
        if (instance == null) {
            instance = new ErrorManager();
        }
        
        return instance;
    }
    
    private ErrorManager() {
    }
    
    public void notify(String message) {
        notify(ErrorLevel.MESSAGE, message);
    }
    
    public void notify(int level, String message) {
        notify(level, message, null);
    }
    
    public void notify(Throwable exception) {
        notify(ErrorLevel.ERROR, exception);
    }
    
    public void notify(int level, Throwable exception) {
        notify(level, null, exception);
    }
    
    public void notify(int level, String message, Throwable exception) {
        // parameters validation
        assert (message != null) || (exception != null);
        
        String dialogTitle;
        String dialogText  = "";
        int dialogType;
        
        if (message != null) {
            LogManager.getInstance().log(level, message);
            dialogText += message + "\n";
        }
        if (exception != null) {
            LogManager.getInstance().log(level, exception);
            dialogText += StringUtils.getInstance().asString(exception);
            
            Throwable cause = exception.getCause();
            while (cause != null) {
                dialogText += "\nCaused by:\n";
                dialogText += StringUtils.getInstance().asString(cause);
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
}
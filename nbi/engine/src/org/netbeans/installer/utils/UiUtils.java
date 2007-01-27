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
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.NO_OPTION;

/**
 *
 * @author Kirill Sorokin
 */
public final class UiUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static boolean showYesNoDialog(String title, String message) {
        final int result = JOptionPane.showConfirmDialog(
                null, 
                message, 
                title, 
                YES_NO_OPTION);
        
        return result == YES_OPTION;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private UiUtils() {
    }
}

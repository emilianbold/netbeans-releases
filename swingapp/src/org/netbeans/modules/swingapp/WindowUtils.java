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

package org.netbeans.modules.swingapp;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Utility class for opening windows. May not be needed anymore and 
 * could probably be removed in the future.
 * @author joshua.marinacci.@sun.com
 */
public abstract class WindowUtils {
    
    /** Creates a new instance of WindowUtils */
    private WindowUtils() {
    }
    
    
    public static JDialog createModalDialog(Component comp, String title) {
        Window window = SwingUtilities.getWindowAncestor(comp);
        JDialog dialog = null;
        if(window instanceof Frame) {
            dialog = new JDialog((Frame)window,title,true);
        } else {
            dialog = new JDialog((Dialog)window,title,true);
        }
        return dialog;
    }
    
}

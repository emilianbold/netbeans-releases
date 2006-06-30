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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.accessibility;

import java.awt.Component;

import javax.accessibility.AccessibleContext;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;

import org.netbeans.jemmy.ComponentChooser;

public abstract class AccessibilityChooser implements ComponentChooser {
    public final boolean checkComponent(Component comp) {
        if(comp instanceof JComponent) {
            return(checkContext(comp.getAccessibleContext()));
        } else if(comp instanceof JDialog) {
            return(checkContext(comp.getAccessibleContext()));
        } else if(comp instanceof JFrame) {
            return(checkContext(comp.getAccessibleContext()));
        } else if(comp instanceof JWindow) {
            return(checkContext(comp.getAccessibleContext()));
        } else {
            return(false);
        }
    }
    public abstract boolean checkContext(AccessibleContext context);
}

/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
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

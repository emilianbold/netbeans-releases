/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.util.ResourceBundle;
import java.awt.*;
import java.awt.datatransfer.*;

import org.openide.util.NbBundle;
import org.netbeans.editor.ImplementationProvider;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/** This is NetBeans specific provider of functionality.
 * See base class for detailed comments.
 *
 * @author David Konecny
 * @since 10/2001
 */

public class NbImplementationProvider extends ImplementationProvider {

    /** Ask NbBundle for the resource bundle */
    public ResourceBundle getResourceBundle(String localizer) {
        return NbBundle.getBundle(localizer);
    }

    public Action getToggleBreakpointAction() {
        try {
            ClassLoader l = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            Class c = l.loadClass("org.netbeans.modules.debugger.support.actions.ToggleBreakpointAction"); // NOI18N
            if (SystemAction.class.isAssignableFrom(c)) {
                return SystemAction.get(c);
            } else {
                return (Action)c.newInstance();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public boolean activateComponent(JTextComponent c) {
        Container container = SwingUtilities.getAncestorOfClass(TopComponent.class, c);
        if (container != null) {
            ((TopComponent)container).requestActive();
            return true;
        }
        return false;
    }

}

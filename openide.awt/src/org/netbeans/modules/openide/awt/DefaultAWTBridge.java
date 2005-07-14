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

package org.netbeans.modules.openide.awt;

import java.awt.Component;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.SystemAction;

/** Default implementaiton of presenters for various action types.
 */
public final class DefaultAWTBridge extends org.netbeans.modules.openide.util.AWTBridge {
    public JMenuItem createMenuPresenter (Action action) {
        if (action instanceof BooleanStateAction) {
            BooleanStateAction b = (BooleanStateAction)action;
            return new Actions.CheckboxMenuItem (b, true);
        }
        if (action instanceof SystemAction) {
            SystemAction s = (SystemAction)action;
            return new Actions.MenuItem (s, true);
        }
            
        return new Actions.MenuItem (action, true);
    }
    
    public JMenuItem createPopupPresenter(Action action) {
        if (action instanceof BooleanStateAction) {
            BooleanStateAction b = (BooleanStateAction)action;
            return new Actions.CheckboxMenuItem (b, false);
        }
        if (action instanceof SystemAction) {
            SystemAction s = (SystemAction)action;
            return new Actions.MenuItem (s, false);
        }
            
        return new Actions.MenuItem (action, false);
    }
    
    public Component createToolbarPresenter(Action action) {
        if (action instanceof BooleanStateAction) {
            BooleanStateAction b = (BooleanStateAction)action;
            return new Actions.ToolbarToggleButton (b);
        }
        if (action instanceof SystemAction) {
            SystemAction s = (SystemAction)action;
            return new Actions.ToolbarButton (s);
        }
            
        return new Actions.ToolbarButton (action);
    }
    
    public javax.swing.JPopupMenu createEmptyPopup() {
        return new org.openide.awt.JPopupMenuPlus ();
    }  
    
    public Component[] convertComponents(Component comp) {
         if (comp instanceof DynamicMenuContent) {
            return ((DynamicMenuContent)comp).getMenuPresenters();
         }
         return new Component[] {comp};
    }
    
}

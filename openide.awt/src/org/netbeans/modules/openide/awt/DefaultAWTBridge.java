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

package org.netbeans.modules.openide.awt;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
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
    
    public JPopupMenu createEmptyPopup() {
        return new JPopupMenu();
    }  
    
    public Component[] convertComponents(Component comp) {
         if (comp instanceof DynamicMenuContent) {
            Component[] toRet = ((DynamicMenuContent)comp).getMenuPresenters();
            boolean atLeastOne = false;
            Collection<Component> col = new ArrayList<Component>();
            for (int i = 0; i < toRet.length; i++) {
                if (toRet[i] instanceof DynamicMenuContent && toRet[i] != comp) {
                    col.addAll(Arrays.asList(convertComponents(toRet[i])));
                    atLeastOne = true;
                } else {
                    if (toRet[i] == null) {
                        toRet[i] = new JSeparator();
                    }
                    col.add(toRet[i]);
                }
            }
            if (atLeastOne) {
                return col.toArray(new Component[col.size()]);
            } else {
                return toRet;
            }
         }
         return new Component[] {comp};
    }
    
}

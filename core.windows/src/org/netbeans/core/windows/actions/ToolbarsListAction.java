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

package org.netbeans.core.windows.actions;

import org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration;
import org.openide.awt.Mnemonics;
import org.openide.awt.ToolbarPool;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

import javax.swing.*;


/** Action that lists toolbars of current toolbar config in a submenu, the
 * same like a popup menu on toolbars area.
 *
 * @author Dafe Simonek
 */
public class ToolbarsListAction extends AbstractAction 
                                implements Presenter.Menu {
    
    public ToolbarsListAction() {
        putValue(NAME,NbBundle.getMessage(ToolbarsListAction.class, "CTL_ToolbarsListAction"));
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        // no operation
    }
    
    public JMenuItem getMenuPresenter() {
        String label = NbBundle.getMessage(ToolbarsListAction.class, "CTL_ToolbarsListAction");
        JMenu menu = new JMenu(label);
        //#40584 fix start setting the empty, transparent icon for the menu item to align it correctly with other items
        //menu.setIcon(new ImageIcon(Utilities.loadImage("org/openide/resources/actions/empty.gif"))); //NOI18N
        //#40584 fix end
        Mnemonics.setLocalizedText(menu, label);
        ToolbarConfiguration curConf = 
            ToolbarConfiguration.findConfiguration(ToolbarPool.getDefault().getConfiguration());
        return curConf.getToolbarsMenu(menu);
    }
    
}


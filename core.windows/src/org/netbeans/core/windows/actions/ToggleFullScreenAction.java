/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.actions;


import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;

import org.netbeans.core.windows.view.ui.MainWindow;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.awt.Mnemonics;
import org.openide.windows.WindowManager;


/**
 * @author   S. Aubrecht
 */
public class ToggleFullScreenAction extends SystemAction implements DynamicMenuContent {

    private JCheckBoxMenuItem [] menuItems;
    
    public JComponent[] getMenuPresenters() {
        createItems();
        updateState();
        return menuItems;
    }

    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return items;
    }
    
    private void updateState() {
        MainWindow frame = (MainWindow)WindowManager.getDefault().getMainWindow();
        menuItems[0].setSelected(frame.isFullScreenMode());
    }
    
    private void createItems() {
        if (menuItems == null) {
            menuItems = new JCheckBoxMenuItem[1];
            menuItems[0] = new JCheckBoxMenuItem(this);
            menuItems[0].setIcon(null);
            Mnemonics.setLocalizedText(menuItems[0], NbBundle.getMessage(ToggleFullScreenAction.class, "CTL_ToggleFullScreenAction"));
        }
    }

    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        MainWindow frame = (MainWindow)WindowManager.getDefault().getMainWindow();
        frame.setFullScreenMode( !frame.isFullScreenMode() );
    }

    public String getName() {
        return NbBundle.getMessage(ToggleFullScreenAction.class, "CTL_ToggleFullScreenAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ToggleFullScreenAction.class);
    }

    public boolean isEnabled() {
        return WindowManager.getDefault().getMainWindow() instanceof MainWindow;
    }
}


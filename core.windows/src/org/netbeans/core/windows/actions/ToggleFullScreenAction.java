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

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */


package org.netbeans.core.windows.actions;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
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
    
    public ToggleFullScreenAction() {
        addPropertyChangeListener( new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if( Action.ACCELERATOR_KEY.equals(evt.getPropertyName()) ) {
                    //119127 - make sure shortcut gets updated in the menu
                    menuItems = null;
                    createItems();
                }
            }
        });
    }
    
    public JComponent[] getMenuPresenters() {
        createItems();
        updateState();
        return menuItems;
    }

    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return menuItems;
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

    @Override
    public boolean isEnabled() {
        return WindowManager.getDefault().getMainWindow() instanceof MainWindow;
    }
}


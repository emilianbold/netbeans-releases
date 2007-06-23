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

package org.netbeans.modules.editor.impl.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.modules.editor.lib.NavigationHistory;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class NavigationHistoryLastEditAction extends BaseAction implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(NavigationHistoryLastEditAction.class.getName());
    
    public NavigationHistoryLastEditAction() {
        super("jump-list-last-edit"); //NOI18N
        putValue(ICON_RESOURCE_PROPERTY, "org/netbeans/modules/editor/resources/navigate_last_edit.png"); // NOI18N

        update();
        NavigationHistory nav = NavigationHistory.getEdits();
        nav.addPropertyChangeListener(WeakListeners.propertyChange(this, nav));
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        NavigationHistory nav = NavigationHistory.getEdits();
        
        NavigationHistory.Waypoint wpt = nav.getCurrentWaypoint();
        if (wpt != null) {
            if (isStandingThere(target, wpt)) {
                wpt = nav.navigateBack();
            } else {
                wpt = null;
            }
        }
        
        if (wpt == null) {
            wpt = nav.navigateLast();
        }
        
        if (wpt != null) {
            NavigationHistoryBackAction.show(wpt);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        update();
    }
    
    private void update() {
        NavigationHistory nav = NavigationHistory.getEdits();
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryLastEditAction.class, 
            "NavigationHistoryLastEditAction_Tooltip_simple"));
        setEnabled(nav.hasNextWaypoints() || nav.hasPreviousWaypoints() || null != nav.getCurrentWaypoint());
    }
 
    private boolean isStandingThere(JTextComponent target, NavigationHistory.Waypoint wpt) {
        return target == wpt.getComponent() && target.getCaret().getDot() == wpt.getOffset();
    }
    
    /** Back action in Go To main menu, wrapper for BaseKit.jumpListPrevAction
     */ 
    public static final class MainMenu extends MainMenuAction {
        
        private JMenuItem jumpLastEditItem;

        public MainMenu () {
            super();
            jumpLastEditItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(NavigationHistoryLastEditAction.class).getString(
                "jump_back_main_menu_item-main-menu"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return jumpLastEditItem;
        }

        protected String getActionName () {
            return "jump-list-last-edit"; //NOI18N
        }
        
//        protected KeyStroke getDefaultAccelerator () {
//            return KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_MASK);
//        }
        
    } // end of JumpBackAction
    
}

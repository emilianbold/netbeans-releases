/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.netbeans.modules.i18n.wizard.I18nWizardAction;
import org.netbeans.modules.i18n.wizard.I18nTestWizardAction;

import org.openide.awt.Actions;
import org.openide.awt.JMenuPlus;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;


/**
 * Abstract superclass for I18N group actions.
 *
 * @author  Peter Zavadsky
 * @see I18nGroupMenuAction
 * @see I18nGroupPopupAction
 */
public abstract class I18nGroupAction extends SystemAction {

   
    /** Array of i18n actions. */
    protected static final SystemAction[] i18nActions = new SystemAction[] {
        SystemAction.get(I18nWizardAction.class),
        SystemAction.get(I18nTestWizardAction.class),
        SystemAction.get(I18nAction.class),
        SystemAction.get(InsertI18nStringAction.class)
    };
    

    /** Does nothing. Shouldn't be called. Implements superclass abstract method. */
    public void actionPerformed(ActionEvent evt) {
    }

    /** Gets localized name of action. Implements superclass abstract method. */
    public String getName() {
        return I18nUtil.getBundle().getString("LBL_I18nGroupActionName");
    }

    /** Gets icon resource. Overrides suprclass method. */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/i18nAction.gif"; // NOI18N
    }

    /** Gets help context. Implements abstract superclass method. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(I18nUtil.HELP_ID_I18N);
    }
    
    
    /** Menu item which will create its items lazilly when the popup will becomming visible.
     * Performance savings.*/
    static class LazyPopup extends JMenuPlus {

        /** Icon. */
        private static Icon icon = null;
        
        /** Indicates if is part of menu, i.e. if should have icons. */
        private boolean isMenu;
        
        /** Indicates whether menu items were created. */
        private boolean created = false;

        
        /** Constructor. */
        private LazyPopup(boolean isMenu, SystemAction action) {
            Actions.setMenuText(this, action.getName(), isMenu);
            
            this.isMenu = isMenu;
            
            if(isMenu) {
                // Binary-incompatible across SystemAction.icon ImageIcon -> Icon change:
                //menu.setIcon (getIcon ());
                if(icon == null) {
                    icon = new ImageIcon(I18nGroupAction.class.getResource("/org/netbeans/modules/i18n/i18nAction.gif")); // NOI18N
                }

                setIcon(icon);
            }
        }

        /** Creates <code>LazyPopup</code> menu item. */
        static JMenuItem createLazyPopup(boolean isMenu, SystemAction action) {
            return new LazyPopup(isMenu, action);
        }
        
        /** Gets popup menu. Overrides superclass. Adds lazy menu items creation. */
        public JPopupMenu getPopupMenu() {
            if(!created)
                createMenuItems();
            
            return super.getPopupMenu();
        }

        /** Creates items when actually needed. */
        private void createMenuItems() {
            created = true;
            removeAll();

            for(int i=0; i<i18nActions.length; i++) {
                SystemAction action = i18nActions[i];

                if(action == null) {
                    addSeparator();
                } else if(!isMenu && action instanceof Presenter.Popup) {
                    add(((Presenter.Popup)action).getPopupPresenter());
                } else if(isMenu && action instanceof Presenter.Menu) {
                    add(((Presenter.Menu)action).getMenuPresenter());
                }
            }
        }
    } // End of class LazyPopup.

}

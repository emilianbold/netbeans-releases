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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.netbeans.modules.i18n.wizard.I18nWizardAction;
import org.netbeans.modules.i18n.wizard.I18nTestWizardAction;

import org.openide.awt.JInlineMenu;
import org.openide.awt.JMenuPlus;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;


/**
 * Abstract class for I18n group actions.
 *
 * @author  Peter Zavadsky
 * @see I18nGroupAction.Menu
 * @see I18nGroupAction.Popup
 */
public abstract class I18nGroupAction extends SystemAction {

    /** Icon. */
    private static Icon icon = null;
    
    /** Array of i18n actions. */
    protected static final SystemAction[] i18nActions = new SystemAction[] {
        SystemAction.get(I18nWizardAction.class),
        SystemAction.get(I18nTestWizardAction.class),
        SystemAction.get(I18nAction.class),
        SystemAction.get(InsertI18nStringAction.class)
    };
    

    /** Does nothing. Shouldn't be called. Implements superclass abstract method. */
    public void actionPerformed(ActionEvent ev) {
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
    
    /** Cretates menu item with lazy popup.
     * @param isMenu flag if the item os part of normal menu (not popup). */
    protected JMenuItem createLazyPopup(boolean isMenu) {
        return new LazyPopup(isMenu);
    }
    
    
    /** Menu item which will create its items lazilly when the popup will becomming visible.
     * Performance savings.*/
    private class LazyPopup extends JInlineMenu implements PopupMenuListener {

        /** Sub menu. */
        private final JMenu menu;

        /** Indicates if is part of menu, i.e. if should have icons. */
        private boolean isMenu;
        
        /** Indicates whether menu items were created. */
        private boolean created = false;
        

        /** Constructor. */
        public LazyPopup(boolean isMenu) {
            this.menu = new JMenuPlus(I18nGroupAction.this.getName());
            this.isMenu = isMenu;
            
            if(isMenu) {
                // Binary-incompatible across SystemAction.icon ImageIcon -> Icon change:
                //menu.setIcon (getIcon ());
                if(icon == null) 
                    icon = new ImageIcon(I18nGroupAction.class.getResource(I18nGroupAction.this.iconResource()));

                menu.setIcon(icon);
            }
            
            menu.getPopupMenu().addPopupMenuListener(this);
            setMenuItems(new JMenuItem[] {menu});
        }


        /** Dummy implementation of <code>PopupMenuListener</code>. */
        public void popupMenuCanceled(PopupMenuEvent evt) {}

        /** Dummy implementation of <code>PopupMenuListener</code>. */
        public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {}

        /** Implemsnts <code>PopupMenuListener</code> method. */
        public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
            // We are AWT-Event-queue.
            if(!created)
                createMenuItems(); 
        }

        /** Creates items when actually needed. */
        private void createMenuItems() {
            created = true;
            menu.removeAll();

            for(int i=0; i<i18nActions.length; i++) {
                SystemAction action = i18nActions[i];

                if(action == null) {
                    menu.addSeparator ();
                } else if(!isMenu && action instanceof Presenter.Popup) {
                    menu.add(((Presenter.Popup)action).getPopupPresenter());
                } else if(isMenu && action instanceof Presenter.Menu) {
                    menu.add(((Presenter.Menu)action).getMenuPresenter());
                }
            }
        }
    } // End of class Popup.

}

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


import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import org.netbeans.modules.i18n.wizard.I18nWizardAction;
import org.netbeans.modules.i18n.wizard.I18nTestWizardAction;

import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;


/**
 * Action which groups all i18n actions. And that's only purpose of this action.
 *
 * @author  Peter Zavadsky
 */
public class I18nGroupAction extends SystemAction implements Presenter.Menu, Presenter.Popup, Presenter.Toolbar {

    /** Generated serial verision UID. */
    static final long serialVersionUID = 2305127847881996960L;

    /** Array of i18n actions. */
    private static final SystemAction[] i18nActions = new SystemAction[] {
        SystemAction.get(I18nWizardAction.class),
        SystemAction.get(I18nTestWizardAction.class),
        SystemAction.get(I18nAction.class),
        SystemAction.get(InsertI18nStringAction.class)
    };

    /** Icon. */
    private static Icon icon = null;
    
    
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
        return new HelpCtx(I18nGroupAction.class);
    }

    /** Implements <code>Presenter.Menu</code> interface. */
    public JMenuItem getMenuPresenter() {
        JMenu menu = new JMenu(getName());
        
        // Binary-incompatible across SystemAction.icon ImageIcon -> Icon change:
        //menu.setIcon (getIcon ());
        if(icon == null) 
            icon = new ImageIcon(I18nGroupAction.class.getResource(iconResource()));
        
        menu.setIcon(icon);
        
        for(int i= 0; i<i18nActions.length; i++) {
            SystemAction action = i18nActions[i];
            
            if (action == null) {
                menu.addSeparator ();
            } else if (action instanceof Presenter.Menu) {
                menu.add(((Presenter.Menu)action).getMenuPresenter());
            }
        }
        
        return menu;
    }

    /** Implements <code>Presenter.Popup</code> interface. */
    public JMenuItem getPopupPresenter () {
        JMenu menu = new JMenu (getName ());
        for(int i=0; i<i18nActions.length; i++) {
            SystemAction action = i18nActions[i];
            
            if(action == null) {
                menu.addSeparator ();
            } else if(action instanceof Presenter.Popup) {
                menu.add(((Presenter.Popup)action).getPopupPresenter());
            }
        }
        
        return menu;
    }

    /** Implements <code>Presenter.Toolbar</code> interface. */
    public Component getToolbarPresenter() {
        // In jdk1.3 could be used new JToolBar(getName()).
        JToolBar toolbar = new JToolBar ();
        toolbar.setName(getName());
        
        for(int i=0; i<i18nActions.length; i++) {
            SystemAction action = i18nActions[i];
            
            if(action == null) {
                toolbar.addSeparator ();
            } else if(action instanceof Presenter.Toolbar) {
                toolbar.add(((Presenter.Toolbar)action).getToolbarPresenter());
            }
        }
        
        return toolbar;
    }

}

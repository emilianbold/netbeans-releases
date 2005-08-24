/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.ContextAwareAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;

/**
 * Action which just holds a few other SystemAction's for grouping purposes.
 * @author cwebster
 */
public class AddActionGroup extends EJBActionGroup {
    
    public String getName() {
        return NbBundle.getMessage(EJBActionGroup.class, "LBL_AddActionGroup");
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    protected Action[] grouped() {
        return new Action[] {
            new AddBusinessMethodAction(null, NbBundle.getMessage(AddBusinessMethodAction.class, "LBL_BusinessMethodAction")),
            new AddCreateMethodAction(null, NbBundle.getMessage(AddCreateMethodAction.class, "LBL_CreateMethodAction")),
            new AddFinderMethodAction(null, NbBundle.getMessage(AddCreateMethodAction.class, "LBL_FinderMethodAction")),
            new AddHomeMethodAction(null, NbBundle.getMessage(AddHomeMethodAction.class, "LBL_HomeMethodAction")),
            new AddSelectMethodAction(null, NbBundle.getMessage(AddSelectMethodAction.class, "LBL_SelectMethodAction")),
            SystemAction.get(AddCmpFieldAction.class)
        };
    }
    
    public JMenuItem getPopupPresenter() {
        return new EnabledItemsOnlyLazyMenu(actionContext);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(PromoteBusinessMethodAction.class);
    }
    
   private final class EnabledItemsOnlyLazyMenu extends JMenu {
        
        private Lookup l;
        
        public EnabledItemsOnlyLazyMenu(Lookup lookup) {
            super(AddActionGroup.this.getName());
            l = lookup;
        }
        
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                Action[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    Action action = grouped[i];
                    if (action == null) {
                        addSeparator();
                    } else {
                        if (action instanceof ContextAwareAction) {
                            action = ((ContextAwareAction)action).createContextAwareInstance(l);
                        }
                        if (action instanceof Presenter.Popup){
                            //  Issue# 54781 - Contextual menu of individual bean nodes needs adjusting
                            //  * only methods that can be added to a bean should show up in the contextual menu
                             if(action.isEnabled()) 
                                 add(((Presenter.Popup)action).getPopupPresenter());
                        } else {
                            assert false : "Action had no popup presenter: " + action;
                        }
                    }
                }
            }
            return super.getPopupMenu();
        }
    }
}

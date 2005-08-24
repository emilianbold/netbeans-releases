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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import javax.swing.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;


/**
 * Action which just holds a few other SystemAction's for grouping purposes.
 * @author cwebster
 */
public class EnterpriseRefActionGroup extends NodeAction implements Presenter.Popup {
    
    public String getName() {
        return NbBundle.getMessage(EnterpriseRefActionGroup.class, "LBL_EnterpriseActionGroup");
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    private static final SystemAction[] grouped() {
        return new SystemAction[] {
            SystemAction.get(CallEjbAction.class),
            SystemAction.get(UseDatabaseAction.class),
            SystemAction.get(SendJMSMessageAction.class),
//            SystemAction.get(SendEmailAction.class)
        };
    }
    
    public JMenuItem getPopupPresenter() {
        boolean oneNodeSelected = getActivatedNodes().length == 1;
        if (oneNodeSelected && hasEnterpriseRefStrategy()) {
            return new LazyMenu();
        }
        JMenuItem i = super.getPopupPresenter();
        i.setVisible(false);
        return i;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(PromoteBusinessMethodAction.class);
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return true;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
         assert false : "Should never be called: ";
    }
    
    private Project getSelectedProject() {
        Node[] activatedNodes = getActivatedNodes();
        DataObject dobj = (DataObject) 
            activatedNodes[0].getLookup().lookup(DataObject.class);
        
        return dobj == null ? null:FileOwnerQuery.getOwner(dobj.getPrimaryFile());
    }
    
    private boolean hasEnterpriseRefStrategy() {
        Project p = getSelectedProject();
        return p != null &&
            p.getLookup().lookup(EnterpriseReferenceContainer.class) != null;
    }
    
    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        public LazyMenu() {
            super(EnterpriseRefActionGroup.this.getName());
        }
        
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                SystemAction[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    SystemAction action = grouped[i];
                    if (action == null) {
                        addSeparator();
                    } else if (action instanceof Presenter.Popup) {
                        add(((Presenter.Popup)action).getPopupPresenter());
                    } else {
                        assert false : "Action had no popup presenter: " + action;
                    }
                }
            }
            return super.getPopupMenu();
        }
 
    }
    
}

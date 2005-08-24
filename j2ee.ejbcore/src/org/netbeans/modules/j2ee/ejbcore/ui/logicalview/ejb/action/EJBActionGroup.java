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

import javax.swing.*;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 * Action which just holds a few other SystemAction's for grouping purposes.
 * @author cwebster
 */
public class EJBActionGroup extends NodeAction implements Presenter.Popup {
    
    Lookup actionContext;
    
    public String getName() {
        return NbBundle.getMessage(EJBActionGroup.class, "LBL_EJBActionGroup");
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    protected Action[] grouped() {
        return new Action[] {
            SystemAction.get(ExposeInLocalAction.class),
            SystemAction.get(ExposeInRemoteAction.class),
            null,
            new AddBusinessMethodAction(null),
            new AddCreateMethodAction(null),
            new AddFinderMethodAction(null),
            new AddHomeMethodAction(null),
            new AddSelectMethodAction(null),
            SystemAction.get(AddCmpFieldAction.class)
        };
    }
    
    public JMenuItem getPopupPresenter() {
        if (isEjbProject()) {
            return getMenu();
        }
        JMenuItem i = super.getPopupPresenter();
        i.setVisible(false);
        return i;
    }
    
    protected JMenu getMenu() {
        return new LazyMenu(actionContext);
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
    
    public boolean isEjbProject() { 
        Node[] activatedNodes = getActivatedNodes();
        return activatedNodes.length == 1 &&
               isContainingProjectEjb((DataObject)
                activatedNodes[0].getLookup().lookup(DataObject.class));
    }
    
    private static boolean isContainingProjectEjb(DataObject dobj) {
        if (dobj == null) {
            return false;
        }
        Project p = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
	if (p==null) {
            return false;
        }
        return EjbJar.getEjbJars(p).length > 0;
    }
    
    /** Implements <code>ContextAwareAction</code> interface method. */
    public Action createContextAwareInstance(Lookup actionContext) {
        this.actionContext = actionContext;
        return super.createContextAwareInstance(actionContext);
    }
    

    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        private Lookup l;
        
        public LazyMenu(Lookup lookup) {
            super(EJBActionGroup.this.getName());
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
                        if (action instanceof Presenter.Popup) {
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

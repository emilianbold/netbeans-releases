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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import javax.lang.model.element.TypeElement;
import javax.swing.*;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
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
            //TODO: RETOUCHE
//            new AddBusinessMethodAction(),
//            new AddCreateMethodAction(),
//            new AddFinderMethodAction(),
//            new AddHomeMethodAction(),
//            new AddSelectMethodAction(),
            SystemAction.get(AddCmpFieldAction.class)
        };
    }
    
    public JMenuItem getPopupPresenter() {
        if (isEnabled() && isEjbProject()) {
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
        if (activatedNodes.length != 1) {
            return false;
        }
        TypeElement jc = Utils.getJavaClassFromNode(activatedNodes[0]);
        boolean result = false;
        //TODO: RETOUCHE
//        if (jc != null) {
//            EjbMethodController c = EjbMethodController.createFromClass(jc);
//            result = (c != null);
//        }
        return result;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        // do nothing -- should never be called
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
                    if (action == null && getItemCount() != 0) {
                        addSeparator();
                    } else {
                        if (action instanceof ContextAwareAction) {
                            action = ((ContextAwareAction)action).createContextAwareInstance(l);
                        }
                        if (action instanceof Presenter.Popup) {
                            add(((Presenter.Popup)action).getPopupPresenter());
                        }
                    }
                }
            }
            return super.getPopupMenu();
        }
    }
    
}

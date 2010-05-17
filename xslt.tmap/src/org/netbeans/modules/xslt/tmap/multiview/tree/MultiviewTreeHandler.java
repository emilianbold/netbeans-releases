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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.tmap.multiview.tree;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.nodes.LogicalTreeHandler;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 * 
 */
public class MultiviewTreeHandler extends LogicalTreeHandler {

    public MultiviewTreeHandler(ExplorerManager explorerManager,
            TMapModel tModel,
            Lookup contextLookup) 
    {
        super(explorerManager, tModel, contextLookup);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        System.out.println("propertyChange: "+evt);
        
        
        String propertyName = evt.getPropertyName();
        TopComponent curTc = getCurrentTopComponent(); 
        if (curTc == null) {
            return;
        }
//        System.out.println("propertyChange name: "+propertyName);
        
        if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED)) {
            if (TopComponent.getRegistry().getActivated() == curTc) {
//                addUndoManager();
//                triggerValidation();
            }
        }
        else if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED_NODES)) {
           if (TopComponent.getRegistry().getActivated() != curTc) {
               doTreeNodeSelectionByActiveNode();
           }
           return;
            
        } else if (propertyName.equals(ExplorerManager.PROP_SELECTED_NODES)) {
            // NAVIGATOR SELECTED NODES SETTED AS ACTIVE NODES
            //navigatorTopComponent.setActivatedNodes(new Node[] {});
            curTc.setActivatedNodes((Node[])evt.getNewValue());
        } else if (propertyName.equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
            //EVENT FOR PROPERTY PROP_ROOT_CONTEXT
            doTreeNodeSelectionByActiveNode();
        }
//        } else if (propertyName.equals(TopComponent.Registry.PROP_OPENED)) {
////            System.out.println("the set of the opened topComponent were changed");
//            TMapNavigatorController.activateLogicalPanel();
//        } /***/else if (propertyName.equals(TopComponent.Registry.PROP_CURRENT_NODES)) {
////            System.out.println("the set of currently selected nodes were changed");
//            TMapNavigatorController.activateLogicalPanel();
//        }               
        

    }
    
    private TopComponent getCurrentTopComponent() {
        BeanTreeView beanTreeView = getBeanTreeView();
        if (beanTreeView == null) {
            return null;
        }

        TopComponent tc = null;
        Component parent = beanTreeView;
        while ((parent = parent.getParent()) != null) {
            if (parent instanceof TopComponent) {
                tc = (TopComponent) parent;
                break;
            }
        }
        
        return tc;
    }
    
}

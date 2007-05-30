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

package org.netbeans.modules.xslt.tmap.nodes;

import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class LogicalTreeHandler {

    private static final long serialVersionUID = 1L;
    private TMapModel myModel;
    //context lookup
    private Lookup myContextLookup;
    
    private BeanTreeView myBeanTreeView;
    private ExplorerManager myExplorerManager;

    public LogicalTreeHandler(ExplorerManager explorerManager,
            TMapModel bpelModel,
            Lookup contextLookup) 
    {
        myModel = bpelModel;
        myExplorerManager = explorerManager;
//        myExplorerManager.addPropertyChangeListener(this);
        
        myContextLookup = contextLookup;
        
        myBeanTreeView = createBeanTreeView();
        
        //add TopComponent Active Node changes listener :
//        TopComponent.getRegistry().addPropertyChangeListener(this);
//        myModel.addEntityChangeListener((ChangeEventListener)this);
    }

    private BeanTreeView createBeanTreeView() {
        BeanTreeView beanTreeView = new BeanTreeView();
        beanTreeView.setRootVisible(true);
        beanTreeView.setEnabled(true);
        beanTreeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        beanTreeView.setDefaultActionAllowed(true);
//        doTreeNodeSelectionByActiveNode();
        
        return beanTreeView;
    }
    
    public void removeListeners() {
//        if (myExplorerManager != null) {
//            myExplorerManager.removePropertyChangeListener(this);
//        }
//        TopComponent.getRegistry().removePropertyChangeListener(this);
//        if (myModel != null) {
//            myModel.removeEntityChangeListener((ChangeEventListener)this);
//        }
        myModel = null;
        myExplorerManager = null;
        
    }

    public BeanTreeView getBeanTreeView() {
        return myBeanTreeView;
    }
    
}

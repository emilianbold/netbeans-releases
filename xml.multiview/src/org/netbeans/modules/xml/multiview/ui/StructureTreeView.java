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

package org.netbeans.modules.xml.multiview.ui;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.NodeTreeModel;
/**
 * StructureTreeView.java
 * Created on December 4, 2002, 3:17 PM
 *
 * @author  bashby
 */
public class StructureTreeView extends BeanTreeView {
    
    /** Creates a new instance of StructureView */
    public StructureTreeView() {
    }
    
    public StructureTreeView(TreeCellRenderer r,String lineStyle){
        super();
        tree.setCellRenderer(r);
        tree.putClientProperty("JTree.lineStyle", lineStyle); // NOI18N
        tree.setShowsRootHandles(false);
        //expandAll();
   }
    
    public NodeTreeModel getModel(){
        return (NodeTreeModel) tree.getModel();
    }
    
    public JTree getTree(){
        return tree;
    }
    
    public void addNotify() {
        super.addNotify();
        expandAll();
    }
}

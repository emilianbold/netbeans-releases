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

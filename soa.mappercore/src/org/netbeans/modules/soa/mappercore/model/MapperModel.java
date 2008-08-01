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

package org.netbeans.modules.soa.mappercore.model;

import java.awt.datatransfer.Transferable;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author anjeleevich
 */
public interface MapperModel extends TreeModel {
    public TreeModel getLeftTreeModel();
    public TreeSourcePin getTreeSourcePin(TreePath treePath);
    
    public Graph getGraph(TreePath treePath);
    public boolean searchGraphsInside(TreePath treePath);

    public boolean canConnect(TreePath treePath, SourcePin source, TargetPin target,
            TreePath oldTreePath, Link oldLink);
    public void connect(TreePath treePath, SourcePin source, TargetPin target,
            TreePath oldTreePath, Link oldLink);
    
    public GraphSubset getGraphSubset(Transferable transferable);
    
    public boolean canCopy(TreePath treePath, GraphSubset graphSubset);
    public boolean canMove(TreePath treePath, GraphSubset graphSubset);
    
    public GraphSubset copy(TreePath treePath, GraphSubset graphGroup, int x, int y);
    public void move(TreePath treePath, GraphSubset graphGroup, int x, int y);
    public void delete(TreePath currentTreePath, GraphSubset graphGroup);
    public void valueChanged(TreePath treePath, VertexItem vertexItem, 
            Object newValue);
    
    /**
     * Indicates if it is allowed editing the specified vertex item inplace. 
     * @param vItem
     * @return
     */
    public boolean canEditInplace(VertexItem vItem);
}

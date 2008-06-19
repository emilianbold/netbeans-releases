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

package org.netbeans.modules.bpel.mapper.tree.spi;

import java.util.List;

/**
 * The SPI interface for constructing extensions to a tree. 
 * The extension model is intended to provide additional items in the tree. 
 * It doesn't allow to eliminate items, which are provided by main 
 * mapper model or by another extension models.
 * 
 * It is used by the BPEL mapper for building left and right trees.
 * 
 * An external code can provide an instance of such interface 
 * to show required tree.
 * 
 * @author nk160297
 */
public interface MapperTreeExtensionModel<TreeItem> {

    /**
     * Returns a list of children objects of the specified parent. 
     * 
     * It is not guaranteed that repeated call will return the list 
     * of the same objects. An empty list can be returned.
     * 
     * @param dataObjectPath is the list in which the deepest tree item is the first.
     * @return
     */
    List<TreeItem> getChildren(Iterable<Object> dataObjectPathItrb);
    
    /**
     * Returns Boolean.TRUE, Boolean.FALSE or null. 
     * Null means that the model doesn't know if the node is leaf or not. 
     */
    public Boolean isLeaf(Object node);

    /**
     * Indicates if the specified tree item can be used as source or target of a link.
     * Null means that the model doesn't know if the node is connectable or not.
     * @param treeItem
     * @return
     */
    public Boolean isConnectable(Object node);
    
    
    /**
     * The interface TreeItemInfoProvider provides extended functions required 
     * for a tree view. 
     * The info provider is specific for the MapperTreeExtensionModel but 
     * the same info provider can be used by different models. So there is 
     * a kind of aggregation here. 
     * @return
     */
    public TreeItemInfoProvider getTreeItemInfoProvider();
    
//
//  Change Events
//  TODO: implement a bit later    
//

//    /**
//     * Adds a listener for the <code>TreeModelEvent</code>
//     * posted after the tree changes.
//     *
//     * @param   l       the listener to add
//     * @see     #removeTreeModelListener
//     */
//    void addTreeModelListener(TreeModelListener l);
//
//    /**
//     * Removes a listener previously added with
//     * <code>addTreeModelListener</code>.
//     *
//     * @see     #addTreeModelListener
//     * @param   l       the listener to remove
//     */  
//    void removeTreeModelListener(TreeModelListener l);
    
}

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
 * The SPI interface for constructing different hierarchy. 
 * It is used by the BPEL mapper for building left and right trees.
 * 
 * An external code can provide an instance of such interface 
 * to show required tree.
 * 
 * @author nk160297
 */
public interface MapperTreeModel<TreeItem> 
        extends MapperTreeExtensionModel<TreeItem> {

    /** 
     * This object can be used as a root by various trees.
     */
    static final Object TREE_ROOT = new Object() {
        @Override
        public String toString() {
            return "Tree_Root"; // NOI18N
        }
    };
    
    /**
     * Returns the root of the tree.  Returns <code>null</code>
     * only if the tree has no nodes.
     * 
     * It is not guaranteed that repeated call will return the same object. 
     * 
     * @return  the root of the tree
     */
    TreeItem getRoot();

    /**
     * Provides a list of extension models.
     * 
     * @return
     */
    List<MapperTreeExtensionModel> getExtensionModelList();
    
    class Utils {
        
        // Look for extension model by the specified class
        public static <CL extends MapperTreeExtensionModel> CL findExtensionModel(
                MapperTreeModel treeModel, Class<CL> modelClass) {
            List<MapperTreeExtensionModel> extTreeModelList = 
                    treeModel.getExtensionModelList();
            for (MapperTreeExtensionModel extTreeModel : extTreeModelList) {
                if (modelClass.isInstance(extTreeModel)) {
                    return modelClass.cast(extTreeModel);
                }
            }
            //
            return null;
        }
    }
    
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

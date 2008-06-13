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

package org.netbeans.modules.soa.ui.tree;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
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
public interface SoaTreeModel extends SoaTreeExtensionModel {

    /** 
     * This object can be used as a root by various trees.
     */
    String TREE_ROOT = "Tree_Root"; // NOI18N

    /**
     * Returns the root data object of the tree.  Returns <code>null</code>
     * only if the tree has no nodes.
     * 
     * It is not guaranteed that repeated call will return the same object. 
     * 
     * @return  the root of the tree
     */
    Object getRoot();

    /**
     * Provides a list of extension models.
     * This method make sense for compound models only. 
     * It a model doesn't have any submodels it can return null here.
     * 
     * @return
     */
    List<SoaTreeExtensionModel> getExtensionModelList();
    
    public class MyUtils {
        
        // Look for extension model by the specified class
        public static <CL extends SoaTreeExtensionModel> CL findExtensionModel(
                SoaTreeModel treeModel, Class<CL> modelClass) {
            List<SoaTreeExtensionModel> extTreeModelList = 
                    treeModel.getExtensionModelList();
            if (extTreeModelList != null) {
                for (SoaTreeExtensionModel extTreeModel : extTreeModelList) {
                    if (modelClass.isInstance(extTreeModel)) {
                        return modelClass.cast(extTreeModel);
                    }
                }
            }
            //
            return null;
        }

        public static String toString(TreeItem treeItem) {
            LinkedList<Object> list = new LinkedList<Object>();
            Iterator pathItr = treeItem.iterator();
            while (pathItr.hasNext()) {
                list.addFirst(pathItr.next());
            }
            //
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for (Object obj : list) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append("/"); // NOI18N
                }
                sb.append(obj.toString());
            }
            //
            return sb.toString();
        }
    
        public static <DO> DO findDataObjectInPath(TreeItem treeItem, Class<DO> doClass) {
            Iterator itr = treeItem.iterator();
            while (itr.hasNext()) {
                Object dataObj = itr.next();
                if (doClass.isInstance(dataObj)) {
                    return doClass.cast(dataObj);
                }
            }
            return null;
        }
        
    }
    
}

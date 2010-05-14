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

/**
 * The common interface for objects which perform searching by a tree.
 * @author supernikita
 */
public interface TreeItemFinder<TreeItem> {
    
    /**
     * 
     * @param treeItem
     * @param result
     */
    FindResult process(TreeItem treeItem, FindResult result);
    
    public final class FindResult {
        
        private boolean mIsFit = false;
        private boolean mDrillDeeper = false;
        
        public FindResult (boolean isFit, boolean drillDeeper) {
            mIsFit = isFit;
            mDrillDeeper = drillDeeper;
        }
        
        /**
         * Indicates if the tree item satisfies the search conditions
         */
        public boolean isFit() {
            return mIsFit;
        }

        /**
         * Indicates if it necessary to search diiper than the specified tree item.
         */
        public boolean drillDeeper() {
            return mDrillDeeper;
        }
        
        public void setFit(boolean newValue) {
            mIsFit = newValue;
        }
        
        public void setDrillDeeper(boolean newValue) {
            mDrillDeeper = newValue;
        }
        
        @Override
        public String toString() {
            return "Fit: " + mIsFit + " DrillDipper: " + mDrillDeeper;
        }
    }
    
}
    

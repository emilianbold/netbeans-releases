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

package org.netbeans.modules.bpel.mapper.tree.search;

import java.util.List;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder.FindResult;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;

/**
 * The finder for looking a sequence of nested objects in a tree.
 * 
 * @author nk160297
 */
public class PathFinder implements TreeItemFinder {

    private transient List<Object> mPathStepsList; // Lazy initialized
    private transient int mStepListIndex = 0;
    
    public PathFinder(List<Object> pathStepsList) {
        mPathStepsList = pathStepsList;
    }
    
    public PathFinder(DirectedList<Object> pathStepsDList) {
        List<Object> pathStepsList = pathStepsDList.constructBackwardList();
        mPathStepsList = pathStepsList;
    }

    protected synchronized List<Object> getPathStepsList() {
        return mPathStepsList;
    }

    public FindResult process(Object treeItem, FindResult result) {
        boolean isFound = false;
        boolean drillDeeper = false;
        //
        List<Object> scList = getPathStepsList();
        if (scList != null) {
            if (mStepListIndex < scList.size()) {
                Object sComp = scList.get(mStepListIndex);
                //
                if (treeItem.equals(sComp)) {
                     // found next schema component !!!
                    mStepListIndex++; // switch to the next step
                    isFound = true;
                    //
                    // if it was not the last step, then continue searching
                    drillDeeper = isFound && mStepListIndex < scList.size();
                }
            }
        }
        //
        boolean isFit = isFound && !drillDeeper;
        //
        if (result == null) {
            return new FindResult(isFit, drillDeeper);
        } else {
            result.setFit(isFit);
            result.setDrillDeeper(drillDeeper);
            return result;
        }
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.tree.search;

import java.util.List;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder.FindResult;
import org.netbeans.modules.worklist.editor.mapper.model.PathConverter.DirectedList;

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

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

package org.netbeans.modules.bpel.mapper.predicates.editor;

import org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate.PredicateExprTreeModel;
import org.netbeans.modules.soa.ui.tree.impl.SimpleFinder;

/**
 * Looks for a predicate expression tree item in the predicate's editor 
 * target tree.
 * @author nk160297
 */
public class PredicateFinder extends SimpleFinder {

    public PredicateFinder() {
    }
    
    protected boolean isFit(Object treeItem) {
        if (treeItem == PredicateExprTreeModel.PREDICATE_EXPRESSION) {
             // found!!!
            return true;
        }
        return false;
    }

    protected boolean drillDeeper(Object treeItem) {
        if (treeItem == PredicateExprTreeModel.PREDICATE_EXPRESSION) {
            return false;
        }
        return true;
    }

}

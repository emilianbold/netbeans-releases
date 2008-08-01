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

import org.netbeans.modules.soa.ui.tree.impl.SimpleFinder;
import org.netbeans.modules.bpel.mapper.tree.models.VariableDeclarationWrapper;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;

/**
 *
 * @author nk160297
 */
public class VariableFinder extends SimpleFinder {

    private AbstractVariableDeclaration mVariableDecl;
    
    public VariableFinder(AbstractVariableDeclaration variableDecl) {
        mVariableDecl = variableDecl;
    }
    
    protected boolean isFit(Object treeItem) {
        if (treeItem == mVariableDecl && 
                !(treeItem instanceof VariableDeclarationScope)) {
             // found!!!
            return true;
        }
        if (treeItem instanceof VariableDeclarationWrapper) {
            VariableDeclaration varDeclDelegate = 
                    ((VariableDeclarationWrapper)treeItem).getDelegate();
            if (varDeclDelegate == mVariableDecl) {
                // found!!!
                return true; 
            }
        }
        //
        return false;
    }

    protected boolean drillDeeper(Object treeItem) {
        if (!(treeItem instanceof BpelEntity)) {
            // Stop searching if out of variable tree.
            return false;
        }
        if (treeItem instanceof Variable) {
            // Stop searching if the tree item is a pure variable.
            return false;
        }
        if (treeItem instanceof VariableDeclarationWrapper) {
            // Stop searching if the tree item is a variable wrapper.
            return false;
        }
        return true;
    }

}

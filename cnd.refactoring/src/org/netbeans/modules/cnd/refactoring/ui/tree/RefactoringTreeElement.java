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

package org.netbeans.modules.cnd.refactoring.ui.tree;

import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;

/**
 * presentation of a leaf for refactoring element
 * @author Vladimir Voskresensky
 */
public class RefactoringTreeElement implements TreeElement { 
    
    private final RefactoringElement refactoringElement;
    private final CsmOffsetable thisObject;
    private CsmScope parent;
    private boolean initedParent = false;
    
    RefactoringTreeElement(RefactoringElement element) {
        this.refactoringElement = element;
        this.thisObject = element.getLookup().lookup(CsmOffsetable.class);
    }
    
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            return TreeElementFactory.getTreeElement(getCsmParent());
        } else {
            return TreeElementFactory.getTreeElement(thisObject.getContainingFile());
        }
    }
    
    public Icon getIcon() {
        return null;   
    }

    public String getText(boolean isLogical) {
        return refactoringElement.getDisplayText();
    }

    public Object getUserObject() {
        return refactoringElement;
    }
    
    private CsmObject getCsmParent() {
        if (!initedParent) {
            initedParent = true;
            parent = CsmRefactoringUtils.getEnclosingScopeElement((CsmObject)thisObject);
        }
        return parent;
    }

    private CsmObject getFeature(CsmObject lookupObject) {
        if (CsmKindUtilities.isInclude(lookupObject)) {
            return lookupObject;
        } else if (lookupObject instanceof CsmReference) {
            CsmObject referenced = ((CsmReference)lookupObject).getReferencedObject();
            return referenced;
        } else {
            System.err.println("getFeature for " + lookupObject);
            return lookupObject;
        }
    }
}

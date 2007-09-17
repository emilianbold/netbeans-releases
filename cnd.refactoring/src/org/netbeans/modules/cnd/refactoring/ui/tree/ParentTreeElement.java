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

package org.netbeans.modules.cnd.refactoring.ui.tree;

import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;

/**
 * 
 * @author Vladimir Voskresensky
 */
public class ParentTreeElement implements TreeElement {
    
    private final CsmUID<CsmObject> element;
    private final Icon icon;
    private final String text;
    /** Creates a new instance of ParentTreeElement */
    public ParentTreeElement(CsmObject element) {
        this.element = CsmRefactoringUtils.getHandler(element);
        this.icon = CsmImageLoader.getIcon(element);
        this.text = CsmRefactoringUtils.getHtml(element);
    }

    public TreeElement getParent(boolean isLogical) {
        CsmObject enclosing = getParent();
        if (enclosing != null) {
            return TreeElementFactory.getTreeElement(enclosing);
        } else {
            System.err.println("element without parent " + getUserObject());
            return null;
        }
    }

    public Icon getIcon() {
        return icon;
    }

    public String getText(boolean isLogical) {
        return text;
    }

    public Object getUserObject() {
        return element.getObject();
    }
    
    private CsmObject getParent() {
        CsmObject obj = (CsmObject) getUserObject();
        CsmObject enclosing = null;
        if (obj != null) {
            enclosing = CsmRefactoringUtils.getEnclosingElement(obj);
        }
        return enclosing;
    }

}

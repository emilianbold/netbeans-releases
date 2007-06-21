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

package org.netbeans.modules.refactoring.java.ui.tree;

import com.sun.source.tree.Tree;
import javax.swing.Icon;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.ui.UIUtilities;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class RefactoringTreeElement implements TreeElement { 
    
    private RefactoringElement refactoringElement;
    private ElementGrip thisFeature;
    private ElementGrip parent;
    
    RefactoringTreeElement(RefactoringElement element) {
        this.refactoringElement = element;
        thisFeature = getFeature(((ElementGrip) element.getLookup().lookup(ElementGrip.class)));
        parent =  thisFeature;
        if (parent == null) {
            parent = thisFeature;
        }
    }
    
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            return TreeElementFactory.getTreeElement(parent);
        } else {
            return TreeElementFactory.getTreeElement(refactoringElement.getParentFile());
        }
    }
    
    private ElementGrip getFeature(ElementGrip el) {
        if (el.getKind() == Tree.Kind.VARIABLE) {
          return el.getParent();  
        }
        return el;
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
}

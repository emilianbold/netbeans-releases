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

package org.netbeans.modules.refactoring.plugins;

import javax.swing.Icon;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;

/**
 *
 * @author Jan Becicka
 */
public class RefactoringTreeElement implements TreeElement {

    RefactoringElement element;

    RefactoringTreeElement(RefactoringElement element) {
        this.element = element;
    }

    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            Object composite = element.getLookup().lookup(Object.class);
            if (composite!=null) {
                return TreeElementFactory.getTreeElement(composite);
            }
        }
        return TreeElementFactory.getTreeElement(element.getParentFile());
    }
    
    public Icon getIcon() {
        return null;
    }

    public String getText(boolean isLogical) {
        return element.getDisplayText();
    }

    public Object getUserObject() {
        return element;
    }
}

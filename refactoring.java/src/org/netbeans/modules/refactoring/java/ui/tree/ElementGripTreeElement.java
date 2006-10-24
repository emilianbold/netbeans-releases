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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.Icon;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class ElementGripTreeElement implements TreeElement {
    
    private ElementGrip element;
    /** Creates a new instance of JavaTreeElement */
    public ElementGripTreeElement(ElementGrip element) {
        this.element = element;
    }

    public TreeElement getParent(boolean isLogical) {
        ElementGrip enclosing = (ElementGrip) element.getParent();
        if (isLogical) {
            if (enclosing == null) {
                return TreeElementFactory.getTreeElement(element.getFileObject().getParent());
            }
            return TreeElementFactory.getTreeElement(enclosing);
        } else {
            return TreeElementFactory.getTreeElement(element.getFileObject());
        }
    }

    public Icon getIcon() {
        return element.getIcon();
    }

    public String getText(boolean isLogical) {
        return RetoucheUtils.htmlize(element.toString());
    }

    public Object getUserObject() {
        return element;
    }
}

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
package org.netbeans.modules.visualweb.insync.faces.refactoring;

import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;

public abstract class MarkupRefactoringElement extends InSyncRefactoringElement {
    protected MarkupUnit markupUnit;

    public MarkupRefactoringElement(FileObject fileObject, MarkupUnit markupUnit, String oldName, String newName) {
        super(fileObject, markupUnit == null?null:markupUnit.getDataObject(), oldName, newName);
        this.markupUnit = markupUnit;
    }

    /**
     * Return the Element to be used as the indication of the position to use for selecting this refactoging element
     * in a document.
     *
     * @return
     */
    protected abstract Element getPositionElement();

    protected int getPositionOffset() {
        // I had to add the .getDocument() call to look for null, as it seems that the markup unit could be destroyed
        // when the refactoring list is repainted
        if (markupUnit == null || markupUnit.getSourceDom() == null)
            return -1;
        Element element = getPositionElement();
        if (element == null)
            return -1;
        int result = markupUnit.getOffset(element);
        return result;
    }

    protected boolean isExternalChange() {
        return false;
    }

}

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

import java.text.MessageFormat;

import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;

import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupVisitor;

public class RenameResourceReferencesRefactoringElement extends RenameElExpressionReferencesRefactoringElement {

    public RenameResourceReferencesRefactoringElement(FileObject fileObject, MarkupUnit markupUnit, String oldName, String newName) {
        super(fileObject, markupUnit, oldName, newName);
    }

    public void doChange(Document document) {
        MarkupVisitor v = new ResourceReferenceUpdater(oldName, newName);
        v.apply(document);
    }

    protected void initDisplayText() {
        displayText = MessageFormat.format(FacesRenameClassRefactoringPlugin.getString("LBL_RenameResourceReferences"), new Object[] {oldName, newName}); // NOI18N
    }

}

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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import javax.lang.model.element.*;

import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Sandip Chitale
 */
final class StringLiteralTransformer extends SearchVisitor {
    
    enum MatchKind {
        EXACT,
        PREFIX,
        SUFFIX,
    }
    
    private final String oldString;
    private final String newString;
    private final MatchKind matchKind;
    
    public StringLiteralTransformer(WorkingCopy workingCopy, String oldString, String newString, MatchKind matchKind) {
        super(workingCopy);
        this.oldString = oldString;
        this.newString = newString;
        this.matchKind = matchKind;
    }

    @Override
    public Tree visitLiteral(LiteralTree literalTree, Element element) {
        // Is this a string literal
        if (literalTree.getKind() == Tree.Kind.STRING_LITERAL) {
            // Replace oldString with newString
            transformStringLiteral(getCurrentPath(), literalTree);
        }
        return super.visitLiteral(literalTree, element);
    }
    
    private void transformStringLiteral(TreePath literalTreePath, LiteralTree literalTree) {
        if (workingCopy.getTreeUtilities().isSynthetic(literalTreePath)) {
            return;
        }
        Object valueObject = literalTree.getValue();
        if (valueObject instanceof String) {
            String value = (String) valueObject;
            switch (matchKind) {
            case EXACT:
                if (value.equals(oldString)) {
                    Tree newLiteralTree = make.Literal(newString);
                    workingCopy.rewrite(literalTree, newLiteralTree);
                }
                break;
            case PREFIX:
                if (value.startsWith(oldString)) {
                    Tree newLiteralTree = make.Literal(newString + value.substring(oldString.length()));
                    workingCopy.rewrite(literalTree, newLiteralTree);
                }
                break;
            case SUFFIX:
                if (value.endsWith(oldString)) {
                    Tree newLiteralTree = make.Literal(value.substring(0, value.length() - oldString.length()) + newString);
                    workingCopy.rewrite(literalTree, newLiteralTree);
                }
                break;
            }
        }
    }
}

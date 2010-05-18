/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

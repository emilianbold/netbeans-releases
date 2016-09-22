/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.AST;

public class ASTIterator {
    protected AST cursor = null;
    protected AST original = null;


    public ASTIterator(AST t) {
        original = cursor = t;
    }

    /** Is 'sub' a subtree of 't' beginning at the root? */
    public boolean isSubtree(AST t, AST sub) {
        AST sibling;

        // the empty tree is always a subset of any tree.
        if (sub == null) {
            return true;
        }

        // if the tree is empty, return true if the subtree template is too.
        if (t == null) {
            if (sub != null) return false;
            return true;
        }

        // Otherwise, start walking sibling lists.  First mismatch, return false.
        for (sibling = t;
             sibling != null && sub != null;
             sibling = sibling.getNextSibling(), sub = sub.getNextSibling()) {
            // as a quick optimization, check roots first.
            if (sibling.getType() != sub.getType()) return false;
            // if roots match, do full match test on children.
            if (sibling.getFirstChild() != null) {
                if (!isSubtree(sibling.getFirstChild(), sub.getFirstChild())) return false;
            }
        }
        return true;
    }

    /** Find the next subtree with structure and token types equal to
     * those of 'template'.
     */
    public AST next(AST template) {
        AST t = null;
        AST sibling = null;

        if (cursor == null) {	// do nothing if no tree to work on
            return null;
        }

        // Start walking sibling list looking for subtree matches.
        for (; cursor != null; cursor = cursor.getNextSibling()) {
            // as a quick optimization, check roots first.
            if (cursor.getType() == template.getType()) {
                // if roots match, do full match test on children.
                if (cursor.getFirstChild() != null) {
                    if (isSubtree(cursor.getFirstChild(), template.getFirstChild())) {
                        return cursor;
                    }
                }
            }
        }
        return t;
    }
}

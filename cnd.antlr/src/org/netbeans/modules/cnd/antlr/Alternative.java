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

import java.util.Hashtable;

/** Intermediate data class holds information about an alternative */
class Alternative {
    // Tracking alternative linked list
    AlternativeElement head;   // head of alt element list
    AlternativeElement tail;  // last element added

    // Syntactic predicate block if non-null
    protected SynPredBlock synPred;
    // Semantic predicate action if non-null
    protected String semPred;
    // Exception specification if non-null
    protected ExceptionSpec exceptionSpec;
    // Init action if non-null;
    protected Lookahead[] cache;	// lookahead for alt.  Filled in by
    // deterministic() only!!!!!!!  Used for
    // code gen after calls to deterministic()
    // and used by deterministic for (...)*, (..)+,
    // and (..)? blocks.  1..k
    protected int lookaheadDepth;	// each alt has different look depth possibly.
    // depth can be NONDETERMINISTIC too.
    // 0..n-1
// If non-null, Tree specification ala -> A B C (not implemented)
    protected Token treeSpecifier = null;
    // True of AST generation is on for this alt
    private boolean doAutoGen;


    public Alternative() {
    }

    public Alternative(AlternativeElement firstElement) {
        addElement(firstElement);
    }

    public void addElement(AlternativeElement e) {
        // Link the element into the list
        if (head == null) {
            head = tail = e;
        }
        else {
            tail.next = e;
            tail = e;
        }
    }

    public boolean atStart() {
        return head == null;
    }

    public boolean getAutoGen() {
        // Don't build an AST if there is a tree-rewrite-specifier
        return doAutoGen && treeSpecifier == null;
    }

    public Token getTreeSpecifier() {
        return treeSpecifier;
    }

    public void setAutoGen(boolean doAutoGen_) {
        doAutoGen = doAutoGen_;
    }
}

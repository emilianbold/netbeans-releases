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

/**A GrammarAnalyzer computes lookahead from Grammar (which contains
 * a grammar symbol table) and can then answer questions about the
 * grammar.
 *
 * To access the RuleBlock for a rule name, the grammar symbol table
 * is consulted.
 *
 * There should be no distinction between static & dynamic analysis.
 * In other words, some of the easy analysis can be done statically
 * and then the part that is hard statically can be deferred to
 * parse-time.  Interestingly, computing LL(k) for k>1 lookahead
 * statically is O(|T|^k) where T is the grammar vocabulary, but,
 * is O(k) at run-time (ignoring the large constant associated with
 * the size of the grammar).  In English, the difference can be
 * described as "find the set of all possible k-sequences of input"
 * versus "does this specific k-sequence match?".
 */
public interface GrammarAnalyzer {
    /**The epsilon token type is an imaginary type used
     * during analysis.  It indicates an incomplete look() computation.
     * Must be kept consistent with Token constants to be between
     * MIN_USER_TYPE and INVALID_TYPE.
     */
    // public static final int EPSILON_TYPE = 2;
    public static final int NONDETERMINISTIC = Integer.MAX_VALUE; // lookahead depth
    public static final int LOOKAHEAD_DEPTH_INIT = -1;
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.paralleladviser.codemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 * Code model utility functions for Parallel Adviser.
 *
 * @author Nick Krasilnikov
 */
public class CodeModelUtils {

    private CodeModelUtils() {
    }

    public static CsmFunction getFunction(CsmProject project, String functionQuilifiedName) {
        if(project == null) {
            return null;
        }
        functionQuilifiedName = functionQuilifiedName.indexOf("(") != -1 ? functionQuilifiedName.substring(0, functionQuilifiedName.indexOf("(")) : functionQuilifiedName; // NOI18N
        Iterator<CsmFunction> iter = CsmSelect.getFunctions(project, functionQuilifiedName);
        CsmFunction declaration = null;
        while (iter.hasNext()) {
            CsmFunction function = iter.next();
            if (CsmKindUtilities.isFunctionDefinition(function)) {
                return function;
            } else { // declaration
                CsmFunctionDefinition definition = function.getDefinition();
                if (definition != null) {
                    return definition;
                } else {
                    declaration = function;
                }
            }
        }
        return declaration;
    }

    public static Collection<CsmLoopStatement> getForStatements(CsmFunction function) {
        if(!CsmKindUtilities.isFunctionDefinition(function)) {
            return Collections.<CsmLoopStatement>emptyList();
        }
        CsmFunctionDefinition funDef = (CsmFunctionDefinition)function;

        CsmCompoundStatement body = funDef.getBody();
        return getForStatements(body);
    }

    public static Collection<CsmLoopStatement> getForStatements(CsmStatement statement) {
        Collection<CsmLoopStatement> loops = new ArrayList<CsmLoopStatement>();
        if (statement != null) {
            switch (statement.getKind()) {
                case COMPOUND:
                    for (CsmStatement stmt : ((CsmCompoundStatement) statement).getStatements()) {
                        loops.addAll(getForStatements(stmt));
                    }
                    break;
                case FOR:
                    loops.addAll(getForStatements(((CsmLoopStatement)statement).getBody()));
                    loops.add((CsmLoopStatement)statement);
                    break;
                case IF:
                    loops.addAll(getForStatements(((CsmIfStatement) statement).getThen()));
                    loops.addAll(getForStatements(((CsmIfStatement) statement).getElse()));
                    break;
                case SWITCH:
                    loops.addAll(getForStatements(((CsmSwitchStatement) statement).getBody()));
                    break;
                case EXPRESSION:
//                    ((CsmExpressionStatement) statement).getExpression().getText();
//                    break;
                case WHILE:
                case DO_WHILE:
                case GOTO:
                case LABEL:
                case CASE:
                case BREAK:
                case DEFAULT:
                case CONTINUE:
                case RETURN:
                case DECLARATION:
                case TRY_CATCH:
                case CATCH:
                case THROW:
            }
        }
        return loops;
    }

    public static boolean canParallelize(CsmLoopStatement loop) {
        if (loop instanceof CsmForStatement) {
            CsmStatement init = ((CsmForStatement)loop).getInitStatement();
            Collection<CsmDeclaration> innerDeclarations = new ArrayList<CsmDeclaration>();
            if(init.getKind() == CsmStatement.Kind.DECLARATION) {
                innerDeclarations.addAll(((CsmDeclarationStatement)init).getDeclarators());
            }
            return canParallelize(loop.getBody(), innerDeclarations);
        }
        return false;
    }

    public static boolean canParallelize(CsmStatement statement, Collection<CsmDeclaration> innerDeclarations) {
        if (statement != null) {
            switch (statement.getKind()) {
                case COMPOUND:
                    for (CsmStatement stmt : ((CsmCompoundStatement) statement).getStatements()) {
                        if(!canParallelize(stmt, innerDeclarations)) {
                            return false;
                        }
                    }
                    break;
                case IF:
                    if (!canParallelize(((CsmIfStatement) statement).getThen(), innerDeclarations)) {
                        return false;
                    }
                    if (!canParallelize(((CsmIfStatement) statement).getElse(), innerDeclarations)) {
                        return false;
                    }
                    break;
                case EXPRESSION:
                    StringBuilder innerDeclRegexp = new StringBuilder(""); // NOI18N
                    boolean first = true;
                    for (CsmDeclaration csmDeclaration : innerDeclarations) {
                        if(!first) {
                            innerDeclRegexp.append("|"); // NOI18N
                        }
                        innerDeclRegexp.append(csmDeclaration.getName());
                        first = false;
                    }
                    String notAssignOperationsRegexp = "\\+|-|\\*|/|%|^|&|\\||~|!|<<|>>|==|!=|<|>|<=|>=|\\+\\+|--"; // NOI18N
                    String constsRegexp = "true|false|(\\d)*.(\\d)*|(\\d)*|\"([^\"]|\\\")*\"|\'([^\']|\\\')*\'"; // NOI18N
                    String expression = statement.getContainingFile().getText(statement.getStartOffset(), statement.getEndOffset()).toString();

                    StringBuilder safeExpressionRegexp = new StringBuilder(""); // NOI18N
                    safeExpressionRegexp.append("(\\w("); // NOI18N
                    safeExpressionRegexp.append(notAssignOperationsRegexp); // NOI18N
                    safeExpressionRegexp.append("|\\s|\\(|\\))*=(\\s)*)*(("); // NOI18N
                    safeExpressionRegexp.append(innerDeclRegexp); // NOI18N
                    safeExpressionRegexp.append(")|("); // NOI18N
                    safeExpressionRegexp.append(notAssignOperationsRegexp); // NOI18N
                    safeExpressionRegexp.append("|\\(|\\))|("); // NOI18N
                    safeExpressionRegexp.append(constsRegexp); // NOI18N
                    safeExpressionRegexp.append(")|(\\s)*)*"); // NOI18N

                    if(!expression.matches(safeExpressionRegexp.toString())) {
                        return false;
                    }
                    break;
                case DECLARATION:
                    innerDeclarations.addAll(((CsmDeclarationStatement)statement).getDeclarators());
                    break;
                case FOR:
                case WHILE:
                case DO_WHILE:
                case GOTO:
                case LABEL:
                case SWITCH:
                case CASE:
                case BREAK:
                case DEFAULT:
                case CONTINUE:
                case RETURN:
                case TRY_CATCH:
                case CATCH:
                case THROW:
                    return false;
            }
        }
        return true;
    }
}

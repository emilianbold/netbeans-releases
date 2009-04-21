/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.completion.csm;

import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExceptionHandler;
import org.netbeans.modules.cnd.api.model.deep.CsmForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmTryCatchStatement;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 * utility class
 * used to find innermost statement inside CsmDeclaration and it's
 * context chain
 * @author vv159170
 */
public class CsmStatementResolver {

    /** Creates a new instance of CsmStatementResolver */
    private CsmStatementResolver() {
    }

    /*
     * finds inner object for given offset and update context
     */
    public static boolean findInnerObject(CsmStatement stmt, int offset, CsmContext context) {
        if( stmt == null ) {
            print("STATEMENT is null"); //NOI18N
            return false;
        }
        if (!CsmOffsetUtilities.isInObject(stmt, offset)) {
            print("Offset " + offset+ " is not in statement " + stmt); //NOI18N
            return false;
        }
        // update context of passed statements
        CsmContextUtilities.updateContext(stmt, offset, context);

        CsmStatement.Kind kind = stmt.getKind();
        boolean found = true;
        switch (kind) {
            case COMPOUND:
                found = findInner((CsmCompoundStatement) stmt, offset, context);
                break;
            case IF:
                found = findInner((CsmIfStatement) stmt, offset, context);
                break;
            case TRY_CATCH:
                found = findInner((CsmTryCatchStatement) stmt, offset, context);
                break;
            case CATCH:
                found = findInner((CsmExceptionHandler) stmt, offset, context);
                break;
            case DECLARATION:
                found = findInner((CsmDeclarationStatement) stmt, offset, context);
                break;
            case WHILE:
            case DO_WHILE:
                found = findInner((CsmLoopStatement) stmt, offset, context);
                break;
            case FOR:
                found = findInner((CsmForStatement) stmt, offset, context);
                break;
            case SWITCH:
                found = findInner((CsmSwitchStatement) stmt, offset, context);
                break;
            case BREAK:
            case CASE:
            case CONTINUE:
            case DEFAULT:
            case EXPRESSION:
            case GOTO:
            case LABEL:
            case RETURN:
                break;
            default:
                print("unexpected statement kind"); //NOI18N
                break;
        }
        return found;
    }

    private static boolean findInner(CsmCompoundStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (stmt != null) {
            for (CsmStatement curSt : stmt.getStatements()) {
                // We should stop in case of macro, so we check sameOffsets for statements.
                // But there could be function_try_block, that we should not skip, in spite of
                // equality of offsets.
                if ((!CsmOffsetUtilities.sameOffsets(stmt, curSt) ||
                        (CsmKindUtilities.isCompoundStatement(stmt) &&
                        CsmKindUtilities.isTryCatchStatement(curSt))) &&
                        findInnerObject(curSt, offset, context)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean findInner(CsmTryCatchStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called";
        if (findInnerObject(stmt.getTryStatement(), offset, context)) {
            return true;
        }
        for (CsmExceptionHandler handler : stmt.getHandlers()) {
            if (findInnerObject(handler, offset, context)) {
                return true;
            }
        }
        return false;
    }

    private static boolean findInner(CsmExceptionHandler stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called";
        return findInner((CsmCompoundStatement) stmt, offset, context);
    }

    private static boolean findInner(CsmIfStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called";

        if (!CsmOffsetUtilities.sameOffsets(stmt, stmt.getCondition())
                && CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            print("in CONDITION of if statement "); //NOI18N
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            return true;
        }
        if (findInnerObject(stmt.getThen(), offset, context)) {
            print("in THEN: "); //NOI18N
            return true;
        }
        if (findInnerObject(stmt.getElse(), offset, context)) {
            print("in ELSE: "); //NOI18N
            return true;
        }
        return false;
    }

    private static boolean findInner(CsmDeclarationStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in declaration statement when called"; //NOI18N
        List<CsmDeclaration> decls = stmt.getDeclarators();
        CsmDeclaration decl = CsmOffsetUtilities.findObject(decls, context, offset);
        if (decl != null && (decls.size() == 1 || !CsmOffsetUtilities.sameOffsets(stmt, decl))) {
            print("we have declarator " + decl); //NOI18N
            if (CsmKindUtilities.isTypedef(decl)) {
                CsmClassifier classifier = ((CsmTypedef)decl).getType().getClassifier();
                if (CsmOffsetUtilities.isInObject(decl, classifier) && !CsmOffsetUtilities.sameOffsets(decl, classifier)) {
                    decl = classifier;
                }
            }
            if (CsmKindUtilities.isEnum(decl)) {
                findInner((CsmEnum)decl, offset, context);
            } else if (CsmKindUtilities.isClass(decl)) {
                findInner((CsmClass)decl, offset, context);
            } else  if (CsmKindUtilities.isFunction(decl)) {
                CsmFunction fun = (CsmFunction) decl;

                // check if offset in parameters
                CsmFunctionParameterList paramList = fun.getParameterList();
//                if (paramList != null) {
                    CsmParameter param = CsmOffsetUtilities.findObject(paramList.getParameters(), context, offset);
                    if (CsmOffsetUtilities.isInObject(paramList, offset) || (param != null && !CsmOffsetUtilities.sameOffsets(fun, param))) {
                        context.add(fun);
                        if (param != null) {
                            CsmType type = param.getType();
                            if (!CsmOffsetUtilities.sameOffsets(param, type)
                                    && CsmOffsetUtilities.isInObject(type, offset)) {
                                context.setLastObject(type);
                            } else {
                                context.setLastObject(param);
                            }
                        }
                    }
//                }
            }
            return true;
        }
        return false;
    }

    private static boolean findInner(CsmEnum enumm, int offset, CsmContext context) {
        CsmContextUtilities.updateContext(enumm, offset, context);
        CsmEnumerator enumerator = CsmOffsetUtilities.findObject(enumm.getEnumerators(), context, offset);
        if (enumerator != null && !CsmOffsetUtilities.sameOffsets(enumm, enumerator)) {
            CsmContextUtilities.updateContext(enumerator, offset, context);
        }
        return true;
    }

    private static boolean findInner(CsmClass clazz, int offset, CsmContext context) {
        CsmContextUtilities.updateContext(clazz, offset, context);
        CsmMember member = CsmOffsetUtilities.findObject(clazz.getMembers(), context, offset);
        if (!CsmOffsetUtilities.sameOffsets(clazz, member)) {
            if (CsmKindUtilities.isClass(member)) {
                findInner((CsmClass)member, offset, context);
            } else if (CsmKindUtilities.isFunctionDefinition(member)) {
                CsmContextUtilities.updateContext(member, offset, context);
                CsmCompoundStatement body = ((CsmFunctionDefinition)member).getBody();
                if (!CsmOffsetUtilities.sameOffsets(member, body)) {
                    findInnerObject(body, offset, context);
                }
            }
        }
        return true;
    }

    private static boolean findInner(CsmLoopStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (!CsmOffsetUtilities.sameOffsets(stmt, stmt.getCondition())
                && CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            print("in condition of loop statement isPostCheck()=" + stmt.isPostCheck()); //NOI18N
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            return true;
        }
        return findInnerObject(stmt.getBody(), offset, context);
    }

    private static boolean findInner(CsmForStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (findInnerObject(stmt.getInitStatement(), offset, context)) {
            print("in INIT of for statement"); //NOI18N
            return true;
        }
        if (CsmOffsetUtilities.isInObject(stmt.getIterationExpression(), offset)) {
            print("in ITERATION  of for statement"); //NOI18N
            CsmContextUtilities.updateContextObject(stmt.getIterationExpression(), offset, context);
            return true;
        }
        if (CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            print("in CONDITION of for statement "); //NOI18N
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            return true;
        }
        return findInnerObject(stmt.getBody(), offset, context);
    }

    private static boolean findInner(CsmSwitchStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (!CsmOffsetUtilities.sameOffsets(stmt, stmt.getCondition())
                && CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            return true;
        }
        return findInnerObject(stmt.getBody(), offset, context);
    }

    private static void print(String s) {
        if (CsmUtilities.DEBUG) { System.out.println(s); }
    }

}

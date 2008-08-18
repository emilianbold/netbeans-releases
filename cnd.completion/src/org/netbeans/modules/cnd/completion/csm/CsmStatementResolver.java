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
import java.util.Iterator;
import java.util.List;
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
            if (CsmUtilities.DEBUG) print("STATEMENT is null"); //NOI18N
            return false;
        } 
        if (!CsmOffsetUtilities.isInObject(stmt, offset)) {
            if (CsmUtilities.DEBUG) print("Offset " + offset+ " is not in statement " + stmt); //NOI18N
            return false;
        }
        // update context of passed statements
        CsmContextUtilities.updateContext(stmt, offset, context);
        
        CsmStatement.Kind kind = stmt.getKind();
        boolean found = true;
        if( kind == CsmStatement.Kind.COMPOUND ) {
            found = findInner((CsmCompoundStatement) stmt, offset, context);
        }
        else if( kind == CsmStatement.Kind.IF ) {
            found = findInner((CsmIfStatement) stmt, offset, context);
        }
        else if( kind == CsmStatement.Kind.TRY_CATCH ) {
            found = findInner((CsmTryCatchStatement) stmt, offset, context);
        }
        else if( kind == CsmStatement.Kind.CATCH ) {
            found = findInner((CsmExceptionHandler) stmt, offset, context);
        }
        else if( kind == CsmStatement.Kind.DECLARATION ) {
            found = findInner((CsmDeclarationStatement) stmt, offset, context);
        }
        else if( kind == CsmStatement.Kind.WHILE || kind == CsmStatement.Kind.DO_WHILE ) {
            found = findInner((CsmLoopStatement) stmt, offset, context);
        }
        else if( kind == CsmStatement.Kind.FOR ) {
            found = findInner((CsmForStatement) stmt, offset, context);
        }
        else if( kind == CsmStatement.Kind.SWITCH ) {
            found = findInner((CsmSwitchStatement) stmt, offset, context);
        }
        else if( kind == CsmStatement.Kind.BREAK ) {
        }
        else if( kind == CsmStatement.Kind.CASE ) {
        }
        else if( kind == CsmStatement.Kind.CONTINUE ) {
        }
        else if( kind == CsmStatement.Kind.DEFAULT ) {
        }
        else if( kind == CsmStatement.Kind.EXPRESSION ) {
        }
        else if( kind == CsmStatement.Kind.GOTO ) {
        }
        else if( kind == CsmStatement.Kind.LABEL ) {
        }
        else if( kind == CsmStatement.Kind.RETURN ) {
        }
        else  {
            if (CsmUtilities.DEBUG) print("unexpected statement kind"); //NOI18N
        }
        return true;
    }
    
    private static boolean findInner(CsmCompoundStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if( stmt != null ) {
            for( Iterator iter = stmt.getStatements().iterator(); iter.hasNext(); ) {
                CsmStatement curSt = (CsmStatement) iter.next();
                if (findInnerObject(curSt, offset, context)) {
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
        for( Iterator iter = stmt.getHandlers().iterator(); iter.hasNext(); ) {
            if (findInnerObject((CsmStatement) iter.next(), offset, context)) {
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

        if (CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            if (CsmUtilities.DEBUG) print("in CONDITION of if statement "); //NOI18N
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            return true;
        }
        if (findInnerObject(stmt.getThen(), offset, context)) {
            if (CsmUtilities.DEBUG) print("in THEN: "); //NOI18N
            return true;
        }
        if (findInnerObject(stmt.getElse(), offset, context)) {
            if (CsmUtilities.DEBUG) print("in ELSE: ");     //NOI18N        
            return true;
        }
        return false;
    }
    
    private static boolean findInner(CsmDeclarationStatement stmt, int offset, CsmContext context) {
        // XXX: search deeper?
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in declaration statement when called"; //NOI18N
        List<CsmDeclaration> decls = stmt.getDeclarators();
        CsmDeclaration declObject = CsmOffsetUtilities.findObject(decls, context, offset);
        if (declObject != null) {
            if (CsmUtilities.DEBUG) print("we have declarator " + declObject); //NOI18N
            return true;
        }
        return false;
    }
    
    private static boolean findInner(CsmLoopStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            if (CsmUtilities.DEBUG) print("in condition of loop statement isPostCheck()=" + stmt.isPostCheck()); //NOI18N
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            return true;
        }
        return findInnerObject(stmt.getBody(), offset, context);
    }
    
    private static boolean findInner(CsmForStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (findInnerObject(stmt.getInitStatement(), offset, context)) {
            if (CsmUtilities.DEBUG) print("in INIT of for statement"); //NOI18N
            return true;
        }
        if (CsmOffsetUtilities.isInObject(stmt.getIterationExpression(), offset)) {
            if (CsmUtilities.DEBUG) print("in ITERATION  of for statement"); //NOI18N
            CsmContextUtilities.updateContextObject(stmt.getIterationExpression(), offset, context);
            return true;
        }
        if (CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            if (CsmUtilities.DEBUG) print("in CONDITION of for statement "); //NOI18N
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            return true;
        }        
        return findInnerObject(stmt.getBody(), offset, context);
    }
    
    private static boolean findInner(CsmSwitchStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            return true;
        }
        return findInnerObject(stmt.getBody(), offset, context);
    }    
//    private static String toString(CsmExpression expr) {
//        if( expr == null ) {
//            return "null"; //NOI18N
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append("text='"); //NOI18N
//        sb.append(expr.getText());
//        sb.append("'"); //NOI18N
//        return sb.toString();
//    }
    
//    private static String toString(CsmType type) {
//        StringBuilder sb = new StringBuilder();
//        if( type == null ) {
//            sb.append("null"); //NOI18N
//        }
//        else {
//            if( type.isPointer() ) sb.append(" * "); //NOI18N
//            if( type.isReference() ) sb.append(" & "); //NOI18N
//            CsmClassifier classifier = type.getClassifier();
//            sb.append(classifier == null ? "<null>": classifier.getName());
//            for( int i = 0; i < type.getArrayDepth(); i++ ) {
//                sb.append("[]"); //NOI18N
//            }
//        }
//        return sb.toString();
//    }
    
//    private static String toString(CsmCondition condition) {
//        if( condition == null ) {
//            return "null"; //NOI18N
//        }
//        StringBuilder sb = new StringBuilder(condition.getKind().toString());
//        sb.append(' '); //NOI18N
//        if( condition.getKind() == CsmCondition.Kind.EXPRESSION  ) {
//            sb.append(toString(condition.getExpression()));
//        }
//        else { // condition.getKind() == CsmCondition.Kind.DECLARATION
//            CsmVariable var = condition.getDeclaration();
//            sb.append(toString(var));
//        }
//        return sb.toString();
//    }
//    
//    private static String toString(CsmVariable var) {
//        if( var == null ) {
//            return "null"; //NOI18N
//        }
//        StringBuilder sb = new StringBuilder(var.getName());
//        sb.append(getOffsetString(var));
//        sb.append(" type: " + toString(var.getType())); //NOI18N
//        return sb.toString();
//    }
//    
//    private static String getOffsetString(CsmOffsetable obj) {
//        return " [" + obj.getStartPosition() + '-' + obj.getEndPosition() + ']'; //NOI18N
//    }
    
    private static void print(String s) {
        if (CsmUtilities.DEBUG) System.out.println(s);
    }    
    
//    /*
//     * container for found object
//     * it could be CsmStatement of CsmObject
//     */
//    public static class Result {
//        private CsmObject result;
//        private CsmStatement container;
//        
//        public Result(CsmStatement container) {
//            this(container, null);
//        }
//        
//        public Result(CsmStatement container, CsmObject result) {
//            this.container = container;
//            this.result = result;
//        }
//        
//        public boolean isStatement() {
//            return result == null;
//        }
//        
//        public CsmObject getObject() {
//            return result;
//        }
//        
//        public CsmStatement getLastStatement() {
//            return container;
//        }
//    }
}

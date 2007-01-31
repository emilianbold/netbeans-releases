/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.model.deep;

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.util.TypeSafeEnum;

/**
 * Represents some statement -
 * acts as a common ancestor for each of the particular statement interfaces
 *
 * @author Vladimir Kvashin
 */
public interface CsmStatement extends CsmOffsetable, CsmObject, CsmScopeElement {

        // TODO: does throws statement include trailing ";" or not?
    
        class Kind extends TypeSafeEnum {
            
            private Kind(String id) {
                super(id);
            }
            
            /** 
             * Label pseudo statement. 
             * Does NOT include the statement following after label, just the label itself
             * An instance is guaranteed to implement CsmLabel 
             */
            public static final Kind LABEL = new Kind("Label"); // NOI18N

            /**
             * "case" pseudo statement. 
             * Does NOT include the statement after "case ...:" clause, just "case ...:" itself
             * An instance is guaranteed to implement CsmCaseStatement
             */
            public static final Kind CASE = new Kind("Case"); // NOI18N

            /** 
             * "default" psewdo statement. 
             * It does NOT include the statement after "default:" clause, just "default:" itself.
             * It isn't a statement from C++ standard point of view
             * TODO: rethink
             * No special derived interface. 
             */
            public static final Kind DEFAULT = new Kind("Default"); // NOI18N

            /** Expression statement. An instance is guaranteed to implement CsmExpressionStatement */
            public static final Kind EXPRESSION = new Kind("Expression"); // NOI18N

            /** Compound statement. An instance is guaranteed to implement CsmCompoundStatement */
            public static final Kind COMPOUND = new Kind("Compound"); // NOI18N

            /** if statement. An instance is guaranteed to implement CsmIfStatement */
            public static final Kind IF = new Kind("If"); // NOI18N

            /** switch statement. An instance is guaranteed to implement CsmSwitchStatement */
            public static final Kind SWITCH = new Kind("Switch"); // NOI18N

            /** while statement. An instance is guaranteed to implement CsmLoopStatement */
            public static final Kind WHILE = new Kind("While"); // NOI18N

            /** do ... while statement. An instance is guaranteed to implement CsmLoopStatement */
            public static final Kind DO_WHILE = new Kind("DoWhile"); // NOI18N

            /** For statement. An instance is guaranteed to implement CsmForStatement */
            public static final Kind FOR = new Kind("For"); // NOI18N

            /** Break statement. No special derived interface. */
            public static final Kind BREAK = new Kind("Break"); // NOI18N

            /** Continue statement. No special derived interface. */
            public static final Kind CONTINUE = new Kind("Continue"); // NOI18N

            /** Return statement. An instance is guaranteed to implement CsmReturnStatement */
            public static final Kind RETURN = new Kind("Return"); // NOI18N

            /** Goto statement. An instance is guaranteed to implement CsmGotoStatement */
            public static final Kind GOTO = new Kind("Goto"); // NOI18N

            /** Declaration statement. An instance is guaranteed to implement CsmDeclarationStatement */
            public static final Kind DECLARATION = new Kind("Declaration"); // NOI18N

            /** Try... catch statement. An instance is guaranteed to implement CsmTryCatchStatement */
            public static final Kind TRY_CATCH = new Kind("TryCatch"); // NOI18N

            /** Exception handler (catch) An instance is guaranteed to implement CsmExceptionHandler */
            public static final Kind CATCH = new Kind("Catch"); // NOI18N
            
            /** Exception handler (catch) An instance is guaranteed to implement CsmExceptionHandler */
            public static final Kind THROW = new Kind("Throw"); // NOI18N
            
        }
        
        /**
         * Gets this statement kind.
         * Kind determines, which derived interface is implemented by the instance.
         *
         * Never use instanceof operator instead of checking kind 
         * (you may use to just make sure that necessary interface is implemented,
         * but first check the kind. For example, if a statement is an instance of CsmCompoundStatement,
         * this does not mean, that this is really compound statement - it might be exceptoin handler 
         * or conditional statement
         */
        Kind getKind();
}

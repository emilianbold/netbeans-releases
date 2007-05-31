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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.DataInput;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import java.util.*;

import antlr.collections.AST;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 *
 * @author Vladimir Kvashin
 */
public class TypeFactory {
    
    private TypeFactory() {}
    
    public static TypeImpl createBuiltinType(String text, AST ptrOperator, int arrayDepth, AST ast, CsmFile file) {
        CsmBuiltIn builtin = BuiltinTypes.getBuiltIn(text);
        return createType(builtin, ptrOperator, arrayDepth, ast, file);
    }

    public static TypeImpl createType(CsmClassifier classifier, AST ptrOperator, int arrayDepth, AST ast, CsmFile file) {
        boolean pointer = false;
        boolean refence = false;
        int pointerDepth = 0;
        if (ptrOperator != null &&
            (ptrOperator.getType() == CPPTokenTypes.CSM_CLASS_DECLARATION ||
            ptrOperator.getType() == CPPTokenTypes.CSM_ENUM_DECLARATION)) {
            ptrOperator = ptrOperator.getFirstChild();
            int count = 0; 
            boolean findBody = false;
            boolean findStruct = false;
            for (; ptrOperator != null; ptrOperator = ptrOperator.getNextSibling()){
                switch( ptrOperator.getType() ) {
                    case CPPTokenTypes.LITERAL_struct:
                    case CPPTokenTypes.LITERAL_class:
                    case CPPTokenTypes.LITERAL_enum:
                    case CPPTokenTypes.LITERAL_union:
                        findStruct = true;
                        continue;
                    case CPPTokenTypes.LCURLY:
                        findBody = true;
                        count++;
                        continue;
                    case CPPTokenTypes.RCURLY:
                        count--;
                        if (findStruct && count == -1){
                            count = 0;
                            findStruct = false;
                            findBody = true;
                        }
                        continue;
                    default:
                        if (findBody && count == 0) {
                            break;
                        }
                        continue;
                }
                break;
            }
        }
        while( ptrOperator != null && ptrOperator.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
            //for( AST token = ptrOperator.getFirstChild(); token != null; token = token.getNextSibling() ) {
                AST token = ptrOperator.getFirstChild();
                switch( token.getType() ) {
                    case CPPTokenTypes.STAR:
                        pointerDepth++;
                        break;
                    case CPPTokenTypes.AMPERSAND:
                        refence = true;
                        break;
                }
            //}
            ptrOperator = ptrOperator.getNextSibling();
        }
        return new TypeImpl(classifier, pointerDepth, refence, arrayDepth, ast, file);
    }
   
    
    public static TypeImpl createType(AST classifier, CsmFile file,  AST ptrOperator, int arrayDepth) {
        boolean pointer = false;
        boolean refence = false;
        int pointerDepth = 0;
        while( ptrOperator != null && ptrOperator.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
            //for( AST token = ptrOperator.getFirstChild(); token != null; token = token.getNextSibling() ) {
                AST token = ptrOperator.getFirstChild();
                switch( token.getType() ) {
                    case CPPTokenTypes.STAR:
                        pointerDepth++;
                        break;
                    case CPPTokenTypes.AMPERSAND:
                        refence = true;
                        break;
                }
            //}
            ptrOperator = ptrOperator.getNextSibling();
        }
        return new TypeImpl(classifier, file, pointerDepth, refence, arrayDepth);
    }
    
}

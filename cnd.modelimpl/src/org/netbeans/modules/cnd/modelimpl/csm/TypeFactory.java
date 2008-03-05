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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

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
        return createType(classifier, ptrOperator, arrayDepth, ast, file, null);
    }
    
    public static TypeImpl createType(CsmClassifier classifier, AST ptrOperator, int arrayDepth, AST ast, CsmFile file, CsmOffsetable offset) {
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
        if(offset == null) {
            return new TypeImpl(classifier, pointerDepth, refence, arrayDepth, ast, file);
        } else {
            return new TypeImpl(classifier, pointerDepth, refence, arrayDepth, ast, file, offset);
        }
    }
   
    
    public static TypeImpl createType(AST classifier, CsmFile file,  AST ptrOperator, int arrayDepth) {
        boolean pointer = false;
        boolean refence = false;
        int pointerDepth = 0;
        while( ptrOperator != null && ptrOperator.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
            //for( AST token = ptrOperator.getFirstChild(); token != null; token = token.getNextSibling() ) {
                AST token = ptrOperator.getFirstChild();
                if (token != null) {
                    switch( token.getType() ) {
                        case CPPTokenTypes.STAR:
                            pointerDepth++;
                            break;
                        case CPPTokenTypes.AMPERSAND:
                            refence = true;
                            break;
                    }
                }
            //}
            ptrOperator = ptrOperator.getNextSibling();
        }
	
	return (TypeFunPtrImpl.isFunctionPointerParamList(classifier)) ?
	    new TypeFunPtrImpl(classifier, file, pointerDepth, refence, arrayDepth) :
	    new TypeImpl(classifier, file, pointerDepth, refence, arrayDepth);
	
//	return new TypeImpl(classifier, file, pointerDepth, refence, arrayDepth);
    }
    
}

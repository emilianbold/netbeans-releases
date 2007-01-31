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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.* ;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * Implements CsmEnum
 * @author Vladimir Kvashin
 */
public class EnumImpl extends ClassEnumBase  implements CsmEnum, CsmMember {
    
    private List/*CsmEnumerator*/ enumerators = Collections.EMPTY_LIST;

    public EnumImpl(AST ast, NamespaceImpl namespace, CsmFile file) {
        this(ast, namespace, file, null);
    }
    
    public EnumImpl(AST ast, NamespaceImpl namespace, CsmFile file, CsmClass containingClass) {
        super(AstUtil.findId(ast, CPPTokenTypes.RCURLY), namespace, file, containingClass, ast);
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST ) {
                for( AST t = token.getFirstChild(); t != null; t = t.getNextSibling() ) {
                    if( t.getType() == CPPTokenTypes.ID ) {
                        EnumeratorImpl ei = new EnumeratorImpl(t, this);
                    }
                }
            }
        }
        register();
    }

    public List getEnumerators() {
        return enumerators;
    }
    
    public void addEnumerator(CsmEnumerator enumerator) {
        if( enumerators == Collections.EMPTY_LIST) {
            enumerators = new ArrayList();
        }
        enumerators.add(enumerator);
    }

    public List getScopeElements() {
        return getEnumerators();
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUM;
    }
}

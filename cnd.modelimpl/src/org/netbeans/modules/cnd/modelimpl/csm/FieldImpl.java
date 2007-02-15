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

import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;

/**
 * CsmVariable + CsmMember implementation
 * @author Vladimir Kvashin
 */
public class FieldImpl extends VariableImpl<CsmField> implements CsmField {

    // only one of containingClassOLD/containingClassUID must be used (based on USE_REPOSITORY)  
//    private /*final*/ CsmClass containingClassOLD;
//    private /*final*/ CsmUID<CsmClass> containingClassUID;
    private final CsmVisibility visibility;

    public FieldImpl(ClassImpl cls, CsmVisibility visibility, String name) {
        super(name, cls.getContainingFile());
        this.visibility = visibility;
        setScope(cls);
    }
    
//    public FieldImpl(AST ast, ClassImpl cls, CsmVisibility visibility) {
//        super(""/*AstUtil.findId(ast)*/, cls.getContainingFile(), 0, 0);
//        AST var = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_VARIABLE_DECLARATION);
//        setName(AstUtil.findId(var == null ? ast : var));
//        init(cls, visibility);
//        setAst(ast);
//        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
//            switch( token.getType() ) {
//                case CPPTokenTypes.LITERAL_static:
//                    setStatic(true);
//                    break;
//            }
//        }
//    }
    
    public FieldImpl(AST ast, CsmFile file, CsmType type, String name, ClassImpl cls, CsmVisibility visibility) {
        super(ast, file, type, name, false);
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_static:
                    setStatic(true);
                    break;
            }
        }
        this.visibility = visibility;
//        this._setContainingClass(cls);
        setScope(cls);
    }
    
//    private void init(ClassImpl cls, CsmVisibility visibility) {
//        this.visibility = visibility;
//        this.containingClass = cls;
//    }
    
    public CsmClass getContainingClass() {
        return (CsmClass) getScope();
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }
     
//    public void setVisibility(CsmVisibility visibility) {
//        this.visibility = visibility;
//    }

//    private CsmClass _getContainingClass() {
//        if (TraceFlags.USE_REPOSITORY) {
//            CsmClass containingClass = UIDCsmConverter.UIDtoClass(containingClassUID);
//            assert (containingClass != null || containingClassUID == null);
//            return containingClass;            
//        } else {
//            return containingClassOLD;
//        }
//    }
//
//    private void _setContainingClass(CsmClass containingClass) {
//        if (TraceFlags.USE_REPOSITORY) {
//            containingClassUID = UIDCsmConverter.ClassToUID(containingClass);
//            assert (containingClassUID != null || containingClass == null);
//        } else {
//            this.containingClassOLD = containingClass;
//        }     
//    }
}

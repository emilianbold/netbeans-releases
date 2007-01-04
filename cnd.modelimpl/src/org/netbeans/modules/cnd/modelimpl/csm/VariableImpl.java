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
import org.netbeans.modules.cnd.api.model.deep.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;

/**
 *
 * @author Dmitriy Ivanov
 */
public class VariableImpl extends OffsetableDeclarationBase implements CsmVariable, Disposable {

    private String name;
    private CsmType type;
    private byte _static = -1;
    private CsmScope scope;
    private byte _extern = -1;
    private ExpressionBase initExpr;
    
    /** Creates a new instance of VariableImpl */
    public VariableImpl(String name, CsmFile file) {
        super(file);
        this.name = name;
        registerInProject();
    }

    /** Creates a new instance of VariableImpl */
    public VariableImpl(AST ast, CsmFile file, CsmType type, String name) {
        this(ast, file, type, name, false);
    }

    /** Creates a new instance of VariableImpl */
    public VariableImpl(AST ast, CsmFile file, CsmType type, String name, boolean isLocal) {
        super(ast, file);
        initInitialValue(ast);
        _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static) ? (byte)1 : (byte)0;
        _extern = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_extern) ? (byte)1 : (byte)0;
        this.name = name;
        this.type = type;
        if (!isLocal) {
            registerInProject();
        }
    }
    
    private void registerInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            ((ProjectBase) project).registerDeclaration(this);
        }
    }
    
    private void unregisterInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            ((ProjectBase) project).unregisterDeclaration(this);
        }
    }
    
    
    /** Gets this element name */
    public String getName() {
        return name;
    }
    
    public String getQualifiedName() {
        CsmScope scope = getScope();
        if( (scope instanceof CsmNamespace) || (scope instanceof CsmClass) ) {
            return ((CsmQualifiedNamedElement) scope).getQualifiedName() + "::" + getName();
        }
        return getName();
    }
  
// Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }

    /** Gets this variable type */
    // TODO: fix it
    public CsmType getType() {
        return type;
    }
    
    private final void initInitialValue(AST node) {
        if( node != null ) {
            AST tok = AstUtil.findChildOfType(node, CPPTokenTypes.ASSIGNEQUAL);
            if( tok != null ) {
                tok = tok.getNextSibling();
            }
            if( tok != null ) {
                initExpr = new ExpressionBase(tok, getContainingFile(), null);
            }
        }
    }
    
    /** Gets this variable initial value */
    public CsmExpression getInitialValue() {
        return initExpr;
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.VARIABLE;
    }

    //TODO: create an interface to place getDeclarationText() in
    public String getDeclarationText() {
        return "";
    }

    public boolean isAuto() {
        return true;
    }

    public boolean isRegister() {
        return false;
    }

    public boolean isStatic() {
        return _static > 0;
    }
    
    public void setStatic(boolean _static) {
        this._static = _static ? (byte) 1 : (byte) 0;
    }

    public boolean isExtern() {
        return _extern > 0;
    }

    public boolean isConst() {
	CsmType type = getType();
	if( type != null ) {
	    return type.isConst();
	}
	return false;
    }

//    // TODO: remove and replace calls with
//    // isConst() && ! isExtern
//    public boolean isConstAndNotExtern() {
//        if( isExtern() ) {
//            return false;
//        }
//        else {
//            // it isn't extern
//            CsmType type = getType();
//            if( type == null ) {
//                return false;
//            }
//            else {
//                return type.isConst();
//            }
//        }
//    }
    
    public boolean isMutable() {
        return false;
    }

    public void setScope(CsmScope scope) {
        unregisterInProject();
        this.scope = scope;
        registerInProject();
    }

    public CsmScope getScope() {
        return scope;
    }

    public void dispose() {
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
    }

    public CsmVariableDefinition getDefinition() {
        String uname = CsmDeclaration.Kind.VARIABLE_DEFINITION.toString() + UNIQUE_NAME_SEPARATOR + getQualifiedName();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
        return (def == null) ? null : (CsmVariableDefinition) def;
    }
}

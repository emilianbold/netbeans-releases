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
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.CsmObjectAccessor;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;

/**
 *
 * @author Dmitriy Ivanov
 */
public class VariableImpl<T> extends OffsetableDeclarationBase<T> implements CsmVariable<T>, Disposable {
    
    private String name;
    private CsmType type;
    private byte _static = -1;
    
    // only one of scopeOLD/scopeAccessor must be used (based on USE_REPOSITORY)
    private CsmScope scopeOLD;
    private CsmObjectAccessor scopeAccessor;
    
    private byte _extern = -1;
    private ExpressionBase initExpr;
    
    /** Creates a new instance of VariableImpl */
    public <T> VariableImpl(String name, CsmFile file) {
        super(file);
        this.name = name;
//        registerInProject();
    }
    
    /** Creates a new instance of VariableImpl */
    public <T> VariableImpl(AST ast, CsmFile file, CsmType type, String name) {
        this(ast, file, type, name, true);
    }
    
    /** Creates a new instance of VariableImpl */
    public <T> VariableImpl(AST ast, CsmFile file, CsmType type, String name, boolean registerInProject) {
        super(ast, file);
        initInitialValue(ast);
        _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static) ? (byte)1 : (byte)0;
        _extern = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_extern) ? (byte)1 : (byte)0;
        this.name = name;
        this.type = type;
        if (registerInProject) {
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
            this.cleanUID();
        }
    }
    
    
    /** Gets this element name */
    public String getName() {
        return name;
    }
    
    public String getQualifiedName() {
        CsmScope scope = getScope();
        if( (scope instanceof CsmNamespace) || (scope instanceof CsmClass) ) {
            return ((CsmQualifiedNamedElement) scope).getQualifiedName() + "::" + getQualifiedNamePostfix(); // NOI18N
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
        this._setScope(scope);
        registerInProject();
    }
    
    public CsmScope getScope() {
        return _getScope();
    }
    
    public void dispose() {
        if( _getScope() instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) _getScope()).removeDeclaration(this);
        }
    }
    
    public CsmVariableDefinition getDefinition() {
        String uname = CsmDeclaration.Kind.VARIABLE_DEFINITION.toString() + UNIQUE_NAME_SEPARATOR + getQualifiedName();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
        return (def == null) ? null : (CsmVariableDefinition) def;
    }
    
    private CsmScope _getScope() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmScope scope = UIDCsmConverter.accessorToScope(this.scopeAccessor);
            assert (scope != null || scopeAccessor == null);
            return scope;
        } else {
            return scopeOLD;
        }
    }
    
    private void _setScope(CsmScope scope) {
        if (TraceFlags.USE_REPOSITORY) {
            this.scopeAccessor = UIDCsmConverter.scopeToAccessor(scope);
            assert (scopeAccessor != null || scope == null);
        } else {
            this.scopeOLD = scope;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of RegistarableDeclaration
    
    // flag indicated wether declaration was registered in project or not
    private boolean registered = false;
    
    public boolean isRegistered() {
        return this.registered;
    }

    public void registered() {
        this.registered = true;
    }

    public void unregistered() {
        this.registered = false;
        cleanUID();
    }    
}

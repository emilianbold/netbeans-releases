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

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 *
 * @param T 
 * @author Dmitriy Ivanov
 */
public class VariableImpl<T> extends OffsetableDeclarationBase<T> implements CsmVariable<T>, Disposable {
    
    private final String name;
    private final CsmType type;
    private boolean _static = false;
    
    // only one of scopeRef/scopeAccessor must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private CsmScope scopeRef;
    private CsmUID<CsmScope> scopeUID;
    
    private final boolean _extern;
    private ExpressionBase initExpr;

    /** Creates a new instance of VariableImpl 
     * @param ast 
     * @param file 
     * @param type 
     * @param name 
     * @param registerInProject 
     */
    public VariableImpl(AST ast, CsmFile file, CsmType type, String name, boolean registerInProject) {
	this(ast, file, type, name, null, registerInProject);
    }
    
    /** Creates a new instance of VariableImpl 
     * @param ast 
     * @param file 
     * @param type 
     * @param name 
     * @param scope variable scope
     * @param registerInProject 
     */
    public VariableImpl(AST ast, CsmFile file, CsmType type, String name, CsmScope scope,  boolean registerInProject) {
        super(ast, file);
        initInitialValue(ast);
        _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static);
        _extern = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_extern);
        this.name = name;
        this.type = type;
	_setScope(scope);
        if (registerInProject) {
            registerInProject();
        }
    }
    
    protected final void registerInProject() {
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
    
    
    /** Gets this element name 
     * @return 
     */
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
    
    public String getUniqueNameWithoutPrefix() {
        if (isExtern()) {
            return getQualifiedName() + " (EXTERN)"; // NOI18N
        } else {
            return getQualifiedName();
        }
    }
    
    /** Gets this variable type 
     * @return 
     */
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
    
    /** Gets this variable initial value 
     * @return 
     */
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
        return _static;
    }
    
    public void setStatic(boolean _static) {
        this._static = _static;
    }
    
    public boolean isExtern() {
        return _extern;
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
        super.dispose();
        onDispose();
        // dispose type
        if (this.type != null && this.type instanceof Disposable) {
            ((Disposable)this.type).dispose();
        }
        if( _getScope() instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) _getScope()).removeDeclaration(this);
        }
        unregisterInProject();
    }
    
    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }    
    
    public CsmVariableDefinition getDefinition() {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.VARIABLE_DEFINITION) + UNIQUE_NAME_SEPARATOR + getQualifiedName();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
        return (def == null) ? null : (CsmVariableDefinition) def;
    }
    
    private CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            if (TraceFlags.USE_REPOSITORY) {
                scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
                assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
            }
        }
        return scope;
    }
    
    private void _setScope(CsmScope scope) {
	// for variables declared in bodies scope is CsmCompoundStatement - it is not Identifiable
        if ((scope instanceof CsmIdentifiable) && TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (scopeUID != null || scope == null);
        } else {
            this.scopeRef = scope;
        }
    }
    
    public String getDisplayText() {
	StringBuilder sb = new StringBuilder();
	CsmType type = getType();
	if( type instanceof TypeImpl ) {
	    return ((TypeImpl) type).getText(false, this.getName()).toString();
	}
	else if( type != null ) {
	    sb.append(type.getText());
	    String name = getName();
	    if (name != null && name.length() >0) {
		sb.append(' ');
		sb.append(name);
	    }
	}
	return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output); 
        assert this.name != null;
        output.writeUTF(this.name);
        output.writeBoolean(this._static);
        output.writeBoolean(this._extern);
        PersistentUtils.writeExpression(initExpr, output);
        PersistentUtils.writeType(type, output);
             
        // could be null UID (i.e. parameter)
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);        
    }  
    
    public VariableImpl(DataInput input) throws IOException {
        super(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        this._static = input.readBoolean();
        this._extern = input.readBoolean();
        this.initExpr = (ExpressionBase) PersistentUtils.readExpression(input);
        this.type = PersistentUtils.readType(input);

        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // could be null UID (i.e. parameter)
        this.scopeRef = null;
    }    
    
    public String toString() {
        return (isExtern() ? "EXTERN " : "") + super.toString(); // NOI18N
    }     
}

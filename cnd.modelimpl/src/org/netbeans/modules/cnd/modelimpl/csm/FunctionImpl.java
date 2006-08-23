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

import java.util.*;
import java.util.List;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 *
 * @author Dmitriy Ivanov, Vladimir Kvashin
 */
public class FunctionImpl extends OffsetableDeclarationBase implements CsmFunction, Disposable, RawNamable {

//    public FunctionImpl(String name, CsmFile file, int start, int end) {
//        super(file, start, end);
//        this.name = name;
//        registerInProject();
//    }
    
    public FunctionImpl(AST ast, CsmFile file, CsmScope scope) {
        super(file, 0, 0);
        init();
        this.scope = scope;
        name = AstUtil.findId(ast);
        if( name == null ) {
            name = "<null>"; // just to avoid NPE
        }
        setAst(ast);
        registerInProject();
    }
    
    /** 
     * Is called by ancestor class just after sertting AST, prior than registering in project, etc
     * Override and place all necessar initialization here instead of attributes initializer or constructor.
     */
    protected void init() {
    }
    
    protected void registerInProject() {
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
    
    protected void setName(String name) {
        this.name = name;
    }

    public String getQualifiedName() {
        CsmScope scope = getScope();
        if( (scope instanceof CsmNamespace) || (scope instanceof CsmClass) ) {
	    String scopeQName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
	    if( scopeQName != null && scopeQName.length() > 0 ) {
		return scopeQName + "::" + getName();
	    }
	    else {
		return getName();
	    }
        }
        return getName();
    }
    
    public String[] getRawName() {
        return AstUtil.getRawNameInChildren(getAst());
    }
    
    public String toString() {
        return "" + getKind() + ' ' + name /*+ " rawName=" + Utils.toString(getRawName())*/;
    }
    
    public String getUniqueNameWithoutPrefix() {
        return getQualifiedName() + getSignature().substring(getName().length());
    }
    
    public Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION;
    }
    
    /** Gets this function's declaration text */
    public String getDeclarationText() {
        return "";
    }
    
    /** 
     * Gets this function definition 
     * TODO: describe getDefiition==this ...
     */
    public CsmFunctionDefinition getDefinition() {
        String uname = CsmDeclaration.Kind.FUNCTION_DEFINITION.toString() + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
        return (def == null) ? null : (CsmFunctionDefinition) def;
    }
    
    /** 
     * Returns true if this class is template, otherwise false.
     * If isTemplate() returns true, this class is an instance of CsmTemplate
     */
    public boolean isTemplate() {
        return false;
    }

    /** 
     * Gets this function body.
     * The same as the following call:
     * (getDefinition() == null) ? null : getDefinition().getBody();
     *
     * TODO: perhaps it isn't worth keeping duplicate to getDefinition().getBody()? (though convenient...)
     */
    public CsmCompoundStatement getBody() {
        return null;
    }
    
    public boolean isInline() {
        return false;
    }
    
//    public boolean isVirtual() {
//        return false;
//    }
//    
//    public boolean isExplicit() {
//        return false;
//    }

    public CsmType getReturnType() {
        if( returnType == null ) {
            AST token = getTypeToken();
            if( token != null ) {
                returnType = AstRenderer.renderType(token, getContainingFile());
            }
            if( returnType == null ) {
                returnType = TypeImpl.createBuiltinType("int", (AST) null, 0,  null/*getAst().getFirstChild()*/, getContainingFile());
            }
        }
        return returnType;
    }
    
    private AST getTypeToken() {
        for( AST token = getAst().getFirstChild(); token != null; token = token.getNextSibling() ) {
            int type = token.getType();
            switch( type ) {
                case CPPTokenTypes.CSM_TYPE_BUILTIN:
                case CPPTokenTypes.CSM_TYPE_COMPOUND:
                    return token;
                default:
                    if( AstRenderer.isQialifier(type) ) {
                        return token;
                    }
            }
        }
        return null;
    }
    
    public List/*<CsmParameter>*/  getParameters() {
        if( parameters == null ) {
            AST ast = AstUtil.findChildOfType(getAst(), CPPTokenTypes.CSM_PARMLIST);
            if( ast != null ) {
                // for K&R-style
                AST ast2 = AstUtil.findSiblingOfType(ast.getNextSibling(), CPPTokenTypes.CSM_PARMLIST);
                if( ast2 != null ) {
                    ast = ast2;
                }
            }
            parameters = AstRenderer.renderParameters(ast, getContainingFile());
        }
        return parameters;
    }
    
    public CsmScope getScope() {
        return scope;
    }

    public void setScope(CsmScope scope) {
        unregisterInProject();
        this.scope = scope;
        registerInProject();
    }
    
    public String getSignature() {
        if( signature == null ) {
            signature = createSignature();
        }
        return signature;
    }
    
    protected String createSignature() {
        // TODO: this fake implementation for Deimos only!
        // we should resolve parameter types and provide
        // kind of canonical representation here
        StringBuffer sb = new StringBuffer(getName());
        sb.append('(');
        for( Iterator iter = getParameters().iterator(); iter.hasNext(); ) {
            CsmParameter param = (CsmParameter) iter.next();
            CsmType type = param.getType();
            if( type != null )  {
                sb.append(type.getText());
                if( iter.hasNext() ) {
                    sb.append(',');
                }
            }
        }
        sb.append(')');
        return sb.toString();
    }
    
    public void dispose() {
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
    }
    
    private String name;
    private CsmType returnType;
    private List/*<CsmParameter>*/  parameters;
    private String signature;
    private CsmScope scope;

}

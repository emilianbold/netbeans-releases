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
    
    private String name;
    private final CsmType returnType;
    private final List/*<CsmParameter>*/  parameters;
    private String signature;
    private final CsmScope scope;
    private final String[] rawName;
    
    /** see comments to isConst() */
    private final byte _const;

//    public FunctionImpl(String name, CsmFile file, int start, int end) {
//        super(file, start, end);
//        this.name = name;
//        registerInProject();
//    }
    
    public FunctionImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file);
        this.scope = scope;
        name = initName(ast);
        rawName = AstUtil.getRawNameInChildren(ast);
        _const = initConst(ast);
        returnType = initReturnType(ast);
        parameters = initParameters(ast);
        if( name == null ) {
            name = "<null>"; // just to avoid NPE
        }
        initBeforeRegister(ast);
        registerInProject();
    }
    
    protected void initBeforeRegister(AST ast) {
        
    }
    
    protected String initName(AST node) {
        return findFunctionName(node);
    }
    
    /*private AST findLastID(AST ast) {
        return getQialifiedId();
//        AST last = null;
//        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
//            int type = token.getType();
//            if( type == CPPTokenTypes.CSM_QUALIFIED_ID ) {
//                last = token;
//            } else if ( type == CPPTokenTypes.COLON ){
//                break;
//            }
//        }
//        return last;
    }*/
    
    private static String extractName(AST token){
        int type = token.getType();
        if( type == CPPTokenTypes.ID ) {
            return token.getText();
        } else if( type == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            AST last = AstUtil.getLastChild(token);
            if( last != null) {
                if( last.getType() == CPPTokenTypes.ID ) {
                    return last.getText();
                } else {
                    AST first = token.getFirstChild();
                    if( first.getType() == CPPTokenTypes.LITERAL_OPERATOR ) {
                        StringBuffer sb = new StringBuffer(first.getText());
                        sb.append(' ');
                        AST next = first.getNextSibling();
                        if( next != null ) {
                            sb.append(next.getText());
                        }
                        return sb.toString();
                    } else if (first.getType() == CPPTokenTypes.ID){
                        return first.getText();
                    }
                }
            }
        }
        return "";
    }
    
    private static String findFunctionName(AST ast) {
        AST token = AstUtil.findMethodName(ast);
        if (token != null){
            return extractName(token);
        }
        return "";
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
    
    protected final void setName(String name) {
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
        return rawName;
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
        CsmProject prj = getContainingFile().getProject();
        CsmDeclaration def = prj.findDeclaration(uname);
        if (def == null) {
            for (Iterator i = prj.getLibraries().iterator(); i.hasNext();){
                CsmProject lib = (CsmProject)i.next();
                def = lib.findDeclaration(uname);
                if (def != null) {
                    break;
                }
            }
        }
        if (def == null) {
            for (Iterator i = CsmModelAccessor.getModel().projects().iterator(); i.hasNext();){
                CsmProject p = (CsmProject)i.next();
                if (p != prj){
                    def = p.findDeclaration(uname);
                    if (def != null) {
                        break;
                    }
                }
            }
        }
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
    
    private CsmType initReturnType(AST node) {
        CsmType ret = null;
        AST token = getTypeToken(node);
        if( token != null ) {
            ret = AstRenderer.renderType(token, getContainingFile());
        }
        if( ret == null ) {
            ret = TypeImpl.createBuiltinType("int", (AST) null, 0,  null/*getAst().getFirstChild()*/, getContainingFile());
        }
        return ret;
    }

    public CsmType getReturnType() {
        return returnType;
    }
    
    private static AST getTypeToken(AST node) {
        for( AST token = node.getFirstChild(); token != null; token = token.getNextSibling() ) {
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
    
    private List initParameters(AST node) {
        AST ast = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_PARMLIST);
        if( ast != null ) {
            // for K&R-style
            AST ast2 = AstUtil.findSiblingOfType(ast.getNextSibling(), CPPTokenTypes.CSM_PARMLIST);
            if( ast2 != null ) {
                ast = ast2;
            }
        }
        return AstRenderer.renderParameters(ast, getContainingFile());
    }
    
    public List/*<CsmParameter>*/  getParameters() {
        return parameters;
    }
    
    public CsmScope getScope() {
        return scope;
    }

    public String getSignature() {
        if( signature == null ) {
            signature = createSignature();
        }
        return signature;
    }
    
    private String createSignature() {
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
        if( isConst() ) {
            sb.append(" const");
        }
        return sb.toString();
    }
    
    public void dispose() {
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
    }
    
    private static byte initConst(AST node) {
        byte ret = 0;
        AST token = node.getFirstChild();
        while( token != null &&  token.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
            token = token.getNextSibling();
        }
        while( token != null ) {
            if( token.getType() == CPPTokenTypes.LITERAL_const ) {
                ret = 1;
                break;
            }
            token = token.getNextSibling();
        }
        return ret;
    }
    
    /** 
     * isConst was originslly in MethodImpl;
     * but this methods needs internally in FunctionDefinitionImpl
     * to create proper sugnature. 
     * Thereform it's moved here as a protected method.
     */
    protected boolean isConst() {
        return _const > 0;
    }
}

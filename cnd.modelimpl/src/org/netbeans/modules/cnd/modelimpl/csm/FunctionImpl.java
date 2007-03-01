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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 *
 * @author Dmitriy Ivanov, Vladimir Kvashin
 */
public class FunctionImpl<T> extends OffsetableDeclarationBase<T> implements CsmFunction<T>, Disposable, RawNamable {
    
    private String name;
    private final CsmType returnType;
    private final List/*<CsmParameter>*/  parametersOLD;
    private final List<CsmUID<CsmParameter>>  parameters;
    private String signature;
    
    // only one of scopeOLD/scopeAccessor must be used (based on USE_REPOSITORY)
    private final CsmScope scopeOLD;
    private final CsmUID<CsmScope> scopeUID;

    private final String[] rawName;
    
    /** see comments to isConst() */
    private final boolean _const;
    
    public FunctionImpl(AST ast, CsmFile file, CsmScope scope) {
        this(ast, file, scope, true);
    }
    
    protected FunctionImpl(AST ast, CsmFile file, CsmScope scope, boolean register) {
        super(ast, file);

        // set scope, do it in constructor to have final fields
        if (TraceFlags.USE_REPOSITORY) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (this.scopeUID != null || scope == null);
            this.scopeOLD = null;
        } else {
            this.scopeOLD = scope;
            this.scopeUID = null;
        }
        
        name = initName(ast);
        rawName = AstUtil.getRawNameInChildren(ast);
        _const = initConst(ast);
        returnType = initReturnType(ast);

        // set parameters, do it in constructor to have final fields
        List parameters = initParameters(ast);
        if (TraceFlags.USE_REPOSITORY) {
            if (parameters == null) {
                this.parameters = null;
            } else {
                this.parameters = RepositoryUtils.put(parameters);
            }
            this.parametersOLD = null;
        } else {
            this.parametersOLD = parameters;
            this.parameters = null;
        }
        
        if( name == null ) {
            name = "<null>"; // just to avoid NPE // NOI18N
        }
        if (register) {
            registerInProject();
        }
    }
    
    protected String initName(AST node) {
        return findFunctionName(node);
    }
    
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
//		    if( first.getType() == CPPTokenTypes.LITERAL_OPERATOR ) {
		    AST operator = AstUtil.findChildOfType(token, CPPTokenTypes.LITERAL_OPERATOR);
		    if( operator != null ) {
			StringBuffer sb = new StringBuffer(operator.getText());
			sb.append(' ');
			AST next = operator.getNextSibling();
			if( next != null ) {
			    sb.append(next.getText());
			}
			return sb.toString();
		    } else {
			AST first = token.getFirstChild();
			if (first.getType() == CPPTokenTypes.ID) {
			    return first.getText();
			}
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
            this.cleanUID();
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
		return scopeQName + "::" + getQualifiedNamePostfix(); // NOI18N
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
        return "" + getKind() + ' ' + name /*+ " rawName=" + Utils.toString(getRawName())*/; // NOI18N
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
            ret = TypeImpl.createBuiltinType("int", (AST) null, 0,  null/*getAst().getFirstChild()*/, getContainingFile()); // NOI18N
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
        return _getParameters();
    }
    
    public CsmScope getScope() {
        return _getScope();
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
            } else if (param.isVarArgs()) {
                sb.append("..."); // NOI18N
            }
        }
        sb.append(')');
        if( isConst() ) {
            sb.append(" const"); // NOI18N
        }
        return sb.toString();
    }
    
    public void dispose() {
        CsmScope scope = _getScope();
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
        _disposeParameters();
    }
    
    private static boolean initConst(AST node) {
        boolean ret = false;
        AST token = node.getFirstChild();
        while( token != null &&  token.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
            token = token.getNextSibling();
        }
        while( token != null ) {
            if( token.getType() == CPPTokenTypes.LITERAL_const ) {
                ret = true;
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
        return _const;
    }

    private CsmScope _getScope() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmScope out = UIDCsmConverter.UIDToScope(this.scopeUID);
            assert (out != null || this.scopeUID == null);
            return out;
        } else {
            return scopeOLD;
        }
    }

    private List _getParameters() {
        if (TraceFlags.USE_REPOSITORY) {
            if (this.parameters == null) {
                return Collections.EMPTY_LIST;
            } else {
                List<CsmParameter> out = UIDCsmConverter.UIDsToDeclarations(parameters);
                return out;
            }
        } else {
            return parametersOLD;
        }
    }

    private void _disposeParameters() {
        if (TraceFlags.USE_REPOSITORY) {
            if (parameters != null) {
                RepositoryUtils.remove(parameters);
            }
        } else {
            this.parametersOLD.clear();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        output.writeUTF(this.name);
        PersistentUtils.writeType(this.returnType, output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.parameters, output);
        factory.writeUID(this.scopeUID, output);
        PersistentUtils.writeStrings(this.rawName, output);
        output.writeBoolean(this._const);
        
        PersistentUtils.writeUTF(this.signature, output);
    }
    
    public FunctionImpl(DataInput input) throws IOException {
        super(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        this.returnType = PersistentUtils.readType(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.parameters = (List<CsmUID<CsmParameter>>) factory.readUIDCollection(new ArrayList<CsmUID<CsmParameter>>(), input);
        this.scopeUID =factory.readUID(input);
        this.rawName = PersistentUtils.readStrings(input, TextCache.getManager());
        this._const = input.readBoolean();
        
        assert TraceFlags.USE_REPOSITORY;
        parametersOLD = null;
        this.scopeOLD = null;
        
        this.signature = PersistentUtils.readUTF(input);
        if (this.signature != null) {
            this.signature = QualifiedNameCache.getString(this.signature);
        }
    }    
}

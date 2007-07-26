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

import java.util.*;
import java.util.List;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
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
 * @param T
 * @author Dmitriy Ivanov, Vladimir Kvashin
 */
public class FunctionImpl<T> extends OffsetableDeclarationBase<T> 
        implements CsmFunction<T>, Disposable, RawNamable, CsmTemplate {
    
    private String name;
    private final CsmType returnType;
    private final List<CsmParameter>  parametersOLD;
    private final List<CsmUID<CsmParameter>>  parameters;
    private final boolean isVoidParameterList;
    private String signature;
    
    // only one of scopeRef/scopeAccessor must be used (based on USE_REPOSITORY)
    private /*final*/ CsmScope scopeRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmScope> scopeUID;
    
    private final String[] rawName;
    
    /** see comments to isConst() */
    private final boolean _const;
    
    public FunctionImpl(AST ast, CsmFile file, CsmScope scope) {
        this(ast, file, scope, true);
    }
    
    private static final boolean CHECK_SCOPE = false;
    protected FunctionImpl(AST ast, CsmFile file, CsmScope scope, boolean register) {
        super(ast, file);
        assert !CHECK_SCOPE || (scope != null);
        // set scope, do it in constructor to have final fields
        if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (this.scopeUID != null || scope == null);
            this.scopeRef = null;
        } else {
            this.scopeRef = scope;
            this.scopeUID = null;
        }
        
        name = initName(ast);
        rawName = AstUtil.getRawNameInChildren(ast);
        _const = initConst(ast);
        returnType = initReturnType(ast);
        initTemplate(ast);
        
        // set parameters, do it in constructor to have final fields
        List<CsmParameter> params = initParameters(ast);
        if (TraceFlags.USE_REPOSITORY) {
            if (params == null) {
                this.parameters = null;
            } else {
                this.parameters = RepositoryUtils.put(params);
            }
            this.parametersOLD = null;
        } else {
            this.parametersOLD = params;
            this.parameters = null;
        }
        if (params == null || params.size() == 0) {
            isVoidParameterList = isVoidParameter(ast);
        } else {
            isVoidParameterList = false;
        }
        
        if( name == null ) {
            name = "<null>"; // just to avoid NPE // NOI18N
        }
        if (register) {
            registerInProject();
        }
    }
    
    private boolean template;
    private String templateSuffix;
    protected String classTemplateSuffix;
    
    private void initTemplate(AST node) {
        boolean template = false, specialization = false;
        switch(node.getType()) {
            case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION: 
            case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION: 
            case CPPTokenTypes.CSM_CTOR_TEMPLATE_DECLARATION: 
            case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:  
                template = true;
                break;
            case CPPTokenTypes.CSM_TEMPLATE_FUNCTION_DEFINITION_EXPLICIT_SPECIALIZATION:
            case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                template = true;
                specialization = true;
                break;
        }

        if (template) {
            AST templateNode = node.getFirstChild();
            assert ( templateNode != null && templateNode.getType() == CPPTokenTypes.LITERAL_template );
            // 0. our grammar can't yet differ template-class's method from template-method
            // so we need to check here if we has template-class or not
            boolean templateClass = false;
            AST qIdToken = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_QUALIFIED_ID);
            // 1. check for definition of template class's method
            // like template<class A> C<A>:C() {}
            AST startTemplateSign = qIdToken != null ? AstUtil.findChildOfType(qIdToken, CPPTokenTypes.LESSTHAN) : null;
            if (startTemplateSign != null) {
                // TODO: fix parsing of inline definition of template operator <
                // like template<class T, class P> bool operator<(T x, P y) {return x<y};
                // workaround is next validation
                AST endTemplateSign = null;//( startTemplateSign.getNextSibling() != null ? startTemplateSign.getNextSibling().getNextSibling() : null);
                for( AST sibling = startTemplateSign.getNextSibling(); sibling != null; sibling = sibling.getNextSibling() ) {
                    if( sibling.getType() == CPPTokenTypes.GREATERTHAN ) {
                        endTemplateSign = sibling;
                        break;
                    }
                }
                if (endTemplateSign != null) {
                    AST scopeSign = endTemplateSign.getNextSibling();
                    if (scopeSign != null && scopeSign.getType() == CPPTokenTypes.SCOPE) {
                        // 2. we have template class, we need to determine, is it specialization definition or not
                        if (specialization) { 
                            // we need to initialize classTemplateSuffix in this case
                            // to avoid mixing different specialization (IZ92138)
                            this.classTemplateSuffix = TemplateUtils.getSpecializationSuffix(qIdToken);
                        }     
                        // but there is still a chance to have template-method of template-class
                        // e.g.: template<class A> template<class B> C<A>::C(B b) {}
                        AST templateSiblingNode = templateNode.getNextSibling();
                        if ( templateSiblingNode != null && templateSiblingNode.getType() == CPPTokenTypes.LITERAL_template ) {
                            // it is template-method of template-class
                            templateNode = templateSiblingNode;
                        } else {
                            // we have no template-method at all
                            template = false;
                        }
                    }
                }
            }
            
            if (template) {
                // 3. We are sure now what we have template-method, 
                // let's check is it specialization template or not
                if (specialization) { 
                    // 3a. specialization
                    if (qIdToken == null) {
                        // malformed template specification
                        templateSuffix = "<>"; //NOI18N
                    } else {
                        templateSuffix = TemplateUtils.getSpecializationSuffix(qIdToken);
                    }
                } else {
                    // 3b. no specialization, plain and simple template-method
                    StringBuilder sb  = new StringBuilder();
                    TemplateUtils.addSpecializationSuffix(templateNode.getFirstChild(), sb);
                    templateSuffix = '<' + sb.toString() + '>';
                }
            }
        }
        this.template = template;
    }
    
    protected String getScopeSuffix() {
        return classTemplateSuffix != null ? classTemplateSuffix : "";
    }

    protected String initName(AST node) {
        return findFunctionName(node);
    }
    
    public String getDisplayName() {
        return isTemplate() ? getName() + templateSuffix : getName();
    }
    
    public List<CsmTemplateParameter> getTemplateParameters() {
        return Collections.EMPTY_LIST;
    }    
    
    public boolean isVoidParameterList(){
        return isVoidParameterList;
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
                        StringBuilder sb = new StringBuilder(operator.getText());
                        sb.append(' ');
                        for( AST next = operator.getNextSibling(); next != null; next = next.getNextSibling() ) {
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
        if( CastUtils.isCast(ast) ) {
            return CastUtils.getFunctionName(ast);
        }
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
    
    
    /** Gets this element name
     * @return name
     */
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
                return scopeQName + getScopeSuffix() + "::" + getQualifiedNamePostfix(); // NOI18N
            } else {
                return getName();
            }
        }
        return getName();
    }
    
    public String[] getRawName() {
        return rawName;
    }
    
    public String getUniqueNameWithoutPrefix() {
        return getQualifiedName() + (template ? templateSuffix : "") + getSignature().substring(getName().length());
    }
    
    public Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION;
    }
    
    /** Gets this function's declaration text
     * @return declaration text
     */
    public String getDeclarationText() {
        return "";
    }
    
    /**
     * Gets this function definition
     * TODO: describe getDefiition==this ...
     * @return definition
     */
    public CsmFunctionDefinition getDefinition() {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmProject prj = getContainingFile().getProject();
        CsmFunctionDefinition def = findDefinition(prj, uname);
        if (def == null) {
            for (CsmProject lib : prj.getLibraries()){
                def = findDefinition(lib, uname);
                if (def != null) {
                    break;
                }
            }
        }
        if (def == null && (prj instanceof ProjectBase)) {
            for(CsmProject dependent : ((ProjectBase)prj).getDependentProjects()){
                def = findDefinition(dependent, uname);
                if (def != null) {
                    break;
                }
            }
        }
        return def;
    }
    
    private CsmFunctionDefinition findDefinition(CsmProject prj, String uname){
        CsmDeclaration res = prj.findDeclaration(uname);
        if (res instanceof CsmFunctionDefinition) {
            return (CsmFunctionDefinition)res;
        }
        if (getParameters().size()==0 && !isVoidParameterList()) {
            CsmScope scope = getScope();
            if (CsmKindUtilities.isNamespace(scope) && ((CsmNamespace)scope).isGlobal()) {
                if (prj instanceof ProjectBase) {
                    String from = uname.substring(0, uname.indexOf('(')+1);
                    Collection<CsmOffsetableDeclaration> decls = ((ProjectBase)prj).findDeclarationsByPrefix(from);
                    for(CsmOffsetableDeclaration decl : decls){
                        if (!ProjectBase.isCppFile(decl.getContainingFile())){
                            return (CsmFunctionDefinition)decl;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Returns true if this class is template, otherwise false.
     * If isTemplate() returns true, this class is an instance of CsmTemplate
     * @return flag indicated if function is template
     */
    public boolean isTemplate() {
        return template;
    }
    
    /**
     * Gets this function body.
     * The same as the following call:
     * (getDefinition() == null) ? null : getDefinition().getBody();
     *
     * TODO: perhaps it isn't worth keeping duplicate to getDefinition().getBody()? (though convenient...)
     * @return body of function
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
            ret = TypeFactory.createBuiltinType("int", (AST) null, 0,  null/*getAst().getFirstChild()*/, getContainingFile()); // NOI18N
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
                    if( AstRenderer.isQualifier(type) ) {
                        return token;
                    }
            }
        }
        return null;
    }
    
    private List<CsmParameter> initParameters(AST node) {
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

    private boolean isVoidParameter(AST node) {
        AST ast = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_PARMLIST);
        if( ast != null ) {
            // for K&R-style
            AST ast2 = AstUtil.findSiblingOfType(ast.getNextSibling(), CPPTokenTypes.CSM_PARMLIST);
            if( ast2 != null ) {
                ast = ast2;
            }
        }
        return AstRenderer.isVoidParameter(ast);
    }
    
    public List<CsmParameter>  getParameters() {
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
        StringBuilder sb = new StringBuilder(getName());
        sb.append('(');
        for( Iterator iter = getParameters().iterator(); iter.hasNext(); ) {
            CsmParameter param = (CsmParameter) iter.next();
            CsmType type = param.getType();
            if( type != null )  {
                sb.append(type.getCanonicalText());
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
        super.dispose();
        onDispose();
        CsmScope scope = _getScope();
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
        this.unregisterInProject();
        _disposeParameters();
    }
    
    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
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
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            if (TraceFlags.USE_REPOSITORY) {
                scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
                assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
            }
        }
        return scope;
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
        factory.writeUIDCollection(this.parameters, output, false);
        PersistentUtils.writeStrings(this.rawName, output);
        output.writeBoolean(this._const);
        
        // not null UID
        assert !CHECK_SCOPE || this.scopeUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
        
        PersistentUtils.writeUTF(this.signature, output);
        output.writeBoolean(isVoidParameterList);
        output.writeUTF(this.getScopeSuffix());
        output.writeBoolean(this.template);
        if (this.template) {
            output.writeUTF(this.templateSuffix);
        }
    }

    public List<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
        l.addAll(getParameters());
        return l;
    }

    public FunctionImpl(DataInput input) throws IOException {
        super(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        this.returnType = PersistentUtils.readType(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.parameters = factory.readUIDCollection(new ArrayList<CsmUID<CsmParameter>>(), input);
        this.rawName = PersistentUtils.readStrings(input, TextCache.getManager());
        this._const = input.readBoolean();
        
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert !CHECK_SCOPE || this.scopeUID != null;
        this.scopeRef = null;
        
        assert TraceFlags.USE_REPOSITORY;
        parametersOLD = null;
        
        this.signature = PersistentUtils.readUTF(input);
        if (this.signature != null) {
            this.signature = QualifiedNameCache.getString(this.signature);
        }
        this.isVoidParameterList = input.readBoolean();
        this.classTemplateSuffix = input.readUTF();
        this.template = input.readBoolean();
        if (this.template) {
            this.templateSuffix = input.readUTF();
        }
    }
}

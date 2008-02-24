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
import java.util.List;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmFunction.OperatorKind;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 *
 * @param T
 * @author Dmitriy Ivanov, Vladimir Kvashin
 */
public class FunctionImpl<T> extends OffsetableDeclarationBase<T> 
        implements CsmFunction<T>, Disposable, RawNamable, CsmTemplate {
    
    private static final String OPERATOR = "operator"; // NOI18N;
    
    private static final CharSequence NULL = CharSequenceKey.create("<null>"); // NOI18N
    private CharSequence name;
    private final CsmType returnType;
    private final List<CsmUID<CsmParameter>>  parameters;
    private CharSequence signature;
    
    // only one of scopeRef/scopeAccessor must be used 
    private /*final*/ CsmScope scopeRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmScope> scopeUID;
    
    private final CharSequence[] rawName;
    
    private List<CsmTemplateParameter> templateParams = Collections.emptyList();
    
    private CharSequence templateSuffix;
    protected CharSequence classTemplateSuffix;
    
    private static final byte FLAGS_VOID_PARMLIST = 1 << 0;
    private static final byte FLAGS_STATIC = 1 << 1;
    private static final byte FLAGS_CONST = 1 << 2;
    private static final byte FLAGS_OPERATOR = 1 << 3;
    private static final byte FLAGS_TEMPLATE = 1 << 4;
    private byte flags;
    
    public FunctionImpl(AST ast, CsmFile file, CsmScope scope) {
        this(ast, file, scope, true);
    }
    
    private static final boolean CHECK_SCOPE = false;
    
    protected FunctionImpl(AST ast, CsmFile file, CsmScope scope, boolean register) {
        
        super(ast, file);
        assert !CHECK_SCOPE || (scope != null);
        
        name = QualifiedNameCache.getManager().getString(initName(ast));
        rawName = AstUtil.getRawNameInChildren(ast);

        assert ast.getFirstChild() != null;
        setStatic(ast.getFirstChild().getType() == CPPTokenTypes.LITERAL_static);
        if (!isStatic()) {
            for( CsmFunction fu : ((FileImpl) file).getStaticFunctionDeclarations() ) {
                if( name.equals(fu.getName()) ) {
                    // we don't check signature here since file-level statics
                    // is C-style construct
                    setStatic(true); 
                    break;
                }
            }
        }
        
        // change scope to file for static methods, but only to prevent 
        // registration in global  namespace
        if( isStatic() && CsmKindUtilities.isNamespace(scope) &&
                ((CsmNamespace)scope).isGlobal()) {
            scope = file;
        }
        
        // set scope, do it in constructor to have final fields
        this.scopeUID = UIDCsmConverter.scopeToUID(scope);
        assert (this.scopeUID != null || scope == null);
        this.scopeRef = null;
        
        RepositoryUtils.hang(this); // "hang" now and then "put" in "register()"
	
        boolean _const = initConst(ast);
        setFlags(FLAGS_CONST, _const);
        returnType = initReturnType(ast, scope);
        initTemplate(ast);
        
        // set parameters, do it in constructor to have final fields
        List<CsmParameter> params = initParameters(ast);
        if (params == null) {
            this.parameters = null;
        } else {
            this.parameters = RepositoryUtils.put(params);
        }
        if (params == null || params.size() == 0) {
            setFlags(FLAGS_VOID_PARMLIST, isVoidParameter(ast));
        } else {
            setFlags(FLAGS_VOID_PARMLIST, false);
        }
        if( name == null ) {
            name = NULL; // just to avoid NPE
        }
        if (name.toString().startsWith(OPERATOR) && 
                (name.length() > OPERATOR.length()) &&
                !Character.isJavaIdentifierPart(name.charAt(OPERATOR.length()))) { // NOI18N
            setFlags(FLAGS_OPERATOR, true);
        }
        if (register) {
            registerInProject();
        }
    }

    private boolean hasFlags(byte mask) {
        return (flags & mask) == mask;
    }
    
    private void setFlags(byte mask, boolean value) {
        if (value) {
            flags |= mask;
        } else {
            flags &= ~mask; 
        }
    }

    public boolean isStatic() {
        return hasFlags(FLAGS_STATIC);
    }
    
    protected void setStatic(boolean value) {
        setFlags(FLAGS_STATIC, value);
    }
    
    private AST findParameterNode(AST node) {
        AST ast = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_PARMLIST);
        if (ast != null) {
            // for K&R-style
            AST ast2 = AstUtil.findSiblingOfType(ast.getNextSibling(), CPPTokenTypes.CSM_KR_PARMLIST);
            if (ast2 != null) {
                ast = ast2;
            }
        }
        return ast;
    }
    
    private void initTemplate(AST node) {
        boolean _template = false, specialization = false;
        switch(node.getType()) {
            case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION: 
            case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION: 
            case CPPTokenTypes.CSM_CTOR_TEMPLATE_DECLARATION: 
            case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:  
                _template = true;
                break;
            case CPPTokenTypes.CSM_TEMPLATE_FUNCTION_DEFINITION_EXPLICIT_SPECIALIZATION:
            case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                _template = true;
                specialization = true;
                break;
        }

        if (_template) {
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
                            _template = false;
                        }
                    }
                }
            }
            
            if (_template) {
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
        setFlags(FLAGS_TEMPLATE, _template);
        if (_template) {
            this.templateParams = TemplateUtils.getTemplateParameters(node.getFirstChild(), this);
        }
    }
    
    protected CharSequence getScopeSuffix() {
        return classTemplateSuffix != null ? classTemplateSuffix : "";
    }

    protected String initName(AST node) {
        return findFunctionName(node);
    }
    
    public CharSequence getDisplayName() {
        return isTemplate() ? CharSequenceKey.create(getName().toString() + templateSuffix) : getName();
    }
    
    public List<CsmTemplateParameter> getTemplateParameters() {
        return templateParams;
    }    
    
    public boolean isVoidParameterList(){
        return hasFlags(FLAGS_VOID_PARMLIST);
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
    
    protected boolean isCStyleStatic() {
        return isStatic() && CsmKindUtilities.isFile(getScope());
    }
    
    protected void registerInProject() {
        if (isCStyleStatic()) {
            // do NOT register in project C-style static funcions!
            return;
        }
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
	    // implicitely calls RepositoryUtils.put()
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
    public CharSequence getName() {
        return name;
    }
    
    protected final void setName(CharSequence name) {
        this.name = QualifiedNameCache.getManager().getString(name);
    }
    
    public CharSequence getQualifiedName() {
        CsmScope scope = getScope();
        if( (scope instanceof CsmNamespace) || (scope instanceof CsmClass) ) {
            CharSequence scopeQName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
            if( scopeQName != null && scopeQName.length() > 0 ) {
                return CharSequenceKey.create(scopeQName.toString() + getScopeSuffix() + "::" + getQualifiedNamePostfix()); // NOI18N
            }
        }
        return getName();
    }
    
    public CharSequence[] getRawName() {
        return rawName;
    }
    
    @Override
    public CharSequence getUniqueNameWithoutPrefix() {
        return getQualifiedName().toString() + (isTemplate() ? templateSuffix : "") + getSignature().toString().substring(getName().length());
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
        if( isCStyleStatic() ) {
            for( CsmDeclaration decl : getContainingFile().getDeclarations() ) {
                if( CsmKindUtilities.isFunctionDefinition(decl) ) {
                    if( getName().equals(decl.getName()) ) {
                        CsmFunctionDefinition fun = (CsmFunctionDefinition) decl;
                        if( getSignature().equals(fun.getSignature())) {
                            return fun;
                        }
                    }
                }
            }
            return null;
        }
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
        return hasFlags(FLAGS_TEMPLATE);
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
    
    private CsmType initReturnType(AST node, CsmScope scope) {
        CsmType ret = null;
        AST token = getTypeToken(node);
        if( token != null ) {
            ret = AstRenderer.renderType(token, getContainingFile());
        }
        if( ret == null ) {
            ret = TypeFactory.createBuiltinType("int", (AST) null, 0,  null/*getAst().getFirstChild()*/, getContainingFile()); // NOI18N
        }
        return TemplateUtils.checkTemplateType(ret, scope);
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
        AST ast = findParameterNode(node);
        return AstRenderer.renderParameters(ast, getContainingFile(), this);
    }

    private boolean isVoidParameter(AST node) {
        AST ast = findParameterNode(node);
        return AstRenderer.isVoidParameter(ast);
    }
    
    public Collection<CsmParameter>  getParameters() {
        return _getParameters();
    }
    
    public CsmScope getScope() {
        return _getScope();
    }
    
    public CharSequence getSignature() {
        if( signature == null ) {
            signature = QualifiedNameCache.getManager().getString(createSignature());
        }
        return signature;
    }
    
    public CsmFunction getDeclaration() {
        return this;
    }
    
    public boolean isOperator() {
        return hasFlags(FLAGS_OPERATOR);
    }
    
    public OperatorKind getOperatorKind() {
        OperatorKind out = OperatorKind.NONE;
        if (isOperator()) {
            String strName = getName().toString();
            int start = strName.indexOf(OPERATOR);
            assert start >= 0 : "must have word \"operator\" in name";
            start += OPERATOR.length();
            String signText = strName.substring(start).trim();
            out = OperatorKind.getKindByImage(signText);
        }
        return out;                
    }
    
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
        l.addAll(getParameters());
        return l;
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
    
    @Override
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
     * isConst was originally in MethodImpl;
     * but this methods needs internally in FunctionDefinitionImpl
     * to create proper signature.
     * Therefor it's moved here as a protected method.
     */
    protected boolean isConst() {
        return hasFlags(FLAGS_CONST);
    }
    
    private CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }
    
    private Collection _getParameters() {
        if (this.parameters == null) {
            return Collections.EMPTY_LIST;
        } else {
            Collection<CsmParameter> out = UIDCsmConverter.UIDsToDeclarations(parameters);
            return out;
        }
    }
    
    private void _disposeParameters() {
        if (parameters != null) {
            RepositoryUtils.remove(parameters);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        output.writeUTF(this.name.toString());
        PersistentUtils.writeType(this.returnType, output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.parameters, output, false);
        PersistentUtils.writeStrings(this.rawName, output);
        
        // not null UID
        assert !CHECK_SCOPE || this.scopeUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
        
        PersistentUtils.writeUTF(this.signature, output);
        output.writeByte(flags);
        output.writeUTF(this.getScopeSuffix().toString());
        if (isTemplate()) {
            output.writeUTF(this.templateSuffix.toString());
        }
        PersistentUtils.writeTemplateParameters(templateParams, output);
    }

    public FunctionImpl(DataInput input) throws IOException {
        super(input);
        this.name = QualifiedNameCache.getManager().getString(input.readUTF());
        assert this.name != null;
        this.returnType = PersistentUtils.readType(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.parameters = factory.readUIDCollection(new ArrayList<CsmUID<CsmParameter>>(), input);
        this.rawName = PersistentUtils.readStrings(input, NameCache.getManager());
        
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert !CHECK_SCOPE || this.scopeUID != null;
        this.scopeRef = null;
        
        this.signature = PersistentUtils.readUTF(input);
        if (this.signature != null) {
            this.signature = QualifiedNameCache.getManager().getString(this.signature);
        }
        this.flags = input.readByte();
        this.classTemplateSuffix = NameCache.getManager().getString(input.readUTF());
        if (isTemplate()) {
            this.templateSuffix = NameCache.getManager().getString(input.readUTF());
        }
        this.templateParams = PersistentUtils.readTemplateParameters(input);
    }
}
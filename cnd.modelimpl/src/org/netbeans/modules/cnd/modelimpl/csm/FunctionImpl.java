/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;

/**
 *
 * @param T
 * @author Dmitriy Ivanov, Vladimir Kvashin
 */
public class FunctionImpl<T> extends OffsetableDeclarationBase<T> 
        implements CsmFunction, Disposable, RawNamable, CsmTemplate {
    
    private static final String OPERATOR = "operator"; // NOI18N;
    
    private final CharSequence name;
    private final CsmType returnType;
//    private final Collection<CsmUID<CsmParameter>>  parameters;
    private final FunctionParameterListImpl parameterList;
    private CharSequence signature;
    
    // only one of scopeRef/scopeAccessor must be used 
    private /*final*/ CsmScope scopeRef;// can be set in onDispose or contstructor only
    private CsmUID<CsmScope> scopeUID;
    
    private final CharSequence[] rawName;

    private TemplateDescriptor templateDescriptor = null;
    
    protected final CharSequence classTemplateSuffix;
    
    private static final byte FLAGS_VOID_PARMLIST = 1 << 0;
    private static final byte FLAGS_STATIC = 1 << 1;
    private static final byte FLAGS_CONST = 1 << 2;
    private static final byte FLAGS_OPERATOR = 1 << 3;
    private static final byte FLAGS_INVALID = 1 << 4;
    protected static final int LAST_USED_FLAG_INDEX = 4;
    private byte flags;
    
    private static final boolean CHECK_SCOPE = false;

    public FunctionImpl(AST ast, CsmFile file, CsmScope scope, boolean register, boolean global) throws AstRendererException {
        this(ast, file, null, scope, register, global);
    }

    public FunctionImpl(AST ast, CsmFile file, CsmType type, CsmScope scope, boolean register, boolean global) throws AstRendererException {

        super(ast, file);
        assert !CHECK_SCOPE || (scope != null);
        
        name = QualifiedNameCache.getManager().getString(initName(ast));
        if (name.length()==0) {
            throw new AstRendererException((FileImpl)file, this.getStartOffset(), "Empty function name."); // NOI18N
        }
        rawName = initRawName(ast);
        AST child = ast.getFirstChild();
        if (child != null) {
            setStatic(child.getType() == CPPTokenTypes.LITERAL_static);
        } else {
            System.err.println("function ast " + ast.getText() + " without childs in file " + file.getAbsolutePath());            
        }
        if (!isStatic()) {
            CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(
                               name, true, true, false);
            Iterator<CsmFunction> it = CsmSelect.getStaticFunctions(file, filter);
            while(it.hasNext()){
                CsmFunction fun = it.next();
                if( name.equals(fun.getName()) ) {
                    // we don't check signature here since file-level statics
                    // is C-style construct
                    setStatic(true); 
                    break;
                }
            }
        }
        
        // change scope to file for static methods, but only to prevent 
        // registration in global  namespace
        if(scope instanceof CsmNamespace) {
            if( !NamespaceImpl.isNamespaceScope(this) ) {
                    scope = file;
            }
        }

        _setScope(scope);

        if (global){
            RepositoryUtils.hang(this); // "hang" now and then "put" in "register()"
        } else {
            Utils.setSelfUID(this);
        }
        boolean _const = initConst(ast);
        setFlags(FLAGS_CONST, _const);
        StringBuilder clsTemplateSuffix = new StringBuilder();
        templateDescriptor = createTemplateDescriptor(ast, this, clsTemplateSuffix, global);
        classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        if(type != null) {
            returnType = type;
        } else {
            returnType = initReturnType(ast);
        }

        // set parameters, do it in constructor to have final fields
        this.parameterList = createParameterList(ast, !global);
        if (this.parameterList == null || this.parameterList.isEmpty()) {
            setFlags(FLAGS_VOID_PARMLIST, isVoidParameter(ast));
        } else {
            setFlags(FLAGS_VOID_PARMLIST, false);
        }
        if (name.toString().startsWith(OPERATOR) && 
                (name.length() > OPERATOR.length()) &&
                !Character.isJavaIdentifierPart(name.charAt(OPERATOR.length()))) { // NOI18N
            setFlags(FLAGS_OPERATOR, true);
        }
        if (register) {
            registerInProject();
        }
        if (this.parameterList == null) {
            System.err.println("NO PARAM LIST FOR FUNC:" + name + " at " + AstUtil.getOffsetString(ast) + " in " + file.getAbsolutePath());
        }
    }

    public void setScope(CsmScope scope) {
        unregisterInProject();
        try {
            this._setScope(scope);
        } catch(AstRendererException e) {
            DiagnosticExceptoins.register(e);
        }
        registerInProject();
    }

    private void _setScope(CsmScope scope) throws AstRendererException {
        // for functions declared in bodies scope is CsmCompoundStatement - it is not Identifiable
        if ((scope instanceof CsmIdentifiable)) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (scopeUID != null || scope == null);
        } else {
            this.scopeRef = scope;
        }        
    }

    /**
     * Return true, if this is a definition of function
     * that is declared in some other place
     * (in other words, that is prefixed with class or namespace.
     * Otherwise - for simple functions with body as the one below:
     * void foo() {}
     * returns false
     */
    public boolean isPureDefinition() {
        return getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION;
    }

    protected boolean hasFlags(byte mask) {
        return (flags & mask) == mask;
    }
    
    protected void setFlags(byte mask, boolean value) {
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
    
    protected CharSequence getScopeSuffix() {
        return classTemplateSuffix != null ? classTemplateSuffix : CharSequences.empty();
    }

    protected String initName(AST node) {
        return findFunctionName(node);
    }

    protected CharSequence[] initRawName(AST node) {
        return findFunctionRawName(node);
    }
    
    public CharSequence getDisplayName() {
        return (templateDescriptor != null) ? CharSequences.create((getName().toString() + templateDescriptor.getTemplateSuffix())) : getName(); // NOI18N
    }
    
    public List<CsmTemplateParameter> getTemplateParameters() {
        return (templateDescriptor != null) ? templateDescriptor.getTemplateParameters() : Collections.<CsmTemplateParameter>emptyList();
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
                if (last.getType() == CPPTokenTypes.GREATERTHAN) {
                    AST lastId = null;
                    int level = 0;
                    for (AST token2 = token.getFirstChild(); token2 != null; token2 = token2.getNextSibling()) {
                        int type2 = token2.getType();
                        switch (type2) {
                            case CPPTokenTypes.ID:
                                lastId = token2;
                                break;
                            case CPPTokenTypes.GREATERTHAN:
                                level--;
                                break;
                            case CPPTokenTypes.LESSTHAN:
                                level++;
                                break;
                            default:
                                if (level == 0) {
                                    lastId = null;
                                }
                        }
                    }
                    if (lastId != null) {
                        last = lastId;
                    }
                }
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

    private static CharSequence[] findFunctionRawName(AST ast) {
        if( CastUtils.isCast(ast) ) {
            return CastUtils.getFunctionRawName(ast);
        }
        return AstUtil.getRawNameInChildren(ast);
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
    
    public CharSequence getQualifiedName() {
        CsmScope scope = getScope();
        if( (scope instanceof CsmNamespace) || (scope instanceof CsmClass) || (scope instanceof CsmNamespaceDefinition) ) {
            CharSequence scopeQName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
            if( scopeQName != null && scopeQName.length() > 0 ) {
                return CharSequences.create(scopeQName.toString() + getScopeSuffix() + "::" + getQualifiedNamePostfix()); // NOI18N
            }
        }
        return getName();
    }
    
    public CharSequence[] getRawName() {
        return rawName;
    }
    
    @Override
    public CharSequence getUniqueNameWithoutPrefix() {
        return getQualifiedName().toString() + getSignature().toString().substring(getName().length());
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
            CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(
                               getName(), true, true, false);
            Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(getContainingFile(), filter);
            while(it.hasNext()){
                CsmDeclaration decl = it.next();
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
        if (def == null) {
            uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
            def = findDefinition(prj, uname);
            if (def == null) {
                for (CsmProject lib : prj.getLibraries()) {
                    def = findDefinition(lib, uname);
                    if (def != null) {
                        break;
                    }
                }
            }
            if (def == null && (prj instanceof ProjectBase)) {
                for (CsmProject dependent : ((ProjectBase) prj).getDependentProjects()) {
                    def = findDefinition(dependent, uname);
                    if (def != null) {
                        break;
                    }
                }
            }
        }
        return def;
    }
    
    // method try to find definition in case cast operator definition is declared without scope
    private CsmDeclaration fixCastOperator(CsmProject prj, String uname) {
        int i = uname.indexOf("operator "); // NOI18N
        if (i > 0) {
            String s = uname.substring(i + 9);
            int j = s.lastIndexOf("::"); // NOI18N
            if (j > 0) {
                s = uname.substring(0, i + 9) + " " + s.substring(j + 2); // NOI18N
                return prj.findDeclaration(s);
            }
        }
        return null;
    }

    private CsmFunctionDefinition findDefinition(CsmProject prj, String uname){
        CsmDeclaration res = prj.findDeclaration(uname);
        if (res == null && this.isOperator()) {
            res = fixCastOperator(prj, uname);
        }
        if (res instanceof CsmFunctionDefinition) {
            return (CsmFunctionDefinition)res;
        }
        if (prj instanceof ProjectBase) {
            int parmSize = getParameters().size();
            boolean isVoid = isVoidParameterList();
            String from = uname.substring(0, uname.indexOf('(')+1);
            Collection<CsmOffsetableDeclaration> decls = ((ProjectBase)prj).findDeclarationsByPrefix(from);
            CsmFunctionDefinition candidate = null;
            for(CsmOffsetableDeclaration decl : decls){
                CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
                int candidateParamSize = def.getParameters().size();
                if (!isVoid && parmSize == 0) {
                    if (!ProjectBase.isCppFile(decl.getContainingFile())){
                        return def;
                    }
                }
                if (parmSize == candidateParamSize) {
                    // TODO check overloads
                    if (candidate == null) {
                        candidate = def;
                    } else {
                        return null;
                    }
                }
            }
            return candidate;
        }
        return null;
    }
    
    /**
     * Returns true if this class is template, otherwise false.
     * @return flag indicated if function is template
     */
    public boolean isTemplate() {
        return templateDescriptor != null;
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
        return TemplateUtils.checkTemplateType(ret, FunctionImpl.this);
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
                case CPPTokenTypes.LITERAL_typename:
                case CPPTokenTypes.LITERAL_struct:
                case CPPTokenTypes.LITERAL_class:
                case CPPTokenTypes.LITERAL_union:
                    return token;
                default:
                    if( AstRenderer.isCVQualifier(type) ) {
                        return token;
                    }
            }
        }
        return null;
    }

    private FunctionParameterListImpl createParameterList(AST funAST, boolean isLocal) {
        return FunctionParameterListImpl.create(getContainingFile(), funAST, this, isLocal);
    }
    
    private boolean isVoidParameter(AST node) {
        AST ast = findParameterNode(node);
        return AstRenderer.isVoidParameter(ast);
    }

    public FunctionParameterListImpl  getParameterList() {
        return parameterList;
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
            OperatorKind binaryKind = OperatorKind.getKindByImage(signText, true);
            OperatorKind nonBinaryKind = OperatorKind.getKindByImage(signText, false);
            if (binaryKind != OperatorKind.NONE && nonBinaryKind != OperatorKind.NONE) {
                // select the best
                int nrParams = getNrParameters();
                if (nrParams == 0) {
                    out = nonBinaryKind;
                } else if (nrParams == 1) {
                    if (CsmKindUtilities.isClass(getScope())) {
                        out = binaryKind;
                    } else {
                        out = nonBinaryKind;
                    }
                } else if (nrParams == 2) {
                    out = binaryKind;
                } else {
                    out = nonBinaryKind;
                }
            } else {
                out = (binaryKind != OperatorKind.NONE) ? binaryKind : nonBinaryKind;
            }
        }
        return out;                
    }
    
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
        l.addAll(getParameters());
        return l;
    }
    
    private CharSequence createSignature() {
        // TODO: this fake implementation for Deimos only!
        // we should resolve parameter types and provide
        // kind of canonical representation here
        StringBuilder sb = new StringBuilder(getName());
        appendTemplateSignature(sb);
        InstantiationProviderImpl.appendParametersSignature(getParameters(), sb);
        if( isConst() ) {
            sb.append(" const"); // NOI18N
        }
        return sb;
    }

    private void appendTemplateSignature(StringBuilder sb) {
        List<CsmTemplateParameter> allTemplateParams = getTemplateParameters();
        List<CsmTemplateParameter> params = new ArrayList<CsmTemplateParameter>();
        if(allTemplateParams != null) {
            int inheritedTemplateParametersNumber = (templateDescriptor != null) ? templateDescriptor.getInheritedTemplateParametersNumber() : 0;
            if(allTemplateParams.size() > inheritedTemplateParametersNumber) {
                Iterator<CsmTemplateParameter> iter = allTemplateParams.iterator();
                for (int i = 0; i < inheritedTemplateParametersNumber && iter.hasNext(); i++) {
                    iter.next();
                }
                for ( ;iter.hasNext();) {
                    params.add(iter.next());
                }
            }
        }
        InstantiationProviderImpl.appendTemplateParamsSignature(params, sb);
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
        setFlags(FLAGS_INVALID, true);
    }

    @Override
    public boolean isValid() {
        return !hasFlags(FLAGS_INVALID) && super.isValid();
    }

    private synchronized void onDispose() {
        if (this.scopeRef == null) {
            // restore container from it's UID
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }
    
    private static boolean initConst(AST node) {
        AST token = node.getFirstChild();
        while( token != null &&  token.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
            token = token.getNextSibling();
        }
        while( token != null ) {
            if (AstRenderer.isConstQualifier(token.getType())) {
                return true;
            }
            token = token.getNextSibling();
        }
        return false;
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
    
    private synchronized CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            // this is possible situation when scope is already invalidated (see IZ#154264)
            //assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }
    
    private Collection<CsmParameter> _getParameters() {
        if (this.parameterList == null) {
            return Collections.<CsmParameter>emptyList();
        } else {
            return parameterList.getParameters();
        }
    }

    private int getNrParameters() {
        if (isVoidParameterList() || this.parameterList == null) {
            return 0;
        } else {
            return this.parameterList.getNrParameters();
        }
    }
    
    private void _disposeParameters() {
        if (this.parameterList != null) {
            parameterList.dispose();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        PersistentUtils.writeType(this.returnType, output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        PersistentUtils.writeParameterList(this.parameterList, output);
        PersistentUtils.writeStrings(this.rawName, output);
        
        // not null UID
        assert !CHECK_SCOPE || this.scopeUID != null;
        factory.writeUID(this.scopeUID, output);
        
        PersistentUtils.writeUTF(this.signature, output);
        output.writeByte(flags);
        PersistentUtils.writeUTF(getScopeSuffix(), output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
    }

    public FunctionImpl(DataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;
        this.returnType = PersistentUtils.readType(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.parameterList = (FunctionParameterListImpl) PersistentUtils.readParameterList(input);
        this.rawName = PersistentUtils.readStrings(input, NameCache.getManager());
        
        this.scopeUID = factory.readUID(input);
        // not null UID
        assert !CHECK_SCOPE || this.scopeUID != null;
        this.scopeRef = null;
        
        this.signature = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        this.flags = input.readByte();
        this.classTemplateSuffix = PersistentUtils.readUTF(input, NameCache.getManager());
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
    }
}

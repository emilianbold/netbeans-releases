/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;

/**
 * A class that 
 * 1) represents function template specilaization declaration (without body)
 * 2) acts as a base class for FunctionDefinitionImpl.
 * In other words, it corresponds to function that has a double colon "::" in its name
 * @param T 
 * @author Vladimir Kvashin
 */
public class FunctionImplEx<T>  extends FunctionImpl<T> {

    private CharSequence qualifiedName;
    private static final byte FAKE_QUALIFIED_NAME = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+1);
    private CharSequence[] classOrNspNames;   
    
    protected FunctionImplEx(CharSequence name, CharSequence rawName, CsmScope scope, boolean _static, boolean _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);
    }
    
    public static<T> FunctionImplEx<T> create(AST ast, CsmFile file, CsmScope scope, boolean register, boolean global) throws AstRendererException {
        int startOffset = getStartOffset(ast);
        int endOffset = getEndOffset(ast);
        
        NameHolder nameHolder = NameHolder.createFunctionName(ast);
        CharSequence name = QualifiedNameCache.getManager().getString(nameHolder.getName());
        if (name.length() == 0) {
            DiagnosticExceptoins.register(new AstRendererException((FileImpl) file, startOffset, "Empty function name.")); // NOI18N
            return null;
        }
        CharSequence rawName = initRawName(ast);
        
        boolean _static = AstRenderer.FunctionRenderer.isStatic(ast, file, name);
        boolean _const = AstRenderer.FunctionRenderer.isConst(ast);

        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, false);

        FunctionImplEx<T> functionImplEx = new FunctionImplEx<T>(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);        
        
        temporaryRepositoryRegistration(global, functionImplEx);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionImplEx, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        functionImplEx.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        functionImplEx.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, functionImplEx, file));
        functionImplEx.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, functionImplEx, file, global), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));        
        
        CharSequence[] classOrNspNames = CastUtils.isCast(ast) ?
            getClassOrNspNames(ast) :
            functionImplEx.initClassOrNspNames(ast);
        functionImplEx.setClassOrNspNames(classOrNspNames);        
        
        if (register) {
            postObjectCreateRegistration(register, functionImplEx);
        } else {
            RepositoryUtils.put(functionImplEx);
        }
        nameHolder.addReference(file, functionImplEx);
        return functionImplEx;
    }

    protected void setClassOrNspNames(CharSequence[] classOrNspNames) {
        this.classOrNspNames = classOrNspNames;
    }
    
    /** @return either class or namespace */
    protected CsmObject findOwner() {
	CharSequence[] cnn = classOrNspNames;
	if( cnn != null ) {
            Resolver resolver = ResolverFactory.createResolver(this);
            try {
                CsmObject obj = resolver.resolve(cnn, Resolver.CLASSIFIER | Resolver.NAMESPACE);
                if (CsmKindUtilities.isClassifier(obj)) {
                    CsmClassifier cls = resolver.getOriginalClassifier((CsmClassifier)obj);
                    if (cls != null) {
                        obj = cls;
                    }
                    if (CsmKindUtilities.isClass(obj)) {
                        return obj;
                    }
                } else if(CsmKindUtilities.isNamespace(obj)) {
                    return obj;
                }
            } finally {
                ResolverFactory.releaseResolver(resolver);
            }
	}
	return null;
    }    


    protected static CharSequence[] getClassOrNspNames(AST ast) {
	assert CastUtils.isCast(ast);
	AST child = ast.getFirstChild();
        if (child != null && child.getType() == CPPTokenTypes.LITERAL_template) {
            child = AstRenderer.skipTemplateSibling(child);
        }
        child = AstRenderer.getFirstSiblingSkipInline(child);
        child = AstRenderer.getFirstSiblingSkipQualifiers(child);
	if( child != null && child.getType() == CPPTokenTypes.ID ) {
	    AST next = child.getNextSibling();
	    if( next != null && next.getType() == CPPTokenTypes.LESSTHAN ) {
                next = AstRenderer.skipTemplateParameters(next);
	    }
	    if( next != null && next.getType() == CPPTokenTypes.SCOPE ) {
		List<CharSequence> l = new ArrayList<CharSequence>();
                APTStringManager manager = NameCache.getManager();
		l.add(manager.getString(AstUtil.getText(child)));
		begin:
		for( next = next.getNextSibling(); next != null; next = next.getNextSibling() ) {
		    switch( next.getType() ) {
			case CPPTokenTypes.ID:
			    l.add(manager.getString(AstUtil.getText(next)));
                            break;
			case CPPTokenTypes.SCOPE:
			    break; // do nothing
			default:
			    break begin;
		    }
		}
		return  l.toArray(new CharSequence[l.size()]);
	    }
	}
	return null;
    }

    protected CharSequence[] initClassOrNspNames(AST node) {
        //qualified id
        AST qid = AstUtil.findMethodName(node);
        if( qid == null ) {
            return null;
        }
        int cnt = qid.getNumberOfChildren();
        if( cnt >= 1 ) {
            List<CharSequence> l = new ArrayList<CharSequence>();
            APTStringManager manager = NameCache.getManager();
            StringBuilder id = new StringBuilder(""); // NOI18N
            int level = 0;
            for( AST token = qid.getFirstChild(); token != null; token = token.getNextSibling() ) {
                int type2 = token.getType();
                switch (type2) {
                    case CPPTokenTypes.ID:
                        id = new StringBuilder(token.getText());
                        break;
                    case CPPTokenTypes.GREATERTHAN:
                        level--;
                        break;
                    case CPPTokenTypes.LESSTHAN:
                        TemplateUtils.addSpecializationSuffix(token, id, !getInheritedTemplateParameters().isEmpty() ? getInheritedTemplateParameters() : getTemplateParameters(), true);
                        level++;
                        break;
                    case CPPTokenTypes.SCOPE:
                        if (id != null && level == 0) {
                            l.add(manager.getString(id.toString()));
                        }
                        break;
                    default:
                }
            }
            return l.toArray(new CharSequence[l.size()]);
        }
        return null;
    }
    
    @Override
    public CharSequence getQualifiedName() {
        if( qualifiedName == null ) {
            qualifiedName = QualifiedNameCache.getManager().getString(findQualifiedName());
        }
        return qualifiedName;
    }

    protected String findQualifiedName() {
        CsmObject owner = findOwner();
        // check if owner is real or fake
        if(CsmKindUtilities.isQualified(owner)) {
            setFlags(FAKE_QUALIFIED_NAME, false);
            return ((CsmQualifiedNamedElement) owner).getQualifiedName().toString() + (!CsmKindUtilities.isSpecialization(owner) ? getScopeSuffix() : "") + "::" + getQualifiedNamePostfix(); // NOI18N
        }
        setFlags(FAKE_QUALIFIED_NAME, true);
        CharSequence[] cnn = classOrNspNames;
        CsmNamespaceDefinition nsd = findNamespaceDefinition();
        StringBuilder sb = new StringBuilder();
        if( nsd != null ) {
            sb.append(nsd.getQualifiedName());
        }
        if( cnn != null ) {
            for (int i = 0; i < cnn.length; i++) {
                if( sb.length() > 0 ) {
                    sb.append("::"); // NOI18N
                }
                sb.append(cnn[i]);
            }
        }
        if( sb.length() == 0 ) {
            sb.append("unknown>"); // NOI18N
        }
        sb.append("::"); // NOI18N
        sb.append(getQualifiedNamePostfix());
        return sb.toString();
    }

    @Override
    protected boolean registerInProject() {
        boolean out = super.registerInProject();
        // if this funtion couldn't find it's FQN => register it as fake and
        // come back later on for registration (see fixFakeRegistration with null ast)
        if( hasFlags(FAKE_QUALIFIED_NAME) ) {
            ((FileImpl) getContainingFile()).onFakeRegisration(this, null);
        }
        return out;
    }
    
    public final boolean fixFakeRegistration(boolean projectParsedMode, AST fixFakeRegistrationAst) {
        boolean fixed = false;
        if (fixFakeRegistrationAst != null) {
            CsmObject owner = findOwner();
            if (CsmKindUtilities.isClass(owner)) {
                CsmClass cls = (CsmClass) owner;
                boolean _static = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_static);
                boolean _extern = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_extern);
                for (CsmMember member : cls.getMembers()) {
                    if (member.isStatic() && member.getName().equals(getName())) {
                        FileImpl aFile = (FileImpl) getContainingFile();
                        aFile.getProjectImpl(true).unregisterDeclaration(this);
                        aFile.removeDeclaration(this);
                        NameHolder nameHolder = NameHolder.createFunctionName(fixFakeRegistrationAst);
                        VariableDefinitionImpl var = VariableDefinitionImpl.create(fixFakeRegistrationAst, getContainingFile(), getReturnType(), nameHolder, _static, _extern);
                        aFile.addDeclaration(var);
                        fixFakeRegistrationAst = null;
                        return true;
                    }
                }
            } else if (CsmKindUtilities.isNamespace(owner)) {
                boolean _static = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_static);
                boolean _extern = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_extern);
                CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                         CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.VARIABLE, CsmDeclaration.Kind.VARIABLE_DEFINITION),
                         CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(((CsmNamespace)owner), filter);
                while (it.hasNext()) {
                    CsmDeclaration decl = it.next();
                    if (CsmKindUtilities.isExternVariable(decl) && decl.getName().equals(getName())) {
                        FileImpl aFile = (FileImpl) getContainingFile();
                        aFile.getProjectImpl(true).unregisterDeclaration(this);
                        aFile.removeDeclaration(this);
                        NameHolder nameHolder = NameHolder.createFunctionName(fixFakeRegistrationAst);
                        VariableDefinitionImpl var = VariableDefinitionImpl.create(fixFakeRegistrationAst, getContainingFile(), getReturnType(), nameHolder, _static, _extern);
                        aFile.addDeclaration(var);
                        fixFakeRegistrationAst = null;
                        return true;
                    }
                }
            }
            if (projectParsedMode) {
                try {
                    FileImpl aFile = (FileImpl) getContainingFile();
                    FunctionImpl<?> fi = FunctionImpl.create(fixFakeRegistrationAst, getContainingFile(), null, this.getScope(),true);
                    fixFakeRegistrationAst = null;
                    aFile.getProjectImpl(true).unregisterDeclaration(this);
                    aFile.removeDeclaration(this);
                    fi.registerInProject();
                    aFile.addDeclaration(fi);
                    fixed = true;
                    if (NamespaceImpl.isNamespaceScope(fi)) {
                        if (CsmKindUtilities.isNamespace(this.getScope())) {
                            ((NamespaceImpl) this.getScope()).addDeclaration(fi);
                        }
                    }
                } catch (AstRendererException e) {
                    DiagnosticExceptoins.register(e);
                }
            }
        } else {
            CharSequence newQname = QualifiedNameCache.getManager().getString(findQualifiedName());
            if (!newQname.equals(qualifiedName)) {
                ProjectBase aProject = ((FileImpl)getContainingFile()).getProjectImpl(true);
                aProject.unregisterDeclaration(this);
                this.cleanUID();
                qualifiedName = newQname;
                registerInProject();
                fixed = true;
            }
        }
        return fixed;
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition() {
        CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(Kind.NAMESPACE_DEFINITION);
        return findNamespaceDefinition(CsmSelect.getDeclarations(getContainingFile(), filter), filter);
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition(Iterator<CsmOffsetableDeclaration> it, CsmFilter filter) {
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (decl.getStartOffset() > this.getStartOffset()) {
                break;
            }
            if (decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION) {
                if (this.getEndOffset() < decl.getEndOffset()) {
                    CsmNamespaceDefinition nsdef = (CsmNamespaceDefinition) decl;
                    CsmNamespaceDefinition inner = findNamespaceDefinition(CsmSelect.getDeclarations(nsdef, filter), filter);
                    return (inner == null) ? nsdef : inner;
                }
            }
        }
        return null;
    }    
    
    public static boolean isFakeFunction(CsmObject declaration) {
        if (declaration instanceof FunctionImplEx<?>) {
            return FunctionImplEx.class.equals(declaration.getClass()) && ((FunctionImplEx)declaration).hasFlags(FAKE_QUALIFIED_NAME);
        } else {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        // can be null
        PersistentUtils.writeUTF(this.qualifiedName, output);
        PersistentUtils.writeStrings(this.classOrNspNames, output);
    }
    
    public FunctionImplEx(RepositoryDataInput input) throws IOException {
	super(input);
        // can be null
        this.qualifiedName = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        this.classOrNspNames = PersistentUtils.readStrings(input, NameCache.getManager());
    }
}

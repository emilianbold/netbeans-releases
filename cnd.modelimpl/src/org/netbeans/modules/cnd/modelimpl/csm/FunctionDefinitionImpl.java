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

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import java.util.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * @author Vladimir Kvasihn
 */
public class FunctionDefinitionImpl<T> extends FunctionImplEx<T> implements CsmFunctionDefinition {

    private CsmUID<CsmFunction> declarationUID;
    private CsmCompoundStatement body;
    private int parseCount;

    protected FunctionDefinitionImpl(CharSequence name, CharSequence rawName, CsmScope scope, boolean _static, boolean _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);
    }
    
    public static<T> FunctionDefinitionImpl<T> create(AST ast, CsmFile file, CsmScope scope, boolean global) throws AstRendererException {
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

        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, true);

        FunctionDefinitionImpl<T> functionDefinitionImpl = new FunctionDefinitionImpl<T>(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);        
        
        temporaryRepositoryRegistration(global, functionDefinitionImpl);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionDefinitionImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        functionDefinitionImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        functionDefinitionImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, functionDefinitionImpl, file));
        functionDefinitionImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, functionDefinitionImpl, file, global), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));        
        
        CharSequence[] classOrNspNames = CastUtils.isCast(ast) ?
            getClassOrNspNames(ast) :
            functionDefinitionImpl.initClassOrNspNames(ast);
        functionDefinitionImpl.setClassOrNspNames(classOrNspNames);        

        CsmCompoundStatement body = AstRenderer.findCompoundStatement(ast, file, functionDefinitionImpl);
        if (body == null) {
            throw new AstRendererException((FileImpl)file, startOffset,
                    "Null body in method definition."); // NOI18N
        }        
        functionDefinitionImpl.setCompoundStatement(body);
        
        postObjectCreateRegistration(global, functionDefinitionImpl);
        nameHolder.addReference(file, functionDefinitionImpl);
        return functionDefinitionImpl;
    }

    protected void setCompoundStatement(CsmCompoundStatement body) {
        this.body = body;
    }    

    @Override
    public void dispose() {
        super.dispose();
        if (body instanceof Disposable) {
            ((Disposable) body).dispose();
        }
    }

    @Override
    public CsmCompoundStatement getBody() {
        return body;
    }

    @Override
    public CsmFunction getDeclaration() {
        CsmFunction declaration = _getDeclaration();
        if (declaration == null || FunctionImplEx.isFakeFunction(declaration)) {
            int newCount = FileImpl.getParseCount();
            if (newCount == parseCount) {
                return declaration;
            }
            _setDeclaration(null);
            declaration = findDeclaration();
            _setDeclaration(declaration);
            parseCount = newCount;
        }
        return declaration;
    }

    private CsmFunction _getDeclaration() {
        CsmFunction decl = UIDCsmConverter.UIDtoDeclaration(this.declarationUID);
        // null object is OK here, because of changed cached reference
        return decl;
    }

    private void _setDeclaration(CsmFunction decl) {
        this.declarationUID = UIDCsmConverter.declarationToUID(decl);
        assert this.declarationUID != null || decl == null;
    }

    // method try to find declaration in case class have exactly one cast operator with desired name
    private CsmDeclaration fixCastOperator(CsmClass owner) {
        CsmDeclaration candidate = null;
        String s1 = getName().toString();
        int i1 = s1.lastIndexOf("::"); // NOI18N
        if (i1 > 0) {
            s1 = "operator  " + s1.substring(i1 + 2); // NOI18N
        }
        Iterator<CsmMember> it = CsmSelect.getClassMembers(owner,
                CsmSelect.getFilterBuilder().createNameFilter("operator", false, true, false)); // NOI18N
        while (it.hasNext()) {
            CsmMember m = it.next();
            String s2 = m.getName().toString();
            int i2 = s2.lastIndexOf("::"); // NOI18N
            if (i2 > 0) {
                s2 = "operator  " + s2.substring(i2 + 2); // NOI18N
            }
            if (s1.equals(s2)) {
                if (candidate == null) {
                    candidate = m;
                } else {
                    candidate = null;
                    break;
                }
            }
        }
        return candidate;
    }

    private CsmFunction findDeclaration() {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        Collection<? extends CsmDeclaration> prjDecls = getContainingFile().getProject().findDeclarations(uname);
        if (prjDecls.isEmpty()) {
            uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
            prjDecls = getContainingFile().getProject().findDeclarations(uname);
        }
        Collection<CsmDeclaration> decls = new ArrayList<CsmDeclaration>(1);
        if (prjDecls.isEmpty()) {
            CsmObject owner = findOwner();
            if(owner == null) {
                owner = CsmBaseUtilities.getFunctionClassByQualifiedName(this);
            }
            if (CsmKindUtilities.isClass(owner)) {
                Iterator<CsmMember> it = CsmSelect.getClassMembers((CsmClass) owner,
                        CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                decls = findByNameAndParamsNumber(it, getName(), getParameters().size());
                if (decls.isEmpty() && isOperator()) {
                    CsmDeclaration cast = fixCastOperator((CsmClass)owner);
                    if (cast != null) {
                        decls.add(cast);
                    }
                }
            } else if (CsmKindUtilities.isNamespace(owner)) {
                Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(((CsmNamespace) owner),
                        CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                decls = findByNameAndParamsNumber(it, getName(), getParameters().size());
            }
        } else {
            decls = findByNameAndParamsNumber(prjDecls.iterator(), getName(), getParameters().size());
        }
        CsmFunction decl = chooseDeclaration(decls);
        return decl;
    }

    private Collection<CsmDeclaration> findByNameAndParamsNumber(Iterator<? extends CsmObject> declarations, CharSequence name, int paramsNumber) {
        Collection<CsmDeclaration> out = new ArrayList<CsmDeclaration>(1);
        Collection<CsmDeclaration> best = new ArrayList<CsmDeclaration>(1);
        Collection<CsmDeclaration> otherVisible = new ArrayList<CsmDeclaration>(1);
        for (Iterator<? extends CsmObject> it = declarations; it.hasNext();) {
            CsmObject o = it.next();
            if (CsmKindUtilities.isFunction(o)) {
                CsmFunction decl = (CsmFunction) o;
                if (decl.getName().equals(name)) {
                    if (decl.getParameters().size() == paramsNumber) {
                        if (!FunctionImplEx.isFakeFunction(decl)) {
                            if (FunctionImpl.isObjectVisibleInFile(getContainingFile(), decl)) {
                                best.add(decl);
                                continue;
                            }
                        }
                        out.add(decl);
                    } else {
                        if (!FunctionImplEx.isFakeFunction(decl)) {
                            if (FunctionImpl.isObjectVisibleInFile(getContainingFile(), decl)) {
                                otherVisible.add(decl);
                            }
                            out.add(decl);
                        }
                    }
                }
            }
        }
        if (!best.isEmpty()) {
            out = best;
        } else if (!otherVisible.isEmpty()) {
            out = otherVisible;
        }
        return out;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    @Override
    protected String findQualifiedName() {
        CsmFunction declaration = _getDeclaration();
        if (declaration != null) {
            return declaration.getQualifiedName().toString();
        }
        return super.findQualifiedName();
    }

    @Override
    public CsmScope getScope() {
        return getContainingFile();
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }

    @Override
    public CsmFunctionDefinition getDefinition() {
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeCompoundStatement(this.body, output);

        // save cached declaration
        UIDObjectFactory.getDefaultFactory().writeUID(this.declarationUID, output);
    }

    public FunctionDefinitionImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);

        // read cached declaration
        this.declarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}

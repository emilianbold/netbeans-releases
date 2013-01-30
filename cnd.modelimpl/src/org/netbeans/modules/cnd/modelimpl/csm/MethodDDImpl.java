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

import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.CompoundStatementImpl.CompoundStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilderContainer;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * Method, which contains it's body right at throws POD (point of declaration)
 * @author Vladimir Kvasihn
 */
public class MethodDDImpl<T> extends MethodImpl<T> implements CsmFunctionDefinition {

    private CsmCompoundStatement body;
    
    protected MethodDDImpl(CharSequence name, CharSequence rawName, CsmClass cls, CsmVisibility visibility,  boolean _virtual, boolean _explicit, boolean _static, boolean _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, cls, visibility, _virtual, _explicit, _static, _const, file, startOffset, endOffset, global);
    }

    public static<T> MethodDDImpl<T> create(AST ast, final CsmFile file, FileContent fileContent, ClassImpl cls, CsmVisibility visibility, boolean global) throws AstRendererException {
        CsmScope scope = cls;
        
        int startOffset = getStartOffset(ast);
        int endOffset = getEndOffset(ast);
        
        NameHolder nameHolder = NameHolder.createFunctionName(ast);
        CharSequence name = QualifiedNameCache.getManager().getString(nameHolder.getName());
        if (name.length() == 0) {
            AstRendererException.throwAstRendererException((FileImpl) file, ast, startOffset, "Empty function name."); // NOI18N
        }
        CharSequence rawName = initRawName(ast);
        
        boolean _static = AstRenderer.FunctionRenderer.isStatic(ast, file, fileContent, name);
        boolean _const = AstRenderer.FunctionRenderer.isConst(ast);
        boolean _virtual = false;
        boolean _explicit = false;
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_static:
                    _static = true;
                    break;
                case CPPTokenTypes.LITERAL_virtual:
                    _virtual = true;
                    break;
                case CPPTokenTypes.LITERAL_explicit:
                    _explicit = true;
                    break;
            }
        }
        
        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, true);

        MethodDDImpl<T> methodDDImpl = new MethodDDImpl<T>(name, rawName, cls, visibility, _virtual, _explicit, _static, _const, file, startOffset, endOffset, global);        
        temporaryRepositoryRegistration(global, methodDDImpl);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, methodDDImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        methodDDImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        methodDDImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, methodDDImpl, file));
        methodDDImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, methodDDImpl, file, fileContent), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));
        CsmCompoundStatement body = AstRenderer.findCompoundStatement(ast, file, methodDDImpl);
        if (body == null) {
            throw AstRendererException.throwAstRendererException((FileImpl)file, ast, startOffset,
                    "Null body in method definition."); // NOI18N
        }        
        methodDDImpl.setCompoundStatement(body);

        postObjectCreateRegistration(global, methodDDImpl);
        nameHolder.addReference(fileContent, methodDDImpl);
        return methodDDImpl;
    }

    protected void setCompoundStatement(CsmCompoundStatement body) {
        this.body = body;
    }
    
    @Override
    public CsmFunction getDeclaration() {
        return this;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (body instanceof Disposable) {
            ((Disposable)body).dispose();
        }
    }

    @Override
    public CsmCompoundStatement getBody() {
        return body;
    }
    
    @Override
    public Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }
    
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }
    
    public static class MethodDDBuilder extends MethodBuilder implements StatementBuilderContainer {

        private CompoundStatementBuilder bodyBuilder;
        
        public void setBodyBuilder(CompoundStatementBuilder builder) {
            bodyBuilder = builder;
        }

        public CompoundStatementBuilder getBodyBuilder() {
            return bodyBuilder;
        }
        
        @Override
        public MethodDDImpl create() {
            CsmClass cls = (CsmClass) getScope();
            boolean _virtual = false;
            boolean _explicit = false;


            MethodDDImpl method = new MethodDDImpl(getName(), getRawName(), cls, getVisibility(), _virtual, _explicit, isStatic(), isConst(), getFile(), getStartOffset(), getEndOffset(), true);
            temporaryRepositoryRegistration(true, method);

            StringBuilder clsTemplateSuffix = new StringBuilder();
            //TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionImpl, clsTemplateSuffix, global);
            //CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);

            //functionImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
            if(getTemplateDescriptorBuilder() != null) {
                method.setTemplateDescriptor(getTemplateDescriptor(), NameCache.getManager().getString(CharSequences.create(""))); // NOI18N
            }

            method.setReturnType(getType());
            ((FunctionParameterListBuilder)getParametersListBuilder()).setScope(method);
            method.setParameters(((FunctionParameterListBuilder)getParametersListBuilder()).create(),
                    true);

            postObjectCreateRegistration(true, method);
            getNameHolder().addReference(getFileContent(), method);

            addDeclaration(method);
            
            getBodyBuilder().setScope(method);
            method.setCompoundStatement(getBodyBuilder().create());

            postObjectCreateRegistration(true, method);
            getNameHolder().addReference(getFileContent(), method);
            
//            addMember(method);
            
            return method;
        }        

        @Override
        public void addStatementBuilder(StatementBuilder builder) {
            assert builder instanceof CompoundStatementBuilder;
            setBodyBuilder((CompoundStatementBuilder)builder);
        }

    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeCompoundStatement(this.body, output);
    }
    
    public MethodDDImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);
    }     
}

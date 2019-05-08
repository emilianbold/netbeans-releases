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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public final class FriendFunctionDDImpl  extends FunctionDDImpl<CsmFriendFunction> implements CsmFriendFunction {
    private final CsmUID<CsmClass> friendClassUID;

    protected FriendFunctionDDImpl(CharSequence name, CharSequence rawName, CsmScope scope, CsmClass cls, boolean _static, FunctionImpl.CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);
        friendClassUID = UIDs.get(cls);
    }

    public static FriendFunctionDDImpl create(AST ast, final CsmFile file, FileContent fileContent, CsmClass cls, CsmScope scope, boolean global) throws AstRendererException {

        int startOffset = getStartOffset(ast);
        int endOffset = getEndOffset(ast);

        NameHolder nameHolder = NameHolder.createFunctionName(ast);
        CharSequence name = QualifiedNameCache.getManager().getString(nameHolder.getName());
        if (name.length() == 0) {
            throw AstRendererException.throwAstRendererException((FileImpl) file, ast, startOffset, "Empty function name."); // NOI18N
        }
        CharSequence rawName = initRawName(ast);

        boolean _static = AstRenderer.FunctionRenderer.isStatic(ast, file, fileContent, name);
        FunctionImpl.CV_RL _const = AstRenderer.FunctionRenderer.isConst(ast);

        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, true);

        FriendFunctionDDImpl friendFunctionDDImpl = new FriendFunctionDDImpl(name, rawName, scope, cls, _static, _const, file, startOffset, endOffset, global);
        temporaryRepositoryRegistration(ast, global, friendFunctionDDImpl);

        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, friendFunctionDDImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);

        friendFunctionDDImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        friendFunctionDDImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, friendFunctionDDImpl, file));
        friendFunctionDDImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, friendFunctionDDImpl, file, fileContent),
                AstRenderer.FunctionRenderer.isVoidParameter(ast));
        CsmCompoundStatement body = AstRenderer.findCompoundStatement(ast, file, friendFunctionDDImpl);
        if (body == null) {
            throw AstRendererException.throwAstRendererException((FileImpl)file, ast, startOffset,
                    "Null body in method definition."); // NOI18N
        }
        friendFunctionDDImpl.setCompoundStatement(body);

        postObjectCreateRegistration(global, friendFunctionDDImpl);
        nameHolder.addReference(fileContent, friendFunctionDDImpl);
        return friendFunctionDDImpl;
    }

    @Override
    public CsmFunction getReferencedFunction() {
        CsmFunction fun = this;
        if (CsmKindUtilities.isFunctionDeclaration(this)){
            fun = getDefinition();
            if (fun == null){
                fun = this;
            }
        }
        return fun;
    }

    @Override
    public CsmFunctionDefinition getDefinition() {
        return this;
    }

    @Override
    public CsmClass getContainingClass() {
        CsmClass cls = UIDCsmConverter.UIDtoClass(friendClassUID);
        assert (friendClassUID != null) : "null object for UID " + friendClassUID;
        return cls;
    }

    @Override
    public Kind getKind() {
        return Kind.FUNCTION_FRIEND_DEFINITION;
    }

    public static class FriendFunctionDDBuilder extends FunctionDDBuilder {
        private final List<Token> bodyTokens = new ArrayList<>();

        @Override
        public CsmScope getScope() {
            CsmScope scope = super.getScope();
            while (CsmKindUtilities.isClass(scope)) {
               CsmScope newScope = ((CsmClass)scope).getScope();
               if (newScope != null) {
                   scope = newScope;
               } else {
                   break;
               }
            }
            return AstRenderer.FunctionRenderer.getScope(scope, getFile(), isStatic(), true);
        }

        public CsmClass getCls() {
            return (CsmClass) super.getScope();
        }

        @Override
        public FriendFunctionDDImpl create() {
            FriendFunctionDDImpl fun = new FriendFunctionDDImpl(getName(), getRawName(), getScope(), getCls(), isStatic(), CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), isGlobal());
            init(fun);
            return fun;
        }

        protected void init(FunctionDDImpl fun) {
            temporaryRepositoryRegistration(isGlobal(), fun);

            setTemplateDescriptor(fun);
            setReturnType(fun);
            setParameters(fun);
            setBody(fun);

            postObjectCreateRegistration(isGlobal(), fun);
            addReference(fun);

            addDeclaration(fun);
        }

        public void addBodyToken(Token token) {
            if (!APTUtils.isEOF(token)) {
                bodyTokens.add(token);
            }
        }

        public TokenStream getBodyTokenStream() {
            if(bodyTokens.isEmpty()) {
                return null;
            } else {
                return new TokenStream() {

                    int index = 0;

                    @Override
                    public Token nextToken() throws TokenStreamException {
                        if(bodyTokens.size() > index) {
                            index++;
                            return bodyTokens.get(index - 1);
                        } else {
                            return null;
                        }
                    }
                };
            }
        }
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory.getDefaultFactory().writeUID(friendClassUID, output);
    }

    public FriendFunctionDDImpl(RepositoryDataInput input) throws IOException {
        super(input);
        friendClassUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}

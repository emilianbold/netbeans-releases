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
import java.util.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;

/**
 * CsmConstructor implementation
 * @author Vladimir Kvasihn
 */
public final class ConstructorImpl extends MethodImpl<CsmConstructor> implements CsmConstructor {

    protected ConstructorImpl(CharSequence name, CharSequence rawName, CsmClass cls, CsmVisibility visibility,  boolean _virtual, boolean _explicit, boolean _static, boolean _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, cls, visibility, _virtual, _explicit, _static, _const, file, startOffset, endOffset, global);
    }

    public static ConstructorImpl createConstructor(AST ast, ClassImpl cls, CsmVisibility visibility, boolean global) throws AstRendererException {
        CsmScope scope = cls;
        CsmFile file = cls.getContainingFile();
        
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
        
        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, false);

        ConstructorImpl constructorImpl = new ConstructorImpl(name, rawName, cls, visibility, _virtual, _explicit, _static, _const, file, startOffset, endOffset, global);        
        temporaryRepositoryRegistration(global, constructorImpl);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, constructorImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        constructorImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        constructorImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, constructorImpl, file));
        constructorImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, constructorImpl, file, global), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));
        postObjectCreateRegistration(global, constructorImpl);
        nameHolder.addReference(cls.getContainingFile(), constructorImpl);
        return constructorImpl;
    }

    @Override
    public Collection<CsmExpression> getInitializerList() {
        return Collections.<CsmExpression>emptyList();
    }
        
    @Override
    public CsmType getReturnType() {
        return NoType.instance();
    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public ConstructorImpl(RepositoryDataInput input) throws IOException {
        super(input);
    }    
}

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

package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Common base for all expression implementations
 * @author Vladimir Kvashin
 */
public final class ExpressionBase extends OffsetableBase implements CsmExpression {
    
    //private final CsmExpression.Kind kind;
    //private final CsmExpression parent;
    private List<CsmExpression> operands;
    private CsmScope scopeRef;
    private CsmUID<CsmScope> scopeUID;
    
    private List<CsmStatement> lambdas;
    
    private ExpressionBase(AST ast, CsmFile file,/* CsmExpression parent,*/ CsmScope scope) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        //this.parent = parent;
        if( scope != null ) {
	    setScope(scope);
	}
    }

    public static ExpressionBase create(AST ast, CsmFile file,/* CsmExpression parent,*/ CsmScope scope) {
        ExpressionBase expr = new ExpressionBase(ast, file, scope);
        AST token = ast.getFirstChild();
        List<CsmStatement> lambdas = new ArrayList<CsmStatement>();
        while (token != null) {
            if(token.getType() == CPPTokenTypes.CSM_DECLARATION_STATEMENT) {
                lambdas.add(AstRenderer.renderStatement(token, file, scope));
            }
            token = token.getNextSibling();
        }
        if(!lambdas.isEmpty()) {
            expr.setLambdas(lambdas);
        }        
        return expr;
    }

    private ExpressionBase(int startOffset, int endOffset, CsmFile file,/* CsmExpression parent,*/ CsmScope scope) {
        super(file, startOffset, endOffset);
        //this.parent = parent;
        if( scope != null ) {
	    setScope(scope);
	}
    }

    public static ExpressionBase create(int startOffset, int endOffset, CsmFile file,/* CsmExpression parent,*/ CsmScope scope) {
        return new ExpressionBase(startOffset, endOffset, file, scope);
    }

    @Override
    public CsmExpression.Kind getKind() {
        return null; //kind;
    }

    @Override
    public CsmScope getScope() {
        return _getScope();
    }

    @Override
    public List<CsmStatement> getLambdas() {
        if(lambdas == null) {
            return Collections.<CsmStatement>emptyList();
        }
        return lambdas;        
    }
    
    public void setLambdas(List<CsmStatement> lambdas) {
        this.lambdas = lambdas;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        onDispose();
        if (lambdas != null) {
            Utils.disposeAll(lambdas);
        }        
    }

    private synchronized void onDispose() {
        if (this.scopeRef == null) {
            // restore container from it's UID if not directly initialized
            this.scopeRef = this.scopeRef != null ? this.scopeRef : UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }
    
    private synchronized CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }

    protected void setScope(CsmScope scope) {
	// within bodies scope is a statement - it is not Identifiable
        if (scope instanceof CsmIdentifiable) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (scopeUID != null || scope == null);
        } else {
            this.scopeRef = scope;
        }
    }

    @Override
    public List<CsmExpression> getOperands() {
        if( operands == null ) {
            operands = new ArrayList<CsmExpression>(0);
        }
        return Collections.unmodifiableList(operands);
    }
    
    @Override
    public CsmExpression getParent() {
        return null; //parent;
    }

    public static class ExpressionBuilder extends ScopedDeclarationBuilder {

        public ExpressionBase create() {
            ExpressionBase expr = new ExpressionBase(getStartOffset(), getEndOffset(), getFile(), getScope());
            return expr;
        }
    }         
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        //PersistentUtils.writeExpression(this.parent, output);
        //PersistentUtils.writeExpressionKind(this.kind, output);
        PersistentUtils.writeExpressions(this.operands, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
    }  
    
    public ExpressionBase(RepositoryDataInput input) throws IOException {
        super(input);
        //this.parent = PersistentUtils.readExpression(input);
        //this.kind = PersistentUtils.readExpressionKind(input);
        this.operands = PersistentUtils.readExpressions(new ArrayList<CsmExpression>(0), input);
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}

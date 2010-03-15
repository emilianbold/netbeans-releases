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
package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.TokenStream;
import java.lang.ref.SoftReference;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Lazy statements
 *
 * @author Vladimir Kvashin, Nick Krasilnikov
 */
abstract public class LazyStatementImpl extends StatementBase implements CsmScope {

    private SoftReference<List<CsmStatement>> statements = null;

    public LazyStatementImpl(AST ast, CsmFile file, CsmFunction scope) {
        super(ast, file, scope);
        // we need to throw away the compound statement AST under this element
        ast.setFirstChild(null);
    }

    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.COMPOUND;
    }

    public List<CsmStatement> getStatements() {
        if (statements == null) {
            return createStatements();
        } else {
            List<CsmStatement> list = statements.get();
            return (list == null) ? createStatements() : list;
        }
    }

    /**
     * 1) Creates a list of statements
     * 2) If it is created successfully, stores a soft reference to this list
     *	  and returns this list,
     *    otherwise just returns empty list
     */
    public List<CsmStatement> createStatements() {
        List<CsmStatement> list = new ArrayList<CsmStatement>();
        if (renderStatements(list)) {
            statements = new SoftReference<List<CsmStatement>>(list);
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    private boolean renderStatements(List<CsmStatement> list) {
        FileImpl file = (FileImpl) getContainingFile();
        TokenStream stream = file.getTokenStream(getStartOffset(), getEndOffset(), getFirstTokenID(), true);
        if (stream == null) {
            Utils.LOG.log(Level.SEVERE, "Can\'t create compound statement: can\'t create token stream for file {0}", file.getAbsolutePath()); // NOI18N
            return false;
        } else {
            AST resolvedAst = resolveLazyStatement(stream);
            renderStatements(resolvedAst, list);
            return true;
        }
    }

    private void renderStatements(AST ast, List<CsmStatement> list) {
        for (ast = (ast == null ? null : ast.getFirstChild()); ast != null; ast = ast.getNextSibling()) {
            CsmStatement stmt = AstRenderer.renderStatement(ast, getContainingFile(), this);
            if (stmt != null) {
                list.add(stmt);
            }
        }
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        // statements are scope elements
        @SuppressWarnings("unchecked")
        Collection<CsmScopeElement> out = (Collection<CsmScopeElement>) ((List<? extends CsmScopeElement>) getStatements());
        return out;
    }

    abstract protected AST resolveLazyStatement(TokenStream tokenStream);
    abstract protected int/*CPPTokenTypes*/ getFirstTokenID();
    
//    {
//        int flags = CPPParserEx.CPP_CPLUSPLUS;
//        if (!TraceFlags.REPORT_PARSING_ERRORS || TraceFlags.DEBUG) {
//            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
//        }
//        CPPParserEx parser = CPPParserEx.getInstance(getContainingFile().getName().toString(), tokenStream, flags);
//        parser.setLazyCompound(false);
//        parser.compound_statement();
//        AST out = parser.getAST();
//        if(out == null) {
//            parser.function_try_block();
//            out = parser.getAST();
//        }
//        return out;
//    }

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }

    public LazyStatementImpl(DataInput input) throws IOException {
        super(input);
        this.statements = null;
    }
}

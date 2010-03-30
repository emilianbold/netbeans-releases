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

import org.netbeans.modules.cnd.antlr.TokenStream;

import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Lazy try-catch statements
 *
 * @author Nick Krasilnikov
 */
public final class LazyTryCatchStatementImpl extends LazyStatementImpl implements CsmCompoundStatement {

    public LazyTryCatchStatementImpl(AST ast, CsmFile file, CsmFunction scope) {
        super(ast, file, scope);
        assert (ast.getType() == CPPTokenTypes.CSM_TRY_CATCH_STATEMENT_LAZY);
    }

    @Override
    protected AST resolveLazyStatement(TokenStream tokenStream) {
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        if (!TraceFlags.REPORT_PARSING_ERRORS || TraceFlags.DEBUG) {
            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        }
        CPPParserEx parser = CPPParserEx.getInstance(getContainingFile().getName().toString(), tokenStream, flags);
        parser.setLazyCompound(false);
        parser.function_try_block(CsmKindUtilities.isConstructor(getScope()));
        AST out = parser.getAST();
        return out;
    }

    @Override
    protected int/*CPPTokenTypes*/ getFirstTokenID() {
        return CPPTokenTypes.LITERAL_try;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }

    public LazyTryCatchStatementImpl(DataInput input) throws IOException {
        super(input);
    }

}

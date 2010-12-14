/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.parser;

import java.util.List;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.LazyStatementImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Voskresensky
 */
@ServiceProvider(service=CsmParserProvider.class, position=1000)
public final class ParserProviderImpl extends CsmParserProvider {

    @Override
    protected CsmParser create(CsmFile file) {
        if (file instanceof FileImpl) {
            return new Antlr2CppParser((FileImpl)file);
        } else {
            return null;
        }
    }

    private final static class Antlr2CppParser implements CsmParserProvider.CsmParser, CsmParserProvider.CsmParserResult {
        private final FileImpl file;
        private CPPParserEx parser;
        private final int flags;
        private CsmObject parserContainer;
        private AST ast;
        private ConstructionKind kind;
        Antlr2CppParser(FileImpl file) {
            this.file = file;
            int aFlags = CPPParserEx.CPP_CPLUSPLUS;
            if (!TraceFlags.REPORT_PARSING_ERRORS) {
                aFlags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
            }
            this.flags = aFlags;
        }

        @Override
        public void init(CsmObject object, TokenStream ts) {
            assert parser == null : "parser can not be reused " + parser;
            assert object != null;
            assert ts != null;
            parserContainer = object;
            parser = CPPParserEx.getInstance(file.getName().toString(), ts, flags);
        }

        @Override
        public CsmParserProvider.CsmParserResult parse(ConstructionKind kind) {
            this.kind = kind;
            switch (kind) {
                case TRANSLATION_UNIT:
                case NAMESPACE_DEFINITION_BODY:
                case TRY_BLOCK:
                    parser.setLazyCompound(false);
                    parser.function_try_block(CsmKindUtilities.isConstructor((((CsmScopeElement)parserContainer).getScope())));
                    break;
                case COMPOUND_STATEMENT:
                    parser.setLazyCompound(false);
                    parser.compound_statement();
                    break;
                case CLASS_BODY:
                default:
                    assert false: "unexpected parse kind " + kind;
            }
            ast = parser.getAST();
            return this;
        }

        @Override
        public void render(Object... context) {
            switch (kind) {
                case TRY_BLOCK:
                case COMPOUND_STATEMENT:
                    @SuppressWarnings("unchecked")
                    List<CsmStatement> list = (List<CsmStatement>) context[0];
                    ((LazyStatementImpl)parserContainer).renderStatements(ast, list);
                    break;
                default:
                    assert false : "unexpected parse kind " + kind;
            }
        }

        @Override
        public Object getAST() {
            return ast;
        }
    }
    
    private final static class Antrl3FortranParser implements CsmParserProvider.CsmParser, CsmParserProvider.CsmParserResult {

        @Override
        public void init(CsmObject object, TokenStream ts) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CsmParserResult parse(ConstructionKind kind) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void render(Object... context) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getAST() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

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
import org.antlr.runtime.tree.CommonTree;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.LazyStatementImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.fsm.core.DataRenderer;
import org.netbeans.modules.cnd.modelimpl.parser.generated.FortranParser;
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
            if (file.getFileType() == CsmFile.FileType.SOURCE_FORTRAN_FILE) {
                return new Antrl3FortranParser((FileImpl)file);
            }
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
            try {
                this.kind = kind;
                switch (kind) {
                    case TRANSLATION_UNIT:
                        parser.translation_unit();
                        break;
                    case TRY_BLOCK:
                        parser.setLazyCompound(false);
                        parser.function_try_block(CsmKindUtilities.isConstructor((((CsmScopeElement)parserContainer).getScope())));
                        break;
                    case COMPOUND_STATEMENT:
                        parser.setLazyCompound(false);
                        parser.compound_statement();
                        break;
                    case NAMESPACE_DEFINITION_BODY:
                        parser.translation_unit();
                        break;
                    case CLASS_BODY:
                        parser.fix_fake_class_members();
                        break;
                    default:
                        assert false: "unexpected parse kind " + kind;
                }
            } catch (Throwable ex) {
                System.err.println(ex.getClass().getName() + " at parsing file " + file.getAbsolutePath()); // NOI18N
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
                case TRANSLATION_UNIT:
                    if (ast != null) {
                        new AstRenderer(file).render(ast);
                        file.incParseCount();
                    }            
                    break;
                case NAMESPACE_DEFINITION_BODY:
                    FileImpl nsBodyFile = (FileImpl) context[0];
                    NamespaceDefinitionImpl nsDef = (NamespaceDefinitionImpl) context[1];
                    CsmNamespace ns = nsDef.getNamespace();
                    if (ast != null && ns instanceof NamespaceImpl) {
                        new AstRenderer(nsBodyFile).render(ast, (NamespaceImpl) ns, nsDef);
                    }                    
                    break;
                case CLASS_BODY:
                    FileImpl clsBodyFile = (FileImpl) context[0];
                    ClassImpl cls = (ClassImpl) context[1];
                    CsmVisibility visibility = (CsmVisibility) context[2];
                    boolean localClass = (Boolean) context[3];
                    cls.fixFakeRender(clsBodyFile, visibility, ast, localClass);
                    break;
                default:
                    assert false : "unexpected parse kind " + kind;
            }
        }
        
        @Override
        public boolean isEmptyAST() {
            return AstUtil.isEmpty(ast, true);
        }

        @Override
        public void dumpAST() {
            System.err.println("\n");
            System.err.print("AST: ");
            System.err.print(file.getAbsolutePath());
            System.err.print(' ');
            AstUtil.toStream(ast, System.err);
            System.err.println("\n");        }
        
        @Override
        public Object getAST() {
            return ast;
        }

        @Override
        public int getErrorCount() {
            return parser.getErrorCount();
        }
    }
    
    private final static class Antrl3FortranParser implements CsmParserProvider.CsmParser, CsmParserProvider.CsmParserResult {
        private final FileImpl file;
        private FortranParserEx parser;
        private CsmObject parserContainer;
        private FortranParser.program_return ret;
        private ConstructionKind kind;

        Antrl3FortranParser(FileImpl file) {
            this.file = file;
        }
        
        @Override
        public void init(CsmObject object, TokenStream ts) {
            parser = new FortranParserEx(ts);
        }

        @Override
        public CsmParserResult parse(ConstructionKind kind) {
            try {
                this.kind = kind;
                switch (kind) {
                    case TRANSLATION_UNIT:
                            ret = parser.program();
                        break;
                    default:
                        assert false : "unexpected parse kind " + kind;    
                }
            } catch (Exception ex) {
                System.err.println(ex.getClass().getName() + " at parsing file " + file.getAbsolutePath()); // NOI18N
            }
            return this;
        }

        @Override
        public void render(Object... context) {
            switch (kind) {
                case TRANSLATION_UNIT:
                    new DataRenderer(file).render(parser.parsedObjects);
                    file.incParseCount();
                    break;
                default:
                    assert false : "unexpected render kind " + kind;
            }
        }

        @Override
        public boolean isEmptyAST() {
            return ret == null || ret.getTree() == null;
        }
        
        @Override
        public Object getAST() {
            return ret == null ? null : ret.getTree();
        }

        @Override
        public int getErrorCount() {
            return parser.getNumberOfSyntaxErrors();
        }

        @Override
        public void dumpAST() {
            CommonTree tree = (CommonTree) ret.getTree();
            System.err.println(tree);
            System.err.println(tree.getChildren());
        }
    }
}

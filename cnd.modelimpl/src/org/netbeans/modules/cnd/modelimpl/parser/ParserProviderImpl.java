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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.antlr.runtime.tree.CommonTree;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl;
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
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Voskresensky
 */
@ServiceProvider(service=CsmParserProvider.class, position=1000)
public final class ParserProviderImpl extends CsmParserProvider {

    @Override
    protected CsmParser create(CsmParserParameters params) {
        CsmFile file = params.getMainFile();
        if (file instanceof FileImpl) {
            if (file.getFileType() == CsmFile.FileType.SOURCE_FORTRAN_FILE) {
                return new Antrl3FortranParser(params);
            }
            if(!TraceFlags.CPP_PARSER_NEW_GRAMMAR) {
                return new Antlr2CppParser(params);
            } else {
                return new Antlr3CXXParser(params);
            }
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
        
        private Map<Integer, CsmObject> objects = null;
        private final CsmParserProvider.CsmParserParameters params;
        
        Antlr2CppParser(CsmParserProvider.CsmParserParameters params) {
            this.params = params;
            this.file = (FileImpl) params.getMainFile();
            int aFlags = CPPParserEx.CPP_CPLUSPLUS;
            if (!TraceFlags.REPORT_PARSING_ERRORS) {
                aFlags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
            }
            this.flags = aFlags;
        }

        @Override
        public void init(CsmObject object, TokenStream ts, CsmParseCallback callback) {
            assert parser == null : "parser can not be reused " + parser;
            assert object != null;
            assert ts != null;
            parserContainer = object;
            CppParserActionEx cppCallback = (CppParserActionEx)callback;
            if (cppCallback == null) {
                if(TraceFlags.CPP_PARSER_ACTION && ((FileImpl)params.getMainFile()).getParsingFileContent() != null) {
                    cppCallback = new CppParserActionImpl(params);
                } else {
                    cppCallback = new CppParserEmptyActionImpl(file);
                }
            }
            if (cppCallback instanceof CppParserActionImpl) {
                objects = ((CppParserActionImpl)cppCallback).getObjectsMap();
            }
            parser = CPPParserEx.getInstance(file, ts, flags, cppCallback);
        }

        @Override
        public CsmParserProvider.CsmParserResult parse(ConstructionKind kind) {
            try {
                this.kind = kind;
                switch (kind) {
                    case TRANSLATION_UNIT_WITH_COMPOUND:
                        parser.setLazyCompound(false);
                        parser.translation_unit();
                        break;
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
                    case ENUM_BODY:
                        parser.fix_fake_enum_members();
                        break;
                    case CLASS_BODY:
                        parser.fix_fake_class_members();
                        break;
                    default:
                        assert false: "unexpected parse kind " + kind;
                }
            } catch (Throwable ex) {
                System.err.println(ex.getClass().getName() + " at parsing file " + file.getAbsolutePath()); // NOI18N
                ex.printStackTrace(System.err);
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
                    ((LazyStatementImpl)parserContainer).renderStatements(ast, list, objects);
                    break;
                case TRANSLATION_UNIT_WITH_COMPOUND:
                case TRANSLATION_UNIT:
                    if (ast != null) {
                        CsmParserProvider.CsmParserParameters descr = (CsmParserProvider.CsmParserParameters) context[0];
                        FileContent parseFileContent = null;
                        if (descr instanceof FileImpl.ParseDescriptor) {
                            parseFileContent = ((FileImpl.ParseDescriptor)descr).getFileContent();
                        }
                        new AstRenderer(file, parseFileContent, objects).render(ast);
                    }            
                    break;
                case NAMESPACE_DEFINITION_BODY:
                {
                    FileContent fileContent = (FileContent) context[0];
                    FileImpl nsBodyFile = fileContent.getFile();
                    NamespaceDefinitionImpl nsDef = (NamespaceDefinitionImpl) context[1];
                    CsmNamespace ns = nsDef.getNamespace();
                    if (ast != null && ns instanceof NamespaceImpl) {
                        new AstRenderer(nsBodyFile, fileContent, objects).render(ast, (NamespaceImpl) ns, nsDef);
                    }     
                    RepositoryUtils.put(ns);
                    break;
                }
                case CLASS_BODY:
                {
                    FileContent fileContent = (FileContent) context[0];
                    ClassImpl cls = (ClassImpl) context[1];
                    CsmVisibility visibility = (CsmVisibility) context[2];
                    boolean localClass = (Boolean) context[3];
                    cls.fixFakeRender(fileContent, visibility, ast, localClass);
                    if (!localClass) {
                        RepositoryUtils.put(cls);
                    }
                    break;
                }
                case ENUM_BODY:
                    {
                    FileContent fileContent = (FileContent) context[0];
                    EnumImpl enumImpl = (EnumImpl) context[1];
                    boolean localEnum = (Boolean) context[2];
                    enumImpl.fixFakeRender(fileContent, ast, localEnum);
                    if (!localEnum) {
                        RepositoryUtils.put(enumImpl);
                    }                    
                    break;
                }
                default:
                    assert false : "unexpected parse kind " + kind;
            }
            file.incParseCount();
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

        @Override
        public void setErrorDelegate(ParserErrorDelegate delegate) {
        }
    }
    
    private final static class Antrl3FortranParser implements CsmParserProvider.CsmParser, CsmParserProvider.CsmParserResult {
        private final FileImpl file;
        private FortranParserEx parser;
        private CsmObject parserContainer;
        private FortranParser.program_return ret;
        private ConstructionKind kind;
        private final CsmParserProvider.CsmParserParameters params;

        Antrl3FortranParser(CsmParserProvider.CsmParserParameters params) {
            this.file = (FileImpl) params.getMainFile();
            this.params = params;
        }
        
        @Override
        public void init(CsmObject object, TokenStream ts, CsmParseCallback callback) {
            int form = APTLanguageSupport.FLAVOR_FORTRAN_FIXED.equals(file.getFileLanguageFlavor()) ? 
                    FortranParserEx.FIXED_FORM : 
                    FortranParserEx.FREE_FORM;
            
            parser = new FortranParserEx(ts, form);
        }

        @Override
        public CsmParserResult parse(ConstructionKind kind) {
            try {
                this.kind = kind;
                switch (kind) {
                    case TRANSLATION_UNIT_WITH_COMPOUND:
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
                case TRANSLATION_UNIT_WITH_COMPOUND:
                case TRANSLATION_UNIT:
                    new DataRenderer((FileImpl.ParseDescriptor)context[0]).render(parser.parsedObjects);
                    break;
                default:
                    assert false : "unexpected render kind " + kind;
            }
            file.incParseCount();
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

        @Override
        public void setErrorDelegate(ParserErrorDelegate delegate) {
        }
    }

    static Token convertToken(org.antlr.runtime.Token token) {
        //assert token == null || token instanceof Antlr3CXXParser.MyToken;
        return (token instanceof Antlr3CXXParser.MyToken) ? ((Antlr3CXXParser.MyToken) token).t : null;
    }

    private final static class Antlr3CXXParser implements CsmParserProvider.CsmParser, CsmParserProvider.CsmParserResult {
        private final FileImpl file;
        private final CsmParserProvider.CsmParserParameters params;
        private CXXParserEx parser;

        private ConstructionKind kind;
        
        private Map<Integer, CsmObject> objects = null;
        
        Antlr3CXXParser(CsmParserProvider.CsmParserParameters params) {
            this.params = params;
            this.file = (FileImpl) params.getMainFile();
        }

        @Override
        public void init(CsmObject object, TokenStream ts, CsmParseCallback callback) {
            assert parser == null : "parser can not be reused " + parser;
            assert ts != null;
            CXXParserActionEx cppCallback = (CXXParserActionEx)callback;
            if (cppCallback == null) {
                cppCallback = new CXXParserActionImpl(params);
            } else {
                cppCallback.pushFile(file);
            }            
            if (cppCallback instanceof CXXParserActionImpl) {
                objects = ((CXXParserActionImpl) cppCallback).getObjectsMap();
            }            
            org.netbeans.modules.cnd.antlr.TokenBuffer tb = new org.netbeans.modules.cnd.antlr.TokenBuffer(ts);            
            org.antlr.runtime.TokenStream tokens = new MyTokenStream(tb);
            parser = new CXXParserEx(tokens, cppCallback);
        }

        @Override
        public CsmParserProvider.CsmParserResult parse(ConstructionKind kind) {
            try {
                this.kind = kind;
                switch (kind) {
                    case TRANSLATION_UNIT_WITH_COMPOUND:
                    case TRANSLATION_UNIT:
                        parser.compilation_unit();
                        break;
                }
            } catch (Throwable ex) {
                System.err.println(ex.getClass().getName() + " at parsing file " + file.getAbsolutePath()); // NOI18N
                ex.printStackTrace(System.err);
            }
            return this;
        }

        @Override
        public void render(Object... context) {
        }
        
        @Override
        public boolean isEmptyAST() {
            return true;
        }

        @Override
        public void dumpAST() {
        }
        
        @Override
        public Object getAST() {
            return null;
        }

        @Override
        public int getErrorCount() {
            return parser.getNumberOfSyntaxErrors();
        }

        @Override
        public void setErrorDelegate(ParserErrorDelegate delegate) {
            parser.setErrorDelegate(delegate);
        }
        
        private static final class MyToken implements org.antlr.runtime.Token {

            org.netbeans.modules.cnd.antlr.Token t;
            org.antlr.runtime.CharStream s = null;

            public MyToken(org.netbeans.modules.cnd.antlr.Token t) {
                this.t = t;
            }

            @Override
            public String getText() {
                return t.getText();
            }

            @Override
            public void setText(String arg0) {
                t.setText(arg0);
            }

            @Override
            public int getType() {
                return t.getType() == APTTokenTypes.EOF ? -1 : t.getType();
            }

            @Override
            public void setType(int arg0) {
                t.setType(arg0);
            }

            @Override
            public int getLine() {
                return t.getLine();
            }

            @Override
            public void setLine(int arg0) {
                t.setLine(arg0);
            }

            @Override
            public int getCharPositionInLine() {
                return t.getColumn();
            }

            @Override
            public void setCharPositionInLine(int arg0) {
                t.setColumn(arg0);
            }

            @Override
            public int getChannel() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public void setChannel(int arg0) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public int getTokenIndex() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public void setTokenIndex(int arg0) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public org.antlr.runtime.CharStream getInputStream() {
                return s;
            }

            @Override
            public void setInputStream(org.antlr.runtime.CharStream arg0) {
                s = arg0;
            }

        }


        static private class MyTokenStream implements org.antlr.runtime.TokenStream {
            org.netbeans.modules.cnd.antlr.TokenBuffer tb;

            public MyTokenStream(org.netbeans.modules.cnd.antlr.TokenBuffer tb) {
                this.tb = tb;
            }

            @Override
            public org.antlr.runtime.Token LT(int arg0) {
                return new MyToken(tb.LT(arg0));
            }

            @Override
            public void consume() {
                tb.consume();
            }

            @Override
            public int LA(int arg0) {
                return tb.LA(arg0) == APTTokenTypes.EOF ? -1 : tb.LA(arg0);
            }

            @Override
            public int mark() {
                return tb.mark();
            }

            @Override
            public int index() {
                return tb.index();
            }

            @Override
            public void rewind(int arg0) {
                tb.rewind(arg0);
            }

            @Override
            public void rewind() {
                tb.rewind(0);
            }

            @Override
            public void seek(int arg0) {
                tb.seek(arg0);
            }

            @Override
            public org.antlr.runtime.Token get(int arg0) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public org.antlr.runtime.TokenSource getTokenSource() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public String toString(int arg0, int arg1) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public String toString(org.antlr.runtime.Token arg0, org.antlr.runtime.Token arg1) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public void release(int arg0) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public String getSourceName() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public int range() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }
        }

    }        
}

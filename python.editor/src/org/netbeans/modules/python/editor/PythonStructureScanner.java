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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.modules.python.editor.scopes.ScopeInfo;
import org.netbeans.modules.python.editor.scopes.SymInfo;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Str;

/**
 * This class analyzes the structure of a Python parse tree
 * and infers structure (navigation items, folds, etc.)
 *
 * @author Tor Norbye
 */
public class PythonStructureScanner implements StructureScanner {
    public static PythonStructureItem create(SymbolTable scopes, ClassDef def) {
        PythonStructureItem item = new PythonStructureItem(scopes, def, def.getInternalName(), ElementKind.CLASS);

        return item;
    }

    public static PythonStructureItem create(SymbolTable scopes, FunctionDef def) {
        String name = def.getInternalName();
        ElementKind kind = ElementKind.METHOD;
        if ("__init__".equals(name)) { // NOI18N
            kind = ElementKind.CONSTRUCTOR;
        }
        PythonStructureItem item = new PythonStructureItem(scopes, def, name, kind);

        return item;
    }

    public static AnalysisResult analyze(CompilationInfo info) {
        AnalysisResult analysisResult = new AnalysisResult();

        PythonTree root = PythonAstUtils.getRoot(info);
        if (root != null) {
            SymbolTable scopes = PythonAstUtils.getParseResult(info).getSymbolTable();
            StructureVisitor visitor = new StructureVisitor(scopes);
            try {
                visitor.visit(root);
                analysisResult.setElements(visitor.getRoots());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return analysisResult;
    }

    public List<? extends StructureItem> scan(CompilationInfo info) {
        PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
        if (parseResult == null) {
            return Collections.emptyList();
        }

        return parseResult.getStructure().getElements();
    }

    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        PythonParserResult result = PythonAstUtils.getParseResult(info);
        PythonTree root = PythonAstUtils.getRoot(result);
        if (root == null) {
            return Collections.emptyMap();
        }

        //TranslatedSource source = result.getTranslatedSource();
        //
        //AnalysisResult ar = result.getStructure();
        //
        //List<?extends AstElement> elements = ar.getElements();
        //List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        BaseDocument doc = (BaseDocument)info.getDocument();
        if (doc != null) {
            try {
                doc.readLock(); // For Utilities.getRowEnd() access
                FoldVisitor visitor = new FoldVisitor(info, doc);
                visitor.visit(root);
                List<OffsetRange> codeBlocks = visitor.getCodeBlocks();

                Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
                folds.put("codeblocks", codeBlocks); // NOI18N

                return folds;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                doc.readUnlock();
            }
        }
        return Collections.emptyMap();
    }

    public Configuration getConfiguration() {
        return null;
    }

    private static class FoldVisitor extends Visitor {
        private List<OffsetRange> codeBlocks = new ArrayList<OffsetRange>();
        private CompilationInfo info;
        private BaseDocument doc;

        private FoldVisitor(CompilationInfo info, BaseDocument doc) {
            this.info = info;

            this.doc = doc;
        }

        private void addFoldRange(PythonTree node) {
            OffsetRange astRange = PythonAstUtils.getRange(node);

            OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
            if (lexRange != OffsetRange.NONE) {
                try {
                    int startRowEnd = Utilities.getRowEnd(doc, lexRange.getStart());
                    if (startRowEnd < lexRange.getEnd()) {
                        codeBlocks.add(new OffsetRange(startRowEnd, lexRange.getEnd()));
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            addFoldRange(node);

            return super.visitClassDef(node);
        }

        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            addFoldRange(node);

            return super.visitFunctionDef(node);
        }

        @Override
        public Object visitStr(Str node) throws Exception {
            addFoldRange(node);
            return super.visitStr(node);
        }

        public List<OffsetRange> getCodeBlocks() {
            return codeBlocks;
        }
    }

    private static class StructureVisitor extends Visitor {
        List<PythonStructureItem> roots = new ArrayList<PythonStructureItem>();
        List<PythonStructureItem> stack = new ArrayList<PythonStructureItem>();
        SymbolTable scopes;

        StructureVisitor(SymbolTable scopes) {
            this.scopes = scopes;
        }

        private List<PythonStructureItem> getRoots() {
            return roots;
        }

        @Override
        public Object visitClassDef(ClassDef def) throws Exception {
            PythonStructureItem item = new PythonStructureItem(scopes, def, def.getInternalName(), ElementKind.CLASS);
            add(item);

            ScopeInfo scope = scopes.getScopeInfo(def);
            if (scope != null && scope.attributes.size() > 0) {
                for (Map.Entry<String, SymInfo> entry : scope.attributes.entrySet()) {
                    // TODO - sort these puppies? Right now their natural order will be
                    // random (hashkey dependent) instead of by source position or by name
                    SymInfo sym = entry.getValue();
                    if (sym.node != null) {
                        String name = entry.getKey();
                        PythonStructureItem attribute = new PythonStructureItem(scopes, sym.node, name, ElementKind.ATTRIBUTE);
                        item.add(attribute);
                    }
                }
            }

            stack.add(item);
            Object result = super.visitClassDef(def);
            stack.remove(stack.size() - 1);

            return result;
        }

        @Override
        public Object visitFunctionDef(FunctionDef def) throws Exception {
            PythonStructureItem item = create(scopes, def);

            add(item);
            stack.add(item);
            Object result = super.visitFunctionDef(def);
            stack.remove(stack.size() - 1);

            return result;
        }

        private void add(PythonStructureItem child) {
            PythonStructureItem parent = stack.size() > 0 ? stack.get(stack.size() - 1) : null;
            if (parent == null) {
                roots.add(child);
            } else {
                parent.add(child);
            }
        }
    }

    public static class AnalysisResult {
        //private List<?extends AstElement> elements;
        private List<PythonStructureItem> elements;

        private AnalysisResult() {
        }

        //private void setElements(List<?extends AstElement> elements) {
        private void setElements(List<PythonStructureItem> elements) {
            this.elements = elements;
        }

        //public List<?extends AstElement> getElements() {
        public List<PythonStructureItem> getElements() {
            if (elements == null) {
                return Collections.emptyList();
            }
            return elements;
        }
    }
}

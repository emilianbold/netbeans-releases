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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.python.editor.scopes.ScopeInfo;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;

/**
 * Semantic highlighter for Python.
 *
 * @author Tor Norbye
 */
public class PythonSemanticHighlighter implements SemanticAnalyzer {
    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public final synchronized void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo info) throws Exception {
        resume();

        if (isCancelled()) {
            return;
        }

        PythonTree root = PythonAstUtils.getRoot(info);
        if (root == null) {
            return;
        }

        PythonParserResult pr = PythonAstUtils.getParseResult(info);
        SymbolTable symbolTable = pr.getSymbolTable();

        SemanticVisitor visitor = new SemanticVisitor(info, symbolTable);
        visitor.visit(root);
        semanticHighlights = visitor.getHighlights();
    }

    private static class SemanticVisitor extends Visitor {
        private final CompilationInfo info;
        private Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<OffsetRange, Set<ColoringAttributes>>(100);
        private final SymbolTable symbolTable;
        private ScopeInfo scope;

        SemanticVisitor(CompilationInfo info, SymbolTable symbolTable) {
            this.info = info;
            this.symbolTable = symbolTable;
        }

        @Override
        public Object visitModule(Module node) throws Exception {
            ScopeInfo oldScope = scope;
            scope = symbolTable.getScopeInfo(node);
            Object ret = super.visitModule(node);
            scope = oldScope;

            return ret;
        }

        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            ScopeInfo oldScope = scope;
            scope = symbolTable.getScopeInfo(node);
            Object ret = super.visitClassDef(node);
            scope = oldScope;

            return ret;
        }

        public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
            return highlights;
        }

        @Override
        public Object visitFunctionDef(FunctionDef def) throws Exception {
            OffsetRange range = PythonAstUtils.getNameRange(info, def);
            highlights.put(range, ColoringAttributes.METHOD_SET);

            ScopeInfo oldScope = scope;
            scope = symbolTable.getScopeInfo(def);
            Object result = super.visitFunctionDef(def);
            scope = oldScope;
            return result;
        }

        @Override
        public Object visitName(Name node) throws Exception {
            String name = node.getInternalId();
            if (scope != null) {
                if (scope.isUnused(name)) {
                    OffsetRange r = PythonAstUtils.getNameRange(info, node);
                    if (scope.isParameter(name)) {
                        highlights.put(r, EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.PARAMETER));
                    } else {
                        highlights.put(r, EnumSet.of(ColoringAttributes.UNUSED));
                    }
                } else if (scope.isParameter(name)) {
                    OffsetRange r = PythonAstUtils.getNameRange(info, node);
                    highlights.put(r, ColoringAttributes.PARAMETER_SET);
                }
            }

            return super.visitName(node);
        }
    }
}

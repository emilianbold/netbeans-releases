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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.PythonParserResult;
import org.netbeans.modules.python.editor.imports.ImportEntry;
import org.netbeans.modules.python.editor.imports.ImportManager;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.alias;

/**
 * Detect unused imports
 *
 * @author Tor Norbye
 */
public class UnusedImports extends PythonAstRule {
    public UnusedImports() {
    }

    public boolean appliesTo(RuleContext context) {
        FileObject fo = context.compilationInfo.getFileObject();
        return fo == null || !fo.getName().equals("__init__"); // NOI18N
    }

    public Set<Class> getKinds() {
        return Collections.<Class>singleton(Module.class);
    }

    public void run(PythonRuleContext context, List<Hint> result) {
        computeUnusedImports(this, context, result, null);
    }

    private static void computeUnusedImports(UnusedImports detector, PythonRuleContext context, List<Hint> result, Map<PythonTree, List<String>> unused) {
        assert result == null || unused == null; // compute either results or set of unused

        CompilationInfo info = context.compilationInfo;
        PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
        SymbolTable symbolTable = parseResult.getSymbolTable();
        List<ImportEntry> unusedImports = symbolTable.getUnusedImports();
        if (unusedImports.size() == 0) {
            return;
        }
        Map<PythonTree, List<String>> maps = new HashMap<PythonTree, List<String>>();
        for (ImportEntry entry : unusedImports) {
            maps.put(entry.node, new ArrayList<String>());
        }
        for (ImportEntry entry : unusedImports) {
            if (entry.isFromImport) {
                String name = entry.asName != null ? entry.asName : entry.symbol;
                maps.get(entry.node).add(name);
            } else {
                String name = entry.asName != null ? entry.asName : entry.module;
                maps.get(entry.node).add(name);
            }
        }
        for (Map.Entry<PythonTree, List<String>> entry : maps.entrySet()) {
            PythonTree node = entry.getKey();
            List<String> list = entry.getValue();
            if (node instanceof Import) {
                Import imp = (Import)node;
                List<alias> names = imp.getInternalNames();
                if (names != null && names.size() == list.size()) {
                    list.clear();
                }
            } else {
                assert node instanceof ImportFrom;
                ImportFrom imp = (ImportFrom)node;
                List<alias> names = imp.getInternalNames();
                if (names != null && names.size() == list.size()) {
                    list.clear();
                }
            }
        }

        for (Map.Entry<PythonTree, List<String>> entry : maps.entrySet()) {
            PythonTree node = entry.getKey();
            List<String> list = entry.getValue();
            if (list.size() == 0) {
                list = null;
            }
            if (unused != null) {
                unused.put(node, list);
            } else {
                addError(detector, context, node, list, result);
            }
        }
    }

    private static void addError(UnusedImports detector, PythonRuleContext context, PythonTree node, List<String> symbols, List<Hint> result) {
        CompilationInfo info = context.compilationInfo;
        OffsetRange range = PythonAstUtils.getNameRange(info, node);
        range = PythonLexerUtils.getLexerOffsets(info, range);
        if (range != OffsetRange.NONE) {
            List<HintFix> fixList = new ArrayList<HintFix>(3);
            fixList.add(new UnusedFix(detector, context, node, symbols, false));
            fixList.add(new UnusedFix(detector, context, null, null, false)); // Remove All
            fixList.add(new UnusedFix(detector, context, null, null, true)); // Organize
            String message;
            if (symbols != null) {
                message = NbBundle.getMessage(NameRule.class, "UnusedImportSymbols", symbols);
            } else {
                message = NbBundle.getMessage(NameRule.class, "UnusedImport");
            }
            Hint desc = new Hint(detector, message, info.getFileObject(), range, fixList, 2500);
            result.add(desc);
        }
    }

    public String getId() {
        return "UnusedImports"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(NameRule.class, "UnusedImports");
    }

    public String getDescription() {
        return NbBundle.getMessage(NameRule.class, "UnusedImportsDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public boolean showInTasklist() {
        return false; // ? or maybe yes?
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    /**
     * Fix to insert self argument or rename first argument to self
     */
    private static class UnusedFix implements /*PreviewableFix*/ HintFix { // Preview not particularly helpful and clutters menu
        private final UnusedImports detector;
        private final PythonRuleContext context;
        private final PythonTree node;
        private final List<String> symbols;
        private final boolean organizeOnly;

        private UnusedFix(UnusedImports detector, PythonRuleContext context, PythonTree node, List<String> symbols, boolean organizeOnly) {
            this.detector = detector;
            this.context = context;
            this.node = node;
            this.symbols = symbols;
            this.organizeOnly = organizeOnly;
        }

        public String getDescription() {
            if (node == null) {
                if (organizeOnly) {
                    return NbBundle.getMessage(CreateDocString.class, "OrganizeImports");
                } else {
                    return NbBundle.getMessage(CreateDocString.class, "DeleteAllUnused");
                }
            } else if (symbols != null) {
                return NbBundle.getMessage(CreateDocString.class, "UnusedFixSymbols", symbols);
            } else {
                return NbBundle.getMessage(CreateDocString.class, "UnusedFix");
            }
        }

        public boolean canPreview() {
            return true;
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);

            ImportManager importManager = new ImportManager(context.compilationInfo);

            if (node == null) {
                if (organizeOnly) {
                    importManager.cleanup(edits, 0, doc.getLength(), true);
                } else {
                    Map<PythonTree, List<String>> onlyNames = new HashMap<PythonTree, List<String>>();
                    computeUnusedImports(detector, context, null, onlyNames);
                    Set<PythonTree> candidates = onlyNames.keySet();
                    importManager.removeImports(edits, candidates, false, onlyNames);
                }
            } else {
                Set<PythonTree> candidates = Collections.singleton(node);
                Map<PythonTree, List<String>> onlyNames = new HashMap<PythonTree, List<String>>();
                onlyNames.put(node, symbols);
                importManager.removeImports(edits, candidates, false, onlyNames);
            }

            return edits;
        }

        public void implement() throws Exception {
            EditList edits = getEditList();
            edits.apply();
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
    }
}

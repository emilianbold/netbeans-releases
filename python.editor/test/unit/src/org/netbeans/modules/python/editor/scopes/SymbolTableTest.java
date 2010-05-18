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
package org.netbeans.modules.python.editor.scopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.python.editor.AstPath;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.PythonParserResult;
import org.netbeans.modules.python.editor.PythonTestBase;
import org.netbeans.modules.python.editor.PythonUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;

/**
 *
 * @author Tor Norbye
 */
public class SymbolTableTest extends PythonTestBase {

    public SymbolTableTest(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    public void checkScopes(String relFilePath) throws Exception {
        GsfTestCompilationInfo info = getInfo(getTestFile(relFilePath));
        PythonTree root = PythonAstUtils.getRoot(info);
        assertNotNull(root);
        PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
        SymbolTable scopeManager = parseResult.getSymbolTable();
        Map<PythonTree, ScopeInfo> scopes = scopeManager.getScopes();

        StringBuilder sb = new StringBuilder();
        List<PythonTree> nodes = new ArrayList<PythonTree>(scopes.keySet());
        Collections.sort(nodes, PythonUtils.NODE_POS_COMPARATOR);

        for (PythonTree node : nodes) {
            ScopeInfo scope = scopes.get(node);
            String dump = scope.dump();
            sb.append(dump);
            sb.append("\n");
        }

        assertDescriptionMatches(relFilePath, sb.toString(), false, ".scopes");
    }

    public void testScopes1() throws Exception {
        checkScopes("testfiles/datetime.py");
    }

    public void testScopes2() throws Exception {
        checkScopes("testfiles/unusedimports1.py");
    }

    public void testScopes3() throws Exception {
        checkScopes("testfiles/compl.py");
    }

    public void testScopes4() throws Exception {
        checkScopes("testfiles/scope.py");
    }

    public void testScopes5() throws Exception {
        checkScopes("testfiles/mimetypes.py");
    }

    public void testScopes6() throws Exception {
        checkScopes("testfiles/scope2.py");
    }

    public void testScopes7() throws Exception {
        checkScopes("testfiles/unresolved.py");
    }

    public void testScopes8() throws Exception {
        checkScopes("testfiles/ConfigParser.py");
    }

    public void testScopes9() throws Exception {
        checkScopes("testfiles/old-decorators1.py");
    }

    public void testScopes10() throws Exception {
        checkScopes("testfiles/old-decorators2.py");
    }

    public void testScopes11() throws Exception {
        // Generates duplicate arg warning!
        checkScopes("testfiles/formatting.py");
    }

    public void testScopes12() throws Exception {
        // Make sure we handle __all__ generators
        checkScopes("testfiles/tokenize.py");
    }

    public void testScopes13() throws Exception {
        // Make sure we invalidate the __all__ list if we're messing with it
        checkScopes("testfiles/os.py");
    }

    public void testScopes14() throws Exception {
        checkScopes("testfiles/zipfile.py");
    }

    public void testScopes15() throws Exception {
        checkScopes("testfiles/scope3.py");
    }

    public void testScopes16() throws Exception {
        checkScopes("testfiles/unittest.py");
    }

    public void testScopes17() throws Exception {
        checkScopes("testfiles/delete2.py");
    }

    // Unstable
    //public void testScopes17() throws Exception {
    //    checkScopes("testfiles/attributes.py");
    //}
    //
    // Unstable
    //public void testScopes18() throws Exception {
    //    checkScopes("testfiles/decorators.py");
    //}

    public void testStress() throws Exception {
        initializeClassPaths();

        List<FileObject> files = findJythonFiles();

        int MAX_FILES = Integer.MAX_VALUE;

        for (int i = 0; i < files.size() && i < MAX_FILES; i++) {
            // Don't take too long for regular test runs -- just check 20 files
            // Comment out occasionally for a full run
            if (i == 20) {
                break;
            }
            final FileObject fo = files.get(i);
            final GsfTestCompilationInfo info = getInfo(fo);
            final PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
            final SymbolTable scopeManager = parseResult.getSymbolTable();

            System.err.println("Scanning " + FileUtil.getFileDisplayName(fo));

            scopeManager.getErrors();
            scopeManager.getFilename();
            scopeManager.getImports();
            scopeManager.getImportsFrom();
            scopeManager.getMainImports();
            scopeManager.getPublicSymbols();
            scopeManager.getUnresolved(info);
            scopeManager.getUnresolvedNames(info);
            scopeManager.getUnused(false, false);
            scopeManager.getUnused(false, true);
            scopeManager.getUnused(true, false);
            scopeManager.getUnused(true, true);
            scopeManager.getUnusedImports();

            PythonTree root = PythonAstUtils.getRoot(info);
            assertNotNull(FileUtil.getFileDisplayName(fo), root);
            List<PythonTree> nodes = getAllNodes(root);
            final List<PythonTree> defs = new ArrayList<PythonTree>();
            PythonAstUtils.addNodesByType(root, new Class[] { PythonTree.class }, defs);
            new NodeFinder(new AstPathChecker() {
                public void check(AstPath path) {
                    PythonTree scope = PythonAstUtils.getLocalScope(path);
                    PythonTree node = path.leaf();

                    PythonAstUtils.isNameNode(node);
                    String name = PythonAstUtils.getName(node);
                    if (name != null) {
                        scopeManager.findDeclaration(scope, name, false);
                        scopeManager.findDeclaration(scope, name, true);
                        scopeManager.getOccurrences(scope, name, false);
                        scopeManager.getOccurrences(scope, name, true);
                        scopeManager.isPrivate(node, name);
                    }
                    if (scope != null) {
                        scopeManager.getDefinedElements(info, scope, "", NameKind.PREFIX);
                    }
                }
            }).visit(root);

            for (PythonTree node : nodes) {
                scopeManager.isTopLevel(node);
                scopeManager.error("testerror", true, node);
            }
        }
    }

    interface AstPathChecker {
        void check(AstPath path);
    }

    private static class NodeFinder extends Visitor {
        private ArrayList<PythonTree> path = new ArrayList<PythonTree>();
        private AstPathChecker checker;

        private NodeFinder(AstPathChecker checker) {
            this.checker = checker;
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            path.add(node);

            checker.check(new AstPath(path));

            super.traverse(node);
            path.remove(path.size()-1);
        }
    }

    // TODO - test the various other functions -- unused, unresolved, etc!

}

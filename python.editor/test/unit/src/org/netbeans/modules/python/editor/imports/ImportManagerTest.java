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

package org.netbeans.modules.python.editor.imports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.PythonIndex;
import org.netbeans.modules.python.editor.PythonTestBase;
import org.netbeans.modules.python.editor.elements.IndexedElement;
import org.netbeans.modules.python.editor.lexer.PythonTokenId;
import org.netbeans.modules.python.editor.options.CodeStyle.ImportCleanupStyle;
import org.netbeans.modules.python.editor.options.FmtOptions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Tor Norbye
 */
public class ImportManagerTest extends PythonTestBase {

    public ImportManagerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Make sure the test file is indexed
        FileObject fo = getTestFile("testfiles/imports/definitions.py");
        GsfTestCompilationInfo info = getInfo(fo);
        assertNotNull(PythonAstUtils.getRoot(info));
        info.getIndex(PythonTokenId.PYTHON_MIME_TYPE);
        // Force init of the index for both files that we care about
        PythonIndex index = PythonIndex.get(info.getIndex(PythonTokenId.PYTHON_MIME_TYPE), info.getFileObject());
        Set<IndexedElement> classes = index.getClasses("DecimalException", NameKind.EXACT_NAME, PythonIndex.ALL_SCOPE, null, false);
        assertTrue(classes.size() > 0);
    }

    private void applyImports(String testFile, boolean commentOut) throws Exception {
        GsfTestCompilationInfo info = getInfo(getTestFile(testFile));
        info.getDocument();
        JEditorPane target = getPane(info.getText());
        Document doc = target.getDocument();

        DataObject dobj = DataObject.find(info.getFileObject());
        assertNotNull(dobj);
        doc.putProperty(Document.StreamDescriptionProperty, dobj);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        if (commentOut) {
            prefs.put(FmtOptions.cleanupUnusedImports, ImportCleanupStyle.COMMENT_OUT.name());
        } else {
            prefs.put(FmtOptions.cleanupUnusedImports, ImportCleanupStyle.DELETE.name());
        }

        new FixImportsAction().actionPerformed(null, target);

        String text = doc.getText(0, doc.getLength());
        assertDescriptionMatches(testFile, text, true, ".imported");
    }

    private void checkImports(String testFile) throws Exception {
        GsfTestCompilationInfo info = getInfo(getTestFile(testFile));
        Document doc = info.getDocument();
        DataObject dobj = DataObject.find(info.getFileObject());
        assertNotNull(dobj);
        doc.putProperty(Document.StreamDescriptionProperty, dobj);

        List<String> ambiguousSymbols = new ArrayList<String>();
        Set<ImportEntry> unused = new HashSet<ImportEntry>();
        Set<ImportEntry> duplicates = new HashSet<ImportEntry>();
        Map<String, String> defaultLists = new HashMap<String, String>();
        Map<String, List<String>> alternatives = new HashMap<String, List<String>>();

        ImportManager manager = new ImportManager(info, (BaseDocument)doc);
        boolean ambiguous = manager.computeImports(ambiguousSymbols, defaultLists, alternatives, unused, duplicates);

        StringBuilder sb = new StringBuilder();
        sb.append("Requires user interaction: ");
        if (ambiguous) {
            sb.append("Yes");
        } else {
            sb.append("No");
        }
        sb.append("\n");
        if (unused.size() > 0) {
            sb.append("Unused imports:\n");
            List<ImportEntry> unusedList = new ArrayList<ImportEntry>(unused);
            Collections.sort(unusedList);
            for (ImportEntry entry : unusedList) {
                sb.append("    ");
                sb.append(entry.module);
                if (entry.symbol != null) {
                    sb.append(" ");
                    sb.append(entry.symbol);
                }
                if (entry.asName != null) {
                    sb.append(" ");
                    sb.append(entry.asName);
                }
                sb.append("\n");
            }
        }
        if (duplicates.size() > 0) {
            sb.append("Duplicate imports:\n");
            List<ImportEntry> duplicatesList = new ArrayList<ImportEntry>(duplicates);
            Collections.sort(duplicatesList);
            for (ImportEntry entry : duplicatesList) {
                sb.append("    ");
                sb.append(entry.module);
                if (entry.symbol != null) {
                    sb.append(" ");
                    sb.append(entry.symbol);
                }
                if (entry.asName != null) {
                    sb.append(" ");
                    sb.append(entry.asName);
                }
                sb.append("\n");
            }
        }
        if (ambiguousSymbols.size() > 0) {
            sb.append("Unresolved Symbols:\n");
            for (int i = 0; i < ambiguousSymbols.size(); i ++) {
                String symbol = ambiguousSymbols.get(i);
                sb.append("    ");
                sb.append(symbol);
                sb.append("\n");
                String deflt = defaultLists.get(symbol);
                List<String> choices = alternatives.get(symbol);
                for (String choice : choices) {
                    sb.append("        ");
                    if (choice.equals(deflt)) {
                        sb.append("*");
                    }
                    if (choice.startsWith("<html>")) {
                        sb.append("<cannot resolve>");
                    } else {
                        sb.append(choice);
                    }
                    sb.append("\n");
                }
            }
        }

        assertDescriptionMatches(testFile, sb.toString(), false, ".imports");
    }

    private void checkOrganize(String testFile, boolean systemLibsFirst, boolean splitImports, boolean commentOut, boolean sort, boolean separateFrom) throws Exception {
        GsfTestCompilationInfo info = getInfo(getTestFile(testFile));
        Document doc = info.getDocument();
        DataObject dobj = DataObject.find(info.getFileObject());
        assertNotNull(dobj);
        doc.putProperty(Document.StreamDescriptionProperty, dobj);

        EditList edits = new EditList((BaseDocument)doc);
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        if (commentOut) {
            prefs.put(FmtOptions.cleanupUnusedImports, ImportCleanupStyle.COMMENT_OUT.name());
        } else {
            prefs.put(FmtOptions.cleanupUnusedImports, ImportCleanupStyle.DELETE.name());
        }
        prefs.putBoolean(FmtOptions.oneImportPerLine, splitImports);
        prefs.putBoolean(FmtOptions.systemLibsFirst, systemLibsFirst);
        prefs.putBoolean(FmtOptions.sortImports, sort);
        prefs.putBoolean(FmtOptions.separateFromImps, separateFrom);

        ImportManager manager = new ImportManager(info, (BaseDocument)doc);
        manager.rewriteMainImports(edits, Collections.<ImportEntry>emptyList(), Collections.<ImportEntry>emptySet());
        edits.apply();

        String text = doc.getText(0, doc.getLength());
        assertDescriptionMatches(testFile, text, true, ".imported");
    }

    public void testIsImported1() throws Exception {
        GsfTestCompilationInfo info = getInfo(getTestFile("testfiles/imports/imports1.py"));

        /*
        import foo
        import bar as BAR
        import module1, module2, module3
        from module4 import Class1
        from module5 import Class2 as Class3
         */
        assertFalse(new ImportManager(info).isImported("foo", "Whatever"));
        assertTrue(new ImportManager(info).isImported("foo", null));
        assertTrue(new ImportManager(info).isImported("bar", "BAR"));
        assertFalse(new ImportManager(info).isImported("bar", "FOO"));
        assertTrue(new ImportManager(info).isImported("module1", null));
        assertFalse(new ImportManager(info).isImported("module1", "Whatever"));
        assertFalse(new ImportManager(info).isImported("module2", "Whatever"));
        assertTrue(new ImportManager(info).isImported("module4", "Class1"));
        assertFalse(new ImportManager(info).isImported("module4", "Class2"));
        assertFalse(new ImportManager(info).isImported("module5", "Class2"));
        assertTrue(new ImportManager(info).isImported("module5", "Class3"));
    }

//    public void testFixImports1() throws Exception {
//        applyImports("testfiles/imports/imports1.py", false);
//    }
//
//    public void testFixImports2() throws Exception {
//        applyImports("testfiles/imports/imports1.py", true);
//    }
//
//    public void testFixImports3() throws Exception {
//        applyImports("testfiles/imports/imports2.py", true);
//    }
//
//    public void testFixImports4() throws Exception {
//        applyImports("testfiles/imports/imports3.py", true);
//    }
//
//    public void testFixImports5() throws Exception {
//        checkImports("testfiles/imports/imports1.py");
//    }
//
//    public void testFixImports6() throws Exception {
//        checkImports("testfiles/imports/imports2.py");
//    }
//
//    public void testFixImports7() throws Exception {
//        checkImports("testfiles/imports/imports3.py");
//    }
//
//    public void testFixDuplicates1() throws Exception {
//        checkImports("testfiles/imports/duplicates1.py");
//    }
//
//    public void testFixDuplicates2() throws Exception {
//        checkImports("testfiles/imports/duplicates2.py");
//    }
//
//    public void testFixDuplicates2b() throws Exception {
//        applyImports("testfiles/imports/duplicates2.py", true);
//    }
//
//    public void testFixDuplicates3() throws Exception {
//        checkImports("testfiles/imports/duplicates3.py");
//    }
//
//    public void testFixDuplicates3b() throws Exception {
//        applyImports("testfiles/imports/duplicates3.py", true);
//    }
//
//    public void testFixDuplicates4() throws Exception {
//        checkImports("testfiles/imports/duplicates4.py");
//    }
//
//    public void testFixDuplicates4b() throws Exception {
//        applyImports("testfiles/imports/duplicates4.py", true);
//    }
//
//    public void testFixDuplicates5() throws Exception {
//        checkImports("testfiles/imports/duplicates5.py");
//    }
//
//    public void testFixDuplicates5b() throws Exception {
//        applyImports("testfiles/imports/duplicates5.py", true);
//    }
//
//    public void testFixDuplicates6() throws Exception {
//        checkImports("testfiles/imports/duplicates6.py");
//    }
//
//    public void testFixDuplicates6b() throws Exception {
//        applyImports("testfiles/imports/duplicates6.py", true);
//    }
//
//    public void testFixDuplicates7() throws Exception {
//        applyImports("testfiles/imports/duplicates7.py", true);
//    }

    public void testFixOrganize1() throws Exception {
        checkOrganize("testfiles/imports/organize1.py", true, true, false, true, false);
    }

    public void testFixOrganize2() throws Exception {
        checkOrganize("testfiles/imports/organize1.py", true, false, false, true, false);
    }

    public void testFixOrganize3() throws Exception {
        checkOrganize("testfiles/imports/organize1.py", true, true, false, true, false);
    }

    public void testFixOrganize4() throws Exception {
        checkOrganize("testfiles/imports/organize1.py", false, true, true, true, false);
    }

    public void testFixOrganize5() throws Exception {
        checkOrganize("testfiles/imports/organize1.py", true, false, false, true, false);
    }

    public void testFixOrganize6() throws Exception {
        checkOrganize("testfiles/imports/imports1.py", true, true, false, true, false);
    }

    public void testFixOrganize7() throws Exception {
        checkOrganize("testfiles/imports/organize1.py", true, true, false, false, false);
    }

    public void testFixOrganize8() throws Exception {
        checkOrganize("testfiles/imports/organize2.py", true, true, false, true, false);
    }

    public void testSortFutureImports() throws Exception {
        // 156442: Python __future__ imports not sorted properly
        checkOrganize("testfiles/imports/futures.py", false, true, false, true, true);
    }

    public void testFixInit() throws Exception {
        checkOrganize("testfiles/package/subpackage1/__init__.py", true, true, false, true, false);
    }

    public void testFixInit2() throws Exception {
        checkOrganize("testfiles/imports/__init__.py", true, true, false, true, false);
    }

    // TODO - try manually importing zlib - doesn't go to the right place (and check for system libs)
}

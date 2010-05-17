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

package org.netbeans.modules.python.editor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.python.editor.elements.IndexedElement;
import org.netbeans.modules.python.editor.elements.IndexedMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;

/**
 *
 * @author Tor Norbye
 */
public class RstFormatterTest extends PythonTestBase {

    public RstFormatterTest(String name) {
        super(name);
    }

    private IndexedElement getFakeElement(String name, ElementKind kind, String url, String rhs, List<String> parameters) {
        IndexedElement fakeElement;
        if (parameters != null) {
            assert kind == ElementKind.METHOD;
            String clz = "Unknown";
            StringBuilder sb2 = new StringBuilder();
            sb2.append(name);

            sb2.append(";F;0;"); // NOI18N

            boolean first = true;
            for (String s : parameters) {
                if (first) {
                    first = false;
                } else {
                    sb2.append(',');
                }
                sb2.append(s);
            }
            sb2.append(';');
            String signature = sb2.toString();
            fakeElement = new IndexedMethod(name, kind, url, rhs, clz, signature);
        } else {
            String clz = null;
            String signature = null;
            fakeElement = new IndexedElement(name, kind, url, rhs, clz, signature);
        }

        return fakeElement;
    }

    private void checkDocumentation(String testFile, String name, ElementKind kind) throws Exception {
        FileObject fo = getTestFile(testFile);
        String url = FileUtil.toFile(fo).toURI().toURL().toExternalForm();
        String rhs = null;

        IndexedElement fakeElement = getFakeElement(name, kind, url, rhs, null);
        String html = RstFormatter.getDocumentation(fakeElement);

        assertDescriptionMatches(testFile, "<html><body>" + html + "</body></html>", true, ".html");
    }

    public void formatFile(String file) throws Exception {
        if (file.endsWith(".rst")) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>\n"); // NOI18N
            String rst = readFile(getTestFile(file));
            sb.append(RstFormatter.document(rst));
            sb.append("</body></html>\n");

            assertDescriptionMatches(file, sb.toString(), false, ".html");

            return;
        }

        CompilationInfo info = getInfo(file);

        List<PythonTree> result = new ArrayList<PythonTree>();
        PythonTree root = PythonAstUtils.getRoot(info);
        PythonAstUtils.addNodesByType(root, new Class[] { Module.class, FunctionDef.class, ClassDef.class }, result);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>\n"); // NOI18N

        for (PythonTree node : result) {
            PythonTree docNode = PythonAstUtils.getDocumentationNode(node);
            if (docNode != null) {
                String name = "";
                ElementKind kind = ElementKind.OTHER;
                List<String> parameters = null;
                if (node instanceof FunctionDef) {
                    kind = ElementKind.METHOD;
                    FunctionDef func = (FunctionDef)node;
                    name = func.getInternalName();
                    parameters = PythonAstUtils.getParameters(func);
                } else if (node instanceof Module) {
                    name = info.getFileObject().getNameExt();
                    kind = ElementKind.MODULE;
                } else if (node instanceof ClassDef) {
                    kind = ElementKind.CLASS;
                    name = ((ClassDef)node).getInternalName();
                }
                String url = FileUtil.toFile(info.getFileObject()).toURI().toURL().toExternalForm();
                String rhs = null;

                IndexedElement fakeElement = getFakeElement(name, kind, url, rhs, parameters);

                String s = RstFormatter.document(info, node, fakeElement);
                if (s != null && s.length() > 0) {
                    sb.append("<h2 style=\"color: green\">Next Comment</h2>\n"); // NOI18N
                    sb.append(s);
                }
            }
        }
        sb.append("</body></html>\n");

        assertDescriptionMatches(file, sb.toString(), false, ".html");
    }

    public void testExtract1() throws Exception {
        RstFormatter formatter = new RstFormatter();
        BaseDocument doc = getDocument(getTestFile("testfiles/rst/stdtypes.rst"));
        String rst = formatter.extractRst("dict", null, ElementKind.CLASS, doc, null);
        assertTrue(rst, rst.trim().startsWith("Return a new dictionary initialized from "));
    }

    public void testExtract2() throws Exception {
        RstFormatter formatter = new RstFormatter();
        BaseDocument doc = getDocument(getTestFile("testfiles/rst/stdtypes.rst"));
        String rst = formatter.extractRst("hex", null, ElementKind.METHOD, doc, null);
        assertTrue(rst, rst.trim().startsWith("Return a representation of a floating-point number as a hexadecimal"));
    }

    public void testExtract3() throws Exception {
        RstFormatter formatter = new RstFormatter();
        BaseDocument doc = getDocument(getTestFile("testfiles/rst/stdtypes.rst"));
        String rst = formatter.extractRst("encoding", null, ElementKind.ATTRIBUTE, doc, null);
        assertTrue(rst, rst.trim().startsWith("The encoding that this file uses. When Unicode strings are written to a file,"));
    }

    public void testExtract4() throws Exception {
        RstFormatter formatter = new RstFormatter();
        BaseDocument doc = getDocument(getTestFile("testfiles/rst/platform.rst"));
        String[] signatureHolder = new String[1];
        String rst = formatter.extractRst("machine", null, ElementKind.METHOD, doc, signatureHolder);
        assertTrue(rst, rst.trim().startsWith("Returns the machine type, e.g"));
        assertEquals("machine()", signatureHolder[0]);
    }

    public void testExtract5() throws Exception {
        RstFormatter formatter = new RstFormatter();
        BaseDocument doc = getDocument(getTestFile("testfiles/rst/zipfile.rst"));
        String rst = formatter.extractRst("ZIP_STORED", null, ElementKind.ATTRIBUTE, doc, null);
        assertTrue(rst, rst.trim().startsWith("The numeric constant for an uncompressed archive membe"));
    }

    public void testExtract6() throws Exception {
        RstFormatter formatter = new RstFormatter();
        BaseDocument doc = getDocument(getTestFile("testfiles/rst/zipfile.rst"));
        String rst = formatter.extractRst("zipfile", null, ElementKind.MODULE, doc, null);
        assertTrue(rst, rst.trim().startsWith(":synopsis: Read and write ZIP-format archive files"));
    }

    public void testFormatBig1() throws Exception {
        formatFile("testfiles/ConfigParser.py");
    }

    public void testRawStringDoc() throws Exception {
        formatFile("testfiles/rawstringdoc.py");
    }

    public void testGetDoc1() throws Exception {
        checkDocumentation("testfiles/rst/zipfile.rst", "ZIP_STORED", ElementKind.ATTRIBUTE);
    }

    public void testGetDoc2() throws Exception {
        checkDocumentation("testfiles/rst/stdtypes.rst", "dict", ElementKind.CLASS);
    }

    public void testGetDoc3() throws Exception {
        checkDocumentation("testfiles/rst/zipfile.rst", "write", ElementKind.METHOD);
    }

    public void testGetDoc4() throws Exception {
        checkDocumentation("testfiles/rst/stdtypes.rst", "close", ElementKind.METHOD);
    }

    public void testGetDoc5() throws Exception {
        checkDocumentation("testfiles/rst/operator.rst", "__delitem__", ElementKind.METHOD);
    }

    public void testFormatAll1() throws Exception {
        formatFile("testfiles/rst/string.rst");
    }

    public void testFormatAll2() throws Exception {
        formatFile("testfiles/rst/stdtypes.rst");
    }

    public void testFormatAll3() throws Exception {
        formatFile("testfiles/rst/zipfile.rst");
    }

    public void testFormatAll4() throws Exception {
        formatFile("testfiles/rst/platform.rst");
    }

    public void testFormatAll5() throws Exception {
        formatFile("testfiles/rst/smtpd.rst");
    }

    public void testFormatAll6() throws Exception {
        formatFile("testfiles/rst/stub_missing.rst");
    }
}

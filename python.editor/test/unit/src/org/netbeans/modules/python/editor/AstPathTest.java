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

import java.util.Iterator;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;

/**
 *
 * @author Tor Norbye
 */
public class AstPathTest extends PythonTestBase {

    public AstPathTest(String name) {
        super(name);
    }

    protected String annotatePath(AstPath path) {
        StringBuilder sb = new StringBuilder();

        Iterator<PythonTree> it = path.rootToLeaf();
        while (it.hasNext()) {
            PythonTree node = it.next();

            sb.append(node.getClass().getSimpleName());
            sb.append(":");
            OffsetRange range = PythonAstUtils.getRange(node);
            sb.append(range);

            sb.append("\n");
        }

        return sb.toString();
    }

    public AstPath getPath(String relFilePath, String caretLine) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        PythonTree root = PythonAstUtils.getRoot(info);
        int caretOffset = getCaretOffset(info.getText(), caretLine);
        AstPath path = AstPath.get(root, caretOffset);

        return path;
    }

    public void checkPath(String relFilePath, String caretLine) throws Exception {
        AstPath path = getPath(relFilePath, caretLine);
        String pathDesc = annotatePath(path);
        assertDescriptionMatches(relFilePath, pathDesc, true, ".path"); // NOI18N
    }

    public void testGetByCaretOffset() throws Exception {
        String relFilePath = "testfiles/ConfigParser.py";
        String caretLine = "Error.__init__(se^lf, \"Section %r already exists\" % section)";
        checkPath(relFilePath, caretLine);
    }

    public void testGetByNode1() throws Exception {
        String relFilePath = "testfiles/ConfigParser.py";
        String caretLine = "Error.__init__(se^lf, \"Section %r already exists\" % section)";
        AstPath path = getPath(relFilePath, caretLine);

        PythonTree node = path.leaf();
        PythonTree root = path.root();
        AstPath newPath = AstPath.get(root, node);
        String pathDesc = annotatePath(newPath);
        assertDescriptionMatches(relFilePath, pathDesc, true, ".path"); // NOI18N
    }

    public void testGetByNode2() throws Exception {
        String relFilePath = "testfiles/ConfigParser.py";
        String caretLine = "Error.__init__(se^lf, \"Section %r already exists\" % section)";
        AstPath path = getPath(relFilePath, caretLine);

        PythonTree node = path.leafParent();
        PythonTree root = path.root();
        AstPath newPath = AstPath.get(root, node);
        String pathDesc = annotatePath(newPath);
        assertDescriptionMatches(relFilePath, pathDesc, true, ".path"); // NOI18N
    }

    public void testGetTypedAncestor() throws Exception {
        String relFilePath = "testfiles/ConfigParser.py";
        String caretLine = "Error.__init__(se^lf, \"Section %r already exists\" % section)";
        AstPath path = getPath(relFilePath, caretLine);
        ClassDef def = (ClassDef)path.getTypedAncestor(ClassDef.class);
        assertNotNull(def);
        assertEquals("DuplicateSectionError", def.getInternalName());
    }

    public void testGetTypedAncestorFrom() throws Exception {
        String relFilePath = "testfiles/test_scope.py";
        String caretLine = "def meth^od_and_var(self):";
        AstPath path = getPath(relFilePath, caretLine);
        assertEquals(FunctionDef.class, path.leaf().getClass());

        PythonTree parent = path.leafParent();
        assertEquals(ClassDef.class, parent.getClass());

        FunctionDef def = (FunctionDef)path.getTypedAncestor(FunctionDef.class, parent);
        assertNotNull(def);
        assertEquals("test", def.getInternalName());
    }
}

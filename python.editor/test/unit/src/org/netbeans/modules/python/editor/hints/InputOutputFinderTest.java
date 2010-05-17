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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.python.editor.AstPath;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.PythonTestBase;
import org.python.antlr.PythonTree;

/**
 *
 * @author Tor Norbye
 */
public class InputOutputFinderTest extends PythonTestBase {

    public InputOutputFinderTest(String name) {
        super(name);
    }

    InputOutputFinder getFinder(String source) throws Exception {
        String BEGIN = "%<%"; // NOI18N
        String END = "%>%"; // NOI18N
        int sourceStartPos = source.indexOf(BEGIN);
        if (sourceStartPos != -1) {
            source = source.substring(0, sourceStartPos) + source.substring(sourceStartPos+BEGIN.length());
        }

        int caretPos = source.indexOf('^');
        if (caretPos != -1) {
            source = source.substring(0, caretPos) + source.substring(caretPos+1);
        }

        int sourceEndPos = source.indexOf(END);
        if (sourceEndPos != -1) {
            source = source.substring(0, sourceEndPos) + source.substring(sourceEndPos+END.length());
        }

        GsfTestCompilationInfo info = getInfoForText(source, "temp.py");
        assertNotNull(info);

        if (caretPos != -1) {
            info.setCaretOffset(caretPos);
        }

        PythonTree root = PythonAstUtils.getRoot(info);
        assertNotNull(root);

        PythonTree startNode = AstPath.get(root, sourceStartPos).leaf();
        PythonTree endNode = AstPath.get(root, sourceEndPos).leaf();

        List<PythonTree> applicableBlocks = Collections.emptyList();
        InputOutputFinder finder = new InputOutputFinder(startNode, endNode, applicableBlocks);

        PythonTree scope = PythonAstUtils.getLocalScope(AstPath.get(root, sourceStartPos));
        assertNotNull(scope);
        finder.visit(scope);

        return finder;
    }

    public void testCall() throws Exception {
        InputOutputFinder finder = getFinder("foo = 1;\n%<%foo()%>%\nprint foo\n");
        Set<String> inputVars = finder.getInputVars();
        assertEquals(Collections.emptySet(), inputVars);

        Set<String> outputVars = finder.getOutputVars();
        assertEquals(Collections.emptySet(), outputVars);
    }

    public void testVariableRead() throws Exception {
        InputOutputFinder finder = getFinder("x = 1\ny = 2\n%<%print x+y%>%");
        Set<String> inputVars = finder.getInputVars();
        Set<String> expected = new HashSet<String>() {{
            add("x");
            add("y");
        }};
        assertEquals(expected, inputVars);

        Set<String> outputVars = finder.getOutputVars();
        assertEquals(Collections.emptySet(), outputVars);
    }

    public void testVariableWrite() throws Exception {
        InputOutputFinder finder = getFinder("%<%y = 1%>%\nprint y\n");
        Set<String> inputVars = finder.getInputVars();
        assertEquals(Collections.emptySet(), inputVars);

        Set<String> outputVars = finder.getOutputVars();
        Set<String> expected = new HashSet<String>() {{
            add("y");
        }};
        assertEquals(expected, outputVars);
    }

    public void testIncrement() throws Exception {
        InputOutputFinder finder = getFinder("%<%x = x + 1%>%\nprint x");
        Set<String> inputVars = finder.getInputVars();
        Set<String> expected = new HashSet<String>() {{
            add("x");
        }};
        assertEquals(expected, inputVars);

        Set<String> outputVars = finder.getOutputVars();
        expected = new HashSet<String>() {{
            add("x");
        }};
        assertEquals(expected, outputVars);
    }


    public void testAssignment() throws Exception {
        InputOutputFinder finder = getFinder(
                "x = 5\n" +
                "%<%y = x + 1%>%\n" +
                "print y");
        Set<String> inputVars = finder.getInputVars();
        Set<String> expected = new HashSet<String>() {{
            add("x");
        }};
        assertEquals(expected, inputVars);

        Set<String> outputVars = finder.getOutputVars();
        expected = new HashSet<String>() {{
            add("y");
        }};
        assertEquals(expected, outputVars);
    }
}

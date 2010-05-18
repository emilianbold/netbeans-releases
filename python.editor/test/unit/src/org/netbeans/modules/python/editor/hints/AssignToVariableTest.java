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

import org.netbeans.modules.python.editor.PythonTestBase;

/**
 *
 * @author Tor Norbye
 */
public class AssignToVariableTest extends PythonTestBase {

    public AssignToVariableTest(String testName) {
        super(testName);
    }

    private PythonAstRule createRule() {
        return new AssignToVariable();
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }

    public void testAssign1() throws Exception {
        findHints(this, createRule(), "testfiles/assign.py", null, "^\"foo\"");
    }

    public void testAssign2() throws Exception {
        findHints(this, createRule(), "testfiles/assign.py", null, "3+1^");
    }

    public void testAssign3() throws Exception {
        findHints(this, createRule(), "testfiles/assign.py", null, "^get_preprocess2");
    }

    public void testNoHint() throws Exception {
        findHints(this, createRule(), "testfiles/ConfigParser.py", null, "d = s^elf._defaults.copy()");
    }

    public void testNoHint2() throws Exception {
        findHints(this, createRule(), "testfiles/ConfigParser.py", null, "\"\"\"Raised when^ a section is multiply-created.");
    }

    public void testNoHint3() throws Exception {
        // Contains call that is not a getter
        findHints(this, createRule(), "testfiles/assign.py", null, "^preprocess1");
    }

    public void testNoHint4() throws Exception {
        // Don't assign docstrings
        findHints(this, createRule(), "testfiles/assign2.py", null, "^\"year -> 1");
    }

    public void testFixAssign() throws Exception {
        applyHint(this, createRule(), "testfiles/assign.py", "^3+1", "Assign");
    }
}

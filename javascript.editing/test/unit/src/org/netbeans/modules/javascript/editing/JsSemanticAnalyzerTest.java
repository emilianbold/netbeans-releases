/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.javascript.editing;

/**
 *
 * @author tor
 */
public class JsSemanticAnalyzerTest extends JsTestBase {
    
    public JsSemanticAnalyzerTest(String testName) {
        super(testName);
    }           
    
    public void testSemantic1() throws Exception {
        checkSemantic("testfiles/semantic1.js");
    }

    public void testSemantic2() throws Exception {
        checkSemantic("testfiles/semantic2.js");
    }

    public void testSemantic3() throws Exception {
        checkSemantic("testfiles/semantic3.js");
    }

    public void testSemantic4() throws Exception {
        checkSemantic("testfiles/semantic4.js");
    }

    public void testSemantic5() throws Exception {
        checkSemantic("testfiles/semantic5.js");
    }

    public void testSemantic6() throws Exception {
        checkSemantic("testfiles/semantic6.js");
    }

    public void testSemantic7() throws Exception {
        checkSemantic("testfiles/semantic7.js");
    }

    public void testSemantic8() throws Exception {
        checkSemantic("testfiles/semantic8.js", "new^");
    }

    public void testSemantic9() throws Exception {
        // Based on issue 147450
        checkSemantic("testfiles/semantic9.js");
    }

    public void testSemanticE4x() throws Exception {
        checkSemantic("testfiles/e4x.js", "order^");
    }

    public void testSemanticE4x2() throws Exception {
        checkSemantic("testfiles/e4x2.js", "order^");
    }

    public void testSemanticTryCatch() throws Exception {
        checkSemantic("testfiles/tryblocks.js");
    }

    public void testSemanticPrototype() throws Exception {
        checkSemantic("testfiles/prototype.js");
    }

    public void testSemanticPrototypeNew() throws Exception {
        checkSemantic("testfiles/prototype-new.js");
    }

    public void testDebuggerKeyword() throws Exception {
        checkSemantic("testfiles/debugger.js");
    }
}

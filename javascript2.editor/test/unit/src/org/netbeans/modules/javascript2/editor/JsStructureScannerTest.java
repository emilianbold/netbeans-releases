/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsStructureScannerTest extends JsTestBase {
    
    public JsStructureScannerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {
        super.assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }
    
    public void testFolds01() throws Exception {
        checkFolds("testfiles/simple.js");
    }
    
    public void testFolds02() throws Exception {
        checkFolds("testfiles/coloring/czechChars.js");
    }
    
    public void testSimpleMethodChain() throws Exception {
        checkStructure("testfiles/completion/simpleMethodChain/methodChainSimple.js");
    }
    
//    public void testTypeInferenceNew() throws Exception {
//        checkStructure("testfiles/completion/typeInferenceNew.js");
//    }
    
    public void testGetterSettterInObjectLiteral() throws Exception {
        checkStructure("testfiles/model/getterSettterInObjectLiteral.js");
    }
    
//    public void testPerson() throws Exception {
//        checkStructure("testfiles/model/person.js");
//    }
    
    public void testAnonymousFunction() throws Exception {
        checkStructure("testfiles/model/jQueryFragment01.js");
    }
    
    public void testIssue198032() throws Exception {
        checkStructure("testfiles/coloring/issue198032.js");
    }
    
    public void testFormatter() throws Exception {
        checkStructure("testfiles/coloring/Formatter.js");
    }
    
//    public void testAssignmnets01() throws Exception {
//        checkStructure("testfiles/coloring/assignments01.js");
//    }
    
    public void testArrays() throws Exception {
        checkStructure("testfiles/completion/arrays/arrays.js");
    }
    
    public void testLiteralObject01() throws Exception {
        checkStructure("testfiles/completion/resolvingThis.js");
    }
    
    public void testDisplayPrototypeProperties01() throws Exception {
        checkStructure("testfiles/coloring/issue215354.js"); 
    }
    
    public void testIssue217031() throws Exception {
        checkStructure("testfiles/completion/issue217031.js"); 
    }
    
    public void testIssue216851() throws Exception {
        checkStructure("testfiles/coloring/issue216851.js"); 
    }
    
    public void testIssue216640() throws Exception {
        checkStructure("testfiles/coloring/issue216640.js"); 
    }
    
    public void testIssue218070() throws Exception {
        checkStructure("testfiles/coloring/issue218070_01.js"); 
    }
}

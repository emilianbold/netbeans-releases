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
    
    public void testIssue226142() throws Exception {
        checkFolds("testfiles/structure/issue226142.js");
    }

    public void testIssue228186() throws Exception {
        checkFolds("testfiles/structure/issue228186.js");
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
        checkStructure("testfiles/completion/arrays/arrays1.js");
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

    public void testIssue149408() throws Exception {
        checkStructure("testfiles/coloring/issue149408.js");
    }
    
    public void testIssue215764() throws Exception {
        checkStructure("testfiles/completion/general/issue215764.js");
    }
    
    public void testIssue22601() throws Exception {
        checkStructure("testfiles/completion/general/issue222601.js");
    }
    
    public void testIssue222691() throws Exception {
        checkStructure("testfiles/coloring/issue222691.js");
    }
    
    public void testIssue222852() throws Exception {
        checkStructure("testfiles/coloring/issue222852.js");
    }
    
    public void testIssue222893() throws Exception {
        checkStructure("testfiles/coloring/issue222893.js");
    }
    
    public void testIssue222910() throws Exception {
        checkStructure("testfiles/coloring/issue222910.js");
    }
    
    public void testIssue222954() throws Exception {
        checkStructure("testfiles/coloring/issue222954.js");
    }
    
    public void testIssue222977() throws Exception {
        checkStructure("testfiles/coloring/issue222977.js");
    }
    
    public void testIssue223037() throws Exception {
        checkStructure("testfiles/completion/general/issue223037.js");
    }
    
    public void testIssue223121() throws Exception {
        checkStructure("testfiles/coloring/issue223121.js");
    }
    
    public void testIssue223313() throws Exception {
        checkStructure("testfiles/coloring/issue223313.js");
    }
    
    public void testIssue223306() throws Exception {
        checkStructure("testfiles/coloring/issue223306.js");
    }
    
    public void testIssue223423() throws Exception {
        checkStructure("testfiles/coloring/issue223423.js");
    }
    
    public void testIssue223264() throws Exception {
        checkStructure("testfiles/coloring/issue223264.js");
    }
    
    public void testIssue223304() throws Exception {
        checkStructure("testfiles/coloring/issue223304.js");
    }
    
    public void testIssue217029() throws Exception {
        checkStructure("testfiles/completion/issue217029.js");
    }
    
    public void testIssue215756() throws Exception {
        checkStructure("testfiles/coloring/issue215756.js");
    }
    
    public void testIssue223699() throws Exception {
        checkStructure("testfiles/coloring/issue223699.js");
    }
    
    public void testIssue217938() throws Exception {
        checkStructure("testfiles/structure/issue217938.js");
    }
    
    public void testIssue205098() throws Exception {
        checkStructure("testfiles/structure/issue205098.js");
    }
    
    public void testIssue223814() throws Exception {
        checkStructure("testfiles/coloring/issue223814.js");
    }
    
    public void testIssue216855() throws Exception {
        checkStructure("testfiles/structure/issue216855.js");
    }
    
    public void testIssue217011() throws Exception {
        checkStructure("testfiles/structure/issue217011.js");
    }

    public void testIssue224090() throws Exception {
        checkStructure("testfiles/structure/issue224090.js");
    }
    
    public void testIssue224562() throws Exception {
        checkStructure("testfiles/coloring/issue224562.js");
    }
    
    public void testIssue225755() throws Exception {
        checkStructure("testfiles/structure/issue225755.js");
    }
    
    public void testIssue225399() throws Exception {
        checkStructure("testfiles/markoccurences/issue225399.js");
    }
    
    public void testIssue224520() throws Exception {
        checkStructure("testfiles/markoccurences/issue224520.js");
    }
    
    public void testIssue226480() throws Exception {
        checkStructure("testfiles/structure/issue226480.js");
    }
    
    public void testIssue226559() throws Exception {
        checkStructure("testfiles/structure/issue226559.js");
    }
    
    public void testIssue226930() throws Exception {
        checkStructure("testfiles/structure/issue226930.js");
    }
    
    public void testIssue227163() throws Exception {
        checkStructure("testfiles/structure/issue227153.js");
    }
    
    public void testIssue222177() throws Exception {
        checkStructure("testfiles/structure/issue222177.js");
    }
    
    public void testIssue226976() throws Exception {
        checkStructure("testfiles/structure/issue226976.js");
    }
    
    public void testIssue228564() throws Exception {
        checkStructure("testfiles/completion/issue228564.js");
    }
    
    public void testIssue222952() throws Exception {
        checkStructure("testfiles/structure/issue222952.js");
    }
    
    public void testIssue226627() throws Exception {
        checkStructure("testfiles/structure/issue226627.js");
    }
    
    public void testIssue226521() throws Exception {
        checkStructure("testfiles/completion/general/issue226521.js");
    }
    
    public void testIssue226490() throws Exception {
        checkStructure("testfiles/structure/issue226490.js");
    }
    
    public void testIssue223967() throws Exception {
        checkStructure("testfiles/completion/general/issue223967.js");
    }
    
    public void testIssue223933() throws Exception {
        checkStructure("testfiles/completion/issue223933.js");
    }
    
    public void testIssue230578() throws Exception {
        checkStructure("testfiles/structure/issue230578.js");
    }
    
    public void testIssue230709() throws Exception {
        checkStructure("testfiles/structure/issue230709.js");
    }
    
    public void testIssue230736() throws Exception {
        checkStructure("testfiles/completion/general/issue230736.js");
    }
    
    public void testIssue230784() throws Exception {
        checkStructure("testfiles/completion/general/issue230784.js");
    }
    
    public void testIssue229717() throws Exception {
        checkStructure("testfiles/model/issue229717.js");
    }
    
    public void testIssue231026() throws Exception {
        checkStructure("testfiles/structure/issue231026.js");
    }
    
    public void testIssue231048() throws Exception {
        checkStructure("testfiles/structure/issue231048.js");
    }
    
    public void testIssue231059() throws Exception {
        checkStructure("testfiles/structure/issue231059.js");
    }
    
    public void testIssue231025() throws Exception {
        checkStructure("testfiles/structure/issue231025.js");
    }
    
    public void testIssue231333() throws Exception {
        checkStructure("testfiles/structure/issue231333.js");
    }
    
    public void testIssue231292() throws Exception {
        checkStructure("testfiles/structure/issue231292.js");
    }
    
    public void testIssue231688() throws Exception {
        checkStructure("testfiles/structure/issue231688.js");
    }
    
    public void testIssue231262() throws Exception {
        checkStructure("testfiles/structure/issue231262.js");
    }
    
    public void testResolvingThis() throws Exception {
        checkStructure("testfiles/structure/resolvingThis.js");
    }
    
    public void testIssue231751() throws Exception {
        checkStructure("testfiles/structure/issue231751.js");
    }
    
    public void testIssue231841() throws Exception {
        checkStructure("testfiles/structure/issue231841.js");
    }
    
    public void testIssue231752() throws Exception {
        checkStructure("testfiles/coloring/issue231752.js");
    }
    
    public void testIssue231908() throws Exception {
        checkStructure("testfiles/structure/issue231908.js");
    }
    
    public void testIssue232549() throws Exception {
        checkStructure("testfiles/structure/issue232549.js");
    }
    
    public void testIssue232570() throws Exception {
        checkStructure("testfiles/completion/issue232570.js");
    }
}

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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.javascript.editing;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.javascript.editing.JsAnalyzer.AnalysisResult;

/**
 *
 * @author Tor Norbye
 */
public class JsAnalyzerTest extends JsTestBase {
    private boolean isJson;

    public JsAnalyzerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public StructureScanner getStructureScanner() {
        if (isJson) {
            return new JsonAnalyzer();
        }
        return super.getStructureScanner();
    }

    private void checkImports(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        JsParseResult result = AstUtilities.getParseResult(info);
        AnalysisResult analysisResult = JsAnalyzer.analyze(result, info);
        List<String> imports = analysisResult.getImports();
        if (imports.size() > 1) {
            Collections.sort(imports);
        }
        
        StringBuilder sb = new StringBuilder();
        for (String s : imports) {
            sb.append(s);
            sb.append("\n");
        }
        
        String annotatedSource = sb.toString();

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".imports");
    }
    
    public void testAnalysis() throws Exception {
        checkStructure("testfiles/SpryEffects.js");
    }

    public void testAnalysis2() throws Exception {
        checkStructure("testfiles/SpryXML.js");
    }

    public void testAnalysis3() throws Exception {
        checkStructure("testfiles/jmaki-uncompressed.js");
    }

    public void testAnalysis4() throws Exception {
        checkStructure("testfiles/orig-dojo.js.uncompressed.js");
    }

    public void testAnalysis5() throws Exception {
        checkStructure("testfiles/dragdrop.js");
    }
    
    public void testFolds1() throws Exception {
        checkFolds("testfiles/SpryEffects.js");
    }

    public void testFolds2() throws Exception {
        checkFolds("testfiles/SpryXML.js");
    }

    public void testFolds3() throws Exception {
        checkFolds("testfiles/jmaki-uncompressed.js");
    }

    public void testFolds4() throws Exception {
        checkFolds("testfiles/orig-dojo.js.uncompressed.js");
    }

    public void testFolds5() throws Exception {
        checkFolds("testfiles/dragdrop.js");
    }

    public void testImports1() throws Exception {
        checkImports("testfiles/fileinclusion.html.js");
    }

    public void testImports2() throws Exception {
        checkImports("testfiles/dragdrop.js");
    }

    public void testClasses() throws Exception {
        checkStructure("testfiles/classes.js");
        checkFolds("testfiles/classes.js");
    }

    public void testJsonFolds() throws Exception {
        isJson = true;
        checkFolds("testfiles/sample.json");
    }

    public void testJsonStructure() throws Exception {
        isJson = true;
        checkStructure("testfiles/sample.json");
    }

    public void testJsonFolds2() throws Exception {
        isJson = true;
        checkFolds("testfiles/sample2.json");
    }

    public void testJsonStructure2() throws Exception {
        isJson = true;
        checkStructure("testfiles/sample2.json");
    }
}

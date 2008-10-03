/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.languages.yaml;

import java.util.List;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.ParserResult;

/**
 *
 * @author Tor Norbye
 */
public class YamlParserTest extends YamlTestBase {
    public YamlParserTest(String testName) {
        super(testName);
    }

    public void testErrors1() throws Exception {
        checkErrors("testfiles/error.yaml");
    }

    public void testErrors2() throws Exception {
        checkErrors("testfiles/error2.yaml");
    }

    public void testErrors3() throws Exception {
        checkErrors("testfiles/error3.yaml");
    }

    public void testHuge() throws Exception {
        StringBuilder sb = new StringBuilder();
        String s = readFile(getTestFile("testfiles/database.yml"));
        while (sb.length() < 1024*1024) {
            sb.append(s);
        }
        String huge = sb.toString();
        String relFilePath = "generated-huge.yml";
        GsfTestCompilationInfo info = getInfoForText(huge, relFilePath);
        String text = info.getText();
        assertNotNull(text);

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);

        List<Error> diagnostics = pr.getDiagnostics();
        String annotatedSource = annotateErrors(text, diagnostics);
        assertDescriptionMatches("testfiles/" + relFilePath, annotatedSource, false, ".errors", false);
        // Make sure we actually skipped parsing this large document!
        assertNull(((YamlParserResult)pr).getObject());
    }
}

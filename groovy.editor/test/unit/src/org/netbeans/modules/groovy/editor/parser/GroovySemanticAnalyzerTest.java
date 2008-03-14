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

package org.netbeans.modules.groovy.editor.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.openide.filesystems.FileObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;

/**
 *
 * @author Matthias Schmidt
 */
public class GroovySemanticAnalyzerTest extends GroovyTestBase {

    public GroovySemanticAnalyzerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        // Logger.getLogger(GroovySemanticAnalyzer.class.getName()).setLevel(Level.FINEST);
        Logger.getLogger(org.netbeans.modules.groovy.editor.AstUtilities.class.getName()).setLevel(Level.FINEST);
    }
    
    // uncomment this to have logging
//    protected Level logLevel() {
//        // enabling logging
//        return Level.INFO;
//        // we are only interested in a single logger, so we set its level in setUp(),
//        // as returning Level.FINEST here would log from all loggers
//    }

    private String annotate(Document doc, Map<OffsetRange, ColoringAttributes> highlights) throws Exception {
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : highlights.keySet()) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                sb.append("|>");
                OffsetRange range = starts.get(i);
                ColoringAttributes ca = highlights.get(range);
                if (ca != null) {
                    sb.append(ca.name());
                    sb.append(':');
                }
            }
            if (ends.containsKey(i)) {
                sb.append("<|");
            }
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }

    private void checkSemantic(String relFilePath) throws Exception {
        GroovySemanticAnalyzer analyzer = new GroovySemanticAnalyzer();
        CompilationInfo info = getInfo(relFilePath);
        analyzer.run(info);
        Map<OffsetRange, ColoringAttributes> highlights = analyzer.getHighlights();

        String annotatedSource = annotate(info.getDocument(), highlights);

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".semantic");
    }

    public void testAnalysis() throws Exception {
        checkSemantic("testfiles/Hello.groovy");
    }
    
}

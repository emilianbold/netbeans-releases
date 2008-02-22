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

package org.netbeans.modules.groovy.editor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.groovy.editor.parser.GroovyOccurrencesFinder;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Martin Adamek
 */
public class GroovyOccurencesFinderTest extends GroovyTestBase {

    public GroovyOccurencesFinderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws IOException {
        super.setUp();
        Logger.getLogger(PathFinderVisitor.class.getName()).setLevel(Level.FINEST);
    }

    // uncomment this to have logging from GroovyLexer
//    protected Level logLevel() {
//        // enabling logging
//        return Level.INFO;
//        // we are only interested in a single logger, so we set its level in setUp(),
//        // as returning Level.FINEST here would log from all loggers
//    }

    public void testParams() throws Exception {
        String caretLine = "        par^ams.each {";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }

    public void testUnusedParams() throws Exception {
        String caretLine = "    private printParams(params, unus^edParam) {";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }

    public void testClassVariable() throws Exception {
        String caretLine = "    Map par^ams = [:]";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }

    private String annotate(BaseDocument doc, Map<OffsetRange, ColoringAttributes> highlights, int caretOffset) throws Exception {
        Set<OffsetRange> ranges = highlights.keySet();
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        int index = 0;
        int length = text.length();
        while (index < length) {
            int lineStart = Utilities.getRowStart(doc, index);
            int lineEnd = Utilities.getRowEnd(doc, index);
            OffsetRange lineRange = new OffsetRange(lineStart, lineEnd);
            boolean skipLine = true;
            for (OffsetRange range : ranges) {
                if (lineRange.containsInclusive(range.getStart()) || lineRange.containsInclusive(range.getEnd())) {
                    skipLine = false;
                }
            }
            if (!skipLine) {
                for (int i = lineStart; i <= lineEnd; i++) {
                    if (i == caretOffset) {
                        sb.append("^");
                    }
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
            }
            index = lineEnd + 1;
        }

        return sb.toString();
    }

    /** Test the occurrences to make sure they equal the golden file.
     * If the symmetric parameter is set, this test will also ensure that asking for
     * occurrences on ANY of the matches produced by the original caret position will
     * produce the exact same map. This is obviously not appropriate for things like
     * occurrences on the exit points.
     */
    private void checkOccurrences(String relFilePath, String caretLine, boolean symmetric) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        String text = info.getText();

        int caretDelta = caretLine.indexOf('^');
        assertTrue(caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue(lineOffset != -1);

        int caretOffset = lineOffset + caretDelta;

        GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder();
        finder.setCaretPosition(caretOffset);
        finder.run(info);
        Map<OffsetRange, ColoringAttributes> occurrences = finder.getOccurrences();

        String annotatedSource = annotate((BaseDocument)info.getDocument(), occurrences, caretOffset);

        assertDescriptionMatches(relFilePath, annotatedSource, true, ".occurrences");
        
        if (symmetric) {
            // Extra check: Ensure that occurrences are symmetric: Placing the caret on ANY of the occurrences
            // should produce the same set!!
            for (OffsetRange range : occurrences.keySet()) {
                finder.setCaretPosition(range.getStart()+range.getLength()/2);
                finder.run(info);
                Map<OffsetRange, ColoringAttributes> alternates = finder.getOccurrences();
                assertEquals("Marks differ between caret positions", occurrences, alternates);
            }
        }
    }

}

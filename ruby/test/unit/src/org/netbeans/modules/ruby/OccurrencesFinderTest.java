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

package org.netbeans.modules.ruby;

import java.util.Map;
import org.netbeans.api.gsf.CompilationInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.gsf.ColoringAttributes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;

/**
 * Test the "mark occurrences" feature in Ruby
 *
 * @author Tor Norbye
 */
public class OccurrencesFinderTest extends RubyTestBase {

    public OccurrencesFinderTest(String testName) {
        super(testName);
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

    private void checkOccurrences(String relFilePath, String caretLine) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        String text = info.getText();

        int caretDelta = caretLine.indexOf('^');
        assertTrue(caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue(lineOffset != -1);

        int caretOffset = lineOffset + caretDelta;

        OccurrencesFinder finder = new OccurrencesFinder();
        finder.setCaretPosition(caretOffset);
        finder.run(info);
        Map<OffsetRange, ColoringAttributes> occurrences = finder.getOccurrences();

        String annotatedSource = annotate((BaseDocument)info.getDocument(), occurrences, caretOffset);

        assertDescriptionMatches(relFilePath, annotatedSource, true, ".occurrences");
    }

    public void testApeParams() throws Exception {
        String caretLine = "  def initialize(ar^gs)";
        checkOccurrences("testfiles/ape.rb", caretLine);
    }

    public void testApeMethodDef() throws Exception {
        String caretLine = "def te^st_entry_posts(entry_collection)";
        checkOccurrences("testfiles/ape.rb", caretLine);
    }

    public void testApeMethodRef() throws Exception {
        String caretLine = "test_entry_pos^ts";
        checkOccurrences("testfiles/ape.rb", caretLine);
    }

    public void testApeSymbol() throws Exception {
        String caretLine = "@@debugging = args[:de^bug]";
        checkOccurrences("testfiles/ape.rb", caretLine);
    }

    public void testApeClassVar() throws Exception {
        String caretLine = "@@deb^ugging = args[:debug]";
        checkOccurrences("testfiles/ape.rb", caretLine);
    }

    public void testApeInstanceVar() throws Exception {
        String caretLine = "@st^eps[-1] << message";
        checkOccurrences("testfiles/ape.rb", caretLine);
    }

    public void testApeExitPoints() throws Exception {
        String caretLine = "d^ef might_fail(uri, requested_e_coll = nil, requested_m_coll = nil)";
        checkOccurrences("testfiles/ape.rb", caretLine);
    }

    public void testUnusedExitPoints() throws Exception {
        String caretLine = "d^ef foo(unusedparam, unusedparam2, usedparam)";
        checkOccurrences("testfiles/unused.rb", caretLine);
    }

    public void testUnusedParams() throws Exception {
        String caretLine = "def foo(un^usedparam, unusedparam2, usedparam)";
        checkOccurrences("testfiles/unused.rb", caretLine);
    }

    public void testUnusedParams2() throws Exception {
        String caretLine = "def foo(unusedparam, unusedparam2, us^edparam)";
        checkOccurrences("testfiles/unused.rb", caretLine);
    }

    public void testUnusedParams3() throws Exception {
        String caretLine = "x.each { |unusedblockvar1, usedbl^ockvar2|";
        checkOccurrences("testfiles/unused.rb", caretLine);
    }

    public void testYieldNode() throws Exception {
        String caretLine = "def exit_t^est";
        checkOccurrences("testfiles/yieldnode.rb", caretLine);
    }

    public void testNestedBlocks() throws Exception {
        checkOccurrences("testfiles/nestedblocks.rb", "[4,5,6].each { |ou^ter|");
    }

    public void testNestedBlocks2() throws Exception {
        checkOccurrences("testfiles/nestedblocks2.rb", "arg.each_pair do |fo^o, val|");
    }

    public void testParellelBlocks() throws Exception {
        checkOccurrences("testfiles/parallelblocks.rb", "foo.each { |i^| puts i } #1");
    }
}

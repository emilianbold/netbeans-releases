/*
 * OccurrencesFinderTest.java
 * JUnit based test
 *
 * Created on July 11, 2007, 8:41 AM
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
}

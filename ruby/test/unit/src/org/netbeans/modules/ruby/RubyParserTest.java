/*
 * RubyParserTest.java
 * JUnit based test
 *
 * Created on August 30, 2007, 9:10 AM
 */

package org.netbeans.modules.ruby;

import java.util.List;
import junit.framework.TestCase;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.ElementHandle;
import org.netbeans.api.gsf.OccurrencesFinder;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.PositionManager;
import org.netbeans.api.gsf.SemanticAnalyzer;
import org.netbeans.api.gsf.SourceFileReader;
import org.netbeans.modules.ruby.RubyParser.Sanitize;

/**
 *
 * @author Tor Norbye
 */
public class RubyParserTest extends RubyTestBase {
    
    public RubyParserTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void checkParseTree(String file, String caretLine, String nodeName) throws Exception {
        CompilationInfo info = getInfo(file);
        
        String text = info.getText();

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
            ((TestCompilationInfo)info).setCaretOffset(caretOffset);
        }

        Node root = AstUtilities.getRoot(info);
        assertNotNull("Parsing broken input failed for " + file, root);
        
        // Ensure that we find the node we're looking for
        if (nodeName != null) {
            RubyParseResult rpr = (RubyParseResult)info.getParserResult();
            OffsetRange range = rpr.getSanitizedRange();
            if (range.containsInclusive(caretOffset)) {
                caretOffset = range.getStart();
            }
            AstPath path = new AstPath(root, caretOffset);
            Node closest = path.leaf();
            assertNotNull(closest);
            String leafName = closest.getClass().getName();
            leafName = leafName.substring(leafName.lastIndexOf('.')+1);
            assertEquals(nodeName, leafName);
        }
    }
        
    public void testPartial1() throws Exception {
        checkParseTree("testfiles/broken1.rb", "x.^", "VCallNode");
    }
    
    public void testPartial1b() throws Exception {
        // Recover even when the caret is elsewhere
        checkParseTree("testfiles/broken1.rb", null, null);
    }

    public void testPartial2() throws Exception {
        checkParseTree("testfiles/broken2.rb", "Foo.new.^", "CallNode");
    }

    public void testPartial3() throws Exception {
        checkParseTree("testfiles/broken3.rb", "x = ^", "ClassNode");
    }

    public void testPartial3b() throws Exception {
        // Recover even when the caret is elsewhere
        checkParseTree("testfiles/broken3.rb", null, null);
    }

    public void testPartial4() throws Exception {
        checkParseTree("testfiles/broken4.rb", "Test::^", "ConstNode");
    }
    
    public void testPartial4b() throws Exception {
        // Recover even when the caret is elsewhere
        checkParseTree("testfiles/broken4.rb", null, null);
    }

    public void testPartial5() throws Exception {
        checkParseTree("testfiles/broken5.rb", "if true^", "DefnNode");
    }

    public void testPartial5MissingEnd() throws Exception {
        // An end is missing and we don't have a current line we can simply
        // clip out; try to compensate
        checkParseTree("testfiles/broken5.rb", null, null);
    }
    
    public void testPartial6() throws Exception {
        checkParseTree("testfiles/broken6.rb", "def ^", "ClassNode");
    }


}

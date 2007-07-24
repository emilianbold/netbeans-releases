/*
 * FormattingTest.java
 *
 * Created on February 23, 2007, 4:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.ruby;

import java.util.List;
import javax.swing.JTextArea;
import javax.swing.text.Caret;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 *
 * @todo Test partial reformatting (indentation in the middle of a file, newline computation in the middle of a file)
 * @todo Partial test: Make sure I don't reformat the line AFTER the newline insertion when I hit newline!
 * 
 * @author Tor Norbye
 */
public class FormatterTest extends RubyTestBase {
    public FormatterTest(String testName) {
        super(testName);
    }

    public void format(String source, String reformatted, FormattingPreferences preferences) throws Exception {
        Formatter formatter = new Formatter();
        if (preferences == null) {
            preferences = new IndentPrefs(2,2);
        }
        
        String BEGIN = "%<%"; // NOI18N
        int startPos = source.indexOf(BEGIN);
        if (startPos != -1) {
            source = source.substring(0, startPos) + source.substring(startPos+BEGIN.length());
        } else {
            startPos = 0;
        }
        
        String END = "%>%"; // NOI18N
        int endPos = source.indexOf(END);
        if (endPos != -1) {
            source = source.substring(0, endPos) + source.substring(endPos+END.length());
        }

        BaseDocument doc = getDocument(source);

        if (endPos == -1) {
            endPos = doc.getLength();
        }
        
        //ParserResult result = parse(fo);
        ParserResult result = null;
        formatter.reindent(doc, startPos, endPos, result, preferences);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
    }
    
    private void reformatFileContents(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);
        String before = doc.getText(0, doc.getLength());
        
        Formatter formatter = new Formatter();
        FormattingPreferences preferences = new IndentPrefs(2,2);

        formatter.reindent(doc, 0, doc.getLength(), null, preferences);
        String after = doc.getText(0, doc.getLength());
        assertEquals(before, after);
    }
    
    public void insertNewline(String source, String reformatted, FormattingPreferences preferences) throws Exception {
        Formatter formatter = new Formatter();
        if (preferences == null) {
            preferences = new IndentPrefs(2,2);
        }

        int sourcePos = source.indexOf('^');     
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');        
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);
        
        BaseDocument doc = getDocument(source);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.insertString(caret.getDot(), "\n", null);
        
            int startPos = caret.getDot()+1;
            int endPos = Utilities.getRowEnd(doc, startPos);

            //ParserResult result = parse(fo);
            ParserResult result = null;
            formatter.reindent(doc, startPos, endPos, result, preferences);

            String formatted = doc.getText(0, doc.getLength());
            assertEquals(reformatted, formatted);
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }
    
    public void testReformatAll() {
        // Find ruby files
        List<FileObject> files = findJRubyRubyFiles();
        assertTrue(files.size() > 0);

        Formatter formatter = new Formatter();
        FormattingPreferences preferences = new IndentPrefs(2,2);
        
        int fileCount = files.size();
        int count = 0;
        
        // indent each one
        for (FileObject fo : files) {
            count++;

// This bug triggers #108889
if (fo.getName().equals("action_controller_dispatcher") && fo.getParent().getName().equals("dispatcher")) {
    System.err.println("SKIPPING known bad file " + fo.getNameExt());
    continue;
}
// This bug triggers #108889
if (fo.getName().equals("parse_f95") && fo.getParent().getName().equals("parsers")) {
    System.err.println("SKIPPING known bad file " + fo.getNameExt());
    continue;
}
// Tested by RubyLexerTest#testDefRegexp 
if (fo.getName().equals("httputils") && fo.getParent().getName().equals("webrick")) {
    System.err.println("SKIPPING known bad file " + fo.getNameExt());
    continue;
}
            System.err.println("Formatting file " + count + "/" + files.size() + " : " + FileUtil.getFileDisplayName(fo));
            
            // check that we end up at indentation level 0
            BaseDocument doc = getDocument(fo);
            
            ParserResult result =  null;// parse(fo);

            try {
                formatter.reindent(doc, 0, doc.getLength(), result, preferences);
            } catch (Exception ex) {
                System.err.println("Exception processing " + FileUtil.getFileDisplayName(fo));
                fail(ex.toString());
                throw new RuntimeException(ex);
            }
            
            // Make sure that we end up on column 0, as all balanced Ruby files typically do
            // (check for special exceptions, e.g. formatted strings and whatnot)
            try {
                int offset = doc.getLength();
                while (offset > 0) {
                    offset = Utilities.getRowStart(doc, offset);
                    if (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset)) {
                        offset = offset-1;
                        continue;
                    }

                    int indentation = Utilities.getRowFirstNonWhite(doc, offset) - offset;
                    
                    if (indentation != 0) {
                        // Make sure the file actually compiles - we might be picking up some testfiles in the
                        // JRuby tree which don't actually pass
                        result = parse(fo);
                        RubyParseResult rpr = (RubyParseResult)result;
                        
                        if (rpr.getRootNode() == null) {
                            System.err.println("WARNING - invalid formatting for " + FileUtil.getFileDisplayName(fo) + " " +
                                    "but it also doesn't parse with JRuby so ignoring");
                            break;
                        }
                    }
                    
                    
                    assertEquals("Failed formatting file " + count + "/" + fileCount + " \n" + fo.getNameExt() + "\n: Last line not at 0 indentation in " + FileUtil.getFileDisplayName(fo) + " indent=" + indentation + " line=" +
                            doc.getText(offset, Utilities.getRowEnd(doc, offset)-offset), 0, indentation);
                    break;
                }
            } catch (Exception ex) {
                fail(ex.toString());
            }
            
            // Also try re-lexing buffer incrementally and make sure it makes sense! (and handle bracket completion stuff)
        }
    }
    
    public void testFormatApe() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/ape.rb");
    }

    public void testFormatDate() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/date.rb");
    }

    public void testFormatResolv() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/resolv.rb");
    }
        
    public void testFormatPostgres() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/postgresql_adapter.rb");
    }

    public void testLineContinuationAsgn() throws Exception {
        format("x =\n1",
               "x =\n  1", null);
    }

    public void testLineContinuation2() throws Exception {
        format("x =\n1", 
               "x =\n    1", new IndentPrefs(2,4));
    }

    public void testLineContinuation3() throws Exception {
        format("x =\n1\ny = 5", 
               "x =\n    1\ny = 5", new IndentPrefs(2,4));
    }

    public void testLineContinuation4() throws Exception {
        format("def foo\nfoo\nif true\nx\nend\nend", 
               "def foo\n  foo\n  if true\n    x\n  end\nend", null);
    }

    public void testLineContinuation5() throws Exception {
        format("def foo\nfoo\nif true\nx\nend\nend", 
               "def foo\n    foo\n    if true\n        x\n    end\nend", new IndentPrefs(4,4));
    }

    // Trigger lexer bug!
    //public void testLineContinuationBackslash() throws Exception {
    //    format("x\\\n= 1", 
    //           "x\\\n  = 1", new IndentPrefs(2,4));
    //}
    
    public void testLineContinuationComma() throws Exception {
        format("render foo,\nbar\nbaz",
               "render foo,\n  bar\nbaz", null);
    }

    public void testCommaIndent() throws Exception {
        insertNewline("puts foo,^", "puts foo,\n  ^", null);
    }
    
    public void testLineContinuationParens() throws Exception {
        format("foo(1,2\n3,4)\nx",
               "foo(1,2\n  3,4)\nx", null);
    }

    public void testLiterals() throws Exception {
        format("def foo\n  x = %q-foo\nbar-",
               "def foo\n  x = %q-foo\nbar-", null);
    }

    public void testLineContinuationAlias() throws Exception {
        format("foo ==\ntrue",
               "foo ==\n  true", null);
        format("alias foo ==\ntrue",
               "alias foo ==\ntrue", null);
        format("def ==\ntrue",
               "def ==\n  true", new IndentPrefs(2,4));
    }

    public void testBrackets() throws Exception {
        format("x = [[5]\n]\ny",
               "x = [[5]\n]\ny", null);
    }

    public void testBrackets2() throws Exception {
        format("x = [\n[5]\n]\ny",
               "x = [\n  [5]\n]\ny", null);
        format("x = [\n[5]\n]\ny",
               "x = [\n  [5]\n]\ny", new IndentPrefs(2,4));
    }

    public void testIndent1() throws Exception {
        insertNewline("x = [^[5]\n]\ny", "x = [\n  ^[5]\n]\ny", null);
    }

    public void testIndent2() throws Exception {
        insertNewline("x = ^", "x = \n  ^", null);
        insertNewline("x = ^", "x = \n    ^", new IndentPrefs(2,4));
        insertNewline("x = ^ ", "x = \n^  ", null);
    }

    public void testIndent3() throws Exception {
        insertNewline("      def foo^", "      def foo\n        ^", null);
    }

    public void testHeredoc1() throws Exception {
        format("def foo\n  s = <<EOS\n  stuff\nEOS\nend",
               "def foo\n  s = <<EOS\n  stuff\nEOS\nend", null);
    }

    public void testHeredoc2() throws Exception {
        format("def foo\n  s = <<-EOS\n  stuff\nEOS\nend",
               "def foo\n  s = <<-EOS\n  stuff\n  EOS\nend", null);
    }
    
    public void testArrayDecl() throws Exception {
        format("@foo = [\n'bar',\n'bar2',\n'bar3'\n]",
                "@foo = [\n  'bar',\n  'bar2',\n  'bar3'\n]", null);
    }

    public void testHashDecl() throws Exception {
        String unformatted = "@foo = {\n" +
            "'bar' => :foo,\n" +
            "'bar2' => :bar,\n" +
            "'bar3' => :baz\n" +
            "}";
        String formatted = "@foo = {\n" +
            "  'bar' => :foo,\n" +
            "  'bar2' => :bar,\n" +
            "  'bar3' => :baz\n" +
            "}";
        format(unformatted, formatted, null);
    }

    public void testParenCommaList() throws Exception {
        String unformatted = "foo(\nx,\ny,\nz\n)";
        String formatted = "foo(\n" +
            "  x,\n" +
            "  y,\n" +
            "  z\n" +
            ")";
        format(unformatted, formatted, null);
    }
    
    public void testDocumentRange1() throws Exception {
        format("      def foo\n%<%foo%>%\n      end\n", 
               "      def foo\n        foo\n      end\n", null);
        format("def foo\nfoo\nend\n", 
               "def foo\n  foo\nend\n", null);
    }

    public void testDocumentRange2() throws Exception {
        format("def foo\n     if true\n           %<%xxx%>%\n     end\nend\n",
                "def foo\n     if true\n       xxx\n     end\nend\n", null);
    }
    
}

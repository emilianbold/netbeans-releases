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

package org.netbeans.modules.javascript.editing;

import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JTextArea;
import javax.swing.text.Caret;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.gsf.api.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;

/**
 *
 * @author tor
 */
public class JsFormatterTest extends JsTestBase {
    
    public JsFormatterTest(String testName) {
        super(testName);
    }            

    private JsFormatter getFormatter(IndentPrefs preferences) {
        if (preferences == null) {
            preferences = new IndentPrefs(4,4);
        }

        Preferences prefs = NbPreferences.forModule(JsFormatterTest.class);
        prefs.put(FmtOptions.indentSize, Integer.toString(preferences.getIndentation()));
        prefs.put(FmtOptions.continuationIndentSize, Integer.toString(preferences.getHangingIndentation()));
        CodeStyle codeStyle = CodeStyle.getTestStyle(prefs);
        
        JsFormatter formatter = new JsFormatter(codeStyle, 80);
        
        return formatter;
    }

    
    public void format(String source, String reformatted, IndentPrefs preferences) throws Exception {
        JsFormatter JsFormatter = getFormatter(preferences);
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
        JsFormatter.reindent(doc, startPos, endPos);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
    }
    
    private void reformatFileContents(String file, IndentPrefs preferences) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);
        //String before = doc.getText(0, doc.getLength());
        
        JsFormatter formatter = getFormatter(preferences);

        formatter.reindent(doc, 0, doc.getLength());
        String after = doc.getText(0, doc.getLength());
        
        assertDescriptionMatches(file, after, false, ".formatted");
    }
    
    public void insertNewline(String source, String reformatted, IndentPrefs preferences) throws Exception {
        JsFormatter JsFormatter = getFormatter(preferences);

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
            int endPos = startPos;

            //ParserResult result = parse(fo);
            JsFormatter.reindent(doc, startPos, endPos);

            String formatted = doc.getText(0, doc.getLength());
            assertEquals(reformatted, formatted);
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }

    // Used to test arbitrary source trees
    //public void testReformatSourceTree() {
    //    List<FileObject> files = new ArrayList<FileObject>();
    //
    //    // Used to test random source trees
    //    File f = new File("/Users/tor/Desktop/facets-1.8.54"); // NOI18N
    //    FileObject root = FileUtil.toFileObject(f);
    //    addAllRubyFiles(root, files);
    //    reformatAll(files);
    //}
    
    private void addAllRubyFiles(FileObject file, List<FileObject> files) {
        if (file.isFolder()) {
            for (FileObject c : file.getChildren()) {
                addAllRubyFiles(c, files);
            }
        } else if (file.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
            files.add(file);
        }
    }
    
//    public void testReformatAll() {
//        // Find ruby files
//        List<FileObject> files = findJRubyRubyFiles();
//        assertTrue(files.size() > 0);
//        
//        reformatAll(files);
//    }
//    
//    private void reformatAll(List<FileObject> files) {
//        IndentPrefs preferences = new IndentPrefs(2,2);
//        JsFormatter JsFormatter = getFormatter(preferences);
//        
//        int fileCount = files.size();
//        int count = 0;
//        
//        // indent each one
//        for (FileObject fo : files) {
//            count++;
//
//// This bug triggers #108889
//if (fo.getName().equals("action_controller_dispatcher") && fo.getParent().getName().equals("dispatcher")) {
//    System.err.println("SKIPPING known bad file " + fo.getNameExt());
//    continue;
//}
//// This bug triggers #108889
//if (fo.getName().equals("parse_f95") && fo.getParent().getName().equals("parsers")) {
//    System.err.println("SKIPPING known bad file " + fo.getNameExt());
//    continue;
//}
//// Tested by RubyLexerTest#testDefRegexp 
//if (fo.getName().equals("httputils") && fo.getParent().getName().equals("webrick")) {
//    System.err.println("SKIPPING known bad file " + fo.getNameExt());
//    continue;
//}
//            System.err.println("Formatting file " + count + "/" + files.size() + " : " + FileUtil.getFileDisplayName(fo));
//            
//            // check that we end up at indentation level 0
//            BaseDocument doc = getDocument(fo);
//            
//            ParserResult result =  null;// parse(fo);
//
//            try {
//                JsFormatter.reindent(doc, 0, doc.getLength(), result);
//            } catch (Exception ex) {
//                System.err.println("Exception processing " + FileUtil.getFileDisplayName(fo));
//                fail(ex.toString());
//                throw new RuntimeException(ex);
//            }
//            
//            // Make sure that we end up on column 0, as all balanced Ruby files typically do
//            // (check for special exceptions, e.g. formatted strings and whatnot)
//            try {
//                int offset = doc.getLength();
//                while (offset > 0) {
//                    offset = Utilities.getRowStart(doc, offset);
//                    if (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset)) {
//                        offset = offset-1;
//                        continue;
//                    }
//
//                    int indentation = Utilities.getRowFirstNonWhite(doc, offset) - offset;
//                    
//                    if (indentation != 0) {
//                        // Make sure the file actually compiles - we might be picking up some testfiles in the
//                        // JRuby tree which don't actually pass
//                        result = parse(fo);
//                        RubyParseResult rpr = (RubyParseResult)result;
//                        
//                        if (rpr.getRootNode() == null) {
//                            System.err.println("WARNING - invalid formatting for " + FileUtil.getFileDisplayName(fo) + " " +
//                                    "but it also doesn't parse with JRuby so ignoring");
//                            break;
//                        }
//                    }
//                    
//                    
//                    assertEquals("Failed formatting file " + count + "/" + fileCount + " \n" + fo.getNameExt() + "\n: Last line not at 0 indentation in " + FileUtil.getFileDisplayName(fo) + " indent=" + indentation + " line=" +
//                            doc.getText(offset, Utilities.getRowEnd(doc, offset)-offset), 0, indentation);
//                    break;
//                }
//            } catch (Exception ex) {
//                fail(ex.toString());
//            }
//            
//            // Also try re-lexing buffer incrementally and make sure it makes sense! (and handle bracket completion stuff)
//        }
//    }

    public void testFormat1() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/prototype.js",new IndentPrefs(2,2));
    }

    public void testFormat2() throws Exception {
        reformatFileContents("testfiles/SpryEffects.js",new IndentPrefs(2,2));
    }

    public void testFormat3() throws Exception {
        reformatFileContents("testfiles/dragdrop.js",new IndentPrefs(2,2));
    }

    public void testFormat4() throws Exception {
        reformatFileContents("testfiles/dojo.js.uncompressed.js",new IndentPrefs(2,2));
    }
    
    public void testSimpleBlock() throws Exception {
        format("if (true) {\nfoo();\n  }\n",
               "if (true) {\n    foo();\n}\n", null);
    }

    public void testBraceFreeBlock() throws Exception {
        format("if (true)\nfoo();\nbar();\n",
               "if (true)\n    foo();\nbar();\n", null);
        format("if (true)\nfoo();\nelse\nbar();\n",
               "if (true)\n    foo();\nelse\n    bar();\n", null);
        format("while (true)\nfoo();\nbar();\n",
               "while (true)\n    foo();\nbar();\n", null);
        format("for (i = 0; i < 5; i++)\nfoo();\nbar();",
               "for (i = 0; i < 5; i++)\n    foo();\nbar();", null);
        // What about thesed: do? with?
    }
    
//    public void testLineContinuationAsgn() throws Exception {
//        format("x =\n1",
//               "x =\n  1", null);
//    }
//
//    public void testLineContinuation2() throws Exception {
//        format("x =\n1", 
//               "x =\n    1", new IndentPrefs(2,4));
//    }
//
//    public void testLineContinuation3() throws Exception {
//        format("x =\n1\ny = 5", 
//               "x =\n    1\ny = 5", new IndentPrefs(2,4));
//    }
//
//    public void testLineContinuation4() throws Exception {
//        format("def foo\nfoo\nif true\nx\nend\nend", 
//               "def foo\n  foo\n  if true\n    x\n  end\nend", null);
//    }
//
//    public void testLineContinuation5() throws Exception {
//        format("def foo\nfoo\nif true\nx\nend\nend", 
//               "def foo\n    foo\n    if true\n        x\n    end\nend", new IndentPrefs(4,4));
//    }
//
//    // Trigger lexer bug!
//    //public void testLineContinuationBackslash() throws Exception {
//    //    format("x\\\n= 1", 
//    //           "x\\\n  = 1", new IndentPrefs(2,4));
//    //}
//    
//    public void testLineContinuationComma() throws Exception {
//        format("render foo,\nbar\nbaz",
//               "render foo,\n  bar\nbaz", null);
//    }
//    
//    public void testQuestionmarkIndent1() throws Exception {
//        format("j = t ?\n1 : 0\nx = 1",
//                "j = t ?\n  1 : 0\nx = 1", null);
//    }
//
//    public void testQuestionmarkIndent2() throws Exception {
//        format("j = t ?\n1 :\n0\nx = 1",
//                "j = t ?\n  1 :\n  0\nx = 1", null);
//    }
//
//    public void testCommaIndent() throws Exception {
//        insertNewline("puts foo,^", "puts foo,\n  ^", null);
//    }
//    
//    public void testQuestionmarkIndent3() throws Exception {
//        insertNewline("j = t ?^",
//                "j = t ?\n  ^", null);
//    }
//    
//    public void testBackslashIndent() throws Exception {
//        insertNewline("puts foo\\^", "puts foo\\\n  ^", null);
//    }
//
//    public void testDotIndent() throws Exception {
//        insertNewline("puts foo.^", "puts foo.\n  ^", null);
//    }
//
//    public void testLineContinuationParens() throws Exception {
//        format("foo(1,2\n3,4)\nx",
//               "foo(1,2\n  3,4)\nx", null);
//    }
//
//    public void testLiterals() throws Exception {
//        format("def foo\n  x = %q-foo\nbar-",
//               "def foo\n  x = %q-foo\nbar-", null);
//    }
//
//    public void testLiterals2() throws Exception {
//        insertNewline("def foo\n=begin\nfoo^\n=end\nend",
//                "def foo\n=begin\nfoo\n^\n=end\nend", null);
//    }
//    
//    public void testLiterals3() throws Exception {
//        insertNewline("def foo\nx = '\nfoo^\n'\nend",
//                "def foo\nx = '\nfoo\n^\n'\nend", null);
//    }
//    
//    public void testLineContinuationAlias() throws Exception {
//        format("foo ==\ntrue",
//               "foo ==\n  true", null);
//        format("alias foo ==\ntrue",
//               "alias foo ==\ntrue", null);
//        format("def ==\ntrue",
//               "def ==\n  true", new IndentPrefs(2,4));
//    }
//
//    public void testBrackets() throws Exception {
//        format("x = [[5]\n]\ny",
//               "x = [[5]\n]\ny", null);
//    }
//
//    public void testBrackets2() throws Exception {
//        format("x = [\n[5]\n]\ny",
//               "x = [\n  [5]\n]\ny", null);
//        format("x = [\n[5]\n]\ny",
//               "x = [\n  [5]\n]\ny", new IndentPrefs(2,4));
//    }
//
//    public void testIndent1() throws Exception {
//        insertNewline("x = [^[5]\n]\ny", "x = [\n  ^[5]\n]\ny", null);
//    }
//
//    public void testIndent2() throws Exception {
//        insertNewline("x = ^", "x = \n  ^", null);
//        insertNewline("x = ^", "x = \n    ^", new IndentPrefs(2,4));
//        insertNewline("x = ^ ", "x = \n^  ", null);
//    }
//
//    public void testIndent3() throws Exception {
//        insertNewline("      def foo^", "      def foo\n        ^", null);
//    }
//
//    public void testHeredoc1() throws Exception {
//        format("def foo\n  s = <<EOS\n  stuff\nEOS\nend",
//               "def foo\n  s = <<EOS\n  stuff\nEOS\nend", null);
//    }
//
//    public void testHeredoc2() throws Exception {
//        format("def foo\n  s = <<-EOS\n  stuff\nEOS\nend",
//               "def foo\n  s = <<-EOS\n  stuff\n  EOS\nend", null);
//    }
//    
//    public void testHeredoc3() throws Exception {
//        format("def foo\n    s = <<EOS\nstuff\n  foo\nbar\nEOS\n  end",
//               "def foo\n  s = <<EOS\nstuff\n  foo\nbar\nEOS\nend", null);
//    }
//
//    public void testHeredoc4() throws Exception {
//        format("def foo\n    s = <<-EOS\nstuff\n  foo\nbar\nEOS\n  end",
//               "def foo\n  s = <<-EOS\nstuff\n  foo\nbar\n  EOS\nend", null);
//    }
//
//    public void testArrayDecl() throws Exception {
//        format("@foo = [\n'bar',\n'bar2',\n'bar3'\n]",
//                "@foo = [\n  'bar',\n  'bar2',\n  'bar3'\n]", null);
//    }
//
//    public void testHashDecl() throws Exception {
//        String unformatted = "@foo = {\n" +
//            "'bar' => :foo,\n" +
//            "'bar2' => :bar,\n" +
//            "'bar3' => :baz\n" +
//            "}";
//        String formatted = "@foo = {\n" +
//            "  'bar' => :foo,\n" +
//            "  'bar2' => :bar,\n" +
//            "  'bar3' => :baz\n" +
//            "}";
//        format(unformatted, formatted, null);
//    }
//
//    public void testParenCommaList() throws Exception {
//        String unformatted = "foo(\nx,\ny,\nz\n)";
//        String formatted = "foo(\n" +
//            "  x,\n" +
//            "  y,\n" +
//            "  z\n" +
//            ")";
//        format(unformatted, formatted, null);
//    }
//    
//    public void testDocumentRange1() throws Exception {
//        format("      def foo\n%<%foo%>%\n      end\n", 
//               "      def foo\n        foo\n      end\n", null);
//        format("def foo\nfoo\nend\n", 
//               "def foo\n  foo\nend\n", null);
//    }
//
//    public void testDocumentRange2() throws Exception {
//        format("def foo\n     if true\n           %<%xxx%>%\n     end\nend\n",
//                "def foo\n     if true\n       xxx\n     end\nend\n", null);
//    }    
//
//    public void testDocumentRange3() throws Exception {
//        format("class Foo\n  def bar\n  end\n\n\n%<%def test\nhello\nend%>%\nend\n",
//               "class Foo\n  def bar\n  end\n\n\n  def test\n    hello\n  end\nend\n", null);
//    }    
//    
//    public void testPercentWIndent110983a() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %w[a b c]^\n    blah",
//            "class Apple\n  def foo\n    snark %w[a b c]\n    ^\n    blah", null);
//    }
//
//    public void testPercentWIndent110983b() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %w,a b c,^\n    blah",
//            "class Apple\n  def foo\n    snark %w,a b c,\n    ^\n    blah", null);
//    }
//
//    public void testPercentWIndent110983c() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %w/a/^\n    blah",
//            "class Apple\n  def foo\n    snark %w/a/\n    ^\n    blah", null);
//    }
//    
//    public void testPercentWIndent110983d() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %W[a b c]^\n    blah",
//            "class Apple\n  def foo\n    snark %W[a b c]\n    ^\n    blah", null);
//    }
//
//    public void testPercentWIndent110983e() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %Q[a b c]^\n    blah",
//            "class Apple\n  def foo\n    snark %Q[a b c]\n    ^\n    blah", null);
//    }
//
//    public void testEof() throws Exception {
//        format("def foo\n     if true\n           %<%xxx%>%\n     end\nend\n",
//                "def foo\n     if true\n       xxx\n     end\nend\n", null);
//        format("x\n",
//               "x\n", null);
//    }
    
    public class IndentPrefs {

        private final int hanging;

        private final int indent;

        public IndentPrefs(int indent, int hanging) {
            super();
            this.indent = indent;
            this.hanging = hanging;
        }

        public int getIndentation() {
            return indent;
        }

        public int getHangingIndentation() {
            return hanging;
        }
    }
    
}

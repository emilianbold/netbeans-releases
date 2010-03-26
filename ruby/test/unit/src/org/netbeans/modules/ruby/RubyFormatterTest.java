/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.util.List;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @todo Test partial reformatting (indentation in the middle of a file, newline computation in the middle of a file)
 * @todo Partial test: Make sure I don't reformat the line AFTER the newline insertion when I hit newline!
 * 
 * @author Tor Norbye
 */
public class RubyFormatterTest extends RubyTestBase {

    public RubyFormatterTest(String testName) {
        super(testName);
    }

    private void reformatFileContents(String file) throws Exception {
        reformatFileContents(file, new IndentPrefs(2, 2));
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

    public void testReformatAll() {
        // Find ruby files
        List<FileObject> files = findJRubyRubyFiles();
        assertTrue(files.size() > 0);

        reformatAll(files);
    }

    private void reformatAll(List<FileObject> files) {
        IndentPrefs preferences = new IndentPrefs(2, 2);
        RubyFormatter formatter = getFormatter(preferences);

        int fileCount = files.size();
        int count = 0;

        // indent each one
        for (FileObject fo : files) {
            count++;

            if (fo.getNameExt().equals("mvm_subvm.rb")) {
                // This file is okay and shouldn't end in column 0
                continue;
            }

            // triggers #134931, in rails 2.3.2 the file has a 'def class' method
            if (fo.getName().equals("deprecation") && fo.getParent().getName().equals("active_support")) {
                System.err.println("SKIPPING known bad file " + fo.getNameExt());
                continue;
            }

            if (fo.getName().equals("delegating_attributes")) {
                System.err.println("SKIPPING known bad file " + fo.getNameExt());
                continue;
            }

            if (fo.getName().equals("sample_02") || fo.getName().equals("sample_03")) {
                System.err.println("Can't properly format sample_02.rb yet - it's unusual" + fo.getNameExt());
                continue;
            }

            // Fails due to #182494
            if (fo.getName().equals("indexer") && fo.getParent().getName().equals("rubygems")) {
                System.err.println("SKIPPING known bad file " + fo.getNameExt());
                continue;
            }

            // Fails due to #182761
            if ((fo.getName().equals("mathn") || fo.getName().equals("rational"))
                    && fo.getParent().getNameExt().equals("1.9")) {
                System.err.println("SKIPPING known bad file " + fo.getNameExt());
                continue;
            }
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
            // When erubis is installed:
            if (fo.getNameExt().equals("test-enhancers.rb") || fo.getNameExt().equals("test-erubis.rb")) {
                System.err.println("SKIPPING " + fo.getNameExt() + " - the lexing of data after __END__ doesn't seem to work");
                continue;
            }
            System.err.println("Formatting file " + count + "/" + files.size() + " : " + FileUtil.getFileDisplayName(fo));

            // check that we end up at indentation level 0
            BaseDocument doc = getDocument(fo);

            try {
                format(doc, formatter, 0, doc.getLength(), true);
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
                        offset = offset - 1;
                        continue;
                    }

                    int indentation = Utilities.getRowFirstNonWhite(doc, offset) - offset;

                    if (indentation != 0) {
                        // Make sure the file actually compiles - we might be picking up some testfiles in the
                        // JRuby tree which don't actually pass
                        if (AstUtilities.getRoot(fo) == null) {
                            System.err.println("WARNING - invalid formatting for " + FileUtil.getFileDisplayName(fo) + " " +
                                    "but it also doesn't parse with JRuby so ignoring");
                            break;
                        }
                    }


                    assertEquals("Failed formatting file " + count + "/" + fileCount + " \n" + fo.getNameExt() + "\n: Last line not at 0 indentation in " + FileUtil.getFileDisplayName(fo) + " indent=" + indentation + " line=" +
                            doc.getText(offset, Utilities.getRowEnd(doc, offset) - offset), getExpectedIndentation(fo), indentation);
                    break;
                }
            } catch (Exception ex) {
                fail(ex.toString());
            }

            // Also try re-lexing buffer incrementally and make sure it makes sense! (and handle bracket completion stuff)
        }
    }

    private static int getExpectedIndentation(FileObject fo) {
        // the last statement in this file uses a ternary operator
        if (fo.getPath().endsWith("actionpack-2.3.2/test/abstract_unit.rb")) {
            return 2;
        }
        return 0;
    }

    public void testFormatApe() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/ape.rb");
    }

    public void testFormatMephisto1() throws Exception {
        reformatFileContents("testfiles/mephisto-site.rb");
    }

    public void testFormatMephisto2() throws Exception {
        reformatFileContents("testfiles/mephisto_controller.rb");
    }

    public void testFormatMephisto3() throws Exception {
        reformatFileContents("testfiles/mephisto-articles-controller.rb");
    }

    public void testFormat110332() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/percent-expressions.rb");
    }

    public void testFormatDate() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/date.rb");
    }

    public void testFormatResolv() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/resolv.rb");
    }

    public void testFormatBegin() throws Exception {
        // Test for http://scripting.netbeans.org/issues/show_bug.cgi?id=112259
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/begin.rb");
    }

    public void testFormatPostgres() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/postgresql_adapter.rb");
    }

    public void testLineContinuationAsgn() throws Exception {
        format("x =\n1",
                "x =\n  1", null);
    }

    // Separate setting for line continuations not yet supported
    //public void testLineContinuation2() throws Exception {
    //    format("x =\n1",
    //           "x =\n    1", new IndentPrefs(2,4));
    //}
    //
    //public void testLineContinuation3() throws Exception {
    //    format("x =\n1\ny = 5",
    //           "x =\n    1\ny = 5", new IndentPrefs(2,4));
    //}
    public void testLineContinuation4() throws Exception {
        format("def foo\nfoo\nif true\nx\nend\nend",
                "def foo\n  foo\n  if true\n    x\n  end\nend", null);
    }

    public void testLineContinuation5() throws Exception {
        format("def foo\nfoo\nif true\nx\nend\nend",
                "def foo\n    foo\n    if true\n        x\n    end\nend", new IndentPrefs(4, 4));
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

    public void testQuestionmarkIndent1() throws Exception {
        format("j = t ?\n1 : 0\nx = 1",
                "j = t ?\n  1 : 0\nx = 1", null);
    }

    public void testQuestionmarkIndent2() throws Exception {
        format("j = t ?\n1 :\n0\nx = 1",
                "j = t ?\n  1 :\n  0\nx = 1", null);
    }

    public void testCommaIndent() throws Exception {
        insertNewline("puts foo,^", "puts foo,\n  ^", null);
    }

    public void testQuestionmarkIndent3() throws Exception {
        insertNewline("j = t ?^",
                "j = t ?\n  ^", null);
    }

    public void testBackslashIndent() throws Exception {
        insertNewline("puts foo\\^", "puts foo\\\n  ^", null);
    }

    public void testDotIndent() throws Exception {
        insertNewline("puts foo.^", "puts foo.\n  ^", null);
    }

    public void testLineContinuationParens() throws Exception {
        format("foo(1,2\n3,4)\nx",
                "foo(1,2\n  3,4)\nx", null);
    }

    public void testLiterals() throws Exception {
        format("def foo\n  x = %q-foo\nbar-",
                "def foo\n  x = %q-foo\nbar-", null);
    }

    public void testLiterals2() throws Exception {
        insertNewline("def foo\n=begin\nfoo^\n=end\nend",
                "def foo\n=begin\nfoo\n^\n=end\nend", null);
    }

    public void testLiterals3() throws Exception {
        insertNewline("def foo\nx = '\nfoo^\n'\nend",
                "def foo\nx = '\nfoo\n^\n'\nend", null);
    }

    public void testLineContinuationAlias() throws Exception {
        format("foo ==\ntrue",
                "foo ==\n  true", null);
        format("alias foo ==\ntrue",
                "alias foo ==\ntrue", null);
        // Different hangingindent not yet supported in the UI
        //format("def ==\ntrue",
        //       "def ==\n  true", new IndentPrefs(2,4));
    }

    public void testBrackets() throws Exception {
        format("x = [[5]\n]\ny",
                "x = [[5]\n]\ny", null);
    }

    public void testBrackets2() throws Exception {
        format("x = [\n[5]\n]\ny",
                "x = [\n  [5]\n]\ny", null);
        // Different hangingindent not yet supported in the UI
        //format("x = [\n[5]\n]\ny",
        //       "x = [\n  [5]\n]\ny", new IndentPrefs(2,4));
    }

    public void testIndent1() throws Exception {
        insertNewline("x = [^[5]\n]\ny", "x = [\n  ^[5]\n]\ny", null);
    }

    public void testIndent2() throws Exception {
        insertNewline("x = ^", "x = \n  ^", null);
        //insertNewline("x = ^", "x = \n    ^", new IndentPrefs(2,4));
        insertNewline("x = ^ ", "x = \n  ^", null);
    }

    public void testIndent3() throws Exception {
        insertNewline("      def foo^", "      def foo\n        ^\n      end", null);
    }

    public void testHeredoc1() throws Exception {
        format("def foo\n  s = <<EOS\n  stuff\nEOS\nend",
                "def foo\n  s = <<EOS\n  stuff\nEOS\nend", null);
    }

    public void testHeredoc2() throws Exception {
        format("def foo\n  s = <<-EOS\n  stuff\nEOS\nend",
                "def foo\n  s = <<-EOS\n  stuff\n  EOS\nend", null);
    }

    public void testHeredoc3() throws Exception {
        format("def foo\n    s = <<EOS\nstuff\n  foo\nbar\nEOS\n  end",
                "def foo\n  s = <<EOS\nstuff\n  foo\nbar\nEOS\nend", null);
    }

    public void testHeredoc4() throws Exception {
        format("def foo\n    s = <<-EOS\nstuff\n  foo\nbar\nEOS\n  end",
                "def foo\n  s = <<-EOS\nstuff\n  foo\nbar\n  EOS\nend", null);
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

    public void testDocumentRange3() throws Exception {
        format("class Foo\n  def bar\n  end\n\n\n%<%def test\nhello\nend%>%\nend\n",
                "class Foo\n  def bar\n  end\n\n\n  def test\n    hello\n  end\nend\n", null);
    }

    public void testPercentWIndent110983a() throws Exception {
        insertNewline(
                "class Apple\n  def foo\n    snark %w[a b c]^\n    blah",
                "class Apple\n  def foo\n    snark %w[a b c]\n    ^\n    blah", null);
    }

    public void testPercentWIndent110983b() throws Exception {
        insertNewline(
                "class Apple\n  def foo\n    snark %w,a b c,^\n    blah",
                "class Apple\n  def foo\n    snark %w,a b c,\n    ^\n    blah", null);
    }

    public void testPercentWIndent110983c() throws Exception {
        insertNewline(
                "class Apple\n  def foo\n    snark %w/a/^\n    blah",
                "class Apple\n  def foo\n    snark %w/a/\n    ^\n    blah", null);
    }

    public void testPercentWIndent110983d() throws Exception {
        insertNewline(
                "class Apple\n  def foo\n    snark %W[a b c]^\n    blah",
                "class Apple\n  def foo\n    snark %W[a b c]\n    ^\n    blah", null);
    }

    public void testPercentWIndent110983e() throws Exception {
        insertNewline(
                "class Apple\n  def foo\n    snark %Q[a b c]^\n    blah",
                "class Apple\n  def foo\n    snark %Q[a b c]\n    ^\n    blah", null);
    }

    public void testEof() throws Exception {
        format("def foo\n     if true\n           %<%xxx%>%\n     end\nend\n",
                "def foo\n     if true\n       xxx\n     end\nend\n", null);
        format("x\n",
                "x\n", null);
    }
}

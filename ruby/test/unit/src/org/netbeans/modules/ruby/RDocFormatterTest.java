/*
 * RDocFormatterTest.java
 * JUnit based test
 *
 * Created on March 9, 2007, 12:18 PM
 */

package org.netbeans.modules.ruby;

import junit.framework.TestCase;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tor
 */
public class RDocFormatterTest extends RubyTestBase {
    
    public RDocFormatterTest(String testName) {
        super(testName);
    }
    
    public void formatFile(String file) throws Exception {
        FileObject fileObj = getTestFile(file);
        BaseDocument doc = getDocument(fileObj);
        RDocFormatter formatter = new RDocFormatter();

        int offset = 0;
        int length = doc.getLength();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>\n");
        boolean started = false;
        boolean inDoc = false;
        for (; offset < length; offset = Utilities.getRowEnd(doc, offset) + 1) {
            if (inDoc && (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset))) {
                String line = doc.getText(offset, Utilities.getRowEnd(doc, offset)-offset);
                formatter.appendLine(line);
                continue;
            }
            int start = Utilities.getRowFirstNonWhite(doc, offset);
            if (start != -1) {
                String line = doc.getText(offset, Utilities.getRowEnd(doc, offset)-offset);
                if (inDoc && line.startsWith("=end")) {
                    inDoc = false;
                    continue;
                }
                Token<? extends TokenId> token = LexUtilities.getToken(doc, start);
                if (token.id() == RubyTokenId.LINE_COMMENT) {
                    started = true;
                    line = doc.getText(start, Utilities.getRowEnd(doc, start)-start);
                    formatter.appendLine(line);
                    continue;
                } else if (token.id() == RubyTokenId.DOCUMENTATION) {
                    started = true;
                    if (line.startsWith("=begin")) {
                        inDoc = true;
                        continue;
                    }
                    formatter.appendLine(line);
                    continue;
                }
            }
            if (started) {
                sb.append("<hr>\n");
                if (start != -1) {
                    sb.append("<pre>");
                    String line = doc.getText(offset, Utilities.getRowEnd(doc, offset)-offset);
                    sb.append(line);
                    sb.append("</pre>");
                }
                sb.append("<br/>\n");
                sb.append(formatter.toHtml());
                // Peek ahead to find the definition
                formatter = new RDocFormatter();
                started = false;
            }
        }
        if (started) {
            sb.append("<h2 style=\"color: green\">Next Comment</h2>\n");
            sb.append(formatter.toHtml());
        }
        sb.append("</body></html>\n");
        
        assertDescriptionMatches(file, sb.toString(), false, ".html");
    }
    
    public void testFormatBig1() throws Exception {
        formatFile("testfiles/resolv.rb");
    }

    public void testFormatBig2() throws Exception {
        formatFile("testfiles/date.rb");
    }

    public void testToHtml() {
        RDocFormatter instance = new RDocFormatter();

        instance.appendLine("# Following does not work:<BR>\n");
        instance.appendLine("# test _italic_<BR>\n");
        instance.appendLine("# test *bold*<BR>\n");
        instance.appendLine("# <P>\n");
        instance.appendLine("# Following does:<BR>\n");
        instance.appendLine("# test +typewriter+<BR>\n");
        instance.appendLine("# test <em>italic_html</em><BR>\n");
        instance.appendLine("# test <b>bold_html</b><BR>\n");
        instance.appendLine("# test <tt>typewriter_html</tt><BR>\n");

        String expected = 
                
"Following does not work:<BR>\n" +
" test <i>italic</i><BR>\n" +
" test <b>bold</b><BR>\n" +
" <P>\n" +
" Following does:<BR>\n" +
" test <tt>typewriter</tt><BR>\n" +
" test <em>italic_html</em><BR>\n" +
" test <b>bold_html</b><BR>\n" +
" test <tt>typewriter_html</tt><BR>\n ";        
        String html = instance.toHtml();
                
        assertEquals(expected, html);
    }
    
    public void testEscape() {
        RDocFormatter instance = new RDocFormatter();
        
        instance.appendLine("#   class EliteGenerator < Rails::Generator::Base");
        
        assertEquals("<pre>\n  class EliteGenerator &lt; Rails::Generator::Base\n</pre>\n", instance.toHtml());
    }

    public void testNoEscape() {
        RDocFormatter instance = new RDocFormatter();
        
        instance.appendLine("# class EliteGenerator <b>Rails::Generator::Base</b>");

        assertEquals("class EliteGenerator <b>Rails::Generator::Base</b> ", instance.toHtml());
    }
    
    public void testNoLabelSpace() {
        RDocFormatter instance = new RDocFormatter();
        instance.appendLine("# [<tt>:id</tt>]");
        assertEquals("<table>\n<tr><td valign=\"top\"><tt>:id</tt> </td><td> </td></tr>\n</table>", instance.toHtml());
    }

    public void testNoNumberSpace() {
        RDocFormatter instance = new RDocFormatter();
        instance.appendLine("# 4.");
        assertEquals("<ol>\n<li value=\"4\"></ol>\n", instance.toHtml());
    }

    public void testNumber() {
        RDocFormatter instance = new RDocFormatter();
        instance.appendLine("# 4. Hello");
        assertEquals("<ol>\n<li value=\"4\">Hello </ol>\n", instance.toHtml());
    }

    public void testNoTableSpace() {
        RDocFormatter instance = new RDocFormatter();
        instance.appendLine("# Next::");
        assertEquals("<table>\n<tr><td valign=\"top\">Next </td><td> </td></tr>\n</table>", instance.toHtml());
    }
    
    public void testCodeFormatting() {
        RDocFormatter instance = new RDocFormatter();
        instance.setSeqName("basename");

        instance.appendLine("#     File.basename(file_name [, suffix] ) -> base_name\n");
        instance.appendLine("#\n");
        instance.appendLine("#\n");
        instance.appendLine("# Returns the last component of the filename given in <i>file_name</i>,\n");
        instance.appendLine("# which must be formed using forward slashes (``<code>/</code>'')\n");
        instance.appendLine("# regardless of the separator used on the local file system. If\n");
        instance.appendLine("# <i>suffix</i> is given and present at the end of <i>file_name</i>,\n");
        instance.appendLine("# it is removed.\n");
        instance.appendLine("#\n");
        instance.appendLine("#    File.basename(\"/home/gumby/work/ruby.rb\")          #=> \"ruby.rb\"\n");
        instance.appendLine("#    File.basename(\"/home/gumby/work/ruby.rb\", \".rb\")   #=> \"ruby\"\n");
        instance.appendLine("#\n");
        instance.appendLine("#\n");
        
        // TODO - no formatting here because the font+color lookup isn't working at test time
        String expected = 
"<pre>\n" +
"File.<b>basename</b>(file_name [, suffix] ) -> base_name\n" +
"<br></pre>\n" +
"<hr>\n" +                
"#\n" +
" #\n" +
" Returns the last component of the filename given in <i>file_name</i>,\n" +
" which must be formed using forward slashes (``<code>/</code>'')\n" +
" regardless of the separator used on the local file system. If\n" +
" <i>suffix</i> is given and present at the end of <i>file_name</i>,\n" +
" it is removed.\n" +
" #\n" +
" <pre>\n" +
"   File.basename(\"/home/gumby/work/ruby.rb\")          #=> \"ruby.rb\"\n" +
"\n" +
"   File.basename(\"/home/gumby/work/ruby.rb\", \".rb\")   #=> \"ruby\"\n" +
"\n" +
"</pre>\n" +
"#\n" +
" #\n ";
        String html = instance.toHtml();
                
        assertEquals(expected, html);
    }
    
    public void testEndTag() {
        RDocFormatter instance = new RDocFormatter();
        
        instance.appendLine("# <b>whatever</b>");
        assertEquals("<b>whatever</b> ", instance.toHtml());
    }

    // TODO test bullets, labels, preformat
}

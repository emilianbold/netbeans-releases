/*
 * RDocFormatterTest.java
 * JUnit based test
 *
 * Created on March 9, 2007, 12:18 PM
 */

package org.netbeans.modules.ruby;

import junit.framework.TestCase;

/**
 *
 * @author tor
 */
public class RDocFormatterTest extends TestCase {
    
    public RDocFormatterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
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

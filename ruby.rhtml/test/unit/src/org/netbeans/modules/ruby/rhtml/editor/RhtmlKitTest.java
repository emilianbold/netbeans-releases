/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.rhtml.editor;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import org.netbeans.modules.ruby.rhtml.RhtmlTestBase;

/**
 *
 * @author Tor Norbye
 */
public class RhtmlKitTest extends RhtmlTestBase {
    
    public RhtmlKitTest(String testName) {
        super(testName);
    }

    public void toggleComment(String text, String expected) throws Exception {
        JEditorPane pane = getPane(text);

        RhtmlKit kit = (RhtmlKit)pane.getEditorKit();
        Action a = kit.getActionByName("comment"); // Should be toggle-comment, fix in GLF
        assertNotNull(a);
        a.actionPerformed(new ActionEvent(pane, 0, ""));

        String toggled = pane.getText();
        assertEquals(expected, toggled);
    }
    
    public void testToggleComment1() throws Exception {
        toggleComment("fo^o", "<%#*foo%>");
    }

    public void testToggleComment2() throws Exception {
        toggleComment("<%#*f^oo%>", "foo");
    }

    public void testToggleComment3() throws Exception {
        toggleComment("<% ruby^ %>", "<%# ruby %>");
    }
    
    public void testToggleComment4() throws Exception {
        toggleComment("<%# ruby^ %>", "<% ruby %>");
    }

    public void testToggleComment5() throws Exception {
        toggleComment("foo\n<div>\n  <span>^</span>  \n  <%= rubyexp %> \n",
                      "foo\n<div>\n  <%#*<span></span>%>  \n  <%= rubyexp %> \n");
    }
    
    public void testToggleComment6() throws Exception {
        toggleComment("foo\n<div>\n  $start$<span></span>$end$  \n  \n  <%= rubyexp %> \n",
                      "foo\n<div>\n  <%#*<span></span>%>  \n  \n  <%= rubyexp %> \n");
    }

    public void testToggleComment7() throws Exception {
        toggleComment("$start$foo\n<div>\n  <span></span>  \n  \n  <%= rubyexp %> \n$end$",
                      "<%#*foo%>\n<%#*<div>%>\n  <%#*<span></span>%>  \n  \n  <%#= rubyexp %> \n");
    }

    public void testToggleComment8() throws Exception {
        toggleComment("$start$<%#*foo%>\n<%#*<div>%>\n  <%#*<span></span>%>  \n  \n  <%#= rubyexp %> \n$end$",
                     "foo\n<div>\n  <span></span>  \n  \n  <%= rubyexp %> \n");
    }

    public void testToggleComment9() throws Exception {
        toggleComment("<div>\n  <% ruby1\n  ru^by2\n  ruby3 %> \n</div>\n",
                      "<div>\n  <% ruby1\n  #ruby2\n  ruby3 %> \n</div>\n");
    }
    
    public void testToggleComment10() throws Exception {
        toggleComment("<div>\n  <% ruby1\n  #ru^by2\n  ruby3 %> \n</div>\n",
                      "<div>\n  <% ruby1\n  ruby2\n  ruby3 %> \n</div>\n");
    }

    public void testToggleComment11() throws Exception {
        toggleComment("$start$<div>\n  <% ruby1\n  ruby2\n  ruby3 %> \n</div>\n$end$",
                             "<%#*<div>%>\n  <%# ruby1\n  ruby2\n  ruby3 %> \n<%#*</div>%>\n");
    }
    
    public void testToggleComment12() throws Exception {
        toggleComment("$start$<%#*<div>%>\n  <%# ruby1\n  ruby2\n  ruby3 %> \n<%#*</div>%>\n$end$", 
                             "<div>\n  <% ruby1\n  ruby2\n  ruby3 %> \n</div>\n");
    }
    
    public void testToggleComment13() throws Exception {
        toggleComment("<% #ruby^ %>", "<% ruby %>");
    }
    
    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }
}

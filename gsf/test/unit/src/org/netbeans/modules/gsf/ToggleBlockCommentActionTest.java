/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.gsf;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.junit.Test;
import org.netbeans.modules.gsf.spi.CommentHandler;
import static org.junit.Assert.*;

/**
 *
 * @author marekfukala
 */
public class ToggleBlockCommentActionTest extends GsfTestBase {

    private CommentHandler handler = new TestCommentHandler();

    public ToggleBlockCommentActionTest() {
        super(ToggleBlockCommentAction.class.getName());
    }

    @Test
    public void testBasic() {
        Document doc = createDocument("hello /** comment */ world");
        int[] comments = handler.getCommentBlocks(doc, 0, doc.getLength());

        assertNotNull(comments);
        assertEquals(2, comments.length);
    }

    @Test
    public void testBasicComment() throws BadLocationException {
        String text = "hello world";
        //             01234567890

        String commented =
                      "/**" + text + "*/";

        Document doc = createDocument(text);
        JEditorPane pane = new JEditorPane("text/plain", null);
        pane.setDocument(doc);
        pane.setSelectionStart(0);
        pane.setSelectionEnd(text.length());
        pane.getCaret().setSelectionVisible(true);

        ToggleBlockCommentAction action = new ToggleBlockCommentAction(handler);
        action.actionPerformed(null, pane);

        String content = doc.getText(0, doc.getLength());
        assertEquals(commented, content);

        //test selection update
        assertEquals(0, pane.getSelectionStart());
        assertEquals(commented.length(), pane.getSelectionEnd());
    }

    @Test
    public void testBasicUncomment() throws BadLocationException {
        String text = "hello world";
        //             01234567890

        String commented =
                      "/**" + text + "*/";

        Document doc = createDocument(commented);
        JEditorPane pane = new JEditorPane("text/plain", null);
        pane.setDocument(doc);
        pane.setSelectionStart(0);
        pane.setSelectionEnd(commented.length());
        pane.getCaret().setSelectionVisible(true);


        ToggleBlockCommentAction action = new ToggleBlockCommentAction(handler);
        action.actionPerformed(null, pane);

        String content = doc.getText(0, doc.getLength());
        System.out.println(content);
        assertEquals(text, content);

         //test selection update
        assertEquals(0, pane.getSelectionStart());
        assertEquals(content.length(), pane.getSelectionEnd());
    }

    @Test
    public void testLineComment() throws BadLocationException {
        String text = "hello world";
        //             01234567890

        Document doc = createDocument(text);
        JEditorPane pane = new JEditorPane("text/plain", null);
        pane.setDocument(doc);
        pane.getCaret().setDot(1);
        pane.getCaret().setSelectionVisible(true);

        ToggleBlockCommentAction action = new ToggleBlockCommentAction(handler);
        action.actionPerformed(null, pane);

        String content = doc.getText(0, doc.getLength());
        assertEquals("/**" + text + "*/", content);
    }

    @Test
    public void testLineUncomment() throws BadLocationException {
        String text = "hello world";
        //             01234567890

        String commented =
                      "/**" + text + "*/";

        Document doc = createDocument(commented);
        JEditorPane pane = new JEditorPane("text/plain", null);
        pane.setDocument(doc);
        pane.getCaret().setDot(1);
        pane.getCaret().setSelectionVisible(true);

        ToggleBlockCommentAction action = new ToggleBlockCommentAction(handler);
        action.actionPerformed(null, pane);

        String content = doc.getText(0, doc.getLength());
        assertEquals(text, content);
    }

     private static class TestCommentHandler extends CommentHandler.DefaultCommentHandler {

        public String getCommentStartDelimiter() {
            return "/**";
        }

        public String getCommentEndDelimiter() {
            return "*/";
        }

    }

}

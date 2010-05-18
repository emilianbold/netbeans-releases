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

package org.netbeans.modules.gsf.spi;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

/**
 *
 * @author marekfukala
 */
public class CommentHandlerTest extends NbTestCase {

    public CommentHandlerTest() {
        super(CommentHandlerTest.class.getName());
    }

    private CommentHandler handler = new TestCommentHandler();

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
    }

    @After
    @Override
    public void tearDown() {
    }

    /**
     * Test of getCommentBlocks method, of class CommentHandler.
     */
    @Test
    public void testNaive() {
        assertEquals("/**", handler.getCommentStartDelimiter());
        assertEquals("*/", handler.getCommentEndDelimiter());
        
        Document doc = new PlainDocument();
        assertNotNull(handler.getCommentBlocks(doc, 0, 0));
    }

    @Test
    public void testBasic() throws BadLocationException {
        Document doc = new PlainDocument();
        doc.insertString(0, "hello /** comment */ world", null);
        //                   01234567890123456789012345
        //                   0         1         2

        int[] comments = handler.getCommentBlocks(doc, 0, doc.getLength());
        assertNotNull(comments);
        assertEquals(2, comments.length);
        assertEquals(6, comments[0]);
        assertEquals(20, comments[1]);

        comments = handler.getCommentBlocks(doc, 6, 20);
        assertNotNull(comments);
        assertEquals(2, comments.length);
        assertEquals(6, comments[0]);
        assertEquals(20, comments[1]);

        comments = handler.getCommentBlocks(doc, 10, 10);
        assertNotNull(comments);
        assertEquals(2, comments.length);
        assertEquals(6, comments[0]);
        assertEquals(20, comments[1]);

        comments = handler.getCommentBlocks(doc, 6, 6);
        assertNotNull(comments);
        assertEquals(2, comments.length);
        assertEquals(6, comments[0]);
        assertEquals(20, comments[1]);

        comments = handler.getCommentBlocks(doc, 20, 22);
        assertNotNull(comments);
        assertEquals(0, comments.length);

        comments = handler.getCommentBlocks(doc, 22, 22);
        assertNotNull(comments);
        assertEquals(0, comments.length);

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

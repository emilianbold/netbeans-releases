/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.editor.ext.html.parser;

import javax.swing.text.BadLocationException;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.HtmlSyntaxSupport;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.Registry;
import org.netbeans.editor.ext.html.parser.AstNode.NodeType;
import org.netbeans.editor.ext.html.test.TestBase;

/**
 *
 * @author mfukala@netbeans.org
 */
public class AstNodeUtilsTest extends TestBase {

    private static final LanguagePath languagePath = LanguagePath.get(HTMLTokenId.language());

    public AstNodeUtilsTest(String testName) {
        super(testName);
    }

//    public static Test suite(){
//	TestSuite suite = new TestSuite();
//        suite.addTest(new SyntaxTreeTest("testFindDescendant"));
//        return suite;
//    }
    
    public void testFindDescendant() throws Exception {
        String code = "<p><a>text</a></p>";
        //             0123456789012345678

        AstNode root = parse(code, null);
        assertNotNull(root);

        assertDescendant(root, 0, "p", NodeType.OPEN_TAG, 0, 18);
        assertDescendant(root, 4, "a", NodeType.OPEN_TAG, 3, 14);
        assertDescendant(root, 8, "a", NodeType.OPEN_TAG, 3, 14);
        AstNode node = assertDescendant(root, 12, "a", NodeType.OPEN_TAG, 3, 14);
        AstNode adjusted = AstNodeUtils.getTagNode(node, 12);

        assertNotNull(adjusted);
        assertEquals(10, adjusted.startOffset());
        assertEquals(14, adjusted.endOffset());
        assertEquals(AstNode.NodeType.ENDTAG, adjusted.type());

        assertDescendant(root, 17, "p", NodeType.OPEN_TAG, 0, 18);

    }

    private AstNode assertDescendant(AstNode searchedNode, int searchOffset, String name, AstNode.NodeType type, int from, int to) {
        AstNode node = AstNodeUtils.findDescendant(searchedNode, searchOffset);
        assertNotNull(node);
        assertEquals(name, node.name());
        assertEquals(type, node.type());
        int[] range = node.getLogicalRange();
        assertNotNull(range);
        assertEquals(from, range[0]);
        assertEquals(to, range[1]);

        return node;
    }

    private AstNode parse(String code, String publicId) throws BadLocationException {
        BaseDocument doc = createDocument();
        doc.insertString(0, code, null);
        HtmlSyntaxSupport sup = HtmlSyntaxSupport.get(doc);
        assertNotNull(sup);

        DTD dtd;
        if (publicId == null) {
            dtd = sup.getDTD();
        } else {
            dtd = Registry.getDTD(publicId, null);
            assertEquals(publicId, dtd.getIdentifier());
        }

        assertNotNull(dtd);
        SyntaxParser parser = SyntaxParser.get(doc, languagePath);
        parser.forceParse();
        return SyntaxTree.makeTree(parser.elements(), dtd);
    }
}

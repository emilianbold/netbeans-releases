/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.editor.ext.html.parser;

import java.net.URI;
import java.util.Map;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class SyntaxParserResultTest extends TestBase {

    public SyntaxParserResultTest(String testName) {
        super(testName);
    }

    
    public void testBasic() {
        String code = "<html><head><title>xxx</title></head><body>yyy</body></html>";
        SyntaxParserResult result = SyntaxParser.parse(code);

        assertNotNull(result);
        assertNotNull(result.getSource());
        assertNotNull(result.getElements());

        assertNull(result.getPublicID()); //not specified
        DTD dtd = result.getDTD(); //fallback
        assertNotNull(dtd);

        assertNotNull(result.getASTRoot());

    }

    public void testInvalidPublicId() {
        String code = "<!DOCTYPE HTML PUBLIC \"invalid_public_id\"><html><head><title>xxx</title></head><body>yyy</body></html>";
        SyntaxParserResult result = SyntaxParser.parse(code);

        assertNotNull(result);

        assertNotNull(result.getPublicID());
        assertEquals("invalid_public_id", result.getPublicID());

        DTD dtd = result.getDTD(); //fallback
        assertNotNull(dtd);

        assertNotNull(result.getASTRoot());

    }

    public void testGetGlobalNamespaces() {
        String code = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:jsp=\"http://java.sun.com/JSP/Page\"></html>";
        SyntaxParserResult result = SyntaxParser.parse(code);

        assertNotNull(result);

        Map<String, URI> nsmap = result.getGlobalNamespaces();

        assertNotNull(nsmap);
        assertEquals(2, nsmap.keySet().size());

        assertTrue(nsmap.containsKey(""));
        assertTrue(nsmap.containsKey("jsp"));

        assertEquals("http://www.w3.org/1999/xhtml", nsmap.get("").toString());
        assertEquals("http://java.sun.com/JSP/Page", nsmap.get("jsp").toString());
    }
    
    public void testGetDeclaredNamespaces() {
        String code = "<html xmlns=\"http://www.w3.org/1999/xhtml\" " +
                "xmlns:jsp=\"http://java.sun.com/JSP/Page\">" +
                "<ui:composition xmlns:ui=\"http://java.sun.com/jsf/facelets\"/>" +
                "</html>";
        
        SyntaxParserResult result = SyntaxParser.parse(code);

        assertNotNull(result);

        Map<String, String> nsmap = result.getDeclaredNamespaces();

        assertNotNull(nsmap);
        assertEquals(3, nsmap.keySet().size());

        assertTrue(nsmap.containsKey("http://www.w3.org/1999/xhtml"));
        assertTrue(nsmap.containsKey("http://java.sun.com/JSP/Page"));
        assertTrue(nsmap.containsKey("http://java.sun.com/jsf/facelets"));

        assertEquals(null, nsmap.get("http://www.w3.org/1999/xhtml"));
        assertEquals("ui", nsmap.get("http://java.sun.com/jsf/facelets"));
        assertEquals("jsp", nsmap.get("http://java.sun.com/JSP/Page"));
        
    }

    public void testGetAstRoot() {
        String code = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:ui=\"http://java.sun.com/jsf/facelets\">" +
                    "<ui:composition><div><ui:define></ui:define></div></ui:composition>" +
                "</html>";

        SyntaxParserResult result = SyntaxParser.parse(code);

        AstNode froot = result.getASTRoot("http://java.sun.com/jsf/facelets");
        assertNotNull(froot);
        assertEquals(2, froot.children().size());
        assertNotNull(AstNodeUtils.query(froot, "ui:composition"));
        assertNotNull(AstNodeUtils.query(froot, "ui:composition/ui:define"));

        AstNode root = result.getASTRoot();
        assertNotNull(root);
        assertEquals(2, root.children().size());
        assertNotNull(AstNodeUtils.query(root, "html"));
        assertNotNull(AstNodeUtils.query(root, "html/div"));
        
    }

}
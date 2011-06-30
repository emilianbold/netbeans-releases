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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.editor.ext.html.parser.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.editor.ext.html.parser.SyntaxAnalyzer;
import java.util.Map;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.spi.EmptyResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.ParseResult;
import org.netbeans.editor.ext.html.parser.spi.UndeclaredContentResolver;
import org.netbeans.editor.ext.html.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class SyntaxAnalyzerResultTest extends TestBase {

    public SyntaxAnalyzerResultTest(String testName) {
        super(testName);
    }


    @Override
    protected void setUp() throws Exception {
        HtmlVersionTest.setDefaultHtmlVersion(HtmlVersion.HTML41_TRANSATIONAL);
        super.setUp();
    }

    public void testGetHtmlTagDefaultNamespace() {
        String code = "<html xmlns=\"namespace\"><head><title>xxx</title></head><body>yyy</body></html>";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();
        assertNotNull(result);
        assertEquals("namespace", result.getHtmlTagDefaultNamespace());

        code = "<html><head xmlns=\"namespace\"><title>xxx</title></head><body>yyy</body></html>";
        source = new HtmlSource(code);
        result = SyntaxAnalyzer.create(source).analyze();
        assertNotNull(result);
        assertNull(result.getHtmlTagDefaultNamespace());

        code = "<div><html xmlns=\"namespace\"><head><title>xxx</title></head><body>yyy</body></html>";
        source = new HtmlSource(code);
        result = SyntaxAnalyzer.create(source).analyze();
        assertNotNull(result);
        assertNull(result.getHtmlTagDefaultNamespace());
    }

    public void testBasic() throws ParseException {
        String code = "<html><head><title>xxx</title></head><body>yyy</body></html>";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        assertNotNull(result);
        assertNotNull(result.getSource().getSourceCode());
        assertNotNull(result.getElements());

        assertNull(result.getPublicID()); //not specified

        HtmlVersion version = result.getHtmlVersion();
        assertEquals(HtmlVersion.HTML41_TRANSATIONAL, version);//fallback
        assertNotNull(version.getDTD());

        HtmlParseResult presult = result.parseHtml();
        assertNotNull(presult);
        assertNotNull(presult.root());

    }

    public void testExistingDoctype() throws ParseException {
        String code = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        assertNotNull(result);

        assertNotNull(result.getPublicID());
        assertEquals("-//W3C//DTD HTML 4.01 Transitional//EN", result.getPublicID());

        HtmlVersion version = result.getHtmlVersion();
        assertEquals(HtmlVersion.HTML41_TRANSATIONAL, version);//fallback
        assertNotNull(version.getDTD());

    }

    public void testDoctypeInLowercase() throws ParseException {
        String code = "<!doctype html public \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        assertNotNull(result);

        assertNotNull(result.getPublicID());
        assertEquals("-//W3C//DTD HTML 4.01 Transitional//EN", result.getPublicID());

        HtmlVersion version = result.getHtmlVersion();
        assertEquals(HtmlVersion.HTML41_TRANSATIONAL, version);//fallback
        assertNotNull(version.getDTD());

    }

    public void testCorruptedDoctype() throws ParseException {
        String code = "<!DOCTYP html>";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        assertNotNull(result);

        assertNull(result.getPublicID());
        
        HtmlVersion version = result.getHtmlVersion();
        assertEquals(HtmlVersion.HTML41_TRANSATIONAL, version);//fallback
        assertNotNull(version.getDTD());

    }



    public void testInvalidPublicId() throws ParseException {
        String code = "<!DOCTYPE HTML PUBLIC \"invalid_public_id\"><html><head><title>xxx</title></head><body>yyy</body></html>";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        assertNotNull(result);

        assertNotNull(result.getPublicID());
        assertEquals("invalid_public_id", result.getPublicID());

        HtmlVersion version = result.getHtmlVersion();
        assertEquals(HtmlVersion.HTML41_TRANSATIONAL, version);//fallback
        assertNotNull(version.getDTD());

        HtmlParseResult presult = result.parseHtml();
        assertNotNull(presult);
        assertNotNull(presult.root());

    }

    public void testHtml5Doctype() throws ParseException {
        String code = "<!doctype html><section><p>ahoj<p>hello<div>xxx</div></section></x>";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        assertNotNull(result);

        SyntaxElement.Declaration declaration = result.getDoctypeDeclaration();
        assertNotNull(declaration);

        assertNull(declaration.getDoctypeFile());
        assertNull(declaration.getPublicIdentifier());

        HtmlParseResult presult = result.parseHtml();
        assertNotNull(presult);
        assertNotNull(presult.root());

    }

    public void testGetDeclaredNamespaces() {
        String code = "<html xmlns=\"http://www.w3.org/1999/xhtml\" " +
                "xmlns:jsp=\"http://java.sun.com/JSP/Page\">" +
                "<ui:composition xmlns:ui=\"http://java.sun.com/jsf/facelets\"/>" +
                "</html>";

        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

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

    public void testGetAstRoot() throws ParseException {
        String code = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:ui=\"http://java.sun.com/jsf/facelets\">" +
                    "<ui:composition><div><ui:define></ui:define></div></ui:composition>" +
                "</html>";

        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        ParseResult presult = result.parseEmbeddedCode("http://java.sun.com/jsf/facelets");
        assertNotNull(presult);
        AstNode froot = presult.root();
        assertNotNull(froot);

        assertEquals(2, froot.children().size());
        assertNotNull(AstNodeUtils.query(froot, "ui:composition"));
        assertNotNull(AstNodeUtils.query(froot, "ui:composition/ui:define"));

        AstNode root = result.parseHtml().root();
        assertNotNull(root);
        assertEquals(2, root.children().size());
        assertNotNull(AstNodeUtils.query(root, "html"));
        assertNotNull(AstNodeUtils.query(root, "html/div"));

    }

    public void testUndeclaredTagsParseTree() throws ParseException {
        String code = "<html>" +
                          "<x:out><div><x:in></x:in></div></x:out>" +
                      "</html>";

        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();


        AstNode froot = result.parseUndeclaredEmbeddedCode().root();

        assertNotNull(froot);
        assertEquals(2, froot.children().size());
        assertNotNull(AstNodeUtils.query(froot, "x:out"));
        assertNotNull(AstNodeUtils.query(froot, "x:out/x:in"));

    }

    public void testGetParseTreeForUnusedNamespace() throws ParseException {
        String code = "<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">" +
                      "</html>";

        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        ParseResult presult = result.parseEmbeddedCode("http://java.sun.com/jsp/jstl/core");

        assertNotNull(presult);
        assertNotNull(presult.root()); //at least the default root node must be present

    }

    public void testGetParseTreeForUndeclaredNamespace() throws ParseException {
        String code = "<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">" +
                      "</html>";

        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        ParseResult presult = result.parseEmbeddedCode("http://java.sun.com/jsf/composite");

        assertNotNull(presult);
        assertTrue(presult instanceof EmptyResult);
        assertNotNull(presult.root()); //at least the default root node must be present

    }
    
    public void testUndeclaredContentResolver() throws ParseException {
        String code = "<body><x:mytag><y:notmine/></x:mytag></body>";

        UndeclaredContentResolver resolver = new UndeclaredContentResolver() {

            @Override
            public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source) {
                return Collections.singletonMap("my_ns", (List<String>)Collections.singletonList("x"));
            }
        };
        
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze(resolver);

        assertTrue(result.getDeclaredNamespaces().containsKey("my_ns"));
        assertTrue(result.getDeclaredNamespaces().containsValue("x"));
        
        //test that the physically undeclared but resolved by UCR code doesn't 
        //fall to the "unknown content" category
        AstNode undeclaredContentRoot = result.parseUndeclaredEmbeddedCode().root();
        assertEquals(1, undeclaredContentRoot.children().size());
        assertNotNull(AstNodeUtils.query(undeclaredContentRoot, "y:notmine"));
        
        ParseResult presult = result.parseEmbeddedCode("my_ns");
        AstNode my_ns_root = presult.root();
        assertNotNull(my_ns_root);
        
        assertNotNull(AstNodeUtils.query(my_ns_root, "x:mytag"));

    }

}

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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.editor.ext.html.parser.api.SyntaxAnalyzer;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.editor.ext.html.parser.spi.HtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlTag;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagAttribute;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagType;
import org.netbeans.junit.NbTestCase;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class Html5ParserTest extends NbTestCase {

    public Html5ParserTest(String name) {
        super(name);
    }

    public static Test xsuite(){
        AstNodeTreeBuilder.DEBUG = true;
	TestSuite suite = new TestSuite();
        suite.addTest(new Html5ParserTest("testParseUnfinishedCode"));
        return suite;
    }

//        String code = "<!DOCTYPE html><body xmlns:f=\"http://sun.com/jsf/core\"></p><section><f:xx></f:xx><p>cau<p>  <p>ahoj</section><section><div></div></section></body>";
//        String code = "<!DOCTYPE html></p><section><p>cau<p>  <p>ahoj</section><section><div></div></section>";
//        String code = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><section><p id=\"my\">cau<p><p class=\"klasa\">ahoj</section>";
//        String code = "<!DOCTYPE html><html xmlns:myns=\"http://marek.org/\"><myns:section><p id=\"my\">cau<p><p class=\"klasa\">ahoj</myns:section></html>";
//        AstNodeUtils.dumpTree(root);
    
    public void testBasic() throws SAXException, IOException, ParseException {
        HtmlParseResult result = parse("<!doctype html><section><div></div></section>");
        AstNode root = result.root();
        assertNotNull(root);
        assertNotNull(AstNodeUtils.query(root, "html/body/section/div")); //html/body are generated
    }

    public void testHtmlAndBodyTags() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body><div>ahoj</div></body></html>");
        AstNode root = result.root();
        assertNotNull(root);
        assertNotNull(AstNodeUtils.query(root, "html"));
        assertNotNull(AstNodeUtils.query(root, "html/head"));
        assertNotNull(AstNodeUtils.query(root, "html/head/title"));
        assertNotNull(AstNodeUtils.query(root, "html/body"));
        assertNotNull(AstNodeUtils.query(root, "html/body/div"));
    }

    public void testProblemsReporting() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html></section>");
        //                              012345678901234567890123456789
        //                              0         1         2
        Collection<ProblemDescription> problems = result.getProblems();

        assertEquals(1, problems.size());
        ProblemDescription p = problems.iterator().next();

        assertEquals(ProblemDescription.ERROR, p.getType());
        assertEquals("nokey", p.getKey()); //XXX fix that
        assertEquals("Stray end tag “section”.", p.getText());
        assertEquals(15, p.getFrom());
        assertEquals(25, p.getTo());

    }

    public void testStyle() throws ParseException {
        String code = "<!DOCTYPE html>\n"
                + "<style type=\"text/css\">\n"
                + "@import \"resources2/ezcompik/newcss2moje.css\";\n"
                + "</style>\n";

//        AstNodeTreeBuilder.DEBUG = true;
        HtmlParseResult result = parse(code);
        AstNode root = result.root();
        assertNotNull(root);
        AstNode head = AstNodeUtils.query(root, "html/head");
        assertNotNull(head);
        assertEquals(2, head.children().size());
        AstNode styleOpenTag = head.children().get(0);

        assertNotNull(styleOpenTag);
        assertEquals(16, styleOpenTag.startOffset());
        assertEquals(39, styleOpenTag.endOffset());

        AstNode styleEndTag = head.children().get(1);
        assertNotNull(styleEndTag);
        assertEquals(87, styleEndTag.startOffset());
        assertEquals(95, styleEndTag.endOffset());

        assertSame(styleEndTag, styleOpenTag.getMatchingTag());
        assertEquals(95, styleOpenTag.getLogicalRange()[1]);

//        AstNodeUtils.dumpTree(result.root());
    }

    public void testParseUnfinishedCode() throws ParseException {
        String code = "<!DOCTYPE HTML>"
                     +"<html>"
                         +"<head>"
                             +"<title>"
                             +"</title>"
                         +"</head>"
                         +"<body>"
                         +"</table>"
                    +"</html>";

        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);
        AstNode html = AstNodeUtils.query(root, "html");
        assertNotNull(html);

        Collection<AstNode> children = html.children();
        assertEquals(4, children.size()); // <head>, </head>,<body>,</table>
        Iterator<AstNode> childernItr = children.iterator();

        assertTrue(childernItr.hasNext());
        AstNode child = childernItr.next();
        assertEquals("head", child.name());
        assertEquals(AstNode.NodeType.OPEN_TAG, child.type());
        assertTrue(childernItr.hasNext());
        child = childernItr.next();
        assertEquals("head", child.name());
        assertEquals(AstNode.NodeType.ENDTAG, child.type());
        assertTrue(childernItr.hasNext());
        child = childernItr.next();
        assertEquals("body", child.name());
        assertEquals(AstNode.NodeType.OPEN_TAG, child.type());
        assertTrue(childernItr.hasNext());
        child = childernItr.next();
        assertEquals("table", child.name());
        assertEquals(AstNode.NodeType.ENDTAG, child.type());
        

//        AstNodeUtils.dumpTree(root);

    }

    public void testGetPossibleOpenTagsInContext() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body><div>ahoj</div></body></html>");

        assertNotNull(result.root());

        AstNode body = AstNodeUtils.query(result.root(), "html/body");
        Collection<HtmlTag> possible = result.getPossibleTagsInContext(body, HtmlTagType.OPEN_TAG);

        assertTrue(!possible.isEmpty());

        HtmlTag divTag = new HtmlTagImpl("div");
        HtmlTag headTag = new HtmlTagImpl("head");

        assertTrue(possible.contains(divTag));
        assertFalse(possible.contains(headTag));

        AstNode head = AstNodeUtils.query(result.root(), "html/head");
        possible = result.getPossibleTagsInContext(head, HtmlTagType.OPEN_TAG);

        assertTrue(!possible.isEmpty());

        HtmlTag titleTag = new HtmlTagImpl("title");
        assertTrue(possible.contains(titleTag));
        assertFalse(possible.contains(headTag));

        AstNode html = AstNodeUtils.query(result.root(), "html");
        possible = result.getPossibleTagsInContext(html, HtmlTagType.OPEN_TAG);
        assertTrue(!possible.isEmpty());
        assertTrue(possible.contains(divTag));

    }

    public void testGetPossibleEndTagsInContext() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body><div>ahoj</div></body></html>");

        assertNotNull(result.root());

        AstNode body = AstNodeUtils.query(result.root(), "html/body");
        Collection<HtmlTag> possible = result.getPossibleTagsInContext(body, HtmlTagType.END_TAG);

        assertTrue(!possible.isEmpty());

        HtmlTag htmlTag = new HtmlTagImpl("html");
        HtmlTag headTag = new HtmlTagImpl("head");
        HtmlTag bodyTag = new HtmlTagImpl("body");
        HtmlTag divTag = new HtmlTagImpl("div");

        Iterator<HtmlTag> possibleItr = possible.iterator();
        assertEquals(htmlTag, possibleItr.next());
        assertEquals(bodyTag, possibleItr.next());

        assertFalse(possible.contains(divTag));

        AstNode head = AstNodeUtils.query(result.root(), "html/head");
        possible = result.getPossibleTagsInContext(head, HtmlTagType.OPEN_TAG);

        assertTrue(!possible.isEmpty());

        HtmlTag titleTag = new HtmlTagImpl("title");
        assertTrue(possible.contains(titleTag));
        assertFalse(possible.contains(headTag));

    }

    private HtmlParseResult parse(CharSequence code) throws ParseException {
        HtmlSource source = new HtmlSource(code);
        HtmlParseResult result = SyntaxAnalyzer.create(source).analyze().parseHtml();

        assertNotNull(result);

        return result;
    }

    private static class HtmlTagImpl implements HtmlTag {

        private String name;

        public HtmlTagImpl(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof HtmlTag)) {
                return false;
            }
            final HtmlTag other = (HtmlTag) obj;
            if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "HtmlTagImpl{" + "name=" + name + '}';
        }

        public Collection<HtmlTagAttribute> getAttributes() {
            return Collections.emptyList();
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean hasOptionalOpenTag() {
            return false;
        }

        public boolean hasOptionalEndTag() {
            return false;
        }
        
    }
}

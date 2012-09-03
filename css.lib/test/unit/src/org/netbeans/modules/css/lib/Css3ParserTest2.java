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
package org.netbeans.modules.css.lib;

import java.util.List;
import javax.swing.text.BadLocationException;
import org.junit.Assert;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 * 
 * THIS TEST DOESN'T REGULARLY RUN IN THE UNIT TEST SUITE (due to the Test2 postfix).
 *
 * Most of the tests are not passing or even valid.
 * 
 * @author marekfukala
 */
public class Css3ParserTest2 extends CslTestBase {

    public Css3ParserTest2(String testName) {
        super(testName);
    }

//    public static Test Xsuite(){
//	TestSuite suite = new TestSuite();
//        suite.addTest(new Css3ParserTest2("testErrorInMediaRule"));
//        return suite;
//    }

    private CssParserResult parse(String source) throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse(source);
        assertNotNull(result);
        return result;
    }

    private static List<ProblemDescription> getErrors(CssParserResult result) {
        return result.getDiagnostics();
    }

    private void assertNoErrors(CssParserResult result) {
        Assert.assertNotNull(result);
        List<ProblemDescription> errors = getErrors(result);
        if(errors.size() > 0) {
            StringBuilder buf = new StringBuilder();
            for(ProblemDescription e : errors) {
                buf.append(e.toString());
                buf.append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
            assertEquals("Unexpected parse errors found: " + buf.toString(), 0, errors.size());
        }
    }

    public void testParserBasis() throws ParseException, BadLocationException {
        CssParserResult result = parse("h1 { color: red; }");
        Assert.assertNotNull(result);
        assertNoErrors(result);
        
    }

    public void testParseComment() throws ParseException, BadLocationException {
        assertNoErrors(parse("h1 { /* comment */ }"));
        assertNoErrors(parse("h1 { color: /* comment */ red; }"));
        assertNoErrors(parse("h1 /* c */ { /* c2 */ color: red; }"));
        assertNoErrors(parse("/* c */ h1 {  color: red; } /* c2 */"));
    }

    private CssParserResult check(String source) throws ParseException, BadLocationException {
        CssParserResult result = parse(source);
        Assert.assertNotNull(result);
        assertNoErrors(result);
        return result;
    }

    public void testIssue183158() throws ParseException, BadLocationException {
        String code = "div { margin-left: -49%; }";
//        dumpTokens(code);
//        dumpParseTree(code);

        check(code);
    }

    public void testIssue183601() throws ParseException, BadLocationException {
        String code = "table tbody tr:not(.Current):hover { }";
        check(code);
    }

    public void testAtSymbol() throws ParseException, BadLocationException {
        String code = "@a ";
        check(code);
    }

    public void testIssue182434() throws ParseException, BadLocationException {
        //css3
        check("A[href^=\"https://\"] {}");
        check("IMG[src*=\"icon\"] {}");
        check("A[href$=\".pdf\"] {} ");

        check("div { quotes: '\"' '\"' \"'\" \"'\"; } ");
    }

    public void testMSSyntax() throws ParseException, BadLocationException {
        check("h1 { top: expression(offsetParent.scrollTop) } ");
        check("h1 { filter:alpha(opacity=50); }");
        check("h1 { filter: progid:DXImageTransform.Microsoft.Blur(PixelRadius=5,MakeShadow=true,ShadowOpacity=0.20); }");
        check("h1 { filter: progid:DXImageTransform.Microsoft.Alpha(opacity=70) }");
        check("h1 { filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='trans.png', sizingMethod='scale'); }");

        //IE8
        check("h1 { -ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=50)\"; }");

        check("h1 { #width: 400px }");

    }

    // @@@ represents a gap from the css perspective in reality filled with 
    // a templating language code.
    public void testParserOnTemplating() throws ParseException, BadLocationException {
        //generated properties
        check("h1 { @@@: red; }");
        check("h1 { color: @@@; }");
        check("h1 { @@@: @@@; }");
        check("h1 { color: @@@ red @@@; }");
//        check("h1 { co@@@lor: red; }");
        check("h1 { @@@@@@: green; }");

        check("h1 { background-image: url(@@@); }");
        check("h1 { background-image: url(\"@@@\"); }");
        check("h1 { color: rgb(@@@,@@@,@@@); }");
        check("h1 { color: rgb(0,0,@@@); }");

        check("h1 { @@@ ; }");
        check("h1 { @@@; }");
        check("h1 { @@@ }");

        //selectors are generated
        check("@@@ { }");
        check("h1 @@@ h2 { }");
//        check("t@@@ble { }");

        check("table > @@@ { }");
        check("t[@@@] { }");
        check("t[x=@@@] { }");
        check("* > t[x=@@@] { }");
        check("E:lang(@@@){ }");
        check("E:@@@{ }");
        check("t[x|=@@@] { }");

//        check("#@@@ { }");//fails
//        check("E#@@@ { }");//fails

        check("@@@ + t[x=@@@] { }");

        check("h1 { @@@: rgb(@@@); }");
        check("span[hello=@@@][@@@]{}");

        check("p.@@@:first-letter {color: @@@}");

//        check("h1 {color: #@@@}");//fails
        check("media @@@{}");

//        check("media TV{ @@@ { } }");//fails
        check("@page:left{margin-left:@@@;}");
    }

    public void testTemplatingInComment() throws ParseException, BadLocationException {
        check("EXAMPLE { /* @@@ */ }");
    }

    public void testParserRootNodeSpan() throws ParseException, BadLocationException {
        String source = "h1 { }";
        //               0123456
        CssParserResult result = parse(source);
        assertNoErrors(result);
        Node node = result.getParseTree();
        Assert.assertNotNull(node);

//        Token t = node.jjtGetFirstToken();
//        while(t != null) {
//            System.out.print(t);
//            System.out.println(" ["+t.kind+"]");
//            t = t.next;
//        }

        //test the root node size - if it spans over the whole source text
        assertEquals(source.length(), node.to() - node.from());

    }

    public void testPropertyValueWithComment() throws ParseException, BadLocationException {
        //fails - issue http://www.netbeans.org/issues/show_bug.cgi?id=162844
        //the problem is that the Node for property value contains also the whitespace and comment

        String code = "h3 { color: red /*.....*/ }";

//        //tokens
//        System.out.println("code='" + code + "'");
//        System.out.println("Tokens: ");
//        CssParserTokenManager tm = new PatchedCssParserTokenManager(new ASCII_CharStream(new StringReader(code)));
//        Token t;
//        while((t = tm.getNextToken()) != null && t.image.length() > 0) {
//            System.out.print("<" + t.offset + "," + t.image + "> ");
//        }
//        System.out.println(".");

        CssParserResult result = check(code);
        Node root = result.getParseTree();
//        System.out.println(root.dump());

        Node node = NodeUtil.query(root, "styleSheetRuleList/rule/styleRule/declaration/expr/term");
        assertNotNull(node);
        assertEquals("red", node.image());
 
    }

    public void testErrorInMediaRule() throws ParseException, BadLocationException {
        String source = "@media page {  htm }  ";
//        dumpParseTree(source);
        CssParserResult result = parse(source);
        Node node = result.getParseTree();
        assertNotNull(node);

        List<ProblemDescription> errors = getErrors(result);
        assertEquals(2, errors.size());

//        Node error = errors.get(0);
//        assertEquals(CssParserTreeConstants.JJTERROR_SKIPBLOCK, error.kind());
//        error = errors.get(1);
//        assertEquals(CssParserTreeConstants.JJTERROR_SKIPBLOCK, error.kind());
    } 

    public void testErrorInStyleRule() throws ParseException, BadLocationException {
        String source = "div {  htm }";
        CssParserResult result = parse(source);

        List<ProblemDescription> errors = getErrors(result);
        assertEquals(2, errors.size());

//        Node error = errors.get(0);
//        assertEquals(CssParserTreeConstants.JJTERROR_SKIPDECL, error.kind());
//        error = errors.get(1);
//        assertEquals(CssParserTreeConstants.JJTERROR_SKIPBLOCK, error.kind());
    }

    public void testErrorInDeclaration() throws ParseException, BadLocationException {
        String source = "div {  color: ; azimuth: center; }";
        CssParserResult result = parse(source);
        List<ProblemDescription> errors = getErrors(result);

        assertEquals(2, errors.size());

//        Node error = errors.get(0);
//        assertEquals(CssParserTreeConstants.JJTERROR_SKIPDECL, error.kind());
//        error = errors.get(1);
//        assertEquals(CssParserTreeConstants.JJTERROR_SKIPBLOCK, error.kind());
    }

    public void testErrorInDeclarationInMediaRule() throws ParseException, BadLocationException {
        String source = "@media page { div { color: } } ";
        CssParserResult result = parse(source);
        List<ProblemDescription> errors = getErrors(result);

        assertEquals(2, errors.size());

//        Node error = errors.get(0);
//        assertEquals(CssParserTreeConstants.JJTERROR_SKIPDECL, error.kind());
//        error = errors.get(1);
//        assertEquals(CssParserTreeConstants.JJTERROR_SKIPBLOCK, error.kind());
    }
    
    public void testNoErrorAfterAtSign() throws ParseException, BadLocationException {
        String source = "a {\n\t@d;\n}";
        CssParserResult result = parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        
        List<ProblemDescription> errors = getErrors(result);
        for(ProblemDescription pd : errors) {
            System.out.println(pd);
        }
        
        assertEquals(1, errors.size());

        ProblemDescription pd1 = errors.get(0);
        assertEquals("Unexpected character(s) '@d',';' found", pd1.getDescription());
    }

}


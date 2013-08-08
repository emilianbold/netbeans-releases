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
package org.netbeans.modules.html.editor.typinghooks;

import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class HtmlTypingHooksTest extends TestBase {

    public HtmlTypingHooksTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlTypedTextInterceptor.adjust_quote_type_after_eq = false;
    }

    public void testHandleEmptyTagCloseSymbol()  {
       Typing ctx = new Typing(new HtmlKit(), "<div|");
       ctx.typeChar('/');
       ctx.assertDocumentTextEquals("<div/>|");
       ctx.typeChar('>');
       ctx.assertDocumentTextEquals("<div/>|");
    }
    
    public void testHandleEmptyTagCloseSymbolAfterWS()  {
       Typing ctx = new Typing(new HtmlKit(), "<div |");
       ctx.typeChar('/');
       ctx.assertDocumentTextEquals("<div />|");
       ctx.typeChar('>');
       ctx.assertDocumentTextEquals("<div />|");
    }
    
    public void testHandleEmptyTagCloseSymbolAfterAttribute()  {
       Typing ctx = new Typing(new HtmlKit(), "<div align='center'|");
       ctx.typeChar('/');
       ctx.assertDocumentTextEquals("<div align='center'/>|");
       ctx.typeChar('>');
       ctx.assertDocumentTextEquals("<div align='center'/>|");
    }
    
    public void testQuoteAutocompletionInHtmlAttribute() {
        Typing ctx = new Typing(new HtmlKit(), "<a href=\"javascript:bigpic(|)\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"javascript:bigpic(\"|)\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"javascript:bigpic(\"\"|)\">");
    }

    public void testSkipClosingQuoteInEmptyAttr() {
        Typing ctx = new Typing(new HtmlKit(), "<a href=\"|\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"\"|>");
    }

     public void testSkipClosingQuoteInNonEmpty() {
        Typing ctx = new Typing(new HtmlKit(), "<a href=\"x|\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"x\"|>");
    }
     
     public void testSkipClosingQuoteInEmptyClassAndId() {
        Typing ctx = new Typing(new HtmlKit(), "<a class=\"|\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=\"\"|>");
        
        ctx = new Typing(new HtmlKit(), "<a id=\"|\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a id=\"\"|>");
    }

     //XXX fix me - css intercepts here
//     public void testSkipClosingQuoteInNonEmptyClassAndId() {
//        Context ctx = new Context(new HtmlKit(), "<a class=\"xx|\">");
//        ctx.typeChar('"');
//        ctx.assertDocumentTextEquals("<a class=\"xx\"|>");
//        
//        ctx = new Context(new HtmlKit(), "<a id=\"yy|\">");
//        ctx.typeChar('"');
//        ctx.assertDocumentTextEquals("<a id=\"yy\"|>");
//    }
     
     //XXX fixme - <div + "> => will autopopup the closing tag, but once completed,
     //the closing tag is not indented properly -- fix in HtmlTypedBreakInterceptor
      
    public void testDoubleQuoteAutocompleteAfterEQ() {
        Typing ctx = new Typing(new HtmlKit(), "<a href|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a href=\"|\"");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a href=\"val|\"");
    }

    public void testDoubleQuoteAutocompleteAfterEQInCSSAttribute() {
        Typing ctx = new Typing(new HtmlKit(), "<a class|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a class=\"|\"");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a class=\"val|\"");
    }

    public void testDoubleQuoteAfterQuotedClassAttribute() {
        Typing ctx = new Typing(new HtmlKit(), "<a class=\"val|");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=\"val\"|");
    }

    public void testDoubleQuoteAfterUnquotedClassAttribute() {
        Typing ctx = new Typing(new HtmlKit(), "<a class=val|");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=val\"|");
    }

    public void testSingleQuoteAutocompleteAfterEQ() {
        Typing ctx = new Typing(new HtmlKit(), "<a href=|");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a href='|'");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a href='val|'");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a href='val'|");
    }

    public void testQuoteChange() {
        Typing ctx = new Typing(new HtmlKit(), "<a href|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a href=\"|\"");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a href='|'");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a href='val|'");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a href='val'|");
    }

    public void testTypeSingleQuoteInUnquoteClassAttr() {
        Typing ctx = new Typing(new HtmlKit(), "<a class=|");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a class=val|");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a class=val'|");
    }
    
    public void testAutocompleteDoubleQuoteOnlyAfterEQ() {
        Typing ctx = new Typing(new HtmlKit(), "<a align|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a align=\"|\"");
        ctx.typeChar('x');
        ctx.assertDocumentTextEquals("<a align=\"x|\"");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a align=\"x\"|");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a align=\"x\"\"|");
    }


    //XXX fix me - css intercepts here
//    public void testAutocompleteDoubleQuoteOnlyAfterEQInClass() {
//        Context ctx = new Context(new HtmlKit(), "<a class|");
//        ctx.typeChar('=');
//        ctx.assertDocumentTextEquals("<a class=\"|\"");
//        ctx.typeChar('x');
//        ctx.assertDocumentTextEquals("<a class=\"x|\"");
//        ctx.typeChar('"');
//        ctx.assertDocumentTextEquals("<a class=\"x\"|");
//        ctx.typeChar('"');
//        ctx.assertDocumentTextEquals("<a class=\"x\"\"|");
//    }

    public void testAutocompleteSingleQuoteOnlyAfterEQ() {
        Typing ctx = new Typing(new HtmlKit(), "<a align|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a align=\"|\"");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a align='|'");
        ctx.typeChar('x');
        ctx.assertDocumentTextEquals("<a align='x|'");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a align='x'|");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a align='x''|");
    }

    //XXX fix me - css intercepts here
//     public void testAutocompleteSingleQuoteOnlyAfterEQInClass() {
//        Context ctx = new Context(new HtmlKit(), "<a class|");
//        ctx.typeChar('=');
//        ctx.assertDocumentTextEquals("<a class=\"|\"");
//        ctx.typeChar('\'');
//        ctx.assertDocumentTextEquals("<a class='|'");
//        ctx.typeChar('x');
//        ctx.assertDocumentTextEquals("<a class='x|'");
//        ctx.typeChar('\'');
//        ctx.assertDocumentTextEquals("<a class='x'|");
//        ctx.typeChar('\'');
//        ctx.assertDocumentTextEquals("<a class='x''|");
//    }

    
    public void testDeleteAutocompletedQuote() {
        Typing ctx = new Typing(new HtmlKit(), "<a class|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a class=\"|\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=");
    }

    public void testDeleteQuote() {
        Typing ctx = new Typing(new HtmlKit(), "<a class=\"|\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=");
    }

    public void testDeleteQuoteWithWSAfter() {
        Typing ctx = new Typing(new HtmlKit(), "<a class=\"|\" ");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class= ");
    }

    public void testDeleteSingleQuote() {
        Typing ctx = new Typing(new HtmlKit(), "<a class='|'");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=");

        //but do not delete if there's a text after the caret
        ctx = new Typing(new HtmlKit(), "<a class='|x'");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=x'");

    }

    public void testDoNotAutocompleteQuoteInValue() {
        Typing ctx = new Typing(new HtmlKit(), "<a x=\"|test\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a x=|test\"");
        ctx.typeChar('"');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a x=\"|test\"");

        //different quotes
        ctx = new Typing(new HtmlKit(), "<a x=\"|test\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a x=|test\"");
        ctx.typeChar('\'');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a x=\'|test\"");

        //no closing quote
        ctx = new Typing(new HtmlKit(), "<a x=\"|test");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a x=|test");
        ctx.typeChar('\'');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a x=\'|test");

    }

    public void testInClassDoNotAutocompleteQuoteInValue() {
        Typing ctx = new Typing(new HtmlKit(), "<a class=\"|test\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=|test\"");
        ctx.typeChar('"');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a class=\"|test\"");

        //different quotes
        ctx = new Typing(new HtmlKit(), "<a class=\"|test\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=|test\"");
        ctx.typeChar('\'');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a class=\'|test\"");

        //no closing quote
        ctx = new Typing(new HtmlKit(), "<a class=\"|test");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=|test");
        ctx.typeChar('\'');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a class=\'|test");

    }

    public void testAdjustQuoteTypeAfterEQ()  {
        HtmlTypedTextInterceptor.adjust_quote_type_after_eq = true;
        try {
            //default type
            assertEquals('"', HtmlTypedTextInterceptor.default_quote_char_after_eq);

            Typing ctx = new Typing(new HtmlKit(), "<a class|");
            ctx.typeChar('=');
            ctx.assertDocumentTextEquals("<a class=\"|\"");
            ctx.typeChar('\'');

            //now should be switched to single quote type
            assertEquals('\'', HtmlTypedTextInterceptor.default_quote_char_after_eq);

            ctx = new Typing(new HtmlKit(), "<a class|");
            ctx.typeChar('=');
            ctx.assertDocumentTextEquals("<a class='|'");
            ctx.typeChar('"');
            
            //now should be switched back to the default double quote type
            assertEquals('"', HtmlTypedTextInterceptor.default_quote_char_after_eq);

        } finally {
            HtmlTypedTextInterceptor.adjust_quote_type_after_eq = false;
        }
    }
}

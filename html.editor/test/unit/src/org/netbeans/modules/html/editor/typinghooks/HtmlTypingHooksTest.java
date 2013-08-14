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

import javax.swing.text.html.HTMLEditorKit;
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
    
    protected Typing typing(String code) {
        return new Typing(new HtmlKit(), code);
    }

    public void testHandleEmptyTagCloseSymbol()  {
       Typing t = typing("<div|");
       t.typeChar('/');
       t.assertDocumentTextEquals("<div/>|");
       t.typeChar('>');
       t.assertDocumentTextEquals("<div/>|");
    }
    
    public void testHandleEmptyTagCloseSymbolAfterWS()  {
       Typing t = typing("<div |");
       t.typeChar('/');
       t.assertDocumentTextEquals("<div />|");
       t.typeChar('>');
       t.assertDocumentTextEquals("<div />|");
    }
    
    public void testHandleEmptyTagCloseSymbolAfterAttribute()  {
       Typing t = typing("<div align='center'|");
       t.typeChar('/');
       t.assertDocumentTextEquals("<div align='center'/>|");
       t.typeChar('>');
       t.assertDocumentTextEquals("<div align='center'/>|");
    }
    
    //Bug 234153 - automatic tag close attempted inside attribute value
    public void testCloseTagSymbolAutocomplete() {
        Typing t = typing("<applet code=\"com|example/MyApplet.class\"/>");
        t.typeChar('/');
        t.assertDocumentTextEquals("<applet code=\"com/|example/MyApplet.class\"/>");
    }
    
    public void testQuoteAutocompletionInHtmlAttribute() {
        Typing t = typing("<a href=\"javascript:bigpic(|)\">");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a href=\"javascript:bigpic(\"|)\">");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a href=\"javascript:bigpic(\"\"|)\">");
    }

    public void testSkipClosingQuoteInEmptyAttr() {
        Typing t = typing("<a href=\"|\">");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a href=\"\"|>");
    }

     public void testSkipClosingQuoteInNonEmpty() {
        Typing t = typing("<a href=\"x|\">");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a href=\"x\"|>");
    }
     
     public void testSkipClosingQuoteInEmptyClassAndId() {
        Typing t = typing("<a class=\"|\">");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a class=\"\"|>");
        
        t = typing("<a id=\"|\">");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a id=\"\"|>");
    }

     public void testSkipClosingQuoteInNonEmptyClassAndId() {
        Typing t = typing("<a class=\"xx|\">");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a class=\"xx\"|>");
        
        t = typing("<a id=\"yy|\">");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a id=\"yy\"|>");
    }
          
     //XXX fixme - <div + "> => will autopopup the closing tag, but once completed,
     //the closing tag is not indented properly -- fix in HtmlTypedBreakInterceptor
      
    public void testDoubleQuoteAutocompleteAfterEQ() {
        Typing t = typing("<a href|");
        t.typeChar('=');
        t.assertDocumentTextEquals("<a href=\"|\"");
        t.typeChar('v');
        t.typeChar('a');
        t.typeChar('l');
        t.assertDocumentTextEquals("<a href=\"val|\"");
    }

    public void testDoubleQuoteAutocompleteAfterEQInCSSAttribute() {
        Typing t = typing("<a class|");
        t.typeChar('=');
        t.assertDocumentTextEquals("<a class=\"|\"");
        t.typeChar('v');
        t.typeChar('a');
        t.typeChar('l');
        t.assertDocumentTextEquals("<a class=\"val|\"");
    }

    public void testDoubleQuoteAfterQuotedClassAttribute() {
        Typing t = typing("<a class=\"val|");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a class=\"val\"|");
    }

    public void testDoubleQuoteAfterUnquotedClassAttribute() {
        Typing t = typing("<a class=val|");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a class=val\"|");
    }

    public void testSingleQuoteAutocompleteAfterEQ() {
        Typing t = typing("<a href=|");
        t.typeChar('\'');
        t.assertDocumentTextEquals("<a href='|'");
        t.typeChar('v');
        t.typeChar('a');
        t.typeChar('l');
        t.assertDocumentTextEquals("<a href='val|'");
        t.typeChar('\'');
        t.assertDocumentTextEquals("<a href='val'|");
    }

    public void testQuoteChange() {
        Typing t = typing("<a href|");
        t.typeChar('=');
        t.assertDocumentTextEquals("<a href=\"|\"");
        t.typeChar('\'');
        t.assertDocumentTextEquals("<a href='|'");
        t.typeChar('v');
        t.typeChar('a');
        t.typeChar('l');
        t.assertDocumentTextEquals("<a href='val|'");
        t.typeChar('\'');
        t.assertDocumentTextEquals("<a href='val'|");
    }

    public void testTypeSingleQuoteInUnquoteClassAttr() {
        Typing t = typing("<a class=|");
        t.typeChar('v');
        t.typeChar('a');
        t.typeChar('l');
        t.assertDocumentTextEquals("<a class=val|");
        t.typeChar('\'');
        t.assertDocumentTextEquals("<a class=val'|");
    }
    
    public void testAutocompleteDoubleQuoteOnlyAfterEQ() {
        Typing t = typing("<a align|");
        t.typeChar('=');
        t.assertDocumentTextEquals("<a align=\"|\"");
        t.typeChar('x');
        t.assertDocumentTextEquals("<a align=\"x|\"");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a align=\"x\"|");
        t.typeChar('"');
        t.assertDocumentTextEquals("<a align=\"x\"\"|");
    }

    public void testAutocompleteDoubleQuoteOnlyAfterEQInClass() {
        Typing t = typing("<a class|");
        t.typeChar('=');
        t.assertDocumentTextEquals("<a class=\"|\"");
        t.typeChar('x');
        t.assertDocumentTextEquals("<a class=\"x|\"");
    }

    public void testAutocompleteSingleQuoteOnlyAfterEQ() {
        Typing t = typing("<a align|");
        t.typeChar('=');
        t.assertDocumentTextEquals("<a align=\"|\"");
        t.typeChar('\'');
        t.assertDocumentTextEquals("<a align='|'");
        t.typeChar('x');
        t.assertDocumentTextEquals("<a align='x|'");
        t.typeChar('\'');
        t.assertDocumentTextEquals("<a align='x'|");
        t.typeChar('\'');
        t.assertDocumentTextEquals("<a align='x''|");
    }

     public void testSwitchAutocompletedQuoteTypeClass() {
        Typing t = typing("<a class|");
        t.typeChar('=');
        t.assertDocumentTextEquals("<a class=\"|\"");
        t.typeChar('\'');
        t.assertDocumentTextEquals("<a class='|'");
    }

    
    public void testDeleteAutocompletedQuote() {
        Typing t = typing("<a class|");
        t.typeChar('=');
        t.assertDocumentTextEquals("<a class=\"|\"");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a class=");
    }

    public void testDeleteQuote() {
        Typing t = typing("<a class=\"|\"");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a class=");
    }

    public void testDeleteQuoteWithWSAfter() {
        Typing t = typing("<a class=\"|\" ");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a class= ");
    }

    public void testDeleteSingleQuote() {
        Typing t = typing("<a class='|'");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a class=");

        //but do not delete if there's a text after the caret
        t = typing("<a class='|x'");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a class=x'");

    }

    public void testDoNotAutocompleteQuoteInValue() {
        Typing t = typing("<a x=\"|test\"");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a x=|test\"");
        t.typeChar('"');

        //do not autocomplete in this case
        t.assertDocumentTextEquals("<a x=\"|test\"");

        //different quotes
        t = typing("<a x=\"|test\"");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a x=|test\"");
        t.typeChar('\'');

        //do not autocomplete in this case
        t.assertDocumentTextEquals("<a x=\'|test\"");

        //no closing quote
        t = typing("<a x=\"|test");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a x=|test");
        t.typeChar('\'');

        //do not autocomplete in this case
        t.assertDocumentTextEquals("<a x=\'|test");

    }

    public void testInClassDoNotAutocompleteQuoteInValue() {
        Typing t = typing("<a class=\"|test\"");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a class=|test\"");
        t.typeChar('"');

        //do not autocomplete in this case
        t.assertDocumentTextEquals("<a class=\"|test\"");

        //different quotes
        t = typing("<a class=\"|test\"");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a class=|test\"");
        t.typeChar('\'');

        //do not autocomplete in this case
        t.assertDocumentTextEquals("<a class=\'|test\"");

        //no closing quote
        t = typing("<a class=\"|test");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<a class=|test");
        t.typeChar('\'');

        //do not autocomplete in this case
        t.assertDocumentTextEquals("<a class=\'|test");

    }

    public void testAdjustQuoteTypeAfterEQ()  {
        HtmlTypedTextInterceptor.adjust_quote_type_after_eq = true;
        try {
            //default type
            assertEquals('"', HtmlTypedTextInterceptor.default_quote_char_after_eq);

            Typing t = typing("<a class|");
            t.typeChar('=');
            t.assertDocumentTextEquals("<a class=\"|\"");
            t.typeChar('\'');

            //now should be switched to single quote type
            assertEquals('\'', HtmlTypedTextInterceptor.default_quote_char_after_eq);

            t = typing("<a class|");
            t.typeChar('=');
            t.assertDocumentTextEquals("<a class='|'");
            t.typeChar('"');
            
            //now should be switched back to the default double quote type
            assertEquals('"', HtmlTypedTextInterceptor.default_quote_char_after_eq);

        } finally {
            HtmlTypedTextInterceptor.adjust_quote_type_after_eq = false;
        }
    }
    
}

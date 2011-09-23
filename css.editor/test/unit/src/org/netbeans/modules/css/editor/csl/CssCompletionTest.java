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
package org.netbeans.modules.css.editor.csl;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.editor.module.main.CssModuleTestBase;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssCompletionTest extends CssModuleTestBase {

    private static String[] AT_RULES = new String[]{"@charset", "@import", "@media", "@page", "@font-face"};


    public CssCompletionTest(String test) {
        super(test);
    }

    public void testAtRules() throws ParseException {
        checkCC("|", AT_RULES, Match.CONTAINS);
        checkCC("@|", AT_RULES, Match.CONTAINS);
        checkCC("@pa|", new String[]{"@page"}, Match.CONTAINS);

        checkCC("|  h1 { }", AT_RULES, Match.CONTAINS);
        checkCC("@| h1 { }", AT_RULES, Match.CONTAINS);
        checkCC("@pa| h1 { }", new String[]{"@page"}, Match.CONTAINS);
    }

    public void testPropertyNames() throws ParseException {
        //empty rule
        checkCC("h1 { | }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { az| }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { azimuth| }", arr("azimuth"), Match.CONTAINS);

        //beginning of the rule
        checkCC("h1 { | \n color: red; }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { az| \n color: red; }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { azimuth| \n color: red; }", arr("azimuth"), Match.CONTAINS);

        //middle in the rule
        checkCC("h1 { color: red;\n | \n padding: 2px;}", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { color: red;\n az| \n padding: 2px;}", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { color: red;\n azimuth| \n padding: 2px;}", arr("azimuth"), Match.CONTAINS);

        //end of the rule
        checkCC("h1 { color: red;\n | }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { color: red;\n az| }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { color: red;\n azimuth| }", arr("azimuth"), Match.CONTAINS);
    }

    //there are only some basic checks since the values completion itself
    //is tested by org.netbeans.modules.css.editor.PropertyModelTest
    public void testPropertyValues() throws ParseException {
//        checkCC("h1 { color: | }", arr("red"), Match.CONTAINS);
//        checkCC("h1 { color: r| }", arr("red"), Match.CONTAINS);

        //fails - questionable whether this is a bug or not,
        //at least it is not consistent with the property names completion
        //checkCC("h1 { color: red| }", arr("red"), Match.CONTAINS);

        checkCC("h1 { color: red | }", arr(), Match.EMPTY);
        checkCC("h1 { border: dotted | }", arr("blue"), Match.CONTAINS);
    }

    public void testCorners() throws ParseException {
        checkCC("h1 { bla| }", arr(), Match.EMPTY);
//        checkCC("h1 { color: ble| }", arr(), Match.EMPTY); //fails - issue #161129
    }

//    public void testIssue160870() throws ParseException {
//        checkCC("h1 { display : | }", arr("block"), Match.CONTAINS);
//    }

    public void testHtmlSelectorsCompletion() throws ParseException {
        checkCC("|", arr("html"), Match.CONTAINS);
        checkCC("ht| ", arr("html"), Match.EXACT);
        checkCC("html | ", arr("body"), Match.CONTAINS);
        checkCC("html bo| ", arr("body"), Match.EXACT);
        checkCC("html, bo| ", arr("body"), Match.EXACT);
        checkCC("html > bo| ", arr("body"), Match.EXACT);
        checkCC("html tit| { }", arr("title"), Match.CONTAINS);
    }
    
    public void testHtmlSelectorsCompletionAfterClassOrIdSelector() throws ParseException, BadLocationException {
        checkCC("#myid |", arr("html"), Match.CONTAINS);
        checkCC("#myid h|", arr("html"), Match.CONTAINS);
        assertComplete("#myid b| { }", "#myid body| { }", "body");
        assertComplete("#myid | { }", "#myid body| { }", "body");
        
        checkCC(".aclass |", arr("html"), Match.CONTAINS);
        checkCC(".aclass h|", arr("html"), Match.CONTAINS);
        assertComplete(".aclass b| { }", ".aclass body| { }", "body");
        assertComplete(".aclass | { }", ".aclass body| { }", "body");
    }
    
    public void testCompleteSelectors() throws ParseException, BadLocationException {
        assertComplete("html b| { }", "html body| { }", "body");
        assertComplete("html bo| { }", "html body| { }", "body");
        assertComplete("html body| { }", "html body| { }", "body");
        assertComplete("html | { }", "html body| { }", "body");
        assertComplete("| { }", "body| { }", "body");
        assertComplete("b| { }", "body| { }", "body");
    }

    public void testHtmlSelectorsInContent() throws ParseException {
        checkCC("h1 { color:red; } | h2 { color:red; }", arr("html"), Match.CONTAINS);
    }

    public void testSystemColors() throws ParseException {
        checkCC("div { color: | }", arr("menu", "window"), Match.CONTAINS);
    }

    public void testHtmlSelectorsInMedia() throws ParseException {
//        checkCC("@media page {  |   } ", arr("html"), Match.CONTAINS);
//        checkCC("@media page {  |   } ", arr("@media"), Match.DOES_NOT_CONTAIN); //media not supported here
        checkCC("@media page {  h1 { } |   } ", arr("html"), Match.CONTAINS); //media not supported here

//        checkCC("@media page {  htm|   } ", arr("html"), Match.EXACT);
//        checkCC("@media page {  html, |   } ", arr("body"), Match.CONTAINS);
//        checkCC("@media page {  html, bo|   } ", arr("body"), Match.CONTAINS);
//        checkCC("@media page {  html > bo|   } ", arr("body"), Match.CONTAINS);
    }

}

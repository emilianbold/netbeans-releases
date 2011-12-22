/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.properties.parser.PropertyModel;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class ListsAndCountersModuleTest extends CssModuleTestBase {

    public ListsAndCountersModuleTest(String testName) {
        super(testName);
    }

    public void testListStyle() {
        assertPropertyDeclaration("list-style: circle outside");
        assertPropertyDeclaration("list-style: lower-alpha");
        assertPropertyDeclaration("list-style: upper-roman inside ");
        assertPropertyDeclaration("list-style: symbols(\"*\" \"\\2020\" \"\\2021\" \"\\A7\");");
    }
    
    public void testListStyleCompletion() {
        PropertyModel p = CssModuleSupport.getPropertyModel("list-style");
        assertAlternatives(p.getGrammar(), "",
                "repeating-linear-gradient","lower-latin","lower-greek",
                "repeating-radial-gradient","disc","lower-alpha","lower-roman",
                "!identifier","georgian","element","upper-alpha","armenian",
                "upper-latin","linear-gradient","!string","image","decimal",
                "upper-roman","!uri","cross-fade","radial-gradient","inside",
                "decimal-leading-zero","square", "circle","none","symbols","outside");
    }
    
    public void testListStyleType() {
        assertPropertyDeclaration("list-style-type: circle");
        assertPropertyDeclaration("list-style-type: none");
        assertPropertyDeclaration("list-style-type: \"hello\"");
        assertPropertyDeclaration("list-style-type: someident");
        assertPropertyDeclaration("list-style-type: symbols(\"*\" \"\\2020\" \"\\2021\" \"\\A7\");");
    }
    
    public void testListStyleTypeCompletion() {
        PropertyModel p = CssModuleSupport.getPropertyModel("list-style-type");
        assertAlternatives(p.getGrammar(), "",
                "georgian","armenian","upper-alpha","upper-latin","!string","lower-latin",
                "circle","lower-greek","decimal","upper-roman","disc","lower-alpha",
                "symbols","lower-roman","none","decimal-leading-zero","square","!identifier");
        
        assertAlternatives(p.getGrammar(), "symbols", "(");
        assertAlternatives(p.getGrammar(), "symbols(",
                "repeating-linear-gradient","element","numeric","linear-gradient",
                "!string","alphabetic","image","symbolic","repeating-radial-gradient",
                "!uri","repeating","cross-fade","non-repeating","radial-gradient");
    }
    
    public void testListStyleImage() {
        assertPropertyDeclaration("list-style-image: none");        
        assertPropertyDeclaration("list-style-image: url(\"http://www.example.com/ellipse.png\")");        

    }
    public void testMarkerAttachmement() {
        assertPropertyDeclaration("marker-attachment: list-container");
        assertPropertyDeclaration("marker-attachment: list-item");
    }
    
    public void testListStylePosition() {
        assertPropertyDeclaration("list-style-position: inside");
        assertPropertyDeclaration("list-style-position: outside");
    }

    public void testMarkerPseudoElementCompletion() throws ParseException {
        checkCC("div::| ", arr("marker"), Match.CONTAINS);
        checkCC("li::mar| ", arr("marker"), Match.CONTAINS);
    }
}

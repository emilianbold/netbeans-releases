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
package org.netbeans.modules.css.lib.api.model;

import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;

/**
 *
 * @author marekfukala
 */
public class StylesheetTest extends NbTestCase {

    public StylesheetTest() {
        super(StylesheetTest.class.getName());
    }

    public void testBasis() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = "h1 { color: red; }";
        //                0123456789012345678
        //                0         1

        Stylesheet model = TestUtil.parse(content).getModel();
        assertNotNull(model);

        List<Rule> rules = model.rules();
        assertNotNull(rules);
        assertEquals(1, rules.size());

        Rule rule = rules.get(0);
        assertNotNull(rule);
        assertEquals("h1", rule.name());
        assertEquals(3, rule.getRuleOpenBracketOffset());
        assertEquals(17, rule.getRuleCloseBracketOffset());

        List<Declaration> items = rule.items();
        assertNotNull(items);
        assertEquals(1, items.size());

        Declaration item = rule.items().get(0);
        assertNotNull(item);

        Item property = item.getProperty();
        assertNotNull(property);
        assertEquals("color", property.image().toString());
        assertEquals(5, property.offset());

        Item value = item.getValue();
        assertNotNull(value);
        assertEquals("red", value.image().toString());
        assertEquals(12, value.offset());

    }

    public void testWsAfterPropertyName() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = "h1 { color : red; }";
        //                0123456789012345678
        //                0         1

        Stylesheet model = TestUtil.parse(content).getModel();
        assertNotNull(model);

        List<Rule> rules = model.rules();
        assertNotNull(rules);
        assertEquals(1, rules.size());


        Declaration item = rules.get(0).items().get(0);
        assertNotNull(item);

        Item property = item.getProperty();
        assertNotNull(property);

        //ws included to the node
        assertEquals("color ", property.image().toString());
        assertEquals(5, property.offset());

    }

    public void testSeverelyBrokenSource() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = "@ ";
        //                0123456789012345678
        //                0         1
        Stylesheet model = TestUtil.parse(content).getModel();
        
        assertNotNull(model);

        List<Rule> rules = model.rules();
        assertNotNull(rules);
        assertEquals(0, rules.size()); //no rules
    }


     public void testImportRule() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = " @import \"file.css\";\nh1 { color : red; }";
        //                0123456789012345678
        //                0         1
        Stylesheet model = TestUtil.parse(content).getModel();        
        assertNotNull(model);

        Collection<String> names = model.imported_files;
        assertNotNull(names);

        assertEquals(1, names.size());
        assertEquals("file.css", names.iterator().next());

    }
     
     
     public void testRuleWithoutCloseBracket() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = "h1 { color: red ";
        //                0123456789012345678
        //                0         1
        Stylesheet model = TestUtil.parse(content).getModel();        
        assertNotNull(model);

        Collection<Rule> rules = model.rules();
        assertNotNull(rules);
        assertEquals(1, rules.size());
        
        Rule rule = rules.iterator().next();
        assertNotNull(rule);        
        assertEquals(-1, rule.getRuleCloseBracketOffset());

    }
     
    public void testDeclarationWithoutProperyValueAndSemicolon() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = "h1 { color:  }";
        //                0123456789012345678
        //                0         1
        
        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpResult(result);
        
        Stylesheet model = result.getModel();        
        assertNotNull(model);

        Collection<Rule> rules = model.rules();
        assertNotNull(rules);
        assertEquals(1, rules.size());
        
        Rule rule = rules.iterator().next();
        assertNotNull(rule);        
        
        List<Declaration> declarations = rule.items();
        assertNotNull(declarations);
        assertEquals(1, declarations.size());
        
        Declaration decl = declarations.iterator().next();        
        assertNotNull(decl);
        
        assertEquals(10, decl.colonOffset());
        assertEquals(-1, decl.semicolonOffset());

        Item prop = decl.getProperty();
        assertNotNull(prop);
        assertEquals("color", prop.image().toString());
        
        Item val = decl.getValue();
        assertNotNull(val);
        assertEquals("", val.image().toString());
        
    }
    
    public void testNamespaces() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = " @namespace prefix \"uri\";";
        //                0123456789012345678
        //                0         1
        Stylesheet model = TestUtil.parse(content).getModel();        
        assertNotNull(model);

        Collection<Namespace> names = model.getNamespaces();
        assertNotNull(names);

        assertEquals(1, names.size());
        Namespace ns = names.iterator().next();
        
        assertEquals("prefix", ns.getPrefix());
        assertEquals("\"uri\"", ns.getResourceIdentifier());

    }

}

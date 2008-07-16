/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.css.editor;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.css.editor.CssPropertyValue.ResolvedToken;
import org.netbeans.modules.css.editor.PropertyModel.Element;
import org.netbeans.modules.css.editor.test.*;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.editor.PropertyModel.GroupElement;

/**
 * @author Marek Fukala
 */
public class PropertyModelTest extends TestBase {

    public PropertyModelTest() throws IOException, BadLocationException {
        super("CssTest");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSimpleSet() {
        GroupElement e = PropertyModel.instance().parse("one two three");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());
    }

    public void testSimpleList() {
        GroupElement e = PropertyModel.instance().parse("one || two || three");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());
    }

    public void testMultiplicity() {
        GroupElement e = PropertyModel.instance().parse("one+ two? three{1,4}");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());

        Element e1 = e.elements().get(0);
        assertEquals(1, e1.getMinimumOccurances());
        assertEquals(Integer.MAX_VALUE, e1.getMaximumOccurances());

        Element e2 = e.elements().get(1);
        assertEquals(0, e2.getMinimumOccurances());
        assertEquals(1, e2.getMaximumOccurances());

        Element e3 = e.elements().get(2);
        assertEquals(1, e3.getMinimumOccurances());
        assertEquals(4, e3.getMaximumOccurances());
    }

    public void testGroupsNesting() {
        GroupElement e = PropertyModel.instance().parse("one [two] [[three] || [four]]");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());
    }

    public void testConsume() {
        String text = "marek scroll fixed marek marek fixed gogo marek";
        String rule = "[ [color]{1,4} | transparent ] | inherit ";

        assertTrue(new CssPropertyValue(rule, "color").success());
        assertTrue(new CssPropertyValue(rule, "inherit").success());
        assertTrue(new CssPropertyValue(rule, "color color").success());
        assertTrue(new CssPropertyValue(rule, "color color color color").success());
        assertFalse(new CssPropertyValue(rule, "color inherit").success());
        assertFalse(new CssPropertyValue(rule, "color color color color color").success());
        assertFalse(new CssPropertyValue(rule, "transparent inherit").success());
    }

    private void dumpList(List<String> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            System.out.print("\"" + stack.get(i) + "\" ");
        }
    }

    private void dumpResult(CssPropertyValue pv) {

        System.out.println("Parsing \"" + pv.propertyDefinition() + "\"");

        System.out.println(pv.log());

        System.out.println(pv.groupElement.toString2(0));

        System.out.println("Success = " + pv.success());

        System.out.print("Tokens: ");
        dumpList(pv.originalStack);
        System.out.println("");

        int i = 0;
        System.out.println("Resolved:");
        for (ResolvedToken rt : pv.resolved()) {
            System.out.println("" + (i++) + ": " + rt.token() + "-> " + rt.element().path());
        }
        System.out.println("-------------");

        if (!pv.success()) {
            System.out.print("Unparsed okens: ");
            dumpList(pv.left());
            System.out.println("");
        }

        if (pv.alternatives().size() > 0) {
            System.out.println("Alternatives:");
            for (Element e : pv.alternatives()) {
                System.out.println(e.path());
            }
            System.out.println("-------------");

            System.out.println("Alternatives by name:");
            Collection<String> values = new HashSet<String>(20);
            for (Element e : pv.alternatives()) {
                values.add(e.toString());
            }

            for (String s : values) {
                System.out.println(s);
            }

        } else {
            System.out.println("No alternatives");
        }
    }

    public void testSequence() {
        String rule = "[marek]{1,2} > jitka";
        String text = "marek marek jitka";

        CssPropertyValue csspv = new CssPropertyValue(rule, text);

        assertTrue(csspv.success());

        rule = "marek > jitka";
        text = "jitka";

        csspv = new CssPropertyValue(rule, text);

        assertFalse(csspv.success());
    }

    public void testFillStack() {
        Stack<String> stack = new Stack<String>();
        CssPropertyValue.fillStack(stack, "bla , ble bli,blo,,blu bly,oh/eh//uh");
//        dumpList(stack);
        assertEquals(17, stack.size());
    }

     public void testFillStackWithQuotedValues() {
        Stack<String> stack = new Stack<String>();
        CssPropertyValue.fillStack(stack, "'Times New Roman',serif");
//        dumpList(stack);
        assertEquals(3, stack.size());
    }
    
    public void testFont() {
        Property p = PropertyModel.instance().getProperty("font");
        CssPropertyValue pv = new CssPropertyValue(p, "20% serif");
//        dumpResult(pv);
        assertTrue(pv.success());
    }

    public void testZeroMultiplicity() {
        String rule = "[marek]? > [jitka]? > [ovecka]";
        String text = "ovecka";
        CssPropertyValue csspv = new CssPropertyValue(rule, text);
        assertTrue(csspv.success());
    }

    public void testAlternativesInSequence() {
        String rule = "marek > jitka > ovecka";

        String text = "marek";
        CssPropertyValue csspv = new CssPropertyValue(rule, text);
        assertTrue(csspv.success());

        assertEquals(1, csspv.alternatives().size());
        assertEquals("jitka", csspv.alternatives().iterator().next().toString());

        text = "marek jitka";
        csspv = new CssPropertyValue(rule, text);

        assertEquals(1, csspv.alternatives().size());
        assertEquals("ovecka", csspv.alternatives().iterator().next().toString());

        text = "marek jitka ovecka";
        csspv = new CssPropertyValue(rule, text);

        assertEquals(0, csspv.alternatives().size());

    }

    public void testFontFamily() {
        Property p = PropertyModel.instance().getProperty("font-family");

        assertTrue(new CssPropertyValue(p, "serif").success());
        assertTrue(new CssPropertyValue(p, "cursive, serif").success());
        assertFalse(new CssPropertyValue(p, "cursive serif").success());
    }
    
     public void testFontFamilyWithQuotedValue() {
        Property p = PropertyModel.instance().getProperty("font-family");
        CssPropertyValue csspv = new CssPropertyValue(p, "'Times New Roman',serif");
//        dumpResult(csspv);
        assertTrue(csspv.success());
    }
    
    public void testFontAlternatives() {
        Property p = PropertyModel.instance().getProperty("font");
        String text = "italic small-caps 30px";
        
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        
        assertTrue(csspv.success());
//        dumpResult(csspv);
        
    }
   
     
    public void testFontSize() {
        Property p = PropertyModel.instance().getProperty("font-size");
        String text = "xx-small";
        
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        
        assertTrue(csspv.success());
    }
}

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

import java.util.ArrayList;
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

    public void testFillStackWithBraces() {
        Stack<String> stack = new Stack<String>();
        CssPropertyValue.fillStack(stack, "rect(20,30,40)");
//        dumpList(stack);
        assertEquals(8, stack.size());
    }

    public void testFillStackWithNewLine() {
        Stack<String> stack = new Stack<String>();
        CssPropertyValue.fillStack(stack, "marek jitka \n");
//        dumpList(stack);
        assertEquals(2, stack.size());
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

    public void testAlternativesOfSequenceInSequence() {
        String rule = "marek > jitka > [ ovecka > beranek ]";

        String text = "marek jitka";
        CssPropertyValue csspv = new CssPropertyValue(rule, text);
        assertTrue(csspv.success());

        assertEquals(1, csspv.alternatives().size());
        assertEquals("ovecka", csspv.alternatives().iterator().next().toString());

        text = "marek jitka ovecka";
        csspv = new CssPropertyValue(rule, text);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());
        assertEquals("beranek", csspv.alternatives().iterator().next().toString());

        text = "marek jitka beranek";
        csspv = new CssPropertyValue(rule, text);

        assertFalse(csspv.success());


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

    public void testColorValues() {
        Property p = PropertyModel.instance().getProperty("color");
        assertTrue(new CssPropertyValue(p, "rgb(10,20,30)").success());
        assertTrue(new CssPropertyValue(p, "rgb(10%,20,30)").success());
        assertTrue(new CssPropertyValue(p, "#ffaa00").success());
        assertTrue(new CssPropertyValue(p, "#fb0").success());
        assertFalse(new CssPropertyValue(p, "#fa001").success());
        assertFalse(new CssPropertyValue(p, "rgb(,20,30)").success());
        assertFalse(new CssPropertyValue(p, "rgb(10,x,30)").success());
    }

    public void testBorder() {
        Property p = PropertyModel.instance().getProperty("border");
        String text = "20px double";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
    }

    public void testMarginWidth() {
        Property p = PropertyModel.instance().getProperty("margin");
        String text = "20px 10em 30px 30em";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
    }

    public void testPaddingWidth() {
        Property p = PropertyModel.instance().getProperty("padding");
        String text = "20px 10em 30px 30em";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
    }

    public void testTimeUnit() {
        Property p = PropertyModel.instance().getProperty("pause-after");
        String text = "200ms";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());

        text = "200";
        csspv = new CssPropertyValue(p, text);
        assertFalse(csspv.success());

        text = "AAms";
        csspv = new CssPropertyValue(p, text);
        assertFalse(csspv.success());

    }

    public void testFrequencyUnit() {
        Property p = PropertyModel.instance().getProperty("pitch");
        String text = "200kHz";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());

        text = "200";
        csspv = new CssPropertyValue(p, text);
        assertFalse(csspv.success());

        text = "AAHz";
        csspv = new CssPropertyValue(p, text);
        assertFalse(csspv.success());

    }

    public void testIdentifierUnit() {
        Property p = PropertyModel.instance().getProperty("counter-increment");
        String text = "ovecka";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());

        text = "10ovecek";
        csspv = new CssPropertyValue(p, text);
        assertFalse(csspv.success());

        text = "-beranek";
        csspv = new CssPropertyValue(p, text);
        assertFalse(csspv.success());

    }

    public void testVoiceFamily() {
        Property p = PropertyModel.instance().getProperty("voice-family");
        String text = "male";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());
        assertEquals(",", csspv.alternatives().iterator().next().toString());

        text = "male, ";
        csspv = new CssPropertyValue(p, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());
        assertEquals(3, csspv.visibleAlternatives().size());

        Collection<String> altNames = getAlternativesNames(csspv.visibleAlternatives());
        assertTrue(altNames.contains("male"));
        assertTrue(altNames.contains("female"));
        assertTrue(altNames.contains("child"));

        text = "";
        csspv = new CssPropertyValue(p, text);

        assertTrue(csspv.success());
        assertEquals(4, csspv.visibleAlternatives().size());

        altNames = getAlternativesNames(csspv.visibleAlternatives());
        assertTrue(altNames.contains("inherit"));
        assertTrue(altNames.contains("male"));
        assertTrue(altNames.contains("female"));
        assertTrue(altNames.contains("child"));

        text = "\"ovecka\"";
        csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
    }

    public void testBackgroundImageURL() {
        Property p = PropertyModel.instance().getProperty("background-image");
        String text = "url('/images/v6/tabs-bg.png')";
        CssPropertyValue csspv = new CssPropertyValue(p, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());

        text = "url'/images/v6/tabs-bg.png')";
        csspv = new CssPropertyValue(p, text);
        assertFalse(csspv.success());

        text = "ury('/images/v6/tabs-bg.png')";
        csspv = new CssPropertyValue(p, text);
        assertFalse(csspv.success());

    }

    public void testPaddingAlternatives() {
        Property p = PropertyModel.instance().getProperty("padding");
        String text = "";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
    }

    public void testAlternativesInGroupMultiplicity() {
        String rule = "[ marek ]*";
        String text = "marek";
        CssPropertyValue csspv = new CssPropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());

        rule = "[ marek jitka ]*";
        text = "marek";
        csspv = new CssPropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(2, csspv.alternatives().size());

        rule = "[ marek > jitka? > ovecka ]*";
        text = "marek jitka";
        csspv = new CssPropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());
        assertEquals("ovecka", csspv.alternatives().iterator().next().toString());

        rule = "[ marek > jitka? > ovecka ]*";
        text = "marek ovecka";
        csspv = new CssPropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());
        assertEquals("marek", csspv.alternatives().iterator().next().toString());


        rule = "[ marek > jitka? > ovecka ]*";
        text = "marek marek";
        csspv = new CssPropertyValue(rule, text);

//        dumpResult(csspv);
        assertFalse(csspv.success());

    }
    //some text acceptors consumed "inherit" as their token
    public void testFontFamily2() {
        Property p = PropertyModel.instance().getProperty("font-family");
        String text = "";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(7, csspv.alternatives().size()); //only comma should be alternative
    }
    //some text acceptors consumed "inherit" as their token
    public void testFontFamilyInheritProblem() {
        Property p = PropertyModel.instance().getProperty("font-family");
        String text = "inherit";
        CssPropertyValue csspv = new CssPropertyValue(p, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());
        assertEquals(0, csspv.alternatives().size()); //only comma should be alternative
    }

    public void testFontThoroughly() {
        Property p = PropertyModel.instance().getProperty("font");
        String text = "20px";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(7, csspv.alternatives().size());
        assertEquals(6, csspv.visibleAlternatives().size());

        text = "20px / ";
        csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(1, csspv.visibleAlternatives().size());
        assertEquals("normal", csspv.visibleAlternatives().iterator().next().toString());

        text = "20px / 5pt";
        csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(5, csspv.visibleAlternatives().size());

        text = "20px / 5pt cursive";
        csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(0, csspv.visibleAlternatives().size()); //only comma should be alternative

    }

    public void testFontThoroughly2() {
        Property p = PropertyModel.instance().getProperty("font");
        String text = "italic";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(9, csspv.visibleAlternatives().size());

        Element alt1 = csspv.alternatives().iterator().next();
        assertNotNull(alt1);
//        assertEquals("-absolute-size", alt1.origin());

        Collection<String> altNames = getAlternativesNames(csspv.alternatives());
        assertTrue(altNames.contains("large"));
        assertTrue(altNames.contains("larger"));
        assertTrue(altNames.contains("medium"));
        assertTrue(altNames.contains("small"));
        assertTrue(altNames.contains("smaller"));
        assertTrue(altNames.contains("x-large"));
        assertTrue(altNames.contains("x-small"));
        assertTrue(altNames.contains("xx-large"));
        assertTrue(altNames.contains("xx-small"));

        text = "italic large";
        csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(6, csspv.visibleAlternatives().size());

        altNames = getAlternativesNames(csspv.alternatives());
        assertTrue(altNames.contains("/"));
        assertTrue(altNames.contains("cursive"));
        assertTrue(altNames.contains("fantasy"));
        assertTrue(altNames.contains("monospace"));
        assertTrue(altNames.contains("serif"));
        assertTrue(altNames.contains("sans-serif"));

        text = "italic large /";
        csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(1, csspv.visibleAlternatives().size());

        alt1 = csspv.alternatives().iterator().next();
        assertNotNull(alt1);
        assertEquals("line-height", alt1.origin());

        altNames = getAlternativesNames(csspv.alternatives());
        assertTrue(altNames.contains("normal"));

        text = "italic large / normal";
        csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(5, csspv.visibleAlternatives().size()); //only comma should be alternative

        altNames = getAlternativesNames(csspv.alternatives());
        assertTrue(altNames.contains("cursive"));
        assertTrue(altNames.contains("fantasy"));
        assertTrue(altNames.contains("monospace"));
        assertTrue(altNames.contains("serif"));
        assertTrue(altNames.contains("sans-serif"));

    }

    public void testCaseSensitivity() {
        Property p = PropertyModel.instance().getProperty("azimuth");
        String text = "behind";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());

        text = "BEHIND";
        csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());

    }

    public void testAbsoluteLengthUnits() {
        Property p = PropertyModel.instance().getProperty("font");
        String text = "12px/14cm sans-serif";
        CssPropertyValue csspv = new CssPropertyValue(p, text);
        assertTrue(csspv.success());
    }

    private Collection<String> getAlternativesNames(Collection<Element> alts) {
        List<String> names = new ArrayList<String>();
        for (Element e : alts) {
            names.add(e.toString());
        }
        return names;
    }

    public void testBackgroundRGBAlternatives() {
        Property p = PropertyModel.instance().getProperty("background");
        String text = "rgb";

        CssPropertyValue csspv = new CssPropertyValue(p, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());
        assertEquals(1, csspv.visibleAlternatives().size());

        Element alt1 = csspv.alternatives().iterator().next();
        assertNotNull(alt1);
        assertEquals("(", alt1.toString());

    }

    public void testAlternativesOfPartialyResolvedSequenceInListGroup() {
        String rule = "[ [ Ema > ma > misu] || prd";
        String text = "Ema";
        CssPropertyValue csspv = new CssPropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());
        assertEquals("ma", csspv.alternatives().iterator().next().toString());
    }

    public void testJindrasCase() {
        String rule = "[ [ x || y ] || b";
        String text = "x b";
        CssPropertyValue csspv = new CssPropertyValue(rule, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());
        assertEquals(0, csspv.alternatives().size());
//        assertEquals("ma", csspv.alternatives().iterator().next().toString());
    }

    public void testUnquotedURL() {
        Property p = PropertyModel.instance().getProperty("list-style");
        String text = "url(http://www.redballs.com/redball.png)";

        CssPropertyValue csspv = new CssPropertyValue(p, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());

    }

    public void testFillStackWithURL() {
        Stack<String> stack = new Stack<String>();
        CssPropertyValue.fillStack(stack, "url(http://www.redballs.com/redball.png)");
//        dumpList(stack);
        assertEquals(4, stack.size());
    }
    
    public void testBackroundImage() {
        Property p = PropertyModel.instance().getProperty("background-image");
        String text = "";

        CssPropertyValue csspv = new CssPropertyValue(p, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());

    }
    
    public void testBackroundPositionOrder() {
        Property p = PropertyModel.instance().getProperty("background-position");
        String text = "center top";

        CssPropertyValue csspv = new CssPropertyValue(p, text);

        dumpResult(csspv);

        assertTrue(csspv.success());

    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.css.editor.properties.parser;

import java.util.Set;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.css.editor.properties.parser.PropertyValue.ResolvedToken;
import org.netbeans.modules.css.editor.test.*;

/**
 * @author Marek Fukala
 */
public class PropertyModelTest extends TestBase {

    public PropertyModelTest(String name) {
        super(name);
    }

    public void testSimpleSet() {
        GroupGrammarElement e = GrammarParser.parse("one | two | three");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());
    }

    public void testSimpleList() {
        GroupGrammarElement e = GrammarParser.parse("one || two || three");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());
    }

    public void testMultiplicity() {
        GroupGrammarElement e = GrammarParser.parse("one+ two? three{1,4}");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());

        GrammarElement e1 = e.elements().get(0);
        assertEquals(1, e1.getMinimumOccurances());
        assertEquals(Integer.MAX_VALUE, e1.getMaximumOccurances());

        GrammarElement e2 = e.elements().get(1);
        assertEquals(0, e2.getMinimumOccurances());
        assertEquals(1, e2.getMaximumOccurances());

        GrammarElement e3 = e.elements().get(2);
        assertEquals(1, e3.getMinimumOccurances());
        assertEquals(4, e3.getMaximumOccurances());
    }

    public void testGroupsNesting() {
        GroupGrammarElement e = GrammarParser.parse("one [two] [[three] || [four]]");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());
    }

    public void testConsume() {
        String rule = "[ [color]{1,4} | transparent ] | inherit ";

        assertTrue(new PropertyValue(rule, "color").success());
        assertTrue(new PropertyValue(rule, "inherit").success());
        assertTrue(new PropertyValue(rule, "color color").success());
        assertTrue(new PropertyValue(rule, "color color color color").success());
        assertFalse(new PropertyValue(rule, "color inherit").success());
        assertFalse(new PropertyValue(rule, "color color color color color").success());
        assertFalse(new PropertyValue(rule, "transparent inherit").success());
    }

    private static void dumpList(List<String> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            System.out.print("\"" + stack.get(i) + "\" ");
        }
    }

    public static void dumpResult(PropertyValue pv) {

        System.out.println("Parsing \"" + pv.propertyDefinition() + "\"");

        System.out.println(pv.log());

        System.out.println(pv.groupGrammarElement.toString2(0));

        System.out.println("Success = " + pv.success());

        System.out.print("Tokens: ");
        dumpList(pv.originalStack);
        System.out.println("");

        int i = 0;
        System.out.println("Resolved:");
        for (ResolvedToken rt : pv.resolved()) {
            System.out.println("" + (i++) + ": " + rt.token() + "-> " + rt.getGrammarElement().path());
        }
        System.out.println("-------------");

        if (!pv.success()) {
            System.out.print("Unparsed okens: ");
            dumpList(pv.left());
            System.out.println("");
        }

        if (pv.alternatives().size() > 0) {
            System.out.println("Alternatives:");
            for (GrammarElement e : pv.alternatives()) {
                System.out.println(e.path());
            }
            System.out.println("-------------");

            System.out.println("Alternatives by name:");
            Collection<String> values = new HashSet<String>(20);
            for (GrammarElement e : pv.alternatives()) {
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
        String rule = "[marek]{1,2} jitka";
        String text = "marek marek jitka";

        PropertyValue csspv = new PropertyValue(rule, text);

        assertTrue(csspv.success());

        rule = "marek jitka";
        text = "jitka";

        csspv = new PropertyValue(rule, text);

        assertFalse(csspv.success());
    }

    public void testFillStack() {
        Stack<String> stack = new Stack<String>();
        PropertyValue.fillStack(stack, "bla , ble bli,blo,,blu bly,oh/eh//uh");
//        dumpList(stack);
        assertEquals(17, stack.size());
    }

    public void testFillStackWithQuotedValues() {
        Stack<String> stack = new Stack<String>();
        PropertyValue.fillStack(stack, "'Times New Roman',serif");
//        dumpList(stack);
        assertEquals(3, stack.size());
    }

    public void testFillStackWithBraces() {
        Stack<String> stack = new Stack<String>();
        PropertyValue.fillStack(stack, "rect(20,30,40)");
//        dumpList(stack);
        assertEquals(8, stack.size());
    }

    public void testFillStackWithNewLine() {
        Stack<String> stack = new Stack<String>();
        PropertyValue.fillStack(stack, "marek jitka \n");
//        dumpList(stack);
        assertEquals(2, stack.size());
    }

    public void testFont() {
        PropertyModel p = CssModuleSupport.getProperty("font");
        PropertyValue pv = new PropertyValue(p, "20% serif");
//        dumpResult(pv);
        assertTrue(pv.success());
    }

    public void testZeroMultiplicity() {
        String rule = "[marek]?  [jitka]?  [ovecka]";
        String text = "ovecka";
        PropertyValue csspv = new PropertyValue(rule, text);
        assertTrue(csspv.success());
    }

    public void testAlternativesInSequence() {
        String rule = "marek  jitka  ovecka";

        String text = "marek";
        PropertyValue csspv = new PropertyValue(rule, text);
        assertTrue(csspv.success());

        assertEquals(1, csspv.alternatives().size());
        assertEquals("jitka", csspv.alternatives().iterator().next().toString());

        text = "marek jitka";
        csspv = new PropertyValue(rule, text);

        assertEquals(1, csspv.alternatives().size());
        assertEquals("ovecka", csspv.alternatives().iterator().next().toString());

        text = "marek jitka ovecka";
        csspv = new PropertyValue(rule, text);

        assertEquals(0, csspv.alternatives().size());

    }

    public void testAlternativesOfSequenceInSequence() {
        String rule = "marek  jitka  [ ovecka  beranek ]";

        String text = "marek jitka";
        PropertyValue csspv = new PropertyValue(rule, text);
        assertTrue(csspv.success());
        
//        dumpResult(csspv);

        assertEquals(1, csspv.alternatives().size());
        assertEquals("ovecka", csspv.alternatives().iterator().next().toString());

        text = "marek jitka ovecka";
        csspv = new PropertyValue(rule, text);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());
        assertEquals("beranek", csspv.alternatives().iterator().next().toString());

        text = "marek jitka beranek";
        csspv = new PropertyValue(rule, text);

        assertFalse(csspv.success());


    }

    public void testFontFamily() {
        PropertyModel p = CssModuleSupport.getProperty("font-family");

        assertTrue(new PropertyValue(p, "serif").success());
        assertTrue(new PropertyValue(p, "cursive, serif").success());
        assertFalse(new PropertyValue(p, "cursive serif").success());
    }

    public void testFontFamilyWithQuotedValue() {
        PropertyModel p = CssModuleSupport.getProperty("font-family");
        PropertyValue csspv = new PropertyValue(p, "'Times New Roman',serif");
//        dumpResult(csspv);
        assertTrue(csspv.success());
    }

    public void testFontAlternatives() {
        PropertyModel p = CssModuleSupport.getProperty("font");
        String text = "italic small-caps 30px";

        PropertyValue csspv = new PropertyValue(p, text);

        assertTrue(csspv.success());
//        dumpResult(csspv);

    }

    public void testFontSize() {
        PropertyModel p = CssModuleSupport.getProperty("font-size");
        String text = "xx-small";

        PropertyValue csspv = new PropertyValue(p, text);

        assertTrue(csspv.success());
    }

    public void testBorder() {
        PropertyModel p = CssModuleSupport.getProperty("border");
        String text = "20px double";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
    }

    public void testMarginWidth() {
        PropertyModel p = CssModuleSupport.getProperty("margin");
        String text = "20px 10em 30px 30em";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
    }

    public void testPaddingWidth() {
        PropertyModel p = CssModuleSupport.getProperty("padding");
        String text = "20px 10em 30px 30em";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
    }

    public void testTimeUnit() {
        PropertyModel p = CssModuleSupport.getProperty("pause-after");
        String text = "200ms";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());

        text = "200";
        csspv = new PropertyValue(p, text);
        assertFalse(csspv.success());

        text = "AAms";
        csspv = new PropertyValue(p, text);
        assertFalse(csspv.success());

    }

    public void testFrequencyUnit() {
        PropertyModel p = CssModuleSupport.getProperty("pitch");
        String text = "200kHz";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());

        text = "200";
        csspv = new PropertyValue(p, text);
        assertFalse(csspv.success());

        text = "AAHz";
        csspv = new PropertyValue(p, text);
        assertFalse(csspv.success());

    }

    public void testIdentifierUnit() {
        PropertyModel p = CssModuleSupport.getProperty("counter-increment");
        String text = "ovecka";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());

        text = "10ovecek";
        csspv = new PropertyValue(p, text);
        assertFalse(csspv.success());

        text = "-beranek";
        csspv = new PropertyValue(p, text);
        assertFalse(csspv.success());

    }

//    public void testVoiceFamily() {
//        PropertyModel p = CssModuleSupport.getProperty("voice-family");
//        String text = "male";
//        PropertyValue csspv = new PropertyValue(p, text);
//        assertTrue(csspv.success());
//        assertEquals(1, csspv.alternatives().size());
//        assertEquals(",", csspv.alternatives().iterator().next().toString());
//
//        text = "male, ";
//        csspv = new PropertyValue(p, text);
//
////        dumpResult(csspv);
//
//        assertTrue(csspv.success());
//        assertEquals(3, csspv.visibleAlternatives().size());
//
//        Collection<String> altNames = getAlternativesNames(csspv.visibleAlternatives());
//        assertTrue(altNames.contains("male"));
//        assertTrue(altNames.contains("female"));
//        assertTrue(altNames.contains("child"));
//
//        text = "";
//        csspv = new PropertyValue(p, text);
//
//        assertTrue(csspv.success());
//        assertEquals(4, csspv.visibleAlternatives().size());
//
//        altNames = getAlternativesNames(csspv.visibleAlternatives());
//        assertTrue(altNames.contains("inherit"));
//        assertTrue(altNames.contains("male"));
//        assertTrue(altNames.contains("female"));
//        assertTrue(altNames.contains("child"));
//
//        text = "\"ovecka\"";
//        csspv = new PropertyValue(p, text);
//        assertTrue(csspv.success());
//    }

    public void testBackgroundImageURL() {
        PropertyModel p = CssModuleSupport.getProperty("background-image");
        String text = "url('/images/v6/tabs-bg.png')";
        PropertyValue csspv = new PropertyValue(p, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());

        text = "url'/images/v6/tabs-bg.png')";
        csspv = new PropertyValue(p, text);
        assertFalse(csspv.success());

        text = "ury('/images/v6/tabs-bg.png')";
        csspv = new PropertyValue(p, text);
        assertFalse(csspv.success());

    }

    public void testPaddingAlternatives() {
        PropertyModel p = CssModuleSupport.getProperty("padding");
        String text = "";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
    }

    public void testAlternativesInGroupMultiplicity() {
        String rule = "[ marek ]*";
        String text = "marek";
        PropertyValue csspv = new PropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());

        rule = "[ marek | jitka ]*";
        text = "marek";
        csspv = new PropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(2, csspv.alternatives().size());

        rule = "[ marek jitka? ovecka ]*";
        text = "marek jitka";
        csspv = new PropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());
        assertEquals("ovecka", csspv.alternatives().iterator().next().toString());

        rule = "[ marek jitka? ovecka ]*";
        text = "marek ovecka";
        csspv = new PropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());
        assertEquals("marek", csspv.alternatives().iterator().next().toString());


        rule = "[ marek jitka? ovecka ]*";
        text = "marek marek";
        csspv = new PropertyValue(rule, text);

//        dumpResult(csspv);
        assertFalse(csspv.success());

    }
    //some text acceptors consumed "inherit" as their token
    public void testFontFamily2() {
        PropertyModel p = CssModuleSupport.getProperty("font-family");
        String text = "";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
        
//        for(GrammarElement e :csspv.alternatives()) {
//            System.out.println(e.toString());
//        }
        
        //removed inherit from the alts
        assertEquals(6, csspv.alternatives().size()); //only comma should be alternative
    }
    //some text acceptors consumed "inherit" as their token
    
    //reenable once I somehow fix the "inherit" generic property
//    public void testFontFamilyInheritProblem() {
//        PropertyModel p = CssModuleSupport.getProperty("font-family");
//        String text = "inherit";
//        PropertyValue csspv = new PropertyValue(p, text);
//
////        dumpResult(csspv);
//
//        assertTrue(csspv.success());
//        assertEquals(0, csspv.alternatives().size()); //only comma should be alternative
//    }

    public void testFontThoroughly() {
        PropertyModel p = CssModuleSupport.getProperty("font");
        String text = "20px";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(7, csspv.alternatives().size());
        assertEquals(6, csspv.visibleAlternatives().size());

        text = "20px / ";
        csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
        
        assertAlternatives(csspv, false, "normal");

        text = "20px / 5pt";
        csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(5, csspv.visibleAlternatives().size());

        text = "20px / 5pt cursive";
        csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(0, csspv.visibleAlternatives().size()); //only comma should be alternative

    }
    
    public void assertAlternatives(PropertyValue val, boolean origins, String... alternatives) {
        Set<String> altNamesSet = new HashSet<String>();
        altNamesSet.addAll(Arrays.asList(alternatives));
        Collection<String> existingAltNames = new ArrayList<String>();
        for(GrammarElement ge : val.visibleAlternatives()) {
            String geName = !origins && ge instanceof ValueGrammarElement 
                    ? ((ValueGrammarElement)ge).value()
                    : ge.origin();
            altNamesSet.remove(geName);
            existingAltNames.add(geName);
        }
    
        if(!altNamesSet.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for(String alt : altNamesSet) {
                sb.append('"');
                sb.append(alt);
                sb.append('"');
                sb.append(' ');
            }
            
            StringBuilder sb2 = new StringBuilder();
            for(String alt : existingAltNames) {
                sb2.append('"');
                sb2.append(alt);
                sb2.append('"');
                sb2.append(' ');
            }
            
            assertTrue(String.format("Missing expected alternatives: %s, found alternatives: %s ", sb.toString(), sb2.toString()), false);
        }
    }
    
    public void testFontThoroughly2() {
        PropertyModel p = CssModuleSupport.getProperty("font");
        String text = "italic";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
        assertEquals(9, csspv.visibleAlternatives().size());

        GrammarElement alt1 = csspv.alternatives().iterator().next();
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
        csspv = new PropertyValue(p, text);
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
        csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
        
        assertAlternatives(csspv, true, "line-height");
        
        alt1 = csspv.alternatives().iterator().next();
        assertNotNull(alt1);
        assertEquals("line-height", alt1.origin());

        altNames = getAlternativesNames(csspv.alternatives());
        assertTrue(altNames.contains("normal"));

        text = "italic large / normal";
        csspv = new PropertyValue(p, text);
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
        PropertyModel p = CssModuleSupport.getProperty("azimuth");
        String text = "behind";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());

        text = "BEHIND";
        csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());

    }

    public void testAbsoluteLengthUnits() {
        PropertyModel p = CssModuleSupport.getProperty("font");
        String text = "12px/14cm sans-serif";
        PropertyValue csspv = new PropertyValue(p, text);
        assertTrue(csspv.success());
    }

    private Collection<String> getAlternativesNames(Collection<GrammarElement> alts) {
        List<String> names = new ArrayList<String>();
        for (GrammarElement e : alts) {
            names.add(e.toString());
        }
        return names;
    }

    public void testBackgroundRGBAlternatives() {
        PropertyModel p = CssModuleSupport.getProperty("background");
        String text = "rgb";

        PropertyValue csspv = new PropertyValue(p, text);

        dumpResult(csspv);

        assertTrue(csspv.success());
        assertEquals(1, csspv.visibleAlternatives().size());

        GrammarElement alt1 = csspv.alternatives().iterator().next();
        assertNotNull(alt1);
        assertEquals("(", alt1.toString());

    }

    public void testAlternativesOfPartialyResolvedSequenceInListGroup() {
        String rule = "[ [ Ema ma misu] || prd";
        String text = "Ema";
        PropertyValue csspv = new PropertyValue(rule, text);

//        dumpResult(csspv);
        assertTrue(csspv.success());
        assertEquals(1, csspv.alternatives().size());
        assertEquals("ma", csspv.alternatives().iterator().next().toString());
    }

    public void XXXtestJindrasCase() {

        // TODO: fix #142254 and enable this test again

        String rule = "[ [ x || y ] || b";
        String text = "x b";
        PropertyValue csspv = new PropertyValue(rule, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());
        assertEquals(0, csspv.alternatives().size());
//        assertEquals("ma", csspv.alternatives().iterator().next().toString());
    }

    public void testUnquotedURL() {
        PropertyModel p = CssModuleSupport.getProperty("-uri");
        String text = "url(http://www.redballs.com/redball.png)";

        PropertyValue csspv = new PropertyValue(p, text);

        dumpResult(csspv);

        assertTrue(csspv.success());

    }

    public void testFillStackWithURL() {
        Stack<String> stack = new Stack<String>();
        PropertyValue.fillStack(stack, "url(http://www.redballs.com/redball.png)");
//        dumpList(stack);
        assertEquals(4, stack.size());
    }
    
    public void testBackroundImage() {
        PropertyModel p = CssModuleSupport.getProperty("background-image");
        String text = "";

        PropertyValue csspv = new PropertyValue(p, text);

//        dumpResult(csspv);

        assertTrue(csspv.success());

    }
    
    public void XXXtestBackroundPositionOrder() {

        // TODO: fix #142254 and enable this test again

        PropertyModel p = CssModuleSupport.getProperty("background-position");
        String text = "center top";

        PropertyValue csspv = new PropertyValue(p, text);

        dumpResult(csspv);

        assertTrue(csspv.success());

    }

    public void testCommnetsInValue() {
        PropertyModel p = CssModuleSupport.getProperty("border-color");
        String text = "red /* comment */ yellow black yellow";

        PropertyValue csspv = new PropertyValue(p, text);

        dumpResult(csspv);

        assertTrue(csspv.success());
    }

    public void testBorder_Top_Style() {
        PropertyModel p = CssModuleSupport.getProperty("border-top-style");

        PropertyValue csspv = new PropertyValue(p, "dotted dotted dashed dashed");
        assertFalse(csspv.success());

        csspv = new PropertyValue(p, "dotted");
        assertTrue(csspv.success());

    }

    public void testIssue185995() {
        PropertyModel p = CssModuleSupport.getProperty("border-color");

        PropertyValue csspv = new PropertyValue(p, "transparent transparent");
        assertTrue(csspv.success());

    }
}

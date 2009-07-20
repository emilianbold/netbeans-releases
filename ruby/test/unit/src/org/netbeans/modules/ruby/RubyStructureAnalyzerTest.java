/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.ruby;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.ruby.elements.AstAttributeElement;
import org.netbeans.modules.ruby.elements.AstClassElement;
import org.netbeans.modules.ruby.options.TypeInferenceSettings;

/**
 * @author Tor Norbye
 */
public class RubyStructureAnalyzerTest extends RubyTestBase {

    public RubyStructureAnalyzerTest(String testName) {
        super(testName);
        RubyIndexer.userSourcesTest = true;
        TypeInferenceSettings.getDefault().setMethodTypeInference(true);
        TypeInferenceSettings.getDefault().setRdocTypeInference(true);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return rubyTestsClassPath();
    }

    private void checkAttributes(String relFilePath) throws Exception {
        Parser.Result parserResult = getParserResult(relFilePath);
        RubyParseResult rpr = AstUtilities.getParseResult(parserResult);
        RubyStructureAnalyzer.AnalysisResult ar = rpr.getStructure();
        Map<AstClassElement, Set<AstAttributeElement>> attributes = ar.getAttributes();

        StringBuilder sb = new StringBuilder();
        // Gotta sort the results
        List<AstClassElement> clzList = new ArrayList<AstClassElement>(attributes.keySet());
        Collections.sort(clzList, new Comparator<AstClassElement>() {

            public int compare(AstClassElement arg0, AstClassElement arg1) {
                return arg0.getFqn().compareTo(arg1.getFqn());
            }
        });
        for (AstClassElement clz : clzList) {
            Set<AstAttributeElement> aes = attributes.get(clz);
            if (aes != null) {
                sb.append(clz.getFqn());
                sb.append("\n");
                List<AstAttributeElement> attributeList = new ArrayList<AstAttributeElement>(aes);
                Collections.sort(attributeList, new Comparator<AstAttributeElement>() {

                    public int compare(AstAttributeElement arg0, AstAttributeElement arg1) {
                        return arg0.getName().compareTo(arg1.getName());
                    }
                });
                for (AstAttributeElement ae : attributeList) {
                    sb.append("  ");
                    sb.append(ae.getName());
                    sb.append("\n");
                }
            }
        }
        String annotatedSource = sb.toString();

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".attributes");
    }

    public void testAnalysis() throws Exception {
        checkStructure("testfiles/postgresql_adapter.rb");
    }

    public void testAnalysis2() throws Exception {
        checkStructure("testfiles/ape.rb");
    }

    public void testAnalysis3() throws Exception {
        checkStructure("testfiles/date.rb");
    }

    public void testAnalysis4() throws Exception {
        checkStructure("testfiles/resolv.rb");
    }

    public void testUnused() throws Exception {
        checkStructure("testfiles/unused.rb");
    }

    public void testProtectionLevels() throws Exception {
        checkStructure("testfiles/protection_levels.rb");
    }

    public void testAttributes1() throws Exception {
        checkAttributes("testfiles/resolv.rb");
    }

    public void testAttributes2() throws Exception {
        checkAttributes("testfiles/attr_declaration.rb");
    }

    public void testFolds1() throws Exception {
        checkFolds("testfiles/resolv.rb");
    }

    public void testFolds2() throws Exception {
        checkFolds("testfiles/postgresql_adapter.rb");
    }

    public void testFolds3() throws Exception {
        checkFolds("testfiles/ape.rb");
    }

    public void testFolds4() throws Exception {
        checkFolds("testfiles/date.rb");
    }

    public void testFolds5() throws Exception {
        checkFolds("testfiles/unused.rb");
    }

    public void testRubyStructureItemEqualsAndHashCode() throws Exception {
        ParserResult parserResult = getParserResult("testfiles/testRubyStructureItemEqualsAndHashCode.rb");
        RubyStructureAnalyzer analyzer = new RubyStructureAnalyzer();

        List<? extends StructureItem> structures = analyzer.scan(parserResult);
        assertEquals("two methods", 3, structures.size());
        StructureItem twoParams = structures.get(0);
        StructureItem oneParamA = structures.get(1);
        StructureItem oneParamB = structures.get(2);
        assertFalse("not equals", twoParams.equals(oneParamA));
        assertFalse("not same hashCode (first: " + twoParams.hashCode() + ", second: " + oneParamA.hashCode() + ')',
                twoParams.hashCode() == oneParamA.hashCode());
        assertFalse("not equals", oneParamA.equals(oneParamB));
        assertEquals("same hashCode - we consider just arity", oneParamA.hashCode(), oneParamB.hashCode());
    }

    public void testRubyStructureItemEqualsAndHashCodeForOptionalParams() throws Exception { // #131134
        ParserResult parserResult = getParserResult("testfiles/testRubyStructureItemEqualsAndHashCodeForOptionalParams.rb");
        RubyStructureAnalyzer analyzer = new RubyStructureAnalyzer();

        List<? extends StructureItem> structures = analyzer.scan(parserResult);
        assertEquals("two methods", 2, structures.size());
        StructureItem first = structures.get(0);
        StructureItem second = structures.get(1);
        assertFalse("not equals", first.equals(second));
        assertFalse("not same hashCode (first: " + first.hashCode() + ", second: " + second.hashCode() + ')',
                first.hashCode() == second.hashCode());
    }

    public void testRubyStructureItemNotEqualsStaticVsInstance() throws Exception { // #115782
        ParserResult parserResult = getParserResult("testfiles/testRubyStructureItemNotEqualsStaticVsInstance.rb");
        RubyStructureAnalyzer analyzer = new RubyStructureAnalyzer();

        List<? extends StructureItem> structures = analyzer.scan(parserResult);
        assertEquals("one class", 1, structures.size());
        StructureItem clazz = structures.get(0);
        assertEquals("Foo class", ElementKind.CLASS, clazz.getKind());
        List<? extends StructureItem> clazzChildrens = clazz.getNestedItems();
        assertEquals("two methods", 2, clazzChildrens.size());
        StructureItem first = clazzChildrens.get(0);
        StructureItem second = clazzChildrens.get(1);
        assertFalse("not equals", second.equals(first));
        assertEquals("same hashCode", first.hashCode(), second.hashCode());
    }

    public void testTestStructure0() throws Exception {
        checkStructure("testfiles/new_test.rb");
    }

    public void testTestStructure1() throws Exception {
        checkStructure("testfiles/test1_spec.rb");
    }

    public void testTestStructure2() throws Exception {
        checkStructure("testfiles/test2_spec.rb");
    }

    public void testTestStructure3() throws Exception {
        checkStructure("testfiles/test3_spec.rb");
    }

    public void testTestStructure4() throws Exception {
        checkStructure("testfiles/test4_spec.rb");
    }

    public void testTestStructure4b() throws Exception {
        checkFolds("testfiles/test4_spec.rb");
    }

    public void testTestStructure5() throws Exception {
        checkStructure("testfiles/test5_spec.rb");
    }

    public void testTestStructure6() throws Exception {
        checkStructure("testfiles/test6_spec.rb");
    }

    public void testTestStructure7() throws Exception {
        checkStructure("testfiles/test7_spec.rb");
    }

    public void testTestStructure8() throws Exception {
        checkStructure("testfiles/test8_spec.rb");
    }

    public void testTestStructure9() throws Exception {
        checkStructure("testfiles/bowling_spec.rb");
    }

    public void testTestStructure9b() throws Exception {
        checkFolds("testfiles/bowling_spec.rb");
    }

    public void testTestStructure10() throws Exception {
        checkFolds("testfiles/japanese_spec.rb");
    }

    public void testEmpty1() throws Exception {
        checkStructure("testfiles/empty.rb");
    }

    public void testEmpty2() throws Exception {
        checkFolds("testfiles/empty.rb");
    }

    public void testLocals() throws Exception {
        checkStructure("testfiles/locals.rb");
    }

    public void testGlobals() throws Exception {
        checkStructure("testfiles/globals.rb");
    }

    public void testConstants() throws Exception {
        checkStructure("testfiles/constants.rb");
    }

    public void testMethodTypeInference() throws Exception {
        checkStructure("testfiles/method_type_inference.rb");
    }

}

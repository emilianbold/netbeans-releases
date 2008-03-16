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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.netbeans.modules.gsf.api.CompilationInfo;
import java.util.ArrayList;
import java.util.Comparator;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.ruby.elements.AstAttributeElement;
import org.netbeans.modules.ruby.elements.AstClassElement;

/**
 *
 * @author Tor Norbye
 */
public class StructureAnalyzerTest extends RubyTestBase {
    
    public StructureAnalyzerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private void annotate(int indent, StringBuilder sb, Document document, List<? extends StructureItem> structure) {
        for (StructureItem element : structure) {
            for (int i = 0; i < indent; i++) {
                sb.append("  ");
            }
            sb.append(element.getName());
            sb.append(":");
            sb.append(element.getKind());
            sb.append(":");
            sb.append(element.getModifiers());
            sb.append(":");
            sb.append(element.getHtml());
            sb.append(":");
            sb.append("\n");
            List<? extends StructureItem> children = element.getNestedItems();
            if (children != null && children.size() > 0) {
                List<? extends StructureItem> c = new ArrayList<StructureItem>(children);
                // Sort children to make tests more stable
                Collections.sort(c, new Comparator<StructureItem>() {
                    public int compare(StructureItem s1, StructureItem s2) {
                        return s1.getName().compareTo(s2.getName());
                    }
                    
                });
                
                annotate(indent+1, sb, document, c);
            }
        }
    }

    private String annotate(Document document, List<? extends StructureItem> structure) {
        StringBuilder sb = new StringBuilder();
        annotate(0, sb, document, structure);
        
        return sb.toString();
    }
    
    
    private void checkStructure(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        StructureAnalyzer analyzer = new StructureAnalyzer();
        HtmlFormatter formatter = new HtmlFormatter() {
            private StringBuilder sb = new StringBuilder();
            
            @Override
            public void reset() {
                sb.setLength(0);
            }

            @Override
            public void appendHtml(String html) {
                sb.append(html);
            }

            @Override
            public void appendText(String text) {
                // TODO escaped
                sb.append("ESCAPED{");
                sb.append(text);
                sb.append("}");
            }

            @Override
            public void name(ElementKind kind, boolean start) {
                if (start) {
                    sb.append(kind);
                }
            }

            @Override
            public void active(boolean start) {
                if (start) {
                    sb.append("ACTIVE{");
                } else {
                    sb.append("}");
                }
            }
            
            @Override
            public void parameters(boolean start) {
                if (start) {
                    sb.append("PARAMETERS{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public void type(boolean start) {
                if (start) {
                    sb.append("TYPE{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public void deprecated(boolean start) {
                if (start) {
                    sb.append("DEPRECATED{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public String getText() {
                return sb.toString();
            }

            @Override
            public void emphasis(boolean start) {
            }
        };
        List<? extends StructureItem> structure = analyzer.scan(info, formatter);
        
        String annotatedSource = annotate(info.getDocument(), structure);

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".structure");
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
    
    private void checkAttributes(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        RubyParseResult rpr = AstUtilities.getParseResult(info);
        StructureAnalyzer.AnalysisResult ar = rpr.getStructure();
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

    public void testAttributes1() throws Exception {
        checkAttributes("testfiles/resolv.rb");
    }
    
    
    private void checkFolds(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        StructureAnalyzer analyzer = new StructureAnalyzer();
        Map<String,List<OffsetRange>> foldsMap = analyzer.folds(info);
        
        // Write folding structure
        String source = info.getText();
        List<Integer> begins = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();
        
        begins.add(0);
        
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '\n') {
                ends.add(i);
                if (i < source.length()) {
                    begins.add(i+1);
                }
            }
        }
        
        ends.add(source.length());

        assertEquals(begins.size(), ends.size());
        List<Character> margin = new ArrayList<Character>(begins.size());
        for (int i = 0; i < begins.size(); i++) {
            margin.add(' ');
        }

        List<String> typeList = new ArrayList<String>(foldsMap.keySet());
        Collections.sort(typeList);
        for (String type : typeList) {
            List<OffsetRange> ranges = foldsMap.get(type);
            for (OffsetRange range : ranges) {
                int beginIndex = Collections.binarySearch(begins, range.getStart());
                if (beginIndex < 0) {
                    beginIndex = -(beginIndex+2);
                }
                int endIndex = Collections.binarySearch(ends, range.getEnd());
                if (endIndex < 0) {
                    endIndex = -(endIndex+2);
                }
                for (int i = beginIndex; i <= endIndex; i++) {
                    char c = margin.get(i);
                    if (i == beginIndex) {
                        c = '+';
                    } else if (c != '+') {
                        if (i == endIndex) {
                            c = '-';
                        } else {
                            c = '|';
                        }
                    }
                    margin.set(i, c);
                }
            }
        }
        
        StringBuilder sb = new StringBuilder(3000);
        for (int i = 0; i < begins.size(); i++) {
            sb.append(margin.get(i));
            sb.append(' ');
            for (int j = begins.get(i), max = ends.get(i); j < max; j++) {
                sb.append(source.charAt(j));
            }
            sb.append('\n');
        }
        String annotatedSource = sb.toString();
        
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".folds");
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

}

/*
 * StructureAnalyzerTest.java
 * JUnit based test
 *
 * Created on July 14, 2007, 4:40 PM
 */

package org.netbeans.modules.ruby;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.gsf.CompilationInfo;
import java.util.ArrayList;
import java.util.Comparator;
import javax.swing.text.Document;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.HtmlFormatter;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.StructureItem;
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
            
            public void reset() {
                sb.setLength(0);
            }

            public void appendHtml(String html) {
                sb.append(html);
            }

            public void appendText(String text) {
                // TODO escaped
                sb.append("ESCAPED{");
                sb.append(text);
                sb.append("}");
            }

            public void name(ElementKind kind, boolean start) {
                if (start) {
                    sb.append(kind);
                }
            }

            public void parameters(boolean start) {
                if (start) {
                    sb.append("PARAMETERS{");
                } else {
                    sb.append("}");
                }
            }

            public void type(boolean start) {
                if (start) {
                    sb.append("TYPE{");
                } else {
                    sb.append("}");
                }
            }

            public void deprecated(boolean start) {
                if (start) {
                    sb.append("DEPRECATED{");
                } else {
                    sb.append("}");
                }
            }

            public String getText() {
                return sb.toString();
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

    private void checkAttributes(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        RubyParseResult rbr = (RubyParseResult)info.getParserResult();
        StructureAnalyzer.AnalysisResult ar = rbr.getStructure();
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

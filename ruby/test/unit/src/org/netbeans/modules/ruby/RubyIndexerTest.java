/*
 * RubyIndexerTest.java
 * JUnit based test
 *
 * Created on July 14, 2007, 4:40 PM
 */

package org.netbeans.modules.ruby;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.gsf.CompilationInfo;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.junit.NbTestCase;

/**
 * @author Tor Norbye
 */
public class RubyIndexerTest extends RubyTestBase {
    
    public RubyIndexerTest(String testName) {
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
    
    private class TestIndex extends Index {
        private int documentNumber;
        private final String localUrl;
        
        public TestIndex(String localUrl) {
            // Leave the end
            int index = localUrl.lastIndexOf('/');
            if (index != -1) {
                localUrl = localUrl.substring(0, index);
            }
            this.localUrl = localUrl;
        }
        
        private final StringBuilder sb = new StringBuilder();
        @Override
        public String toString() {
            return sb.toString().replace(localUrl, "<TESTURL>");
        }
        
        private String sortCommaList(String s) {
            String[] items = s.split(",");
            Arrays.sort(items);
            StringBuilder sb = new StringBuilder();
            for (String item : items) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(item);
            }
            
            return sb.toString();
        }

        public void gsfStore(Set<Map<String, String>> fieldToData, Set<Map<String, String>> noIndexData, Map<String, String> toDelete) throws IOException {
            sb.append("\n\nDocument ");
            sb.append(Integer.toString(documentNumber++));
            sb.append("\n");
            
            sb.append("Delete:");
            List<String> keys = new ArrayList<String>(toDelete.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                sb.append("  ");
                sb.append(key);
                sb.append(" : ");
                sb.append(toDelete.get(key));
                sb.append("\n");
            }
            sb.append("\n");

            sb.append("Indexed:");
            sb.append("\n");
            List<String> strings = new ArrayList<String>();
            for (Map<String,String> map : fieldToData) {
                for (String key : map.keySet()) {
                    strings.add(key + " : " + map.get(key));
                }
            }
            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }

            sb.append("\n");
            sb.append("Not Indexed:");
            sb.append("\n");
            strings = new ArrayList<String>();
            for (Map<String,String> map : noIndexData) {
                for (String key : map.keySet()) {
                    String value = map.get(key);
                    if (value.indexOf(',') != -1) {
                        value = sortCommaList(value);
                    }
                    strings.add(key + " : " + value);
                }
            }
            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }
        }

        public void gsfSearch(String primaryField, String name, NameKind kind, Set<SearchScope> scope, Set<SearchResult> result) throws IOException {
            throw new UnsupportedOperationException("Not supported in this test.");
        }
        
    }
    
    private void checkIndexer(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        RubyParseResult rpr = (RubyParseResult) info.getParserResult();

        File rubyFile = new File(getDataDir(), relFilePath);
        Index index = new TestIndex(rubyFile.toURI().toURL().toExternalForm());
        RubyIndexer indexer = new RubyIndexer();
        RubyIndex.setClusterUrl("bogus"); // No translation
        indexer.updateIndex(index, rpr);
        
        String annotatedSource = index.toString();

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".indexed");
    }
    
    public void testAnalysis2() throws Exception {
        checkIndexer("testfiles/ape.rb");
    }
    
    public void testAnalysis() throws Exception {
        checkIndexer("testfiles/postgresql_adapter.rb");
    }

    public void testAnalysis3() throws Exception {
        checkIndexer("testfiles/date.rb");
    }

    public void testAnalysis4() throws Exception {
        checkIndexer("testfiles/resolv.rb");
    }

    public void testUnused() throws Exception {
        checkIndexer("testfiles/unused.rb");
    }

    public void testRails1() throws Exception {
        checkIndexer("testfiles/action_controller.rb");
    }

    public void testRails2() throws Exception {
        checkIndexer("testfiles/action_view.rb");
    }
    
    public void testRails3() throws Exception {
        checkIndexer("testfiles/action_mailer.rb");
    }
    
    public void testRails4() throws Exception {
        checkIndexer("testfiles/action_web_service.rb");
    }
    
    public void testRails5() throws Exception {
        checkIndexer("testfiles/active_record.rb");
    }

    public void testTopLevel() throws Exception {
        checkIndexer("testfiles/top_level.rb");
    }
    
    public void testTopLevel2() throws Exception {
        checkIndexer("testfiles/option_parser_spec.rb");
    }

    public void testTopLevel3() throws Exception {
        checkIndexer("testfiles/method_definer_test.rb");
    }
}

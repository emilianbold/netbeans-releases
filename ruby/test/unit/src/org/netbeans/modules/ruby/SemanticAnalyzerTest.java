package org.netbeans.modules.ruby;

import org.netbeans.modules.gsf.api.SemanticAnalyzer;

/**
 * Test the semantic analyzer / highlighter
 * 
 * @author Tor Norbye
 */
public class SemanticAnalyzerTest extends RubyTestBase {

    public SemanticAnalyzerTest(String testName) {
        super(testName);
    }

    @Override
    protected SemanticAnalyzer getSemanticAnalyzer() {
        return new SemanticAnalysis();
    }

    public void testAnalysis() throws Exception {
        checkSemantic("testfiles/postgresql_adapter.rb");
    }

    public void testAnalysis2() throws Exception {
        checkSemantic("testfiles/ape.rb");
    }

    public void testAnalysis3() throws Exception {
        checkSemantic("testfiles/date.rb");
    }

    public void testAnalysis4() throws Exception {
        checkSemantic("testfiles/resolv.rb");
    }

    public void testUnused() throws Exception {
        checkSemantic("testfiles/unused.rb");
    }
    
    public void testUnused2() throws Exception {
        checkSemantic("testfiles/unused2.rb");
    }
}

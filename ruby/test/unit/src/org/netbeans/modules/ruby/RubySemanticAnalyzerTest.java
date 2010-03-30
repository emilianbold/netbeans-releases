package org.netbeans.modules.ruby;

import java.util.concurrent.Callable;

/**
 * Test the semantic analyzer / highlighter
 * 
 * @author Tor Norbye
 */
public class RubySemanticAnalyzerTest extends RubyTestBase {

    public RubySemanticAnalyzerTest(String testName) {
        super(testName);
    }

    public void testAnalysis() throws Exception {
        failsDueToIssue182494(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                checkSemantic("testfiles/postgresql_adapter.rb");
                return null;
            }
        });
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
    
    public void testEmpty1() throws Exception {
        checkSemantic("testfiles/empty.rb");
    }

    public void testJavaPrefixes() throws Exception {
        checkSemantic("testfiles/java.rb");
    }
}

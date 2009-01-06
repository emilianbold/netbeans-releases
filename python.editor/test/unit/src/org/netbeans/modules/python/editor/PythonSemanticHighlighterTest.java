/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

/**
 *
 * @author Tor Norbye
 */
public class PythonSemanticHighlighterTest extends PythonTestBase {

    public PythonSemanticHighlighterTest(String testName) {
        super(testName);
    }

    public void testSemantic1() throws Exception {
        checkSemantic("testfiles/empty.py");
    }

    public void testSemantic2() throws Exception {
        checkSemantic("testfiles/ConfigParser.py");
    }
    
    public void testSemantic3() throws Exception {
        checkSemantic("testfiles/datetime.py");
    }

    public void testSemantic4() throws Exception {
        checkSemantic("testfiles/getopt.py");
    }
    
    public void testSemantic5() throws Exception {
        checkSemantic("testfiles/test_scope.py");
    }

    public void testDecorators() throws Exception {
        checkSemantic("testfiles/staticmethods.py");
    }

    public void testDecorators2() throws Exception {
        checkSemantic("testfiles/decorators.py");
    }
}

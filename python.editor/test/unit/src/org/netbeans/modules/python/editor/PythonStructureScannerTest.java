/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

/**
 *
 * @author Tor Norbye
 */
public class PythonStructureScannerTest extends PythonTestBase {

    public PythonStructureScannerTest(String testName) {
        super(testName);
    }

    public void testStructure1() throws Exception {
        checkStructure("testfiles/empty.py");
    }

    public void testStructure2() throws Exception {
        checkStructure("testfiles/ConfigParser.py");
    }

    public void testStructure3() throws Exception {
        checkStructure("testfiles/datetime.py");
    }

    public void testStructure4() throws Exception {
        checkStructure("testfiles/getopt.py");
    }
    
    public void testStructure5() throws Exception {
        checkStructure("testfiles/test_scope.py");
    }
    
    public void testFolds1() throws Exception {
        checkFolds("testfiles/empty.py");
    }

    public void testFolds2() throws Exception {
        checkFolds("testfiles/ConfigParser.py");
    }

    public void testFolds3() throws Exception {
        checkFolds("testfiles/datetime.py");
    }

    public void testFolds4() throws Exception {
        checkFolds("testfiles/getopt.py");
    }
    
    public void testFolds5() throws Exception {
        checkFolds("testfiles/test_scope.py");
    }
    
    public void testModifiers() throws Exception {
        checkStructure("testfiles/modifiers.py");
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.editor.hints;

import org.netbeans.modules.python.editor.PythonTestBase;

/**
 *
 * @author Tor Norbye
 */
public class NameRuleTest extends PythonTestBase {
    public NameRuleTest(String testName) {
        super(testName);
    }

    private PythonAstRule createRule() {
        return new NameRule();
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }

    public void testHint1() throws Exception {
        findHints(this, createRule(), "testfiles/ConfigParser.py", null, null);
    }

    public void testHint2() throws Exception {
        findHints(this, createRule(), "testfiles/names.py", null, null);
    }

    public void testHint3() throws Exception {
        findHints(this, createRule(), "testfiles/names2.py", null, null);
    }

    public void testHint4() throws Exception {
        findHints(this, createRule(), "testfiles/getopt.py", null, null);
    }

    public void testHint5() throws Exception {
        findHints(this, createRule(), "testfiles/datetime.py", null, null);
    }

    public void testHint6() throws Exception {
        findHints(this, createRule(), "testfiles/test_scope.py", null, null);
    }

    public void testFix1() throws Exception {
        applyHint(this, createRule(), "testfiles/names2.py",
                "^def noargs()",
                "def noargs()^",
                "Insert a new first param");
    }

    public void testFix2() throws Exception {
        applyHint(this, createRule(), "testfiles/names2.py",
                "^def bad2(filename)",
                "bad2(filename)^",
                "Rename");
    }
}

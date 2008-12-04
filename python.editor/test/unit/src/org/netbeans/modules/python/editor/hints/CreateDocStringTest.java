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
public class CreateDocStringTest extends PythonTestBase {

    public CreateDocStringTest(String testName) {
        super(testName);
    }

    private PythonAstRule createRule() {
        return new CreateDocString();
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }
    
    public void testHint1() throws Exception {
        findHints(this, createRule(), "testfiles/create_docstring.py", null, "def set^up");
    }

    public void testHint2() throws Exception {
        // There should be no matches here!
        findHints(this, createRule(), "testfiles/create_docstring.py", null, "a^lready");
    }

    public void testHint3() throws Exception {
        findHints(this, createRule(), "testfiles/create_docstring.py", null, "Datagram^RequestHandler");
    }

    public void testFix1() throws Exception {
        applyHint(this, createRule(), "testfiles/create_docstring.py", "def set^up", "one");
    }

    public void testFix2() throws Exception {
        applyHint(this, createRule(), "testfiles/create_docstring.py", "def set^up", "multi");
    }

    public void testFix3() throws Exception {
        applyHint(this, createRule(), "testfiles/create_docstring.py", "Datagram^RequestHandler", "one");
    }

    public void testFix4() throws Exception {
        applyHint(this, createRule(), "testfiles/create_docstring.py", "Datagram^RequestHandler", "multi");
    }

    public void testFix5() throws Exception {
        applyHint(this, createRule(), "testfiles/create_docstring2.py", "def fa^", "one");
    }

    public void testFix6() throws Exception {
        applyHint(this, createRule(), "testfiles/create_docstring3.py", "def fa^", "one");
    }

    public void testFix7() throws Exception {
        applyHint(this, createRule(), "testfiles/create_docstring4.py", "def sec^", "one");
    }
}

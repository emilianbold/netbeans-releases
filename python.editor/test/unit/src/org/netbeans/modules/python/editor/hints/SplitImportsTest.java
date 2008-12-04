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
public class SplitImportsTest extends PythonTestBase {

    public SplitImportsTest(String testName) {
        super(testName);
    }

    private PythonAstRule createRule() {
        return new SplitImports();
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }

    public void testHint1() throws Exception {
        findHints(this, createRule(), "testfiles/split_imports.py", null, null);
    }

    public void testFix1() throws Exception {
        applyHint(this, createRule(), "testfiles/split_imports.py", "^import sys, os, foobar", "Split");
    }

    public void testFix2() throws Exception {
        applyHint(this, createRule(), "testfiles/split_imports.py", "^import sys as whatever, os as bar", "Split");
    }
}

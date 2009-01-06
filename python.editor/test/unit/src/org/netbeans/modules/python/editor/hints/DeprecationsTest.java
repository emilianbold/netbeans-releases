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
public class DeprecationsTest extends PythonTestBase {

    public DeprecationsTest(String testName) {
        super(testName);
    }

    private PythonAstRule createRule() {
        return new Deprecations();
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }

    public void testNoHints() throws Exception {
        findHints(this, createRule(), "testfiles/test_scope.py", null, null);
    }

    public void testDeprecations() throws Exception {
        findHints(this, createRule(), "testfiles/deprecated-imports.py", null, null);
    }
}

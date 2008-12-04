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
public class RelativeImportsTest extends PythonTestBase {

    public RelativeImportsTest(String testName) {
        super(testName);
    }

    private PythonAstRule createRule() {
        return new RelativeImports();
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }

    public void testNoHints() throws Exception {
        findHints(this, createRule(), "testfiles/pickle.py", null, null);
    }

    public void testHint1() throws Exception {
        findHints(this, createRule(), "testfiles/toppkg/medpkg/lowpkg/imports.py", null, null);
    }

    public void testHint2() throws Exception {
        findHints(this, createRule(), "testfiles/package/subpackage1/moduleX.py", null, null);
    }

    public void testFix1() throws Exception {
        applyHint(this, createRule(), "testfiles/toppkg/medpkg/lowpkg/imports.py", "from .m^oduleY import spam", "Replace");
    }

    public void testFix2() throws Exception {
        applyHint(this, createRule(), "testfiles/toppkg/medpkg/lowpkg/imports.py", "fr^om ..subpackage1 import moduleY", "Replace");
    }

    public void testFix3() throws Exception {
        applyHint(this, createRule(), "testfiles/toppkg/medpkg/lowpkg/imports.py", "fr^om ...package import bar", "Replace");
    }

    public void testFix4() throws Exception {
        applyHint(this, createRule(), "testfiles/package/subpackage1/moduleX.py", "f^rom . import moduleY", "Replace");
    }

    public void testFix5() throws Exception {
        applyHint(this, createRule(), "testfiles/package/subpackage1/moduleX.py", "from .m^oduleY import spam", "Replace");
    }
}

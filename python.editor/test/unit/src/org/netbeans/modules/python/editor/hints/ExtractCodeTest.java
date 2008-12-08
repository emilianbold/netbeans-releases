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
public class ExtractCodeTest extends PythonTestBase {

    public ExtractCodeTest(String testName) {
        super(testName);
    }

    private PythonSelectionRule createRule() {
        return new ExtractCode();
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }

    public void testHint1() throws Exception {
        checkHints(createRule(), "testfiles/extract1.py",
                "^# Beginning of extraction segment",
                "# End of extraction segment^");
    }

    public void testFix1() throws Exception {
        applyHint(this, createRule(), "testfiles/extract1.py",
                "^# Beginning of extraction segment",
                "# End of extraction segment^",
                "Extract Method", true);
    }

    public void testFix1b() throws Exception {
        applyHint(this, createRule(), "testfiles/extract1.py",
                "^        # Beginning of extraction segment",
                "# End of extraction segment^",
                "Extract Method", true);
    }

    public void testFix2() throws Exception {
        applyHint(this, createRule(), "testfiles/extract2.py",
                "^simple_code = 1",
                "simple_code = simple_code+1^",
                "Extract Method", true);
    }

    public void testFix3() throws Exception {
        applyHint(this, createRule(), "testfiles/extract2.py",
                "^not_used = 1",
                "print simple_code + not_used^",
                "Extract Method", true);
    }

    public void testFix4() throws Exception {
        applyHint(this, createRule(), "testfiles/datetime.py",
                "^assert 1 <= month <= 12, month",
                "assert 1 <= month <= 12, month^",
                "Extract Method", true);
    }

    public void testFix5() throws Exception {
        // 150932: Quickfix extract method does not change all instances of the chosen name
        applyHint(this, createRule(), "testfiles/extract4.py",
                "^if _a >a:",
                "c = b^",
                "Extract Method", true);
    }

    public void testFix6() throws Exception {
        applyHint(this, createRule(), "testfiles/extract5.py",
                "^if _a >a:",
                "c = b^",
                "Extract Method", true);
    }

    public void testFix7() throws Exception {
        applyHint(this, createRule(), "testfiles/extract1.py",
                "^not_used_in_block = 1",
                "read_after_block_only = 4\n^",
                "Extract Method", true);
    }

    //public void testFix4() throws Exception {
    //    applyHint(this, createRule(), "testfiles/extract2.py",
    //            "^preprocess(foo, source, output_file, macros, include_dirs, extra_preargs, extra_postargs);",
    //            "get_preprocess(self, source, output_file, macros, include_dirs, extra_preargs, extra_postargs)^",
    //            "Extract Method");
    //}

    public void testFix8() throws Exception {
        applyHint(this, createRule(), "testfiles/ConfigParser.py",
                "^try:",
                "raise NoSectionError(section)^",
                "Extract Method", true);
    }
}

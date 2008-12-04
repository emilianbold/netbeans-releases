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
public class SurroundWithTest extends PythonTestBase {

    public SurroundWithTest(String testName) {
        super(testName);
    }

    private PythonSelectionRule createRule() {
        return new SurroundWith();
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }

    public void testHint1() throws Exception {
        checkHints(createRule(), "testfiles/datetime.py",
                "^for dim in _DAYS_IN_MONTH[1:]:",
                "del dbm, dim^");
    }

    public void testHint2() throws Exception {
        checkHints(createRule(), "testfiles/datetime.py",
                "^\"year -> number of days in year (366 if a leap year, else 365).\"",
                "\"year -> number of days in year (366 if a leap year, else 365).\"^");
    }

    public void testHint3() throws Exception {
        checkHints(createRule(), "testfiles/datetime.py",
                "^# Now compute how many 4-year cycles precede it.",
                "^    # And now how many single years.");
    }

    public void testHint4() throws Exception {
        checkHints(createRule(), "testfiles/simple.py",
                "^x = 1",
                "y = 2^");
    }

    public void testNoHint1() throws Exception {
        checkHints(createRule(), "testfiles/datetime.py",
                "f^or dim in _DAYS_IN_MONTH[1:]:",
                "del dbm, dim^");
    }

    public void testNoHint2() throws Exception {
        checkHints(createRule(), "testfiles/datetime.py",
                "^for dim in _DAYS_IN_MONTH[1:]:",
                "del dbm, di^m");
    }

    public void testNoHint3() throws Exception {
        checkHints(createRule(), "testfiles/datetime.py",
                "^def _is_leap(year):",
                " return year % 4 == 0 and (year % 100 != 0 or year % 400 == 0)^");
    }

    public void testNoHint4() throws Exception {
        checkHints(createRule(), "testfiles/datetime.py",
                "def ^_days_before_year(year):",
                "def _days_before_year^(year):");
    }

    public void testFix1() throws Exception {
        applyHint(this, createRule(), "testfiles/datetime.py",
                "^assert 1 <= month <= 12, month",
                "return 29^",
                "Surround With Try/Except\n");
    }

    public void testFix2() throws Exception {
        applyHint(this, createRule(), "testfiles/datetime.py",
                "^assert 1 <= month <= 12, month",
                "return 29^",
                "Surround With Try/Except/Finally");
    }

    public void testFix3() throws Exception {
        applyHint(this, createRule(), "testfiles/datetime.py",
                "^assert 1 <= month <= 12, month",
                "return 29^",
                "Surround With Try/Finally");
    }

    public void testFix4() throws Exception {
        applyHint(this, createRule(), "testfiles/surround.py",
                "^        print \"second\"",
                "^        print \"third\"",
                "Surround With Try/Finally");
    }

    public void testFix5() throws Exception {
        applyHint(this, createRule(), "testfiles/datetime.py",
                "^_DI400Y = _days_before_year(401)",
                "^_DI4Y   = _days_before_year(5)",
                "Surround With Try/Finally");
    }
}

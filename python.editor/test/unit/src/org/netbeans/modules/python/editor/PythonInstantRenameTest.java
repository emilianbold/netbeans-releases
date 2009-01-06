/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

/**
 *
 * @author Tor Norbye
 */
public class PythonInstantRenameTest extends PythonTestBase {

    public PythonInstantRenameTest(String testName) {
        super(testName);
    }

    public void testRename1() throws Exception {
        String caretLine = "def __init__(self, m^sg=''):";
        checkRenameSections("testfiles/ConfigParser.py", caretLine);
    }

    public void testRename2() throws Exception {
        String caretLine = "for (ke^y, value) in self._sections[section].items():";
        checkRenameSections("testfiles/ConfigParser.py", caretLine);
    }

    public void testRename3() throws Exception {
        String caretLine = "raise Interpola^tionSyntaxError(option, section,";
        checkRenameSections("testfiles/ConfigParser.py", caretLine);
    }

    public void testRename4() throws Exception {
        String caretLine = "print toplevelv^ar4";
        checkRenameSections("testfiles/occurrences2.py", caretLine);
    }

    public void testRename5() throws Exception {
        String caretLine = "x = myf^unc";
        checkRenameSections("testfiles/occurrences2.py", caretLine);
    }

    public void testRename6() throws Exception {
        String caretLine = "print toplevel^var2";
        checkRenameSections("testfiles/occurrences2.py", caretLine);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class PythonOccurrencesMarkerTest extends PythonTestBase {

    public PythonOccurrencesMarkerTest(String testName) {
        super(testName);
    }

    public void testMarks1() throws Exception {
        // TODO - these tests SHOULD pass symmetric but don't yet
        boolean symmetric = false;

        String caretLine = "def __init__(self, m^sg=''):";
        checkOccurrences("testfiles/ConfigParser.py", caretLine, symmetric);
    }

    public void testMarks2() throws Exception {
        // TODO - these tests SHOULD pass symmetric but don't yet
        boolean symmetric = false;

        String caretLine = "for (ke^y, value) in self._sections[section].items():";
        checkOccurrences("testfiles/ConfigParser.py", caretLine, symmetric);
    }

    public void test150581() throws Exception {
        FileObject testFile = getXTestPythonHomeFo().getFileObject("Lib/repr.py");
        assertNotNull(testFile);
        GsfTestCompilationInfo info = getInfo(testFile);
        PythonOccurrencesMarker marker = new PythonOccurrencesMarker();
        marker.setCaretPosition(786);
        marker.run(info);
        marker.getOccurrences();
        assertNull(PythonOccurrencesMarker.error);
    }

    // I think this test is wrong...
    //public void test150581b() throws Exception {
    //    // TODO - these tests SHOULD pass symmetric but don't yet
    //    boolean symmetric = false;
    //
    //    String caretLine = "__builtin__.re^pr(x)";
    //    checkOccurrences("testfiles/occurrences1.py", caretLine, symmetric);
    //}

    public void testMarks3() throws Exception {
        boolean symmetric = true;
        String caretLine = "new^format.append(zreplace)";
        checkOccurrences("testfiles/datetime.py", caretLine, symmetric);
    }

    public void testMarks4() throws Exception {
        boolean symmetric = true;
        String caretLine = "x = myf^unc";
        checkOccurrences("testfiles/occurrences2.py", caretLine, symmetric);
    }

    public void testMarks5() throws Exception {
        boolean symmetric = true;
        String caretLine = "import m^odule1";
        checkOccurrences("testfiles/occurrences2.py", caretLine, symmetric);
    }

    public void testMarks6() throws Exception {
        boolean symmetric = true;
        String caretLine = "import module3 as modu^le4";
        checkOccurrences("testfiles/occurrences2.py", caretLine, symmetric);
    }

    public void testMarks7() throws Exception {
        boolean symmetric = true;
        String caretLine = "toplevelv^ar2 =";
        checkOccurrences("testfiles/occurrences2.py", caretLine, symmetric);
    }

    public void testMarks8() throws Exception {
        boolean symmetric = true;
        String caretLine = "def mymethod(self,param1,pa^ram2):";
        checkOccurrences("testfiles/occurrences2.py", caretLine, symmetric);
    }

    public void testMarks9() throws Exception {
        boolean symmetric = true;
        String caretLine = "def _bu^ild_struct_time(";
        checkOccurrences("testfiles/datetime.py", caretLine, symmetric);
    }

    public void testMarks10() throws Exception {
        boolean symmetric = true;
        String caretLine = "sel^f.year, self.month, self.day = year, month, day";
        checkOccurrences("testfiles/datetime.py", caretLine, symmetric);
    }

    public void testMarks11() throws Exception {
        boolean symmetric = true;
        String caretLine = "# @type ^xy str";
        checkOccurrences("testfiles/typevars.py", caretLine, symmetric);
    }

    public void testStress() throws Exception {
        List<FileObject> files = findJythonFiles();

        //int MAX_FILES = Integer.MAX_VALUE;
        int MAX_FILES = 100;

        for (int i = 0; i < files.size() && i < MAX_FILES; i++) {
            FileObject fo = files.get(i);
            GsfTestCompilationInfo info = getInfo(fo);
            Document doc = info.getDocument();
            for (int offset = 0; offset < doc.getLength(); offset++) {
                PythonOccurrencesMarker marker = new PythonOccurrencesMarker();
                marker.setCaretPosition(offset);
                marker.run(info);
                marker.getOccurrences();
                assertNull("Handling " + FileUtil.getFileDisplayName(fo) + " at offset " + offset, PythonOccurrencesMarker.error);
            }
        }
    }
}

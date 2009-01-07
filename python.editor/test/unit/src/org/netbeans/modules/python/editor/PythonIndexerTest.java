/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

import org.netbeans.modules.python.editor.elements.IndexedElement;

/**
 *
 * @author Tor Norbye
 */
public class PythonIndexerTest extends PythonTestBase {
    
    public PythonIndexerTest(String testName) {
        super(testName);
    }
    
    @Override
    public String prettyPrintValue(String key, String value) {
        if (value == null) {
            return value;
        }
        int index = -1;
        if (PythonIndexer.FIELD_MEMBER.equals(key) ||PythonIndexer.FIELD_ITEM.equals(key)) {
            index = IndexedElement.FLAG_INDEX;
        } else if (PythonIndexer.FIELD_CLASS_ATTR_NAME.equals(key)) {
            index = 0;
            value = ";" + value + ";";
        }
        if (index != -1) {
            // Decode the attributes
            int attributeIndex = 0;
            for (int i = 0; i < index; i++) {
                attributeIndex = value.indexOf(';', attributeIndex+1);
            }
            int flags = IndexedElement.decode(value, attributeIndex+1, 0);
            String desc = IndexedElement.decodeFlags(flags);
            value = value.substring(0, attributeIndex) + ";" + desc + value.substring(value.indexOf(';', attributeIndex+1));
        }

        return value;
    }


    public void testIsIndexable1() throws Exception {
        checkIsIndexable("testfiles/javascript.js", false);
    }

    public void testIsIndexable2() throws Exception {
        checkIsIndexable("testfiles/compiled.pyc", false);
    }

    public void testIsIndexable3() throws Exception {
        checkIsIndexable("testfiles/ConfigParser.py", true);
    }

    public void testIsIndexable4() throws Exception {
        checkIsIndexable("testfiles/datetime.py", true);
    }

    public void testIsIndexable5() throws Exception {
        checkIsIndexable("testfiles/antlr_python_runtime-3.1.1-py2.5.egg", true);
    }

    public void testIndex1() throws Exception {
        checkIndexer("testfiles/ConfigParser.py");
    }

    public void testIndex2() throws Exception {
        checkIndexer("testfiles/datetime.py");
    }

    public void testIndex3() throws Exception {
        checkIndexer("testfiles/doc.py");
    }

    public void testIndex4() throws Exception {
        checkIndexer("testfiles/md5.py");
    }

    public void testIndex5() throws Exception {
        checkIndexer("testfiles/scope.py");
    }

    public void testIndex6() throws Exception {
        checkIndexer("testfiles/httplib.py");
    }

    public void testIndex7() throws Exception {
        checkIndexer("testfiles/minicompat.py");
    }

    public void testIndex8() throws Exception {
        checkIndexer("testfiles/socket.py");
    }

    public void testIndex9() throws Exception {
        checkIndexer("testfiles/jreload.py");
    }

    public void testIndex10() throws Exception {
        checkIndexer("testfiles/doctest.py");
    }

    public void testIndex11() throws Exception {
        checkIndexer("testfiles/zipfile.py");
    }

    public void testIndex12() throws Exception {
        checkIndexer("testfiles/os.py");
    }

    public void testIndex13() throws Exception {
        checkIndexer("testfiles/unittest.py");
    }

    public void testIndex14() throws Exception {
        checkIndexer("testfiles/properties.py");
    }

    public void testIndex15() throws Exception {
        checkIndexer("testfiles/tarfile.py");
    }

    public void testRstIndex1() throws Exception {
        checkIndexer("testfiles/rst/zipfile.rst");
    }

    public void testRstIndex2() throws Exception {
        checkIndexer("testfiles/rst/stdtypes.rst");
    }

    public void testRstIndex3() throws Exception {
        checkIndexer("testfiles/rst/platform.rst");
    }

    public void testRstIndex4() throws Exception {
        checkIndexer("testfiles/rst/smtpd.rst");
    }

    public void testRstIndex5() throws Exception {
        checkIndexer("testfiles/rst/exceptions.rst");
    }

    public void testRstIndex6() throws Exception {
        checkIndexer("testfiles/rst/logging.rst");
    }

    public void testRstIndex7() throws Exception {
        checkIndexer("testfiles/rst/string.rst");
    }

    public void testRstIndex8() throws Exception {
        checkIndexer("testfiles/rst/bz2.rst");
    }

    public void testRstIndex9() throws Exception {
        checkIndexer("testfiles/rst/constants.rst");
    }

    public void testIndexEgg() throws Exception {
        checkIndexer("testfiles/antlr_python_runtime-3.1.1-py2.5.egg");
    }
}

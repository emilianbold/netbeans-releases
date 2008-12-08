/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

import org.netbeans.modules.python.editor.lexer.PythonTokenId;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.openide.filesystems.FileObject;
import org.python.antlr.PythonTree;

/**
 * Test for the Python type analyzer.
 * 
 * @author Tor Norbye
 */
public class PythonTypeAnalyzerTest extends PythonTestBase {

    public PythonTypeAnalyzerTest(String testName) {
        super(testName);
    }

    private PythonTypeAnalyzer getAnalyzer(String file, String caretLine, boolean findMethod) throws Exception {
        FileObject fo = getTestFile(file);
        GsfTestCompilationInfo info = getInfo(fo);
        PythonTree root = PythonAstUtils.getRoot(info);
        initializeRegistry();
        PythonIndex index = PythonIndex.get(info.getIndex(PythonTokenId.PYTHON_MIME_TYPE));

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = info.getText().indexOf(caretLine);
            assertTrue(lineOffset != -1);
            caretOffset = lineOffset + caretDelta;
        }

        AstPath path = AstPath.get(root, caretOffset);
        PythonTree node = path.leaf();

        if (findMethod) {
            PythonTree method = PythonAstUtils.getLocalScope(path);
            assertNotNull(method);

            root = method;
        }

        PythonTypeAnalyzer instance = new PythonTypeAnalyzer(info, index, root, node, caretOffset, caretOffset, fo);

        return instance;
    }

    public void testGetType1() throws Exception {
        PythonTypeAnalyzer instance = getAnalyzer("testfiles/types.py", "#^FIRST_CARET_POS", true);

        assertEquals("SomeOtherClass", instance.getType("x"));
        assertEquals("SomeOtherClass", instance.getType("y"));
        assertEquals("SomeOtherClass", instance.getType("z"));
        assertEquals("String", instance.getType("yz"));
        assertEquals("Number", instance.getType("w"));
        assertEquals(null, instance.getType("unknown"));
    }

    public void testGetType2() throws Exception {
        PythonTypeAnalyzer instance = getAnalyzer("testfiles/types.py", "#^SECOND_CARET_POS", true);

        assertEquals("Other", instance.getType("x"));
        assertEquals("Number", instance.getType("y"));
        assertEquals("String", instance.getType("z"));
        assertEquals("String", instance.getType("yz"));
        assertEquals("Number", instance.getType("w"));
        assertEquals(null, instance.getType("unknown"));
    }

    public void testGetType3() throws Exception {
        PythonTypeAnalyzer instance = getAnalyzer("testfiles/compl2.py", "unknown^", true);

        assertEquals("ZipFile", instance.getType("myothervar"));
        assertEquals("file", instance.getType("myvar"));
        assertEquals(null, instance.getType("unknown"));
    }

}

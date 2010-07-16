/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

import java.util.List;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.Error;

/**
 *
 * @author Tor Norbye
 */
public class PythonParserTest extends PythonTestBase {

    public PythonParserTest(String testName) {
        super(testName);
    }

//    private void checkParseTree(String file, String caretLine, String nodeType) throws Exception {
//        PythonParser.runtimeException = null;
//        CompilationInfo info = getInfo(file);
//
//        String text = info.getText();
//
//        int caretOffset = -1;
//        if (caretLine != null) {
//            int caretDelta = caretLine.indexOf("^");
//            assertTrue(caretDelta != -1);
//            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
//            int lineOffset = text.indexOf(caretLine);
//            assertTrue(lineOffset != -1);
//
//            caretOffset = lineOffset + caretDelta;
//            ((GsfTestCompilationInfo)info).setCaretOffset(caretOffset);
//        }
//
//        PythonTree root = PythonAstUtils.getRoot(info);
//        assertNotNull("Parsing broken input failed for " + file, root);
//
//        // Ensure that we find the node we're looking for
//        if (nodeType != null) {
//            PythonParserResult rpr = PythonAstUtils.getParseResult(info);
//            OffsetRange range = rpr.getSanitizedRange();
//            if (range.containsInclusive(caretOffset)) {
//                caretOffset = range.getStart();
//            }
//            AstPath path = AstPath.get(root, caretOffset);
//            PythonTree closest = path.leaf();
//            assertNotNull(closest);
//            String leafName = closest.getClass().getName();
//            leafName = leafName.substring(leafName.lastIndexOf('.')+1);
//            assertEquals(nodeType, closest.toString());
//        }
//        assertNull(PythonParser.runtimeException);
//    }

    private void checkNoParseAbort(String file) throws Exception {
        checkNoParseAbort(file, null);
    }
    private void checkNoParseAbort(String file, String caretLine) throws Exception {
        PythonParser.runtimeException = null;
        CompilationInfo info = getInfo(file);
        PythonTree root = PythonAstUtils.getRoot(info);
        assertNull(PythonParser.runtimeException != null ? PythonParser.runtimeException.toString() : "OK", PythonParser.runtimeException);
        // Check that a walk works too
        if (root != null) {
            new Visitor() {
                @Override
                public void traverse(PythonTree node) throws Exception {
                    super.traverse(node);
                }

            }.visit(root);
        }
    }

    protected void checkErrors(String relFilePath, String caretLine) throws Exception {
        GsfTestCompilationInfo info = getInfo(relFilePath);

        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = info.getText().indexOf(caretLine);
            assertTrue("NOT FOUND: " + info.getFileObject().getName() + ":" + caretLine, lineOffset != -1);

            int caretOffset = lineOffset + caretDelta;
            info.setCaretOffset(caretOffset);
        }

        String text = info.getText();
        assertNotNull(text);

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);

        List<Error> diagnostics = pr.getDiagnostics();
        String annotatedSource = annotateErrors(text, diagnostics);
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".errors");
    }
    
    public void testPartial11() throws Exception {
        checkNoParseAbort("testfiles/errors1.py");
        checkErrors("testfiles/errors1.py");
    }

    public void testCastAbort1() throws Exception {
        checkNoParseAbort("testfiles/errors2.py");
        checkErrors("testfiles/errors2.py");
    }

    public void testInfiniteLoop1() throws Exception {
        checkNoParseAbort("testfiles/errors3.py");
        checkErrors("testfiles/errors3.py");
    }

    public void testInfiniteLoop2() throws Exception {
        checkNoParseAbort("testfiles/errors4.py");
        checkErrors("testfiles/errors4.py");
    }

    public void testPositions() throws Exception {
        checkNoParseAbort("testfiles/errors5.py");
    }

    // Not yet passing
    public void testAsKeyword() throws Exception {
        // See 150921
        checkNoParseAbort("testfiles/lib-old/Para.py");
    }

    public void testNpe() throws Exception {
        // See 150921
        checkNoParseAbort("testfiles/errors6.py");
    }

    public void testNpe2() throws Exception {
        // See 155904
        checkNoParseAbort("testfiles/errors7.py");
    }

    public void testCastException1() throws Exception {
        checkNoParseAbort("testfiles/errors8.py");
    }

    public void testCastException2() throws Exception {
        checkNoParseAbort("testfiles/errors9.py");
    }

    public void testPython26Syntax() throws Exception {
        checkNoParseAbort("testfiles/exceptas.py");
    }

    public void testMissingColons1() throws Exception {
        checkNoParseAbort("testfiles/errors10.py");
        checkErrors("testfiles/errors10.py");
    }

    public void testMissingColons2() throws Exception {
        checkNoParseAbort("testfiles/errors11.py");
        checkErrors("testfiles/errors11.py");
    }

    // https://netbeans.org/bugzilla/show_bug.cgi?id=178442
    public void testIncremental1() throws Exception {
        checkErrors("testfiles/errors12.py", "ABC,^DEF");
    }
    public void testIncremental2() throws Exception {
        checkErrors("testfiles/errors13.py", "newparam,^");
    }
}

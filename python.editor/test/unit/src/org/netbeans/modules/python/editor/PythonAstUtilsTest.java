/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.exprType;

/**
 *
 * @author Tor Norbye
 */
public class PythonAstUtilsTest extends PythonTestBase {

    public PythonAstUtilsTest(String testName) {
        super(testName);
    }

    public void testScope() throws Exception {
        String relFilePath = "testfiles/ConfigParser.py";
        String caretLine = "Error.__init__(se^lf, \"Section %r already exists\" % section)";

        CompilationInfo info = getInfo(relFilePath);
        PythonTree root = PythonAstUtils.getRoot(info);
        int lineOffset = caretLine.indexOf('^');
        assertTrue(lineOffset != -1);
        caretLine = caretLine.substring(0, lineOffset) + caretLine.substring(lineOffset+1);
        int offset = info.getText().indexOf(caretLine)+lineOffset;

        AstPath path = AstPath.get(root, offset);

//        assertEquals("ClassDef", PythonAstUtils.getClassDef(path).toString());
        assertEquals("ClassDef", PythonAstUtils.getClassScope(path).toString());
        assertEquals("FunctionDef", PythonAstUtils.getLocalScope(path).toString());
    }

    public void testGetParamters() throws Exception {
        String relFilePath = "testfiles/ConfigParser.py";
        String caretLine = "Error.__init__(se^lf, \"Section %r already exists\" % section)";

        CompilationInfo info = getInfo(relFilePath);
        PythonTree root = PythonAstUtils.getRoot(info);
        int lineOffset = caretLine.indexOf('^');
        assertTrue(lineOffset != -1);
        caretLine = caretLine.substring(0, lineOffset) + caretLine.substring(lineOffset+1);
        int offset = info.getText().indexOf(caretLine)+lineOffset;

        AstPath path = AstPath.get(root, offset);

        FunctionDef def = (FunctionDef)PythonAstUtils.getLocalScope(path);
        assertEquals("[self, section]", PythonAstUtils.getParameters(def).toString());
    }

    public void testStress() throws Exception {
        List<FileObject> files = findJythonFiles();

        int MAX_FILES = Integer.MAX_VALUE;

        for (int i = 0; i < files.size() && i < MAX_FILES; i++) {
            FileObject fo = files.get(i);
            GsfTestCompilationInfo info = getInfo(fo);
            PythonTree root = PythonAstUtils.getRoot(info);
            assertNotNull(FileUtil.getFileDisplayName(fo), root);
            List<PythonTree> nodes = getAllNodes(root);
            for (PythonTree node : nodes) {
                if (node instanceof Call) {
                    PythonAstUtils.getCallName((Call)node);
                    PythonAstUtils.isGetter((Call)node, false);
                }
                PythonAstUtils.getDocumentation(node);
                PythonAstUtils.getDocumentationNode(node);
                if (node instanceof exprType) {
                    PythonAstUtils.getExprName((exprType)node);
                }
                PythonAstUtils.getName(node);
                PythonAstUtils.isNameNode(node);
                PythonAstUtils.getRange(node);
                PythonAstUtils.getNameRange(info, node);
                if (node instanceof FunctionDef) {
                    PythonAstUtils.getParameters((FunctionDef)node);
                }

                //Document doc = info.getDocument();
                //for (int offset = 0; offset < doc.getLength(); offset++) {
                //    assertNull("Handling " + FileUtil.getFileDisplayName(fo) + " at offset " + offset, PythonOccurrencesMarker.error);
                //}

            }

            final List<PythonTree> defs = new ArrayList<PythonTree>();
            PythonAstUtils.addNodesByType(root, new Class[] { PythonTree.class }, defs);
            new NodeFinder(new AstPathChecker() {
                public void check(AstPath path) {
                    PythonAstUtils.getClassDef(path);
                    PythonAstUtils.getClassScope(path);
                    PythonAstUtils.getFuncDef(path);
                    PythonAstUtils.getLocalScope(path);
                    PythonAstUtils.getParentClassFromNode(path, null, "");
                    PythonAstUtils.getParentClassFromNode(path, path.leaf(), "");
                    for (PythonTree def : defs) {
                        PythonAstUtils.isClassMethod(path, (FunctionDef)def);
                    }
                }
            }).visit(root);
        }
    }

    interface AstPathChecker {
        void check(AstPath path);
    }

    private static class NodeFinder extends Visitor {
        private ArrayList<PythonTree> path = new ArrayList<PythonTree>();
        private AstPathChecker checker;

        private NodeFinder(AstPathChecker checker) {
            this.checker = checker;
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            path.add(node);

            checker.check(new AstPath(path));

            super.traverse(node);
            path.remove(path.size()-1);
        }
    }

//    public void testFindSpecialNodeTypes() throws Exception {
//        List<FileObject> files = findJythonFiles();
//
//        int MAX_FILES = Integer.MAX_VALUE;
//
//        for (int i = 0; i < files.size() && i < MAX_FILES; i++) {
//            final FileObject fo = files.get(i);
//            GsfTestCompilationInfo info = getInfo(fo);
//            PythonTree root = PythonAstUtils.getRoot(info);
//            new Visitor() {
//
//                @Override
//                public Object visitGlobal(Global node) throws Exception {
//                    String s = fo.getNameExt();
//                    fail(s);
//                    return super.visitGlobal(node);
//                }
//
//            }.visit(root);
//        }
//    }


}

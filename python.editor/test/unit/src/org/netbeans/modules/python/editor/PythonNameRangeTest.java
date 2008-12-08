/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;

/**
 * Test the name range
 *
 * @author Tor Norbye
 */
public class PythonNameRangeTest extends PythonTestBase {
    public PythonNameRangeTest(String testName) {
        super(testName);
    }

    @Override
    protected String describeNode(CompilationInfo info, Object obj, boolean includePath) throws Exception {
        return obj.toString();
    }

    @Override
    protected String getOffsetTestGoldenSuffix() {
        return ".nameoffsets";
    }

    @Override
    protected void initializeNodes(CompilationInfo info, ParserResult result, List<Object> validNodes,
            Map<Object,OffsetRange> positions, List<Object> invalidNodes) throws Exception {
        PythonTree root = PythonAstUtils.getRoot(info);
        assertNotNull(root);

        new TreeVisitor(validNodes, invalidNodes, positions, info).visit(root);
    }

    public void testOffsets1() throws Exception {
        checkOffsets("testfiles/empty.py");
    }

    public void testOffsets2() throws Exception {
        checkOffsets("testfiles/ConfigParser.py");
    }

    public void testOffsets3() throws Exception {
        checkOffsets("testfiles/datetime.py");
    }

    public void testOffsets4() throws Exception {
        checkOffsets("testfiles/getopt.py");
    }

    public void testOffsets5() throws Exception {
        checkOffsets("testfiles/test_scope.py");
    }

    private static class TreeVisitor extends Visitor {
        private List<Object> validNodes;
        private List<Object> invalidNodes;
        private Map<Object, OffsetRange> positions;
        private CompilationInfo info;

        TreeVisitor(List<Object> validNodes, List<Object> invalidNodes, Map<Object,
            OffsetRange> positions, CompilationInfo info) {
            this.validNodes = validNodes;
            this.invalidNodes = invalidNodes;
            this.positions = positions;
            this.info = info;
        }
        
        @Override
        public void traverse(PythonTree node) throws Exception {
            assertTrue(node.getCharStartIndex() <= node.getCharStopIndex());

            OffsetRange range = PythonAstUtils.getRange(node);
            OffsetRange nameRange = PythonAstUtils.getNameRange(info, node);
            if (!range.equals(nameRange) || PythonAstUtils.isNameNode(node)) {
                validNodes.add(node);
                positions.put(node, nameRange);
            }

            super.traverse(node);
        }

    }

}

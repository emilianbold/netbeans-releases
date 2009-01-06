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
 * Test offsets in the python parse trees
 *
 * @author Tor Norbye
 */
public class PythonAstOffsetsTest extends PythonTestBase {
    public PythonAstOffsetsTest(String testName) {
        super(testName);
    }

    @Override
    protected String describeNode(CompilationInfo info, Object obj, boolean includePath) throws Exception {
        return obj.toString();
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

    public void testAttributes() throws Exception {
        checkOffsets("testfiles/attribute.py");
    }

    public void testDecorators() throws Exception {
        checkOffsets("testfiles/staticmethods.py");
    }

    public void test149618() throws Exception {
        checkOffsets("testfiles/issue149618.py");
    }

    private static class TreeVisitor extends Visitor {
        private List<Object> validNodes;
        private List<Object> invalidNodes;
        private Map<Object, OffsetRange> positions;

        TreeVisitor(List<Object> validNodes, List<Object> invalidNodes, Map<Object,
            OffsetRange> positions, CompilationInfo info) {
            this.validNodes = validNodes;
            this.invalidNodes = invalidNodes;
            this.positions = positions;
        }
        
        @Override
        public void traverse(PythonTree node) throws Exception {
            assertTrue(node.getCharStartIndex() <= node.getCharStopIndex());

            OffsetRange range = new OffsetRange(node.getCharStartIndex(), node.getCharStopIndex());
            if (range.getStart() != 0 || range.getEnd() != 0) { // Don't include 0-0 PythonTrees, these are errors
                validNodes.add(node);
                positions.put(node, range);
            } else {
                invalidNodes.add(node);
            }

            super.traverse(node);
        }

    }

}

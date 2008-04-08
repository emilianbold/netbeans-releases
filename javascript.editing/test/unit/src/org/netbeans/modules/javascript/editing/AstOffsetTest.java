/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;

/**
 * Check offsets for the JavaScript AST
 * 
 * @author Tor Norbye
 */
public class AstOffsetTest extends JsTestBase {
    
    public AstOffsetTest(String testName) {
        super(testName);
    }            
    
    private String getNodePath(Node node, BaseDocument doc) throws Exception {
        String s = Token.fullName(node.getType());
        while (node != null) {
            int line = Utilities.getLineOffset(doc, node.getSourceStart());
            int offset = node.getSourceStart()-Utilities.getRowStart(doc, node.getSourceStart());
            String offsetDesc = line + ":" + offset;
            String n = Token.fullName(node.getType()) + "[" + offsetDesc + "]";
            if (s != null) {
                s = n + ":" + s;
            } else {
                s = n;
            }
            node = node.getParentNode();
        }
        
        return s;
    }
    
    private void setRanges(Node node, Map<Integer,List<Node>> starts, Map<Integer,List<Node>> ends, 
            Map<Node,OffsetRange> nodes, Map<Node,String> descriptions, BaseDocument doc) throws Exception {
        if (node.getSourceStart() > node.getSourceEnd()) {
            assertTrue(getNodePath(node, doc) + "; node=" + node.toString() + " at line " + org.netbeans.editor.Utilities.getLineOffset(doc, node.getSourceStart()), false);
        }
        OffsetRange range = new OffsetRange(node.getSourceStart(), node.getSourceEnd());
        nodes.put(node, range);
        String desc = Token.fullName(node.getType());
        descriptions.put(node, desc);
        if (range.getStart() != 0 || range.getEnd() != 0) { // Don't include 0-0 nodes, these are errors
            List<Node> list = starts.get(range.getStart());
            if (list == null) {
                list = new ArrayList<Node>();
                starts.put(range.getStart(), list);
            }
            list.add(node);
            list = ends.get(range.getEnd());
            if (list == null) {
                list = new ArrayList<Node>();
                ends.put(range.getEnd(), list);
            }
            list.add(node);
        }
        
        if (node.hasChildren()) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                assert child != null;
                setRanges(child, starts, ends, nodes, descriptions, doc);
            }
        }
    }
    
    private static int getDepth(Node n) {
        int depth = 0;
        while (n != null) {
            n = n.getParentNode();
            depth++;
        }
        
        return depth;
    }
    
    private static final Comparator<Node> FORWARDS_COMPARATOR = new Comparator<Node>() {
        public int compare(Node o1, Node o2) {
            return getDepth(o1) - getDepth(o2);
        }
    };

    private static final Comparator<Node> BACKWARDS_COMPARATOR = new Comparator<Node>() {
        public int compare(Node o1, Node o2) {
            return getDepth(o2) - getDepth(o1);
        }
    };
    
    private String annotate(Document doc, Node root) throws Exception {
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());

        Map<Node,OffsetRange> nodes = new HashMap<Node,OffsetRange>(100);
        Map<Integer,List<Node>> starts = new HashMap<Integer,List<Node>>(100);
        Map<Integer,List<Node>> ends = new HashMap<Integer,List<Node>>(100);
        Map<Node,String> descriptions = new HashMap<Node,String>(100);
        setRanges(root, starts, ends, nodes, descriptions, (BaseDocument)doc);

        // Sort nodes
        for (List<Node> list : starts.values()) {
            Collections.sort(list, FORWARDS_COMPARATOR);
        }
        for (List<Node> list : ends.values()) {
            Collections.sort(list, BACKWARDS_COMPARATOR);
        }
        
        // Include 0-0 nodes first
        List<String> missing = new ArrayList<String>();
        for (Map.Entry<Node,OffsetRange> entry : nodes.entrySet()) {
            OffsetRange range = entry.getValue();
            if (range.getStart() == 0 && range.getEnd() == 0) {
                Node val = entry.getKey();
                missing.add("Missing position for node " + getNodePath(val, (BaseDocument)doc));
            }
        }
        Collections.sort(missing);
        for (String s : missing) {
            sb.append(s);
            sb.append("\n");
        }
        sb.append("\n");
        
        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                List<Node> ns = starts.get(i);
                for (Node n : ns) {
                    sb.append("<");
                    String desc = descriptions.get(n);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            if (ends.containsKey(i)) {
                List<Node> ns = ends.get(i);
                for (Node n : ns) {
                    sb.append("</");
                    String desc = descriptions.get(n);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            char c = text.charAt(i);
            switch (c) {
            case '&': sb.append("&amp;"); break;
            case '<': sb.append("&lt;"); break;
            case '>': sb.append("&gt;"); break;
            default:
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private void checkOffsets(String relFilePath) throws Exception {
        checkOffsets(relFilePath, null);
    }
    
    private void checkOffsets(String relFilePath, String caretLine) throws Exception {
        JsParser.runtimeException = null;
        CompilationInfo info = getInfo(relFilePath);
        
        String text = info.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
            ((TestCompilationInfo)info).setCaretOffset(caretOffset);
        }

        assertNotNull(AstUtilities.getParseResult(info));
        assertNull(JsParser.runtimeException != null ? JsParser.runtimeException.toString() : "", JsParser.runtimeException);

        Node root = AstUtilities.getRoot(info);
        assertNotNull(root);

        String annotatedSource = annotate(info.getDocument(), root);
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".offsets");
    }

    public void testOffsets1() throws Exception {
        checkOffsets("testfiles/semantic1.js");
    }

    public void testOffsets2() throws Exception {
        checkOffsets("testfiles/semantic2.js");
    }

    public void testOffsets3() throws Exception {
        checkOffsets("testfiles/semantic3.js");
    }

    public void testOffsets4() throws Exception {
        checkOffsets("testfiles/semantic4.js");
    }

    public void testOffsets5() throws Exception {
        checkOffsets("testfiles/semantic5.js");
    }

    public void testOffsets6() throws Exception {
        checkOffsets("testfiles/semantic6.js");
    }

    public void testOffsets7() throws Exception {
        checkOffsets("testfiles/semantic7.js");
    }

    public void testOffsets8() throws Exception {
        checkOffsets("testfiles/semantic8.js", "new^");
    }

    public void testOffsetsE4x() throws Exception {
        checkOffsets("testfiles/e4x.js", "order^");
    }

    public void testOffsetsE4x2() throws Exception {
        checkOffsets("testfiles/e4x2.js", "order^");
    }

    public void testOffsetsTryCatch() throws Exception {
        checkOffsets("testfiles/tryblocks.js");
    }

    public void testOffsetsPrototype() throws Exception {
        checkOffsets("testfiles/prototype.js");
    }

    public void testOffsetsPrototypeNew() throws Exception {
        checkOffsets("testfiles/prototype-new.js");
    }

    public void testOffsetsSwitches() throws Exception {
        checkOffsets("testfiles/switches.js");
    }
}

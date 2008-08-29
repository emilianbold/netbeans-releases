/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.languages.yaml;

import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import org.jruby.util.ByteList;
import org.jvyamlb.Positionable;
import org.jvyamlb.nodes.Node;
import org.jvyamlb.nodes.PositionedScalarNode;
import org.jvyamlb.nodes.PositionedSequenceNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;

/**
 * Structure Scanner for YAML
 * 
 * @author Tor Norbye
 */
public class YamlScanner implements StructureScanner {

    public List<? extends StructureItem> scan(CompilationInfo info) {
        YamlParserResult result = (YamlParserResult) info.getEmbeddedResult(YamlTokenId.YAML_MIME_TYPE, 0);
        if (result != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }

    List<? extends StructureItem> scanStructure(YamlParserResult result) {
        Node node = result.getObject();
        if (node != null) {
            // Skip root node
            return new YamlStructureItem(node, null, 0).getNestedItems();
        }

        return Collections.emptyList();
    }

    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        YamlParserResult result = (YamlParserResult) info.getEmbeddedResult(YamlTokenId.YAML_MIME_TYPE, 0);
        if (result == null) {
            return Collections.emptyMap();
        }

        List<? extends StructureItem> items = result.getItems();
        if (items.size() == 0) {
            return Collections.emptyMap();
        }

        Map<String,List<OffsetRange>> folds = new HashMap<String,List<OffsetRange>>();
        List<OffsetRange> codeblocks = new ArrayList<OffsetRange>();
        folds.put("codeblocks", codeblocks); // NOI18N
        BaseDocument doc = (BaseDocument)info.getDocument();

        for (StructureItem item : items) {
            try {
                addBlocks(doc, codeblocks, item);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                break;
            }
        }

        return folds;
    }

    private void addBlocks(BaseDocument doc, List<OffsetRange> codeblocks, StructureItem item) throws BadLocationException {
        int begin = (int) item.getPosition();
        int end = (int) item.getEndPosition();
        int firstRowEnd = Utilities.getRowEnd(doc, begin);
        if (begin < end && firstRowEnd != Utilities.getRowEnd(doc, end)) {
            codeblocks.add(new OffsetRange(firstRowEnd, end));
        } else {
            return;
        }

        for (StructureItem child : item.getNestedItems()) {
            int childBegin = (int) child.getPosition();
            int childEnd = (int) child.getEndPosition();
            if (childBegin >= begin && childEnd <= end) {
                addBlocks(doc, codeblocks, child);
            }
        }
    }

    public Configuration getConfiguration() {
        return new Configuration(false, false, 0);
    }

    private class YamlStructureItem implements StructureItem, Comparable<YamlStructureItem> {

        private final String name;
        private List<YamlStructureItem> children;
        private final Node node;
        private final long begin;
        private final long end;
        private final int depth;

        YamlStructureItem(Node node, String name, int depth, long begin, long end) {
            this.node = node;
            this.name = name;
            this.begin = begin;
            this.end = end;
            this.depth = depth;
        }

        YamlStructureItem(Node node, String name, int depth) {
            this(node, name,  depth, ((Positionable) node).getRange().start.offset, ((Positionable) node).getRange().end.offset);
        }

        public String getName() {
            return name;
        }

        public String getSortText() {
            return getName();
        }

        public String getHtml(HtmlFormatter formatter) {
            String s = getName();
            try {
                return XMLUtil.toElementContent(s);
            } catch (CharConversionException cce) {
                Exceptions.printStackTrace(cce);
                return s;
            }
        }

        public ElementHandle getElementHandle() {
            return null;
        }

        public ElementKind getKind() {
            return ElementKind.ATTRIBUTE;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            return getNestedItems().size() == 0;
        }

        public List<? extends StructureItem> getNestedItems() {
            if (children == null) {
                if (depth > 20) {
                    // Avoid boundless recursion in some yaml parse trees
                    children = Collections.emptyList();
                    return children;
                }
                Object value = node.getValue();
                if (value instanceof Map) {
                    children = new ArrayList<YamlStructureItem>();
                    Map map = (Map) value;
                    Set<Map.Entry> entrySet = map.entrySet();

                    for (Map.Entry entry : entrySet) {

                        Object key = entry.getKey();
                        if (key instanceof PositionedSequenceNode) {
                            PositionedSequenceNode psn = (PositionedSequenceNode)key;
                            Object keyValue = psn.getValue();
                            assert keyValue instanceof List;
                            List<Node> list = (List<Node>)keyValue;
                            for (Node o : list) {
                                //String childName = o.getValue().toString();
                                Object childValue = o.getValue();
                                if (childValue instanceof List || childValue instanceof Map) {
                                    children.add(new YamlStructureItem(o, "list item", depth+1));
                                } else {
                                    String childName = childValue.toString();
                                    children.add(new YamlStructureItem(o, childName, depth+1));
                                }
                            }
                            Object entryValue = entry.getValue();
                            if (entryValue instanceof PositionedSequenceNode) {
                                psn = (PositionedSequenceNode)entryValue;
                                keyValue = psn.getValue();
                                assert keyValue instanceof List;
                                list = (List<Node>)keyValue;
                                for (Node o : list) {
                                    //String childName = o.getValue().toString();
                                    Object childValue = o.getValue();
                                    if (childValue instanceof List || childValue instanceof Map) {
                                        children.add(new YamlStructureItem(o, "list item", depth+1));
                                    } else {
                                        String childName = childValue.toString();
                                        children.add(new YamlStructureItem(o, childName, depth+1));
                                    }
                                }
                            }
                        } else {
                            assert key instanceof PositionedScalarNode;
                            //ScalarNode scalar = (ScalarNode)key;
                            PositionedScalarNode scalar = (PositionedScalarNode) key;
                            String childName = scalar.getValue().toString();
                            Node child = (Node) entry.getValue();
                            if (child != null) {
                                int e = ((Positionable) child).getRange().end.offset;
                                // If you have an "empty" key, e.g.
                                //   foo:
                                //   bar: Hello World
                                // here foo is "empty" but I get a child of "" positioned at the beginning
                                // of "bar", which is wrong. In this case, don't include the child in the
                                // position bounds.
                                if (child.getValue() instanceof ByteList && ((ByteList)child.getValue()).length() == 0) {
                                    e = ((Positionable) scalar).getRange().end.offset;
                                }
                                children.add(new YamlStructureItem(child, childName, depth+1,
                                        // Range: beginning of -key- to ending of -value-
                                        ((Positionable) scalar).getRange().start.offset,
                                        e));
                            }
                        }
                    }
                    // Keep the list ordered, same order as in the document!!
                    Collections.sort(children);
                } else if (value instanceof List) {
                    children = new ArrayList<YamlStructureItem>();
                    List<Node> list = (List<Node>) value;
                    for (Node o : list) {
                        //String childName = o.getValue().toString();
                        Object childValue = o.getValue();
                        if (childValue instanceof List || childValue instanceof Map) {
                            children.add(new YamlStructureItem(o, "list item", depth+1));
                        } else {
                            String childName = childValue.toString();
                            children.add(new YamlStructureItem(o, childName, depth+1));
                        }
                    }
                } else {
                    children = Collections.emptyList();
                }
            }

            return children;
        }

        public long getPosition() {
            return begin;
        }

        public long getEndPosition() {
            return end;
        }

        public ImageIcon getCustomIcon() {
            return null;
        }

        public int compareTo(YamlStructureItem other) {
            return (int)(begin-other.begin);
        }

        //@Override
        //public String toString() {
        //    return "YamlStructureItem(" + name + ",begin=" + begin;
        //}
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distributigon
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
package org.netbeans.editor.ext.html.parser.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Content;
import org.netbeans.editor.ext.html.dtd.DTD.ContentModel;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.openide.util.Parameters;

/**
 *
 * @author  mfukala@netbeans.org, Tomasz.Slota@Sun.COM
 */
public class AstNode {

    public static final String NAMESPACE_PROPERTY = "namespace";

    public enum NodeType {

        UNKNOWN_TAG, ROOT, COMMENT, DECLARATION, ERROR,
        TEXT, TAG, UNMATCHED_TAG, OPEN_TAG, ENDTAG, ENTITY_REFERENCE
    };
    private final String name;
    private NodeType nodeType;
    protected int startOffset;
    protected int endOffset;
    protected int logicalEndOffset;
    private List<AstNode> children = null;
    private AstNode parent = null;
    private Map<String, Attribute> attributes = null;
    private Content content = null;
    private ContentModel contentModel = null;
    private Element dtdElement = null;
    private Collection<ProblemDescription> descriptions = null;
    private List<String> stack = null; //for debugging
    private AstNode matchingNode = null;
    private boolean isEmpty = false;
    private Map<String, Object> properties;

    public static AstNode createRootNode(int from, int to, DTD dtd) {
        return new RootAstNode(from, to, dtd);
    }

    //TODO - replace the public constructors by factory methods
    public AstNode(String name, NodeType nodeType, int startOffset, int endOffset, Element dtdElement, boolean isEmpty, List<String> stack) {
        this(name, nodeType, startOffset, endOffset, isEmpty);
        this.dtdElement = dtdElement;
        this.contentModel = dtdElement != null ? dtdElement.getContentModel() : null;
        this.content = contentModel != null ? contentModel.getContent() : null;
        this.stack = stack;
    }

    public AstNode(String name, NodeType nodeType, int startOffset, int endOffset, boolean isEmpty) {
        this.name = name;
        this.nodeType = nodeType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.logicalEndOffset = endOffset;
        this.isEmpty = isEmpty;
    }

    public boolean isRootNode() {
        return false;
    }

    public boolean isVirtual() {
        return startOffset() == -1 && endOffset() == -1;
    }

    public void detachFromParent() {
        setParent(null);
    }

    public String getNamespace() {
        return (String) getRootNode().getProperty(NAMESPACE_PROPERTY);
    }

    public AstNode getMatchingTag() {
        return matchingNode;
    }

    /**
     * Returns an offsets range of the area which this node spans.
     * The behaviour differs based on the node type.
     * For matched open tags nodes the range is following:
     * openTag.startOffset, matchingTag.endOffset
     *
     * For the rest of node types the area is
     * node.startOffset, node.endOffset
     *
     * @return non-null int array - new int[]{from, to};
     *
     */
    public int[] getLogicalRange() {
        return new int[]{startOffset, logicalEndOffset};
    }

    public void setLogicalEndOffset(int offset) {
        this.logicalEndOffset = offset;
    }

    public void setMatchingNode(AstNode match) {
        this.matchingNode = match;
    }

    public boolean needsToHaveMatchingTag() {
        //non-dtd elements always need to have a pair
        if (getDTDElement() == null) {
            return true;
        } else {
            //dtd elements
            if (type() == NodeType.OPEN_TAG) {
                return !getDTDElement().hasOptionalEnd();
            } else if (type() == NodeType.ENDTAG) {
                return !getDTDElement().hasOptionalStart();
            } else {
                return false;
            }
        }
    }

    public Element getDTDElement() {
        return dtdElement;
    }

    public boolean reduce(Element element) {
        if (contentModel == null) {
            return false; //unknown tag can contain anything, error reports done somewhere else
        }

        Boolean canReduce = null;
        //process includes/excludes from the root node to the leaf
        List<AstNode> path = new ArrayList<AstNode>();
        for(AstNode node = this; node.type() != AstNode.NodeType.ROOT; node = node.parent()) {
            path.add(0, node);
        }
        for(AstNode node : path) {
            DTD.ContentModel cModel = node.getDTDElement().getContentModel();
            if(cModel.getIncludes().contains(element)) {
                canReduce = true;
            }
            if(cModel.getExcludes().contains(element)) {
                canReduce = false;
            }
        }

        if(canReduce != null) {
            return canReduce;
        }

        //explicitly exluded or included elements doesn't affect the reduction!
        if (contentModel.getExcludes().contains(element)) {
            return false;
        }
        if (contentModel.getIncludes().contains(element)) {
            return true;
        }
        Content c = content.reduce(element.getName());
        if (c != null) {
            content = c;
            return true;
        } else {
            //hack!?!?!!
            //nothing reduced, it still may be valid if one of the expected elements
            //has optional start && end
            for (Object o : contentModel.getContent().getPossibleElements()) {
                Element e = (Element) o;
                if (e != null && e.hasOptionalStart() && e.hasOptionalEnd()) {
                    //try to reduce here
                    Content c2 = e.getContentModel().getContent().reduce(element.getName());
                    if (c2 != null) {
                        //hmmm, the element can contain the element
                        content = Content.EMPTY_CONTENT; //?????????????????
                        return true;
                    }
                }
            }
            return false;
        }
    }

//    void dumpContent() {
//        for(Object o : content.getPossibleElements()) {
//            Element e = (Element)o;
//            System.out.print(e + ", ");
//        }
//    }
    public boolean isResolved() {
        if (content == null) {
            return false;
        }
        //CDATA or EMPTY element
        if (content instanceof DTD.ContentLeaf) {
            DTD.ContentLeaf cleaf = (DTD.ContentLeaf) content;
            if ("CDATA".equals(cleaf.getElementName()) ||
                    "EMPTY".equals(cleaf.getElementName())) {
                return true;
            }
        }

        //#PCDATA hack
        if (content.getPossibleElements().size() == 1) {
            if (content.getPossibleElements().iterator().next() == null) {
                //#PCDATA - consider resolved
                return true;
            }
        }

        return content == Content.EMPTY_CONTENT || content.isDiscardable(); //XXX: is that correct???
    }

    public List<Element> getUnresolvedElements() {
        if (!isResolved()) {
            return (List<Element>) content.getPossibleElements();
        } else {
            return null;
        }
    }

    public List<Element> getAllPossibleElements() {
        assert content != null;

        List<Element> col = new ArrayList<Element>();
        col.addAll((Collection<Element>) content.getPossibleElements());
        col.addAll(contentModel.getIncludes());
        col.removeAll(contentModel.getExcludes());
        return col;
    }

    public synchronized void addDescriptionToNode(String key, String message, int type) {
        //adjust the description position and length for open tag
        //only the tag name is annotated, not the whole tag
        int from = startOffset();
        int to = endOffset();

        if (type() == NodeType.OPEN_TAG) {
            to = from + 1 /* "<".length() */ + name().length(); //end of the tag name
            if (to == endOffset() - 1) {
                //if the closing greater than '>' symbol immediately follows
                //the tag name extend the description area to it as well
                to++;
            }
        }

        addDescription(ProblemDescription.create(key, message, type, from, to));
    }

    public synchronized void addDescriptionsToNode(Collection<String[]> keys_messages, int type) {
        for (String[] msg : keys_messages) {
            addDescriptionToNode(msg[0], msg[1], type);
        }
    }

    public synchronized void addDescription(ProblemDescription message) {
        if (descriptions == null) {
            descriptions = new ArrayList<ProblemDescription>(2);
        }
        descriptions.add(message);
    }

    public synchronized void addDescriptions(Collection<ProblemDescription> messages) {
        if (descriptions == null) {
            descriptions = new LinkedHashSet<ProblemDescription>(2);
        }
        descriptions.addAll(messages);
    }

    public Collection<ProblemDescription> getDescriptions() {
        return descriptions == null ? Collections.<ProblemDescription>emptyList() : descriptions;
    }

    public String name() {
        return name;
    }

    public NodeType type() {
        return nodeType;
    }

    public int startOffset() {
        return startOffset;
    }

    public int endOffset() {
        return endOffset;
    }

    public void setEndOffset(int offset) {
        this.endOffset = offset;
    }

    public int logicalStartOffset() {
        return getLogicalRange()[0];
    }

    public int logicalEndOffset() {
        return getLogicalRange()[1];
    }

    public List<AstNode> children() {
        return children == null ? Collections.EMPTY_LIST : children;
    }

    public List<AstNode> children(NodeFilter filter) {
        List<AstNode> filtered = new ArrayList<AstNode>(children().size());
        for(AstNode child : children()) {
            if(filter.accepts(child)) {
                filtered.add(child);
            }
        }
        return filtered;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public String getNamespacePrefix() {
        int colonIndex = name().indexOf(':');
        return colonIndex == -1 ? null : name().substring(0, colonIndex);
    }

    public String getNameWithoutPrefix() {
        if(name == null) { throw new NullPointerException(); } //issue 194537 - some diagnostic has been added to the html.parser module so once exception reporter reopens the issue again the ide log should be source of the info.
        int colonIndex = name().indexOf(':');
        return colonIndex == -1 ? name() : name().substring(colonIndex + 1);
    }

    public Object getProperty(String key) {
        return properties == null ? null : properties.get(key);
    }

    public synchronized void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(key, value);
    }

    public AstNode getRootNode() {
        if (this instanceof RootAstNode) {
            return this;
        } else {
            return parent().getRootNode();
        }
    }

    public void addChild(AstNode child) {
        initChildren();
        children.add(child);
        child.setParent(this);
    }

    public boolean insertBefore(AstNode node, AstNode insertBeforeNode) {
        initChildren();
        int idx = children.indexOf(insertBeforeNode);
        if(idx == -1) {
            return false; //no such node in children
        }
        children.add(idx, node);
        node.setParent(this);
        return true;
    }

    public void addChildren(List<AstNode> childrenList) {
        initChildren();
        for(AstNode child : childrenList) {
            addChild(child);
        }
    }

    public void removeChild(AstNode child) {
        initChildren();
        child.setParent(null);
        children.remove(child);
    }

    public void removeChildren(List<AstNode> childrenList) {
        initChildren();
        for(AstNode child : new ArrayList<AstNode>(childrenList)) {
            removeChild(child);
        }
    }

    private synchronized void initChildren() {
        if (children == null) {
            children = new LinkedList<AstNode>();
        }
    }

    public void setAttribute(Attribute attr) {
        if (attributes == null) {
            attributes = new HashMap<String, Attribute>();
        }
        attributes.put(attr.name(), attr);
    }

    public Collection<String> getAttributeKeys() {
        return attributes == null ? Collections.EMPTY_LIST : attributes.keySet();
    }

    public Collection<Attribute> getAttributes() {
        return attributes == null ? Collections.EMPTY_LIST : attributes.values();
    }

    public Collection<Attribute> getAttributes(AttributeFilter filter) {
        if(attributes == null) {
            return Collections.EMPTY_LIST;
        }
        Collection<Attribute> filtered = new ArrayList<Attribute>(getAttributes().size() / 2);
        for(Attribute attr : getAttributes()) {
            if(filter.accepts(attr)) {
                filtered.add(attr);
            }
        }
        return filtered;
    }

    public Attribute getAttribute(String attributeName) {
        return attributes == null ? null : attributes.get(attributeName);
    }

    public String getUnqotedAttributeValue(String attributeName) {
        Attribute a = getAttribute(attributeName);
        return a == null ? null : a.unquotedValue();
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();

        //basic info
        boolean isTag = type() == NodeType.OPEN_TAG || type() == NodeType.ENDTAG;

        if (isTag) {
            b.append(type() == NodeType.OPEN_TAG ? "<" : "");
            b.append(type() == NodeType.ENDTAG ? "</" : "");
        }
        if(getMatchingTag() != null) {
            b.append('*');
        }

        if (name() != null) {
            b.append(name());
        }
        if (isTag) {
            b.append('>');
        } else {
            if (name() != null) {
                b.append(':');
            }
            b.append(type());
        }
        b.append('(');
        if(isVirtual()) {
            b.append("virtual");
        } else {
            b.append(startOffset());
            b.append('-');
            b.append(endOffset());
            if (logicalEndOffset != endOffset) {
                b.append('/');
                b.append(logicalEndOffset);
            }
        }
        b.append(')');

        //add dtd element info
        Element e = getDTDElement();
        if (e != null) {
            b.append("[");
            b.append(e.hasOptionalStart() ? "O" : "R");
            b.append(e.hasOptionalEnd() ? "O" : "R");
            if (e.isEmpty()) {
                b.append("E");
            }
            b.append(isResolved() ? "" : "!");
            b.append("]");
        }

        b.append('{');
        //attributes
        for(Attribute a : getAttributes()) {
            b.append(a.toString());
            b.append(',');
        }
        b.append('}');

        //attched messages
        for (ProblemDescription d : getDescriptions()) {
            b.append(d.getKey());
            b.append(' ');
        }

        //dump stack if possible
        if (stack != null) {
            b.append(";S:");
            for (String item : stack) {
                b.append(item);
                b.append(',');
            }
            b.deleteCharAt(b.length() - 1);
        }

        if(!getDescriptions().isEmpty()) {
            b.append("; issues:");
            for(ProblemDescription d : getDescriptions()) {
                b.append(d);
            }
        }


        return b.toString();
    }

    String signature() {
        return name() + "[" + type() + "]";
    }

    public AstNode parent() {
        return parent;
    }

    /** returns the AST path from the root element */
    public AstPath path() {
        return new AstPath(null, this);
    }

    private void setParent(AstNode parent) {
        this.parent = parent;
    }

    public static interface NodeFilter {
        public boolean accepts(AstNode node);
    }

    /** Attributes acceptor, allows to query node attributes.*/
    public static interface AttributeFilter {
        public boolean accepts(Attribute attribute);
    }

    public static class Attribute {

        private static final char NS_PREFIX_DELIMITER = ':';

        protected String name;
        protected String value;
        protected int nameOffset;
        protected int valueOffset;

        public Attribute(String name, String value, int nameOffset, int valueOffset) {
            this.name = name;
            this.value = value;
            this.nameOffset = nameOffset;
            this.valueOffset = valueOffset;
        }

        public String name() {
            return name;
        }

        public String namespacePrefix() {
            int delimIndex = name().indexOf(NS_PREFIX_DELIMITER);
            return delimIndex == -1 ? null : name().substring(0, delimIndex);
        }

        public String nameWithoutNamespacePrefix() {
            int delimIndex = name().indexOf(NS_PREFIX_DELIMITER);
            return delimIndex == -1 ? name() : name().substring(delimIndex + 1);
        }

        public int nameOffset() {
            return nameOffset;
        }

        public int valueOffset() {
            return valueOffset;
        }

        public int unqotedValueOffset() {
            return isValueQuoted() ? valueOffset + 1 : valueOffset;
        }

        public String value() {
            return value;
        }

        public String unquotedValue() {
            return isValueQuoted() ? value.substring(1, value.length() - 1) : value;
        }

        public boolean isValueQuoted() {
            if (value.length() < 2) {
                return false;
            } else {
                return ((value.charAt(0) == '\'' || value.charAt(0) == '"') &&
                        (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
            }
        }

        @Override
        public String toString() {
            return "Attr[" + name() + "(" + nameOffset() + ")=" + value + "(" + valueOffset() + ")]";
        }


    }

    private static class RootAstNode extends AstNode {

        private static String ROOT_NODE_NAME = "root"; //NOI18N
        private DTD dtd;

        private RootAstNode(int startOffset, int endOffset, DTD dtd) {
            super(ROOT_NODE_NAME, NodeType.ROOT, startOffset, endOffset, false);
            this.dtd = dtd;
        }

        @Override
        public boolean isRootNode() {
            return true;
        }

        @Override
        public List<Element> getAllPossibleElements() {
            return dtd == null ? Collections.emptyList() : dtd.getElementList(null);
        }
    }
}

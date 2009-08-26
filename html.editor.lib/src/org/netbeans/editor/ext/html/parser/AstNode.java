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
package org.netbeans.editor.ext.html.parser;

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
    private String name;
    private NodeType nodeType;
    private int startOffset;
    private int endOffset;
    private int logicalEndOffset;
    private List<AstNode> children = null;
    private AstNode parent = null;
    private Map<String, Object> attributes = null;
    private Content content = null;
    private ContentModel contentModel = null;
    private Element dtdElement = null;
    private Collection<Description> descriptions = null;
    private List<String> stack = null; //for debugging
    private AstNode matchingNode = null;
    private boolean isEmpty = false;
    private Map<String, Object> properties;

    static AstNode createRootNode(int from, int to, DTD dtd) {
        return new RootAstNode(from, to, dtd);
    }

    //TODO - replace the public constructors by factory methods
    AstNode(String name, NodeType nodeType, int startOffset, int endOffset, Element dtdElement, boolean isEmpty, List<String> stack) {
        this(name, nodeType, startOffset, endOffset, isEmpty);
        this.dtdElement = dtdElement;
        this.contentModel = dtdElement != null ? dtdElement.getContentModel() : null;
        this.content = contentModel != null ? contentModel.getContent() : null;
        this.stack = stack;
    }

    AstNode(String name, NodeType nodeType, int startOffset, int endOffset, boolean isEmpty) {
        this.name = name;
        this.nodeType = nodeType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.logicalEndOffset = endOffset;
        this.isEmpty = isEmpty;
    }

    public String getNamespace() {
        return (String)getRootNode().getProperty(NAMESPACE_PROPERTY);
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

    void setLogicalEndOffset(int offset) {
        this.logicalEndOffset = offset;
    }

    void setMatchingNode(AstNode match) {
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

    boolean reduce(Element element) {
        if (contentModel == null) {
            return false; //unknown tag can contain anything, error reports done somewhere else
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
    boolean isResolved() {
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

    synchronized void addDescriptionToNode(String key, String message, int type) {
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

        addDescription(Description.create(key, message, type, from, to));
    }

    synchronized void addDescriptionsToNode(Collection<String[]> keys_messages, int type) {
        for (String[] msg : keys_messages) {
            addDescriptionToNode(msg[0], msg[1], type);
        }
    }

    synchronized void addDescription(Description message) {
        if (descriptions == null) {
            descriptions = new ArrayList<Description>(2);
        }
        descriptions.add(message);
    }

    synchronized void addDescriptions(Collection<Description> messages) {
        if (descriptions == null) {
            descriptions = new LinkedHashSet<Description>(2);
        }
        descriptions.addAll(messages);
    }

    public Collection<Description> getDescriptions() {
        return descriptions == null ? Collections.<Description>emptyList() : descriptions;
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

    public int logicalStartOffset() {
        return getLogicalRange()[0];
    }

    public int logicalEndOffset() {
        return getLogicalRange()[1];
    }

    public List<AstNode> children() {
        return children == null ? Collections.EMPTY_LIST : children;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public String getNamespacePrefix() {
        int colonIndex = name().indexOf(':');
        return colonIndex == -1 ? null : name().substring(0, colonIndex);
    }

    public String getNameWithoutPrefix() {
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

    void addChild(AstNode child) {
        if (children == null) {
            children = new LinkedList<AstNode>();
        }
        children.add(child);
        child.setParent(this);
    }

    void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(key, value);
    }

    public Collection<String> getAttributeKeys() {
        return attributes == null ? Collections.EMPTY_LIST : attributes.keySet();
    }

    public Object getAttribute(String key) {
        return attributes == null ? null : attributes.get(key);
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
        b.append(startOffset());
        b.append('-');
        b.append(endOffset());
        if (logicalEndOffset != endOffset) {
            b.append('/');
            b.append(logicalEndOffset);
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

        //attched messages
        for (Description d : getDescriptions()) {
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

    public static final class Description {

        public static final int INFORMATION = 0;
        public static final int WARNING = 1;
        public static final int ERROR = 2;
        private String key;
        private String text;
        private int from, to;
        private int type;

        public static Description create(String key, String text, int type, int from, int to) {
            return new Description(key, text, type, from, to);
        }

        private Description(String key, String text, int type, int from, int to) {
            this.key = key;
            this.text = text;
            this.type = type;
            this.from = from;
            this.to = to;
        }

        public String getKey() {
            return key;
        }

        public int getType() {
            return type;
        }

        public int getFrom() {
            return from;
        }

        public String getText() {
            return text;
        }

        public int getTo() {
            return to;
        }

        @Override
        public String toString() {
            return dump(null);
        }

        public String dump(String code) {
            String ttype = "";
            switch (getType()) {
                case INFORMATION:
                    ttype = "Information"; //NOI18N
                    break;
                case WARNING:
                    ttype = "Warning"; //NOI18N
                    break;
                case ERROR:
                    ttype = "Error"; //NOI18N
                    break;
            }
            String nodetext = code == null ? "" : code.substring(getFrom(), getTo());
            return ttype + ":" + getKey() + " [" + getFrom() + " - " + getTo() + "]: '" + nodetext + (getText() != null ? "'; msg=" + getText() : "");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Description other = (Description) obj;
            if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
                return false;
            }
            if (this.from != other.from) {
                return false;
            }
            if (this.to != other.to) {
                return false;
            }
            if (this.type != other.type) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + (this.key != null ? this.key.hashCode() : 0);
            hash = 23 * hash + this.from;
            hash = 23 * hash + this.to;
            hash = 23 * hash + this.type;
            return hash;
        }
    }

    private static class RootAstNode extends AstNode {

        private static String ROOT_NODE_NAME = "root"; //NOI18N
        private DTD dtd;

        RootAstNode(int startOffset, int endOffset, DTD dtd) {
            super(ROOT_NODE_NAME, NodeType.ROOT, startOffset, endOffset, false);
            this.dtd = dtd;
        }

        @Override
        public List<Element> getAllPossibleElements() {
            return dtd.getElementList(null);
        }
    }
}

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
package org.netbeans.modules.html.editor.lib.html4parser;

import org.netbeans.modules.html.editor.lib.api.elements.ElementFilter;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.AttributeFilter;
import org.netbeans.modules.html.editor.lib.api.elements.FeaturedNode;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.TreePath;
import java.util.*;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.dtd.DTD;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Content;
import org.netbeans.modules.html.editor.lib.dtd.DTD.ContentModel;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Element;
import org.openide.util.CharSequences;
import org.openide.util.Parameters;

/**
 *
 * @author mfukala@netbeans.org, Tomasz.Slota@Sun.COM
 */
public class AstNode implements FeaturedNode {

    public static final String NAMESPACE_PROPERTY = "namespace";
    //an attempt to save some memory
    private static byte tchildren = 0;
//    private static byte parent = 1;
    private static byte tattributes = 2;
    private static byte tcontent = 3;
    private static byte tcontentModel = 4;
    private static byte tdtdElement = 5;
    private static byte tdescriptions = 6;
    private static byte tstack = 7;
//    private static byte matchingNode = 8;
    private static byte tproperties = 9;

    @Override
    public int[] logicalRange() {
        return getLogicalRange();
    }

    @Override
    public Node matchingTag() {
        return getMatchingTag();
    }

//    @Override
//    public String getProperty(String propertyName) {
//        return getProperty(propertyName);
//    }

    private static class PEntry {

        private byte type;
        private Object entry;
        private PEntry next;

        public PEntry(byte type, Object entry, PEntry next) {
            this.type = type;
            this.entry = entry;
            this.next = next;
        }
    }
    //to store arbitrary and usually not used properties
    private PEntry pEntry;

    private void putProp(byte type, Object value) {
        if (pEntry == null) {
            //not a single entry
            pEntry = new PEntry(type, value, null);
            return;
        }

        PEntry pe = pEntry;
        PEntry lastNonNullPe;
        do {
            lastNonNullPe = pe;
            if (pe.type == type) {
                //update existing entry
                pe.entry = value;
                return;
            }
            pe = pe.next;
        } while (pe != null);

        //no entry of such type found - add a new one to the last entry
        lastNonNullPe.next = new PEntry(type, value, null);
    }

    //linked list
    private Object getProp(byte type) {
        PEntry pe = pEntry;
        while (pe != null) {
            if (pe.type == type) {
                return pe.entry;
            }
            pe = pe.next;
        }
        return null;
    }
    //base properties held by the AstNode itself
    protected final int startOffset;
    protected int endOffset;
    protected int logicalEndOffset;
    private final CharSequence name;
    private boolean empty = false;
    private final ElementType nodeType;
    private Node parent = null;
    private Node matchingNode = null;

//    private List<AstNode> children = null;
//    private Map<String, Attribute> attributes = null;
//    private Content content = null;
//    private ContentModel contentModel = null;
//    private Element dtdElement = null;
//    private Collection<ProblemDescription> descriptions = null;
//    private List<String> stack = null; //for debugging
//    private Map<String, Object> properties;
    public AstNode(CharSequence name, ElementType nodeType, int startOffset, int endOffset, Element dtdElement, boolean isEmpty, List<CharSequence> stack) {
        this(name, nodeType, startOffset, endOffset, isEmpty);

        setDtdElement(dtdElement);

        setContentModel(dtdElement != null ? dtdElement.getContentModel() : null);

        ContentModel cModel = getContentModel();
        setContent(cModel != null ? cModel.getContent() : null);

        setStack(stack);
    }

    public AstNode(CharSequence name, ElementType nodeType, int startOffset, int endOffset, boolean isEmpty) {
        Parameters.notNull("name", name);

        this.name = name;
        this.nodeType = nodeType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.logicalEndOffset = endOffset;
        this.empty = isEmpty;
    }

    @Override
    public CharSequence nodeId() {
        return name;
    }

    @Override
    public int from() {
        return startOffset();
    }

    @Override
    public int to() {
        return endOffset();
    }

    @Override
    public CharSequence image() {
        return null;
    }

    @Override
    public Collection<ProblemDescription> problems() {
        return getDescriptions();
    }

    private ContentModel getContentModel() {
        return (ContentModel) getProp(tcontentModel);
    }

    private void setContentModel(ContentModel model) {
        putProp(tcontentModel, model);
    }

    private Content getContent() {
        return (Content) getProp(tcontent);
    }

    private void setContent(Content c) {
        putProp(tcontent, c);
    }

    private List<CharSequence> getStack() {
        return (List<CharSequence>) getProp(tstack);
    }

    private void setStack(List<CharSequence> s) {
        putProp(tstack, s);
    }

    private Collection<ProblemDescription> getDescriptions_property() { //we already have getDescriptions()
        return (Collection<ProblemDescription>) getProp(tdescriptions);
    }

    private void setDescriptions(Collection<ProblemDescription> d) {
        putProp(tdescriptions, d);
    }

    private List<Node> getChildren() {
        return (List<Node>) getProp(tchildren);
    }

    private void setChildren(List<Node> c) {
        putProp(tchildren, c);
    }

    private Map<String, AstAttribute> getAttributes_property() {
        return (Map<String, AstAttribute>) getProp(tattributes);
    }

    private void setAttributes(Map<String, AstAttribute> a) {
        putProp(tattributes, a);
    }

    private Map<String, Object> getProperties() {
        return (Map<String, Object>) getProp(tproperties);
    }

    private void setProperties(Map<String, Object> p) {
        putProp(tproperties, p);
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

    public Node getMatchingTag() {
//        return (AstNode) getProp(matchingNode);
        return matchingNode;
    }

    /**
     * Returns an offsets range of the area which this node spans. The behaviour
     * differs based on the node type. For matched open tags nodes the range is
     * following: openTag.startOffset, matchingTag.endOffset
     *
     * For the rest of node types the area is node.startOffset, node.endOffset
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

    public void setMatchingNode(Node match) {
//        putProp(matchingNode, match);]
        this.matchingNode = match;
    }

    public boolean needsToHaveMatchingTag() {
        //non-dtd elements always need to have a pair
        if (getDTDElement() == null) {
            return true;
        } else {
            //dtd elements
            if (type() == ElementType.OPEN_TAG) {
                return !getDTDElement().hasOptionalEnd();
            } else if (type() == ElementType.END_TAG) {
                return !getDTDElement().hasOptionalStart();
            } else {
                return false;
            }
        }
    }

    public Element getDTDElement() {
        return (Element) getProp(tdtdElement);
    }

    private void setDtdElement(Element de) {
        putProp(tdtdElement, de);
    }

    public boolean reduce(Element element) {
        if (getContentModel() == null) {
            return false; //unknown tag can contain anything, error reports done somewhere else
        }

        Boolean canReduce = null;
        //process includes/excludes from the root node to the leaf
        List<AstNode> path = new ArrayList<AstNode>();
        for (AstNode node = this; node.type() != ElementType.ROOT; node = (AstNode) node.parent()) {
            path.add(0, node);
        }
        for (AstNode node : path) {
            DTD.ContentModel cModel = node.getDTDElement().getContentModel();
            if (cModel.getIncludes().contains(element)) {
                canReduce = true;
            }
            if (cModel.getExcludes().contains(element)) {
                canReduce = false;
            }
        }

        if (canReduce != null) {
            return canReduce;
        }

        //explicitly exluded or included elements doesn't affect the reduction!
        if (getContentModel().getExcludes().contains(element)) {
            return false;
        }
        if (getContentModel().getIncludes().contains(element)) {
            return true;
        }
        Content c = getContent().reduce(element.getName());
        if (c != null) {
            setContent(c);
            return true;
        } else {
            //hack!?!?!!
            //nothing reduced, it still may be valid if one of the expected elements
            //has optional start && end
            for (Object o : getContentModel().getContent().getPossibleElements()) {
                Element e = (Element) o;
                if (e != null && e.hasOptionalStart() && e.hasOptionalEnd()) {
                    //try to reduce here
                    Content c2 = e.getContentModel().getContent().reduce(element.getName());
                    if (c2 != null) {
                        //hmmm, the element can contain the element
                        setContent(Content.EMPTY_CONTENT); //?????????????????
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean isResolved() {
        Content _content = getContent();
        if (_content == null) {
            return false;
        }
        //CDATA or EMPTY element
        if (_content instanceof DTD.ContentLeaf) {
            DTD.ContentLeaf cleaf = (DTD.ContentLeaf) _content;
            if ("CDATA".equals(cleaf.getElementName())
                    || "EMPTY".equals(cleaf.getElementName())) {
                return true;
            }
        }

        //#PCDATA hack
        if (_content.getPossibleElements().size() == 1) {
            if (_content.getPossibleElements().iterator().next() == null) {
                //#PCDATA - consider resolved
                return true;
            }
        }

        return _content == Content.EMPTY_CONTENT || _content.isDiscardable(); //XXX: is that correct???
    }

    public List<Element> getUnresolvedElements() {
        if (!isResolved()) {
            return (List<Element>) getContent().getPossibleElements();
        } else {
            return null;
        }
    }

    public List<Element> getAllPossibleElements() {
        Content content = getContent();
        assert content != null;

        List<Element> col = new ArrayList<Element>();
        col.addAll((Collection<Element>) content.getPossibleElements());
        col.addAll(getContentModel().getIncludes());
        col.removeAll(getContentModel().getExcludes());
        return col;
    }

    public synchronized void addDescriptionToNode(String key, String message, int type) {
        //adjust the description position and length for open tag
        //only the tag name is annotated, not the whole tag
        int from = startOffset();
        int to = endOffset();

        if (type() == ElementType.OPEN_TAG) {
            to = from + 1 /*
                     * "<".length()
                     */ + name().length(); //end of the tag name
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
        if (getDescriptions_property() == null) {
            setDescriptions(new ArrayList<ProblemDescription>(2));
        }
        getDescriptions_property().add(message);
    }

    public synchronized void addDescriptions(Collection<ProblemDescription> messages) {
        if (getDescriptions_property() == null) {
            setDescriptions(new LinkedHashSet<ProblemDescription>(messages.size()));
        }
        getDescriptions_property().addAll(messages);
    }

    public Collection<ProblemDescription> getDescriptions() {
        return getDescriptions_property() == null ? Collections.<ProblemDescription>emptyList() : getDescriptions_property();
    }

    public CharSequence name() {
        return name;
    }

    public ElementType type() {
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

    @Override
    public List<Node> children() {
        List<Node> children = getChildren();
        return children == null ? Collections.EMPTY_LIST : children;
    }

    public List<Node> children(ElementFilter filter) {
        List<Node> filtered = new ArrayList<Node>(children().size());
        for (Node child : children()) {
            if (filter.accepts(child)) {
                filtered.add(child);
            }
        }
        return filtered;
    }

    public boolean isEmpty() {
        return empty;
    }

    public CharSequence getNamespacePrefix() {
        int colonIndex = CharSequences.indexOf(name(), ":");
        return colonIndex == -1 ? null : name().subSequence(0, colonIndex);
    }

    public CharSequence getNameWithoutPrefix() {
        int colonIndex = CharSequences.indexOf(name(), ":");
        return colonIndex == -1 ? name() : name().subSequence(colonIndex + 1, name().length());
    }

    public Object getProperty(String key) {
        Map<String, Object> properties = getProperties();
        return properties == null ? null : properties.get(key);
    }

    public synchronized void setProperty(String key, Object value) {
        if (getProperties() == null) {
            setProperties(new HashMap<String, Object>());
        }
        getProperties().put(key, value);
    }

    public AstNode getRootNode() {
        if (this instanceof RootAstNode) {
            return this;
        } else {
            AstNode astparent = (AstNode) parent();
            return astparent.getRootNode();
        }
    }

    public void addChild(AstNode child) {
        initChildren();
        getChildren().add(child);
        child.setParent(this);
    }

    public boolean insertBefore(AstNode node, AstNode insertBeforeNode) {
        List<Node> _children = getChildren();
        initChildren();
        int idx = _children.indexOf(insertBeforeNode);
        if (idx == -1) {
            return false; //no such node in children
        }
        _children.add(idx, node);
        node.setParent(this);
        return true;
    }

    public void addChildren(List<AstNode> childrenList) {
        initChildren();
        for (AstNode child : childrenList) {
            addChild(child);
        }
    }

    public void removeChild(AstNode child) {
        initChildren();
        child.setParent(null);
        getChildren().remove(child);
    }

    public void removeChildren(List<AstNode> childrenList) {
        initChildren();
        for (AstNode child : new ArrayList<AstNode>(childrenList)) {
            removeChild(child);
        }
    }

    private synchronized void initChildren() {
        if (getChildren() == null) {
            setChildren(new LinkedList<Node>());
        }
    }

    public void setAttribute(AstAttribute attr) {
        if (getAttributes_property() == null) {
            setAttributes(new HashMap<String, AstAttribute>());
        }
        getAttributes_property().put(attr.name(), attr);
    }

    public Collection<String> getAttributeKeys() {
        Map<String, AstAttribute> _attributes = getAttributes_property();
        return _attributes == null ? Collections.EMPTY_LIST : _attributes.keySet();
    }

    public Collection<AstAttribute> getAttributes() {
        Map<String, AstAttribute> _attributes = getAttributes_property();
        return _attributes == null ? Collections.EMPTY_LIST : _attributes.values();
    }

    public Collection<AstAttribute> getAttributes(AttributeFilter filter) {
        Map<String, AstAttribute> _attributes = getAttributes_property();
        if (_attributes == null) {
            return Collections.EMPTY_LIST;
        }
        Collection<AstAttribute> filtered = new ArrayList<AstAttribute>(getAttributes_property().size() / 2);
        for (AstAttribute attr : getAttributes()) {
            if (filter.accepts(attr)) {
                filtered.add(attr);
            }
        }
        return filtered;
    }

    public AstAttribute getAttribute(String attributeName) {
        Map<String, AstAttribute> _attributes = getAttributes_property();
        return _attributes == null ? null : _attributes.get(attributeName);
    }

    public String getUnqotedAttributeValue(String attributeName) {
        AstAttribute a = getAttribute(attributeName);
        return a == null ? null : a.unquotedValue();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        //basic info
        boolean isTag = type() == ElementType.OPEN_TAG || type() == ElementType.END_TAG;

        if (isTag) {
            b.append(type() == ElementType.OPEN_TAG ? "<" : "");
            b.append(type() == ElementType.END_TAG ? "</" : "");
        }
        if (getMatchingTag() != null) {
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
        if (isVirtual()) {
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
        for (AstAttribute a : getAttributes()) {
            b.append(a.toString());
            b.append(',');
        }
        b.append('}');

        //attched messages
        for (ProblemDescription d : getDescriptions_property()) {
            b.append(d.getKey());
            b.append(' ');
        }

        //dump stack if possible
        if (getStack() != null) {
            b.append(";S:");
            for (CharSequence item : getStack()) {
                b.append(item);
                b.append(',');
            }
            b.deleteCharAt(b.length() - 1);
        }

        if (!getDescriptions_property().isEmpty()) {
            b.append("; issues:");
            for (ProblemDescription d : getDescriptions_property()) {
                b.append(d);
            }
        }


        return b.toString();
    }

    String signature() {
        return name() + "[" + type() + "]";
    }

    public Node parent() {
        return parent;
    }

    /**
     * returns the AST path from the root element
     */
    public TreePath path() {
        return new TreePath(null, this);
    }

    private void setParent(Node p) {
        this.parent = p;
    }

    public static class AstAttribute implements Attribute {

        private static final char NS_PREFIX_DELIMITER = ':';
        protected CharSequence name;
        protected CharSequence value;
        protected int nameOffset;
        protected int valueOffset;

        public AstAttribute(CharSequence name, CharSequence value, int nameOffset, int valueOffset) {
            this.name = name;
            this.value = value;
            this.nameOffset = nameOffset;
            this.valueOffset = valueOffset;
        }

        public String name() {
            return name.toString();
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
            return value.toString();
        }

        public String unquotedValue() {
            return isValueQuoted() ? value().substring(1, value().length() - 1) : value();
        }

        public boolean isValueQuoted() {
            if (value.length() < 2) {
                return false;
            } else {
                return ((value.charAt(0) == '\'' || value.charAt(0) == '"')
                        && (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
            }
        }

        @Override
        public String toString() {
            return "Attr[" + name() + "(" + nameOffset() + ")=" + value + "(" + valueOffset() + ")]";
        }
    }

    static class RootAstNode extends AstNode {

        private static String ROOT_NODE_NAME = "root"; //NOI18N
        private DTD dtd;

        RootAstNode(int startOffset, int endOffset) {
            super(ROOT_NODE_NAME, ElementType.ROOT, startOffset, endOffset, false);
        }
        
        RootAstNode(int startOffset, int endOffset, DTD dtd) {
            super(ROOT_NODE_NAME, ElementType.ROOT, startOffset, endOffset, false);
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

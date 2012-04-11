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

import java.util.*;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.lib.dtd.DTD;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Content;
import org.netbeans.modules.html.editor.lib.dtd.DTD.ContentModel;
import org.openide.util.CharSequences;
import org.openide.util.Parameters;

/**
 *
 * @author mfukala@netbeans.org, Tomasz.Slota@Sun.COM
 */
public class AstNode implements FeaturedNode, OpenTag, CloseTag {

    public static final String NAMESPACE_PROPERTY = "namespace";

    private final int startOffset, endOffset;
    private final CharSequence name;
    private final boolean empty;
    private final ElementType nodeType;
    private final org.netbeans.modules.html.editor.lib.dtd.DTD.Element dtdElement;
    
    private int semanticEndOffset;
    private Node parent;
    private Node matchingNode;
    private List<Element> children;
    private Map<String, Attribute> attributes;
    private final ContentModel contentModel;
    
    private Collection<ProblemDescription> descriptions = null;
    private List<CharSequence> stack = null; //for debugging
    private Map<String, Object> properties;
    private Content content;

    public AstNode(CharSequence name,
            ElementType nodeType,
            int startOffset,
            int endOffset,
            org.netbeans.modules.html.editor.lib.dtd.DTD.Element dtdElement,
            boolean isEmpty,
            List<CharSequence> stack) {
        
        this.name = name;
        this.nodeType = nodeType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.semanticEndOffset = endOffset;
        this.empty = isEmpty;

        this.dtdElement = dtdElement;
        this.contentModel = dtdElement != null ? dtdElement.getContentModel() : null;
        this.content = contentModel != null ? contentModel.getContent() : null;

        this.stack = stack;
    }

    public AstNode(CharSequence name, 
            ElementType nodeType, 
            int startOffset, 
            int endOffset, 
            boolean isEmpty) {
        this(name, nodeType, startOffset, endOffset, null, isEmpty, null);
    }

    @Override
    public int semanticEnd() {
        return semanticEndOffset;
    }

    @Override
    public OpenTag matchingOpenTag() {
        return (OpenTag) getMatchingTag();
    }
    
    @Override
    public CharSequence id() {
        return name;
    }

    @Override
    public int from() {
        return startOffset;
    }

    @Override
    public int to() {
        return endOffset;
    }

    @Override
    public CharSequence image() {
        return null;
    }

    @Override
    public Collection<ProblemDescription> problems() {
        return descriptions == null ? Collections.<ProblemDescription>emptyList() : descriptions;
    }

    private ContentModel getContentModel() {
        return contentModel;
    }

    private Content getContent() {
        return content;
    }

    private void setContent(Content content) {
        this.content = content;
    }

    boolean isVirtual() {
        return from() == -1 && to() == -1;
    }

    void detachFromParent() {
        setParent(null);
    }

    Node getMatchingTag() {
        return matchingNode;
    }

    void setLogicalEndOffset(int offset) {
        this.semanticEndOffset = offset;
    }

    void setMatchingNode(Node match) {
        this.matchingNode = match;
    }

    boolean needsToHaveMatchingTag() {
        //non-dtd elements always need to have a pair
        if (getDTDElement() == null) {
            return true;
        } else {
            //dtd elements
            if (type() == ElementType.OPEN_TAG) {
                return !getDTDElement().hasOptionalEnd();
            } else if (type() == ElementType.CLOSE_TAG) {
                return !getDTDElement().hasOptionalStart();
            } else {
                return false;
            }
        }
    }

    org.netbeans.modules.html.editor.lib.dtd.DTD.Element getDTDElement() {
        return dtdElement;
    }

    boolean reduce(org.netbeans.modules.html.editor.lib.dtd.DTD.Element element) {
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
                org.netbeans.modules.html.editor.lib.dtd.DTD.Element e = (org.netbeans.modules.html.editor.lib.dtd.DTD.Element) o;
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

    boolean isResolved() {
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

    List<Element> getUnresolvedElements() {
        if (!isResolved()) {
            return (List<Element>) getContent().getPossibleElements();
        } else {
            return null;
        }
    }

    List<org.netbeans.modules.html.editor.lib.dtd.DTD.Element> getAllPossibleElements() {
        Content c = getContent();
        if(c == null) {
            return Collections.emptyList();
        }

        List<org.netbeans.modules.html.editor.lib.dtd.DTD.Element> col = new ArrayList<org.netbeans.modules.html.editor.lib.dtd.DTD.Element>();
        col.addAll((Collection<org.netbeans.modules.html.editor.lib.dtd.DTD.Element>) c.getPossibleElements());
        col.addAll(getContentModel().getIncludes());
        col.removeAll(getContentModel().getExcludes());
        return col;
    }

    synchronized void addDescriptionToNode(String key, String message, int type) {
        //adjust the description position and length for open tag
        //only the tag name is annotated, not the whole tag
        int from = from();
        int to = to();

        if (type() == ElementType.OPEN_TAG) {
            to = from + 1 /*
                     * "<".length()
                     */ + name().length(); //end of the tag name
            if (to == to() - 1) {
                //if the closing greater than '>' symbol immediately follows
                //the tag name extend the description area to it as well
                to++;
            }
        }

        addDescription(ProblemDescription.create(key, message, type, from, to));
    }

    synchronized void addDescriptionsToNode(Collection<String[]> keys_messages, int type) {
        for (String[] msg : keys_messages) {
            addDescriptionToNode(msg[0], msg[1], type);
        }
    }

    synchronized void addDescription(ProblemDescription message) {
        if (descriptions == null) {
            descriptions = new ArrayList<ProblemDescription>(1);
        }
        descriptions.add(message);

    }

    synchronized void addDescriptions(Collection<ProblemDescription> messages) {
        if (descriptions == null) {
            descriptions = new ArrayList<ProblemDescription>(1);
        }
        descriptions.addAll(messages);
    }

    @Override
    public CharSequence name() {
        return name;
    }

    @Override
    public ElementType type() {
        return nodeType;
    }

    @Override
    public Collection<Element> children() {
        return children == null ? Collections.EMPTY_LIST : children;
    }

    @Override
    public Collection<Element> children(ElementFilter filter) {
        Collection<Element> filtered = new ArrayList<Element>(children().size());
        for (Element child : children()) {
            if (filter.accepts(child)) {
                filtered.add(child);
            }
        }
        return filtered;
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public Object getProperty(String key) {
        return properties == null ? null : properties.get(key);
    }

    synchronized void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(key, value);
    }

    AstNode getRootNode() {
        if (this instanceof RootAstNode) {
            return this;
        } else {
            AstNode astparent = (AstNode) parent();
            return astparent.getRootNode();
        }
    }

    void addChild(AstNode child) {
        initChildren();
        children.add(child);
        child.setParent(this);
    }

    boolean insertBefore(AstNode node, AstNode insertBeforeNode) {
        List<Element> _children = children;
        initChildren();
        int idx = _children.indexOf(insertBeforeNode);
        if (idx == -1) {
            return false; //no such node in children
        }
        _children.add(idx, node);
        node.setParent(this);
        return true;
    }

    void addChildren(List<AstNode> childrenList) {
        initChildren();
        for (AstNode child : childrenList) {
            addChild(child);
        }
    }

    void removeChild(AstNode child) {
        initChildren();
        child.setParent(null);
        children.remove(child);
    }

    void removeChildren(List<AstNode> childrenList) {
        initChildren();
        for (AstNode child : new ArrayList<AstNode>(childrenList)) {
            removeChild(child);
        }
    }

    private synchronized void initChildren() {
        if (children == null) {
            children = new LinkedList<Element>();
        }
    }

    void setAttribute(AstAttribute attr) {
        if (attributes == null) {
            attributes = new HashMap<String, Attribute>();
        }
        attributes.put(attr.name(), attr);
    }

    @Override
    public Attribute getAttribute(String attributeName) {
        return attributes == null ? null : attributes.get(attributeName);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        //basic info
        boolean isTag = type() == ElementType.OPEN_TAG || type() == ElementType.CLOSE_TAG;

        if (isTag) {
            b.append(type() == ElementType.OPEN_TAG ? "<" : "");
            b.append(type() == ElementType.CLOSE_TAG ? "</" : "");
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
            b.append(from());
            b.append('-');
            b.append(to());
            if (semanticEndOffset != endOffset) {
                b.append('/');
                b.append(semanticEndOffset);
            }
        }
        b.append(')');

        //add dtd element info
        org.netbeans.modules.html.editor.lib.dtd.DTD.Element e = getDTDElement();
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
        for (Attribute a : attributes()) {
            b.append(a.toString());
            b.append(',');
        }
        b.append('}');

        //attched messages
        for (ProblemDescription d : problems()) {
            b.append(d.getKey());
            b.append(' ');
        }

        //dump stack if possible
        if (stack!= null) {
            b.append(";S:");
            for (CharSequence item : stack) {
                b.append(item);
                b.append(',');
            }
            b.deleteCharAt(b.length() - 1);
        }

        if (!problems().isEmpty()) {
            b.append("; issues:");
            for (ProblemDescription d : problems()) {
                b.append(d);
            }
        }


        return b.toString();
    }

    String signature() {
        return name() + "[" + type() + "]";
    }

    @Override
    public Node parent() {
        return parent;
    }

    private void setParent(Node p) {
        this.parent = p;
    }

    @Override
    public Collection<Element> children(ElementType type) {
        Collection<Element> filtered = new ArrayList<Element>();
        for (Element child : children()) {
            if (child.type() == type) {
                filtered.add(child);
            }
        }
        return filtered;
    }

    @Override
    public Collection<Attribute> attributes() {
        return attributes == null ? Collections.EMPTY_LIST : attributes.values();
    }

    @Override
    public Collection<Attribute> attributes(AttributeFilter filter) {
        Collection<Attribute> attrs = new ArrayList<Attribute>();
        for (Attribute a : attributes()) {
            if (filter.accepts(a)) {
                attrs.add(a);
            }
        }
        return attrs;
    }

    @Override
    public CloseTag matchingCloseTag() {
        return (CloseTag) getMatchingTag();
    }

    @Override
    public CharSequence namespacePrefix() {
        int colonIndex = CharSequences.indexOf(name(), ":");
        return colonIndex == -1 ? null : name().subSequence(0, colonIndex);

    }

    @Override
    public CharSequence unqualifiedName() {
        int colonIndex = CharSequences.indexOf(name(), ":");
        return colonIndex == -1 ? name() : name().subSequence(colonIndex + 1, name().length());
    }

    @Override
    public <T extends Element> Collection<T> children(Class<T> type) {
        Collection<T> filtered = new ArrayList<T>();
        for (Element child : children()) {
            if (type.isAssignableFrom(child.getClass())) {
                filtered.add(type.cast(child));
            }
        }
        return filtered;
    }
}

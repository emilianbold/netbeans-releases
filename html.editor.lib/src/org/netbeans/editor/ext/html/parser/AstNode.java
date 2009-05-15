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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Content;
import org.netbeans.editor.ext.html.dtd.DTD.ContentModel;
import org.netbeans.editor.ext.html.dtd.DTD.Element;

/**
 *
 * @author Tomasz.Slota@Sun.COM, mfukala@netbeans.org
 */
public class AstNode {

    public enum NodeType {COMMENT, DECLARATION, ERROR,
        TEXT, TAG, UNMATCHED_TAG, OPEN_TAG, ENDTAG, ENTITY_REFERENCE};

    private String name;
    private NodeType nodeType;
    private int startOffset;
    private int endOffset;
    private List<AstNode> children = null;
    private AstNode parent = null;
    private Map<String, Object> attributes = null;
    private Content content = null;
    private ContentModel contentModel = null;
    private Collection<Description> descriptions = null;

    AstNode(String name, NodeType nodeType, int startOffset, int endOffset, ContentModel contentModel) {
        this(name, nodeType, startOffset, endOffset);
        this.contentModel = contentModel;
        this.content = contentModel != null ? contentModel.getContent() : null;
    }

    AstNode(String name, NodeType nodeType, int startOffset, int endOffset) {
        this.name = name;
        this.nodeType = nodeType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    boolean isDTDTag() {
        return contentModel != null;
    }

    boolean reduce(Element element) {
        if(contentModel == null) {
            return false; //unknown tag can contain anything, error reports done somewhere else
        }

        //explicitly exluded or included elements doesn't affect the reduction!
        if(contentModel.getExcludes().contains(element)) {
            return false;
        }
        if(contentModel.getIncludes().contains(element)) {
            return true;
        }
        Content c = content.reduce(element.getName());
        if(c != null) {
            content = c;
            return true;
        } else {
            //hack!?!?!!
            //nothing reduced, it still may be valid of one of the expected elements
            //has optional start && end
            for(Object o : contentModel.getContent().getPossibleElements()) {
                Element e = (Element)o;
                if(e != null && e.hasOptionalStart() && e.hasOptionalEnd()) {
                    //try to reduce here
                    Content c2 = e.getContentModel().getContent().reduce(element.getName());
                    if(c2 != null) {
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
        if(content == null) {
            return false;
        }
        //CDATA
        if(content instanceof DTD.ContentLeaf) {
            if( "CDATA".equals(((DTD.ContentLeaf)content).getElementName()) ) {
                return true;
            }
        }

        //#PCDATA hack
        if(content.getPossibleElements().size() == 1) {
            if(content.getPossibleElements().iterator().next() == null) {
                //#PCDATA - consider resolved
                return true;
            }
        }

        return content == Content.EMPTY_CONTENT || content.isDiscardable(); //XXX: is that correct???
    }

    Collection<Element> getUnresolvedElements() {
        if(!isResolved()) {
            return (Collection<Element>)content.getPossibleElements();
        } else {
            return null;
        }
    }

    Collection<Element> getAllPossibleElements() {
        if(content == null) {
            return null;
        } else {
            Collection<Element> col = new ArrayList<Element>();
            col.addAll((Collection<Element>)content.getPossibleElements());
            col.addAll(contentModel.getIncludes());
            col.removeAll(contentModel.getExcludes());
            return col;
        }
    }

    public synchronized void addDescriptionToNode(String key, String message, int type) {
        addDescription(Description.create(key, message, type, startOffset(), endOffset()));
    }

    public synchronized void addDescriptionsToNode(Collection<String[]> keys_messages, int type) {
        for(String[] msg : keys_messages) {
            addDescriptionToNode(msg[0], msg[1], type);
        }
    }

    public synchronized void addDescription(Description message) {
        if(descriptions == null) {
            descriptions = new ArrayList<Description>(2);
        }
        descriptions.add(message);
    }

    public synchronized void addDescriptions(Collection<Description> messages) {
        if(descriptions == null) {
            descriptions = new HashSet<Description>(2);
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

    public List<AstNode> children() {
        return children == null ? Collections.EMPTY_LIST : children;
    }

    void addChild(AstNode child) {
        if (children == null) {
            children = new LinkedList<AstNode>();
        }
        children.add(child);
        child.setParent(this);
    }

    void markUnmatched(){
        nodeType = NodeType.UNMATCHED_TAG;
    }

    void setAttribute(String key, Object value) {
        if(attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes == null ? null : attributes.get(key);
    }

    @Override
    public String toString() {
        StringBuilder childrenText = new StringBuilder();

        for (AstNode child : children()){
            String childTxt = child.toString();

            for (String line : childTxt.split("\n")){
                childrenText.append("-");
                childrenText.append(line);
                childrenText.append('\n');
            }
        }

        return name() + ":" + type() + "<" + startOffset() + ","
                + endOffset() + ">\n" + childrenText.toString();
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

    void removeTagChildren(){
        for (Iterator<AstNode> it = children().iterator(); it.hasNext();){
            if (it.next().isTagNode()){
                it.remove();
            }
        }
    }

    boolean isTagNode(){
        return type() == NodeType.TAG || type() == NodeType.UNMATCHED_TAG;
    }

    private void setParent(AstNode parent) {
        this.parent = parent;
    }

    void setEndOffset(int endOffset){
        this.endOffset = endOffset;
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

}

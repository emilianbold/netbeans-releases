/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import nu.validator.htmlparser.common.TokenizerState;
import nu.validator.htmlparser.common.TokenizerStateListener;
import nu.validator.htmlparser.impl.CoalescingTreeBuilder;
import nu.validator.htmlparser.impl.ElementName;
import nu.validator.htmlparser.impl.HtmlAttributes;
import nu.validator.htmlparser.impl.TreeBuilder;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.AstNodeFactory;
import org.xml.sax.SAXException;

/**
 * An implementation of {@link TreeBuilder} building a tree of {@link AstNode}s
 *
 * In contrary to the default implementations of the {@link TreeBuilder} this
 * builder also puts end tags to the tree of nodes.
 *
 * @author marekfukala
 */
public class AstNodeTreeBuilder extends CoalescingTreeBuilder<AstNode> implements TokenizerState, TokenizerStateListener {

    public static boolean DEBUG = false;
    public static boolean DEBUG_STATES = false;

    private final AstNodeFactory factory;
    private AstNode root;

    //element's internall offsets
    private int tag_lt_offset;
    private int tag_gt_offset;
    
    private boolean self_closing_starttag;

    //stack of opened tags
    private Stack<AstNode> stack = new Stack<AstNode>();

    //stack of encountered end tags
    LinkedList<AstNode> physicalEndTagsQueue = new LinkedList<AstNode>();

    private ElementName startTag;

    //holds found attributes of an open tag
    private Stack<AttrInfo> attrs = new Stack<AttrInfo>();

    public AstNodeTreeBuilder() {
        factory = AstNodeFactory.instance();
    }

    public AstNode getRoot() {
        return root;
    }

    public AstNode getCurrentNode() {
        return stack.peek();
    }
    
    @Override
    protected void elementPopped(String namespace, String name, AstNode t) throws SAXException {
        if (DEBUG) {
            System.out.println("-" + t + (t.isVirtual() ? "[virtual]" : "") +  "; stack:" + dumpStack());
        }
        
        AstNode top = stack.pop();
        assert top == t;

        //try if the first end tag in the queue matches to the popped node
        AstNode node = physicalEndTagsQueue.peekLast();
        if (node != null) {
            if (node.name().equals(t.name())) {
                //it seems that the end tag corresponds to the popped node
                
                //remove the end tag from the queue
                physicalEndTagsQueue.pollLast();
                
                //add the end tag node to its parent
                if(!stack.isEmpty()) {
                    stack.peek().addChild(node);
                }

                //set matching node
                t.setMatchingNode(node);
                node.setMatchingNode(t);

                //set logical end of the paired open tag
                t.setLogicalEndOffset(node.endOffset());

            } else {
                //set logical range of the current open tag node to the beginning of the current open tag node
                t.setLogicalEndOffset(node.startOffset());
            }
        }

        if(stack.size() == 1 /* only root tag in the stack */ && !physicalEndTagsQueue.isEmpty()) {
            //there are no nodes on the stack, but there are some physical endtags left
            if(DEBUG) {
                System.out.println("LEFT in stack of end tags: " + dumpEndTags());
            }
            //attach all the stray end tags to the currently popped node
            for(ListIterator<AstNode> leftEndTags = physicalEndTagsQueue.listIterator(); leftEndTags.hasNext(); ) {
                AstNode left = leftEndTags.next();
                t.addChild(left);
                leftEndTags.remove();
            }


        }

        super.elementPopped(namespace, name, t);
    }

    @Override
    protected void elementPushed(String namespace, String name, AstNode t) throws SAXException {

        if (DEBUG) {
            System.out.println("+" + t + "; stack:" + dumpStack());
        }

        stack.push(t);

        //>>>experimental only!!!
        t.elementName = startTag;
        //<<<

        //stray end tags - add them to the current node
        AstNode head;
        while ((head = physicalEndTagsQueue.poll()) != null) {
            stack.peek().addChild(head);
        }
        
        super.elementPushed(namespace, name, t);
    }

    private String dumpStack() {
        return collectionOfNodesToString(stack);
    }

    private String dumpEndTags() {
        return collectionOfNodesToString(physicalEndTagsQueue);
    }
    
    private String collectionOfNodesToString(Collection<AstNode> col) {
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (Iterator<AstNode> i = col.iterator(); i.hasNext();) {
            AstNode en = i.next();
            b.append(en.name());
            b.append(", ");
        }
        b.append(']');
        return b.toString();
        
    }

    @Override
    public void stateChanged(int from, int to, int offset) {
        if(DEBUG_STATES) {
            System.out.println(STATE_NAMES[from] + " -> " + STATE_NAMES[to] + " at " + offset);
        }

        switch (to) {
            case TAG_OPEN:
                tag_lt_offset = offset;
                break;

            case CLOSE_TAG_OPEN_NOT_PCDATA:
                switch(from) {
                    case TAG_OPEN_NON_PCDATA:
                        //close tag in CDATA (e.g. </style> tag after embedded stylesheet)
                        tag_lt_offset = offset - 1; //the state transition happens after <'/' char found
                }

            case DATA:
                switch (from) {
                    case SELF_CLOSING_START_TAG:
                        self_closing_starttag = true;
                    case ATTRIBUTE_NAME:
                    case AFTER_ATTRIBUTE_VALUE_QUOTED:
                    case AFTER_ATTRIBUTE_NAME:
                    case TAG_NAME:
                    case BEFORE_ATTRIBUTE_NAME:
                    case BEFORE_ATTRIBUTE_VALUE:
                    case ATTRIBUTE_VALUE_UNQUOTED:
                    case CLOSE_TAG_OPEN_NOT_PCDATA:
                        tag_gt_offset = offset;
                        break;

                }
                break;

            case ATTRIBUTE_NAME:
                switch(from) {
                    case BEFORE_ATTRIBUTE_NAME:
                        //switching to attribute name
                        AttrInfo ainfo = new AttrInfo();
                        attrs.push(ainfo);
                        ainfo.nameOffset = offset;
                        break;
                }
                break;

            case BEFORE_ATTRIBUTE_VALUE:
                switch(from) {
                    case ATTRIBUTE_NAME:
                        attrs.peek().equalSignOffset = offset;
                         break;
                }
                break;

            case ATTRIBUTE_VALUE_DOUBLE_QUOTED:
                attrs.peek().valueQuotationType = AttrInfo.ValueQuotation.DOUBLE;
                attrs.peek().valueOffset = offset;
                break;
            case ATTRIBUTE_VALUE_SINGLE_QUOTED:
                attrs.peek().valueQuotationType = AttrInfo.ValueQuotation.SINGLE;
                attrs.peek().valueOffset = offset;
                break;
            case ATTRIBUTE_VALUE_UNQUOTED:
                attrs.peek().valueQuotationType = AttrInfo.ValueQuotation.NONE;
                attrs.peek().valueOffset = offset;
                break;
                
        }
    }

    @Override
    public void startTag(ElementName en, HtmlAttributes ha, boolean bln) throws SAXException {
        if (DEBUG) {
            System.out.println("startTag " + en.name + "(" + tagBeginningOffset() + " - " + tagEndOffset() + ")");
        }

        startTag = en;
        super.startTag(en, ha, bln);

        //>>>experimental only!!!!!!!!!
        AstNode last = stack.peek();
        last.treeBuilderState = mode;
        //<<<


        startTag = null;
    }

    @Override
    public void endTag(ElementName en) throws SAXException {
        if (DEBUG) {
            System.out.print("endTag " + en.name + "(" + tagBeginningOffset() + " - " + tagEndOffset() + ")");
        }
        
        physicalEndTagsQueue.add(factory.createEndTag(en.name, tagBeginningOffset(), tagEndOffset()));

        if(DEBUG) {
            System.out.println("end tags: " + dumpEndTags());

        }

        super.endTag(en);
    }

    public  int tagBeginningOffset() {
        return tag_lt_offset;
    }

    public  int tagEndOffset() {
        return tag_gt_offset + 1 /* 1 == the '>' length */;
    }

    private void resetIntenallPositions() {
        tag_gt_offset = -1;
        tag_lt_offset = -1;
        self_closing_starttag = false;
        attrs.clear();
    }

    @Override
    protected void appendCharacters(AstNode t, String string) throws SAXException {
        //no-op
    }

    @Override
    protected void appendComment(AstNode t, String string) throws SAXException {
        //no-op
    }

    @Override
    protected void appendCommentToDocument(String string) throws SAXException {
        //no-op
    }

    @Override
    protected void insertFosterParentedCharacters(String string, AstNode t, AstNode t1) throws SAXException {
        //???????
    }

    @Override
    protected AstNode createElement(String namespace, String name, HtmlAttributes attributes) throws SAXException {

        if (DEBUG) {
            System.out.println("ns=" + namespace);
        }
        AstNode node;
        if (startTag != null && startTag.name.equals(name)) {
            node = factory.createOpenTag(name, tag_lt_offset, tag_gt_offset + 1, self_closing_starttag);
            addAttributesToElement(node, attributes);
            resetIntenallPositions();

        } else {
            //virtual element
            node = factory.createOpenTag(name, -1, -1, false);
            addAttributesToElement(node, attributes);
        }

        return node;
    }

    @Override
    protected AstNode createHtmlElementSetAsRoot(HtmlAttributes attributes) throws SAXException {
        if(DEBUG) {
            System.out.println("+HTML ROOT");
        }
        
        root = factory.createRootNode();

        AstNode rootTag = createElement("http://www.w3.org/1999/xhtml", "html", attributes);
        stack.push(root);

        root.addChild(rootTag);
        
        return rootTag;
    }

    @Override
    protected void detachFromParent(AstNode node) throws SAXException {
        node.detachFromParent();
    }

    @Override
    protected boolean hasChildren(AstNode node) throws SAXException {
        return !node.children().isEmpty();
    }

    @Override
    protected void appendElement(AstNode child, AstNode parent) throws SAXException {
        parent.addChild(child);
    }

    @Override
    //move node's children to another node
    protected void appendChildrenToNewParent(AstNode from, AstNode to) throws SAXException {
        List<AstNode> children = from.children();
        from.removeChildren(children);
        to.addChildren(children);
    }

    @Override
    protected void insertFosterParentedChild(AstNode t, AstNode t1, AstNode t2) throws SAXException {
        assert false : "lets see when this is called";
    }

    @Override
    protected void addAttributesToElement(AstNode node, HtmlAttributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            //XXX I assume the attributes order is the same as in the source code
            AttrInfo attrInfo = attrs.elementAt(i);
            AstNode.Attribute attr = factory.createAttribute(
                    attributes.getLocalName(i),
                    attributes.getValue(i),
                    attrInfo.nameOffset,
                    attrInfo.valueOffset);

            node.setAttribute(attr);
        }
    }

    private static class AttrInfo {
        public int nameOffset, equalSignOffset, valueOffset;
        public ValueQuotation valueQuotationType;
        private enum ValueQuotation {
            NONE, SINGLE, DOUBLE;
        }
    }
    
}

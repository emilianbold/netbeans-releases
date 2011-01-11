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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import nu.validator.htmlparser.common.TransitionHandler;
import nu.validator.htmlparser.impl.CoalescingTreeBuilder;
import nu.validator.htmlparser.impl.ElementName;
import nu.validator.htmlparser.impl.HtmlAttributes;
import static nu.validator.htmlparser.impl.Tokenizer.*;
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
public class AstNodeTreeBuilder extends CoalescingTreeBuilder<AstNode> implements TransitionHandler {

    static final Logger LOGGER = Logger.getLogger(AstNodeTreeBuilder.class.getName());
    static boolean LOG, LOG_FINER;

    static {
        initLogLevels();
    }

    private static void initLogLevels() {
        LOG = LOGGER.isLoggable(Level.FINE);
        LOG_FINER = LOGGER.isLoggable(Level.FINER);
    }

    private final AstNodeFactory factory;
    private AstNode root;
    //element's internall offsets
    private int offset;
    private int tag_lt_offset;
    private boolean self_closing_starttag;
    //stack of opened tags
    private Stack<AstNode> stack = new Stack<AstNode>();
    //stack of encountered end tags
    LinkedList<AstNode> physicalEndTagsQueue = new LinkedList<AstNode>();
    private ElementName startTag;
    //holds found attributes of an open tag
    private Stack<AttrInfo> attrs = new Stack<AttrInfo>();

    private AstNode currentTag;

    public AstNodeTreeBuilder(AstNode rootNode) {
        this.root = rootNode;
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
        if (LOG) {
            LOGGER.fine(String.format("- %s %s; stack: %s", t, t.isVirtual() ? "[virtual]" : "", dumpStack())); //NOI18N
        }


        //normally the stack.pop() == t, but under some circumstances when the code is broken
        //this doesn't need to be true. In such case drop all the nodes from top
        //of the stack until we find t node.
        AstNode top = null;
        Stack<AstNode> removedFromStack = new Stack<AstNode>();
        while(!stack.isEmpty()) {
            top = stack.pop();
            removedFromStack.push(top);
            if(top == t) {
                break;
            }
        }
        if(t != top) {
            //weird, there doesn't seem to be the 't' node pushed
            //better put all the removed nodes back to the stack
            LOGGER.info(String.format("The node %s has been popped but not previously pushed!", t));
            while(!removedFromStack.isEmpty()) {
                stack.push(removedFromStack.pop());
            }
        }

        assert !stack.isEmpty();

        AstNode match = null;
        for (AstNode n : physicalEndTagsQueue) {
            if (n.name().equals(t.name())) {
                match = n;
                break;
            }
        }

        if (match != null) {
            //remove all until the found element
            List<AstNode> toremove = physicalEndTagsQueue.subList(0, physicalEndTagsQueue.indexOf(match) + 1);

            if (toremove.size() > 1) {
                //there are some stray end tags, add them to the current open tag node
                for (AstNode n : toremove.subList(0, toremove.size() - 1)) {
                    t.addChild(n);
                }
            }
            //and remove all the end tags
            toremove.clear();

            //add the end tag node to its parent
            if (!stack.isEmpty()) {
                stack.peek().addChild(match);
            }

            //set matching node
            t.setMatchingNode(match);
            match.setMatchingNode(t);

            //set logical end of the paired open tag
            t.setLogicalEndOffset(match.endOffset());
        } else {
            //no match found, the open tag node's logical range should be set to something meaningful -
            //to the latest end tag found likely causing this element to be popped
            AstNode latestEndTag = physicalEndTagsQueue.peek();
            if(latestEndTag != null) {
                t.setLogicalEndOffset(latestEndTag.startOffset());
            } else if(startTag != null) {
                //or to an open tag which implies this tag to be closed
                t.setLogicalEndOffset(tag_lt_offset);
            } else {
                //the rest - simply current token offset
                t.setLogicalEndOffset(offset);
            }

        }

        if (stack.size() == 1 /* only root tag in the stack */ && !physicalEndTagsQueue.isEmpty()) {
            //there are no nodes on the stack, but there are some physical endtags left
            if (LOG) {
                LOGGER.fine(String.format("LEFT in stack of end tags: %s", dumpEndTags()));//NOI18N
            }
            //attach all the stray end tags to the currently popped node
            for (ListIterator<AstNode> leftEndTags = physicalEndTagsQueue.listIterator(); leftEndTags.hasNext();) {
                AstNode left = leftEndTags.next();
                t.addChild(left);
                leftEndTags.remove();
            }


        }

        super.elementPopped(namespace, name, t);
    }

    @Override
    protected void elementPushed(String namespace, String name, AstNode t) throws SAXException {

        if (LOG) {
            LOGGER.fine(String.format("+ %s %s; stack: %s", t, t.isVirtual() ? "[virtual]" : "", dumpStack())); //NOI18N
        }

        stack.push(t);

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

    public void transition(int from, int to, boolean reconsume, int offset) throws SAXException {
        if (LOG_FINER) {
            LOGGER.finer(String.format("%s -> %s at %s", Util.TOKENIZER_STATE_NAMES[from], Util.TOKENIZER_STATE_NAMES[to], offset));//NOI18N
        }
        this.offset = offset;
        int tag_gt_offset = -1;
        switch (to) {
            case TAG_OPEN:
                tag_lt_offset = offset;
                break;

            case NON_DATA_END_TAG_NAME:
                if(from == RAWTEXT_RCDATA_LESS_THAN_SIGN
                        || from == SCRIPT_DATA_LESS_THAN_SIGN) {
                    //end tag in RAW text (like <title> content)
                    tag_lt_offset = offset - 1; //-1 is here because we are already at the tag name just after the &lt; char
                }
                break;

            case RAWTEXT:
                //strange transition happening at the closing > char at the tag end:
                //<style type=\"text/css\"> 
                if(from == AFTER_ATTRIBUTE_VALUE_QUOTED) {
                    tag_gt_offset = offset;
                }
                break;

            case RCDATA:
            case SCRIPT_DATA:
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
                    case NON_DATA_END_TAG_NAME:
                        //+1 ... add the > char itself
                        tag_gt_offset = offset + 1;
                        break;

                }
                break;

            case ATTRIBUTE_NAME:
                switch (from) {
                    case BEFORE_ATTRIBUTE_NAME:
                        //switching to attribute name
                        AttrInfo ainfo = new AttrInfo();
                        attrs.push(ainfo);
                        ainfo.nameOffset = offset;
                        break;
                }
                break;

            case BEFORE_ATTRIBUTE_VALUE:
                switch (from) {
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

        //set the current tag end offset:
        //the transition for the closing tag symbol are done AFTER the element for the tag is created,
        //so it needs to be additionaly set to the latest element
        if(tag_gt_offset != -1 && currentTag != null) {
            currentTag.setEndOffset(tag_gt_offset);
            currentTag.setLogicalEndOffset(tag_gt_offset);

            //refresh the matching open tag's logical end offset
            if(currentTag.type() == AstNode.NodeType.ENDTAG) {
                AstNode pair = currentTag.getMatchingTag();
                if(pair != null) {
                    pair.setLogicalEndOffset(tag_gt_offset);
                }
            }

            currentTag = null;
            tag_gt_offset = -1;
        }

    }

    @Override
    public void startTag(ElementName en, HtmlAttributes ha, boolean bln) throws SAXException {
        if (LOG) {
            LOGGER.fine(String.format("open tag %s at %s", en.name, tag_lt_offset));//NOI18N
        }
        
        startTag = en;
        super.startTag(en, ha, bln);
        startTag = null;
    }

    @Override
    public void endTag(ElementName en) throws SAXException {
        if (LOG) {
            LOGGER.fine(String.format("close tag %s at %s", en.name, tag_lt_offset));//NOI18N
        }

        physicalEndTagsQueue.add(currentTag = factory.createEndTag(en.name, tag_lt_offset, -1));

        if (LOG) {
            LOGGER.fine(String.format("end tags: %s", dumpEndTags()));//NOI18N
        }

        super.endTag(en);
    }

    private void resetIntenallPositions() {
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
        if(LOG) {
            LOGGER.fine(String.format("createElement(%s)", name));//NOI18N
        }

        AstNode node;
        if (startTag != null && startTag.name.equals(name)) {
            currentTag = node = factory.createOpenTag(name, tag_lt_offset, -1, self_closing_starttag);
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
        if (LOG) {
            LOGGER.fine("createHtmlElementSetAsRoot()");//NOI18N
        }

        AstNode rootTag = createElement("http://www.w3.org/1999/xhtml", "html", attributes);//NOI18N
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

    //http://www.whatwg.org/specs/web-apps/current-work/multipage/tokenization.html#foster-parenting
    @Override
    protected void insertFosterParentedChild(AstNode child, AstNode table, AstNode stackParent) throws SAXException {
        AstNode parent = table.parent();
        if (parent != null) { // always an element if not null
            parent.insertBefore(child, table);
        } else {
            stackParent.addChild(child);
        }
    }

    @Override
    protected void addAttributesToElement(AstNode node, HtmlAttributes attributes) throws SAXException {
        //there are situations (when the code is corrupted) when 
        //the attributes recorded during lexing (lexical states switching)
        //do not contain all the attrs from HtmlAttributes.
        int attrs_count = Math.min(attributes.getLength(), attrs.size());

        for (int i = 0; i < attrs_count; i++) {
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

    //for unit tests
    static void setLoggerLevel(Level level) {
        LOGGER.setLevel(level);
        LOGGER.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                System.err.println(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }

        });
        initLogLevels();
    }

    private static class AttrInfo {

        public int nameOffset, equalSignOffset, valueOffset;
        public ValueQuotation valueQuotationType;

        private enum ValueQuotation {

            NONE, SINGLE, DOUBLE;
        }
    }
}

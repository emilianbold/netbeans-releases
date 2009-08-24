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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.editor.ext.html.parser.AstNode.Description;
import org.netbeans.editor.ext.html.parser.SyntaxElement.TagAttribute;
import org.openide.util.NbBundle;

/**
 * Planar parser result to AST convertor
 *
 * @author mfukala@netbeans.org
 */
public class SyntaxTree {

    //error messages keys, used for unit testing
    static final String UNEXPECTED_TAG_KEY = "unexpected_tag"; //NOI18N
    static final String UNRESOLVED_TAG_KEY = "unresolved_tag"; //NOI18N
    static final String UNKNOWN_TAG_KEY = "unknown_tag"; //NOI18N
    static final String UNKNOWN_ATTRIBUTE_KEY = "unknown_attribute"; //NOI18N
    static final String FORBIDDEN_END_TAG = "forbidded_endtag"; //NOI18N
    static final String UNMATCHED_TAG = "unmatched_tag"; //NOI18N
    static final String MISSING_REQUIRED_END_TAG = "missing_required_end_tag"; //NOI18N
    static final String MISSING_REQUIRED_ATTRIBUTES = "missing_required_attribute"; //NOI18N
    static final String TAG_CANNOT_BE_EMPTY = "tag_cannot_be_empty"; //NOI18N

    public static AstNode makeTree(List<SyntaxElement> elements, DTD dtd) {
        if (dtd == null) {
            return makeUncheckedTree(elements);
        } else {
            return makeCheckedTree(elements, dtd);
        }
    }

    private static AstNode makeUncheckedTree(List<SyntaxElement> elements) {
        assert elements != null : "passed elements list cannot but null"; //NOI18N

        SyntaxElement last = elements.size() > 0 ? elements.get(elements.size() - 1) : null;
        int lastEndOffset = last == null ? 0 : last.offset() + last.length();

        //create a root node, it can contain one or more child nodes
        //normally just <html> node should be its child
        AstNode rootNode = AstNode.createRootNode(0, lastEndOffset, null);
        LinkedList<AstNode> stack = new LinkedList<AstNode>();
        stack.add(rootNode);

        for (SyntaxElement element : elements) {

            if (element.type() == SyntaxElement.TYPE_TAG) { //open tag
                assert element instanceof SyntaxElement.Tag;

                SyntaxElement.Tag tagElement = (SyntaxElement.Tag) element;
                String tagName = tagElement.getName();

                AstNode lastNode = stack.getLast();

                //create an AST node for current element
                AstNode openTagNode = new AstNode(tagName, AstNode.NodeType.OPEN_TAG,
                        tagElement.offset(), tagElement.offset() + tagElement.length(), tagElement.isEmpty());

                //add existing tag attributes
                setTagAttributes(openTagNode, tagElement);

                //possible add the node to the nodes stack
                if (!(tagElement.isEmpty())) {
                    stack.addLast(openTagNode);
                }

                //add the node to its parent
                lastNode.addChild(openTagNode);

            } else if (element.type() == SyntaxElement.TYPE_ENDTAG) { //close tag

                String tagName = ((SyntaxElement.Named) element).getName();

                AstNode closeTagNode = new AstNode(tagName, AstNode.NodeType.ENDTAG,
                        element.offset(), element.offset() + element.length(), false);

                int matched_index = -1;
                for (int i = stack.size() - 1; i >= 0; i--) {
                    AstNode node = stack.get(i);
                    if (tagName.equals(node.name())) {
                        //ok, match
                        matched_index = i;
                        break;
                    }
                }

                assert matched_index != 0; //never match root node, either -1 or > 0

                if (matched_index > 0) {
                    //something matched
                    AstNode match = stack.get(matched_index);

                    //remove them ALL the left elements from the stack
                    for (int i = stack.size() - 1; i > matched_index; i--) {
                        AstNode node = stack.get(i);
                        node.setLogicalEndOffset(closeTagNode.startOffset());
                        stack.remove(i);
                    }

                    //add the node to the proper parent
                    AstNode match_parent = stack.get(matched_index - 1);
                    match_parent.addChild(closeTagNode);

                    //wont' help GS at all, but should be ok
                    match.setMatchingNode(closeTagNode);
                    match.setLogicalEndOffset(closeTagNode.endOffset());
                    closeTagNode.setMatchingNode(match);

                    //remove the matched tag from stack
                    stack.removeLast();

                } else {
                    //add it to the last node
                    stack.getLast().addChild(closeTagNode);
                }

            } else {
                //rest of the syntax element types
                //XXX do we need to have these in the AST???

                // add a new AST node to the last node on the stack
//                AstNode.NodeType nodeType = intToNodeType(element.type());
//
//                AstNode node = new AstNode(null, nodeType, element.offset(),
//                        element.offset() + element.length(), false);
//
//                stack.getLast().addChild(node);
            }
        }

        //check the stack content and resolve left nodes
        for (int i = stack.size() - 1; i > 0; i--) { // (i > 0) == do not process the very first (root) node
            AstNode node = stack.get(i);
            node.setLogicalEndOffset(lastEndOffset);

        }

        return rootNode;
    }

    private static AstNode makeCheckedTree(List<SyntaxElement> elements, DTD dtd) {
        assert elements != null;
        assert dtd != null;

        SyntaxElement last = elements.size() > 0 ? elements.get(elements.size() - 1) : null;
        int lastEndOffset = last == null ? 0 : last.offset() + last.length();

        //create a root node, it can contain one or more child nodes
        //normally just <html> node should be its child
        AstNode rootNode = AstNode.createRootNode(0, lastEndOffset, dtd);
        LinkedList<AstNode> stack = new LinkedList<AstNode>();
        stack.add(rootNode);

        for (SyntaxElement element : elements) {

            if (element.type() == SyntaxElement.TYPE_TAG) { //open tag
                assert element instanceof SyntaxElement.Tag;

                SyntaxElement.Tag tagElement = (SyntaxElement.Tag) element;
                String tagName = tagElement.getName();

                AstNode lastNode = stack.getLast();

                //try to find DTD element for the current node
                Element currentNodeDtdElement = dtd.getElement(tagName);

                if (currentNodeDtdElement == null) {
                    //TODO: keep the unknown nodes in a separate tree under the
                    //parser result so features like tag matching can work but
                    //the pure HTML tree is not affected

                    //no DTD tag, just mark as unknown and add it as a child of current stack's top node
                    AstNode unknownTagNode = new AstNode(tagName, AstNode.NodeType.UNKNOWN_TAG,
                            tagElement.offset(), tagElement.offset() + tagElement.length(), tagElement.isEmpty());

                    //ignore namespaced tags, they won't be matched, but without errors
                    if (!isIgnoredTagName(tagName)) {
                        String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNKNOWN_TAG", //NOI18N
                                new Object[]{tagName});

                        unknownTagNode.addDescriptionToNode(UNKNOWN_TAG_KEY, errorMessage, AstNode.Description.WARNING);
                    }

                    lastNode.addChild(unknownTagNode);

                    continue; //process next syntax element

                }


                //create an AST node for current element

                AstNode openTagNode = new AstNode(tagName, AstNode.NodeType.OPEN_TAG,
                        tagElement.offset(), tagElement.offset() + tagElement.length(),
                        currentNodeDtdElement, tagElement.isEmpty(), stack(stack));

//                //check if the tag can be empty
//                if(tagElement.isEmpty() && !currentNodeDtdElement.isEmpty()) {
//                    //the tag is empty, but cannot be, mark error
//                    openTagNode.addDescriptionToNode(TAG_CANNOT_BE_EMPTY, NbBundle.getMessage(SyntaxTree.class, "MSG_TAG_CANNOT_BE_EMPTY"), Description.ERROR);
//                }

                //check tag attributes
                checkTagAttributes(openTagNode, (SyntaxElement.Tag) tagElement, currentNodeDtdElement);

                //add existing tag attributes
                setTagAttributes(openTagNode, tagElement);

                //check if the last open tag allows this tag as its content, do not do that for root node
                if (lastNode != rootNode && !lastNode.reduce(currentNodeDtdElement)) {
                    //current node cannot be present inside its parent

                    if (!lastNode.isResolved()) {
                        //the parent node is not resolved we cannot close it
                        //some mandatory content unresolved, report error
                        String expectedElements = elementsToString(lastNode.getAllPossibleElements());
                        openTagNode.addDescriptionToNode(
                                UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTree.class, "MSG_UNEXPECTED_TAG", //NOI18N
                                new Object[]{currentNodeDtdElement.getName(), expectedElements}), Description.ERROR);

                    } else {
                        //the parent node is resolved so can be possibly closed

                        //check if the node we are going to close have required end tag
                        //if so show an error
                        Element lastDtdElement = dtd.getElement(lastNode.name());

                        assert lastDtdElement != null; //only DTD based elements are put into the stack

                        if (!lastDtdElement.hasOptionalEnd()) {
                            //the last node has required end tag => report error
                            Collection<Element> possibleElems = lastNode.getAllPossibleElements();
                            if (possibleElems.isEmpty()) {
                                openTagNode.addDescriptionToNode(UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTree.class, "MSG_UNEXPECTED_TAG_NO_EXPECTED_CONTENT", //NOI18N
                                        new Object[]{currentNodeDtdElement.getName()}), Description.ERROR);
                            } else {
                                String expectedElements = elementsToString(possibleElems);
                                openTagNode.addDescriptionToNode(
                                        UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTree.class, "MSG_UNEXPECTED_TAG", //NOI18N
                                        new Object[]{currentNodeDtdElement.getName(), expectedElements}), Description.ERROR);
                            }

                        } else {
                            //the last node has optional end tag, can be closed

                            /* We need to try if the current element is resolved by one of the nodes in the stack
                            because of the optional end tag of the last node
                            sample:

                            <html>
                            <head>
                            <title>
                            </title>
                            <body> -- current element, last element is head
                            </body>
                            </html>

                            In the example above the last element == <head> with optional end
                            so it can be closed by <body> start element

                            In general we need to remove all elements from the top to down
                            of the stack until one reduces the current element. If none of
                            them does this mark current element as error.

                             */


                            int reduce_index = -1;
                            for (int i = stack.size() - 1; i >
                                    0; i--) {
                                AstNode node = stack.get(i);
                                //node can be possibly closed by this tag
                                if (node.reduce(currentNodeDtdElement)) {
                                    //this node reduces this current element
                                    reduce_index = i;
                                    break;

                                }


                            }

                            if (reduce_index == -1) {
                                //no reduce
                                //workaround
                                stack.remove(lastNode);
                                lastNode.setLogicalEndOffset(openTagNode.startOffset());

                                lastNode =
                                        stack.getLast();
                                lastNode.addChild(openTagNode);


                                //mark the error only if the parent node is not root
                                if (lastNode != rootNode) {
                                    //nothing reduces the current element, error
                                    openTagNode.addDescriptionToNode(UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTree.class, "MSG_UNEXPECTED_TAG_NO_EXPECTED_CONTENT", //NOI18N
                                            new Object[]{currentNodeDtdElement.getName()}), Description.ERROR);
                                } else {
                                    stack.addLast(openTagNode);
                                }

                                continue; //!!!!!!!!!!!!
                            } else {
                                //remove all nodes from stack up to the reducing one if has optional ends
                                //close all nodes up to the one which reduced the current element
                                for (int i = stack.size() - 1; i >
                                        reduce_index; i--) {
                                    AstNode node = stack.get(i);
                                    if (hasOptionalEndTag(node)) {
                                        node.setLogicalEndOffset(openTagNode.startOffset());
                                        stack.remove(i);
                                    } else {
                                        break;
                                    }

                                }
                                lastNode = stack.getLast(); //update lastnode to the new top
                            }

                        }
                    }
                }


                //possible add the node to the nodes stack
                if (!(tagElement.isEmpty() || currentNodeDtdElement.isEmpty())) {
                    //the node is neither empty by declaration nor by definition
                    stack.addLast(openTagNode);
                }

//add the node to its parent
                lastNode.addChild(openTagNode);

            } else if (element.type() == SyntaxElement.TYPE_ENDTAG) { //close tag

                String tagName = ((SyntaxElement.Named) element).getName();

                //test if DTD tag, if not do not try to match
                Element dtdElement = dtd.getElement(tagName);
                if (dtdElement == null) {
                    //no DTD tag, just mark as unknown and add it as a child of current stack's top node
                    AstNode unknownTagNode = new AstNode(tagName, AstNode.NodeType.UNKNOWN_TAG,
                            element.offset(), element.offset() + element.length(), false);

                    //ignore namespaced tags, they won't be matched, but without errors
                    if (!isIgnoredTagName(tagName)) {
                        String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNKNOWN_TAG", //NOI18N
                                new Object[]{tagName});

                        unknownTagNode.addDescriptionToNode(UNKNOWN_TAG_KEY, errorMessage, AstNode.Description.WARNING);
                    }

                    stack.getLast().addChild(unknownTagNode);

                    continue; //process next syntax element

                }



                AstNode closeTagNode = new AstNode(tagName, AstNode.NodeType.ENDTAG,
                        element.offset(), element.offset() + element.length(), dtdElement, false, stack(stack));

                int matched_index = -1;
                for (int i = stack.size() - 1; i >=
                        0; i--) {
                    AstNode node = stack.get(i);
                    if (tagName.equals(node.name())) {
                        //found the matching open tag, maybe
                        //check if the matching open tag has forbidden end tag
                        if (AstNodeUtils.hasForbiddenEndTag(node)) {
                            //cannot match, report error
                            closeTagNode.addDescriptionToNode(FORBIDDEN_END_TAG, NbBundle.getMessage(SyntaxTree.class, "MSG_FORBIDDEN_ENDTAG"), Description.ERROR); //NOI18N
                        } else {
                            //ok, match
                            matched_index = i;
                            break;

                        }


                    }
                }

                assert matched_index != 0; //never match root node, either -1 or > 0

                if (matched_index > 0) {
                    //something matched
                    AstNode match = stack.get(matched_index);

                    if (matched_index != stack.size() - 1) {
                        //some tags are skipped, needs to be resolved

                        //go through all the left stacked tags and try to
                        //check if they can be closed or not
                        for (int i = stack.size() - 1; i >
                                matched_index; i--) {
                            AstNode node = stack.get(i);

                            if (!node.isResolved()) {
                                //unresolved content
                                String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNRESOLVED_TAG", //NOI18N
                                        new Object[]{elementsToString(node.getAllPossibleElements())});

                                node.addDescriptionToNode(UNRESOLVED_TAG_KEY, errorMessage, Description.ERROR);
                            }






                            if (!hasOptionalEndTag(node)) {
                                //missing end tag
                                String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_MISSING_REQUIRED_END_TAG"); //NOI18N
                                node.addDescriptionToNode(MISSING_REQUIRED_END_TAG, errorMessage, Description.ERROR);
                            }
                        }

//remove them ALL the left elements from the stack
                        for (int i = stack.size() - 1; i >
                                matched_index; i--) {
                            AstNode node = stack.get(i);
                            node.setLogicalEndOffset(closeTagNode.startOffset());
                            stack.remove(i);
                        }




                    }

//verify the matched tag:
//check if the tag content is resolved (only for html tags)
                    if (!match.isResolved()) {
                        //some mandatory content unresolved, report error to the open tag
                        String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNRESOLVED_TAG", //NOI18N
                                new Object[]{elementsToString(match.getAllPossibleElements())});

                        match.addDescriptionToNode(UNRESOLVED_TAG_KEY, errorMessage, Description.ERROR);

                    }

//add the node to the proper parent
                    AstNode match_parent = stack.get(matched_index - 1);
                    match_parent.addChild(closeTagNode);

                    //wont' help GS at all, but should be ok
                    match.setMatchingNode(closeTagNode);
                    match.setLogicalEndOffset(closeTagNode.endOffset());
                    closeTagNode.setMatchingNode(match);

                    //remove the matched tag from stack
                    stack.removeLast();

                } else {
                    //no match, mark as unmatched is has required start tag
                    if (!dtdElement.hasOptionalStart()) {
                        String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNMATCHED_TAG"); //NOI18N
                        closeTagNode.addDescriptionToNode(UNMATCHED_TAG, errorMessage, Description.WARNING);
                    }

                    //add it to the last node
                    stack.getLast().addChild(closeTagNode);
                }

            } else {
                //rest of the syntax element types
                //XXX do we need to have these in the AST???

                // add a new AST node to the last node on the stack
                AstNode.NodeType nodeType = intToNodeType(element.type());

                AstNode node = new AstNode(null, nodeType, element.offset(),
                        element.offset() + element.length(), false);

                stack.getLast().addChild(node);
            }

        }

        //check the stack content and resolve left nodes
        for (int i = stack.size() - 1; i >
                0; i--) { // (i > 0) == do not process the very first (root) node
            AstNode node = stack.get(i);

            boolean nodeOk = true;

            if (!hasOptionalEndTag(node)) {
                //unclosed tag, mark
                String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNMATCHED_TAG"); //NOI18N
                node.addDescriptionToNode(UNMATCHED_TAG, errorMessage, Description.WARNING);
                nodeOk = false;
            }

            if (!node.isResolved()) {
                //unresolved, mark
                String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNRESOLVED_TAG", //NOI18N
                        new Object[]{elementsToString(node.getAllPossibleElements())});

                node.addDescriptionToNode(UNRESOLVED_TAG_KEY, errorMessage, Description.ERROR);
                nodeOk =
                        false;
            }

//            if(nodeOk) {
//if the tag is ok, then close it by the end of the file
            node.setLogicalEndOffset(lastEndOffset);
//            }

        }

        return rootNode;
    }

    private static List<String> stack(LinkedList<AstNode> stack) {
        List<String> s = new ArrayList<String>();
        for (AstNode node : stack) {
            s.add(node.name());
        }

        return s;
    }

    private static boolean hasOptionalEndTag(AstNode node) {
        Element e = node.getDTDElement();
        assert e != null;

        return e.hasOptionalEnd();
    }

    private static boolean isIgnoredTagName(String tagName) {
        return tagName.contains(":"); //NOI18N
    }

    private static boolean isIgnoredTagAttribute(String tagName) {
        return tagName.contains(":"); //NOI18N
    }

    private static void checkTagAttributes(AstNode node, SyntaxElement.Tag element, Element dtdElement) {
        //check existing attributes
        List<TagAttribute> existingAttrs = element.getAttributes();
        List<String> existingAttrNames = new ArrayList<String>(existingAttrs.size());
        for (TagAttribute ta : existingAttrs) {
            String tagName = ta.getName().toLowerCase(Locale.ENGLISH);
            existingAttrNames.add(ta.getName().toLowerCase(Locale.ENGLISH));
            DTD.Attribute attr = dtdElement.getAttribute(tagName);
            if (attr == null) {
                if (!isIgnoredTagAttribute(ta.getName())) {
                    //unknown attribute
                    Description desc = Description.create(UNKNOWN_ATTRIBUTE_KEY, NbBundle.getMessage(SyntaxTree.class, "MSG_UNKNOWN_ATTRIBUTE", //NOI18N
                            new Object[]{ta.getName(), element.getName()}), Description.WARNING, ta.getNameOffset(), ta.getNameOffset() + ta.getName().length());

                    node.addDescription(desc);
                }

            }
        }

        //check missing required attributes
        StringBuffer missingAttributesListMsg = new StringBuffer();
        for (Object _attr : dtdElement.getAttributeList(null)) {
            DTD.Attribute attr = (DTD.Attribute) _attr;
            if (attr.isRequired() && !existingAttrNames.contains(attr.getName())) {
                //missing required attribute
                missingAttributesListMsg.append(attr.getName());
                missingAttributesListMsg.append(", ");
            }

        }
        if (missingAttributesListMsg.length() > 0) {
            //cut last comma and space
            missingAttributesListMsg.deleteCharAt(missingAttributesListMsg.length() - 2);
            //attach the error description
            node.addDescriptionToNode(
                    MISSING_REQUIRED_ATTRIBUTES,
                    NbBundle.getMessage(SyntaxTree.class, "MSG_MISSING_REQUIRED_ATTRIBUTES", //NOI18N
                    new Object[]{missingAttributesListMsg.toString()}),
                    Description.WARNING);
        }

    }

    private static void setTagAttributes(AstNode node, SyntaxElement.Tag tag) {
        for (TagAttribute ta : tag.getAttributes()) {
            if (ta != null) {
                node.setAttribute(ta.getName(), dequote(ta.getValue()));
            }

        }
    }

    private static String elementsToString(Collection<Element> elements) {
        StringBuffer b = new StringBuffer();
        for (Element e : elements) {
            b.append('<'); //NOI18N
            b.append(e.getName());
            b.append('>'); //NOI18N
            b.append(", "); //NOI18N
        }

        if (b.length() > 0) {
            //strip last delimiters
            b.delete(b.length() - 2, b.length());
        }

        return b.toString();
    }

    private static AstNode.NodeType intToNodeType(int type) {
        switch (type) {
            case SyntaxElement.TYPE_COMMENT:
                return AstNode.NodeType.COMMENT;
            case SyntaxElement.TYPE_DECLARATION:
                return AstNode.NodeType.DECLARATION;
            case SyntaxElement.TYPE_ENDTAG:
                return AstNode.NodeType.ENDTAG;
            case SyntaxElement.TYPE_ENTITY_REFERENCE:
                return AstNode.NodeType.ENTITY_REFERENCE;
            case SyntaxElement.TYPE_ERROR:
                return AstNode.NodeType.ERROR;
            case SyntaxElement.TYPE_TAG:
                return AstNode.NodeType.OPEN_TAG;
            case SyntaxElement.TYPE_TEXT:
                return AstNode.NodeType.TEXT;
        }

        return null;
    }

    private static String dequote(String text) {
        if (text.length() < 2) {
            return text;
        } else {
            if ((text.charAt(0) == '\'' || text.charAt(0) == '"') &&
                    (text.charAt(text.length() - 1) == '\'' || text.charAt(text.length() - 1) == '"')) {
                return text.substring(1, text.length() - 1);
            }

        }
        return text;
    }
}

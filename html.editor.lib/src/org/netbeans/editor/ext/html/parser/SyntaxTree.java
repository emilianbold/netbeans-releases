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
import org.netbeans.editor.ext.html.dtd.DTD.ContentModel;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.editor.ext.html.parser.AstNode.Description;
import org.netbeans.editor.ext.html.parser.SyntaxElement.TagAttribute;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM, mfukala@netbeans.org
 */
public class SyntaxTree {

    //error messages keys, used for unit testing
    static final String UNEXPECTED_TAG_KEY = "unexpected_tag";
    static final String UNRESOLVED_TAG_KEY = "unresolved_tag";
    static final String UNKNOWN_TAG_KEY = "unknown_tag";
    static final String UNKNOWN_ATTRIBUTE_KEY = "unknown_attribute";
    static final String FORBIDDEN_END_TAG = "forbidded_endtag";
    static final String UNMATCHED_TAG = "unmatched_tag";

    static boolean DEBUG = false; //for unit testing

    public static AstNode makeTree(List<SyntaxElement> elements, DTD dtd) {
        assert elements != null;
        assert dtd != null;

        boolean isXHTML = org.netbeans.editor.ext.html.dtd.Utils.isXHTMLPublicId(dtd.getIdentifier());

        SyntaxElement last = elements.size() > 0 ? elements.get(elements.size() - 1) : null;
        int lastEndOffset = last == null ? 0 : last.offset() + last.length();

        AstNode root = new AstNode("root", null, 0, lastEndOffset);
        LinkedList<AstNode> nodeStack = new LinkedList<AstNode>();
        nodeStack.add(root);

        for (SyntaxElement element : elements) {
            if (element.type() == SyntaxElement.TYPE_TAG) {
                assert element instanceof SyntaxElement.Tag;
                // OPENING TAG
                // create tag node, push it on stack
                // add opening tag node

                String tagName = ((SyntaxElement.Named) element).getName();

                AstNode lastNode = null;
                //find first dtd tag in the stack
                for(int i = nodeStack.size() - 1; i >= 0; i--) {
                    if(nodeStack.get(i).isDTDTag()) {
                        lastNode = nodeStack.get(i);
                        break;
                    }
                }

                if (DEBUG) {
                    System.out.println("--------------------------------");
                    System.out.println("Processing tag " + tagName);
                    System.out.println("Last open tag = " + (lastNode != null ? lastNode.name() : "<NONE>"));
                }

                Element currentNodeDtdElement = dtd.getElement(isXHTML ? tagName : tagName.toUpperCase(Locale.ENGLISH));
                ContentModel contentModel = null;
                Collection<String[]> errorMessages = new ArrayList<String[]>(2);
                Collection<Description> unknownAttributeMessages = new ArrayList<Description>(2);

                //some error checks >>>
                if (currentNodeDtdElement != null) {
                    if (lastNode != null) {
                        //check if the last open tag allows this tag as its content
                        //if not, close the previous open tag by the end of this tag
                        if (!lastNode.reduce(currentNodeDtdElement)) {
                            //current node cannot be present inside its parent

                            if (!lastNode.isResolved()) {
                                //the parent node is not resolved we cannot close it
                                //some mandatory content unresolved, report error
                                String expectedElements = elementsToString(lastNode.getAllPossibleElements());
                                errorMessages.add(new String[]{UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTree.class, "MSG_UNEXPECTED_TAG",
                                        new Object[]{currentNodeDtdElement.getName(), expectedElements})});

                                if (DEBUG) {
                                    System.out.println("NODE NOT RESOLVED! Missing " + expectedElements);
                                }
                            } else {
                                //the parent node is resolved so can be possibly closed

                                //check if the node we are going to close have required end tag
                                //if so show an error
                                Element lastDtdElement = dtd.getElement(isXHTML ? lastNode.name() : lastNode.name().toUpperCase(Locale.ENGLISH));
                                if (lastDtdElement != null && !lastDtdElement.hasOptionalEnd()) {
                                    //the last node need an end tag => error
                                    Collection<Element> possibleElems = lastNode.getAllPossibleElements();
                                    if (possibleElems.isEmpty()) {
                                        errorMessages.add(new String[]{UNEXPECTED_TAG_KEY,NbBundle.getMessage(SyntaxTree.class, "MSG_UNEXPECTED_TAG_NO_EXPECTED_CONTENT",
                                                new Object[]{currentNodeDtdElement.getName()})});
                                    } else {
                                        String expectedElements = elementsToString(possibleElems);
                                        errorMessages.add(new String[]{UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTree.class, "MSG_UNEXPECTED_TAG",
                                                new Object[]{currentNodeDtdElement.getName(), expectedElements})});
                                    }
                                } else {
                                    //the last node has optional end tag, can be closed

                                    //close the previous node
                                    lastNode.setEndOffset(element.offset());
                                    nodeStack.remove(lastNode);

//                                    //hmm, the last node didn't resolve this tag, lets try its parent
//                                    AstNode parentNode = nodeStack.getLast();
//                                    if (!parentNode.isResolved()) {
//                                        //an attempt to reduce the current node within its parent
//                                        parentNode.reduce(currentNodeDtdElement);
//                                    }

                                    if (DEBUG) {
                                        System.out.println("Closing tag " + lastNode.name() + " by the end of this tag!");
                                    }
                                }


                            }
                        }
                    }

                    //check tag attributes
                    unknownAttributeMessages.addAll(checkTagAttributes((SyntaxElement.Tag) element, currentNodeDtdElement));

                    //create DTD content for this node
                    contentModel = currentNodeDtdElement.getContentModel();
                }

                //<<< end of error checks

                int openingTagEndOffset = element.offset() + element.length();
                AstNode newTagNode = new AstNode(tagName, AstNode.NodeType.TAG,
                        element.offset(), openingTagEndOffset, contentModel);

                nodeStack.getLast().addChild(newTagNode);
                if (!(( (SyntaxElement.Tag) element).isEmpty() ||
                        (currentNodeDtdElement != null && currentNodeDtdElement.isEmpty()))) {
                    //the node is neeither empty by declaration nor by definition
                    nodeStack.add(newTagNode);
                }

                AstNode openingTagNode = new AstNode(tagName, AstNode.NodeType.OPEN_TAG,
                        element.offset(), openingTagEndOffset);

                openingTagNode.addDescriptionsToNode(errorMessages, Description.ERROR);
                openingTagNode.addDescriptions(unknownAttributeMessages);

                newTagNode.addChild(openingTagNode);
            } else if (element.type() == SyntaxElement.TYPE_ENDTAG) {
                // CLOSING TAG
                // is it consistent with the last open tag? for now assuming 'yes'
                // add closing tag node
                // pop current node from the stack

                String tagName = ((SyntaxElement.Named) element).getName();
                int lastMatchedTag = nodeStack.size() - 1;

                while (!tagName.equals(nodeStack.get(lastMatchedTag).name()) && lastMatchedTag > 0) {
                    lastMatchedTag--;
                }

                int closingTagEndOffset = element.offset() + element.length();

                AstNode closingTag = new AstNode(tagName, AstNode.NodeType.ENDTAG,
                        element.offset(), closingTagEndOffset);

                if (tagName.equals(nodeStack.get(lastMatchedTag).name())) {
                    int nodesToDelete = nodeStack.size() - lastMatchedTag - 1;
                    removeNLastNodes(nodesToDelete, nodeStack);

                    AstNode lastNode = nodeStack.getLast();

                    lastNode.addChild(closingTag);
                    lastNode.setEndOffset(closingTagEndOffset);

                    //some error checks >>>
                    if (lastNode.type() == AstNode.NodeType.TAG) {
                        AstNode openTag = lastNode.children().get(0);
                        assert openTag.type() == AstNode.NodeType.OPEN_TAG : "Unexpected tag type: " + openTag.type();

                        Element dtdElement = dtd.getElement(isXHTML ? openTag.name() : openTag.name().toUpperCase(Locale.ENGLISH));
                        //check if the tag content is resolved (only for html tags)
                        if (dtdElement != null) {
                            if (!lastNode.isResolved()) {
                                //some mandatory content unresolved, report error to the open tag
                                String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNRESOLVED_TAG",
                                        new Object[]{elementsToString(lastNode.getAllPossibleElements())});

                                openTag.addDescriptionToNode(UNRESOLVED_TAG_KEY, errorMessage, Description.ERROR);
                            }

                            //test if the tag is empty - if so the and tag is forbidden
                            if (dtdElement.isEmpty()) {
                                closingTag.addDescriptionToNode(FORBIDDEN_END_TAG, NbBundle.getMessage(SyntaxTree.class, "MSG_FORBIDDEN_ENDTAG"), Description.ERROR);
                            }

                        } else {
                            //non-html tag, report error
                            //but only if the tagname doesn't contain prefix e.g. <ui:composion> for facelets
                            if (!openTag.name().contains(":")) {
                                String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNKNOWN_TAG",
                                        new Object[]{openTag.name()});
                                openTag.addDescriptionToNode(UNKNOWN_TAG_KEY, errorMessage, Description.ERROR);
                            }
                        }
                    }

                    //<<< end of error checks

                    nodeStack.removeLast();
                } else {
                    // unmatched closing tag
                    AstNode newTagNode = new AstNode(tagName, AstNode.NodeType.TAG,
                            element.offset(), closingTagEndOffset);

                    String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNMATCHED_TAG");
                    newTagNode.addDescriptionToNode(UNMATCHED_TAG, errorMessage, Description.WARNING);

                    nodeStack.getLast().addChild(newTagNode);
                    nodeStack.add(newTagNode);
                    newTagNode.addChild(closingTag);
                }

            } else {
                // add a new AST node to the last node on the stack
                AstNode.NodeType nodeType = intToNodeType(element.type());

                AstNode node = new AstNode(null, nodeType, element.offset(),
                        element.offset() + element.length());

                nodeStack.getLast().addChild(node);
            }
        }

        removeNLastNodes(nodeStack.size() - 1, nodeStack);
        return root;
    }

    private static Collection<Description> checkTagAttributes(SyntaxElement.Tag element, Element dtdElement) {
        Collection<Description> errmsgs = new ArrayList<Description>(3);
        //check attributes
        List<TagAttribute> existingAttrs = element.getAttributes();

        for (TagAttribute ta : existingAttrs) {
            if (dtdElement.getAttribute(ta.getName().toLowerCase(Locale.ENGLISH)) == null) {
                //unknown attribute
                Description desc = Description.create(UNKNOWN_ATTRIBUTE_KEY, NbBundle.getMessage(SyntaxTree.class, "MSG_UNKNOWN_ATTRIBUTE",
                        new Object[]{ta.getName(), element.getName()}), Description.WARNING, ta.getNameOffset(), ta.getNameOffset() + ta.getName().length());

                errmsgs.add(desc);
            }
        }
        return errmsgs;
    }

    private static String elementsToString(Collection<Element> elements) {
        StringBuffer b = new StringBuffer();
        for (Element e : elements) {
            b.append('<');
            b.append(e.getName());
            b.append('>');
            b.append(", ");
        }

        if(b.length() > 0) {
            //strip last delimiters
            b.delete(b.length() - 2, b.length());
        }

        return b.toString();
    }

    private static void removeNLastNodes(int nodesToDelete, LinkedList<AstNode> nodeStack) {
        LinkedList<LinkedList<AstNode>> orphanMatrix = new LinkedList<LinkedList<AstNode>>();

        for (int i = 0; i < nodesToDelete; i++) {
            LinkedList<AstNode> orphans = new LinkedList<AstNode>();
            AstNode lastInStack = nodeStack.getLast();
            lastInStack.markUnmatched();

            String errorMessage = NbBundle.getMessage(SyntaxTree.class, "MSG_UNMATCHED_TAG");
            lastInStack.addDescriptionToNode(UNMATCHED_TAG, errorMessage, Description.WARNING);

            for (AstNode child : lastInStack.children()) {
                if (child.type() == AstNode.NodeType.TAG || child.type() == AstNode.NodeType.UNMATCHED_TAG) {
                    orphans.add(child);
                }
            }

            lastInStack.removeTagChildren();

            nodeStack.removeLast();
            orphanMatrix.addFirst(orphans);
        }

        for (LinkedList<AstNode> orphans : orphanMatrix) {
            for (AstNode orphan : orphans) {
                nodeStack.getLast().addChild(orphan);
            }
        }
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

}

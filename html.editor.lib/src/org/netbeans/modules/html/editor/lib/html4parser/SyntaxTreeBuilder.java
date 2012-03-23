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
package org.netbeans.modules.html.editor.lib.html4parser;

import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.TagElement;
import java.util.*;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.dtd.DTD;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Element;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.CharSequences;
import org.openide.util.NbBundle;

/**
 * Planar parser result to AST convertor
 *
 * @author mfukala@netbeans.org
 */
public class SyntaxTreeBuilder {

    //error messages keys, used for unit testing
    
    //disabled for SyntaxParser.Behaviour.DISABLE_STRUCTURE_CHECKS
    static final String UNEXPECTED_TAG_KEY = "unexpected_tag"; //NOI18N
    static final String UNRESOLVED_TAG_KEY = "unresolved_tag"; //NOI18N

    //following errors are NOT disabled for SyntaxParser.Behaviour.DISABLE_STRUCTURE_CHECKS
    static final String UNKNOWN_TAG_KEY = "unknown_tag"; //NOI18N
    static final String UNKNOWN_ATTRIBUTE_KEY = "unknown_attribute"; //NOI18N
    static final String FORBIDDEN_END_TAG = "forbidded_endtag"; //NOI18N
    static final String UNMATCHED_TAG = "unmatched_tag"; //NOI18N
    static final String MISSING_REQUIRED_END_TAG = "missing_required_end_tag"; //NOI18N
    public static final String MISSING_REQUIRED_ATTRIBUTES = "missing_required_attribute"; //NOI18N
    static final String TAG_CANNOT_BE_EMPTY = "tag_cannot_be_empty"; //NOI18N
    
    
    private static final String ARTIFICIAL_NODE_NAME = "_NO_NAME_PROVIDED_"; //NOI18N

    private static final String COLON = ":"; //NOI18N
    
    //XXX >>> fix this, fake context only!!!
    private static final class Context {
        private boolean isPropertyEnabled(String prop) {
            return false;
        }
    }
    private static final Context context = new Context();
    //XXX <<<

    public static Node makeTree(HtmlSource source, HtmlVersion version, Collection<org.netbeans.modules.html.editor.lib.api.elements.Element> elements) {
        DTD dtd = version.getDTD();

        assert elements != null;
        assert dtd != null;

        CharSequence sourceCode = source.getSourceCode();
        
        //create a root node, it can contain one or more child nodes
        //normally just <html> node should be its child
        AstNode rootNode = new AstNode.RootAstNode(0, sourceCode.length(), dtd);
        LinkedList<AstNode> stack = new LinkedList<AstNode>();
        stack.add(rootNode);

        for (org.netbeans.modules.html.editor.lib.api.elements.Element element : elements) {

            if (element.type() == ElementType.OPEN_TAG) { //open tag

                TagElement tagElement = (TagElement) element;
                CharSequence tagName = tagElement.name();

                AstNode lNode = stack.getLast();

                //try to find DTD element for the current node
                Element currentNodeDtdElement = dtd.getElement(tagName.toString());

                if (currentNodeDtdElement == null) {
                    //no DTD tag, just mark as unknown and add it as a child of current stack's top node
                    AstNode unknownTagNode = new AstNode(tagName, ElementType.OPEN_TAG,
                            tagElement.from(), tagElement.to(), tagElement.isEmpty());

                    copyProblemsFromElementToNode(element, unknownTagNode);

                    //ignore namespaced tags, they won't be matched, but without errors
                    if (!isIgnoredTagName(tagName)) {
                        String errorMessage = NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNKNOWN_TAG", //NOI18N
                                new Object[]{tagName});

                        unknownTagNode.addDescriptionToNode(UNKNOWN_TAG_KEY, errorMessage, ProblemDescription.WARNING);
                    }

                    lNode.addChild(unknownTagNode);

                    continue; //process next syntax element

                }


                //create an AST node for current element

                AstNode openTagNode = new AstNode(tagName, ElementType.OPEN_TAG,
                        tagElement.from(), tagElement.to(),
                        currentNodeDtdElement, tagElement.isEmpty(), stack(stack));

                copyProblemsFromElementToNode(element, openTagNode);

//                //check if the tag can be empty
//                if(tagElement.isEmpty() && !currentNodeDtdElement.isEmpty()) {
//                    //the tag is empty, but cannot be, mark error
//                    openTagNode.addDescriptionToNode(TAG_CANNOT_BE_EMPTY, NbBundle.getMessage(SyntaxTree.class, "MSG_TAG_CANNOT_BE_EMPTY"), Description.ERROR);
//                }

                //check tag attributes
                if(!context.isPropertyEnabled(SyntaxAnalyzer.Behaviour.DISABLE_ATTRIBUTES_CHECKS.name())) {
                    checkTagAttributes(openTagNode, (TagElement) tagElement, currentNodeDtdElement);
                }

                //add existing tag attributes
                setTagAttributes(openTagNode, tagElement);

                //check if the last open tag allows this tag as its content, do not do that for root node
                if (lNode != rootNode && !lNode.reduce(currentNodeDtdElement)) {
                    //current node cannot be present inside its parent

                    if (!lNode.isResolved()) {
                        //the parent node is not resolved we cannot close it
                        //some mandatory content unresolved, report error
                        if (!context.isPropertyEnabled(SyntaxAnalyzer.Behaviour.DISABLE_STRUCTURE_CHECKS.name())) {
                            String expectedElements = elementsToString(lNode.getAllPossibleElements());
                            openTagNode.addDescriptionToNode(
                                    UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNEXPECTED_TAG", //NOI18N
                                    new Object[]{currentNodeDtdElement.getName(), expectedElements}), ProblemDescription.ERROR);
                        }
                    } else {
                        //the parent node is resolved so can be possibly closed

                        //check if the node we are going to close have required end tag
                        //if so show an error
                        Element lastDtdElement = dtd.getElement(lNode.name().toString());

                        assert lastDtdElement != null; //only DTD based elements are put into the stack

                        if (!lastDtdElement.hasOptionalEnd()) {
                            //the last node has required end tag => report error
                            if(!context.isPropertyEnabled(SyntaxAnalyzer.Behaviour.DISABLE_STRUCTURE_CHECKS.name())) {
                                Collection<Element> possibleElems = lNode.getAllPossibleElements();
                                if (possibleElems.isEmpty()) {
                                    openTagNode.addDescriptionToNode(UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNEXPECTED_TAG_NO_EXPECTED_CONTENT", //NOI18N
                                            new Object[]{currentNodeDtdElement.getName()}), ProblemDescription.ERROR);
                                } else {
                                    String expectedElements = elementsToString(possibleElems);
                                    openTagNode.addDescriptionToNode(
                                            UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNEXPECTED_TAG", //NOI18N
                                            new Object[]{currentNodeDtdElement.getName(), expectedElements}), ProblemDescription.ERROR);
                                }
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
                                stack.remove(lNode);
                                lNode.setLogicalEndOffset(openTagNode.startOffset());

                                lNode =
                                        stack.getLast();
                                lNode.addChild(openTagNode);


                                //mark the error only if the parent node is not root
                                if (lNode != rootNode) {
                                    if (!context.isPropertyEnabled(SyntaxAnalyzer.Behaviour.DISABLE_STRUCTURE_CHECKS.name())) {
                                        //nothing reduces the current element, error
                                        openTagNode.addDescriptionToNode(UNEXPECTED_TAG_KEY, NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNEXPECTED_TAG_NO_EXPECTED_CONTENT", //NOI18N
                                                new Object[]{currentNodeDtdElement.getName()}), ProblemDescription.ERROR);
                                    }
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
                                lNode = stack.getLast(); //update lNode to the new top
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
                lNode.addChild(openTagNode);

            } else if (element.type() == ElementType.END_TAG) { //close tag

                TagElement telement = (TagElement)element;
                CharSequence tagName = telement.name();

                //test if DTD tag, if not do not try to match
                Element dtdElement = dtd.getElement(tagName.toString());
                if (dtdElement == null) {
                    //no DTD tag, just mark as unknown and add it as a child of current stack's top node
                    AstNode unknownTagNode = new AstNode(tagName, ElementType.UNKNOWN_TAG,
                            element.from(), element.to(), false);

                    copyProblemsFromElementToNode(element, unknownTagNode);

                    //ignore namespaced tags, they won't be matched, but without errors
                    if (!isIgnoredTagName(tagName)) {
                        String errorMessage = NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNKNOWN_TAG", //NOI18N
                                new Object[]{tagName});

                        unknownTagNode.addDescriptionToNode(UNKNOWN_TAG_KEY, errorMessage, ProblemDescription.WARNING);
                    }

                    stack.getLast().addChild(unknownTagNode);

                    continue; //process next syntax element

                }



                AstNode closeTagNode = new AstNode(tagName, ElementType.END_TAG,
                        element.from(), element.to(), dtdElement, false, stack(stack));

                copyProblemsFromElementToNode(element, closeTagNode);

                int matched_index = -1;
                for (int i = stack.size() - 1; i >=
                        0; i--) {
                    AstNode node = stack.get(i);
                    if (LexerUtils.equals(tagName, node.name(), true, false)) {
                        //found the matching open tag, maybe
                        //check if the matching open tag has forbidden end tag
                        if (AstNodeUtils.hasForbiddenEndTag(node)) {
                            //cannot match, report error
                            closeTagNode.addDescriptionToNode(FORBIDDEN_END_TAG, NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_FORBIDDEN_ENDTAG"), ProblemDescription.ERROR); //NOI18N
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

                            if (!context.isPropertyEnabled(SyntaxAnalyzer.Behaviour.DISABLE_STRUCTURE_CHECKS.name())) {
                                if (!node.isResolved()) {
                                    //unresolved content
                                    String errorMessage = NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNRESOLVED_TAG", //NOI18N
                                            new Object[]{elementsToString(node.getAllPossibleElements())});

                                    node.addDescriptionToNode(UNRESOLVED_TAG_KEY, errorMessage, ProblemDescription.ERROR);
                                }
                            }

                            if (!hasOptionalEndTag(node)) {
                                //missing end tag
                                String errorMessage = NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_MISSING_REQUIRED_END_TAG"); //NOI18N
                                node.addDescriptionToNode(MISSING_REQUIRED_END_TAG, errorMessage, ProblemDescription.ERROR);
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
                    if(!context.isPropertyEnabled(SyntaxAnalyzer.Behaviour.DISABLE_STRUCTURE_CHECKS.name())) {
                    if (!match.isResolved()) {
                            //some mandatory content unresolved, report error to the open tag
                            String errorMessage = NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNRESOLVED_TAG", //NOI18N
                                    new Object[]{elementsToString(match.getAllPossibleElements())});

                            match.addDescriptionToNode(UNRESOLVED_TAG_KEY, errorMessage, ProblemDescription.ERROR);

                        }
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
                    //no match, mark as unmatched if has required start tag
                    if (!dtdElement.hasOptionalStart()) {
                        String errorMessage = NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNMATCHED_TAG"); //NOI18N
                        closeTagNode.addDescriptionToNode(UNMATCHED_TAG, errorMessage, ProblemDescription.ERROR);
                    }

                    //add it to the last node
                    stack.getLast().addChild(closeTagNode);
                }

            } else if (element.type() == ElementType.ERROR ||
                    element.type() == ElementType.COMMENT) { //error || comment
                // add a new AST node to the last node on the stack

                AstNode node = new AstNode(ARTIFICIAL_NODE_NAME, element.type(), element.from(),
                        element.to(), false);

                copyProblemsFromElementToNode(element, node);

                stack.getLast().addChild(node);

            } else {
                //rest of the syntax element types of unimportant types,
                //they are not present in the parse tree
                
            }

        }

        //check the stack content and resolve left nodes
        for (int i = stack.size() - 1; i >
                0; i--) { // (i > 0) == do not process the very first (root) node
            AstNode node = stack.get(i);

            if (!hasOptionalEndTag(node)) {
                //unclosed tag, mark
                String errorMessage = NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNMATCHED_TAG"); //NOI18N
                node.addDescriptionToNode(UNMATCHED_TAG, errorMessage, ProblemDescription.ERROR);
            }

            if(!context.isPropertyEnabled(SyntaxAnalyzer.Behaviour.DISABLE_STRUCTURE_CHECKS.name())) {
                if (!node.isResolved()) {
                    //unresolved, mark
                    String errorMessage = NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNRESOLVED_TAG", //NOI18N
                            new Object[]{elementsToString(node.getAllPossibleElements())});

                    node.addDescriptionToNode(UNRESOLVED_TAG_KEY, errorMessage, ProblemDescription.ERROR);
                }
            }

//            if(nodeOk) {
//if the tag is ok, then close it by the end of the file
            node.setLogicalEndOffset(sourceCode.length());
//            }

        }

        return rootNode;
    }

    private static void copyProblemsFromElementToNode(org.netbeans.modules.html.editor.lib.api.elements.Element element, AstNode node) {
        Collection<ProblemDescription> problems = element.problems();
        if(problems == null) {
            return ;
        }
        for(ProblemDescription problem : problems) {
            node.addDescription(problem);
        }
    }

    private static List<CharSequence> stack(LinkedList<AstNode> stack) {
        List<CharSequence> s = new ArrayList<CharSequence>();
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

    private static boolean isIgnoredTagName(CharSequence tagName) {
        return CharSequences.indexOf(tagName, COLON) != -1; //NOI18N
    }

    private static boolean isIgnoredTagAttribute(CharSequence tagName) {
        return CharSequences.indexOf(tagName, COLON) != -1; //NOI18N
    }

    private static void checkTagAttributes(AstNode node, TagElement element, Element dtdElement) {
        //check existing attributes
        Collection<Attribute> existingAttrs = element.attributes();
        List<String> existingAttrNames = new ArrayList<String>(existingAttrs.size());
        for (Attribute ta : existingAttrs) {
            String stagName = ta.name().toString();
            String tagName = stagName.toLowerCase(Locale.ENGLISH);
            existingAttrNames.add(tagName);
            DTD.Attribute attr = dtdElement.getAttribute(tagName);
            if (attr == null) {
                if (!isIgnoredTagAttribute(ta.name())) {
                    //unknown attribute
                    ProblemDescription desc = ProblemDescription.create(UNKNOWN_ATTRIBUTE_KEY, NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_UNKNOWN_ATTRIBUTE", //NOI18N
                            new Object[]{stagName, element.name().toString()}), 
                            ProblemDescription.WARNING, ta.nameOffset(), ta.nameOffset() + stagName.length());

                    node.addDescription(desc);
                }

            }
        }

        //check missing required attributes
        Collection<String> missingAttrsNames = new ArrayList<String>();
        for (Object _attr : dtdElement.getAttributeList(null)) {
            DTD.Attribute attr = (DTD.Attribute) _attr;
            if (attr.isRequired() && !existingAttrNames.contains(attr.getName())) {
                missingAttrsNames.add(attr.getName());
            }

        }

        StringBuilder missingAttributesListMsg = new StringBuilder();
        for(String missingAttrName : missingAttrsNames) {
            //missing required attribute
            missingAttributesListMsg.append(missingAttrName);
            missingAttributesListMsg.append(", ");
        }


        if (missingAttributesListMsg.length() > 0) {
            //cut last comma and space
            missingAttributesListMsg.deleteCharAt(missingAttributesListMsg.length() - 2);
            //attach the error description
            node.addDescriptionToNode(
                    MISSING_REQUIRED_ATTRIBUTES,
                    NbBundle.getMessage(SyntaxTreeBuilder.class, "MSG_MISSING_REQUIRED_ATTRIBUTES", //NOI18N
                    new Object[]{missingAttributesListMsg.toString()}),
                    ProblemDescription.WARNING);
            //store the collection of the missing attribute for further usage in hintfixes
            node.setProperty(MISSING_REQUIRED_ATTRIBUTES, missingAttrsNames);
        }

    }

    protected static void setTagAttributes(AstNode node, TagElement tag) {
        for (Attribute ta : tag.attributes()) {
            if (ta != null) {
                AstNode.AstAttribute nodeAttribute = new AstNode.AstAttribute(ta.name(),
                        ta.value(), ta.nameOffset(), ta.valueOffset());

                node.setAttribute(nodeAttribute);
            }

        }
    }

    private static String elementsToString(Collection<Element> elements) {
        StringBuilder b = new StringBuilder();
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

    
}

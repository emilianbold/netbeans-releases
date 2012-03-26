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

import java.util.*;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementFilter;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.dtd.DTD;

/**
 *
 * @author marek
 */
public class AstNodeUtils {

    /** sarches for a descendant of the given node
     * @param node the base node
     * @param offset the offset for which we try to find a descendant node
     * @param forward the direction bias
     * @param physicalAstNodeOnly if true the found descendant will be returned only if the offset falls to its physical boundaries.
     */
    public static AstNode findNode(AstNode node, int offset, boolean forward, boolean physicalAstNodeOnly) {
        if(physicalAstNodeOnly) {
            return findNodeByPhysicalRange(node, offset, forward);
        } else {
            return findNodeByLogicalRange(node, offset, forward);
        }
    }

    private static AstNode findNodeByPhysicalRange(AstNode node, int offset, boolean forward) {
        for (Element child : node.children()) {
            AstNode achild = (AstNode)child;
            if(matchesNodeRange(achild, offset, forward, true)) {
                return achild;
            } else if(node.from() > offset) {
                //already behind the possible candidates
                return null;
            } else {
                //lets try this branch
                AstNode candidate = findNodeByPhysicalRange(achild, offset, forward);
                if(candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static AstNode findNodeByLogicalRange(AstNode node, int offset, boolean forward) {
        for (Element child : node.children()) {
            AstNode achild = (AstNode)child;
            if(achild.isVirtual()) {
                //we need to recurse into every virtual branch blindly hoping there might by some
                //real nodes fulfilling our constrains
                AstNode n =  findNodeByLogicalRange(achild, offset, forward);
                if(n != null) {
                    return n;
                }
            }
            if(matchesNodeRange(achild, offset, forward, false)) {
                return findNodeByLogicalRange(achild, offset, forward);
            }
        }
        return node.isVirtual() ? null : node;
    }

    private static boolean matchesNodeRange(AstNode node, int offset, boolean forward, boolean physicalAstNodeRangeOnly) {
        int from = physicalAstNodeRangeOnly || node.logicalStartOffset()== -1 ? node.from() : node.logicalStartOffset();
        int to = physicalAstNodeRangeOnly || node.logicalEndOffset() == -1 ? node.endOffset() : node.logicalEndOffset();

        if(forward) {
            if(offset >= from&& offset < to) {
                return true;
            }
        } else {
            if (offset > from && offset <= to) {
                return true;
            }
        }
        return false;
    }

    public static AstNode getTagNode(AstNode node, int astOffset) {
        if (node.type() == ElementType.OPEN_TAG) {
            if (astOffset >= node.from() && astOffset < node.endOffset()) {
                //the offset falls directly to the tag
                return node;
            }

            AstNode match = (AstNode)node.getMatchingTag();
            if (match != null && match.type() == ElementType.CLOSE_TAG) {
                //end tag is possibly the searched node
                if (astOffset >= match.from() && astOffset < match.endOffset()) {
                    return match;
                }
            }

            //offset falls somewhere inside the logical range but outside of
            //the open or end tag ranges.
            return null;
        }

        return node;
    }

  
    public static Collection<AstNode> getPossibleEndTagElements(AstNode leaf) {
        Collection<AstNode> possible = new LinkedList<AstNode>();

        for (;;) {
            if (leaf.type() == ElementType.ROOT) {
                break;
            }

            //if dtd element and doesn't have forbidden end tag
            if ((leaf.getDTDElement() == null || !AstNodeUtils.hasForbiddenEndTag(leaf))
                    && leaf.type() == ElementType.OPEN_TAG) {

                possible.add(leaf);

                //check if the tag needs to have a matching tag and if is matched already
                if (leaf.needsToHaveMatchingTag() && leaf.getMatchingTag() == null) {
                    //if not, any of its parent cannot be closed here
                    break;
                }
            }
            leaf = (AstNode)leaf.parent();
            assert leaf != null;
        }
        return possible;
    }


    public static Collection<DTD.Element> getPossibleOpenTagElements(AstNode node) {
        return getPossibleOpenTagElements(node.getRootNode(), node.endOffset());

    }

    public static Collection<DTD.Element> getPossibleOpenTagElements(AstNode root, int astPosition) {
        HashSet<DTD.Element> elements = new HashSet<DTD.Element>();

        assert root.type() == ElementType.ROOT;

        //exlusive to start offset so |<div> won't return the div tag but <|div> will
        AstNode leafAstNodeForPosition = AstNodeUtils.findNode(root, astPosition, false, false);
        if(leafAstNodeForPosition == null) {
            //may happen if one sarches at the 0 position with backward bias
            //or at the end of the root node range with forward bias
            leafAstNodeForPosition = root;
        }


        if(leafAstNodeForPosition.logicalEndOffset == astPosition) {
            if(leafAstNodeForPosition.parent() != null) {
                leafAstNodeForPosition = (AstNode)leafAstNodeForPosition.parent();
            }
        }

        //search first dtd element node in the tree path
        while (leafAstNodeForPosition.getDTDElement() == null &&
                leafAstNodeForPosition.type() != ElementType.ROOT) {
            leafAstNodeForPosition = (AstNode)leafAstNodeForPosition.parent();
        }

        assert leafAstNodeForPosition != null;

        //root allows all dtd elements
        if (leafAstNodeForPosition == root) {
            return root.getAllPossibleElements();
        }

        //check if the ast offset falls into the node range (not logical range!!!)
        if (leafAstNodeForPosition.from() <= astPosition && leafAstNodeForPosition.endOffset() > astPosition) {
            //if so return empty list - nothing is allowed inside tag content
            return Collections.EMPTY_LIST;
        }

        assert leafAstNodeForPosition.type() == ElementType.OPEN_TAG; //nothing else than open tag can contain non-tag content

        DTD.ContentModel contentModel = leafAstNodeForPosition.getDTDElement().getContentModel();
        DTD.Content content = contentModel.getContent();
        //resolve all preceding siblings before the astPosition
        Collection<DTD.Element> childrenBefore = new ArrayList<DTD.Element>();
        for (Element sibling : leafAstNodeForPosition.children()) {
            AstNode asibling = (AstNode)sibling;
            if (sibling.from() >= astPosition) {
                //process only siblings before the offset!
                break;
            }
            if (sibling.type() == ElementType.OPEN_TAG) {
                DTD.Content subcontent = content.reduce(asibling.getDTDElement().getName());
                if (subcontent != null) {
                    //sibling reduced - update the content to the resolved one
                    if(content == subcontent) {
                        //the content is reduced to itself
                    } else {
                        content = subcontent;
                        childrenBefore.add(asibling.getDTDElement());
                    }
                } else {
                    //the siblibg doesn't reduce the content - it is unallowed there - ignore it
                }
            }
        }

        if (!leafAstNodeForPosition.needsToHaveMatchingTag()) {
            //optional end, we need to also add results for the situation
            //the node is automatically closed - which is before the node start

            //but do not do that on the root level
            if (leafAstNodeForPosition.parent().type() != ElementType.ROOT) {
                Collection<DTD.Element> elementsBeforeLeaf = getPossibleOpenTagElements(root, leafAstNodeForPosition.from());
                //remove all elements which has already been reduced before
                elementsBeforeLeaf.removeAll(childrenBefore);
                elements.addAll(elementsBeforeLeaf);
            }
        }

//      elements.addAll(content.getPossibleElements());
        addAllPossibleElements(elements, content.getPossibleElements());

        //process includes/excludes from the root node to the leaf
        List<AstNode> path = new ArrayList<AstNode>();
        for(AstNode node = leafAstNodeForPosition; node.type() != ElementType.ROOT; node = (AstNode)node.parent()) {
            path.add(0, node);
        }
        for(AstNode node : path) {
            DTD.ContentModel cModel = node.getDTDElement().getContentModel();
            elements.addAll(cModel.getIncludes());
            elements.removeAll(cModel.getExcludes());
        }

        return elements;
    }

//    public static Collection<DTD.Element> getPossibleOpenTagElements(AstNode leafAstNodeForPosition) {
//        HashSet<DTD.Element> elements = new HashSet<DTD.Element>();
//
//        int astPosition = leafAstNodeForPosition.from();
//        AstNode root = leafAstNodeForPosition.getRootAstNode();
//
//        //search first dtd element node in the tree path
//        while (leafAstNodeForPosition.getDTDElement() == null &&
//                leafAstNodeForPosition.type() != AstNode.AstNodeType.ROOT) {
//            leafAstNodeForPosition = leafAstNodeForPosition.parent();
//        }
//
//        assert leafAstNodeForPosition != null;
//
//        //root allows all dtd elements
//        if (leafAstNodeForPosition == root) {
//            return root.getAllPossibleElements();
//        }
//
//
//        assert leafAstNodeForPosition.type() == AstNode.AstNodeType.OPEN_TAG; //nothing else than open tag can contain non-tag content
//
//        DTD.ContentModel contentModel = leafAstNodeForPosition.getDTDElement().getContentModel();
//        DTD.Content content = contentModel.getContent();
//        //resolve all preceding siblings before the astPosition
//        Collection<DTD.Element> childrenBefore = new ArrayList<DTD.Element>();
//        for (AstNode sibling : leafAstNodeForPosition.children()) {
//            if (sibling.from() >= astPosition) {
//                //process only siblings before the offset!
//                break;
//            }
//            if (sibling.type() == AstNode.AstNodeType.OPEN_TAG) {
//                DTD.Content subcontent = content.reduce(sibling.getDTDElement().getName());
//                if (subcontent != null) {
//                    //sibling reduced - update the content to the resolved one
//                    if(content == subcontent) {
//                        //the content is reduced to itself
//                    } else {
//                        content = subcontent;
//                        childrenBefore.add(sibling.getDTDElement());
//                    }
//                } else {
//                    //the siblibg doesn't reduce the content - it is unallowed there - ignore it
//                }
//            }
//        }
//
//        if (!leafAstNodeForPosition.needsToHaveMatchingTag()) {
//            //optional end, we need to also add results for the situation
//            //the node is automatically closed - which is before the node start
//
//            //but do not do that on the root level
//            if (leafAstNodeForPosition.parent().type() != AstNode.AstNodeType.ROOT) {
//                Collection<DTD.Element> elementsBeforeLeaf = getPossibleOpenTagElements(root, leafAstNodeForPosition.from());
//                //remove all elements which has already been reduced before
//                elementsBeforeLeaf.removeAll(childrenBefore);
//                elements.addAll(elementsBeforeLeaf);
//            }
//        }
//
////      elements.addAll(content.getPossibleElements());
//        addAllPossibleElements(elements, content.getPossibleElements());
//
//        //process includes/excludes from the root node to the leaf
//        List<AstNode> path = new ArrayList<AstNode>();
//        for(AstNode node = leafAstNodeForPosition; node.type() != AstNode.AstNodeType.ROOT; node = node.parent()) {
//            path.add(0, node);
//        }
//        for(AstNode node : path) {
//            DTD.ContentModel cModel = node.getDTDElement().getContentModel();
//            elements.addAll(cModel.getIncludes());
//            elements.removeAll(cModel.getExcludes());
//        }
//
//        return elements;
//    }

    private static void addAllPossibleElements(Set<DTD.Element> result, Collection<DTD.Element> elements) {
        for(DTD.Element element : elements) {
            result.add(element);
            if(element.hasOptionalStart()) {
                addAllPossibleElements(result, element.getContentModel().getContent().getPossibleElements());
            }
        }
    }

    public static boolean hasForbiddenEndTag(AstNode node) {
        return node.getDTDElement() != null ? node.getDTDElement().isEmpty() : false;
    }

    /** finds closest physical preceeding node to the offset */
    public static AstNode getClosestNodeBackward(AstNode context, int offset, ElementFilter filter) {
        for (Element child : context.children(filter)) {
            if (child.from() >= offset) {
                 return context;
            }
            context = getClosestNodeBackward((AstNode)child, offset, filter);
        }
        return context;
    }
//
//    /**
//     * @return an Iterator of nodes preceeding the given node to the root.
//     * The algorithm is to:
//     * 1) return preceeding siblings of the given node,
//     * 2) then its parent and all its siblings
//     * 3) -> 1) until root node is reached
//     */
//
//    public static Iterator<AstNode> getAllPredecessorsIterator() {
//        return null;
//    }

}
    

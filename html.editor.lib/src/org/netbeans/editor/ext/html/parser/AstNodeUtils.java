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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.netbeans.editor.ext.html.dtd.DTD;

/**
 *
 * @author marek
 */
public class AstNodeUtils {

    private static final String INDENT = "   ";

    public static String dumpTree(AstNode node) {
        StringBuffer buf = new StringBuffer();
        dumpTree(node, buf);
        System.out.println(buf.toString());
        return buf.toString();
    }

    public static void dumpTree(AstNode node, StringBuffer buf) {
        dump(node, "", buf);
    }

    private static void dump(AstNode node, String prefix, StringBuffer buf) {
        buf.append(prefix + node.toString());
        buf.append('\n');
        for (AstNode child : node.children()) {
            dump(child, prefix + INDENT, buf);
        }
    }

    public static AstNode getRoot(AstNode node) {
        for (;;) {
            if (node.parent() == null) {
                return node;
            } else {
                node = node.parent();
            }
        }
    }

    public static AstNode findDescendant(AstNode node, int astOffset) {
        return findDescendant(node, astOffset, false);
    }

    public static AstNode findDescendant(AstNode node, int astOffset, boolean exclusiveStartOffset) {
        int[] nodeRange = node.getLogicalRange();

        int so = nodeRange[0];
        int eo = nodeRange[1];


        if (astOffset < so || astOffset > eo) {
            //we are out of the scope - may happen just with the first client call
            return node;
        }

        if (exclusiveStartOffset) {
            so++;
        }

        if (astOffset >= so && astOffset < eo && node.children().isEmpty()) {
            //if the node matches and has no children we found it
            return node;
        }

        for (AstNode child : node.children()) {
            int[] childNodeRange = child.getLogicalRange();
            int ch_so = childNodeRange[0];

            if (exclusiveStartOffset) {
                ch_so++;
            }

            int ch_eo = childNodeRange[1];
            if (astOffset >= ch_so && astOffset < ch_eo) {
                //the child is or contains the searched node
                return findDescendant(child, astOffset, true);
            }

        }

        return node;
    }

    public static AstNode getTagNode(AstNode node, int astOffset) {
        if (node.type() == AstNode.NodeType.OPEN_TAG) {
            if (astOffset >= node.startOffset() && astOffset < node.endOffset()) {
                //the offset falls directly to the tag
                return node;
            }

            AstNode match = node.getMatchingTag();
            if (match != null && match.type() == AstNode.NodeType.ENDTAG) {
                //end tag is possibly the searched node
                if (astOffset >= match.startOffset() && astOffset < match.endOffset()) {
                    return match;
                }
            }

            //offset falls somewhere inside the logical range but outside of
            //the open or end tag ranges.
            return null;
        }

        return node;
    }

    /** find an AstNode according to the path
     * example of path: html/body/table|2/tr -- find a second table tag in body tag
     *
     * note: queries OPEN TAGS ONLY!
     */
    public static AstNode query(AstNode base, String path) {
        StringTokenizer st = new StringTokenizer(path, "/");
        AstNode found = base;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int indexDelim = token.indexOf('|');

            String nodeName = indexDelim >= 0 ? token.substring(0, indexDelim) : token;
            String sindex = indexDelim >= 0 ? token.substring(indexDelim + 1, token.length()) : "0";
            int index = Integer.parseInt(sindex);

            int count = 0;
            AstNode foundLocal = null;
            for (AstNode child : found.children()) {
                if (child.type() == AstNode.NodeType.OPEN_TAG && child.name().equals(nodeName) && count++ == index) {
                    foundLocal = child;
                    break;
                }
            }
            if (foundLocal != null) {
                found = foundLocal;

                if (!st.hasMoreTokens()) {
                    //last token, we may return
                    assert found.name().equals(nodeName);
                    return found;
                }

            } else {
                return null; //no found
            }
        }

        return null;
    }

    public static Collection<DTD.Element> getPossibleOpenTagElements(AstNode root, int astPosition) {
        HashSet<DTD.Element> elements = new HashSet<DTD.Element>();

        assert root.type() == AstNode.NodeType.ROOT;

        //exlusive to start offset so |<div> won't return the div tag but <|div> will
        AstNode leafNodeForPosition = AstNodeUtils.findDescendant(root, astPosition, true);

        //search first dtd element node in the tree path
        while(leafNodeForPosition.getDTDElement() == null &&
                leafNodeForPosition.type() != AstNode.NodeType.ROOT) {
            leafNodeForPosition = leafNodeForPosition.parent();
        }

        assert leafNodeForPosition != null;

        //root allows all dtd elements
        if (leafNodeForPosition == root) {
            return root.getAllPossibleElements();
        }

        //check if the ast offset falls into the node range (not logical range!!!)
        if (leafNodeForPosition.startOffset() <= astPosition && leafNodeForPosition.endOffset() > astPosition) {
            //if so return empty list - nothing is allowed inside tag content
            return Collections.EMPTY_LIST;
        }

        assert leafNodeForPosition.type() == AstNode.NodeType.OPEN_TAG; //nothing else than open tag can contain non-tag content

        DTD.ContentModel contentModel = leafNodeForPosition.getDTDElement().getContentModel();
        DTD.Content content = contentModel.getContent();
        //resolve all preceding siblings before the astPosition
        for (AstNode sibling : leafNodeForPosition.children()) {
            if (sibling.startOffset() >= astPosition) {
                //process only siblings before the offset!
                break;
            }
            if (sibling.type() == AstNode.NodeType.OPEN_TAG) {
                DTD.Content subcontent = content.reduce(sibling.getDTDElement().getName());
                if(subcontent != null) {
                    //sibling reduced - update the content to the resolved one
                    content = subcontent;
                } else {
                    //the siblibg doesn't reduce the content - it is unallowed there - ignore it
                }
            }
        }

        if (!leafNodeForPosition.needsToHaveMatchingTag()) {
            //optional end, we need to also add results for the situation
            //the node is automatically closed - which is before the node start

            //but do not do that on the root level
            if(leafNodeForPosition.parent().type() != AstNode.NodeType.ROOT) {
                elements.addAll(getPossibleOpenTagElements(root, leafNodeForPosition.startOffset()));
            }
        }

        elements.addAll(content.getPossibleElements());
        elements.addAll(contentModel.getIncludes());
        elements.removeAll(contentModel.getExcludes());

        return elements;
    }

    public static boolean hasForbiddenEndTag(AstNode node) {
        return node.getDTDElement() != null ? node.getDTDElement().isEmpty() : false;
    }
   
    public static void visitChildren(AstNode node, AstNodeVisitor visitor) {
        for (AstNode n : node.children()) {
            visitor.visit(n);
            visitChildren(n, visitor);
        }
    }

    public static void visitAncestors(AstNode node, AstNodeVisitor visitor) {
        AstNode parent = (AstNode) node.parent();
        if (parent != null) {
            visitor.visit(parent);
            visitAncestors(parent, visitor);
        }
    }
}
    

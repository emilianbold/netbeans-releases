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
package org.netbeans.modules.css.lib.api;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author mfukala@netbeans.org
 */
public final class NodeUtil {

    private static final String INDENT = "    ";//NOI18N

    private NodeUtil() {
    }
    
    public static CharSequence getNodeImage(Node node, CharSequence source) {
        return source.subSequence(node.from(), node.to());
    }
    
     public static int[] getTrimmedNodeRange(Node node) {
        CharSequence text = node.name();
        int from_diff;
        int to_diff;
        for(from_diff = 0; from_diff < text.length(); from_diff++) {
            if(!Character.isWhitespace(text.charAt(from_diff))) {
                break;
            }
        }

        for(to_diff = 0; to_diff < text.length() - from_diff; to_diff++) {
            if(!Character.isWhitespace(text.charAt(text.length() - 1 - to_diff))) {
                break;
            }
        }

        return new int[]{node.from() + from_diff, node.to() - to_diff};
    }

//    public static Token getNodeToken(Node node, int tokenKind) {
//        Token t = node.jjtGetFirstToken();
//        if (t == null) {
//            return null;
//        }
//        do {
//            if(t.kind == tokenKind) {
//                return t;
//            }
//            t = t.next;
//        } while (t != node.jjtGetLastToken());
//        return null;
//    }

    public static Node findDescendant(Node node, int astOffset) {
        int so = node.from();
        int eo = node.to();
       
        
        if (astOffset < so || astOffset > eo) {
            //we are out of the scope - may happen just with the first client call
            return null;
        }

        if (astOffset >= so && astOffset <= eo && node.children().isEmpty()) {
            //if the node matches and has no children we found it
            return node;
        }

        for (Node child : node.children()) {

            int ch_so = child.from();
            int ch_eo = child.to();
            if (astOffset >= ch_so && astOffset <= ch_eo) {
                //the child is or contains the searched node
                return findDescendant(child, astOffset);
            }

        }

        return node;
    }

    
    /** @return first child of the node with the specified kind. */
    public static Node getChildByType(Node node, NodeType type) {
        Node[] children = getChildrenByType(node, type);
        return children.length == 0 ? null : children[0];
    }

    public static Node getAncestorByType(Node node, final NodeType type) {
	AtomicReference<Node> found = new AtomicReference<Node>();
        NodeVisitor visitor = new NodeVisitor<AtomicReference<Node>>(found) {

            @Override
            public boolean visit(Node node) {
                if(node.type() == type) {
                    getResult().set(node);
                    return true;
                }
                return false;
            }
            
        };
        visitor.visitAncestors(node);
	return found.get();
    }
    
    /** @return list of children of the node with the specified kind. */
    public static Node[] getChildrenByType(Node node, NodeType type) {
        ArrayList<Node> list = new ArrayList<Node>(node.children().size() / 4);
        for(Node child : node.children()) {
            if(child.type() == type) {
                list.add(child);
            }
        }
        return list.toArray(new Node[]{});
    }
    
   
        /** @return A sibling node before or after the given node. */
    public static Node getSibling(Node node, boolean before) {
        Node parent = node.parent();
        if(parent == null) {
            return null;
        }
        Node sibling = null;
        for(int i = 0; i < parent.children().size() ; i++) {
            List<Node> children = parent.children();
            Node child = children.get(i);
            if(child == node) {
                //we found myself
                if(before) {
                    if(i == 0) {
                        //we are first node, no sibling before
                        return null;
                    } else {
                        return children.get(i - 1);
                    }
                } else {
                    //after
                    if(i == children.size() - 1) {
                        //we are last node, no sibling after
                        return null;
                    } else {
                        return children.get(i + 1);
                    }
                }
            }
        }
        return sibling;
    }

     
    public static Node query(Node base, String path) {
        return query(base, path, false);
    }

    /** find an Node according to the given tree path
     * example of path: declaration/property|1/color -- find a second color property  (index from zero)
     */
    public static Node query(Node base, String path, boolean caseInsensitive) {
        StringTokenizer st = new StringTokenizer(path, "/");
        Node found = base;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int indexDelim = token.indexOf('|');

            String nodeName = indexDelim >= 0 ? token.substring(0, indexDelim) : token;
            if (caseInsensitive) {
                nodeName = nodeName.toLowerCase(Locale.ENGLISH);
            }
            String sindex = indexDelim >= 0 ? token.substring(indexDelim + 1, token.length()) : "0";
            int index = Integer.parseInt(sindex);

            int count = 0;
            Node foundLocal = null;
            for (Node child : found.children()) {
                String childName = child.name();
                if ((caseInsensitive ? childName = childName.toLowerCase(Locale.ENGLISH) : childName).equals(nodeName) && count++ == index) {
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
    
    public static void dumpTree(Node node) {
        PrintWriter pw = new PrintWriter(System.out);
        dumpTree(node, pw);
        pw.flush();
    }
    
    public static void dumpTree(Node node, PrintWriter pw) {
        dump(node, 0, pw);
        
    }

    private static void dump(Node tree, int level, PrintWriter pw) {
        for (int i = 0; i < level; i++) {
            pw.print(INDENT);
        }
        treeToString(tree, pw);
        for (Node c : tree.children()) {
            dump(c, level + 1, pw);
        }
    }

    private static void treeToString(Node tree, PrintWriter b)  {
        b.print(tree.name());
        b.print(' ');
        b.print('[');
        b.print(tree.type().name());
        b.print(']');
        b.print('(');
        b.print(Integer.toString(tree.from()));
        b.print('-');
        b.print(Integer.toString(tree.to()));
        b.print(')');
        b.println();
    }
}

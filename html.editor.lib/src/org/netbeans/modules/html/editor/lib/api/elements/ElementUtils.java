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
package org.netbeans.modules.html.editor.lib.api.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marek
 */
public class ElementUtils {

    private static final String INDENT = "   "; //NOI18N

    private ElementUtils() {
    }

    public static CharSequence unquotedValue(Attribute attribute) {
        return isValueQuoted(attribute) ? attribute.value().subSequence(1, attribute.value().length() - 1) : attribute.value();
    }

    public static boolean isValueQuoted(Attribute attr) {
        CharSequence value = attr.value();
        if (value.length() < 2) {
            return false;
        } else {
            return ((value.charAt(0) == '\'' || value.charAt(0) == '"')
                    && (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
        }
    }

    /**
     * sarches for a descendant of the given node
     *
     * @param node the base node
     * @param offset the offset for which we try to find a descendant node
     * @param forward the direction bias
     * @param physicalNodeOnly if true the found descendant will be returned
     * only if the offset falls to its physical boundaries.
     */
    public static Element findElement(Node node, int offset, boolean forward, boolean physicalNodeOnly) {
        if (physicalNodeOnly) {
            return findByPhysicalRange(node, offset, forward);
        } else {
            return findByLogicalRange(node, offset, forward);
        }
    }

    private static Element findByPhysicalRange(Node node, int offset, boolean forward) {
        for (Element child : node.children()) {
            Node achild = (Node) child;
            if (matchesNodeRange(achild, offset, forward, true)) {
                return achild;
            } else if (node.from() > offset) {
                //already behind the possible candidates
                return null;
            } else {
                //lets try this branch
                Element candidate = findByPhysicalRange(achild, offset, forward);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    public static boolean isVirtualNode(Node node) {
        return node.from() == -1 && node.to() == -1;
    }

    private static Element findByLogicalRange(Node node, int offset, boolean forward) {
        for (Element child : node.children()) {
            Node achild = (Node) child;
            if (isVirtualNode(achild)) {
                //we need to recurse into every virtual branch blindly hoping there might by some
                //real nodes fulfilling our constrains
                Element n = findByLogicalRange(achild, offset, forward);
                if (n != null) {
                    return n;
                }
            }
            if (matchesNodeRange(achild, offset, forward, false)) {
                return findByLogicalRange(achild, offset, forward);
            }
        }
        return isVirtualNode(node) ? null : node;
    }

    private static boolean matchesNodeRange(Node node, int offset, boolean forward, boolean physicalNodeRangeOnly) {
        int lf, lt;
        switch (node.type()) {
            case OPEN_TAG:
//            case END_TAG:
                OpenTag t = (OpenTag) node;
                lf = t.logicalRange()[0];
                lt = t.logicalRange()[1];
                break;
            default:
                lf = node.from();
                lt = node.to();
        }

        int from = physicalNodeRangeOnly || lf == -1 ? node.from() : lf;
        int to = physicalNodeRangeOnly || lt == -1 ? node.to() : lt;

        if (forward) {
            if (offset >= from && offset < to) {
                return true;
            }
        } else {
            if (offset > from && offset <= to) {
                return true;
            }
        }
        return false;
    }

    public static String dumpTree(Element node) {
        return dumpTree(node, (CharSequence) null);
    }

    public static String dumpTree(Element node, CharSequence source) {
        StringBuffer buf = new StringBuffer();
        dumpTree(node, buf, source);
        System.out.println(buf.toString());
        return buf.toString();
    }

    public static void dumpTree(Element node, StringBuffer buf) {
        dumpTree(node, buf, null);
    }

    public static void dumpTree(Element node, StringBuffer buf, CharSequence source) {
        dump(node, "", buf, source);
    }

    private static void dump(Element element, String prefix, StringBuffer buf, CharSequence source) {
        buf.append(prefix);
        buf.append(element.toString());
        if (source != null && element.from() != -1 && element.to() != -1) {
            buf.append(" (");
            buf.append(source.subSequence(element.from(), element.to()));
            buf.append(")");
        }
        buf.append('\n');
        if(element instanceof Node) {
            Node node = (Node)element;
            for (Element child : node.children()) {
                dump(child, prefix + INDENT, buf, source);
            }
        }
    }

    public static Node getRoot(Node node) {
        for (;;) {
            if (node.parent() == null) {
                return node;
            } else {
                node = node.parent();
            }
        }
    }

    /**
     * Returns a list of all ancestors of the given node matching the filter.
     * Closest ancestors are at the beginning of the list.
     */
    public static List<Node> getAncestors(Node node, ElementFilter filter) {
        List<Node> matching = new ArrayList<Node>();
        Node n = node;
        do {
            if (filter.accepts(n)) {
                matching.add(n);
            }

            n = n.parent();
        } while (n != null);

        return matching;
    }

    public static List<Element> getChildrenRecursivelly(Element element, ElementFilter filter, boolean recurseOnlyMatching) {
        List<Element> matching = new ArrayList<Element>();
        getChildrenRecursivelly(matching, element, filter, recurseOnlyMatching);
        return matching;
    }

    private static void getChildrenRecursivelly(List<Element> found, Element element, ElementFilter filter, boolean recurseOnlyMatching) {
        if(!(element instanceof Node)) {
            return ;
        }
        
        Node node = (Node)element;
        for (Element child : node.children()) {
            if (filter.accepts(child)) {
                found.add(child);
                getChildrenRecursivelly(found, child, filter, recurseOnlyMatching);
            } else {
                if (!recurseOnlyMatching) {
                    getChildrenRecursivelly(found, child, filter, recurseOnlyMatching);
                }
            }
        }
    }

    public static Element query(Node base, String path) {
        return query(base, path, false);
    }

    /**
     * find an Node according to the path example of path: html/body/table|2/tr
     * -- find a second table tag in body tag
     *
     * note: queries OPEN TAGS ONLY!
     */
    public static Element query(Node base, String path, boolean caseInsensitive) {
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
            OpenTag foundLocal = null;
            for (Element child : found.children(ElementType.OPEN_TAG)) {
                OpenTag openTag = (OpenTag) child;
                if(LexerUtils.equals(openTag.name(), nodeName, caseInsensitive, false) && count++ == index) {
                    foundLocal = openTag;
                    break;
                }
            }
            if (foundLocal != null) {
                found = foundLocal;

                if (!st.hasMoreTokens()) {
                    //last token, we may return
                    OpenTag openTag = (OpenTag)found;
                    assert LexerUtils.equals(openTag.name(), nodeName, false, false);
                    return found;
                }

            } else {
                return null; //no found
            }
        }

        return null;
    }

    public static boolean isDescendant(Node ancestor, Node descendant) {
        if (ancestor == descendant) {
            return false;
        }
        Node node = descendant;
        while ((node = node.parent()) != null) {
            if (ancestor == node) {
                return true;
            }
        }
        return false;
    }

    public static void visitChildren(Element element, ElementVisitor visitor, ElementType nodeType) {
        if(!(element instanceof Node)) {
            return ;
        }
        Node node = (Node)element;
        for (Element n : node.children()) {
            if (nodeType == null || n.type() == nodeType) {
                visitor.visit(n);
            }
            visitChildren(n, visitor, nodeType);
        }
    }

    public static void visitChildren(Node node, ElementVisitor visitor) {
        visitChildren(node, visitor, null);
    }

    public static void visitAncestors(Element node, ElementVisitor visitor) {
        Node parent = (Node) node.parent();
        if (parent != null) {
            visitor.visit(parent);
            visitAncestors(parent, visitor);
        }
    }
}

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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.visualweb.designer.jsf.text;

import java.util.Arrays;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.insync.faces.Entities;
import org.openide.ErrorManager;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * XXX Moved from designer/../Position.
 *
 * Represents a location within a DOM document. The position is defined
 * the same as the w3c Range description of a boundary point:
 *  http://www.w3.org/TR/DOM-Level-2-Traversal-Range/
 *
 * @todo Rename to DomPosition? There are many other Position classes.
 *
 * @author  Tor Norbye
 */
/*public*/ class DomPositionImpl implements DomPosition {
    /**
     *  Special position which represents an uninitialized, unknown or
     *  nonexistent position in any document.
     */
//    public static final Position NONE = new Position(null, -1, Bias.FORWARD);
    
    private final DomDocumentImpl domDocumentImpl;
    private final Node node;
    private final int offset;
    private final Bias bias;

    /** Create a new position at the given node and offset. Note that the node is
     * the PARENT of the node you're pointing to - read the DOM traversal document
     * referenced at the top of this class' javadoc. Text nodes are special handled;
     * in this case the node points to the text node and the offset to a character
     * within the text node. */
    static DomPosition create(DomDocumentImpl domDocumentImpl, Node node, int offset, Bias bias) {
        if (domDocumentImpl == null || node == null) {
            return NONE;
        }
        return new DomPositionImpl(domDocumentImpl, node, offset, bias);
    }
    
    private /*public*/ DomPositionImpl(DomDocumentImpl domDocumentImpl, Node node, int offset, Bias bias) {
        if (domDocumentImpl == null || node == null) {
            throw new NullPointerException(
                    "Parameters domDocumentImpl or node may not be null" +
                    ", domDocumentImpl=" + domDocumentImpl + ", node=" + node); // NOI18N
        }
        this.domDocumentImpl = domDocumentImpl;
        this.node = node;
        this.offset = offset;
        this.bias = bias;
    }

//    // XXX is this a mistake? Should I make positions immutable?? They are hardly ever
//    // mutated... See setLocation too
//    public void setOffset(int offset) {
//        this.offset = offset;
//    }

    /**
     * Return the bias of the position. Positions are always between nodes or
     * characters; this defines which side we have an affinity to.
     */
    public Bias getBias() {
        return bias;
    }

    /** Create a position for the given node. This will be the Node's PARENT
     * node plus its index in that parent's node list.
     * If after is true, the position should point to the position AFTER
     * this element.
     */
//    public static Position create(Node node, boolean after) {
    static DomPosition createNext(DomDocumentImpl domDocumentImpl, Node node, boolean after) {
        if (node == null) {
            return NONE;
        }

        if (node.getNodeType() == Node.TEXT_NODE) {
            return new DomPositionImpl(domDocumentImpl, node, after ? node.getNodeValue().length() : 0,
                after ? Bias.BACKWARD : Bias.FORWARD);
        } else {
            Node parent = node.getParentNode();
            
            if (parent == null) {
                return NONE;
            }
            
            int index = -1;

            while (node != null) {
                node = node.getPreviousSibling();
                index++;
            }

            if (after) {
                index++;
            }

            return new DomPositionImpl(domDocumentImpl, parent, index, after ? Bias.BACKWARD : Bias.FORWARD);
        }
    }

//    public static Position create(Node node, Bias bias) {
//        Node parent = node.getParentNode();
//        int index = -1;
//
//        while (node != null) {
//            node = node.getPreviousSibling();
//            index++;
//        }
//
//        if (bias == Bias.BACKWARD) {
//            index--;
//
//            if (index < 0) {
//                index = 0;
//                bias = Bias.FORWARD;
//            }
//        }
//
//        return new Position(parent, index, bias);
//    }

//    // XXX is this a mistake? Should I make positions immutable?? They are hardly ever
//    // mutated... See setOffset too
//    void setLocation(Node node, int offset, Bias bias) {
//        this.node = node;
//        this.offset = offset;
//        this.bias = bias;
//    }

    /**
     * Fetches the current offset
     *
     * @return the offset >= 0
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Fetches the element/node that contains the position
     *
     * @return the element/node
     */
    public Node getNode() {
        return node;
    }

    /** Return true if this position points to some position inside the
     * given element. Pointing to either side of the element is not considered
     * inside.
     * @todo XXX THIS NEEDS A UNIT TEST!
     */
    public boolean isInside(Element element) {
        if (this == DomPositionImpl.NONE) {
            return false;
        }

        // Compute positions before and after the element, then see
        // if "this" is after/equals to the before and before/equal to the after.
        Node curr = element;
        Node parent = element.getParentNode();
        
        if (parent == null) {
            // #116200 Possible NPE.
            return false;
        }
        
        int index = -1;
        while (curr != null) {
            curr = curr.getPreviousSibling();
            index++;
        }

        if ((compareBoundaryPoints(parent, index, node, offset) > 0) &&
                (compareBoundaryPoints(parent, index + 1, node, offset) < 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  Return true iff this position is earlier than (or at the same position
     *  as) the given position in the document
     *  @param pos The position to compare to
     *  @return True iff this position is earlier than or equal to the
     *     given position
     */
//    public boolean isEarlierThan(Position pos) {
    public boolean isEarlierThan(DomPosition pos) {
        return compareBoundaryPoints(pos, this) <= 0;
    }

    /**
     *  Return true iff this position is later than (or at the same position
     *  as) the given position in the document
     *  @param pos The position to compare to
     *  @return True iff this position is later than or equal to the
     *     given position
     */
//    public boolean isLaterThan(Position pos) {
    public boolean isLaterThan(DomPosition pos) {
        return compareBoundaryPoints(pos, this) >= 0;
    }

    /**
     *  Return true iff this position is earlier than (but NOT at the same position
     *  as) the given position in the document
     *  @param pos The position to compare to
     *  @return True iff this position is strictly earlier than (e.g. not
     *  equal to) the given position
     */
//    public boolean isStrictlyEarlierThan(Position pos) {
    public boolean isStrictlyEarlierThan(DomPosition pos) {
        return compareBoundaryPoints(pos, this) < 0;
    }

    /**
     *  Return true iff this position is later than (but NOT at the same position
     *  as) the given position in the document
     *  @param pos The position to compare to
     *  @return True iff this position is strictly later than (e.g. not
     *  equal to) the given position
     */
//    public boolean isStrictlyLaterThan(Position pos) {
    public boolean isStrictlyLaterThan(DomPosition pos) {
        return compareBoundaryPoints(pos, this) > 0;
    }

    public boolean equals(Object object) {
        if (object instanceof DomPositionImpl) {
//            return ((getNode() == ((Position)pos).getNode())
//            && (getOffset() == ((Position)pos).getOffset()));
            DomPositionImpl pos = (DomPositionImpl)object;
            // XXX Should be equals used as well? And bias too?
            return getNode() == pos.getNode() && getOffset() == pos.getOffset();
//                    && getBias() == pos.getBias();
        }
        return false;
    }

    public int hashCode() { // XXX is this the right signature?
//        return getOffset() * getNode().hashCode();
        return Arrays.hashCode(new Object[] {getNode(), getOffset() /*, getBias()*/});
    }

//    public static short compareBoundaryPoints(Position a, Position b) {
    public static short compareBoundaryPoints(DomPosition a, DomPosition b) {
        assert (a != NONE) && (b != NONE);

//        Node endPointA = a.node;
//        int offsetA = a.offset;
//        Node endPointB = b.node;
//        int offsetB = b.offset;
        Node endPointA = a.getNode();
        int offsetA = a.getOffset();
        Node endPointB = b.getNode();
        int offsetB = b.getOffset();

        return compareBoundaryPoints(endPointA, offsetA, endPointB, offsetB);
    }

    // This method from Xerces' RangeImpl.java
    public static short compareBoundaryPoints(Node endPointA, int offsetA, Node endPointB,
        int offsetB) {
        // XXX do special handling of Position.NONE
        //Log.err.log("compareBoundaryPoints(" + a +", " + b + ")");
        // The DOM Spec outlines four cases that need to be tested
        // to compare two range boundary points:
        //   case 1: same container
        //   case 2: Child C of container A is ancestor of B
        //   case 3: Child C of container B is ancestor of A
        //   case 4: preorder traversal of context tree.
        // case 1: same container
        if (endPointA == endPointB) {
            if (offsetA < offsetB) {
                return 1;
            }

            if (offsetA == offsetB) {
                return 0;
            }

            return -1;
        }

        // case 2: Child C of container A is ancestor of B
        // This can be quickly tested by walking the parent chain of B
        for (Node c = endPointB, p = c.getParentNode(); p != null; c = p, p = p.getParentNode()) {
            if (p == endPointA) {
                int index = indexOf(c, endPointA);

                if (offsetA <= index) {
                    return 1;
                }

                return -1;
            }
        }

        // case 3: Child C of container B is ancestor of A
        // This can be quickly tested by walking the parent chain of A
        for (Node c = endPointA, p = c.getParentNode(); p != null; c = p, p = p.getParentNode()) {
            if (p == endPointB) {
                int index = indexOf(c, endPointB);

                if (index < offsetB) {
                    return 1;
                }

                return -1;
            }
        }

        // case 4: preorder traversal of context tree.
        // Instead of literally walking the context tree in pre-order,
        // we use relative node depth walking which is usually faster
        int depthDiff = 0;

        for (Node n = endPointA; n != null; n = n.getParentNode())
            depthDiff++;

        for (Node n = endPointB; n != null; n = n.getParentNode())
            depthDiff--;

        while (depthDiff > 0) {
            endPointA = endPointA.getParentNode();
            depthDiff--;
        }

        while (depthDiff < 0) {
            endPointB = endPointB.getParentNode();
            depthDiff++;
        }

        for (Node pA = endPointA.getParentNode(), pB = endPointB.getParentNode(); pA != pB;
                pA = pA.getParentNode(), pB = pB.getParentNode()) {
            endPointA = pA;
            endPointB = pB;
        }

        for (Node n = endPointA.getNextSibling(); n != null; n = n.getNextSibling()) {
            if (n == endPointB) {
                return 1;
            }
        }

        return -1;
    }

    // Also from Xerces:

    /** what is the index of the child in the parent */
    private static int indexOf(Node child, Node parent) {
        if (child.getParentNode() != parent) {
            return -1;
        }

        int i = 0;

        for (Node node = parent.getFirstChild(); node != child; node = node.getNextSibling()) {
            i++;
        }

        return i;
    }

    /**
     *  Return the position which is earliest in the document: a or b.
     *  @todo Document behavior where one or both of the positions are Position.NONE
     *  @param a The first position to compare
     *  @param b The second position to compare
     *  @return The position which is earliest in the document: a or b
     */
//    public static Position first(Position a, Position b) {
    public static DomPosition first(DomPosition a, DomPosition b) {
        if (a == NONE) {
            return b;
        } else if (b == NONE) {
            return a;
        }

        if (a.isEarlierThan(b)) {
            return a;
        } else {
            return b;
        }
    }

    /**
     *  Return the position which is latest in the document: a or b.
     *  @todo Document behavior where one or both of the positions are Position.NONE
     *  @param a The first position to compare
     *  @param b The second position to compare
     *  @return The position which is latest in the document: a or b
     */
//    public static Position last(Position a, Position b) {
    public static DomPosition last(DomPosition a, DomPosition b) {
        if (a == NONE) {
            return b;
        } else if (b == NONE) {
            return a;
        }

        if (a.isLaterThan(b)) {
            return a;
        } else {
            return b;
        }
    }

    public String toString() {
        if (this == NONE) {
            return "Position.NONE";
        }

        Node curr = node;

        while (curr.getParentNode() != null) {
            curr = curr.getParentNode();
        }

        String type;

        if (curr instanceof DocumentFragment) {
            type = "FRAG";
        } else {
            type = "DOC";
        }

        if (node instanceof Text) {
            String str = node.getNodeValue();
            String before = str.substring(0, offset);
            String after = str.substring(offset);

//            return "Position-" + type + "(rendered=" + MarkupService.isRenderedNode(node) + ",[#text:" + offset +
            return "Position-" + type + "(rendered=" + domDocumentImpl.isRenderedNode(node) + ",[#text:" + offset +
            ": Bias=" + bias + "; " + before + "^" + after + "])";
        } else {
            NodeList nl = node.getChildNodes();
            String description = "";

            if (offset < nl.getLength()) {
                description = " (before " + nl.item(offset) + ")";

                if (offset > 0) {
                    description = description + " (after " + nl.item(offset - 1) + ")";
                }
            } else if ((offset == nl.getLength()) && (offset > 0)) {
                description = " (after " + nl.item(offset - 1) + ")";
            }

//            return "Position-" + type + "(rendered=" + MarkupService.isRenderedNode(node) + "," + getNode() + "," +
            return "Position-" + type + "(rendered=" + domDocumentImpl.isRenderedNode(node) + "," + getNode() + "," +
            getOffset() + "):" + bias + "; " + description;
        }
    }

    // Moved to designer/markup
//    /**
//     *  Return true iff the node corresponding to this position is rendered. This is not defined
//     * for the Position.NONE object. By "is rendered" I mean that the position points to a node
//     * in a renderer-hierarchy DOM (such as HTML rendered from JSF components).
//     */
//    public boolean isRendered() {
//        return isRenderedNode(node);
//    }
//
//    public static boolean isRenderedNode(Node n) {
//        if (n == null) { // includes Position.NONE
//
//            return false;
//        }
//
//        if (n instanceof RaveElement) {
//            return ((RaveElement)n).isRendered();
//        }
//
//        if (n instanceof RaveTextElement) {
//            return ((RaveTextElement)n).isRendered();
//        }
//
//        return false;
//    }

//    /** Return true iff the given node is a node in the "source" tree, e.g.
//     * the JSP document. Returns false otherwise - e.g. the element is
//     * in a DocumentFragment. Does not check to see if the Element is in
//     * the "right" document.
//     * @param curr The node to be checked
//     * @param dom The JSPX document DOM
//     */
//    public static boolean isSourceNode(Node curr, org.w3c.dom.Document dom) {
//        return !isRenderedNode(curr);
//        return !MarkupService.isRenderedNode(curr);
//    }

    
    /** Gets whether is it rendered position. Note: <code>NONE</code> is not considered rendered position.
     * XXX There should be only rendered position here (in the designer). */
    public boolean isRenderedPosition() {
        if (this == DomPositionImpl.NONE) {
            return false;
        }
        
//        return MarkupService.isRenderedNode(node);
        return domDocumentImpl.isRenderedNode(node);
    }
    
    /** Gets whether it is source position. Note: <code>NONE</code> is not considered source position.
     * XXX There should be only rendered position here (in the designer). */
    public boolean isSourcePosition() {
        if (this == DomPositionImpl.NONE) {
            return false;
        }
        
        return node == MarkupService.getSourceNodeForNode(node);
    }
    
    /**
     *  Return a position in the rendered DOM that corresponds to this position (which needs to
     *  be a position in a source DOM).
     * @throws UnsupportedOperationException if this is Position.NONE instance
     */
//    public Position getRenderedPosition() {
    public DomPosition getRenderedPosition() {
        // TODO -- cache the rendered position for the caret!!! Perhaps it should be a caret method instead!
//        assert this != Position.NONE;
//        if (this == Position.NONE) {
//        if (this == DomPosition.NONE) {
//            throw new UnsupportedOperationException("Method getRenderedPosition() can't be called on Position.NONE instance!"); // NOI18N
//        }

//        assert !isRendered() : this;
//        if (MarkupService.isRenderedNode(node)) {
        if (domDocumentImpl.isRenderedNode(node)) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Node is expected not rendered, node=" + node)); // NOI18N
        }

        // TODO - rewrite this. Moved from elsewhere and clashing a bit. Use different
        // temp variables than node and offset.
        Node node = this.node;
        NodeList children = node.getChildNodes();
        int offset = this.offset;

        if ((node.getNodeType() == Node.ELEMENT_NODE) ||
                (node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)) {
            if (children.getLength() == 0) {
                // The position points to a -potential- insert position (where there are
                // no nodes yet)
//                Node n = ((RaveRenderNode)node).getRenderedNode();
                Node n = MarkupService.getRenderedNodeForNode(node);

                if (n != null) {
                    return domDocumentImpl.createDomPosition(n, 0, Bias.FORWARD);
                } else {
//                    return NONE;
                    return DomPosition.NONE;
                }
            } else if ((bias == Bias.BACKWARD) && (offset > 0) &&
                    ((offset - 1) < children.getLength())) {
                node = node.getChildNodes().item(offset - 1);

//                return Position.create(((RaveRenderNode)node).getRenderedNode(), true);
                return domDocumentImpl.createNextDomPosition(MarkupService.getRenderedNodeForNode(node), true);
            } else if (offset < children.getLength()) {
                node = node.getChildNodes().item(offset);

//                return Position.create(((RaveRenderNode)node).getRenderedNode(), false);
                return domDocumentImpl.createNextDomPosition(MarkupService.getRenderedNodeForNode(node), false);
            } else {
                // The offset is larger than or equal to the number of children.
                // Use the last child.
                node = node.getChildNodes().item(children.getLength() - 1);

//                return Position.create(((RaveRenderNode)node).getRenderedNode(), true);
                return domDocumentImpl.createNextDomPosition(MarkupService.getRenderedNodeForNode(node), true);
            }

            //      } else if (node.getNodeType() == Node.TEXT_NODE) {
//        } else if (node instanceof RaveText) { // text, cdata section etc. ?
        } else if (node instanceof Text) { // text, cdata section etc. ?

            Text xs = (Text)node;
//            assert !xs.isRendered();
//            if (MarkupService.isRenderedNode(xs)) {
            if (domDocumentImpl.isRenderedNode(xs)) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Node is expected to be not rendered, node=" + xs)); // NOI18N
            }

//            RaveText xr = xs.getRendered();
            Text xr = MarkupService.getRenderedTextForText(xs);

            if (xr == null) {
                // This node has not yet been rendered
                return NONE;
            }

            // Compute the offset position corresponding to "node,offset" 
            // in xt.
//            assert offset <= node.getNodeValue().length();
            if (offset > node.getNodeValue().length()) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Offset is greater than expected, offset=" + offset // NOI18N
                        + ", node.getNodeValue().lenght()=" + node.getNodeValue().length() + ", for node=" + node)); // NOI18N
//                return NONE;
                return DomPosition.NONE;
            }

            int renderOffset;

//            if (xs.isJspx()) {
            if (MarkupService.isJspxNode(xs)) {
                String jspx = xs.getNodeValue();
                String xhtml = xr.getNodeValue();
                if(jspx == xhtml || jspx.indexOf('&') == -1) {
                    renderOffset = offset;
                } else {
                    // <markup_separation>
//                    renderOffset = MarkupServiceProvider.getDefault().
//                            getExpandedOffset(jspx, offset);
                    // ====
//                    renderOffset = InSyncService.getProvider().getExpandedOffset(jspx, offset);
//                    renderOffset = WebForm.getDomProviderService().getExpandedOffset(jspx, offset);
                    renderOffset = Entities.getExpandedOffset(jspx, offset);
                    // </markup_separation>
                }
            } else {
                // Html to Html rendering: no offset change
                renderOffset = offset;
            }

            // Ensure that the offset is valid in the rendered node since the
            // source may be ahead (as in right when you add a new character in a word;
            // we haven't yet updated the UI (that's done in a delayed fashion
            // via the DomSynchronizer)
            int max = xr.getNodeValue().length();

            if (renderOffset > max) {
                renderOffset = max;
            }

            return new DomPositionImpl(domDocumentImpl, xr, renderOffset, bias);
        } else {
            // Not sure how to handle this one
            ErrorManager.getDefault().log("Unexpected node type in getRendered: " + node);

            return DomPositionImpl.NONE;
        }
    }

    /** If the current position is in a rendered/DocumentFragment subtree of the
     * document, try to locate the equivalent position in the source document
     * and return that. If it cannot find an equivalent source
     * position it will return Position.NONE.
     */
//    public Position getSourcePosition() {
    public DomPosition getSourcePosition() {
//        assert this != Position.NONE;

//        assert isRendered() : this;
//        if (!MarkupService.isRenderedNode(node)) {
        if (!domDocumentImpl.isRenderedNode(node)) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Node is expected to be rendered, node=" + node)); // NOI18N
        }

        // TODO - rewrite this. Moved from elsewhere and clashing a bit. Use different
        // temp variables than node and offset.
        Node node = this.node;
        NodeList children = node.getChildNodes();
        int offset = this.offset;

        if ((node.getNodeType() == Node.ELEMENT_NODE) ||
                (node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)) {
            if (children.getLength() == 0) {
                // The position points to a -potential- insert position (where there are
                // no nodes yet)
//                Node n = ((RaveRenderNode)node).getSourceNode();
                Node n = MarkupService.getSourceNodeForNode(node);

                if (n != null) {
                    return new DomPositionImpl(domDocumentImpl, n, 0, Bias.FORWARD);
                } else {
//                    return NONE;
                    return DomPosition.NONE;
                }
            } else if ((bias == Bias.BACKWARD) && (offset > 0) &&
                    ((offset - 1) < children.getLength())) {
                node = node.getChildNodes().item(offset - 1);

                Node curr = node;

//                while (curr instanceof RaveRenderNode) {
//                    Node source = ((RaveRenderNode)curr).getSourceNode();
                while (curr != null) {
                    Node source = MarkupService.getSourceNodeForNode(curr);

                    if (source != null) {
                        return domDocumentImpl.createNextDomPosition(source, true);
                    }

                    curr = curr.getParentNode();
                }

//                return Position.NONE;
                return DomPosition.NONE;
            } else if (offset < children.getLength()) {
                node = node.getChildNodes().item(offset);

                Node curr = node;

//                while (curr instanceof RaveRenderNode) {
//                    Node source = ((RaveRenderNode)curr).getSourceNode();
                while (curr != null) {
                    Node source = MarkupService.getSourceNodeForNode(curr);

                    if (source != null) {
                        return domDocumentImpl.createNextDomPosition(source, false);
                    }

                    curr = curr.getParentNode();
                }

                return DomPositionImpl.NONE;
            } else {
                // The offset is larger than or equal to the number of children.
                // Use the last child.
                node = node.getChildNodes().item(children.getLength() - 1);

                Node curr = node;

//                while (curr instanceof RaveRenderNode) {
//                    Node source = ((RaveRenderNode)curr).getSourceNode();
                while (curr != null) {
                    Node source = MarkupService.getSourceNodeForNode(curr);

                    if (source != null) {
                        return domDocumentImpl.createNextDomPosition(source, true);
                    }

                    curr = curr.getParentNode();
                }

                return DomPositionImpl.NONE;
            }

            //      } else if (node.getNodeType() == Node.TEXT_NODE) {
//        } else if (node instanceof RaveText) { // text, cdata section etc. ?
        } else if (node instanceof Text) { // text, cdata section etc. ?

            Text xr = (Text)node;
//            assert xr.isRendered();
//            if (!MarkupService.isRenderedNode(xr)) {
            if (!domDocumentImpl.isRenderedNode(xr)) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Node is expected to be rendered, node=" + node)); // NOI18N
            }

//            RaveText xs = xr.getSource();
            Text xs = MarkupService.getSourceTextForText(xr);

            if (xs == null) {
                // This node has not yet been rendered
                return NONE;
            }

            // Compute the offset position corresponding to "node,offset" 
            // in xt.
            assert offset <= xr.getNodeValue().length();

            int sourceOffset;

//            if (xs.isJspx()) {
            if (MarkupService.isJspxNode(xs)) {
                String jspx = xs.getNodeValue();
                String xhtml = xr.getNodeValue();
                if(jspx == xhtml || jspx.indexOf('&') == -1) {
                    sourceOffset = offset;
                } else {
                    // <markup_separation>
//                    sourceOffset = MarkupServiceProvider.getDefault().
//                            getUnexpandedOffset(jspx, offset);
                    // ====
//                    sourceOffset = InSyncService.getProvider().getUnexpandedOffset(jspx, offset);
//                    sourceOffset = WebForm.getDomProviderService().getUnexpandedOffset(jspx, offset);
                    sourceOffset = Entities.getUnexpandedOffset(jspx, offset);
                    // </markup_separation>
                }
            } else {
                // Html to Html rendering: no offset change
                sourceOffset = offset;
            }

            // Ensure that the offset is valid in the rendered node since the
            // source may be ahead (as in right when you add a new character in a word;
            // we haven't yet updated the UI (that's done in a delayed fashion
            // via the DomSynchronizer)
            int max = xs.getNodeValue().length();

            if (sourceOffset > max) {
                sourceOffset = max;
            }

            return new DomPositionImpl(domDocumentImpl, xs, sourceOffset, bias);
        } else {
            // Not sure how to handle this one
            ErrorManager.getDefault().log("Unexpected node type in getSource: " + node);

            return DomPositionImpl.NONE;
        }
    }

    /** If the position is pointing to a specific element (e.g. right before or right after),
     * return it. Otherwise, return null.
     * @return The element pointed to by this position, or null
     */
    public Element getTargetElement() {
        if ((this == NONE) || node instanceof Text) {
            return null;
        }

        NodeList nl = node.getChildNodes();

        Node target = null;

        if (bias == Bias.FORWARD) {
            if (offset < nl.getLength()) {
                target = nl.item(offset);
            } else if ((offset == nl.getLength()) && (offset > 0)) {
                target = nl.item(offset - 1);
            }
        } else if (offset > 0) { // bias is backwards
            target = nl.item(offset - 1);
        }

//        if (target instanceof RaveElement) {
//            return (RaveElement)target;
//        }
        if (target instanceof Element) {
            return (Element)target;
        }

        return null;
    }
}

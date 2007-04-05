/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.designer.jsf.text;


import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;

import org.openide.ErrorManager;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.DocumentRange;


/**
 * XXX Moved from designer/../Range.
 *
 * Represents a range: two positions, where one position
 * is considered the "dot" (current location). E.g. if you
 * swipe select out a range, the "start" point of the selection
 * is the mark, and the "end" point is the mark.
 *
 * <p> Implementation notes: I first used DOM Range directly,
 * but that doesn't work well for example within rendered jsf
 * fragments, since the Range can only be attached to the Document
 * itself; it also had no concept of dot and mark, so I was having
 * to track this state separately along with the range, and there's
 * some cleanup logic to be done, so all this is now managed by
 * this class.
 *
 * @todo Rename class to SelectionRange, since it's not a generic Range
 * class (like Position is a generic Position class), it's really tied
 * to selection, both in its "dot" and "mark" distinctions as well as
 * its synchronization to the system clipboard etc.
 *
 * @author  Tor Norbye
 */
/*public*/ class DomRangeImpl implements DomProvider.DomRange {
//    private WebForm webform;
    private final JsfForm jsfForm;

    /** Associated DOM range. Will be null for e.g. document
     * ranges.
     */
    private org.w3c.dom.ranges.Range domRange;

    /** Tracks whether or not the domRange has the dot as the start
     * container or the end container (true means start container) */
    private boolean dotIsFirst;

    // Should these be static? Means we can't have multiple simultaneous
    // ranges - but that's currently how it's used anyway.... perhaps I
    // should ENFORCE that by making this a singleton class!
    // Yes, ... but for now just unstatic it 'til I get time to address this
//    private /* static */ Position cachedDot = new Position(null, 0, Bias.FORWARD);
//    private /* static */ Position cachedMark = new Position(null, 0, Bias.FORWARD);
//    private /* static */ Position cachedDot = Position.create(null, 0, Bias.FORWARD);
//    private /* static */ Position cachedMark = Position.create(null, 0, Bias.FORWARD);
    private DomPosition cachedDot = DomPositionImpl.create(null, 0, Bias.FORWARD);
    private DomPosition cachedMark = DomPositionImpl.create(null, 0, Bias.FORWARD);
    
    private Bias dotBias = Bias.FORWARD;
    private Bias markBias = Bias.FORWARD;

    public static DomRangeImpl create(/*WebForm webForm,*/ JsfForm jsfForm, Node dotNode, int dotOffset, Node markNode, int markOffset) {
        return new DomRangeImpl(/*webForm,*/ jsfForm, dotNode, dotOffset, markNode, markOffset);
    }
    
    private DomRangeImpl(/*WebForm webform,*/ JsfForm jsfForm, Node dotNode, int dotOffset, Node markNode, int markOffset) {
//        this.webform = webform;
        this.jsfForm = jsfForm;

        // Determine if this node is in a DocumentFragment which means
        // it's read only
        Node curr = dotNode;

        while (curr.getParentNode() != null) {
            curr = curr.getParentNode();
        }

//        if (curr == webform.getJspDom()) {
        if (curr == jsfForm.getJspDom()) {
            // It's below the main document dom node so it's part of
            // the writable document portion
//            DocumentRange dom = (DocumentRange)webform.getJspDom();
            DocumentRange dom = (DocumentRange)jsfForm.getJspDom();
            
            domRange = dom.createRange();
            domRange.setStart(markNode, markOffset);
            domRange.setEnd(dotNode, dotOffset);
//        } else if (webform.getManager().isInlineEditing()) {
        } else if (jsfForm.isInlineEditing()) {
            // Handle regions in generated dom
            assert curr instanceof DocumentFragment;

            //assert webform.getSelection().getInlineEditor().getFragment() == curr;
            DocumentRange dom = (DocumentRange)curr.getOwnerDocument();
            domRange = dom.createRange();
            domRange.setStart(markNode, markOffset);
            domRange.setEnd(dotNode, dotOffset);
        } else {
//            assert false : dotNode + "; " + curr + "; " + webform.getJspDom(); // NOI18N
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("dotNode=" + dotNode + ", curr=" + curr + ", jsp dom=" + webform.getJspDom())); // NOI18N
                    new IllegalStateException("dotNode=" + dotNode + ", curr=" + curr + ", jsp dom=" + jsfForm.getJspDom())); // NOI18N
        }

        dotIsFirst = false;
        ensureMarkIsFirst();
    }

    /** Free up resources associated with this range */
    public void detach() {
        if (domRange != null) {
            domRange.detach();
            domRange = null;
        }
    }

    public void setRange(Node start, int startOffset, Node end, int endOffset) {
        // Is it safe to reuse the range?  If it isn't, we try
        // to change the caret over from a DocumentFragment to a Document
        // or vice versa, after setting one end point, xerces throws an
        // exception because the range straddles documents. There's no Range
        // method to set both points unfortunately so we've gotta work around
        // as follows.
        if (start == end) {
            domRange.setEnd(end, endOffset);
            domRange.setStart(start, startOffset);
        } else {
            domRange.detach();

//            DocumentRange dom = (DocumentRange)webform.getJspDom();
            DocumentRange dom = (DocumentRange)jsfForm.getJspDom();
            domRange = dom.createRange();
            domRange.setStart(end, endOffset);
            domRange.setEnd(start, startOffset);
            dotIsFirst = false;
        }

        ensureMarkIsFirst();

        if (dotIsFirst) {
            dotBias = Bias.FORWARD;
            markBias = Bias.BACKWARD;
        } else {
            markBias = Bias.FORWARD;
            dotBias = Bias.BACKWARD;
        }
    }

    public void setDot(Node dotNode, int dotOffset, Bias bias) {
        if (dotIsFirst) {
            domRange.setStart(dotNode, dotOffset);
        } else {
            domRange.setEnd(dotNode, dotOffset);
        }

        dotBias = bias;
        ensureMarkIsFirst();
    }

    public void setMark(Node markNode, int markOffset, Bias bias) {
        if (dotIsFirst) {
            domRange.setEnd(markNode, markOffset);
        } else {
            domRange.setStart(markNode, markOffset);
        }

        markBias = bias;
        ensureMarkIsFirst();
    }

    /** Ensure that the mark location is before the dot location in
     * the range. If not, it will swap the two and flip the "dotIsFirst"
     * flag. (So getMark() will correctly return a mark location after the
     * dot location, if you've swiped selection towards the left, but
     * internally the range end container is now representing the mark.)
     */
    private void ensureMarkIsFirst() {
        int result =
            DomPositionImpl.compareBoundaryPoints(domRange.getStartContainer(), domRange.getStartOffset(),
                domRange.getEndContainer(), domRange.getEndOffset());

        if (result < 0) {
            // Swap
            Node tempNode = domRange.getStartContainer();
            int tempOffset = domRange.getStartOffset();
            domRange.setStart(domRange.getEndContainer(), domRange.getEndOffset());
            domRange.setEnd(tempNode, tempOffset);
            dotIsFirst = !dotIsFirst;
        }
    }

    /** Return true iff the given position coincides with the dot position */
//    public boolean isDot(Position dot) {
    public boolean isDot(DomPosition dot) {
        if (dotIsFirst) {
            return (dot.getNode() == domRange.getStartContainer()) &&
            (dot.getOffset() == domRange.getStartOffset());
        } else {
            return (dot.getNode() == domRange.getEndContainer()) &&
            (dot.getOffset() == domRange.getEndOffset());
        }
    }

    /** Return true iff the range is empty, e.g. the dot and mark are the same */
    public boolean isEmpty() {
        return (domRange.getStartContainer() == domRange.getEndContainer()) &&
        (domRange.getStartOffset() == domRange.getEndOffset());
    }

    public String toString() {
        return "Range(dot=" + getDot() + ";" + getMark() + ")"; // NOI18N
    }

    /**
     * Fetches the current position of the caret.
     *
     * @return the position >= 0
     */
//    public Position getDot() {
    public DomPosition getDot() {
        //boolean empty = isEmpty();
        boolean empty = true;

        if (dotIsFirst) {
//            cachedDot.setLocation(domRange.getStartContainer(), domRange.getStartOffset(),
//                empty ? dotBias : Bias.FORWARD);
            cachedDot = DomPositionImpl.create(domRange.getStartContainer(), domRange.getStartOffset(), empty ? dotBias : Bias.FORWARD);
        } else {
//            cachedDot.setLocation(domRange.getEndContainer(), domRange.getEndOffset(),
//                empty ? dotBias : Bias.BACKWARD);
            cachedDot = DomPositionImpl.create(domRange.getEndContainer(), domRange.getEndOffset(), empty ? dotBias : Bias.FORWARD);
        }

        return cachedDot;
    }

    /** Return the first endpoint of the range in the document. */
//    public Position getFirstPosition() {
    public DomPosition getFirstPosition() {
        if (dotIsFirst) {
            return getDot();
        } else {
            return getMark();
        }
    }

    /** Return the second/last endpoint of the range in the document. */
//    public Position getLastPosition() {
    public DomPosition getLastPosition() {
        if (dotIsFirst) {
            return getMark();
        } else {
            return getDot();
        }
    }

    /**
     * Fetches the current position of the caret.
     *
     * @return the position >= 0
     */
//    public Position getMark() {
    public DomPosition getMark() {
        //boolean empty = isEmpty();
        boolean empty = true;

        if (dotIsFirst) {
//            cachedMark.setLocation(domRange.getEndContainer(), domRange.getEndOffset(),
//                empty ? markBias : Bias.BACKWARD);
            cachedMark = DomPositionImpl.create(domRange.getEndContainer(), domRange.getEndOffset(), empty ? markBias : Bias.FORWARD);
        } else {
//            cachedMark.setLocation(domRange.getStartContainer(), domRange.getStartOffset(),
//                empty ? markBias : Bias.FORWARD);
            cachedMark = DomPositionImpl.create(domRange.getStartContainer(), domRange.getStartOffset(), empty ? markBias : Bias.FORWARD);
        }

        return cachedMark;
    }

    // ---- Mutation Events

    /** Delete the contents of the selection. Beep if the range is read-only.
     *  @return true iff the deletion succeeded (an empty selection can always
     *   be successfully deleted)
     */
    public boolean deleteRangeContents() {
        if (isEmpty()) {
            return true;
        }

        if (domRange != null) {
            // Somehow the range does "bad" stuff with deletion - sometimes
            // the start container ends up being set back up to the parent!
            // Notagood. So we try to preserve it instead.
            Node sc = domRange.getStartContainer();
            int so = domRange.getStartOffset();
            Node ec = domRange.getEndContainer();
            int eo = domRange.getEndOffset();

////            if (webform.getManager().isInlineEditing() ||
//            if (jsfForm.isInlineEditing() ||
////                    (Position.isSourceNode(sc, webform.getDom()) &&
////                    Position.isSourceNode(ec, webform.getDom()))) {
//            (!MarkupService.isRenderedNode(sc)
//            && !MarkupService.isRenderedNode(ec))) {
//                deleteComponents();
//                domRange.deleteContents();
//            } else {
//                Node sourceStart = null;
//                int sourceOffset = so;
//
////                if (Position.isRenderedNode(sc)) {
//                if (MarkupService.isRenderedNode(sc)) {
////                    Position source = new Position(sc, so, Bias.FORWARD);
//                    DomPositionImpl source = DomPositionImpl.create(sc, so, Bias.FORWARD);
//
//                    if (source != DomPositionImpl.NONE) {
//                        sourceStart = source.getNode();
//                        sourceOffset = source.getOffset();
//                    }
//                }
//
//                Node sourceEnd = null;
//                int sourceEndOffset = eo;
//
////                if (Position.isRenderedNode(ec)) {
//                if (MarkupService.isRenderedNode(ec)) {
////                    Position source = new Position(ec, eo, Bias.FORWARD);
//                    DomPositionImpl source = DomPositionImpl.create(ec, eo, Bias.FORWARD);
//
//                    if (source != DomPositionImpl.NONE) {
//                        sourceEnd = source.getNode();
//                        sourceEndOffset = source.getOffset();
//                    }
//                }
//
//                if ((sourceStart != null) && (sourceEnd != null)) {
//                    setRange(sourceStart, sourceOffset, sourceEnd, sourceEndOffset);
//                    deleteComponents();
//                    domRange.deleteContents();
//                }
//            }

            // XXX Moved to DomDocumentImpl.
//            // XXX For now it works only over the source nodes. That has to be changes.
//            if (!jsfForm.isInlineEditing() && (MarkupService.isRenderedNode(sc) || MarkupService.isRenderedNode(ec))) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new IllegalStateException("It is not inline editing, nor both nodes are source ones," // NOI18N
//                            + "\nstartNode=" + sc // NOI18N
//                            + "\nendNode=" + ec)); // NOI18N
//                return false;
//            }
//            deleteComponents();
            domRange.deleteContents();

            domRange.setStart(sc, so);
            domRange.setEnd(sc, so);

            return true;
        } else {
            return false;
        }
    }

//    /** Delete all the JSF components found in the given range */
//    private void deleteComponents() {
//        // This will require a traversal, but probably not using the
//        // DomTraversal class since we'll be deleting elements as
//        // we're traversing
////        Position second = getLastPosition();
//        DomPosition second = getLastPosition();
//
////        if (second == Position.NONE) {
//        if (second == DomPosition.NONE) {
//            return;
//        }
//
////        Position first = getFirstPosition();
//        DomPosition first = getFirstPosition();
//        assert first.isEarlierThan(first);
//
//        Node firstNode = first.getNode();
//
//        if (firstNode instanceof Element) {
//            if (first.getOffset() < firstNode.getChildNodes().getLength()) {
//                firstNode = firstNode.getChildNodes().item(first.getOffset());
//            }
//        }
//
//        Node secondNode = second.getNode();
//
//        if (first.equals(second)) {
//            secondNode = firstNode;
//        } else if (secondNode instanceof Element) {
//            if ((second.getOffset() > 0) &&
//                    (second.getOffset() <= secondNode.getChildNodes().getLength())) {
//                secondNode = secondNode.getChildNodes().item(second.getOffset() - 1);
//            } else if (second.getOffset() == 0) {
//                // Gotta locate immediate inorder traversal neighbor to the left
//                while ((secondNode != null) && (secondNode.getPreviousSibling() == null)) {
//                    secondNode = secondNode.getParentNode();
//                }
//
//                if (secondNode == null) {
//                    ErrorManager.getDefault().log("Unexpected second position " + second); // NOI18N
//
//                    return;
//                }
//
//                secondNode = secondNode.getPreviousSibling();
//
//                while (true) {
//                    NodeList nl = secondNode.getChildNodes();
//
//                    if (nl.getLength() > 0) {
//                        secondNode = nl.item(nl.getLength() - 1);
//                    } else {
//                        break;
//                    }
//                }
//            }
//        }
//
//        // Insert content for the first node
//        if ((firstNode == secondNode) && firstNode instanceof Text) {
//            // Common case - and we're done; no components to be deleted here
//            return;
//        }
//
//        // Iterate over the range building up all the DesignBeans to be
//        // destroyed
////        ArrayList beans = new ArrayList();
//        List<Element> components = new ArrayList<Element>();
//
////        org.w3c.dom.Document dom = webform.getJspDom();
//        org.w3c.dom.Document dom = jsfForm.getJspDom();
//
//        if (!(dom instanceof DocumentTraversal)) {
//            return;
//        }
//
//        DocumentTraversal trav = (DocumentTraversal)dom;
//
//        // Iterating over all since we can't just limit ourselves to text nodes
//        // in case the target node is not necessarily a text node!
//        NodeIterator iterator = trav.createNodeIterator(dom, NodeFilter.SHOW_ALL, null, false);
//
//        // The node iterator doesn't seem to have a way to jump to a
//        // particular node, so we search for it ourselves
//        Node curr = firstNode;
//
//        while (curr != null) {
//            try {
//                curr = iterator.nextNode();
//
//                if (curr == firstNode) {
//                    break;
//                }
//            } catch (DOMException ex) {
//                ErrorManager.getDefault().notify(ex);
//
//                break;
//            }
//        }
//
//        Node last = secondNode;
//
//        while (curr != null) {
////            if (curr instanceof RaveElement) {
////                RaveElement element = (RaveElement)curr;
//            if (curr instanceof Element) {
//                Element element = (Element)curr;
////                DesignBean bean = element.getDesignBean();
////                DesignBean bean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
////                DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//                Element componentRootElement = MarkupService.getRenderedElementForElement(element);
//                
////                if ((bean != null) &&
//                if ((componentRootElement != null) &&
//                        ((element.getParentNode() == null) ||
////                        (element.getParentNode() instanceof RaveElement &&
////                        (((RaveElement)element.getParentNode()).getDesignBean() != bean)))) {
//                        (element.getParentNode() instanceof Element
////                        && InSyncService.getProvider().getMarkupDesignBeanForElement((Element)element.getParentNode()) != bean))) {
////                        && WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)element.getParentNode()) != bean))) {
//                        && MarkupService.getRenderedElementForElement((Element)element.getParentNode()) != componentRootElement))) {
////                    if (!beans.contains(bean)) {
////                        beans.add(bean);
////                    }
//                    if (!components.contains(componentRootElement)) {
//                        components.add(componentRootElement);
//                    }
//                }
//            }
//
//            if ((curr == null) || (curr == last)) {
//                break;
//            }
//
//            try {
//                curr = iterator.nextNode();
//            } catch (DOMException ex) {
//                ErrorManager.getDefault().notify(ex);
//
//                break;
//            }
//        }
//
//        iterator.detach();
//
////        FacesModel model = webform.getModel();
////        Document doc = webform.getDocument();
//
////        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
////        DomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomRangeImpl.class, "LBL_DeleteText")); // NOI18N
//        try {
////            doc.writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//
////            for (int i = 0; i < beans.size(); i++) {
////                DesignBean bean = (DesignBean)beans.get(i);
//            for (Element componentRootElement : components) {
//
////                if (!FacesSupport.isSpecialBean(/*webform, */bean)) {
////                if (!Util.isSpecialBean(bean)) {
////                if (bean instanceof MarkupDesignBean && !WebForm.getDomProviderService().isSpecialComponent(
////                        WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)bean))) {
////                if (!WebForm.getDomProviderService().isSpecialComponent(componentRootElement)) {
//                if (!JsfSupportUtilities.isSpecialComponent(componentRootElement)) {
////                    model.getLiveUnit().deleteBean(bean);
////                    webform.deleteBean(bean);
////                    webform.deleteComponent(componentRootElement);
//                    jsfForm.deleteComponent(componentRootElement);
//                }
//            }
//        } finally {
////            doc.writeUnlock();
////            webform.getModel().writeUnlock(undoEvent);
////            webform.writeUnlock(writeLock);
//            jsfForm.writeUnlock(writeLock);
//        }
//    }

    /** Return true iff the range is in a read-only region */
    public boolean isReadOnlyRegion() {
        return domRange == null;
    }

    // XXX Moved to DomDocumentImpl.
//    /** Return the text in the range (linearized to a String); this is only the
//     * text nodes, not comment nodes, not markup, etc.
//     */
//    public String getText() {
//        // Since we'll be iterating forwards, gotta make sure we know
//        // which point is first
////        Position second = getLastPosition();
//        DomPosition second = getLastPosition();
//
////        if (second == Position.NONE) {
//        if (second == DomPosition.NONE) {
//            return "";
//        }
//
////        Position first = getFirstPosition();
//        DomPosition first = getFirstPosition();
//        assert first.isEarlierThan(first);
//
//        StringBuffer sb = new StringBuffer();
//
//        Node firstNode = first.getNode();
//
//        if (firstNode instanceof Element) {
//            if (first.getOffset() < firstNode.getChildNodes().getLength()) {
//                firstNode = firstNode.getChildNodes().item(first.getOffset());
//            }
//        }
//
//        Node secondNode = second.getNode();
//
//        if (first.equals(second)) {
//            secondNode = firstNode;
//        } else if (secondNode instanceof Element) {
//            if ((second.getOffset() > 0) &&
//                    (second.getOffset() <= secondNode.getChildNodes().getLength())) {
//                secondNode = secondNode.getChildNodes().item(second.getOffset() - 1);
//            } else if (second.getOffset() == 0) {
//                // Gotta locate immediate inorder traversal neighbor to the left
//                while ((secondNode != null) && (secondNode.getPreviousSibling() == null)) {
//                    secondNode = secondNode.getParentNode();
//                }
//
//                if (secondNode == null) {
//                    ErrorManager.getDefault().log("Unexpected second position " + second); // NOI18N
//
//                    return "";
//                }
//
//                secondNode = secondNode.getPreviousSibling();
//
//                while (true) {
//                    NodeList nl = secondNode.getChildNodes();
//
//                    if (nl.getLength() > 0) {
//                        secondNode = nl.item(nl.getLength() - 1);
//                    } else {
//                        break;
//                    }
//                }
//            }
//        }
//
//        // Insert content for the first node
//        if (firstNode instanceof Text) {
//            if (secondNode == firstNode) {
//                String s = firstNode.getNodeValue();
//
//                for (int i = first.getOffset(); i < second.getOffset(); i++) {
//                    sb.append(s.charAt(i));
//                }
//
//                return sb.toString();
//            } else {
//                String s = firstNode.getNodeValue();
//
//                for (int i = first.getOffset(), n = s.length(); i < n; i++) {
//                    sb.append(s.charAt(i));
//                }
//            }
//        }
//
//        // Append content for all the nodes between first and second
////        org.w3c.dom.Document dom = webform.getJspDom();
//        org.w3c.dom.Document dom = jsfForm.getJspDom();
//
//        if (!(dom instanceof DocumentTraversal)) {
//            return "";
//        }
//
//        DocumentTraversal trav = (DocumentTraversal)dom;
//
//        // Iterating over all since we can't just limit ourselves to text nodes
//        // in case the target node is not necessarily a text node!
//        NodeIterator iterator = trav.createNodeIterator(dom, NodeFilter.SHOW_ALL, null, false);
//        Node curr = firstNode;
//
//        // The node iterator doesn't seem to have a way to jump to a particular node,
//        // so we search for it ourselves
//        while (curr != null) {
//            try {
//                curr = iterator.nextNode();
//
//                if (curr == firstNode) {
//                    break;
//                }
//            } catch (DOMException ex) {
//                ErrorManager.getDefault().notify(ex);
//
//                break;
//            }
//        }
//
//        Node last = secondNode;
//
//        while (curr != null) {
//            try {
//                curr = iterator.nextNode();
//            } catch (DOMException ex) {
//                ErrorManager.getDefault().notify(ex);
//
//                break;
//            }
//
//            if ((curr == null) || (curr == last)) {
//                break;
//            }
//
//            if (curr instanceof Text) {
//                sb.append(curr.getNodeValue());
//            }
//        }
//
//        iterator.detach();
//
//        // Append content for the last node
//        if (secondNode instanceof Text) {
//            String s = secondNode.getNodeValue();
//
//            for (int i = 0; i < second.getOffset(); i++) {
//                sb.append(s.charAt(i));
//            }
//        }
//
//        return sb.toString();
//    }
}

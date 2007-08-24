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

package org.netbeans.modules.visualweb.css2;


import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.netbeans.modules.visualweb.designer.InlineEditor;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.openide.ErrorManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * Class responsible for mapping between the DOM (jsp document and
 * rendered html fragments) and the view (boxes and pixel coordinates).
 * It also handles caret motion. Note that methods which return positions
 * never return null, they return Position.NONE in that case.
 *
 * @todo Handle motion in tables
 * @todo Handle motion past iframes, fragment box includes, etc.
 * @todo There are two getLastLineboxPosition methods; join them
 * @todo Handle the "hidden" flag on boxes, and skip them in
 *  visual traversal
 *
 * @author Tor Norbye
 */
public final class ModelViewMapper {
//    private WebForm webform;

    private ModelViewMapper(WebForm webform) {
//        this.webform = webform;
    }

    /**
     * Given a caret position, return the caret position to its left. This
     * may not actually be the left on the screen, e.g. if you press left at the
     * beginning of a line you may end up on the right side of the previous
     * line.
     * @param pos The caret position in the DOM
     * @return The next visual position to the "left" of pos, or Position.NONE if
     * there is no such position in the document (e.g. when you're at the
     * first visual position in the document.)
     */
//    public static Position computeArrowLeft(WebForm webform, Position sourcePos) {
    public static DomPosition computeArrowLeft(WebForm webform, DomPosition sourcePos) {
    
//        assert !sourcePos.isRendered();
//        if (MarkupService.isRenderedNode(sourcePos.getNode())) {
        if (sourcePos.isRenderedPosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node is expected to be not rendered, node=" + sourcePos.getNode())); // NOI18N
        }

//        Position pos = sourcePos.getRenderedPosition();
        DomPosition pos = sourcePos.getRenderedPosition();

        // TODO: in a table, use special case such that we handle the
        // first-cell-in-a-row scenario correctly
        LineBox lb = findLineBox(webform.getPane().getPageBox(), pos);

        if (lb == null) {
            // Caret positions should always be in a linebox unless we're inline editing;
            // in inline editing mode we can delete backwards until we have no caret position
            // XXX #104463 This doesn't seem to be correct here.
//            assert webform.getManager().isInlineEditing() : pos;

//            return Position.NONE;
            return DomPosition.NONE;
        }

//        Position p = findPrevPosition(lb, pos, webform.getManager().getInlineEditor());
        DomPosition p = findPrevPosition(lb, pos, webform.getManager().getInlineEditor());

//        if (p != Position.NONE) {
        if (p != DomPosition.NONE) {
            
//            if (p.isRendered()) {
//            if (MarkupService.isRenderedNode(p.getNode())) {
            if (p.isRenderedPosition()) {
                p = p.getSourcePosition();
            }
        }

        return p;
    }

    /**
     * Given a caret position, return the caret position to its right. This
     * may not actually be the right on the screen, e.g. if you press right at the
     * end of a line you may end up on the left side of the next
     * line.
     * @param pos The caret position in the DOM
     * @return The next visual position to the "right" of pos, or Position.NONE if
     * there is no such position in the document (e.g. when you're at the
     * last visual position in the document.)
     */
//    public static Position computeArrowRight(WebForm webform, Position sourcePos) {
    public static DomPosition computeArrowRight(WebForm webform, DomPosition sourcePos) {
//        assert !sourcePos.isRendered();
//        if (MarkupService.isRenderedNode(sourcePos.getNode())) {
        if (sourcePos.isRenderedPosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node is expected to be not rendered, node=" + sourcePos.getNode())); // NOI18N
        }

//        Position pos = sourcePos.getRenderedPosition();
        DomPosition pos = sourcePos.getRenderedPosition();

        // TODO: in a table, use special case such that we handle the
        // last-cell-in-a-row scenario correctly
        LineBox lb = findLineBox(webform.getPane().getPageBox(), pos);

        if (lb == null) {
//            assert false : pos; // Caret positions should always be in a linebox!!

//            return Position.NONE;
            return DomPosition.NONE;
        }

//        Position p = findNextPosition(lb, pos, webform.getManager().getInlineEditor());
        DomPosition p = findNextPosition(lb, pos, webform.getManager().getInlineEditor());

//        if (p != Position.NONE) {
        if (p != DomPosition.NONE) {
            
//            if (p.isRendered()) {
//            if (MarkupService.isRenderedNode(p.getNode())) {
            if (p.isRenderedPosition()) {
                p = p.getSourcePosition();
            }
        }

        return p;
    }

    /**
     * Given a caret position, return the caret position to "above" it; e.g. the
     * position we should move the caret to if the user presses the up arrow key.
     * @param pos The caret position in the DOM
     * @return The next visual position above the given pos, or Position.NONE if
     * there is no such position in the document (e.g. when you're on the first line
     *  in the document.)
     */
//    public static Position computeArrowUp(WebForm webform, Position sourcePos) {
    public static DomPosition computeArrowUp(WebForm webform, DomPosition sourcePos) {
        // TODO: If in a table, figure out the current position (I could stash these right 
        // on the cellboxes), subtract one, and if still within the table, look for the first
        // cell root (e.g. skip spans) and use that. Otherwise (if we're outside of the table), 
        // use the table itself as an origin, then do the normal linebox search.
//        assert !sourcePos.isRendered();
//        if (MarkupService.isRenderedNode(sourcePos.getNode())) {
        if (sourcePos.isRenderedPosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IllegalStateException("Node is expected to be not rendered, node=" + sourcePos.getNode())); // NOI18N
        }

//        Position pos = sourcePos.getRenderedPosition();
        DomPosition pos = sourcePos.getRenderedPosition();

        LineBox lb = findLineBox(webform.getPane().getPageBox(), pos);

        if (lb == null) {
//            assert false : pos; // Caret positions should always be in a linebox!!

//            return Position.NONE;
            return DomPosition.NONE;            
        }

//        DesignerCaret caret = webform.getPane().getCaret();
//        assert caret != null;
        if (!webform.getPane().hasCaret()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Pane doesn't have caret!"));
            return DomPosition.NONE;
        }

//        Point magicPosition = caret.getMagicCaretPosition();
        Point magicPosition = webform.getPane().getCaretMagicPosition();

        LineBox prev = lb;

        while (true) {
            prev = findPrevLineBox(prev);

            if (prev == null) {
//                return Position.NONE;
                return DomPosition.NONE;
            }

//            Position p = prev.computePosition(magicPosition.x - prev.getAbsoluteX());
            DomPosition p = prev.computePosition(magicPosition.x - prev.getAbsoluteX());

//            if (DesignerUtils.checkPosition(p, false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE) {
//            if (isValidPosition(p, false, /*webform*/webform.getManager().getInlineEditor())) {
            if (isValidPosition(webform, p, false, /*webform*/webform.getManager().getInlineEditor())) {
//                if (p != Position.NONE) {
                if (p != DomPosition.NONE) {
                    
//                    if (p.isRendered()) {
//                    if (MarkupService.isRenderedNode(p.getNode())) {
                    if (p.isRenderedPosition()) {
                        p = p.getSourcePosition();
                    }
                }

                return p;
            }
        }
    }

    /**
     * Given a caret position, return the caret position to "below" it; e.g. the
     * position we should move the caret to if the user presses the down arrow key.
     * @param pos The caret position in the DOM
     * @return The next visual position below the given pos, or Position.NONE if
     * there is no such position in the document (e.g. when you're on the last line
     *  in the document.)
     */
//    public static Position computeArrowDown(WebForm webform, Position sourcePos) {
    public static DomPosition computeArrowDown(WebForm webform, DomPosition sourcePos) {
        // TODO: If in a table, figure out the current position (I could stash these right 
        // on the cellboxes), add one, and if still within the table, look for the first
        // cell root (e.g. skip spans) and use that. Otherwise (if we're outside of the table), 
        // use the table itself as an origin, then do the normal linebox search.
//        assert !sourcePos.isRendered();
//        if (MarkupService.isRenderedNode(sourcePos.getNode())) {
        if (sourcePos.isRenderedPosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Node is expected to be not rendered, node=" + sourcePos.getNode())); // NOI18N
        }

//        Position pos = sourcePos.getRenderedPosition();
        DomPosition pos = sourcePos.getRenderedPosition();

        LineBox lb = findLineBox(webform.getPane().getPageBox(), pos);

        if (lb == null) {
//            assert false : pos; // Caret positions should always be in a linebox!!

//            return Position.NONE;
            return DomPosition.NONE;
        }

//        DesignerCaret caret = webform.getPane().getCaret();
//        assert caret != null;
        if (!webform.getPane().hasCaret()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Pane doesn't have caret!"));
            return DomPosition.NONE;
        }

//        Point magicPosition = caret.getMagicCaretPosition();
        Point magicPosition = webform.getPane().getCaretMagicPosition();

        LineBox next = lb;

        while (true) {
            next = findNextLineBox(next);

            if (next == null) {
//                return Position.NONE;
                return DomPosition.NONE;
            }

//            Position p = next.computePosition(magicPosition.x - next.getAbsoluteX());
            DomPosition p = next.computePosition(magicPosition.x - next.getAbsoluteX());

//            if (DesignerUtils.checkPosition(p, false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE) {
//            if (isValidPosition(p, false, /*webform*/webform.getManager().getInlineEditor())) {
            if (isValidPosition(webform, p, false, /*webform*/webform.getManager().getInlineEditor())) {
//                if (p != Position.NONE) {
                if (p != DomPosition.NONE) {
                    
//                    if (p.isRendered()) {
//                    if (MarkupService.isRenderedNode(p.getNode())) {
                    if (p.isRenderedPosition()) {
                        p = p.getSourcePosition();
                    }
                }

                return p;
            }
        }
    }

    /**
     * Given a LineBox and a position in that line box, find the FIRST acceptable caret position.
     * Note that the position may be in a different line box (this happens if the
     * position you passed in was the last caret position in the linebox).
     */
//    private static Position findNextPosition(LineBox lb, Position pos, InlineEditor inlineEditor) {
    private static DomPosition findNextPosition(LineBox lb, DomPosition pos, InlineEditor inlineEditor) {
//        assert pos.isRendered();
//        if (!MarkupService.isRenderedNode(pos.getNode())) {
        if (pos.isSourcePosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node is expected to be rendered, node=" + pos.getNode())); // NOI18N
        }

        Node node = pos.getNode();
        int offset = pos.getOffset();

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            NodeList nl = node.getChildNodes();

            if ((offset >= nl.getLength()) || ((pos.getBias() == Bias.BACKWARD) && (offset > 0))) {
                node = nl.item(offset - 1);
                offset = 1;
            } else {
                node = nl.item(offset);
                offset = 0;
            }
        }

        CssBox box = null;

        // Iterate over the line box contents. When you find the box containing the 
        // current position, see if it contains the next position, and if so, return it.
        int i = 0;

        // Iterate over the line box contents. When you find the box containing the 
        // current position, see if it contains the next position, and if so, return it.
        int n = lb.getBoxCount();

        for (; i < n; i++) {
            box = lb.getBox(i);

            if (box.getBoxType() == BoxType.TEXT) {
                TextBox tb = (TextBox)box;

                if ((tb.getNode() == node) && (
                    /* This doesn't work when the position is somewhere in the DOM I'm
                       not displaying, like in a region of space characters. Since I search
                       the dom forwards however, I don't need this extra check.
                    tb.getDomStartOffset() <= offset && */
                    offset <= tb.getDomEndOffset())) {
//                    Position p = tb.getNext(pos);
                    DomPosition p = tb.getNext(pos);

//                    if (p != Position.NONE) {
                    if (p != DomPosition.NONE) {
                        return p;
                    }

                    break;
                }
            } else if (box.getBoxType() == BoxType.SPACE) {
                SpaceBox tb = (SpaceBox)box;

                if ((tb.getNode() == node) && (
                    /* This doesn't work when the position is somewhere in the DOM I'm
                       not displaying, like in a region of space characters. Since I search
                       the dom forwards however, I don't need this extra check.
                    tb.getDomStartOffset() <= offset && */
                    offset <= tb.getDomEndOffset())) {
//                    Position p = tb.getNext(pos);
                    DomPosition p = tb.getNext(pos);

//                    if (p != Position.NONE) {
                    if (p != DomPosition.NONE) {
                        return p;
                    }

                    break;
                }
            } else if (box.getBoxType() == BoxType.LINEBREAK) {
                break;
//            } else if (getElement(box) == node) {
            } else if (getComponentRootElementParentForCssBox(box) == node) {
                if (offset == 0) {
                    // We can resolve this one quickly! Just pick position after
//                    return Position.create(getElement(box).getSource(), true);
//                    return Position.create(MarkupService.getSourceElementForElement(getElement(box)), true);
//                    return Position.create(MarkupService.getSourceElementForElement(
//                            getComponentRootElementParentForCssBox(box)), true);
                    return lb.getWebForm().createDomPosition(MarkupService.getSourceElementForElement(getComponentRootElementParentForCssBox(box)), true);
                }

                if (i >= (n - 1)) {
                    // We've walked out of the last textbox/spacebox etc in the line
                    // so jump to the next line
                    return getFirstPosNextLine(lb, inlineEditor);
                } else {
                    return findFirstLineboxPosition(lb, i + 1, inlineEditor);
                }

                /*
                } else if (pos.isInside(box.getSourceElement())) {
                break;
                 */
            }
        }

//        assert i != n; // LineBox should have contained box; if not input arg was invalid
        if (i == n) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Next position not found, lb=" + lb + ", pos=" + pos)); // NOI18N
//            return Position.NONE;
            return DomPosition.NONE;
        }


        while (true) { // May have to try repeatedly to skip JSF fragment linebox contents

            if (i == n) {
                return getFirstPosNextLine(lb, inlineEditor);
            }

            // If there is no next box, or if the next box is a linebreak box,
            // Jump to the Next Line lb.getBox(i)
            if ((i >= (n - 1)) || // if there is no next box
                    (lb.getBox(i + 1).getBoxType() == BoxType.LINEBREAK)) {
                // Jump to the next line: Find Next Line Box and if not null,
                // Find First Position in it and return that.
                return getFirstPosNextLine(lb, inlineEditor);
            }

            // If the next box is a space box or a text box, ask it for the next position
            // (e.g. skipping its first position) and return it.
            CssBox next = lb.getBox(i + 1);

            if (next.getBoxType() == BoxType.TEXT) {
                // We know it SHOULD have one position since all text and space boxes
                // must have at least one character
                TextBox tb = (TextBox)next;
//                Position p = tb.getNext(tb.getFirstPosition());
                DomPosition p = tb.getNext(tb.getFirstPosition());

//                if (DesignerUtils.checkPosition(p, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(p, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), p, false, inlineEditor)) {
                    return p;
                } else {
                    // else it's probably a JSF component
                    i++;

                    continue;
                }
            } else if (next.getBoxType() == BoxType.SPACE) {
                SpaceBox tb = (SpaceBox)next;
//                Position p = tb.getNext(tb.getFirstPosition());
                DomPosition p = tb.getNext(tb.getFirstPosition());

//                if (DesignerUtils.checkPosition(p, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(p, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), p, false, inlineEditor)) {
                    return p;
                } else {
                    // else it's probably a JSF component
                    i++;

                    continue;
                }
            }

            // Otherwise the next box must be an inline box (like an image, or a
            // button), so skip it. If there are no more boxes in the LineBox after
            // this box, we should jump to the next line.
            i += 2;

            if (i >= n) {
                // We've walked out of the last textbox/spacebox etc in the line
                // so jump to the next line
                return getFirstPosNextLine(lb, inlineEditor);
            } else {
                return findFirstLineboxPosition(lb, i, inlineEditor);
            }
        }
    }

    /**
     * Given a LineBox, compute the NEXT linebox and return its first
     * position. Used as part of getNextPosition for various cases.
     */
//    private static Position getFirstPosNextLine(LineBox lb, InlineEditor inlineEditor) {
    private static DomPosition getFirstPosNextLine(LineBox lb, InlineEditor inlineEditor) {
        // Jump to the next line: Find Next Line Box and if not null,
        // Find First Position in it and return that.
        LineBox nextLine = findNextLineBox(lb);

        if (nextLine != null) {
            return findFirstLineboxPosition(nextLine, 0, inlineEditor);
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /**
     * Given a LineBox, compute the PREVIOUS linebox and return its last.
     * position. Used as part of getPrevPosition for various cases.
     * @param inlineEditor (XXX suspicious) InlineEditor which is active in the page (or <code>null</code>).
     */
//    private static Position getLastPosPrevLine(LineBox lb, InlineEditor inlineEditor) {
    private static DomPosition getLastPosPrevLine(LineBox lb, InlineEditor inlineEditor) {
        // Jump to the previous line:
        // Find Previous Line Box and if not null, Find Last Position in it
        // and return that.
        LineBox prevLine = findPrevLineBox(lb);

        if (prevLine != null) {
            return findLastLineboxPosition(prevLine, inlineEditor);
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /**
     * Analogous to Find Next Position but move in the opposite direction.
     * Note that the position may be in a different line box (this happens if the position
     * you passed in was the first caret position in the linebox).
     * @param inlineEditor (XXX suspicious) InlineEditor which is active in the page (or <code>null</code>).
     */
//    private static Position findPrevPosition(LineBox lb, Position pos, InlineEditor inlineEditor) {
    private static DomPosition findPrevPosition(LineBox lb, DomPosition pos, InlineEditor inlineEditor) {
//        assert pos.isRendered();
//        if (!MarkupService.isRenderedNode(pos.getNode())) {
        if (pos.isSourcePosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node is expected to be rendered, node=" + pos.getNode())); // NOI18N
        }

        Node node = pos.getNode();
        int offset = pos.getOffset();

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            NodeList nl = node.getChildNodes();

            if ((offset >= nl.getLength()) || ((pos.getBias() == Bias.BACKWARD) && (offset > 0))) {
                node = nl.item(offset - 1);
                offset = 1;
            } else {
                node = nl.item(offset);
                offset = 0;
            }
        }

        CssBox box = null;

        // Iterate over the line box contents. When you find the box containing the 
        // current position, see if it contains the previous position, and if so, return it.
        int i = 0;

        // Iterate over the line box contents. When you find the box containing the 
        // current position, see if it contains the previous position, and if so, return it.
        int n = lb.getBoxCount();

        for (; i < n; i++) {
            box = lb.getBox(i);

            if (box.getBoxType() == BoxType.TEXT) {
                TextBox tb = (TextBox)box;

                if ((tb.getNode() == node) && (
                    /* This doesn't work when the position is somewhere in the DOM I'm
                       not displaying, like in a region of space characters. Since I search
                       the dom forwards however, I don't need this extra check.
                    tb.getDomStartOffset() <= offset && */
                    offset <= tb.getDomEndOffset())) {
//                    Position p = tb.getPrev(pos);
                    DomPosition p = tb.getPrev(pos);

//                    if (p != Position.NONE) {
                    if (p != DomPosition.NONE) {
                        return p;
                    }

                    break;
                }
            } else if (box.getBoxType() == BoxType.SPACE) {
                SpaceBox tb = (SpaceBox)box;

                if ((tb.getNode() == node) && (
                    /* This doesn't work when the position is somewhere in the DOM I'm
                       not displaying, like in a region of space characters. Since I search
                       the dom forwards however, I don't need this extra check.
                    tb.getDomStartOffset() <= offset && */
                    offset <= tb.getDomEndOffset())) {
//                    Position p = tb.getPrev(pos);
                    DomPosition p = tb.getPrev(pos);

//                    if (p != Position.NONE) {
                    if (p != DomPosition.NONE) {
                        return p;
                    }

                    break;
                }
            } else if (box.getBoxType() == BoxType.LINEBREAK) {
                break;
//            } else if (getElement(box) == node) {
            } else if (getComponentRootElementParentForCssBox(box) == node) {
                if (offset == 1) {
                    // We can resolve this one quickly! Just pick position before
//                    return Position.create(getElement(box).getSource(), false);
//                    return Position.create(MarkupService.getSourceElementForElement(getElement(box)), false);
//                    return Position.create(MarkupService.getSourceElementForElement(
//                            getComponentRootElementParentForCssBox(box)), false);
                    return lb.getWebForm().createDomPosition(MarkupService.getSourceElementForElement(getComponentRootElementParentForCssBox(box)), false);
                }

                if (i == 0) {
                    // We've walked out of the first textbox/spacebox etc in the line
                    // so jump to the previous line
                    return getLastPosPrevLine(lb, inlineEditor);
                } else {
                    return findLastLineboxPosition(lb, i - 1, inlineEditor);
                }

                /*
                } else if (pos.isInside(box.getSourceElement())) {
                break;
                 */
            }
        }

//        assert i != n; // LineBox should have contained box; if not input arg was invalid
        if (i == n) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Previous position not found, lb=" + lb + ", pos=" + pos)); // NOI18N
//            return Position.NONE;
            return DomPosition.NONE;
        }


        while (true) {
            // If there is no previous box, Jump to the Previous Line
            if (i == 0) {
                // Jump to the previous line:
                return getLastPosPrevLine(lb, inlineEditor);
            }

            // If the previous box is a space box or a text box, ask it for the previous position
            // (e.g. skipping its last position) and return it.
            CssBox prev = lb.getBox(i - 1);

            if (prev.getBoxType() == BoxType.TEXT) {
                // We know it SHOULD have one position since all text and space boxes
                // must have at least one character
                TextBox tb = (TextBox)prev;
//                Position p = tb.getPrev(tb.getLastPosition());
                DomPosition p = tb.getPrev(tb.getLastPosition());

//                if (DesignerUtils.checkPosition(p, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(p, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), p, false, inlineEditor)) {
                    return p;
                } else {
                    // else it's probably a JSF component
                    i--;

                    continue;
                }
            } else if (prev.getBoxType() == BoxType.SPACE) {
                SpaceBox tb = (SpaceBox)prev;
//                Position p = tb.getPrev(tb.getLastPosition());
                DomPosition p = tb.getPrev(tb.getLastPosition());

//                if (DesignerUtils.checkPosition(p, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(p, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), p, false, inlineEditor)) {
                    return p;
                } else {
                    // else it's probably a JSF component
                    i--;

                    continue;
                }
            }

            // Otherwise the previous box must be an inline box (like an image, or a
            // button), so skip it. If this was the first box in the linebox, we should
            // jump to the previous line. (can I combine these too?
            i--;

            if (i < 0) {
                // We've walked out of the first textbox/spacebox etc in the line
                // so jump to the previous line
                return getLastPosPrevLine(lb, inlineEditor);
            } else {
                return findLastLineboxPosition(lb, i, inlineEditor);
            }
        }
    }

    /**
     * Given a Line Box, return the first position in the line box
     * starting at the given box offset. Pass in 0 to get the first
     * position in the line box.
     * @param lb The line box
     * @return The first legal caret position in the line box
     */
//    private static Position findFirstLineboxPosition(LineBox lb, int index, InlineEditor inlineEditor) {
    private static DomPosition findFirstLineboxPosition(LineBox lb, int index, InlineEditor inlineEditor) {
        if (lb.getBoxCount() <= index) {
//            assert false : lb;

//            return Position.NONE;
            return DomPosition.NONE;
        }

        while (true) {
            CssBox box = lb.getBox(index);

            if (box.getBoxType() == BoxType.TEXT) {
                TextBox tb = (TextBox)box;
//                Position pos = tb.getFirstPosition();
                DomPosition pos = tb.getFirstPosition();

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            } else if (box.getBoxType() == BoxType.SPACE) {
                SpaceBox tb = (SpaceBox)box;
//                Position pos = tb.getFirstPosition();
                DomPosition pos = tb.getFirstPosition();

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            } else {
                // assert that this is a simple inline, noncontainer box,
                // such as an image, a StringBox, an iframe, etc.
//                Position pos = Position.create(box.getSourceElement(), false);
                DomPosition pos = lb.getWebForm().createDomPosition(box.getSourceElement(), false);

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            }

            index++;

            if (index == lb.getBoxCount()) {
                while (true) {
                    lb = findNextLineBox(lb);

                    if (lb == null) {
//                        return Position.NONE;
                        return DomPosition.NONE;
                    }

                    index = 0;

                    if (index < lb.getBoxCount()) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Given a Line Box, return the last position in the line box.
     * @param lb The line box
     * @param inlineEditor (XXX suspicious) InlineEditor which is active in the page (or <code>null</code>).
     * @return The last legal caret position in the line box
     */
//    private static Position findLastLineboxPosition(LineBox lb, int index, InlineEditor inlineEditor) {
    private static DomPosition findLastLineboxPosition(LineBox lb, int index, InlineEditor inlineEditor) {
        if (lb.getBoxCount() <= index) {
            assert false : lb;

//            return Position.NONE;
            return DomPosition.NONE;
        }

        while (true) {
            CssBox box = lb.getBox(index);

            if (box.getBoxType() == BoxType.TEXT) {
                TextBox tb = (TextBox)box;
//                Position pos = tb.getLastPosition();
                DomPosition pos = tb.getLastPosition();

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            } else if (box.getBoxType() == BoxType.SPACE) {
                SpaceBox tb = (SpaceBox)box;
//                Position pos = tb.getLastPosition();
                DomPosition pos = tb.getLastPosition();

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            } else {
                // assert that this is a simple inline, noncontainer box,
                // such as an image, a StringBox, an iframe, etc.
                if (index > 0) {
                    CssBox prev = lb.getBox(index - 1);

                    if (prev.getBoxType() == BoxType.TEXT) {
                        TextBox tb = (TextBox)prev;
//                        Position pos = tb.getLastPosition();
                        DomPosition pos = tb.getLastPosition();

//                        if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                        if (isValidPosition(pos, false, inlineEditor)) {
                        if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                            return pos;
                        }
                    } else if (prev.getBoxType() == BoxType.SPACE) {
                        SpaceBox tb = (SpaceBox)prev;
//                        Position pos = tb.getLastPosition();
                        DomPosition pos = tb.getLastPosition();

//                        if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                        if (isValidPosition(pos, false, inlineEditor)) {
                        if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                            return pos;
                        }
                    }
                }

//                Position pos = Position.create(box.getSourceElement(), false);
                DomPosition pos = lb.getWebForm().createDomPosition(box.getSourceElement(), false);

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            }

            index--; // Probably a JSF component

            if (index < 0) {
                while (true) {
                    lb = findPrevLineBox(lb);

                    if (lb == null) {
//                        return Position.NONE;
                        return DomPosition.NONE;
                    }

                    index = lb.getBoxCount() - 1;

                    if (index >= 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Given a Line Box, return the last position in the line box.
     * @todo XXX This needs to be combined with findLastLineboxPosition(LineBox,int)!!!
     * @param lb The line box
     * @param inlineEditor (XXX suspicious) InlineEditor which is active in the page (or <code>null</code>).
     * @return The last legal caret position in the line box
     */
//    private static Position findLastLineboxPosition(LineBox lb, InlineEditor inlineEditor) {
    private static DomPosition findLastLineboxPosition(LineBox lb, InlineEditor inlineEditor) {
        if (lb.getBoxCount() == 0) {
//            assert false : lb;

//            return Position.NONE;
            return DomPosition.NONE;
        }

        int index = lb.getBoxCount() - 1;

        while (true) {
            CssBox box = lb.getBox(index);

            if (box.getBoxType() == BoxType.LINEBREAK) {
//                Position pos = Position.create(box.getSourceElement(), false);
                DomPosition pos = lb.getWebForm().createDomPosition(box.getSourceElement(), false);

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            }

            if (box.getBoxType() == BoxType.TEXT) {
                TextBox tb = (TextBox)box;
//                Position pos = tb.getLastPosition();
                DomPosition pos = tb.getLastPosition();

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            } else if (box.getBoxType() == BoxType.SPACE) {
                SpaceBox tb = (SpaceBox)box;
//                Position pos = tb.getLastPosition();
                DomPosition pos = tb.getLastPosition();

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            } else {
//                Position pos = Position.create(box.getSourceElement(), true);
                DomPosition pos = lb.getWebForm().createDomPosition(box.getSourceElement(), true);

//                if (DesignerUtils.checkPosition(pos, false, inlineEditor) != Position.NONE) {
//                if (isValidPosition(pos, false, inlineEditor)) {
                if (isValidPosition(lb.getWebForm(), pos, false, inlineEditor)) {
                    return pos;
                }
            }

            // It was probably a JSF component
            index--;

            if (index < 0) {
                while (true) {
                    lb = findPrevLineBox(lb);

                    if (lb == null) {
//                        return Position.NONE;
                        return DomPosition.NONE;
                    }

                    index = lb.getBoxCount() - 1;

                    if (index >= 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Given a box, find the next linebox in flow searching forwards.
     * @param box The box to start the search from (which may be
     *  inside a linebox. The containing linebox will not be returned
     *  as the next linebox.
     * @return The next linebox in flow.
     */
    private static LineBox findNextLineBox(CssBox box) {
        if ((box != null) && (box.getBoxType() != BoxType.LINEBOX) &&
                box.getParent() instanceof LineBoxGroup) {
            // The box is already managed as part of a line box group
            // See which line box it's in.
            CssBox parent = box.getParent();

            for (int j = 0, n = parent.getBoxCount(); j < n; j++) {
                CssBox linebox = parent.getBox(j);

                if (linebox.getBoxType() != BoxType.LINEBOX) {
                    continue;
                }

                for (int k = 0, m = linebox.getBoxCount(); k < m; k++) {
                    CssBox child = linebox.getBox(k);

                    if (child == box) {
                        // Found it
                        int lineno = j + 1;

                        while (lineno < n) {
                            CssBox next = parent.getBox(lineno);

                            if (next.getBoxType() == BoxType.LINEBOX) {
                                return (LineBox)next;
                            }

                            lineno++;
                        }

                        break;
                    }
                }
            }

            box = parent;
        }

        // We're not in a linebox group, or we were already in the first
        // line of that line box group...
        while (box != null) {
            CssBox next = box.getNextNormalBlockBox();

            while (next != null) {
                LineBox lb = findFirstLineBox(next);

                if (lb != null) {
                    return lb;
                }

                next = next.getNextNormalBlockBox();
            }

            box = box.getParent();
        }

        return null;
    }

    /**
     * Given a box, find the previous linebox in flow searching backwards.
     * @param box The box to start the search from (which may be
     *  inside a linebox. The containing linebox will not be returned
     *  as the next linebox.
     * @return The previous linebox in flow.
     */
    private static LineBox findPrevLineBox(CssBox box) {
        if ((box != null) && (box.getBoxType() != BoxType.LINEBOX) &&
                box.getParent() instanceof LineBoxGroup) {
            // The box is already managed as part of a line box group
            // See which line box it's in.
            CssBox parent = box.getParent();

            for (int j = 0, n = parent.getBoxCount(); j < n; j++) {
                CssBox linebox = parent.getBox(j);

                if (linebox.getBoxType() != BoxType.LINEBOX) {
                    continue;
                }

                for (int k = 0, m = linebox.getBoxCount(); k < m; k++) {
                    CssBox child = linebox.getBox(k);

                    if (child == box) {
                        // Found it
                        int lineno = j - 1;

                        while (lineno >= 0) {
                            CssBox prev = parent.getBox(lineno);

                            if (prev.getBoxType() == BoxType.LINEBOX) {
                                return (LineBox)prev;
                            }

                            lineno--;
                        }

                        break;
                    }
                }
            }

            box = parent;
        }

        while (box != null) {
            CssBox prev = box.getPrevNormalBlockBox();

            while (prev != null) {
                LineBox lb = findLastLineBox(prev);

                if (lb != null) {
                    return lb;
                }

                prev = prev.getPrevNormalBlockBox();
            }

            box = box.getParent();
        }

        return null;
    }

    /**
     * Return the first linebox found in a depth search of this box tree
     * @param box The root box to check
     * @return The first LineBox found under the box
     */
    private static LineBox findFirstLineBox(CssBox box) {
        if ((box.getBoxType() == BoxType.LINEBOX) && box instanceof LineBox) { // LineBoxGroup is also BoxType.LINEBOX

            return (LineBox)box;
        }

        for (int i = 0, n = box.getBoxCount(); i < n; i++) {
            CssBox child = box.getBox(i);

            if (child instanceof ExternalDocumentBox) {
                continue;
            }

            LineBox match = findFirstLineBox(child);

            if (match != null) {
                return match;
            }
        }

        return null;
    }

    /**
     * Return the last linebox found in a depth search of this box tree
     * @param box The root box to check
     * @return The last LineBox found under the box
     */
    private static LineBox findLastLineBox(CssBox box) {
        if ((box.getBoxType() == BoxType.LINEBOX) && box instanceof LineBox) { // LineBoxGroup is also BoxType.LINEBOX

            return (LineBox)box;
        }

        for (int i = box.getBoxCount() - 1; i >= 0; i--) {
            CssBox child = box.getBox(i);

            if (child instanceof ExternalDocumentBox) {
                continue;
            }

            LineBox match = findLastLineBox(child);

            if (match != null) {
                return match;
            }
        }

        return null;
    }

    /** Find the LineBox corresponding to the given position */
//    private static LineBox findLineBox(PageBox pageBox, Position pos) {
    private static LineBox findLineBox(PageBox pageBox, DomPosition pos) {
//        CssBox box = findBox(webform.getPane().getPageBox(), pos);
        CssBox box = findBox(pageBox, pos);

        if (box == null) {
            return null;
        }

        return findLineBox(box, pos.getBias() == Bias.FORWARD);
    }

    /**
     * Find the LineBox corresponding to the given box.
     * If the box has an element which spans multiple lines (like a <span> whose
     * text doesn't all fit on a single line) the "first" parameter will be
     * used to decide if we should return the first linebox matching or the last
     * linebox matching this box.
     */
    private static LineBox findLineBox(CssBox box, boolean first) { // HACK ALERT

        CssBox curr = box;

        while ((curr != null) && /*isSpan() &&*/
                !(curr instanceof LineBoxGroup)) {
            curr = curr.getParent();
        }

        if (curr == null) {
            return null;
        }

        if (!(curr instanceof LineBoxGroup)) {
            // I must have run into an block box or absolutely positioned
            // box of some sort;
            // Look downwards for the first normal flow child and
            // if it's a line box group, use that
            CssBox firstBox = curr.getFirstNormalBox();

            if (firstBox instanceof LineBoxGroup) {
                // HACK - returning first linebox - should find closest instead!!
                // XXX is there any chance this could be a float???
                return (LineBox)firstBox.getBox(0);
            } else {
                return null;
            }
        }

        LineBoxGroup lbg = (LineBoxGroup)curr;

        for (int i = 0, n = lbg.getBoxCount(); i < n; i++) {
            CssBox b = lbg.getBox(i);

            if (b instanceof LineBox) {
                LineBox lb = (LineBox)b;

                for (int j = 0, m = lb.getBoxCount(); j < m; j++) {
                    if (lb.getBox(j) == box) {
                        return lb;
                    }
                }
            } else {
                // Floats can appear in a line context.
                assert b.getBoxType() == BoxType.FLOAT;

                // Float contents aren't in line boxes
                // XXX not true! but won't fix that right now
                continue;
            }
        }

        // We were found in a LineBoxGroup but not in one of its LineBoxes.
        // That means we're a managed box that does not render its own inline box,
        // such as for example "<b>".
        // At this point we could go and search the boxes children to see if any of
        // -them- (or their children, e.g. in <b><i><u>Hello...) render to an inline
        // box. However the point is we shouldn't have even gotten the caret into
        // this position - it should always be associated with an inline position.
        // So rather than hack this here (where could be in trouble - what if there
        // is no inlinebox inside our cursor position, e.g. <b>^</b> - so nothing
        // here is rendered in any linebox, in that case we're hosed. So we need
        // to fix the root cause of this.
        // Uh oh this is unavoidable.
        // Let's say my source has this:   
        //   <h:outputText value="foo"/><h:outputText value="bar"/>
        // Clearly I should be able to put the caret in between these texts. But in
        // The HTML dom I have this:   
        //   <span>foo</span>^<span>bar</span>
        // The only valid caret position here is between the spans (indicated by ^).
        // In terms of the sosurce dom this means I can only put the caret in a position
        // that is rendered into a managed box - which doesn't appear in the line box
        // hierarchy (the <span>.)
        // Thus, I need to go here and make sure I can find line boxes for these things
        // too. Note that a <span> can span multiple lines. Thus the span may have more
        // than one associated linebox. 
//        Node element = getElement(box);
        Node element = getComponentRootElementParentForCssBox(box);
        assert element != null;

        if (first) {
            // Look for the FIRST box present in a linebox for the given managed box
            for (int i = 0, n = lbg.getBoxCount(); i < n; i++) {
                CssBox b = lbg.getBox(i);

                if (b instanceof LineBox) {
                    LineBox lb = (LineBox)b;

                    for (int j = 0, m = lb.getBoxCount(); j < m; j++) {
                        CssBox ilb = lb.getBox(j);
//                        Node node = getElement(ilb);
                        Node node = getComponentRootElementParentForCssBox(ilb);

                        // Search up the DOM to see if this leaf box is inside the given
                        // target box' element
                        while (node != null) {
                            if (node == element) {
                                return lb;
                            }

                            node = node.getParentNode();
                        }
                    }
                } else {
                    // Floats can appear in a line context.
                    assert b.getBoxType() == BoxType.FLOAT;

                    // Float contents aren't in line boxes
                    // XXX not true! but won't fix that right now
                    continue;
                }
            }
        } else {
            // Look for the LAST box present in a linebox for the given managed box
            // Same as above, but we search backwards in the linebox list as
            // well as backwards in each linebox. This ensures that I find the
            // last matching box rather than the first.
            for (int n = lbg.getBoxCount(), i = n - 1; i >= 0; i--) {
                CssBox b = lbg.getBox(i);

                if (b instanceof LineBox) {
                    LineBox lb = (LineBox)b;

                    for (int m = lb.getBoxCount(), j = m - 1; j >= 0; j--) {
                        CssBox ilb = lb.getBox(j);
//                        Node node = getElement(ilb);
                        Node node = getComponentRootElementParentForCssBox(ilb);

                        // Search up the DOM to see if this leaf box is inside the given
                        // target box' element
                        while (node != null) {
                            if (node == element) {
                                return lb;
                            }

                            node = node.getParentNode();
                        }
                    }
                } else {
                    // Floats can appear in a line context.
                    assert b.getBoxType() == BoxType.FLOAT;

                    // Float contents aren't in line boxes
                    // XXX not true! but won't fix that right now
                    continue;
                }
            }
        }

        return null;
    }

    /** Find the box whose corresponding node matches the given node, starting
     * at the given root box. The offset is only relevant when we're dealing with
     * a text box, where the node is split into many boxes.
     */
    public static CssBox findBox(CssBox root, Node target, int offset) { // HACK ALERT

        if (root.getBoxType() == BoxType.TEXT) {
            TextBox tb = (TextBox)root;

            if ((tb.getNode() == target) && (
                /* This doesn't work when the position is somewhere in the DOM I'm
                   not displaying, like in a region of space characters. Since I search
                   the dom forwards however, I don't need this extra check.
                tb.getDomStartOffset() <= offset && */
                offset <= tb.getDomEndOffset())) {
                return tb;
            }

            return null;
        } else if (root.getBoxType() == BoxType.SPACE) {
            SpaceBox tb = (SpaceBox)root;

            if ((tb.getNode() == target) && (
                /* This doesn't work when the position is somewhere in the DOM I'm
                   not displaying, like in a region of space characters. Since I search
                   the dom forwards however, I don't need this extra check.
                tb.getDomStartOffset() <= offset && */
                offset <= tb.getDomEndOffset())) {
                return tb;
            }

            return null;
        }

        if ((root.getElement() == target) || (root.getSourceElement() == target)) {
            return root;
        }

        for (int i = 0, n = root.getBoxCount(); i < n; i++) {
            CssBox child = root.getBox(i);
            CssBox match = findBox(child, target, offset);

            if (match != null) {
                if ((match.getBoxType() == BoxType.SPACE) && (i < (n - 1))) {
                    CssBox match2 = findBox(root.getBox(i + 1), target, offset);

                    if (match2 != null) {
                        if ((match2.getBoxType() == BoxType.TEXT) &&
                                (((TextBox)match2).getDomStartOffset() >= offset)) {
                            return match2;
                        } else if (match2.getBoxType() != BoxType.SPACE) {
                            return match2;
                        }
                    }
                }

                return match;
            }
        }

        return null;
    }

    /** Find the CssBox corresponding to a given position */
//    private static CssBox findBox(PageBox root, Position pos) { // HACK ALERT
    private static CssBox findBox(PageBox root, DomPosition pos) { // HACK ALERT

//        if (pos == Position.NONE) {
        if (pos == DomPosition.NONE) {
            return null;
        }

        Node node = pos.getNode();
        int offset = pos.getOffset();

        if ((node.getNodeType() == Node.TEXT_NODE) ||
                (node.getNodeType() == Node.CDATA_SECTION_NODE)) {
            // Text node: locate the corresponding line box
            // The offset may point to a collapsed space character
            // so watch out:)
            Node p = node.getParentNode();
            CssBox box = null;

            // Try to shortcut the search through the box hierarchy for
            // the node matching this position by looking up the parent box
            if ((p != null) && (p.getNodeType() == Node.ELEMENT_NODE)) {
                Element pe = (Element)p;
//                CssBox pb = CssBox.getBox(pe);
                CssBox pb = root.getWebForm().findCssBoxForElement(pe);

                if (pb != null) {
                    box = findBox(pb, node, offset);

                    if ((box == null) && (node.getNodeValue().length() == 0) &&
                            (node.getNextSibling() != null)) {
                        // When I'm doing inline editing, I may be deleting
                        // text within a node, so I end up pointing to
                        // an empty text. If so try to look for the position
                        // next to it instead.
                        box = findBox(pb, node.getNextSibling(), 0);
                    }

                    if ((box == null) && DesignerUtils.onlyWhitespace(node.getNodeValue())) {
                        // Perhaps it's a space box we've pulled
                        if (pos.getBias() == Bias.FORWARD) {
                            if (node.getNextSibling() != null) {
                                box = findBox(pb, node.getNextSibling(), 0);
                            }
                        } else { // Bias.BACKWARD

                            if (node.getPreviousSibling() != null) {
                                box = findBox(pb, node.getPreviousSibling(), 0);
                            }
                        }
                    }
                }
            }

            if (box == null) {
                box = findBox(root, node, offset);
            }

            return box;
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            // Element node
            // offset 0 means before the element, offset 1 means after
            Element element = (Element)node;
            NodeList list = element.getChildNodes();
            int len = list.getLength();

            if ((len == 0) || (offset >= len)) {
                // We have a 0 index but no children, e.g. we're
                // inside an empty element.
                // XXX Can we do anything here? For now just use the parent
                // element
            } else if ((pos.getBias() == Bias.BACKWARD) && (offset > 0) && ((offset - 1) < len)) {
                Node child = list.item(offset - 1);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    element = (Element)child;
                } else if ((child.getNodeType() == Node.TEXT_NODE) ||
                        (child.getNodeType() == Node.CDATA_SECTION_NODE)) {
//                    CssBox pb = CssBox.getBox(element);
                    CssBox pb = root.getWebForm().findCssBoxForElement(element);
                    
                    CssBox box = null;

                    if (pb != null) {
                        box = findBox(pb, child, offset);

                        if ((box == null) && (child.getNodeValue().length() == 0) &&
                                (child.getNextSibling() != null)) {
                            // When I'm doing inline editing, I may be deleting
                            // text within a node, so I end up pointing to
                            // an empty text. If so try to look for the position
                            // next to it instead.
                            box = findBox(pb, child.getNextSibling(), 0);
                        }
                    }

                    if (box == null) {
                        box = findBox(root, child, offset);
                    }

                    return box;
                }
            } else if (offset < len) {
                Node child = list.item(offset);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    element = (Element)child;
                } // else use parent -- which we already have stored in element
            }

//            CssBox box = CssBox.getBox(element);
            CssBox box = root.getWebForm().findCssBoxForElement(element);

            // Todo: find the nearest linebox
            return box;
        } else {
            // Not sure how to handle this one
            ErrorManager.getDefault().log("Unexpected node type in findBox: " + node);

            return null;
        }
    }

    /**
     * For the given caret position, compute the visible caret rectangle
     * on the screen.  Note that the position must fit certain criteria:
     * it needs to point inside a LineBox (e.g. be a valid caret position).
     * @param A position in the source dom (where carets live for example)
     */
//    public static Rectangle modelToView(PageBox pageBox, Position sourcePos) {
    public static Rectangle modelToView(PageBox pageBox, DomPosition sourcePos) {
        // assert that the position is a valid position here?
//        if (sourcePos == Position.NONE) {
        if (sourcePos == DomPosition.NONE) {
            return null;
        }

//        assert !sourcePos.isRendered();
//        if (MarkupService.isRenderedNode(sourcePos.getNode())) {
        if (sourcePos.isRenderedPosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node is expected to be not rendered, node=" + sourcePos.getNode())); // NOI18N
        }

//        Position pos = sourcePos.getRenderedPosition();
        DomPosition pos = sourcePos.getRenderedPosition();

//        if (pos == Position.NONE) {
        if (pos == DomPosition.NONE) {
            return null; // not yet rendered
        }

        Node node = pos.getNode();
        int offset = pos.getOffset();

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // XXX this should not be necessary!
            //XhtmlElement xel = (XhtmlElement)node;
            //node = xel.getSourceElement();
            if (node.getChildNodes().getLength() == 0) {
                // Have no children
                // Try to use font height instead
                Element element = (Element)node;
//                CssBox box = CssBox.getBox(element);
                CssBox box = pageBox.getWebForm().findCssBoxForElement(element);

                // Find the first actual item in a linebox since parents
                // in inline context don't get inserted. But what about floats??
                while ((box != null) && (box.getHeight() == CssBox.UNINITIALIZED) &&
                        (box.getBoxCount() > 0)) {
                    box = box.getBox(0);
                }

                // Todo: find the nearest linebox
                if (box != null) {
                    int height = box.getHeight();
//                    Font font = CssLookup.getFont(element, DesignerSettings.getInstance().getDefaultFontSize());
//                    Font font = CssProvider.getValueService().getFontForElement(element, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//                    if (font != null) {
//                        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
//                        height = metrics.getHeight();
//                    }
                    // XXX Missing text.
                    height = CssUtilities.getDesignerFontMetricsForElement(element, null, pageBox.getWebForm().getDefaultFontSize()).getHeight();

                    Rectangle r =
                        new Rectangle(box.getAbsoluteX(), box.getAbsoluteY(), box.getWidth(), height);

                    if (box.isInlineBox()) {
                        r.y = (box.getAbsoluteY() + box.getHeight()) - height;
                    }

                    return r;
                }

                return null;
            } else if ((pos.getBias() == Bias.BACKWARD) && (offset > 0) &&
                    ((offset - 1) < node.getChildNodes().getLength())) {
                node = node.getChildNodes().item(offset - 1);
                offset = 1;
            } else if (offset < node.getChildNodes().getLength()) {
                node = node.getChildNodes().item(offset);
                offset = 0;
            } else if (offset > 0) {
                // XXX not a good idea for <br> !
                // XXX This is highly suspect!!! I should probably use node.getChildNodes().getLength()-1 here!!! not offset-1
                node = node.getChildNodes().item(offset - 1);

                if (node.getNodeType() == Node.TEXT_NODE) {
                    offset = node.getNodeValue().length();
                } else {
                    offset = 1; // XXX ?
                }
            }
        } else if (node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            if (node.getChildNodes().getLength() == 0) {
                // Have no children
                return null; // this shouldn't happen!!
            } else if ((pos.getBias() == Bias.BACKWARD) && (offset > 0) &&
                    ((offset - 1) < node.getChildNodes().getLength())) {
                node = node.getChildNodes().item(offset - 1);
                offset = 1;
            } else if (offset < node.getChildNodes().getLength()) {
                node = node.getChildNodes().item(offset);
                offset = 0;
            } else if (offset > 0) {
                // XXX not a good idea for <br> !
                node = node.getChildNodes().item(offset - 1);

                if (node.getNodeType() == Node.TEXT_NODE) {
                    offset = node.getNodeValue().length();
                } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                    return modelToView(pageBox,
//                        new Position(node, node.getChildNodes().getLength(), Bias.FORWARD));
//                            Position.create(node, node.getChildNodes().getLength(), Bias.FORWARD));
                            pageBox.getWebForm().createDomPosition(node, node.getChildNodes().getLength(), Bias.FORWARD));
                } else {
                    offset = 1; // XXX ?
                }
            }
        }

        if ((node.getNodeType() == Node.TEXT_NODE) ||
                (node.getNodeType() == Node.CDATA_SECTION_NODE)) {
            //            // XXX this should not be necessary!
            //            if (node instanceof XhtmlText) {
            //                XhtmlText xt = (XhtmlText)node;
            //                node = xt.getSourceNode();
            //            }
            // Text node: locate the corresponding line box
            // The offset may point to a collapsed space character
            // so watch out:)
            Node p = node.getParentNode();
            CssBox pb = null;
            CssBox box = null;

            // Try to shortcut the search through the box hierarchy for
            // the node matching this position by looking up the parent box
            if ((p != null) && (p.getNodeType() == Node.ELEMENT_NODE)) {
                Element pe = (Element)p;
//                pb = CssBox.getBox(pe);
                pb = pageBox.getWebForm().findCssBoxForElement(pe);

                if (pb != null) {
                    // XXX here we should use the SOURCE node!
                    box = findBox(pb, node, offset);
                }

                //                if (box == null) {
                //                    LineBox lb = findLineBox(pb);
                //                    if (lb != null) {
                //                        box = findBox(lb, node, offset);                        
                //                    }
                //                }
            }

            if (box == null) {
                box = findBox(pageBox, node, offset);
            }

            if ((box == null) &&
                    ((node.getNodeType() == Node.TEXT_NODE) ||
                    (node.getNodeType() == Node.CDATA_SECTION_NODE)) &&
                    (node.getNextSibling() != null) &&
                    (node.getNextSibling().getNodeType() == Node.ELEMENT_NODE) &&
                    ((Element)node.getNextSibling()).getTagName().equals(HtmlTag.BR.name)) {
                box = findBox(pageBox, node.getNextSibling(), 0);
            }

            if (box != null) {
                int x = box.getAbsoluteX();

                if (box.getBoxType() == BoxType.TEXT) {
//                    return ((TextBox)box).getBoundingBox(new Position(node, offset, Bias.FORWARD));
//                    return ((TextBox)box).getBoundingBox(Position.create(node, offset, Bias.FORWARD));
                    return ((TextBox)box).getBoundingBox(pageBox.getWebForm().createDomPosition(node, offset, Bias.FORWARD));
                } else if (box.getBoxType() == BoxType.SPACE) {
                    return ((SpaceBox)box).getBoundingBox(pageBox.getWebForm().createDomPosition(node, offset, Bias.FORWARD));

                    /*
                    } else if (box instanceof FormComponentBox) {
                    return ((FormComponentBox)box).getBoundingBox(new Position(node, offset));
                    */
                }

                return new Rectangle(x, box.getAbsoluteY(), box.getWidth(), box.getHeight());
            }

            if ((offset > 0) && (node.getNodeValue().charAt(offset - 1) == ' ')) {
                // Special case: the caret is after a space at the end of a text node;
                // in this case, since we don't create boxes for spaces, there's no
                // corresponding box recording the location. So find the most recent
                // box instead.
                box = findBox(pageBox, node, offset - 1);

                if (box != null) {
                    Rectangle r = null;

                    if (box.getBoxType() == BoxType.TEXT) {
//                        r = ((TextBox)box).getBoundingBox(new Position(node, offset, Bias.FORWARD));
//                        r = ((TextBox)box).getBoundingBox(Position.create(node, offset, Bias.FORWARD));
                        r = ((TextBox)box).getBoundingBox(pageBox.getWebForm().createDomPosition(node, offset, Bias.FORWARD));
                        r.width += ((TextBox)box).getMetrics().charWidth(' ');

                        return r;
                    } else if (box.getBoxType() == BoxType.SPACE) {
//                        r = ((SpaceBox)box).getBoundingBox(new Position(node, offset, Bias.FORWARD));
//                        r = ((SpaceBox)box).getBoundingBox(Position.create(node, offset, Bias.FORWARD));
                        r = ((SpaceBox)box).getBoundingBox(pageBox.getWebForm().createDomPosition(node, offset, Bias.FORWARD));
                        r.width += ((SpaceBox)box).getMetrics().charWidth(' ');

                        return r;
                    }

                    int SPACE = 5; // hack - gotta find true space width

                    return new Rectangle(box.getAbsoluteX() + SPACE, box.getAbsoluteY(),
                        box.getWidth(), box.getHeight());
                }
            }

            // XXX do I have the same issue with space in front of
            // boxes too?
            if (pb != null) {
                int height = pb.getHeight();
//                Font font = CssLookup.getFont(pb.getElement(), DesignerSettings.getInstance().getDefaultFontSize());
//                Font font = CssProvider.getValueService().getFontForElement(pb.getElement(), DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//                if (font != null) {
//                    FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
//                    height = metrics.getHeight();
//                }
                // XXX Missing text.
                height = CssUtilities.getDesignerFontMetricsForElement(pb.getElement(), null, pageBox.getWebForm().getDefaultFontSize()).getHeight();

                // Try to find a sibling box
                Node curr = node.getPreviousSibling();

                while (curr != null) {
                    box = findBox(pb, curr, 0);

                    if (box != null) {
                        break;
                    }

                    curr = curr.getPreviousSibling();
                }

                if (box != null) {
                    // THIS IS A HACK! Just trying to get closer to the real position of the cursor
                    Rectangle r =
                        new Rectangle(box.getAbsoluteX() + box.getWidth(), box.getAbsoluteY(),
                            box.getWidth(), height);

                    // XXX adjust to get to the bottom as well??? (if inline box) as done elsewhere...
                    curr = curr.getNextSibling();

                    while (curr != node) {
                        if (curr instanceof Element && ((Element)curr).getTagName().equals("br")) {
                            // newline
                            r.y += height;
                            r.x = box.getAbsoluteX();
                        }

                        curr = curr.getNextSibling();
                    }

                    return r;
                }

                Rectangle r =
                    new Rectangle(pb.getAbsoluteX(), pb.getAbsoluteY(), pb.getWidth(), height);

                return r;
            }

            return null;
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            // Element node
            // offset 0 means before the element, offset 1 means after
            Element element = (Element)node;
//            CssBox box = CssBox.getBox(element);
            CssBox box = pageBox.getWebForm().findCssBoxForElement(element);

            // Find the first actual item in a linebox since parents
            // in inline context don't get inserted. But what about floats??
            while ((box != null) && (box.getHeight() == CssBox.UNINITIALIZED) &&
                    (box.getBoxCount() > 0)) {
                box = box.getBox(0);
            }

            if (box == null) {
                box = findBox(pageBox, node, offset);
            }

//            if ((box != null) && (box.getDesignBean() == null)) {
//            if ((box != null) && (CssBox.getMarkupDesignBeanForCssBox(box) == null)) {
            if ((box != null) && (CssBox.getElementForComponentRootCssBox(box) == null)) {
                // We have a generated box... go find the associated line box
                LineBox lb = findLineBox(box, offset == 0);

                if (lb != null) {
                    //CssBox lastBox = findBeanBox(lb, box.getDesignBean(), offset == 0);
                    CssBox lastBox = null;
//                    DesignBean bean = getBean(box);
                    Element parentComponentRootElement = getComponentRootElementParentForCssBox(box);
                    boolean first = (offset == 0);

                    CssBox beanBox = null;

                    for (int i = 0, n = lb.getBoxCount(); i < n; i++) {
                        CssBox ilb = lb.getBox(i);

//                        if (getBean(ilb) == bean) {
                        if (getComponentRootElementParentForCssBox(ilb) == parentComponentRootElement) {
                            beanBox = ilb;

                            if (first) {
                                break;
                            }
                        }
                    }

                    lastBox = beanBox;

                    if (lastBox != null) {
                        box = lastBox;
                    }
                }
            }

            // Todo: find the nearest linebox
            if (box != null) {
                int height = box.getHeight();

                // Try to use font height instead
//                Font font = CssLookup.getFont(element, DesignerSettings.getInstance().getDefaultSize());
//                Font font = CssProvider.getValueService().getFontForElement(element, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//                if (font != null) {
//                    FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
//                    height = metrics.getHeight();
//                }
                // XXX Missing text.
                height = CssUtilities.getDesignerFontMetricsForElement(element, null, pageBox.getWebForm().getDefaultFontSize()).getHeight();

                Rectangle r =
                    new Rectangle(box.getAbsoluteX(), box.getAbsoluteY(), box.getWidth(), height);

                if (offset == 1) {
                    r.x += box.getWidth();
                }

                if (box.isInlineBox()) {
                    r.y = (box.getAbsoluteY() + box.getHeight()) - height;
                }

                return r;
            }

            return null;
        } else {
            // Not sure how to handle this one
            ErrorManager.getDefault().log("Unexpected node type in modelToView: " + node);

            return null;
        }
    }

//    /** Find the DesignBean associated with a particular box. */
//    private static DesignBean getBean(CssBox box) {
////        RaveElement element = getElement(box);
//        Element element = getElement(box);
//
////        if (element != null) {
////            return element.getDesignBean();
////        }
////
////        return null;
////        return InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//        return WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//    }
//
//    private static Element getElement(CssBox box) {
////        RaveElement element = (RaveElement)box.getElement();
//        Element element = box.getElement();
//
//        while (true) {
//            //if (element.getDesignBean() != null) {
//            //    return element;
//            //}
////            if (element.getSource() != null) {
//            if (MarkupService.getSourceElementForElement(element) != null) {
//                return element;
//            }
//
//            Node parent = element.getParentNode();
//
//            if (parent instanceof Element) {
//                element = (Element)parent;
//            } else {
//                return null;
//            }
//        }
//    }
    
    private static Element getComponentRootElementParentForCssBox(CssBox cssBox) {
        while (true) {
            Element componentRootElement = CssBox.getElementForComponentRootCssBox(cssBox);
            if (componentRootElement != null) {
                return componentRootElement;
            }
            
            cssBox = cssBox.getParent();
            if (cssBox == null) {
                return null;
            }
        }        
    }

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.
     * The returned position is always a position in the source DOM,
     * not a position in the rendered DOM (unless it is Position.NONE
     * which means the position does not correlate to a position in the document.)
     *
     *
     * @todo Find a better home for this method
     * @param x the X coordinate >= 0
     * @param y the Y coordinate >= 0
     * @return the location within the model that best represents the
     *  given point in the view >= 0.
     */
//    public static Position viewToModel(WebForm webform, int x, int y) {
    public static DomPosition viewToModel(WebForm webform, int x, int y) {
        try {
//            Position pos = findClosestPosition(webform, x, y);
            DomPosition pos = findClosestPosition(webform, x, y);

//            if ((pos != Position.NONE) && pos.isRendered()) {
//            if ((pos != Position.NONE)
            if ((pos != DomPosition.NONE)
//            && MarkupService.isRenderedNode(pos.getNode())) {
            && pos.isRenderedPosition()) {
                return pos.getSourcePosition();
            } else {
                return pos;
            }

            // Assert that the position computed is a valid caret position (e.g.
            // points inside a LineBox) here?
        } catch (Throwable t) {
            ErrorManager.getDefault().notify(t);

//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /** For the given box, locate the position closest to the given x position
     */
//    private static Position findClosestPosition(WebForm webform, int x, int y) {
    private static DomPosition findClosestPosition(WebForm webform, int x, int y) {
        CssBox box = webform.getPane().getPageBox().findCssBox((int)x, (int)y);

        if (box == null) {
//            return Position.NONE;
            return DomPosition.NONE;
        }

        assert !box.isPlaceHolder();

        if (box instanceof LineBoxGroup) {
            // We have clicked on a line that is not covered by a LineBox but
            // is inside a group of line boxes - e.g. this line is shorter than
            // others. Find it.
//            Position pos = findLineBoxClosestTo(box, y, webform.getManager().getInlineEditor());
            DomPosition pos = findLineBoxClosestTo(box, y, webform.getManager().getInlineEditor());

//            if (pos != Position.NONE) {
            if (pos != DomPosition.NONE) {
                return pos;
            }
        }

        // If you hit a linebox, just use that
        LineBox lb = findLineBox(box, true);

        if (lb != null) {
//            Position p = lb.computePosition(x - lb.getAbsoluteX());
            DomPosition p = lb.computePosition(x - lb.getAbsoluteX());

//            if (DesignerUtils.checkPosition(p, false, webform.getManager().getInlineEditor()) != Position.NONE) {
//            if (isValidPosition(p, false, webform.getManager().getInlineEditor())) {
            if (isValidPosition(webform, p, false, webform.getManager().getInlineEditor())) {
                return p;
            }
        }

//        Position pos = findLineBoxClosestTo(box, y, webform.getManager().getInlineEditor());
        DomPosition pos = findLineBoxClosestTo(box, y, webform.getManager().getInlineEditor());

//        if (pos != Position.NONE) {
        if (pos != DomPosition.NONE) {
            return pos;
        }

        // No linebox, so look both above and below for matches,
        // and pick the closest location
        LineBox prevLine = findPrevLineBox(box);

        if (prevLine == null) {
            prevLine = findLastLineBox(box);
        }

        Rectangle prev = null;
//        Position prevPos = null;
        DomPosition prevPos = null;

        if (prevLine != null) {
            prevPos = findLastLineboxPosition(prevLine, webform.getManager().getInlineEditor());

//            if (prevPos != Position.NONE) {
            if (prevPos != DomPosition.NONE) {
//                if (prevPos.isRendered()) {
//                if (MarkupService.isRenderedNode(prevPos.getNode())) {
                if (prevPos.isRenderedPosition()) {
                    prevPos = prevPos.getSourcePosition();
                }

                prev = modelToView(webform.getPane().getPageBox(), prevPos);
            }
        }

        LineBox nextLine = findNextLineBox(box);

        if (nextLine == null) {
            nextLine = findFirstLineBox(box);
        }

        Rectangle next = null;
//        Position nextPos = null;
        DomPosition nextPos = null;

        if (nextLine != null) {
            nextPos = findFirstLineboxPosition(nextLine, 0, webform.getManager().getInlineEditor());

//            if (nextPos != Position.NONE) {
            if (nextPos != DomPosition.NONE) {
//                if (nextPos.isRendered()) {
//                if (MarkupService.isRenderedNode(nextPos.getNode())) {
                if (nextPos.isRenderedPosition()) {
                    nextPos = nextPos.getSourcePosition();
                }

                next = modelToView(webform.getPane().getPageBox(), nextPos);
            }
        }

        if ((next != null) && (prev != null)) {
            if (prevLine == nextLine) {
                return prevPos;
            }

            // See if we're closer to the first or the last line box - pick whichever
            // one is closest. Note however that we only look at the y coordinate, not
            // the real distance (Math.sqrt(dx*dx+dy*dy))
            if (Math.abs(y - next.y) < Math.abs(y - prev.y)) {
                return nextPos;
            } else {
                return prevPos;
            }
        } else if (next != null) {
            return nextPos;
        } else if (prev != null) {
            return prevPos;
        } else {
            /* XXX I can't do this because I'm called when the read lock for
             * view hierarchy is held, and calling into DesignBean creation
             * will require a write lock on the unit...
            if (!webform.isGridMode()) {
                // There are no caret positions in the document. We should create one.
                // Are we closer to the top or the end? Just use y position.
                if (y > pageBox.getHeight()/2) {
                    return createEndPosition();
                } else {
                    return createBeginPosition();
                }
            }
             */

            //assert prevPos == Position.NONE && nextPos == Position.NONE : prevPos + "," + nextPos;
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

//    private static Position findLineBoxClosestTo(CssBox box, int y, InlineEditor inlineEditor) {
    private static DomPosition findLineBoxClosestTo(CssBox box, int y, InlineEditor inlineEditor) {
        if ((box.getBoxType() == BoxType.LINEBOX) && box instanceof LineBox) {
            LineBox lb = (LineBox)box;
            int cy = lb.getAbsoluteY();

            if ((y == CssBox.AUTO) || ((y >= cy) && (y <= (cy + lb.getHeight())))) {
                return findLastLineboxPosition(lb, inlineEditor);
            }
        }

        if (!(box instanceof ExternalDocumentBox)) {
            // We've clicked outside of any of the children; see
            // if we can find a closer one
            for (int i = 0, n = box.getBoxCount(); i < n; i++) {
//                Position match = findLineBoxClosestTo(box.getBox(i), y, inlineEditor);
                DomPosition match = findLineBoxClosestTo(box.getBox(i), y, inlineEditor);

                if (match != null) {
                    return match;
                }
            }
        }

//        return Position.NONE;
        return DomPosition.NONE;
    }

    /** Return the position at the beginning of the line containing the given position */
//    public static Position getLineBegin(WebForm webform, Position sourcePos) {
    public static DomPosition getLineBegin(WebForm webform, DomPosition sourcePos) {
//        assert !sourcePos.isRendered();
//        if (MarkupService.isRenderedNode(sourcePos.getNode())) {
        if (sourcePos.isRenderedPosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node is expected to be not rendered, node=" + sourcePos.getNode())); // NOI18N
        }

//        Position pos = sourcePos.getRenderedPosition();
        DomPosition pos = sourcePos.getRenderedPosition();

        LineBox lb = findLineBox(webform.getPane().getPageBox(), pos);

        if (lb == null) {
            assert false : pos; // Caret positions should always be in a linebox!!

//            return Position.NONE;
            return DomPosition.NONE;
        }

//        Position p = findFirstLineboxPosition(lb, 0, webform.getManager().getInlineEditor());
        DomPosition p = findFirstLineboxPosition(lb, 0, webform.getManager().getInlineEditor());

        return p.getSourcePosition();
    }

    /** Return the position at the end of the line containing the given position */
//    public static Position getLineEnd(WebForm webform, Position sourcePos) {
    public static DomPosition getLineEnd(WebForm webform, DomPosition sourcePos) {
//        assert !sourcePos.isRendered();
//        if (MarkupService.isRenderedNode(sourcePos.getNode())) {
        if (sourcePos.isRenderedPosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node is expected to be not rendered, node=" + sourcePos.getNode())); // NOI18N
        }

//        Position pos = sourcePos.getRenderedPosition();
        DomPosition pos = sourcePos.getRenderedPosition();

        LineBox lb = findLineBox(webform.getPane().getPageBox(), pos);

        if (lb == null) {
//            assert false : pos; // Caret positions should always be in a linebox!!

//            return Position.NONE;
            return DomPosition.NONE;
        }

//        Position p = findLastLineboxPosition(lb, webform.getManager().getInlineEditor());
        DomPosition p = findLastLineboxPosition(lb, webform.getManager().getInlineEditor());

        return p.getSourcePosition();
    }

    /** Find the beginning of the word from the given position */
//    public static Position getWordStart(PageBox pageBox, Position sourcePos) {
    public static DomPosition getWordStart(PageBox pageBox, DomPosition sourcePos) {
//        assert !sourcePos.isRendered();
//        if (MarkupService.isRenderedNode(sourcePos.getNode())) {
        if (sourcePos.isRenderedPosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node is expected to be not rendered, node=" + sourcePos.getNode()));
        }

//        Position pos = sourcePos.getRenderedPosition();
        DomPosition pos = sourcePos.getRenderedPosition();

//        PageBox pageBox = webform.getPane().getPageBox();
        CssBox box = findBox(pageBox, pos);
        
        // XXX #110083 Possible NPE.
        if (box == null) {
            return DomPosition.NONE;
        }

        if (box.getBoxType() == BoxType.TEXT) {
            TextBox tb = (TextBox)box;
//            Position p = new Position(tb.getNode(), tb.getDomStartOffset(), Bias.FORWARD);
//            Position p = Position.create(tb.getNode(), tb.getDomStartOffset(), Bias.FORWARD);
            DomPosition p = pageBox.getWebForm().createDomPosition(tb.getNode(), tb.getDomStartOffset(), Bias.FORWARD);

            return p.getSourcePosition();
        } else if (box.getBoxType() == BoxType.SPACE) {
            SpaceBox tb = (SpaceBox)box;
//            Position p = new Position(tb.getNode(), tb.getDomStartOffset(), Bias.FORWARD);
//            Position p = Position.create(tb.getNode(), tb.getDomStartOffset(), Bias.FORWARD);
            DomPosition p = pageBox.getWebForm().createDomPosition(tb.getNode(), tb.getDomStartOffset(), Bias.FORWARD);

            return p.getSourcePosition();
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /** Find the end of the word from the given position */
//    public static Position getWordEnd(PageBox pageBox, Position sourcePos) {
    public static DomPosition getWordEnd(PageBox pageBox, DomPosition sourcePos) {
//        assert !sourcePos.isRendered();
//        if (MarkupService.isRenderedNode(sourcePos.getNode())) {
        if (sourcePos.isRenderedPosition()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node is expected to be not rendered, node=" + sourcePos.getNode()));
        }

//        Position pos = sourcePos.getRenderedPosition();
        DomPosition pos = sourcePos.getRenderedPosition();

//        PageBox pageBox = webform.getPane().getPageBox();
        CssBox box = findBox(pageBox, pos);

        if (box.getBoxType() == BoxType.TEXT) {
            TextBox tb = (TextBox)box;
//            Position p = new Position(tb.getNode(), tb.getDomEndOffset(), Bias.BACKWARD);
//            Position p = Position.create(tb.getNode(), tb.getDomEndOffset(), Bias.BACKWARD);
            DomPosition p = pageBox.getWebForm().createDomPosition(tb.getNode(), tb.getDomEndOffset(), Bias.BACKWARD);

            return p.getSourcePosition();
        } else if (box.getBoxType() == BoxType.SPACE) {
            SpaceBox tb = (SpaceBox)box;
//            Position p = new Position(tb.getNode(), tb.getDomEndOffset(), Bias.BACKWARD);
//            Position p = Position.create(tb.getNode(), tb.getDomEndOffset(), Bias.BACKWARD);
            DomPosition p = pageBox.getWebForm().createDomPosition(tb.getNode(), tb.getDomEndOffset(), Bias.BACKWARD);

            return p.getSourcePosition();
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /**
     * Return the first caret position in the document. If create is true,
     * create one if necessary.
     */
//    public static Position getFirstDocumentPosition(WebForm webform, boolean create) {
    public static DomPosition getFirstDocumentPosition(WebForm webform, boolean create) {
        // XXX Very suspicious assertion.
//        assert webform.getPane().getPageBox().getElement().getOwnerDocument() == webform.getJspDom();
        if (webform.getPane().getPageBox().getElement().getOwnerDocument() != webform.getHtmlDom()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Owner document is expected to be html dom=" + webform.getHtmlDom() // NOI18N
                    + ", but it is dom=" + webform.getPane().getPageBox().getElement().getOwnerDocument())); // NOI18N
        }

        // First try to put the caret inside the the default parent of the component.
        // This will avoid bugs like 6252951 where we start out with a caret
        // outside the form component, which is probably where users want components
        // added.
//        FacesPageUnit unit = webform.getModel().getFacesUnit();
//
//        if (unit != null) {
//            MarkupBean mb = unit.getDefaultParent();
////            RaveElement element = (RaveElement)mb.getElement();
//            Element element = mb.getElement();
        
//        Element element = webform.getDefaultParentMarkupBeanElement();
//        if (element != null) {

//            if (element.getDesignBean() != null) {
////                CssBox box = findBox(element.getDesignBean());
//                CssBox box = findBox(webform.getPane().getPageBox(), element.getDesignBean());
//            MarkupDesignBean markupDesignBean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//            MarkupDesignBean markupDesignBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//            if (markupDesignBean != null) {
//                CssBox box = findBox(webform.getPane().getPageBox(), markupDesignBean);
        
//            Element componentRootElement = MarkupService.getRenderedElementForElement(element);
            Element componentRootElement = webform.getDefaultParentComponent();
            if (componentRootElement != null) {
                CssBox box = findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);

                if (box != null) {
                    LineBox lb = findFirstLineBox(box);

                    if (lb != null) {
//                        Position pos = findFirstLineboxPosition(lb, 0, webform.getManager().getInlineEditor());
                        DomPosition pos = findFirstLineboxPosition(lb, 0, webform.getManager().getInlineEditor());

//                        if (pos != Position.NONE) {
                        if (pos != DomPosition.NONE) {
//                            if (pos.isRendered()) {
//                            if (MarkupService.isRenderedNode(pos.getNode())) {
                            if (pos.isRenderedPosition()) {
                                return pos.getSourcePosition();
                            } else {
                                return pos;
                            }
                        }
                    }
                }
            }
//        }

        if (webform.getPane().getPageBox() != null) {
            LineBox lb = findFirstLineBox(webform.getPane().getPageBox());

            if (lb != null) {
//                Position pos = findFirstLineboxPosition(lb, 0, webform.getManager().getInlineEditor());
                DomPosition pos = findFirstLineboxPosition(lb, 0, webform.getManager().getInlineEditor());

//                if (pos != Position.NONE) {
                if (pos != DomPosition.NONE) {
//                    if (pos.isRendered()) {
//                    if (MarkupService.isRenderedNode(pos.getNode())) {
                    if (pos.isRenderedPosition()) {
                        return pos.getSourcePosition();
                    } else {
                        return pos;
                    }
                }
            }
        }

        // Try to pick a place to set the caret
        // First, if we have a webform, set the caret inside the <h:form> if
        // one exists (and has a <br>)
        // TODO; this is tricky. We've gotta make sure we only use
        // normal flow children, not absolutely positioned children etc.
        //        assert false : "need layout tree to set caret";
//        return Position.NONE;
        return DomPosition.NONE;

        /*
        Element body = webform.getMarkup().getBody();
        NodeList children = body.getChildNodes();
        Position pos;
        if (children.getLength() > 0 &&
            children.item(0).getNodeType() == Node.ELEMENT_NODE &&
            ((Element)children.item(0)).getTagName().equals("p")) { // NOI18N
            pos = new Position(children.item(0), 0, Bias.FORWARD);
        } else if (children.getLength() > 1 &&
                   children.item(0).getNodeType() == Node.TEXT_NODE &&
                   Utilities.onlyWhitespace(children.item(0).getNodeValue()) &&
                   children.item(1).getNodeType() == Node.ELEMENT_NODE &&
                   ((Element)children.item(1)).getTagName().equals("p")) { // NOI18N
            pos = new Position(children.item(1), 0, Bias.FORWARD);
        } else {
            pos = new Position(webform.getDocument().getBody(), 0, Bias.FORWARD);
        }

         */
    }

    /**
     * Return the last caret position in the document. If create is true,
     * create one if necessary.
     */
//    public static Position getLastDocumentPosition(WebForm webform, boolean create) {
    public static DomPosition getLastDocumentPosition(WebForm webform, boolean create) {
        // XXX Very suspicious assertion.
//        assert webform.getPane().getPageBox().getElement().getOwnerDocument() == webform.getJspDom();
        if (webform.getPane().getPageBox().getElement().getOwnerDocument() != webform.getHtmlDom()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Owner document is expected to be html dom=" + webform.getHtmlDom() // NOI18N
                    + ", but it is dom=" + webform.getPane().getPageBox().getElement().getOwnerDocument())); // NOI18N
        }

        if (webform.getPane().getPageBox() != null) {
            LineBox lb = findLastLineBox(webform.getPane().getPageBox());

            if (lb != null) {
//                Position pos = findLastLineboxPosition(lb, lb.getBoxCount() - 1, webform.getManager().getInlineEditor());
                DomPosition pos = findLastLineboxPosition(lb, lb.getBoxCount() - 1, webform.getManager().getInlineEditor());

//                if (pos != Position.NONE) {
                if (pos != DomPosition.NONE) {
//                    if (pos.isRendered()) {
//                    if (MarkupService.isRenderedNode(pos.getNode())) {
                    if (pos.isRenderedPosition()) {
                        return pos.getSourcePosition();
                    } else {
                        return pos;
                    }
                }
            }
        }

        // Try to pick a place to set the caret
        // First, if we have a webform, set the caret inside the <h:form> if
        // one exists (and has a <br>)
        // TODO; this is tricky. We've gotta make sure we only use
        // normal flow children, not absolutely positioned children etc.
        //        assert false : "need layout tree to set caret";
//        return Position.NONE;
        return DomPosition.NONE;

        /*
        Element body = webform.getMarkup().getBody();
        NodeList children = body.getChildNodes();
        Position pos;
        if (children.getLength() > 0 &&
            children.item(0).getNodeType() == Node.ELEMENT_NODE &&
            ((Element)children.item(0)).getTagName().equals("p")) { // NOI18N
            pos = new Position(children.item(0), 0, Bias.FORWARD);
        } else if (children.getLength() > 1 &&
                   children.item(0).getNodeType() == Node.TEXT_NODE &&
                   Utilities.onlyWhitespace(children.item(0).getNodeValue()) &&
                   children.item(1).getNodeType() == Node.ELEMENT_NODE &&
                   ((Element)children.item(1)).getTagName().equals("p")) { // NOI18N
            pos = new Position(children.item(1), 0, Bias.FORWARD);
        } else {
            pos = new Position(webform.getDocument().getBody(), 0, Bias.FORWARD);
        }

         */
    }
    

//    public static boolean isValidPosition(Position pos, boolean adjust, InlineEditor inline) {
    public static boolean isValidPosition(WebForm webForm, DomPosition pos, boolean adjust, InlineEditor inline) {
//        return findValidPosition(pos, adjust, inline) != Position.NONE;
        return findValidPosition(webForm, pos, adjust, inline) != DomPosition.NONE;
    }
    
    // XXX Moved from DesignerUtils.
    /**
     * Given a position in the DOM, find the closest valid position.
     * In particular, the position is not allowed to be inside any
     * "renders children" nodes.  It also doesn't allow positions
     * that are "adjacent" (before, after) an absolutely positioned
     * element.
     *
     * @param pos Position to be checked
     * @param adjust If true, adjust the position to the nearest (above)
     *   position that is valid.
     * @param inline inlineEditor which is in the game in the designer or null.
     * @todo This method is mostly used to determine if a position is a valid
     *   caret position now. Perhaps rename it to that (isValidCaretPosition).
     * @param dom The JSPX document DOM
     */
//    public static Position checkValidPosition(Position pos, boolean adjust, /*WebForm webform*/InlineEditor inline) {
//    public static Position findValidPosition(Position pos, boolean adjust, /*WebForm webform*/InlineEditor inline) {
    public static DomPosition findValidPosition(WebForm webForm, DomPosition pos, boolean adjust, /*WebForm webform*/InlineEditor inline) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".checkPosition(Position, boolean, WebForm)");
//        }
//        if(pos == null || webform == null) {
        if (pos == null) {
            return null;
        }
//        if (pos == Position.NONE) {
        if (pos == DomPosition.NONE) {
            return pos;
        }
        
        Node node = pos.getNode();
        
        if (!adjust) {
//            InlineEditor inline = webform.getManager().getInlineEditor();
            
            if (inline != null) {
                if (inline.checkPosition(pos)) {
                    return pos;
                } else {
//                    return Position.NONE;
                    return DomPosition.NONE;
                }
            }
        }
        
        // Don't accept positions adjacent to an absolutely or relatively positioned container
        if (!adjust) {
//            RaveElement target = pos.getTargetElement();
//            if ((target != null) && target.isRendered()) {
            Element target = pos.getTargetElement();
//            if (MarkupService.isRenderedNode(target)) {
            if (webForm.isRenderedNode(target)) {
//                Value val = CssLookup.getValue(target, XhtmlCss.POSITION_INDEX);
                CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(target, XhtmlCss.POSITION_INDEX);
                
//                if ((val == CssValueConstants.ABSOLUTE_VALUE) ||
//                        (val == CssValueConstants.RELATIVE_VALUE) ||
//                        (val == CssValueConstants.FIXED_VALUE)) {
                if (CssProvider.getValueService().isAbsoluteValue(cssValue)
                || CssProvider.getValueService().isRelativeValue(cssValue)
                || CssProvider.getValueService().isFixedValue(cssValue)) {
//                    return Position.NONE;
                    return DomPosition.NONE;
                }
            }
        }
        
        while (node != null) {
//            if (node instanceof RaveRenderNode) {
//                RaveRenderNode rn = (RaveRenderNode)node;
//                if (rn.isRendered() && (rn.getSourceNode() == null)) {
//            if (MarkupService.isRenderedNode(node) && MarkupService.getSourceNodeForNode(node) == null) {
            if (webForm.isRenderedNode(node) && MarkupService.getSourceNodeForNode(node) == null) {
                    if (adjust) {
                        Node curr = node;
                        
                        while (curr != null) {
                            if (curr.getNodeType() == Node.ELEMENT_NODE) {
//                                RaveElement e = (RaveElement)curr;
                                Element e = (Element)curr;
                                
//                                if (e.getSource() != null) {
                                if (MarkupService.getSourceElementForElement(e) != null) {
//                                    MarkupDesignBean bean = e.getDesignBean();
//                                    MarkupDesignBean bean = InSyncService.getProvider().getMarkupDesignBeanForElement(e);
//                                    MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(e);
//                                    
//                                    if (bean != null) {
//                                        bean = /*FacesSupport.*/findRendersChildren(bean);
////                                        e = (RaveElement)bean.getElement();
//                                        e = bean.getElement();
//                                    }
                                    Element se = WebForm.getDomProviderService().getSourceElementWhichRendersChildren(e);
                                    if (se != null) {
                                        e = se;
                                    }
                                    
//                                    return Position.create(e, pos.getOffset() > 0);
                                    return webForm.createDomPosition(e, pos.getOffset() > 0);
                                }
                            }
                            
                            curr = curr.getParentNode();
                            
                            if (curr == null) {
//                                return Position.NONE;
                                return DomPosition.NONE;
                            }
                        }
                    } else {
//                        return Position.NONE;
                        return DomPosition.NONE;
                    }
//                }
            }
            
//            if (node instanceof RaveElement) {
//                RaveElement element = (RaveElement)node;
            if (node instanceof Element) {
                Element element = (Element)node;
                
//                if (element.getDesignBean() != null) {
//                    MarkupDesignBean bean = element.getDesignBean();
//                MarkupDesignBean bean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//                MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//                
//                if (bean != null) {
//                    MarkupDesignBean parent = /*FacesSupport.*/findRendersChildren(bean);
//                    
//                    // XXX what if bean itself is a renders children?
//                    if (parent != bean) {
                Element se = WebForm.getDomProviderService().getSourceElementWhichRendersChildren(element);
                if (se != null) {
//                    if (se != element) {
                    // XXX #112580 Needs to compare rendered with rendered or source with source element.
                    if (se != MarkupService.getSourceElementForElement(element)) {
                        if (adjust) {
                            // There was a renders-children parent we
                            // should skip
//                            Element parentElement = parent.getElement();
                            Element parentElement = se;
                            
//                            return Position.create(parentElement, pos.getOffset() > 0);
                            return webForm.createDomPosition(parentElement, pos.getOffset() > 0);
                        } else {
//                            return Position.NONE;
                            return DomPosition.NONE;
                        }
                    }
                    
                    break;
                }
            }
            
            node = node.getParentNode();
        }
        
//        InlineEditor inline = webform.getManager().getInlineEditor();
        
//        if (((pos != Position.NONE) && ((inline != null) && inline.checkPosition(pos))) ||
        if (((pos != DomPosition.NONE) && ((inline != null) && inline.checkPosition(pos))) ||
//                !pos.isRendered()) {
//        !MarkupService.isRenderedNode(pos.getNode())) {
        pos.isSourcePosition()) {
            return pos;
        } else if (adjust) {
            // Try to find the corresponding source
            node = pos.getNode();
            
            while (node != null) {
//                if (node instanceof RaveElement) {
//                    RaveElement element = (RaveElement)node;
                if (node instanceof Element) {
                    Element element = (Element)node;
                    
//                    if (element.getDesignBean() != null) {
//                        DesignBean bean = element.getDesignBean();
//                    DesignBean bean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//                    DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//                    if (bean != null) {
                    Element componentRootElement = WebForm.getDomProviderService().getComponentRootElementForElement(element);
                    if (componentRootElement != null) {
//                        Element el = FacesSupport.getElement(bean);
//                        Element el = Util.getElement(bean);
//                        Element el = WebForm.getDomProviderService().getElement(bean);
                        Element sourceElement = MarkupService.getSourceElementForElement(componentRootElement);
//                        return Position.create(sourceElement, pos.getOffset() > 0);
                        return webForm.createDomPosition(sourceElement, pos.getOffset() > 0);
                    }
                }
                
                node = node.getParentNode();
            }
            
//            return Position.NONE;
            return DomPosition.NONE;
        } else {
            //            // XXX shouldn't this be return pos; ? Try to click somewhere in BoxModelTest
            //            // layout-floats3.html
            //            return Position.NONE;
            //        }
            return pos;
        }
    }

//    // XXX Moved from FacesSupport.
//    /** Find outermost renders-children bean above the given bean, or
//     * the bean itself if there is no such parent.
//     */
//    private /*public*/ static MarkupDesignBean findRendersChildren(MarkupDesignBean bean) {
//        // Similar to FacesSupport.findHtmlContainer(bean), but
//        // we need to return the outermost html container itself, not
//        // the parent, since we're not looking for its container but
//        // the bean to be moved itself.
//        MarkupDesignBean curr = bean;
//
////        for (; curr != null; curr = FacesSupport.getBeanParent(curr)) {
//        for (; curr != null; curr = getBeanParent(curr)) {
//            if (curr.getInstance() instanceof F_Verbatim) {
//                // If you have a verbatim, we're okay to add html comps below it
//                return bean;
//            }
//
//            if (curr.getInstance() instanceof UIComponent) {
//                // Need to set the Thread's context classloader to be the Project's ClassLoader.
//            	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
//            	try {
////                    Thread.currentThread().setContextClassLoader(InSyncService.getProvider().getContextClassLoader(curr));
//                    Thread.currentThread().setContextClassLoader(WebForm.getDomProviderService().getContextClassLoaderForDesignContext(curr.getDesignContext()));
//                    if (((UIComponent)curr.getInstance()).getRendersChildren()) {
//                    	bean = curr;
//                        // Can't break here - there could be an outer
//                        // renders-children parent
//                    }               
//            	} finally {
//                    Thread.currentThread().setContextClassLoader(oldContextClassLoader);
//            	}                
//            }
//        }
//
//        return bean;
//    }
//    
//    // XXX Moved from FacesSupport.
//    /**
//     * Return the parent of the given markup design bean, if the parent is
//     * a MarkupDesignBean.
//     */
//    private static MarkupDesignBean getBeanParent(MarkupDesignBean bean) {
//        DesignBean parent = bean.getBeanParent();
//
//        if (parent instanceof MarkupDesignBean) {
//            return (MarkupDesignBean)parent;
//        }
//
//        return null;
//    }


    /** Try to find the box corresponding to the given element. */
    public static CssBox findBox(PageBox pageBox, Element element) {
//        CssBox box = CssBox.getBox(element);
        CssBox box = pageBox.getWebForm().findCssBoxForElement(element);

        if (box != null) {
            return box;
        }

//        Position pos = Position.create(element, false);
        DomPosition pos = pageBox.getWebForm().createDomPosition(element, false);

//        if (!pos.isRendered()) {
//        if (!MarkupService.isRenderedNode(pos.getNode())) {
        if (pos.isSourcePosition()) {
            pos = pos.getRenderedPosition();
        }

//        if (pos == Position.NONE) {
        if (pos == DomPosition.NONE) {
            return null;
        }

//        box = findBox(webform.getPane().getPageBox(), pos);
        box = findBox(pageBox, pos);

        if (box != null) {
            return box;
        }

        return null;
    }

//    /** Locate a component in the visible view given the x,y coordinates
//     * XXX Get rid of it, replace with #findElement. */
//    public static MarkupDesignBean findMarkupDesignBean(CssBox box) {
//        for (; box != null; box = box.getParent()) {
//            MarkupDesignBean boxMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(box);
////            if (box.getDesignBean() != null) {
////                DesignBean lb = box.getDesignBean();
//            if (boxMarkupDesignBean != null) {
//                DesignBean lb = boxMarkupDesignBean;
//
////                if (FacesSupport.isSpecialBean(/*webform, */lb)) {
////                if (Util.isSpecialBean(lb)) {
//                if (lb instanceof MarkupDesignBean && WebForm.getDomProviderService().isSpecialComponent(
//                        WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)lb))) {
//                    continue;
//                }
//
//                return boxMarkupDesignBean;
//            }
//        }
//
//        return null;
//    }

    /** Locates element in the visible view given the box.
     * @return <code>Element</code> or <code>null</code> if there is not such */
    public static Element findElement(CssBox box) {
        for (; box != null; box = box.getParent()) {
            // #107084 Find the element, don't check for the root.
//            Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
            Element element = box.getElement();
            if (element != null) {
                if (WebForm.getDomProviderService().isSpecialComponent(element)) {
                    continue;
                }

                return element;
            }
        }

        return null;
    }

    // XXX #106870 This is different from findElement.
    /** Locates component root element in the visible view given the box.
     * @return <code>Element</code> or <code>null</code> if there is not such */
    public static Element findComponentRootElement(CssBox box) {
        for (; box != null; box = box.getParent()) {
            // #107084 Find the element, don't check for the root.
//            Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
//            Element element = box.getComponentRootElement();
//            if (element != null) {
            // XXX #113773 Fixing selecting of some element whose parent box 
            // is excluded from the hierarchy (suspicous architecture).
            Element element = box.getElement();
            if (WebForm.getDomProviderService().isPrincipalElement(element, null)) {
                if (WebForm.getDomProviderService().isSpecialComponent(element)) {
                    continue;
                }

                return element;
            }
        }

        return null;
    }
    
    
    public static CssBox findBox(PageBox pageBox, int x, int y) {
//        PageBox pageBox = webform.getPane().getPageBox();

        // Locate closest box
        return pageBox.findCssBox(x, y);
    }

//    /** XXX Get rid of it, replace with #findElement. */
//    public static MarkupDesignBean findMarkupDesignBean(PageBox pageBox, int x, int y) {
////        CssBox box = findBox(x, y);
//        CssBox box = findBox(pageBox, x, y);
//
//        return findMarkupDesignBean(box);
//    }
    
    public static Element findElement(PageBox pageBox, int x, int y) {
//        CssBox box = findBox(x, y);
        CssBox box = findBox(pageBox, x, y);

        return findElement(box);
    }

//    public static Rectangle findShape(PageBox pageBox, DesignBean lbean) {
    public static Rectangle findShape(PageBox pageBox, Element componentRootElement) {
//        CssBox box = findBox(pageBox, lbean);
//        if (!(lbean instanceof MarkupDesignBean)) {
//            return null;
//        }
        if (componentRootElement == null) {
            return null;
        }
        CssBox box = findBoxForComponentRootElement(pageBox, componentRootElement);

        if (box == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                new NullPointerException("Null box for bean=" + lbean)); // NOI18N
                    new NullPointerException("Null box for element=" + componentRootElement)); // NOI18N

            return null;
        }

        return new Rectangle(box.getAbsoluteX(), box.getAbsoluteY(), box.getWidth(), box.getHeight());
    }

//    // XXX Get rid of this. Replace with #findCssBoxForComponentRootElement.
//    public static CssBox findBox(PageBox pageBox, DesignBean lbean) {
////        PageBox pageBox = webform.getPane().getPageBox();
////        return pageBox.find(lbean);
//        return pageBox.findCssBox(lbean);
//    }

    public static CssBox findBoxForComponentRootElement(PageBox pageBox, Element componentRootElement) {
//        PageBox pageBox = webform.getPane().getPageBox();
//        return pageBox.find(lbean);
        return pageBox.findCssBoxForComponentRootElement(componentRootElement);
    }
    
    public static List<Rectangle> getComponentRectangles(PageBox pageBox, /*DesignBean lb*/Element componentRootElement) {
        List<Rectangle> result = new ArrayList<Rectangle>();
//        PageBox pageBox = webform.getPane().getPageBox();
//        pageBox.computeRectangles(lb, result);
        pageBox.computeRectangles(componentRootElement, result);

        if (result.size() == 0) {
            // Didn't find any bounds for the component. That can happen
            // with some components, like TableRowGroups, which
            // correspond to elements that don't have direct
            // representatives as boxes, such as <tr> in tables
            // If so, just try to compute its bounds and return
            // it here
//            Rectangle r = getComponentBounds(lb);
//            Rectangle r = getComponentBounds(pageBox, lb);
            Rectangle r = getComponentBounds(pageBox, componentRootElement);

            if (r != null) {
                result.add(r);
            }
        }

        return result;
    }

    public static Rectangle getComponentBounds(PageBox pageBox, /*DesignBean lb*/Element componentRootElement) {
//        PageBox pageBox = webform.getPane().getPageBox();
//        if (!(lb instanceof MarkupDesignBean)) {
//            return null;
//        }
//        Element componentRootElement = SelectionManager.getComponentRootElementForMarkupDesignBean((MarkupDesignBean)lb);
        if (componentRootElement == null) {
            return null;
        }

//        Rectangle bounds = pageBox.computeBounds(lb, null);
        Rectangle bounds = pageBox.computeBounds(componentRootElement, null);

        // Some components render to a top level element which isn't represented
        // directly in the box hierarchy, such as <tr> for example; TableBoxes
        // don't have row boxes, they manage cells directly.
        // In this case, look for the bounds of all the children of the component
        // and assume that the parent is basically made up of the union of its children
        if (bounds == null) {
//            for (int i = 0, n = lb.getChildBeanCount(); i < n; i++) {
//                DesignBean child = lb.getChildBean(i);
//                bounds = pageBox.computeBounds(child, bounds);
//            }
            Element[] childComponentRootElements = WebForm.getDomProviderService().getChildComponentRootElements(componentRootElement);
            for (Element child : childComponentRootElements) {
                bounds = pageBox.computeBounds(child, bounds);
            }
        }

        return bounds;
    }

//    public Rectangle getRegionBounds(MarkupMouseRegion region) {
//        return webform.getPane().getPageBox().computeRegionBounds(region, null);
//    }
}

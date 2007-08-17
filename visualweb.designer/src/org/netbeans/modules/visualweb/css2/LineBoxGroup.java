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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.netbeans.modules.visualweb.designer.DesignerPane;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;

import org.openide.ErrorManager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * This class represents a LineBoxGroup; a box representing
 * a single Inline context, which will be rendered as a set
 * of LineBoxes managed by this LineBoxGroup.
 * This class also maintains floating boxes, since these are
 * positioned along with line boxes (and in particular, if a float
 * appears in the middle of a stack of line boxes, we need to know
 * exactly where to put it - and only the LineBoxGroup can handle this.)
 * @todo Rename to LineBoxGroup
 *
 * @author Tor Norbye
 */
public class LineBoxGroup extends ContainerBox {
    //private int maxWidth;
    //private int nextX;
    private final FontMetrics metrics;
    private LineBox lineBox = null;
    private int targetY;
//    private ArrayList floats = null;
    List<CssBox> floats = null;
    private BoxList allBoxes;

    public LineBoxGroup(WebForm webform, Element element, FontMetrics metrics) {
        // A LineBox isn't exactly an inline box, but it's NOT a block
        // level box. Perhaps I should reverse the logic and call
        // the constructor parameter "blocklevel" instead.
        super(webform, element, BoxType.LINEBOX, true, false);

        // XXX why am I passing element to super? I shouldn't do that
        this.metrics = metrics;

        effectiveTopMargin = 0; // Is this still necessary?
        effectiveBottomMargin = 0;
    }

    /** LineBoxes do not have margins, borders, grids, etc. like regular
     * boxes.
     */
    protected void initialize() {
        effectiveTopMargin = 0;
        effectiveBottomMargin = 0;
    }

    /** LineBoxes do not have margins, borders, grids, etc. like regular
     * boxes.
     */
    protected void initializeInvariants() {
    }

    // Gotta override this one too since it gets called separately from initialize()
    protected void initializeBackground() {
    }

    public FontMetrics getMetrics() {
        return metrics;
    }

    /**
     *  Report whether the line box is empty (contains no inline boxes).
     *
     *  @return true iff there are no inline boxes in this linebox.
     */
    public boolean isEmpty() {
        return (allBoxes == null) || (allBoxes.size() == 0);
    }

    /**
     * The actual "box list" (as seen by CssBox.getBoxCount())
     * is not updated until we perform layout, since we'll need to
     * create multiple intermediate line boxes to fit the contents
     * depending on the actual layout width and float box positioning.
     * TODO - consider changing this box into something else,
     * e.g. LineBoxGroup
     */
    protected void addBox(CssBox box, CssBox prevBox, CssBox nextBox) {
        assert box.isInlineBox() || (box.getBoxType() == BoxType.FLOAT);

        if (allBoxes == null) {
            allBoxes = new BoxList(10); // XXX good default? Do statistics.
        } else if ((box.getBoxType() == BoxType.SPACE) && (allBoxes != null) &&
                (allBoxes.get(allBoxes.size() - 1).getBoxType() == BoxType.SPACE)) {
            // Suppress repeated spaces. I really should make this into
            // an assertion and make it easier for clients of this method
            // to determine if we've already put a space box in so they
            // can avoid creating the box itself.
            return;
        }

        allBoxes.add(box, prevBox, nextBox);
        
        // XXX #98826 Very suspicoius code leading to the issue.
        // Wrong parentage, while the boxes in the 'allBoxes' BoxList are not the real chilren,
        // the real chilidren are kept in the 'boxes' BoxList.
        // However to comment out the nexst line doesn't work either, a messy impl.
        box.setParent(this);
        box.setPositionedBy(this);
    }

    /**
     * Remove a box from the "master" box list, not the actual
     * children list (which contains line boxes)
     */
    protected boolean removeBox(CssBox box) {
        if (allBoxes == null) {
            ErrorManager.getDefault().log("Unexpected removeBox " + box + ": lbg already empty");

            return false;
        } else {
            boolean ret = allBoxes.remove(box);
            
            // XXX #109306 Remove also sibling boxes representing the same element.
            // XXX Why they don't have common parent?
            Element element = box.getElement();
            if (element != null) {
                int size = allBoxes.size();
                List<CssBox> toRemove = new ArrayList<CssBox>();
                for (int i = 0; i < size; i++) {
                    CssBox siblingBox = allBoxes.get(i);
                    if (siblingBox == null) {
                        continue;
                    }
                    if (element == siblingBox.getElement()) {
                        toRemove.add(siblingBox);
                    }
                }
                for (CssBox siblingBoxToRemove : toRemove) {
                    allBoxes.remove(siblingBoxToRemove);
                }
            }

            if (allBoxes.size() == 0) {
                ContainerBox parent = getParent();
                // Linebox now empty - remove it
                if (parent != null) {
                    parent.removeBox(this);
                }
            }

            return ret;
        }
    }

    protected CssBox notifyChildResize(CssBox child, FormatContext context) {
        int oldHeight = contentHeight;
        relayout(context);

        ContainerBox parent = getParent();
        if (contentHeight != oldHeight) {
            return parent.notifyChildResize(this, context);
        } else {
            // The linebox may be new; ensure that it's positioned
            parent.positionBox(this, context);
        }

        return this;
    }

    /**
     * Lays out ALL the children; should ONLY be called as part of updateLayout.
     * @todo Find a way to enforce that.
     */
    protected void layoutChild(CssBox box, FormatContext context, boolean handleChildren) {
        // The notifyChildResize call will do a relayout anyway

        /*
        if (handleChildren) {
        relayout(context);
        }
         */
    }

    /**
     * NOTE: prevBox and nextBox refer to the siblings for
     * the new LineBoxGroup to be created (if any), not where
     * within the LineBoxGroup to add the inline box!
     */
    private void addToLineBox(FormatContext context, CssBox ibox) {
        if (lineBox == null) {
            int maxWidth = context.getMaxWidth(this, targetY);
            int indent = 0;

            Element element = getElement();
            if ((super.getBoxCount() == 0) && (element != null)) {
                // First line box should look at the text-indent property
//                indent = CssLookup.getLength(element, XhtmlCss.TEXT_INDENT_INDEX);
                indent = CssUtilities.getCssLength(element, XhtmlCss.TEXT_INDENT_INDEX);

                if (indent == AUTO) {
                    indent = 0;
                }
            }

            lineBox = new LineBox(webform, element, maxWidth, indent);
            lineBox.setParent(this);
            lineBox.setContainingBlock(containingBlockX, containingBlockY, containingBlockWidth,
                containingBlockHeight);
        }

        lineBox.addBox(ibox, null, null);
    }

    /** Perform layout on the inline box contents.
     * This typically means splitting it up into multiple
     * sub-lineboxes.
     */
    public void relayout(FormatContext context) {
        // TODO - look up width by subtracting widths of floats!
        // But that means we need to know the current "y" coordinate
        // we're targeted for.
        if (allBoxes == null) {
            return;
        }

        // TODO - instead of recreating the line box each time,
        // simply diff and see if flow changes - if it doesn't,
        // we're golden
        removeBoxes();

        targetY = 0;
        lineBox = null; // redundant?

        // Compute final size
        // Since the LineBoxGroup is an anonymous box, it doesn't have margins
        // and can't have AUTO etc. set on it, so we simply sum up the children
        // sizes
        contentWidth = 0;
        contentHeight = 0;

        relayoutChildren(context, allBoxes, getElement());

        finishLine(context);

        //finishing relatives has been moved to ContainerBox
        //now is time to shift relative boxes around
        //note, though, that all the browsers paint relative boxes 
        //on top (in front) of other boxes, as if they had a greater z-index.
        //This is, kind of, against the spec, which says (9.9):
        //"Boxes with the same stack level in a stacking context 
        //are stacked bottom-to-top according to document tree order."
        //if we were implementing the spec the same way as browsers, we would 
        //finish relatives after all inlines in the document.
        //finishRelatives(context);

        width = contentWidth;
        height = contentHeight;
    }

    private void relayoutChildren(FormatContext context, BoxList list, Element element) {
        if (list == null) {
            return;
        }

        int n = list.size();
        boolean wrap = true;

        if (element != null) {
//            Value v = CssLookup.getValue(element, XhtmlCss.WHITE_SPACE_INDEX);
            CssValue cssV = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.WHITE_SPACE_INDEX);
//            wrap = v == CssValueConstants.NORMAL_VALUE;
            wrap = CssProvider.getValueService().isNormalValue(cssV);
        }

        for (int i = 0; i < n; i++) {
            CssBox box = list.get(i);
            //relative boxes are similar to float boxes
            if (box.getBoxType() == BoxType.FLOAT || box.getBoxType() == BoxType.RELATIVE) {
                if (box.isInlineBox() && !box.isReplacedBox() && box instanceof ContainerBox
                && box.getBoxType() != BoxType.FLOAT) { // XXX #99707 Excluding floats from here.
                    LineBox oldLineBox = lineBox;
                    int oldTargetY = targetY;
                    int oldContentWidth = contentWidth;
                    int oldContentHeight = contentHeight;
                    int indent = 0; // XXX Can you have indents here?
                    lineBox = new LineBox(webform, box.getElement(), containingBlockWidth, indent);
                    lineBox.isFloated = box.getBoxType() == BoxType.FLOAT;
                    lineBox.setParent(this);
                    lineBox.setContainingBlock(containingBlockX, containingBlockY,
                        containingBlockWidth, containingBlockHeight);
                    relayoutChildren(context, ((ContainerBox)box).getBoxList(), box.getElement());
                    finishLine(context);
                    lineBox = oldLineBox;
                    targetY = oldTargetY;
                    contentWidth = oldContentWidth;
                    contentHeight = oldContentHeight;
                } else {
                    if(box.getBoxType() == BoxType.RELATIVE) {
                        super.layoutChild(box, context, true);
                        //Ah, no. The float are to be added into the linebox
                        //super.addBox(box, null, null);
                        addToLineBox(context, box);
                        //super.positionBox(box, context);
                    } else {
                        assert box.getBoxType() == BoxType.FLOAT;
                        if (floats == null) {
                            floats = new ArrayList<CssBox>();
                        }

                        super.addBox(box, null, null);
                        floats.add(box);
                    }
                }

                // Floats at the beginning of the line should be processed right away so
                // they are positioned here, not after any other text on this line
                if ((lineBox == null) || lineBox.isEmpty()) {
                    if (floats != null) { // XXX is this right if lineBox.isFloated?
                        finishFloats(context);
                    }
                }

                // Should we do any kind of content width update here?
            } else if (box.getBoxType() == BoxType.SPACE) {
                //if (lineBox != null) { // Ignore space at line beginning
                if (!wrap || (lineBox == null) || lineBox.isEmpty() || 
                        lineBox.canFit(context, box, targetY)) {
                    addToLineBox(context, box);
                } else {
                    finishLine(context);
                    addToLineBox(context, box);
                }

                //}
            } else if (box.getBoxType() == BoxType.LINEBREAK) {
                // Break the line here
                addToLineBox(context, box);
                finishLine(context);

                if (context.floats != null) {
//                    Value clear = CssLookup.getValue(box.getElement(), XhtmlCss.CLEAR_INDEX);
                    CssValue cssClear = CssProvider.getEngineService().getComputedValueForElement(box.getElement(), XhtmlCss.CLEAR_INDEX);

//                    if (clear != CssValueConstants.NONE_VALUE) {
                    if (!CssProvider.getValueService().isNoneValue(cssClear)) {
                        int cleared = context.clear(cssClear, null);

                        if (cleared > Integer.MIN_VALUE) {
                            int clearance = cleared - (getAbsoluteY() + targetY);

                            if (clearance > 0) {
                                targetY += clearance;
                                contentHeight += clearance;
                                height += clearance;
                            }
                        }
                    }
                }
            } else if (!box.isReplacedBox() && box instanceof ContainerBox
            // XXX #6494312 Hack!!! Faking support of inline table, letting it to fall back to else clause.
            // TODO Provide full support for inline tables.
            && !(box instanceof TableBox)) {
                // TODO - if the box has dimensions (even without
                // contents), we still need to reserve space for it!!!
                //need to assign original container to all sub-boxes,
                //otherwise it will be lost.
                BoxList boxList = ((ContainerBox)box).getBoxList();
                for(int j = 0; j < boxList.size(); j++) {
                    boxList.get(j).originalInlineContainer = box;
                }
                relayoutChildren(context, boxList, box.getElement());
            } else {
                boolean formatContent = true;

                if (box.getBoxType() == BoxType.TEXT) {
                    formatContent = false;
                } else if (box.getBoxType() == BoxType.SPACE) {
                    formatContent = false;
                } // XXX what about replaced boxes

                if (formatContent) {
                    // We need to do this later if we support complicated boxes in
                    // inline context --- for now just initialize
                    //box.initialize();
                    //box.relayout(context);
                    super.layoutChild(box, context, true);

                    //No: positionBox(box, context);
                    // XXX #113117 To be sure also the position is set.
                    positionBox(box, context);
                }

                if (!wrap || (lineBox == null) || lineBox.isEmpty() || 
                        lineBox.canFit(context, box, targetY)) {
                    addToLineBox(context, box);
                } else {
                    finishLine(context);
                    addToLineBox(context, box);
                }
            }
        }
    }

    private void finishLine(FormatContext context) {
        if ((lineBox == null) || lineBox.isEmpty()) {
            if (floats != null) { // XXX is this right if lineBox.isFloated?
                finishFloats(context);
            }

            return;
        }

        int lineHeight = lineBox.applyVerticalAlignments();

        if (lineHeight == 0) {
            if (metrics != null) {
                lineHeight = metrics.getHeight();
            } else {
                ErrorManager.getDefault().log("Big hack in computing line height for linebox!");
                lineHeight = 14;
            }
        }

        lineBox.contentWidth = lineBox.getActualWidth();
        lineBox.contentHeight = lineHeight;
        lineBox.width = lineBox.contentWidth;
        lineBox.height = lineBox.contentHeight;

        // Do after setWidth
        if (!lineBox.isFloated) {
            int leftEdge = context.getLeftEdge(this, targetY, lineHeight); // Pass in getParent() instead?
            lineBox.applyHorizontalAlignments(leftEdge, lineHeight, context);
        }

        if (lineBox.contentWidth > contentWidth) {
            contentWidth = lineBox.contentWidth;
        }

        contentHeight += lineBox.contentHeight;

        if (lineBox.getX() == UNINITIALIZED) {
            lineBox.setX(0);
        }

        lineBox.setY(targetY);
        super.addBox(lineBox, null, null);
        targetY += lineHeight;

        if ((lineBox != null) && lineBox.isFloated) {
            if (floats == null) {
                floats = new ArrayList<CssBox>();
            }

            floats.add(lineBox);
            lineBox = null;
        } else if (floats != null) {
            lineBox = null;
            finishFloats(context);
        } else {
            lineBox = null;
        }
    }
    
    void finishRelatives(FormatContext context) {
        //walk though all relative boxes and shift them
        int n = allBoxes.size();
        
        for (int i = 0; i < n; i++) {
            CssBox child = allBoxes.get(i);
            if(child.getBoxType() == BoxType.RELATIVE) {
                //in many cases, height of the container is not known 
                //by the time relitive box gets positioned
                //so, first define a containing block again
                super.setContainingBlock(child, context);
                //then, need to recalculate the properties
                Element childElement = child.getElement();
                CssProvider.getEngineService().uncomputeValueForElement(childElement, XhtmlCss.LEFT_INDEX);
                CssProvider.getEngineService().uncomputeValueForElement(childElement, XhtmlCss.RIGHT_INDEX);
                CssProvider.getEngineService().uncomputeValueForElement(childElement, XhtmlCss.TOP_INDEX);
                CssProvider.getEngineService().uncomputeValueForElement(childElement, XhtmlCss.BOTTOM_INDEX);
                child.left = CssUtilities.getCssLength(childElement, XhtmlCss.LEFT_INDEX);
                child.right = CssUtilities.getCssLength(childElement, XhtmlCss.RIGHT_INDEX);
                child.top = CssUtilities.getCssLength(childElement, XhtmlCss.TOP_INDEX);
                child.bottom = CssUtilities.getCssLength(childElement, XhtmlCss.BOTTOM_INDEX);
                if(child.left == AUTO) {
                    if(child.right == AUTO) {
                        child.left = 0;
                        child.right = 0;
                    } else {
                        child.left = -child.right;
                    }
                } else {
                    if(child.right == AUTO) {
                        child.right = -child.left;
                    } else {
                        //overconstrained
                        child.right = -child.left;
                    }
                }
                if(child.top == AUTO) {
                    if(child.bottom == AUTO) {
                        child.top = 0;
                        child.bottom = 0;
                    } else {
                        child.top = -child.bottom;
                    }
                } else {
                    if(child.bottom == AUTO) {
                        child.bottom = -child.top;
                    } else {
                        //overconstrained
                        child.bottom = -child.top;
                    }
                }
                int newX = child.getX() + child.left;
                int newY = child.getY() + child.top;
                child.setLocation(newX, newY);
            }
        }
    }

    private void finishFloats(FormatContext context) {
        if (floats != null) {
            for (int i = 0, n = floats.size(); i < n; i++) {
                CssBox box = floats.get(i);

                if (box.getBoxType() != BoxType.LINEBOX) {
                    // XXX true here - what if we're doing this layout as part
                    // of a child notify - in that case we shouldn't relayout the children!
                    super.layoutChild(box, context, true);
                    
                    // XXX #113117 To be sure also the position is set.
                    positionBox(box, context); // TEMP
                }
                
                int theY = targetY;
                //if the line box is not the first one, and there's space 
                //for a float in the one above, shift the float up 
                //if(linebox != null)
                if(box.isClearBox()/* && findClearContainer(box) == null*/) {
                    //"clear" float box needs to be positioned below any floating box
                    //on the left/right/both depending on the "clear" property value
                    //so, let's find the floating box positioned previously
                    //with the biggest getAbsoluteY() + getHeight() value.
                    ///is there a container which is "clear" itself?
                    //9.5.2: It may be that the element itself has floating descendants; 
                    //the 'clear' property has no effect on those
                    //now let's adjust the targetY value 
                    CssBox prev = context.getPrevFloatingForClear(box);
                    if(prev != null) {
                        //this box is "clear" and there are floats above it.
                        int newY = context.adjustY(prev.getHeight() + 
                                prev.bottomMargin + prev.topMargin, 
                                prev, this) + box.topMargin;
                        theY = Math.max(targetY, newY);
                    }
                } else {
                    //9.5.1. #5
                    //The outer top of a floating box may not be higher than the outer top 
                    //of any block or floated box generated by an element earlier in the source document.
                    CssBox prev = context.getPrevFloatingForFloat(box);
                    if(prev != null) {
                        int newY = context.adjustY(0, prev, this);
                        theY = Math.max(targetY, newY);
                    }
                }
                
                //now lets check if the floats fits here
                //9.5.1 #7
                //A left-floating box that has another left-floating
                //box to its left may not have its right outer edge to the right
                //of its containing block's right edge. (Loosely: a left float
                //may not stick out at the right edge, unless it is already
                //as far to the left as possible.) An analogous rule holds
                //for right-floating elements.
                if(context.getMaxWidth(this, theY, box.getHeight()) < box.getWidth()) {
                    //move it to the new line, then
                    CssBox prev = context.getLowestFloatingForFloat(box);
                    if(prev != null) {
                        int newY = context.adjustY(prev.getHeight(), prev, this);
                        theY = Math.max(targetY, newY);
                    }
                }

                positionFloatBox(theY, box, context);
//                positionFloatBox(targetY, box, context);
            }
        }

        floats = null;
    }
    
//    static CssBox findClearContainer(CssBox box) {
//        CssBox parent = box;
//        while((parent = parent.getParent()) != null) {
//            CssValue cssClear = CssProvider.getEngineService().
//                    getComputedValueForElement(parent.getElement(), XhtmlCss.CLEAR_INDEX);
//            
//            if(CssProvider.getValueService().isBothValue(cssClear) ||
//                    CssProvider.getValueService().isLeftValue(cssClear) ||
//                    CssProvider.getValueService().isRightValue(cssClear))
//                return(parent);
//        }
//        return(null);
//    }

    // XXX why isn't this reusing the ContainerBox positionFloatBox code?
    private void positionFloatBox(int py, CssBox box, FormatContext context) {
        box.effectiveTopMargin = 0; // XXX ?
        box.effectiveBottomMargin = 0;

        CssBox parentBox = box.getParent();
        assert parentBox != null;
        assert parentBox == this;

//        Value floating = CssLookup.getValue(box.getElement(), XhtmlCss.FLOAT_INDEX);
        CssValue cssFloating = CssProvider.getEngineService().getComputedValueForElement(box.getElement(), XhtmlCss.FLOAT_INDEX);
        boolean leftSide;

//        if (floating == CssValueConstants.LEFT_VALUE) {
        if (CssProvider.getValueService().isLeftValue(cssFloating)) {
            leftSide = true;
//        } else {
//            assert floating == CssValueConstants.RIGHT_VALUE;
        } else if (CssProvider.getValueService().isRightValue(cssFloating)) {

            // None not permitted since we wouldn't have identified a
            // float boxtype in the first place in BoxType.getBoxType
            leftSide = false;
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Unexpected floating value, cssFloating=" + cssFloating));
            return;
        }

        // Add/position floating box: its margins do NOT collapse!!
        box.effectiveTopMargin = box.topMargin;
        box.effectiveBottomMargin = box.bottomMargin;

        // Locate the floating box at the next linebox position
        int px;

        if (leftSide) {
            px = context.getLeftEdge(this, py, box.getHeight());
        } else {
            px = context.getRightEdge(this, py, box.getHeight()) - box.getWidth();
        }

        box.setLocation(px, py);

        // Further layout operations need to know about this float box
        // so lineboxes can be shortened
        context.addFloat(px, py, box, leftSide);

        //context.floating = oldFloating;
    }

    /**
     * {@inheritDoc}
     *
     * @todo Update this to do logic parallel to {@link relayoutChildren}, e.g.
     *   when child is a non-replaced ContainerBox process its BoxList using the
     *   below logic rather than just call its getPrefMinWidth method which does
     *   not have LineBoxGroup semantics. Similarly, handle floating boxes
     *   specially.
     */
    public int getPrefMinWidth() {
        if (allBoxes == null) {
            return 0;
        }

        boolean wrap = true;

        Element element = getElement();
        if (element != null) {
//            Value v = CssLookup.getValue(element, XhtmlCss.WHITE_SPACE_INDEX);
            CssValue cssV = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.WHITE_SPACE_INDEX);
//            wrap = v == CssValueConstants.NORMAL_VALUE;
            wrap = CssProvider.getValueService().isNormalValue(cssV);
        }

        if (wrap) {
            int largest = 0;
            int n = allBoxes.size();

            for (int i = 0; i < n; i++) {
                CssBox child = allBoxes.get(i);
                int min;

                if (child.getBoxType() == BoxType.LINEBREAK) {
                    min = 0;
                } else {
                    min = child.getPrefMinWidth();
                }

                if (min > largest) {
                    largest = min;
                }
            }

            return largest;
        } else {
            BoxList list = allBoxes;
            int max = 0;
            int line = 0;
            int n = list.size();

            for (int i = 0; i < n; i++) {
                CssBox child = list.get(i);

                if (child.getBoxType() == BoxType.LINEBREAK) {
                    // Break line - start from scratch
                    line = 0;
                } else {
                    line += child.getPrefMinWidth();
                }

                if (line > max) {
                    max = line;
                }
            }

            return max;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @todo Update this to do logic parallel to {@link relayoutChildren}, e.g.
     *   when child is a non-replaced ContainerBox process its BoxList using the
     *   below logic rather than just call its getPrefWidth method which does
     *   not have LineBoxGroup semantics. Similarly, handle floating boxes
     *   specially.
     */
    public int getPrefWidth() {
        return getPrefWidth(allBoxes);
    }

    protected int getPrefWidth(BoxList list) {
        if (list == null) {
            return 0;
        }

        //for the sake of those floats, that belong to this line box group
        //(not to a line box), and have width in %
        //contentWidth = getParent().contentWidth;

        int max = 0;
        int line = 0;
        int n = list.size();

        for (int i = 0; i < n; i++) {
            CssBox child = list.get(i);

            if (child.getBoxType() == BoxType.LINEBREAK) {
                // Break line - start from scratch
                line = 0;
            } else {
                line += child.getPrefWidth();
            }

            if (line > max) {
                max = line;
            }
        }

        return max;
    }

    protected void initializeHorizontalWidths(FormatContext context) {
        if (allBoxes == null) {
            return;
        }

        for (int i = 0, n = allBoxes.size(); i < n; i++) {
            CssBox box = allBoxes.get(i);

            // We don't care about absolute/fixed children!
            if (box.getBoxType().isAbsolutelyPositioned()) {
                // Can this happen in a linebox?
                continue;
            }

            box.initializeHorizontalWidths(context);
        }
    }

    void computeHorizontalLengths(FormatContext context) {
        // LineBoxes are anonymous, so they should have no "auto" settings
        // on them
    }

    void computeVerticalLengths(FormatContext context) {
        //actually, it's not the lineboxgroup that needs to include the floats - 
        //it's linebox's container. The code moved into getSizeWithFloats(FormatContext)
        /*
        // if the box (the contained of LineBoxGroup) is a floating box itself
        //its size should include nested floats. All the browsers do so.
        CssValue cssFloating = CssProvider.getEngineService().getComputedValueForElement(this.getElement(), XhtmlCss.FLOAT_INDEX);
        if (CssProvider.getValueService().isLeftValue(cssFloating) ||
                CssProvider.getValueService().isRightValue(cssFloating)) {
            //here we need to look for all boxes whos "float" property is set
            boolean foundAFloatBox = false;
            
            int top = Integer.MAX_VALUE;
            int bottom = Integer.MIN_VALUE;
            int n = getBoxCount();
            
            for (int i = 0; i < n; i++) {
                CssBox child = getBox(i);
                
                cssFloating = CssProvider.getEngineService().getComputedValueForElement(child.getElement(), XhtmlCss.FLOAT_INDEX);
                
                if (CssProvider.getValueService().isLeftValue(cssFloating) ||
                        CssProvider.getValueService().isRightValue(cssFloating)) {
                    
                    if (child.getY() < top) {
                        top = child.getY();
                    }
                    
                    if ((child.getY() + child.getHeight()) > bottom) {
                        bottom = child.getY() + child.getHeight();
                    }
                    
                    foundAFloatBox = true;
                }
            }
            if(foundAFloatBox) {
                if (top != Integer.MAX_VALUE) {
                    contentHeight = bottom - top;
                }
            }
        }
         */
    }
    
    int getSizeWithFloats() {
        // if the box (the contained of LineBoxGroup) is a floating box itself
        //its size should include nested floats. All the browsers do so.
        CssValue cssFloating = CssProvider.getEngineService().getComputedValueForElement(this.getElement(), XhtmlCss.FLOAT_INDEX);
        if (CssProvider.getValueService().isLeftValue(cssFloating) ||
                CssProvider.getValueService().isRightValue(cssFloating)) {
            //here we need to look for all boxes whos "float" property is set
            boolean foundAFloatBox = false;
            
            int top = Integer.MAX_VALUE;
            int bottom = Integer.MIN_VALUE;
            int n = getBoxCount();
            
            for (int i = 0; i < n; i++) {
                CssBox child = getBox(i);
                
                cssFloating = CssProvider.getEngineService().getComputedValueForElement(child.getElement(), XhtmlCss.FLOAT_INDEX);
                
                if (CssProvider.getValueService().isLeftValue(cssFloating) ||
                        CssProvider.getValueService().isRightValue(cssFloating)) {
                    if (child.getY() < top) {
                        top = child.getY();
                    }
                    if ((child.getY() + child.getHeight()) > bottom) {
                        bottom = child.getY() + child.getHeight();
                    }
                    foundAFloatBox = true;
                }
            }
            if(foundAFloatBox) {
                if (bottom != Integer.MAX_VALUE) {
                    return bottom - top;
                }
            }
        }
        return 0;
    }

    /** Split this linebox, creating a new linebox containing all
     * the elements following the child box "lastBox"; the new
     * line box will containg the remaining elements, and this
     * line box will be truncated to only contain the elements
     * up to lastBox.
     */
    LineBoxGroup split(org.netbeans.modules.visualweb.css2.CssBox lastBox) {
        // Find the box to be inserted
        assert allBoxes != null;

        int pos = 0;
        int n = allBoxes.size();

        for (; pos < n; pos++) {
            if (allBoxes.get(pos) == lastBox) {
                break;
            }
        }

        assert pos < n;
        pos++;

        LineBoxGroup split = new LineBoxGroup(webform, getElement(), metrics);
        int remainder = n - pos;
        split.allBoxes = new BoxList(remainder + 4); // XXX good default? Do statistics.

        for (int i = pos; i < n; i++) {
            split.allBoxes.add(allBoxes.get(i), null, null);
        }

        // Fix parent pointers
        for (int i = 0; i < remainder; i++) {
            split.allBoxes.get(i).setParent(split);
            split.allBoxes.get(i).setPositionedBy(split);
        }

        allBoxes.truncate(pos);

        return split;
    }

//    // XXX TODO JSF specific, replace with the latter method.
//    public void computeRectangles(DesignBean component, List list) {
//        // Look through my line boxes, and for each item, walk back up the parent chain
//        // looking for the given live bean. When found, the leaf is added to the bounds.
//        Rectangle bounds = null;
//
//        for (int i = 0, n = getBoxCount(); i < n; i++) {
//            // LineBoxGroups contain mostly LineBoxes, but can have floats too
//            CssBox box = getBox(i);
//
//            if (box instanceof LineBox) {
//                LineBox lb = (LineBox)box;
//
//                for (int j = 0, m = lb.getBoxCount(); j < m; j++) {
//                    CssBox leaf = lb.getBox(j);
//
//                    // Is it conceivable that a single line context will have
//                    // multiple separate segments for different live beans? If so
//                    // I guess I should only join rectangles for -contiguous-
//                    // sections of boxes
//                    if (hasComponentAncestor(leaf, component)) {
//                        // Yessss
//                        Rectangle r =
//                            new Rectangle(leaf.getAbsoluteX(), leaf.getAbsoluteY(),
//                                leaf.getWidth(), leaf.getHeight());
//
//                        if (bounds == null) {
//                            // allocations get mutated by later
//                            // transformations so I've gotta make a copy
//                            bounds = r;
//                        } else {
//                            bounds.add(r);
//                        }
//
//                        // We don't break here - multiple items in the line box group
//                        // may be descendants and we want to include all in the bounds
//                        // computation
//                    }
//                }
//            } else {
//                assert box.getBoxType() == BoxType.FLOAT || box.getBoxType() == BoxType.RELATIVE;
//                box.computeRectangles(component, list);
//            }
//        }
//
//        if (bounds != null) {
//            list.add(bounds);
//        }
//    }
    
    // XXX TODO This will replace the above.
    public void computeRectangles(Element componentRootElement, List<Rectangle> list) {
        // Look through my line boxes, and for each item, walk back up the parent chain
        // looking for the given live bean. When found, the leaf is added to the bounds.
        Rectangle bounds = null;

        for (int i = 0, n = getBoxCount(); i < n; i++) {
            // LineBoxGroups contain mostly LineBoxes, but can have floats too
            CssBox box = getBox(i);

            if (box instanceof LineBox) {
                LineBox lb = (LineBox)box;

                for (int j = 0, m = lb.getBoxCount(); j < m; j++) {
                    CssBox leaf = lb.getBox(j);

                    // Is it conceivable that a single line context will have
                    // multiple separate segments for different live beans? If so
                    // I guess I should only join rectangles for -contiguous-
                    // sections of boxes
                    if (hasComponentAncestor(leaf, componentRootElement)) {
                        // Yessss
                        Rectangle r =
                            new Rectangle(leaf.getAbsoluteX(), leaf.getAbsoluteY(),
                                leaf.getWidth(), leaf.getHeight());

                        if (bounds == null) {
                            // allocations get mutated by later
                            // transformations so I've gotta make a copy
                            bounds = r;
                        } else {
                            bounds.add(r);
                        }

                        // We don't break here - multiple items in the line box group
                        // may be descendants and we want to include all in the bounds
                        // computation
                    }
                }
            } else {
                assert box.getBoxType() == BoxType.FLOAT || box.getBoxType() == BoxType.RELATIVE;
                box.computeRectangles(componentRootElement, list);
            }
        }

        if (bounds != null) {
            list.add(bounds);
        }
    }

//    // XXX TODO JSF specific, replace with the latter method.
//    public Rectangle computeBounds(DesignBean component, Rectangle bounds) {
//        // Look through my line boxes, and for each item, walk back up the parent chain
//        // looking for the given live bean. When found, the leaf is added to the bounds.
//        for (int i = 0, n = getBoxCount(); i < n; i++) {
//            // LineBoxGroups contain mostly LineBoxes, but can have floats too
//            CssBox box = getBox(i);
//
//            if (box instanceof LineBox) {
//                LineBox lb = (LineBox)box;
//
//                for (int j = 0, m = lb.getBoxCount(); j < m; j++) {
//                    CssBox leaf = lb.getBox(j);
//
//                    if (hasComponentAncestor(leaf, component)) {
//                        // Yessss
//                        Rectangle r =
//                            new Rectangle(leaf.getAbsoluteX(), leaf.getAbsoluteY(),
//                                leaf.getWidth(), leaf.getHeight());
//
//                        if (bounds == null) {
//                            // allocations get mutated by later
//                            // transformations so I've gotta make a copy
//                            bounds = r;
//                        } else {
//                            bounds.add(r);
//                        }
//
//                        // We don't break here - multiple items in the line box group
//                        // may be descendants and we want to include all in the bounds
//                        // computation
//                    }
//                }
//            } else {
//                assert box.getBoxType() == BoxType.FLOAT || box.getBoxType() == BoxType.RELATIVE;
//                bounds = box.computeBounds(component, bounds);
//            }
//        }
//
//        return bounds;
//    }
    
    // XXX TODO This will replace the above.
    public Rectangle computeBounds(Element componentRootElement, Rectangle bounds) {
        // Look through my line boxes, and for each item, walk back up the parent chain
        // looking for the given live bean. When found, the leaf is added to the bounds.
        for (int i = 0, n = getBoxCount(); i < n; i++) {
            // LineBoxGroups contain mostly LineBoxes, but can have floats too
            CssBox box = getBox(i);

            if (box instanceof LineBox) {
                LineBox lb = (LineBox)box;

                for (int j = 0, m = lb.getBoxCount(); j < m; j++) {
                    CssBox leaf = lb.getBox(j);

                    if (hasComponentAncestor(leaf, componentRootElement)) {
                        // Yessss
                        Rectangle r =
                            new Rectangle(leaf.getAbsoluteX(), leaf.getAbsoluteY(),
                                leaf.getWidth(), leaf.getHeight());

                        if (bounds == null) {
                            // allocations get mutated by later
                            // transformations so I've gotta make a copy
                            bounds = r;
                        } else {
                            bounds.add(r);
                        }

                        // We don't break here - multiple items in the line box group
                        // may be descendants and we want to include all in the bounds
                        // computation
                    }
                }
            } else {
                assert box.getBoxType() == BoxType.FLOAT || box.getBoxType() == BoxType.RELATIVE;
                bounds = box.computeBounds(componentRootElement, bounds);
            }
        }

        return bounds;
    }

//    // TODO JSF specific, this needs to be replaced by the latter method.
//    /** Return true iff the given leaf has a box as an ancestor (but below
//      * this LineBoxGroup) that corresponds to the given live bean */
//    private boolean hasComponentAncestor(CssBox leaf, DesignBean component) {
//        while ((leaf != null) && (leaf != this)) {
////            if (leaf.getDesignBean() == component) {
//            if (getMarkupDesignBeanForCssBox(leaf) == component) {
//                return true;
//            }
//
//            leaf = leaf.getParent();
//        }
//
//        return false;
//    }
    
    // TODO This will replace the above method.
    /** Return true iff the given leaf has a box as an ancestor (but below
      * this LineBoxGroup) that corresponds to the given live bean */
    private boolean hasComponentAncestor(CssBox leaf, Element componentRootElement) {
        while ((leaf != null) && (leaf != this)) {
//            if (leaf.getDesignBean() == component) {
//            if (getElementForComponentRootCssBox(leaf) == componentRootElement) {
            // XXX #107084 There needs to be a way how to find a component for
            // the line box type of boxes, which otherwise don't have a component root element.
            // Before it was working only thanks to 'broken' hierarchy, the parent was different (ContainerBox)
            // than the actual box (LineBoxGroup) having one (TextBox) as its child.
            if (leaf.getElement() == componentRootElement) {
                return true;
            }

            leaf = leaf.getParent();
        }

        return false;
    }

    /** FOR TESTSUITE ONLY! */
    public BoxList getManagedBoxes() {
        return allBoxes;
    }

    protected void paintBackground(Graphics g, int x, int y) {
        //LayeredHighlighter h = doc.getWebForm().getPane().getHighlighter();
        //h.paintLayeredHighlights(g, p0, p1, a, tc, this);
        DesignerPane pane = webform.getPane();

        if (pane == null) {
            return; // TESTSUITE
        }

//        DesignerCaret caret = pane.getCaret();
//        if ((caret != null) && caret.hasSelection()) {
        if (pane.hasCaretSelection()) {
            // Determine if the range intersects our line box group
//            Position sourceCaretBegin = caret.getFirstPosition();
//            DomPosition sourceCaretBegin = caret.getFirstPosition();
            DomPosition sourceCaretBegin = pane.getFirstPosition();

            // XXX I ought to have a cached method on the caret for obtaining the rendered
            // location!
//            Position caretBegin = sourceCaretBegin.getRenderedPosition();
//            Position sourceCaretEnd = caret.getLastPosition();
//            Position caretEnd = sourceCaretEnd.getRenderedPosition();
            DomPosition caretBegin = sourceCaretBegin.getRenderedPosition();
            
//            DomPosition sourceCaretEnd = caret.getLastPosition();
            DomPosition sourceCaretEnd = pane.getLastPosition();
            
            DomPosition caretEnd = sourceCaretEnd.getRenderedPosition();

            Node firstNode = findFirstNode();

            if (firstNode == null) {
                return;
            }

            Node lastNode = findLastNode();
            Node caretBeginNode = caretBegin.getNode();

            if (caretBeginNode == null) {
                return;
            }

            Node caretEndNode = caretEnd.getNode();

            if (caretEndNode == null) {
                return;
            }

//            int r1 =
//                Position.compareBoundaryPoints(caretBeginNode, caretBegin.getOffset(), lastNode,
//                    10000);
            int r1 = webform.compareBoundaryPoints(caretBeginNode, caretBegin.getOffset(), lastNode, 10000);
            
//            int r2 =
//                Position.compareBoundaryPoints(caretEndNode, caretEnd.getOffset(), firstNode, 0);
            int r2 = webform.compareBoundaryPoints(caretEndNode, caretEnd.getOffset(), firstNode, 0);

            if ((r1 >= 0) && (r2 <= 0)) {
                PageBox pageBox = pane.getPageBox();

                // TODO I should make sure modelToView uses the render nodes!
//                Rectangle p0 = pageBox.modelToView(sourceCaretBegin);
//                Rectangle p1 = pageBox.modelToView(sourceCaretEnd);
                Rectangle p0 = ModelViewMapper.modelToView(pageBox, sourceCaretBegin);
                Rectangle p1 = ModelViewMapper.modelToView(pageBox, sourceCaretEnd);

                if ((p0 != null) && (p1 != null)) {
                    if ((p1.y < p0.y) || ((p0.y == p1.y) && (p1.x < p0.x))) {
                        // Swap to make sure mark comes visually before dot
                        Rectangle temp = p0;
                        p0 = p1;
                        p1 = temp;
                    }

                    g.setColor(pane.getSelectionColor());

                    if (p0.y == p1.y) {
                        // same line, render a rectangle
                        // No - shouldn't include the width on p1!
                        p1.width = 0;

                        Rectangle r = p0.union(p1);

                        if ((r.y + r.height) < y) {
                            // CULL: This can happen when you have a component which gets
                            // replicated multiple times; for example, an output text
                            // in a data table. The output text DesignBean itself
                            // is repeated on multiple separate rows. For fast 
                            // modelToView computations for carets and such, I stash
                            // the box rendered for a DesignBean directly on the
                            // DesignBean itself. However, this means that it's the
                            // LAST box created for a DesignBean which has its
                            // position associated with the DesignBean. Thus, even
                            // though every single row in the data table could
                            // match the caret test above (when the caret is actually
                            // inside the LiveBean, as is the case for editing the
                            // value attribute of an output text), the position
                            // lookup for the node will have to choose one particular
                            // rendering of that node -- it currently uses the last one.
                            // Therefore, the position we've been assigned may not
                            // represent positions in this linebox, and if so, cull
                            // the painting since we could be overwriting foreground
                            // text on a previous line.
                            return;
                        }

                        g.fillRect(r.x, r.y, r.width, r.height);
                    } else {
                        // different lines
                        if ((p1.y + p1.height) < y) {
                            // See comment CULL above
                            return;
                        }

                        int y2 = y + height;

                        // Only paint regions that vertically intersect our box
                        if (((p0.y + p0.height) > y) && (p0.y < y2)) {
                            int p0ToMarginWidth = (x + width) - p0.x;
                            int my1 = p0.y;
                            int my2 = my1 + p0.height;

                            if (my1 < y) {
                                my1 = y;
                            }

                            if (my2 > y2) {
                                my2 = y2;
                            }

                            g.fillRect(p0.x, my1, p0ToMarginWidth, my2 - my1);
                        }

                        if ((p0.y + p0.height) != p1.y) {
                            int my1 = p0.y + p0.height;
                            int myh = p1.y - (p0.y + p0.height);
                            int my2 = my1 + myh;

                            if ((my2 > y) && (my1 < y2)) {
                                // Clip to current box size
                                if (my2 > y2) {
                                    my2 = y2;
                                }

                                if (my1 < y) {
                                    my1 = y;
                                }

                                g.fillRect(x, my1, width, my2 - my1);
                            }
                        }

                        if (((p1.y + p1.height) > y) && (p1.y < y2)) {
                            int my1 = p1.y;
                            int my2 = my1 + p1.height;

                            if (my1 < y) {
                                my1 = y;
                            }

                            if (my2 > y2) {
                                my2 = y2;
                            }

                            g.fillRect(x, my1, (p1.x - x), my2 - my1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Paint the linebox. This is overiding super because while we
     * don't need the clipping in ContainerBox, we also need to
     * change background painting.
     * The issue is that let's say we have a bunch of text boxes
     * in a linebox with backgrounds. Selection-range painting, which
     * is done here in LineBoxGroup, needs to happen AFTER the textboxes
     * have drawn their backgrounds, but BEFORE they paint their text
     * foregrounds!
     *
     * So TextBoxes and LineBoxes have been modified to not paint their
     * backgrounds as part of paint. And here LineBoxGroup will first
     * paint its LineBox backgrounds, then the selection, then its
     * LineBox foregrounds/text.
     */
    public void paint(Graphics g, int px, int py) {
        px += getX();
        py += getY();

        // Box model quirk: my coordinate system is based on the visual
        // extents of the boxes - e.g. location and size of the border
        // edge.  Because of this, when visually traversing the hierarchy,
        // I need to add in the margins.
        px += leftMargin;
        py += effectiveTopMargin;

        if ((Math.abs(px) > 50000) || (Math.abs(py) > 50000) || (Math.abs(width) > 50000) ||
                (Math.abs(height) > 50000)) {
//            g.setColor(java.awt.Color.RED);
//            g.drawString("Fatal Painting Error: box " + this.toString(), 0,
//                g.getFontMetrics().getHeight());
            // XXX Improving the above error handling.
            // TODO Why is actually this state invalid?
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Fatal painting error:" // NOI18N
                            + "\nbad box=" + this // NOI18N
                            + "\nparent of bad box=" + this.getParent())); // NOI18N

            return;
        }

        // Paint children backgrounds -- before our own selection
        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox box = getBox(i);

            if (box.getBoxType() == BoxType.LINEBOX) {
                box.paintBackground(g, px, py);
            } // other boxes will do their own background painting
        }

        paintBackground(g, px, py);

        // Paint children foregrounds
        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox box = getBox(i);
            CssBox positionParent = box.getPositionedBy();

            // XXX is this possible for LineBoxes ? I don't think so...
            // Could optimize out. LineBoxes can't be positioned by
            // anyone but the LineBoxGroup directly.
            if (positionParent != this) {
                // Not positioned by us - need to compute the
                // positioning parent's absolute position
                box.paint(g, positionParent.getAbsoluteX(), positionParent.getAbsoluteY());
            } else {
                box.paint(g, px, py);
            }
        }

        if (CssBox.paintSpaces) {
            g.setColor(Color.CYAN);
            g.drawRect(getAbsoluteX(), getAbsoluteY(), width, height);
        }
    }

    /** Locate the first node that is included in this linebox */
    private Node findFirstNode() {
        for (int i = 0, n = allBoxes.size(); i < n; i++) {
            CssBox box = allBoxes.get(i);

            if (box.getBoxType() == BoxType.TEXT) {
                return ((TextBox)box).getNode();
            } else if (box.getBoxType() == BoxType.SPACE) {
                return ((SpaceBox)box).getNode();
            } else if (box.getElement() != null) {
                return box.getElement();
            }
        }

        return null;
    }

    /** Locate the last node that is included in this linebox */
    private Node findLastNode() {
        for (int i = allBoxes.size() - 1; i >= 0; i--) {
            CssBox box = allBoxes.get(i);

            if (box.getBoxType() == BoxType.TEXT) {
                return ((TextBox)box).getNode();
            } else if (box.getBoxType() == BoxType.SPACE) {
                return ((SpaceBox)box).getNode();
            } else if (box.getElement() != null) {
                return box.getElement();
            }
        }

        return null;
    }
}

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
import java.awt.Graphics;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.netbeans.modules.visualweb.designer.DesignerUtils;

import org.openide.ErrorManager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * LineBox, used during inline formatting of CSS2 content.
 * The LineBox wholds Boxes for a single line.
 * <p>
 * See  http://www.w3.org/TR/REC-CSS2/visuren.html
 *
 * @todo Should the line box know its width? Its margins?
 *
 * NOTE: This class has close ties with the ModelMapper class so be
 * sure to keep the two in sync.
 *
 * @author Tor Norbye
 */
public class LineBox extends ContainerBox {
    private int maxWidth;
    private int nextX;

    /** True iff the linebox should be float positioned */
    boolean isFloated;

    public LineBox(WebForm webform, Element element, int maxWidth, int indent) {
        // A LineBox isn't exactly an inline box, but it's NOT a block
        // level box. Perhaps I should reverse the logic and call
        // the constructor parameter "blocklevel" instead.
        super(webform, element, BoxType.LINEBOX, true, false);

        // XXX why am I passing element to super? I shouldn't do that
        this.maxWidth = maxWidth;
        this.nextX = indent;

        effectiveTopMargin = 0; // Is this still necessary?
        effectiveBottomMargin = 0;
    }

    protected String paramString() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < getBoxCount(); i++) {
            CssBox child = getBox(i);

            if (child.getBoxType() == BoxType.TEXT) {
                sb.append(((TextBox)child).getText());
            } else if (child.getBoxType() == BoxType.SPACE) {
                sb.append(' ');
            } else if (child.getBoxType() == BoxType.LINEBREAK) {
                sb.append("<br>");
            } else if (child.getElement() != null) {
                sb.append('<');
                sb.append(child.getElement().getTagName());
                sb.append('>');
            }
        }

        return "floated=" + isFloated + "," + "contents=" + sb.toString() + "," + // NOI18N
        super.paramString();
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

    /**
     *  Return the maximum/allocated width of this line box.
     *  This will typically be the width of the containing block,
     *  but floats may reduce the width.
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     *  Check whether the given box will fit in this line box.
     */
    public boolean canFit(CssBox box) {

        int boxWidth = box.getWidth();

        return !((nextX + leftMargin + boxWidth) > maxWidth);
    }

    /**
     *  Check whether the given box will fit in this line box.
     * Now counting floats.
     */
    public boolean canFit(FormatContext context, CssBox box, int targetY) {
        if(box.getParent() == null) {
            return(canFit(box));
        }
        //we need to recalculate the height and the width for a newly added
        //inline box, because it may not fit if there is a clear box and
        //the inline box height is bigger than the line height. In such a case,
        //this line will overlap with the clear box

        //a resulting line height would be a maximum of the boxes height
        int maxHeight = Math.max(box.getHeight(), box.getHeight());
        //need to recalculate the line width
        int maxWidth = context.getMaxWidth(this.getParent(), targetY, maxHeight);

        int boxWidth = box.getWidth();

        return !((nextX + leftMargin + boxWidth) > maxWidth);
    }

    /**
     *  Return true iff the line box is "full", e.g. it cannot accomodate
     *  any more content.
     */
    public boolean isFull() {
        return nextX >= maxWidth;
    }

    /**
     * Return the "real" width of the box. This is the actual width
     * of the text/boxes in the line box, as opposed to the allocated
     * width.
     *
     * @todo If text-align is justify, we should return maxWidth since
     *  we will fill it out.
     */
    public int getActualWidth() {
        return nextX;
    }

    /**
     * Return the next x position that we will assign to a box
     * added to this line box. The caller is responsible for actually
     * making sure the box will fit (by calling canFit()) first.
     */
    public int getNextX() {
        return nextX;
    }

    /**
     *  Report whether the line box is empty (contains no inline boxes).
     *
     *  @return true iff there are no inline boxes in this linebox.
     */
    public boolean isEmpty() {
        return getBoxCount() == 0;
    }

    /**
     * Adjust the horizontal alingment of this line. Shift the
     * linebox around in the containing block; this mostly mean moving
     * it to the left or right containing block edge, but it also has to
     * for account for floats.
     * lineHeight must be passed in; we need the line height to
     * decide if we intersect any floats. I could just use the box height
     * (which will be set after applyVerticalAlignments) but this ensures
     * that the caller doesn't accidentally call applyHorizontalAlignments
     * before applyVerticalAlignments, etc.
     * @todo This method works by moving the entire linebox left/right.
     *   That's not right (and wouldn't work for justify). It also
     *   conflicts with what is computed in addToLineBox (where we set
     *   the position.)   Revisit this.
     */
    public void applyHorizontalAlignments(int leftEdge, int lineHeight, FormatContext context) {
        if (isEmpty()) {
            return;
        }

        // TODO Left edge code is wrong - not taking floats into consideration
//        Value al = null;
        CssValue cssAl = null;

        Element element = getElement();
        if (element != null) {
//            al = CssLookup.getValue(element, XhtmlCss.TEXT_ALIGN_INDEX);
            cssAl = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.TEXT_ALIGN_INDEX);
        } else {
            ErrorManager.getDefault().log("linebox element was null - can't look up an alignment for it");
        }

//        if (al != null) {
        if (cssAl != null) {
//            if (al == CssValueConstants.LEFT_VALUE) {
            if (CssProvider.getValueService().isLeftValue(cssAl)) {
                // Done below - fall through
//            } else if (al.equals(CssValueConstants.RIGHT_VALUE)) {
                // XXX Revise if really equals is needed or it is just a mistake and identity check is OK.
            } else if (CssProvider.getValueService().isRightValue(cssAl)) {
                // XXX No, gotta subtrack rightPadding, rightBorderWidth,
                // and rightPadding!
                int x =  /*containingBlockX+*/(leftEdge + containingBlockWidth) - getWidth();
                setX(x);

                return;
//            } else if (al == CssValueConstants.CENTER_VALUE || al == CssValueConstants.RAVECENTER_VALUE) {
            } else if (CssProvider.getValueService().isCenterValue(cssAl)
            || CssProvider.getValueService().isRaveCenterValue(cssAl)) {
                // XXX No, gotta subtrack rightPadding, rightBorderWidth,
                // and rightPadding!
                int x =  /*containingBlockX*/+leftEdge + ((containingBlockWidth - getWidth()) / 2);
                setX(x);

                return;
//            } else if (al == CssValueConstants.JUSTIFY_VALUE) {
            } else if (CssProvider.getValueService().isJustifyValue(cssAl)) {
                // "Conforming user agents may interpret the value
                // 'justify' as 'left' or 'right', depending on
                // whether the element's default writing direction is
                // left-to-right or right-to-left, respectively. "
                // Ah heck, it's easy so let's do it (not doing
                // character spacing, just word spacing)
                int n = getBoxCount();
                setX(0);

                if (n > 1) {
                    int textWidth = 0;

                    for (int i = 0; i < n; i++) {
                        textWidth += getBox(i).getWidth(); // contentWidth instead?
                    }

                    int space = maxWidth - textWidth;

                    if (space < textWidth) { // don't justify nearly empty lines

                        int portion = space / (n - 1); // separator
                        int x = 0;

                        for (int i = 0; i < (n - 1); i++) {
                            CssBox box = getBox(i);
                            box.setX(x);
                            x += box.getWidth();
                            x += portion;
                        }

                        CssBox lastBox = getBox(n - 1);
                        lastBox.setX(maxWidth - lastBox.getWidth());
                    }
                }
            } else {
//                assert false : al;
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Unexpected alignment value, cssAl=" + cssAl)); // NOI18N
            }
        }

        // Left alignment: only need to adjust for floats
        setX(leftEdge); // XXX What about containingBlockX?
    }

    /**
     *  For each of the inline boxes on this line, adjust
     *  the vertical position of the inline boxes according
     *  to the individual vertical alignment settings.
     *  <p
     *  @todo Implement proper alignment fixing using font alignments
     *        etc.
     *  @return the total height of the line. If the LineBox is
     *          empty, it will return -1.
     */
    public int applyVerticalAlignments() {
        /*
        if (isEmpty()) {
            return -1;
        }
         */
        int height = 0;

        // The above is the MINIMUM line height - however, the spec
        // (section 10.8.1) actually states that it's the minimum line
        // height when set on a block-level element; if it's set on an
        // inline element, it's the exact height of each box generated
        // by the element (except for inline replaced element, where
        // the box height is given by the height property.)
        // This gets a bit tricky since the attribute is inherited.
        int num = getBoxCount();
        int maxAbove = 0; // above baseline
        int maxBelow = 0; // below baseline
        boolean haveText = false;

        for (int i = 0; i < num; i++) {
            // XXX This needs to get better - gotta use font
            // alignment, images, etc.
            CssBox box = getBox(i);
            int h;
            //10.8
            //On an inline-level element, 'line-height' specifies the height
            //that is used in the calculation of the line box height (except for
            //inline replaced elements, where the height of the box is given
            //by the 'height' property).
            CssValue cssBoxHeightValue = CssProvider.getEngineService().
                    getComputedValueForElement(box.getElement(), XhtmlCss.LINE_HEIGHT_INDEX);
            if (!box.isReplacedBox() && !CssProvider.getValueService().isNormalValue(cssBoxHeightValue)
            // XXX #6494312 Faking support for inline tables.
            && !(box instanceof TableBox)) {
                h = (int)cssBoxHeightValue.getFloatValue();
            } else {
                h = box.getHeight();
                if (box.getBoxType() == BoxType.TEXT) {
                    haveText = true;
                }
                
            }

            int above = box.getContributingBaseline();
            int below = h-above;
            
            // Boxes that have no baseline, such as images, also don't force themselves
            // below the baseline
            if (above == 0) {
                below = 0;
            }
            
            if (above > maxAbove) {
                maxAbove = above;
            }
            
            if (below > maxBelow) {
                maxBelow = below;
            }
            
            if (!box.isReplacedBox() && !CssProvider.getValueService().isNormalValue(cssBoxHeightValue)
            // XXX #6494312 Faking support for inline tables.
            && !(box instanceof TableBox)) {
            } else {
                if (maxAbove + maxBelow > h) {
                    h = maxAbove + maxBelow;
                }
            }

            //if the box belongs to some nested container, need to take the line-height 
            //property from that container (in case it has it)
            CssBox inlineParent = box;
            while((inlineParent = inlineParent.originalInlineContainer) != null) {
                cssBoxHeightValue = CssProvider.getEngineService().
                        getComputedValueForElement(inlineParent.getElement(), 
                        XhtmlCss.LINE_HEIGHT_INDEX);
                if(!CssProvider.getValueService().isNormalValue(cssBoxHeightValue) &&
                        !CssProvider.getEngineService().isInheritedStyleValueForElement(
                        inlineParent.getElement(), 
                        XhtmlCss.LINE_HEIGHT_INDEX)) {
                    //height of the line is exactly the value
                    h = (int)cssBoxHeightValue.getFloatValue();
                    break;
                }
            }
            if (height < h) {
                height = h;
            }
        }

        Element element = getElement();
        if (haveText) {
            if (element != null) { // XXX This is wrong - I should use the element of the child that had text
                // If not, try setting body font size to 48pt, and have a single <span> with font size 8pt. 
                // The linebox containing the span should have height 8 not 48!
//                Value heightValue = CssLookup.getValue(element, XhtmlCss.LINE_HEIGHT_INDEX);
                CssValue cssHeightValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.LINE_HEIGHT_INDEX);
                
//                if (heightValue != CssValueConstants.NORMAL_VALUE) {
                if (!CssProvider.getValueService().isNormalValue(cssHeightValue)) {
//                    int lineHeight = (int)heightValue.getFloatValue();
                    int lineHeight = (int)cssHeightValue.getFloatValue();
                    if (lineHeight > height) {
                        height = lineHeight;
                    }
                }
            }
        }        

        // linebox's baseline. This is the "baseline of the parent box"
        // listed in the CSS2 spec under the vertical-align property.
//        int baseLine = height - maxBelow;
        // XXX #109310 The baseline is at the bottom.
        int baseLine = height;

        // TODO: implement half-leading!! See CSS2 section 10.8.1!
        // Vertical alignment:
        // TODO: implement correct text-top, text-bottom and middle
        // semantics, plus properly compute offset to be used in sub
        // and super alignment
        for (int i = 0; i < num; i++) {
            // XXX This needs to get better - gotta use font
            // alignment, images, etc.
            CssBox box = getBox(i); // XX do nothing for LINEBREAK!!!
//            Value align = CssLookup.getValue(box.getElement(), XhtmlCss.VERTICAL_ALIGN_INDEX);
            CssValue cssAlign;
            if(box.originalInlineContainer != null && 
                    !CssProvider.getEngineService().isInheritedStyleValueForElement(box.getElement(), XhtmlCss.VERTICAL_ALIGN_INDEX)) {
                cssAlign = CssProvider.getEngineService().getComputedValueForElement(box.originalInlineContainer.getElement(), XhtmlCss.VERTICAL_ALIGN_INDEX);
            } else {
                cssAlign = CssProvider.getEngineService().getComputedValueForElement(box.getElement(), XhtmlCss.VERTICAL_ALIGN_INDEX);
            }
//            if (align == CssValueConstants.BASELINE_VALUE) {
            if (CssProvider.getValueService().isBaseLineValue(cssAlign)) {
                // XXX Just have a metrics accessor on CssBox instead and use that?
                int y = baseLine - box.getBaseline();
                box.setY(y);
//            } else if ((align == CssValueConstants.SUPER_VALUE) ||
//                    (align == CssValueConstants.SUB_VALUE)) {
            } else if (CssProvider.getValueService().isSuperValue(cssAlign)
            || CssProvider.getValueService().isSubValue(cssAlign)) {
                int SHIFT;

                // TODO -- gotta compute from font size! But which font? linebox'!
//                if (align == CssValueConstants.SUPER_VALUE) {
                if (CssProvider.getValueService().isSuperValue(cssAlign)) {
                    SHIFT = -3;
                } else {
                    SHIFT = 3;
                }

                int y = baseLine - box.getBaseline() + SHIFT;
                box.setY(y);
//            } else if ((align == CssValueConstants.TOP_VALUE) ||
//                    (align == CssValueConstants.TEXT_TOP_VALUE)) {
            } else if (CssProvider.getValueService().isTopValue(cssAlign)
            || CssProvider.getValueService().isTextTopValue(cssAlign)) {
                // This implements top, not text-top so add separate computation
                // for that
                box.setY(0);
//            } else if ((align == CssValueConstants.BOTTOM_VALUE) ||
//                    (align == CssValueConstants.TEXT_BOTTOM_VALUE)) {
            } else if (CssProvider.getValueService().isBottomValue(cssAlign)
            || CssProvider.getValueService().isTextBottomValue(cssAlign)) {
                // This implements bottom, not text-bottom so add separate
                // computation for that
                box.setY(height - box.getHeight());
//            } else if (align == CssValueConstants.MIDDLE_VALUE) {
            } else if (CssProvider.getValueService().isMiddleValue(cssAlign)) {
                // How do we compute the parent x-height called for in
                // the spec?  Hack for now -- just use half of
                // parent's fontheight!  Looks like Batik's
                // LengthManager is doing a 0.5 factor of the font
                // height to compute EXS anyway!
                
                // XXX #6344561 The computation didn't work for images, only for text.
                // TODO How it should be done correct way, study the spec (CSS2 10.8.1).
                if(box instanceof ImageBox) {
                    int y = (baseLine / 2) - (box.getHeight() / 2);
                    box.setY(y);
                } else {
//                    int pex = (int)CssLookup.getFontSize(element, DesignerSetings.getInstance().getDefaultFontSize()) / 2;
//                    int pex = (int)CssProvider.getValueService().getFontSizeForElement(element, DesignerSettings.getInstance().getDefaultFontSize()) / 2;
                    int pex = (int)CssProvider.getValueService().getFontSizeForElement(element, webform.getDefaultFontSize()) / 2;
                    int y = baseLine - (box.getHeight() / 2) - pex;
                    box.setY(y);
                }
            } else {
                // Percentage or length -- not yet handled!

                /*
                    // If percentage: from http://www.westciv.com/style_master/academy/css_tutorial/properties/text_layout.html
                "Percentage values

                Specifying vertical-align as a percentage value gives rise to a quite complicated situation. The baseline of the element is raised above the baseline of its parent element. By how much? By that percentage of the element's line-height.

                For example, {vertical-align: 20%} with an element that has a line-height of 10pt, the baseline of the element will be raised 2 points above the baseline of its parent element.

                You can lower the baseline of an element below the baseline of its parent by using negative percentage values."
                */
                
                // XXX This nasty assertion always breaks the designer, revise what is supposed to be here.
//                assert false : align;

                // XXX #109310 This should be lenght value now (what to do with percentage?).
                // For now handle similar way like a baseline.
                int absoluteAlign = (int)cssAlign.getFloatValue();
                int newY = baseLine - box.getBaseline() - absoluteAlign;
                box.setY(newY);
            }
        }

        if (height == 0) {
            // We've added a space but not text - return default line height
            if (element != null) {
//                Value heightValue = CssLookup.getValue(element, XhtmlCss.LINE_HEIGHT_INDEX);
                CssValue cssHeightValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.LINE_HEIGHT_INDEX);

//                if (heightValue == CssValueConstants.NORMAL_VALUE) {
                if (CssProvider.getValueService().isNormalValue(cssHeightValue)) {
//                    height = (int)(1.1 * CssLookup.getFontSize(element, DesignerSettings.getInstance().getDefaultFontSize()));
//                    height = (int)(1.1 * CssProvider.getValueService().getFontSizeForElement(element, DesignerSettings.getInstance().getDefaultFontSize()));
                    height = (int)(1.1 * CssProvider.getValueService().getFontSizeForElement(element, webform.getDefaultFontSize()));
                } else {
//                    height = (int)heightValue.getFloatValue();
                    height = (int)cssHeightValue.getFloatValue();
                }
            }
        }

        return height;
    }

    protected void paintBackground(Graphics g, int px, int py) {
        // super.paint(g); // We don't want backgrounds painted, etc.
        // XXX pass in Rectangle directly since that's what we always want?
        if (isEmpty()) {
            return;
        }

        px += getX();
        py += getY();

        int num = getBoxCount();

        for (int i = 0; i < num; i++) {
            CssBox ilb = getBox(i);

            if (ilb.getBoxType() == BoxType.LINEBREAK) {
                assert i == (num - 1);

                return;
            }

            CssBox positionParent = ilb.getPositionedBy();

            if (positionParent != this) { // XXX Can this happen in a linebox???

                // Not positioned by us - need to compute the
                // positioning parent's absolute position
                ilb.paintBackground(g, positionParent.getAbsoluteX(), positionParent.getAbsoluteY());
            } else {
                ilb.paintBackground(g, px, py);
            }
        }
    }

    public void paint(Graphics g, int px, int py) {
        // super.paint(g); // We don't want backgrounds painted, etc.
        // XXX pass in Rectangle directly since that's what we always want?
        if (isEmpty()) {
            return;
        }

        px += getX();
        py += getY();

        int num = getBoxCount();

        for (int i = 0; i < num; i++) {
            CssBox ilb = getBox(i);

            if (ilb.getBoxType() == BoxType.LINEBREAK) {
                assert i == (num - 1);

                if (!CssBox.paintSpaces) {
                    continue;
                }
            }

            CssBox positionParent = ilb.getPositionedBy();

            if (positionParent != this) {
                // Not positioned by us - need to compute the
                // positioning parent's absolute position
                ilb.paint(g, positionParent.getAbsoluteX(), positionParent.getAbsoluteY());
            } else {
                ilb.paint(g, px, py);
            }
        }

        if (CssBox.paintSpaces) {
            g.setColor(Color.PINK);
            g.drawRect(getAbsoluteX(), getAbsoluteY(), (width == 0) ? 10 : width, height);
        }
    }

    protected void addBox(CssBox box, CssBox prevBox, CssBox nextBox) {
        assert box.isInlineBox();

        // Suppress repeated LineBox.SPACE's.
        // XXX is this taken care of by the LineBoxGroup?
        if ((box.getBoxType() == BoxType.SPACE) && (getBoxCount() > 0) &&
                (getBox(getBoxCount() - 1).getBoxType() == BoxType.SPACE)) {
            return;
        }

        //super.addBox(box, prevBox, nextBox);
        // Don't do a super.addBox because we want this list to be
        // "anonymous"; the fact that boxes are parented by the linebox
        // is not known to them - so don't set parent pointers, and don't
        // maintain parent indices (BoxList.syncParentIndices)
        if (boxes == null) {
            int initialSize = 25;

            // XXX todo: pick an initial size based on the box we're
            // about to create; e.g. look at our node/element field,
            // look at the number of children, and do something based
            // on that. Typically we should do the number of element
            // nodes in the child (since most text nodes are just
            // whitespace formatting), but for LineBoxes we should do
            // something smarter.
            boxes = new BoxList(initialSize);
            boxes.setSyncParentIndices(false);

            if (boxType != BoxType.LINEBOX) {
                boxes.setKeepSorted(true);
            }
        }

        // This seems to be not needed yet.
//        // XXX #113899 Ensure the correct order. Due to complicated architecture,
//        // strange layout processing of LineBoxGroup sometimes leads to wrong order in the line box.
//        int size = boxes.size();
//        if (size > 0 && prevBox == null && nextBox == null) {
//            for (int i = 0; i < size; i++) {
//                CssBox sibling = boxes.get(i);
//                if(DesignerUtils.getNextSiblingElement(box.getElement()) == sibling.getElement()) {
//                    nextBox = sibling;
//                    break;
//                }
//            }
//            for (int i = size - 1; i >= 0; i--) {
//                CssBox sibling = boxes.get(i);
//                if (DesignerUtils.getPreviousSiblingElement(box.getElement()) == sibling.getElement()) {
//                    prevBox = sibling;
//                    break;
//                }
//            }
//        }

        boxes.add(box, prevBox, nextBox);

        //box.setParent(this);
        box.setPositionedBy(this); // ????

        box.setLocation(nextX, 0);
        nextX += box.getWidth() + box.leftMargin + box.rightMargin;
    }
    
    public void relayout(FormatContext context) {
        // LineBoxGroup should never delegate layout to us but let's
        // make doubly sure since inheriting ContainerBox.relayout
        // would be Bad.
        throw new RuntimeException();
    }

    void computeHorizontalLengths(FormatContext context) {
        // LineBoxes are anonymous, so they should have no "auto" settings
        // on them
    }

    void computeVerticalLengths(FormatContext context) {
        // LineBoxes are anonymous, so they should have no "auto" settings
        // on them
    }

    /** Return a position within this line box that the given x coordinate
     * points to in the document.
     */
//    public Position computePosition(int px) {
    public DomPosition computePosition(int px) {
        if (getBoxCount() > 0) {
            // Ensure that we're not pointing to a position further out on
            // the line than the last box
            CssBox last = getBox(getBoxCount() - 1);
            int rightmost = (last.getX() + last.getWidth()) - 1;

            if (rightmost < px) {
                px = rightmost;
            }
        }

        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox box = getBox(i);

            if (box.getBoxType() == BoxType.LINEBREAK) {
//                Position pos = Position.create(box.getSourceElement(), false);
                DomPosition pos = webform.createDomPosition(box.getSourceElement(), false);

//                if (DesignerUtils.checkPosition(pos, false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE) {
//                if (ModelViewMapper.isValidPosition(pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                if (ModelViewMapper.isValidPosition(webform, pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                    return pos;
                }
            }

            // TODO -- reformat
            int left = box.getX();
            int right = left + box.getWidth();

            if (left > px) {
                // We've already passed the spot: it must be over a space
                // between this inline box and the previous inline box
                // See which one is closest
                CssBox closest = null;
                boolean after = false;

                if (i > 0) {
                    int currDistance = left - px;
                    CssBox prevBox = getBox(i - 1);
                    int prevDistance = px - (prevBox.getX() + prevBox.getWidth());

                    if (currDistance <= prevDistance) {
                        closest = box;
                    } else {
                        closest = prevBox;
                        after = true;
                    }
                } else {
                    // No previous box - so this is definitely closest
                    closest = box;
                }

                if (closest.getBoxType() == BoxType.TEXT) {
                    // Text boxes: we support positions (offsets)
                    // within the text
                    TextBox tb = (TextBox)closest;
//                    Position pos = tb.getFirstPosition();
                    DomPosition pos = tb.getFirstPosition();

//                    if (DesignerUtils.checkPosition(pos, false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE) {
//                    if (ModelViewMapper.isValidPosition(pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                    if (ModelViewMapper.isValidPosition(webform, pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                        return pos;
                    }
                } else if (closest.getBoxType() == BoxType.SPACE) {
                    // Space boxes: we support positions (offsets)
                    // within the text
                    SpaceBox tb = (SpaceBox)closest;
//                    Position pos = tb.getFirstPosition();
                    DomPosition pos = tb.getFirstPosition();

//                    if (DesignerUtils.checkPosition(pos, false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE) {
//                    if (ModelViewMapper.isValidPosition(pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                    if (ModelViewMapper.isValidPosition(webform, pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                        return pos;
                    }
                } else {
//                    Position pos = Position.create(closest.getSourceElement(), after);
                    DomPosition pos = webform.createDomPosition(closest.getSourceElement(), after);

//                    if (DesignerUtils.checkPosition(pos, false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE) {
//                    if (ModelViewMapper.isValidPosition(pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                    if (ModelViewMapper.isValidPosition(webform, pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                        return pos;
                    }
                }
            } else if ((px >= left) && (px <= right)) {
                if (box.getBoxType() == BoxType.TEXT) {
                    // Text boxes: we support positions (offsets)
                    // within the text
//                    Position pos = ((TextBox)box).computePosition(px - left);
                    DomPosition pos = ((TextBox)box).computePosition(px - left);

//                    if (DesignerUtils.checkPosition(pos, false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE) {
//                    if (ModelViewMapper.isValidPosition(pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                    if (ModelViewMapper.isValidPosition(webform, pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                        return pos;
                    }
                } else if (box.getBoxType() == BoxType.SPACE) {
                    // Text boxes: we support positions (offsets)
                    // within the text
//                    Position pos = ((SpaceBox)box).computePosition(px - left);
                    DomPosition pos = ((SpaceBox)box).computePosition(px - left);

//                    if (DesignerUtils.checkPosition(pos, false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE) {
//                    if (ModelViewMapper.isValidPosition(pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                    if (ModelViewMapper.isValidPosition(webform, pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                        return pos;
                    }

                    /*
                    } else if (DesignerUtils.isCaretTarget(box)) {
                    // It's an "atomic" unit; place the caret
                    // on the outside
                    } else if (box instanceof FormComponentBox) {
                    Position pos = ((FormComponentBox)box).computePosition(x-box.getAbsoluteX());
                    if (pos != null) {
                        Position pos = pos;
                        if (Utilities.checkPosition(pos, false, webformwebform.getManager().getInlineEditor()) != Position.NONE) {
                            return pos;
                        }
                    }
                     */
                }

                // Other boxes (iframes, images, etc.): the offset
                // is either "before" or "after" the element:
                // offset 0 or 1. We round the x to the nearest
                // left or right edge to decide which side it goes
                // on.
                boolean after = px > ((left + right) / 2);
//                Position pos = Position.create(box.getSourceElement(), after);
                DomPosition pos = webform.createDomPosition(box.getSourceElement(), after);

//                if (DesignerUtils.checkPosition(pos, false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE) {
//                if (ModelViewMapper.isValidPosition(pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                if (ModelViewMapper.isValidPosition(webform, pos, false, /*webform*/webform.getManager().getInlineEditor())) {
                    return pos;
                }
            }
        }

//        return Position.NONE; // happens when we have no place to put the caret -- all JSF elements
        return DomPosition.NONE; // happens when we have no place to put the caret -- all JSF elements
    }
}

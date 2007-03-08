/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package org.netbeans.modules.visualweb.css2;


import java.util.List;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValueService;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import java.util.ArrayList;
import org.openide.ErrorManager;



/**
 * FormatContext used during layout/formatting of CSS2 boxes
 *
 * @todo For now I've made the fields public. Might consider
 *         using accessors and mutators.
 * @todo Should I use float/double instead of ints? CSS2 calls
 *         for that, but...
 *
 * @author Tor Norbye
 */
public class FormatContext {
    public ViewportBox initialCB; // Initial containing block / viewport
    public int initialWidth; // Width of the initial containing block
    public int initialHeight; // Height of the initial containing block
    public LineBox lineBox; // XXX SHOULD REMOVE!

    //public Element element;
    public boolean floating; // whether or not the currently formatted box is a floating box
    public List<FloatingBoxInfo> floats;

    /*
    public String toString() {
        return "FormatContext[" +
            lineBox +
            "]";
    }
    */
    public void addFloat(int x, int y, CssBox box, boolean leftSide) {
        if (floats == null) {
            floats = new ArrayList<FloatingBoxInfo>(4);
        }

        FloatingBoxInfo info = new FloatingBoxInfo(x, y, box, leftSide);
        floats.add(info);
    }

    /** Return the maximum x coordinate available - this is the right edge
     * of the containing block, minus the widths of any floats overlapping
     * this line.
     * (Actually, since floats could overlap, it returns the
     * leftmost coordinate of any float overlapping this line that is
     * floated to the right, minus the rightmost coordinate of any
     * float overlapping this line that is floated to the left...) */
    int getMaxWidth(CssBox parent, int y) {
        return(getMaxWidth(parent, y, 0));
    }

    /** Return the maximum x coordinate available - this is the right edge
     * of the containing block, minus the widths of any floats overlapping
     * this line.
     * @param y - lineBox top.
     * @param height - linebox height
     */
    int getMaxWidth(CssBox parent, int y, int height) {
        // XXX Here's an idea. Look for a common parent (between the current
        // formatting box and the floating box), and translate
        // coordinate systems appropriately so the coordinates match!
        int result = parent.containingBlockWidth;

        if (floating || (floats == null)) {
            return result;
        }

        int rightEdge = result;
        int leftEdge = 0;

        for (int i = 0, n = floats.size(); i < n; i++) {
            FloatingBoxInfo info = floats.get(i);
            CssBox box = info.box;
            if(canAdjustY(parent, box.getParent())) {
                int yp = adjustY(y, parent, box.getParent());
                
                if (
                        (yp >= info.y) &&
                        (yp < (info.y + box.getHeight())) || //top is within
                        (yp + height >= info.y) &&
                        (yp + height < (info.y + box.getHeight())) || // bottom is within
                        (yp + height <= info.y) &&
                        (yp + height > (info.y + box.getHeight())) // float is within
                        ) {
                    if (info.leftSide) {
                        int boxRightEdge = info.x + box.getWidth();
                        
                        if (boxRightEdge > leftEdge) {
                            leftEdge = boxRightEdge;
                        }
                    } else {
                        if (info.x < result) {
                            rightEdge = info.x;
                        }
                    }
                }
            }
        }

        //this could happen while trying to computate space available for 
        //other floats
        //if ((rightEdge - leftEdge) < 0) {
        //    ErrorManager.getDefault().log("Float computation: Unexpected problem");
        //}

        //return result-1;
        return rightEdge - leftEdge;
    }

    /** Return the minimum x coordinate available - this is the left edge
     * of the containing block, plus the widths of any floats overlapping
     * this line.
     * (Actually, since floats could overlap, it returns the
     * rightmost coordinate of any float overlapping this line that is
     * floated to the left.
     */
    int getLeftEdge(CssBox parent, int y, int height) {
        // When formatting a floating box, don't look for other floating
        // boxes!
        if (floating) {
            //return 0;
            return parent.leftMargin + parent.leftBorderWidth + parent.leftPadding;
        }

        int leftEdge = 0;
        int n = (floats != null) ? floats.size() : 0;

        for (int i = 0; i < n; i++) {
            FloatingBoxInfo info = floats.get(i);

            if (!info.leftSide) {
                continue;
            }

            CssBox box = info.box;

            // Find out if y, in the "parent" coordinate system, 
            // "intersect" the box (which is in its own coordinate system
            // which may be different from the parent one)
            int yp = adjustY(y, parent, box.getParent());

            if (((yp + height) > info.y) && (yp < (info.y + box.getHeight()))) {
                int boxRightEdge = info.x + box.getWidth();

                if (boxRightEdge > leftEdge) {
                    leftEdge = boxRightEdge;
                }
            }
        }

        // XXX should I add these in before the comparisons?
        return leftEdge + parent.leftMargin + parent.leftBorderWidth + parent.leftPadding;
    }

    /**
     * Given a coordinate in yBox, convert it to a coordinate
     * in floatParent. Remember that each coordinate is relative to
     * its parent, so this method essentially finds the nearest
     * common ancestor box, computes the y coordinate value relative
     * to that ancestor, it also computes the y coordinate of
     * the floatParent, and subtracts the yBox from the floatParent
     * y to compute the y value relative to the floatParent.
     * (It would be conceptually easier to just call getAbsoluteY()
     * on both boxes, subtract the difference and add y. But we cannot
     * call getAbsoluteY() on these boxes here because we are in the
     * middle of the layout and beyond the float parent the boxes may
     * not yet be positioned so we'll include UNINITIALIZED constants
     * in the computation and get wrong results.
     */
    int adjustY(int y, CssBox yBox, CssBox floatParent) {
        // Compute the common ancestor
        if (yBox == floatParent) {
            return y;
        } else {
            CssBox closest = findCommonAncestor(yBox, floatParent);
            
            if (closest == null) {
                assert false : yBox + ";" + floatParent;

                return y;
            }
            
            int yb = y;

            while (yBox != closest) {
                yb += yBox.getY();
                //yBox = yBox.getParent();
                yBox = yBox.getPositionedBy();
                // XXX TODO add in margins?
            }

            int yf = 0;

            while (floatParent != closest) {
                yf += floatParent.getY();
                //floatParent = floatParent.getParent();
                floatParent = floatParent.getPositionedBy();
                // XXX TODO add in margins?
            }

            return yb - yf;
        }
    }

    /* Find the closest common ancestor of a and b */
    private CssBox findCommonAncestor(CssBox a, CssBox b) {
        for (CssBox outer = a; outer != null; outer = outer.getParent()) {
            for (CssBox inner = b; inner != null; inner = inner.getParent()) {
                if (inner == outer) {
                    return inner;
                }
            }
        }
        
        return null;
    }
    
    /** Return the maximum x coordinate available - this is the right edge
     * of the containing block, minus the widths of any floats overlapping
     * this line.
     * (Actually, since floats could overlap, it returns the
     * leftmost coordinate of any float overlapping this line that is
     * floated to the right.
     */
    int getRightEdge(CssBox parent, int y, int height) {
        // When formatting a floating box, don't look for other floating
        // boxes!
        if (floating) {
            return parent.containingBlockWidth - parent.rightMargin - parent.rightBorderWidth -
            parent.rightPadding;
        }

        int rightEdge = parent.containingBlockWidth;
        int n = (floats != null) ? floats.size() : 0;

        for (int i = 0; i < n; i++) {
            FloatingBoxInfo info = floats.get(i);

            if (info.leftSide) {
                continue;
            }

            CssBox box = info.box;
            int yp = adjustY(y, parent, box.getParent());

            if (((yp + height) > info.y) && (yp < (info.y + box.getHeight()))) {
                int boxLeftEdge = info.x;

                if (boxLeftEdge < rightEdge) {
                    rightEdge = boxLeftEdge;
                }
            }
        }

        return rightEdge - parent.rightMargin - parent.rightBorderWidth - parent.rightPadding;
    }

    /**
     * Compute the clearance for a particular y position (compute how much
     * we have to add to it to clear all the floating boxes on the given
     * side(s)), as well as removing floats from the list.
     * @param side The side to be cleared
     * @param ignoreChildren If not null, ignore any floats that are children of this
     *    given box
     * @return the absolute y position of the cleared area. Will return Integer.MIN_VALUE
     * if nothing had to be cleared.
     */
    public int clear(CssValue cssSide, CssBox ignoreChildren) {
        int n = (floats != null) ? floats.size() : 0;

        if (n == 0) {
            return Integer.MIN_VALUE;
        }

        int cleared = Integer.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            FloatingBoxInfo info = floats.get(i);

            if (ignoreChildren != null) {
                CssBox curr = info.box;
                boolean isChild = false;

                while (curr != null) {
                    if (curr == ignoreChildren) {
                        isChild = true;

                        break;
                    }

                    curr = curr.getParent();
                }

                if (isChild) {
                    // TODO - should I skip the float list removal below?
                    continue;
                }
            }

//            if (((side == CssValueConstants.LEFT_VALUE) && !info.leftSide) ||
//                    ((side == CssValueConstants.RIGHT_VALUE) && info.leftSide)) {
            if ((CssProvider.getValueService().isLeftValue(cssSide) && !info.leftSide)
            || (CssProvider.getValueService().isRightValue(cssSide) && info.leftSide)) {
                continue;
            }

            CssBox box = info.box;
            int bottom = box.getAbsoluteY() + box.getHeight();

            if (bottom > cleared) {
                cleared = bottom;
            }
        }

        // Remove items from the clear list that are no longer in flow "scope".
        // E.g. if we've cleared items in the list we no longer have to check for
        // them.
        boolean skip = false;

//        if (side != CssValueConstants.BOTH_VALUE) {
        if (CssProvider.getValueService().isBothValue(cssSide)) {
            for (int i = 0; i < n; i++) {
                FloatingBoxInfo info = floats.get(i);

//                if (((side == CssValueConstants.LEFT_VALUE) && !info.leftSide) ||
//                        ((side == CssValueConstants.RIGHT_VALUE) && info.leftSide)) {
                if ((CssProvider.getValueService().isLeftValue(cssSide) && !info.leftSide)
                || (CssProvider.getValueService().isRightValue(cssSide) && info.leftSide)) {
                    skip = true;

                    continue;
                }
            }
        }

        if (skip) {
            // Gotta remove just some from the float list, not all
            for (int i = 0; i < n; i++) {
                FloatingBoxInfo info = floats.get(i);

//                if (((side == CssValueConstants.LEFT_VALUE) && !info.leftSide) ||
//                        ((side == CssValueConstants.RIGHT_VALUE) && info.leftSide)) {
                if ((CssProvider.getValueService().isLeftValue(cssSide) && !info.leftSide)
                || (CssProvider.getValueService().isRightValue(cssSide) && info.leftSide)) {
                    floats.remove(info);
                }
            }

            if (floats.size() == 0) {
                floats = null;
            }
        } else {
            floats = null;
        }

        return cleared;
    }

    private boolean isImportantFloat(CssBox flt, boolean left, boolean right) {
        CssValue cssFloating = CssProvider.getEngineService().getComputedValueForElement(flt.getElement(), XhtmlCss.FLOAT_INDEX);
        CssValueService service = CssProvider.getValueService();
        return(
                left  && service.isLeftValue(cssFloating) ||
                right && service.isRightValue(cssFloating));
    }
    
    /**
     * Depending on "clear" property of the box, looks for a previous floating box
     * on either or both sides of the containing block.
     * left
     * The top margin of the generated box is increased enough that the top border edge 
     * is below the bottom outer edge of any left-floating boxes 
     * that resulted from elements earlier in the source document.  
     * This is not the most efficient algorithm perhaps. We can instead look for a lowest
     * line box containing a float and then took a longer float within it. 
     * However, this algorithm looks right.
     */
    CssBox getPrevFloatingForClear(CssBox box) {
        if(!box.isClearBox()) {
            //the box has no "clear" property
            return(null);
        }

        CssValue clearValue = CssProvider.getEngineService().
                getComputedValueForElement(box.getElement(), XhtmlCss.CLEAR_INDEX);
        CssValueService service = CssProvider.getValueService();

        return(getLowestBottom(box,
                (service.isLeftValue (clearValue) || service.isBothValue(clearValue)),
                (service.isRightValue(clearValue) || service.isBothValue(clearValue))));
    }
    
    CssBox getLowestFloatingForFloat(CssBox box) {

        CssValue floatValue = CssProvider.getEngineService().
                getComputedValueForElement(box.getElement(), XhtmlCss.FLOAT_INDEX);
        CssValueService service = CssProvider.getValueService();

        return(getLowestBottom(box, true, true));
        //return(getLowestBottom(box, service.isLeftValue(floatValue), service.isRightValue(floatValue)));
    }

    private CssBox getLowestBottom(CssBox box, boolean left, boolean right) {
        int n = (floats != null) ? floats.size() : 0;

        if (n == 0) {
            return null;
        }

        int maxNextPosition=Integer.MIN_VALUE; int yAdj;
        CssBox yBox, result = null;
        for (int i = 0; i < n; i++) {
            FloatingBoxInfo info = floats.get(i);
            if(info.box == box || parentOf(box, info.box)) {
                return(result);
            }
            if(canAdjustY(info.box, box.getPositionedBy())) {
                if(isImportantFloat(info.box, left, right)) {
                    yAdj = adjustY(0, info.box, box.getPositionedBy()) + info.box.getHeight();
                    if(yAdj > maxNextPosition) {
                        maxNextPosition = yAdj;
                        result = info.box;
                    }
                }
            }
        }
        return(result);
    }
    
    private CssBox getLowestTop(CssBox box) {
        int n = (floats != null) ? floats.size() : 0;

        if (n == 0) {
            return null;
        }
        
        int maxNextPosition=Integer.MIN_VALUE; int yAdj;
        CssBox result = null;
        for (int i = 0; i < n; i++) {
            FloatingBoxInfo info = floats.get(i);
            if(info.box == box) {
                return(result);
            }
            if(canAdjustY(info.box, box.getPositionedBy())) {
                yAdj = adjustY(0, info.box, box.getPositionedBy());
                if(yAdj > maxNextPosition) {
                    maxNextPosition = yAdj;
                    result = info.box;
                }
            }
        }
        return(result);
   }

    /*
     * Checks if all parents are positioned up to common parent.
     */
    private boolean canAdjustY(CssBox box1, CssBox box2) {
        CssBox closest = findCommonAncestor(box1, box2);
        while (box1 != closest) {
            if(box1.getY() == CssBox.UNINITIALIZED) {
                return false;
            }
            box1 = box1.getPositionedBy();
        }
        while (box2 != closest) {
            if(box2.getY() == CssBox.UNINITIALIZED) {
                return false;
            }
            box2 = box2.getPositionedBy();
        }
        return(true);
    }
    
    private boolean parentOf(CssBox container, CssBox box) {
        CssBox parent = box;
        while((parent = parent.getParent()) != null) {
            if(parent == container) {
                return(true);
            }
        }
        return(false);
    }

    /**
     * Looks for a previous floating box 
     */
    CssBox getPrevFloatingForFloat(CssBox box) {
        int n = (floats != null) ? floats.size() : 0;

        if (n == 0) {
            return null;
        }

        //the code below does not work
        //in some cases some of the floats may not be positioned vertically yet, 
        //so it is not enough just to look on the previous box - 
        //we really have to find a lowest top between the already positioned
        //ones
        /*
        CssBox lastOne = null;
        for (int i = 0; i < n; i++) {
            FloatingBoxInfo info = (FloatingBoxInfo)floats.get(i);
            if(info.box == box || parentOf(box, info.box)) {
                return(lastOne);
            }
            lastOne = info.box;
        }
         */
        return(getLowestTop(box));
    }
}

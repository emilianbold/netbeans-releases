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
package org.netbeans.modules.visualweb.css2;


import java.util.List;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValueService;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import java.util.ArrayList;



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
    List<FloatingBoxInfo> floats;

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
        return(getMaxWidth(null, parent, y, 0));
    }

    /** Return the maximum x coordinate available - this is the right edge
     * of the containing block, minus the widths of any floats overlapping
     * this line.
     * @param y - lineBox top.
     * @param height - linebox height
     */
    int getMaxWidth(CssBox cssBox, CssBox parent, int y, int height) {
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
            
            // XXX #117840 Check for parentage, and skip if this float is child of the examined box.
            if (box == cssBox || isParentOf(cssBox, box)) {
                continue;
            }
            
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
    int getLeftEdge(CssBox cssBox, CssBox parent, int y, int height) {
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

            // XXX #117400 Skip if the cssBox is parent of this float.
            if (isParentOf(cssBox, box)) {
                continue;
            }
            
            // Find out if y, in the "parent" coordinate system, 
            // "intersect" the box (which is in its own coordinate system
            // which may be different from the parent one)
            int yp = adjustY(y, parent, box.getParent());

            if (((yp + height) > info.y) && (yp < (info.y + box.getHeight()))) {
                // XXX #117789 One needs to take into account also the margins, borders, paddings.
                CssBox closest = findCommonAncestor(cssBox, box);
                int cssBoxAccumulatedLefts = 0;
                int floatBoxAccumulatedHorizontals = 0;
                if (closest != null) {
                    cssBoxAccumulatedLefts = computeAccumulatedLefts(cssBox, closest);
                    floatBoxAccumulatedHorizontals = computeAccumulatedHorizontals(box, closest);
                }
                
                int boxRightEdge = info.x + box.getWidth();

//                if (boxRightEdge > leftEdge) {
//                    leftEdge = boxRightEdge;
//                }
                if ((boxRightEdge + floatBoxAccumulatedHorizontals) > (leftEdge + cssBoxAccumulatedLefts)) {
                    leftEdge = boxRightEdge;
                }
            }
        }

        // XXX should I add these in before the comparisons?
        return leftEdge + parent.leftMargin + parent.leftBorderWidth + parent.leftPadding;
    }
    
    /** Get accumulated left margins, border widths, padding 
     * up to the parentBox (excluding). */
    private static int computeAccumulatedLefts(CssBox cssBox, CssBox parentBox) {
        int accumulatedLefts = 0;
        while (cssBox != null) {
            if (cssBox == parentBox) {
                break;
            }
            int leftM = cssBox.leftMargin;
            if (leftM != CssBox.UNINITIALIZED && leftM != CssBox.AUTO) {
                accumulatedLefts += leftM;
            }
            int leftB = cssBox.leftBorderWidth;
            if (leftB != CssBox.UNINITIALIZED && leftB != CssBox.AUTO) {
                accumulatedLefts += leftB;
            }
            int leftP = cssBox.leftPadding;
            if (leftP != CssBox.UNINITIALIZED && leftP != CssBox.AUTO) {
                accumulatedLefts += leftP;
            }
            cssBox = cssBox.getParent();
        }
        return accumulatedLefts;
    }

    /** Gets accumulated horizontal (left and right) margins, border widths, padding 
     * up to the parentBox (excluding). */
    private static int computeAccumulatedHorizontals(CssBox cssBox, CssBox parentBox) {
        int accumulatedHorizontals = 0;
        while (cssBox != null) {
            if (cssBox == parentBox) {
                break;
            }
            int leftM = cssBox.leftMargin;
            if (leftM != CssBox.UNINITIALIZED && leftM != CssBox.AUTO) {
                accumulatedHorizontals += leftM;
            }
            int rightM = cssBox.rightMargin;
            if (rightM != CssBox.UNINITIALIZED && rightM != CssBox.AUTO) {
                accumulatedHorizontals += rightM;
            }
            int leftB = cssBox.leftBorderWidth;
            if (leftB != CssBox.UNINITIALIZED && leftB != CssBox.AUTO) {
                accumulatedHorizontals += leftB;
            }
            int rightB = cssBox.rightBorderWidth;
            if (rightB != CssBox.UNINITIALIZED && rightB != CssBox.AUTO) {
                accumulatedHorizontals += rightB;
            }
            int leftP = cssBox.leftPadding;
            if (leftP != CssBox.UNINITIALIZED && leftP != CssBox.AUTO) {
                accumulatedHorizontals += leftP;
            }
            int rightP = cssBox.rightPadding;
            if (rightP != CssBox.UNINITIALIZED && rightP != CssBox.AUTO) {
                accumulatedHorizontals += rightP;
            }
            cssBox = cssBox.getParent();
        }
        return accumulatedHorizontals;
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
                // XXX #123611 Another case of fatal painting error.
//                yb += yBox.getY();
                int yPos = yBox.getY();
                if (yPos == CssBox.UNINITIALIZED || yPos == CssBox.AUTO) {
//                    break;
                } else {
                    yb += yBox.getY();
                }
                //yBox = yBox.getParent();
                yBox = yBox.getPositionedBy();
                // XXX TODO add in margins?
            }

            int yf = 0;

            while (floatParent != closest) {
                // XXX #123611 Another case of fatal painting error.
//                yf += floatParent.getY();
                int yPos = floatParent.getY();
                if (yPos == CssBox.UNINITIALIZED || yPos == CssBox.AUTO) {
//                    break;
                } else {
                    yf += floatParent.getY();
                }
                
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
    int getRightEdge(CssBox cssBox, CssBox parent, int y, int height) {
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
            
            // XXX #117400 Skip if the cssBox is parent of the float.
            if (isParentOf(cssBox, box)) {
                continue;
            }
            
            int yp = adjustY(y, parent, box.getParent());

            if (((yp + height) > info.y) && (yp < (info.y + box.getHeight()))) {
                // XXX #117789 One needs to take into account also the margins, borders, paddings.
                CssBox closest = findCommonAncestor(cssBox, box);
                int cssBoxAccumulatedHorizontals = 0;
                int floatBoxAccumulatedLefts = 0;
                if (closest != null) {
                    cssBoxAccumulatedHorizontals = computeAccumulatedHorizontals(cssBox, closest);
                    floatBoxAccumulatedLefts = computeAccumulatedLefts(box, closest);
                }
                
                int boxLeftEdge = info.x;

                if ((boxLeftEdge + floatBoxAccumulatedLefts) < (rightEdge + cssBoxAccumulatedHorizontals)) {
                    rightEdge = boxLeftEdge;
                }
            }
        }

        return rightEdge - parent.rightMargin - parent.rightBorderWidth - parent.rightPadding;
    }
    
    private static boolean isParentOf(CssBox parentBox, CssBox cssBox) {
        if (parentBox == null || cssBox == null) {
            return false;
        }
        
        CssBox parent = cssBox.getParent();
        while (parent != null) {
            if (parent == parentBox) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
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

        CssBox clearContainerBox = findClearContainer(box);
        
        int maxNextPosition=Integer.MIN_VALUE; int yAdj;
        CssBox yBox, result = null;
        for (int i = 0; i < n; i++) {
            FloatingBoxInfo info = floats.get(i);
            if(info.box == box || parentOf(box, info.box)
            || (clearContainerBox != null && parentOf(info.box, clearContainerBox))) {
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
    
    private static CssBox findClearContainer(CssBox box) {
        CssBox parent = box;
        while((parent = parent.getParent()) != null) {
            CssValue cssClear = CssProvider.getEngineService().
                    getComputedValueForElement(parent.getElement(), XhtmlCss.CLEAR_INDEX);
            
            if(CssProvider.getValueService().isBothValue(cssClear) ||
                    CssProvider.getValueService().isLeftValue(cssClear) ||
                    CssProvider.getValueService().isRightValue(cssClear))
                return(parent);
        }
        return(null);
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
            // XXX #117400 If this is parent of the float stop.
            if(info.box == box) {
//            if(info.box == box || isParentOf(box, info.box)) {
                return(result);
            }
            if (isParentOf(box, info.box)) {
                continue;
            }
            if(canAdjustY(info.box, box.getPositionedBy())) {
                yAdj = adjustY(0, info.box, box.getPositionedBy());
                // XXX #117400 Use the parent float.
//                if(yAdj > maxNextPosition) {
                if(yAdj > maxNextPosition || isParentOf(info.box, result)) {
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

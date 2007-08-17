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

import org.netbeans.modules.visualweb.api.designer.cssengine.CssListValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JViewport;

import org.openide.ErrorManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.w3c.dom.Text;


// For CVS archaeology: This file used to be called org.netbeans.modules.visualweb.css2.CssContainerBox

/**
 * A CSS box which can hold children. Also a box which performs Css Layout!
 * @todo I'm handling inline parents (e.g. <span>) and block parents
 * (e.g. <div>, <p>, ...) differently. Perhaps instead of these if's in various
 * places, make separate CssInlineContainerBox and CssBlockContainer box classes
 * implementing the different semantics? (And a common parent for things like
 * defining the box list field.)
 *
 * @author Tor Norbye
 */
public class ContainerBox extends CssBox {
    /** @todo Given that I support CSS2 whitespace handling now, shouldn't I nuke this flag? */
    static final boolean COLLAPSE = true;
    static Rectangle sharedClipRect = new Rectangle();
    BoxList boxes;
    protected boolean grid = false;
    protected boolean clipOverflow;

    public ContainerBox(WebForm webform, Element element, BoxType boxType, boolean inline,
        boolean replaced) {
        super(webform, element, boxType, inline, replaced);
    }

    protected void initialize() {
        super.initialize();
        initializeGrid();

        // Initialize overflow property
        Element element = getElement();
        if (element != null) {
//            Value val = CssLookup.getValue(element, XhtmlCss.OVERFLOW_INDEX);
            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.OVERFLOW_INDEX);

//            if (val != CssValueConstants.VISIBLE_VALUE) {
            if (!CssProvider.getValueService().isVisibleValue(cssValue)) {
                clipOverflow = true;
            }
        }
    }

    /** Return true iff the given box is "opaque", e.g. paints its own
     * contents, in which case we don't want to for example make it paint
     * its own background to erase grid dots underneath
     */
    protected boolean isOpaqueBox() {
        return false;
    }

    /** Return true iff this box is "on top of" a container with an
     * image -- unless a box in between this box and the image box
     * paints the background with a solid color */
    private boolean isAboveImage() {
        CssBox curr = getParent();

        while (curr != null) {
            if (curr.bgPainter != null) { // painter implies bg image

                return true;
            }

            if (curr.bg != null) {
                return false;
            }

            curr = curr.getParent();
        }

        return false;
    }

    protected void initializeBackground() {
        super.initializeBackground();

        ContainerBox parent = getParent();
        // Note: TableBox.CellBox doesn't call super.initializeBackground,
        // so if you do additional work here, check CellBox too.
        // XXX parent cannot be null here since background is an invariant and
        // is initialized from the constructor! Therefore, this code is
        // useless at the moment; I've gotta make it run later
        if (!isOpaqueBox() && (!inline || boxType.isAbsolutelyPositioned()) && (bg == null)
        && (parent != null) && !grid && (parent.isGrid() || (parent.tag == HtmlTag.FORM))
        // special handling for the <form> tag - we want the grid to
        // shine through
        && (tag != HtmlTag.FORM) && !isAboveImage()) {
            // The parent has painted a grid, yet we're not a grid
            // component, so we've gotta "erase" the parent's grid dots
            CssBox p = parent;

            while ((p != null) && (p.bg == null)) {
                p = p.getParent();
            }

            if (p != null) {
                bg = p.bg;
            }
        }
    }

    /** Initialize whether this box should show a visual grid and should receive
     * grid mode handling from mouse operations */
    protected void initializeGrid() {
        if (!inline) {
//            Value val = CssLookup.getValue(getElement(), XhtmlCss.RAVELAYOUT_INDEX);
            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(getElement(), XhtmlCss.RAVELAYOUT_INDEX);

//            if (val == CssValueConstants.GRID_VALUE) {
            if (CssProvider.getValueService().isGridValue(cssValue)) {
                setGrid(true);
            }
        }
    }

    /** Set whether this is a grid-positioned box */
    public void setGrid(boolean grid) {
        this.grid = grid;
    }

    /** Indicate whether this is a grid-positioned box */
    public boolean isGrid() {
        if (tag == HtmlTag.FORM) {
            return grid || getParent().isGrid();
        }

        return grid;
    }

    /**
     * Return the list of boxes "managed" by this box.  Managed simply
     * means that the coordinates in the boxes are all relative to this
     * one.
     */
    public int getBoxCount() {
        return (boxes != null) ? boxes.size() : 0;
    }

    /**
     * Return the box with the given index. There is no particular
     * significance to the index other than identifying a box; in particular
     * boxes with adjacent indices may not be adjacent visually.
     */
    public CssBox getBox(int index) {
        return boxes.get(index);
    }

    protected BoxList getBoxList() {
        return boxes;
    }

    public CssBox[] getBoxes() {
        if(boxes == null) {
            return new CssBox[0];
        }

        List<CssBox> boxList = new ArrayList<CssBox>();
        int count = boxes.size();
        for(int i = 0; i < count; i++) {
            CssBox box = boxes.get(i);
            boxList.add(box);
        }
        
        return boxList.toArray(new CssBox[boxList.size()]);
    }
    
    boolean containsChild(CssBox cssBox) {
        if (cssBox == null) {
            return false;
        }
        CssBox[] children = getBoxes();
        return Arrays.asList(children).contains(cssBox);
    }
    
    /**
     * Remove all the children boxes from the box list
     */
    protected void removeBoxes() {
        boxes = null;
    }

    /**
     * Add a new box to the list of boxes managed by this box
     */
    protected void addBox(CssBox box, CssBox prevBox, CssBox nextBox) {
        if (boxes == null) {
            //boxes = new ArrayList(8);
            int initialSize = 8;

            // XXX todo: pick an initial size based on the box we're
            // about to create; e.g. look at our node/element field,
            // look at the number of children, and do something based
            // on that. Typically we should do the number of element
            // nodes in the child (since most text nodes are just
            // whitespace formatting), but for LineBoxes we should do
            // something smarter.
            // Ditto for tables - we know roughly how many cells we're
            // going to add (non-rectangular tables or tables with
            // colspans and rowspans will be smaller).
            boxes = new BoxList(initialSize);

            if (boxType != BoxType.LINEBOX) {
                boxes.setKeepSorted(true);
            }
        }

        boxes.add(box, prevBox, nextBox);
        box.setParent(this);
        box.setPositionedBy(this);
    }

    /**
     * Remove a particular box from the children list
     */
    protected boolean removeBox(CssBox box) {
        if (boxes == null) {
            // Internal error
            ErrorManager.getDefault().log("Unexpected box removal - box list is empty already");

            return false;
        }

        return boxes.remove(box);
    }

    /** This method can be called to make a hint as to how many
     * children will be added to this box. If the box already has
     * children, this method has no effect.
     */
    protected void setProbableChildCount(int children) {
        if (boxes == null) {
            boxes = new BoxList(children);

            if (boxType != BoxType.LINEBOX) {
                boxes.setKeepSorted(true);
            }
        }
    }

    /**
     *  Return the last box in the format list for this box container, or
     *  null if there are no boxes yet.
     * @todo Is this method unused now that I have getPrevNormalBox?
     */

    /*
    CssBox getLastBox() {
        return (getBoxCount() > 0) ? getBox(getBoxCount() - 1) : null;
    }
    */

    /** For debugging purposes only */
    static void printLayout(CssBox b, StringBuffer sb, int depth) {
        sb.append("\n");

        //Log.indent(depth);
        //sb.append("printLayout(box=" + box + ", depth=" + depth);
        for (int i = 0; i < depth; i++) {
            sb.append("   ");
        }

        sb.append("  [" + b.getX() + "," + b.getY() + "," + b.getWidth() + "," + b.getHeight() +
            "] ");
        sb.append("  BoxModel [");

        if ((b.boxType != null) && b.boxType.isPositioned()) {
            sb.append(b.left + "," + b.top + "," + b.right + "," + b.bottom + ",");
        }

        sb.append(b.contentWidth + "," + b.contentHeight + ", mrg=" + b.leftMargin + "," +
            b.effectiveTopMargin + "] ");
        sb.append(" CB  [" + b.containingBlockX + "," + b.containingBlockY + "," +
            b.containingBlockWidth + "," + b.containingBlockHeight + "] ");

        //        sb.append(
        //            "  Color ["
        //                + org.netbeans.modules.visualweb.designer.DesignerUtils.colorToStringName(b.bg)
        //                + "] ");
        sb.append("\n");

        if (b.border != null) {
            sb.append("  Border [" + b.border.toString() + "]\n");
        }

        for (int i = 0; i < depth; i++) {
            sb.append("   ");
        }

        sb.append(b.toString());

        //                if (lb == null) {
        for (int i = 0; i < depth; i++) {
            sb.append("   ");
        }

        sb.append("  [abs=" + b.getAbsoluteX() + "," + b.getAbsoluteY() + ", rel=" + b.getX() +
            "," + b.getY() + ", size=" + b.getWidth() + "," + b.getHeight() + "]  ");

        if (b.getBoxCount() > 0) {
            //Log.indent(depth);
            //sb.append("Num Boxes=" + box.getBoxCount());
            for (int l = 0; l < b.getBoxCount(); l++) {
                CssBox bc = b.getBox(l);
                sb.append("\n");

                for (int i = 0; i < (depth + 1); i++) {
                    sb.append("   ");
                }

                sb.append("Box number " + l + "\n");
                printLayout(bc, sb, depth + 1);
                sb.append("\n");
            }
        }
    }

    /**
     * Recursively create and add boxes for all the children elements
     * of the element corresponding to this box. In other words, build
     * the box hierarchy.  This will not do layout on the boxes - assigning
     * positions, sizes, or flowing inline content. org.netbeans.modules.visualweb.css2.FacesSupport.printNode(element)
     */
    protected void createChildren(CreateContext context) {
        Element element = getElement();
        if (element == null) {
            return;
        }

        NodeList list = element.getChildNodes();
        int len = list.getLength();
        setProbableChildCount(len);

        for (int i = 0; i < len; i++) {
            org.w3c.dom.Node child = (org.w3c.dom.Node)list.item(i);

            if ((child.getNodeType() == Node.TEXT_NODE) && COLLAPSE &&
                    DesignerUtils.onlyWhitespace(child.getNodeValue())) {
                continue;
            }

            addNode(context, child, null, null, null);
        }
    }

    /**
     * Add a box for the given child node
     */
    void addNode(CreateContext context, Node node, Element sourceElement, CssBox prevBox,
        CssBox nextBox) {
        // Find out what kind of box positioning we're dealing with
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element)node;
            String tagName = element.getTagName();
            HtmlTag tag = HtmlTag.getTag(tagName);

            if (tag == null) {
                if ((tagName.length() > 0) && Character.isUpperCase(tagName.charAt(0))) {
//                    // TODO - line number?
////                    org.netbeans.modules.visualweb.insync.markup.MarkupUnit unit = webform.getMarkup();
//                    Element e = MarkupService.getCorrespondingSourceElement(element);
//                    String message =
//                        NbBundle.getMessage(ContainerBox.class, "UppercaseTag", tagName);
////                    MarkupService.displayError(unit.getFileObject(), unit.computeLine(e), message);
//                    Document doc = e.getOwnerDocument();
//                    FileObject fo = InSyncService.getProvider().getFileObject(doc);
//                    int line = InSyncService.getProvider().computeLine(doc, e);
//                    InSyncService.getProvider().getRaveErrorHandler().displayErrorForFileObject(message, fo, line, 0);
                    // XXX This validation should be done in parser, not here!
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new IllegalStateException("The element has uppercase tag name, element=" + element)); // NOI18N
                }

                // What do we do about unrecognized content? Should
                // probably process its children, right? E.g. if the
                // document contains <foobar>Hello World</foobar>,
                // "Hello World" should be shown even though it's
                // contained in an "unknown" element.
                NodeList nl = element.getChildNodes();
                int num = nl.getLength();

                if (num > 0) {
                    setProbableChildCount(num); // or addProbablyChildCount?? XXX

                    for (int i = 0, n = num; i < n; i++) {
                        Node nn = nl.item(i);

                        if ((nn.getNodeType() == Node.TEXT_NODE) && COLLAPSE &&
                                DesignerUtils.onlyWhitespace(nn.getNodeValue())) {
                            continue;
                        }

                        addNode(context, nn, null, prevBox, nextBox); // Recurse
                    }
                }

                return;
            } else {
                // XXX if element == body, it should be the case that
                // boxType == BoxType.STATIC -- we should ignore the position
                // and float properties! (See section 9.1.2)
//                Value display = CssLookup.getValue(element, XhtmlCss.DISPLAY_INDEX);
                CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.DISPLAY_INDEX);

//                if (display == CssValueConstants.NONE_VALUE) {
                if (CssProvider.getValueService().isNoneValue(cssDisplay)) {
                    return;
                }

//                boolean inline = isInlineTag(display, element, tag);
                boolean inline = isInlineTag(cssDisplay, element, tag);

                BoxType boxType = BoxType.getBoxType(element);
                boolean replaced = tag.isReplacedTag();
                CssBox box = null;

//                if (display == CssValueConstants.TABLE_VALUE /* || display == CssValueConstants.INLINE_TABLE_VALUE*/) {
                if (CssProvider.getValueService().isTableValue(cssDisplay)) {
                    // TODO when the dust settles: move this logic into the box factory

                    /*
                    if (display == CssValueConstants.INLINE_TABLE_VALUE) {
                        inline = true;
                        replaced = true;
                    }
                     */
                    box = TableBox.getTableBox(webform, element, boxType, inline, replaced);
                } else {
                    box = BoxFactory.create(context, tag, webform, element, boxType, inline,
                            replaced);
                }

                if (box == null) {
                    // It's a hidden element - e.g. <input type="hidden">, 
                    // or perhaps some tag we've marked as visually hidden,
                    // e.g. <area>.
                    return;
                }

                if (sourceElement != null) {
                    // This box was derived from a different source element
                    // (e.g. a jsf tag which was rendered into an html tag)
                    // so set up a mapping from the source element as well
//                    CssBox.putBoxReference(sourceElement, box);
                    putBoxReference(sourceElement, box);
                }

                addBoxNode(tag, box, context, prevBox, nextBox);
            }
        } else if ((node.getNodeType() == Node.TEXT_NODE) ||
                (node.getNodeType() == Node.CDATA_SECTION_NODE)) {
            String content = node.getNodeValue();
            Element styleElement = null;

            if ((node.getParentNode() != null) &&
                    (node.getParentNode().getNodeType() == Node.ELEMENT_NODE)) {
                styleElement = (Element)node.getParentNode();

                // XXX I should just use context.element without above
                // check and cast - context.element should always
                // point to the nearest element from which I draw CSS
                // properties!
                //assert parent == this.element;
                //if (parent != this.element) {
                //    System.out.println("Parent element was " + parent + " and this.element is " + this.element);
                //}
            }

            Text textNode = (node instanceof Text) ? (Text)node : null;

            //            if (textNode != null) {
            //                textNode = textNode.getSourceNode();
            //            }
            addText(context, textNode, styleElement, content);
        }
    }

    /** Add the given child box to this parent box list, and
     * recursively create its children / box hiearchy. */
    protected void addBoxNode(HtmlTag tag, CssBox box, CreateContext context, CssBox prevBox,
        CssBox nextBox) {
        // Special handling when we're inserting in the middle of an
        // existing box tree
        if ((prevBox != null) || (nextBox != null)) {
            // For absolutely positioned boxes, we don't care about siblings
            if (!box.getBoxType().isAbsolutelyPositioned()) {
                addSiblingBoxNode(tag, box, context, prevBox, nextBox);

                return;
            }

            nextBox = null;
            prevBox = null;
        }

        // XXX just pass in null for the other puppies
        boolean finishLineBoxAfterChildren = false;
        boolean preserveLineBox = false;
        LineBoxGroup oldLineBox = null;

        if ((box.getBoxType() == BoxType.ABSOLUTE) || (box.getBoxType() == BoxType.FIXED)) {
            // Even inline tags should be positioned absolutely (not
            // added to a line box) but don't break the inline context
            finishLineBoxAfterChildren = true;
            preserveLineBox = true;
            oldLineBox = context.lineBox;
            context.lineBox = null;
            getBlockBox().addBox(box, prevBox, nextBox);

            if (box.getBoxType() == BoxType.FIXED) {
                context.addFixedBox(box);
            }
        } else if (box.getBoxType() == BoxType.FLOAT) {
            addToLineBox(context, box, null, null); // XXX Should finishLineBoxAfterChildren!
            preserveLineBox = true;
            oldLineBox = context.lineBox;
            context.lineBox = null;
        } else if (box.isBlockLevel()) {
            finishLineBox(context);
            getBlockBox().addBox(box, prevBox, nextBox); // redundant?
            finishLineBoxAfterChildren = true;
        } else {
            addToLineBox(context, box, null, null);
        }

        // Create children of the box
        if (box instanceof ContainerBox) {
            ((ContainerBox)box).createChildren(context);
        }

        if (finishLineBoxAfterChildren) {
            finishLineBox(context);
        }

        if (preserveLineBox) {
            context.lineBox = oldLineBox;
        }
    }

    /** Add the given child box to this parent box list, and
     * recursively create its children / box hiearchy.
     * Deal with "prevBox" and "nextBox" to determine insertion
     * in the middle of the box hierarchy; used during insert events.
     *
     * @todo This code is really hacky; clean it up.
     */
    protected void addSiblingBoxNode(HtmlTag tag, CssBox box, CreateContext context,
        CssBox prevBox, CssBox nextBox) {
        boolean finishLineBoxAfterChildren = false;
        boolean preserveLineBox = false;
        LineBoxGroup oldLineBox = null;

        if ((box.getBoxType() == BoxType.ABSOLUTE) || (box.getBoxType() == BoxType.FIXED)) {
            // Absolute boxes do not care where they're inserted - 
            // other than the actual painting/rendering order, since
            // z-order is sometimes related to the model/boxlist
            // position.
            // Even inline tags should be positioned absolutely (not
            // added to a line box) but don't break the inline context
            finishLineBoxAfterChildren = true;
            preserveLineBox = true;
            oldLineBox = context.lineBox;
            context.lineBox = null;
            getBlockBox().addBox(box, prevBox, nextBox);

            if (box.getBoxType() == BoxType.FIXED) {
                context.addFixedBox(box);
            }
        } else if (box.getBoxType() == BoxType.FLOAT) {
            // XXX how does this work?
            addToLineBox(context, box, prevBox, nextBox);
            preserveLineBox = true;
            oldLineBox = context.lineBox;
            context.lineBox = null;
        } else if (box.isBlockLevel()) {
            // Since we're not looking at absolute positioning, adjust prev/next
            // references to refer to normal children only
            while ((prevBox != null) &&
                    !(prevBox.getBoxType().isNormalFlow() ||
                    (prevBox.getBoxType().isInlineTextBox()))) {
                prevBox = prevBox.getPrevNormalBox();
            }

            while ((nextBox != null) &&
                    !(nextBox.getBoxType().isNormalFlow() ||
                    (nextBox.getBoxType().isInlineTextBox()))) {
                nextBox = nextBox.getNextNormalBox();
            }

            if ((prevBox != null) && (nextBox != null) && prevBox.isInlineBox() &&
                    nextBox.isInlineBox()) {
                // Block box is inserted in the middle of an inline context.
                // This means we need to split the linebox in half and
                // insert the block box in the middle.
                // XXX TODO
                LineBoxGroup lb = null;

                if (prevBox instanceof LineBoxGroup) {
                    lb = (LineBoxGroup)prevBox; // happens when prev was absolutely positioned for example
                } else if (prevBox.getParent() instanceof LineBoxGroup) {
                    lb = (LineBoxGroup)prevBox.getParent();
                } else {
                    lb = (LineBoxGroup)prevBox.getParent().getParent();
                }

                // prev and next should be in the same lineboxgroup
                //assert lb == (nextBox.getParent() instanceof LineBoxGroup ?
                //              (LineBoxGroup)nextBox.getParent() :
                //              (LineBoxGroup)nextBox.getParent().getParent());
                //      gotta check if nextBox instanceof LineBoxGroup too
                LineBoxGroup alb = lb.split(prevBox);
                context.prevChangedBox = lb;
                context.nextChangedBox = alb;
                getBlockBox().addBox(box, lb, null);
                getBlockBox().addBox(alb, box, null);
            } else {
                // We're either right above or right below a block box,
                // so simply insert the box where it needs to be.
                finishLineBox(context);

                if ((prevBox != null) && prevBox.isInlineBox()) {
                    while ((prevBox != null) && !(prevBox instanceof LineBoxGroup)) {
                        prevBox = prevBox.getParent();
                    }
                }

                if ((nextBox != null) && nextBox.isInlineBox()) {
                    while ((nextBox != null) && !(nextBox instanceof LineBoxGroup)) {
                        nextBox = nextBox.getParent();
                    }
                }

                getBlockBox().addBox(box, prevBox, nextBox);
                finishLineBoxAfterChildren = true;
            }
        } else {
            // Inline
            // Since we're not looking at absolute positioning, adjust prev/next
            // references to refer to normal children only
            while ((prevBox != null) &&
                    !(prevBox.getBoxType().isNormalFlow() ||
                    (prevBox.getBoxType().isInlineTextBox()))) {
                prevBox = prevBox.getPrevNormalBox();
            }

            while ((nextBox != null) &&
                    !(nextBox.getBoxType().isNormalFlow() ||
                    (nextBox.getBoxType().isInlineTextBox()))) {
                nextBox = nextBox.getNextNormalBox();
            }

            if ((prevBox != null) && (nextBox != null) && prevBox.isInlineBox() &&
                    nextBox.isInlineBox()) {
                // Inline box is inserted in the middle of an inline context.
                // This means we simply need to inject the inline box
                // into the middle of the LineBoxGroup
                LineBoxGroup lb = null;

                if (prevBox instanceof LineBoxGroup) {
                    lb = (LineBoxGroup)prevBox; // happens when prev was absolutely positioned for example
                } else if (prevBox.getParent() instanceof LineBoxGroup) {
                    lb = (LineBoxGroup)prevBox.getParent();
                } else {
                    lb = (LineBoxGroup)prevBox.getParent().getParent();
                }

                // prev and next should be in the same lineboxgroup
                //assert lb == (nextBox.getParent() instanceof LineBoxGroup ?
                //              (LineBoxGroup)nextBox.getParent() :
                //               (LineBoxGroup)nextBox.getParent().getParent());
                //      gotta check if nextBox instanceof LineBoxGroup too
                lb.addBox(box, prevBox, nextBox);
            } else if ((prevBox != null) && prevBox.isInlineBox()) {
                // We're inserting an inline element between an
                // inline box and a block box; add it to the previous
                // linebox
                LineBoxGroup lb = null;

                if (prevBox instanceof LineBoxGroup) {
                    lb = (LineBoxGroup)prevBox;
                    prevBox = null;
                } else if (prevBox.getParent() instanceof LineBoxGroup) {
                    lb = (LineBoxGroup)prevBox.getParent();
                } else {
                    assert (prevBox.getParent() != null) &&
                    prevBox.getParent().getParent() instanceof LineBoxGroup;
                    lb = (LineBoxGroup)prevBox.getParent().getParent();
                }

                lb.addBox(box, prevBox, null);
            } else if ((nextBox != null) && nextBox.isInlineBox()) {
                // We're inserting an inline element between a
                // block box and an inline box; add it to the front
                // of the next linebox
                LineBoxGroup lb = null;

                if (nextBox instanceof LineBoxGroup) {
                    lb = (LineBoxGroup)nextBox;
                    nextBox = null;
                } else if (nextBox.getParent() instanceof LineBoxGroup) {
                    lb = (LineBoxGroup)nextBox.getParent();
                } else {
                    assert (nextBox.getParent() != null) &&
                    nextBox.getParent().getParent() instanceof LineBoxGroup;
                    lb = (LineBoxGroup)nextBox.getParent().getParent();
                }

                lb.addBox(box, null, nextBox);
            } else {
                // We're inserting an inline element between two block
                // boxes
                addToLineBox(context, box, prevBox, nextBox);
            }
        }

        // Create children of the box
        if (box instanceof ContainerBox) {
            ((ContainerBox)box).createChildren(context);
        }

        if (finishLineBoxAfterChildren) {
            finishLineBox(context);
        }

        if (preserveLineBox) {
            context.lineBox = oldLineBox;
        }
    }

    /**
     *  For a given TEXT_NODE/CDATA_SECTION_NODE, add the text to the
     *  current line box
     */
    protected void addText(CreateContext context, Text textNode, Element styleElement,
        String text) {
        // Check font size and attributes
        int decoration = 0;
        Color fg = Color.black;
        Color bg = null;
        FontMetrics metrics = context.metrics;
        boolean collapseSpaces = true;
        boolean hidden = false;

        if (styleElement != null) {
//            metrics = CssLookup.getFontMetrics(styleElement);
//            metrics = CssProvider.getValueService().getFontMetricsForElement(styleElement);
            metrics = CssUtilities.getDesignerFontMetricsForElement(styleElement, text, webform.getDefaultFontSize());
            
//            fg = CssLookup.getColor(styleElement, XhtmlCss.COLOR_INDEX);
            fg = CssProvider.getValueService().getColorForElement(styleElement, XhtmlCss.COLOR_INDEX);

            if (fg == null) {
                fg = Color.black;
            }

            //bg = Css.getColor(styleElement, XhtmlCssEngine.BACKGROUND_COLOR_INDEX);
            // Pick up the background color for text only when the background
            // color is set on an inline tag. If the background color
            // is coming from a block level box, then the background will
            // already have been painted as part of the block. Thus, this
            // will let us paint backgrounds when set on e.g. spans.
            // I also noticed Mozilla will inherit background colors when
            // set on inline tags - e.g.
            //   <span style="background: red"><b><i>Has Red Bg</i></b></span>
            // So this code achieves that too; it's a bit of a hack since
            // it won't use proper selectors etc - when we redo the CSS
            // parser this should hopefully be taken care of by the
            // cascade!
            bg = null;

            CssBox curr = this;

            while ((curr != null) && curr.inline && !curr.replaced) {
                if (curr.bg == null) {
                    // XXX This is a hack! I should initialize this
                    // earlier on. 
                    // XXX this should not be a problem anymore, I have initializeInvariants now
                    curr.initializeBackground();
                }

                if (curr.bg != null) {
                    bg = curr.bg;

                    break;
                }

                curr = curr.getParent();
            }

//            Value val = CssLookup.getValue(styleElement, XhtmlCss.TEXT_DECORATION_INDEX);
            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(styleElement, XhtmlCss.TEXT_DECORATION_INDEX);

//            switch (val.getCssValueType()) {
//            case CSSValue.CSS_VALUE_LIST:
//                ListValue lst = CssLookup.getListValue(val);
//                if (lst == null) {
//                    break;
//                }
            CssListValue cssListValue = CssProvider.getValueService().getComputedCssListValue(cssValue);
            if (cssListValue != null) {

//                int len = lst.getLength();
                int len = cssListValue.getLength();

                for (int i = 0; i < len; i++) {
//                    Value v = lst.item(i);
                    CssValue cssV = cssListValue.item(i);
//                    String s = v.getStringValue();
                    String s = cssV.getStringValue();

                    switch (s.charAt(0)) {
                    case 'u':
                        decoration |= TextBox.UNDERLINE;

                        break;

                    case 'o':
                        decoration |= TextBox.OVERLINE;

                        break;

                    case 'l':
                        decoration |= TextBox.STRIKE;

                        break;
                    }
                }

//                break;
//            default:
            } else {
                // XXX what happened?
            }

            // XXX Technically, should check for decoration=="overline" too...
            // (See section 16.3.1). However, does that have ANY practical
            // utility?
//            val = CssLookup.getValue(styleElement, XhtmlCss.WHITE_SPACE_INDEX);
            CssValue cssValue2 = CssProvider.getEngineService().getComputedValueForElement(styleElement, XhtmlCss.WHITE_SPACE_INDEX);

//            if ((val == CssValueConstants.PRE_VALUE) || (val == CssValueConstants.PRE_WRAP_VALUE)) {
            if (CssProvider.getValueService().isPreValue(cssValue2)
            || CssProvider.getValueService().isPreWrapValue(cssValue2)) {
                collapseSpaces = false;
            }

//            val = CssLookup.getValue(getElement(), XhtmlCss.VISIBILITY_INDEX);
            CssValue cssValue3 = CssProvider.getEngineService().getComputedValueForElement(getElement(), XhtmlCss.VISIBILITY_INDEX);
//            hidden = (val != CssValueConstants.VISIBLE_VALUE);
            hidden = !CssProvider.getValueService().isVisibleValue(cssValue3);
        } else {
            // Initialize metrics to something useful!
            ErrorManager.getDefault().log("Gotta set font from somewhere else!");
        }

        addText(text, styleElement, textNode, context, metrics, fg, bg, decoration, collapseSpaces,
            hidden);
    }

    protected void addGrayItalicText(CreateContext context, Element styleElement, String text) {
        FontMetrics metrics = null;

        if (styleElement != null) {
//            metrics = CssLookup.getFontMetrics(styleElement);
//            metrics = CssProvider.getValueService().getFontMetricsForElement(styleElement);
            metrics = CssUtilities.getDesignerFontMetricsForElement(styleElement, text, webform.getDefaultFontSize());

            Font font = metrics.getFont();
            font = font.deriveFont(Font.ITALIC);
//            metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            metrics = DesignerUtils.getFontMetrics(font);
        }

        if (metrics == null) {
            metrics = context.metrics;
        }

        addText(text, styleElement, null, context, metrics, Color.GRAY, null, 0, true, false);
    }

    /**
     * May add one or more boxes, each one of which may have box
     * children.
     * @todo Move this routine into static TextBox method?
     */
    private void addText(String content, Element styleElement, Text textNode,
        CreateContext context, FontMetrics metrics, Color fg, Color bg, int decoration,
        boolean collapseSpaces, boolean hidden) {
        // XXX separateTextBox(context);
        // Translate the string from jspx to xhtml
        // When doing visual position computations, we need to 
        // reverse the computation.
        String source = content;

//        if ((textNode != null) && textNode.isJspx()) {
        if (textNode != null && MarkupService.isJspxNode(textNode)) {
            content =
                    // <markup_separation>
//                MarkupServiceProvider.getDefault().expandHtmlEntities(content, true, styleElement);
                    // ====
//                InSyncService.getProvider().expandHtmlEntities(content, true, styleElement);
                WebForm.getDomProviderService().expandHtmlEntities(content, true, styleElement);
                    // </markup_separation>
        }

        if (styleElement != null) {
//            Value v1 = CssLookup.getValue(styleElement, XhtmlCss.FONT_VARIANT_INDEX);
//            Value v2 = CssLookup.getValue(styleElement, XhtmlCss.TEXT_TRANSFORM_INDEX);
            CssValue cssV1 = CssProvider.getEngineService().getComputedValueForElement(styleElement, XhtmlCss.FONT_VARIANT_INDEX);
            CssValue cssV2 = CssProvider.getEngineService().getComputedValueForElement(styleElement, XhtmlCss.TEXT_TRANSFORM_INDEX);

//            if ((v1 == CssValueConstants.SMALL_CAPS_VALUE) ||
//                    (v2 == CssValueConstants.UPPERCASE_VALUE)) {
            if (CssProvider.getValueService().isSmallCapsValue(cssV1)
            || CssProvider.getValueService().isUpperCaseValue(cssV2)) {
                // Uppercase the text
                content = content.toUpperCase();

                // TODO (much later): split the text up like under capitalization
                // and apply different fonts to the initial letters
                // and the rest of the words. I can't trivially do that
                // here because I would create separate TextBoxes for the
                // initial character and the rest of the words, and this
                // COULD be split up both in text justification and in word
                // wrapping by the LineBox and LineBoxGroup containers, which
                // would be visually disasterous. I think the painting of
                // this would really have to be done in the TextBox itself.
//            } else if (v2 == CssValueConstants.LOWERCASE_VALUE) {
            } else if (CssProvider.getValueService().isLowerCaseValue(cssV2)) {
                content = content.toLowerCase();
//            } else if (v2 == CssValueConstants.CAPITALIZE_VALUE) {
            } else if (CssProvider.getValueService().isCapitalizeValue(cssV2)) {
                StringBuffer sb = new StringBuffer(content.length());
                boolean capitalize = false;

                for (int i = 0, n = content.length(); i < n; i++) {
                    char c = content.charAt(i);

                    if (Character.isWhitespace(c)) {
                        capitalize = true;
                    } else if (capitalize) {
                        c = Character.toUpperCase(c);
                        capitalize = false;
                    }

                    sb.append(c);
                }

                content = sb.toString();
            }
        }

        // Determine how much text will fit
        char[] contentChars = null;
        int len = content.length();

        /*
          From the HTML4.01 spec: make sure we handle this correctly:

          In Western scripts, for example, text should only be
          wrapped at white space. Early user agents incorrectly
          wrapped lines just after the start tag or just before
          the end tag of an element, which resulted in dangling
          punctuation. For example, consider this sentence:

          A statue of the <A href="cih78">Cihuateteus</A>, who are
          patron ...

          Wrapping the line just before the end tag of the A
          element causes the comma to be stranded at the beginning
          of the next line:

          A statue of the Cihuateteus
          , who are patron ...

        */

        // Find (and add) the next word
        int begin = 0;
        int end;

        if (!collapseSpaces) {
            end = len;

            if (contentChars == null) {
                contentChars = content.toCharArray(); // XXX share among views?
            }

            if (content.indexOf('\n') != -1) { // XXX what about \r and \r\n and \n\r?

                for (int i = begin; i < end; i++) {
                    if (content.charAt(i) == '\n') {
                        // Split here           
                        if (i > begin) {
                            TextBox box =
                                new TextBox(webform, styleElement, textNode, contentChars, content,
                                    source, begin, i, fg, bg, decoration, metrics, hidden);
                            addToLineBox(context, box, null, null);
                        }

                        addToLineBox(context, new LineBreakBox(webform, styleElement, null), null,
                            null);
                        begin = i + 1;
                    }
                }

                if (end > begin) {
                    TextBox box =
                        new TextBox(webform, styleElement, textNode, contentChars, content, source,
                            begin, end, fg, bg, decoration, metrics, hidden);
                    addToLineBox(context, box, null, null);
                }

                addToLineBox(context, new LineBreakBox(webform, styleElement, null), null, null);
            } else {
                TextBox box =
                    new TextBox(webform, styleElement, textNode, contentChars, content, source,
                        begin, end, fg, bg, decoration, metrics, hidden);
                addToLineBox(context, box, null, null);
            }

            return;
        }

        while (begin < len) {
            //char c = content.charAt(begin);
            if (Character.isWhitespace(content.charAt(begin))) {
                int spaceBegin = begin;
                begin++;

                // Skip remaining space characters
                while ((begin < len) && Character.isWhitespace(content.charAt(begin))) {
                    begin++;
                }

                // XXX What if the linebox previous box is a space box?
                // if so there's nothing to do since the linebox
                // should never allow too consecutive space boxes...
                // However... I need to extend model range to include
                // my current range... Can this ever happen?
                SpaceBox box =
                    new SpaceBox(webform, styleElement, textNode, content, source, spaceBegin,
                        begin, fg, bg, decoration, metrics, hidden);
                addToLineBox(context, box, null, null);
            }

            // Find end of word
            end = begin;

            while ((end < len) && !Character.isWhitespace(content.charAt(end))) {
                end++;
            }

            if (end > begin) {
                if (contentChars == null) {
                    contentChars = content.toCharArray(); // XXX share among views?
                }

                TextBox box =
                    new TextBox(webform, styleElement, textNode, contentChars, content, source,
                        begin, end, fg, bg, decoration, metrics, hidden);
                addToLineBox(context, box, null, null);
                begin = end;
            } else {
                break;
            }
        }
    }

    /**
     * Recompute the layout. Once the layout routine gets to a point
     * where the child layout matches the computed layout, it will leave
     * that tree alone.  Thus, only the portions of the layout below
     * this box that need to be recomputed are updated
     *
     * @param cx X position of containing block
     * @param cy Y position of containing block
     * @param cw Width of containing block
     * @param ch Height of containing block
     */
    public void relayout(FormatContext context) {
        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox box = getBox(i);

            if (!box.isPlaceHolder()) {
                if ((box.getBoxType() == BoxType.LINEBOX) ||
                        
                    // Note: This is a bit inefficient: we will be positioning
                    // the boxes more than once while there are floats in effect.
                    // However, we need to ensure that boxes are at least roughly
                    // positioned such that the "is floating box overlapping?" code
                    // can compute relative distances between children of this box
                    // and a common ancestor with a floating box still in effect.
                    // And we can't simply move all positioning up to this point
                    // because accurate block positioning requires the margin to
                    // be computed, which isn't possible until vertical distances
                    // have been computed (which has to be done after the children
                    // have been laid out)
                    (box.getBoxType().isNormalFlow() && (context.floats != null))) {
                    positionBox(box, context);
                }

                layoutChild(box, context, true);

//                if ((box.getBoxType() != BoxType.LINEBOX) &&
//                        (box.isBlockLevel() || box.getBoxType().isAbsolutelyPositioned())) {
                // XXX #113117 To be sure also the normal flow elements have position set.
                if ((box.getBoxType() != BoxType.LINEBOX)
                && (box.isBlockLevel() || box.getBoxType().isAbsolutelyPositioned() || box.getBoxType().isNormalFlow())) {
                    positionBox(box, context);
                }
            }
        }
    }

    /** @todo: make this a CssBox method instead - e.g. have relayout()
     * do this, and have relayout on children call super, then recurse! XXX
     * NO! This is passing values (such as the containing block) to the
     * child, that would have to be done by the parent first!
     */
    protected void layoutChild(CssBox box, FormatContext context, boolean handleChildren) {
        // Set new containing block size
        setContainingBlock(box, context);

        // Set up margin values etc.
        // XXX NO! This should be done at cration time, not layout time!
        // Oh wait - layout may write values into AUTO-fields.... hm....
        // add effectiveLeft/Right (perhaps call them "used" as in the spec)
        // and only mutate those from computeHorizontal/VerticalLengths
        box.initialize();

        box.computeHorizontalLengths(context);

        if (box.contentWidth != AUTO) {
            box.width =
                box.leftBorderWidth + box.leftPadding + box.contentWidth + box.rightPadding +
                box.rightBorderWidth;
        } else {
            box.width = UNINITIALIZED;
        }

        boolean abs = box.getBoxType().isAbsolutelyPositioned();
        List<FloatingBoxInfo> oldFloats = null;
        boolean oldFloating = false;

        if (abs) {
            // Absolute positioned children are not affected by floating boxes
            oldFloats = context.floats;
            oldFloating = context.floating;
            context.floats = null;
            context.floating = false;
        }
        
        // XXX #109564 This seems to caused the issue, but what it could cause now?
//        if(getBoxType() == BoxType.FLOAT) {
//            oldFloats = context.floats;
//            context.floats = null;
//        }

        if (handleChildren) {
            // Lay out the children now that we've established a containing
            // block and have constrained the content width if necessary.
            box.relayout(context);
        }

        // XXX #109564 This seems to caused the issue, but what it could cause now?
//        if(getBoxType() == BoxType.FLOAT) {
//            context.floats = oldFloats;
//        }

        if (abs) {
            context.floats = oldFloats;
            context.floating = oldFloating;
        }

        box.computeVerticalLengths(context);
        box.height =
            box.topBorderWidth + box.topPadding + box.contentHeight + box.bottomPadding +
            box.bottomBorderWidth;
        
        if (handleChildren && box instanceof ContainerBox) {
            //only now, when we calculated the height, we can move the relatives
            ((ContainerBox)box).finishAllRelatives(context);
        }
    }
    
    //to move the relatives, see if a child is a linebox
    void finishAllRelatives(FormatContext context) {
        for(int i = 0; i < getBoxCount(); i++) {
            CssBox box = getBox(i);
            if(box instanceof LineBoxGroup) {
                ((LineBoxGroup)box).finishRelatives(context);
            }
        }
    }

    /** Very similar to relayout(context) but does not compute
     * vertical dimensions, and does not position the boxes.
     * Used to initialize box dimensions for computation
     * of minimum widths when we're computing minimum widths
     * for table cells, etc.
     */
    protected void initializeHorizontalWidths(FormatContext context) {
        super.initializeHorizontalWidths(context);

        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox box = getBox(i);

            // We don't care about absolute/fixed children!
            if (box.getBoxType().isAbsolutelyPositioned()) {
                continue;
            }

            box.initializeHorizontalWidths(context);
        }
    }

    /** Set the containing block for the given box.
     * See section 10.1 of the CSS2.1 spec for details.
     */
    void setContainingBlock(CssBox box, FormatContext context) {
        // Containing Block Box - See CSS21 section 10.1 for logic.
        BoxType boxType = box.getBoxType();

        if ((boxType == BoxType.STATIC) || (boxType == BoxType.RELATIVE) // What about floats? They aren't mentioned in section 10.1, so I assume
                // they mean that floats do have a "static" position property
                 ||(boxType == BoxType.FLOAT)) {
            // if the element's position is 'relative' or 'static',
            // the containing block is formed by the content edge of
            // the nearest block-level, table cell or inline-block
            // ancestor box.
            CssBox cbb = box.getParent();

            while ((cbb != null) && !(cbb instanceof FrameBox) &&
                    !cbb.isBlockLevel() &&
                    /*floats and relatives are part of normal flow, so*/
                    !((boxType == BoxType.FLOAT || boxType == BoxType.RELATIVE)  
                    && cbb.getBoxType().isAbsolutelyPositioned())) {
                cbb = cbb.getParent();
            }

            if (cbb.contentWidth != AUTO) {
                box.containingBlockWidth = cbb.contentWidth;
            } else {
                box.containingBlockWidth =
                    cbb.containingBlockWidth - cbb.leftMargin - cbb.leftBorderWidth -
                    cbb.leftPadding - cbb.rightPadding - cbb.rightBorderWidth - cbb.rightMargin;
            }

            if (cbb.contentHeight != AUTO) {
                box.containingBlockHeight = cbb.contentHeight;
            } else {
                box.containingBlockHeight =
                    cbb.containingBlockHeight - cbb.topMargin - cbb.topBorderWidth -
                    cbb.topPadding - cbb.bottomPadding - cbb.bottomBorderWidth - cbb.bottomMargin;
            }

            // XXX how does x interact with box hierarchies (where children
            // are relative to their parents) and how about margins? effective
            // margins?
            box.containingBlockX =
                cbb.containingBlockX + cbb.leftMargin + cbb.leftBorderWidth + cbb.leftPadding;
            box.containingBlockY =
                cbb.containingBlockY + cbb.topMargin + cbb.topBorderWidth + cbb.topPadding;
        } else if (boxType == BoxType.LINEBOX) {
            // Line boxes simply keep their parent's containing blocks

            /* No - that doesn't work - for example, a <div width=70%>
             * may have a linebox - that line box should inherit the
             * content width - 70% - not the div's containing block
             * which is 100%...
            box.containingBlockX = containingBlockX;
            box.containingBlockY = containingBlockY;
            box.containingBlockWidth = containingBlockWidth;
            box.containingBlockHeight = containingBlockHeight;
             */
            /*
            box.containingBlockX = 0; //leftBorderWidth+leftPadding;
            box.containingBlockY = 0; //topBorderWidth+topPadding;
            // XXX is contentWidth initialized?
            box.containingBlockWidth = contentWidth-rightBorderWidth-rightPadding-leftBorderWidth-leftPadding;
            box.containingBlockHeight = contentHeight-bottomBorderWidth-bottomPadding-topBorderWidth-topPadding;
             */
            box.containingBlockX = leftMargin + leftBorderWidth + leftPadding;
            box.containingBlockY = topMargin + topBorderWidth + topPadding;

            // XXX is contentWidth initialized?
            box.containingBlockWidth = contentWidth;
            box.containingBlockHeight = contentHeight;
        } else if (boxType == BoxType.ABSOLUTE) {
            CssBox cbb = box.getParent();

            // Find nearest ancestor with a 'position' of 'absolute', 
            // 'relative' or 'fixed'
            while ((cbb != null) &&
                    !(cbb.getBoxType().isPositioned() || (cbb.tag == HtmlTag.FRAME))) {
                cbb = cbb.getParent();
            }

            // We don't have a positioned parent: use the viewport
            if (cbb == null) {
                box.containingBlockX = 0;
                box.containingBlockY = 0;
                box.containingBlockWidth = context.initialWidth;
                box.containingBlockHeight = context.initialHeight;
                box.setPositionedBy(context.initialCB);
            } else if (cbb.isBlockLevel()) {
                /*
                // The containing block is formed by the padding edge of
                // the ancestor
                // XXX what if the ancestor has set a width? If so,
                // I should use it, shouldn't I?
                box.containingBlockWidth =
                    cbb.containingBlockWidth - cbb.leftMargin - cbb.leftBorderWidth -
                    cbb.rightBorderWidth - cbb.rightMargin;

                // XXX what about effective margins etc.?
                box.containingBlockHeight =
                    cbb.containingBlockHeight - cbb.topMargin - cbb.topBorderWidth -
                    cbb.bottomBorderWidth - cbb.bottomMargin;

                // XXX how does x interact with box hierarchies (where
                // children are relative to their parents) and how about
                // margins? effective margins?
                box.containingBlockX = cbb.containingBlockX + cbb.leftMargin + cbb.leftBorderWidth;
                box.containingBlockY = cbb.containingBlockY + cbb.topMargin + cbb.topBorderWidth;
                 */
                box.containingBlockWidth = cbb.contentWidth + leftPadding + rightPadding;
                box.containingBlockHeight = cbb.contentHeight + topPadding + bottomPadding;
                box.containingBlockX = leftBorderWidth;
                box.containingBlockY = topBorderWidth;
            } else {
                // LTR assumption: the top and left of the containing
                // block are the top and left content edges of the first
                // box generated by the ancestor, and the bottom and
                // right are the bottom and right content edges of the
                // last box of the ancestor.
                // XXX revisit this code once I have redone the way
                // inline boxes are handled.
                // XXX I don't quite understand this rule. Or if I do,
                // it will be difficult to compute - we don't know the
                // last box of the ancestor yet - we haven't added it!
                // For now, just use the containing block assigned to
                // the ancestor.
                LineBoxGroup lbg = null;

                if (box.getParent() instanceof LineBoxGroup) {
                    lbg = (LineBoxGroup)box.getParent();
                } else if ((box.getParent() != null) &&
                        box.getParent().getParent() instanceof LineBox) {
                    lbg = (LineBoxGroup)box.getParent().getParent();
                } else {
                    // XXX #6464191 TODO How should be this handled?
                    // In this case box.getParent() was ContainerBox of the Group Panel.
//                    assert false;
                }

                if (lbg != null) {
                    box.containingBlockX = lbg.containingBlockX;
                    box.containingBlockY = lbg.containingBlockY;
                    box.containingBlockWidth = lbg.containingBlockWidth;
                    box.containingBlockHeight = lbg.containingBlockHeight;
                }
            }
        } else if (boxType == BoxType.FIXED) {
            // If the element has 'position: fixed', the containing
            // block is established by the viewport.  
            box.containingBlockX = 0;
            box.containingBlockY = 0;
            box.containingBlockWidth = context.initialWidth;
            box.containingBlockHeight = context.initialHeight;
            box.setPositionedBy(context.initialCB);
        } else {
            assert false : boxType;
        }
    }

    /** Position the box - set its x and y values relative to the
     * parent's border edge. Positioning depends on the box' BoxType.
     * This method should get called after margins etc. have been
     * computed properly (e.g. all AUTO values must be resolved).
     */
    protected void positionBox(CssBox box, FormatContext context) {
        // LineBoxes will do special handling of inline boxes; this
        // assert ensures that the LineBox overrides this method and
        // handles it correctly.
        //assert box.isBlockLevel();
        BoxType type = box.getBoxType();

        if ((type == BoxType.STATIC) || (type == BoxType.LINEBOX)) {
            positionBlockBox(box, context);
        } else if (type == BoxType.ABSOLUTE) {
            positionAbsoluteBox(box);
        } else if (type == BoxType.FLOAT) {
            positionFloatBox(box);
        } else if (type == BoxType.RELATIVE) {
            // XXX hm, shouldn't relative boxes be allowed in inline ctx
            // too?  The LineBox needs to call this method as well!
            positionRelativeBox(box, context);
        } else if (type == BoxType.FIXED) {
            positionFixedBox(box, context);
        } else {
            assert false : type;
        }
    }

    private int calculateMargin(int prevMargin, int boxMargin) {
            // Handle negative offsets
            if ((prevMargin >= 0) && (boxMargin >= 0)) {
                // Normal case
                //  The larger of adjacent margin values is used. 
                return Math.max(prevMargin, boxMargin);

                // OLD:
            } else if ((prevMargin < 0) && (boxMargin < 0)) {
                // If the adjacent margins are all negative, the larger
                // of the negative values is used.
                // XXX this is not how I re-read the spec; it says "If
                // there are no positive margins, the absolute maximum
                // of the negative adjoining margins is deducted from
                // zero."   So I take abs
                //margin = Math.max(-prevMargin, -boxMargin);
                return Math.min(prevMargin, boxMargin);
            } else {
                // If positive and negative vertical margins are
                // adjacent, the value should be collapsed thus: the
                // largest of the negative margin values should be
                // subtracted from the largest positive margin value.
                if ((prevMargin >= 0) && (boxMargin < 0)) {
                    return prevMargin + boxMargin;
                } else {
                    assert (prevMargin < 0) && (boxMargin >= 0);
                    return boxMargin + prevMargin;
                }
            }
    }
    /** Position a box in block formatting context
     * @todo See computeEffectiveMargins and combine these
     */
    private void positionBlockBox(CssBox box, FormatContext context) {
        CssBox parentBox = box.getParent();
        assert parentBox != null;

        // 9.4.1: each box's left outer edge touches the left edge of the
        // containing block
        // This means that the distance between the child's border
        // edge and the parent's border edge, which we measure x relative to,
        // is parent padding plus child margin (since the parent padding
        // edge is aligned with the child outer edge. To get to the parent
        // border edge from the parent padding edge we add the parent padding,
        // and to get to the child border edge from the child outer edge,
        // we add the child margin.)
        box.setX(parentBox.leftPadding + parentBox.leftBorderWidth);

        // XXX Tables are block tags -- sorta -- but need different behavior
        // here. In particular, they should also be indented by floating
        // boxes:
        // compute y first, set to targetY
        //int leftEdge = context.getLeftEdge(box, targetY, box.getHeight());
        // TODO: look for "clear" property to clear floats - see section
        // 9.5.2 of the CSS2.1 spec
        // Collapsing vertical margins: specified in 8.3.1
        CssBox prevNormalBox = box.getPrevNormalBlockBox();

        if (prevNormalBox != null) {
            if(prevNormalBox instanceof LineBoxGroup) {
                prevNormalBox.computeVerticalLengths(context);
            }
            int newY = prevNormalBox.getY() + prevNormalBox.getHeight() + 
                            calculateMargin(prevNormalBox.getCollapsedBottomMargin(),
                            box.getCollapsedTopMargin());
            box.setY(newY);
            box.effectiveTopMargin = 0;
        } else {
            // Else: this is the first normal block box we're adding;
            // this means we need to consider collapsing the nested
            // margins.
            // Margins should only collapse if there is no padding or
            // border separating them
            if ((parentBox.topBorderWidth == 0) && (parentBox.topPadding == 0)) {
                // Yes - collapse margins!
                // Collapse our parentBox.topMargin with box.topMargin -
                // except since this has a cumulative effect, gotta
                // collapse with the EFFECTIVE margin instead!
                // Handle negative offsets
                //int margin;
                int margin = parentBox.getCollapsedTopMargin();

                /*
                //int parentMargin = parentBox.topMargin;
                //int boxMargin = box.effectiveTopMargin;
                int parentMargin = parentBox.getCollapsedTopMargin();
                int boxMargin = box.getcollapsedMargin();

                if (parentMargin >= 0 && boxMargin >= 0) {
                    // Normal case
                    //  The larger of adjacent margin values is used.
                    margin =
                        Math.max(parentMargin, boxMargin);
                    // OLD:
                } else if (
                    parentMargin < 0 && boxMargin < 0) {

                    // If the adjacent margins are all negative, the larger
                    // of the negative values is used.

                    // XXX this is not how I re-read the spec; it says "If
                    // there are no positive margins, the absolute maximum
                    // of the negative adjoining margins is deducted from
                    // zero."   So I take abs
                    //margin = Math.max(-parentMargin, -boxMargin);
                    margin =
                        Math.min(parentMargin, boxMargin);
                } else {
                    // If positive and negative vertical margins are
                    // adjacent, the value should be collapsed thus: the
                    // largest of the negative margin values should be
                    // subtracted from the largest positive margin value.
                    if (parentMargin >= 0
                        && boxMargin < 0) {
                        margin = parentMargin + boxMargin;
                    } else {
                        assert parentMargin < 0
                            && boxMargin >= 0;
                        margin = boxMargin + parentMargin;
                    }
                }
                */
                //6406309 fix
                //parentBox.effectiveTopMargin = margin;
                box.effectiveTopMargin = 0; // collapsed

                // All coordinates in this box will be relative to
                // the effectiveTopMargin
                // box.setY(0); // Since we've collapsed the box with our
                // own top margin, the box' border edge is located at
                // the same position as the paren'ts border edge.
                box.setY(parentBox.topBorderWidth);
            } else {
                // No, don't collapse margins
                box.setY(parentBox.topPadding + parentBox.topBorderWidth //);
                     +box.effectiveTopMargin); // XXX Already added?

                parentBox.effectiveTopMargin = parentBox.topMargin;
                box.effectiveTopMargin = 0;
            }
        }

        //in case the box is clear box, we also need to look for a previous 
        //float box
        if(box.isClearBox()/* && LineBoxGroup.findClearContainer(box) == null*/) {
            CssBox prevFloatBox = context.getPrevFloatingForClear(box);
            if (prevFloatBox != null) {
                int newY = context.adjustY(prevFloatBox.getHeight() + 
                        prevFloatBox.bottomMargin + prevFloatBox.topMargin, 
                        prevFloatBox, this) + box.topMargin;
                if(newY > box.getY()) {
                    box.setY(newY);
                }
            }
 
        }

        /*
        if ((context.floats != null) && (box.getBoxType() != BoxType.LINEBOX) &&
                (box.getElement() != null)) {
//            Value clear = CssLookup.getValue(box.getElement(), XhtmlCss.CLEAR_INDEX);
            CssValue cssClear = CssProvider.getEngineService().getComputedValueForElement(box.getElement(), XhtmlCss.CLEAR_INDEX);

//            if (clear != CssValueConstants.NONE_VALUE) {
            if (!CssProvider.getValueService().isNoneValue(cssClear)) {
//                box.clearTop(context, clear);
                box.clearTop(context, cssClear);
            }
        }
         */
    }

    /** Position a box in block formatting context */
    private void positionRelativeBox(CssBox box, FormatContext context) {
        positionBlockBox(box, context);
        box.setLocation(box.getX() + box.left, box.getY() + box.top);
    }

    /** Position a box by absolute coordinates (where the coordinates
     * are relative to the containing block
     */
    private void positionAbsoluteBox(CssBox box) {
        box.setLocation(box.left + leftBorderWidth, box.top + topBorderWidth);
    }

    /** Position a box that is fixed to the viewport */
    private void positionFixedBox(CssBox box, FormatContext context) {
        ViewportBox vb = context.initialCB;
        JViewport viewport = vb.getViewport();

        if (viewport != null) {
            Point p = viewport.getViewPosition();
            box.setLocation(box.left + p.x, box.top + p.y);
        } else {
            box.setLocation(box.left, box.top);
        }
    }

    /** Position a box that is floated left or right
     * <p>
     * See section 9.5.1 for details.
     * </p>
     */
    private void positionFloatBox(CssBox box) {
        // Most of the work done in the LineBoxGroup!
        CssBox parentBox = box.getParent();
        assert parentBox != null;

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
                    new IllegalStateException("Unexpected value, cssFloating=" + cssFloating));
            return;
        }

        // 9.4.1: each box's left outer edge touches the left edge of the
        // containing block
        // This means that the distance between the child's border
        // edge and the parent's border edge, which we measure x relative to,
        // is parent padding plus child margin (since the parent padding
        // edge is aligned with the child outer edge. To get to the parent
        // border edge from the parent padding edge we add the parent padding,
        // and to get to the child border edge from the child outer edge,
        // we add the child margin.)
        if (leftSide) {
            box.setX(parentBox.leftPadding + parentBox.leftBorderWidth);

            // + box.leftMargin); // Already added?
        } else {
//            System.out.println("WRONG RIGHT POSITIONING!");
//            box.setX(parentBox.leftPadding + parentBox.leftBorderWidth);
            // XXX #99707 Based on the left float positioning above.
            box.setX(parentBox.width - parentBox.rightPadding - parentBox.rightBorderWidth);
        }

        // TODO: look for "clear" property to clear floats - see section
        // 9.5.2 of the CSS2.1 spec
        // Collapsing vertical margins: specified in 8.3.1
        CssBox prevBox = box.getPrevNormalBox();

        if (prevBox != null) {
            // We have a previous box - place the new box below it,
            // offset by the collapsed margins -
            // Handle negative offsets
            int margin;

            //int prevMargin = prevBox.effectiveBottomMargin;
            //int boxMargin = box.effectiveTopMargin;
            int prevMargin = prevBox.getCollapsedBottomMargin();
            int boxMargin = box.getCollapsedTopMargin();

            if ((prevMargin >= 0) && (boxMargin >= 0)) {
                // Normal case
                //  The larger of adjacent margin values is used. 
                margin = Math.max(prevMargin, boxMargin);

                // OLD:
            } else if ((prevMargin < 0) && (boxMargin < 0)) {
                // If the adjacent margins are all negative, the larger
                // of the negative values is used.
                // XXX this is not how I re-read the spec; it says "If
                // there are no positive margins, the absolute maximum
                // of the negative adjoining margins is deducted from
                // zero."   So I take abs
                //margin = Math.max(-prevMargin, -boxMargin);
                margin = Math.min(prevMargin, boxMargin);
            } else {
                // If positive and negative vertical margins are
                // adjacent, the value should be collapsed thus: the
                // largest of the negative margin values should be
                // subtracted from the largest positive margin value.
                if ((prevMargin >= 0) && (boxMargin < 0)) {
                    margin = prevMargin + boxMargin;
                } else {
                    assert (prevMargin < 0) && (boxMargin >= 0);
                    margin = boxMargin + prevMargin;
                }
            }

            box.setY( /*parentBox.topBorderWidth*/
                prevBox.getY() + prevBox.getHeight() + margin);
            box.effectiveTopMargin = 0;
        } else {
            // Else: this is the first normal block box we're adding;
            // this means we need to consider collapsing the nested
            // margins.
            // Margins should only collapse if there is no padding or
            // border separating them
            if ((parentBox.topBorderWidth == 0) && (parentBox.topPadding == 0)) {
                // Yes - collapse margins!
                // Collapse our parentBox.topMargin with box.topMargin -
                // except since this has a cumulative effect, gotta
                // collapse with the EFFECTIVE margin instead!
                // Handle negative offsets
                //int margin;
                int margin = parentBox.getCollapsedTopMargin();

                /*
                //int parentMargin = parentBox.topMargin;
                //int boxMargin = box.effectiveTopMargin;
                int parentMargin = parentBox.getCollapsedTopMargin();
                int boxMargin = box.getcollapsedMargin();

                if (parentMargin >= 0 && boxMargin >= 0) {
                    // Normal case
                    //  The larger of adjacent margin values is used.
                    margin =
                        Math.max(parentMargin, boxMargin);
                    // OLD:
                } else if (
                    parentMargin < 0 && boxMargin < 0) {

                    // If the adjacent margins are all negative, the larger
                    // of the negative values is used.

                    // XXX this is not how I re-read the spec; it says "If
                    // there are no positive margins, the absolute maximum
                    // of the negative adjoining margins is deducted from
                    // zero."   So I take abs
                    //margin = Math.max(-parentMargin, -boxMargin);
                    margin =
                        Math.min(parentMargin, boxMargin);
                } else {
                    // If positive and negative vertical margins are
                    // adjacent, the value should be collapsed thus: the
                    // largest of the negative margin values should be
                    // subtracted from the largest positive margin value.
                    if (parentMargin >= 0
                        && boxMargin < 0) {
                        margin = parentMargin + boxMargin;
                    } else {
                        assert parentMargin < 0
                            && boxMargin >= 0;
                        margin = boxMargin + parentMargin;
                    }
                }
                */
                parentBox.effectiveTopMargin = margin;
                box.effectiveTopMargin = 0; // collapsed

                // All coordinates in this box will be relative to
                // the effectiveTopMargin
                // box.setY(0); // Since we've collapsed the box with our
                // own top margin, the box' border edge is located at
                // the same position as the paren'ts border edge.
                box.setY(parentBox.topBorderWidth);
            } else {
                // No, don't collapse margins
                box.setY(parentBox.topPadding + parentBox.topBorderWidth //);
                     +box.effectiveTopMargin); // XXX Already added?

                parentBox.effectiveTopMargin = parentBox.topMargin;
                box.effectiveTopMargin = 0;
            }
        }
    }

    /** Returns true if this box represents an inline span.  This is true for
      * inline boxes that are not block tags, or are not inline tags that are
      * absolutely positioned, or are replaced.
     */
    private boolean isSpan() {
        return inline && !boxType.isAbsolutelyPositioned() && !replaced;
//        return false; // TEMP
    }
    
    private static boolean isTextualBox(CssBox box) {
        if (box == null) {
            return false;
        }
        BoxType boxType = box.getBoxType();
        return boxType == BoxType.TEXT || boxType == BoxType.SPACE || boxType == BoxType.LINEBREAK;
    }
    
    /**
     * NOTE: prevBox and nextBox refer to the siblings for
     * the new LineBoxGroup to be created (if any), not where
     * within the LineBoxGroup to add the inline box!
     */
    private void addToLineBox(CreateContext context, CssBox ibox, CssBox prevBox, CssBox nextBox) {
        // Inline tags like <span> in flow context should simply include
        // the children as normal children; the LineBoxGroup will organize
        // them into LineBoxes
        
        // XXX #105679 This doesn't know how to deal with the 'textual' boxes, adding those normally.
        if (isSpan() && !isTextualBox(ibox)) {
            addBox(ibox, prevBox, nextBox);
        } else {
            if (context.lineBox == null) {
                FontMetrics metrics = context.metrics;

                if (ibox.getBoxType() == BoxType.TEXT) {
                    metrics = ((TextBox)ibox).getMetrics();
                } else if (ibox.getBoxType() == BoxType.SPACE) {
                    metrics = ((SpaceBox)ibox).getMetrics();
                }

                context.lineBox = new LineBoxGroup(webform, getElement(), metrics);

                getBlockBox().addBox(context.lineBox, prevBox, nextBox);
            }

            context.lineBox.addBox(ibox, null, null);
        }
    }

    /**
     * Finish the current line box we're working on; a new
     * addToLineBox call will generate a new line.
     */
    void finishLineBox(CreateContext context) {
        if (context.lineBox == null) {
            return;
        }

        if (context.lineBox.isEmpty()) {
            return;
        }

        context.lineBox = null;
    }

    public int getPrefMinWidth() {
        if (inline) {
//            Value val = CssLookup.getValue(getElement(), XhtmlCss.WHITE_SPACE_INDEX);
            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(getElement(), XhtmlCss.WHITE_SPACE_INDEX);
//            if ((val == CssValueConstants.PRE_VALUE) || (val == CssValueConstants.NOWRAP_VALUE)) {
            if (CssProvider.getValueService().isPreValue(cssValue)
            || CssProvider.getValueService().isNoWrapValue(cssValue)) {
                return getPrefWidth();
            }
        }

        int largest = 0;
        int n = getBoxCount();

        for (int i = 0; i < n; i++) {
            CssBox child = getBox(i);

            if (child.getBoxType().isAbsolutelyPositioned()) {
                continue;
            }

            int min = child.getPrefMinWidth();

            if (min > largest) {
                largest = min;
            }
        }

        if (leftMargin != AUTO) {
            largest += leftMargin;
        }

        if (rightMargin != AUTO) {
            largest += rightMargin;
        }

        // Borders and padding can't be left auto, can they?
        largest += (leftBorderWidth + leftPadding + rightBorderWidth + rightPadding);

        int curr = super.getPrefMinWidth();

        if (curr > largest) {
            largest = curr;
        }

        return largest;
    }

    public int getPrefWidth() {
        int largest = 0;
        int n = getBoxCount();

        if (inline && !boxType.isAbsolutelyPositioned()) {
            // Let the line box compute the size of these children
            CssBox curr = getParent();

            while ((curr != null) && !(curr instanceof LineBoxGroup)) {
                curr = curr.getParent();
            }

            if (curr != null) {
                largest = ((LineBoxGroup)curr).getPrefWidth(boxes);
            } else {
                // Shouldn't happen - this is just for safety right now
                // Inline tag: add up all the children and use that
                for (int i = 0; i < n; i++) {
                    CssBox child = getBox(i);

                    if (child.getBoxType().isAbsolutelyPositioned()) {
                        continue;
                    }

                    // XXX does not properly handle LineBox.LINEBREAK
                    largest += child.getPrefWidth();
                }
            }
        } else {
            // Block tag: find the widest child and use that
            for (int i = 0; i < n; i++) {
                CssBox child = getBox(i);

                if (child.getBoxType().isAbsolutelyPositioned()) {
                    continue;
                }

                int min = child.getPrefWidth();

                if (min > largest) {
                    largest = min;
                }
            }
        }

        if (leftMargin != AUTO) {
            largest += leftMargin;
        }

        if (rightMargin != AUTO) {
            largest += rightMargin;
        }

        // Borders and padding can't be left auto, can they?
        largest += (leftBorderWidth + leftPadding + rightBorderWidth + rightPadding);

        int curr = super.getPrefWidth();

        if (curr > largest) {
            largest = curr;
        }

        return largest;
    }

    protected boolean hasNormalBlockLevelChildren() {
        // XXX This gets tricky. Children boxes may be anonymous
        // block boxes - but I haven't been creating those!
        // Figure out how to handle this....
        int n = getBoxCount();

        for (int i = 0; i < n; i++) {
            CssBox child = getBox(i);

            if (child.getBoxType().isNormalFlow()) {
                if (!child.isInlineBox()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Notify the parent that the given child has been resized.
     * This might cause a reflow.
     * @return The topmost box in the hierarchy which had its size changed.
     */
    protected CssBox notifyChildResize(CssBox child, FormatContext context) {
        // Update positions to accomodate the new dimensions
        // of the given child
        // We don't check the parent pointer because pageBox should
        // override this method
        if (child.getParentIndex() != -1) {
            positionBox(child, context);
        }

        // If this is an absolutely positioned box, the parent has
        // no interest in our dimensions so we don't need to
        // propagate the size change.
        //if (boxType.isAbsolutelyPositioned()) {
        if (child.getBoxType().isAbsolutelyPositioned()) {
            return child;
        }

        // Move siblings further down in the flow
        if (child.getBoxType().isNormalFlow()) {
            // Adjust the remaining normal flow boxes too
            int i = child.getParentIndex() + 1;
            int n = getBoxCount();

            for (; i < n; i++) {
                CssBox sibling = getBox(i);

                if (sibling.getBoxType().isNormalFlow()) {
                    positionBox(sibling, context);
                }
            }
        }

        ContainerBox parent = getParent();
        if (parent == null) {
            return this; // page box: no furhter propagation is necessary

            // its contentHeight/height is computed from the extents,
            // which we will update manually
        }

        int oldWidth = contentWidth;
        int oldHeight = contentHeight;
        parent.layoutChild(this, context, false);

        // What about left/top, etc.?
        // Have the dimensions changed? If not, we're done
        if ((contentWidth == oldWidth) && (contentHeight == oldHeight)) {
            // TODO what if the effective margin has changed?
            // That might require us to propagate the change upwards
            // too!

            /*
            if (child.getParentIndex() != -1) {
                positionBox(child, context);
            }
             */
            return child;
        }

        // Otherwise, propagate the dimension change notification
        // to the parent so it can adjust its positions
        return parent.notifyChildResize(this, context);
    }

    protected void paintBox(Graphics g, int x, int y, int w, int h) {
        super.paintBox(g, x, y, w, h);

//        if (grid && GridHandler.getInstance().grid()) {
//        if (grid && getWebForm().getGridHandler().grid()) {
//        if (grid && GridHandler.getDefault().isGrid()) {
        if (grid && webform.isGridShow()) {
            if (hidden && !(this instanceof PageBox)) { // paint grid for the root pagebox!

                return;
            }

            paintGrid(g, x, y, w, h);
        }
    }

    protected void paintGrid(Graphics g, int x, int y, int w, int h) {
        //long start = System.currentTimeMillis();
        int width = w;
        int height = h;

        // On my U60, this takes roughly 24S-34 ms for the default
        // screensize
        Color oldColor = g.getColor();
        Color gridColor = webform.getColors().gridColor;
        assert gridColor != null;
        g.setColor(gridColor);

//        final int gridWidth = GridHandler.getInstance().getGridWidth();
//        final int gridHeight = GridHandler.getInstance().getGridHeight();
//        final int gridTraceWidth = GridHandler.getInstance().getGridTraceWidth();
//        final int gridTraceHeight = GridHandler.getInstance().getGridTraceHeight();
//        final int gridOffset = GridHandler.getInstance().getGridOffset();
//        GridHandler gridHandler = getWebForm().getGridHandler();
//        GridHandler gridHandler = GridHandler.getDefault();
//        final int gridWidth = gridHandler.getGridWidth();
//        final int gridHeight = gridHandler.getGridHeight();
//        final int gridTraceWidth = gridHandler.getGridTraceWidth();
//        final int gridTraceHeight = gridHandler.getGridTraceHeight();
//        final int gridOffset = gridHandler.getGridOffset();
        final int gridWidth = webform.getGridWidth();
        final int gridHeight = webform.getGridHeight();
        final int gridTraceWidth = webform.getGridTraceWidth();
        final int gridTraceHeight = webform.getGridTraceHeight();
        final int gridOffset = webform.getGridOffset();

        // Draw a plain grid (one dot on each gridsize boundary
        //for (int x = gridOffset; x < width; x += gridSize) {
        //    for (int y = gridOffset; y < height; y += gridSize) {
        //        //g.drawLine(x, y, x, y);
        //        // Is this faster?
        //        g.fillRect(x, y, 1, 1);
        //    }
        //}
        int xstart = gridOffset;

        // Draw grid with smaller trace lines along the axes
        for (int xp = xstart; xp < width; xp += gridWidth) {
            for (int yp = gridTraceHeight; yp < height; yp += gridTraceHeight) {
                //g.drawLine(xp, yp, xp, yp);
                // Is this faster?
                g.fillRect(x + xp, y + yp, 1, 1);
            }
        }

        int ystart = gridOffset;

        // This is going to redraw all the pixels on the gridsize*gridsize
        // grid - I can count gridTrace's and skip these.  TODO.
        for (int xp = gridTraceWidth; xp < width; xp += gridTraceWidth) {
            for (int yp = ystart; yp < height; yp += gridHeight) {
                //g.drawLine(xp, yp, xp, yp);
                // Is this faster?
                g.fillRect(x + xp, y + yp, 1, 1);
            }
        }

        g.setColor(oldColor);

        //long end = System.currentTimeMillis();
        //System.out.println("Scan time for grid painting = " + (end-start) + " ms");
    }

    /** The iframe box should be clipped */
    public void paint(Graphics g, int px, int py) {
        if (clipOverflow) {
            g.getClipBounds(sharedClipRect);

            int cx = sharedClipRect.x;
            int cy = sharedClipRect.y;
            int cw = sharedClipRect.width;
            int ch = sharedClipRect.height;
            g.clipRect(px + leftMargin + getX(), py + effectiveTopMargin + getY(), width, height);
            super.paint(g, px, py);
            g.setClip(cx, cy, cw, ch);
        } else {
            super.paint(g, px, py);
        }
    }

    /** Prints a listing of this container box.
     * @see java.awt.Container#list(java.io.PrintStream, int) */
    public void list(PrintStream out, int indent) {
	super.list(out, indent);
        
        CssBox[] boxes = getBoxes();
	for (int i = 0 ; i < boxes.length ; i++) {
	    CssBox box = boxes[i];
	    if (box != null) {
		box.list(out, indent+1);
	    }
	}
    }

    /** Prints a listing of this container box.
     * @see java.awt.Container#list(java.io.PrintWriter, int) */
    public void list(PrintWriter out, int indent) {
	super.list(out, indent);
        
        CssBox[] boxes = getBoxes();
	for (int i = 0 ; i < boxes.length ; i++) {
	    CssBox box = boxes[i];
	    if (box != null) {
		box.list(out, indent+1);
	    }
	}
    }
    
    /** Finds all external form in this box tree. */
    public WebForm[] findExternalForms() {
        List<WebForm> externalForms = new ArrayList<WebForm>();
        CssBox[] boxes = getBoxes();
        for (CssBox box : boxes) {
            if (box instanceof ExternalDocumentBox) {
                ExternalDocumentBox externalDocBox = (ExternalDocumentBox)box;
                WebForm externalForm = externalDocBox.getExternalForm();
                if (externalForm != null) {
                    externalForms.add(externalForm);
                }
            } else if (box instanceof ContainerBox) {
                ContainerBox containerBox = (ContainerBox)box;
                WebForm[] webForms = containerBox.findExternalForms();
                externalForms.addAll(Arrays.asList(webForms));
            }
        }
        return externalForms.toArray(new WebForm[externalForms.size()]);
    }
    
    /** Determine if the given element represents an inline tag
     * @todo Move to Utilities ?
     */
    private /*public*/ static boolean isInlineTag(CssValue cssDisplay, Element element, HtmlTag tag) {
////        if (display == CssValueConstants.NONE_VALUE) {
//        if (CssProvider.getValueService().isNoneValue(cssDisplay)) {
//            return false;
//        }
//
////        if ((display == CssValueConstants.BLOCK_VALUE) ||
////                (display == CssValueConstants.LIST_ITEM_VALUE) ||
////                (display == CssValueConstants.TABLE_VALUE) ||
////                (
////            /* These are not always block
////            display == CssValueConstants.COMPACT_VALUE ||
////            display == CssValueConstants.RUN_IN_VALUE ||
////             */
////            display == CssValueConstants.INLINE_BLOCK_VALUE)) {
//        if (CssProvider.getValueService().isBlockValue(cssDisplay)
//        || CssProvider.getValueService().isListItemValue(cssDisplay)
//        || CssProvider.getValueService().isTableValue(cssDisplay)
//        || CssProvider.getValueService().isInlineBlockValue(cssDisplay)){
//            return false;
////        } else if (display == CssValueConstants.INLINE_VALUE) {
//        } else if (CssProvider.getValueService().isInlineValue(cssDisplay)) {
//            return true;
//
//            // TODO: Handle rest of constants appropriately.
//            // The "inline" boolean flag is too simplistic; we should
//            // store the formatting type here and do the right type
//            // of layout
//
//            /*
//              CssValueConstants.COMPACT_VALUE,
//              CssValueConstants.INLINE_TABLE_VALUE,
//              CssValueConstants.MARKER_VALUE,
//              CssValueConstants.RUN_IN_VALUE,
//              CssValueConstants.TABLE_VALUE,
//              CssValueConstants.TABLE_CAPTION_VALUE,
//              CssValueConstants.TABLE_CELL_VALUE,
//              CssValueConstants.TABLE_COLUMN_VALUE,
//              CssValueConstants.TABLE_COLUMN_GROUP_VALUE,
//              CssValueConstants.TABLE_FOOTER_GROUP_VALUE,
//              CssValueConstants.TABLE_HEADER_GROUP_VALUE,
//              CssValueConstants.TABLE_ROW_VALUE,
//              CssValueConstants.TABLE_ROW_GROUP_VALUE,
//             */
//        } else {
//            // Else - use tag default
//            if (tag == null) {
//                tag = HtmlTag.getTag(element.getTagName());
//            }
//
//            if (tag != null) {
//                return tag.isInlineTag();
//            }
//        }
//
//        return true;
        return CssProvider.getValueService().isInlineTag(cssDisplay, element, tag);
    }
}

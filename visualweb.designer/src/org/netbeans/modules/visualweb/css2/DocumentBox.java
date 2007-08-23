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

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.designer.CssUtilities;

import javax.swing.JViewport;
import org.netbeans.modules.visualweb.api.designer.DomProvider;

import org.openide.ErrorManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.netbeans.modules.visualweb.designer.DesignerPane;
import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * Represents the document - with a <body> tag. Used to display not only
 * our document page, but iframes as well.
 *
 * @author Tor Norbye.
 */
public abstract class DocumentBox extends ContainerBox {
    // Display statistics such as number of boxes created, time required, etc.
    private static final boolean debugstats = System.getProperty("designer.stats") != null;

    // Statistics
    private static int numBoxes;
    private static int numLeaves;
    private static int maxChildren;
    private static int maxDepth;
    private static int maxElementDepth;
    private static int numElements;
    private static int textNodes;
    private static int textBoxes;
    private static int spaceBoxes;
    private static int blankTextNodes;
    protected FormatContext context;
    protected int layoutWidth;
    protected int layoutHeight;
    protected DesignerPane pane;
    protected JViewport viewport;
    protected int currWidth = -1;
    protected Element body;
    protected int maxWidth = -1;
    protected boolean layoutValid = false;
    protected BoxList fixedBoxes;

    /** Currently scrolled-to x position in the top left corner of the
     * viewport. */
    protected int viewportX;

    /** Currently scrolled-to y position in the top left corner of the
     * viewport. */
    protected int viewportY;

    /** Creates a new instance of PageBox */
    public DocumentBox(DesignerPane pane, WebForm webform, Element body, BoxType boxType,
        boolean inline, boolean replaced) {
        // XXX What do we pass in as a containing block?
        super(webform, body, boxType, inline, replaced);
        this.pane = pane; // XXX do we need this in the document
        this.body = body;

        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }

    /**
     * Create the children. The FrameBox uses its own create context
     * since the document contained within is hidden to the outside,
     * and it gets its own local context for the entire document.
     */
    protected void createChildren(CreateContext context) {
        // TODO instead of checking on the box count, which could be 0
        // for valid reasons, have a dedicated flag here which is
        // invalidated on document edits, etc.
        CreateContext cc;

        if (context != null) {
            cc = new CreateContext(context);
        } else {
            cc = new CreateContext();
        }

        cc.pushPage(webform);

        try {
//            Font font = CssLookup.getFont(body, DesignerSettings.getInstance().getDefaultFontSize());
//            Font font = CssProvider.getValueService().getFontForElement(body, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//            cc.metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            // XXX Missing text.
            cc.metrics = CssUtilities.getDesignerFontMetricsForElement(body, null, webform.getDefaultFontSize());

            super.createChildren(cc);
            fixedBoxes = cc.getFixedBoxes();
        } finally {
            cc.popPage();
        }
    }

    public void relayout(FormatContext context) {
        /*
        if (contentWidth == AUTO) {
            contentWidth = getIntrinsicWidth();
        }
        if (contentHeight == AUTO) {
            contentHeight = getIntrinsicHeight();
        }
        */

        // Note - we don't pass in context.initialCB since 
        // fixed boxes should not be relative to the outer viewport
        // by default
        relayout(null, contentWidth, contentHeight, -1);
    }

    /**
     * Layout the page hierarchy.
     */
    public void relayout(JViewport viewport, int initialWidth, int initialHeight, int wrapWidth) {
        layoutValid = true;

        if (initialWidth == currWidth) {
            return;
        }

        if (initialWidth == Integer.MAX_VALUE) {
            // Intermediate/invalid startup state - don't do layout.
            // We'll soon get a resize with an appropriate size.
            return;
        }

        currWidth = initialWidth;

        // All the layout computations have to use the wrap width for the containing blocks etc
        // to get wrapping, attachments to the right side, etc. to work correctly. However, that
        // means we end up with a root box sized by the wrapping column - which causes various
        // painting problems for the scrollpane etc. So when we're done, we'll set the width
        // back to the initialwidth if that's larger than the computed width.
        int savedWidth = -1;

        if (wrapWidth != -1) {
            savedWidth = initialWidth;
            initialWidth = wrapWidth;
        }

        if (debugstats) {
            // During development only
//            CSSEngine.styleLookupCount = 0;
            CssProvider.getEngineService().clearEngineStyleLookupCount();
        }

        // Ensure box hierarchy has been created
        long create = 0;

        // Ensure box hierarchy has been created
        long start = 0;

        if (getBoxCount() == 0) {
            // TODO instead of checking on the box count, which could be 0
            // for valid reasons, have a dedicated flag here which is 
            // invalidated on document edits, etc.
            if (debugstats) {
                start = System.currentTimeMillis();
            }

//            XhtmlCssEngine engine = CssLookup.getCssEngine(body);
//            if (engine != null) {
//                engine.clearTransientStyleSheetNodes();
//            }
            
            // XXX #110849 Fixing the relayout. It seems it depends on uncomputed CSS values,
            // which seems to be wrong, but it is hard to fix that.
            // This fixes the layout, but might be a potential performance issue (with larger pages, projects).
//            CssProvider.getEngineService().clearTransientStyleSheetNodesForDocument(body.getOwnerDocument());
            CssProvider.getEngineService().clearComputedStylesForElement(body); // TEMP

            createChildren(null);

            if (debugstats) {
                long end = System.currentTimeMillis();
                create = end - start;
            }
        }

        // Perform layout
        if (debugstats) {
            start = System.currentTimeMillis();
        }

        // Initialize body margins and padding
        initialize();

        // Auto margins are not valid on the page box
        if (leftMargin == AUTO) {
            leftMargin = 0;
        }

        if (rightMargin == AUTO) {
            rightMargin = 0;
        }

        if (topMargin == AUTO) {
            topMargin = 0;
        }

        if (bottomMargin == AUTO) {
            bottomMargin = 0;
        }

        width = initialWidth;
        height = initialHeight; // XXX if AUTO don't copy to child

        if (width != AUTO) {
            contentWidth =
                width -
                (leftPadding + leftBorderWidth + leftMargin + rightMargin + rightBorderWidth +
                rightPadding);
        } else {
            contentWidth = AUTO;
        }

        if (height != AUTO) {
            contentHeight =
                height -
                (topPadding + topBorderWidth + topMargin + bottomMargin + bottomBorderWidth +
                bottomPadding);
        } else {
            contentHeight = AUTO;
        }

        setContainingBlock(leftBorderWidth + leftPadding, topBorderWidth + topPadding,
            contentWidth, contentHeight);

        context = new FormatContext();
        context.initialCB = new ViewportBox(viewport, initialWidth, initialHeight);
        context.initialWidth = initialWidth;
        context.initialHeight = initialHeight;

        try {
            layoutContext(context);
        } catch (Throwable e) { // want to catch assertion errors too
            ErrorManager.getDefault().notify(e);
            e.printStackTrace();
            layoutValid = false;
            pane.repaint();

            return;
        }

        if (savedWidth > width) {
            width = savedWidth;
            contentWidth =
                width -
                (leftPadding + leftBorderWidth + leftMargin + rightMargin + rightBorderWidth +
                rightPadding);
        }

        updateSizeInfo();

        long layout = 0;

        if (debugstats) {
            long end = System.currentTimeMillis();
            layout = end - start;
            org.openide.awt.StatusDisplayer.getDefault().setStatusText("Box Creation: " + create +
                " ms, layout: " + layout + " ms");
        }

        if (DEBUGFORMAT) {
            StringBuffer sb = new StringBuffer(1000);
            printLayout(sb);
            System.out.println(sb.toString());
        }

        if (debugstats) {
            gatherStatistics();

            // Additional statistics that may be interesting:
            // Average and maximum number of styles per element
            // Should tell me something about the speed of style lookups
            System.out.println("\nLayout Statistics:\n");
            System.out.println("Number of boxes: " + numBoxes);
            System.out.println("Number of leaf boxes: " + numLeaves);
            System.out.println("Number of elements: " + numElements);
//            System.out.println("Number of CSS style lookups: " + CSSEngine.styleLookupCount);
            System.out.println("Number of CSS style lookups: " + CssProvider.getEngineService().getEngineStyleLookupCount());
            System.out.println("Average lookups per box: " +
//                (CSSEngine.styleLookupCount / numBoxes));
                    (CssProvider.getEngineService().getEngineStyleLookupCount() / numBoxes));

            if (numBoxes > numLeaves) {
                System.out.println("Average lookups per non-leaf box: " +
//                    (CSSEngine.styleLookupCount / (numBoxes - numLeaves)));
                    (CssProvider.getEngineService().getEngineStyleLookupCount() / (numBoxes - numLeaves)));
            }

            System.out.println("Maximum box tree depth: " + maxDepth);
            System.out.println("Maximum element tree depth: " + maxElementDepth);
            System.out.println("Maximum child count: " + maxChildren);

            if (numBoxes > numLeaves) {
                System.out.println("Average child count: " +
                    ((numBoxes - 1) / (numBoxes - numLeaves)));
            }

            System.out.println("Number of text nodes: " + textNodes);
            System.out.println("Number of blank-only text nodes: " + blankTextNodes);
            System.out.println("Number of text boxes: " + textBoxes);
            System.out.println("Number of space boxes: " + spaceBoxes);
            System.out.println("Box Creation: " + (create / 1000.0) + " sec");
            System.out.println("Layout Computation: " + (layout / 1000.0) + " sec");
        }

        // XXX It would be nice to know how wide we actually are, let's
        // say widthActual. That way we know that we have a correct layout
        // for any x in the interval [widthActual,width], so we can
        // suppress resize requests until the widths is outside of that range!
        // (Note to self: that's only partially true; the actual width
        // could be less because of margins; these would have to be INCLUDED
        // in my actual width computation for the below to be correct)
    }

    protected void layoutContext(FormatContext context) {
        super.relayout(context);
        
        // XXX #99918 Ajusting the fixed boxes.
        adjustFixedBoxesIssue99918();
    }

    /** XXX #99918 Adjusts the position of fixed boxes,
     * when it is possible to compute so called 'static box',
     * that one is not possible to reliably compute in this architecture
     * at the intended place (getStaticLeft, getStaticTop in CssBox)
     * when the top or left values are auto, so it is hacked here. */
    private void adjustFixedBoxesIssue99918() {
        BoxList fixed = fixedBoxes;
        if (fixed == null) {
            return;
        }
        int size = fixed.size();
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            CssBox fixedBox = fixed.get(i);
            adjustFixedBoxLeftIssue99918(fixedBox);
            adjustFixedBoxTopIssue99918(fixedBox);
        }
    }
    
    private void adjustFixedBoxLeftIssue99918(CssBox fixedBox) {
        if (CssProvider.getValueService().isAutoValue(
                CssProvider.getEngineService().getComputedValueForElement(fixedBox.getElement(), XhtmlCss.LEFT_INDEX))
        ) {
            CssBox parentBox = fixedBox.getParent();
            if (parentBox != null && parentBox != fixedBox.getPositionedBy()) {
                fixedBox.left += parentBox.getAbsoluteX();
                fixedBox.setX(fixedBox.left);
            }
        }
    }
    
    private void adjustFixedBoxTopIssue99918(CssBox fixedBox) {
        if (CssProvider.getValueService().isAutoValue(
                CssProvider.getEngineService().getComputedValueForElement(fixedBox.getElement(), XhtmlCss.TOP_INDEX))
        ) {
            CssBox parentBox = fixedBox.getParent();
            if (parentBox != null && parentBox != fixedBox.getPositionedBy()) {
                fixedBox.top += parentBox.getAbsoluteY();
                fixedBox.setY(fixedBox.top);
            }
        }
    }

    protected void updateSizeInfo() {
        updateExtents(0, 0, 0); // NOT getAbsoluteX()/getAbsoluteY(), since 
                                //paint will call super which adds them in

        int extentWidth = extentX2 - extentX;
        int extentHeight = extentY2 - extentY;
        width = extentWidth;
        height = extentHeight;
        contentWidth =
            width -
            (leftPadding + leftBorderWidth + leftMargin + rightMargin + rightBorderWidth +
            rightPadding);
        contentHeight =
            height -
            (topPadding + topBorderWidth + topMargin + bottomMargin + bottomBorderWidth +
            bottomPadding);

        layoutWidth = width;
        layoutHeight = height;
    }

    /*
    public void setSize(int width, int height) {
        //Log.err.log("************************************************************************************\nPageBox.setSize - width= " + width + "\nLayoutValid was " + layoutValid + "  and contextwidth=" + (context != null ? Integer.toString(layoutWidth) : "null"));
        if (!layoutValid) {
            maxWidth = width;
            //setWidth(width);
            //setHeight(height);
            //layout();
        }
    }
    */
    public void redoLayout(boolean immediate) {
        currWidth = -1;
        removeBoxes();
        layoutValid = false;
        webform.getManager().updateInsertBox();

        if (immediate) {
            relayout(null);
        }
    }

    // FOR DEBUGFORMATTING ONLY!
    public void printLayout(StringBuffer sb) {
        sb.append("\nLAYOUT for " + this + "\n----------------\n");
        sb.append("\nInline Content/Lineboxes:");
        printLayout(this, sb, 0);
    }

    public int getAbsoluteX() {
        return leftMargin;
    }

    public int getAbsoluteY() {
        return effectiveTopMargin;
    }

    /**
     * A node was inserted into the document, below the given parent.
     */
    public void inserted(Node node, Node parent) {
        assert parent != null;
        assert parent.getNodeType() == Node.ELEMENT_NODE;
        assert node.getNodeType() == Node.ELEMENT_NODE;

        if (!layoutValid) { // next paint will do a full relayout anyway
            redoLayout(false); // ensure that boxes are null too incase layout was set to valid

            // with only the intent of a relayout without box recreation
            return;
        }

        if (context == null) {
            // Will be doing full relayout on next repaint anyway
            return;
        }

        Element element = null;
        CssBox target = null;

        if ((node.getNodeType() == Node.TEXT_NODE) ||
                (node.getNodeType() == Node.CDATA_SECTION_NODE) ||
                (node.getNodeType() == Node.ENTITY_REFERENCE_NODE)) {
            // If you change some text that's already "flown",
            // the target should be the line box group
            // containing the text
            // XXX how do I find an existing LBG for a node?
            // How do I decide where to add one?
            // Let's say you have <p><p> and the caret is in between these;
            // how do we end up adding it in the right place?
            redoLayout(true);

            return;

            // If there is no line box group for this, we've
            // gotta add one
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            element = (Element)node;

            // Table cells need special handling
            // I could go subclass addNode in TableBox to try to handle this more 
            // elegantly. Worry about removeBox too.
//            Value display = CssLookup.getValue(element, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.DISPLAY_INDEX);

//            if ((display == CssValueConstants.TABLE_ROW_VALUE) ||
//                    (display == CssValueConstants.TABLE_CELL_VALUE)) {
            if (CssProvider.getValueService().isTableRowValue(cssDisplay)
            || CssProvider.getValueService().isTableCellValue(cssDisplay)) {
                // What about (display == CssValueConstants.TABLE_ROW_GROUP_VALUE) ?
                // I only need to check for TBODY, THEAD, TFOOT, COL, etc.
                // if I support dynamically inserting these (or text nodes
                // dynamically within them).
//                while ((element != null)
//                        (CssLookup.getValue(element, XhtmlCss.DISPLAY_INDEX) != CssValueConstants.TABLE_VALUE)) {
                while (element != null
                && !CssProvider.getValueService().isTableValue(CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.DISPLAY_INDEX))) {
                    // TODO - what if the td is not inside a table? We'll
                    // get a class cast exception here - make this safer
                    element = (Element)parent;
                    parent = element.getParentNode();
                }

                if (element == null) {
                    ErrorManager.getDefault().log("Unexpected <td> outside of a table: " + node);
                    redoLayout(true);

                    return;
                }

                if (node != element) {
//                    changed(node, node.getParentNode(), false);
                    changed(node, node.getParentNode(), null);
                    return;
                }

                node = element;
            }

            // XXX todo -- use the mapper?
            // target = (ContainerBox)doc.getWebForm().getMapper().findBox(element);
//            target = CssBox.getBox(element);
            target = getWebForm().findCssBoxForElement(element);
        }

        Element parentElement = (Element)parent;

        // Update box hierarchy
        // Gotta figure out the parent of the inserted node,
        // discover which box it corresponds to, and then insert
        // it in the proper child position.
//        ContainerBox parentBox = (ContainerBox)webform.getMapper().findBox(parentElement);
        // XXX #6484485 Possible ClassCastException.
//        ContainerBox parentBox = (ContainerBox)ModelViewMapper.findBox(webform.getPane().getPageBox(), parentElement);
        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), parentElement);
        ContainerBox parentBox;
        if (box instanceof ContainerBox) {
            parentBox = (ContainerBox)box;
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("There was expected ContainerBox for parent element=" + parentElement // NOI18N
                    + ", but it is box=" + box)); // NOI18N
            parentBox = null;
        }

        if (parentBox == null) {
            redoLayout(true);

            return;
        }

        if (parentBox instanceof LineBox) {
            parentBox = parentBox.getParent();
        }

        CreateContext cc = new CreateContext();
        cc.pushPage(webform);
//        Font font = CssLookup.getFont(body, DesignerSettings.getInstance().getDefaultFontSize());
//        Font font = CssProvider.getValueService().getFontForElement(body, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//        cc.metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
        // XXX Missing text.
        cc.metrics = CssUtilities.getDesignerFontMetricsForElement(body, null, webform.getDefaultFontSize());

        // Previous and next boxes
        Node prevNode = node.getPreviousSibling();

        for (; prevNode != null; prevNode = prevNode.getPreviousSibling()) {
             if ((prevNode.getNodeType() == Node.TEXT_NODE) &&
                DesignerUtils.onlyWhitespace(prevNode.getNodeValue())) {
                 continue;
             }
             
             if (prevNode.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)prevNode;
                HtmlTag tag = HtmlTag.getTag(e.getTagName());
                if ((tag != null && tag.isHiddenTag()) || ((tag == HtmlTag.INPUT) && 
                        e.getAttribute(HtmlAttribute.TYPE).equals("hidden"))) {
                    continue;
                }
             }
             
             break;
        }

        Node nextNode = node.getNextSibling();

        for (; nextNode != null; nextNode = nextNode.getNextSibling()) {
             if ((nextNode.getNodeType() == Node.TEXT_NODE) &&
                DesignerUtils.onlyWhitespace(nextNode.getNodeValue())) {
                 continue;
             }
             
             if (nextNode.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)nextNode;
                HtmlTag tag = HtmlTag.getTag(e.getTagName());
                if ((tag != null && tag.isHiddenTag()) || ((tag == HtmlTag.INPUT) && 
                        e.getAttribute(HtmlAttribute.TYPE).equals("hidden"))) {
                    continue;
                }
             }
             
             break;
        }
        
        /*
        CssBox prevBox = prevNode != null &&
            prevNode.getNodeType() == Node.ELEMENT_NODE ?
            CssBox.getBox((Element)prevNode) : null;
         */
        CssBox prevBox = null;

        if (prevNode != null) {
            if (prevNode.getNodeType() == Node.ELEMENT_NODE) {
                // XXX use the Mapper?
//                prevBox = CssBox.getBox((Element)prevNode);
                prevBox = getWebForm().findCssBoxForElement((Element)prevNode);
            } else {
                prevBox = ModelViewMapper.findBox(parentBox, prevNode, 0);
            }
        }

        /*
        CssBox nextBox = nextNode != null &&
            nextNode.getNodeType() == Node.ELEMENT_NODE ?
            CssBox.getBox((Element)nextNode) : null;
         */
        CssBox nextBox = null;

        if (nextNode != null) {
            if (nextNode.getNodeType() == Node.ELEMENT_NODE) {
                // XXX use the mapper?
//                nextBox = CssBox.getBox((Element)nextNode);
                nextBox = getWebForm().findCssBoxForElement((Element)nextNode);
            } else {
                nextBox = ModelViewMapper.findBox(parentBox, nextNode, 0);
            }
        }

        // Incremental Layout: may not do the right thing when we add a block box
        // in and we have either prev or next nodes that are inline boxes
        if ((nextBox == null) && (prevBox != null)) {
            int index = prevBox.getParentIndex() + 1;

            if (prevBox.getParent() instanceof LineBoxGroup) {
                BoxList boxes = ((LineBoxGroup)prevBox.getParent()).getManagedBoxes();

                if (boxes.size() > index) {
                    nextBox = boxes.get(index);
                }
            } else {
                if (prevBox.getParent().getBoxCount() > index) {
                    nextBox = prevBox.getParent().getBox(index);
                }
            }
        } else if ((nextBox != null) && (prevBox == null)) {
            int index = nextBox.getParentIndex() - 1;

            if (index >= 0) {
                if (nextBox.getParent() instanceof LineBoxGroup) {
                    prevBox = ((LineBoxGroup)nextBox.getParent()).getManagedBoxes().get(index);
                } else {
                    prevBox = nextBox.getParent().getBox(index);
                }
            }
        }

        try {
            parentBox.addNode(cc, node, null, prevBox, nextBox);
        } catch (Throwable ex) { // want to catch assertions too
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
            redoLayout(true);

            return;
        }

        if (cc.getFixedBoxes() != null) {
            // We've added a fixed box
            if (fixedBoxes == null) {
                fixedBoxes = cc.getFixedBoxes();
            } else {
                BoxList fp = cc.getFixedBoxes();

                for (int i = 0, n = fp.size(); i < n; i++) {
                    fixedBoxes.add(fp.get(i), null, null);
                }
            }
        }

        // Update layout        
        if (target == null) {
            // XXX use the mapper?
//            target = CssBox.getBox(element);
            target = getWebForm().findCssBoxForElement(element);

            if (target == null) {
                // Internal error - didn't find box for element
                redoLayout(true);

                return;
            }
        }

        // I may have inserted another linebox - make sure its layout is processed
        // as well.
        if (cc.prevChangedBox != null) {
            updateLayout(cc.prevChangedBox);
        }

        // Unfortunately, we may have "preloaded" the component with styles above
        // before we had actual containing blocks assigned to the elements, so
        // clear the styles first
//        CssLookup.clearComputedStyles(target.getElement());
        CssProvider.getEngineService().clearComputedStylesForElement(target.getElement());

        updateLayout(target);

        // I may have inserted another linebox - make sure its layout is processed
        // as well.
        if (cc.nextChangedBox != null) {
            updateLayout(cc.nextChangedBox);
        }

        updateSizeInfo();

        if ((Math.abs(extentX) > 50000) || (Math.abs(extentY) > 50000) ||
                (Math.abs(extentX2) > 50000) || (Math.abs(extentY2) > 50000)) {
            // Something went horribly wrong during incremental layout, so
            // do it from scratch
            redoLayout(true);

            return;
        }

        webform.getManager().updateInsertBox();

        if (pane != null) {
            pane.repaint();
        }
    }

    public void removed(Node node, Node parent) {
        boxes = null;
        redoLayout(false);
    }

    /**
     * @todo Remove the parent pointer, we don't need it for change events
     */
    public void changed(Node node, Node parent, Element[] changedElements) {
        assert parent != null;

        //assert node.getNodeType() == Node.ELEMENT_NODE;
        if (!layoutValid) { // next paint will do a full relayout anyway
            redoLayout(false); // ensure that boxes are null too incase layout was set to valid

            // with only the intent of a relayout without box recreation
            return;
        }

        if (context == null) {
            // Will be doing full relayout on next repaint anyway
            return;
        }

        Element element = null;
        CssBox target = null;

        if ((node.getNodeType() == Node.TEXT_NODE) ||
                (node.getNodeType() == Node.CDATA_SECTION_NODE) ||
                (node.getNodeType() == Node.ENTITY_REFERENCE_NODE)) {
            // If you change some text that's already "flown",
            // the target should be the line box group
            // containing the text
            // XXX how do I find an existing LBG for a node?
            // How do I decide where to add one?
            // Let's say you have <p><p> and the caret is in between these;
            // how do we end up adding it in the right place?
            // For now, redoing all layout for text changes!
            redoLayout(true);

            return;

            // If there is no line box group for this, we've
            // gotta add one
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            element = (Element)node;

            // Table cells need special handling
            // I could go subclass addNode in TableBox to try to handle this more 
            // elegantly. Worry about removeBox too.
//            Value display = CssLookup.getValue(element, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.DISPLAY_INDEX);

//            if ((display == CssValueConstants.TABLE_ROW_VALUE) ||
//                    (display == CssValueConstants.TABLE_CELL_VALUE)) {
            if (CssProvider.getValueService().isTableRowValue(cssDisplay)
            || CssProvider.getValueService().isTableCellValue(cssDisplay)) {
                // What about (display == CssValueConstants.TABLE_ROW_GROUP_VALUE) ?
                // I only need to check for TBODY, THEAD, TFOOT, COL, etc.
                // if I support dynamically inserting these (or text nodes
                // dynamically within them).
//                while ((element != null) &&
//                        (CssLookup.getValue(element, XhtmlCss.DISPLAY_INDEX) != CssValueConstants.TABLE_VALUE)) {
                while (element != null && parent != null
                && !CssProvider.getValueService().isTableValue(CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.DISPLAY_INDEX))) {
                    element = (Element)parent;
                    parent = element.getParentNode();
                }

                if (element == null) {
                    //  Unexpected <td> outside of a table
                    redoLayout(true);

                    return;
                }

                node = element;
            }

            // XXX use the mapper?
//            target = CssBox.getBox(element);
            target = getWebForm().findCssBoxForElement(element);
        } else {
            // Unexpected node for change event
            redoLayout(true);

            return;
        }

        // Unfortunately, we may have "preloaded" the component with styles above
        // before we had actual containing blocks assigned to the elements, so
        // clear the styles first
//        CssLookup.clearComputedStyles(target.getElement());
        // XXX This needs to be cleared before the new box is added into the hierarchy (see below).
//        CssProvider.getEngineService().clearComputedStylesForElement(element);
        if (changedElements == null || changedElements.length == 0) {
            CssProvider.getEngineService().clearComputedStylesForElement(element);
        } else {
            // XXX #105179 Update style only for the changed elements (and their children).
            for (Element changedElement : changedElements) {
                CssProvider.getEngineService().clearComputedStylesForElement(changedElement);
            }
        }
        
        //!CQ parent may be the Document itself... assert parent.getNodeType() == Node.ELEMENT_NODE;
        //Element parentElement = (Element)parent;
        if (target == null) {
            // Internal error - didn't find box for element
            redoLayout(true);

            return;
        }

        ContainerBox parentBox = target.getParent();

        if (parentBox instanceof LineBox) {
            parentBox = parentBox.getParent();
        }

        /* Temporarily disabled; gotta resolve issue of how to
         * transfer updated styles from the source element (which
         * is manipulated by the GridHandler) to the generated
         * JSF element (which is rendered during layout)
        if (wasMove && target.getBoxType().isAbsolutelyPositioned()) {
            //parentBox.positionBox(target, context);
            parentBox.layoutChild(target, context, false);
            updateSizeInfo();
            if (Math.abs(extentX) > 50000 || Math.abs(extentY) > 50000 ||
                Math.abs(extentX2) > 50000 || Math.abs(extentY2) > 50000) {
                // Something went horribly wrong during incremental layout, so
                // do it from scratch
                redoLayout(true);
                return;
            }
            if (pane != null) {
                pane.repaint();
            }
            return;
        }
        */
        
        CreateContext cc = new CreateContext();
        cc.pushPage(webform);
//        Font font = CssLookup.getFont(body, DesignerSettings.getInstance().getDefaultFontSize());
//        Font font = CssProvider.getValueService().getFontForElement(body, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//        cc.metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
        // XXX Missing text.
        cc.metrics = CssUtilities.getDesignerFontMetricsForElement(body, null, webform.getDefaultFontSize());

        // XXX #109306 Remove also sibling boxes representing the same element.
        // XXX Why they don't have common parent?
        CssBox[] boxesToRemove = parentBox.getBoxesToRemove(target);
        
        // Add the new box right behind the old box        
        parentBox.addNode(cc, node, null, target, null);

        // Remove the old box
        CssBox deleted = target;

        // XXX Removing the old box (and its connected siblings).
        boolean removed = false;
        for (CssBox boxToRemove : boxesToRemove) {
            if (parentBox.removeBox(boxToRemove)) {
                removed = true;
            }
        }
//        if (!parentBox.removeBox(target)) {
        if (removed) {
            // XXX Suspicious (presumably not working attemt) to the recovery.
            // Internal error, but try to gracefully recover
            redoLayout(true);

            return;
        }

        if (fixedBoxes != null) {
            fixedBoxes.remove(target);

            if (fixedBoxes.size() == 0) {
                fixedBoxes = null;
            }
        }

        if (cc.getFixedBoxes() != null) {
            // We've added a fixed box
            if (fixedBoxes == null) {
                fixedBoxes = cc.getFixedBoxes();
            } else {
                BoxList fp = cc.getFixedBoxes();

                for (int i = 0, n = fp.size(); i < n; i++) {
                    fixedBoxes.add(fp.get(i), null, null);
                }
            }
        }

        // Update layout: look up new box for this element
        // XXX use the mapper
//        target = CssBox.getBox(element);
        target = getWebForm().findCssBoxForElement(element);

        if (target == null) {
            // Internal error - didn't find box for element
            redoLayout(true);

            return;
        }

        // I may have inserted another linebox - make sure its layout is processed
        // as well.
        // XXX That can't happen for change, right? 
        if (cc.prevChangedBox != null) {
            updateLayout(cc.prevChangedBox);
        }

//        // Unfortunately, we may have "preloaded" the component with styles above
//        // before we had actual containing blocks assigned to the elements, so
//        // clear the styles first
////        CssLookup.clearComputedStyles(target.getElement());
//        CssProvider.getEngineService().clearComputedStylesForElement(target.getElement());

        boolean redoDeleted = target.getParent() != parentBox;
        updateLayout(target);

        if (redoDeleted) {
            // Have changed parents - gotta ensure the layout is accurate in
            // the old tree too
            // This is for example the case if an inline element changes its positioning
            // from normal to absolute - the old linebox needs updating
            //updateLayout(parentBox);
            if (parentBox.getParentIndex() == -1) {
                // The parent box itself was removed, so notify its parent
                // This should only happen when the parentbox is a lineboxgroup
                assert parentBox instanceof LineBoxGroup;

                ContainerBox p = parentBox.getParent();

                if (p != null) {
                    p.notifyChildResize(parentBox, context);
                }
            } else {
                parentBox.notifyChildResize(deleted, context);
            }
        }

        // I may have inserted another linebox - make sure its layout is processed
        // as well.
        // XXX That can't happen for change, right? 
        if (cc.nextChangedBox != null) {
            assert false;
            updateLayout(cc.nextChangedBox);
        }

        updateSizeInfo();

        if ((Math.abs(extentX) > 50000) || (Math.abs(extentY) > 50000) ||
                (Math.abs(extentX2) > 50000) || (Math.abs(extentY2) > 50000)) {
            // Something went horribly wrong during incremental layout, so
            // do it from scratch
            redoLayout(true);

            return;
        }

        webform.getManager().updateInsertBox();

        if (pane != null) {
            pane.repaint();
        }
    }

    /**
     * The box hiearchy has changed: update the layout, and return the topmost
     * box whose dimensions changed.
     */
    protected CssBox updateLayout(CssBox target) {
        // Relayout the target - down the hierarchy
        // Create FormatContext; what about LineBox? Need to
        // find 
        // For now, just reusing existing context
        //int oldWidth = target.contentWidth;
        //int oldHeight = target.contentHeight;
        ContainerBox parent = target.getParent();

        // If we insert a new line box, it needs a containing block
        // We generally work our way outwards with layout, but this assumes containing
        // blocks are known since they (horizontal ones) are computed top down
        if (parent instanceof LineBoxGroup && (parent.containingBlockWidth <= 0)) {
            parent.getParent().setContainingBlock(parent, context);
        }

        parent.layoutChild(target, context, true);

        // Call positionBox(target, context); ?
        // Have the dimensions changed? If not, we're done
        // XXX No - what if something else changed, like top/left?
        // And gotta update extents too - and scrollbar
        // And of course I've gotta position it!

        /*
        if (target.contentWidth == oldWidth &&
            target.contentHeight == oldHeight) {
            // TODO what if the effective margin has changed?
            // That might require us to propagate the change upwards
            // too!
            return;
        }
         */

        // Sometimes (in particular, for inline boxes) the parent has changed;
        // once we've called layoutChild, the inserted inline box is placed
        // into a linebox. Make sure we don't miss a resize requirement here.
        return parent.notifyChildResize(target, context);
    }

    //private static int numStyles;
    private void gatherStatistics() {
        numBoxes = 0;
        maxChildren = 0;
        maxDepth = 0;
        maxElementDepth = 0;
        numLeaves = 0;
        numElements = 0;
        textNodes = 0;
        textBoxes = 0;
        spaceBoxes = 0;
        blankTextNodes = 0;

        //numStyles = 0;
        gatherStatisticsBoxTree(this, 0);
        gatherStatisticsElementTree(body, 0);

        // Count styles -- how?
    }

    private void gatherStatisticsBoxTree(CssBox box, int depth) {
        int childCount = box.getBoxCount();

        for (int i = 0; i < childCount; i++) {
            CssBox child = box.getBox(i);
            gatherStatisticsBoxTree(child, depth + 1);
        }

        numBoxes++;

        if (depth > maxDepth) {
            maxDepth = depth;
        }

        if (childCount > maxChildren) {
            maxChildren = childCount;
        }

        if (childCount == 0) {
            numLeaves++;
        }

        if (box.getBoxType() == BoxType.TEXT) {
            textBoxes++;
        } else if (box.getBoxType() == BoxType.SPACE) {
            spaceBoxes++;
        }
    }

    private void gatherStatisticsElementTree(Node node, int depth) {
        org.w3c.dom.NodeList list = node.getChildNodes();
        int len = list.getLength();

        for (int i = 0; i < len; i++) {
            org.w3c.dom.Node child = (org.w3c.dom.Node)list.item(i);
            gatherStatisticsElementTree(child, depth + 1);
        }

        numElements++;

        if (depth > maxElementDepth) {
            maxElementDepth = depth;
        }

        if ((node.getNodeType() == Node.TEXT_NODE) ||
                (node.getNodeType() == Node.CDATA_SECTION_NODE)) {
            textNodes++;

            if (DesignerUtils.onlyWhitespace(node.getNodeValue())) {
                blankTextNodes++;
            }
        }
    }
}

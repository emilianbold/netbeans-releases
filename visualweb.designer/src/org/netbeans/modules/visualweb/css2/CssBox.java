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

import org.netbeans.modules.visualweb.api.designer.cssengine.CssComputedValue;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssListValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.spi.designer.Decoration;
import org.netbeans.modules.visualweb.designer.ImageCache;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;

import org.openide.util.Utilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.netbeans.modules.visualweb.designer.DesignerPane;
import org.netbeans.modules.visualweb.designer.Interaction;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * Represents a CSS2 Box. I chose to call it CssBox instead of Box since
 * that would conflict with a JDK class.
 * <p>
 * See  http://www.w3.org/TR/REC-CSS2/box.html
 *
 * @todo Rename class to just "Box". And factor out the container-related
 *  functionality into a subclass, ContainerBox.
 * @todo Clarify the getX(), getWidth(), etc. methods - possibly rename
 *  them, to clarify which edge they are positioning - the margin,
 *  the border, the padding or the content? Ditto for width - which width
 *  is it returning - the margin box, the padding box, the border box,
 *  the content box?
 * @todo Split computeHorizontalLength up in subportions
 * @author Tor Norbye
 */
public class CssBox implements Box {
    /** Grid size for design-time borders. XXX we really should make this dynamic to match the user's chosen grid size! */
    private static final int MINIMUM_BEAN_SIZE = 24;

//    public static final int AUTO = Integer.MAX_VALUE - 1;
    public static final int AUTO = CssUtilities.AUTO;

//    public static final int UNINITIALIZED = Integer.MAX_VALUE - 2; // debugging
    public static final int UNINITIALIZED = Box.UNINITIALIZED; // debugging

    /** XXX Very suspicious usage, see PageBox, and try to get rid of it.
      What a name? What it has to do with persistence? */
//    public static boolean noBoxPersistence = false;

    public static final int X_AXIS = SwingConstants.HORIZONTAL;
    public static final int Y_AXIS = SwingConstants.VERTICAL;

    // Log info pertaining to box layout
    protected static final boolean DEBUGFORMAT = false;
    private static boolean debugPaint = System.getProperty("rave.debugLayout") != null; // NOI18N
    static boolean paintPositioning = System.getProperty("rave.debugPositioning") != null; // NOI18N
    static boolean paintSpaces = debugPaint;
    static boolean paintText = false; //debugPaint;

    // XXX TODO Get rid of this too (before it was org.netbeans.modules.visualweb.text.Document here).
    protected final WebForm webform;
    int top = AUTO;
    int left = AUTO;
    int right = AUTO;
    int bottom = AUTO;
    int contentWidth;
    int contentHeight;

    /** Z stacking order. AUTO means not set */
    int z = AUTO;
    
    /** Parent box. */
    private ContainerBox parent;
    // XXX
    CssBox positionedBy;

    // TODO: update these puppies.
    int extentX;

    // XXX No - minimum default value should be e.g. Integer.MAX_VALUE
    int extentY;
    int extentX2;
    int extentY2;
    
//    // XXX Get rid of this from here.
//    private MarkupDesignBean bean;
    
    int topMargin; // XXX do I need to do floating point arithmetic on this?
    int bottomMargin;
    int leftMargin;
    int rightMargin;
    int topPadding;
    int bottomPadding;
    int leftPadding;
    int rightPadding;
    int topBorderWidth;
    int bottomBorderWidth;
    int rightBorderWidth;
    int leftBorderWidth;
    CssBorder border;
    
    // XXX Get rid of these from here.
    /** Rendered element. */
    private final Element element;
    
//    /** Source element. */
//    private Element sourceElement;
    
    Color bg;
    HtmlTag tag;
    BackgroundImagePainter bgPainter;
    int x = UNINITIALIZED;
    int y = UNINITIALIZED;
    int width = UNINITIALIZED;
    int height = UNINITIALIZED;
    private int parentIndex = -1;

    /** The containing block extents associated with a box is the
     * size it has been allocated by its parent - in other words, it's
     * NOT the block it exposes to its children! The containing block
     * exposed to its children is the content edge - e.g.
     * x+leftBorderWidth+leftPadding, to x+width-rightBorderWidth-rightPadding.
     */
    int containingBlockX;
    int containingBlockY;
    int containingBlockWidth = -1;
    int containingBlockHeight = -1;
    boolean inline;
    boolean replaced;
    BoxType boxType;
    int effectiveTopMargin = UNINITIALIZED;
    int effectiveBottomMargin = UNINITIALIZED;
    boolean hidden;

    private final boolean initialFocus;
    
    //this field is used by in inline layout by
    //LineBox and LineBoxGrouh classes to store an inline container 
    //where the box originally resided in.
    //This is needed to get css properties from that container
    //There's no other way to access the container, 
    //because in inline layout, boxes are taken out of it
    // and placed in an inline box
    CssBox originalInlineContainer = null;
    

    public CssBox(WebForm webform, Element element, BoxType boxType, boolean inline,
        boolean replaced) {
        // XXX How come it can be null (see ViewportBox), and there is no test
        // against possible NPE in the code.
        this.webform = webform;
        this.boxType = boxType;

        this.inline = inline;

        // Make sure all block level boxes are container boxes
        // why is that? spec does not say it, does it?
        // assert inline || this instanceof ContainerBox;

            //            // Ensure that we pick up the original jsp element, not
            //            // a document fragment copy as is the case for html children
            //            // of a rendered jsf component
            //            this.element = ((XhtmlElement)element).getSourceElement();
            //            // Don't confuse the above getSourceElement with our
            //            // "sourceElement" field; the latter is used to correlate
            //            // a rendered jsf element (such as an "<input>" with its
            //            // source element "h:command_button"). I could in fact
            //            // use the field to mean both, but to do that I'll need to
            //            // go and change a lot of getElement usage, since
            //            // for all html elements I want the source element, and for
            //            // most jsf rendered elements, I want the effective (rendered)
            //            // element.
            this.element = element;

        this.replaced = replaced;

        initializeDesignBean();

        effectiveTopMargin = 0;
        effectiveBottomMargin = 0;
        initializeInvariants();
        
//        this.initialFocus = DesignerActions.isFocus(getDesignBean());
//        this.initialFocus = isFocus(getDesignBean());
//        this.initialFocus = isFocus(getMarkupDesignBeanForCssBox(this));
        this.initialFocus = WebForm.getDomProviderService().isFocusedElement(CssBox.getElementForComponentRootCssBox(this));
    }
    

    // XXX Still suspicious, should be cleaner when and when not to init it.
    protected void initializeDesignBean() {
        if ((element != null) && (boxType != BoxType.LINEBOX)) {
            // DO THIS ONLY FOR LIVEBEAN-ELEMENTS! putBoxReference(element, this);
            putBoxReference(element, this); // TEMPORARY HACK! XXX TODO REMOVEME [PERFORMANCE] Should I just do this in the else clause for bean!=null below?

//            bean = FacesSupport.getDesignBean(element);
//
////            if (element.getParentNode() instanceof RaveElement &&
////                    (((RaveElement)element.getParentNode()).getDesignBean() == bean)) {
//            if (element.getParentNode() instanceof Element
////            && InSyncService.getProvider().getMarkupDesignBeanForElement((Element)element.getParentNode()) == bean) {
//            && WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)element.getParentNode()) == bean) {
//                // Only set the sourceElement if we're a new toplevel
//                // element for this bean
//                bean = null;
//            }
            
////            bean = WebForm.getDomProviderService().getMarkupDesignBeanForComponentRootElement(element);
//            MarkupDesignBean bean = getMarkupDesignBeanForComponentRootCssBox(this);
//            
//            if (bean != null) {
//                sourceElement = bean.getElement();
            // XXX Strange check, just keeping it the same like it was before.
//            Element renderedElement = getElementForPrincipalCssBox(this);
//            if (renderedElement == element) {
//                Element sourceElement = MarkupService.getSourceElementForElement(renderedElement);
            Element sourceElement = findSourceElement();
            if (sourceElement != null) {
                //assert bean == ((XhtmlElement)sourceElement).getDesignBean() : "sourceElement " +
                //sourceElement + " has a designbean (" +
                //((XhtmlElement)sourceElement).getDesignBean() + ") different from bean " +
                //bean;
                putBoxReference(sourceElement, this);
            }
//            }
        }
    }
    
    /** Initialize the box - but initialize only the properties that do not depend on
     * layout, e.g. the containing block.
     */
    protected void initializeInvariants() {
        // initializeBorder(); ?
        initializeBackground();

        if (element != null) {
//            Value v = CssLookup.getValue(element, XhtmlCss.VISIBILITY_INDEX);
            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.VISIBILITY_INDEX);
//            hidden = (v != CssValueConstants.VISIBLE_VALUE);
            hidden = (!CssProvider.getValueService().isVisibleValue(cssValue));
        }
    }

    /**
     * @todo XXX I should have a phase flag here, which indicates
     *  whether I'm creating boxes, or doing layout. Some stuff can/should
     *  be initialized early on (box creation), other stuff must be deferred
     *  (because it might depend on the containing block).
     *  For example, CssContainerBlock's addText() method depends on the
     *  background to be initialized during box creation, but only the
     *  background color - and other parts of initializeBackground
     *  (related to background-position) must be computed later on.
     * XXX update: initializeInvariants is now used for the box creation phase
     * and initialize() should only be called when we have a containing block
     * so margin percentages etc. can be computed
     * @todo Rename this method to initializeLayout
     */
    protected void initialize() {
        if (element != null) {
            initializeBorder(); // move to initializeInvariants???
            initializeMargins();
            initializePadding();

            //initializeBackground(); moved to initializeInvariants
            //initializeZOrder(); // XXX done from BoxList.add because
            //initialize() is called during -layout-, and the z order is
            //needed during box creation. (XXX why? I should be able to
            //delay that!)
        } else {
            effectiveTopMargin = 0;
            effectiveBottomMargin = 0;
        }
    }

    protected void initializeContentSize() {
//        contentWidth = CssLookup.getLength(element, XhtmlCss.WIDTH_INDEX);
//        contentHeight = CssLookup.getLength(element, XhtmlCss.HEIGHT_INDEX);
        contentWidth = CssUtilities.getCssLength(element, XhtmlCss.WIDTH_INDEX);
        contentHeight = CssUtilities.getCssLength(element, XhtmlCss.HEIGHT_INDEX);
    }

    /** Get the BoxType for this box */
    public BoxType getBoxType() {
        return boxType;
    }

    /** Set the box type for this box */

    /*
    void setBoxType(BoxType boxType) {
        this.boxType = boxType;
    }
    */
    protected void initializeZOrder() {
        // Look up Z
//        Value val = CssLookup.getValue(element, XhtmlCss.Z_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.Z_INDEX);

//        if (val != CssValueConstants.AUTO_VALUE) {
        if (!CssProvider.getValueService().isAutoValue(cssValue)) {
//            z = (int)val.getFloatValue();
            z = (int)cssValue.getFloatValue();
        }
    }

    protected void initializeBorder() {
        border = CssBorder.getBorder(element);

        if (border != null) {
            leftBorderWidth = border.getLeftBorderWidth();
            topBorderWidth = border.getTopBorderWidth();
            bottomBorderWidth = border.getBottomBorderWidth();
            rightBorderWidth = border.getRightBorderWidth();

            // XXX contentWidth/contentHeight may not have been initialized yet!
        }

        considerDesignBorder(); // Important: do AFTER border initialization above
    }

    /** Border Patrol! Make sure there's a border if one is needed. */
    protected void considerDesignBorder() {
        // Don't add design border for boxes that already have a border
        if (border != null) {
            return;
        }

        // Only add design time border for absolutely positioned elements
        if (!boxType.isAbsolutelyPositioned()) {
            return;
        }

//        MarkupDesignBean bean = getMarkupDesignBeanForCssBox(this);
//        // Only add design borders for boxes that correspond to a live bean
//        // Gotta do something more here for markup beans
//        if ((bean == null) || ((
//            // Deal with the fact that we get design-ids repeated on children
//            // now, e.g. <table design-id="foo"><tr design-id="foo"> ....
//            // We only want the outer most box to have the design border
////            parent != null) && (parent.getDesignBean() == bean))) {
//            parent != null) && (getMarkupDesignBeanForCssBox(parent) == bean))) {
//            return;
//        }
        Element componentRootElement = getElementForComponentRootCssBox(this);
        if (componentRootElement == null) {
            return;
        }

        if (tag == HtmlTag.FORM) {
            // No design border on the form tag itself
            return;
        }

//        // Special case: the page separator shouldn't have a design border even
//        // though it renders a block box and may have dimensions set on it!
//        // TODO: Mark it horizontal resizable only!
//        if (bean.getInstance() instanceof com.sun.rave.web.ui.component.PageSeparator
//        || bean.getInstance() instanceof com.sun.webui.jsf.component.PageSeparator) {
//            return;
//        }
        if(WebForm.getDomProviderService().ignoreDesignBorder(componentRootElement)) {
            return;
        }

////        XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
//
//        // Design borders only on block boxes, or boxes that have specified
//        // a particular size (e.g. non-auto)
//        if (!isInlineBox() || engine.isInlineValue((RaveElement)element, XhtmlCss.WIDTH_INDEX) ||
//                engine.isInlineValue((RaveElement)element, XhtmlCss.HEIGHT_INDEX)) {
        if (!isInlineBox()
        || CssProvider.getEngineService().isInlineStyleValue(element, XhtmlCss.WIDTH_INDEX)
        || CssProvider.getEngineService().isInlineStyleValue(element, XhtmlCss.HEIGHT_INDEX)) {
            border = CssBorder.getDesignerBorder();
            leftBorderWidth = border.getLeftBorderWidth();
            topBorderWidth = border.getTopBorderWidth();
            bottomBorderWidth = border.getBottomBorderWidth();
            rightBorderWidth = border.getRightBorderWidth();
        }
    }

    protected void initializeBackground() {
        // Note: TableBox.CellBox doesn't call super.initializeBackground,
        // so if you do additional work here, check CellBox too.
        initializeBackgroundColor();
        initializeBackgroundImage();
    }

    protected void initializeBackgroundColor() {
//        bg = CssLookup.getColor(element, XhtmlCss.BACKGROUND_COLOR_INDEX);
        bg = CssProvider.getValueService().getColorForElement(element, XhtmlCss.BACKGROUND_COLOR_INDEX);
    }
    
    protected void initializeBackgroundImage() {
//        ImageIcon bgImage = BackgroundImagePainter.getBackgroundImage(webform, element);
//        MarkupUnit markupUnit = webform.getMarkup();
//        if (markupUnit == null) {
//            // #6457856 NPE
//            return;
//        }
//        URL markupUnitBase = markupUnit.getBase();
        URL markupUnitBase = webform.getBaseUrl();
        if (markupUnitBase == null) {
            // #6457856 NPE.
            return;
        }
        
//        URL imageUrl = CssBoxUtilities.getBackgroundImageUrl(element, markupUnitBase);
        URL imageUrl = CssProvider.getEngineService().getBackgroundImageUrlForElement(element, markupUnitBase);
        ImageIcon bgImage;
        if (imageUrl != null) {
            // XXX Revise this caching impl.
//            ImageCache imageCache = webform.getDocument().getImageCache();
            ImageCache imageCache = webform.getImageCache();
            bgImage = imageCache.get(imageUrl);
            if (bgImage == null) {
                bgImage = new ImageIcon(imageUrl);
                imageCache.put(imageUrl, bgImage);
            }
        } else {
            bgImage = null;
        }

        if (bgImage != null) {
//            Value repeatValue = CssLookup.getValue(element, XhtmlCss.BACKGROUND_REPEAT_INDEX);
            CssValue cssRepeatValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.BACKGROUND_REPEAT_INDEX);
//            ListValue positionValue =
//                CssLookup.getListValue(CssLookup.getValue(element,
//                        XhtmlCss.BACKGROUND_POSITION_INDEX));
            CssListValue cssPositionValue = CssProvider.getValueService().getComputedCssListValue(
                    CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.BACKGROUND_POSITION_INDEX));
            bgPainter = new BackgroundImagePainter(bgImage, cssRepeatValue, cssPositionValue, element, webform.getDefaultFontSize());
        }
    }

    protected void initializeMargins() {
//        leftMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
//        rightMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_RIGHT_INDEX);
//        topMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_TOP_INDEX);
        leftMargin = CssUtilities.getCssLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
        rightMargin = CssUtilities.getCssLength(element, XhtmlCss.MARGIN_RIGHT_INDEX);
        
        topMargin = CssUtilities.getCssLength(element, XhtmlCss.MARGIN_TOP_INDEX);
        if (topMargin == AUTO) {
            // XXX #6490331 Now resolve to 0, which covers almost all of the cases.
            // FIXME See: http://www.w3.org/TR/CSS21/visudet.html#Computing_heights_and_margins.
            topMargin = 0;
        }
        effectiveTopMargin = topMargin;
        
//        bottomMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_BOTTOM_INDEX);
        bottomMargin = CssUtilities.getCssLength(element, XhtmlCss.MARGIN_BOTTOM_INDEX);
        if (bottomMargin == AUTO) {
            // XXX #6490331 Now resolve to 0, which covers almost all of the cases.
            // FIXME See: http://www.w3.org/TR/CSS21/visudet.html#Computing_heights_and_margins.
            bottomMargin = 0;
        }

        effectiveBottomMargin = bottomMargin;
    }

    protected void initializePadding() {
//        leftPadding = CssLookup.getLength(element, XhtmlCss.PADDING_LEFT_INDEX);
//        rightPadding = CssLookup.getLength(element, XhtmlCss.PADDING_RIGHT_INDEX);
        leftPadding = CssUtilities.getCssLength(element, XhtmlCss.PADDING_LEFT_INDEX);
        rightPadding = CssUtilities.getCssLength(element, XhtmlCss.PADDING_RIGHT_INDEX);

        // Unlike margins, padding values are not allowed to be negative!
        if (leftPadding < 0) {
            leftPadding = 0;
        }

        if (rightPadding < 0) {
            rightPadding = 0;
        }

//        topPadding = CssLookup.getLength(element, XhtmlCss.PADDING_TOP_INDEX);
//        bottomPadding = CssLookup.getLength(element, XhtmlCss.PADDING_BOTTOM_INDEX);
        topPadding = CssUtilities.getCssLength(element, XhtmlCss.PADDING_TOP_INDEX);
        bottomPadding = CssUtilities.getCssLength(element, XhtmlCss.PADDING_BOTTOM_INDEX);

        if (topPadding < 0) {
            topPadding = 0;
        }

        if (bottomPadding < 0) {
            bottomPadding = 0;
        }
    }

    /** Return the element being rendered by this box; for jsf components
     * this means it will be a rendered html element, not a jsf element */
    public final Element getElement() {
        return element;
    }

    // XXX TEMP
    private Element findSourceElement() {
        // XXX Strange check, just keeping it the same like it was before.
        Element renderedElement = getElementForPrincipalCssBox(this);
        if (renderedElement == element) {
            return MarkupService.getSourceElementForElement(renderedElement);
        }
        return null;
    }
    
    /** Return the element representing this box in the source; for jsf
     * this will be the actual jsf element, not an html rendered element
     * for it.
     * XXX Heavy hack!! This method cheats, if there is not corresponding source element,
     * it returns its rendered element (#getElement). Get rid of this.
     */
    public final Element getSourceElement() {
        Element sourceElement = findSourceElement();
        if (sourceElement != null) {
            return sourceElement;
        } else {
            return getElement();
        }
    }

//    // XXX TODO JSF specific, replace with the latter method.
//    /** Compute a list of rectangles for all the boxes that represent
//     * the given live bean. Inline spans are added as a single bounding
//     * rectangle, not as individual rectangles for each inline text, image,
//     * form or iframe box. */
//    protected void computeRectangles(DesignBean component, List list) {
////        if (getDesignBean() == component) {
//        if (getMarkupDesignBeanForCssBox(this) == component) {
//            // allocations get mutated by later
//            // transformations so I've gotta make a copy
//            list.add(new Rectangle(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight()));
//
//            return; // No need to do children!
//        }
//
//        for (int i = 0, n = getBoxCount(); i < n; i++) {
//            CssBox child = getBox(i);
//            child.computeRectangles(component, list);
//
//            if (list.size() >= 1) {
//                break;
//            }
//        }
//    }

    // This will replace the above.
    /** Compute a list of rectangles for all the boxes that represent
     * the given live bean. Inline spans are added as a single bounding
     * rectangle, not as individual rectangles for each inline text, image,
     * form or iframe box. */
    protected void computeRectangles(Element componentRootElement, List<Rectangle> list) {
//        if (getDesignBean() == component) {
        if (getElementForComponentRootCssBox(this) == componentRootElement
        || WebForm.getDomProviderService().areLinkedToSameBean(element, componentRootElement)) {
            // allocations get mutated by later
            // transformations so I've gotta make a copy
            list.add(new Rectangle(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight()));

            return; // No need to do children!
        }

        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox child = getBox(i);
//            child.computeRectangles(component, list);
            child.computeRectangles(componentRootElement, list);

            if (list.size() >= 1) {
                break;
            }
        }
    }

//    // XXX TODO JSF specific, replace with the latter method.
//    protected Rectangle computeBounds(DesignBean component, Rectangle bounds) {
////        if (getDesignBean() == component) {
//        if (getMarkupDesignBeanForCssBox(this) == component) {
//            Rectangle r = new Rectangle(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());
//
//            if (bounds == null) {
//                // allocations get mutated by later
//                // transformations so I've gotta make a copy
//                bounds = r;
//            } else {
//                bounds.add(r);
//            }
//        }
//
//        for (int i = 0, n = getBoxCount(); i < n; i++) {
//            CssBox child = getBox(i);
//            bounds = child.computeBounds(component, bounds);
//        }
//
//        return bounds;
//    }
    
    // This will replace the above.
    protected Rectangle computeBounds(Element componentRootElement, Rectangle bounds) {
//        if (getDesignBean() == component) {
        if (getElementForComponentRootCssBox(this) == componentRootElement
        || WebForm.getDomProviderService().areLinkedToSameBean(element, componentRootElement)) {
            Rectangle r = new Rectangle(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

            if (bounds == null) {
                // allocations get mutated by later
                // transformations so I've gotta make a copy
                bounds = r;
            } else {
                bounds.add(r);
            }
        }

        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox child = getBox(i);
            bounds = child.computeBounds(componentRootElement, bounds);
        }

        return bounds;
    }

//    public /*protected*/ final Rectangle computeRegionBounds(MarkupMouseRegion region, Rectangle bounds) {
    public /*protected*/ final Rectangle computeRegionBounds(Element regionElement, Rectangle bounds) {
//        if ((element != null) && (((RaveElement)element).getMarkupMouseRegion() == region)) {
//        if (element != null && (InSyncService.getProvider().getMarkupMouseRegionForElement(element) == region)) {
//        if (element != null && (WebForm.getDomProviderService().getMarkupMouseRegionForElement(element) == region)) {
        if (element != null && WebForm.getDomProviderService().isSameRegionOfElement(regionElement, element)) {
            Rectangle r = new Rectangle(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

            if (bounds == null) {
                // allocations get mutated by later
                // transformations so I've gotta make a copy
                bounds = r;
            } else {
                bounds.add(r);
            }
        }

        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox child = getBox(i);

            if ((child.element != null) && (child.element.getParentNode() != element)) {
                Node curr = child.element.getParentNode();
                boolean found = false;

//                while (curr instanceof RaveElement && (curr != element)) {
//                    RaveElement xel = (RaveElement)curr;
                while (curr instanceof Element && (curr != element)) {
                    Element xel = (Element)curr;

//                    if (xel.getMarkupMouseRegion() == region) {
//                    if (InSyncService.getProvider().getMarkupMouseRegionForElement(xel) == region) {
//                    if (WebForm.getDomProviderService().getMarkupMouseRegionForElement(xel) == region) {
                    if (WebForm.getDomProviderService().isSameRegionOfElement(regionElement, xel)) {
                        found = true;
                    }

                    curr = curr.getParentNode();
                }

                if (found) {
                    // Yes, use the entire child as part of the region. One of
                    // its invisible parents (such as a <tr>) includes it.
                    Rectangle r =
                        new Rectangle(child.getAbsoluteX(), child.getAbsoluteY(), child.getWidth(),
                            child.getHeight());

                    if (bounds == null) {
                        // allocations get mutated by later
                        // transformations so I've gotta make a copy
                        bounds = r;
                    } else {
                        bounds.add(r);
                    }

                    continue;
                }
            }

//            bounds = child.computeRegionBounds(region, bounds);
            bounds = child.computeRegionBounds(regionElement, bounds);
        }

        return bounds;
    }

//    /** Returns true if this box represents an inline span.  This is true for
//      * inline boxes that are not block tags, or are not inline tags that are
//      * absolutely positioned, or are replaced.
//     */
//    protected boolean isSpan() {
//        return inline && !boxType.isAbsolutelyPositioned() && !replaced &&
//        this instanceof ContainerBox;
//    }

//    /** Return the first box matching the given DesignBean. */
//    private static CssBox findCssBoxInTree(DesignBean bean, CssBox cssBox) {
    /** Returns the first box matching the given component root element (rendered element). */
    private static CssBox findCssBoxInTree(Element componentRootElement, CssBox cssBox) {
        if (componentRootElement == null) {
            return null;
        }
//        if (bean == cssBox.getDesignBean()) {
//        if (bean == getMarkupDesignBeanForCssBox(cssBox)) {
        if (componentRootElement == getElementForComponentRootCssBox(cssBox)) {
            return cssBox;
        }

        for (int i = 0, n = cssBox.getBoxCount(); i < n; i++) {
            CssBox box = cssBox.getBox(i);
//            CssBox match = findCssBoxInTree(bean, box);
            CssBox match = findCssBoxInTree(componentRootElement, box);

            if (match != null) {
                return match;
            }
        }

        return null;
    }

//    /** Return the first box matching the given DesignBean.
//     * XXX Get rid of this and replace with #findCssBoxForComponentRootElement. */
//    protected CssBox findCssBox(DesignBean bean) {
////        Element e = FacesSupport.getElement(bean);
////        Element e = Util.getElement(bean);
////        Element e = WebForm.getDomProviderService().getElement(bean);
//        Element componentRootElement = WebForm.getDomProviderService().getRenderedElement(bean);
//
////        if (e != null) {
//////            CssBox box = CssBox.getBox(e);
////            CssBox box = getWebForm().findCssBoxForElement(e);
//        if (componentRootElement != null) {
//            CssBox box = getWebForm().findCssBoxForElement(componentRootElement);
//
//            if (box != null) {
//                return box;
//            }
//        }
//
////        return findCssBoxInTree(bean, this);
//        return findCssBoxInTree(componentRootElement, this);
//    }
    
    /** Gets the first box matching the given component root element. */
    protected CssBox findCssBoxForComponentRootElement(Element componentRootElement) {
        if (componentRootElement == null) {
            return null;
        }

        CssBox box = getWebForm().findCssBoxForElement(componentRootElement);
        if (box != null) {
            return box;
        }

        return findCssBoxInTree(componentRootElement, this);
    }

    /** Stash a reference to this box on the element
     * So we can quickly look up a Box for an element
     *
     */
    void putBoxReference(Element element, CssBox box) {
//        if (noBoxPersistence) {
//            return;
//        }

        getWebForm().setCssBoxForElement(element, box);
//        if (element instanceof RaveElement) {
//            ((RaveElement)element).setBox(box);
//        }

        /* This doesn't help, since putBoxReference is called from the
         * box constructor, and the parent has not been initialized (or
         * even computed) decided yet.
        // Use the topmost box that refers to this element
        if (box.parent != null &&
            (box.parent.element == element || box.parent.sourceElement == element)) {
            return;
        }
        */
    }

    // XXX Moved to WebForm.
//    /**
//     * Locate the box that was created for the given element,
//     * if any. May return null if the element has not been
//     * rendered.
//     */
//    public CssBox findCssBoxForElement(Element element) {
////        RaveElement e = (RaveElement)element;
////        CssBox box = (CssBox)e.getBox();
//        CssBox box = getWebForm().getCssBoxForElement(element);
//
//        if (box != null) {
//            // Work around the problem that children boxes when created may
//            // overwrite the existing box reference in the element. I can't
//            // simply at that point go and look to see if a reference already exists
//            // and if so not point to my new box, since that would break
//            // incremental layout etc. -- I create multiple generations of
//            // boxes for each element, and in the box constructor I don't
//            // know the parentage of the box yet.
//            while ((box.parent != null) && (box.parent.getElement() == element)) {
//                box = box.parent;
//            }
//
////            e.setBox(box);
//            getWebForm().setCssBoxForElement(element, box);
//
//            return box;
//        }
//
//        return null;
//    }

    /**
     * Paints the CSS box according to the attributes
     * given.  This should paint the border, padding,
     * and background.
     *
     * @param g the rendering surface.
     * @param x the x coordinate of the allocated area to
     *  render into.
     * @param y the y coordinate of the allocated area to
     *  render into.
     * @param w the width of the allocated area to render into.
     * @param h the height of the allocated area to render into.
     * @param v the view making the request.  This is
     *  used to get the AttributeSet, and may be used to
     *  resolve percentage arguments.
     */
    protected void paintBox(Graphics g, int x, int y, int w, int h) {
        if (hidden) {
            return;
        }

        if (border != null) {
            border.paintBorder(g, x, y, w, h);
        }

        // NO! Don't do this if a particular border edge is dashed
        // or none - in that case the border should "shine through!
        x += leftBorderWidth;
        y += topBorderWidth;
        w -= leftBorderWidth;
        w -= rightBorderWidth;
        h -= topBorderWidth;
        h -= bottomBorderWidth;

        if (bg != null) {
            g.setColor(bg);
            g.fillRect(x, y, w, h);
        } // I can't do an "else" here to avoid "double painting" the background

        // (first fillRect, then gradient painting over it) because I don't know
        // that the background painter will fully cover the box.  For example,
        // with background-repeat: repeat-x the full y won't be painted, etc.
        if (bgPainter != null) {
            // According to the CSS spec section 1.4.21, the background
            // image covers the padding rectangle
            bgPainter.paint(g, x, y, w, h);
        }
    }
    
    /**
     * <p>
     * Return the horizontal actual position of the visual portion of this box (the
     * border edge in the CSS 2 model) from the outermost viewport.
     * This is the actual screen pixel where the visual part of the component
     * will be painted.
     * </p>
     * <p>
     * Note that the name "getAbsoluteX" has "absolute" in it because unlike
     * getX(), which returns the -relative- position of this box (relative to
     * its parents border edge), this method computes the absolute, or actual,
     * screen coordinate. Note also that it's a recursive call, unlike getX()
     * which is a plain field accessor.
     * </p>
     *
     * @return The actual horizontal position of the beginning of the lefthand side border
     *   of this box (or if no border, the padding edge, and if no padding, the
     *   content edge).
     */
    public int getAbsoluteX() {
        if (positionedBy != parent) {
            return positionedBy.getAbsoluteX() + getX() + leftMargin;
        }

        if (parent != null) {
            return parent.getAbsoluteX() + x + leftMargin;
        } else {
            return x + leftMargin;
        }
    }

    /**
     * <p>
     * Return the actual vertical position of the visual portion of this box (the
     * border edge in the CSS 2 model) from the outermost viewport.
     * This is the actual screen pixel where the visual part of the component
     * will be painted.
     * </p>
     * <p>
     * Note that the name "getAbsoluteY" has "absolute" in it because unlike
     * getY(), which returns the -relative- position of this box (relative to
     * its parents border edge), this method computes the absolute, or actual,
     * screen coordinate. Note also that it's a recursive call, unlike getY()
     * which is a plain field accessor.
     * </p>
     * @return The actual vertical position of the beginning of the lefthand side border
     *   of this box (or if no border, the padding edge, and if no padding, the
     *   content edge).
     */
    public int getAbsoluteY() {
        if (positionedBy != parent) {
            return positionedBy.getAbsoluteY() + getY() + effectiveTopMargin;
        }

        if (parent != null) {
            return parent.getAbsoluteY() + y + effectiveTopMargin;
        } else {
            return y + effectiveTopMargin;
        }
    }

    /**
     *  Set the computed width of the box - from the left border edge
     *  to the right border edge - in other words, the width of the
     *  content, plus the padding, plus the border widths.
     * <b>For use by the formatter only.</b>
     * @param width the actual width of the box
     */

    /*
    void setWidth(int width) {
        Log.err.log(
            "setWidth("
                + width
                + ") on "
                + getElement()
                + " disabled for now");
        Thread.dumpStack();
        //this.width = width;
    }
    */

    /**
     *  Set the computed height of the box - from the top border edge
     *  to the bottom border edge - in other words, the height of the
     *  content, plus the padding, plus the border widths.
     * <b>For use by the formatter only.</b>
     * @param height the actual height of the box
     */

    /*
    void setHeight(int height) {
        Log.err.log(
            "setHeight("
                + height
                + ") on "
                + getElement()
                + " disabled for now");
        Thread.dumpStack();
        //this.height = height;
    }
    */

    /** Return the baseline of this box, if applicable. The baseline is the distance from
     * the top of the box to the baseline of the content in the box. This method has no
     * meaning for block-formatted boxes. For inline boxes, the box will be aligned with
     * the linebox baseline along its own baseline, when the vertical alignment is set to
     * "baseline".
     */
    protected int getBaseline() {
        return getHeight();
    }

    /** Return the baseline of this box as it contributes to computing the baseline in a
     * linebox. For most boxes, this is the same as their normal baseline. Images however
     * act a bit differently; normally their baseline are at the bottom of the image;
     * thus the image bottom will be aligned in a linebox along with the text baseline.
     * However, a really tall image will NOT push the baseline down like other boxes do.
     * This method captures the baseline that should be used for computing the overall
     * baseline before laying out the boxes along the baseline.
     */
    protected int getContributingBaseline() {
        return getBaseline();
    }

    /** Report whether this box is a grid positioning container. */
    public boolean isGrid() {
        return false;
    }

    /**
     * Set the x position of the box. This referers to the location of
     * the border edge - in other words where the box visually begins.
     * <b>For use by the formatter only.</b>
     * @param x the new x coordinate
     */
    void setX(int x) {
        this.x = x;
    }

    /**
     * Set the y position of the box. This referers to the location of
     * the border edge  - in other words where the box visually begins.
     * <b>For use by the formatter only.</b>
     * @param y the new y coordinate
     */
    void setY(int y) {
        this.y = y;
    }

    /** Convenience method to set both x and y in one call.
     * @see setX
     * @see setY
     */
    public final void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final String toString() {
        return super.toString() + "[" + paramString() + "]"; // NOI18N
    }

    protected String paramString() {
        return "pos=" + x + ":" + y + "," // NOI18N
                + "element=" + element // NOI18N
//                + ", markupDesignBean=" + getMarkupDesignBeanForCssBox(this) // NOI18N
//                + ", componentRootElement=" + getElementForComponentRootCssBox(this) // NOI18N
                + ", size=" + width + ":" + height // NOI18N
//                + ", contentWidth=" + contentWidth // NOI18N
//                + ", containingBlockWidth=" + containingBlockWidth
                ; // NOI18N
    }

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
//            g.setColor(Color.RED);
//            g.setFont(g.getFont().deriveFont(8.0f));

            CssBox badBox = findBadBox(this);
//            g.drawString("Fatal Painting Error: box " + badBox.toString(), 0,
//                g.getFontMetrics().getHeight());
//
//            if (badBox.getParent() != null) {
//                g.drawString("Fatal Painting Error: box " + badBox.getParent().toString(), 0,
//                    2 * g.getFontMetrics().getHeight());
//            }
            // XXX Improving the above error handling.
            // TODO Why is actually this state invalid?
            // XXX FIXME Possibility of this state is wrong.
            info(new IllegalStateException("Fatal painting error:" // NOI18N
                            + "\nthis box=" + this // NOI18N
                            + "\nbad box=" + badBox // NOI18N
                            + "\nparent of bad box=" + badBox.getParent())); // NOI18N

            return;
        }

        paintBackground(g, px, py);

        // Paint children
        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox box = getBox(i);

            if (DesignerPane.INCREMENTAL_LAYOUT) {
                if (DesignerPane.isOutsideClip(box.extentX, box.extentY, box.extentX2, box.extentY2)) {
                    //if (PageBox.debugclip)
                    //    System.out.println("CLIPPED BOX " + box);
// <decoration>
//                    continue;
// ====
                    // XXX Check the decoration too.
                    if (isBoxDecorationOutsideClip(box)) {
                        continue;
                    }
// </decoration>
                }
            }

            CssBox positionParent = box.getPositionedBy();

            if (positionParent != this) {
                // Not positioned by us - need to compute the
                // positioning parent's absolute position
                box.paint(g, positionParent.getAbsoluteX(), positionParent.getAbsoluteY());
            } else {
                box.paint(g, px, py);
            }
        }

        if (hasInitialFocus()) {
            paintFocusWaterMark(g, px, py);
        }
        
        // XXX Painting decoration if needed.
        paintDecoration(g, px + getWidth(), py);
        
        if (paintPositioning) {
            paintDebugPositioning(g);
        }

        // Children will do additional stuff
    }

    
    private /*static*/ boolean isBoxDecorationOutsideClip(CssBox box) {
//        if (!DesignerSettings.getInstance().isShowDecorations()) {
        if (!webform.isShowDecorations()) {
            return true;
        }
        
        Decoration decoration = box.getDecoration();
        if (decoration != null) {
            // XXX Where could be the location of the decoration?
            int x1 = box.extentX2;
            int y1 = box.extentY;
            int x2 = x1 + decoration.getWidth();
            int y2 = y1 + decoration.getHeight();

            return DesignerPane.isOutsideClip(x1, y1, x2, y2);
        }
        
        return true;
    }

    protected final boolean hasInitialFocus() {
        return initialFocus;
    }
    
    protected final void paintFocusWaterMark(Graphics g, int x, int y) {
        Image image = Utilities.loadImage("org/netbeans/modules/visualweb/designer/resources/focus-watermark.gif"); // NOI18N
        if ((image != null) && (g instanceof Graphics2D)) {
            Graphics2D g2d = (Graphics2D)g;
            AffineTransform t = new AffineTransform(); // XXX keep transform object around?
            t.translate(x, y);

            Composite oldAlpha = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2d.drawImage(image, t, null);
            g2d.setComposite(oldAlpha);
        }
    }
    
    protected void paintDecoration(Graphics g, int x, int y) {
//        if (!DesignerSettings.getInstance().isShowDecorations()) {
        if (!webform.isShowDecorations()) {
            return;
        }
        
        Decoration decoration = getDecoration();
        if (decoration == null) {
            return;
        }
        
//        Color oldColor = g.getColor();
//        Color color = new Color(255, 255, 0, 150); // TEMP
//        g.setColor(color);
//        g.fillRect(x, y, decoration.getWidth(), decoration.getHeight());
//        g.setColor(oldColor);
//
////                g.drawImage(image, x, y, null);
        Image image = decoration.getImage();
        if (image != null) {
            ImageIcon imageIcon = new ImageIcon(image);
            imageIcon.paintIcon(null, g, x, y);
        }
    }

    protected void paintDebugPositioning(Graphics g) {
//        MarkupDesignBean bean = getMarkupDesignBeanForCssBox(this);
////        if ((bean == null) || !boxType.isPositioned() || (FacesSupport.getFacesBean(bean) == null) ||
////                FacesSupport.isFormBean(webform, bean)) {
//        if ((bean == null) || !boxType.isPositioned()
////        || (Util.getFacesBean(bean) == null) || Util.isFormBean(webform.getModel(), bean)) {
//        || !WebForm.getDomProviderService().isFacesBean(bean)
//        || webform.isFormBean(bean)) {
//            return;
//        }
        Element componentRootElement = getElementForComponentRootCssBox(this);
//        if ((bean == null) || !boxType.isPositioned() || (FacesSupport.getFacesBean(bean) == null) ||
//                FacesSupport.isFormBean(webform, bean)) {
        if ((componentRootElement == null) || !boxType.isPositioned()
//        || (Util.getFacesBean(bean) == null) || Util.isFormBean(webform.getModel(), bean)) {
        || !WebForm.getDomProviderService().isFacesComponent(componentRootElement)
        || webform.isFormComponent(componentRootElement)) {
            return;
        }

        // TODO -- paint bottom, right, etc.?
        CssBox positionParent = getPositionedBy();

        if (positionParent == null) {
            return;
        }

        int rx = positionParent.getAbsoluteX();
        int ry = positionParent.getAbsoluteY();

        int px = getAbsoluteX();
        int py = getAbsoluteY();

        Graphics2D g2d = (Graphics2D)g;
        Stroke oldStroke = g2d.getStroke();

        //int width = 1;
        //BasicStroke s =
        //    new BasicStroke((float)width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
        //        10.0f, new float[] { 5 * width, (5 * width) + width }, 0.0f);
        //
        //g2d.setStroke(s);
        g.setColor(Color.LIGHT_GRAY);

        String xStr = Integer.toString(x);
        String yStr = Integer.toString(y);
        Font font = UIManager.getFont("Label.font");
        g2d.setFont(font);

        FontMetrics metrics = g2d.getFontMetrics();
        int xWidth = metrics.stringWidth(xStr);
        int yWidth = metrics.getAscent() + metrics.getDescent();

        // First segment
        int len = ((px - rx) / 2) - (xWidth / 2) - 2;

        if (len > 0) {
            g.drawLine(rx, py, rx + len, py);
            g.drawLine(px - len, py, px, py);
        }

        g.drawString(xStr, rx + len + 1, (py + (metrics.getHeight() / 2)) - metrics.getDescent());

        len = ((py - ry) / 2) - (yWidth / 2) - 2;

        if (len > 0) {
            g.drawLine(px, ry, px, ry + len);
            g.drawLine(px, py - len, px, py);
        }

        g2d.setStroke(oldStroke);
        g.drawString(yStr, px - (xWidth / 2), py - len - metrics.getDescent() + 1);
    }

    /** Find the deepest box that has invalid dimensions. This must be called on a bad
     * box to begin with since it will return self if nothing deeper is found.
     */
    private static CssBox findBadBox(CssBox box) {
        for (int i = 0, n = box.getBoxCount(); i < n; i++) {
            CssBox child = box.getBox(i);

            if ((Math.abs(child.extentX) > 50000) || (Math.abs(child.extentX2) > 50000) ||
                    (Math.abs(child.extentY) > 50000) || (Math.abs(child.extentY2) > 50000)) {
                return findBadBox(child);
            } else if ((Math.abs(child.getX()) > 50000) || (Math.abs(child.getY()) > 50000) ||
                    (Math.abs(child.width) > 50000) || (Math.abs(child.height) > 50000) ||
                    (Math.abs(child.leftMargin) > 50000) ||
                    ((Math.abs(child.effectiveTopMargin)) > 50000)) {
                return findBadBox(child);
            }
        }

        return box;
    }

    protected void paintBackground(Graphics g, int x, int y) {
        if (hidden) {
            return;
        }

        paintBox(g, x, y, getWidth(), getHeight());
    }

    /**
     *  If this box supports internal resizing, return the Cursor to be
     *  shown over the given coordinate
     */
    public int getInternalResizeDirection(int x, int y) {
        return Cursor.DEFAULT_CURSOR;
    }

    public Interaction getInternalResizer(int x, int y) {
        return null;
    }

//    /** Return a DesignBean associated with this box, if any.
//     * @deprecated Use {@link #getMarkupDesignBean} instead. */
//    public MarkupDesignBean getDesignBean() {
//        return bean;
//    }
    
//    /** XXX This is JSF specific. Move away from here.
//     * Gets associated markup design bean for the css box. It is associated only
//     * for the css box represents component top level elements except lines, texts and spaces. */
//    public static MarkupDesignBean getMarkupDesignBeanForCssBox(CssBox cssBox) {
////        if (cssBox == null) {
////            return null;
////        }
////        
////        Element element = cssBox.getElement();
////        BoxType boxType = cssBox.getBoxType();
////        if (element == null
////        || boxType == BoxType.LINEBOX
////        || boxType == BoxType.TEXT
////        || boxType == BoxType.SPACE) {
////            // XXX As before no assigned MarkupDesignBean for the above cases.
////            return null;
////        }
////        return getMarkupDesignBeanForComponentRootCssBox(cssBox);
//        return WebForm.getDomProviderService().getMarkupDesignBeanForElement(
//                getElementForComponentRootCssBox(cssBox));
//    }
    
//    private static MarkupDesignBean getMarkupDesignBeanForComponentRootCssBox(CssBox cssBox) {
//        if (cssBox == null) {
//            return null;
//        }
//        Element element = cssBox.getElement();
//        ContainerBox parentBox = cssBox.getParent();
//        Element parentBoxElement = parentBox == null ? null : parentBox.getElement();
//        return WebForm.getDomProviderService().getMarkupDesignBeanForComponentRootElement(element, parentBoxElement);
//    }

    /** XXX This will replace the JSF specific above methods.
     * Gets associated element for the css box. It returns the element only if the specified box
     * represents component top level element except lines, texts and spaces. */
    public static Element getElementForComponentRootCssBox(CssBox cssBox) {
        if (cssBox == null) {
            return null;
        }
        
        Element element = cssBox.getElement();
        BoxType boxType = cssBox.getBoxType();
        if (element == null
        || boxType == BoxType.LINEBOX
        || boxType == BoxType.TEXT
        || boxType == BoxType.SPACE) {
            // XXX As before no assigned MarkupDesignBean for the above cases.
            return null;
        }
        return getElementForPrincipalCssBox(cssBox);
    }
    
    private static Element getElementForPrincipalCssBox(CssBox cssBox) {
        if (cssBox == null) {
            return null;
        }
        Element element = cssBox.getElement();
        ContainerBox parentBox = cssBox.getParent();
        Element parentBoxElement = parentBox == null ? null : parentBox.getElement();
        return WebForm.getDomProviderService().isPrincipalElement(element, parentBoxElement) ? element : null;
    }
    
    
    /** Return the deepest box containing the given point. */
    protected CssBox findCssBox(int x, int y, int px, int py, int depth) {
        // TODO - get rid of px/py
        // Reverse order: list is ordered back to front
        for (int i = getBoxCount() - 1; i >= 0; i--) {
            CssBox box = getBox(i);

            // Is the coordinate in an absolute child of this box?
            boolean absChild =
                (x >= box.getExtentX()) && (x <= box.getExtentX2()) && (y >= box.getExtentY()) &&
                (y <= box.getExtentY2());
            
            // XXX Check decoration too.
            boolean absChildDecoration = isInsideBoxDecoration(x, y, box);

//            if (absChild) {
            if (absChild || absChildDecoration) {
                CssBox match = box.findCssBox(x, y, px, py, depth + 1);

                if (match != null) {
                    // TODO: we may have multiple matches (overlapping);
                    // should select all and compare best match based on
                    // Z-order!  (A flag to the method could indicate if we
                    // needed best match or any-match behavior. For mouse
                    // motion feedback for example, we don't care if we have
                    // the best match, only that we have a match.
                    // TODO - I can for example have a match field in this
                    // method, and if I find -two- matches, I pick the best
                    // fit, then continue.
                    return match;
                }
            }
        }

        // See if this match is okay.
        // This is necesary because we investigate children whose
        // extents include absolutely positioned views

        /*
        boolean match = x >=getExtentX()
                    && x <= getExtentX2()
                    && y >= getExtentY()
                    && y <= getExtentY2();
         */
        int ax = getAbsoluteX();
        int ay = getAbsoluteY();
        boolean match =
            (x >= ax) && (x <= (ax + getWidth())) && (y >= ay) && (y <= (ay + getHeight()));
        
        // XXX Check decoration too.
        boolean matchDecoration = isInsideDecoration(x, y, ax, ay);

        if (match || matchDecoration) {
//        if (match) {
            return this;
        } else {
            return null;
        }
    }
    
    private /*static*/ boolean isInsideBoxDecoration(int x, int y, CssBox box) {
//        if (!DesignerSettings.getInstance().isShowDecorations()) {
        if (!webform.isShowDecorations()) {
            return false;
        }
        
        Decoration decoration = box.getDecoration();
        if (decoration != null) {
            return (x >= box.getExtentX2())
                && (x <= box.getExtentX2() + decoration.getWidth())
                && (y >= box.getExtentY())
                && (y <= box.getExtentY() + decoration.getHeight());
        }
        
        return false;
    }
    
    private boolean isInsideDecoration(int x, int y, int ax, int ay) {
//        if (!DesignerSettings.getInstance().isShowDecorations()) {
        if (!webform.isShowDecorations()) {
            return false;
        }
        
        Decoration decoration = getDecoration();
        if (decoration != null) {
            return (x >= ax + getWidth()) 
                && (x <= (ax + getWidth() + decoration.getWidth()))
                && (y >= ay)
                && (y <= (ay + decoration.getHeight()));
        }
        
        return false;
    }

    /**
     * Return the smallest x coordinate of any children boxes (only for
     * absolutely positioned boxes) in this box hiearchy.
     * @return the absolute x coordinate in pixels
     * @todo Consider returning a relative coordinate here - relative
     *  to the current box' coordinate system that is! See
     *  ContainerBox.updateExtents.
     */
    protected int getExtentX() {
        // Later, I may have a separate extent from x,y,w,h:
        // This incorporates absolute positions in any descendant children,
        // not just directly managed width/height.
        return extentX;

        // For now, just implement it to return our position
        //return getX();
    }

    /**
     * Return the largest x coordinate of any children boxes (only for
     * absolutely positioned boxes) in this box hiearchy.
     * @return the x coordinate in pixels
     */
    protected int getExtentX2() {
        // See comment under getExtentX()
        return extentX2;

        //return getX() + getWidth();
    }

    /**
     * Return the smallest y coordinate of any children boxes (only for
     * absolutely positioned boxes) in this box hiearchy.
     * @return the y coordinate in pixels
     */
    protected int getExtentY() {
        // See comment under getExtentX()
        return extentY;

        //return getY();
    }

    /**
     * Return the largest y coordinate of any children boxes (only for
     * absolutely positioned boxes) in this box hiearchy.
     * @return the y coordinate in pixels
     */
    protected int getExtentY2() {
        // See comment under getExtentX()
        return extentY2;

        //return getY() + getHeight();
    }

    /** Update the extents to include the given position */

    /*
    protected void updateExtents(int x, int y, int x2, int y2) {
        if (x < extentX) {
            extentX = x;
        }
        if (x2 > extentX2) {
            extentX2 = x2;
        }
        if (y < extentY) {
            extentY = y;
        }
        if (y2 > extentY2) {
            extentY2 = y2;
        }
    }
    */

    /** Update the extents for this box (which involves the extents
     * of any of its children)
     */
    protected void updateExtents(int px, int py, int depth) {
        /*
        TODO: Currently this code produces ABSOLUTE coordinates for the
        extents. That will make document mutation updates trickier, since
        I will have to update all the boxes to adjust absolute coordinates
        when for example a newline is inserted near the top.

        Instead, why don't have I coordinates relative to the current box?
        Thus, when looking at the body box, I see coordinates relative to
        0,0 - e.g. absolute coordinates. When I go down to a particular
        child, I see extents relative to that box' x,y coordinate.
        This has the advantage that I can make local modifications,
        and only have to walk up the direct parent chain and adjust extents.
        */
        if (positionedBy != parent) {
            px = positionedBy.getAbsoluteX();
            py = positionedBy.getAbsoluteY();
        }

        px += getX();
        py += getY();

        // Box model quirk: my coordinate system is based on the visual
        // extents of the boxes - e.g. location and size of the border
        // edge.  Because of this, when visually traversing the hierarchy,
        // I need to add in the margins.
        px += leftMargin;
        py += effectiveTopMargin;

        if (DEBUGFORMAT) {
            // Look for problematic children
            if ((Math.abs(px) > 50000) || (Math.abs(py) > 50000) || (Math.abs(width) > 50000) ||
                    (Math.abs(height) > 50000)) {
                fine("Size wrong for " + this);
            }
        }

        extentX = px;
        extentY = py;
        extentX2 = px + width;
        extentY2 = py + height;

        // Check children
        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox child = getBox(i);
            child.updateExtents(px, py, depth + 1);

            if (child.extentX < extentX) {
                extentX = child.extentX;
            }

            if (child.extentY < extentY) {
                extentY = child.extentY;
            }

            if (child.extentX2 > extentX2) {
                extentX2 = child.extentX2;
            }

            if (child.extentY2 > extentY2) {
                extentY2 = child.extentY2;
            }
        }
    }

    /**
     * Return the parent or "container" for this box.
     */
    public final ContainerBox getParent() {
        return parent;
    }

    /**
     * Return the parent responsible for positioning this box.
     * This is typically the same as the container parent, but
     * in the case of "positioned" elements such as absolutely
     * positioned boxes, it will be some other ancestor.
     * The important aspect of this is that the coordinates
     * will be relative to this positioned-by parent, not
     * the container parent!
     */
    public CssBox getPositionedBy() {
        return positionedBy;
    }

    final void setParent(ContainerBox parent) {
        this.parent = parent;
    }

    void setPositionedBy(CssBox positionedBy) {
        this.positionedBy = positionedBy;
    }

    /**
     * Return the list of boxes "managed" by this box.  Managed simply
     * means that the coordinates in the boxes are all relative to this
     * one.
     */
    public int getBoxCount() {
        return 0;
    }

    /**
     * Return the box with the given index. There is no particular
     * significance to the index other than identifying a box; in particular
     * boxes with adjacent indices may not be adjacent visually.
     */
    public CssBox getBox(int index) {
        // Should only be called on ContainerBox-es, and these will
        // override this method
        throw new IllegalArgumentException();
    }

    /**
     * Set the containing block for this box.
     */
    protected void setContainingBlock(int x, int y, int width, int height) {
        containingBlockX = x;
        containingBlockY = y;
        containingBlockWidth = width;
        containingBlockHeight = height;
    }

    /*
     * Is this a clear box ("clear" CSS property is "left", "right", "both".
     */ 
    public boolean isClearBox() {
        CssValue cssClear = CssProvider.getEngineService().
                getComputedValueForElement(getElement(), XhtmlCss.CLEAR_INDEX);
        return 
                CssProvider.getValueService().isLeftValue(cssClear) ||
                CssProvider.getValueService().isBothValue(cssClear) ||
                CssProvider.getValueService().isRightValue(cssClear);
    }

    /** Is this box inline-level? If this method returns true, the box
     * is inline level, otherwise it is block level.
     */
    public boolean isInlineBox() {
        return inline;
    }

    public boolean isBlockLevel() {
        return !inline;
    }

    /**
     * Is this box replaced?
     */
    public boolean isReplacedBox() {
        return replaced;
    }

    public HtmlTag getTag() {
        return tag;
    }

    /** Return the <code>WebForm</code> this box is associated with.
     * @todo get rid of it from CSS box rendering */
    public WebForm getWebForm() {
        return webform;
    }

    // BOX MODEL stuff

    /** Return the -preferred- minimum width; this is the smallest
     * width that will avoid breaking up content beyond word boundaries,
     * or clipping images, etc.
     * @todo XXX Figure out if I should include the padding and border
     *  (and margins?) here - or just the content width? Certainly
     *  when it takes children into consideration, it needs to add in
     *  their margins, padding and borders - but what about itself?
     *  Look at the uses of this method and adjust. For example, in
     *  TableBox I add margins in afterwards - that's not good.
     */
    protected int getPrefMinWidth() {
        // contentWidth should be computed already
        int result;

        if ((contentWidth != AUTO) && (contentWidth != UNINITIALIZED)) {
            result = contentWidth;
        } else {
            result = getIntrinsicWidth();
        }

        result += (leftBorderWidth + leftPadding + rightPadding + rightBorderWidth);

        if (leftMargin != AUTO) {
            result += leftMargin;
        }

        if (rightMargin != AUTO) {
            result += rightMargin;
        }

        return result;
    }

    /**
     * Return the -preferred- width; this is the largest
     * width that the box can occupy.
     */
    protected int getPrefWidth() {
        // contentWidth should be computed already
        int result;

        if ((contentWidth != AUTO) && (contentWidth != UNINITIALIZED)) {
            result = contentWidth;
        } else {
            result = getIntrinsicWidth();
        }

        result += (leftBorderWidth + leftPadding + rightPadding + rightBorderWidth);

        if (leftMargin != AUTO) {
            result += leftMargin;
        }

        if (rightMargin != AUTO) {
            result += rightMargin;
        }

        return result;
    }

    /**
     * Return the width of the content - the distance between the
     * content edges. This can be set by the "width" CSS2 property.
     * This width, plus the left and right padding, constitutes the
     * width of the containing block established by this box for its
     * descendants.
     */
    protected int getContentWidth() {
        return width - leftPadding - rightPadding;
    }

    /**
     * Return the height of the content - the distance between the
     * content edges. This can be set by the "height" CSS2 property.
     * This height, plus the top and bottom padding, constitutes the
     * height of the containing block established by this box for its
     * descendants.
     */
    protected int getContentHeight() {
        return height - topPadding - bottomPadding;
    }

    /**
     * Return the x position of the content box, relative to the parent box
     */
    protected int getContentX() {
        return x + leftMargin + leftBorderWidth + leftPadding;
    }

    /**
     * Return the y position of the content box, relative to the parent box
     */
    protected int getContentY() {
        return y + topMargin + topBorderWidth + topPadding;
    }

    /**
     * Return the horizontal position of the box <b>relative</b> to its parent.
     * This points to the -border edge- of the box - not the margin edge,
     * not the padding edge (aka the containing block edge).
     * In other words, the x attribute tells you where visually the box
     * begins, relative to where its parent's border edge is located.
     *
     * @return the border x position of the box
     */
    public int getX() {
        return x;
    }

    /**
     * Return the vertical position of the box <b>relative</b> to its parent.
     * This points to the -border edge- of the box - not the margin edge,
     * not the padding edge (aka the containing block edge).
     * In other words, the y attribute tells you where visually the box
     * begins, relative to where its parent's border edge is located.
     *
     * @return the border y position of the box
     */
    public int getY() {
        return y;
    }

    /**
     * Return the z index of the box for the current stacking context.
     * Will be AUTO if not set using the z-index CSS property.
     *
     * @return the z index of the box.
     */
    public int getZ() {
        return z;
    }

    /**
     *  Return the actual width (in pixels) of the box.
     *  This refers to the distance between the left and right
     *  border edges - in other words the "visual" width of
     *  the box, since it includes padding, borders and content,
     *  and is therefore different than the containing block's
     *  width.
     *  (See CSS2 spec section 8.1).
     *
     *  @return the actual width of the box
     */
    public int getWidth() {
        return width;
    }

    /**
     *  Return the actual height (in pixels) of the box.
     *  This refers to the distance between the top and bottom
     *  border edges - in other words the "visual" height of
     *  the box, since it includes padding, borders and content,
     *  and is therefore different than the containing block's
     *  height.
     *  (See CSS2 spec section 8.1).
     *
     *  @return the actual width of the box
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the intrinsic width of the box. This is only defined for boxes
     * representing replaced elements as well as text boxes
     */
    protected int getIntrinsicWidth() {
        return 0; // XXX override in ImageBox, FormComponentBox, etc.
    }

    /**
     * Get the intrinsic width of the box. This is only defined for boxes
     * representing replaced elements as well as text boxes
     */
    protected int getIntrinsicHeight() {
        return 0; // XXX override in ImageBox, FormComponentBox, etc.
    }

    /**
     * Get the left margin of the box.
     * @return The left side margin of the box
     */
    public int getLeftMargin() {
        return leftMargin;
    }

    /**
     * Get the right margin of the box
     * @return The right side margin of the box
     */
    public int getRightMargin() {
        return rightMargin;
    }

    /**
     * Get the effective top margin of the box. This is
     * the collapsed margin of this box (e.g. the max of its margin
     * and any first children, modulo some scenarios with negative margins.)
     * @return The top margin of the box
     */
    public int getEffectiveTopMargin() {
        return effectiveTopMargin;
    }

    /**
     * Get the effective bottom margin of the box. This is
     * the collapsed margin of this box (e.g. the max of its margin
     * and any last children, modulo some scenarios with negative margins.)
     * @return The bottom margin of the box
     */
    protected int getEffectiveBottomMargin() {
        return effectiveBottomMargin;
    }

    /**
     * Set the left margin and effective top margin.
     * @param leftMargin The new left margin
     * @param effectiveTopMargin The new effective top margin
     */
    public void setMargins(int leftMargin, int effectiveTopMargin) {
        this.leftMargin = leftMargin;
        this.effectiveTopMargin = effectiveTopMargin;
    }

    /**
     * Return true for boxes where the size of border and padding is included
     * in the width assigned the element.
     * For example, if you have {@code <div style="width: 100px; border: 20px solid gray"></div>}
     * then the actual visual size of the box (not including the invisible margins) is going
     * to be 140 pixels. On the other hand, if you have
     * {@code <table style="width: 100px; border: 20px solid gray">...</table>} then
     * the actual width is 100 pixels. Thus, to distinguish the different kinds of
     * formatting behaviors (I haven't found details in the CSS spec regarding
     * this, but I'm sure it's there) this method lets you query what kind of
     * border/padding computation this box is performing.  For the normal case (div
     * in the above example), this method will return false. For table, and buttons,
     * it will return true.
     * @return True iff this box will subtract the border and padding widths from the
     *  assigned content width and content height to make the component fit in exactly
     *  the allocated size
     * @see getContentInsets
     */
    protected boolean isBorderSizeIncluded() {
        return false;
    }

    /**
     * <p>
     * Return the insets from the border box to the actual content area of the box.
     * This is normally the border width plus the padding, but for boxes like
     * buttons (which include their own borders, see {@link #isBorderSizeIncluded})
     * and tables (which may include a caption) it will be something different.
     * You can also think of this method as returning the difference in size between
     * the visual size of the box (the border box) and the content box (e.g. the
     * CSS width and height).
     * </p>
     * <p>
     * <b>Note</b>: to be more accurate, this method should return the insets from
     * the outermost box generated for the element for this box, to the area covered
     * by the CSS width and height dimensions.
     * Thus, in the case of a captioned table, where the caption is above the table,
     * the offset will be the size of the caption (and for the table, NOT the border
     * or padding since tables include their borders in the CSS width size.
     * </p>
     *
     * @return The insets from outside the borders of the box to the box covered by
     *   the CSS width and height properties for this element
     */
    public Insets getCssSizeInsets() {
        return new Insets(topBorderWidth + topPadding, leftBorderWidth + leftPadding,
            bottomBorderWidth + bottomPadding, rightBorderWidth + rightPadding);
    }

    /**
     * Recompute the layout. Once the layout routine gets to a point
     * where the child layout matches the computed layout, it will leave
     * that tree alone.  Thus, only the portions of the layout below
     * this box that need to be recomputed are updated
     */
    protected void relayout(FormatContext context) {
        // Nothing to do - no children
    }

    /** Very similar to relayout(context) but does not compute
     * vertical dimensions, and does not position the boxes.
     * Used to initialize box dimensions for computation
     * of minimum widths when we're computing minimum widths
     * for table cells, etc.
     * <p>
     * NOTE: It's very important for CSS computations of values that
     * are percentages to be "cleared" if we "pre-compute" them using
     * getPrefWidth and getPrefMinWidth before a containing block
     * has been computed (as is the case when we're calling getPrefWidth
     * to compute dimensions for table layout), since otherwise the
     * computed width value is cached for later, even when a real containing
     * block is available. If you put a table widht a div inside it, and
     * set the width of the div to 100%, the div will show up with zero
     * width without this caveat since otherwise during the outer table
     * layout, we're calling initializeHorizontalWidths and getPrefMinWidth
     * on the div, with a 0 containing block, so the computed value for
     * the div is 0*100%=0, and when we subsequently lay out the table,
     * even though the containing block may now be 600px the CSS lookup
     * for the width attribute will return the previously computed 0-value
     * rather than 600.
     */
    protected void initializeHorizontalWidths(FormatContext context) {
        if (element == null) {
            fine("Unexpected null element in initialize horizontal widths");

            return; // why does this happen?
        }

//        // No containing block for children - 100% etc. should
//        // evaluate to 0.
//        containingBlockX = 0;
//        containingBlockY = 0;
//        containingBlockWidth = 0;
//        containingBlockHeight = 0;
        // XXX #6504407 Incorrectly rendered table component when dealt with percentage width values.
        // see also http://www.w3.org/TR/CSS21/tables.html#auto-table-layout
        initializeContainingBlock();

        initialize();

        /* OLD: this gets us into trouble! For example, when called on
           an absolutely positioned div to initialize its children, it
           will call shrinkToFit, which winds up calling
           initializeHorizontalWidths, which again calls shrinkToFit ->
           stack overflow.
        computeHorizontalLengths(context);
        if (contentWidth != AUTO) {
            width = leftBorderWidth + leftPadding
                + contentWidth + rightPadding
                + rightBorderWidth;
        } else {
            width = UNINITIALIZED;
        }
        */

        // All we really care about is having margins, padding
        // and content width initialized... as a bonus, this
        // is cheaper than computeHorizontalLengths() too!
        // Set parent width to 0 to force percentages to evaluate to 0
//        Value val = CssLookup.getValue(element, XhtmlCss.WIDTH_INDEX);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.WIDTH_INDEX);
        CssValue cssValue = computeWidthCssValue();

        //boolean uncompute = false;
//        if (val == CssValueConstants.AUTO_VALUE) {
        if (cssValue == null // XXX #6460007 Possible NPE.
        || CssProvider.getValueService().isAutoValue(cssValue)) {
            if (replaced) {
                contentWidth = getIntrinsicWidth();
            } else {
                contentWidth = 0;
                //uncompute = true;
            }
        } else {
//            contentWidth = (int)val.getFloatValue();
            contentWidth = (int)cssValue.getFloatValue();
            //uncompute = cssValue instanceof CssComputedValue &&
            //    CssProvider.getValueService().
            //    isOfPrimitivePercentageType(((CssComputedValue)cssValue).getCascadedValue());
        }
        
        //if (uncompute) {
            // We have now initialized the width value for the element,
            // possibly using wrong containing block widths (they
            // are often not set when doing a width prescan via
            // getPrefMinWidth etc.)   The problem is that when we're
            // doing a "real" layout the old computed value is cached.
            // So we want to "recompute" this value.
        if(cssValue instanceof CssComputedValue) {
            uncomputeWidthCssValue();
        }
        //}
    }
    
    private void initializeContainingBlock() {
        // No containing block for children - 100% etc. should
        // evaluate to 0.
        
        // XXX That seems to be incorrect, see #6504407.
//        containingBlockX = 0;
//        containingBlockY = 0;
//        containingBlockWidth = 0;
//        containingBlockHeight = 0;
        // XXX Overriding the default behavior, initing to 0.
        containingBlockX = 0;
        containingBlockY = 0;
        
        int containerWidth = getParentContainerLength(this, XhtmlCss.WIDTH_INDEX);
        int containerHeight = getParentContainerLength(this, XhtmlCss.HEIGHT_INDEX);
        
        int componentWidth = getIntrinsicWidth();
        int componentHeight = getIntrinsicHeight();
        containingBlockWidth = componentWidth > containerWidth ? containerWidth : componentWidth;
        containingBlockHeight = componentHeight > containerHeight ? containerHeight : componentHeight;
    }
    
    private static int getParentContainerLength(CssBox cssBox, int propIndex) {
        ContainerBox parent = cssBox.getParent();
        while (parent != null) {
            Element element = parent.getElement();
            if (element != null) {
                int length = CssUtilities.getCssLength(element, propIndex);
                if (length != AUTO) {
                    return length;
                }
            }
            parent = parent.getParent();
        }
        return 0;
    }
    
    // XXX See overriding in JspIncludeBox.
    protected CssValue computeWidthCssValue() {
        return CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.WIDTH_INDEX);
    }
    // XXX See overriding in JspIncludeBox.
    protected void uncomputeWidthCssValue() {
        CssProvider.getEngineService().uncomputeValueForElement(element, XhtmlCss.WIDTH_INDEX);
    }

    /** Set the index of this box in the parent's box list */
    void setParentIndex(int idx) {
        parentIndex = idx;
    }

    /** Get the index of this box in the parent's box list */
    protected int getParentIndex() {
        return parentIndex;
    }

    // CSS Formatting related routines

    /**
      * Compute widths and margins, as discussed in section
      * 10.3 of the CSS2 spec:
      * http://www.w3.org/TR/CSS21/visudet.html#Computing_widths_and_margins
      * <p>
      * NOTE: This may have the side effect of setting
      * contentHEIGHT for replaced elements. This is because
      * the spec calls for special treatment of width and height
      * for replaced elements, in order to preserve the aspect ratio
      * of the intrinsic size of the replaced elements. If we first
      * solve the content width without looking at the height, we end
      * up having to compute the aspect ratio in the vertical computation
      * method since it will no longer be the case that both width and
      * height are auto (which initially tells us that we should use
      * the intrinsic size for both).
      * <p>
      * <b>Note</b>: While this method computes horizontal dimensions,
      * it also initializes box.contentHeight from its css property!
      * It may not compute the value (and will happily leave it AUTO)
      * but it is properly initialized for use by computeVerticalLengths.
      * <p>
      * <b>Note</b>: I changed the implementation of 10.3.8 since the
      * rules seemed a bit incorrect; see details in the relevant method.
      *
      * @param parentWidth the width of the containing block established
      *   by the parent.
      * @todo I don't think I'm computing it right for absolute boxes
      *   positioned via right/bottom. I end up setting left=staticLeft
      *   even when I have everything else computed - then I end up discovering
      *   I'm overconstrained, and I throw away the right value (the one that's
      *   set correctly!) instead of left, which is the one I "invented" using
      *   getStaticLeft.  Ditto for vertical computations. Look back at the
      *   rules and fix this.
      */
    void computeHorizontalLengths(FormatContext context) {
        int parentWidth = containingBlockWidth;

        assert boxType != BoxType.LINEBOX : this;

        // Initialize width
        Element element = getElement();

        if (element != null) {
            // This will initialize the height too.
            // We have to initialize this early, since in case of replaced elements,
            // we want to maintain the aspect ratio if only one of content width
            // and height are set to auto. The only way we can check that is if we
            // initialize both now.
            initializeContentSize();

            // Initialize left/right - only defined for positioned elements
            if (boxType.isPositioned()) {
//                left = CssLookup.getLength(element, XhtmlCss.LEFT_INDEX);
//                right = CssLookup.getLength(element, XhtmlCss.RIGHT_INDEX);
                left = CssUtilities.getCssLength(element, XhtmlCss.LEFT_INDEX);
                right = CssUtilities.getCssLength(element, XhtmlCss.RIGHT_INDEX);
            }

            if (isBorderSizeIncluded()) {
                // These boxes (like <input>, <select>, etc.) behave in the standard
                // box model way, but widths and heights specified for them refer to the
                // border box, not the content box, so adjust the specified width to the
                // standard notation before computing.  NOTE: I have not found where in the
                // spec this is described, this is only observed empirically -- browsers
                // treat these tags differently. Please insert spec reference here if
                // anyone knows where this is spec'ed.
                if (contentWidth != AUTO) {
                    contentWidth -= (leftBorderWidth + leftPadding + rightPadding +
                    rightBorderWidth);
                }

                if (contentHeight != AUTO) {
                    contentHeight -= (topBorderWidth + topPadding + bottomPadding +
                    bottomBorderWidth);
                }
            }
        } else {
            contentWidth = AUTO;
            contentHeight = AUTO;

            // XXX what about the others?
        }

        if (boxType == BoxType.FLOAT) {
            computeHorizontalFloat(context, parentWidth);
        } else if (!replaced && boxType.isAbsolutelyPositioned()) {
            computeHorizontalNonReplacedAbsPos(context, parentWidth);
        } else if (replaced && boxType.isAbsolutelyPositioned()) {
            computeHorizontalReplacedAbsPos(context, parentWidth);
        } else if (inline) {
            computeHorizInlineNormal();
        } else if (!inline && boxType.isNormalFlow()) {
            computeHorizNonInlineNormalFlow(context, parentWidth);
        } else {
            // how did we get here?
            assert false : this + ";" + boxType + ";" + inline + ";" + replaced;
        }

        if (boxType == BoxType.RELATIVE) {
            // Same as normal above, but normal flow computations don't
            // consider the left and right values so fix them up here
            // as described in section 9.4.3 of the CSS2.1 spec
            if ((left == AUTO) && (right == AUTO)) {
                left = 0;
                right = 0;
            } else if (left == AUTO) {
                left = -right;
            } else if (right == AUTO) {
                right = -left;
            } else { // overconstrained
                right = -left; // LTR assumption
            }
        }
    }

    private void computeHorizontalFloat(FormatContext context, int parentWidth) {
        // Left and right don't apply, do they?
        if (left == AUTO) {
            left = 0;
        }

        if (right == AUTO) {
            right = 0;
        }

        if (leftMargin == AUTO) {
            leftMargin = 0;
        }

        if (rightMargin == AUTO) {
            rightMargin = 0;
        }

        if (replaced) {
            // 10.3.6: floating replaced elements
            updateAutoContentSize();
        } else {
            // 10.3.5: floating non-replaced elements
            if (contentWidth == AUTO) {
                // This was revised in CSS2.1: Should use shrink to fit.
                int availableWidth =
                    parentWidth - leftMargin - left - leftBorderWidth - leftPadding - rightPadding -
                    rightBorderWidth - rightMargin - right;
                contentWidth = shrinkToFit(availableWidth, context);
            }
        }
    }

    private void computeHorizontalNonReplacedAbsPos(FormatContext context, int parentWidth) {
        // Section 10.3.7: Absolutely positioned, non-replaced elements
        // For BoxType.FIXED, the containing block is the
        // initial containing block instead of the viewport
        // (for the purpose of computing widths, margins, etc.)
        if (boxType == BoxType.FIXED) {
            parentWidth = getInitialWidth(context);
        }

        if ((left == AUTO) && (right == AUTO) && (contentWidth == AUTO)) {
            if (leftMargin == AUTO) {
                leftMargin = 0;
            }

            if (rightMargin == AUTO) {
                rightMargin = 0;
            }

            left = getStaticLeft(context);

            // Apply CSS21 section 10.3.7's rule 3:
            int availableWidth =
                parentWidth - leftMargin - 0 //right=0
                 -leftBorderWidth - leftPadding - rightPadding - rightBorderWidth - rightMargin -
                left;
            contentWidth = shrinkToFit(availableWidth, context);
            right =
                parentWidth - left - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                rightPadding - rightBorderWidth - rightMargin;
        } else if ((left != AUTO) && (right != AUTO) && (contentWidth != AUTO)) {
            if ((leftMargin == AUTO) && (rightMargin == AUTO)) {
                int leftOver =
                    parentWidth - left - leftBorderWidth - leftPadding - contentWidth -
                    rightPadding - rightBorderWidth;
                int margin = leftOver / 2;
                int remainder = leftOver % 2;
                leftMargin = margin;
                rightMargin = margin + remainder;
            } else if ((leftMargin != AUTO) && (rightMargin == AUTO)) {
                rightMargin =
                    parentWidth - left - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                    rightPadding - rightBorderWidth - right;
            } else if ((leftMargin == AUTO) && (rightMargin != AUTO)) {
                leftMargin =
                    parentWidth - left - leftBorderWidth - leftPadding - contentWidth -
                    rightPadding - rightBorderWidth - rightMargin - right;
            } else { // Overconstrained

                // Ignore value for right and solve for that value
                right =
                    parentWidth - left - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                    rightPadding - rightBorderWidth - rightMargin;
            }
        } else {
            if (leftMargin == AUTO) {
                leftMargin = 0;
            }

            if (rightMargin == AUTO) {
                rightMargin = 0;
            }

            // Perform one of rules 1-6 in section 10.3.7
            if ((left == AUTO) && (contentWidth == AUTO) && // (1)
                    (right != AUTO)) {
                // calculate the available width: this is found by
                // solving for 'width' after setting 'left' (in
                // case 1) or 'right' (in case 3) to 0.
                int availableWidth =
                    parentWidth - leftMargin - 0 //left=0
                     -leftBorderWidth - leftPadding - rightPadding - rightBorderWidth -
                    rightMargin - right;
                contentWidth = shrinkToFit(availableWidth, context);
                left =
                    parentWidth - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                    rightPadding - rightBorderWidth - rightMargin - right;
            } else if ((left == AUTO) && (right == AUTO) && (contentWidth != AUTO)) { // (2)

                // Since we're ltr
                left = getStaticLeft(context);
                right =
                    parentWidth - left - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                    rightPadding - rightBorderWidth - rightMargin;
            } else if ((contentWidth == AUTO) && (right == AUTO) && (left != AUTO)) { // (3)

                // calculate the available width: this is found by
                // solving for 'width' after setting 'left' (in
                // case 1) or 'right' (in case 3) to 0.
                int availableWidth =
                    parentWidth - leftMargin - 0 //right=0
                     -leftBorderWidth - leftPadding - rightPadding - rightBorderWidth -
                    rightMargin - left;
                contentWidth = shrinkToFit(availableWidth, context);
                right =
                    parentWidth - left - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                    rightPadding - rightBorderWidth - rightMargin;
            } else if ((left == AUTO) && (contentWidth != AUTO) && (right != AUTO)) { // (4)
                left =
                    parentWidth - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                    rightPadding - rightBorderWidth - rightMargin - right;
            } else if ((left != AUTO) && (contentWidth == AUTO) && (right != AUTO)) { // (5)
                contentWidth =
                    parentWidth - left - leftMargin - leftBorderWidth - leftPadding - rightPadding -
                    rightBorderWidth - rightMargin - right;
            } else if ( // This is a common case - move it up
                (left != AUTO) && (contentWidth != AUTO) && (right == AUTO)) { // (6)
                right =
                    parentWidth - left - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                    rightPadding - rightBorderWidth - rightMargin;
            }
        }

        // Check that we made it:
        assert (left + leftMargin + leftBorderWidth + leftPadding + contentWidth + rightPadding +
        rightBorderWidth + rightMargin + right) == parentWidth;
    }

    private void computeHorizontalReplacedAbsPos(FormatContext context, int parentWidth) {
        // Section 10.3.8: Absolutely positioned, replaced elements
        // For BoxType.FIXED, the containing block is the
        // initial containing block instead of the viewport
        // (for the purpose of computing widths, margins, etc.)
        if (boxType == BoxType.FIXED) {
            parentWidth = getInitialWidth(context);
        }

        updateAutoContentSize();

        if (leftMargin == AUTO) {
            leftMargin = 0;
        }

        if (rightMargin == AUTO) {
            rightMargin = 0;
        }

        // XXX The following is not in the rule substitution list
        // for 10.3.8 in CSS21, but probably what was intended
        if ((left != AUTO) && (right == AUTO)) {
            right =
                parentWidth - left - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                rightPadding - rightBorderWidth - rightMargin;
        } else if ((left == AUTO) && (right != AUTO)) {
            left =
                parentWidth - right - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                rightPadding - rightBorderWidth - rightMargin;
        } else
        // .... now back to your regular programming, courtesy
        // of channel 10.3.8 on the CSS21 network
        if (left == AUTO) { // (2)
            left = getStaticLeft(context); // ltr
        }

        // skipping 10.3.8's step 3 since we're not in ltr
        if (right == AUTO) { // (4)

            if (leftMargin == AUTO) {
                leftMargin = 0;
            }

            if (rightMargin == AUTO) {
                rightMargin = 0;
            }
        }

        if ((leftMargin == AUTO) && (rightMargin == AUTO)) { // (5)

            int leftOver =
                parentWidth - left - leftBorderWidth - leftPadding - contentWidth - rightPadding -
                rightBorderWidth - right;
            int margin = leftOver / 2;
            int remainder = leftOver % 2;
            leftMargin = margin;
            rightMargin = margin + remainder;
        }

        int numAuto = 0;

        if (contentWidth == AUTO) {
            numAuto++;
        }

        if (leftMargin == AUTO) {
            numAuto++;
        }

        if (rightMargin == AUTO) {
            numAuto++;
        }

        if (right == AUTO) {
            numAuto++;
        }

        // left can't be auto (in ltr) since we've applied
        // rule 2
        if (numAuto == 1) { // (6)

            int total =
                parentWidth - left - leftBorderWidth - rightBorderWidth - leftPadding -
                rightPadding;
            Equation equation =
                new Equation(total, new int[] { leftMargin, rightMargin, contentWidth, right });
            equation.solve();

            switch (equation.index) {
            case 0:
                leftMargin = equation.value;

                break;

            case 1:
                rightMargin = equation.value;

                break;

            case 2:
                contentWidth = equation.value;

                break;

            case 3:
                right = equation.value;

                break;
            }
        } else { // (7)
            assert numAuto == 0;

            // Overconstrained. Since we're in a ltr context, ignore
            // the right and recompute it to make the equality
            // true.
            right =
                parentWidth - left - leftMargin - leftBorderWidth - leftPadding - contentWidth -
                rightPadding - rightBorderWidth - rightMargin;
        }

        // Check that we made it:
        assert (left + leftMargin + leftBorderWidth + leftPadding + contentWidth + rightPadding +
        rightBorderWidth + rightMargin + right) == parentWidth;
    }

    private void computeHorizInlineNormal() {
        // Section 10.3.1, 10.3.2, 10.6.1, 10.6.2:
        // inline replaced or non-replaced elements
        if (replaced) {
            // 10.3.2, 10.6.2
            updateAutoContentSize();
        }

        //        else {
        //            // "width" and "height" does not apply.
        //            // The width is given by the text width; section 10.6.1
        //            // says that the height of the box is given by the
        //            // line-height property, but that seems wrong - how can
        //            // it then be vertically aligned if all the boxes are
        //            // the line-height?
        //        }
        //do not set left, right, top and bottom for relative boxes
        if(getBoxType() != BoxType.RELATIVE) {
            if (left == AUTO) {
                left = 0;
            }

            if (right == AUTO) {
                right = 0;
            }

            if (top == AUTO) {
                top = 0;
            }

            if (bottom == AUTO) {
                bottom = 0;
            }
        }
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
    }

    protected void computeHorizNonInlineNormalFlow(FormatContext context, int parentWidth) {
        // Section 10.3.3: block-level, non-replaced elements, normal flow
        // Section 10.3.4: block-level, replaced elements in normal flow
        if (left == AUTO) {
            left = 0;
        }

        if (right == AUTO) {
            right = 0;
        }

        if (replaced && (contentWidth == AUTO)) {
            updateAutoContentSize();
        }

        // Count the number of "auto"'s we're dealing with
        int numAuto = 0;

        if (contentWidth == AUTO) {
            numAuto++;
        }

        if (leftMargin == AUTO) {
            numAuto++;
        }

        if (rightMargin == AUTO) {
            numAuto++;
        }

        // This equation must be true:
        // leftMargin+leftWidth+leftPadding+width+rightPadding+rightWidth+rightMargin = parentWidth
        if (numAuto == 0) {
            // Overconstrained. Since we're in a ltr context, ignore
            // the rightMargin and recompute it to make the equality
            // true.
            rightMargin =
                parentWidth - contentWidth - leftMargin - leftBorderWidth - leftPadding -
                rightPadding - rightBorderWidth;
        } else if (numAuto == 1) {
            // "easy" - just solve this equation for the AUTO parameter:
            // leftMargin+leftWidth+leftPadding+width+rightPadding+rightWidth+rightMargin=parentWidth
            int total =
                parentWidth - leftBorderWidth - rightBorderWidth - leftPadding - rightPadding;
            Equation equation =
                new Equation(total, new int[] { leftMargin, rightMargin, contentWidth });
            equation.solve();

            switch (equation.index) {
            case 0:
                leftMargin = equation.value;

                break;

            case 1:
                rightMargin = equation.value;

                break;

            case 2:
                contentWidth = equation.value;

                break;
            }

            // XXX I could automate these kinds of computations
            // if instead of having leftPadding, rightPadding etc.
            // be direct fields in the CssBox class; they could
            // instead be fields in an array, e.g.
            // span[LEFTMARGIN], span[RIGHTMARGIN], span[WIDTH], etc.
            // Food for thought.
        } else if ((numAuto == 2) && (leftMargin == AUTO) && (rightMargin == AUTO)) {
            // Same case as above - except here the spec calls for
            // computing equal values for left and right margins -
            // thus the equation becomes
            // 2*margin+leftWidth+leftPadding+width+rightPadding+rightWidth=parentWidth
            int leftOver =
                parentWidth - contentWidth - leftBorderWidth - leftPadding - rightPadding -
                rightBorderWidth;
            int margin = leftOver / 2;
            int remainder = leftOver % 2;
            leftMargin = margin;
            rightMargin = margin + remainder;
        } else if (contentWidth == AUTO) {
            // Set all the other auto values to zero and compute the
            // width from the equality
            if (leftMargin == AUTO) {
                leftMargin = 0;
            }

            if (rightMargin == AUTO) {
                rightMargin = 0;
            }

            if (leftPadding == AUTO) {
                leftPadding = 0;
            }

            if (rightPadding == AUTO) {
                rightPadding = 0;
            }

            if (leftBorderWidth == AUTO) {
                leftBorderWidth = 0;
            }

            if (rightBorderWidth == AUTO) {
                rightBorderWidth = 0;
            }

            contentWidth =
                parentWidth - leftMargin - leftBorderWidth - leftPadding - rightPadding -
                rightBorderWidth - rightMargin;
        } else {
            // "underconstrained". We have auto for too many parameters.
            // The spec does not address this (at least not in
            // the relevant section, 10.3.)
            // XXX NO - I just realized padding can't be AUTO!
            // (Not an option).  So it shouldn't be a
            // problem. Simplify this by presubtracting the total
            // and don't include padding in the parameters.
            // For now, just set them all to 0
            if (leftMargin == AUTO) {
                leftMargin = 0;
            }

            if (rightMargin == AUTO) {
                rightMargin = 0;
            }

            if (leftPadding == AUTO) {
                leftPadding = 0;
            }

            if (rightPadding == AUTO) {
                rightPadding = 0;
            }

            if (leftBorderWidth == AUTO) {
                leftBorderWidth = 0;
            }

            if (rightBorderWidth == AUTO) {
                rightBorderWidth = 0;
            }
        }

        // Check that we made it:
        assert (leftMargin + leftBorderWidth + leftPadding + contentWidth + rightPadding +
        rightBorderWidth + rightMargin) == parentWidth;
    }

    /** Compute the horizontal "static position" of an element.
     * This is defined in section 10.3.7 of the CSS2.1 spec:
     * <blockquote>
     *  The static position for 'left' is the distance from the left
     *  edge of the containing block to the left margin edge of a
     *  hypothetical box that would have been the first box of the
     *  element if its 'position' property had been 'static'. The
     *  value is negative if the hypothetical box is to the left of
     *  the containing block
     * </blockquote>
     */
    private int getStaticLeft(FormatContext context) {
        /*
        Log.err.log("getStaticLeft computation is bustabazoink -- linebox is bogus in FormatContext");
        CssBox block = getBlockBox();
        return block.getX(); // XXXX This is probably still bogus!
        */

        // XXX this isn't right!
        // TODO getStaticLeft computation is bustabazoink -- linebox is bogus in FormatContext
        if (isInlineBox() && (context.lineBox != null) && context.lineBox.canFit(this)) {
            // XXX what if we have an inline box but it should be right
            // shifted due to floats???
            return context.lineBox.getNextX();
        } else {
            // Block boxes are placed exactly at the containing block
            // boundary so there's no distance
            return 0;
        }
    }

    /** Compute position of the current linebox - even if one hasn't
     * been started yet.
     */
    private int getLineBoxY(FormatContext context) {
        int py;

        if (context.lineBox != null) {
            py = context.lineBox.getY();
        } else {
            // Compute location where the line box should appear.
            CssBox prevBox = getPrevNormalBox();

            if (prevBox != null) {
                // Attach below bottom of previous block box
                int margin = prevBox.effectiveBottomMargin;
                py = prevBox.getY() + prevBox.getHeight() + margin;
            } else {
                // Attach at the top of the box
                //CssBox block = getBlockBox();
                // The above doesn't work because it aborts at inline parents
                // that are absolutely positioned; I don't want that here I think
                CssBox blockBox = this;

                while (blockBox.inline /* && !blockBox.boxType.isAbsolutelyPositioned()*/) {
                    blockBox = blockBox.parent;
                }

                py = blockBox.topPadding + blockBox.topBorderWidth;
            }
        }

        return py;
    }

    /** If this is a block-level box, return self, otherwise
     * return the nearest block-level ancestor box.
     * Note that absolutely positioned inline boxes are considered
     * block boxes, and so are replaced inline boxes (e.g. iframe, stringbox).
     */
    protected ContainerBox getBlockBox() {
        // XXX what about floats?
        if (!inline || boxType.isAbsolutelyPositioned()) {
            return (ContainerBox)this;
        } else {
            CssBox blockBox = this;

            while (blockBox.inline && !blockBox.boxType.isAbsolutelyPositioned() &&
                    !blockBox.replaced || !(blockBox instanceof ContainerBox)) {
                blockBox = blockBox.parent;
            }

            return (ContainerBox)blockBox;
        }
    }

    /** Get the most recent normal-flow box prior to the current box
     * in the parent's box list */
    protected CssBox getPrevNormalBox() {
        ContainerBox parent = getParent();
        if (parent == null) {
            return null;
        }
        
        // XXX #98826 Workaround of the parentage linkning issue. Incorrect parent assigned.
        if (!parent.containsChild(this)) {
            return null;
        }

        for (int i = getParentIndex() - 1; i >= 0; i--) {
            CssBox prev = parent.getBox(i);

	    // XXX #6329717 NPE. TODO It seems that the access to the container CssBox children
	    // is done from various threads, and it is not in sync.
	    if (prev == null) {
		continue;
	    }
	    
            ///if this is a linebox group, though, we need to check whether its
            //container is normal flow or not
            if(prev.getBoxType() == BoxType.LINEBOX) {
                if (!prev.getParent().getBoxType().isNormalFlow()) {
                    continue;
                }
            }
            
            if (prev.getBoxType().isNormalFlow()) {
                return prev;
            }
        }

        return null;
    }

    /** Get the next normal-flow box after to the current box
     * in the parent's box list */
    protected CssBox getNextNormalBox() {
        ContainerBox parent = getParent();
        if (parent == null) {
            return null;
        }

        // XXX #98826 Workaround of the parentage linkning issue. Incorrect parent assigned.
        if (!parent.containsChild(this)) {
            return null;
        }
        
        int n = parent.getBoxCount();

        for (int i = getParentIndex() + 1; i < n; i++) {
            CssBox next = parent.getBox(i);

	    // XXX #6329717 NPE. TODO It seems that the access to the container CssBox children
	    // is done from various threads, and it is not in sync.
	    if (next == null) {
		continue;
	    }
	    
            if (next.getBoxType().isNormalFlow()) {
                return next;
            }
        }

        return null;
    }

    /** Get the most recent normal-flow box prior to the current box
     * in the parent's box list that is also a block box. Lineboxes
     * are considered block level for this purpose. Same as getPrevNormalBox
     * except we also include LineBoxes. */
    protected CssBox getPrevNormalBlockBox() {
        ContainerBox parent = getParent();
        if (parent == null) {
            return null;
        }

        // XXX #98826 Workaround of the parentage linkning issue. Incorrect parent assigned.
        if (!parent.containsChild(this)) {
            return null;
        }
        
        for (int i = getParentIndex() - 1; i >= 0; i--) {
            CssBox prev = parent.getBox(i);

	    // XXX #6329717 NPE. TODO It seems that the access to the container CssBox children
	    // is done from various threads, and it is not in sync.
	    if (prev == null) {
		continue;
	    }
	    
            if (prev.getBoxType().isNormalFlow() &&
                    ((prev.getBoxType() == BoxType.LINEBOX) || prev.isBlockLevel())) {
                return prev;
            }
        }

        return null;
    }

    /** Get the next normal-flow box after to the current box
     * in the parent's box list that is also a block box. Lineboxes
     * are considered block level for this purpose. Same as getNextNormalBox
     * except we also include LineBoxes. */
    protected CssBox getNextNormalBlockBox() {
        ContainerBox parent = getParent();
        if (parent == null) {
            return null;
        }
        
        // XXX #98826 Workaround of the parentage linkning issue. Incorrect parent assigned.
        if (!parent.containsChild(this)) {
            return null;
        }

        int n = parent.getBoxCount();

        for (int i = getParentIndex() + 1; i < n; i++) {
            CssBox next = parent.getBox(i);

	    // XXX #6329717 NPE. TODO It seems that the access to the container CssBox children
	    // is done from various threads, and it is not in sync.
	    if (next == null) {
		continue;
	    }
	    
            if (next.getBoxType().isNormalFlow() &&
                    ((next.getBoxType() == BoxType.LINEBOX) || next.isBlockLevel())) {
                return next;
            }
        }

        return null;
    }

    /** Get the first normal-flow child box */
    CssBox getFirstNormalBox() {
        int n = getBoxCount();

        for (int i = 0; i < n; i++) {
            CssBox first = getBox(i);

            if (first.getBoxType().isNormalFlow()) {
                return first;
            }
        }

        return null;
    }

    /** Get the last normal-flow child box */
    private CssBox getLastNormalBox() {
        for (int i = getBoxCount() - 1; i >= 0; i--) {
            CssBox last = getBox(i);

            if (last.getBoxType().isNormalFlow()) {
                return last;
            }
        }

        return null;
    }

//    /** Get the first normal-flow child box that is also a block box.
//     * Lineboxes are considered block for this purpose.
//     */
//    CssBox getFirstNormalBlockBox() {
//        int n = getBoxCount();
//
//        for (int i = 0; i < n; i++) {
//            CssBox first = getBox(i);
//
//            if (first.getBoxType().isNormalFlow() &&
//                    ((first.getBoxType() == BoxType.LINEBOX) || first.isBlockLevel())) {
//                return first;
//            }
//        }
//
//        return null;
//    }
//
//    /** Get the last normal-flow child box that is also a block box.
//     * Lineboxes are considered block for this purpose.
//     */
//    CssBox getLastNormalBlockBox() {
//        for (int i = getBoxCount() - 1; i >= 0; i--) {
//            CssBox last = getBox(i);
//
//            if (last.getBoxType().isNormalFlow() &&
//                    ((last.getBoxType() == BoxType.LINEBOX) || last.isBlockLevel())) {
//                return last;
//            }
//        }
//
//        return null;
//    }

    /** Compute the vertical "static position" of an element.
     * This is defined in section 10.6.4 of the CSS2.1 spec.
     * See also section 10.3.7 and the getStaticLeft() method.
     */
    private int getStaticTop(FormatContext context) {
        return getLineBoxY(context);
    }

    protected int shrinkToFit(int availableWidth, FormatContext context) {
        // XXX does width refer to the content width, or border
        // width, or what, here?
        // Calculate the preferred width by formatting the content
        // without breaking lines other than where explicit line
        // breaks occur, and also calculate the preferred minimum
        // width, e.g. by trying all possible line breaks. CSS2.1
        // does not define the exact algorithm.
        //
        // Then the shrink-to-fit width is:
        // min(max(preferred minimum width, available width), preferred width).
        // This box has already been width initialized, so only initialize
        // its children
        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox box = getBox(i);

            // We don't care about absolute/fixed children!
            if (box.getBoxType().isAbsolutelyPositioned()) {
                continue;
            }
            box.uncomputeWidthCssValue();
            box.initializeHorizontalWidths(context);
        }

        // XXX #109564 This loop was commented out (from unknown reasons here),
        // it was present in 5.5, and it fixes the mentioned issue.
        //XXX TODO 
        //temporary fix for 6480767. see bug info for more details
        //for all boxes within linebox, if the box width is in %, 
        //uninitialize contentWidth, so that intrinsic width is taken        
        for (int i = 0; i < getBoxCount(); i++) {
            CssBox box = getBox(i);
            if(box instanceof LineBoxGroup) {
                for (int j = 0; j < ((LineBoxGroup)box).getManagedBoxes().size(); j++) {
                    CssBox aFloat = ((LineBoxGroup)box).getManagedBoxes().get(j);
                    CssValue cssValue = CssProvider.getEngineService().
                            getComputedValueForElement(aFloat.element, XhtmlCss.WIDTH_INDEX);
                    if (cssValue != null
                            && !CssProvider.getValueService().isAutoValue(cssValue) &&
                            cssValue instanceof CssComputedValue &&
                            CssProvider.getValueService().
                            isOfPrimitivePercentageType(((CssComputedValue)cssValue).getCascadedValue())) {
                        aFloat.contentWidth = UNINITIALIZED;
                        CssProvider.getEngineService().uncomputeValueForElement(aFloat.element, XhtmlCss.WIDTH_INDEX);
                    }
                }
            }
        }

        int preferred = getPrefWidth();
        int minimum = getPrefMinWidth();
        int shrinkFit = Math.min(Math.max(minimum, availableWidth), preferred);

//        MarkupDesignBean bean = getMarkupDesignBeanForCssBox(this);
        Element componentRootElement = getElementForComponentRootCssBox(this);
//        if ((shrinkFit <= (leftBorderWidth + rightBorderWidth)) && (bean != null)) { // XXX padding too?
        if ((shrinkFit <= (leftBorderWidth + rightBorderWidth)) && (componentRootElement != null)) { // XXX padding too?
            shrinkFit = MINIMUM_BEAN_SIZE;

//            if ((border == null) && ((parent == null) || (parent.getDesignBean() != bean)) &&
//            if ((border == null) && ((parent == null) || (getMarkupDesignBeanForCssBox(parent) != bean)) &&
            if ((border == null) && ((parent == null) || (getElementForComponentRootCssBox(parent) != componentRootElement)) &&
                    (tag != HtmlTag.FORM)) {
                border = CssBorder.getDesignerBorder();
                leftBorderWidth = border.getLeftBorderWidth();
                topBorderWidth = border.getTopBorderWidth();
                bottomBorderWidth = border.getBottomBorderWidth();
                rightBorderWidth = border.getRightBorderWidth();
            }
        }
        
        return shrinkFit;
    }
    
    private int computeContentHeight() {
        // CSS21, section 10.6.4 - rule 3 for example:
        // "then the height is based on the content" --
        // what do they mean? Something similar to shrinkToFit?
        // Is it the case that we've already done layout of the
        // children when we call this method, so we can look up the actual
        // width that was required? - but if we've done that, how could
        // the width still say auto? Do I store the computed children
        // height elsewhere?
        // (aha. something similar to the case in 10.6.3 where I look
        // at the children boxes and check their positions?)
        // XXX should I use box.width? Did layout set it?
        // XXX Check contentHeigeht attribute the way I'm doing it
        // in getprefminwidth!
        int result = 0;
        int maxY = 0;
        int n = getBoxCount();

        for (int i = 0; i < n; i++) {
            CssBox child = getBox(i);

            if (child.getBoxType().isNormalFlow()) {
                int bottom = child.getY() + child.getHeight();

                if (bottom > maxY) {
                    maxY = bottom;
                }
            }
        }

        if ((maxY == topPadding) && (topBorderWidth > 0)) {
            result = topBorderWidth;
        } else {
            result = maxY - topPadding - topBorderWidth;
        }

//        MarkupDesignBean bean = getMarkupDesignBeanForCssBox(this);
        Element componentRootElement = getElementForComponentRootCssBox(this);
        // XXX TODO - gotta add in margins here?
        // XXX gotta add in min-heights too? Nope, if height has already
        // been computed we should be okay.
        // I suspect the above only works on TextBoxes because their sizes
        // are computed at box creation time... I need to do more initialization
        // to compute additional heights from attributes, margin alignment, etc.
        // Design time borders for 0-sized components
//        if ((result <= topBorderWidth) && (bean != null) && // XXX add in padding too, not just borderwidth?
        if ((result <= topBorderWidth) && (componentRootElement != null) && // XXX add in padding too, not just borderwidth?
                !element.getTagName().equals(HtmlTag.HR.name)) { // <hr>'s COULD be 0 sized
            result = MINIMUM_BEAN_SIZE;
        }

        return result;
    }

    /**
     * Compute vertical heights and margins, as discussed in section
     * 10.6 of the CSS2 spec.
     *
     * @todo Revisit section 10.6.3, I'm not implementing that correctly
     *  (I might have implemented it according to the CSS2 spec, and in
     *  CSS21 there are a bunch of additional conditions related to
     *  margin collapsing)
     * @todo Floating box section 10.6.6 is not fully implemented.
     * <p>
     * <b>Note</b>: I changed the implementation of 10.6.5 since the
     * rules seemed a bit incorrect; see details in the relevant method.
     *
     * @param parentHeight the height of the containing block established
     *   by the parent.
     */
    void computeVerticalLengths(FormatContext context) {
        int parentHeight = containingBlockHeight;
        assert boxType != BoxType.LINEBOX : this;

        // Initialize height
        Element element = getElement();

        if (element != null) {
            // Initialize top/bottom - only defined for positioned elements
            if (boxType.isPositioned()) {
//                top = CssLookup.getLength(element, XhtmlCss.TOP_INDEX);
//                bottom = CssLookup.getLength(element, XhtmlCss.BOTTOM_INDEX);
                top = CssUtilities.getCssLength(element, XhtmlCss.TOP_INDEX);
                bottom = CssUtilities.getCssLength(element, XhtmlCss.BOTTOM_INDEX);
            }
        }

        if ((boxType == BoxType.FLOAT) && !replaced) {
            computeVerticalNonReplacedFloat();
        } else if (boxType.isAbsolutelyPositioned() && !replaced) {
            computeVerticalNonReplacedAbsPos(context, parentHeight);
        } else if (boxType.isAbsolutelyPositioned() && replaced) {
            computeVerticalReplacedAbsPos(context, parentHeight);
        } else if (inline && !replaced) {
            computeVerticalNonReplacedInline();
        } else if ((inline && replaced) || (replaced && !inline && boxType.isNormalFlow()) ||
                (replaced && (boxType == BoxType.FLOAT))) {
            computeVerticalSec10_6_2();
        } else if (!inline && !replaced && boxType.isNormalFlow()) {
            computeNonInlineNonReplacedNormal();
        }

        if (effectiveTopMargin == UNINITIALIZED) {
            effectiveTopMargin = topMargin;
        }

        if (effectiveBottomMargin == UNINITIALIZED) {
            effectiveBottomMargin = bottomMargin;
        }
    
        if (boxType == BoxType.RELATIVE) {
            // Same as normal above, but normal flow computations don't
            // consider the top and bottom values so fix them up here
            // as described in section 9.4.3 of the CSS2.1 spec
            if ((top == AUTO) && (bottom == AUTO)) {
                top = 0;
                bottom = 0;
            } else if (top == AUTO) {
                top = -bottom;
            } else if (bottom == AUTO) {
                bottom = -top;
            } else { // overconstrained
                bottom = -top; // always, not ltr dependent like widths
            }
        }
    }

    private void computeVerticalNonReplacedFloat() {
        // 10.6.6 Floating, non-replaced elements
        if (topMargin == AUTO) {
            topMargin = 0;
        }

        if (bottomMargin == AUTO) {
            bottomMargin = 0;
        }

        if (contentHeight == AUTO) {
            // This is similar to 10.6.3, but with a modification
            // to the height search where we look for floating
            // children and adjust the height to accomodate their margin
            // edges
            int top = Integer.MAX_VALUE;
            int bottom = Integer.MIN_VALUE;

            if (hasNormalBlockLevelChildren()) {
                // XXX This gets tricky. Children boxes may be anonymous
                // block boxes - but I haven't been creating those!
                // Figure out how to handle this....
                // XXX This is still not clear. Look at section 10.6.3
                // in CSS21 again.
                // distance is the distance between the top border-edge
                // of the topmost block-level child box that doesn't have
                // margins collapsed through it, and the bottom border-edge
                // of the bottommost block-level child box that doesn't
                // have margins collapsed through it.
                int distance = 0;

                //CssBox topBox = null;
                //CssBox bottomBox = null;
                int n = getBoxCount();

                for (int i = 0; i < n; i++) {
                    CssBox child = getBox(i);

                    if (child.getY() < top) {
                        top = child.getY();

                        //topBox = child;
                    }

                    if ((child.getY() + child.getHeight()) > bottom) {
                        bottom = child.getY() + child.getHeight();

                        //bottomBox = child;
                    }
                }

                //if (topBox != null) {
                if (top != Integer.MAX_VALUE) {
                    distance = bottom - top;
                }

                contentHeight = distance;
            } else {
                // distance = distance from the top of the topmost
                // line box and the bottom most line box
                int distance = 0;

                //LineBoxGroup topBox = null;
                //LineBoxGroup bottomBox = null;
                int n = getBoxCount();

                for (int i = 0; i < n; i++) {
                    CssBox child = getBox(i);

                    if (!(child instanceof LineBoxGroup)) {
                        continue;
                    }

                    LineBoxGroup lineBox = (LineBoxGroup)child;

                    if (lineBox.getY() < top) {
                        top = lineBox.getY();

                        //topBox = lineBox;
                    }

                    if ((lineBox.getY() + lineBox.getHeight()) > bottom) {
                        bottom = lineBox.getY() + lineBox.getHeight();

                        //bottomBox = lineBox;
                    }
                }

                if ((top != Integer.MAX_VALUE) && (bottom != Integer.MAX_VALUE)) {
                    //distance = bottomBox.bottom - topBox.top;
                    distance = bottom - top;
                }

                contentHeight = distance;
            }

            // "In addition, if the element has any floating
            // descendants whose top margin edge is above the top
            // established above or whose bottom margin edge is
            // below the bottom, then the height is increased to
            // include those edges. Only floats that are children of
            // the element itself or of descendants in the normal
            // flow are taken into account, i.e., floats inside
            // absolutely positioned descendants are not."
            // Adjust top and bottom as necessary
            for(int i = 0; i < getBoxCount(); i++) {
                CssBox box = getBox(i);
                if(box instanceof LineBoxGroup) {
                    int newSize = ((LineBoxGroup)box).getSizeWithFloats();
                    if(newSize > contentHeight) {
                        contentHeight = newSize;
                    }
                } else if (box != null && box.getBoxType() == BoxType.FLOAT) {
                    // XXX #99707 Counting floats. This is just a hack.
                    int newSize = box.getHeight();
                    if (newSize != UNINITIALIZED && newSize != AUTO && newSize > contentHeight) {
                        contentHeight = newSize;
                    }
                }
            }
        }
    }

    private void computeVerticalNonReplacedAbsPos(FormatContext context, int parentHeight) {
        // 10.6.4 Absolutely positioned, non-replaced elements
        // For BoxType.FIXED, the containing block is the
        // initial containing block instead of the viewport
        // (for the purpose of computing heights, margins, etc.)
        if (boxType == BoxType.FIXED) {
            parentHeight = getInitialHeight(context);
        }
        
        // XXX #6511830 Heavy hack to avoid fatal painting error,
        // can't deal with value AUTO here. But is using initial height correct?
        if (parentHeight == AUTO) {
            parentHeight = getInitialHeight(context);
        }

        // Must satisfy this constraint:
        //top+topMargin+topBorderWidth+topPadding+height+bottomPadding+bottomBorderWidth+bottomMargin+bottom==parentHeight
        if ((top == AUTO) && (bottom == AUTO) && (contentHeight == AUTO)) {
            top = getStaticTop(context);

            if (topMargin == AUTO) {
                topMargin = 0;
            }

            if (bottomMargin == AUTO) {
                bottomMargin = 0;
            }

            // 10.6.4, rule 3:
            // "then the height is based on the content" -- what do they
            // mean?
            contentHeight = computeContentHeight();
            bottom =
                parentHeight - top - topMargin - topBorderWidth - topPadding - contentHeight -
                bottomPadding - bottomBorderWidth - bottomMargin;
        } else if ((top != AUTO) && (bottom != AUTO) && (contentHeight != AUTO)) {
            if ((topMargin == AUTO) && (bottomMargin == AUTO)) {
                int leftOver =
                    parentHeight - top - topBorderWidth - topPadding - contentHeight -
                    bottomPadding - bottomBorderWidth - bottom;
                int margin = leftOver / 2;
                int remainder = leftOver % 2;
                topMargin = margin;
                bottomMargin = margin + remainder;
            } else if ((topMargin != AUTO) && (bottomMargin == AUTO)) {
                bottomMargin =
                    parentHeight - top - topMargin - topBorderWidth - topPadding - contentHeight -
                    bottomPadding - bottomBorderWidth - bottom;
            } else if ((topMargin == AUTO) && (bottomMargin != AUTO)) {
                topMargin =
                    parentHeight - top - topBorderWidth - topPadding - contentHeight -
                    bottomPadding - bottomBorderWidth - bottomMargin - bottom;
            } else { // Overconstrained

                // Ignore value for bottom and solve for that value
                bottom =
                    parentHeight - top - topMargin - topBorderWidth - topPadding - contentHeight -
                    bottomPadding - bottomBorderWidth - bottomMargin;
            }
        } else {
            // Perform one of rules 1-6 in section 10.6.4
            // The rules didn't pull out the "set auto values
            // for margin-top and margin-bottom to 0" part, but
            // included it in each rule; I've done the same (didn't
            // notice that it's included for all the rules until now).
            if ((top == AUTO) && (contentHeight == AUTO) && // (1)
                    (bottom != AUTO)) {
                // "then the height is based on the content" --
                // what do they mean?
                contentHeight = computeContentHeight();

                if (topMargin == AUTO) {
                    topMargin = 0;
                }

                if (bottomMargin == AUTO) {
                    bottomMargin = 0;
                }

                top = parentHeight - topMargin - topBorderWidth - topPadding - contentHeight -
                    bottomPadding - bottomBorderWidth - bottomMargin - bottom;
            } else if ((top == AUTO) && (bottom == AUTO) && (contentHeight != AUTO)) { // (2)
                top = getStaticTop(context); // ltr

                if (topMargin == AUTO) {
                    topMargin = 0;
                }

                if (bottomMargin == AUTO) {
                    bottomMargin = 0;
                }

                bottom =
                    parentHeight - top - topMargin - topBorderWidth - topPadding - contentHeight -
                    bottomPadding - bottomBorderWidth - bottomMargin;
            } else if ((contentHeight == AUTO) && (bottom == AUTO) && (top != AUTO)) { // (3)

                // "then the height is based on the content" --
                // what do they mean?
                contentHeight = computeContentHeight(); // XXX why would this return 0?

                if (topMargin == AUTO) {
                    topMargin = 0;
                }

                if (bottomMargin == AUTO) {
                    bottomMargin = 0;
                }

                bottom =
                    parentHeight - top - topMargin - topBorderWidth - topPadding - contentHeight -
                    bottomPadding - bottomBorderWidth - bottomMargin;
            } else if ((top == AUTO) && (contentHeight != AUTO) && (bottom != AUTO)) { // (4)

                if (topMargin == AUTO) {
                    topMargin = 0;
                }

                if (bottomMargin == AUTO) {
                    bottomMargin = 0;
                }

                top = parentHeight - topMargin - topBorderWidth - topPadding - contentHeight -
                    bottomPadding - bottomBorderWidth - bottomMargin - bottom;
            } else if ((top != AUTO) && (contentHeight == AUTO) && (bottom != AUTO)) { // (5)

                if (topMargin == AUTO) {
                    topMargin = 0;
                }

                if (bottomMargin == AUTO) {
                    bottomMargin = 0;
                }

                contentHeight =
                    parentHeight - top - topMargin - topBorderWidth - topPadding - bottomPadding -
                    bottomBorderWidth - bottomMargin - bottom;
            } else if ((top != AUTO) && (contentHeight != AUTO) && (bottom == AUTO)) { // (6)

                if (topMargin == AUTO) {
                    topMargin = 0;
                }

                if (bottomMargin == AUTO) {
                    bottomMargin = 0;
                }

                bottom =
                    parentHeight - top - topMargin - topBorderWidth - topPadding - contentHeight -
                    bottomPadding - bottomBorderWidth - bottomMargin;
            }
        }

        // Check that we made it
        assert (top + topMargin + topBorderWidth + topPadding + contentHeight + bottomPadding +
        bottomBorderWidth + bottomMargin + bottom) == parentHeight;
    }

    private void computeVerticalReplacedAbsPos(FormatContext context, int parentHeight) {
        // 10.6.5 Absolutely positioned, replaced elements
        // For BoxType.FIXED, the containing block is the
        // initial containing block instead of the viewport
        // (for the purpose of computing heights, margins, etc.)
        if (boxType == BoxType.FIXED) {
            parentHeight = getInitialHeight(context);
        }
        
        // XXX #6511830 Heavy hack to avoid fatal painting error,
        // can't deal with value AUTO here. But is using initial height correct?
        if (parentHeight == AUTO) {
            parentHeight = getInitialHeight(context);
        }

        updateAutoContentSize(); // (1)

        // XXX The following is not in the rule substitution list
        // for 10.6.5 in CSS21, but probably what was intended
        if ((top == AUTO) && (bottom != AUTO)) {
            if (topMargin == AUTO) {
                topMargin = 0;
            }

            if (bottomMargin == AUTO) {
                bottomMargin = 0;
            }

            top = parentHeight - bottom - topMargin - topBorderWidth - topPadding - contentHeight -
                bottomPadding - bottomBorderWidth - bottomMargin;
        } else if ((top != AUTO) && (bottom == AUTO)) {
            if (topMargin == AUTO) {
                topMargin = 0;
            }

            if (bottomMargin == AUTO) {
                bottomMargin = 0;
            }

            bottom =
                parentHeight - top - topMargin - topBorderWidth - topPadding - contentHeight -
                bottomPadding - bottomBorderWidth - bottomMargin;
        } else
        // .... now back to your regular programming, courtesy
        // of channel 10.3.8 on the CSS21 network
        if (top == AUTO) { // (2)
            top = getStaticTop(context); // ltr
        }

        if (bottom == AUTO) { // (3)

            if (topMargin == AUTO) {
                topMargin = 0;
            }

            if (bottomMargin == AUTO) {
                bottomMargin = 0;
            }
        }

        if ((topMargin == AUTO) && (bottomMargin == AUTO)) { // (4)

            int leftOver =
                parentHeight - top - topBorderWidth - topPadding - contentHeight - bottomPadding -
                bottomBorderWidth - bottom;
            int margin = leftOver / 2;
            int remainder = leftOver % 2;
            topMargin = margin;
            bottomMargin = margin + remainder;
        }

        int numAuto = 0;

        if (contentHeight == AUTO) {
            numAuto++;
        }

        if (topMargin == AUTO) {
            numAuto++;
        }

        if (bottomMargin == AUTO) {
            numAuto++;
        }

        if (bottom == AUTO) {
            numAuto++;
        }

        // top can't be auto since we've applied rule 2
        if (numAuto == 1) { // (5)

            int total =
                parentHeight - top - topBorderWidth - bottomBorderWidth - topPadding -
                bottomPadding;
            Equation equation =
                new Equation(total, new int[] { topMargin, bottomMargin, contentHeight, bottom });
            equation.solve();

            switch (equation.index) {
            case 0:
                topMargin = equation.value;

                break;

            case 1:
                bottomMargin = equation.value;

                break;

            case 2:
                contentHeight = equation.value;

                break;

            case 3:
                bottom = equation.value;

                break;
            }
        } else { // (6)
            assert numAuto == 0;

            // Overconstrained. Since we're in a ltr context, ignore
            // the bottomMargin and recompute it to make the equality
            // true.
            bottom =
                parentHeight - top - topMargin - topBorderWidth - topPadding - contentHeight -
                bottomPadding - bottomBorderWidth - bottomMargin;
        }

        // Check that we made it:
        assert (top + topMargin + topBorderWidth + topPadding + contentHeight + bottomPadding +
        bottomBorderWidth + bottomMargin + bottom) == parentHeight;
    }

    private void computeVerticalNonReplacedInline() {
        if (this instanceof TableBox) {
            // XXX #6494312 For inline tables this algorithm doesn't apply.
        } else {
            // 10.6.1: Inline, non-replaced elements
    //        Value heightValue = CssLookup.getValue(element, XhtmlCss.LINE_HEIGHT_INDEX);
            CssValue cssHeightValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.LINE_HEIGHT_INDEX);

    //        if (heightValue == CssValueConstants.NORMAL_VALUE) {
            if (CssProvider.getValueService().isNormalValue(cssHeightValue)) {
    //            contentHeight = (int)(1.1 * CssLookup.getFontSize(element, DesignerSettings.getInstance().getDefaultFontSize()));
//                contentHeight = (int)(1.1 * CssProvider.getValueService().getFontSizeForElement(element, DesignerSettings.getInstance().getDefaultFontSize()));
                contentHeight = (int)(1.1 * CssProvider.getValueService().getFontSizeForElement(element, webform.getDefaultFontSize()));
            } else {
    //            contentHeight = (int)heightValue.getFloatValue();
                contentHeight = (int)cssHeightValue.getFloatValue();
            }
        }

        // XXX this seems wrong!
        // XXX slow. Optimize!
        // Also, it doesn't handle cases where the are multiple differently
        // sized fonts in the line. See the spec for a suggestion on how
        // to calculate it.
        // 10.6.1 says: "The vertical padding, border and margin
        // of an inline, non-replaced box start at the top and
        // bottom of the content area, not the 'line-height'.
        // XXX Does this mean that vertical padding, borders and
        // margins should be zero for non-replaced inline
        // elements?
        topPadding = 0;
        bottomPadding = 0;
        topBorderWidth = 0;
        bottomBorderWidth = 0;
        topMargin = 0;
        bottomMargin = 0;

        // XXX Really unsure that the above is okay
    }

    private void computeVerticalSec10_6_2() {
        // 10.6.2 Inline replaced elements, block-level replaced elements
        // in normal flow, and floating replaced elements
        if (topMargin == AUTO) {
            topMargin = 0;
        }

        if (bottomMargin == AUTO) {
            bottomMargin = 0;
        }

        updateAutoContentSize();
    }

    private void computeNonInlineNonReplacedNormal() {
        // 10.6.3 Block-level, non-replaced elements in normal flow
        if (topMargin == AUTO) {
            topMargin = 0;
        }

        if (bottomMargin == AUTO) {
            bottomMargin = 0;
        }

        if (contentHeight == AUTO) {
            if (hasNormalBlockLevelChildren()) {
                // XXX This gets tricky. Children boxes may be anonymous
                // block boxes - but I haven't been creating those!
                // Figure out how to handle this....
                // XXX This is still not clear. Look at section 10.6.3
                // in CSS21 again.
                // distance is the distance between the top border-edge
                // of the topmost block-level child box that doesn't have
                // margins collapsed through it, and the bottom border-edge
                // of the bottommost block-level child box that doesn't
                // have margins collapsed through it.
                int distance = 0;
                int top = Integer.MAX_VALUE;
                int bottom = Integer.MIN_VALUE;

                //CssBox topBox = null;
                //CssBox bottomBox = null;
                int n = getBoxCount();

                for (int i = 0; i < n; i++) {
                    CssBox child = getBox(i);

                    if (!child.getBoxType().isNormalFlow()) {
                        continue;
                    }

                    if (child.getY() < top) {
                        top = child.getY();

                        //topBox = child;
                    }

                    if ((child.getY() + child.getHeight()) > bottom) {
                        bottom = child.getY() + child.getHeight();

                        //bottomBox = child;
                    }
                }

                //if (topBox != null) {
                if (top != Integer.MAX_VALUE) {
                    distance = bottom - top;
                }

                contentHeight = distance;
            } else {
                // distance = distance from the top of the topmost
                // line box and the bottom most line box
                int distance = 0;
                int top = Integer.MAX_VALUE;
                int bottom = Integer.MIN_VALUE;

                //LineBoxGroup topBox = null;
                //LineBoxGroup bottomBox = null;
                int n = getBoxCount();

                for (int i = 0; i < n; i++) {
                    CssBox child = getBox(i);

                    if (!child.getBoxType().isNormalFlow()) {
                        continue;
                    }

                    if (!(child instanceof LineBoxGroup)) {
                        continue;
                    }

                    LineBoxGroup lineBox = (LineBoxGroup)child;

                    if (lineBox.getY() < top) {
                        top = lineBox.getY();

                        //topBox = lineBox;
                    }

                    if ((lineBox.getY() + lineBox.getHeight()) > bottom) {
                        bottom = lineBox.getY() + lineBox.getHeight();

                        //bottomBox = lineBox;
                    }
                }

                if ((top != Integer.MAX_VALUE) && (bottom != Integer.MAX_VALUE)) {
                    //distance = bottomBox.bottom - topBox.top;
                    distance = bottom - top;
                } else {
                    distance = getIntrinsicHeight();
                }

                contentHeight = distance;
            }
        }
    }

    /**
     * Clear previous boxes such that this box is positioned below the
     * clear area.
     */
    protected void clearTop(FormatContext context, CssValue cssSide) {
        int cleared = context.clear(cssSide, this);

        if (cleared > Integer.MIN_VALUE) {
            int clearance = cleared - getAbsoluteY();

            if (clearance > 0) {
                y += clearance;
            }
        }
    }

    /**
     * Adjust height and contentHeight to clear floating
     * boxes on either (or both) sides within this box.
     */
    protected void clearBottom(FormatContext context, CssValue cssSide) {
        int cleared = context.clear(cssSide, null);

        if (cleared > Integer.MIN_VALUE) {
            int clearance = cleared - (getAbsoluteY() + getHeight());

            if (clearance > 0) {
                contentHeight += clearance;
                height += clearance;
            }
        }
    }

    /**
     * Check whether the given box has any block level children that
     * participate in normal flow (e.g. not floating boxes or
     * absolutely positioned boxes).
     *
     * @todo Gotta add positioning type to CssBox!
     *
     */
    protected boolean hasNormalBlockLevelChildren() {
        return false;
    }

    /** Return the width of the initial containing block */
    private int getInitialWidth(FormatContext context) {
        return context.initialWidth;
    }

    /** Return the height of the initial containing block */
    private int getInitialHeight(FormatContext context) {
        return context.initialHeight;
    }

    /**
     * Compute the content width/height settings for a replaced
     *  This will do nothing if the content width or height are
     * already set to something other than AUTO; however, if set to
     * AUTO it will compute a size which preserves the aspect
     * ratio. If both width and height are set to auto, it returns the
     * intrinsic size.  <p> See the errata since this was incorrect in
     * the spec.
     * http://www.w3.org/Style/css2-updates/REC-CSS2-19980512-errata.html
     * specifically section 10.3.2 (which also applies to 10.3.4,
     * 10.3.6, and 10.3.8).
     */
    protected void updateAutoContentSize() {
        if ((contentWidth == AUTO) && (contentHeight == AUTO)) {
            if (contentWidth == AUTO) {
                contentWidth = getIntrinsicWidth();
            }

            if (contentHeight == AUTO) {
                contentHeight = getIntrinsicHeight();
            }
        } else if ((contentWidth == AUTO) && (contentHeight != AUTO)) {
            if (getIntrinsicHeight() == 0) {
                contentWidth = getIntrinsicWidth();
            } else {
                contentWidth = (getIntrinsicWidth() * contentHeight) / getIntrinsicHeight();
            }
        } else if ((contentWidth != AUTO) && (contentHeight == AUTO)) {
            if (getIntrinsicWidth() == 0) {
                contentHeight = getIntrinsicHeight();
            } else {
                contentHeight = (getIntrinsicHeight() * contentWidth) / getIntrinsicWidth();
            }

            //} else if (contentWidth != AUTO &&
            //         contentHeight != AUTO) {
            //    // Do nothing
        }
    }

    /** Compute the effective top margin for this box.
     * Collapsing vertical margins: specified in 8.3.1
     * @todo I don't quite understand the "collapsed through" discussion
     *  in 8.3.1. Revisit and make sure it's supported correctly.
     */
    protected int getCollapsedTopMargin() {
        int max = (topMargin > 0) ? topMargin : 0; // largest positive margin
        int min = (topMargin > 0) ? 0 : (-topMargin); // abs(largest negative margin)

        if (getBoxCount() > 0) {
            CssBox child = getFirstNormalBox();

            while (child != null) {
                if (child.topMargin > 0) {
                    if (child.topMargin > max) {
                        max = child.topMargin;
                    }
                } else {
                    if (-child.topMargin > min) {
                        min = -child.topMargin;
                    }
                }

                // TODO: also abort collapse search if the child has clearance
                if ((child.topBorderWidth == 0) && (child.topPadding == 0) &&
                        (child.getBoxCount() > 0)) {
                    child = child.getFirstNormalBox();
                } else {
                    break;
                }
            }
        }

        return max - min;
    }

    /** Compute the effective bottom margins for this box.
     * Collapsing vertical margins: specified in 8.3.1.
     * @todo I don't quite understand the "collapsed through" discussion
     *  in 8.3.1. Revisit and make sure it's supported correctly.
     */
    protected int getCollapsedBottomMargin() {
        int max = (bottomMargin > 0) ? bottomMargin : 0; // largest positive margin
        int min = (bottomMargin > 0) ? 0 : (-bottomMargin); // abs(largest negative margin)

        if (getBoxCount() > 0) {
            CssBox child = getLastNormalBox();

            while (child != null) {
                if (child.bottomMargin > 0) {
                    if (child.bottomMargin > max) {
                        max = child.bottomMargin;
                    }
                } else {
                    if (-child.bottomMargin > min) {
                        min = -child.bottomMargin;
                    }
                }

                // TODO: also abort collapse search if the child has clearance
                if ((child.bottomBorderWidth == 0) && (child.bottomPadding == 0) &&
                        (child.getBoxCount() > 0)) {
                    child = child.getLastNormalBox();
                } else {
                    break;
                }
            }
        }

        return max - min;
    }

    // Implements ContainingBlock
    public int getBlockWidth() {
        return containingBlockWidth;
    }

    // Implements ContainingBlock
    public int getBlockHeight() {
        return containingBlockHeight;
    }

//    /** XXX For testsuite only */
//    public String getUnitializedField() {
//        if (x == UNINITIALIZED) {
//            return "x";
//        }
//
//        if (y == UNINITIALIZED) {
//            return "y";
//        }
//
//        if (width == UNINITIALIZED) {
//            return "width";
//        }
//
//        if (height == UNINITIALIZED) {
//            return "height";
//        }
//
//        if (this instanceof ContainerBox) {
//            if (containingBlockWidth == -1) {
//                return "containingBlockWidth";
//            }
//
//            if (containingBlockHeight == -1) {
//                return "containingBlockHeight";
//            }
//
//            if (contentWidth == AUTO) {
//                return "contentWidth";
//            }
//
//            if (contentHeight == AUTO) {
//                return "contentHeight";
//            }
//
//            if (leftMargin == AUTO) {
//                return "leftMargin";
//            }
//
//            if (rightMargin == AUTO) {
//                return "rightMargin";
//            }
//        }
//
//        if (!(this instanceof PageBox)) {
//            if (positionedBy == null) {
//                return "positionedBy";
//            }
//
//            if (parent == null) {
//                return "parent";
//            }
//        }
//
//        return null;
//    }

    // -------------------------------------------------------------------------------------------------
    // Stuff for debugging purposes (DOM inspector BoxNode)
    
    // TODO Only DomInspector, get rid of it (do not create new pulbic methods here).
    public Rectangle getCBRectangle() {
        return new Rectangle(containingBlockX, containingBlockY, containingBlockWidth,
            containingBlockHeight);
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Rectangle getExtentsRectangle() {
        return new Rectangle(extentX, extentY, extentX2 - extentX, extentY2 - extentY);
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Point getPosition() {
        return new Point(getAbsoluteX(), getAbsoluteY());
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Point getRelPosition() {
        return new Point(getX(), getY());
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Rectangle getPositionRect() {
        if (boxType.isPositioned()) {
            return new Rectangle(left, top, bottom, right);
        } else {
            return null;
        }
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public String getBoxTypeName() {
        return boxType.getDescription();
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Dimension getContentSize() {
        return new Dimension(contentWidth, contentHeight);
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Rectangle getMarginRectangle() {
        return new Rectangle(leftMargin, topMargin, rightMargin, bottomMargin);
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Rectangle getPaddingRectangle() {
        return new Rectangle(leftPadding, topPadding, rightPadding, bottomPadding);
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Rectangle getBorderWidthRectangle() {
        return new Rectangle(leftBorderWidth, topBorderWidth, rightBorderWidth, bottomBorderWidth);
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public Color getBg() {
        return bg;
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public String getStyles() {
        if (element != null) {
//            String styles = CssLookup.getAllStyles(element);
            String styles = CssProvider.getEngineService().getAllStylesForElement(element);

            if (styles == null) {
                return "";
            } else {
                return styles;
            }
        } else if ((this.getBoxType() == BoxType.TEXT) || (this.getBoxType() == BoxType.SPACE)) {
            return parent.getStyles();
        } else { // e.g. anonymous box such as a CaptionedTable

            return "";
        }
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public String getRules() {
        if (element != null) {
//            String styles = CssLookup.getAllRules(element);
            String styles = CssProvider.getEngineService().getAllRulesForElement(element);

            if (styles == null) {
                return "";
            } else {
                return styles;
            }
        } else if ((this.getBoxType() == BoxType.TEXT) || (this.getBoxType() == BoxType.SPACE)) {
            return parent.getRules();
        } else {
            return "";
        }
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public String getComputedStyles() {
        if (element != null) {
//            String styles = CssLookup.getAllComputedStyles(element);
            String styles = CssProvider.getEngineService().getAllComputedStylesForElement(element);

            if (styles == null) {
                return "";
            } else {
                return styles;
            }
        } else if ((this.getBoxType() == BoxType.TEXT) || (this.getBoxType() == BoxType.SPACE)) {
            return parent.getComputedStyles();
        } else { // e.g. anonymous box such as a CaptionedTable

            return "";
        }
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public boolean getPaintSpaces() {
        return paintSpaces;
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public void setPaintSpaces(boolean paintSpaces) {
        this.paintSpaces = paintSpaces;
        webform.getPane().repaint();
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public boolean getPaintText() {
        return paintText;
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public void setPaintText(boolean paintText) {
        this.paintText = paintText;
        webform.getPane().repaint();
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public boolean getPaintPositions() {
        return paintPositioning;
    }

    // TODO Only DomInspector, get rid of it (do not create new public methods here).
    public void setPaintPositions(boolean paintPositioning) {
        this.paintPositioning = paintPositioning;
        webform.getPane().repaint();
    }

    protected boolean isPlaceHolder() {
        return false;
    }

    /** Prints out the this box. 
     * @see java.awt.Component#list(java.io.PrintStream, int) */
    public void list(PrintStream out, int indent) {
	for (int i = 0 ; i < indent ; i++) {
	    out.print("    "); // NOI18N
	}
	out.println("*" + this); // NOI18N
    }

    /** Prints out the this box. 
     * @see java.awt.Component#list(java.io.PrintWriter, int) */
    public void list(PrintWriter out, int indent) {
	for (int i = 0 ; i < indent ; i++) {
	    out.print("    "); // NOI18N
	}
	out.println("*" + this); // NOI18N
    }

    /** Gets <code>Decoration</code> associated with this box.
     * @return gets the associated <code>Decoration</code> or null if there is none. */
    public Decoration getDecoration() {
//        return DecorationManager.getDefault().getDecoration(element);
        return webform.getDecoration(element);
    }
    
    /**
     * Class used to solve equations involving containing blocks where
     * one of the parameters (margin, content width, etc.) are unknown.
     */
    private static class Equation {
        /** List of integers, where exactly one can have the value
         * of AUTO. The other variables plus the AUTO field's value
         * should equal total, so solve for AUTO.
         */
        private final int[] variables;

        /** When a solution has been found, index points to the
         * variable that we solved for - e.g. the variable that
         * was originally AUTO.
         */
        private int index;

        /** When a solution has been found, this is the value of
         * the variable we solved for.
         */
        private int value;

        /** All the integers in variables, plus the AUTO one,
         * should total this amount.
         */
        private final int total;

        /** */
        private Equation(int total, int[] variables) {
            this.total = total;
            this.variables = variables;
        }

        /**
         * Given a list of integers, of which exactly one might be AUTO,
         * return the total minus all the other parameters: in other words
         * solve the equation sum(params)=total.  Return the answer as a
         * 2-element integer array: the first element is the index of the
         * parameter which was AUTO (e.g. the one we solved the equation
         * for), and the second element is the new value of that parameter.
         *
         * XXX NO - I just realized padding can't be AUTO! (Not an option).
         * So it shouldn't be a problem. Simplify this by presubtracting
         * the total and don't include padding in the parameters.
         */
        private void solve() {
            index = -1;

            for (int i = 0; i < variables.length; i++) {
                if (variables[i] == AUTO) {
                    index = i;

                    break;
                }
            }

            // we should only be called when number-of-auto==1
            assert index != -1;

            int remaining = total;

            for (int i = 0; i < variables.length; i++) {
                if (i != index) {
                    remaining -= variables[i];
                }
            }

            value = remaining;
        }
    }

    public Element getComponentRootElement() {
        return getElementForComponentRootCssBox(this);
    }

    public Box[] getChildren() {
        List<Box> boxes = new ArrayList<Box>();
        for (int i = 0, n = getBoxCount(); i < n; i++) {
            CssBox box = getBox(i);
            boxes.add(box);
        }
        return boxes.toArray(new Box[boxes.size()]);
    }

    public boolean isPositioned() {
        return getBoxType().isPositioned();
    }

    public boolean isAbsolutelyPositioned() {
        return getBoxType().isAbsolutelyPositioned();
    }
    
    
//    /** Gets the width of the block which directly contains the
//     * given element. */
//    public float getBlockWidth(Element element) {
//        CssBox box = getWebForm().getCssBoxForElement(element);
////        if (element instanceof RaveElement) {
////            RaveElement e = (RaveElement)element;
////            block = e.getBox();
////        }
//        if (box != null) {
//            return box.getBlockWidth();
//        }
//        ErrorManager.getDefault().log("No containing block available for element " + element); // NOI18N
//        return 0.0f; // if no available containing block, just use 0
//    }
//    
//    /** Gets the height of the block which directly contains the
//     * given element. */
//    public float getBlockHeight(Element element) {
//        CssBox box = getWebForm().getCssBoxForElement(element);
////        if (element instanceof RaveElement) {
////            block = ((RaveElement)element).getBox();
////        }
//        if (box != null) {
//            return box.getBlockHeight();
//        }
//        ErrorManager.getDefault().log("No containing block available for element " + element); // NOI18N
//        return 0.0f; // if no available containing block, just use 0
//    }
    
//    // XXX TODO Temporary. There should be a cleaner mechanism managing the links
//    // between the elements and the corresponding boxes.
//    private static final Map elementToBox = new WeakHashMap(200);
//    
//    private static void setCssBoxForElement(Element element, CssBox box) {
//        // XXX Copied from the original impl (in RaveElement).
//        org.w3c.dom.Node parent = element.getParentNode();
//        if ((parent instanceof Element) && getCssBoxForElement((Element)parent) == box) {
//            return; // Don't duplicate a bean reference on all the children!
//        }
//        
//        synchronized (elementToBox) {
//            elementToBox.put(element, box);
//        }
//    }
//    
//    private static CssBox getCssBoxForElement(Element element) {
//        synchronized (elementToBox) {
//            return (CssBox)elementToBox.get(element);
//        }
//    }
//    
//    // XXX Temporary, see DesignerService.copyBoxForElement.
//    public static void copyBoxForElement(Element fromElement, Element toElement) {
//        CssBox box = getCssBoxForElement(fromElement);
//        setCssBoxForElement(toElement, box);
//    }


//    /** XXX Copy also in insync/FacesDnDSupport.
//     * XXX Provides the auto value as <code>AUTO</code>, revise that, it looks very dangerous. */
//    public static int getCssLength(Element element, int property) {
////        Value val = getValue(element, property);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, property);
//        
//        // XXX #6460007 Possible NPE.
//        if (cssValue == null) {
//            // XXX What value to return?
//            return 0;
//        }
//        
////        if (val == CssValueConstants.AUTO_VALUE) {
//        if (CssProvider.getValueService().isAutoValue(cssValue)) {
//            return CssBox.AUTO;
//        }
//        
////        return (int)val.getFloatValue();
//        return (int)cssValue.getFloatValue();
//    }
    
    
//    /** XXX Moved from DesignerActions.
//     * Returns whether or not this component is the initial focus.
//     * @param bean The bean associated with the component
//     * @return whether or not that component is the initial focus
//     */
//    private static boolean isFocus(DesignBean bean) {
//        if (bean == null) {
//            return false;
//        }
//
//        DesignBean body = getWebuiBody(bean);
//
//        if (body == null) {
//            return false;
//        }
//
//        DesignProperty prop = body.getProperty("focus");  // NOI18N
//
//        if ((prop != null) && (prop.getValue() != null)) {
//            // The property points to the client id, not the instance name!
//            return prop.getValue().equals(getClientId(bean));
//        } else {
//            return false;
//        }
//    }
//
//    // XXX Moved from DesignerActions.
//    /** Find the Body component of the page containing the given bean, if any */
//    private static DesignBean getWebuiBody(DesignBean bean) {
//        DesignBean parent = bean.getBeanParent();
//
//        while (parent != null) {
//            if (parent.getInstance() instanceof com.sun.rave.web.ui.component.Body
//            || parent.getInstance() instanceof com.sun.webui.jsf.component.Body) {
//                return parent;
//            }
//
//            parent = parent.getBeanParent();
//        }
//
//        return null;
//    }
//    
//    // XXX Moved from DesignerActions.
//    /** Get the client id for the given DesignBean */
//    private static String getClientId(DesignBean bean) {
//        Object instance = bean.getInstance();
//
//        if (!(instance instanceof UIComponent)) {
//            return null;
//        }
//
//        UIComponent uic = (UIComponent)instance;
//        DesignContext dcontext = bean.getDesignContext();
//        FacesContext fcontext = ((FacesDesignContext)dcontext).getFacesContext();
//
//        return uic.getClientId(fcontext);
//    }

    private static void info(Exception ex) {
        getLogger().log(Level.INFO, null, ex);
    }
    
    private static void fine(String message) {
        getLogger().fine(message);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(CssBox.class.getName());
    }
}

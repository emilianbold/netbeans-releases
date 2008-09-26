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

import org.netbeans.modules.visualweb.api.designer.cssengine.CssComputedValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openide.ErrorManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.ImageCache;


/**
 * ImageBox renders an image.
 * <p>
 * @todo Handle borders
 * @todo Handle ALT text
 * @todo Handle width and height settings when the image is not
 *   found
 * @todo Is the "broken image" handling still working?  It should have
 *   a border too!
 * <p>
 * Portions of this code taken from javax.swing.text.html.ImageView
 * <p>
 * @author Scott Violet
 * @author Tor Norbye
 */
public class ImageBox extends CssBox {
    /*
    protected String paramString() {
        return super.paramString() + ", " +
            "element=" + element;
    }
    */

    // Code from ImageView (modified for Rave purposes)

    /**
     * If true, when some of the bits are available a repaint is done.
     * <p>
     * This is set to false as swing does not offer a repaint that takes a
     * delay. If this were true, a bunch of immediate repaints would get
     * generated that end up significantly delaying the loading of the image
     * (or anything else going on for that matter).
     */
    private static boolean sIsInc = false;

    /**
     * Repaint delay when some of the bits are available.
     */
    private static int sIncRate = 100;

//    /**
//     * Icon used while the image is being loaded.
//     */
//    private static Icon sPendingImageIcon;
//
//    /**
//     * Icon used if the image could not be found.
//     */
//    private static Icon sMissingImageIcon;
//
//    private static final String PENDING_IMAGE_SRC = "icons/image-delayed.gif";
//    private static final String MISSING_IMAGE_SRC = "icons/image-failed.gif";

    // Height/width to use before we know the real size, these should at least
    // the size of <code>sMissingImageIcon</code> and
    // <code>sPendingImageIcon</code>
    private static final int DEFAULT_WIDTH = 38;
    private static final int DEFAULT_HEIGHT = 38;

    // BEGIN RAVE MODIFICATIONS
    // Actually, reverted. Ignore this.
    //private static final int DEFAULT_WIDTH = 200;
    //private static final int DEFAULT_HEIGHT= 65;
    // END RAVE MODIFICATIONS
    // Bitmask values
    private static final int LOADING_FLAG = 1;
    private static final int LINK_FLAG = 2;
    private static final int WIDTH_FLAG = 4;
    private static final int HEIGHT_FLAG = 8;
    private static final int RELOAD_FLAG = 16;
    private static final int RELOAD_IMAGE_FLAG = 32;

    //    private AttributeSet attr;
    private Image image;
    private int width;
    private int height;

    /** Bitmask containing some of the above bitmask values. Because the
     * image loading notification can happen on another thread access to
     * this is synchronized (at least for modifying it). */
    private int state;
    private Container container;
    private Rectangle fBounds;

    /**
     * We don't directly implement ImageObserver, instead we use an instance
     * that calls back to us.
     */
    private ImageObserver imageObserver;

    public ImageBox(WebForm webform, Element element, Container container, BoxType boxType,
        boolean inline) {
        super(webform, element, boxType, inline, true);
        this.container = container;

        fBounds = new Rectangle();
        imageObserver = new ImageHandler();
        state = RELOAD_FLAG | RELOAD_IMAGE_FLAG;
        preloadImage();
    }

    public static CssBox getImageBox(WebForm webform, Element element, Container container,
        BoxType boxType, boolean inline) {
        ImageBox imageBox = new ImageBox(webform, element, container, boxType, inline);

        //if (imageBox.image != null) {
        if (imageBox.isValidUrl()) {
            return imageBox;
        }

        String alt = element.getAttribute(HtmlAttribute.ALT);

        if ((alt.length() == 0) && element.hasAttribute(HtmlAttribute.ALT) &&
                !element.hasAttribute(HtmlAttribute.SRC)) {
            // User specifically wants no alt, and has no image -- use sized box
            // For example, google does underlines this way: with an img
            // tag
            return new CssBox(webform, element, boxType, inline, true);
        }

//        if (((alt == null) || (alt.length() == 0)) && (imageBox.getDesignBean() != null)) {
//        if (((alt == null) || (alt.length() == 0)) && (CssBox.getMarkupDesignBeanForCssBox(imageBox) != null)) {
        if (((alt == null) || (alt.length() == 0)) && (CssBox.getElementForComponentRootCssBox(imageBox) != null)) {
            alt = NbBundle.getMessage(ImageBox.class, "LBL_Image");
        }

        if (alt != null) {
            // TODO - pass in our width/height as the intrinsic
            // image width!
//            int width = CssLookup.getLength(element, XhtmlCss.WIDTH_INDEX);
//            int height = CssLookup.getLength(element, XhtmlCss.HEIGHT_INDEX);
            int width = CssUtilities.getCssLength(element, XhtmlCss.WIDTH_INDEX);
            int height = CssUtilities.getCssLength(element, XhtmlCss.HEIGHT_INDEX);

            if (width == 0) {
                width = AUTO;
            }

            if (height == 0) {
                height = AUTO;
            }

            imageBox.initializeBorder();

            StringBox sb =
                new StringBox(webform, element, boxType, alt, imageBox.border, width, height);

            //sb.setDrawBorder(true);
            return sb;
        } else {
            return imageBox;
        }
    }

    protected void initializeHorizontalWidths(FormatContext context) {
        super.initializeHorizontalWidths(context);

        // Ensure that we initialize width, contentWidth etc. since those
        // are normally delayed for images until layout time, but during
        // table layout for an ancestor we need the width before our own
        // box has been laid out
        if (super.width == UNINITIALIZED) {
            this.relayout(context);
        }
    }

    protected void initializeBorder() {
        Element element = getElement();
        // Border: percentages are not allowed
        int borderWidth = HtmlAttribute.getIntegerAttributeValue(element, HtmlAttribute.BORDER, 0);

        if (borderWidth < 0) {
            borderWidth = 0;
        }

        int defStyle = (borderWidth == 0) ? CssBorder.STYLE_NONE : CssBorder.STYLE_OUTSET;
        border = CssBorder.getBorder(element, borderWidth, defStyle, CssBorder.FRAME_BOX);

        if (border != null) {
            leftBorderWidth = border.getLeftBorderWidth();
            topBorderWidth = border.getTopBorderWidth();
            bottomBorderWidth = border.getBottomBorderWidth();
            rightBorderWidth = border.getRightBorderWidth();
        }

        considerDesignBorder();
    }

    public void relayout(FormatContext context) {
        sync();
        width = (int)getPreferredSpan(X_AXIS);
        height = (int)getPreferredSpan(Y_AXIS);
        this.contentWidth = this.width;
        this.contentHeight = this.height;

        //super.width = width;
        //super.height = height;
        super.width =
            leftBorderWidth + leftPadding + contentWidth + rightPadding + rightBorderWidth;
        super.height =
            topBorderWidth + topPadding + contentHeight + bottomPadding + bottomBorderWidth;
    }

//    public String toString() {
//        return "ImageBox[" + paramString() + "]";
//    }

    /**
     * Returns the text to display if the image can't be loaded. This is
     * obtained from the Elements attribute set with the attribute name
     * <code>HTML.HtmlAttribute.ALT</code>.
     */
    public String getAltText() {
        Element element = getElement();
        return element == null ? null : element.getAttribute(HtmlAttribute.ALT);
    }

    /**
     * Return a URL for the image source,
     * or null if it could not be determined.
     */
    public URL getImageURL() {
        Element element = getElement();
        String src = element == null ? null : element.getAttribute(HtmlAttribute.SRC);

        if ((src == null) || (src.length() == 0)) {
            return null;
        }

//        return InSyncService.getProvider().resolveUrl(webform.getMarkup().getBase(), webform.getJspDom(), src);
        return webform.resolveUrl(src);
    }

    /**
     * Returns the icon to use if the image couldn't be found.
     */
    private static Icon getNoImageIcon() {
        return loadIcon("org/netbeans/modules/visualweb/designer/resources/image-failed.gif"); // NOI18N
    }

    /**
     * Returns the icon to use while in the process of loading the image.
     */
    private static Icon getLoadingImageIcon() {
        return loadIcon("org/netbeans/modules/visualweb/designer/resources/image-delayed.gif"); // NOI18N
    }

    /**
     * Returns the image to render.
     */
    public Image getImage() {
        sync();

        return image;
    }

    public void paint(Graphics g, int px, int py) {
        if (hidden) {
            return;
        }

        //super.paint(g, px, py);
        sync();

        px += getX();
        py += getY();

        px += leftMargin;
        py += effectiveTopMargin;

        Image image = getImage();

        if ((Math.abs(px) > 50000) || (Math.abs(py) > 50000) || (Math.abs(width) > 50000) ||
                (Math.abs(height) > 50000)) {
//            g.setColor(Color.RED);
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

        paintBackground(g, px, py);

        //fBounds.setBounds(rect);
        int x = px + leftBorderWidth + leftPadding;
        int y = py + topBorderWidth + topPadding;

        // XXX Don't I need to add in margins?
        fBounds.setBounds(x, y, getWidth(), getHeight());

        //paintHighlights(g, a);

        /* No - box parent should deal with borders, margins, etc.
        if (clip != null) {
            g.clipRect(rect.x + leftInset, rect.y + topInset,
                       rect.width - leftInset - rightInset,
                       rect.height - topInset - bottomInset);
        }
        */
        if (image != null) {
            if (!hasPixels(image)) {
                // No pixels yet, use the default
                Icon icon = getLoadingImageIcon();

                if (icon != null) {
                    icon.paintIcon(container, g, x /*rect.x + leftInset*/, y /*rect.y + topInset */);
                }
            } else {
                // Draw the image
                g.drawImage(image, x /*rect.x + leftInset*/, y /*rect.y + topInset*/, width,
                    height, imageObserver);
            }
        } else {
            Icon icon = getNoImageIcon();

            if (icon != null) {
                icon.paintIcon(container, g, x /*rect.x + leftInset*/, y /*rect.y + topInset*/);
            }

            // TODO - alt view

            /*
            View view = getAltView();
            // Paint the view representing the alt text, if its non-null
            if (view != null && ((state & WIDTH_FLAG) == 0 ||
                                 width > DEFAULT_WIDTH)) {
                // Assume layout along the y direction
                Rectangle altRect = new Rectangle
                    (rect.x + leftInset + DEFAULT_WIDTH, rect.y + topInset,
                     rect.width - leftInset - rightInset - DEFAULT_WIDTH,
                     rect.height - topInset - bottomInset);

                view.paint(g, altRect);
            }
            */
        }

        /* Already removed the clip code above
        if (clip != null) {
            // Reset clip.
            g.setClip(clip.x, clip.y, clip.width, clip.height);
        }
        */

        //        if (width > WATERMARK_SIZE && height > WATERMARK_SIZE) { // don't draw watermarks for tiny images
        //           paintFacesWatermark(g, px, py);
        //        }

        if (paintPositioning) {
            paintDebugPositioning(g);
        }
    }

    /**
     * Determines the preferred span for this view along an
     * axis.
     *
     * @param axis may be either X_AXIS or Y_AXIS
     * @return   the span the view would like to be rendered into;
     *           typically the view is told to render into the span
     *           that is returned, although there is no guarantee;
     *           the parent may choose to resize or break the view
     */
    public float getPreferredSpan(int axis) {
        sync();

        // If the attributes specified a width/height, always use it!
        if ((axis == CssBox.X_AXIS) && ((state & WIDTH_FLAG) == WIDTH_FLAG)) {
            //getPreferredSpanFromAltView(axis);
            return width;
        }

        if ((axis == CssBox.Y_AXIS) && ((state & HEIGHT_FLAG) == HEIGHT_FLAG)) {
            //getPreferredSpanFromAltView(axis);
            return height;
        }

        Image image = getImage();

        if (image != null) {
            switch (axis) {
            case CssBox.X_AXIS:
                return width;

            case CssBox.Y_AXIS:
                return height;

            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        } else {
            // TODO - handle alt view

            /*
            View view = getAltView();
            float retValue = 0f;

            if (view != null) {
                retValue = view.getPreferredSpan(axis);
            }
            switch (axis) {
            case CssBox.X_AXIS:
                return retValue + (float)(width + leftInset + rightInset);
            case CssBox.Y_AXIS:
                return retValue + (float)(height + topInset + bottomInset);
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
            }
            */
            return 0;
        }
    }

    /**
     * Returns true if the passed in image has a non-zero width and height.
     */
    private boolean hasPixels(Image image) {
        return (image != null) && (image.getHeight(imageObserver) > 0) &&
        (image.getWidth(imageObserver) > 0);
    }

//    /**
//     * Fetch a resource relative to the HTMLEditorKit classfile.
//     * If this is called on 1.2 the loading will occur under the
//     * protection of a doPrivileged call to allow the HTMLEditorKit
//     * to function when used in an applet.
//     *
//     * @param name the name of the resource, relative to the
//     *  HTMLEditorKit class
//     * @return a stream representing the resource
//     */
//    static InputStream getResourceAsStream(String name) {
//        return ImageBox.class.getResourceAsStream(name);
//
//        /*
//        try {
//        return ResourceLoader.getResourceAsStream(name);
//        } catch (Throwable e) {
//        // If the class doesn't exist or we have some other
//        // problem we just try to call getResourceAsStream directly.
//        return ImageAbstractBox.class.getResourceAsStream(name);
//        }
//        */
//    }
//
//    private Icon makeIcon(final String gifFile) throws IOException {
//        /* Copy resource into a byte array.  This is
//         * necessary because several browsers consider
//         * Class.getResource a security risk because it
//         * can be used to load additional classes.
//         * Class.getResourceAsStream just returns raw
//         * bytes, which we can convert to an image.
//         */
//        InputStream resource = getResourceAsStream(gifFile);
//
//        if (resource == null) {
//            ErrorManager.getDefault().log(ImageBox.class.getName() + "/" + gifFile + " not found.");
//
//            return null;
//        }
//
//        BufferedInputStream in = new BufferedInputStream(resource);
//        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
//        byte[] buffer = new byte[1024];
//        int n;
//
//        while ((n = in.read(buffer)) > 0) {
//            out.write(buffer, 0, n);
//        }
//
//        in.close();
//        out.flush();
//
//        buffer = out.toByteArray();
//
//        if (buffer.length == 0) {
//            ErrorManager.getDefault().log("warning: " + gifFile + " is zero-length");
//
//            return null;
//        }
//
//        return new ImageIcon(buffer);
//    }
    
    private static Icon loadIcon(String iconResource) {
        Image image = ImageUtilities.loadImage(iconResource);
        if (image == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("No image for iconResource=" + iconResource)); // NOI18N
        }
        return image == null ? null : new ImageIcon(image);
    }

    /**
     * Request that this view be repainted.
     * Assumes the view is still at its last-drawn location.
     */
    private void repaint(long delay) {
        if ((container != null) && (fBounds != null)) {
            container.repaint(delay, fBounds.x, fBounds.y, fBounds.width, fBounds.height);
        }
    }


    /**
     * Makes sure the necessary properties and image is loaded.
     */
    private void sync() {
        int s = state;

        if ((s & RELOAD_IMAGE_FLAG) != 0) {
            refreshImage();
        }

        s = state;

        if ((s & RELOAD_FLAG) != 0) {
            synchronized (this) {
                state = (state | RELOAD_FLAG) ^ RELOAD_FLAG;
            }
        }
    }

    /** Return true iff the url is set and valid such that we'll most
     * likely be able to display this image.
     */
    private boolean isValidUrl() {
        // try to access the resource
        return (image != null);
    }

    /**
     * Loads the image and updates the size accordingly. This should be
     * invoked instead of invoking <code>loadImage</code> or
     * <code>updateImageSize</code> directly.
     * @return true if any of the sizes are relative to the containing block
     * (e.g. a percentage).
     */
    private boolean refreshImage() {
        synchronized (this) {
            // clear out width/height/realoadimage flag and set loading flag
            state =
                (state | LOADING_FLAG | RELOAD_IMAGE_FLAG | WIDTH_FLAG | HEIGHT_FLAG) ^
                (WIDTH_FLAG | HEIGHT_FLAG | RELOAD_IMAGE_FLAG);
            image = null;
            width = height = 0;
        }

        try {
            // Load the image
            loadImage();

            // And update the size params
            return updateImageSize();
        } finally {
            synchronized (this) {
                // Clear out state in case someone threw an exception.
                state = (state | LOADING_FLAG) ^ LOADING_FLAG;
            }
        }
    }

    /** Try to load in the image early. This will tell us if we need
     * to use a text box instead for example if the url is bad.
     */
    private void preloadImage() {
        boolean relative = refreshImage();

        if (relative) {
            // Ensure that we do this again once we know the viewport
            // size - set flag such that sync() in relout will recompute
            // picture
            state = RELOAD_FLAG | RELOAD_IMAGE_FLAG;

            // Clear out the style related values for this element
            // since we may have accessed them - they would now get cached
            // until next time which would still keep the old
            // containing block width (-1) instead of looking up a new one
//            CssLookup.clearComputedStyles(getElement());
            CssProvider.getEngineService().clearComputedStylesForElement(getElement());
        }
    }

    /**
     * Loads the image from the URL <code>getImageURL</code>. This should
     * only be invoked from <code>refreshImage</code>.
     */
    private void loadImage() {
        URL src = getImageURL();
        Image newImage = null;

        if (src != null) {
//            ImageCache cache = webform.getDocument().getImageCache();
            ImageCache cache = webform.getImageCache();
            ImageIcon ii = cache.get(src);

            if (ii == null) {
                newImage = Toolkit.getDefaultToolkit().createImage(src);

                // Always load synchronously - we need size info
                // for layout
                if (newImage != null) {
                    // Force the image to be loaded by using an ImageIcon.
                    ii = new ImageIcon();
                    ii.setImage(newImage);
                    cache.put(src, ii);
                }
            } else {
                newImage = ii.getImage();
            }
        }

        image = newImage;
    }

    /**
     * Recreates and reloads the image.  This should
     * only be invoked from <code>refreshImage</code>.
     * @return true if any of the sizes are relative to the containing block
     * (e.g. a percentage).
     */
    private boolean updateImageSize() {
        int newWidth = 0;
        int newHeight = 0;
        int newState = 0;
        Image newImage = getImage();
        boolean relative = false;

        if (newImage != null) {
            Element elem = getElement();

            // Get the width/height and set the state ivar before calling
            // anything that might cause the image to be loaded, and thus the
            // ImageHandler to be called.
            //newWidth = Css.getLength(elem, XhtmlCss.WIDTH_INDEX);
            // We want the value itself so we can check for percentages
//            Value val = CssLookup.getValue(elem, XhtmlCss.WIDTH_INDEX);
            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(elem, XhtmlCss.WIDTH_INDEX);

//            if (val == CssValueConstants.AUTO_VALUE) {
            if (CssProvider.getValueService().isAutoValue(cssValue)) {
                newWidth = CssBox.AUTO;
            } else {
//                newWidth = (int)val.getFloatValue();
                newWidth = (int)cssValue.getFloatValue();
//                relative =
//                    relative ||
//                    (val instanceof ComputedValue &&
//                    (((ComputedValue)val).getCascadedValue().getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE));
                relative = relative
                        || (cssValue instanceof CssComputedValue
                            && CssProvider.getValueService().isOfPrimitivePercentageType(((CssComputedValue)cssValue).getCascadedValue()));
            }

            if ((newWidth > 0) && (newWidth != AUTO)) {
                newState |= WIDTH_FLAG;
            }

            //newHeight = Css.getLength(elem, XhtmlCss.HEIGHT_INDEX);
//            val = CssLookup.getValue(elem, XhtmlCss.HEIGHT_INDEX);
            CssValue cssValue2 = CssProvider.getEngineService().getComputedValueForElement(elem, XhtmlCss.HEIGHT_INDEX);

//            if (val == CssValueConstants.AUTO_VALUE) {
            if (CssProvider.getValueService().isAutoValue(cssValue2)) {
                newHeight = CssBox.AUTO;
            } else {
//                newHeight = (int)val.getFloatValue();
                newHeight = (int)cssValue2.getFloatValue();
//                relative =
//                    relative ||
//                    (val instanceof ComputedValue &&
//                    (((ComputedValue)val).getCascadedValue().getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE));
                relative = relative
                        || (cssValue2 instanceof CssComputedValue
                            && CssProvider.getValueService().isOfPrimitivePercentageType(((CssComputedValue)cssValue2).getCascadedValue()));
            }

            if ((newHeight > 0) && (newHeight != AUTO)) {
                newState |= HEIGHT_FLAG;
            }

            if ((newWidth <= 0) || (newWidth == AUTO)) {
                newWidth = newImage.getWidth(imageObserver);

                if (newWidth <= 0) {
                    newWidth = DEFAULT_WIDTH;
                }
            }

            if ((newHeight <= 0) || (newHeight == AUTO)) {
                newHeight = newImage.getHeight(imageObserver);

                if (newHeight <= 0) {
                    newHeight = DEFAULT_HEIGHT;
                }
            }

            // Make sure the image starts loading:
            if ((newState & (WIDTH_FLAG | HEIGHT_FLAG)) != 0) {
                Toolkit.getDefaultToolkit().prepareImage(newImage, newWidth, newHeight,
                    imageObserver);
            } else {
                Toolkit.getDefaultToolkit().prepareImage(newImage, -1, -1, imageObserver);
            }

            boolean createText = false;

            synchronized (this) {
                // If imageloading failed, other thread may have called
                // ImageLoader which will null out image, hence we check
                // for it.
                if (image != null) {
                    if (((newState & WIDTH_FLAG) == WIDTH_FLAG) || (width == 0)) {
                        width = newWidth;
                    }

                    if (((newState & HEIGHT_FLAG) == HEIGHT_FLAG) || (height == 0)) {
                        height = newHeight;
                    }
                } else {
                    createText = true;

                    if ((newState & WIDTH_FLAG) == WIDTH_FLAG) {
                        width = newWidth;
                    }

                    if ((newState & HEIGHT_FLAG) == HEIGHT_FLAG) {
                        height = newHeight;
                    }
                }

                state = state | newState;
                state = (state | LOADING_FLAG) ^ LOADING_FLAG;
            }

//            if (createText) {
//                // Only reset if this thread determined image is null
//                // TODO: show text instead?
//            }
        } else {
            width = height = DEFAULT_HEIGHT;
            updateBorderForNoImage();

            // TODO - no image, show text instead
        }

        return relative;
    }

    /**
     * Invokes <code>preferenceChanged</code> on the event displatching
     * thread.
     */
    private void safePreferenceChanged() {
        Thread.dumpStack();

        /*
        if (SwingUtilities.isEventDispatchThread()) {
            preferenceChanged(null, true, true);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        preferenceChanged(null, true, true);
                    }
                });
        }
        */
    }

    /**
     * Invoked if no image is found, in which case a default border is
     * used if one isn't specified.
     */
    private void updateBorderForNoImage() {
    }

    public int getIntrinsicWidth() {
        return width;
    }

    public int getIntrinsicHeight() {
        return height;
    }

    /**
     * ImageHandler implements the ImageObserver to correctly update the
     * display as new parts of the image become available.
     */
    private class ImageHandler implements ImageObserver {
        // This can come on any thread. If we are in the process of reloading
        // the image and determining our state (loading == true) we don't fire
        // preference changed, or repaint, we just reset the fWidth/fHeight as
        // necessary and return. This is ok as we know when loading finishes
        // it will pick up the new height/width, if necessary.
        public boolean imageUpdate(Image img, int flags, int x, int y, int newWidth, int newHeight) {
            if ((image == null) || (image != img)) {
                return false;
            }

            // Bail out if there was an error:
            if ((flags & (ABORT | ERROR)) != 0) {
                repaint(0);

                synchronized (ImageBox.this) {
                    if (image == img) {
                        // Be sure image hasn't changed since we don't
                        // initialy synchronize
                        image = null;

                        if ((state & WIDTH_FLAG) != WIDTH_FLAG) {
                            width = DEFAULT_WIDTH;
                        }

                        if ((state & HEIGHT_FLAG) != HEIGHT_FLAG) {
                            height = DEFAULT_HEIGHT;
                        }

                        // No image, use a default border.
                        updateBorderForNoImage();
                    }

                    if ((state & LOADING_FLAG) == LOADING_FLAG) {
                        // No need to resize or repaint, still in the process
                        // of loading.
                        return false;
                    }
                }

                // TODO - show text instead?
                safePreferenceChanged();

                return false;
            }

            // Resize image if necessary:
            short changed = 0;

            Element element = getElement();
            if (((flags & ImageObserver.HEIGHT) != 0) &&
                    !element.hasAttribute(HtmlAttribute.HEIGHT)) {
                changed |= 1;
            }

            if (((flags & ImageObserver.WIDTH) != 0) && !element.hasAttribute(HtmlAttribute.WIDTH)) {
                changed |= 2;
            }

            synchronized (ImageBox.this) {
                if (image != img) {
                    return false;
                }

                if (((changed & 1) == 1) && ((state & WIDTH_FLAG) == 0)) {
                    width = newWidth;
                }

                if (((changed & 2) == 2) && ((state & HEIGHT_FLAG) == 0)) {
                    height = newHeight;
                }

                if ((state & LOADING_FLAG) == LOADING_FLAG) {
                    // No need to resize or repaint, still in the process of
                    // loading.
                    return true;
                }
            }

            // Repaint when done or when new pixels arrive:
            if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
                repaint(0);
            } else if (((flags & SOMEBITS) != 0) && sIsInc) {
                repaint(sIncRate);
            }

            return ((flags & ALLBITS) == 0);
        }
    }

    public int getBaseline() {
        return getHeight();
    }

    public int getContributingBaseline() {
        // Images behave funny: they are baseline aligned but do not contribute
        // to make a taller baseline.
        return 0;
    }

}

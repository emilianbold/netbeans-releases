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


import java.awt.EventQueue;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JViewport;

import org.netbeans.modules.visualweb.api.designer.Designer.ExternalBox;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssListValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.netbeans.modules.visualweb.designer.DesignerPane;
import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.ImageCache;
import org.netbeans.modules.visualweb.designer.WebForm;

import org.openide.util.NbBundle;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * ExternalDocumentBox is an abstract class used by FrameBox,
 * JspIncludeBox etc. -- boxes that represent content rendered from
 * external documents / separate files.
 *
 * @author Tor Norbye
 *
 */
public abstract class ExternalDocumentBox extends DocumentBox implements ExternalBox {
    /** Flag which indicates that this page wants to load the resource
     * via a url, and that we don't support it*/
//    protected boolean external;
    private boolean haveCreatedChildren;
    private WebForm frameForm;

    /** Flag which indicates whether or not this box is the root of a layout hierarchy.
     * If it is, computing absolute positions for example should terminate at this box
     * even if the parent pointer points further.
     */
    private boolean layoutRoot;

    protected ExternalDocumentBox(DesignerPane pane, /*WebForm frameForm,*/ WebForm webform,
    Element element, /*URL url,*/ BoxType boxType, boolean inline, boolean replaced) {
        super(pane, webform, element, boxType, inline, replaced);
//        this.frameForm = frameForm;
        this.frameForm = findExternalForm(webform, element);
    }

    // XXX Moved to designer/jsf/../JsfForm.
//    protected final static WebForm findForm(WebForm webform, URL url) {
////        DocumentCache cache = webform.getDocument().getFrameBoxCache();
//        DocumentCache cache = webform.getFrameBoxCache();
//        WebForm frameForm = cache.get(url);
//
//        if (frameForm != null) {
//            return frameForm;
//        }
//
//        // According to HTML4.01 section 16.5: "The contents of the
//        // IFRAME element, on the other hand, should only be displayed
//        // by user agents that do not support frames or are configured
//        // not to display frames."
//        // Thus, we don't walk the children array; instead, we
//        // fetch the url document and display that instead
//        if (url == null) {
//            return null;
//        }
//
//        FileObject fo = URLMapper.findFileObject(url);
//
//        if (fo != null) {
//            frameForm = loadPage(fo);
//        }
//
//        if (frameForm == null) {
//            frameForm = loadPage(url);
//        }
//
//        if ((frameForm != null) && (frameForm != WebForm.EXTERNAL)) {
//            cache.put(url, frameForm);
//        }
//
////        // Set the cell renderer pane if necessary
////        if ((frameForm != null) && (frameForm.getRenderPane() == null)) {
////            frameForm.setRenderPane(webform.getRenderPane());
////        }
//
//        return frameForm;
//    }

    protected abstract String getUrlString();

//    private static WebForm loadPage(URL url) {
//        //Log.err.log("URL box loading not yet implemented");
//        return WebForm.EXTERNAL;
//
////        /*
////        // Compute document base for the other document
////        //        try {
////        //            url = new URL(getBase(), href);
////        //        } catch (MalformedURLException mfe) {
////        //            try {
////        //                ErrorManager.getDefault().notify(mfe);
////        //                url = new URL(href);
////        //            } catch (MalformedURLException mfe2) {
////        //                ErrorManager.getDefault().notify(mfe);
////        //                url = null;
////        //            }
////        //        }
////        //        if (url != null) {
////        StringBuffer sb = new StringBuffer();
////        try {
////            InputStream uis = url.openStream();
////            Reader r = new BufferedReader(new InputStreamReader(uis));
////            int c;
////            while ((c = r.read()) != -1) {
////                sb.append((char)c);
////            }
////        } catch (IOException ioe) {
////            ErrorManager.getDefault().notify(ioe);
////            return false;
////        }
////        String str = sb.toString();
////
////        // Construct a document containing the string buffer
////        StringContent content = new StringContent(str.length()+5);
////        try {
////            content.insertString(0, str);
////        } catch (Exception e) {
////            ErrorManager.getDefault().notify(e);
////            return false;
////        }
////        AbstractDocument adoc = new PlainDocument(content);
////        DataObject dobj = null;
////        String filename = url.toString(); // only used for diagnostic messages, right?
////
////        MarkupUnit markup = new MarkupUnit(dobj, adoc, filename, MarkupUnit.ALLOW_XML);
////        markup.sync();
////        //if (!markup.getState().equals(markup.getState().CLEAN)) {
////        if (!markup.getState().equals(Unit.State.CLEAN)) {
////            return false;
////        }
////
////        CellRendererPane renderPane = webform.getPane().getRenderPane();
////        Log.err.log("FrameBox initialization for external urls not yet done");
////        */
////        /* XXX Not yet implemented
////        frameForm = new WebForm(markup, renderPane);
////        DesignerPane pane = null;
////        Document document = new Document(frameForm);
////        frameForm.setDocument(document);
////        return success;
////        */
//    }
//
//    private static WebForm loadPage(FileObject fobj) {
//        DataObject dobj = null;
//
//        try {
//            dobj = DataObject.find(fobj);
//        } catch (DataObjectNotFoundException ex) {
//            return null;
//        }
//
//        /*
//        // Wrapper which handles errors
//        LiveFacesCookie c = LiveFacesCookie.getInstanceFor(dobj);
//        if (c == null) {
//            ErrorManager.getDefault().log("Data object " + dobj + " ain't got no insync cookie!");
//            return false;
//        }
//        FacesModel model = getDocument().getWebForm().getModel();
//        model.syncFromDoc();
//        if (model.getMarkup().getState().isInvalid()) {
//            return false;
//        }
//        markup = model.getMarkup();
//         */
//
//        // XXX Does this work for a form which is not yet open?
////        WebForm frameForm = WebForm.findWebForm(dobj);
//        WebForm frameForm = WebForm.getWebFormForDataObject(dobj);
//
////        if ((frameForm != null) && (frameForm.getModel() != null)) {
////            frameForm.getModel().sync();
//        if (frameForm != null) {
//            frameForm.syncModel();
//
//            return frameForm;
//        } else {
//            return null;
//        }
//    }

    protected void initializeBackgroundColor() {
        WebForm frameForm = getExternalForm();
//        if ((frameForm != null) && !frameForm.getModel().isBusted()) {
        if (frameForm != null && !frameForm.isModelBusted()) {
            // Use the pointed-to document's background colors
            // etc.
//            Element frameFormElement = frameForm.getDocument().getWebForm().getHtmlBody();
            Element frameFormElement = frameForm.getHtmlBody();
//            bg = CssLookup.getColor(frameFormElement, XhtmlCss.BACKGROUND_COLOR_INDEX);
            bg = CssProvider.getValueService().getColorForElement(frameFormElement, XhtmlCss.BACKGROUND_COLOR_INDEX);
        } else {
            super.initializeBackgroundColor();
        }
    }

    protected void initializeBackgroundImage() {
        WebForm frameForm = getExternalForm();
//        if ((frameForm != null) && !frameForm.getModel().isBusted()) {
        if (frameForm != null && !frameForm.isModelBusted()) {
            // Use the pointed-to document's background colors
            // etc.
//            Element frameFormElement = frameForm.getDocument().getWebForm().getHtmlBody();
            Element frameFormElement = frameForm.getHtmlBody();
//            ImageIcon bgImage = BackgroundImagePainter.getBackgroundImage(webform, frameFormElement);
//            URL imageUrl = CssBoxUtilities.getBackgroundImageUrl(frameFormElement, webform.getMarkup().getBase());
//            URL imageUrl = CssProvider.getEngineService().getBackgroundImageUrlForElement(frameFormElement, webform.getMarkup().getBase());
            URL imageUrl = CssProvider.getEngineService().getBackgroundImageUrlForElement(frameFormElement, webform.getBaseUrl());
            ImageIcon bgImage;
            if (imageUrl != null) {
                // XXX Revise this caching impl.
//                ImageCache imageCache = webform.getDocument().getImageCache();
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
//                Value repeatValue = CssLookup.getValue(frameFormElement, XhtmlCss.BACKGROUND_REPEAT_INDEX);
                CssValue cssRepeatValue = CssProvider.getEngineService().getComputedValueForElement(frameFormElement, XhtmlCss.BACKGROUND_REPEAT_INDEX);
//                ListValue positionValue =
//                        CssLookup.getListValue(CssLookup.getValue(frameFormElement, XhtmlCss.BACKGROUND_POSITION_INDEX));
                CssListValue cssPositionValue = CssProvider.getValueService().getComputedCssListValue(
                        CssProvider.getEngineService().getComputedValueForElement(frameFormElement, XhtmlCss.BACKGROUND_POSITION_INDEX));
//                bgPainter = new BackgroundImagePainter(bgImage, repeatValue, positionValue);
                bgPainter = new BackgroundImagePainter(bgImage, cssRepeatValue, cssPositionValue, frameFormElement, frameForm.getDefaultFontSize());
            }
        } else {
            super.initializeBackgroundImage();
        }
    }
    
    /** When building the box hierarchy, instead of adding content
     * for children, add the string attribute content
     */
    protected void createChildren(CreateContext context) {
        // Since frameboxes are cached, make sure we only do this once.
        // LAYOUT on the other hand should NOT be suppressed, since it
        // may have to be done again if the parent box changes, etc.
        if (haveCreatedChildren) {
            return;
        }

        haveCreatedChildren = true;

        Element element = getElement();
        WebForm frameForm = getExternalForm();
//        if (external) {
        if (frameForm == null) {
            String desc =
                NbBundle.getMessage(ExternalDocumentBox.class, "UrlNotSupported", // NOI18N
                    getUrlString());
            LineBoxGroup old = context.lineBox;
            context.lineBox = null;
            addText(context, null, element, desc);
            finishLineBox(context);
            context.lineBox = old;

            // Clip the overflow of the text in case it spills outside the
            // requested size of the frame/fragment
            clipOverflow = true;

            return;
        }

//        if (frameForm == null) {
//            return;
//        }

        // We allow invalid forms to be shown when included in say <iframes>
        // But should I include some note about parsing errors???
//        if (frameForm.getModel().isBusted()) {
        if (frameForm.isModelBusted()) {
            return;
        }

//        Element body = frameForm.getDocument().getWebForm().getHtmlBody();
        Element body = frameForm.getHtmlBody();

        if (body == null) {
            return;
        }

        // Make sure styles inherit right into the included content
//        RaveElement.setStyleParent(body, element);
        CssProvider.getEngineService().setStyleParentForElement(body, element);
//        ((RaveDocument)frameForm.getDom()).setCssEngine(webform.getDom().getCssEngine());
//        CssProvider.getEngineService().reuseCssEngineForDocument(frameForm.getJspDom(), webform.getJspDom());
        frameForm.reuseCssStyle(webform);

//        XhtmlCssEngine engine = CssLookup.getCssEngine(body);
//        if (engine != null) {
//            engine.clearTransientStyleSheetNodes();
//        }
        CssProvider.getEngineService().clearTransientStyleSheetNodesForDocument(body.getOwnerDocument());

        // We need to have our own create context here, so fixed boxes
        // doesn't bleed into the parent document fixed box list, etc.
        CreateContext cc = new CreateContext(context);
        cc.pushPage(frameForm);

        try {
//            Font font = CssLookup.getFont(body, DesignerSettings.getInstance().getDefaultFontSize());
//            Font font = CssProvider.getValueService().getFontForElement(body, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//            cc.metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            // XXX Missing text.
            cc.metrics = CssUtilities.getDesignerFontMetricsForElement(body, null, frameForm.getDefaultFontSize());

            NodeList list = body.getChildNodes();
            int len = list.getLength();
            setProbableChildCount(len);

            for (int i = 0; i < len; i++) {
                org.w3c.dom.Node child = (org.w3c.dom.Node)list.item(i);

                if ((child.getNodeType() == Node.TEXT_NODE) && COLLAPSE &&
                        DesignerUtils.onlyWhitespace(child.getNodeValue())) {
                    continue;
                }

                addNode(cc, child, null, null, null);
            }

            fixedBoxes = cc.getFixedBoxes();
        } finally {
            WebForm page = cc.popPage();

            assert page == frameForm;
        }
    }

    // Override standard methods to give frames special treatment, since
    // they are "black boxes" as far as the box hierarchy is concerned
    protected CssBox findCssBox(int x, int y, int px, int py, int depth) {
        // Don't expose any of the boxes within the frame
        // See if this match is okay.
        // This is necesary because we investigate children whose
        // extents include absolutely positioned views
        boolean match =
            (x >= getExtentX()) && (x <= getExtentX2()) && (y >= getExtentY()) &&
            (y <= getExtentY2());

        if (match) {
            return this;
        } else {
            return null;
        }
    }

//    // XXX Get rid of this. Replace with #findCssBoxForComponentRootElement.
//    protected CssBox findCssBox(DesignBean bean) {
////        if (bean == getDesignBean()) {
////        if (bean == CssBox.getMarkupDesignBeanForCssBox(this)) {
////            return this;
////        }
//        Element componentRootElement = WebForm.getDomProviderService().getRenderedElement(bean);
//        if (componentRootElement != null && componentRootElement == CssBox.getElementForComponentRootCssBox(this)) {
//            return this;
//        }
//
//        // XXX Why not?
//        // Don't search among the children
//        return null;
//    }
    
    protected CssBox findCssBoxForComponentRootElement(Element componentRootElement) {
        if (componentRootElement != null
        && componentRootElement == CssBox.getElementForComponentRootCssBox(this)) {
            return this;
        }

        // XXX Why not?
        // Don't search among the children
        return null;
    }

    /** What should the default intrinsic width be? Mozilla 1.6 seems
     * to use 300x150.
     */
    public int getIntrinsicWidth() {
        return 300;
    }

    /** What should the default intrinsic height be? Mozilla 1.6 seems
     * to use 300x150.
     */
    public int getIntrinsicHeight() {
        return 150;
    }

//    /** Open the given page source in the editor */
//    public void open() {
//        if (frameForm == null) {
//            java.awt.Toolkit.getDefaultToolkit().beep();
//
//            return;
//        }
//
//        DataObject dobj = frameForm.getDataObject();
//        OpenCookie oc = (OpenCookie)dobj.getCookie(OpenCookie.class);
//
//        if (oc != null) {
//            oc.open();
//        }
//    }

    protected void updateSizeInfo() {
        // Unlike the main page box, we don't want the extents of the
        // view box to include the initial viewport
        width = 0;
        height = 0;
        super.updateSizeInfo();
    }

    public int getAbsoluteX() {
        ContainerBox parent = getParent();
        if (positionedBy != parent) {
            return positionedBy.getAbsoluteX() + getX() + leftMargin;
        }

        if (!layoutRoot && (parent != null)) {
            return parent.getAbsoluteX() + x + leftMargin;
        } else {
            return x + leftMargin;
        }
    }

    public int getAbsoluteY() {
        ContainerBox parent = getParent();
        if (positionedBy != parent) {
            return positionedBy.getAbsoluteY() + getY() + effectiveTopMargin;
        }

        if (!layoutRoot && (parent != null)) {
            return parent.getAbsoluteY() + y + effectiveTopMargin;
        } else {
            return y + effectiveTopMargin;
        }
    }

    public void relayout(JViewport viewport, int initialWidth, int initialHeight, int wrapWidth) {
        try {
            layoutRoot = true;
            super.relayout(viewport, initialWidth, initialHeight, wrapWidth);
        } finally {
            layoutRoot = false;
        }
    }

    /** Return the webform associated with this box, containing the
     * model for the included/referenced external document. May be null.
     */
    public WebForm getExternalForm() {
        if (frameForm == null || !frameForm.isModelValid()) {
            frameForm = findExternalForm(super.getWebForm(), getElement());
        }
        return frameForm;
    }

    /** Redefine to return the <code>WebForm</code> for the content being shown
     * in the external document. That way JSF rendering etc. will use
     * the correct form.
     * XXX This looks suspicious.
     */
    @Override
    public WebForm getWebForm() {
        WebForm frameForm = getExternalForm();
        if (frameForm != null) {
            return frameForm;
        }

        return super.getWebForm();
    }
    
    public DomProvider getExternalDomProvider() {
        WebForm externalForm = getExternalForm();
        return externalForm == null ? null : externalForm.getDomProvider();
    }
    
    protected WebForm findExternalForm(WebForm webform, Element element) {
        URL src = getContentURL(webform, element); // TODO - check for null here!
        return webform.findExternalForm(src);
    }
    
    protected abstract URL getContentURL(WebForm webform, Element element);    
}

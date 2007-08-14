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

import java.net.URL;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;

import org.openide.util.NbBundle;
import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * FrameBox represents a &lt;frame&gt; or &lt;iframe&gt; tag

 * @todo The fact that I'm inlining the body contents is a bit
 *  "hacky"; some CSS properties should be taken from the <frame>,
 *  elements (e.g. position, margin, padding), whereas others
 *  should be taken from the <body> element pointed to by the frame
 *  src.
 * @todo frameborder, marginwidth, and marginheight are not supported
 * yet; they should be, similar to the TableBox.
 *
 * @author Tor Norbye
 *
 */
public class FrameBox extends ExternalDocumentBox {
    /** Use the "getFrameBox" factory method instead */
    private FrameBox(/*WebForm frameForm,*/ WebForm webform, Element element, /*URL url,*/ BoxType boxType,
        boolean inline, boolean replaced) {
        super(webform.getPane(), /*frameForm,*/ webform, element, /*url,*/ boxType, inline, replaced);
    }

    //private Element body;

    /** Create a new framebox, or provide one from a cache */
    public static ContainerBox getFrameBox(CreateContext context, WebForm webform, Element element,
        BoxType boxType, HtmlTag tag, boolean inline) {
//        URL src = getContentURL(webform, element);
//        WebForm frameForm = null;
//
//        if (src != null) {
////            frameForm = findForm(webform, src);
//            frameForm = webform.findExternalForm(src);
//        }

//        boolean external = frameForm == WebForm.EXTERNAL;
//        boolean external = frameForm == null;

//        if (frameForm == WebForm.EXTERNAL) {
//            frameForm = null;
//        }


        FrameBox box =
            new FrameBox(/*frameForm,*/ webform, element, /*src,*/ boxType, inline, tag.isReplacedTag());
//        box.external = external;
        
        WebForm frameForm = box.getExternalForm();
        if (frameForm != null) {
            if (context.isVisitedForm(frameForm)) {
                return new StringBox(webform, element, boxType,
                    NbBundle.getMessage(FrameBox.class, "RecursiveFrame"), null, AUTO, AUTO);
            }

            // XXX Moved to designer/jsf/../JsfForm.
//            //context.visitForm(frameForm);
//            frameForm.setContextPage(webform);
        }

        return box;
    }

    protected String getUrlString() {
        Element element = getElement();
        return element == null ? null : element.getAttribute(HtmlAttribute.SRC);
    }

    /**
     * Return a URL for the frame content,
     * or null if it could not be determined.
     */
    @Override
    protected URL getContentURL(WebForm webform, Element element) {
        String src = element.getAttribute(HtmlAttribute.SRC);

        if ((src == null) || (src.length() == 0)) {
            return null;
        }

//        return InSyncService.getProvider().resolveUrl(webform.getMarkup().getBase(), webform.getJspDom(), src);
        return webform.resolveUrl(src);
    }

//    public String toString() {
//        return "FrameBox[" + paramString() + "]";
//    }

    /*
    protected String paramString() {
        return super.paramString() + ", " + markup;
    }
    */
    public void relayout(FormatContext context) {
        int oldWidth = width;
        int oldHeight = height;
        int oldContentWidth = contentWidth;
        int oldContentHeight = contentHeight;
        super.relayout(context);
        width = oldWidth;
        height = oldHeight;
        contentWidth = oldContentWidth;
        contentHeight = oldContentHeight;
    }

    // Override standard methods to give frames special treatment, since
    // they are "black boxes" as far as the box hierarchy is concerned

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

    protected void updateExtents(int px, int py, int depth) {
        // Override to ensure that we set/clip the extents to the bounding
        // rectangle of the frame.
        super.updateExtents(px, py, depth); // do it for the children....the poor children...

        if (positionedBy != getParent()) {
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

        extentX = px;
        extentY = py;
        extentX2 = px + width;
        extentY2 = py + height;
    }

    public int getPrefMinWidth() {
        return getPrefWidth();
    }

    // Override because we don't want to look inside the box; frames
    // are treated as atomic blocks - they're not breakable
    public int getPrefWidth() {
        int result;

//        if ((contentWidth != AUTO) && (contentWidth != UNINITIALIZED)) {
        if ((contentWidth != AUTO) && (contentWidth != Box.UNINITIALIZED)) {
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
}

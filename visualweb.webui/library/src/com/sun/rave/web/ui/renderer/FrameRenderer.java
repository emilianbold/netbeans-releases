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
package com.sun.rave.web.ui.renderer;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import com.sun.rave.web.ui.component.Frame;
import com.sun.rave.web.ui.util.RenderingUtilities;

/**
 * <p>Renderer for a {@link Frame} component.</p>
 */

public class FrameRenderer extends AbstractRenderer {


    // ======================================================== Static Variables

     /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] =
    { "name",  "scrolling"}; //NOI18N

    // -------------------------------------------------------- Renderer Methods


    /**
     * <p>Render the appropriate element start for the outermost
     * element.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        Frame frame = (Frame) component;
        
        // I don't think this is the correct way to write the XML
        // header /avk
        
        if (!RenderingUtilities.isPortlet(context)) {
            writer.startElement("frame", component);
       }
        
    }
    
    
    
    /**
     * <p>Render the appropriate element attributes, followed by the
     * nested <code>&lt;head&gt;</code> element, plus the beginning
     * of a nested <code>&lt;body&gt;</code> element.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
        Frame frame = (Frame) component;
        
        // Render a nested "head" element
        if (!RenderingUtilities.isPortlet(context)) {
            //id
            String id=frame.getClientId(context);
            if (id != null) {
                writer.writeAttribute("id", id, null); //NOI18N
            }
            //url
            String url=frame.getUrl();
            if (url != null) {
                RenderingUtilities.renderURLAttribute(context, 
                    writer, 
                    component, 
                    "src", //NOI18N    
                    url, 
                    null); //NOI18N                       
            }
            //class
            String styleClass = frame.getStyleClass();
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, null); //NOI18N
            }
            //style
            String style = frame.getStyle();
            if (style != null) {
                writer.writeAttribute("style", style, null); //NOI18N
            }
            //write out the rest of the attributes
            addStringAttributes(context, component, writer, stringAttributes);
             
            //frameborder
            boolean border = frame.isFrameBorder();
            if (border) {
                writer.writeAttribute("frameborder", "1", null); //NOI18N
            } else {
                writer.writeAttribute("frameborder", "0", null); //NOI18N
            }
            //longdesc
            String longdesc = frame.getLongDesc();
            if (longdesc != null) {
                writer.writeAttribute("longdesc", longdesc, null); //NOI18N
            }
            //marginWidth
            Integer  marginWidth = new Integer(frame.getMarginWidth());
            if (frame.getMarginWidth() >= 0) {
                writer.writeAttribute("marginwidth", marginWidth.toString(), null); //NOI18N
            }
            //marginHeight
            Integer marginHeight = new Integer(frame.getMarginHeight());
            if (frame.getMarginHeight() >= 0) {
                writer.writeAttribute("marginheight", marginHeight.toString(), null); //NOI18N
            }
            
            renderResizeAttribute(writer, component);
 
            //tooltip
            String toolTip = frame.getToolTip();
            if (toolTip != null) {
                writer.writeAttribute("title", toolTip, "toolTip"); //NOI18N
            }
          }
        
    }
    /**
     * <p>Render the appropriate element end.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
        Frame frame = (Frame) component;
        
        // End the outermost "html" element
        if (!RenderingUtilities.isPortlet(context)) {
            writer.write(" />"); //NOI18N
            writer.write("\n"); //NOI18N
        }
                
    }
    
    
    // ---------------------------------------------------s- protected Methods

    protected void renderResizeAttribute(ResponseWriter writer, UIComponent comp) 
            throws IOException {
        //noresize
        boolean noresize = ((Frame)comp).isNoResize();
        if (noresize) {
            writer.writeAttribute("noresize", "noresize", null); //NOI18N
        }
    }
}

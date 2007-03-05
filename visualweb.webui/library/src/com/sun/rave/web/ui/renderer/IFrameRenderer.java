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
import com.sun.rave.web.ui.component.IFrame;
import com.sun.rave.web.ui.util.RenderingUtilities;

/**
 * <p>Renderer for a {@link IFrameRenderer} component.</p>
 */

public class IFrameRenderer extends FrameRenderer {


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
        IFrame frame = (IFrame) component;
        
        // I don't think this is the correct way to write the XML
        // header /avk
        
        if (!RenderingUtilities.isPortlet(context)) {
            writer.startElement("iframe", component);
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
        
        IFrame frame = (IFrame) component;
        
        super.renderAttributes(context, component, writer);
        
        // Render a nested "head" element
        if (!RenderingUtilities.isPortlet(context)) {
            //align
            String align = frame.getAlign();
            if (align != null) {
                writer.writeAttribute("align", align, null); //NOI18N
            }
            
           //marginWidth
            String  width = frame.getWidth();
            if (width != null) {
                writer.writeAttribute("width", width.toString(), null); //NOI18N
            }
            //marginHeight
            String height = frame.getHeight();
            if (height != null) {
                writer.writeAttribute("height", height.toString(), null); //NOI18N
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
              
        // End the outermost "html" element
        if (!RenderingUtilities.isPortlet(context)) {
            writer.endElement("iframe"); //NOI18N
            writer.write("\n"); //NOI18N
        }
                
    }
    
    
    // ------------------------------------------------------- Protected Methods
    
    protected void renderResizeAttribute(ResponseWriter writer, UIComponent comp) 
            throws IOException {
            //intentionally blank
    }

}

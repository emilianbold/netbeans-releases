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
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import javax.faces.FacesException;
import com.sun.rave.web.ui.component.Body;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.MessageUtil;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Renderer for a {@link Body} component.</p>
 */

public class BodyRenderer extends AbstractRenderer {

    private static final boolean DEBUG = false;

    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] =
    { "onClick", "onDblClick", "onMouseDown", "onMouseUp",
      "onMouseOver", "onMouseMove", "onMouseOut", "onKeyPress", "onKeyDown",
      "onKeyUp", "onFocus", "onBlur"}; //NOI18N
    
    /**
     * <p>The set of integer pass-through attributes to be rendered.</p>
     */
    private static final String integerAttributes[] =
    { "tabIndex" }; //NOI18N
    
    
    /**
     * <p>Render the appropriate element start, depending on whether the
     * <code>for</code> property is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to render.
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
        // Start the appropriate element
        if (RenderingUtilities.isPortlet(context)) {
            return;
        }
        
        if(!(component instanceof Body)) {
            Object[] params = { component.toString(),
            this.getClass().getName(),
            Body.class.getName() };
            String message = MessageUtil.getMessage
                    ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                    "Renderer.component", params);              //NOI18N
            throw new FacesException(message);
        }
        
        writer.startElement("body", component); //NOI18N
    }
    
    
    /**
     * <p>Render the appropriate element attributes,
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
        if (RenderingUtilities.isPortlet(context)) {
            return;
        }
        
        Body body = (Body) component;
        
        addCoreAttributes(context, component, writer, null);
        addStringAttributes(context, component, writer, stringAttributes);
        
        
        // onload is a special case;
        String onload = body.getOnLoad();
        StringBuffer sb = new StringBuffer(256);
        if (onload != null)
            sb.append(onload);
        if (body.getFocusID(context) != null && !RenderingUtilities.isPortlet(context)) {
            if (onload != null)
                sb.append("; ");
            sb.append("return ");
            sb.append(body.getJavaScriptObjectName(context));
            sb.append(".setInitialFocus();");
        }
        if (sb.length() > 0)
            writer.writeAttribute("onload", sb.toString(), null); //NO18N
        
        // <RAVE>
        String imageUrl = body.getImageURL();
        if (imageUrl != null && imageUrl.length() > 0) {
            String resourceUrl =
                    context.getApplication().getViewHandler().getResourceURL(context, imageUrl);
            writer.writeAttribute("background", resourceUrl, null); //NOI18N
        }
        // </RAVE>
        
        // unload is a special case;
        String onUnload = body.getOnUnload();
        sb.setLength(0);
        if (onUnload != null)
            sb.append(onUnload);
        if (body.getFocusID(context) != null && !RenderingUtilities.isPortlet(context)) {
            if (onUnload != null)
                sb.append("; ");
            sb.append("return ");
            sb.append(body.getJavaScriptObjectName(context));
            sb.append(".setScrollPosition();");
        }
        if (sb.length() > 0)
            writer.writeAttribute("onunload", sb.toString(), null); //NO18N
        
        addIntegerAttributes(context, component, writer, integerAttributes);
        writer.write("\n"); //NOI18N
    }
    
    /**
     * <p>Render the appropriate element end, depending on whether the
     * <code>for</code> property is set or not.</p>
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
        
        Body body = (Body)component;
        if(body.getFocusID(context) == null || RenderingUtilities.isPortlet(context)) {
            return;
        }
        String id = body.getClientId(context);
        
        StringBuffer jsBuffer = new StringBuffer(128);
        jsBuffer.append("\n<script type=\"text/javascript\">\nvar ");
        jsBuffer.append(body.getJavaScriptObjectName(context));
        
        jsBuffer.append(" = new Body('");
        jsBuffer.append(body.getFocusID(context));
        jsBuffer.append("');");
        
        jsBuffer.append("\n</script>\n");
        
        writer.write(jsBuffer.toString());
        writer.endElement("body"); //NOI18N
        writer.write("\n"); //NOI18N
    }
}

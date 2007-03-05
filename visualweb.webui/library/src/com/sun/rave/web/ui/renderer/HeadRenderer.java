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
import java.net.URL;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.Head;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;


/**
 * <p>Renderer for a {@link Head} component.</p>
 */

public class HeadRenderer extends AbstractRenderer {


    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] = { "profile" }; //NOI18N

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
	if (!RenderingUtilities.isPortlet(context)) {
            writer.startElement("head", component); //NOI18N
	}
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
    protected void renderAttributes(FacesContext context, 
				    UIComponent component, 
                                    ResponseWriter writer) 
	throws IOException {
        
        Head head = (Head) component;
        
        if (!RenderingUtilities.isPortlet(context)) {
            
	    // Profile
            addStringAttributes(context, component, writer, stringAttributes);

	    // Meta tags
	    writer.write("\n"); //NOI18N
	    renderMetaTag("no-cache", "Pragma", writer, head); 
	    renderMetaTag("no-cache", "Cache-Control", writer, head); 
	    renderMetaTag("no-store", "Cache-Control", writer, head); 
	    renderMetaTag("max-age=0", "Cache-Control", writer, head); 
	    renderMetaTag("1", "Expires", writer, head); 

            // Title
            String title = head.getTitle();            
            if (title == null) {
                title = "";
            }
            
	    writer.startElement("title",  head);
            writer.write(title);
            writer.endElement("title");
            writer.write("\n"); //NOI18N
            
	    // Base
            if(head.isDefaultBase()) {
                writer.startElement("base", head); //NOI18N
                // TODO - verify the requirements w.r.t. printing this href
                writer.writeURIAttribute("href", Util.getBase(context), null); //NOI18N
                writer.endElement("base"); //NOI18N
                writer.write("\n"); //NOI18N
            }

	    // Link and Scripts
            Theme theme = ThemeUtilities.getTheme(context);
            //master link to always write out
            RenderingUtilities.renderJavaScript(head, theme, context, writer);
            RenderingUtilities.renderStyleSheetLink(head, theme, context, writer);
            
            writer.write(getCookieScript(context));           
	}
    }

    private String getCookieScript(FacesContext context) { 
        String viewId = context.getViewRoot().getViewId(); 
        String urlString = 
            context.getApplication().getViewHandler().getActionURL(context, viewId);
      
        StringBuffer jsBuffer = new StringBuffer(256); 
        jsBuffer.append("\n<script type=\"text/javascript\">\nvar "); 
        jsBuffer.append("sjwuic_ScrollCookie"); 
        jsBuffer.append(" = new sjwuic_ScrollCookie('"); 
        jsBuffer.append(viewId);
        jsBuffer.append("', '"); 
        jsBuffer.append(urlString); 
        jsBuffer.append("'); \n</script>\n"); 
        return jsBuffer.toString();       
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

        // Start the appropriate element
        if (!RenderingUtilities.isPortlet(context)) {
            writer.endElement("head"); //NOI18N
            writer.write("\n"); //NOI18N
        }
    }


    private void renderMetaTag(String content, String httpEquivalent, 
			       ResponseWriter writer, Head head) 
            throws IOException { 

	writer.startElement("meta", head); 
	writer.writeAttribute("content", content, null); 
	writer.writeAttribute("http-equiv", httpEquivalent, null); 
	writer.endElement("meta"); 
	writer.writeText("\n", null); 
    } 
    

}

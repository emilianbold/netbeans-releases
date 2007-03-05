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


import com.sun.rave.web.ui.component.Link;
//import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.util.RenderingUtilities;
import java.beans.Beans;

import java.io.IOException;
import java.lang.NullPointerException;
import java.lang.StringBuffer;
import java.net.URL;
import java.util.Map;

import javax.faces.component.UIComponent;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * <p>This class is responsible for rendering the link component for the
 * HTML Render Kit.</p> <p> The link component can be used as an Link</p>
 */
public class LinkRenderer extends AbstractRenderer {

    // -------------------------------------------------------- Static Variables

    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] =
    { "charset", "media", "rel", "type"}; //NOI18N

      
      // -------------------------------------------------------- Renderer Methods
         
      
      /**
       * <p>Render the start of an Link (Link) tag.</p>
       * @param context <code>FacesContext</code> for the current request
       * @param component <code>UIComponent</code> to be rendered
       * @param writer <code>ResponseWriter</code> to which the element
       * start should be rendered
       * @exception IOException if an input/output error occurs
       */
      protected void renderStart(FacesContext context, UIComponent component,
      ResponseWriter writer) throws IOException {      
          //intentionally empty
      }
      
    /**
     * <p>Render the attributes for an Link tag.  The onclick attribute will contain
     * extra javascript that will appropriately submit the form if the URL field is
     * not set.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * attributes should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
    ResponseWriter writer) throws IOException {

        //intentionally empty
    }
      
    /**
     * <p>Close off the Link tag.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * end should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
        ResponseWriter writer) throws IOException {
        // End the appropriate element

         Link link = (Link) component;
         
         if (!RenderingUtilities.isPortlet(context)) {
            writer.startElement("link", link);
            addCoreAttributes(context, component, writer, null);
            addStringAttributes(context, component, writer, stringAttributes);
	    String lang = link.getUrlLang();
            if (null != lang) {
                writer.writeAttribute("hreflang", lang, "lang"); //NOI18N
            }
            // the URL is the tough thing because it needs to be encoded:
            
            String url = link.getUrl();
            
            if (url != null) {          
                writer.writeAttribute("href", //NOI18N
                    context.getApplication().getViewHandler()
                        .getResourceURL(context, url), 
                    "url"); //NOI18N
            }

            writer.endElement("link"); //NOI18N
            writer.write("\n"); //NOI18N
        }

    }
            
      // --------------------------------------------------------- Private Methods
      
}

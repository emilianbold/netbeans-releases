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


import com.sun.rave.web.ui.component.Script;
import com.sun.rave.web.ui.util.RenderingUtilities;

import java.io.IOException;

import javax.faces.component.UIComponent;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * <p>This class is responsible for rendering the script component for the
 * HTML Render Kit.</p> <p> The script component can be used as an Script</p>
 */
public class ScriptRenderer extends AbstractRenderer {

    // -------------------------------------------------------- Static Variables

    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] =
    { "charset", "type"}; //NOI18N


      // -------------------------------------------------------- Renderer Methods
         
      
      /**
       * <p>Render the start of an Script (Script) tag.</p>
       * @param context <code>FacesContext</code> for the current request
       * @param component <code>UIComponent</code> to be rendered
       * @param writer <code>ResponseWriter</code> to which the element
       * start should be rendered
       * @exception IOException if an input/output error occurs
       */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        writer.startElement("script", component);     
    }
      
    /**
     * <p>Render the attributes for an Script tag.  The onclick attribute will contain
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

        Script script = (Script) component;
        addCoreAttributes(context, component, writer, null);
        addStringAttributes(context, component, writer, stringAttributes);
        // the URL is the tough thing because it needs to be encoded:
        String url = script.getUrl();
        if (url != null) {
            // Get resource URL -- bugtraq #6305522.
            RenderingUtilities.renderURLAttribute(context, writer, script, "src", //NO18N
                context.getApplication().getViewHandler()
                    .getResourceURL(context, url),
                "url"); //NO18N         
        }
    }
      
    /**
     * <p>Close off the Script tag.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * end should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
        ResponseWriter writer) throws IOException {
        // End the appropriate element

        Script script = (Script) component;
        
        writer.endElement("script"); //NOI18N
        writer.write("\n"); //NOI18N
       

    }
            
      // --------------------------------------------------------- Private Methods
      
}

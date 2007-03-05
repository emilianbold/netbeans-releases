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
import com.sun.rave.web.ui.component.PageSeparator;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;


/**
 * <p>Renderer for a {@link PageSeparator} component.</p>
 */

public class PageSeparatorRenderer extends AbstractRenderer {


    // ======================================================== Static Variables


    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] =
    { "onClick", "onDblClick",  "onMouseUp", //NOI18N
       "onMouseDown", "onMouseMove", "onMouseOut", "onMouseOver"}; //NOI18N


      // -------------------------------------------------------- Renderer Methods


    /**
     * <p>Render the appropriate element start, depending on whether the
     * <code>for</code> property is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component StaticText component
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
                               ResponseWriter writer) throws IOException {

    }


    /**
     * <p>Render the appropriate element attributes, followed by the
     * label content, depending on whether the <code>for</code> property
     * is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component StaticText component
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
                                    ResponseWriter writer) throws IOException {

     }


    /**
     * <p>Render the appropriate element end, depending on whether the
     * <code>for</code> property is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
                             ResponseWriter writer) throws IOException {
        PageSeparator pageSep = (PageSeparator) component;
        
        writer.startElement("table", component);
        String style = pageSep.getStyle();
        if (style != null) {
            writer.writeAttribute("style", style, null); // NOI18N
        }
        RenderingUtilities.renderStyleClass(context, writer, component, null);
        writer.writeAttribute("border", "0", null); // NOI18N
        writer.writeAttribute("width", "100%", null); // NOI18N
        writer.writeAttribute("cellpadding", "0", null); // NOI18N
        writer.writeAttribute("cellspacing", "0", null); // NOI18N        
        writer.startElement("tr", component);
        writer.startElement("td", component);
        writer.writeAttribute("colspan", "3", null); // NOI18N
        
        RenderingUtilities.renderSpacer(context, writer, component, 30, 1);        
        writer.endElement("td");
        writer.endElement("tr");
        writer.startElement("tr", component);
        writer.startElement("td", component);        
        RenderingUtilities.renderSpacer(context, writer, component, 1, 10);        
         writer.endElement("td");
        writer.startElement("td", component);
        Theme theme = ThemeUtilities.getTheme(context);

        writer.writeAttribute("class", theme.getStyleClass(
            ThemeStyles.TITLE_LINE), null); // NOI18N
        writer.writeAttribute("width", "100%", null); // NOI18N
        RenderingUtilities.renderSpacer(context, writer, component, 1, 1);        
        writer.endElement("td");
        writer.startElement("td", component);
        RenderingUtilities.renderSpacer(context, writer, component, 1, 10);        
        writer.endElement("td");
        writer.endElement("tr");
        writer.endElement("table");
    }


    // --------------------------------------------------------- Private Methods



}

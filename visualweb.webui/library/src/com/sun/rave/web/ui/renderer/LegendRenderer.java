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
import java.util.Properties;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.Legend;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>Renderer for an {@link Legend} component.</p>
 *
 */
public class LegendRenderer extends AbstractRenderer {

    // Default position.
    private static final String DEFAULT_POSITION = "right";

    /** Creates a new instance of LegendRenderer */
    public LegendRenderer() {
        // default constructor
    }    

    /**
     * Renders the legend.
     * 
     * @param context The current FacesContext
     * @param component The Legend object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        if (context == null || component == null || writer == null) {
            throw new NullPointerException();
        }
        
	Legend legend = (Legend) component;

        if (!legend.isRendered()) {
            return;
        }

	// Render the outer div
	renderOuterDiv(context, legend, writer);
	// Render the legend image
	RenderingUtilities.renderComponent(legend.getLegendImage(), context);
	writer.write("&nbsp;"); // NOI18N
	// Render the legend text
	String text = (legend.getText() != null) ? legend.getText() : 
	    getTheme().getMessage("Legend.requiredField"); //NOI18N
	writer.writeText(text, null);
	// Close the outer div
	writer.endElement("div"); //NOI18N
    }	

    /** 
     * Renders the outer div which contains the legend.
     * 
     * @param context The current FacesContext
     * @param alert The Legend object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderOuterDiv(FacesContext context, 
            Legend legend, ResponseWriter writer) throws IOException {

        String style = legend.getStyle();
	String id = legend.getClientId(context);
	String divStyleClass = getTheme().getStyleClass(
			   ThemeStyles.LABEL_REQUIRED_DIV);
	String align = (legend.getPosition() != null) ?
	    legend.getPosition() : DEFAULT_POSITION;

        writer.startElement("div", legend); //NOI18N
	if (id != null) {
	    writer.writeAttribute("id", id, null);  //NOI18N
	}
	writer.writeAttribute("align", align, null); //NOI18N
        if (style != null) {
            writer.writeAttribute("style", style, "style");  //NOI18N
        }
	RenderingUtilities.renderStyleClass(context, writer,
	    (UIComponent) legend, divStyleClass);
    }

    /*
     * Utility to get theme.
     */
    private Theme getTheme() {
	return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

}


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

import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.util.RenderingUtilities;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * Renderer for a {@link com.sun.rave.web.ui.component.PanelLayout} component.
 *
 * @author gjmurphy
 */
public class PanelLayoutRenderer extends AbstractRenderer {
    
    protected void renderStart(FacesContext context, UIComponent component, ResponseWriter writer) 
    throws IOException {
        writer.startElement("div", component); //NOI18N
    }

    protected void renderAttributes(FacesContext context, UIComponent component, ResponseWriter writer) 
    throws IOException {
        PanelLayout panelLayout = (PanelLayout)component;
        StringBuffer buffer = new StringBuffer();
        
        // Write id attribute
        String id = component.getId();
        writer.writeAttribute("id", panelLayout.getClientId(context), "id");
        
        // Write style attribute
        if (PanelLayout.GRID_LAYOUT.equals(panelLayout.getPanelLayout()))
	    buffer.append("position: relative; -rave-layout: grid;"); //NOI18N
        String style = panelLayout.getStyle();
        if (style != null && style.length() > 0) {
            buffer.append(" ");
            buffer.append(style);
        }
        writer.writeAttribute("style", buffer.toString(), "style");
        
        // Write style class attribute
        RenderingUtilities.renderStyleClass(context, writer, component, null);
    }

    protected void renderEnd(FacesContext context, UIComponent component, ResponseWriter writer) 
    throws IOException {
        writer.endElement("div"); //NOI18N
    }

    public boolean getRendersChildren() {
        return true;
    }
    
}

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

import com.sun.rave.web.ui.component.PanelGroup;
import com.sun.rave.web.ui.util.RenderingUtilities;
import java.io.IOException;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * Renderer for a {@link com.sun.rave.web.ui.component.PanelGroup} component.
 *
 * @author gjmurphy
 */
public class PanelGroupRenderer extends AbstractRenderer {
    
    private String elementName;
    
    protected void renderStart(FacesContext context, UIComponent component, ResponseWriter writer)
            throws IOException {
        PanelGroup panelGroup = (PanelGroup) component;
        if (panelGroup.isBlock())
            elementName = "div"; //NOI18N
        else
            elementName = "span"; //NOI18N
        writer.startElement(elementName, component);
    }
    
    protected void renderAttributes(FacesContext context, UIComponent component, ResponseWriter writer)
            throws IOException {
        addCoreAttributes(context, component, writer, null);
    }
    
    public boolean getRendersChildren() {
        return true;
    }
    
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        PanelGroup panelGroup = (PanelGroup) component;
        List children = panelGroup.getChildren();
        ResponseWriter writer = context.getResponseWriter();
        UIComponent separatorFacet = panelGroup.getFacet(PanelGroup.SEPARATOR_FACET);
        if (separatorFacet != null) {
            for (int i = 0; i < children.size(); i++) {
                if (i > 0)
                    RenderingUtilities.renderComponent(separatorFacet, context);
                RenderingUtilities.renderComponent((UIComponent) children.get(i), context);
            }
        } else {
            String separator = panelGroup.getSeparator();
            if (separator == null)
                separator = "\n";
            for (int i = 0; i < children.size(); i++) {
                if (i > 0)
                    writer.write(separator);
                RenderingUtilities.renderComponent((UIComponent) children.get(i), context);
            }
        }
    }
    
    protected void renderEnd(FacesContext context, UIComponent component, ResponseWriter writer)
            throws IOException {
        writer.endElement(elementName);
    }
    
}

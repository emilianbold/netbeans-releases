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
package org.netbeans.modules.visualweb.web.ui.dt.renderer;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * A delegating renderer for panel-like components. Outputs default minimum
 * CSS width and height settings if panel contains no children.
 *
 * @author gjmurphy
 */
public abstract class PanelDesignTimeRenderer extends AbstractDesignTimeRenderer {

    /** Creates a new instance of PanelDesignTimeRenderer */
    public PanelDesignTimeRenderer(Renderer renderer) {
        super(renderer);
    }

    abstract protected String getContainerElementName(UIComponent component);

    abstract protected String getShadowText(UIComponent component);

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (component.getChildCount() == 0) {
            ResponseWriter writer = context.getResponseWriter();
            writer.startElement(getContainerElementName(component), component);
            String id = component.getId();
            writer.writeAttribute("id", id, "id"); //NOI18N

            // Calculate CSS height and width style settings
            UIComponent parentComponent = component.getParent();
            StringBuffer sb = new StringBuffer();
            String style = (String) component.getAttributes().get("style");
            if ((style != null) && (style.length() > 0)) {
                sb.append(style);
                sb.append("; "); // NOI18N
            }
            if (style == null || style.indexOf("width:") == -1) {
                sb.append("width: 128px; "); // NOI18N
            }
            if (style == null || style.indexOf("height:") == -1) {
                if ("span".equals(getContainerElementName(component))) {
                    sb.append("height: 24px; "); // NOI18N
                } else {
                    sb.append("height: 128px; "); // NOI18N
                }
            }
            writer.writeAttribute("style", sb.toString(), null); //NOI18N
            sb.setLength(0);

            // Calculate CSS style classes
            String styleClass = (String) component.getAttributes().get("styleClass");
            if ((styleClass != null) && (styleClass.length() > 0)) {
                sb.append(styleClass);
                sb.append(" ");
            }
            sb.append(UNINITITIALIZED_STYLE_CLASS);
            sb.append(" ");
            sb.append(BORDER_STYLE_CLASS);
            writer.writeAttribute("class", sb.toString(), null); // NOI18

            writer.writeText(getShadowText(component), null);
            writer.endElement(getContainerElementName(component));

            return;
        }

        super.encodeBegin(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (component.getChildCount() == 0) {
            return;
        }

        super.encodeEnd(context, component);
    }

}

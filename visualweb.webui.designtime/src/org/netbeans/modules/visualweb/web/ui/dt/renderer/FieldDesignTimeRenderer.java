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

import com.sun.rave.web.ui.component.Field;
import com.sun.rave.web.ui.component.Icon;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
import java.io.IOException;
import javax.faces.render.Renderer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;


/**
 * A delegating renderer for components based on {@link org.netbeans.modules.visualweb.web.ui.component.Field}.
 * This delegating renderer takes over when the component is read-only, since the
 * field renderers replace the field component with a proxy component when the
 * <code>readOnly</code> property is true, which makes the resulting HTML unselectable
 * on the designer. This delegating renderer also provides a shadow text value
 * when the component is read-only and there is no text value.
 *
 * @author gjmurphy
 */
public class FieldDesignTimeRenderer extends AbstractDesignTimeRenderer {

    /** Creates a new instance of FieldDesignTimeRenderer */
    public FieldDesignTimeRenderer(Renderer renderer) {
        super(renderer);
    }

    protected String getShadowText(FacesContext context, Field field) {
        return DesignMessageUtil.getMessage(FieldDesignTimeRenderer.class, "field.readOnly.value");
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        Field field = (Field) component;
        if (field.isReadOnly()) {
            ResponseWriter writer = context.getResponseWriter();
            Object value = field.getText();
            writer.startElement("span", field); // NOI18N
            writer.writeAttribute("id", field.getId(), "id"); //NOI18N
            String style = field.getStyle();
            if (style != null && style.length() > 0)
                writer.writeAttribute("style", style, null); //NOI18N
            String styleClass = field.getStyleClass();
            StringBuffer styleClassBuffer = new StringBuffer();
            if (styleClass != null)
                styleClassBuffer.append(styleClass);
            UIComponent label = field.getLabelComponent(context, "");
            if (label != null) {
                writer.writeAttribute("class", styleClassBuffer.toString(), null); // NOI18N
                styleClassBuffer.setLength(0);
                RenderingUtilities.renderComponent(label, context);
                Theme theme = ThemeUtilities.getTheme(context);
                Icon icon = theme.getIcon(ThemeImages.DOT);
                icon.setId(component.getId().concat("_spacer")); //NOI18N
                icon.setHeight(1);
                icon.setWidth(10);
                RenderingUtilities.renderComponent(icon, context);
                writer.startElement("span", field); // NOI18N
                writer.writeAttribute("id", field.getId().concat("_readOnly"), "id"); //NOI18N
            }
            if (value == null) {
                if (styleClassBuffer.length() > 0)
                    styleClassBuffer.append(' ');
                styleClassBuffer.append(UNINITITIALIZED_STYLE_CLASS);
                writer.writeAttribute("class", styleClassBuffer.toString(), null); // NOI18N
                writer.writeText(getShadowText(context, field), null); // NOI18N
            } else {
                writer.writeAttribute("class", styleClassBuffer.toString(), null); // NOI18N
                writer.writeText(ConversionUtilities.convertValueToString(field, value), null);
            }
            writer.endElement("span"); // NOI18N
        } else {
            super.encodeEnd(context, component);
        }
    }
    
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Field field = (Field) component;
        if (!field.isReadOnly())
            super.encodeBegin(context, component);
    }
    
}

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

import com.sun.rave.web.ui.component.ImageComponent;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.renderer.ImageRenderer;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * A delegating renderer for {@link org.netbeans.modules.visualweb.web.ui.component.ImageComponent}.
 * This delegating renderer takes over when the component has no image or icon
 * property set, outputting the component's display name.
 *
 * @author gjmurphy
 */
public class ImageDesignTimeRenderer extends AbstractDesignTimeRenderer {

    public ImageDesignTimeRenderer() {
        super(new ImageRenderer());
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        ImageComponent image = (ImageComponent) component;
        if (image.getUrl() == null && image.getIcon() == null) {
            ResponseWriter writer = context.getResponseWriter();
            writer.startElement("span", image); // NOI18N
            writer.writeAttribute("id", image.getId(), "id"); //NOI18N
            writer.writeAttribute("style", image.getStyle(), "style"); //NOI18N
            writer.writeText("<" + DesignMessageUtil.getMessage(StaticTextDesignTimeRenderer.class, "image.label") + ">", null); //NOI18N
            writer.endElement("span"); // NOI18N
        } else {
            super.encodeBegin(context, component);
        }
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ImageComponent image = (ImageComponent) component;
        if (image.getUrl() != null || image.getIcon() != null) {
            super.encodeEnd(context, component);
        }
    }

}

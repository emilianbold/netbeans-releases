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

import com.sun.rave.web.ui.component.Anchor;
import com.sun.rave.web.ui.component.PropertySheetSection;
import com.sun.rave.web.ui.component.SkipHyperlink;
import com.sun.rave.web.ui.renderer.AnchorRenderer;
import com.sun.rave.web.ui.util.RenderingUtilities;
import java.io.IOException;
import java.net.URL;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * A delegating renderer for {@link org.netbeans.modules.visualweb.web.ui.component.Anchor} that
 * outputs an HTML named anchor, and an image with the anchor icon, only if
 * the anchor is not being used as an unparented helper component and it is
 * not a child of the utility SkipHyperlink.
 *
 * @author gjmurphy
 */
public class AnchorDesignTimeRenderer extends AbstractDesignTimeRenderer {

    static final String ANCHOR_ICON =
            "/org/netbeans/modules/visualweb/web/ui/dt/component/Anchor_C16.png"; //NOI18N

    boolean isTextDefaulted;

    public AnchorDesignTimeRenderer() {
        super(new AnchorRenderer());
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        UIComponent parent = component.getParent();
        if (component instanceof Anchor && parent != null && !SkipHyperlink.class.isAssignableFrom(parent.getClass())
                && !PropertySheetSection.class.isAssignableFrom(parent.getClass())) {
            Anchor anchor = (Anchor) component;
            ResponseWriter writer = context.getResponseWriter();
            writer.startElement("a", anchor); //NOI18N
            String id = anchor.getId();
            writer.writeAttribute("id", id, "id"); //NOI18N
            String style = anchor.getStyle();
            if (style != null)
                writer.writeAttribute("style", style, null); //NOI18N
            String styleClass = anchor.getStyleClass();
            if (styleClass != null)
                RenderingUtilities.renderStyleClass(context, writer, component, null);
            writer.writeAttribute("name", id, null); //NOI18N
            writer.startElement("img", anchor); //NOI18N
            URL url = this.getClass().getResource(ANCHOR_ICON); // NOI18N
            writer.writeURIAttribute("src", url, null); //NOI18N
            writer.endElement("img"); // NOI18N
            writer.endElement("a"); //NOI18N
        }
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
    }

    public void encodeChildren(FacesContext context, UIComponent component) {
    }

}

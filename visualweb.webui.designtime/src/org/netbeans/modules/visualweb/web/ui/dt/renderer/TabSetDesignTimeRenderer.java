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

import com.sun.rave.web.ui.component.TabSet;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.renderer.TabSetRenderer;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.render.Renderer;

/**
 * A delegating renderer for {@link org.netbeans.modules.visualweb.web.ui.component.TabSet} that renders
 * a minimal block of markup when the there are no tab children.
 *
 * @author gjmurphy
 */
public class TabSetDesignTimeRenderer extends AbstractDesignTimeRenderer {

    /** Creates a new instance of TabSetDesignTimeRenderer */
    public TabSetDesignTimeRenderer() {
        super(new TabSetRenderer());
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (component instanceof TabSet && component.getChildCount() == 0) {
            ResponseWriter writer = context.getResponseWriter();
            writer.startElement("div", component);
            String style = ((TabSet) component).getStyle();
            writer.writeAttribute("style", style, "style");
            writer.startElement("span", component);
            writer.writeAttribute("class", super.UNINITITIALIZED_STYLE_CLASS, "class");
            String label = DesignMessageUtil.getMessage(TabSetDesignTimeRenderer.class, "tabSet.label");
            char[] chars = label.toCharArray();
            writer.writeText(chars, 0, chars.length);
            writer.endElement("span");
            writer.endElement("div");
        } else {
            super.encodeBegin(context, component);
        }
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (!(component instanceof TabSet && component.getChildCount() == 0))
            super.encodeEnd(context, component);
    }

}

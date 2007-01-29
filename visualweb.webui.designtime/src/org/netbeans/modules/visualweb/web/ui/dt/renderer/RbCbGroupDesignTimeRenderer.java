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

import com.sun.rave.web.ui.component.Selector;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;

/**
 * A delegating renderer base class for {@link org.netbeans.modules.visualweb.web.ui.component.CheckboxGroup}
 * and {@link org.netbeans.modules.visualweb.web.ui.component.RadioButtonGroup}. If component's items
 * property is not bound, outputs a minimal block of markup so that the component
 * can be manipulated on the design surface.
 *
 * @author gjmurphy
 */
public abstract class RbCbGroupDesignTimeRenderer extends SelectorDesignTimeRenderer {

    String label;

    /** Creates a new instance of RbCbGroupDesignTimeRenderer */
    public RbCbGroupDesignTimeRenderer(Renderer renderer, String label) {
        super(renderer);
        this.label = label;
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (component instanceof Selector) {
            Selector selector = (Selector) component;
            ValueBinding itemsBinding = selector.getValueBinding("items");
            if (itemsBinding == null) {
                ResponseWriter writer = context.getResponseWriter();
                writer.startElement("div", component);
                String style = selector.getStyle();
                writer.writeAttribute("style", style, "style");
                writer.startElement("span", component);
                writer.writeAttribute("class", super.UNINITITIALIZED_STYLE_CLASS, "class");
                char[] chars = label.toCharArray();
                writer.writeText(chars, 0, chars.length);
                writer.endElement("span");
                writer.endElement("div");
            }
        }
        super.encodeBegin(context, component);
    }

}

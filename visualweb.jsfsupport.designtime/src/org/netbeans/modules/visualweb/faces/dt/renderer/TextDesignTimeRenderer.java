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

package org.netbeans.modules.visualweb.faces.dt.renderer;

import com.sun.faces.renderkit.html_basic.TextRenderer;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * A delegating renderer for {@link javax.faces.component.UIOutput}. If there
 * is no value, a "shadow" value is set.
 *
 * @author gjmurphy
 */

public class TextDesignTimeRenderer extends AbstractDesignTimeRenderer {

    static ComponentBundle bundle = ComponentBundle.getBundle(TextDesignTimeRenderer.class);

    public TextDesignTimeRenderer() {
        super(new TextRenderer());
    }

    boolean shadowValueSet;

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        shadowValueSet = false;
        if (component instanceof UIOutput) {
            Object value = ((UIOutput) component).getValue();
            if (!(component instanceof UIInput) && value == null) {
                Map attributesMap = component.getAttributes();
                String styleClass = (String) attributesMap.get("styleClass"); //NOI18N
                attributesMap.put("styleClass", addStyleClass(styleClass, UNINITITIALIZED_STYLE_CLASS)); //NOI18N
                ((UIOutput) component).setValue(bundle.getMessage("Text.default.label")); //NOI18N
                shadowValueSet = true;
            }
        }
        this.renderer.encodeBegin(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        this.renderer.encodeEnd(context, component);
        if (shadowValueSet) {
            Map attributesMap = component.getAttributes();
            String styleClass = removeStyleClass((String) attributesMap.get("styleClass"), UNINITITIALIZED_STYLE_CLASS); //NOI18N
            if (styleClass == null || styleClass.length() == 0)
                attributesMap.put("styleClass", null); //NOI18N
            else
                attributesMap.put("styleClass", styleClass); //NOI18N
            ((UIOutput) component).setValue(null);
        }
    }

}

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
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;

/**
 * A delegating renderer for components that extend
 * {@link org.netbeans.modules.visualweb.web.ui.component.UICommand}, which does two things. If
 * the value property is not set, temporarily sets the value and style to
 * cause the component to be rendered with a default "shadow label". If the
 * value property is bound, but the value is null or the empty string,
 * temporarily sets the value to a "dummy string" that is keyed to the type
 * of the value.
 *
 * @author gjmurphy
 */
public abstract class ActionSourceDesignTimeRenderer extends AbstractDesignTimeRenderer {

    protected static String STYLE_CLASS_PROP = "styleClass"; //NOI18N
    protected static String VALUE_PROP = "value"; //NOI18N

    boolean isTextSet;
    boolean isStyleSet;

    public ActionSourceDesignTimeRenderer(Renderer renderer) {
        super(renderer);
    }

    /**
     * Returns a display string to set as the component's value if shadowed
     * text is required.
     */
    protected abstract String getShadowText();

    /**
     * Determines if shadowed text is required. Default implementation is
     * simply to check for a null value.
     */
    protected boolean needsShadowText(UICommand component) {
        return component.getValue() == null;
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (UICommand.class.isAssignableFrom(component.getClass())) {
            ValueBinding valueBinding = component.getValueBinding(VALUE_PROP); //NOI18N
            UICommand command = (UICommand) component;
            Object value = command.getValue();
            if (valueBinding != null && (value == null || value.toString().length() == 0)) {
                Object dummyValue = getDummyData(context, valueBinding);
                command.setValue(dummyValue);
                isTextSet = true;
            } else if (needsShadowText(command)) {
                command.setValue(getShadowText());
                String styleClass = (String) component.getAttributes().get(STYLE_CLASS_PROP);
                component.getAttributes().put(STYLE_CLASS_PROP, addStyleClass(styleClass, UNINITITIALIZED_STYLE_CLASS));
                isTextSet = true;
                isStyleSet = true;
            }
        }
        super.encodeBegin(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        super.encodeEnd(context, component);
        if (isTextSet) {
            UICommand command = (UICommand)component;
            command.setValue(null);
            isTextSet = false;
        }
        if (isStyleSet) {
            String styleClass = (String) component.getAttributes().get(STYLE_CLASS_PROP);
            component.getAttributes().put(STYLE_CLASS_PROP, removeStyleClass(styleClass, UNINITITIALIZED_STYLE_CLASS));
            isStyleSet = false;
        }
    }

}

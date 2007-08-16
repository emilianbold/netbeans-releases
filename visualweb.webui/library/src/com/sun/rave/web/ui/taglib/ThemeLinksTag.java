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
package com.sun.rave.web.ui.taglib;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentTag;
import com.sun.rave.web.ui.el.ConstantMethodBinding;

/**
 * <p>Auto-generated component tag class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public class ThemeLinksTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.ThemeLinks";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.ThemeLinks";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        javaScript = null;
        styleSheet = null;
        styleSheetInline = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (javaScript != null) {
            if (isValueReference(javaScript)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(javaScript);
                _component.setValueBinding("javaScript", _vb);
            } else {
                _component.getAttributes().put("javaScript", Boolean.valueOf(javaScript));
            }
        }
        if (styleSheet != null) {
            if (isValueReference(styleSheet)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(styleSheet);
                _component.setValueBinding("styleSheet", _vb);
            } else {
                _component.getAttributes().put("styleSheet", Boolean.valueOf(styleSheet));
            }
        }
        if (styleSheetInline != null) {
            if (isValueReference(styleSheetInline)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(styleSheetInline);
                _component.setValueBinding("styleSheetInline", _vb);
            } else {
                _component.getAttributes().put("styleSheetInline", Boolean.valueOf(styleSheetInline));
            }
        }
    }

    // javaScript
    private String javaScript = null;
    public void setJavaScript(String javaScript) {
        this.javaScript = javaScript;
    }

    // styleSheet
    private String styleSheet = null;
    public void setStyleSheet(String styleSheet) {
        this.styleSheet = styleSheet;
    }

    // styleSheetInline
    private String styleSheetInline = null;
    public void setStyleSheetInline(String styleSheetInline) {
        this.styleSheetInline = styleSheetInline;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

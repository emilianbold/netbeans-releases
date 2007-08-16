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

public class PropertyTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.Property";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.Property";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        disabled = null;
        helpText = null;
        label = null;
        labelAlign = null;
        noWrap = null;
        overlapLabel = null;
        style = null;
        styleClass = null;
        visible = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (disabled != null) {
            if (isValueReference(disabled)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(disabled);
                _component.setValueBinding("disabled", _vb);
            } else {
                _component.getAttributes().put("disabled", Boolean.valueOf(disabled));
            }
        }
        if (helpText != null) {
            if (isValueReference(helpText)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(helpText);
                _component.setValueBinding("helpText", _vb);
            } else {
                _component.getAttributes().put("helpText", helpText);
            }
        }
        if (label != null) {
            if (isValueReference(label)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(label);
                _component.setValueBinding("label", _vb);
            } else {
                _component.getAttributes().put("label", label);
            }
        }
        if (labelAlign != null) {
            if (isValueReference(labelAlign)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(labelAlign);
                _component.setValueBinding("labelAlign", _vb);
            } else {
                _component.getAttributes().put("labelAlign", labelAlign);
            }
        }
        if (noWrap != null) {
            if (isValueReference(noWrap)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(noWrap);
                _component.setValueBinding("noWrap", _vb);
            } else {
                _component.getAttributes().put("noWrap", Boolean.valueOf(noWrap));
            }
        }
        if (overlapLabel != null) {
            if (isValueReference(overlapLabel)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(overlapLabel);
                _component.setValueBinding("overlapLabel", _vb);
            } else {
                _component.getAttributes().put("overlapLabel", Boolean.valueOf(overlapLabel));
            }
        }
        if (style != null) {
            if (isValueReference(style)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(style);
                _component.setValueBinding("style", _vb);
            } else {
                _component.getAttributes().put("style", style);
            }
        }
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(styleClass);
                _component.setValueBinding("styleClass", _vb);
            } else {
                _component.getAttributes().put("styleClass", styleClass);
            }
        }
        if (visible != null) {
            if (isValueReference(visible)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(visible);
                _component.setValueBinding("visible", _vb);
            } else {
                _component.getAttributes().put("visible", Boolean.valueOf(visible));
            }
        }
    }

    // disabled
    private String disabled = null;
    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

    // helpText
    private String helpText = null;
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    // label
    private String label = null;
    public void setLabel(String label) {
        this.label = label;
    }

    // labelAlign
    private String labelAlign = null;
    public void setLabelAlign(String labelAlign) {
        this.labelAlign = labelAlign;
    }

    // noWrap
    private String noWrap = null;
    public void setNoWrap(String noWrap) {
        this.noWrap = noWrap;
    }

    // overlapLabel
    private String overlapLabel = null;
    public void setOverlapLabel(String overlapLabel) {
        this.overlapLabel = overlapLabel;
    }

    // style
    private String style = null;
    public void setStyle(String style) {
        this.style = style;
    }

    // styleClass
    private String styleClass = null;
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    // visible
    private String visible = null;
    public void setVisible(String visible) {
        this.visible = visible;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

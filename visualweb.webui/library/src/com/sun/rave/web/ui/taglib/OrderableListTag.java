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

public class OrderableListTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.OrderableList";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.OrderableList";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        disabled = null;
        label = null;
        labelLevel = null;
        labelOnTop = null;
        list = null;
        moveTopBottom = null;
        multiple = null;
        readOnly = null;
        rows = null;
        style = null;
        styleClass = null;
        tabIndex = null;
        toolTip = null;
        visible = null;
        converter = null;
        immediate = null;
        required = null;
        validator = null;
        valueChangeListener = null;
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
        if (label != null) {
            if (isValueReference(label)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(label);
                _component.setValueBinding("label", _vb);
            } else {
                _component.getAttributes().put("label", label);
            }
        }
        if (labelLevel != null) {
            if (isValueReference(labelLevel)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(labelLevel);
                _component.setValueBinding("labelLevel", _vb);
            } else {
                _component.getAttributes().put("labelLevel", Integer.valueOf(labelLevel));
            }
        }
        if (labelOnTop != null) {
            if (isValueReference(labelOnTop)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(labelOnTop);
                _component.setValueBinding("labelOnTop", _vb);
            } else {
                _component.getAttributes().put("labelOnTop", Boolean.valueOf(labelOnTop));
            }
        }
        if (list != null) {
            if (isValueReference(list)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(list);
                _component.setValueBinding("list", _vb);
            } else {
                _component.getAttributes().put("list", list);
            }
        }
        if (moveTopBottom != null) {
            if (isValueReference(moveTopBottom)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(moveTopBottom);
                _component.setValueBinding("moveTopBottom", _vb);
            } else {
                _component.getAttributes().put("moveTopBottom", Boolean.valueOf(moveTopBottom));
            }
        }
        if (multiple != null) {
            if (isValueReference(multiple)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(multiple);
                _component.setValueBinding("multiple", _vb);
            } else {
                _component.getAttributes().put("multiple", Boolean.valueOf(multiple));
            }
        }
        if (readOnly != null) {
            if (isValueReference(readOnly)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(readOnly);
                _component.setValueBinding("readOnly", _vb);
            } else {
                _component.getAttributes().put("readOnly", Boolean.valueOf(readOnly));
            }
        }
        if (rows != null) {
            if (isValueReference(rows)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(rows);
                _component.setValueBinding("rows", _vb);
            } else {
                _component.getAttributes().put("rows", Integer.valueOf(rows));
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
        if (tabIndex != null) {
            if (isValueReference(tabIndex)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(tabIndex);
                _component.setValueBinding("tabIndex", _vb);
            } else {
                _component.getAttributes().put("tabIndex", Integer.valueOf(tabIndex));
            }
        }
        if (toolTip != null) {
            if (isValueReference(toolTip)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(toolTip);
                _component.setValueBinding("toolTip", _vb);
            } else {
                _component.getAttributes().put("toolTip", toolTip);
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
        if (converter != null) {
            if (isValueReference(converter)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(converter);
                _component.setValueBinding("converter", _vb);
            } else {
                Converter _converter = FacesContext.getCurrentInstance().
                    getApplication().createConverter(converter);
                _component.getAttributes().put("converter", _converter);
            }
        }
        if (immediate != null) {
            if (isValueReference(immediate)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(immediate);
                _component.setValueBinding("immediate", _vb);
            } else {
                _component.getAttributes().put("immediate", Boolean.valueOf(immediate));
            }
        }
        if (required != null) {
            if (isValueReference(required)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(required);
                _component.setValueBinding("required", _vb);
            } else {
                _component.getAttributes().put("required", Boolean.valueOf(required));
            }
        }
        if (validator != null) {
            if (isValueReference(validator)) {
                MethodBinding _mb = getFacesContext().getApplication().createMethodBinding(validator, validatorArgs);
                _component.getAttributes().put("validator", _mb);
            } else {
                throw new IllegalArgumentException(validator);
            }
        }
        if (valueChangeListener != null) {
            if (isValueReference(valueChangeListener)) {
                MethodBinding _mb = getFacesContext().getApplication().createMethodBinding(valueChangeListener, valueChangeListenerArgs);
                _component.getAttributes().put("valueChangeListener", _mb);
            } else {
                throw new IllegalArgumentException(valueChangeListener);
            }
        }
    }

    // disabled
    private String disabled = null;
    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

    // label
    private String label = null;
    public void setLabel(String label) {
        this.label = label;
    }

    // labelLevel
    private String labelLevel = null;
    public void setLabelLevel(String labelLevel) {
        this.labelLevel = labelLevel;
    }

    // labelOnTop
    private String labelOnTop = null;
    public void setLabelOnTop(String labelOnTop) {
        this.labelOnTop = labelOnTop;
    }

    // list
    private String list = null;
    public void setList(String list) {
        this.list = list;
    }

    // moveTopBottom
    private String moveTopBottom = null;
    public void setMoveTopBottom(String moveTopBottom) {
        this.moveTopBottom = moveTopBottom;
    }

    // multiple
    private String multiple = null;
    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

    // readOnly
    private String readOnly = null;
    public void setReadOnly(String readOnly) {
        this.readOnly = readOnly;
    }

    // rows
    private String rows = null;
    public void setRows(String rows) {
        this.rows = rows;
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

    // tabIndex
    private String tabIndex = null;
    public void setTabIndex(String tabIndex) {
        this.tabIndex = tabIndex;
    }

    // toolTip
    private String toolTip = null;
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    // visible
    private String visible = null;
    public void setVisible(String visible) {
        this.visible = visible;
    }

    // converter
    private String converter = null;
    public void setConverter(String converter) {
        this.converter = converter;
    }

    // immediate
    private String immediate = null;
    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }

    // required
    private String required = null;
    public void setRequired(String required) {
        this.required = required;
    }

    // validator
    private String validator = null;
    public void setValidator(String validator) {
        this.validator = validator;
    }

    // valueChangeListener
    private String valueChangeListener = null;
    public void setValueChangeListener(String valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

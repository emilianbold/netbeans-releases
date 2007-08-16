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

public class EditableListTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.EditableList";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.EditableList";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        disabled = null;
        fieldLabel = null;
        fieldValidator = null;
        labelLevel = null;
        list = null;
        listLabel = null;
        listOnTop = null;
        listValidator = null;
        maxLength = null;
        multiple = null;
        readOnly = null;
        rows = null;
        sorted = null;
        style = null;
        styleClass = null;
        tabIndex = null;
        toolTip = null;
        visible = null;
        required = null;
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
        if (fieldLabel != null) {
            if (isValueReference(fieldLabel)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(fieldLabel);
                _component.setValueBinding("fieldLabel", _vb);
            } else {
                _component.getAttributes().put("fieldLabel", fieldLabel);
            }
        }
        if (fieldValidator != null) {
            if (isValueReference(fieldValidator)) {
                MethodBinding _mb = getFacesContext().getApplication().createMethodBinding(fieldValidator, validatorArgs);
                _component.getAttributes().put("fieldValidator", _mb);
            } else {
                throw new IllegalArgumentException(fieldValidator);
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
        if (list != null) {
            if (isValueReference(list)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(list);
                _component.setValueBinding("list", _vb);
            } else {
                _component.getAttributes().put("list", list);
            }
        }
        if (listLabel != null) {
            if (isValueReference(listLabel)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(listLabel);
                _component.setValueBinding("listLabel", _vb);
            } else {
                _component.getAttributes().put("listLabel", listLabel);
            }
        }
        if (listOnTop != null) {
            if (isValueReference(listOnTop)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(listOnTop);
                _component.setValueBinding("listOnTop", _vb);
            } else {
                _component.getAttributes().put("listOnTop", Boolean.valueOf(listOnTop));
            }
        }
        if (listValidator != null) {
            if (isValueReference(listValidator)) {
                MethodBinding _mb = getFacesContext().getApplication().createMethodBinding(listValidator, validatorArgs);
                _component.getAttributes().put("listValidator", _mb);
            } else {
                throw new IllegalArgumentException(listValidator);
            }
        }
        if (maxLength != null) {
            if (isValueReference(maxLength)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(maxLength);
                _component.setValueBinding("maxLength", _vb);
            } else {
                _component.getAttributes().put("maxLength", Integer.valueOf(maxLength));
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
        if (sorted != null) {
            if (isValueReference(sorted)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(sorted);
                _component.setValueBinding("sorted", _vb);
            } else {
                _component.getAttributes().put("sorted", Boolean.valueOf(sorted));
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
        if (required != null) {
            if (isValueReference(required)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(required);
                _component.setValueBinding("required", _vb);
            } else {
                _component.getAttributes().put("required", Boolean.valueOf(required));
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

    // fieldLabel
    private String fieldLabel = null;
    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    // fieldValidator
    private String fieldValidator = null;
    public void setFieldValidator(String fieldValidator) {
        this.fieldValidator = fieldValidator;
    }

    // labelLevel
    private String labelLevel = null;
    public void setLabelLevel(String labelLevel) {
        this.labelLevel = labelLevel;
    }

    // list
    private String list = null;
    public void setList(String list) {
        this.list = list;
    }

    // listLabel
    private String listLabel = null;
    public void setListLabel(String listLabel) {
        this.listLabel = listLabel;
    }

    // listOnTop
    private String listOnTop = null;
    public void setListOnTop(String listOnTop) {
        this.listOnTop = listOnTop;
    }

    // listValidator
    private String listValidator = null;
    public void setListValidator(String listValidator) {
        this.listValidator = listValidator;
    }

    // maxLength
    private String maxLength = null;
    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
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

    // sorted
    private String sorted = null;
    public void setSorted(String sorted) {
        this.sorted = sorted;
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

    // required
    private String required = null;
    public void setRequired(String required) {
        this.required = required;
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

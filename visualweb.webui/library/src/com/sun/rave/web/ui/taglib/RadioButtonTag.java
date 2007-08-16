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

public class RadioButtonTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.RadioButton";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.RadioButton";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        labelLevel = null;
        imageURL = null;
        name = null;
        selected = null;
        selectedValue = null;
        disabled = null;
        label = null;
        onBlur = null;
        onChange = null;
        onClick = null;
        onDblClick = null;
        onFocus = null;
        onKeyDown = null;
        onKeyPress = null;
        onKeyUp = null;
        onMouseDown = null;
        onMouseMove = null;
        onMouseOut = null;
        onMouseOver = null;
        onMouseUp = null;
        onSelect = null;
        readOnly = null;
        style = null;
        styleClass = null;
        tabIndex = null;
        toolTip = null;
        visible = null;
        converter = null;
        immediate = null;
        validator = null;
        valueChangeListener = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (labelLevel != null) {
            if (isValueReference(labelLevel)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(labelLevel);
                _component.setValueBinding("labelLevel", _vb);
            } else {
                _component.getAttributes().put("labelLevel", Integer.valueOf(labelLevel));
            }
        }
        if (imageURL != null) {
            if (isValueReference(imageURL)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(imageURL);
                _component.setValueBinding("imageURL", _vb);
            } else {
                _component.getAttributes().put("imageURL", imageURL);
            }
        }
        if (name != null) {
            if (isValueReference(name)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(name);
                _component.setValueBinding("name", _vb);
            } else {
                _component.getAttributes().put("name", name);
            }
        }
        if (selected != null) {
            if (isValueReference(selected)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(selected);
                _component.setValueBinding("selected", _vb);
            } else {
                _component.getAttributes().put("selected", selected);
            }
        }
        if (selectedValue != null) {
            if (isValueReference(selectedValue)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(selectedValue);
                _component.setValueBinding("selectedValue", _vb);
            } else {
                _component.getAttributes().put("selectedValue", selectedValue);
            }
        }
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
        if (onBlur != null) {
            if (isValueReference(onBlur)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onBlur);
                _component.setValueBinding("onBlur", _vb);
            } else {
                _component.getAttributes().put("onBlur", onBlur);
            }
        }
        if (onChange != null) {
            if (isValueReference(onChange)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onChange);
                _component.setValueBinding("onChange", _vb);
            } else {
                _component.getAttributes().put("onChange", onChange);
            }
        }
        if (onClick != null) {
            if (isValueReference(onClick)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onClick);
                _component.setValueBinding("onClick", _vb);
            } else {
                _component.getAttributes().put("onClick", onClick);
            }
        }
        if (onDblClick != null) {
            if (isValueReference(onDblClick)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onDblClick);
                _component.setValueBinding("onDblClick", _vb);
            } else {
                _component.getAttributes().put("onDblClick", onDblClick);
            }
        }
        if (onFocus != null) {
            if (isValueReference(onFocus)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onFocus);
                _component.setValueBinding("onFocus", _vb);
            } else {
                _component.getAttributes().put("onFocus", onFocus);
            }
        }
        if (onKeyDown != null) {
            if (isValueReference(onKeyDown)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onKeyDown);
                _component.setValueBinding("onKeyDown", _vb);
            } else {
                _component.getAttributes().put("onKeyDown", onKeyDown);
            }
        }
        if (onKeyPress != null) {
            if (isValueReference(onKeyPress)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onKeyPress);
                _component.setValueBinding("onKeyPress", _vb);
            } else {
                _component.getAttributes().put("onKeyPress", onKeyPress);
            }
        }
        if (onKeyUp != null) {
            if (isValueReference(onKeyUp)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onKeyUp);
                _component.setValueBinding("onKeyUp", _vb);
            } else {
                _component.getAttributes().put("onKeyUp", onKeyUp);
            }
        }
        if (onMouseDown != null) {
            if (isValueReference(onMouseDown)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseDown);
                _component.setValueBinding("onMouseDown", _vb);
            } else {
                _component.getAttributes().put("onMouseDown", onMouseDown);
            }
        }
        if (onMouseMove != null) {
            if (isValueReference(onMouseMove)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseMove);
                _component.setValueBinding("onMouseMove", _vb);
            } else {
                _component.getAttributes().put("onMouseMove", onMouseMove);
            }
        }
        if (onMouseOut != null) {
            if (isValueReference(onMouseOut)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseOut);
                _component.setValueBinding("onMouseOut", _vb);
            } else {
                _component.getAttributes().put("onMouseOut", onMouseOut);
            }
        }
        if (onMouseOver != null) {
            if (isValueReference(onMouseOver)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseOver);
                _component.setValueBinding("onMouseOver", _vb);
            } else {
                _component.getAttributes().put("onMouseOver", onMouseOver);
            }
        }
        if (onMouseUp != null) {
            if (isValueReference(onMouseUp)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseUp);
                _component.setValueBinding("onMouseUp", _vb);
            } else {
                _component.getAttributes().put("onMouseUp", onMouseUp);
            }
        }
        if (onSelect != null) {
            if (isValueReference(onSelect)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onSelect);
                _component.setValueBinding("onSelect", _vb);
            } else {
                _component.getAttributes().put("onSelect", onSelect);
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

    // labelLevel
    private String labelLevel = null;
    public void setLabelLevel(String labelLevel) {
        this.labelLevel = labelLevel;
    }

    // imageURL
    private String imageURL = null;
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // name
    private String name = null;
    public void setName(String name) {
        this.name = name;
    }

    // selected
    private String selected = null;
    public void setSelected(String selected) {
        this.selected = selected;
    }

    // selectedValue
    private String selectedValue = null;
    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
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

    // onBlur
    private String onBlur = null;
    public void setOnBlur(String onBlur) {
        this.onBlur = onBlur;
    }

    // onChange
    private String onChange = null;
    public void setOnChange(String onChange) {
        this.onChange = onChange;
    }

    // onClick
    private String onClick = null;
    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    // onDblClick
    private String onDblClick = null;
    public void setOnDblClick(String onDblClick) {
        this.onDblClick = onDblClick;
    }

    // onFocus
    private String onFocus = null;
    public void setOnFocus(String onFocus) {
        this.onFocus = onFocus;
    }

    // onKeyDown
    private String onKeyDown = null;
    public void setOnKeyDown(String onKeyDown) {
        this.onKeyDown = onKeyDown;
    }

    // onKeyPress
    private String onKeyPress = null;
    public void setOnKeyPress(String onKeyPress) {
        this.onKeyPress = onKeyPress;
    }

    // onKeyUp
    private String onKeyUp = null;
    public void setOnKeyUp(String onKeyUp) {
        this.onKeyUp = onKeyUp;
    }

    // onMouseDown
    private String onMouseDown = null;
    public void setOnMouseDown(String onMouseDown) {
        this.onMouseDown = onMouseDown;
    }

    // onMouseMove
    private String onMouseMove = null;
    public void setOnMouseMove(String onMouseMove) {
        this.onMouseMove = onMouseMove;
    }

    // onMouseOut
    private String onMouseOut = null;
    public void setOnMouseOut(String onMouseOut) {
        this.onMouseOut = onMouseOut;
    }

    // onMouseOver
    private String onMouseOver = null;
    public void setOnMouseOver(String onMouseOver) {
        this.onMouseOver = onMouseOver;
    }

    // onMouseUp
    private String onMouseUp = null;
    public void setOnMouseUp(String onMouseUp) {
        this.onMouseUp = onMouseUp;
    }

    // onSelect
    private String onSelect = null;
    public void setOnSelect(String onSelect) {
        this.onSelect = onSelect;
    }

    // readOnly
    private String readOnly = null;
    public void setReadOnly(String readOnly) {
        this.readOnly = readOnly;
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

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

public class ButtonTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.Button";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.Button";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        action = null;
        actionListener = null;
        alt = null;
        disabled = null;
        escape = null;
        imageURL = null;
        mini = null;
        noTextPadding = null;
        onBlur = null;
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
        primary = null;
        reset = null;
        style = null;
        styleClass = null;
        tabIndex = null;
        text = null;
        toolTip = null;
        visible = null;
        immediate = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (action != null) {
            if (isValueReference(action)) {
                MethodBinding _mb = getFacesContext().getApplication().createMethodBinding(action, actionArgs);
                _component.getAttributes().put("action", _mb);
            } else {
                MethodBinding _mb = new ConstantMethodBinding(action);
                _component.getAttributes().put("action", _mb);
            }
        }
        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                MethodBinding _mb = getFacesContext().getApplication().createMethodBinding(actionListener, actionListenerArgs);
                _component.getAttributes().put("actionListener", _mb);
            } else {
                throw new IllegalArgumentException(actionListener);
            }
        }
        if (alt != null) {
            if (isValueReference(alt)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(alt);
                _component.setValueBinding("alt", _vb);
            } else {
                _component.getAttributes().put("alt", alt);
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
        if (escape != null) {
            if (isValueReference(escape)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(escape);
                _component.setValueBinding("escape", _vb);
            } else {
                _component.getAttributes().put("escape", Boolean.valueOf(escape));
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
        if (mini != null) {
            if (isValueReference(mini)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(mini);
                _component.setValueBinding("mini", _vb);
            } else {
                _component.getAttributes().put("mini", Boolean.valueOf(mini));
            }
        }
        if (noTextPadding != null) {
            if (isValueReference(noTextPadding)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(noTextPadding);
                _component.setValueBinding("noTextPadding", _vb);
            } else {
                _component.getAttributes().put("noTextPadding", Boolean.valueOf(noTextPadding));
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
        if (primary != null) {
            if (isValueReference(primary)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(primary);
                _component.setValueBinding("primary", _vb);
            } else {
                _component.getAttributes().put("primary", Boolean.valueOf(primary));
            }
        }
        if (reset != null) {
            if (isValueReference(reset)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(reset);
                _component.setValueBinding("reset", _vb);
            } else {
                _component.getAttributes().put("reset", Boolean.valueOf(reset));
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
        if (text != null) {
            if (isValueReference(text)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(text);
                _component.setValueBinding("text", _vb);
            } else {
                _component.getAttributes().put("text", text);
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
        if (immediate != null) {
            if (isValueReference(immediate)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(immediate);
                _component.setValueBinding("immediate", _vb);
            } else {
                _component.getAttributes().put("immediate", Boolean.valueOf(immediate));
            }
        }
    }

    // action
    private String action = null;
    public void setAction(String action) {
        this.action = action;
    }

    // actionListener
    private String actionListener = null;
    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }

    // alt
    private String alt = null;
    public void setAlt(String alt) {
        this.alt = alt;
    }

    // disabled
    private String disabled = null;
    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

    // escape
    private String escape = null;
    public void setEscape(String escape) {
        this.escape = escape;
    }

    // imageURL
    private String imageURL = null;
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // mini
    private String mini = null;
    public void setMini(String mini) {
        this.mini = mini;
    }

    // noTextPadding
    private String noTextPadding = null;
    public void setNoTextPadding(String noTextPadding) {
        this.noTextPadding = noTextPadding;
    }

    // onBlur
    private String onBlur = null;
    public void setOnBlur(String onBlur) {
        this.onBlur = onBlur;
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

    // primary
    private String primary = null;
    public void setPrimary(String primary) {
        this.primary = primary;
    }

    // reset
    private String reset = null;
    public void setReset(String reset) {
        this.reset = reset;
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

    // text
    private String text = null;
    public void setText(String text) {
        this.text = text;
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

    // immediate
    private String immediate = null;
    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

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

public class ImageComponentTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.Image";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.Image";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        align = null;
        alt = null;
        border = null;
        height = null;
        hspace = null;
        icon = null;
        longDesc = null;
        onClick = null;
        onDblClick = null;
        onMouseDown = null;
        onMouseMove = null;
        onMouseOut = null;
        onMouseOver = null;
        onMouseUp = null;
        style = null;
        styleClass = null;
        toolTip = null;
        url = null;
        visible = null;
        vspace = null;
        width = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (align != null) {
            if (isValueReference(align)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(align);
                _component.setValueBinding("align", _vb);
            } else {
                _component.getAttributes().put("align", align);
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
        if (border != null) {
            if (isValueReference(border)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(border);
                _component.setValueBinding("border", _vb);
            } else {
                _component.getAttributes().put("border", Integer.valueOf(border));
            }
        }
        if (height != null) {
            if (isValueReference(height)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(height);
                _component.setValueBinding("height", _vb);
            } else {
                _component.getAttributes().put("height", Integer.valueOf(height));
            }
        }
        if (hspace != null) {
            if (isValueReference(hspace)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(hspace);
                _component.setValueBinding("hspace", _vb);
            } else {
                _component.getAttributes().put("hspace", Integer.valueOf(hspace));
            }
        }
        if (icon != null) {
            if (isValueReference(icon)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(icon);
                _component.setValueBinding("icon", _vb);
            } else {
                _component.getAttributes().put("icon", icon);
            }
        }
        if (longDesc != null) {
            if (isValueReference(longDesc)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(longDesc);
                _component.setValueBinding("longDesc", _vb);
            } else {
                _component.getAttributes().put("longDesc", longDesc);
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
        if (toolTip != null) {
            if (isValueReference(toolTip)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(toolTip);
                _component.setValueBinding("toolTip", _vb);
            } else {
                _component.getAttributes().put("toolTip", toolTip);
            }
        }
        if (url != null) {
            if (isValueReference(url)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(url);
                _component.setValueBinding("url", _vb);
            } else {
                _component.getAttributes().put("url", url);
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
        if (vspace != null) {
            if (isValueReference(vspace)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(vspace);
                _component.setValueBinding("vspace", _vb);
            } else {
                _component.getAttributes().put("vspace", Integer.valueOf(vspace));
            }
        }
        if (width != null) {
            if (isValueReference(width)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(width);
                _component.setValueBinding("width", _vb);
            } else {
                _component.getAttributes().put("width", Integer.valueOf(width));
            }
        }
    }

    // align
    private String align = null;
    public void setAlign(String align) {
        this.align = align;
    }

    // alt
    private String alt = null;
    public void setAlt(String alt) {
        this.alt = alt;
    }

    // border
    private String border = null;
    public void setBorder(String border) {
        this.border = border;
    }

    // height
    private String height = null;
    public void setHeight(String height) {
        this.height = height;
    }

    // hspace
    private String hspace = null;
    public void setHspace(String hspace) {
        this.hspace = hspace;
    }

    // icon
    private String icon = null;
    public void setIcon(String icon) {
        this.icon = icon;
    }

    // longDesc
    private String longDesc = null;
    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
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

    // toolTip
    private String toolTip = null;
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    // url
    private String url = null;
    public void setUrl(String url) {
        this.url = url;
    }

    // visible
    private String visible = null;
    public void setVisible(String visible) {
        this.visible = visible;
    }

    // vspace
    private String vspace = null;
    public void setVspace(String vspace) {
        this.vspace = vspace;
    }

    // width
    private String width = null;
    public void setWidth(String width) {
        this.width = width;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

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

public class FrameSetTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.FrameSet";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.FrameSet";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        border = null;
        borderColor = null;
        cols = null;
        frameBorder = null;
        frameSpacing = null;
        rows = null;
        style = null;
        styleClass = null;
        toolTip = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (border != null) {
            if (isValueReference(border)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(border);
                _component.setValueBinding("border", _vb);
            } else {
                _component.getAttributes().put("border", Integer.valueOf(border));
            }
        }
        if (borderColor != null) {
            if (isValueReference(borderColor)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(borderColor);
                _component.setValueBinding("borderColor", _vb);
            } else {
                _component.getAttributes().put("borderColor", borderColor);
            }
        }
        if (cols != null) {
            if (isValueReference(cols)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(cols);
                _component.setValueBinding("cols", _vb);
            } else {
                _component.getAttributes().put("cols", cols);
            }
        }
        if (frameBorder != null) {
            if (isValueReference(frameBorder)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(frameBorder);
                _component.setValueBinding("frameBorder", _vb);
            } else {
                _component.getAttributes().put("frameBorder", Boolean.valueOf(frameBorder));
            }
        }
        if (frameSpacing != null) {
            if (isValueReference(frameSpacing)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(frameSpacing);
                _component.setValueBinding("frameSpacing", _vb);
            } else {
                _component.getAttributes().put("frameSpacing", Integer.valueOf(frameSpacing));
            }
        }
        if (rows != null) {
            if (isValueReference(rows)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(rows);
                _component.setValueBinding("rows", _vb);
            } else {
                _component.getAttributes().put("rows", rows);
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
    }

    // border
    private String border = null;
    public void setBorder(String border) {
        this.border = border;
    }

    // borderColor
    private String borderColor = null;
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    // cols
    private String cols = null;
    public void setCols(String cols) {
        this.cols = cols;
    }

    // frameBorder
    private String frameBorder = null;
    public void setFrameBorder(String frameBorder) {
        this.frameBorder = frameBorder;
    }

    // frameSpacing
    private String frameSpacing = null;
    public void setFrameSpacing(String frameSpacing) {
        this.frameSpacing = frameSpacing;
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

    // toolTip
    private String toolTip = null;
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

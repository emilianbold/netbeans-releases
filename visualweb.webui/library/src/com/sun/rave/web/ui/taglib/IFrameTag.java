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

public class IFrameTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.IFrame";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.IFrame";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        align = null;
        height = null;
        width = null;
        frameBorder = null;
        longDesc = null;
        marginHeight = null;
        marginWidth = null;
        name = null;
        scrolling = null;
        style = null;
        styleClass = null;
        toolTip = null;
        url = null;
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
        if (height != null) {
            if (isValueReference(height)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(height);
                _component.setValueBinding("height", _vb);
            } else {
                _component.getAttributes().put("height", height);
            }
        }
        if (width != null) {
            if (isValueReference(width)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(width);
                _component.setValueBinding("width", _vb);
            } else {
                _component.getAttributes().put("width", width);
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
        if (longDesc != null) {
            if (isValueReference(longDesc)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(longDesc);
                _component.setValueBinding("longDesc", _vb);
            } else {
                _component.getAttributes().put("longDesc", longDesc);
            }
        }
        if (marginHeight != null) {
            if (isValueReference(marginHeight)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(marginHeight);
                _component.setValueBinding("marginHeight", _vb);
            } else {
                _component.getAttributes().put("marginHeight", Integer.valueOf(marginHeight));
            }
        }
        if (marginWidth != null) {
            if (isValueReference(marginWidth)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(marginWidth);
                _component.setValueBinding("marginWidth", _vb);
            } else {
                _component.getAttributes().put("marginWidth", Integer.valueOf(marginWidth));
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
        if (scrolling != null) {
            if (isValueReference(scrolling)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(scrolling);
                _component.setValueBinding("scrolling", _vb);
            } else {
                _component.getAttributes().put("scrolling", scrolling);
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
    }

    // align
    private String align = null;
    public void setAlign(String align) {
        this.align = align;
    }

    // height
    private String height = null;
    public void setHeight(String height) {
        this.height = height;
    }

    // width
    private String width = null;
    public void setWidth(String width) {
        this.width = width;
    }

    // frameBorder
    private String frameBorder = null;
    public void setFrameBorder(String frameBorder) {
        this.frameBorder = frameBorder;
    }

    // longDesc
    private String longDesc = null;
    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    // marginHeight
    private String marginHeight = null;
    public void setMarginHeight(String marginHeight) {
        this.marginHeight = marginHeight;
    }

    // marginWidth
    private String marginWidth = null;
    public void setMarginWidth(String marginWidth) {
        this.marginWidth = marginWidth;
    }

    // name
    private String name = null;
    public void setName(String name) {
        this.name = name;
    }

    // scrolling
    private String scrolling = null;
    public void setScrolling(String scrolling) {
        this.scrolling = scrolling;
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

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

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

public class AlertTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.Alert";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.Alert";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        alt = null;
        detail = null;
        linkAction = null;
        linkTarget = null;
        linkText = null;
        linkToolTip = null;
        linkURL = null;
        style = null;
        styleClass = null;
        summary = null;
        tabIndex = null;
        type = null;
        visible = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (alt != null) {
            if (isValueReference(alt)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(alt);
                _component.setValueBinding("alt", _vb);
            } else {
                _component.getAttributes().put("alt", alt);
            }
        }
        if (detail != null) {
            if (isValueReference(detail)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(detail);
                _component.setValueBinding("detail", _vb);
            } else {
                _component.getAttributes().put("detail", detail);
            }
        }
        if (linkAction != null) {
            if (isValueReference(linkAction)) {
                MethodBinding _mb = getFacesContext().getApplication().createMethodBinding(linkAction, actionArgs);
                _component.getAttributes().put("linkAction", _mb);
            } else {
                MethodBinding _mb = new ConstantMethodBinding(linkAction);
                _component.getAttributes().put("linkAction", _mb);
            }
        }
        if (linkTarget != null) {
            if (isValueReference(linkTarget)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(linkTarget);
                _component.setValueBinding("linkTarget", _vb);
            } else {
                _component.getAttributes().put("linkTarget", linkTarget);
            }
        }
        if (linkText != null) {
            if (isValueReference(linkText)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(linkText);
                _component.setValueBinding("linkText", _vb);
            } else {
                _component.getAttributes().put("linkText", linkText);
            }
        }
        if (linkToolTip != null) {
            if (isValueReference(linkToolTip)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(linkToolTip);
                _component.setValueBinding("linkToolTip", _vb);
            } else {
                _component.getAttributes().put("linkToolTip", linkToolTip);
            }
        }
        if (linkURL != null) {
            if (isValueReference(linkURL)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(linkURL);
                _component.setValueBinding("linkURL", _vb);
            } else {
                _component.getAttributes().put("linkURL", linkURL);
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
        if (summary != null) {
            if (isValueReference(summary)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(summary);
                _component.setValueBinding("summary", _vb);
            } else {
                _component.getAttributes().put("summary", summary);
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
        if (type != null) {
            if (isValueReference(type)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(type);
                _component.setValueBinding("type", _vb);
            } else {
                _component.getAttributes().put("type", type);
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

    // alt
    private String alt = null;
    public void setAlt(String alt) {
        this.alt = alt;
    }

    // detail
    private String detail = null;
    public void setDetail(String detail) {
        this.detail = detail;
    }

    // linkAction
    private String linkAction = null;
    public void setLinkAction(String linkAction) {
        this.linkAction = linkAction;
    }

    // linkTarget
    private String linkTarget = null;
    public void setLinkTarget(String linkTarget) {
        this.linkTarget = linkTarget;
    }

    // linkText
    private String linkText = null;
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    // linkToolTip
    private String linkToolTip = null;
    public void setLinkToolTip(String linkToolTip) {
        this.linkToolTip = linkToolTip;
    }

    // linkURL
    private String linkURL = null;
    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
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

    // summary
    private String summary = null;
    public void setSummary(String summary) {
        this.summary = summary;
    }

    // tabIndex
    private String tabIndex = null;
    public void setTabIndex(String tabIndex) {
        this.tabIndex = tabIndex;
    }

    // type
    private String type = null;
    public void setType(String type) {
        this.type = type;
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

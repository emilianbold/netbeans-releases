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

public class MetaTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.Meta";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.Meta";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        content = null;
        httpEquiv = null;
        name = null;
        scheme = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (content != null) {
            if (isValueReference(content)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(content);
                _component.setValueBinding("content", _vb);
            } else {
                _component.getAttributes().put("content", content);
            }
        }
        if (httpEquiv != null) {
            if (isValueReference(httpEquiv)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(httpEquiv);
                _component.setValueBinding("httpEquiv", _vb);
            } else {
                _component.getAttributes().put("httpEquiv", httpEquiv);
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
        if (scheme != null) {
            if (isValueReference(scheme)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(scheme);
                _component.setValueBinding("scheme", _vb);
            } else {
                _component.getAttributes().put("scheme", scheme);
            }
        }
    }

    // content
    private String content = null;
    public void setContent(String content) {
        this.content = content;
    }

    // httpEquiv
    private String httpEquiv = null;
    public void setHttpEquiv(String httpEquiv) {
        this.httpEquiv = httpEquiv;
    }

    // name
    private String name = null;
    public void setName(String name) {
        this.name = name;
    }

    // scheme
    private String scheme = null;
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

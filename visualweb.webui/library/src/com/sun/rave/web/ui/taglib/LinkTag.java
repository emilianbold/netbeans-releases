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

public class LinkTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.Link";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.Link";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        charset = null;
        media = null;
        rel = null;
        type = null;
        url = null;
        urlLang = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (charset != null) {
            if (isValueReference(charset)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(charset);
                _component.setValueBinding("charset", _vb);
            } else {
                _component.getAttributes().put("charset", charset);
            }
        }
        if (media != null) {
            if (isValueReference(media)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(media);
                _component.setValueBinding("media", _vb);
            } else {
                _component.getAttributes().put("media", media);
            }
        }
        if (rel != null) {
            if (isValueReference(rel)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(rel);
                _component.setValueBinding("rel", _vb);
            } else {
                _component.getAttributes().put("rel", rel);
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
        if (url != null) {
            if (isValueReference(url)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(url);
                _component.setValueBinding("url", _vb);
            } else {
                _component.getAttributes().put("url", url);
            }
        }
        if (urlLang != null) {
            if (isValueReference(urlLang)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(urlLang);
                _component.setValueBinding("urlLang", _vb);
            } else {
                _component.getAttributes().put("urlLang", urlLang);
            }
        }
    }

    // charset
    private String charset = null;
    public void setCharset(String charset) {
        this.charset = charset;
    }

    // media
    private String media = null;
    public void setMedia(String media) {
        this.media = media;
    }

    // rel
    private String rel = null;
    public void setRel(String rel) {
        this.rel = rel;
    }

    // type
    private String type = null;
    public void setType(String type) {
        this.type = type;
    }

    // url
    private String url = null;
    public void setUrl(String url) {
        this.url = url;
    }

    // urlLang
    private String urlLang = null;
    public void setUrlLang(String urlLang) {
        this.urlLang = urlLang;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

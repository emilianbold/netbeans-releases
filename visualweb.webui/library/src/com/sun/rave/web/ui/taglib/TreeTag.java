/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

public class TreeTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.Tree";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.Tree";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        clientSide = null;
        expandOnSelect = null;
        immediate = null;
        required = null;
        selected = null;
        style = null;
        styleClass = null;
        text = null;
        visible = null;
        action = null;
        actionListener = null;
        imageURL = null;
        target = null;
        toolTip = null;
        url = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (clientSide != null) {
            if (isValueReference(clientSide)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(clientSide);
                _component.setValueBinding("clientSide", _vb);
            } else {
                _component.getAttributes().put("clientSide", Boolean.valueOf(clientSide));
            }
        }
        if (expandOnSelect != null) {
            if (isValueReference(expandOnSelect)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(expandOnSelect);
                _component.setValueBinding("expandOnSelect", _vb);
            } else {
                _component.getAttributes().put("expandOnSelect", Boolean.valueOf(expandOnSelect));
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
        if (selected != null) {
            if (isValueReference(selected)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(selected);
                _component.setValueBinding("selected", _vb);
            } else {
                _component.getAttributes().put("selected", selected);
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
        if (text != null) {
            if (isValueReference(text)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(text);
                _component.setValueBinding("text", _vb);
            } else {
                _component.getAttributes().put("text", text);
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
        if (imageURL != null) {
            if (isValueReference(imageURL)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(imageURL);
                _component.setValueBinding("imageURL", _vb);
            } else {
                _component.getAttributes().put("imageURL", imageURL);
            }
        }
        if (target != null) {
            if (isValueReference(target)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(target);
                _component.setValueBinding("target", _vb);
            } else {
                _component.getAttributes().put("target", target);
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

    // clientSide
    private String clientSide = null;
    public void setClientSide(String clientSide) {
        this.clientSide = clientSide;
    }

    // expandOnSelect
    private String expandOnSelect = null;
    public void setExpandOnSelect(String expandOnSelect) {
        this.expandOnSelect = expandOnSelect;
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

    // selected
    private String selected = null;
    public void setSelected(String selected) {
        this.selected = selected;
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

    // text
    private String text = null;
    public void setText(String text) {
        this.text = text;
    }

    // visible
    private String visible = null;
    public void setVisible(String visible) {
        this.visible = visible;
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

    // imageURL
    private String imageURL = null;
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // target
    private String target = null;
    public void setTarget(String target) {
        this.target = target;
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

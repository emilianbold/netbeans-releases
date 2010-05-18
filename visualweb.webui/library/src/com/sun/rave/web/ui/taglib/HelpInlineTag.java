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

public class HelpInlineTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.HelpInline";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.HelpInline";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        style = null;
        styleClass = null;
        text = null;
        type = null;
        visible = null;
        converter = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
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

    // converter
    private String converter = null;
    public void setConverter(String converter) {
        this.converter = converter;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}

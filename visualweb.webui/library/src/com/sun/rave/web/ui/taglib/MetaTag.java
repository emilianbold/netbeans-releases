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

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

public class FrameTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.Frame";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.Frame";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        frameBorder = null;
        longDesc = null;
        marginHeight = null;
        marginWidth = null;
        name = null;
        noResize = null;
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
        if (noResize != null) {
            if (isValueReference(noResize)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(noResize);
                _component.setValueBinding("noResize", _vb);
            } else {
                _component.getAttributes().put("noResize", Boolean.valueOf(noResize));
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

    // noResize
    private String noResize = null;
    public void setNoResize(String noResize) {
        this.noResize = noResize;
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

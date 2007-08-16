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
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <span style="color: rgb(0, 0, 0);"><span
 *  style="text-decoration: line-through;"></span>Use
 * the <code>ui:pageSeparator</code>
 * tag to create a horizontal line that separates items on the
 * page.&nbsp; The
 * tag can be used as a standalone tag to
 * insert a new horizontal line in a page,
 * or used within the facet of another tag to override a default page
 * separator.&nbsp; For example, the <code>ui:pageSeparator</code>
 * tag
 * can be used in the <a
 *  href="http://smpt.east/%7Esmorgan/lockhart/tlddoc/ui/pageAlert.html"><code>ui:pageAlert</code></a>
 * tag's <code>pageAlertSeparator</code>
 * facet. </span><br
 *  style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);"><br>
 * </span>
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout</h3>
 * <span style="color: rgb(0, 0, 0);">This
 * tag renders a horizontal
 * line inside</span><span
 *  style="text-decoration: line-through; font-weight: bold; color: rgb(0, 0, 0);"></span><span
 *  style="color: rgb(0, 0, 0);"> an
 * HTML table that will
 * size according to the size of the page.</span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers</h3>
 * <span style="color: rgb(0, 0, 0);">TBD</span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Client
 * Side Javascript Functions</h3>
 * <span style="color: rgb(0, 0, 0);">None.
 * </span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Example</h3>
 * <span style="color: rgb(0, 0, 0);"></span><b
 *  style="color: rgb(0, 0, 0);">Example
 * 1: Using <span
 *  style="text-decoration: line-through;"></span>
 * a standalone
 * ui:pageSeparator tag<span
 *  style="text-decoration: line-through;"></span><br>
 * <br>
 * </b><code
 *  style="color: rgb(0, 0, 0);">&lt;?xml
 * version="1.0"
 * encoding="UTF-8"?&gt;<br>
 * &lt;jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core"
 * xmlns:h="http://java.sun.com/jsf/html"
 * xmlns:jsp="http://java.sun.com/JSP/Page"
 * xmlns:ui="http://www.sun.com/web/ui"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;jsp:directive.page
 * contentType="text/html;charset=ISO-8859-1"
 * pageEncoding="UTF-8"/&gt;&lt;f:view&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:page frame="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:html&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:head title="blah" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;
 * &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp;
 * &nbsp;&nbsp; &lt;ui:pageSeparator id="mypageseparator"
 * /&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;
 * &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp;
 * &lt;/ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;/ui:html&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;/ui:page&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:view&gt;<br>
 * &lt;/jsp:root&gt;<br>
 * <br>
 * <br>
 * </code><b
 *  style="color: rgb(0, 0, 0);"></b>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class PageSeparatorBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>PageSeparatorBase</code>.</p>
     */
    public PageSeparatorBase() {
        super();
        setRendererType("com.sun.rave.web.ui.PageSeparator");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.PageSeparator";
    }

    // style
    private String style = null;

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueBinding _vb = getValueBinding("style");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     * @see #getStyle()
     */
    public void setStyle(String style) {
        this.style = style;
    }

    // styleClass
    private String styleClass = null;

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueBinding _vb = getValueBinding("styleClass");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     * @see #getStyleClass()
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    // value
    private Object value = null;

    public Object getValue() {
        if (this.value != null) {
            return this.value;
        }
        ValueBinding _vb = getValueBinding("value");
        if (_vb != null) {
            return (Object) _vb.getValue(getFacesContext());
        }
        return null;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    // visible
    private boolean visible = false;
    private boolean visible_set = false;

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     */
    public boolean isVisible() {
        if (this.visible_set) {
            return this.visible;
        }
        ValueBinding _vb = getValueBinding("visible");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return true;
    }

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     * @see #isVisible()
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        this.visible_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.style = (String) _values[1];
        this.styleClass = (String) _values[2];
        this.value = (Object) _values[3];
        this.visible = ((Boolean) _values[4]).booleanValue();
        this.visible_set = ((Boolean) _values[5]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[6];
        _values[0] = super.saveState(_context);
        _values[1] = this.style;
        _values[2] = this.styleClass;
        _values[3] = this.value;
        _values[4] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}

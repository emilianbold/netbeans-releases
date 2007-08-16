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
 * <p
 *  style="color: rgb(0, 0, 0);"><span
 *  style="text-decoration: line-through;"></span>Use
 * the <code>ui:markup </code>tag
 * to insert any type of XML markup
 * in the rendered HTML page. The <code>ui:markup</code>
 * tag allows you to insert HTML
 * elements into the JSP page where HTML is not permitted inside a JSF
 * tag.&nbsp; <br>
 * </p>
 * <h3 style="color: rgb(0, 0, 0);">Configuring
 * the markup tag <br>
 * </h3>
 * <p style="color: rgb(0, 0, 0);">Use
 * the <code>tag</code>
 * attribute to specify the type of
 * HTML element to insert.&nbsp; For instance, to insert a <code>&lt;p&gt;</code>
 * tag, set the attribute to <code>tag="p"</code>
 * in the <code>ui:markup</code>
 * tag.&nbsp; Note that you do not
 * include the angle brackets.</p>
 * <p style="color: rgb(0, 0, 0);">If
 * the HTML element you are
 * inserting is a singleton element, you must specify the <code>singleton</code>
 * attribute.&nbsp; The singleton attribute causes the trailing<code>
 * /&gt;</code>
 * to be generated in the rendered HTML. For example, the&nbsp; <code>&lt;br&gt;</code>
 * element is a singleton element, which must be rendered as <code>&lt;br
 * /&gt; </code>to be XHTML
 * compliant.</p>
 * <p style="color: rgb(0, 0, 0);">If
 * you want to specify
 * additional HTML attributes for the element you are inserting, use the <code>extraAttributes</code>
 * attribute. <br>
 * </p>
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout</h3>
 * <span style="color: rgb(0, 0, 0);">The
 * rendered HTML page includes
 * the HTML element that was specified in the </span><code
 *  style="color: rgb(0, 0, 0);">ui:markup</code><span
 *  style="color: rgb(0, 0, 0);"> tag's </span><code
 *  style="color: rgb(0, 0, 0);">tag</code><span
 *  style="color: rgb(0, 0, 0);"> attribute, along with
 * any HTML
 * attributes that were included in the <code>extraAttributes</code>
 * attribute.<br>
 * </span>
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers<br>
 * </h3>
 * <p style="color: rgb(0, 0, 0);">None.</p>
 * <h3 style="color: rgb(0, 0, 0);">Client
 * Side Javascript
 * Functions&nbsp;</h3>
 * <span style="color: rgb(0, 0, 0);">None.</span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Examples</h3>
 * <span style="color: rgb(0, 0, 0);"></span>
 * <h4 style="color: rgb(0, 0, 0);">Example
 * 1:&nbsp; Insert a
 * singleton element <br>
 * </h4>
 * <pre style="color: rgb(0, 0, 0);"><code> &lt;ui:markup tag="br" singleton="true" /&gt;</code><code><br></code></pre>
 * <p style="color: rgb(0, 0, 0);">This
 * generates <code>&lt;br
 * /&gt;</code>.</p>
 * <code style="color: rgb(0, 0, 0);"></code>
 * <h4 style="color: rgb(0, 0, 0);">Example
 * 2: Insert a <code>&lt;div&gt;</code>
 * element with a style attribute <br>
 * </h4>
 * <code style="color: rgb(0, 0, 0);"></code><code
 *  style="color: rgb(0, 0, 0);">&nbsp;&nbsp;&nbsp;
 * &lt;ui:markup
 * tag="div"
 * style="color:blue" /&gt; <br>
 * </code>
 * <p style="color: rgb(0, 0, 0);">This
 * generates <code>&lt;div
 * style="color:blue" &gt;</code></p>
 * <code style="color: rgb(0, 0, 0);"></code>
 * <h4 style="color: rgb(0, 0, 0);">Example
 * 3: Insert a <code>&lt;h3&gt;</code>
 * with HTML attributes<br>
 * </h4>
 * <pre style="color: rgb(0, 0, 0);"><code>&nbsp; &nbsp; </code><code>&lt;ui:markup tag="h3" extraAttributes="onclick='alert(&amp;quot;foobar&amp;quot;);'" &gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText text="Example" /&gt;<br>&nbsp;&nbsp;&nbsp; &lt;/ui:markup&gt;</code></pre>
 * <code style="color: rgb(0, 0, 0);"></code><span
 *  style="color: rgb(0, 0, 0);">This generates a
 * level 3 head titled Example that
 * will display the alert with "foobar" in it when clicked.</span>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class MarkupBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>MarkupBase</code>.</p>
     */
    public MarkupBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Markup");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Markup";
    }

    // extraAttributes
    private String extraAttributes = null;

    /**
 * <p>Add the rest of the attribute name="value" type pairs inside this 
 *         attribute.  The inserted attributes will need to be escaped.</p>
     */
    public String getExtraAttributes() {
        if (this.extraAttributes != null) {
            return this.extraAttributes;
        }
        ValueBinding _vb = getValueBinding("extraAttributes");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Add the rest of the attribute name="value" type pairs inside this 
 *         attribute.  The inserted attributes will need to be escaped.</p>
     * @see #getExtraAttributes()
     */
    public void setExtraAttributes(String extraAttributes) {
        this.extraAttributes = extraAttributes;
    }

    // singleton
    private boolean singleton = false;
    private boolean singleton_set = false;

    /**
 * <p>Flag indicating that tag is a singleton tag and that it should end with
 *         a trailing /</p>
     */
    public boolean isSingleton() {
        if (this.singleton_set) {
            return this.singleton;
        }
        ValueBinding _vb = getValueBinding("singleton");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Flag indicating that tag is a singleton tag and that it should end with
 *         a trailing /</p>
     * @see #isSingleton()
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
        this.singleton_set = true;
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

    // tag
    private String tag = null;

    /**
 * <p>Name of the HTML element to render.</p>
     */
    public String getTag() {
        if (this.tag != null) {
            return this.tag;
        }
        ValueBinding _vb = getValueBinding("tag");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Name of the HTML element to render.</p>
     * @see #getTag()
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.extraAttributes = (String) _values[1];
        this.singleton = ((Boolean) _values[2]).booleanValue();
        this.singleton_set = ((Boolean) _values[3]).booleanValue();
        this.style = (String) _values[4];
        this.styleClass = (String) _values[5];
        this.tag = (String) _values[6];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[7];
        _values[0] = super.saveState(_context);
        _values[1] = this.extraAttributes;
        _values[2] = this.singleton ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.singleton_set ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.style;
        _values[5] = this.styleClass;
        _values[6] = this.tag;
        return _values;
    }

}

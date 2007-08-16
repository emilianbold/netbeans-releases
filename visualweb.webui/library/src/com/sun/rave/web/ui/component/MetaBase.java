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
 * <span style="text-decoration: line-through;"></span><span
 *  style="color: rgb(0, 0, 0);">Use the </span><code
 *  style="color: rgb(0, 0, 0);">ui:meta</code><span
 *  style="color: rgb(0, 0, 0);"> tag to create an
 * HTML </span><code
 *  style="color: rgb(0, 0, 0);">&lt;meta&gt;</code><span
 *  style="color: rgb(0, 0, 0);"> element in the
 * rendered HTML page.
 * The </span><code
 *  style="color: rgb(0, 0, 0);">&lt;meta&gt;</code><span
 *  style="color: rgb(0, 0, 0);">
 * element
 * provides meta-information about your page, such as descriptions and
 * keywords for search engines and refresh rates.&nbsp; The </span><code
 *  style="color: rgb(0, 0, 0);">ui:meta</code><span
 *  style="color: rgb(0, 0, 0);">
 * tag must
 * be inside a </span><code
 *  style="color: rgb(0, 0, 0);"><a
 *  href="http://smpt.east/%7Esmorgan/lockhart/tlddoc/ui/head.html">ui:head</a></code><span
 *  style="color: rgb(0, 0, 0);">
 * tag.
 * </span><br
 *  style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);">&nbsp;</span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout
 * </h3>
 * <span
 *  style="text-decoration: line-through; color: rgb(0, 0, 0);"></span><span
 *  style="color: rgb(0, 0, 0);">The rendered HTML
 * page
 * contains an
 * HTML </span><code
 *  style="color: rgb(0, 0, 0);">&lt;meta&gt;</code><span
 *  style="color: rgb(0, 0, 0);"> tag and its
 * associated attributes.</span>
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers</h3>
 * <span style="color: rgb(0, 0, 0);">None.</span><br>
 * <h3>Client Side Javascript
 * Functions</h3>
 * None. <span style="color: rgb(255, 153, 0);"></span>
 * <h3>Example</h3>
 * <b>Example 1: Create a Meta tag<br>
 * <br>
 * <span style="color: rgb(255, 153, 0);"></span></b>
 * <div style="margin-left: 40px;"><code>....<br>
 * &lt;ui:head title="meta example" &gt;<br>
 * </code><code>&nbsp;&nbsp;&nbsp;
 * &lt;ui:meta httpEquiv="refresh"
 * content="5" /&gt;
 * <br>
 * &lt;/ui:head&gt;<br>
 * ....</code><br>
 * </div>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class MetaBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>MetaBase</code>.</p>
     */
    public MetaBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Meta");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Meta";
    }

    // content
    private String content = null;

    /**
 * <p>The content attribute is used to specify the data to  associate with a 
 *         name attribute or httpEquiv attribute in the ui:meta tag.</p>
     */
    public String getContent() {
        if (this.content != null) {
            return this.content;
        }
        ValueBinding _vb = getValueBinding("content");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The content attribute is used to specify the data to  associate with a 
 *         name attribute or httpEquiv attribute in the ui:meta tag.</p>
     * @see #getContent()
     */
    public void setContent(String content) {
        this.content = content;
    }

    // httpEquiv
    private String httpEquiv = null;

    /**
 * <p>The httpEquiv attribute is used to specify a value for the http-equiv 
 *         attribute of an HTML Meta element. The http-equiv attribute specifies 
 *         HTTP properties that the web server can use in the HTTP response header.</p>
     */
    public String getHttpEquiv() {
        if (this.httpEquiv != null) {
            return this.httpEquiv;
        }
        ValueBinding _vb = getValueBinding("httpEquiv");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The httpEquiv attribute is used to specify a value for the http-equiv 
 *         attribute of an HTML Meta element. The http-equiv attribute specifies 
 *         HTTP properties that the web server can use in the HTTP response header.</p>
     * @see #getHttpEquiv()
     */
    public void setHttpEquiv(String httpEquiv) {
        this.httpEquiv = httpEquiv;
    }

    // name
    private String name = null;

    /**
 * <p>The identifier that is assigned to a property in the meta element.  
 *         The content attribute provides the actual content of the property that 
 *         is identified by the name attribute.</p>
     */
    public String getName() {
        if (this.name != null) {
            return this.name;
        }
        ValueBinding _vb = getValueBinding("name");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The identifier that is assigned to a property in the meta element.  
 *         The content attribute provides the actual content of the property that 
 *         is identified by the name attribute.</p>
     * @see #getName()
     */
    public void setName(String name) {
        this.name = name;
    }

    // scheme
    private String scheme = null;

    /**
 * <p>Defines a format to be used to interpret the value of the content 
 *         attribute.</p>
     */
    public String getScheme() {
        if (this.scheme != null) {
            return this.scheme;
        }
        ValueBinding _vb = getValueBinding("scheme");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Defines a format to be used to interpret the value of the content 
 *         attribute.</p>
     * @see #getScheme()
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.content = (String) _values[1];
        this.httpEquiv = (String) _values[2];
        this.name = (String) _values[3];
        this.scheme = (String) _values[4];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[5];
        _values[0] = super.saveState(_context);
        _values[1] = this.content;
        _values[2] = this.httpEquiv;
        _values[3] = this.name;
        _values[4] = this.scheme;
        return _values;
    }

}

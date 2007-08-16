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
 *  style="color: rgb(0, 0, 0);">ui:script</code><span
 *  style="color: rgb(0, 0, 0);"> tag to create a </span><code
 *  style="color: rgb(0, 0, 0);">&lt;script&gt;</code><span
 *  style="color: rgb(0, 0, 0);"> element in the rendered
 * HTML page.
 * The </span><code
 *  style="color: rgb(0, 0, 0);">ui:script</code><span
 *  style="color: rgb(0, 0, 0);"> tag must be used within
 * the </span><code
 *  style="color: rgb(0, 0, 0);">ui:head</code><span
 *  style="color: rgb(0, 0, 0);"> tag, or within the </span><code
 *  style="color: rgb(0, 0, 0);">ui:body</code><span
 *  style="color: rgb(0, 0, 0);"> tag.&nbsp; The </span><code
 *  style="color: rgb(0, 0, 0);">ui:script</code><span
 *  style="color: rgb(0, 0, 0);"> tag can be used to
 * refer to a
 * Javascript file, by using the url attribute. The tag can also be used
 * embed Javascript code within the rendered HTML page. </span>
 * <p style="color: rgb(0, 0, 0);">The
 * client-side script allows
 * you to perform some interactive functions such as input checking before
 * the page is submitted. <br>
 * </p>
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout</h3>
 * <span
 *  style="text-decoration: line-through; color: rgb(0, 0, 0);"></span><span
 *  style="color: rgb(0, 0, 0);">The rendered HTML page
 * contains a <code>&lt;script&gt;</code>
 * element with any attributes
 * specified through the <code>ui:script</code>
 * tag attributes.&nbsp; <br>
 * <br>
 * </span>
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers</h3>
 * <span style="color: rgb(0, 0, 0);">None.<br>
 * </span>
 * <h3 style="color: rgb(0, 0, 0);">Client
 * Side Javascript Functions</h3>
 * <span style="color: rgb(0, 0, 0);">None.
 * </span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Example</h3>
 * <b style="color: rgb(0, 0, 0);">Example
 * 1: Create a script tag to a file</b><br
 *  style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <code style="color: rgb(0, 0, 0);">
 * &lt;ui:script url="/pathtomyjs/myjavascript.js" /&gt;
 * </code><i
 *  style="color: rgb(0, 0, 0);"><br>
 * </i><br
 *  style="color: rgb(0, 0, 0);">
 * <b style="color: rgb(0, 0, 0);">Example
 * 2: Create a script tag with embedded script (Not Recommended)</b><br
 *  style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <code style="color: rgb(0, 0, 0);">
 * &lt;ui:script&gt;
 * <br>
 * <code style="color: rgb(0, 0, 0);">
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;f:verbatim&gt;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; function foo(text) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alert(text);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/f:verbatim&gt;</code><br>
 * &lt;/ui:script&gt;</code><br>
 * <br><span style="color: rgb(0, 0, 0);">Note: If the embeded script includes characters like "<" or "&", the script
 * code should be placed in an external file (see Example 1).
 * </span><br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class ScriptBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>ScriptBase</code>.</p>
     */
    public ScriptBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Script");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Script";
    }

    // charset
    private String charset = null;

    /**
 * <p>Defines the character (charset) encoding of the target URL.
 *          See iana.org for a complete list of character encodings.</p>
     */
    public String getCharset() {
        if (this.charset != null) {
            return this.charset;
        }
        ValueBinding _vb = getValueBinding("charset");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Defines the character (charset) encoding of the target URL.
 *          See iana.org for a complete list of character encodings.</p>
     * @see #getCharset()
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    // type
    private String type = null;

    /**
 * <p>Indicates the MIME type of the script.  Default is "text/javascript"</p>
     */
    public String getType() {
        if (this.type != null) {
            return this.type;
        }
        ValueBinding _vb = getValueBinding("type");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return "text/javascript";
    }

    /**
 * <p>Indicates the MIME type of the script.  Default is "text/javascript"</p>
     * @see #getType()
     */
    public void setType(String type) {
        this.type = type;
    }

    // url
    private String url = null;

    /**
 * <p>Defines the absolute or relative URL to a file that contains the 
 *          script.  Use this attribute to refer to a file instead of inserting the 
 *          script into your HTML document</p>
     */
    public String getUrl() {
        if (this.url != null) {
            return this.url;
        }
        ValueBinding _vb = getValueBinding("url");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Defines the absolute or relative URL to a file that contains the 
 *          script.  Use this attribute to refer to a file instead of inserting the 
 *          script into your HTML document</p>
     * @see #getUrl()
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.charset = (String) _values[1];
        this.type = (String) _values[2];
        this.url = (String) _values[3];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[4];
        _values[0] = super.saveState(_context);
        _values[1] = this.charset;
        _values[2] = this.type;
        _values[3] = this.url;
        return _values;
    }

}

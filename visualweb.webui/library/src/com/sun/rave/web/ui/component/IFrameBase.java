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
 * <span
 *  style="color: rgb(0, 0, 0);"><span
 *  style="text-decoration: line-through;"></span>Use
 * the <code>ui:iframe</code>
 * tag&nbsp;
 * to create an inline frame in the rendered HTML page. The <code>ui:iframe</code>
 * tag inserts a frame in which another web page can be displayed inside
 * the web application page. <br>
 * </span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout</h3>
 * <span
 *  style="text-decoration: line-through; color: rgb(0, 0, 0);"></span>
 * <span style="color: rgb(0, 0, 0);">The
 * iframe component is
 * rendered as an </span><code
 *  style="color: rgb(0, 0, 0);">&lt;iframe&gt;</code><span
 *  style="color: rgb(0, 0, 0);"> XHTML
 * element.&nbsp; The <code>ui:iframe</code>
 * tag can be configured by using the tag's attributes, which map to the <code>&lt;iframe&gt;</code>
 * properties and are similarly named. <br>
 * </span>
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers</h3>
 * <span style="color: rgb(0, 0, 0);">None.</span>
 * <h3 style="color: rgb(0, 0, 0);">Client
 * Side Javascript Functions</h3>
 * <span style="color: rgb(0, 0, 0);">None.
 * </span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Example</h3>
 * <b style="color: rgb(0, 0, 0);">Example
 * 1: Using the ui:iframe
 * tag
 * appropriately in a JSP page:<br>
 * </b><b
 *  style="color: rgb(0, 0, 0);"><br>
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
 * &nbsp;&nbsp; &lt;ui:staticText id="text1" text="Below is a
 * frame within this page without a frameset" /&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;
 * &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp;
 * &nbsp;&nbsp; &lt;ui:markup tag="br" singleton="true"
 * /&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;
 * &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp;
 * &nbsp;&nbsp; &lt;ui:iframe url="http://google.com" /&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;
 * &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp;
 * &lt;/ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;/ui:html&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;/ui:page&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:view&gt;<br>
 * &lt;/jsp:root&gt;</code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class IFrameBase extends com.sun.rave.web.ui.component.Frame {

    /**
     * <p>Construct a new <code>IFrameBase</code>.</p>
     */
    public IFrameBase() {
        super();
        setRendererType("com.sun.rave.web.ui.IFrame");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.IFrame";
    }

    // align
    private String align = null;

    /**
 * <p>Specifies how to align the iframe according to the surrounding text.  One
 *       of the following: left, right, top, middle, bottom</p>
     */
    public String getAlign() {
        if (this.align != null) {
            return this.align;
        }
        ValueBinding _vb = getValueBinding("align");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Specifies how to align the iframe according to the surrounding text.  One
 *       of the following: left, right, top, middle, bottom</p>
     * @see #getAlign()
     */
    public void setAlign(String align) {
        this.align = align;
    }

    // height
    private String height = null;

    /**
 * <p>Defines the height of the iframe in pixels or as a percentage of it's 
 *       container</p>
     */
    public String getHeight() {
        if (this.height != null) {
            return this.height;
        }
        ValueBinding _vb = getValueBinding("height");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Defines the height of the iframe in pixels or as a percentage of it's 
 *       container</p>
     * @see #getHeight()
     */
    public void setHeight(String height) {
        this.height = height;
    }

    // noResize
    private boolean noResize = false;
    private boolean noResize_set = false;

    /**
 * <p>Set the value of the noResize attribute to "true" when  user 
 *         is not allowed to resize the frame.</p>
     */
    public boolean isNoResize() {
        if (this.noResize_set) {
            return this.noResize;
        }
        ValueBinding _vb = getValueBinding("noResize");
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
 * <p>Set the value of the noResize attribute to "true" when  user 
 *         is not allowed to resize the frame.</p>
     * @see #isNoResize()
     */
    public void setNoResize(boolean noResize) {
        this.noResize = noResize;
        this.noResize_set = true;
    }

    // width
    private String width = null;

    /**
 * <p>Defines the width of the iframe in pixels or as a percentage of it's 
 *       container</p>
     */
    public String getWidth() {
        if (this.width != null) {
            return this.width;
        }
        ValueBinding _vb = getValueBinding("width");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Defines the width of the iframe in pixels or as a percentage of it's 
 *       container</p>
     * @see #getWidth()
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.align = (String) _values[1];
        this.height = (String) _values[2];
        this.noResize = ((Boolean) _values[3]).booleanValue();
        this.noResize_set = ((Boolean) _values[4]).booleanValue();
        this.width = (String) _values[5];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[6];
        _values[0] = super.saveState(_context);
        _values[1] = this.align;
        _values[2] = this.height;
        _values[3] = this.noResize ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.noResize_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.width;
        return _values;
    }

}

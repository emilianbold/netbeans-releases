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
 * <span style="color: rgb(0, 0, 0);">Use the ui:page tag to indicate the
 * beginning of the part of the JSP page that is used by the Sun Java Web
 * UI Components. All the JSF components used in the page must be nested
 * within the ui:page tag. You must use the ui:html and ui:head tags
 * first, after the opening of the ui:page tag.&nbsp;&nbsp;Then you must use either
 * the ui:body tag or the ui:frameset tag.&nbsp;&nbsp;The ui:page tag takes care of 
 * generating the preamble for the HTML page.</span><br>
 * <h3 style="color: rgb(0, 0, 0);">HTML Elements and Layout</h3>
 * 
 * <p style="color: rgb(0, 0, 0);">If the application is used in
 * a servlet environment, the rendered HTML will include <code>&lt;?xml&gt;</code>
 * and <code>&lt;!DOCTYPE&gt;</code> headers. The content of the headers
 * is determined by the <code>ui:page</code>
 * attributes that you
 * specify.&nbsp; Use the frameset and xhtml attributes to create the <code>&lt;!DOCTYPE&gt;</code>
 * header that you want.&nbsp; For example, if you set frameset and xhtml
 * attributes to true, the rendered <code>&lt;!DOCTYPE&gt;</code> header
 * will indicate XHTML Frameset, as in the following example:<br>
 * </p>
 * <pre style="color: rgb(0, 0, 0);">&lt;!DOCTYPE HTML <br> PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br> "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"&gt;<br><br></pre>
 * <span style="color: rgb(0, 0, 0);">If you set both attributes to
 * false,&nbsp; the rendered <code>&lt;!DOCTYPE&gt;</code>
 * header will indicate HTML Transitional,&nbsp; as in the following
 * example:<br>
 * </span>
 * <pre style="color: rgb(0, 0, 0);" wrap="">&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"&gt;</pre>
 * <p style="color: rgb(0, 0, 0);">You can also use the frameset and xhtml
 * attributes
 * to specify&nbsp; XHTML
 * Transitional and HTML Frameset.<br>
 * </p>
 * <h3 style="color: rgb(0, 0, 0);">Client Side Javascript Functions</h3>
 * <span style="color: rgb(0, 0, 0);">None.
 * </span><br style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Example</h3>
 * <b style="color: rgb(0, 0, 0);">Example 1: Using the page tag
 * appropriately in a JSP page:<br>
 * <br>
 * </b><code style="color: rgb(0, 0, 0);">&lt;?xml version="1.0"
 * encoding="UTF-8"?&gt;<br>
 * &lt;jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core"
 * xmlns:h="http://java.sun.com/jsf/html"
 * xmlns:jsp="http://java.sun.com/JSP/Page"
 * xmlns:ui="http://www.sun.com/web/ui"&gt;<br>
 * &lt;jsp:directive.page contentType="text/html;charset=ISO-8859-1"
 * pageEncoding="UTF-8"/&gt;<br>
 * &lt;f:view&gt;<br>
 * &nbsp; <span style="font-weight: bold;">&lt;ui:page&gt;</span><br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:html&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp; <span style="font-weight: bold;">......<br>
 * &nbsp;&nbsp;&nbsp; &lt;</span>/ui:html&gt;<br>
 * &nbsp; &lt;/ui:page&gt;<br>
 * &lt;/f:view&gt;<br>
 * </code><b style="color: rgb(0, 0, 0);"><span
 * style="font-family: monospace;"></span></b>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class PageBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>PageBase</code>.</p>
     */
    public PageBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Page");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Page";
    }

    // frame
    private boolean frame = false;
    private boolean frame_set = false;

    /**
 * <p>Use the frame attribute to
 * indicate whether the page should render frames. If this attribute is
 * true, the rendered HTML page includes a </span><code
 * style="color: rgb(0, 0, 0);">&lt;frameset&gt;</code><span
 * style="color: rgb(0, 0, 0);"> element. If false, the rendered page
 * uses a </span><code style="color: rgb(0, 0, 0);">&lt;body&gt;</code><span
 * style="color: rgb(0, 0, 0);"> tag.&nbsp; This attribute also
 * influences the rendering of the <code>&lt;!DOCTYPE&gt;</code>
 * declaration. If frameset is true, the <code>&lt;!DOCTYPE&gt;</code> will
 * be one of the following,
 * depending on the setting of xhtml attribute.<br><pre style="color: rgb(0, 0, 0);">&lt;!DOCTYPE html <br> PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br> "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"&gt;<br><br>&lt;!DOCTYPE html <br> PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN"<br> "http://www.w3.org/TR/html4/DTD/frameset.dtd"&gt;<br></pre></p>
     */
    public boolean isFrame() {
        if (this.frame_set) {
            return this.frame;
        }
        ValueBinding _vb = getValueBinding("frame");
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
 * <p>Use the frame attribute to
 * indicate whether the page should render frames. If this attribute is
 * true, the rendered HTML page includes a </span><code
 * style="color: rgb(0, 0, 0);">&lt;frameset&gt;</code><span
 * style="color: rgb(0, 0, 0);"> element. If false, the rendered page
 * uses a </span><code style="color: rgb(0, 0, 0);">&lt;body&gt;</code><span
 * style="color: rgb(0, 0, 0);"> tag.&nbsp; This attribute also
 * influences the rendering of the <code>&lt;!DOCTYPE&gt;</code>
 * declaration. If frameset is true, the <code>&lt;!DOCTYPE&gt;</code> will
 * be one of the following,
 * depending on the setting of xhtml attribute.<br><pre style="color: rgb(0, 0, 0);">&lt;!DOCTYPE html <br> PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br> "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"&gt;<br><br>&lt;!DOCTYPE html <br> PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN"<br> "http://www.w3.org/TR/html4/DTD/frameset.dtd"&gt;<br></pre></p>
     * @see #isFrame()
     */
    public void setFrame(boolean frame) {
        this.frame = frame;
        this.frame_set = true;
    }

    // xhtml
    private boolean xhtml = false;
    private boolean xhtml_set = false;

    /**
 * <p>XHTML transitional page or HTML transitional page. This attribute influences
 *         the rendering of the <code>&lt;!DOCTYPE&gt;</code> declaration. If xhtml
 *         is true, the <code>&lt;!DOCTYPE&gt;</code> will be one of the following,
 *         depending on the setting of frameset attribute.<br><pre style="color: rgb(0, 0, 0);">&lt;!DOCTYPE html <br> PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br> "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"</pre><pre style="color: rgb(0, 0, 0);">&lt;!DOCTYPE html <br> PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"<br> "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;</pre></p>
     */
    public boolean isXhtml() {
        if (this.xhtml_set) {
            return this.xhtml;
        }
        ValueBinding _vb = getValueBinding("xhtml");
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
 * <p>XHTML transitional page or HTML transitional page. This attribute influences
 *         the rendering of the <code>&lt;!DOCTYPE&gt;</code> declaration. If xhtml
 *         is true, the <code>&lt;!DOCTYPE&gt;</code> will be one of the following,
 *         depending on the setting of frameset attribute.<br><pre style="color: rgb(0, 0, 0);">&lt;!DOCTYPE html <br> PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br> "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"</pre><pre style="color: rgb(0, 0, 0);">&lt;!DOCTYPE html <br> PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"<br> "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;</pre></p>
     * @see #isXhtml()
     */
    public void setXhtml(boolean xhtml) {
        this.xhtml = xhtml;
        this.xhtml_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.frame = ((Boolean) _values[1]).booleanValue();
        this.frame_set = ((Boolean) _values[2]).booleanValue();
        this.xhtml = ((Boolean) _values[3]).booleanValue();
        this.xhtml_set = ((Boolean) _values[4]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[5];
        _values[0] = super.saveState(_context);
        _values[1] = this.frame ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.frame_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.xhtml ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.xhtml_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}

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
 * <span style="color: rgb(0, 0, 0);">Use
 * the <code>ui:frameSet</code>
 * tag to define a new set of frames in the rendered HTML page. <br>
 * When using <code>ui:frameSet</code>
 * and <code>ui:frame</code>
 * tags in
 * your application, you must also set the <code>ui:page</code>
 * tag's
 * frame attribute to "true".<br>
 * </span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout</h3>
 * <span style="color: rgb(0, 0, 0);">The
 * rendered HTML page contains
 * an XHTML-compliant <code>&lt;frameset&gt;</code>
 * element. </span><br
 *  style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers</h3>
 * <span style="color: rgb(0, 0, 0);">What
 * theme elements apply?</span><br
 *  style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Client
 * Side Javascript Functions</h3>
 * <span style="color: rgb(0, 0, 0);">None.&nbsp;
 * </span><br
 *  style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Examples</h3>
 * <p style="color: rgb(0, 0, 0);"></p>
 * <h4 style="color: rgb(0, 0, 0);">Example
 * 1: Creating a
 * frameset of two rows and two columns<br>
 * </h4>
 * <code style="color: rgb(0, 0, 0);">&lt;?xml
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
 * &lt;ui:frameSet rows="10%,*" cols="10%,*" style="color:blue"
 * styleClass="blue" toolTip="blah"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:frame toolTip="blah" url="../faces/hyperlink/hyperlink.jsp"
 * frameBorder="true" noResize="false"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:frame toolTip="blah1" url="../faces/hyperlink/nextpage.jsp"
 * frameBorder="true" noResize="false"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:frame toolTip="blah2" url="http://www.google.com"
 * frameBorder="true" noResize="false"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:frame toolTip="blah3" url="http://www.yahoo.com"
 * frameBorder="true" noResize="false"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;/ui:frameSet&gt;<br>
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

public abstract class FrameSetBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>FrameSetBase</code>.</p>
     */
    public FrameSetBase() {
        super();
        setRendererType("com.sun.rave.web.ui.FrameSet");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.FrameSet";
    }

    // border
    private int border = Integer.MIN_VALUE;
    private boolean border_set = false;

    /**
 * <p>The width, in pixels, of the space around frames. The frameSpacing 
 *         attribute and the border attribute set the same property in different 
 *         browsers.  Set frameSpacing and border to the same value.</p>
     */
    public int getBorder() {
        if (this.border_set) {
            return this.border;
        }
        ValueBinding _vb = getValueBinding("border");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
 * <p>The width, in pixels, of the space around frames. The frameSpacing 
 *         attribute and the border attribute set the same property in different 
 *         browsers.  Set frameSpacing and border to the same value.</p>
     * @see #getBorder()
     */
    public void setBorder(int border) {
        this.border = border;
        this.border_set = true;
    }

    // borderColor
    private String borderColor = null;

    /**
 * <p>The bordercolor attribute allows you to set the color of the frame 
 *          borders using a hex value or a color name.</p>
     */
    public String getBorderColor() {
        if (this.borderColor != null) {
            return this.borderColor;
        }
        ValueBinding _vb = getValueBinding("borderColor");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The bordercolor attribute allows you to set the color of the frame 
 *          borders using a hex value or a color name.</p>
     * @see #getBorderColor()
     */
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    // cols
    private String cols = null;

    /**
 * <p>Defines the number and size of columns in a frameset. The size can be 
 *          specified in pixels, percentage of the page width, or with an 
 *          asterisk (*).  Specifying * causes the columns to use available space.
 *          See the HTML specification for the frameset element for more details.</p>
     */
    public String getCols() {
        if (this.cols != null) {
            return this.cols;
        }
        ValueBinding _vb = getValueBinding("cols");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Defines the number and size of columns in a frameset. The size can be 
 *          specified in pixels, percentage of the page width, or with an 
 *          asterisk (*).  Specifying * causes the columns to use available space.
 *          See the HTML specification for the frameset element for more details.</p>
     * @see #getCols()
     */
    public void setCols(String cols) {
        this.cols = cols;
    }

    // frameBorder
    private boolean frameBorder = false;
    private boolean frameBorder_set = false;

    /**
 * <p>Flag indicating whether frames should have borders or not. If 
 *          frameBorder is true, decorative borders are drawn. If frameBorder is  
 *          false, a space between frames shows up as the background color of the
 *          page.  To show no border or space between frames, you should set 
 *          frameBorder to false, and set frameSpacing and border to 0.</p>
     */
    public boolean isFrameBorder() {
        if (this.frameBorder_set) {
            return this.frameBorder;
        }
        ValueBinding _vb = getValueBinding("frameBorder");
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
 * <p>Flag indicating whether frames should have borders or not. If 
 *          frameBorder is true, decorative borders are drawn. If frameBorder is  
 *          false, a space between frames shows up as the background color of the
 *          page.  To show no border or space between frames, you should set 
 *          frameBorder to false, and set frameSpacing and border to 0.</p>
     * @see #isFrameBorder()
     */
    public void setFrameBorder(boolean frameBorder) {
        this.frameBorder = frameBorder;
        this.frameBorder_set = true;
    }

    // frameSpacing
    private int frameSpacing = Integer.MIN_VALUE;
    private boolean frameSpacing_set = false;

    /**
 * <p>The width, in pixels, of the space around frames. The frameSpacing attribute 
 *         and the border attribute set the same property in different browsers.  
 *         Set frameSpacing and border to the same value.</p>
     */
    public int getFrameSpacing() {
        if (this.frameSpacing_set) {
            return this.frameSpacing;
        }
        ValueBinding _vb = getValueBinding("frameSpacing");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
 * <p>The width, in pixels, of the space around frames. The frameSpacing attribute 
 *         and the border attribute set the same property in different browsers.  
 *         Set frameSpacing and border to the same value.</p>
     * @see #getFrameSpacing()
     */
    public void setFrameSpacing(int frameSpacing) {
        this.frameSpacing = frameSpacing;
        this.frameSpacing_set = true;
    }

    // rows
    private String rows = null;

    /**
 * <p>Defines the number and size of rows in a frameset. The size can be 
 *          specified in pixels, percentage of the page length, or with an 
 *          asterisk (*).  Specifying * causes the rows to use available space.
 *          See the HTML specification for the frameset element for more details.</p>
     */
    public String getRows() {
        if (this.rows != null) {
            return this.rows;
        }
        ValueBinding _vb = getValueBinding("rows");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Defines the number and size of rows in a frameset. The size can be 
 *          specified in pixels, percentage of the page length, or with an 
 *          asterisk (*).  Specifying * causes the rows to use available space.
 *          See the HTML specification for the frameset element for more details.</p>
     * @see #getRows()
     */
    public void setRows(String rows) {
        this.rows = rows;
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

    // toolTip
    private String toolTip = null;

    /**
 * <p>Display the text as a tooltip for this component</p>
     */
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueBinding _vb = getValueBinding("toolTip");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Display the text as a tooltip for this component</p>
     * @see #getToolTip()
     */
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.border = ((Integer) _values[1]).intValue();
        this.border_set = ((Boolean) _values[2]).booleanValue();
        this.borderColor = (String) _values[3];
        this.cols = (String) _values[4];
        this.frameBorder = ((Boolean) _values[5]).booleanValue();
        this.frameBorder_set = ((Boolean) _values[6]).booleanValue();
        this.frameSpacing = ((Integer) _values[7]).intValue();
        this.frameSpacing_set = ((Boolean) _values[8]).booleanValue();
        this.rows = (String) _values[9];
        this.style = (String) _values[10];
        this.styleClass = (String) _values[11];
        this.toolTip = (String) _values[12];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[13];
        _values[0] = super.saveState(_context);
        _values[1] = new Integer(this.border);
        _values[2] = this.border_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.borderColor;
        _values[4] = this.cols;
        _values[5] = this.frameBorder ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.frameBorder_set ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = new Integer(this.frameSpacing);
        _values[8] = this.frameSpacing_set ? Boolean.TRUE : Boolean.FALSE;
        _values[9] = this.rows;
        _values[10] = this.style;
        _values[11] = this.styleClass;
        _values[12] = this.toolTip;
        return _values;
    }

}

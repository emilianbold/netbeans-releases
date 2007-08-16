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
 * <span style="color: rgb(0, 0, 0);">Use the ui:frame tag inside a
 * ui:frameSet tag to denote a new XHTML frame. </span><br
 * style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">HTML Elements and Layout</h3>
 * <p style="color: rgb(0, 0, 0);">If you use a ui:frame and ui:frameSet
 * tags you should set the ui:page tag's frame attribute to "true".<br>
 * </p>
 * This tag renders an xhtml compliant &lt;frame&gt; tag.&nbsp; <br>
 * <h3 style="color: rgb(0, 0, 0);">Client Side Javascript Functions</h3>
 * <span style="color: rgb(0, 0, 0);">None.
 * </span><br style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Example</h3>
 * <b style="color: rgb(0, 0, 0);">Example 1: Using the ui:frame tag
 * appropriately in a JSP page:<br>
 * <br>
 * </b><code style="color: rgb(0, 0, 0);">&lt;?xml version="1.0"
 * encoding="UTF-8"?&gt;<br>
 * &lt;jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core"
 * xmlns:h="http://java.sun.com/jsf/html"
 * xmlns:jsp="http://java.sun.com/JSP/Page"
 * xmlns:ui="http://www.sun.com/web/ui"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;jsp:directive.page
 * contentType="text/html;charset=ISO-8859-1"
 * pageEncoding="UTF-8"/&gt;&lt;f:view&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:page frame="true"&gt;<br>
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
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:page&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:view&gt;<br>
 * &lt;/jsp:root&gt;<br>
 * </code><b style="color: rgb(0, 0, 0);"><span
 * style="font-family: monospace;"></span></b>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class FrameBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>FrameBase</code>.</p>
     */
    public FrameBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Frame");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Frame";
    }

    // frameBorder
    private boolean frameBorder = false;
    private boolean frameBorder_set = false;

    /**
 * <p>Set the value of the frameBorder attribute to "true" when a border is 
 *         needed around the frame.</p>
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
 * <p>Set the value of the frameBorder attribute to "true" when a border is 
 *         needed around the frame.</p>
     * @see #isFrameBorder()
     */
    public void setFrameBorder(boolean frameBorder) {
        this.frameBorder = frameBorder;
        this.frameBorder_set = true;
    }

    // longDesc
    private String longDesc = null;

    /**
 * <p>A URL to a long description of the frame contents. Use it for browsers that do not support frames</p>
     */
    public String getLongDesc() {
        if (this.longDesc != null) {
            return this.longDesc;
        }
        ValueBinding _vb = getValueBinding("longDesc");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>A URL to a long description of the frame contents. Use it for browsers that do not support frames</p>
     * @see #getLongDesc()
     */
    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    // marginHeight
    private int marginHeight = Integer.MIN_VALUE;
    private boolean marginHeight_set = false;

    /**
 * <p>Defines the top and bottom margins in the frame</p>
     */
    public int getMarginHeight() {
        if (this.marginHeight_set) {
            return this.marginHeight;
        }
        ValueBinding _vb = getValueBinding("marginHeight");
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
 * <p>Defines the top and bottom margins in the frame</p>
     * @see #getMarginHeight()
     */
    public void setMarginHeight(int marginHeight) {
        this.marginHeight = marginHeight;
        this.marginHeight_set = true;
    }

    // marginWidth
    private int marginWidth = Integer.MIN_VALUE;
    private boolean marginWidth_set = false;

    /**
 * <p>Defines the left and right margins in the frame</p>
     */
    public int getMarginWidth() {
        if (this.marginWidth_set) {
            return this.marginWidth;
        }
        ValueBinding _vb = getValueBinding("marginWidth");
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
 * <p>Defines the left and right margins in the frame</p>
     * @see #getMarginWidth()
     */
    public void setMarginWidth(int marginWidth) {
        this.marginWidth = marginWidth;
        this.marginWidth_set = true;
    }

    // name
    private String name = null;

    /**
 * <p>Defines a unique name for the frame (to use in scripts)</p>
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
 * <p>Defines a unique name for the frame (to use in scripts)</p>
     * @see #getName()
     */
    public void setName(String name) {
        this.name = name;
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

    // scrolling
    private String scrolling = null;

    /**
 * <p>Determines scrollbar action (valid values are: yes, no, auto)</p>
     */
    public String getScrolling() {
        if (this.scrolling != null) {
            return this.scrolling;
        }
        ValueBinding _vb = getValueBinding("scrolling");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Determines scrollbar action (valid values are: yes, no, auto)</p>
     * @see #getScrolling()
     */
    public void setScrolling(String scrolling) {
        this.scrolling = scrolling;
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

    // url
    private String url = null;

    /**
 * <p>Defines the URL of the file to show in the frame.</p>
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
 * <p>Defines the URL of the file to show in the frame.</p>
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
        this.frameBorder = ((Boolean) _values[1]).booleanValue();
        this.frameBorder_set = ((Boolean) _values[2]).booleanValue();
        this.longDesc = (String) _values[3];
        this.marginHeight = ((Integer) _values[4]).intValue();
        this.marginHeight_set = ((Boolean) _values[5]).booleanValue();
        this.marginWidth = ((Integer) _values[6]).intValue();
        this.marginWidth_set = ((Boolean) _values[7]).booleanValue();
        this.name = (String) _values[8];
        this.noResize = ((Boolean) _values[9]).booleanValue();
        this.noResize_set = ((Boolean) _values[10]).booleanValue();
        this.scrolling = (String) _values[11];
        this.style = (String) _values[12];
        this.styleClass = (String) _values[13];
        this.toolTip = (String) _values[14];
        this.url = (String) _values[15];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[16];
        _values[0] = super.saveState(_context);
        _values[1] = this.frameBorder ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.frameBorder_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.longDesc;
        _values[4] = new Integer(this.marginHeight);
        _values[5] = this.marginHeight_set ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = new Integer(this.marginWidth);
        _values[7] = this.marginWidth_set ? Boolean.TRUE : Boolean.FALSE;
        _values[8] = this.name;
        _values[9] = this.noResize ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.noResize_set ? Boolean.TRUE : Boolean.FALSE;
        _values[11] = this.scrolling;
        _values[12] = this.style;
        _values[13] = this.styleClass;
        _values[14] = this.toolTip;
        _values[15] = this.url;
        return _values;
    }

}

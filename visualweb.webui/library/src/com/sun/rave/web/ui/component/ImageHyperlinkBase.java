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
 * <span style="color: rgb(51, 51, 255);"><span
 *  style="color: rgb(0, 0, 0);">Use the </span><code
 *  style="color: rgb(0, 0, 0);">ui:imageHyperlink</code><span
 *  style="color: rgb(0, 0, 0);">
 * tag to display a clickable image in the rendered HTML page. The image
 * is surrounded by an HTML hyperlink, allowing the image to function as a
 * hyperlink.&nbsp; This tag is based on a <code>ui:hyperlink</code>
 * tag and functions the same way.&nbsp; The main difference is this
 * tag will format an image with a surrounding hyperlink.&nbsp; See
 * the <code>ui:hyperlink</code>
 * tag for more examples on using a hyperlink.</span></span><br>
 * <br>
 * The
 * <span style="color: rgb(51, 51, 255);"><code
 *  style="color: rgb(0, 0, 0);">ui:imageHyperlink</code><span
 *  style="color: rgb(0, 0, 0);"></span></span>
 * component
 * can be also be used to submit forms. If the action attribute is used,
 * the form is submitted. If the
 * url attribute is used, the link is a normal hyperlink that sends the
 * browser to a new location.<br>
 * <br>
 * <span style="color: rgb(0, 0, 0);">The
 * <code>ui:imageHyperlink</code>
 * can display a clickable icon image from the current theme in the
 * rendered HTML page using the "icon" attribute. The <code></code>tag
 * allows you to use an
 * icon (a small image) from the current theme. Currently the list of
 * icons that you can use is not publicly supported, but the icon names
 * are specified in the <code>/com/sun/rave/web/ui/suntheme/SunTheme.properties</code>
 * file. The names are listed as resource keys of the format <code>image.ICON_NAME.</code>
 * Use only the part of the key that follows <code>image.
 * </code>For
 * example, if the key is <code>image.ALARM_CRITICAL_SMALL</code>,
 * you
 * should use <code>ALARM_CRITICAL_SMALL
 * </code>as the specified
 * icon name in the <code>ui:</code></span><span
 *  style="color: rgb(0, 0, 0);"><code>imageHyperlink</code>
 * </span><span
 *  style="color: rgb(0, 0, 0);"><code></code>tag.
 * In the near future a
 * supported list will be published.&nbsp; </span><br>
 * <span style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout</h3>
 * <span style="color: rgb(0, 0, 0);"></span>
 * <p><span
 *  style="color: rgb(0, 0, 0);">The rendered HTML page
 * contains
 * an XHTML <code>&lt;a&gt;</code>
 * element with an <code>&lt;img&gt;</code>
 * element inside. <code></code>
 * Image attributes that are specified
 * with the <code>ui:imageHyperlink</code>
 * tag are used as attributes in
 * the <code>&lt;img&gt;</code>&nbsp;
 * element.</span><br>
 * </p>
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers</h3>
 * <span style="color: rgb(0, 0, 0);">None</span><br>
 * <h3>Client Side Javascript
 * Functions<span
 *  style="color: rgb(255, 153, 0);"></span><br>
 * </h3>
 * <p>None. <span
 *  style="color: rgb(255, 153, 0);"></span></p>
 * <h3>Examples&nbsp;<span
 *  style="color: rgb(255, 153, 0);"></span></h3>
 * <span style="color: rgb(255, 153, 0);"></span>
 * <h4>Example 1: Create an <span
 *  style="text-decoration: line-through;"></span><span
 *  style="color: rgb(0, 0, 0);">imageHyperlink</span>
 * with yahoo gif<br>
 * </h4>
 * <span style="color: rgb(255, 153, 0);"></span><code>&lt;ui:imageHyperlink
 * id="imagehyperlinktest1" imageURL="./myyahoo.gif"
 * action="#{HyperlinkBean.getRequiredHelp}" /&gt;</code></span><span
 *  style="color: rgb(0, 0, 0);"><br>
 * </span>
 * <h4 style="color: rgb(0, 0, 0);">Example
 * 2: Create an IconHyperlink using the required
 * indicator icon<br>
 * </h4>
 * <span style="color: rgb(0, 0, 0);">
 * </span><code
 *  style="color: rgb(0, 0, 0);">&lt;ui:iconHyperlink
 * id="iconhyperlinktest1" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * icon="LABEL_REQUIRED_ICON"
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * action="#{HyperlinkBean.getRequiredHelp}" /&gt;
 * </code><span
 *  style="color: rgb(0, 0, 0);"><br>
 * </span>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class ImageHyperlinkBase extends com.sun.rave.web.ui.component.Hyperlink {

    /**
     * <p>Construct a new <code>ImageHyperlinkBase</code>.</p>
     */
    public ImageHyperlinkBase() {
        super();
        setRendererType("com.sun.rave.web.ui.ImageHyperlink");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.ImageHyperlink";
    }

    // align
    private String align = null;

    /**
 * <p>Specifies the position of the image with respect to its context.
 * 	Valid values are: bottom (the default); middle; top; left; right.</p>
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
 * <p>Specifies the position of the image with respect to its context.
 * 	Valid values are: bottom (the default); middle; top; left; right.</p>
     * @see #getAlign()
     */
    public void setAlign(String align) {
        this.align = align;
    }

    // alt
    private String alt = null;

    /**
 * <p>Alternative text description used by screen reader tools</p>
     */
    public String getAlt() {
        if (this.alt != null) {
            return this.alt;
        }
        ValueBinding _vb = getValueBinding("alt");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Alternative text description used by screen reader tools</p>
     * @see #getAlt()
     */
    public void setAlt(String alt) {
        this.alt = alt;
    }

    // border
    private int border = Integer.MIN_VALUE;
    private boolean border_set = false;

    /**
 * <p>Specifies the width of the img border in pixels.
 *         The default value for this attribute depends on the client browser</p>
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
 * <p>Specifies the width of the img border in pixels.
 *         The default value for this attribute depends on the client browser</p>
     * @see #getBorder()
     */
    public void setBorder(int border) {
        this.border = border;
        this.border_set = true;
    }

    // height
    private int height = Integer.MIN_VALUE;
    private boolean height_set = false;

    /**
 * <p>When specified, the width and height attributes tell the client browser to override the natural image or object size in favor of these values, specified in pixels. Some browsers might not support this behavior.</p>
     */
    public int getHeight() {
        if (this.height_set) {
            return this.height;
        }
        ValueBinding _vb = getValueBinding("height");
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
 * <p>When specified, the width and height attributes tell the client browser to override the natural image or object size in favor of these values, specified in pixels. Some browsers might not support this behavior.</p>
     * @see #getHeight()
     */
    public void setHeight(int height) {
        this.height = height;
        this.height_set = true;
    }

    // hspace
    private int hspace = Integer.MIN_VALUE;
    private boolean hspace_set = false;

    /**
 * <p>Specifies the amount of white space in pixels to be inserted to the left and 
 * 	right of the image. The default value is not specified but is 
 * 	generally a small, non-zero size.</p>
     */
    public int getHspace() {
        if (this.hspace_set) {
            return this.hspace;
        }
        ValueBinding _vb = getValueBinding("hspace");
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
 * <p>Specifies the amount of white space in pixels to be inserted to the left and 
 * 	right of the image. The default value is not specified but is 
 * 	generally a small, non-zero size.</p>
     * @see #getHspace()
     */
    public void setHspace(int hspace) {
        this.hspace = hspace;
        this.hspace_set = true;
    }

    // icon
    private String icon = null;

    /**
 * <p>The identifier of the desired theme image.</p>
     */
    public String getIcon() {
        if (this.icon != null) {
            return this.icon;
        }
        ValueBinding _vb = getValueBinding("icon");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The identifier of the desired theme image.</p>
     * @see #getIcon()
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    // imageURL
    private String imageURL = null;

    /**
 * <p>Absolute or relative URL to the image to be rendered.</p>
     */
    public String getImageURL() {
        if (this.imageURL != null) {
            return this.imageURL;
        }
        ValueBinding _vb = getValueBinding("imageURL");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Absolute or relative URL to the image to be rendered.</p>
     * @see #getImageURL()
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // textPosition
    private String textPosition = null;

    /**
 * <p>Specifies where the text will be placed relative to the image. The valid 
 *         values currently are "right" or "left". There will be support in the 
 *         future for "top" and "bottom".</p>
     */
    public String getTextPosition() {
        if (this.textPosition != null) {
            return this.textPosition;
        }
        ValueBinding _vb = getValueBinding("textPosition");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return "right";
    }

    /**
 * <p>Specifies where the text will be placed relative to the image. The valid 
 *         values currently are "right" or "left". There will be support in the 
 *         future for "top" and "bottom".</p>
     * @see #getTextPosition()
     */
    public void setTextPosition(String textPosition) {
        this.textPosition = textPosition;
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

    // vspace
    private int vspace = Integer.MIN_VALUE;
    private boolean vspace_set = false;

    /**
 * <p>Specifies the amount of white space in pixels to be inserted above and below the 
 * 	image. The default value is not specified but is generally a small, 
 * 	non-zero size.</p>
     */
    public int getVspace() {
        if (this.vspace_set) {
            return this.vspace;
        }
        ValueBinding _vb = getValueBinding("vspace");
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
 * <p>Specifies the amount of white space in pixels to be inserted above and below the 
 * 	image. The default value is not specified but is generally a small, 
 * 	non-zero size.</p>
     * @see #getVspace()
     */
    public void setVspace(int vspace) {
        this.vspace = vspace;
        this.vspace_set = true;
    }

    // width
    private int width = Integer.MIN_VALUE;
    private boolean width_set = false;

    /**
 * <p>Image width override. When specified, the width and height attributes 
 * 	tell user agents to override the natural image or object size in favor 
 * 	of these values.</p>
     */
    public int getWidth() {
        if (this.width_set) {
            return this.width;
        }
        ValueBinding _vb = getValueBinding("width");
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
 * <p>Image width override. When specified, the width and height attributes 
 * 	tell user agents to override the natural image or object size in favor 
 * 	of these values.</p>
     * @see #getWidth()
     */
    public void setWidth(int width) {
        this.width = width;
        this.width_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.align = (String) _values[1];
        this.alt = (String) _values[2];
        this.border = ((Integer) _values[3]).intValue();
        this.border_set = ((Boolean) _values[4]).booleanValue();
        this.height = ((Integer) _values[5]).intValue();
        this.height_set = ((Boolean) _values[6]).booleanValue();
        this.hspace = ((Integer) _values[7]).intValue();
        this.hspace_set = ((Boolean) _values[8]).booleanValue();
        this.icon = (String) _values[9];
        this.imageURL = (String) _values[10];
        this.textPosition = (String) _values[11];
        this.visible = ((Boolean) _values[12]).booleanValue();
        this.visible_set = ((Boolean) _values[13]).booleanValue();
        this.vspace = ((Integer) _values[14]).intValue();
        this.vspace_set = ((Boolean) _values[15]).booleanValue();
        this.width = ((Integer) _values[16]).intValue();
        this.width_set = ((Boolean) _values[17]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[18];
        _values[0] = super.saveState(_context);
        _values[1] = this.align;
        _values[2] = this.alt;
        _values[3] = new Integer(this.border);
        _values[4] = this.border_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = new Integer(this.height);
        _values[6] = this.height_set ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = new Integer(this.hspace);
        _values[8] = this.hspace_set ? Boolean.TRUE : Boolean.FALSE;
        _values[9] = this.icon;
        _values[10] = this.imageURL;
        _values[11] = this.textPosition;
        _values[12] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        _values[14] = new Integer(this.vspace);
        _values[15] = this.vspace_set ? Boolean.TRUE : Boolean.FALSE;
        _values[16] = new Integer(this.width);
        _values[17] = this.width_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}

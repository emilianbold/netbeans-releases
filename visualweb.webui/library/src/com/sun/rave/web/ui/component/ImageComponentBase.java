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
 * <code></code>Use
 * the <code>ui:image</code>
 * tag to display an inline image in the rendered
 * HTML page. The url attribute is used to specify the URL to the image
 * file that
 * is to be displayed.&nbsp;&nbsp; If you use an image that is a
 * PNG type and ends with ".png " this tag will output the correct format
 * for an &lt;img&gt; tag to display a PNG correctly in Internet
 * Explorer.&nbsp; In order to use this feature you must set the
 * height and width properties of this tag..<br>
 * <br>
 * The <code>ui:image</code>
 * tag can be used to display a theme-specific image in the
 * rendered HTML page. The icon attribute used in the <code>ui:image
 * </code>tag is
 * a key value that is mapped to a URL in theme properties file. The key
 * is used
 * to look up the appropriate image source and related attributes from the
 * current
 * theme. By specifying a key, you avoid the need to specify predefined
 * constants
 * such as height and width. The image can also be seamlessly changed when
 * a
 * different theme is selected.<br>
 * <br>
 * Note: currently the list of
 * icons that you can use is not publicly
 * supported, but the icon names are specified in the
 * <code>/com/sun/rave/web/ui/suntheme/SunTheme.properties</code>
 * file. The names are
 * listed as resource keys of the format <code>image.ICON_NAME</code>.
 * Use only
 * the part of the key that follows image. For example, if the key is
 * <code>image.ALARM_CRITICAL_SMALL</code>,
 * you should specify
 * <code>ALARM_CRITICAL_SMALL</code>
 * as the value of the icon attribute of the
 * <code>ui:icon</code>
 * tag. A list of supported icon values will be published in
 * the near future.<br>
 * <h3>HTML Elements and Layout</h3>
 * The rendered HTML page displays an XHTML compliant <code>&lt;img&gt;</code>
 * element with any applicable element attributes. The attributes can be
 * specified
 * through the <code>&lt;ui:image&gt;</code>
 * tag attributes.
 * <h3>Client Side Javascript
 * Functions</h3>
 * None.
 * <br>
 * <h3>Examples</h3>
 * <h4>Example 1: Create an image</h4>
 * <code>&lt;ui:image id="image1"
 * url="../images/dot.gif" /&gt;
 * <br>
 * <br>
 * </code>This will generate the
 * following markup: <br>
 * &nbsp;&nbsp; <br>
 * <code>&lt;img
 * src="../images/dot.gif" alt="" /&gt;
 * </code><br>
 * <br>
 * <h4>Example 2: Create a theme
 * specific image<br>
 * </h4>
 * <code>&lt;ui:image id="image2"
 * icon="</code><code>ALARM_CRITICAL_SMALL</code><code>"
 * /&gt;
 * </code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class ImageComponentBase extends javax.faces.component.UIGraphic {

    /**
     * <p>Construct a new <code>ImageComponentBase</code>.</p>
     */
    public ImageComponentBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Image");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Image";
    }

    /**
     * <p>Return the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property aliases.</p>
     *
     * @param name Name of value binding to retrieve
     */
    public ValueBinding getValueBinding(String name) {
        if (name.equals("url")) {
            return super.getValueBinding("value");
        }
        return super.getValueBinding(name);
    }

    /**
     * <p>Set the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property
     * aliases.</p>
     *
     * @param name    Name of value binding to set
     * @param binding ValueBinding to set, or null to remove
     */
    public void setValueBinding(String name,ValueBinding binding) {
        if (name.equals("url")) {
            super.setValueBinding("value", binding);
            return;
        }
        super.setValueBinding(name, binding);
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
 * 	The default value for this attribute depends on the web browser</p>
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
        return 0;
    }

    /**
 * <p>Specifies the width of the img border in pixels.
 * 	The default value for this attribute depends on the web browser</p>
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
 * <p>When specified, the width and height attributes tell web browsers 
 * 	to override the natural image or object size in favor of these values</p>
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
 * <p>When specified, the width and height attributes tell web browsers 
 * 	to override the natural image or object size in favor of these values</p>
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
 * <p>Specifies the amount of white space in pixels to be inserted to the
 * 	left and right of the image. The default value is not specified but is       
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
 * <p>Specifies the amount of white space in pixels to be inserted to the
 * 	left and right of the image. The default value is not specified but is       
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

    // longDesc
    private String longDesc = null;

    /**
 * <p>A verbose description of this image</p>
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
 * <p>A verbose description of this image</p>
     * @see #getLongDesc()
     */
    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    // onClick
    private String onClick = null;

    /**
 * <p>Scripting code executed when a mouse click
 *     occurs over this component.</p>
     */
    public String getOnClick() {
        if (this.onClick != null) {
            return this.onClick;
        }
        ValueBinding _vb = getValueBinding("onClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse click
 *     occurs over this component.</p>
     * @see #getOnClick()
     */
    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    // onDblClick
    private String onDblClick = null;

    /**
 * <p>Scripting code executed when a mouse double click
 *     occurs over this component.</p>
     */
    public String getOnDblClick() {
        if (this.onDblClick != null) {
            return this.onDblClick;
        }
        ValueBinding _vb = getValueBinding("onDblClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse double click
 *     occurs over this component.</p>
     * @see #getOnDblClick()
     */
    public void setOnDblClick(String onDblClick) {
        this.onDblClick = onDblClick;
    }

    // onMouseDown
    private String onMouseDown = null;

    /**
 * <p>Scripting code executed when the user presses a mouse button while the
 *     mouse pointer is on the component.</p>
     */
    public String getOnMouseDown() {
        if (this.onMouseDown != null) {
            return this.onMouseDown;
        }
        ValueBinding _vb = getValueBinding("onMouseDown");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses a mouse button while the
 *     mouse pointer is on the component.</p>
     * @see #getOnMouseDown()
     */
    public void setOnMouseDown(String onMouseDown) {
        this.onMouseDown = onMouseDown;
    }

    // onMouseMove
    private String onMouseMove = null;

    /**
 * <p>Scripting code executed when the user moves the mouse pointer while
 *     over the component.</p>
     */
    public String getOnMouseMove() {
        if (this.onMouseMove != null) {
            return this.onMouseMove;
        }
        ValueBinding _vb = getValueBinding("onMouseMove");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user moves the mouse pointer while
 *     over the component.</p>
     * @see #getOnMouseMove()
     */
    public void setOnMouseMove(String onMouseMove) {
        this.onMouseMove = onMouseMove;
    }

    // onMouseOut
    private String onMouseOut = null;

    /**
 * <p>Scripting code executed when a mouse out movement
 *     occurs over this component.</p>
     */
    public String getOnMouseOut() {
        if (this.onMouseOut != null) {
            return this.onMouseOut;
        }
        ValueBinding _vb = getValueBinding("onMouseOut");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse out movement
 *     occurs over this component.</p>
     * @see #getOnMouseOut()
     */
    public void setOnMouseOut(String onMouseOut) {
        this.onMouseOut = onMouseOut;
    }

    // onMouseOver
    private String onMouseOver = null;

    /**
 * <p>Scripting code executed when the user moves the  mouse pointer into
 *     the boundary of this component.</p>
     */
    public String getOnMouseOver() {
        if (this.onMouseOver != null) {
            return this.onMouseOver;
        }
        ValueBinding _vb = getValueBinding("onMouseOver");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user moves the  mouse pointer into
 *     the boundary of this component.</p>
     * @see #getOnMouseOver()
     */
    public void setOnMouseOver(String onMouseOver) {
        this.onMouseOver = onMouseOver;
    }

    // onMouseUp
    private String onMouseUp = null;

    /**
 * <p>Scripting code executed when the user releases a mouse button while
 *     the mouse pointer is on the component.</p>
     */
    public String getOnMouseUp() {
        if (this.onMouseUp != null) {
            return this.onMouseUp;
        }
        ValueBinding _vb = getValueBinding("onMouseUp");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user releases a mouse button while
 *     the mouse pointer is on the component.</p>
     * @see #getOnMouseUp()
     */
    public void setOnMouseUp(String onMouseUp) {
        this.onMouseUp = onMouseUp;
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
    /**
 * <p>Absolute or relative URL to the image to be rendered.</p>
     */
    public String getUrl() {
        return (String) getValue();
    }

    /**
 * <p>Absolute or relative URL to the image to be rendered.</p>
     * @see #getUrl()
     */
    public void setUrl(String url) {
        setValue((Object) url);
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
 * <p>Specifies the amount of white space in pixels to be inserted above and
 * 	below the image. The default value is not specified but is generally a
 * 	small, non-zero size.</p>
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
 * <p>Specifies the amount of white space in pixels to be inserted above and
 * 	below the image. The default value is not specified but is generally a
 * 	small, non-zero size.</p>
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
 * 	tell web browsers to override the natural image or object size in favor 
 * 	of these values, specified in pixels. Some browsers might not support 
 * 	this behavior.</p>
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
 * 	tell web browsers to override the natural image or object size in favor 
 * 	of these values, specified in pixels. Some browsers might not support 
 * 	this behavior.</p>
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
        this.longDesc = (String) _values[10];
        this.onClick = (String) _values[11];
        this.onDblClick = (String) _values[12];
        this.onMouseDown = (String) _values[13];
        this.onMouseMove = (String) _values[14];
        this.onMouseOut = (String) _values[15];
        this.onMouseOver = (String) _values[16];
        this.onMouseUp = (String) _values[17];
        this.style = (String) _values[18];
        this.styleClass = (String) _values[19];
        this.toolTip = (String) _values[20];
        this.visible = ((Boolean) _values[21]).booleanValue();
        this.visible_set = ((Boolean) _values[22]).booleanValue();
        this.vspace = ((Integer) _values[23]).intValue();
        this.vspace_set = ((Boolean) _values[24]).booleanValue();
        this.width = ((Integer) _values[25]).intValue();
        this.width_set = ((Boolean) _values[26]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[27];
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
        _values[10] = this.longDesc;
        _values[11] = this.onClick;
        _values[12] = this.onDblClick;
        _values[13] = this.onMouseDown;
        _values[14] = this.onMouseMove;
        _values[15] = this.onMouseOut;
        _values[16] = this.onMouseOver;
        _values[17] = this.onMouseUp;
        _values[18] = this.style;
        _values[19] = this.styleClass;
        _values[20] = this.toolTip;
        _values[21] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[22] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        _values[23] = new Integer(this.vspace);
        _values[24] = this.vspace_set ? Boolean.TRUE : Boolean.FALSE;
        _values[25] = new Integer(this.width);
        _values[26] = this.width_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}

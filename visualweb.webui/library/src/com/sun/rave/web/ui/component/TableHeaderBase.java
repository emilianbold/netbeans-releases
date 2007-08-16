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
 * The tableHeader component provides a layout mechanism for displaying headers.
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class TableHeaderBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>TableHeaderBase</code>.</p>
     */
    public TableHeaderBase() {
        super();
        setRendererType("com.sun.rave.web.ui.TableHeader");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.TableHeader";
    }

    // abbr
    private String abbr = null;

    public String getAbbr() {
        if (this.abbr != null) {
            return this.abbr;
        }
        ValueBinding _vb = getValueBinding("abbr");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    // align
    private String align = null;

    /**
 * <p>Sets the horizontal alignment (left, right, justify, center) for the cell contents</p>
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
 * <p>Sets the horizontal alignment (left, right, justify, center) for the cell contents</p>
     * @see #getAlign()
     */
    public void setAlign(String align) {
        this.align = align;
    }

    // axis
    private String axis = null;

    public String getAxis() {
        if (this.axis != null) {
            return this.axis;
        }
        ValueBinding _vb = getValueBinding("axis");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }

    // bgColor
    private String bgColor = null;

    public String getBgColor() {
        if (this.bgColor != null) {
            return this.bgColor;
        }
        ValueBinding _vb = getValueBinding("bgColor");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    // char
    private String _char = null;

    public String getChar() {
        if (this._char != null) {
            return this._char;
        }
        ValueBinding _vb = getValueBinding("char");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    public void setChar(String _char) {
        this._char = _char;
    }

    // charOff
    private String charOff = null;

    public String getCharOff() {
        if (this.charOff != null) {
            return this.charOff;
        }
        ValueBinding _vb = getValueBinding("charOff");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    public void setCharOff(String charOff) {
        this.charOff = charOff;
    }

    // colSpan
    private int colSpan = Integer.MIN_VALUE;
    private boolean colSpan_set = false;

    /**
 * <p>The number of columns spanned by a cell</p>
     */
    public int getColSpan() {
        if (this.colSpan_set) {
            return this.colSpan;
        }
        ValueBinding _vb = getValueBinding("colSpan");
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
 * <p>The number of columns spanned by a cell</p>
     * @see #getColSpan()
     */
    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
        this.colSpan_set = true;
    }

    // extraHtml
    private String extraHtml = null;

    /**
 * <p>Extra HTML to be appended to the tag output by this renderer.</p>
     */
    public String getExtraHtml() {
        if (this.extraHtml != null) {
            return this.extraHtml;
        }
        ValueBinding _vb = getValueBinding("extraHtml");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Extra HTML to be appended to the tag output by this renderer.</p>
     * @see #getExtraHtml()
     */
    public void setExtraHtml(String extraHtml) {
        this.extraHtml = extraHtml;
    }

    // groupHeader
    private boolean groupHeader = false;
    private boolean groupHeader_set = false;

    /**
 * <p>Flag indicating this component should render a group header. The default renders
 * a column header. This should not be used if selectHeader or sortHeader are used.</p>
     */
    public boolean isGroupHeader() {
        if (this.groupHeader_set) {
            return this.groupHeader;
        }
        ValueBinding _vb = getValueBinding("groupHeader");
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
 * <p>Flag indicating this component should render a group header. The default renders
 * a column header. This should not be used if selectHeader or sortHeader are used.</p>
     * @see #isGroupHeader()
     */
    public void setGroupHeader(boolean groupHeader) {
        this.groupHeader = groupHeader;
        this.groupHeader_set = true;
    }

    // headers
    private String headers = null;

    /**
 * <p>Space separated list of header cell ID values</p>
     */
    public String getHeaders() {
        if (this.headers != null) {
            return this.headers;
        }
        ValueBinding _vb = getValueBinding("headers");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Space separated list of header cell ID values</p>
     * @see #getHeaders()
     */
    public void setHeaders(String headers) {
        this.headers = headers;
    }

    // height
    private String height = null;

    /**
 * <p>Set the cell height in pixels (deprecated in HTML 4.0)</p>
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
 * <p>Set the cell height in pixels (deprecated in HTML 4.0)</p>
     * @see #getHeight()
     */
    public void setHeight(String height) {
        this.height = height;
    }

    // noWrap
    private boolean noWrap = false;
    private boolean noWrap_set = false;

    /**
 * <p>Disable word wrapping (deprecated in HTML 4.0)</p>
     */
    public boolean isNoWrap() {
        if (this.noWrap_set) {
            return this.noWrap;
        }
        ValueBinding _vb = getValueBinding("noWrap");
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
 * <p>Disable word wrapping (deprecated in HTML 4.0)</p>
     * @see #isNoWrap()
     */
    public void setNoWrap(boolean noWrap) {
        this.noWrap = noWrap;
        this.noWrap_set = true;
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

    // onKeyDown
    private String onKeyDown = null;

    /**
 * <p>Scripting code executed when the user presses down on a key while the
 *     component has focus.</p>
     */
    public String getOnKeyDown() {
        if (this.onKeyDown != null) {
            return this.onKeyDown;
        }
        ValueBinding _vb = getValueBinding("onKeyDown");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses down on a key while the
 *     component has focus.</p>
     * @see #getOnKeyDown()
     */
    public void setOnKeyDown(String onKeyDown) {
        this.onKeyDown = onKeyDown;
    }

    // onKeyPress
    private String onKeyPress = null;

    /**
 * <p>Scripting code executed when the user presses and releases a key while
 *     the component has focus.</p>
     */
    public String getOnKeyPress() {
        if (this.onKeyPress != null) {
            return this.onKeyPress;
        }
        ValueBinding _vb = getValueBinding("onKeyPress");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses and releases a key while
 *     the component has focus.</p>
     * @see #getOnKeyPress()
     */
    public void setOnKeyPress(String onKeyPress) {
        this.onKeyPress = onKeyPress;
    }

    // onKeyUp
    private String onKeyUp = null;

    /**
 * <p>Scripting code executed when the user releases a key while the
 *     component has focus.</p>
     */
    public String getOnKeyUp() {
        if (this.onKeyUp != null) {
            return this.onKeyUp;
        }
        ValueBinding _vb = getValueBinding("onKeyUp");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user releases a key while the
 *     component has focus.</p>
     * @see #getOnKeyUp()
     */
    public void setOnKeyUp(String onKeyUp) {
        this.onKeyUp = onKeyUp;
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

    // rowSpan
    private int rowSpan = Integer.MIN_VALUE;
    private boolean rowSpan_set = false;

    /**
 * <p>The number of rows spanned by a cell</p>
     */
    public int getRowSpan() {
        if (this.rowSpan_set) {
            return this.rowSpan;
        }
        ValueBinding _vb = getValueBinding("rowSpan");
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
 * <p>The number of rows spanned by a cell</p>
     * @see #getRowSpan()
     */
    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
        this.rowSpan_set = true;
    }

    // scope
    private String scope = null;

    /**
 * <p>Indicates that information in a cell is also acting as a header</p>
     */
    public String getScope() {
        if (this.scope != null) {
            return this.scope;
        }
        ValueBinding _vb = getValueBinding("scope");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Indicates that information in a cell is also acting as a header</p>
     * @see #getScope()
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    // selectHeader
    private boolean selectHeader = false;
    private boolean selectHeader_set = false;

    /**
 * <p>Flag indicating this component should render a selection column header. The 
 * default renders a column header. This should not be used if groupHeader or 
 * sortHeader are used.</p>
     */
    public boolean isSelectHeader() {
        if (this.selectHeader_set) {
            return this.selectHeader;
        }
        ValueBinding _vb = getValueBinding("selectHeader");
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
 * <p>Flag indicating this component should render a selection column header. The 
 * default renders a column header. This should not be used if groupHeader or 
 * sortHeader are used.</p>
     * @see #isSelectHeader()
     */
    public void setSelectHeader(boolean selectHeader) {
        this.selectHeader = selectHeader;
        this.selectHeader_set = true;
    }

    // sortHeader
    private boolean sortHeader = false;
    private boolean sortHeader_set = false;

    /**
 * <p>Flag indicating this component should render a sortable column header. The 
 * default renders a column header. This should not be used if groupHeader or 
 * selectHeader are used.</p>
     */
    public boolean isSortHeader() {
        if (this.sortHeader_set) {
            return this.sortHeader;
        }
        ValueBinding _vb = getValueBinding("sortHeader");
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
 * <p>Flag indicating this component should render a sortable column header. The 
 * default renders a column header. This should not be used if groupHeader or 
 * selectHeader are used.</p>
     * @see #isSortHeader()
     */
    public void setSortHeader(boolean sortHeader) {
        this.sortHeader = sortHeader;
        this.sortHeader_set = true;
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

    // valign
    private String valign = null;

    /**
 * <p>Vertical alignment (top, middle, bottom) for the content of each cell in the column</p>
     */
    public String getValign() {
        if (this.valign != null) {
            return this.valign;
        }
        ValueBinding _vb = getValueBinding("valign");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Vertical alignment (top, middle, bottom) for the content of each cell in the column</p>
     * @see #getValign()
     */
    public void setValign(String valign) {
        this.valign = valign;
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

    // width
    private String width = null;

    /**
 * <p>Set the width of the column in either pixels or percent(deprecated in HTML 4.0)</p>
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
 * <p>Set the width of the column in either pixels or percent(deprecated in HTML 4.0)</p>
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
        this.abbr = (String) _values[1];
        this.align = (String) _values[2];
        this.axis = (String) _values[3];
        this.bgColor = (String) _values[4];
        this._char = (String) _values[5];
        this.charOff = (String) _values[6];
        this.colSpan = ((Integer) _values[7]).intValue();
        this.colSpan_set = ((Boolean) _values[8]).booleanValue();
        this.extraHtml = (String) _values[9];
        this.groupHeader = ((Boolean) _values[10]).booleanValue();
        this.groupHeader_set = ((Boolean) _values[11]).booleanValue();
        this.headers = (String) _values[12];
        this.height = (String) _values[13];
        this.noWrap = ((Boolean) _values[14]).booleanValue();
        this.noWrap_set = ((Boolean) _values[15]).booleanValue();
        this.onClick = (String) _values[16];
        this.onDblClick = (String) _values[17];
        this.onKeyDown = (String) _values[18];
        this.onKeyPress = (String) _values[19];
        this.onKeyUp = (String) _values[20];
        this.onMouseDown = (String) _values[21];
        this.onMouseMove = (String) _values[22];
        this.onMouseOut = (String) _values[23];
        this.onMouseOver = (String) _values[24];
        this.onMouseUp = (String) _values[25];
        this.rowSpan = ((Integer) _values[26]).intValue();
        this.rowSpan_set = ((Boolean) _values[27]).booleanValue();
        this.scope = (String) _values[28];
        this.selectHeader = ((Boolean) _values[29]).booleanValue();
        this.selectHeader_set = ((Boolean) _values[30]).booleanValue();
        this.sortHeader = ((Boolean) _values[31]).booleanValue();
        this.sortHeader_set = ((Boolean) _values[32]).booleanValue();
        this.style = (String) _values[33];
        this.styleClass = (String) _values[34];
        this.toolTip = (String) _values[35];
        this.valign = (String) _values[36];
        this.visible = ((Boolean) _values[37]).booleanValue();
        this.visible_set = ((Boolean) _values[38]).booleanValue();
        this.width = (String) _values[39];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[40];
        _values[0] = super.saveState(_context);
        _values[1] = this.abbr;
        _values[2] = this.align;
        _values[3] = this.axis;
        _values[4] = this.bgColor;
        _values[5] = this._char;
        _values[6] = this.charOff;
        _values[7] = new Integer(this.colSpan);
        _values[8] = this.colSpan_set ? Boolean.TRUE : Boolean.FALSE;
        _values[9] = this.extraHtml;
        _values[10] = this.groupHeader ? Boolean.TRUE : Boolean.FALSE;
        _values[11] = this.groupHeader_set ? Boolean.TRUE : Boolean.FALSE;
        _values[12] = this.headers;
        _values[13] = this.height;
        _values[14] = this.noWrap ? Boolean.TRUE : Boolean.FALSE;
        _values[15] = this.noWrap_set ? Boolean.TRUE : Boolean.FALSE;
        _values[16] = this.onClick;
        _values[17] = this.onDblClick;
        _values[18] = this.onKeyDown;
        _values[19] = this.onKeyPress;
        _values[20] = this.onKeyUp;
        _values[21] = this.onMouseDown;
        _values[22] = this.onMouseMove;
        _values[23] = this.onMouseOut;
        _values[24] = this.onMouseOver;
        _values[25] = this.onMouseUp;
        _values[26] = new Integer(this.rowSpan);
        _values[27] = this.rowSpan_set ? Boolean.TRUE : Boolean.FALSE;
        _values[28] = this.scope;
        _values[29] = this.selectHeader ? Boolean.TRUE : Boolean.FALSE;
        _values[30] = this.selectHeader_set ? Boolean.TRUE : Boolean.FALSE;
        _values[31] = this.sortHeader ? Boolean.TRUE : Boolean.FALSE;
        _values[32] = this.sortHeader_set ? Boolean.TRUE : Boolean.FALSE;
        _values[33] = this.style;
        _values[34] = this.styleClass;
        _values[35] = this.toolTip;
        _values[36] = this.valign;
        _values[37] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[38] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        _values[39] = this.width;
        return _values;
    }

}

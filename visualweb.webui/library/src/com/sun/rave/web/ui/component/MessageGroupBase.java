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
 * Use the <code>ui:messageGroup</code> tag to display a list of messages that are not (necessarily) associated with any single component.<br>
 * <h3>HTML Elements and Layout</h3>
 * A message group consists of a list of messages. <br>
 * <h3>Client Side Javascript Functions</h3>
 * none.<br>
 * <h3>Examples</h3>
 * <b>Example 1: An example showing how to show a list of global errors at the top of the page:</b> <br>
 * <code><br>
 * &lt;ui:messageGroup showGlobalOnly="true"/&gt;<br>
 * <br>
 * &lt;ui:staticText text="Validator checks that the value is between 0-10 inclusive. /&gt;<br>
 * &lt;ui:message for="form1:textTest1"/&gt;<br>
 * <br>
 * &lt;ui:field id="textTest1" label="Enter a number:" 
 * text="#{FieldTest.number}" validator="#{FieldTest.checkNumber}"/&gt;<br>
 * </code>
 * <br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class MessageGroupBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>MessageGroupBase</code>.</p>
     */
    public MessageGroupBase() {
        super();
        setRendererType("com.sun.rave.web.ui.MessageGroup");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.MessageGroup";
    }

    // showDetail
    private boolean showDetail = false;
    private boolean showDetail_set = false;

    /**
 * <p>Flag to determine whether to show the detail message.</p>
     */
    public boolean isShowDetail() {
        if (this.showDetail_set) {
            return this.showDetail;
        }
        ValueBinding _vb = getValueBinding("showDetail");
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
 * <p>Flag to determine whether to show the detail message.</p>
     * @see #isShowDetail()
     */
    public void setShowDetail(boolean showDetail) {
        this.showDetail = showDetail;
        this.showDetail_set = true;
    }

    // showGlobalOnly
    private boolean showGlobalOnly = false;
    private boolean showGlobalOnly_set = false;

    /**
 * <p>Flag to determine whether to show the global message.</p>
     */
    public boolean isShowGlobalOnly() {
        if (this.showGlobalOnly_set) {
            return this.showGlobalOnly;
        }
        ValueBinding _vb = getValueBinding("showGlobalOnly");
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
 * <p>Flag to determine whether to show the global message.</p>
     * @see #isShowGlobalOnly()
     */
    public void setShowGlobalOnly(boolean showGlobalOnly) {
        this.showGlobalOnly = showGlobalOnly;
        this.showGlobalOnly_set = true;
    }

    // showSummary
    private boolean showSummary = false;
    private boolean showSummary_set = false;

    /**
 * <p>Flag to determine whether to show the summary message.</p>
     */
    public boolean isShowSummary() {
        if (this.showSummary_set) {
            return this.showSummary;
        }
        ValueBinding _vb = getValueBinding("showSummary");
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
 * <p>Flag to determine whether to show the summary message.</p>
     * @see #isShowSummary()
     */
    public void setShowSummary(boolean showSummary) {
        this.showSummary = showSummary;
        this.showSummary_set = true;
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

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.showDetail = ((Boolean) _values[1]).booleanValue();
        this.showDetail_set = ((Boolean) _values[2]).booleanValue();
        this.showGlobalOnly = ((Boolean) _values[3]).booleanValue();
        this.showGlobalOnly_set = ((Boolean) _values[4]).booleanValue();
        this.showSummary = ((Boolean) _values[5]).booleanValue();
        this.showSummary_set = ((Boolean) _values[6]).booleanValue();
        this.style = (String) _values[7];
        this.styleClass = (String) _values[8];
        this.toolTip = (String) _values[9];
        this.visible = ((Boolean) _values[10]).booleanValue();
        this.visible_set = ((Boolean) _values[11]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[12];
        _values[0] = super.saveState(_context);
        _values[1] = this.showDetail ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.showDetail_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.showGlobalOnly ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.showGlobalOnly_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.showSummary ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.showSummary_set ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.style;
        _values[8] = this.styleClass;
        _values[9] = this.toolTip;
        _values[10] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[11] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}

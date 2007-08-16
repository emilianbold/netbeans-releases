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
 * Use the <code>ui:alert</code>
 * tag to display an inline alert
 * message at the top of the rendered HTML page. Inline alert messages
 * permit users to correct problems or proceed with their work without
 * having to dismiss a window and navigate to a new location.&nbsp;
 * There 2 facets associated with an alert tag: <br>
 * &nbsp;&nbsp;&nbsp; alertImage - allows the developer to put
 * in their own image and/or text<br>
 * &nbsp;&nbsp;&nbsp; alertLink - allows the developer to put
 * in their own formatted link or extra text.<br>
 * <h3>HTML Elements and Layout</h3>
 * An alert message consists of an icon depicting the type of alert -
 * information, success, warning, or error - along with a <code>summary</code>
 * message and an optional <code>detail</code>
 * message.
 * <h3>Client Side Javascript
 * Functions</h3>
 * none.
 * <h3>Examples</h3>
 * <b>Example 1: An example showing
 * an informational alert:</b> <br>
 * <code>&lt;ui:alert id="msg1"
 * type="information" summary="Patch Installed Successfully" detail="Patch
 * 9997-01 was successfully installed on host alpha, beta and zed."
 * /&gt;<br>
 * </code>
 * <br>
 * <b>Example 2: An example showing
 * an error alert with an alert link:</b><br>
 * <code>&lt;ui:alert id="msg2"
 * type="error" summary="Patch Installation Failed!"
 * detail="Patch 9997-01 was not installed on host alpha, beta and zed."
 * linkText="View Log" linkURL="/log/patch-log.txt" linkTarget="_blank"
 * linkToolTip="Open Window Containing Error Log"/&gt;<br>
 * <br>
 * </code><b>Example
 * 3: An example showing the use of an alertImage facet:</b><br>
 * <code>&lt;ui:alert id="msg2"
 * type="error" summary="Patch Installation Failed!"
 * detail="Patch 9997-01 was not installed on host alpha, beta and
 * zed."&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;f:facet name="alertImage"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp; &lt;ui:image
 * id="foo" </code><code>url="../images/foo.gif"
 * /&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:facet&gt;<br>
 * &lt;/ui:alert&gt;<br>
 * </code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class AlertBase extends javax.faces.component.UIOutput {

    /**
     * <p>Construct a new <code>AlertBase</code>.</p>
     */
    public AlertBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Alert");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Alert";
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

    // detail
    private String detail = null;

    /**
 * <p>Optional detailed message text for the alert. This message might include more information about the alert and instructions for what to do about the alert.</p>
     */
    public String getDetail() {
        if (this.detail != null) {
            return this.detail;
        }
        ValueBinding _vb = getValueBinding("detail");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Optional detailed message text for the alert. This message might include more information about the alert and instructions for what to do about the alert.</p>
     * @see #getDetail()
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    // linkAction
    private javax.faces.el.MethodBinding linkAction = null;

    /**
 * <p>Method binding representing a method that receives action from the 
 *         embedded hyperlink component.</p>
     */
    public javax.faces.el.MethodBinding getLinkAction() {
        if (this.linkAction != null) {
            return this.linkAction;
        }
        ValueBinding _vb = getValueBinding("linkAction");
        if (_vb != null) {
            return (javax.faces.el.MethodBinding) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Method binding representing a method that receives action from the 
 *         embedded hyperlink component.</p>
     * @see #getLinkAction()
     */
    public void setLinkAction(javax.faces.el.MethodBinding linkAction) {
        this.linkAction = linkAction;
    }

    // linkTarget
    private String linkTarget = null;

    /**
 * <p>The window (target) in which to load the link that is specified with linkText.</p>
     */
    public String getLinkTarget() {
        if (this.linkTarget != null) {
            return this.linkTarget;
        }
        ValueBinding _vb = getValueBinding("linkTarget");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The window (target) in which to load the link that is specified with linkText.</p>
     * @see #getLinkTarget()
     */
    public void setLinkTarget(String linkTarget) {
        this.linkTarget = linkTarget;
    }

    // linkText
    private String linkText = null;

    /**
 * <p>The text for an optional link that is appended to the detail message.</p>
     */
    public String getLinkText() {
        if (this.linkText != null) {
            return this.linkText;
        }
        ValueBinding _vb = getValueBinding("linkText");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The text for an optional link that is appended to the detail message.</p>
     * @see #getLinkText()
     */
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    // linkToolTip
    private String linkToolTip = null;

    /**
 * <p>Sets the value of the title attribute for the HTML element. The specified text
 *         will display as a tooltip if the mouse cursor hovers over the link that is specified
 *         with linkText.</p>
     */
    public String getLinkToolTip() {
        if (this.linkToolTip != null) {
            return this.linkToolTip;
        }
        ValueBinding _vb = getValueBinding("linkToolTip");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Sets the value of the title attribute for the HTML element. The specified text
 *         will display as a tooltip if the mouse cursor hovers over the link that is specified
 *         with linkText.</p>
     * @see #getLinkToolTip()
     */
    public void setLinkToolTip(String linkToolTip) {
        this.linkToolTip = linkToolTip;
    }

    // linkURL
    private String linkURL = null;

    /**
 * <p>Absolute, relative, or context relative (starting with "/") URL to the resource to navigate
 *         to when the link that is specified with linkText is selected.</p>
     */
    public String getLinkURL() {
        if (this.linkURL != null) {
            return this.linkURL;
        }
        ValueBinding _vb = getValueBinding("linkURL");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Absolute, relative, or context relative (starting with "/") URL to the resource to navigate
 *         to when the link that is specified with linkText is selected.</p>
     * @see #getLinkURL()
     */
    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
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

    // summary
    private String summary = null;

    /**
 * <p>Summary message text for the alert. This brief message is prominently 
 *          displayed next to the icon.</p>
     */
    public String getSummary() {
        if (this.summary != null) {
            return this.summary;
        }
        ValueBinding _vb = getValueBinding("summary");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Summary message text for the alert. This brief message is prominently 
 *          displayed next to the icon.</p>
     * @see #getSummary()
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    // tabIndex
    private int tabIndex = Integer.MIN_VALUE;
    private boolean tabIndex_set = false;

    /**
 * <p>The position of this component in the tabbing order sequence</p>
     */
    public int getTabIndex() {
        if (this.tabIndex_set) {
            return this.tabIndex;
        }
        ValueBinding _vb = getValueBinding("tabIndex");
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
 * <p>The position of this component in the tabbing order sequence</p>
     * @see #getTabIndex()
     */
    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
        this.tabIndex_set = true;
    }

    // type
    private String type = null;

    /**
 * <p>The type or category of alert. This type can be set to either "information", "success", "warning" or "error".</p>
     */
    public String getType() {
        if (this.type != null) {
            return this.type;
        }
        ValueBinding _vb = getValueBinding("type");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The type or category of alert. This type can be set to either "information", "success", "warning" or "error".</p>
     * @see #getType()
     */
    public void setType(String type) {
        this.type = type;
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
        this.alt = (String) _values[1];
        this.detail = (String) _values[2];
        this.linkAction = (javax.faces.el.MethodBinding) restoreAttachedState(_context, _values[3]);
        this.linkTarget = (String) _values[4];
        this.linkText = (String) _values[5];
        this.linkToolTip = (String) _values[6];
        this.linkURL = (String) _values[7];
        this.style = (String) _values[8];
        this.styleClass = (String) _values[9];
        this.summary = (String) _values[10];
        this.tabIndex = ((Integer) _values[11]).intValue();
        this.tabIndex_set = ((Boolean) _values[12]).booleanValue();
        this.type = (String) _values[13];
        this.visible = ((Boolean) _values[14]).booleanValue();
        this.visible_set = ((Boolean) _values[15]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[16];
        _values[0] = super.saveState(_context);
        _values[1] = this.alt;
        _values[2] = this.detail;
        _values[3] = saveAttachedState(_context, linkAction);
        _values[4] = this.linkTarget;
        _values[5] = this.linkText;
        _values[6] = this.linkToolTip;
        _values[7] = this.linkURL;
        _values[8] = this.style;
        _values[9] = this.styleClass;
        _values[10] = this.summary;
        _values[11] = new Integer(this.tabIndex);
        _values[12] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.type;
        _values[14] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[15] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}

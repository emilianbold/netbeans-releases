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
 * the <code>ui:pageAlert</code>
 * tag to display a full page alert. A page alert differs from the
 * inline alert (see </span><a
 *  href="file:///Users/smorgan/syncdocs/ui/alert.html"
 *  style="color: rgb(0, 0, 0);">ui:alert</a><span
 *  style="color: rgb(0, 0, 0);">) in that the content of
 * the page
 * that invokes the pageAlert is replaced by the alert page. An inline
 * alert is a smaller alert that is inserted in the page that invokes the
 * alert. </span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout</h3>
 * <span style="color: rgb(0, 0, 0);">A
 * page alert consists of:
 * </span>
 * <ul style="color: rgb(0, 0, 0);">
 *   <li><span
 *  style="text-decoration: line-through; font-weight: bold;"></span>an
 * icon depicting the type of alert - question, information,
 * warning, or error</li>
 *   <li>a page title next to the icon</li>
 *   <li>a summary message</li>
 *   <li>a detailed message</li>
 *   <li>one input component - (one text
 * field, one text area, one checkbox,
 * one set of radio buttons, one drop-down menu, or one scrolling list)</li>
 *   <li>a page separator line<br>
 *   </li>
 *   <li>a set of page level buttons</li>
 * </ul>
 * <p style="color: rgb(0, 0, 0);"><br>
 * <span style="color: rgb(0, 0, 0);">The input field and the set of
 * buttons are optional items, which must be specified with facets. </span><br>
 * </p>
 * <p style="color: rgb(0, 0, 0);">The
 * following diagram shows
 * the locations of each of the page alert areas, and the facets that are
 * supported for specified areas.</p>
 * <table style="color: rgb(0, 0, 0);"
 *  border="1" width="100%">
 *   <tbody>
 *     <tr>
 *       <td width="100%">Alert
 * Icon (or optional <code>pageAlertImage</code> facet), Page Title Text (or 
 * optional <code>pageAlertTitle</code> facet)</td>
 *     </tr>
 *     <tr>
 *       <td colspan="2" width="100%">Alert
 * summary<span
 *  style="text-decoration: line-through;"></span></td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top;">Detailed
 * message<br>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td colspan="2" width="100%">Optional
 *       <code>pageAlertInput</code>
 * facet<br>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top;">Page separator (or optional
 *       <code>pageAlertSeparator</code>
 * facet)<br>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td colspan="2" align="right"
 *  width="100%">Optional <code>pageAlertButtons </code>facet</td>
 *     </tr>
 *   </tbody>
 * </table>
 * <p style="color: rgb(0, 0, 0);">&nbsp;</p>
 * <h3 style="color: rgb(0, 0, 0);">Facets</h3>
 * <span style="color: rgb(0, 0, 0);">The
 * <code>ui:pageAlert</code>
 * tag supports the following facets.</span><br>
 * <br>
 * <table style="text-align: left; width: 100%;"
 *  border="1" cellpadding="2" cellspacing="2">
 *   <tbody>
 *     <tr>
 *       <td style="vertical-align: top;"><code
 *  style="color: rgb(0, 0, 0);">pageAlertTitle</code></td>
 *       <td
 *  style="vertical-align: top; color: rgb(0, 0, 0);">Specifies
 * a custom component to use to display the title of the alert.<span
 *  style="color: rgb(255, 153, 0);"></span><br>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top;"><code
 *  style="color: rgb(0, 0, 0);">pageAlertInput</code></td>
 *       <td
 *  style="vertical-align: top; color: rgb(0, 0, 0);">Specifies
 * a component to use in the body of the full page alert. This facet can be
 * used to display an input component
 *  for the user to perform tasks related
 * to the alert, for example.<br>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top;"><code
 *  style="color: rgb(0, 0, 0);">pageAlertButtons</code></td>
 *       <td
 *  style="vertical-align: top; color: rgb(0, 0, 0);">Specifies
 * components to use for the buttons at the bottom of the alert page. This
 * facet can be used to display a back button, for example. If you want to specify more than
 * one button, you might find it helpful to enclose the button components
 * in a <code>ui:panelGroup</code> component.<br>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td
 *  style="vertical-align: top; color: rgb(0, 0, 0);"><code>pageAlertSeparator<br>
 *       </code></td>
 *       <td
 *  style="vertical-align: top; color: rgb(0, 0, 0);">Specifies
 * a component to use for the page separator that is displayed above the
 * page buttons. The component included in this facet replaces the default
 * page separator.<br>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td
 *  style="vertical-align: top; color: rgb(0, 0, 0);"><code>pageAlertImage<br>
 *       </code></td>
 *       <td
 *  style="vertical-align: top; color: rgb(0, 0, 0);">Specifies
 * a component to use to display the alert icon. The component included
 * in this facet replaces the default icon.<br>
 *       </td>
 *     </tr>
 *   </tbody>
 * </table>
 * <br>
 * <h3>Client Side Javascript
 * Functions</h3>
 * None. <span style="color: rgb(255, 153, 0);"></span><br>
 * <h3>Examples</h3>
 * <h4><span
 *  style="color: rgb(0, 0, 0);">Example 1:</span>&nbsp;
 * Simple example, with alert message and a back button.</h4>
 * <code></code>
 * <pre>	&lt;ui:pageAlert id="pagealert" title="Error!" type="error"<br>            summary="Server Not Responding." <br>            detail="The server jurassic is not responding. Verify that the power cable is connected."&gt;<br>	  &lt;f:facet name="pageAlertButtons"&gt;<br>              &lt;ui:button text="Back" action="indexPage" /&gt;<br>	&lt;/ui:pageAlert&gt;</pre>
 * <h4><span style="color: rgb(255, 153, 0);"></span><span
 *  style="color: rgb(0, 0, 0);">Example 2:</span>
 * With
 * input field and one page button.</h4><code></code>
 * <pre>	&lt;ui:pageAlert id="pagealert" title="Password Expired!" type="warning"<br>            summary="Password expired." <br>            detail="Your password has expired. Enter a new password"&gt;<br>	  &lt;f:facet name="pageAlertButtons"&gt;<br>              &lt;ui:button text="Go to Login Page" action="success" /&gt; <br>          &lt;/f:facet&gt;<br>          &lt;f:facet name="pageAlertInput"&gt;<br>            &lt;ui:panelGroup id="pageAlertStuff"&gt;<br>              &lt;ui:label text="New Password:" for="passwordField"/&gt;<br>              &lt;ui:field id="passwordField" type="password"/&gt;<br>            &lt;/ui:panelGroup&gt;<br>          &lt;/f:facet&gt; <br>	&lt;/ui:pageAlert&gt;<br></pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class PageAlertBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>PageAlertBase</code>.</p>
     */
    public PageAlertBase() {
        super();
        setRendererType("com.sun.rave.web.ui.PageAlert");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.PageAlert";
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

    // escape
    private boolean escape = false;
    private boolean escape_set = false;

    /**
 * <p>Flag indicating that the message text should be escaped so that it is 
 *         not interpreted by the browser.</p>
     */
    public boolean isEscape() {
        if (this.escape_set) {
            return this.escape;
        }
        ValueBinding _vb = getValueBinding("escape");
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
 * <p>Flag indicating that the message text should be escaped so that it is 
 *         not interpreted by the browser.</p>
     * @see #isEscape()
     */
    public void setEscape(boolean escape) {
        this.escape = escape;
        this.escape_set = true;
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
 * <p>Summary message text for the alert. This brief message is displayed under the page alert title.</p>
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
 * <p>Summary message text for the alert. This brief message is displayed under the page alert title.</p>
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

    // title
    private String title = null;

    /**
 * <p>The text to display as the page title</p>
     */
    public String getTitle() {
        if (this.title != null) {
            return this.title;
        }
        ValueBinding _vb = getValueBinding("title");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The text to display as the page title</p>
     * @see #getTitle()
     */
    public void setTitle(String title) {
        this.title = title;
    }

    // type
    private String type = null;

    /**
 * <p>The type or category of alert. The type attribute can be set to one of the following:  "question", "information", "warning" or "error". The default type is error.</p>
     */
    public String getType() {
        if (this.type != null) {
            return this.type;
        }
        ValueBinding _vb = getValueBinding("type");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return "error";
    }

    /**
 * <p>The type or category of alert. The type attribute can be set to one of the following:  "question", "information", "warning" or "error". The default type is error.</p>
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
        this.escape = ((Boolean) _values[3]).booleanValue();
        this.escape_set = ((Boolean) _values[4]).booleanValue();
        this.style = (String) _values[5];
        this.styleClass = (String) _values[6];
        this.summary = (String) _values[7];
        this.tabIndex = ((Integer) _values[8]).intValue();
        this.tabIndex_set = ((Boolean) _values[9]).booleanValue();
        this.title = (String) _values[10];
        this.type = (String) _values[11];
        this.visible = ((Boolean) _values[12]).booleanValue();
        this.visible_set = ((Boolean) _values[13]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[14];
        _values[0] = super.saveState(_context);
        _values[1] = this.alt;
        _values[2] = this.detail;
        _values[3] = this.escape ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.escape_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.style;
        _values[6] = this.styleClass;
        _values[7] = this.summary;
        _values[8] = new Integer(this.tabIndex);
        _values[9] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.title;
        _values[11] = this.type;
        _values[12] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
